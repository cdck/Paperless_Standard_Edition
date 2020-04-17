package xlk.paperless.standard.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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

import java.io.File;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.bean.ChatMessage;
import xlk.paperless.standard.util.AppUtil;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.MyApplication;
import xlk.paperless.standard.view.meet.MeetingActivity;
import xlk.paperless.standard.view.notice.NoticeActivity;
import xlk.paperless.standard.view.score.ScoreActivity;
import xlk.paperless.standard.view.video.VideoActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static xlk.paperless.standard.view.chatonline.ChatVideoActivity.isChatingOpened;
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
 * @Description: 后台服务
 */
public class BackstageService extends Service {

    private final String TAG = "BackstageService-->";
    private JniHandler jni = JniHandler.getInstance();
    public static boolean isVideoPlaying;//是否正在播放
    public static boolean isMandatoryPlaying;//是否正在被强制性播放中
    public static boolean haveNewPlayInform;

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
    public void BusEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DOWNLOAD_VALUE://平台下载
                downloadInform(msg);
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_UPLOAD_VALUE://上传进度通知
                uploadInform(msg);
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBERPERMISSION_VALUE://参会人权限变更通知
                InterfaceMember.pbui_Type_MemberPermission o = jni.queryAttendPeoplePermissions();
                MyApplication.allMemberPermissions = o.getItemList();
                for (int i = 0; i < MyApplication.allMemberPermissions.size(); i++) {
                    InterfaceMember.pbui_Item_MemberPermission item = MyApplication.allMemberPermissions.get(i);
                    if (item.getMemberid() == MyApplication.localMemberId) {
                        MyApplication.localPermissions = Constant.getChoose(item.getPermission());
                        break;
                    }
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETIM_VALUE://会议交流
                if (!chatIsShowing) {
                    byte[] o1 = (byte[]) msg.getObjs()[0];
                    InterfaceIM.pbui_Type_MeetIM meetIM = InterfaceIM.pbui_Type_MeetIM.parseFrom(o1);
                    if (meetIM.getMsgtype() == 0) {//文本类消息
                        int badgeNumber = mBadge.getBadgeNumber();
                        MeetingActivity.chatMessages.add(new ChatMessage(0, meetIM));
                        mBadge.setBadgeNumber(++badgeNumber);
                    }
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD_VALUE:
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDPICTURE_VALUE) {//添加图片通知
                    byte[] o1 = (byte[]) msg.getObjs()[0];
                    addPicInform(o1);
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY_VALUE://媒体播放通知
                mediaPlayInform(msg);
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_STREAMPLAY_VALUE://流播放通知
                streamPlayInform(msg);
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICECONTROL_VALUE://设备控制
                deviceControlInform(msg);
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET_VALUE://公告
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_PUBLIST_VALUE) {
                    LogUtil.i(TAG, "BusEvent -->" + "发布公告通知");
                    byte[] o1 = (byte[]) msg.getObjs()[0];
                    InterfaceBullet.pbui_BulletDetailInfo detailInfo = InterfaceBullet.pbui_BulletDetailInfo.parseFrom(o1);
                    List<InterfaceBullet.pbui_Item_BulletDetailInfo> itemList = detailInfo.getItemList();
                    if (!itemList.isEmpty()) {
                        InterfaceBullet.pbui_Item_BulletDetailInfo info = itemList.get(0);
                        int bulletid = info.getBulletid();
                        NoticeActivity.jump(bulletid, this);
                    }
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FILESCOREVOTE_VALUE:
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_START_VALUE) {
                    LogUtil.i(TAG, "BusEvent -->" + "自定义文件评分投票  发起");
                    byte[] o1 = (byte[]) msg.getObjs()[0];
                    InterfaceFilescorevote.pbui_Type_StartUserDefineFileScoreNotify info = InterfaceFilescorevote.pbui_Type_StartUserDefineFileScoreNotify.parseFrom(o1);
                    int voteid = info.getVoteid();
                    LogUtil.i(TAG, "BusEvent -->" + "收到发起文件自定义选项评分 voteid= " + voteid);
                    startActivity(new Intent(this, ScoreActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("voteid", voteid));
                }
                break;
        }
    }

    private void deviceControlInform(EventMessage msg) throws InvalidProtocolBufferException {
        byte[] o = (byte[]) msg.getObjs()[0];
        InterfaceDevice.pbui_Type_DeviceControl object = InterfaceDevice.pbui_Type_DeviceControl.parseFrom(o);
        int oper = object.getOper();//enum Pb_DeviceControlFlag
        int operval1 = object.getOperval1();//操作对应的参数 如更换主界面的媒体ID
        int operval2 = object.getOperval2();//操作对应的参数
        if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_MODIFYLOGO.getNumber()) {
            LogUtil.i(TAG, "deviceControl: 更换Logo通知");
            //本地没有才下载
            FileUtil.createDir(Constant.configuration_picture_dir);
            jni.creationFileDownload(Constant.configuration_picture_dir + Constant.MAIN_LOGO_PNG_TAG + ".png", operval1, 1, 0, Constant.MAIN_LOGO_PNG_TAG);
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_SHUTDOWN.getNumber()) {//关机
            LogUtil.i(TAG, "deviceControl: 关机");
        } else if (oper == InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_REBOOT.getNumber()) {//重启
            LogUtil.i(TAG, "deviceControl: 重启");
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

    private void streamPlayInform(EventMessage msg) throws InvalidProtocolBufferException {
        byte[] datas = (byte[]) msg.getObjs()[0];
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
            //是否是强制性播放
            isMandatoryPlaying = isMandatory;
            haveNewPlayInform = true;
            if (!isVideoPlaying) {
                startActivity(new Intent(this, VideoActivity.class).setFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            } else {
                if (isMandatoryPlaying) {
                    EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_MANDATORY).build());
                }
            }
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
        byte[] datas = (byte[]) msg.getObjs()[0];
        InterfacePlaymedia.pbui_Type_MeetMediaPlay mediaPlay = InterfacePlaymedia.pbui_Type_MeetMediaPlay.parseFrom(datas);
        int res = mediaPlay.getRes();
        LogUtil.i(TAG, "mediaPlayInform -->" + "媒体播放通知 res= " + res);
        if (res != 0) return;//只处理资源ID为0的播放资源
        int mediaid = mediaPlay.getMediaid();
        int createdeviceid = mediaPlay.getCreatedeviceid();
        int triggerid = mediaPlay.getTriggerid();
        int triggeruserval = mediaPlay.getTriggeruserval();
        int type = mediaid & Constant.MAIN_TYPE_BITMASK;
        if (type == Constant.MEDIA_FILE_TYPE_AUDIO || type == Constant.MEDIA_FILE_TYPE_VIDEO) {
            LogUtil.i(TAG, "mediaPlayInform -->" + "媒体播放通知：isVideoPlaying= " + isVideoPlaying);
            haveNewPlayInform = true;
            if (!isVideoPlaying) {
                startActivity(new Intent(this, VideoActivity.class).setFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            }
        } else {
            LogUtil.i(TAG, "mediaPlayInform -->" + "媒体播放通知：下载文件后打开");
            //创建好下载目录
            FileUtil.createDir(Constant.ROOT_DIR);
            /** **** **  查询该媒体ID的文件名  ** **** **/
            byte[] bytes = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(), mediaid);
            InterfaceBase.pbui_CommonTextProperty pbui_commonTextProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(bytes);
            String fielName = pbui_commonTextProperty.getPropertyval().toStringUtf8();
            jni.creationFileDownload(Constant.ROOT_DIR + fielName, mediaid, 0, 0, Constant.SHOULD_OPEN_FILE_KEY);
        }
    }

    private void addPicInform(byte[] datas) throws InvalidProtocolBufferException {
        InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail object = InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail.parseFrom(datas);
        int rPicSrcmemid = object.getSrcmemid();
        long rPicSrcwbid = object.getSrcwbid();
        ByteString rPicData = object.getPicdata();
        int opermemberid = object.getOpermemberid();
        MyApplication.operid = object.getOperid();
        if (!isSharing) {
            if (disposePicOpermemberid == opermemberid && disposePicSrcmemid == rPicSrcmemid
                    && disposePicSrcwbidd == rPicSrcwbid) {
                tempPicData = rPicData;
                disposePicOpermemberid = 0;
                disposePicSrcmemid = 0;
                disposePicSrcwbidd = 0;
            }
            return;
        } else {
            EventBus.getDefault().postSticky(new EventMessage.Builder().type(Constant.BUS_SHARE_PIC).objs(object).build());
        }
    }

    private void downloadInform(EventMessage msg) throws InvalidProtocolBufferException {
        byte[] data2 = (byte[]) msg.getObjs()[0];
        InterfaceDownload.pbui_Type_DownloadCb pbui_type_downloadCb = InterfaceDownload.pbui_Type_DownloadCb.parseFrom(data2);
        int mediaid = pbui_type_downloadCb.getMediaid();
        int progress = pbui_type_downloadCb.getProgress();
        int nstate = pbui_type_downloadCb.getNstate();
        String filepath = pbui_type_downloadCb.getPathname().toStringUtf8();
        String s = filepath.substring(filepath.lastIndexOf("/") + 1).toLowerCase();
        String userStr = pbui_type_downloadCb.getUserstr().toStringUtf8();
        if (nstate == 4) {//下载退出---不管成功与否,下载结束最后一次的状态都是这个
            File f = new File(filepath);
            if (f.exists()) {
                LogUtil.i(TAG, "BusEvent -->" + "下载完成：" + filepath);
                if (userStr.equals(Constant.MAIN_BG_PNG_TAG)) {
                    EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_MAIN_BG).objs(filepath).build());
                } else if (userStr.equals(Constant.SUB_BG_PNG_TAG)) {
                    EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_SUB_BG).objs(filepath).build());
                } else if (userStr.equals(Constant.NOTICE_BG_PNG_TAG)) {
                    EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_NOTICE_BG).objs(filepath).build());
                } else if (userStr.equals(Constant.NOTICE_LOGO_PNG_TAG)) {
                    EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_NOTICE_LOGO).objs(filepath).build());
                } else if (userStr.equals(Constant.MAIN_LOGO_PNG_TAG)) {
                    EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_MAIN_LOGO).objs(filepath).build());
                } else if (userStr.equals(Constant.ROOM_BG_PNG_TAG)) {
                    EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_ROOM_BG).objs(filepath).build());
                } else if (userStr.equals(Constant.SHOULD_OPEN_FILE_KEY)) {//下载完成后需要打开的文件
                    FileUtil.openFile(this, f);
                } else if (userStr.equals(Constant.AGENDA_FILE_KEY)) {//下载的议程文件
                    EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_AGENDA_FILE).objs(filepath).build());
                }
            } else {
                ToastUtil.show(getApplicationContext(), R.string.err_download);
            }
        }
    }

    private void uploadInform(EventMessage msg) throws InvalidProtocolBufferException {
        byte[] datas = (byte[]) msg.getObjs()[0];
        InterfaceUpload.pbui_TypeUploadPosCb uploadPosCb = InterfaceUpload.pbui_TypeUploadPosCb.parseFrom(datas);
        // /storage/emulated/0/PaperlessStandardEdition/ArtboardPicture/1584785387355.png
        String pathName = uploadPosCb.getPathname().toStringUtf8();
        String userStr = uploadPosCb.getUserstr().toStringUtf8();
        int status = uploadPosCb.getStatus();
//        int mediaId = uploadPosCb.getMediaId();
//        int per = uploadPosCb.getPer();
//        int uploadflag = uploadPosCb.getUploadflag();
//        int userval = uploadPosCb.getUserval();
//        byte[] bytes = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(), mediaId);
//        InterfaceBase.pbui_CommonTextProperty pbui_commonTextProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(bytes);
//        //1584785387355.png
//        String uploadFileName = b2s(pbui_commonTextProperty.getPropertyval());
        if (status == InterfaceMacro.Pb_Upload_State.Pb_UPLOADMEDIA_FLAG_HADEND_VALUE) {//结束上传
            LogUtil.i(TAG, "uploadInform -->" + pathName + " 上传完毕");
            if (userStr.equals(Constant.upload_draw_pic)) {//从画板上传的图片
                FileUtil.delFileByPath(pathName);
            }
        } else if (status == InterfaceMacro.Pb_Upload_State.Pb_UPLOADMEDIA_FLAG_NOSERVER_VALUE) {
            LogUtil.i(TAG, "uploadInform -->" + " 没找到可用的服务器");
        } else if (status == InterfaceMacro.Pb_Upload_State.Pb_UPLOADMEDIA_FLAG_ISBEING_VALUE) {
            LogUtil.i(TAG, "uploadInform -->" + pathName + " 已经存在");
        }
    }
}
