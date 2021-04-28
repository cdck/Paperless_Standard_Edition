package xlk.paperless.standard.view.meet;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceAgenda;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeetfunction;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.util.ArrayList;
import java.util.List;

import cc.shinichi.library.ImagePreview;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.base.BasePresenter;

import static xlk.paperless.standard.data.Constant.RESOURCE_0;
import static xlk.paperless.standard.data.Constant.RESOURCE_10;
import static xlk.paperless.standard.data.Constant.RESOURCE_11;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc :
 */
public class MeetingPresenter extends BasePresenter {
    private final String TAG = "MeetingPresenter-->";
    private final Context cxt;
    private final IMeet view;
    private JniHandler jni = JniHandler.getInstance();
    private List<InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo> functions = new ArrayList<>();
    private CountDownTimer countDownTimer;
    private boolean hasAgenda = false;

    public MeetingPresenter(Context cxt, IMeet view) {
        super();
        this.cxt = cxt;
        this.view = view;
    }


    @Override
    public void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case Constant.BUS_SIGN_IN_LIST_PAGE: {//议程变更通知
                boolean toListPage = (boolean) msg.getObject();
                view.changeSignInPage(toListPage);
                break;
            }
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETAGENDA_VALUE://议程变更通知
                queryAgenda();
                break;
            case Constant.BUS_NET_WORK:
                int isAvailable = (int) msg.getObjects()[0];
                LogUtil.d(TAG, "网络变更通知 -->" + Values.isOneline);
                if (Values.isOneline != isAvailable) {
                    Values.isOneline = isAvailable;
                    if (isAvailable == 0) {
                        close();
                    }
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_TIME_VALUE:
                Object[] objs = msg.getObjects();
                byte[] data = (byte[]) objs[0];
                InterfaceBase.pbui_Time pbui_time = InterfaceBase.pbui_Time.parseFrom(data);
                //微秒 转换成毫秒 除以 1000
                view.updateTime(pbui_time.getUsec() / 1000);
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FUNCONFIG_VALUE:
                LogUtil.d(TAG, "BusEvent -->" + "会议功能变更通知");
                queryMeetFunction();
                break;
            case Constant.BUS_MAIN_LOGO://logo图标下载完成
                String o2 = (String) msg.getObjects()[0];
                Drawable drawable = Drawable.createFromPath(o2);
                view.updateLogo(drawable);
                break;
//            case Constant.BUS_SUB_BG://子界面背景图下载完成
            case Constant.BUS_MAIN_BG://背景图下载完成
                String o1 = (String) msg.getObjects()[0];
                Drawable drawable1 = Drawable.createFromPath(o1);
                view.updateBg(drawable1);
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETFACECONFIG_VALUE://界面配置变更通知
                LogUtil.d(TAG, "BusEvent -->" + "界面配置变更通知");
                queryInterFaceConfiguration();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT_VALUE://会议排位变更通知
                LogUtil.d(TAG, "BusEvent -->" + "会议排位变更通知");
                queryLocalRole();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE_VALUE://会场设备信息变更通知
                byte[] o = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsgForDouble msgForDouble = InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(o);
                int id = msgForDouble.getId();
                int subid = msgForDouble.getSubid();
                int opermethod = msgForDouble.getOpermethod();
                if (opermethod == 4 && subid == Values.localDeviceId) {
                    LogUtil.d(TAG, "BusEvent -->" + "会场设备信息变更通知 退到主界面 id=" + id + ", subid= " + subid);
                    view.jump2main();
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO_VALUE://设备寄存器变更通知
                LogUtil.d(TAG, "BusEvent -->" + "设备寄存器变更通知");
                byte[] bytes = jni.queryDevicePropertiesById(InterfaceMacro.Pb_MeetDevicePropertyID.Pb_MEETDEVICE_PROPERTY_NETSTATUS_VALUE,
                        Values.localDeviceId);
                if (bytes == null) {
                    LogUtil.d(TAG, "BusEvent -->" + "bytes为空 设置离线");
                    view.updateOnline(cxt.getString(R.string.offline));
                    close();
                    return;
                }
                InterfaceDevice.pbui_DeviceInt32uProperty pbui_deviceInt32uProperty = InterfaceDevice.pbui_DeviceInt32uProperty.parseFrom(bytes);
                int propertyval = pbui_deviceInt32uProperty.getPropertyval();
                boolean isonline = propertyval == 1;
                if (!isonline) {
                    LogUtil.d(TAG, "BusEvent -->" + "isonline为false 设置离线");
                    close();
                } else {
                    view.updateOnline(cxt.getString(R.string.online));
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEFACESHOW_VALUE://设备会议信息变更通知
                LogUtil.i(TAG, "BusEvent -->" + "设备会议信息变更通知");
                queryDeviceMeetInfo();
                break;
            case Constant.BUS_PREVIEW_IMAGE:
                String filepath = (String) msg.getObjects()[0];
                LogUtil.i(TAG, "BusEvent 将要打开的图片路径：" + filepath);
                int index = 0;
                if (!picPath.contains(filepath)) {
                    picPath.add(filepath);
                    index = picPath.size() - 1;
                } else {
                    for (int i = 0; i < picPath.size(); i++) {
                        if (picPath.get(i).equals(filepath)) {
                            index = i;
                        }
                    }
                }
                previewImage(index);
                break;
            default:
                break;
        }
    }

    List<String> picPath = new ArrayList<>();

    private void previewImage(int index) {
        if (picPath.isEmpty()) {
            return;
        }
        ImagePreview.getInstance()
                .setContext(cxt)
                .setImageList(picPath)//设置图片地址集合
                .setIndex(index)//设置开始的索引
                .setShowDownButton(false)//设置是否显示下载按钮
                .setShowCloseButton(false)//设置是否显示关闭按钮
                .setEnableDragClose(true)//设置是否开启下拉图片退出
                .setEnableUpDragClose(true)//设置是否开启上拉图片退出
                .setEnableClickClose(true)//设置是否开启点击图片退出
                .setShowErrorToast(true)
                .start();
    }

    private long lastTime = 0;

    private void close() {
        LogUtil.e(TAG, "close 进行倒计时关闭 --> " + lastTime + ", " + Values.isOneline);
        if (lastTime > 0) return;
        lastTime = System.currentTimeMillis();
        countDownTimer = new CountDownTimer(60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                LogUtil.i(TAG, "close CountDownTimer onTick -->" + millisUntilFinished + ", " + Values.isOneline);
                if (Values.isOneline == 1) {
                    byte[] bytes = jni.queryDevicePropertiesById(InterfaceMacro.Pb_MeetDevicePropertyID.Pb_MEETDEVICE_PROPERTY_NETSTATUS_VALUE,
                            Values.localDeviceId);
                    if (bytes != null) {
                        InterfaceDevice.pbui_DeviceInt32uProperty pbui_deviceInt32uProperty = null;
                        try {
                            pbui_deviceInt32uProperty = InterfaceDevice.pbui_DeviceInt32uProperty.parseFrom(bytes);
                            int propertyval = pbui_deviceInt32uProperty.getPropertyval();
                            if (propertyval == 1) {
                                //已经有网络了就停止
                                LogUtil.i(TAG, "close CountDownTimer 恢复了在线状态");
                                countDownTimer.cancel();
                                countDownTimer = null;
                                lastTime = 0;
                                view.updateOnline(cxt.getString(R.string.online));
                            } else {
                                LogUtil.i(TAG, "close CountDownTimer 仍然是离线状态");
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                LogUtil.e(TAG, "close CountDownTimer onFinish  完毕  " + Values.isOneline);
                countDownTimer = null;
                lastTime = 0;
                view.jump2main();
            }
        };
        countDownTimer.start();
    }

    public void initial() {
        //  修改本机界面状态
        jni.modifyContextProperties(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_ROLE_VALUE,
                InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MemFace_VALUE);
        //缓存会议目录
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORY.getNumber());
        //会议目录文件
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE.getNumber());
        //缓存会议评分
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FILESCOREVOTESIGN_VALUE);
        // 缓存会场设备
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE.getNumber());
        //缓存会场设备
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE.getNumber());
        // 缓存会议排位
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT.getNumber());
        // 缓存参会人信息
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER.getNumber());
        //缓存参会人权限
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBERPERMISSION.getNumber());
        //缓存投票信息
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTEINFO.getNumber());
        //人员签到
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN.getNumber());
        //公告信息
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber());
        //会议视频
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVIDEO.getNumber());
    }

    /**
     * 查询会议功能
     */
    public void queryMeetFunction() {
        try {
            InterfaceMeetfunction.pbui_Type_MeetFunConfigDetailInfo funConfigDetailInfo = jni.queryMeetFunction();
            if (funConfigDetailInfo == null) {
                return;
            }
            functions.clear();
            List<InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo> itemList = funConfigDetailInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo item = itemList.get(i);
                LogUtil.i(TAG, "queryMeetFunction -->funcode=" + item.getFuncode() + ", position=" + item.getPosition());
                if (item.getFuncode() != Constant.FUN_CODE_SHARED_FILE
                        && item.getFuncode() != Constant.FUN_CODE_VOTE_RESULT) {
                    functions.add(item);
                }
            }
            view.updateFunction(functions);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    //查询界面配置
    public void queryInterFaceConfiguration() {
        try {
            InterfaceFaceconfig.pbui_Type_FaceConfigInfo faceConfigInfo = jni.queryInterFaceConfiguration();
            if (faceConfigInfo == null) return;
            List<InterfaceFaceconfig.pbui_Item_FacePictureItemInfo> pictureList = faceConfigInfo.getPictureList();
            List<InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo> onlytextList = faceConfigInfo.getOnlytextList();
            List<InterfaceFaceconfig.pbui_Item_FaceTextItemInfo> textList = faceConfigInfo.getTextList();
            for (int i = 0; i < pictureList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FacePictureItemInfo itemInfo = pictureList.get(i);
                int faceid = itemInfo.getFaceid();
                int flag = itemInfo.getFlag();
                boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_LOGO.getNumber()) {
                    view.setLogoVisibility(isShow);
                    if (isShow) {
                        FileUtil.createDir(Constant.DIR_PICTURE);
                        jni.creationFileDownload(Constant.DIR_PICTURE + Constant.MAIN_LOGO_PNG_TAG + ".png", itemInfo.getMediaid(), 1, 0, Constant.MAIN_LOGO_PNG_TAG);
                    }
                }
//                else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MAINBG_VALUE) {//主界面背景图
//                    FileUtil.createDir(Constant.dir_picture);
//                    jni.creationFileDownload(Constant.dir_picture + Constant.MAIN_BG_PNG_TAG + ".png", itemInfo.getMediaid(), 1, 0, Constant.MAIN_BG_PNG_TAG);
//                }
//                else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_SUBBG_VALUE) {//子界面背景图
//                    LogUtil.d(TAG, "fun_queryInterFaceConfiguration -->" + "下载子界面背景图");
//                    FileUtil.createDir(Constant.configuration_picture_dir);
//                    jni.creationFileDownload(Constant.configuration_picture_dir + Constant.SUB_BG_PNG_TAG + ".png", itemInfo.getMediaid(), 1, 0, Constant.SUB_BG_PNG_TAG);
//                }
            }
            for (int i = 0; i < onlytextList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo info = onlytextList.get(i);
                int faceid = info.getFaceid();
                int flag = info.getFlag();
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COLTDTEXT.getNumber()) {
                    view.setCompanyName(info.getText().toStringUtf8());
                    break;
                }
            }
            for (int i = 0; i < textList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info = textList.get(i);
                if (info.getFaceid() == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COMPANY.getNumber()) {//公司名称
                    int flag = info.getFlag();
                    boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
                    LogUtil.d(TAG, "fun_queryInterFaceConfiguration -->是否显示公司名称：" + isShow);
                    view.setCompanyVisibility(isShow);
                } else if (info.getFaceid() == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_LOGO_GEO.getNumber()) {//Logo图标,只需要更新位置坐标
                    LogUtil.d(TAG, "queryInterFaceConfiguration -->" + "更新logo图标大小");
                    view.updateLogoSize(R.id.meet_logo, info);
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    //查询本机是否在线
    public void queryIsOnline() {
        byte[] bytes = jni.queryDevicePropertiesById(InterfaceMacro.Pb_MeetDevicePropertyID.Pb_MEETDEVICE_PROPERTY_NETSTATUS_VALUE, Values.localDeviceId);
        if (bytes == null) {
            view.updateOnline(cxt.getResources().getString(R.string.offline));
            return;
        }
        try {
            InterfaceDevice.pbui_DeviceInt32uProperty pbui_deviceInt32uProperty = InterfaceDevice.pbui_DeviceInt32uProperty.parseFrom(bytes);
            int propertyval = pbui_deviceInt32uProperty.getPropertyval();
            view.updateOnline((propertyval == 1) ? cxt.getResources().getString(R.string.online) : cxt.getResources().getString(R.string.offline));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryDeviceMeetInfo() {
        try {
            InterfaceDevice.pbui_Type_DeviceFaceShowDetail deviceMeetInfo = jni.queryDeviceMeetInfo();
            if (deviceMeetInfo == null) return;
            Values.localMeetingId = deviceMeetInfo.getMeetingid();
            Values.localMemberId = deviceMeetInfo.getMemberid();
            Values.localMemberName = deviceMeetInfo.getMembername().toStringUtf8();
            Values.localMeetingName = deviceMeetInfo.getMeetingname().toStringUtf8();
//            Values.localDeviceId = deviceMeetInfo.getDeviceid();
            Values.localRoomId = deviceMeetInfo.getRoomid();
            view.updateMeetName(deviceMeetInfo);
            queryLocalRole();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryPermission() {
        try {
            InterfaceMember.pbui_Type_MemberPermission memberPermission = jni.queryAttendPeoplePermissions();
            if (memberPermission == null) return;
            Values.allPermissions = memberPermission.getItemList();
            for (int i = 0; i < Values.allPermissions.size(); i++) {
                InterfaceMember.pbui_Item_MemberPermission permission = Values.allPermissions.get(i);
                if (permission.getMemberid() == Values.localMemberId) {
                    Values.localPermission = permission.getPermission();
                    return;
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryLocalRole() {
        try {
            InterfaceBase.pbui_CommonInt32uProperty property = jni.queryMeetRankingProperty(InterfaceMacro.Pb_MeetSeatPropertyID.Pb_MEETSEAT_PROPERTY_ROLEBYMEMBERID.getNumber());
            if (property == null) return;
            int propertyval = property.getPropertyval();
            Values.localRole = propertyval;
            if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere.getNumber()
                    || propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary.getNumber()
                    || propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin.getNumber()) {
                //当前是主持人或秘书或管理员，设置拥有所有权限
                Values.hasAllPermissions = true;
                view.hasOtherFunction(true);
                if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere.getNumber()) {
                    view.updateMemberRole(cxt.getString(R.string.role_host));
                } else if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary.getNumber()) {
                    view.updateMemberRole(cxt.getString(R.string.role_secretary));
                } else {
                    view.updateMemberRole(cxt.getString(R.string.role_admin));
                }
            } else {
                Values.hasAllPermissions = false;
                view.hasOtherFunction(false);
                view.updateMemberRole(cxt.getString(R.string.role_member));
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void initVideoRes() {
        jni.initVideoRes(RESOURCE_0, Values.screen_width, Values.screen_height);
        jni.initVideoRes(RESOURCE_10, Values.screen_width, Values.screen_height);
        jni.initVideoRes(RESOURCE_11, Values.screen_width, Values.screen_height);
    }

    public void releaseVideoRes() {
        jni.releaseVideoRes(RESOURCE_0);
        jni.releaseVideoRes(RESOURCE_10);
        jni.releaseVideoRes(RESOURCE_11);
    }

    public boolean hasAgenda() {
        return hasAgenda;
    }

    public void queryAgenda() {
        try {
            InterfaceAgenda.pbui_meetAgenda pbui_meetAgenda = jni.queryAgenda();
            hasAgenda = pbui_meetAgenda != null;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
