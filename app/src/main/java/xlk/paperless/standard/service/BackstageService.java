package xlk.paperless.standard.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceBullet;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceDownload;
import com.mogujie.tt.protobuf.InterfaceFilescorevote;
import com.mogujie.tt.protobuf.InterfaceIM;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfacePlaymedia;
import com.mogujie.tt.protobuf.InterfaceStream;
import com.mogujie.tt.protobuf.InterfaceUpload;
import com.mogujie.tt.protobuf.InterfaceWhiteboard;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cc.shinichi.library.ImagePreview;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.WpsModel;
import xlk.paperless.standard.data.bean.ChatMessage;
import xlk.paperless.standard.data.bean.JsonBean;
import xlk.paperless.standard.receiver.WpsReceiver;
import xlk.paperless.standard.util.AppUtil;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.meet.MeetingActivity;
import xlk.paperless.standard.view.notice.BulletinActivity;
import xlk.paperless.standard.view.score.ScoreActivity;
import xlk.paperless.standard.view.video.VideoActivity;

import static xlk.paperless.standard.data.Values.isMandatoryPlaying;
import static xlk.paperless.standard.view.draw.DrawPresenter.disposePicOpermemberid;
import static xlk.paperless.standard.view.draw.DrawPresenter.disposePicSrcmemid;
import static xlk.paperless.standard.view.draw.DrawPresenter.disposePicSrcwbidd;
import static xlk.paperless.standard.view.draw.DrawPresenter.isSharing;
import static xlk.paperless.standard.view.draw.DrawPresenter.tempPicData;
import static xlk.paperless.standard.view.meet.MeetingActivity.chatIsShowing;
import static xlk.paperless.standard.view.meet.MeetingActivity.mBadge;

/**
 * @author xlk
 * @date 2020/3/11
 * @desc 后台服务
 */
public class BackstageService extends Service {

    private final String TAG = "BackstageService-->";
    private JniHandler jni = JniHandler.getInstance();
    /**
     * 监听WPS广播
     */
    private WpsReceiver receiver;
    List<String> picPath = new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //平台下载
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DOWNLOAD_VALUE:
                downloadInform(msg);
                break;
            //上传进度通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_UPLOAD_VALUE:
                uploadInform(msg);
                break;
            //处理WPS广播监听
            case Constant.BUS_WPS_RECEIVER:
                boolean isopen = (boolean) msg.getObjects()[0];
                if (isopen) {
                    registerWpsBroadCase();
                } else {
                    unregisterWpsBroadCase();
                }
                break;
            //参会人权限变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBERPERMISSION_VALUE:
                InterfaceMember.pbui_Type_MemberPermission o = jni.queryAttendPeoplePermissions();
                Values.allPermissions = o.getItemList();
                for (int i = 0; i < Values.allPermissions.size(); i++) {
                    InterfaceMember.pbui_Item_MemberPermission item = Values.allPermissions.get(i);
                    if (item.getMemberid() == Values.localMemberId) {
                        Values.localPermission = item.getPermission();
                        break;
                    }
                }
                break;
            //会议交流
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETIM_VALUE:
                if (mBadge != null) {
                    if (!chatIsShowing) {
                        byte[] o1 = (byte[]) msg.getObjects()[0];
                        InterfaceIM.pbui_Type_MeetIM meetIM = InterfaceIM.pbui_Type_MeetIM.parseFrom(o1);
                        if (meetIM.getMsgtype() == 0) {
                            //文本类消息
                            int badgeNumber = mBadge.getBadgeNumber();
                            MeetingActivity.chatMessages.add(new ChatMessage(0, meetIM));
                            mBadge.setBadgeNumber(++badgeNumber);
                        }
                    }
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD_VALUE:
                //添加图片通知
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDPICTURE_VALUE) {
                    byte[] o1 = (byte[]) msg.getObjects()[0];
                    addPicInform(o1);
                }
                break;
            //媒体播放通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY_VALUE:
                mediaPlayInform(msg);
                break;
            //流播放通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_STREAMPLAY_VALUE:
                streamPlayInform(msg);
                break;
            //设备控制
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICECONTROL_VALUE:
                deviceControlInform(msg);
                break;
            //公告
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET_VALUE:
                byte[] bulletin = (byte[]) msg.getObjects()[0];
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_PUBLIST_VALUE) {
                    LogUtil.i(TAG, "发布公告通知");
                    InterfaceBullet.pbui_BulletDetailInfo detailInfo = InterfaceBullet.pbui_BulletDetailInfo.parseFrom(bulletin);
                    List<InterfaceBullet.pbui_Item_BulletDetailInfo> itemList = detailInfo.getItemList();
                    if (!itemList.isEmpty()) {
                        InterfaceBullet.pbui_Item_BulletDetailInfo info = itemList.get(0);
                        int bulletid = info.getBulletid();
                        BulletinActivity.jump(bulletid, this);
                    }
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FILESCOREVOTE_VALUE:
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_START_VALUE) {
                    LogUtil.i(TAG, "BusEvent -->" + "自定义文件评分投票  发起");
                    byte[] o1 = (byte[]) msg.getObjects()[0];
                    InterfaceFilescorevote.pbui_Type_StartUserDefineFileScoreNotify info = InterfaceFilescorevote.pbui_Type_StartUserDefineFileScoreNotify.parseFrom(o1);
                    int voteid = info.getVoteid();
                    LogUtil.i(TAG, "BusEvent -->" + "收到发起文件自定义选项评分 voteid= " + voteid);
                    startActivity(new Intent(this, ScoreActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra(Constant.EXTRA_VOTE_ID, voteid));
                }
                break;
//            case Constant.BUS_PREVIEW_IMAGE:
//                String filepath = (String) msg.getObjs()[0];
//                int index;
//                if (!picPath.contains(filepath)) {
//                    picPath.add(filepath);
//                    index = picPath.size() - 1;
//                } else {
//                    index = picPath.indexOf(filepath);
//                }
//                previewImage(index);
//                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DBSERVERERROR_VALUE: {
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_Type_MeetDBServerOperError info = InterfaceBase.pbui_Type_MeetDBServerOperError.parseFrom(bytes);
                int type = info.getType();
                int method = info.getMethod();
                // InterfaceMacro#Pb_DB_StatusCode
                int status = info.getStatus();
                LogUtil.i(TAG, "onEventMessage 数据后台回复的错误信息 type=" + type + ",method=" + method + ",status=" + status);
                operateResult(type, method, status);
                break;
            }
            //收到远程配置的通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE: {
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceDevice.pbui_Type_MeetRemoteSetNotify info = InterfaceDevice.pbui_Type_MeetRemoteSetNotify.parseFrom(bytes);
                int deviceid = info.getDeviceid();
                String jsonText = info.getJsontext().toStringUtf8();
                JsonBean jsonBean = JSON.parseObject(jsonText, JsonBean.class);
                if (jsonBean == null) {
                    break;
                }
                int restart = jsonBean.getRestart();
                LogUtil.i(TAG, "onEventMessage restart=" + restart + ",deviceid=" + deviceid);
                List<JsonBean.ItemBean> item = jsonBean.getItem();
                for (int i = 0; i < item.size(); i++) {
                    JsonBean.ItemBean itemBean = item.get(i);
                    LogUtil.i(TAG, "收到远程配置的通知 section=" + itemBean.getSection() + ",key=" + itemBean.getKey() + ",value=" + itemBean.getValue());
                }
            }
            default:
                break;
        }
    }

    private void operateResult(int type, int method, int status) {
        switch (status) {
            //单条查询记录
            case InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_SINGLERECORDD_VALUE: {
                break;
            }
            //无返回记录
            case InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_NORECORED_VALUE: {
                break;
            }
            //成功
            case InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_DONE_VALUE: {
                if (type == InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ADMIN_VALUE && method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_LOGON_VALUE) {
                    ToastUtil.show(R.string.modify_password_successful);
                }
                break;
            }
            //请求失败
            case InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_FAIL_VALUE: {
                if (type == InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ADMIN_VALUE && method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_LOGON_VALUE) {
                    ToastUtil.show(R.string.login_request_failed);
                }
                break;
            }
            //数据库异常
            case InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_EXCPT_DB_VALUE: {
                ToastUtil.show(R.string.database_exception);
                break;
            }
            //服务器异常
            case InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_EXCPT_SV_VALUE: {
                ToastUtil.show(R.string.server_exception);
                break;
            }
            //权限限制
            case InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_ACCESSDENIED_VALUE: {
                ToastUtil.show(R.string.no_permission);
                break;
            }
            //密码错误
            case InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_PSWFAILED_VALUE: {
                ToastUtil.show(R.string.login_error_5);
                break;
            }
            //创建会议有冲突
            case InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_COLL_MEETING_VALUE: {
                ToastUtil.show(R.string.conflict_creating_meeting);
                break;
            }
            //参数错误，不应该为0
            case InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_PARAMETERZERO_VALUE: {

                break;
            }
            //不存在的数据
            case InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_NOTEXIST_VALUE: {

                break;
            }
            //协议版本不区配
            case InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_PROTOLDISMATCH_VALUE: {

                break;
            }
            default:
                //多条查询记录
                break;
        }
    }

    private void previewImage(int index) {
        if (picPath.isEmpty()) {
            return;
        }
        ImagePreview.getInstance()
                .setContext(this)
                //设置图片地址集合
                .setImageList(picPath)
                //设置开始的索引
                .setIndex(index)
                //设置是否显示下载按钮
                .setShowDownButton(false)
                //设置是否显示关闭按钮
                .setShowCloseButton(false)
                //设置是否开启下拉图片退出
                .setEnableDragClose(true)
                //设置是否开启上拉图片退出
                .setEnableUpDragClose(true)
                //设置是否开启点击图片退出
                .setEnableClickClose(true)
                .setShowErrorToast(true)
                .start();
    }

    private void registerWpsBroadCase() {
        if (receiver == null) {
            receiver = new WpsReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(WpsModel.Reciver.ACTION_SAVE);
            filter.addAction(WpsModel.Reciver.ACTION_CLOSE);
            filter.addAction(WpsModel.Reciver.ACTION_HOME);
//            filter.addAction(WpsModel.Reciver.ACTION_BACK);
            registerReceiver(receiver, filter);
        }
    }

    private void unregisterWpsBroadCase() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void deviceControlInform(EventMessage msg) throws InvalidProtocolBufferException {
        byte[] o = (byte[]) msg.getObjects()[0];
        InterfaceDevice.pbui_Type_DeviceControl object = InterfaceDevice.pbui_Type_DeviceControl.parseFrom(o);
        int oper = object.getOper();//enum Pb_DeviceControlFlag
        int operval1 = object.getOperval1();//操作对应的参数 如更换主界面的媒体ID
        int operval2 = object.getOperval2();//操作对应的参数
        if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_MODIFYLOGO.getNumber()) {
            LogUtil.i(TAG, "deviceControl: 更换Logo通知");
            //本地没有才下载
//            FileUtil.createDir(Constant.configuration_picture_dir);
//            jni.creationFileDownload(Constant.configuration_picture_dir + Constant.MAIN_LOGO_PNG_TAG + ".png", operval1, 1, 0, Constant.MAIN_LOGO_PNG_TAG);
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_SHUTDOWN.getNumber()) {//关机
            LogUtil.i(TAG, "deviceControl: 关机");
            createSuProcess("reboot -p");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_REBOOT.getNumber()) {//重启
            LogUtil.i(TAG, "deviceControl: 重启");
            createSuProcess("reboot");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_PROGRAMRESTART.getNumber()) {//重启软件
            LogUtil.i(TAG, "deviceControl: 重启软件");
            AppUtil.restartApplication(getApplicationContext());
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTUP.getNumber()) {//升
            LogUtil.i(TAG, "deviceControl: 升");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTDOWN.getNumber()) {//降
            LogUtil.i(TAG, "deviceControl: 降");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTSTOP.getNumber()) {//停止升（降）
            LogUtil.i(TAG, "deviceControl: 停止升(降)");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_MODIFYMAINBG.getNumber()) {//更换主界面
            LogUtil.i(TAG, "deviceControl: 更换主界面");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_MODIFYPROJECTBG.getNumber()) {//更换投影界面
            LogUtil.i(TAG, "deviceControl: 更换投影界面");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_MODIFYSUBBG.getNumber()) {//更换子界面
            LogUtil.i(TAG, "deviceControl: 更换子界面");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_MODIFYFONTCOLOR.getNumber()) {//更换字体颜色
            LogUtil.i(TAG, "deviceControl: 更换字体颜色");
        }
    }

    /**
     * 需要root权限
     *
     * @param cmd "reboot -p" 关机; "reboot" 重启
     */
    private void createSuProcess(String cmd) {
        DataOutputStream os = null;
        Process process;
        File rootUser = new File("/system/xbin/ru");
        try {
            if (rootUser.exists()) {
                process = Runtime.getRuntime().exec(rootUser.getAbsolutePath());
            } else {
                process = Runtime.getRuntime().exec("su");
            }
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit $?\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void streamPlayInform(EventMessage msg) throws InvalidProtocolBufferException {
        byte[] datas = (byte[]) msg.getObjects()[0];
        InterfaceStream.pbui_Type_MeetStreamPlay meetStreamPlay = InterfaceStream.pbui_Type_MeetStreamPlay.parseFrom(datas);
        int res = meetStreamPlay.getRes();
        int createdeviceid = meetStreamPlay.getCreatedeviceid();
        LogUtil.i(TAG, "streamPlayInform -->" + "流播放通知 res =" + res);
        if (res == 0) {
            int triggerid = meetStreamPlay.getTriggerid();
            int deviceid = meetStreamPlay.getDeviceid();
            int subid = meetStreamPlay.getSubid();
            int triggeruserval = meetStreamPlay.getTriggeruserval();
            boolean isMandatory = triggeruserval == InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE;
            if (isMandatoryPlaying) {//当前正在被强制播放中
                if (isMandatory) {//收到新的强制性播放
                    LogUtil.i(TAG, "streamPlayInform -->" + "当前属于强制性播放中，收到新的强制流播放播放");
                } else {//收到的不是强制性播放
                    LogUtil.i(TAG, "streamPlayInform -->" + "当前属于强制性播放中，不处理非强制的流播放通知");
                    return;
                }
            }
            if (createdeviceid != Values.localDeviceId) {
                //是否是强制性播放
                isMandatoryPlaying = isMandatory;
                EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_MANDATORY).build());
            }
            EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_HIDE_FAB).build());
            Values.haveNewPlayInform = true;
//            if (!isVideoPlaying) {
            startActivity(new Intent(this, VideoActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    .putExtra(Constant.EXTRA_VIDEO_ACTION, InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_STREAMPLAY_VALUE)
                    .putExtra(Constant.EXTRA_VIDEO_DEVICE_ID, deviceid)
            );
//            } else {
//                if (isMandatoryPlaying) {
//                    EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_MANDATORY).build());
//                }
//            }
        }
//        else if (res == 11) {
//            LogUtil.i(TAG, "streamPlayInform -->" + "会议视屏聊天 isChatingOpened= " + isChatingOpened);
//            if (!isChatingOpened) {
////                startActivity(new Intent(this, ChatVideoActivity.class)
////                        .putExtra(Constant.extra_camrea_res, createdeviceid)
////                        .setFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
//            }else {
//                EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_CHAT_STATE).build());
//            }
//        }
    }

    private void mediaPlayInform(EventMessage msg) throws InvalidProtocolBufferException {
        byte[] datas = (byte[]) msg.getObjects()[0];
        InterfacePlaymedia.pbui_Type_MeetMediaPlay mediaPlay = InterfacePlaymedia.pbui_Type_MeetMediaPlay.parseFrom(datas);
        int res = mediaPlay.getRes();
        LogUtil.i(TAG, "mediaPlayInform -->" + "媒体播放通知 res= " + res);
        if (res != 0) {
            //只处理资源ID为0的播放资源
            return;
        }
        int mediaid = mediaPlay.getMediaid();
        int createdeviceid = mediaPlay.getCreatedeviceid();
        int triggerid = mediaPlay.getTriggerid();
        int triggeruserval = mediaPlay.getTriggeruserval();
        boolean isMandatory = triggeruserval == InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE;
        int type = mediaid & Constant.MAIN_TYPE_BITMASK;
        int subtype = mediaid & Constant.SUB_TYPE_BITMASK;
        if (type == Constant.MEDIA_FILE_TYPE_AUDIO || type == Constant.MEDIA_FILE_TYPE_VIDEO) {
            LogUtil.i(TAG, "mediaPlayInform -->" + "媒体播放通知：isVideoPlaying= " + Values.isVideoPlaying);
            if (createdeviceid != Values.localDeviceId) {
                isMandatoryPlaying = isMandatory;
            }
            Values.haveNewPlayInform = true;
//            if (!isVideoPlaying) {
            startActivity(new Intent(this, VideoActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    .putExtra(Constant.EXTRA_VIDEO_ACTION, InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY_VALUE)
                    .putExtra(Constant.EXTRA_VIDEO_SUBTYPE, subtype)
            );
//            }
        } else {
            LogUtil.i(TAG, "mediaPlayInform -->" + "媒体播放通知：下载文件后打开");
            //创建好下载目录
            FileUtil.createDir(Constant.DIR_DATA_FILE);
            /** **** **  查询该媒体ID的文件名  ** **** **/
            byte[] bytes = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(), mediaid);
            InterfaceBase.pbui_CommonTextProperty pbui_commonTextProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(bytes);
            String fielName = pbui_commonTextProperty.getPropertyval().toStringUtf8();
            String pathname = Constant.DIR_DATA_FILE + fielName;
            File file = new File(pathname);
            if (file.exists()) {
                if (Values.downloadingFiles.contains(mediaid)) {
                    ToastUtil.show(R.string.currently_downloading);
                } else {
                    FileUtil.openFile(this, file);
                }
                return;
            }
            jni.creationFileDownload(pathname, mediaid, 0, 0, Constant.DOWNLOAD_SHOULD_OPEN_FILE);
        }
    }

    private void addPicInform(byte[] datas) throws InvalidProtocolBufferException {
        InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail object = InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail.parseFrom(datas);
        int rPicSrcmemid = object.getSrcmemid();
        long rPicSrcwbid = object.getSrcwbid();
        ByteString rPicData = object.getPicdata();
        int opermemberid = object.getOpermemberid();
        Values.operid = object.getOperid();
        if (!isSharing) {
            if (disposePicOpermemberid == opermemberid && disposePicSrcmemid == rPicSrcmemid
                    && disposePicSrcwbidd == rPicSrcwbid) {
                tempPicData = rPicData;
                disposePicOpermemberid = 0;
                disposePicSrcmemid = 0;
                disposePicSrcwbidd = 0;
            }
        } else {
            EventBus.getDefault().postSticky(new EventMessage.Builder().type(Constant.BUS_SHARE_PIC).objects(object).build());
        }
    }

    private void downloadInform(EventMessage msg) throws InvalidProtocolBufferException {
        byte[] data2 = (byte[]) msg.getObjects()[0];
        InterfaceDownload.pbui_Type_DownloadCb pbui_type_downloadCb = InterfaceDownload.pbui_Type_DownloadCb.parseFrom(data2);
        int mediaid = pbui_type_downloadCb.getMediaid();
        int progress = pbui_type_downloadCb.getProgress();
        int nstate = pbui_type_downloadCb.getNstate();
        int err = pbui_type_downloadCb.getErr();
        String filepath = pbui_type_downloadCb.getPathname().toStringUtf8();
        String userStr = pbui_type_downloadCb.getUserstr().toStringUtf8();
        String fileName = filepath.substring(filepath.lastIndexOf("/") + 1).toLowerCase();
        if (userStr.equals(Constant.DOWNLOAD_NO_INFORM)) {
            LogUtil.i(TAG, "downloadInform 无进度通知的下载 filepath=" + filepath);
            return;
        }
        if (nstate == InterfaceMacro.Pb_Download_State.Pb_STATE_MEDIA_DOWNLOAD_WORKING_VALUE) {
            //主页背景
            if (!userStr.equals(Constant.MAIN_BG_PNG_TAG)
                    //主页logo
                    && !userStr.equals(Constant.MAIN_LOGO_PNG_TAG)
                    //子界面背景
                    && !userStr.equals(Constant.SUB_BG_PNG_TAG)
                    //公告背景
                    && !userStr.equals(Constant.NOTICE_BG_PNG_TAG)
                    //公告logo
                    && !userStr.equals(Constant.NOTICE_LOGO_PNG_TAG)
                    //投影背景
                    && !userStr.equals(Constant.PROJECTIVE_BG_PNG_TAG)
                    //投影logo
                    && !userStr.equals(Constant.PROJECTIVE_LOGO_PNG_TAG)
                    //会场底图
                    && !userStr.equals(Constant.ROOM_BG_PNG_TAG)
                    //下载议程文件
                    && !userStr.equals(Constant.DOWNLOAD_AGENDA_FILE)
                    //归档文件
                    && !userStr.equals(Constant.ARCHIVE_DOWNLOAD_FILE)
                    //归档议程文件
                    && !userStr.equals(Constant.ARCHIVE_AGENDA_FILE)
            ) {
                ToastUtil.show(getString(R.string.file_downloaded_percent, fileName, progress + "%"));
            }
            if (userStr.equals(Constant.ARCHIVE_DOWNLOAD_FILE) || userStr.equals(Constant.ARCHIVE_AGENDA_FILE)) {
                EventBus.getDefault().post(new EventMessage.Builder().type(Constant.ARCHIVE_BUS_DOWNLOAD_FILE).objects(mediaid, fileName, progress).build());
            }
        } else if (nstate == InterfaceMacro.Pb_Download_State.Pb_STATE_MEDIA_DOWNLOAD_EXIT_VALUE) {
            //下载退出---不管成功与否,下载结束最后一次的状态都是这个
            if (Values.downloadingFiles.contains(mediaid)) {
                int index = Values.downloadingFiles.indexOf(mediaid);
                Values.downloadingFiles.remove(index);
            }
            File file = new File(filepath);
            if (file.exists()) {
                LogUtil.i(TAG, "BusEvent -->" + "下载完成：" + filepath);
                switch (userStr) {
                    case Constant.MAIN_BG_PNG_TAG:
                        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_MAIN_BG).objects(filepath, mediaid).build());
                        break;
                    case Constant.MAIN_LOGO_PNG_TAG:
                        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_MAIN_LOGO).objects(filepath, mediaid).build());
                        break;
                    case Constant.NOTICE_BG_PNG_TAG:
                        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_NOTICE_BG).objects(filepath, mediaid).build());
                        break;
                    case Constant.NOTICE_LOGO_PNG_TAG:
                        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_NOTICE_LOGO).objects(filepath, mediaid).build());
                        break;
                    case Constant.PROJECTIVE_BG_PNG_TAG:
                        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_PROJECTIVE_BG).objects(filepath, mediaid).build());
                        break;
                    case Constant.PROJECTIVE_LOGO_PNG_TAG:
                        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_PROJECTIVE_LOGO).objects(filepath, mediaid).build());
                        break;
                    case Constant.SUB_BG_PNG_TAG:
                        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_SUB_BG).objects(filepath, mediaid).build());
                        break;
                    case Constant.ROOM_BG_PNG_TAG:
                        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_ROOM_BG).objects(filepath, mediaid).build());
                        break;
                    //下载完成后需要打开的文件
                    case Constant.DOWNLOAD_SHOULD_OPEN_FILE:
                        FileUtil.openFile(this, file);
                        break;
                    //下载的议程文件
                    case Constant.DOWNLOAD_AGENDA_FILE:
                        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_AGENDA_FILE).objects(filepath, mediaid).build());
                        break;
                    //归档议程文件，下载成功
                    case Constant.ARCHIVE_AGENDA_FILE:
                        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.ARCHIVE_BUS_AGENDA_FILE).objects(filepath, mediaid).build());
                        break;
                    //桌牌背景图片，下载完成
                    case Constant.DOWNLOAD_TABLE_CARD_BG:
                        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_TABLE_CARD_BG).objects(filepath, mediaid).build());
                        break;
                    default:
                        break;
                }
            } else {
                LogUtil.i(TAG, "downloadInform 没有找到文件 filepath=" + filepath);
//                ToastUtil.show(R.string.err_download);
            }
        } else {
            LogUtil.i(TAG, "downloadInform 下载状态：" + nstate + ", 下载错误码：" + err + ", 文件名：" + fileName);
        }
    }

    private void uploadInform(EventMessage msg) throws InvalidProtocolBufferException {
        byte[] datas = (byte[]) msg.getObjects()[0];
        InterfaceUpload.pbui_TypeUploadPosCb uploadPosCb = InterfaceUpload.pbui_TypeUploadPosCb.parseFrom(datas);
        String pathName = uploadPosCb.getPathname().toStringUtf8();
        String userStr = uploadPosCb.getUserstr().toStringUtf8();
        int status = uploadPosCb.getStatus();
        int mediaId = uploadPosCb.getMediaId();
        int per = uploadPosCb.getPer();
//        int uploadflag = uploadPosCb.getUploadflag();
//        int userval = uploadPosCb.getUserval();
        byte[] bytes = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(), mediaId);
        InterfaceBase.pbui_CommonTextProperty pbui_commonTextProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(bytes);
        String fileName = pbui_commonTextProperty.getPropertyval().toStringUtf8();
        LogUtil.i(TAG, "uploadInform -->" + "上传进度：" + per + "\npathName= " + pathName);
        if (status == InterfaceMacro.Pb_Upload_State.Pb_UPLOADMEDIA_FLAG_HADEND_VALUE) {
            //结束上传
            if (userStr.equals(Constant.UPLOAD_DRAW_PIC)) {
                //从画板上传的图片
                FileUtil.delFileByPath(pathName);
            } else if (userStr.equals(Constant.UPLOAD_PUBLISH_FILE)) {
                //上传会议发布文件完毕
                EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_UPLOAD_RELEASE_FILE_FINISH).build());
            }
            ToastUtil.show(getString(R.string.upload_completed, fileName));
            LogUtil.i(TAG, "uploadInform -->" + fileName + " 上传完毕");
        } else if (status == InterfaceMacro.Pb_Upload_State.Pb_UPLOADMEDIA_FLAG_NOSERVER_VALUE) {
            LogUtil.i(TAG, "uploadInform -->" + " 没找到可用的服务器");
        } else if (status == InterfaceMacro.Pb_Upload_State.Pb_UPLOADMEDIA_FLAG_ISBEING_VALUE) {
            LogUtil.i(TAG, "uploadInform -->" + pathName + " 已经存在");
        }
    }
}
