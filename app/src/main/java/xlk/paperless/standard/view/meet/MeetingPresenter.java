package xlk.paperless.standard.view.meet;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.SuperKotlin.pictureviewer.ImagePagerActivity;
import com.SuperKotlin.pictureviewer.PictureConfig;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeetfunction;
import com.mogujie.tt.protobuf.InterfaceMember;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.util.AppUtil;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.BasePresenter;
import xlk.paperless.standard.view.MyApplication;

/**
 * @author xlk
 * @date 2020/3/9
 * @Description:
 */
public class MeetingPresenter extends BasePresenter {
    private final String TAG = "MeetingPresenter-->";
    private final Context cxt;
    private final IMeet view;
    private JniHandler jni = JniHandler.getInstance();
    private List<InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo> functions = new ArrayList<>();

    public MeetingPresenter(Context cxt, IMeet view) {
        this.cxt = cxt;
        this.view = view;
    }

    @Override
    public void register() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void unregister() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void BusEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case Constant.BUS_NET_WORK:
                if (!AppUtil.isNetworkAvailable(cxt)) {
                    view.jump2main();
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_TIME_VALUE:
                Object[] objs = msg.getObjs();
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
                String o2 = (String) msg.getObjs()[0];
                Drawable drawable = Drawable.createFromPath(o2);
                view.updateLogo(drawable);
                break;
            case Constant.BUS_SUB_BG://子界面背景图下载完成
                String o1 = (String) msg.getObjs()[0];
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
                byte[] o = (byte[]) msg.getObjs()[0];
                InterfaceBase.pbui_MeetNotifyMsgForDouble msgForDouble = InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(o);
                int id = msgForDouble.getId();
                int subid = msgForDouble.getSubid();
                int opermethod = msgForDouble.getOpermethod();
                if (opermethod == 4 && subid == MyApplication.localDeviceId) {
                    LogUtil.d(TAG, "BusEvent -->" + "会场设备信息变更通知 退到主界面 id=" + id + ", subid= " + subid);
                    view.jump2main();
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO_VALUE://设备寄存器变更通知
                LogUtil.d(TAG, "BusEvent -->" + "设备寄存器变更通知");
                byte[] bytes = jni.queryDevicePropertiesById(InterfaceMacro.Pb_MeetDevicePropertyID.Pb_MEETDEVICE_PROPERTY_NETSTATUS_VALUE,
                        MyApplication.localDeviceId);
                if (bytes == null) {
                    LogUtil.d(TAG, "BusEvent -->" + "bytes为空 设置离线");
                    view.updateOnline(cxt.getString(R.string.offline));
                    view.jump2main();
                    return;
                }
                InterfaceDevice.pbui_DeviceInt32uProperty pbui_deviceInt32uProperty = InterfaceDevice.pbui_DeviceInt32uProperty.parseFrom(bytes);
                int propertyval = pbui_deviceInt32uProperty.getPropertyval();
                boolean isonline = propertyval == 1;
                if (!isonline) {
                    LogUtil.d(TAG, "BusEvent -->" + "isonline为false 设置离线");
                    view.jump2main();
                } else {
                    view.updateOnline(cxt.getString(R.string.online));
                }
                break;

            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEFACESHOW_VALUE://设备会议信息变更通知
                LogUtil.i(TAG, "BusEvent -->" + "设备会议信息变更通知");
                queryDeviceMeetInfo();
                break;
            case Constant.BUS_PREVIEW_IMAGE:
                String filepath = (String) msg.getObjs()[0];
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
        }
    }

    List<String> picPath = new ArrayList<>();

    private void previewImage(int index) {
        if (picPath.isEmpty()) {
            return;
        }
        //使用方式
        PictureConfig config = new PictureConfig.Builder()
                .setListData((ArrayList<String>) picPath)    //图片数据List<String> list
                .setPosition(index)    //图片下标（从第position张图片开始浏览）
//                .setDownloadPath("pictureviewer")	//图片下载文件夹地址
                .setIsShowNumber(true)//是否显示数字下标
//                .needDownload(true)	//是否支持图片下载
//                .setPlacrHolder(R.mipmap.icon)	//占位符图片（图片加载完成前显示的资源图片，来源drawable或者mipmap）
                .build();
        ImagePagerActivity.startActivity(cxt, config);
    }

    public void initial() {
        //  修改本机界面状态
        jni.setInterfaceState(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_ROLE_VALUE,
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
    }

    //查询会议功能
    public void queryMeetFunction() {
        try {
            InterfaceMeetfunction.pbui_Type_MeetFunConfigDetailInfo funConfigDetailInfo = jni.queryMeetFunction();
            if (funConfigDetailInfo == null) return;
            functions.clear();
            functions.addAll(funConfigDetailInfo.getItemList());
            for (int i = 0; i < functions.size(); i++) {
                LogUtil.i(TAG, "queryMeetFunction -->" + functions.get(i).getFuncode() + ", " + functions.get(i).getPosition());
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
                    FileUtil.createDir(Constant.configuration_picture_dir);
                    jni.creationFileDownload(Constant.configuration_picture_dir + Constant.MAIN_LOGO_PNG_TAG + ".png", itemInfo.getMediaid(), 1, 0, Constant.MAIN_LOGO_PNG_TAG);
                    view.setLogoVisibility(isShow);
                    break;
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_SUBBG_VALUE) {//子界面背景图
                    LogUtil.d(TAG, "fun_queryInterFaceConfiguration -->" + "下载子界面背景图");
                    FileUtil.createDir(Constant.configuration_picture_dir);
                    jni.creationFileDownload(Constant.configuration_picture_dir + Constant.SUB_BG_PNG_TAG + ".png", itemInfo.getMediaid(), 1, 0, Constant.SUB_BG_PNG_TAG);
                }
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
        byte[] bytes = jni.queryDevicePropertiesById(InterfaceMacro.Pb_MeetDevicePropertyID.Pb_MEETDEVICE_PROPERTY_NETSTATUS_VALUE, MyApplication.localDeviceId);
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
            MyApplication.localMeetingId = deviceMeetInfo.getMeetingid();
            MyApplication.localMemberId = deviceMeetInfo.getMemberid();
            MyApplication.localMemberName = deviceMeetInfo.getMembername().toStringUtf8();
            MyApplication.localMeetingName = deviceMeetInfo.getMeetingname().toStringUtf8();
            MyApplication.localDeviceId = deviceMeetInfo.getDeviceid();
            MyApplication.localRoomId = deviceMeetInfo.getRoomid();
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
            MyApplication.allMemberPermissions = memberPermission.getItemList();
            for (int i = 0; i < MyApplication.allMemberPermissions.size(); i++) {
                InterfaceMember.pbui_Item_MemberPermission permission = MyApplication.allMemberPermissions.get(i);
                if (permission.getMemberid() == MyApplication.localMemberId) {
                    MyApplication.localPermissions = Constant.getChoose(permission.getPermission());
                    return;
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryLocalRole() {
        try {
            InterfaceBase.pbui_CommonInt32uProperty property = jni.queryMeetRankingProperty(0, InterfaceMacro.Pb_MeetSeatPropertyID.Pb_MEETSEAT_PROPERTY_ROLEBYMEMBERID.getNumber());
            if (property == null) return;
            int propertyval = property.getPropertyval();
            MyApplication.localRole = propertyval;
            if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere.getNumber()
                    || propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary.getNumber()
                    || propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin.getNumber()) {
                //当前是主持人或秘书或管理员，设置拥有所有权限
                MyApplication.hasAllPermissions = true;
                view.hasOtherFunction(true);
                if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere.getNumber()) {
                    view.updateMemberRole(cxt.getString(R.string.role_host));
                } else if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary.getNumber()) {
                    view.updateMemberRole(cxt.getString(R.string.role_secretary));
                } else {
                    view.updateMemberRole(cxt.getString(R.string.role_admin));
                }
            } else {
                MyApplication.hasAllPermissions = false;
                view.hasOtherFunction(false);
                view.updateMemberRole(cxt.getString(R.string.role_member));
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void initVideoRes() {
        jni.initVideoRes(0, MyApplication.screen_width, MyApplication.screen_height);
        jni.initVideoRes(10, MyApplication.screen_width, MyApplication.screen_height);
        jni.initVideoRes(11, MyApplication.screen_width, MyApplication.screen_height);
    }

    public void releaseVideoRes() {
        jni.releaseVideoRes(0);
        jni.releaseVideoRes(10);
        jni.releaseVideoRes(11);
    }
}
