package xlk.paperless.standard.data;

import android.graphics.PointF;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceAgenda;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceBullet;
import com.mogujie.tt.protobuf.InterfaceContext;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceDownload;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceFilescorevote;
import com.mogujie.tt.protobuf.InterfaceIM;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceMeetfunction;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfacePlaymedia;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.mogujie.tt.protobuf.InterfaceSignin;
import com.mogujie.tt.protobuf.InterfaceStop;
import com.mogujie.tt.protobuf.InterfaceStream;
import com.mogujie.tt.protobuf.InterfaceUpload;
import com.mogujie.tt.protobuf.InterfaceVideo;
import com.mogujie.tt.protobuf.InterfaceVote;
import com.mogujie.tt.protobuf.InterfaceWhiteboard;

import java.util.List;

import xlk.paperless.standard.util.IniUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.MyApplication;

import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc
 */
public class JniHandler {
    private final String TAG = "JniHandler-->";
    private Call jni;
    private static JniHandler instance;

    public static JniHandler getInstance() {
        if (instance == null) {
            synchronized (JniHandler.class) {
                if (instance == null) {
                    instance = new JniHandler();
                }
            }
        }
        return instance;
    }

    private JniHandler() {
        jni = Call.getInstance();
    }


    /**
     * 初始化无纸化网络平台
     *
     * @param uniqueId
     * @return
     */
    public boolean javaInitSys(String uniqueId) {
        LogUtil.i(TAG, "javaInitSys -->" + uniqueId);
        InterfaceBase.pbui_MeetCore_InitParam.Builder tmp = InterfaceBase.pbui_MeetCore_InitParam.newBuilder();
        tmp.setPconfigpathname(s2b(IniUtil.iniFile.getAbsolutePath()));
        tmp.setProgramtype(InterfaceMacro.Pb_ProgramType.Pb_MEET_PROGRAM_TYPE_MEETCLIENT.getNumber());
        tmp.setStreamnum(4);
        tmp.setLogtofile(0);
        tmp.setKeystr(s2b(uniqueId));
        InterfaceBase.pbui_MeetCore_InitParam pb = tmp.build();
        boolean bret = true;
        if (-1 == jni.Init_walletSys(pb.toByteArray())) {
            bret = false;
        }
        LogUtil.d(TAG, "javaInitSys --> 初始化结果：" + bret);
        return bret;
    }

    public void InitAndCapture(int type, int channelindex) {
        jni.InitAndCapture(type, channelindex);
    }

    /**
     * 按属性ID查询指定上下文属性
     *
     * @param propertyid
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceContext.pbui_MeetContextInfo queryContextProperty(int propertyid) throws InvalidProtocolBufferException {
        InterfaceContext.pbui_QueryMeetContextInfo.Builder builder = InterfaceContext.pbui_QueryMeetContextInfo.newBuilder();
        builder.setPropertyid(propertyid);
        InterfaceContext.pbui_QueryMeetContextInfo build = builder.build();
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETCONTEXT_VALUE,
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY_VALUE, build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "queryContextProperty :  按属性ID查询指定上下文属性失败 --->>> ");
            return null;
        }
        LogUtil.e(TAG, "queryContextProperty:    按属性ID查询指定上下文属性成功 --->>> ");
        return InterfaceContext.pbui_MeetContextInfo.parseFrom(array);
    }

    public InterfaceDevice.pbui_Type_DeviceDetailInfo queryDevInfoById(int devid) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID build = InterfaceBase.pbui_QueryInfoByID.newBuilder()
                .setId(devid).build();
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO_VALUE,
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SINGLEQUERYBYID_VALUE, build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "queryDevInfoById :  查询指定ID的设备信息失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryDevInfoById :  查询指定ID的设备信息成功 --> ");
        return InterfaceDevice.pbui_Type_DeviceDetailInfo.parseFrom(array);
    }

    public InterfaceDevice.pbui_Type_DeviceFaceShowDetail queryDeviceMeetInfo() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEFACESHOW_VALUE,
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY_VALUE, null);
        if (array == null) {
            LogUtil.e(TAG, "queryDeviceMeetInfo:  110.查询设备会议信息 --->>> 失败");
            return null;
        }
        LogUtil.i(TAG, "queryDeviceMeetInfo:  110.查询设备会议信息 --->>> 成功");
        return InterfaceDevice.pbui_Type_DeviceFaceShowDetail.parseFrom(array);
    }

    /**
     * 查询界面配置
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceFaceconfig.pbui_Type_FaceConfigInfo queryInterFaceConfiguration() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETFACECONFIG.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY_VALUE, null);
        if (array == null) {
            LogUtil.e(TAG, "queryInterFaceConfiguration :  查询界面配置 -->失败 ");
            return null;
        }
        LogUtil.i(TAG, "queryInterFaceConfiguration :  查询界面配置 -->成功 ");
        return InterfaceFaceconfig.pbui_Type_FaceConfigInfo.parseFrom(array);
    }

    /**
     * 缓存会议数据
     *
     * @param type
     * @return
     * @throws InvalidProtocolBufferException
     */
    public void cacheData(int type) {
        InterfaceBase.pbui_MeetCacheOper.Builder builder = InterfaceBase.pbui_MeetCacheOper.newBuilder();
        builder.setCacheflag(InterfaceMacro.Pb_CacheFlag.Pb_MEET_CACEH_FLAG_ZERO.getNumber());
        builder.setId(1);
        jni.call_method(type, InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_CACHE.getNumber(), builder.build().toByteArray());
        LogUtil.e(TAG, "cacheData :  缓存会议数据 --> " + type);
    }

    /**
     * @param pathname   下载媒体全路径名称
     * @param mediaid    媒体ID
     * @param newfile    =0 不覆盖同名文件,=1 覆盖下载
     * @param onlyfinish =1 表示只需要结束的通知
     * @param userStr    用户传入的自定义字串标识
     */
    public void creationFileDownload(String pathname, int mediaid, int newfile, int onlyfinish, String userStr) {
        InterfaceDownload.pbui_Type_DownloadStart.Builder builder = InterfaceDownload.pbui_Type_DownloadStart.newBuilder();
        builder.setMediaid(mediaid);
        builder.setNewfile(newfile);
        builder.setOnlyfinish(onlyfinish);
        LogUtil.e(TAG, "creationFileDownload:  mediaid --->>> " + mediaid + "  文件：" + pathname);
        builder.setPathname(s2b(pathname));
        builder.setUserstr(s2b(userStr));
        InterfaceDownload.pbui_Type_DownloadStart build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DOWNLOAD.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD_VALUE, build.toByteArray());
        LogUtil.e(TAG, "creationFileDownload:  创建一个文件下载 --->>>pathname=  " + pathname);
    }

    /**
     * 修改本机界面状态
     * Pb_MemState_MainFace=0; //处于主界面
     * Pb_MemState_MemFace=1;//参会人员界面
     * Pb_MemState_AdminFace=2;//后台管理界面
     *
     * @param propertyid
     * @param value
     */
    public void setInterfaceState(int propertyid, int value) {
        InterfaceContext.pbui_MeetContextInfo.Builder builder = InterfaceContext.pbui_MeetContextInfo.newBuilder();
        builder.setPropertyid(propertyid);
        builder.setPropertyval(value);
        InterfaceContext.pbui_MeetContextInfo build = builder.build();
        byte[] bytes = build.toByteArray();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETCONTEXT.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SETPROPERTY.getNumber(), bytes);
        LogUtil.e(TAG, "setInterfaceState:  修改本机界面状态 为 --->>> " + value);
    }

    /**
     * 129.查询指定ID的会议
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceMeet.pbui_Type_MeetMeetInfo queryMeetFromId(int value) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID.Builder builder = InterfaceBase.pbui_QueryInfoByID.newBuilder();
        builder.setId(value);
        InterfaceBase.pbui_QueryInfoByID build = builder.build();
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SINGLEQUERYBYID.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "queryMeetFromId :  查询指定ID的会议失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryMeetFromId :  查询指定ID的会议成功 --> ");
        return InterfaceMeet.pbui_Type_MeetMeetInfo.parseFrom(array);
    }

    /**
     * 120.添加会场设备
     *
     * @return
     */
    public void addPlaceDevice(int roomid, int devid) {
        InterfaceRoom.pbui_Type_MeetRoomModDeviceInfo.Builder builder = InterfaceRoom.pbui_Type_MeetRoomModDeviceInfo.newBuilder();
        builder.setRoomid(roomid);
        builder.addDeviceid(devid);
        LogUtil.e(TAG, "addPlaceDevice : 添加会场设备操作  --> 会议室ID: " + roomid + ", 设备ID: " + devid);
        InterfaceRoom.pbui_Type_MeetRoomModDeviceInfo build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD_VALUE, build.toByteArray());
        LogUtil.e(TAG, "addPlaceDevice :  添加会场设备 --> ");
    }

    /**
     * 92.查询参会人员
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceMember.pbui_Type_MemberDetailInfo queryAttendPeople() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY_VALUE, null);
        if (array == null) {
            LogUtil.e(TAG, "queryAttendPeople:  查询参会人员失败 --->>> ");
            return null;
        }
        LogUtil.e(TAG, "queryAttendPeopleFromId:  查询参会人员成功 --->>> ");

        return InterfaceMember.pbui_Type_MemberDetailInfo.parseFrom(array);
    }

    /**
     * 查询参会人员属性
     *
     * @param propertyid   属性ID 参见 Pb_MemberPropertyID
     * @param parameterval 传入参数 为0表示本机设置定的人员id
     * @return
     */
    public InterfaceMember.pbui_Type_MeetMembeProperty queryMemberProperty(int propertyid, int parameterval) throws InvalidProtocolBufferException {
        InterfaceMember.pbui_Type_MeetMemberQueryProperty build = InterfaceMember.pbui_Type_MeetMemberQueryProperty.newBuilder()
                .setPropertyid(propertyid)
                .setParameterval(parameterval)
                .build();
        byte[] bytes = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE,
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY_VALUE, build.toByteArray());
        if (bytes == null) {
            LogUtil.e(TAG, "queryMemberProperty -->" + "查询参会人员属性失败");
            return null;
        }
        LogUtil.d(TAG, "queryMemberProperty -->" + "查询参会人员属性成功");
        return InterfaceMember.pbui_Type_MeetMembeProperty.parseFrom(bytes);
    }

    /**
     * 181.查询会议排位
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceRoom.pbui_Type_MeetSeatDetailInfo queryMeetRanking() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY_VALUE, null);
        if (array == null) {
            LogUtil.e(TAG, "queryMeetRanking :  查询会议排位失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryMeetRanking :  查询会议排位成功 --> ");
        return InterfaceRoom.pbui_Type_MeetSeatDetailInfo.parseFrom(array);
    }


    /**
     * 扫码加入会议
     *
     * @param meetingid         会议ID
     * @param Pb_MeetMemberRole 参会人身份
     * @param memberDetailInfo  参会人
     */
    public void joinMeeting(int meetingid, int Pb_MeetMemberRole, InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo) {
        InterfaceMember.pbui_Type_ScanEnterMeet.Builder builder = InterfaceMember.pbui_Type_ScanEnterMeet.newBuilder();
        builder.setMeetingid(meetingid);
        builder.setMemberrole(Pb_MeetMemberRole);
        builder.setMemberinfo(memberDetailInfo);
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ENTER.getNumber(), builder.build().toByteArray());
        LogUtil.e(TAG, "joinMeeting :  扫码加入会议 --> ");
    }

    /**
     * 182.修改会议排位
     *
     * @return
     */
    public void modifMeetRanking(int nameid, int role, int seatid) {
        InterfaceRoom.pbui_Item_MeetSeatDetailInfo.Builder builder1 = InterfaceRoom.pbui_Item_MeetSeatDetailInfo.newBuilder();
        builder1.setNameId(nameid);
        builder1.setSeatid(seatid);
        builder1.setRole(role);
        InterfaceRoom.pbui_Type_MeetSeatDetailInfo.Builder builder = InterfaceRoom.pbui_Type_MeetSeatDetailInfo.newBuilder();
        builder.addItem(builder1);
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_MODIFY_VALUE, builder.build().toByteArray());
        LogUtil.e(TAG, "modifMeetRanking:  修改会议排位 --->>>nameid: " + nameid + ", role:  " + role + " , devid: " + seatid);
    }

    /**
     * 93.添加参会人员
     *
     * @param info
     * @return
     */
    public void addAttendPeople(InterfaceMember.pbui_Item_MemberDetailInfo info) {
        InterfaceMember.pbui_Type_MemberDetailInfo build = InterfaceMember.pbui_Type_MemberDetailInfo.newBuilder()
                .addItem(info).build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "addAttendPeople:  添加参会人员 --->>> ");
    }

    /**
     * 207.发送签到
     *
     * @param memberid 签到的人员ID,为0表示当前绑定的人员
     * @param signType 签到方式
     * @param pwd      密码
     * @param picdata  数据类型
     * @return
     */
    public void sendSign(int memberid, int signType, String pwd, ByteString picdata) {
        InterfaceSignin.pbui_Type_DoMeetSignIno.Builder builder = InterfaceSignin.pbui_Type_DoMeetSignIno.newBuilder();
        builder.setMemberid(memberid);
        builder.setSigninType(signType);
        builder.setPassword(s2b(pwd));
        builder.setPsigndata(picdata);
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD.getNumber(), builder.build().toByteArray());
        LogUtil.e(TAG, "sendSign:  发送签到 --->>> signType: " + signType);
    }

    /**
     * 238.查询会议功能
     *
     * @return Pb_Meet_FunctionCode
     * @throws InvalidProtocolBufferException
     */
    public InterfaceMeetfunction.pbui_Type_MeetFunConfigDetailInfo queryMeetFunction() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FUNCONFIG.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "queryMeetFunction :  查询会议功能失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryMeetFunction :  查询会议功能成功 --> ");
        return InterfaceMeetfunction.pbui_Type_MeetFunConfigDetailInfo.parseFrom(array);
    }

    /**
     * 按属性ID查询指定设备属性
     *
     * @param propetyid InterfaceMacro.Pb_MeetDevicePropertyID
     * @param devId     =0是本机
     * @return pbui_DeviceInt32uProperty（整数）如果是查询的网络状态=0离线，=1在线
     * pbui_DeviceStringProperty（字符串）
     */
    public byte[] queryDevicePropertiesById(int propetyid, int devId) {
        InterfaceDevice.pbui_MeetDeviceQueryProperty.Builder builder = InterfaceDevice.pbui_MeetDeviceQueryProperty.newBuilder();
        builder.setPropertyid(propetyid);
        builder.setDeviceid(devId);
        builder.setParamterval(0);
        InterfaceDevice.pbui_MeetDeviceQueryProperty build = builder.build();
        byte[] bytes = build.toByteArray();
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY.getNumber(), bytes);
        if (array == null) {
            LogUtil.e(TAG, "queryDevicePropertiesById :  按属性ID查询指定设备属性失败 --->>> ");
            return null;
        }
        LogUtil.e(TAG, "queryDevicePropertiesById:  按属性ID查询指定设备属性成功 --->>> ");
        return array;
    }

    /**
     * 查询会议属性
     *
     * @param propertyid Pb_MeetPropertyID
     * @param val1
     * @param val2
     * @return pbui_CommonInt32uProperty\pbui_CommonInt64uProperty
     */
    public byte[] queryMeetingProperty(int propertyid, int val1, int val2) {
        InterfaceBase.pbui_CommonQueryProperty build = InterfaceBase.pbui_CommonQueryProperty.newBuilder()
                .setPropertyid(propertyid)
                .setParameterval(val1)
                .setParameterval2(val2)
                .build();
        byte[] bytes = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO_VALUE,
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY_VALUE, build.toByteArray());
        if (bytes == null) {
            LogUtil.e(TAG, "queryMeetingProperty -->" + "查询会议属性失败");
            return null;
        }
        LogUtil.d(TAG, "queryMeetingProperty -->" + "查询会议属性成功");
        return bytes;
    }

    /**
     * 136.查询会议目录
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceFile.pbui_Type_MeetDirDetailInfo queryMeetDir() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORY.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "queryMeetDir :  查询会议目录失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryMeetDir :  查询会议目录成功 --> ");
        return InterfaceFile.pbui_Type_MeetDirDetailInfo.parseFrom(array);
    }


    /**
     * 143.查询会议目录文件
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceFile.pbui_Type_MeetDirFileDetailInfo queryMeetDirFile(int dirId) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID.Builder builder = InterfaceBase.pbui_QueryInfoByID.newBuilder();
        builder.setId(dirId);
        InterfaceBase.pbui_QueryInfoByID build = builder.build();
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "queryMeetDirFile :  查询会议目录文件失败 --> " + dirId);
            return null;
        }
        LogUtil.e(TAG, "queryMeetDirFile :  查询会议目录文件成功 --> " + dirId);
        return InterfaceFile.pbui_Type_MeetDirFileDetailInfo.parseFrom(array);
    }

    /**
     * 查询设备信息
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceDevice.pbui_Type_DeviceDetailInfo queryDeviceInfo() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "queryDeviceInfo :  查询设备信息失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryDeviceInfo :  查询设备信息成功 --> ");
        return InterfaceDevice.pbui_Type_DeviceDetailInfo.parseFrom(array);
    }


    /**
     * 媒体播放操作
     *
     * @param mediaid
     * @param devIds
     * @param pos
     * @param res
     * @param triggeruserval
     * @param flag           InterfaceMacro.Pb_MeetPlayFlag
     */
    public void mediaPlayOperate(int mediaid, List<Integer> devIds, int pos, int res, int triggeruserval, int flag) {
        InterfacePlaymedia.pbui_Type_MeetDoMediaPlay.Builder builder = InterfacePlaymedia.pbui_Type_MeetDoMediaPlay.newBuilder();
        builder.setPlayflag(flag);
        builder.setMediaid(mediaid);
        builder.addAllDeviceid(devIds);
        builder.setPos(pos);
        builder.addRes(res);
        builder.setTriggeruserval(triggeruserval);
        InterfacePlaymedia.pbui_Type_MeetDoMediaPlay build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_START.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "mediaPlayOperate:  媒体播放操作 --->>> ");
    }

    /**
     * 97.查询参会人员权限
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceMember.pbui_Type_MemberPermission queryAttendPeoplePermissions() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBERPERMISSION.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "queryAttendPeoplePermissions :  查询参会人员权限失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryAttendPeoplePermissions :  查询参会人员权限成功 --> ");
        return InterfaceMember.pbui_Type_MemberPermission.parseFrom(array);
    }

    /**
     * 上传文件
     *
     * @param uploadflag Pb_Upload_Flag =0开启上传进度回调，=1上传结束才回调进度
     */
    public void uploadFile(int uploadflag, int dirid, int attrib, String newname, String pathname, int userval, int mediaid, String userStr) {
        InterfaceUpload.pbui_Type_AddUploadFile.Builder builder = InterfaceUpload.pbui_Type_AddUploadFile.newBuilder();
        builder.setUploadflag(uploadflag);
        builder.setDirid(dirid);
        builder.setAttrib(attrib);
        builder.setNewname(s2b(newname));
        builder.setPathname(s2b(pathname));
        builder.setUserval(userval);
        builder.setUserstr(s2b(userStr));
        InterfaceUpload.pbui_Type_AddUploadFile build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_UPLOAD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "uploadFile :   --> 上传文件 " + newname + ", dirid= " + dirid + ", pathname= " + pathname);
    }

    /**
     * 185.发送会议交流信息
     *
     * @return
     */
    public void sendChatMessage(String msg, int msgType, List<Integer> ids) {
        InterfaceIM.pbui_Type_SendMeetIM.Builder builder = InterfaceIM.pbui_Type_SendMeetIM.newBuilder();
        builder.setMsg(s2b(msg));
        builder.setMsgtype(msgType);
        builder.addAllUserids(ids);
        InterfaceIM.pbui_Type_SendMeetIM build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETIM.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SEND.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "sendMeetInfo:  发送会议交流信息 --->>> ");
    }

    /**
     * 查询会议排位属性
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceBase.pbui_CommonInt32uProperty queryMeetRankingProperty(int parameterval, int propertyid) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_CommonQueryProperty.Builder builder = InterfaceBase.pbui_CommonQueryProperty.newBuilder();
        builder.setParameterval(parameterval);
        builder.setPropertyid(propertyid);
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY.getNumber(), builder.build().toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "queryMeetRankingProperty :  查询会议排位属性失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryMeetRankingProperty :  查询会议排位属性成功 --> ");
        return InterfaceBase.pbui_CommonInt32uProperty.parseFrom(array);
    }

    /**
     * 获取指定会场ID的底图ID
     *
     * @param roomid 会场ID
     * @return
     * @throws InvalidProtocolBufferException
     */
    public int queryMeetRoomProperty(int roomid) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_CommonQueryProperty.Builder builder = InterfaceBase.pbui_CommonQueryProperty.newBuilder();
        builder.setParameterval(roomid);
        builder.setParameterval2(roomid);
        builder.setPropertyid(InterfaceMacro.Pb_MeetRoomPropertyID.Pb_MEETROOM_PROPERTY_BGPHOTOID.getNumber());
        byte[] bytes = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOM.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY.getNumber(), builder.build().toByteArray());
        int propertyval = 0;
        if (bytes != null) {
            InterfaceBase.pbui_CommonInt32uProperty pbui_commonInt32uProperty = InterfaceBase.pbui_CommonInt32uProperty.parseFrom(bytes);
            propertyval = pbui_commonInt32uProperty.getPropertyval();
        }
        LogUtil.e(TAG, "queryMeetRoomProperty :  获取指定会场ID的底图ID --> " + roomid + ",  propertyval:" + propertyval);
        return propertyval;
    }

    /**
     * 206.查询签到
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceSignin.pbui_Type_MeetSignInDetailInfo querySignin() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "querySignin :  查询签到失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "querySignin :  查询签到成功 --> ");
        return InterfaceSignin.pbui_Type_MeetSignInDetailInfo.parseFrom(array);
    }

    /**
     * 会场设备排位详细信息
     *
     * @param id 会议室ID
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo placeDeviceRankingInfo(int id) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID.Builder builder = InterfaceBase.pbui_QueryInfoByID.newBuilder();
        builder.setId(id);
        InterfaceBase.pbui_QueryInfoByID build = builder.build();
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DETAILINFO.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "placeDeviceRankingInfo :  查询会场设备排位详细信息失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "placeDeviceRankingInfo :  查询会场设备排位详细信息成功 --> ");
        return InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo.parseFrom(array);
    }


    /**
     * 发送请求参会人员权限请求
     *
     * @param devid     向谁的设备申请权限
     * @param privilege 需要申请的权限
     * @return
     */
    public void sendAttendRequestPermissions(int devid, int privilege) {
        InterfaceDevice.pbui_Type_MeetRequestPrivilege.Builder builder = InterfaceDevice.pbui_Type_MeetRequestPrivilege.newBuilder();
        builder.addDevid(devid);
        builder.setPrivilege(privilege);
        InterfaceDevice.pbui_Type_MeetRequestPrivilege build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_REQUESTPRIVELIGE.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "sendAttendRequestPermissions:  发送请求参会人员权限请求 --->>> 申请的权限： " + privilege);
    }


    /**
     * 172.查询会议视频
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceVideo.pbui_Type_MeetVideoDetailInfo queryMeetVedio() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVIDEO.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "queryMeetVedio :  查询会议视频失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryMeetVedio :  查询会议视频成功 --> ");
        return InterfaceVideo.pbui_Type_MeetVideoDetailInfo.parseFrom(array);
    }

    /**
     * 初始化播放资源
     *
     * @param resid
     * @param w
     * @param h
     * @return
     */
    public void initVideoRes(int resid, int w, int h) {
        InterfacePlaymedia.pbui_Type_MeetInitPlayRes.Builder builder = InterfacePlaymedia.pbui_Type_MeetInitPlayRes.newBuilder();
        builder.setRes(resid);
        builder.setY(0);
        builder.setX(0);
        builder.setW(w);
        builder.setH(h);
        InterfacePlaymedia.pbui_Type_MeetInitPlayRes build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_INIT.getNumber(), build.toByteArray());
        LogUtil.d(TAG, "initVideoRes:  初始化播放资源 --->>> " + w + "," + h);
    }

    /**
     * 释放播放资源
     *
     * @return
     */
    public void releaseVideoRes(int resValue) {
        InterfacePlaymedia.pbui_Type_MeetDestroyPlayRes.Builder builder = InterfacePlaymedia.pbui_Type_MeetDestroyPlayRes.newBuilder();
        builder.setRes(resValue);
        InterfacePlaymedia.pbui_Type_MeetDestroyPlayRes build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DESTORY.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "releaseVideoRes :  释放播放资源 --->>> resValue= " + resValue);
    }

    /**
     * 流播放(多个资源)
     *
     * @param srcdeviceid    要抓取屏幕的设备ID
     * @param subid          2：抓取屏幕 3：摄像头
     * @param triggeruserval 一般写 0  Pb_TriggerUsedef
     * @param allres         播放所用的资源
     * @param alldeviceid    通知的目标设备（进行播放的设备）
     * @return
     */
    public void streamPlay(int srcdeviceid, int subid, int triggeruserval, List<Integer> allres, List<Integer> alldeviceid) {
        InterfaceStream.pbui_Type_MeetDoStreamPlay.Builder builder = InterfaceStream.pbui_Type_MeetDoStreamPlay.newBuilder();
        builder.setSrcdeviceid(srcdeviceid);
        builder.setSubid(subid);
        builder.setTriggeruserval(triggeruserval);
        builder.addAllRes(allres);
        builder.addAllDeviceid(alldeviceid);
        InterfaceStream.pbui_Type_MeetDoStreamPlay build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_STREAMPLAY.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_START.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "streamPlay:  流播放 --->>> ");
    }

    /**
     * 流播放(单个资源)
     *
     * @param srcdeviceid    要抓取屏幕的设备ID
     * @param subid          2：抓取屏幕 3：摄像头
     * @param triggeruserval 一般写 0  Pb_TriggerUsedef
     * @param resid          播放所用的资源id
     * @param alldeviceid    通知的目标设备（进行播放的设备）
     * @return
     */
    public void streamPlay(int srcdeviceid, int subid, int triggeruserval, int resid, List<Integer> alldeviceid) {
        InterfaceStream.pbui_Type_MeetDoStreamPlay.Builder builder = InterfaceStream.pbui_Type_MeetDoStreamPlay.newBuilder();
        builder.setSrcdeviceid(srcdeviceid);
        builder.setSubid(subid);
        builder.setTriggeruserval(triggeruserval);
        builder.addRes(resid);
        builder.addAllDeviceid(alldeviceid);
        InterfaceStream.pbui_Type_MeetDoStreamPlay build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_STREAMPLAY.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_START.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "streamPlay:  流播放 --->>> ");
    }

    /**
     * 248.停止资源操作
     *
     * @return
     */
    public void stopResourceOperate(List<Integer> res, List<Integer> devIds) {
        InterfaceStop.pbui_Type_MeetDoStopResWork.Builder builder = InterfaceStop.pbui_Type_MeetDoStopResWork.newBuilder();
        builder.addAllRes(res);
        builder.addAllDeviceid(devIds);
        InterfaceStop.pbui_Type_MeetDoStopResWork build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_STOPPLAY.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_CLOSE.getNumber(), build.toByteArray());
        LogUtil.i(TAG, "stopResourceOperate:  停止资源操作  ---> ");
    }

    /**
     * 254.设置播放暂停
     *
     * @return
     */
    public void setPlayStop(int resIndex, List<Integer> devIds) {
        InterfacePlaymedia.pbui_Type_MeetDoPlayControl.Builder builder = InterfacePlaymedia.pbui_Type_MeetDoPlayControl.newBuilder();
        builder.setResindex(resIndex);
        builder.addAllDeviceid(devIds);
        InterfacePlaymedia.pbui_Type_MeetDoPlayControl build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_PAUSE.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "setPlayStop:  设置播放暂停 --->>> ");
    }

    /**
     * 255.设置播放回复
     *
     * @return
     */
    public void setPlayRecover(int resIndex, List<Integer> devIds) {
        InterfacePlaymedia.pbui_Type_MeetDoPlayControl.Builder builder = InterfacePlaymedia.pbui_Type_MeetDoPlayControl.newBuilder();
        builder.setResindex(resIndex);
        builder.addAllDeviceid(devIds);
        InterfacePlaymedia.pbui_Type_MeetDoPlayControl build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_PLAY.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "setPlayRecover:  设置播放回复 --->>> ");
    }

    /**
     * 253.设置播放位置
     *
     * @return
     */
    public void setPlayPlace(int resIndex, int pos, List<Integer> devIds, int triggeruserval, int playflag) {
        InterfacePlaymedia.pbui_Type_MeetDoSetPlayPos.Builder builder = InterfacePlaymedia.pbui_Type_MeetDoSetPlayPos.newBuilder();
        builder.setResindex(resIndex);
        builder.setPos(pos);
        builder.addAllDeviceid(devIds);
        builder.setTriggeruserval(triggeruserval);
        builder.setPlayflag(playflag);
        InterfacePlaymedia.pbui_Type_MeetDoSetPlayPos build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAY.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_MOVE.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "setPlayPlace:  设置播放位置 --->>> ");
    }

    /**
     * 网页查询
     */
    public InterfaceBase.pbui_meetUrl webQuery() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEFAULTURL.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "webQuery  网页查询 --->>> 失败");
            return null;
        }
        LogUtil.e(TAG, "webQuery  网页查询 --->>> 成功");
        return InterfaceBase.pbui_meetUrl.parseFrom(array);
    }

    /**
     * 查询议程
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceAgenda.pbui_meetAgenda queryAgenda() throws InvalidProtocolBufferException {
        InterfaceAgenda.pbui_meetAgenda build = InterfaceAgenda.pbui_meetAgenda.newBuilder()
                .setAgendatype(0)
                .build();
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETAGENDA.getNumber(),
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "queryAgenda :  查询议程失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryAgenda :  查询议程成功 --> ");
        return InterfaceAgenda.pbui_meetAgenda.parseFrom(array);
    }

    /**
     * 149.查询文件属性
     *
     * @return
     */
    public byte[] queryFileProperty(int propertyid, int parmeterval/*,int parmeterva2*/) {
        InterfaceBase.pbui_CommonQueryProperty.Builder builder = InterfaceBase.pbui_CommonQueryProperty.newBuilder();
        builder.setPropertyid(propertyid);
        builder.setParameterval(parmeterval);
//        builder.setParameterval2(parmeterva2);
        InterfaceBase.pbui_CommonQueryProperty build = builder.build();
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY.getNumber(), build.toByteArray());
        //  pbui_CommonInt64uProperty、Type_MeetMFileQueryPropertyString、
        if (array == null) {
            LogUtil.e(TAG, "queryFileProperty:  查询文件属性失败 --->>> ");
            return new byte[0];
        }
        return array;
    }

    /**
     * 根据媒体ID查询文件名
     * @param mediaId
     * @return
     */
    public String getFileName(int mediaId) {
        String fileName = "";
        byte[] bytes = queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(), mediaId);
        try {
            InterfaceBase.pbui_CommonTextProperty pbui_commonTextProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(bytes);
            fileName = pbui_commonTextProperty.getPropertyval().toStringUtf8();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * 223.白板删除记录
     *
     * @return
     */
    public void whiteBoardDeleteRecord(int memberid, int operid, int opermemberid, int srcmemid, long srcwbid, long utcstamp, int figuretype) {
        InterfaceWhiteboard.pbui_Type_MeetDoClearWhiteBoard.Builder builder = InterfaceWhiteboard.pbui_Type_MeetDoClearWhiteBoard.newBuilder();
        builder.setMemberid(memberid);
        builder.setOperid(operid);
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setUtcstamp(utcstamp);
        builder.setFiguretype(figuretype);
        InterfaceWhiteboard.pbui_Type_MeetDoClearWhiteBoard build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DEL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "whiteBoardDeleteRecord:  白板删除记录 --->>> ");
    }

    /**
     * 225.添加墨迹
     *
     * @return
     */
    public void addInk(int operid, int opermemberid, int srcmemid, long srcwbid, long utcstamp,
                       int figuretype, int linesize, int argb, List<PointF> allpinklist) {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardInkItem.Builder builder = InterfaceWhiteboard.pbui_Type_MeetWhiteBoardInkItem.newBuilder();
        builder.setOperid(operid);
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setUtcstamp(utcstamp);
        builder.setFiguretype(figuretype);
        builder.setLinesize(linesize);
        builder.setArgb(argb);
        for (int i = 0; i < allpinklist.size(); i++) {
            builder.addPinklist(allpinklist.get(i).x);
            builder.addPinklist(allpinklist.get(i).y);
        }
        LogUtil.e(TAG, "addInk   发送的xy个数--->>> " + builder.getPinklistCount());
//        builder.addAllPinklist(allpinklist);
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardInkItem build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDINK.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "addInk:  添加墨迹 --->>> ");
    }

    /**
     * 228.添加矩形、直线、圆形
     *
     * @return
     */
    public void addDrawFigure(int operid, int opermemberid, int srcmemid, long srcwbid, long utcstamp,
                              int type, int size, int color, List<Float> allpt) {
        InterfaceWhiteboard.pbui_Item_MeetWBRectDetail.Builder builder = InterfaceWhiteboard.pbui_Item_MeetWBRectDetail.newBuilder();
        builder.setOperid(operid);
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setUtcstamp(utcstamp);
        builder.setFiguretype(type);
        builder.setLinesize(size);
        builder.setArgb(color);
        builder.addAllPt(allpt);
        InterfaceWhiteboard.pbui_Item_MeetWBRectDetail build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDRECT.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "addDrawFigure:  添加矩形、直线、圆形 --->>> ");
    }


    /**
     * 231.添加文本
     *
     * @return
     */
    public void addText(int operid, int opermemberid, int srcmemid, long srcwbid, long utcstamp, int figuretype, int fontsize, int fontflag,
                        int argb, String fontname, float lx, float ly, String ptext) {
        InterfaceWhiteboard.pbui_Item_MeetWBTextDetail.Builder builder = InterfaceWhiteboard.pbui_Item_MeetWBTextDetail.newBuilder();
        builder.setOperid(operid);
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setUtcstamp(utcstamp);
        builder.setFiguretype(figuretype);
        builder.setFontsize(fontsize);
        builder.setFontflag(fontflag);
        builder.setArgb(argb);
        builder.setFontname(s2b(fontname));
        builder.setLx(lx);
        builder.setLy(ly);
        builder.setPtext(s2b(ptext));
        InterfaceWhiteboard.pbui_Item_MeetWBTextDetail build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDTEXT.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "addText:  添加文本 --->>> " + ptext);
    }

    /**
     * 217.同意加入
     *
     * @return
     */
    public void agreeJoin(int opermemberid, int srcmemid, long srcwbid) {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper.Builder builder = InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper.newBuilder();
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setOpermemberid(opermemberid);
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ENTER.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "agreeJoin :  同意加入 --> ");
    }

    /**
     * 215.拒绝加入
     *
     * @return
     */
    public void rejectJoin(int opermemberid, int srcmemid, long srcwbid) {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper.Builder builder = InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper.newBuilder();
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardOper build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_REJECT.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "rejectJoin:  拒绝加入 --->>> ");
    }

    /**
     * 213.广播本身退出白板
     *
     * @return
     */
    public void broadcastStopWhiteBoard(int operflag, String medianame, int opermemberid, int srcmemid, long srcwbid, List<Integer> alluserid) {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardControl.Builder builder = InterfaceWhiteboard.pbui_Type_MeetWhiteBoardControl.newBuilder();
        builder.setOperflag(operflag);
        builder.setMedianame(s2b(medianame));
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.addAllUserid(alluserid);
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardControl build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_CONTROL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "inquiryStartWhiteBoard:  广播本身退出白板 --->>> ");
    }

    /**
     * 209.发起白板
     *
     * @return
     */
    public void coerceStartWhiteBoard(int operFlag, String mediaName, int operMemberid, int srcmemId, long srcwbId, List<Integer> allUserId) {
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardControl.Builder tmp3 = InterfaceWhiteboard.pbui_Type_MeetWhiteBoardControl.newBuilder();
        tmp3.setOperflag(operFlag);
        tmp3.setMedianame(s2b(mediaName));
        tmp3.setOpermemberid(operMemberid);
        tmp3.setSrcmemid(srcmemId);
        tmp3.setSrcwbid(srcwbId);
        tmp3.addAllUserid(allUserId);
        tmp3.setOperflag(InterfaceMacro.Pb_MeetPostilOperType.Pb_MEETPOTIL_FLAG_REQUESTOPEN.getNumber());
        InterfaceWhiteboard.pbui_Type_MeetWhiteBoardControl build = tmp3.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_CONTROL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "coerceStartBoard:  发起白板 --->>> ");
    }


    /**
     * 234.添加图片
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public void addPicture(int operid, int opermemberid, int srcmemid, long srcwbid, long utcstamp, int figuretype, float lx, float ly, ByteString picdata) throws InvalidProtocolBufferException {
        InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail.Builder builder = InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail.newBuilder();
        builder.setOperid(operid);
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setUtcstamp(utcstamp);
        builder.setFiguretype(figuretype);
        builder.setLx(lx);
        builder.setLy(ly);
        builder.setPicdata(picdata);
        LogUtil.e(TAG, "addPicture :   --->>> " + figuretype);
        InterfaceWhiteboard.pbui_Item_MeetWBPictureDetail build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADDPICTURE.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "addPicture:  共享添加图片 --->>> ");
    }

    /**
     * 222.白板清空记录
     *
     * @return
     */
    public void whiteBoardClearRecord(int operid, int opermemberid, int srcmemid, long srcwbid, long utcstamp, int figuretype) {
        InterfaceWhiteboard.pbui_Type_MeetDoClearWhiteBoard.Builder builder = InterfaceWhiteboard.pbui_Type_MeetDoClearWhiteBoard.newBuilder();
        builder.setOperid(operid);
        builder.setOpermemberid(opermemberid);
        builder.setSrcmemid(srcmemid);
        builder.setSrcwbid(srcwbid);
        builder.setUtcstamp(utcstamp);
        builder.setFiguretype(figuretype);
        InterfaceWhiteboard.pbui_Type_MeetDoClearWhiteBoard build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DELALL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "whiteBoardClearInform:  222.白板清空记录 --->>> ");
    }

    public void call(int type, int iskeyframe, long pts, byte[] data) {
        jni.call(type, iskeyframe, pts, data);
    }


    /**
     * 查询可加入的同屏会话
     */
    public InterfaceDevice.pbui_Type_DeviceResPlay queryCanJoin() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_RESINFO.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "queryCanJoin  查询可加入的同屏会话失败 --->>> ");
            return null;
        }
        LogUtil.e(TAG, "queryCanJoin  查询可加入的同屏会话成功 --->>> ");
        return InterfaceDevice.pbui_Type_DeviceResPlay.parseFrom(array);
    }

    public InterfaceFilescorevote.pbui_Type_UserDefineFileScore queryScoreFile() {
        byte[] bytes = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FILESCOREVOTE_VALUE,
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY_VALUE, null);
        if (bytes == null) {
            LogUtil.e(TAG, "queryScoreFile -->" + "查询会议评分失败");
            return null;
        }
        InterfaceFilescorevote.pbui_Type_UserDefineFileScore pbui_type_userDefineFileScore = null;
        try {
            pbui_type_userDefineFileScore = InterfaceFilescorevote.pbui_Type_UserDefineFileScore.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        LogUtil.d(TAG, "queryScoreFile -->" + "查询会议评分成功");
        return pbui_type_userDefineFileScore;
    }

    /**
     * 62.执行终端控制
     *
     * @param oper enum Pb_DeviceControlFlag
     * @return
     */
    public void executeTerminalControl(int oper, int operval1, int operval2, List<Integer> devids) {
        InterfaceDevice.pbui_Type_DeviceOperControl.Builder builder = InterfaceDevice.pbui_Type_DeviceOperControl.newBuilder();
        builder.setOper(oper);
        builder.setOperval1(operval1);
        builder.setOperval2(operval2);
        builder.addAllDevid(devids);
        InterfaceDevice.pbui_Type_DeviceOperControl build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICECONTROL.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_CONTROL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "executeTerminalControl:  执行终端控制 --->>> oper= " + oper);
    }

    /**
     * 辅助签到操作
     *
     * @param devids
     */
    public void signAlterationOperate(List<Integer> devids) {
        InterfaceDevice.pbui_MeetDoEnterMeet.Builder builder = InterfaceDevice.pbui_MeetDoEnterMeet.newBuilder();
        builder.addAllDevid(devids);
        InterfaceDevice.pbui_MeetDoEnterMeet build = builder.build();
        byte[] bytes = build.toByteArray();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ENTER.getNumber(), bytes);
        LogUtil.e(TAG, "signAlterationOperate:  辅助签到操作 --->>> ");
    }

    /**
     * 200.查询投票
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceVote.pbui_Type_MeetVoteDetailInfo queryVote() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTEINFO.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "queryVote :  查询投票失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryVote :  查询投票成功 --> ");
        return InterfaceVote.pbui_Type_MeetVoteDetailInfo.parseFrom(array);
    }

    public InterfaceVote.pbui_Type_MeetVoteDetailInfo queryVoteByType(int voteType) throws InvalidProtocolBufferException {
        InterfaceVote.pbui_Type_MeetVoteComplexQuery build = InterfaceVote.pbui_Type_MeetVoteComplexQuery.newBuilder()
                .setMaintype(voteType).build();
        byte[] bytes = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTEINFO_VALUE,
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_COMPLEXQUERY_VALUE, build.toByteArray());
        if (bytes == null) {
            LogUtil.d(TAG, "queryVoteByType -->" + "查询指定类别投票失败：" + voteType);
            return null;
        }
        LogUtil.d(TAG, "queryVoteByType -->" + "查询指定类别投票成功：" + voteType);
        return InterfaceVote.pbui_Type_MeetVoteDetailInfo.parseFrom(bytes);
    }

    /**
     * 191.新建一个投票
     *
     * @return
     */
    public void createVote(InterfaceVote.pbui_Item_MeetOnVotingDetailInfo vote) {
        InterfaceVote.pbui_Type_MeetOnVotingDetailInfo.Builder builder1 = InterfaceVote.pbui_Type_MeetOnVotingDetailInfo.newBuilder();
        builder1.addItem(vote);
        InterfaceVote.pbui_Type_MeetOnVotingDetailInfo build = builder1.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "createVote:  新建一个投票 --->>> ");
    }

    /**
     * 192.修改一个投票
     *
     * @return
     */
    public void modifyVote(InterfaceVote.pbui_Item_MeetOnVotingDetailInfo item) {
        InterfaceVote.pbui_Type_MeetOnVotingDetailInfo.Builder builder = InterfaceVote.pbui_Type_MeetOnVotingDetailInfo.newBuilder();
        builder.addItem(item);
        InterfaceVote.pbui_Type_MeetOnVotingDetailInfo build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_MODIFY.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "modifyVote:  修改一个投票 --->>> ");
    }

    /**
     * 194.删除投票
     *
     * @return
     */
    public void deleteVote(Integer voteid) {
        InterfaceVote.pbui_Type_MeetStopVoteInfo.Builder builder = InterfaceVote.pbui_Type_MeetStopVoteInfo.newBuilder();
        builder.addVoteid(voteid);
        InterfaceVote.pbui_Type_MeetStopVoteInfo build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DEL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "deleteVote:  删除投票 --->>> ");
    }

    /**
     * 查询参会人员详细信息
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceMember.pbui_Type_MeetMemberDetailInfo queryAttendPeopleDetailed() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DETAILINFO.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "queryAttendPeopleDetailed :  查询参会人员详细信息失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryAttendPeopleDetailed :  查询参会人员详细信息成功 --> ");
        return InterfaceMember.pbui_Type_MeetMemberDetailInfo.parseFrom(array);
    }

    /**
     * 发起投票
     *
     * @param memberIds 参会人ID
     * @param voteid    投票ID
     * @param seconds   单位秒
     */
    public void launchVote(List<Integer> memberIds, int voteid, int seconds) {
        InterfaceVote.pbui_ItemVoteStart.Builder b = InterfaceVote.pbui_ItemVoteStart.newBuilder();
        b.setVoteid(voteid);
        b.setVoteflag(InterfaceMacro.Pb_VoteStartFlag.Pb_MEET_VOTING_FLAG_AUTOEXIT_VALUE);
        b.setTimeouts(seconds);
        b.addAllMemberid(memberIds);
        InterfaceVote.pbui_Type_MeetStartVoteInfo.Builder builder = InterfaceVote.pbui_Type_MeetStartVoteInfo.newBuilder();
        builder.addItem(b);
        InterfaceVote.pbui_Type_MeetStartVoteInfo build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_START.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "initiateVote:  发起投票 --->>> ");
    }

    /**
     * 195.停止投票
     *
     * @return
     */
    public void stopVote(int voteid) {
        InterfaceVote.pbui_Type_MeetStopVoteInfo.Builder builder = InterfaceVote.pbui_Type_MeetStopVoteInfo.newBuilder();
        builder.addVoteid(voteid);
        InterfaceVote.pbui_Type_MeetStopVoteInfo build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_STOP.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "deleteVote:  停止投票 --->>> ");
    }

    /**
     * 189.查询发起的投票
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceVote.pbui_Type_MeetOnVotingDetailInfo queryInitiateVote() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "queryInitiateVote:  查询发起的投票失败 --->>> ");
            return null;
        }
        LogUtil.e(TAG, "queryVote:  查询发起的投票成功 --->>> ");
        return InterfaceVote.pbui_Type_MeetOnVotingDetailInfo.parseFrom(array);
    }

    /**
     * 203.查询指定投票的提交人
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceVote.pbui_Type_MeetVoteSignInDetailInfo querySubmittedVoters(int voteId) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID build = InterfaceBase.pbui_QueryInfoByID.newBuilder()
                .setId(voteId).build();
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTESIGNED.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "queryOneVoteSubmitter :  查询指定投票的提交人失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryOneVoteSubmitter :  查询指定投票的提交人成功 --> ");
        return InterfaceVote.pbui_Type_MeetVoteSignInDetailInfo.parseFrom(array);
    }

    /**
     * 查询指定评分的提交人
     *
     * @param voteid
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceFilescorevote.pbui_Type_UserDefineFileScoreMemberStatistic queryScoreSubmittedScore(int voteid) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID build = InterfaceBase.pbui_QueryInfoByID.newBuilder().setId(voteid).build();
        byte[] bytes = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FILESCOREVOTESIGN_VALUE, InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY_VALUE, build.toByteArray());
        if (bytes == null) {
            LogUtil.e(TAG, "queryScoreSubmittedScore -->" + "查询指定评分提交人失败");
            return null;
        }
        LogUtil.e(TAG, "queryScoreSubmittedScore -->" + "查询指定评分提交人成功");
        return InterfaceFilescorevote.pbui_Type_UserDefineFileScoreMemberStatistic.parseFrom(bytes);

    }

    /**
     * 发起评分
     *
     * @param voteid    评分ID
     * @param voteflag  Pb_VoteStartFlag
     * @param timeouts
     * @param memberIds 人员ID
     */
    public void startScore(int voteid, int voteflag, int timeouts, List<Integer> memberIds) {
        InterfaceFilescorevote.pbui_Type_StartUserDefineFileScore build = InterfaceFilescorevote.pbui_Type_StartUserDefineFileScore.newBuilder()
                .addAllMemberid(memberIds)
                .setTimeouts(timeouts)
                .setVoteflag(voteflag)
                .setVoteid(voteid)
                .build();
        LogUtil.d(TAG, "startScore -->" + "发起评分：votid= " + voteid + "，memberIds: " + memberIds.toString());
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FILESCOREVOTE_VALUE, InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_START_VALUE, build.toByteArray());
    }

    /**
     * 停止评分
     *
     * @param voteid
     */
    public void stopScore(int voteid) {
        InterfaceFilescorevote.pbui_Type_DeleteUserDefineFileScore build = InterfaceFilescorevote.pbui_Type_DeleteUserDefineFileScore.newBuilder()
                .addVoteid(voteid).build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FILESCOREVOTE_VALUE, InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_STOP_VALUE, build.toByteArray());
    }

    /**
     * 提交文件评分
     *
     * @param voteid
     * @param memberid
     * @param opinion  评分意见
     * @param allscore 所有分数
     */
    public void submitScore(int voteid, int memberid, String opinion, List<Integer> allscore) {
        InterfaceFilescorevote.pbui_Type_UserDefineFileScoreMemberStatisticNotify build = InterfaceFilescorevote.pbui_Type_UserDefineFileScoreMemberStatisticNotify.newBuilder()
                .setContent(s2b(opinion))
                .setMemberid(memberid)
                .addAllScore(allscore)
                .setVoteid(voteid).build();
        LogUtil.d(TAG, "submitScore -->" + "提交文件评分");
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FILESCOREVOTESIGN_VALUE, InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SUBMIT_VALUE, build.toByteArray());
    }

    /**
     * 提交投票结果
     *
     * @param selcnt  选择的个数
     * @param voteid  投票ID
     * @param selitem 选择的答案（十进制代表数）
     */
    public void submitVoteResult(int selcnt, int voteid, int selitem) {
        InterfaceVote.pbui_Item_MeetSubmitVote.Builder builder1 = InterfaceVote.pbui_Item_MeetSubmitVote.newBuilder();
        builder1.setSelcnt(selcnt);
        builder1.setVoteid(voteid);
        builder1.setSelitem(selitem);
        InterfaceVote.pbui_Type_MeetSubmitVote.Builder builder = InterfaceVote.pbui_Type_MeetSubmitVote.newBuilder();
        builder.addItem(builder1);
        InterfaceVote.pbui_Type_MeetSubmitVote build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SUBMIT.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "submitVoteResult:  提交投票结果 --->>> ");
    }

    /**
     * 204.查询投票提交人属性
     *
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceBase.pbui_CommonInt32uProperty queryVoteSubmitterProperty(int voteid, int memberid, int propertyid) {
        InterfaceVote.pbui_Type_MeetVoteQueryProperty.Builder builder = InterfaceVote.pbui_Type_MeetVoteQueryProperty.newBuilder();
        builder.setVoteid(voteid);
        builder.setMemberid(memberid);
        builder.setPropertyid(propertyid);
        InterfaceVote.pbui_Type_MeetVoteQueryProperty build = builder.build();
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTESIGNED.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "queryVoteSubmitterProperty :  查询投票提交人属性失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryVoteSubmitterProperty :  查询投票提交人属性成功 --> ");
        try {
            return InterfaceBase.pbui_CommonInt32uProperty.parseFrom(array);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            LogUtil.e(TAG, "queryVoteSubmitterProperty -->" + "类型转换失败");
            return null;
        }
    }


    /**
     * 查询公告
     */
    public InterfaceBullet.pbui_BulletDetailInfo queryNotice() throws InvalidProtocolBufferException {
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERY.getNumber(), null);
        if (array == null) {
            LogUtil.e(TAG, "queryNotice:  查询公告失败 --->>> ");
            return null;
        }
        LogUtil.e(TAG, "queryNotice:  查询公告成功 --->>> ");
        return InterfaceBullet.pbui_BulletDetailInfo.parseFrom(array);
    }

    /**
     * 添加公告
     *
     * @param item
     */
    public void addNotice(InterfaceBullet.pbui_Item_BulletDetailInfo item) {
        InterfaceBullet.pbui_BulletDetailInfo.Builder builder = InterfaceBullet.pbui_BulletDetailInfo.newBuilder();
        builder.addItem(item);
        InterfaceBullet.pbui_BulletDetailInfo build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ADD.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "addNotice:  添加公告 --->>> ");
    }

    /**
     * 修改公告
     *
     * @param item
     */
    public void modifNotice(InterfaceBullet.pbui_Item_BulletDetailInfo item) {
        InterfaceBullet.pbui_BulletDetailInfo.Builder builder = InterfaceBullet.pbui_BulletDetailInfo.newBuilder();
        builder.addItem(item);
        InterfaceBullet.pbui_BulletDetailInfo build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_MODIFY.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "modifNotice:  修改公告 --->>> ");
    }

    /**
     * 删除公告
     *
     * @param item
     */
    public void deleteNotice(InterfaceBullet.pbui_Item_BulletDetailInfo item) {
        InterfaceBullet.pbui_BulletDetailInfo.Builder builder = InterfaceBullet.pbui_BulletDetailInfo.newBuilder();
        builder.addItem(item);
        InterfaceBullet.pbui_BulletDetailInfo build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_DEL.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "deleteNotice:  删除公告 --->>> ");
    }

    /**
     * 查询指定的公告
     *
     * @param value
     * @return
     * @throws InvalidProtocolBufferException
     */
    public InterfaceBullet.pbui_BulletDetailInfo queryAssignNotice(int value) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID.Builder builder = InterfaceBase.pbui_QueryInfoByID.newBuilder();
        builder.setId(value);
        InterfaceBase.pbui_QueryInfoByID build = builder.build();
        byte[] array = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SINGLEQUERYBYID.getNumber(), build.toByteArray());
        if (array == null) {
            LogUtil.e(TAG, "queryAssignNotice :  查询指定的公告失败 --> ");
            return null;
        }
        LogUtil.e(TAG, "queryAssignNotice :  查询指定的公告成功 --> ");
        return InterfaceBullet.pbui_BulletDetailInfo.parseFrom(array);
    }

    /**
     * 发布公告
     *
     * @param item
     * @param devids 要观看的设备
     */
    public void pushNotice(InterfaceBullet.pbui_Item_BulletDetailInfo item, List<Integer> devids) {
        InterfaceBullet.pbui_Type_MeetPublishBulletInfo.Builder builder = InterfaceBullet.pbui_Type_MeetPublishBulletInfo.newBuilder();
        builder.addAllDeviceid(devids);
        builder.setItem(item);
        InterfaceBullet.pbui_Type_MeetPublishBulletInfo build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_PUBLIST.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "issueNotice:  发布公告 --->>> ");
    }

    /**
     * 停止公告
     *
     * @param bulletid
     * @param devids
     */
    public void stopNotice(int bulletid, List<Integer> devids) {
        InterfaceBullet.pbui_Type_StopBullet.Builder builder = InterfaceBullet.pbui_Type_StopBullet.newBuilder();
        builder.setBulletid(bulletid);
        builder.addAllPdevid(devids);
        InterfaceBullet.pbui_Type_StopBullet build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_STOP.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "stopNotice :  停止公告 --> " + devids.toString());
    }

    public InterfaceFilescorevote.pbui_Type_UserDefineFileScore queryScoreById(int scoreId) throws InvalidProtocolBufferException {
        InterfaceBase.pbui_QueryInfoByID build = InterfaceBase.pbui_QueryInfoByID.newBuilder().setId(scoreId).build();
        byte[] bytes = jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FILESCOREVOTE_VALUE, InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_SINGLEQUERYBYID_VALUE, build.toByteArray());
        if (bytes == null) {
            LogUtil.e(TAG, "queryScoreById -->" + "查询指定ID的评分失败 scoreId= " + scoreId);
            return null;
        }
        LogUtil.i(TAG, "queryScoreById -->" + "查询指定ID的评分成功 scoreId= " + scoreId);
        return InterfaceFilescorevote.pbui_Type_UserDefineFileScore.parseFrom(bytes);
    }

    /**
     * 设备对讲
     *
     * @param devids
     * @param flag   Interface_device.Pb_DeviceInviteFlag
     */
    public void deviceIntercom(List<Integer> devids, int flag) {
        InterfaceDevice.pbui_Type_DoDeviceChat build = InterfaceDevice.pbui_Type_DoDeviceChat.newBuilder()
                .addAllDevid(devids)
                .setInviteflag(flag)
                .build();
        LogUtil.d(TAG, "deviceIntercom -->" + "设备对讲 flag = " + flag);
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE,
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_REQUESTINVITE_VALUE, build.toByteArray());
    }

    /**
     * 回复设备对讲
     *
     * @param devid 回复的设备
     * @param flag  Interface_device.Pb_DeviceInviteFlag  =1同意，=0拒绝
     *              Pb_DEVICE_INVITECHAT_FLAG_DEAL
     */
    public void replyDeviceIntercom(int devid, int flag) {
        InterfaceDevice.pbui_Type_DeviceChat build = InterfaceDevice.pbui_Type_DeviceChat.newBuilder()
                .setOperdeviceid(devid)
                .setInviteflag(flag)
                .build();
        LogUtil.d(TAG, "deviceIntercom -->" + "回复设备对讲");
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE,
                InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_RESPONSEINVITE_VALUE, build.toByteArray());
    }

    /**
     * 停止设备对讲
     *
     * @param devid 发起端设备ID
     */
    public void stopDeviceIntercom(int devid) {
        InterfaceDevice.pbui_Type_DoExitDeviceChat build = InterfaceDevice.pbui_Type_DoExitDeviceChat.newBuilder()
                .setOperdeviceid(devid)
                .build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE, InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_EXITCHAT_VALUE, build.toByteArray());
    }

    /**
     * 回复参会人员权限请求
     *
     * @param devid      回复给的对象
     * @param returncode 1=同意,0=不同意
     * @return
     */
    public void revertAttendPermissionsRequest(int devid, int returncode) {
        InterfaceDevice.pbui_Type_MeetResponseRequestPrivilege.Builder builder = InterfaceDevice.pbui_Type_MeetResponseRequestPrivilege.newBuilder();
        builder.addDevid(devid);
        builder.setReturncode(returncode);
        InterfaceDevice.pbui_Type_MeetResponseRequestPrivilege build = builder.build();
        jni.call_method(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER.getNumber(), InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_RESPONSEPRIVELIGE.getNumber(), build.toByteArray());
        LogUtil.e(TAG, "revertPermissionsRequest:    回复参会人员权限请求 --->>> ");
    }
}
