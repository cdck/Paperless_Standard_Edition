package xlk.paperless.standard.view.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaCodecInfo;
import android.text.TextUtils;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceContext;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Call;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.util.AppUtil;
import xlk.paperless.standard.util.CodecUtil;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.IniUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.BasePresenter;
import xlk.paperless.standard.view.MyApplication;

import static xlk.paperless.standard.view.MyApplication.initializationIsOver;

/**
 * @author xlk
 * @date 2020/3/9
 * @Description:
 */
public class MainPresenter extends BasePresenter {
    private final String TAG = "MainPresenter-->";
    private IniUtil iniUtil = IniUtil.getInstance();
    private JniHandler jni = JniHandler.getInstance();
    private final Context cxt;
    private final IMain view;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberDetailInfos = new ArrayList<>();
    //存放未绑定的人员
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> chooseMemberDetailInfos = new ArrayList<>();

    public MainPresenter(Context cxt, IMain iMain) {
        this.cxt = cxt;
        this.view = iMain;
    }

    public void setInterfaceState() {
        //  修改本机界面状态
        jni.setInterfaceState(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_ROLE_VALUE,
                InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MainFace_VALUE);
    }

    @Override
    public void register() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void unregister() {
        EventBus.getDefault().unregister(this);
    }

    // 复制ini、dev文件
    public void initConfFile() {
        //拷贝配置文件
        if (!IniUtil.iniFile.exists()) {
            LogUtil.d(TAG, "initConfFile :  拷贝配置文件 --> ");
            copyTo("client.ini", Constant.ROOT_DIR, Constant.INI_NAME);
        } else {
            LogUtil.d(TAG, "initConfFile :  已有ini文件 --> ");
            iniUtil.loadFile(IniUtil.iniFile);
            String streamprotol = iniUtil.get("selfinfo", "streamprotol");
            String disablemulticast = iniUtil.get("Audio", "disablemulticast");
            if (streamprotol == null || streamprotol.equals("") || disablemulticast == null || disablemulticast.equals("")) {
                iniUtil.put("selfinfo", "streamprotol", 1);
                iniUtil.put("Audio", "disablemulticast", 1);
                iniUtil.store();//修改后提交
                LogUtil.d(TAG, "initConfFile :  进行添加 streamprotol 和 disablemulticast--> ");
            }
        }
        //设置版本信息
        setVersion();
        File devFile = new File(Constant.ROOT_DIR + "/" + Constant.DEV_NAME);
        if (!devFile.exists()) {
            copyTo(Constant.DEV_NAME, Constant.ROOT_DIR, Constant.DEV_NAME);
        } else {
            devFile.delete();
            copyTo(Constant.DEV_NAME, Constant.ROOT_DIR, Constant.DEV_NAME);
        }
    }

    /**
     * 复制文件
     */
    private void copyTo(String fromPath, String toPath, String fileName) {
        // 复制位置
        // opPath：mnt/sdcard/lcuhg/health/
        // mnt/sdcard：表示sdcard
        File toFile = new File(toPath);
        // 如果不存在，创建文件夹
        if (!toFile.exists()) {
            boolean isCreate = toFile.mkdirs();
            // 打印创建结果
            LogUtil.i("create dir", String.valueOf(isCreate));
        }
        try {
            // 根据文件名获取assets文件夹下的该文件的inputstream
            InputStream fromFileIs = cxt.getResources().getAssets().open(fromPath);
            int length = fromFileIs.available(); // 获取文件的字节数
            byte[] buffer = new byte[length]; // 创建byte数组
            FileOutputStream fileOutputStream = new FileOutputStream(toFile + "/" + fileName); // 字节输入流
            BufferedInputStream bufferedInputStream = new BufferedInputStream(
                    fromFileIs);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                    fileOutputStream);
            int len = bufferedInputStream.read(buffer);
            while (len != -1) {
                bufferedOutputStream.write(buffer, 0, len);
                len = bufferedInputStream.read(buffer);
            }
            bufferedInputStream.close();
            bufferedOutputStream.close();
            fromFileIs.close();
            fileOutputStream.close();
            LogUtil.i(TAG, "copyTo方法 拷贝" + fromPath + "完成------");
            //确保有ini文件
            if (fromPath.equals(Constant.INI_NAME)) {
                LogUtil.d(TAG, "进入设置版本信息。。。。");
                iniUtil.loadFile(IniUtil.iniFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setVersion() {
        LogUtil.d(TAG, "setVersion -->设置版本信息");
        PackageManager pm = cxt.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(cxt.getPackageName(), 0);
            LogUtil.d(TAG, "当前版本名称：" + packageInfo.versionName + ", 版本号：" + packageInfo.versionCode);
            view.updateVersion(packageInfo.versionName);
            String hardver = "";
            String softver = "";
            if (packageInfo.versionName.contains(".")) {
                hardver = packageInfo.versionName.substring(0, packageInfo.versionName.indexOf("."));
                softver = packageInfo.versionName.substring(packageInfo.versionName.indexOf(".") + 1, packageInfo.versionName.length());
            }
            iniUtil.put("selfinfo", "hardver", hardver);
            iniUtil.put("selfinfo", "softver", softver);
            iniUtil.store();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initialization() {
        new Thread(() -> jni.javaInitSys(AppUtil.getUniqueId(cxt))).start();
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void BusEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case Constant.BUS_NET_WORK:

                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_TIME_VALUE:
                Object[] objs = msg.getObjs();
                byte[] data = (byte[]) objs[0];
                InterfaceBase.pbui_Time pbui_time = InterfaceBase.pbui_Time.parseFrom(data);
                //微秒 转换成毫秒 除以 1000
                view.updateTime(pbui_time.getUsec() / 1000);
                break;
            case Constant.BUS_MAIN_LOGO:
                String filepath = (String) msg.getObjs()[0];
                Drawable drawable = Drawable.createFromPath(filepath);
                view.updateLogo(drawable);
                break;
            case Constant.BUS_MAIN_BG:
                String filepath1 = (String) msg.getObjs()[0];
                Drawable drawable1 = Drawable.createFromPath(filepath1);
                view.updateBackground(drawable1);
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_READY_VALUE:
                int method = msg.getMethod();
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    LogUtil.i(TAG, "BusEvent -->" + "平台初始化完毕");
                    initializationIsOver = true;
                    view.initialized();
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_LOGON_VALUE) {
                    initializationIsOver = false;
                    LogUtil.i(TAG, "BusEvent -->" + "平台初始化失败");
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEMEETSTATUS_VALUE://界面状态变更通知
                LogUtil.i(TAG, "BusEvent -->" + "界面状态变更通知");
                Object[] objs1 = msg.getObjs();
                int datalen = (int) objs1[1];
                if (datalen > 0) {
                    queryInterFaceConfiguration();
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEFACESHOW_VALUE://设备会议信息变更通知
                LogUtil.i(TAG, "BusEvent -->" + "设备会议信息变更通知");
                queryDevMeetInfo();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO_VALUE://会议信息变更通知
                LogUtil.i(TAG, "BusEvent -->" + "会议信息变更通知");
                queryMeetFromId();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE://参会人员变更通知
                byte[] o = (byte[]) msg.getObjs()[0];
                InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(o);
                int id = pbui_meetNotifyMsg.getId();
                int opermethod = pbui_meetNotifyMsg.getOpermethod();
                LogUtil.i(TAG, "BusEvent -->" + "参会人员变更通知 id= " + id + ", opermethod= " + opermethod);
                queryAttendPeople();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETFACECONFIG_VALUE://界面配置变更通知
                LogUtil.i(TAG, "BusEvent -->" + "界面配置变更通知");
                queryInterFaceConfiguration();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN_VALUE://会议签到结果返回
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_LOGON_VALUE) {
                    //签到密码返回
                    byte[] datas = (byte[]) msg.getObjs()[0];
                    InterfaceBase.pbui_Type_MeetDBServerOperError dbServerOperError = InterfaceBase.pbui_Type_MeetDBServerOperError.parseFrom(datas);
                    int type = dbServerOperError.getType();
                    int method1 = dbServerOperError.getMethod();
                    int status = dbServerOperError.getStatus();
                    if (status == InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_DONE_VALUE) {
                        LogUtil.i(TAG, "BusEvent -->" + "签到成功，进入会议");
                        view.jump2meet();
                    } else if (status == InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_PSWFAILED_VALUE) {
                        LogUtil.i(TAG, "BusEvent -->" + "签到密码错误");
                        ToastUtil.show(cxt, R.string.sign_in_password_error);
                        view.signIn();
                    }
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE://设备交互信息
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ENTER_VALUE) {
                    LogUtil.i(TAG, "BusEvent -->" + "辅助签到进入会议");
                    view.jump2meet();
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT_VALUE://会议排位变更通知
                LogUtil.d(TAG, "BusEvent -->" + "会议排位变更通知");
                queryLocalRole();
                break;
        }
    }

    //初始化流通道
    public void initStream() {
        int format = CodecUtil.selectColorFormat(CodecUtil.selectCodec("video/avc"), "video/avc");
        switch (format) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                Call.COLOR_FORMAT = 0;
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                Call.COLOR_FORMAT = 1;
                break;
        }
        jni.InitAndCapture(0, 2);
        jni.InitAndCapture(0, 3);
    }

    public void queryContextProperty() {
        try {
            InterfaceContext.pbui_MeetContextInfo pbui_meetContextInfo = jni.queryContextProperty(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_SELFID_VALUE);
            if (pbui_meetContextInfo == null) {
                return;
            }
            MyApplication.localDeviceId = pbui_meetContextInfo.getPropertyval();
            LogUtil.d(TAG, "queryContextProperty -->本机的设备ID：" + MyApplication.localDeviceId);
            setDevName();
            queryDevMeetInfo();
            queryInterFaceConfiguration();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryInterFaceConfiguration() {
        try {
            InterfaceFaceconfig.pbui_Type_FaceConfigInfo faceConfigInfo = jni.queryInterFaceConfiguration();
            if (faceConfigInfo == null) return;
            List<InterfaceFaceconfig.pbui_Item_FacePictureItemInfo> pictureList = faceConfigInfo.getPictureList();
            List<InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo> onlytextList = faceConfigInfo.getOnlytextList();
            List<InterfaceFaceconfig.pbui_Item_FaceTextItemInfo> textList = faceConfigInfo.getTextList();
            for (int i = 0; i < pictureList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FacePictureItemInfo itemInfo = pictureList.get(i);
                int faceid = itemInfo.getFaceid();
                int mediaid = itemInfo.getMediaid();
                String userStr = "";
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MAINBG_VALUE) {//主界面背景
                    userStr = Constant.MAIN_BG_PNG_TAG;
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_LOGO_VALUE) {//logo图标
                    userStr = Constant.MAIN_LOGO_PNG_TAG;
                }
                if (!TextUtils.isEmpty(userStr)) {
                    FileUtil.createDir(Constant.configuration_picture_dir);
                    jni.creationFileDownload(Constant.configuration_picture_dir + userStr + ".png", mediaid, 1, 0, userStr);
                }
            }
            for (int i = 0; i < onlytextList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo itemInfo = onlytextList.get(i);
                int faceid = itemInfo.getFaceid();
                String text = itemInfo.getText().toStringUtf8();
                LogUtil.i(TAG, "queryInterFaceConfiguration onlytextList-->" + faceid + ", text= " + text);
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COLTDTEXT_VALUE) {//公司名称
                    view.updateCompany(text);
                }
//                else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERCOMPANY_VALUE) {//参会人单位
//                    view.updateUnit(text);
//                }
//                else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_COLTDTEXT_VALUE) {//公司名称
//                    view.updateUnit(text);
//                }
            }
            for (int i = 0; i < textList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FaceTextItemInfo itemInfo = textList.get(i);
                int faceid = itemInfo.getFaceid();
                LogUtil.i(TAG, "queryInterFaceConfiguration -->" + "faceid= " + faceid);
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEETNAME_VALUE) {//会议名称
                    view.updateTv(R.id.meet_tv_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERCOMPANY_VALUE) {//参会人单位
                    view.updateTv(R.id.unit_tv_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERNAME_VALUE) {//参会人名称
                    view.updateTv(R.id.member_tv_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERJOB_VALUE) {//参会人职业
                    view.updateTv(R.id.post_tv_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_SEATNAME_VALUE) {//座席名称
                    view.updateTv(R.id.seat_tv_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_TIMER_VALUE) {//日期时间
                    view.updateDate(R.id.date_relative_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COMPANY_VALUE) {//单位名称
                    view.updateTv(R.id.company_tv_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_LOGO_GEO_VALUE) {//Logo图标,只需要更新位置坐标
                    view.update(R.id.logo_iv_main, itemInfo);
                    boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (itemInfo.getFlag() & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
                    view.isShowLogo(isShow);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_checkin_GEO_VALUE) {//进入会议按钮 text
//                    view.updateEnterView(R.id.slideview_main, itemInfo);
                    view.updateBtn(R.id.enter_btn_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_manage_GEO_VALUE) {//进入后台 text
                    view.updateBtn(R.id.set_btn_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_topstatus_GEO_VALUE) {//会议状态
                    view.updateTv(R.id.meet_state, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_remark_GEO_VALUE) {//备注信息
                    view.updateTv(R.id.note_info, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_ver_GEO_VALUE) {//软件版本
                    view.updateTv(R.id.app_version, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_role_GEO_VALUE) {//角色
                    view.updateTv(R.id.member_role, itemInfo);
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryDevMeetInfo() {
        try {
            InterfaceDevice.pbui_Type_DeviceFaceShowDetail devMeetInfo = jni.queryDeviceMeetInfo();
            if (devMeetInfo == null) return;
            view.updateUI(devMeetInfo);
            //缓存参会人信息(不然收不到参会人变更通知)
            jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE);
            //缓存会议信息
            jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO_VALUE);
            //缓存排位信息(不然收不到排位变更通知)
            jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT_VALUE);
            //查询参会人单位
            InterfaceMember.pbui_Type_MeetMembeProperty info = jni.queryMemberProperty(InterfaceMacro.Pb_MemberPropertyID.Pb_MEETMEMBER_PROPERTY_COMPANY_VALUE, 0);
            if (info != null) {
                String unit = info.getPropertytext().toStringUtf8();
                LogUtil.d(TAG, "queryDevMeetInfo -->" + "是否是单位：" + unit);
                view.updateUnit(unit);
            }
            InterfaceMember.pbui_Type_MeetMembeProperty info1 = jni.queryMemberProperty(InterfaceMacro.Pb_MemberPropertyID.Pb_MEETMEMBER_PROPERTY_COMMENT_VALUE, 0);
            if (info1 != null) {
                view.updateNote(info1.getPropertytext().toStringUtf8());
            }
            queryLocalRole();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryMeetingState(int meetingid) {
        byte[] bytes = jni.queryMeetingProperty(InterfaceMacro.Pb_MeetPropertyID.Pb_MEET_PROPERTY_STATUS_VALUE, meetingid, 0);
        if (bytes == null) {
            return;
        }
        try {
            InterfaceBase.pbui_CommonInt32uProperty info = InterfaceBase.pbui_CommonInt32uProperty.parseFrom(bytes);
            int propertyval = info.getPropertyval();
            LogUtil.i(TAG, "queryMeetingState -->" + "会议状态：" + propertyval);
            view.updateMeetingState(propertyval);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryLocalRole() {
        try {
            InterfaceBase.pbui_CommonInt32uProperty property = jni.queryMeetRankingProperty(0, InterfaceMacro.Pb_MeetSeatPropertyID.Pb_MEETSEAT_PROPERTY_ROLEBYMEMBERID.getNumber());
            if (property == null) return;
            int propertyval = property.getPropertyval();
            if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere.getNumber()
                    || propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary.getNumber()
                    || propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin.getNumber()) {
                //当前是主持人或秘书或管理员，设置拥有所有权限
                MyApplication.hasAllPermissions = true;
                if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere.getNumber()) {
                    view.updateMemberRole(cxt.getString(R.string.member_role_host));
                } else if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary.getNumber()) {
                    view.updateMemberRole(cxt.getString(R.string.member_role_secretary));
                } else {
                    view.updateMemberRole(cxt.getString(R.string.member_role_admin));
                }
            } else {
                MyApplication.hasAllPermissions = false;
                view.updateMemberRole(cxt.getString(R.string.member_role_ordinary));
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void setDevName() throws InvalidProtocolBufferException {
        if (MyApplication.localDeviceId == 0) return;
        InterfaceDevice.pbui_Type_DeviceDetailInfo devInfoById = jni.queryDevInfoById(MyApplication.localDeviceId);
        if (devInfoById == null) return;
        InterfaceDevice.pbui_Item_DeviceDetailInfo info = devInfoById.getPdevList().get(0);
        view.updateSeatName(info.getDevname().toStringUtf8());
    }

    public void bindMeeting(int meetingid, int roomId) {
        LogUtil.d(TAG, "bindMeeting -->" + "会议ID：" + meetingid);
        MyApplication.localMeetingId = meetingid;
        jni.setInterfaceState(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_CURMEETINGID_VALUE, meetingid);
        queryMeetFromId();
    }

    private void queryMeetFromId() {
        try {
            InterfaceMeet.pbui_Type_MeetMeetInfo pbui_type_meetMeetInfo = jni.queryMeetFromId(MyApplication.localMeetingId);
            if (pbui_type_meetMeetInfo == null) return;
            List<InterfaceMeet.pbui_Item_MeetMeetInfo> itemList = pbui_type_meetMeetInfo.getItemList();
            InterfaceMeet.pbui_Item_MeetMeetInfo info = itemList.get(0);
            LogUtil.d(TAG, "MainActivity.fun_queryMeetFromId :  查询指定ID的会议 --> " + info.getId());
            int roomId = info.getRoomId();
            MyApplication.localRoomId = roomId;
            MyApplication.localRoomName = info.getRoomname().toStringUtf8();
            jni.addPlaceDevice(roomId, MyApplication.localDeviceId);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryAttendPeople() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo attendPeople = jni.queryAttendPeople();
            if (attendPeople == null) return;
            memberDetailInfos.clear();
            memberDetailInfos.addAll(attendPeople.getItemList());
            queryMeetRanking();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryMeetRanking() {
        try {
            InterfaceRoom.pbui_Type_MeetSeatDetailInfo seatDetailInfo = jni.queryMeetRanking();
            if (seatDetailInfo == null) return;
            List<Integer> ids = new ArrayList<>();
            List<InterfaceRoom.pbui_Item_MeetSeatDetailInfo> seatDetailInfos = seatDetailInfo.getItemList();
            for (InterfaceRoom.pbui_Item_MeetSeatDetailInfo info : seatDetailInfos) {
//                Log.i(TAG, "queryMeetRanking: 设备ID：" + info.getSeatid() + ", 参会人员ID：" + info.getNameId() + ", 人员身份：" + info.getRole()
//                        + "，本机：" + MyApplication.localDeviceId + "," + MyApplication.localMemberId);
                if (info.getNameId() != 0) {//说明已经绑定了参会人
                    if (info.getSeatid() == MyApplication.localDeviceId) {
                        //本机已经绑定了参会人了
                        LogUtil.d(TAG, "queryMeetRanking -->" + "本机已经绑定了参会人了");
                        return;
                    }
                    ids.add(info.getNameId());//收集已经绑定设备的人员ID
                }
            }
            chooseMemberDetailInfos.clear();
            for (InterfaceMember.pbui_Item_MemberDetailInfo info : memberDetailInfos) {
                if (!ids.contains(info.getPersonid())) {//过滤已经绑定的人员
                    //添加未绑定的人员
                    chooseMemberDetailInfos.add(info);
                }
            }
            view.showBindMemberView(chooseMemberDetailInfos);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void joinMeeting(int memberId) {
        jni.modifMeetRanking(memberId, InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal_VALUE, MyApplication.localDeviceId);
    }

    public void addAttendPeople(InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo) {
        jni.addAttendPeople(memberDetailInfo);
    }

    public void sendSign(int memberid, int signType, String pwd, ByteString picdata) {
        jni.sendSign(memberid, signType, pwd, picdata);
    }
}
