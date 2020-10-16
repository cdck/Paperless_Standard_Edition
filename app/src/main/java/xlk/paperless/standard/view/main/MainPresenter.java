package xlk.paperless.standard.view.main;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaCodecInfo;
import android.text.TextUtils;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceAdmin;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Call;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.util.AppUtil;
import xlk.paperless.standard.util.CodecUtil;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.IniUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.view.MyApplication;

import static xlk.paperless.standard.data.Values.localDeviceId;


/**
 * @author xlk
 * @date 2020/3/9
 * @desc
 */
public class MainPresenter extends BasePresenter {
    private final String TAG = "MainPresenter-->";
    private IniUtil iniUtil = IniUtil.getInstance();
    private WeakReference<Context> cxt;
    private WeakReference<IMain> view;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberDetailInfos = new ArrayList<>();
    /**
     * 存放未绑定的人员
     */
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> chooseMemberDetailInfos = new ArrayList<>();

    public MainPresenter(Context cxt, IMain iMain) {
        super();
        this.cxt = new WeakReference<>(cxt);
        this.view = new WeakReference<>(iMain);
    }

    public void setInterfaceState() {
        //  修改本机界面状态
        jni.setInterfaceState(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_ROLE_VALUE,
                InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MainFace_VALUE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cxt.clear();
        view.clear();
        memberDetailInfos.clear();
        memberDetailInfos = null;
        chooseMemberDetailInfos.clear();
        chooseMemberDetailInfos = null;
    }

    /**
     * 复制ini、dev文件
     */
    public void initConfFile() {
        //拷贝配置文件
        if (!IniUtil.iniFile.exists()) {
            LogUtil.d(TAG, "initConfFile :  拷贝配置文件 --> ");
            copyTo(Constant.INI_NAME, Constant.INI_NAME);
        } else {
            LogUtil.d(TAG, "initConfFile :  已有ini文件 --> ");
        }
        //设置版本信息
        setVersion();
        getMaxBitRate();
        File devFile = new File(Constant.DEV_FILE_PATH);
        if (!devFile.exists()) {
            copyTo(Constant.DEV_NAME, Constant.DEV_NAME);
        } else {
            devFile.delete();
            copyTo(Constant.DEV_NAME, Constant.DEV_NAME);
        }
    }

    private void getMaxBitRate() {
        if (iniUtil.loadFile(IniUtil.iniFile)) {
            String s = iniUtil.get("OtherConfiguration", "maxBitRate");
            try {
                MyApplication.maxBitRate = Integer.parseInt(s) * 1000;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 复制文件
     */
    private void copyTo(String fromPath, String fileName) {
        FileUtil.createDir(Constant.ROOT_DIR);
        File toFile = new File(Constant.ROOT_DIR);
        try {
            // 根据文件名获取assets文件夹下的该文件的inputstream
            InputStream fromFileIs = cxt.get().getResources().getAssets().open(fromPath);
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
                iniUtil.loadFile(IniUtil.iniFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setVersion() {
        LogUtil.d(TAG, "setVersion -->设置版本信息");
        PackageManager pm = cxt.get().getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(cxt.get().getPackageName(), 0);
            LogUtil.d(TAG, "当前版本名称：" + packageInfo.versionName + ", 版本号：" + packageInfo.versionCode);
            view.get().updateVersion(packageInfo.versionName);
            String hardver = "";
            String softver = "";
            if (packageInfo.versionName.contains(".")) {
                hardver = packageInfo.versionName.substring(0, packageInfo.versionName.indexOf("."));
                softver = packageInfo.versionName.substring(packageInfo.versionName.indexOf(".") + 1, packageInfo.versionName.length());
            }
            if (iniUtil.loadFile(IniUtil.iniFile)) {
                LogUtil.i(TAG, "setVersion 设置到ini文件中");
                iniUtil.put("selfinfo", "hardver", hardver);
                iniUtil.put("selfinfo", "softver", softver);
                iniUtil.store();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initialization() {
        MyApplication.threadPool.execute(() -> {
            jni.javaInitSys(AppUtil.getUniqueId(cxt.get()));
        });
    }

    @Override
    public void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case Constant.BUS_NET_WORK: {
                int isAvailable = (int) msg.getObjects()[0];
                LogUtil.i(TAG, "网络变更 -->" + isAvailable);
                if (Values.isOneline == -1) return;
                if (Values.isOneline != isAvailable) {
                    Values.isOneline = isAvailable;
                    if (!Values.initializationIsOver) {
                        view.get().checkNetWork();
                    }
                }
                break;
            }
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_TIME_VALUE: {
                Object[] objs = msg.getObjects();
                byte[] data = (byte[]) objs[0];
                InterfaceBase.pbui_Time pbui_time = InterfaceBase.pbui_Time.parseFrom(data);
                //微秒 转换成毫秒 除以 1000
                view.get().updateTime(pbui_time.getUsec() / 1000);
                break;
            }
            case Constant.BUS_MAIN_LOGO: {
                String filepath = (String) msg.getObjects()[0];
                Drawable drawable = Drawable.createFromPath(filepath);
                view.get().updateLogo(drawable);
                break;
            }
            case Constant.BUS_MAIN_BG: {
                String filepath1 = (String) msg.getObjects()[0];
                Drawable drawable1 = Drawable.createFromPath(filepath1);
                view.get().updateBackground(drawable1);
                break;
            }
            //平台登陆验证返回
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEVALIDATE_VALUE: {
                byte[] s = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_Type_DeviceValidate deviceValidate = InterfaceBase.pbui_Type_DeviceValidate.parseFrom(s);
                int valflag = deviceValidate.getValflag();
                List<Integer> valList = deviceValidate.getValList();
                List<Long> user64BitdefList = deviceValidate.getUser64BitdefList();
                String binaryString = Integer.toBinaryString(valflag);
                LogUtil.i(TAG, "initFailed valflag=" + valflag + "，二进制：" + binaryString + ", valList=" + valList.toString() + ", user64List=" + user64BitdefList.toString());
                int count = 0, index;
                //  1 1101 1111
                char[] chars = binaryString.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if ((chars[chars.length - 1 - i]) == '1') {
                        //有效位个数+1
                        count++;
                        //有效位当前位于valList的索引（跟i是无关的）
                        index = count - 1;
                        int code = valList.get(index);
                        LogUtil.d(TAG, "initFailed 有效位：" + i + ",当前有效位的个数：" + count);
                        switch (i) {
                            case 0:
                                LogUtil.e(TAG, "initFailed 区域服务器ID：" + code);
                                break;
                            case 1:
                                LogUtil.e(TAG, "initFailed 设备ID：" + code);
                                Values.localDeviceId = code;
                                break;
                            case 2:
                                LogUtil.e(TAG, "initFailed 状态码：" + code);
                                ToastUtil.errorToast(code);
                                break;
                            case 3:
                                LogUtil.e(TAG, "initFailed 到期时间：" + code);
                                break;
                            case 4:
                                LogUtil.e(TAG, "initFailed 企业ID：" + code);
                                break;
                            case 5:
                                LogUtil.e(TAG, "initFailed 协议版本：" + code);
                                break;
                            case 6:
                                LogUtil.e(TAG, "initFailed 注册时自定义的32位整数值：" + code);
                                break;
                            case 7:
                                LogUtil.e(TAG, "initFailed 当前在线设备数：" + code);
                                break;
                            case 8:
                                LogUtil.e(TAG, "initFailed 最大在线设备数：" + code);
                                break;
                            default:
                                break;
                        }
                    }
                }
                break;
            }
            //平台初始化结果
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_READY_VALUE: {
                int method = msg.getMethod();
                byte[] bytes = (byte[]) msg.getObjects()[0];
                if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    InterfaceBase.pbui_Ready error = InterfaceBase.pbui_Ready.parseFrom(bytes);
                    int areaid = error.getAreaid();
                    LogUtil.i(TAG, "BusEvent -->" + "平台初始化完毕 连接上的区域服务器ID=" + areaid);
                    Values.initializationIsOver = true;
                    view.get().initialized();
                } else if (method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_LOGON_VALUE) {
                    InterfaceBase.pbui_Type_LogonError error = InterfaceBase.pbui_Type_LogonError.parseFrom(bytes);
                    int errcode = error.getErrcode();
                    LogUtil.i(TAG, "BusEvent -->" + "平台初登陆失败通知 errcode=" + errcode);
                    Values.initializationIsOver = false;
                    ToastUtil.loginError(errcode);
                }
                break;
            }
//            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEMEETSTATUS_VALUE://界面状态变更通知
//                LogUtil.i(TAG, "BusEvent -->" + "界面状态变更通知");
//                Object[] objs1 = msg.getObjs();
//                int datalen = (int) objs1[1];
//                if (datalen > 0) {
//                    queryInterFaceConfiguration();
//                }
//                break;
            //设备会议信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEFACESHOW_VALUE: {
                LogUtil.i(TAG, "BusEvent -->" + "设备会议信息变更通知");
                queryDevMeetInfo();
                break;
            }
            //会场设备信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE_VALUE: {
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    byte[] bytes = (byte[]) msg.getObjects()[0];
                    InterfaceBase.pbui_MeetNotifyMsgForDouble info = InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(bytes);
                    //会议室id
                    int id = info.getId();
                    //设备id
                    int subid = info.getSubid();
                    //操作id
                    int opermethod = info.getOpermethod();
                    LogUtil.i(TAG, "busEvent 会场设备信息变更通知 会议室id=" + id + ", 设备id=" + subid + ", 操作id=" + opermethod);
//                    if (subid == Values.localDeviceId) {
//                        if (id == Values.localRoomId) {
//                            if (opermethod == 2) {
//                                //添加进入会议室
//                            } else if (opermethod == 4) {
//                                //从会议室移除
//                            }
//                        }
//                    }
                }
                break;
            }
            //会议信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO_VALUE: {
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg info = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(bytes);
                int id = info.getId();
                int opermethod = info.getOpermethod();
                LogUtil.i(TAG, "BusEvent -->" + "会议信息变更通知 id=" + id + ", opermethod=" + opermethod);
//                if (id != 0 && id == Values.localMeetingId) {
//                    queryMeetFromId();
//                }
                break;
            }
            //参会人员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE: {
                byte[] o = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(o);
                int id = pbui_meetNotifyMsg.getId();
                int opermethod = pbui_meetNotifyMsg.getOpermethod();
                LogUtil.i(TAG, "BusEvent -->" + "参会人员变更通知 id= " + id + ", opermethod= " + opermethod);
                queryAttendPeople();
                break;
            }
            //界面配置变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETFACECONFIG_VALUE: {
                LogUtil.i(TAG, "BusEvent -->" + "界面配置变更通知");
                queryInterFaceConfiguration();
                break;
            }
            //会议签到结果返回
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN_VALUE: {
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_LOGON_VALUE) {
                    //签到密码返回
                    byte[] datas = (byte[]) msg.getObjects()[0];
                    InterfaceBase.pbui_Type_MeetDBServerOperError dbServerOperError = InterfaceBase.pbui_Type_MeetDBServerOperError.parseFrom(datas);
                    int type = dbServerOperError.getType();
                    int method1 = dbServerOperError.getMethod();
                    int status = dbServerOperError.getStatus();
                    if (status == InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_DONE_VALUE) {
                        LogUtil.i(TAG, "BusEvent -->" + "签到成功，进入会议");
                        ToastUtil.show(R.string.sign_in_successfully);
                        view.get().jump2meet();
                    } else if (status == InterfaceMacro.Pb_DB_StatusCode.Pb_STATUS_PSWFAILED_VALUE) {
                        LogUtil.i(TAG, "BusEvent -->" + "签到密码错误");
                        ToastUtil.show(R.string.sign_in_password_error);
                        view.get().readySignIn();
                    }
                }
                break;
            }
            //设备交互信息
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE: {
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ENTER_VALUE) {
                    LogUtil.i(TAG, "BusEvent -->" + "辅助签到进入会议");
                    view.get().jump2meet();
                }
                break;
            }
            //会议排位变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT_VALUE: {
                byte[] datas = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg inform = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(datas);
                LogUtil.d(TAG, "BusEvent -->" + "会议排位变更通知 id=" + inform.getId() + ",operMethod=" + inform.getOpermethod());
                if (inform.getId() != 0 && inform.getId() == localDeviceId) {
                    queryLocalRole();
                }
                break;
            }
            //管理员登录返回
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ADMIN_VALUE: {
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_LOGON_VALUE) {
                    LogUtil.i(TAG, "BusEvent 管理员登录返回");
                    byte[] bytes = (byte[]) msg.getObjects()[0];
                    InterfaceAdmin.pbui_Type_AdminLogonStatus info = InterfaceAdmin.pbui_Type_AdminLogonStatus.parseFrom(bytes);
                    if (info != null) {
                        view.get().loginStatus(info);
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * 初始化流通道
     */
    public void initStream() {
        int format = CodecUtil.selectColorFormat(Objects.requireNonNull(CodecUtil.selectCodec("video/avc")), "video/avc");
        switch (format) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                Call.COLOR_FORMAT = 0;
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                Call.COLOR_FORMAT = 1;
                break;
            default:
                break;
        }
        jni.InitAndCapture(0, 2);
        jni.InitAndCapture(0, 3);
    }

    public void queryContextProperty() {
//        try {
//            InterfaceContext.pbui_MeetContextInfo pbui_meetContextInfo = jni.queryContextProperty(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_SELFID_VALUE);
//            if (pbui_meetContextInfo != null) {
//                Values.localDeviceId = pbui_meetContextInfo.getPropertyval();
        LogUtil.d(TAG, "queryContextProperty -->本机的设备ID：" + Values.localDeviceId);
        setDevName();
        queryInterFaceConfiguration();
        queryDevMeetInfo();
//            }
//        } catch (InvalidProtocolBufferException e) {
//            e.printStackTrace();
//        }
    }

    private void cacheData() {
        //缓存参会人信息(不然收不到参会人变更通知)
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE);
        //缓存会议信息
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO_VALUE);
        //缓存排位信息(不然收不到排位变更通知)
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT_VALUE);
    }

    private void queryInterFaceConfiguration() {
        try {
            InterfaceFaceconfig.pbui_Type_FaceConfigInfo faceConfigInfo = jni.queryInterFaceConfiguration();
            if (faceConfigInfo == null) {
                return;
            }
            List<InterfaceFaceconfig.pbui_Item_FacePictureItemInfo> pictureList = faceConfigInfo.getPictureList();
            List<InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo> onlytextList = faceConfigInfo.getOnlytextList();
            List<InterfaceFaceconfig.pbui_Item_FaceTextItemInfo> textList = faceConfigInfo.getTextList();
            for (int i = 0; i < pictureList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FacePictureItemInfo itemInfo = pictureList.get(i);
                int faceid = itemInfo.getFaceid();
                LogUtil.i(TAG, "queryInterFaceConfiguration pictureList faceid=" + faceid);
                int mediaid = itemInfo.getMediaid();
                String userStr = "";
                //主界面背景
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MAINBG_VALUE) {
                    userStr = Constant.MAIN_BG_PNG_TAG;
                    //logo图标
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_LOGO_VALUE) {
                    userStr = Constant.MAIN_LOGO_PNG_TAG;
                }
                if (!TextUtils.isEmpty(userStr)) {
                    FileUtil.createDir(Constant.DIR_PICTURE);
                    jni.creationFileDownload(Constant.DIR_PICTURE + userStr + ".png", mediaid, 1, 0, userStr);
                }
            }
            for (int i = 0; i < onlytextList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo itemInfo = onlytextList.get(i);
                int faceid = itemInfo.getFaceid();
                LogUtil.i(TAG, "queryInterFaceConfiguration onlytextList faceid=" + faceid);
                String text = itemInfo.getText().toStringUtf8();
                //公司名称
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COLTDTEXT_VALUE) {
                    view.get().updateCompany(text);
                    break;
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
                LogUtil.i(TAG, "queryInterFaceConfiguration textList faceid=" + faceid);
                if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEETNAME_VALUE) {//会议名称
                    view.get().updateTv(R.id.meet_tv_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERCOMPANY_VALUE) {//参会人单位
                    view.get().updateTv(R.id.unit_tv_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERNAME_VALUE) {//参会人名称
                    view.get().updateTv(R.id.member_tv_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERJOB_VALUE) {//参会人职业
                    view.get().updateTv(R.id.post_tv_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_SEATNAME_VALUE) {//座席名称
                    view.get().updateTv(R.id.seat_tv_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_TIMER_VALUE) {//日期时间
                    view.get().updateDate(R.id.date_relative_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COMPANY_VALUE) {//单位名称
                    view.get().updateTv(R.id.company_tv_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_LOGO_GEO_VALUE) {//Logo图标,只需要更新位置坐标
                    view.get().update(R.id.logo_iv_main, itemInfo);
                    boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (itemInfo.getFlag() & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
                    view.get().isShowLogo(isShow);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_checkin_GEO_VALUE) {//进入会议按钮 text
//                    view.updateEnterView(R.id.slideview_main, itemInfo);
                    view.get().updateBtn(R.id.enter_btn_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_manage_GEO_VALUE) {//进入后台 text
//                    view.get().updateBtn(R.id.set_btn_main, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_topstatus_GEO_VALUE) {//会议状态
                    view.get().updateTv(R.id.meet_state, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_remark_GEO_VALUE) {//备注信息
                    view.get().updateTv(R.id.note_info, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_ver_GEO_VALUE) {//软件版本
                    view.get().updateTv(R.id.app_version, itemInfo);
                } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_role_GEO_VALUE) {//角色
                    view.get().updateTv(R.id.member_role, itemInfo);
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryDevMeetInfo() {
        try {
            InterfaceDevice.pbui_Type_DeviceFaceShowDetail devMeetInfo = jni.queryDeviceMeetInfo();
            if (devMeetInfo == null) {
                return;
            }
            Values.localMeetingId = devMeetInfo.getMeetingid();
            Values.localMemberId = devMeetInfo.getMemberid();
            Values.localRoomId = devMeetInfo.getRoomid();
            Values.localSigninType = devMeetInfo.getSigninType();
            Values.localMeetingName = devMeetInfo.getMeetingname().toStringUtf8();
            Values.localMemberName = devMeetInfo.getMembername().toStringUtf8();
            LogUtil.i(TAG, "queryDevMeetInfo 设备会议信息："
                    + "\n设备id=" + devMeetInfo.getDeviceid()
                    + "\n会议id=" + Values.localMeetingId
                    + "\n人员id=" + Values.localMemberId
                    + "\n会场ID=" + Values.localRoomId
                    + "\n签到类型=" + Values.localSigninType
                    + "\n会议名称=" + Values.localMeetingName
                    + "\n人员名称=" + Values.localMemberName
                    + "\n公司名称=" + devMeetInfo.getCompany().toStringUtf8()
                    + "\n职位名称=" + devMeetInfo.getJob().toStringUtf8()
            );
            cacheData();
            view.get().updateUI(devMeetInfo);
            if (Values.localMeetingId != 0) {
                queryMeetingState();
            } else {
                view.get().updateMeetingState(-1);
            }
            if (Values.localMemberId != 0) {
                //查询参会人单位
                InterfaceMember.pbui_Type_MeetMembeProperty info = jni.queryMemberProperty(InterfaceMacro.Pb_MemberPropertyID.Pb_MEETMEMBER_PROPERTY_COMPANY_VALUE, 0);
                if (info != null) {
                    String unit = info.getPropertytext().toStringUtf8();
                    LogUtil.d(TAG, "queryDevMeetInfo --> 单位：" + unit);
                    view.get().updateUnit(unit);
                }
                //查询备注
                InterfaceMember.pbui_Type_MeetMembeProperty info1 = jni.queryMemberProperty(InterfaceMacro.Pb_MemberPropertyID.Pb_MEETMEMBER_PROPERTY_COMMENT_VALUE, 0);
                if (info1 != null) {
                    String noteinfo = info1.getPropertytext().toStringUtf8();
                    LogUtil.d(TAG, "queryDevMeetInfo --> 备注：" + noteinfo);
                    view.get().updateNote(cxt.get().getString(R.string.note_info_, noteinfo));
                }
                queryLocalRole();
            } else {
                view.get().updateMemberRole("");
                view.get().updateNote("");
                view.get().updateUnit("");
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryMeetingState() {
        byte[] bytes = jni.queryMeetingProperty(InterfaceMacro.Pb_MeetPropertyID.Pb_MEET_PROPERTY_STATUS_VALUE, Values.localMeetingId, 0);
        if (bytes == null) {
            return;
        }
        try {
            InterfaceBase.pbui_CommonInt32uProperty info = InterfaceBase.pbui_CommonInt32uProperty.parseFrom(bytes);
            int propertyval = info.getPropertyval();
            LogUtil.i(TAG, "queryMeetingState -->" + "会议状态：" + propertyval);
            view.get().updateMeetingState(propertyval);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryLocalRole() {
        try {
//            InterfaceRoom.pbui_Type_MeetSeatDetailInfo info = jni.queryMeetRanking();
//            if (info != null) {
//                List<InterfaceRoom.pbui_Item_MeetSeatDetailInfo> itemList = info.getItemList();
//                for (int i = 0; i < itemList.size(); i++) {
//                    InterfaceRoom.pbui_Item_MeetSeatDetailInfo pbui_item_meetSeatDetailInfo = itemList.get(i);
//                    int seatid = pbui_item_meetSeatDetailInfo.getSeatid();
//                    int nameId = pbui_item_meetSeatDetailInfo.getNameId();
//                    int role = pbui_item_meetSeatDetailInfo.getRole();
//                    if (seatid == localDeviceId && nameId == localMemberId) {
//                        Values.localRole = role;
//                        LogUtil.i(TAG, "queryLocalRole 本机参会人角色 " + role);
//                        if (role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere_VALUE
//                                || role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE
//                                || role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin_VALUE) {
//                            //当前是主持人或秘书或管理员，设置拥有所有权限
//                            Values.hasAllPermissions = true;
//                            if (role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere_VALUE) {
//                                view.get().updateMemberRole(cxt.get().getString(R.string.member_role_host));
//                            } else if (role == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE) {
//                                view.get().updateMemberRole(cxt.get().getString(R.string.member_role_secretary));
//                            } else {
//                                view.get().updateMemberRole(cxt.get().getString(R.string.member_role_admin));
//                            }
//                        } else {
//                            Values.hasAllPermissions = false;
//                            view.get().updateMemberRole(cxt.get().getString(R.string.member_role_ordinary));
//                        }
//                    }
//                }
//            }

            InterfaceBase.pbui_CommonInt32uProperty property = jni.queryMeetRankingProperty(
                    InterfaceMacro.Pb_MeetSeatPropertyID.Pb_MEETSEAT_PROPERTY_ROLEBYMEMBERID_VALUE);
            if (property == null) {
                return;
            }
            int propertyval = property.getPropertyval();
            Values.localRole = propertyval;
            LogUtil.i(TAG, "queryLocalRole 本机参会人角色 " + propertyval);
            if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere_VALUE
                    || propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE
                    || propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin_VALUE) {
                //当前是主持人或秘书或管理员，设置拥有所有权限
                Values.hasAllPermissions = true;
                if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere_VALUE) {
                    view.get().updateMemberRole(cxt.get().getString(R.string.member_role_host));
                } else if (propertyval == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE) {
                    view.get().updateMemberRole(cxt.get().getString(R.string.member_role_secretary));
                } else {
                    view.get().updateMemberRole(cxt.get().getString(R.string.member_role_admin));
                }
            } else {
                Values.hasAllPermissions = false;
                view.get().updateMemberRole(cxt.get().getString(R.string.member_role_ordinary));
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void setDevName() {
        if (Values.localDeviceId == 0) {
            return;
        }
        InterfaceDevice.pbui_Type_DeviceDetailInfo devInfoById = jni.queryDevInfoById(Values.localDeviceId);
        if (devInfoById == null) {
            return;
        }
        InterfaceDevice.pbui_Item_DeviceDetailInfo info = devInfoById.getPdevList().get(0);
        view.get().updateSeatName(info.getDevname().toStringUtf8());
    }

    /**
     * @param meetingid
     * @param roomId
     * @deprecated
     */
    void bindMeeting(int meetingid, int roomId) {
        LogUtil.d(TAG, "bindMeeting -->" + "会议ID：" + meetingid);
        Values.localMeetingId = meetingid;
        jni.setInterfaceState(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_CURMEETINGID_VALUE, meetingid);
        queryMeetFromId();
    }

    /**
     * 查询本机的会议，将本机添加进会议室中
     */
    private void queryMeetFromId() {
        try {
            InterfaceMeet.pbui_Type_MeetMeetInfo pbui_type_meetMeetInfo = jni.queryMeetFromId(Values.localMeetingId);
            if (pbui_type_meetMeetInfo == null) {
                return;
            }
            List<InterfaceMeet.pbui_Item_MeetMeetInfo> itemList = pbui_type_meetMeetInfo.getItemList();
            InterfaceMeet.pbui_Item_MeetMeetInfo info = itemList.get(0);
            LogUtil.d(TAG, "MainActivity.fun_queryMeetFromId :  查询指定ID的会议 --> " + info.getId());
            int roomId = info.getRoomId();
            Values.localRoomId = roomId;
            Values.localRoomName = info.getRoomname().toStringUtf8();
            jni.addPlaceDevice(roomId, Values.localDeviceId);
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
//                LogUtil.i(TAG, "queryMeetRanking: 设备id：" + info.getSeatid() + ", 参会人员id：" + info.getNameId() + ", 人员身份：" + info.getRole()
//                        + "，本机设备id和人员id：" + Values.localDeviceId + "," + Values.localMemberId);
                if (info.getNameId() != 0) {//说明已经绑定了参会人
                    if (info.getSeatid() == Values.localDeviceId) {
                        //本机已经绑定了参会人了
                        LogUtil.d(TAG, "queryMeetRanking -->" + "本机已经绑定了参会人了 参会人id=" + info.getNameId() + ", seatId=" + info.getSeatid()
                                + ",本机设备id=" + Values.localDeviceId + ",本机名称=" + Values.localDeviceName);
                        queryDevMeetInfo();
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
            view.get().showBindMemberView(chooseMemberDetailInfos);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void joinMeeting(int memberId) {
        jni.modifMeetRanking(memberId, InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal_VALUE, Values.localDeviceId);
    }

    public void addAttendPeople(InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo) {
        jni.addAttendPeople(memberDetailInfo);
    }

    public void sendSign(int memberid, int signType, String pwd, ByteString picdata) {
        jni.sendSign(memberid, signType, pwd, picdata);
    }

    public void login(String user, String pwd, int isAscill, int loginMode) {
        jni.login(user, pwd, isAscill, loginMode);
    }
}
