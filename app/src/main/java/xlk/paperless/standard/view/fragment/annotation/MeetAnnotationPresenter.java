package xlk.paperless.standard.view.fragment.annotation;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.DevMember;
import xlk.paperless.standard.data.bean.SeatMember;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BasePresenter;

/**
 * @author xlk
 * @date 2020/3/18
 * @desc
 */
public class MeetAnnotationPresenter extends BasePresenter {
    private final String TAG = "MeetAnnotationPresenter-->";
    private final IMeetAnnotation view;
    private final Context cxt;
    private JniHandler jni = JniHandler.getInstance();
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberDetailInfos = new ArrayList<>();
    List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> fileDetailInfos = new ArrayList<>();
    List<SeatMember> seatMembers = new ArrayList<>();
    List<Integer> saveConsentDevices = new ArrayList<>();
    List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceDetailInfos = new ArrayList<>();
    List<DevMember> onlineMembers = new ArrayList<>();
    List<InterfaceDevice.pbui_Item_DeviceDetailInfo> onLineProjectors = new ArrayList<>();

    public MeetAnnotationPresenter(Context cxt, IMeetAnnotation view) {
        super();
        this.cxt = cxt;
        this.view = view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //会议排位变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT_VALUE:
                queryMeetRanking();
                break;
            //参会人员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE:
                queryMember();
                break;
            //会议目录文件变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE_VALUE:
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
                    byte[] o = (byte[]) msg.getObjects()[0];
                    InterfaceBase.pbui_MeetNotifyMsgForDouble pbui_meetNotifyMsgForDouble = InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(o);
                    if (pbui_meetNotifyMsgForDouble.getId() == Constant.ANNOTATION_FILE_DIRECTORY_ID) {
                        queryFile();
                    }
                }
                break;
            //设备交互
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE: {
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_RESPONSEPRIVELIGE.getNumber()) {
                    //收到参会人员权限请求回复
                    byte[] o = (byte[]) msg.getObjects()[0];
                    InterfaceDevice.pbui_Type_MeetRequestPrivilegeResponse object = InterfaceDevice.pbui_Type_MeetRequestPrivilegeResponse.parseFrom(o);
                    // returncode 1=同意,0=不同意
                    int returncode = object.getReturncode();
                    // 发起请求的设备ID
                    int deviceid = object.getDeviceid();
                    // 发起请求的人员ID
                    int memberid = object.getMemberid();
                    LogUtil.i(TAG, "busEvent 收到参会人员权限请求回复 returncode=" + returncode+",deviceid="+deviceid+",memberid="+memberid);
                    //查看批注文件权限有了
                    if (returncode == 1) {
                        if (!saveConsentDevices.contains(deviceid)) {
                            saveConsentDevices.add(deviceid);
                        }
                        String name = null;
                        for (int i = 0; i < memberDetailInfos.size(); i++) {
                            if (memberDetailInfos.get(i).getPersonid() == memberid) {
                                name = memberDetailInfos.get(i).getName().toStringUtf8();
                                ToastUtil.show(cxt.getString(R.string.agreed_postilview, name));
                                break;
                            }
                        }
                        queryFile();
                    } else {
                        if (saveConsentDevices.contains(deviceid)) {
                            saveConsentDevices.remove(deviceid);
                        }
                        for (int i = 0; i < memberDetailInfos.size(); i++) {
                            if (memberDetailInfos.get(i).getPersonid() == memberid) {
                                String name = memberDetailInfos.get(i).getName().toStringUtf8();
                                ToastUtil.show(cxt.getString(R.string.reject_postilview, name));
                                break;
                            }
                        }
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    public void queryMember() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo attendPeople = jni.queryAttendPeople();
            if (attendPeople == null) {
                return;
            }
            memberDetailInfos.clear();
            memberDetailInfos.addAll(attendPeople.getItemList());
            queryMeetRanking();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryMeetRanking() {
        try {
            InterfaceRoom.pbui_Type_MeetSeatDetailInfo meetSeatDetailInfo = jni.queryMeetRanking();
            if (meetSeatDetailInfo == null) return;
            seatMembers.clear();
            List<InterfaceRoom.pbui_Item_MeetSeatDetailInfo> itemList = meetSeatDetailInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceRoom.pbui_Item_MeetSeatDetailInfo info = itemList.get(i);
                for (int j = 0; j < memberDetailInfos.size(); j++) {
                    InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo = memberDetailInfos.get(j);
                    if (info.getNameId() == memberDetailInfo.getPersonid()) {
                        seatMembers.add(new SeatMember(memberDetailInfo, info));
                        break;
                    }
                }
            }
            view.updateMemberRv(seatMembers);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void sendAttendRequestPermissions(int devId, int code) {
        jni.sendAttendRequestPermissions(devId, code);
    }

    public void queryFile() {
        try {
            InterfaceFile.pbui_Type_MeetDirFileDetailInfo dirFileDetailInfo = jni.queryMeetDirFile(Constant.ANNOTATION_FILE_DIRECTORY_ID);
            fileDetailInfos.clear();
            if (dirFileDetailInfo == null) {
                view.updateFileRv(fileDetailInfos);
                return;
            }
            fileDetailInfos.addAll(dirFileDetailInfo.getItemList());
            view.updateFileRv(fileDetailInfos);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public boolean hasPermission(int devId) {
        if (devId == Values.localDeviceId) {
            return true;
        } else {
            return saveConsentDevices.contains(devId);
        }
    }

    public void downloadFile(InterfaceFile.pbui_Item_MeetDirFileDetailInfo fileDetailInfo) {
        LogUtil.d(TAG, "downloadFile -->" + "下载文件：" + fileDetailInfo.getName().toStringUtf8());
        if (FileUtil.createDir(Constant.DIR_ANNOTATION_FILE)) {
            File file = new File(Constant.DIR_ANNOTATION_FILE + fileDetailInfo.getName().toStringUtf8());
            if (file.exists()) {
                if (Values.downloadingFiles.contains(fileDetailInfo.getMediaid())) {
                    ToastUtil.show(R.string.currently_downloading);
                } else {
                    ToastUtil.show(R.string.already_exists_locally);
                }
                return;
            }
            jni.creationFileDownload(Constant.DIR_ANNOTATION_FILE + fileDetailInfo.getName().toStringUtf8(),
                    fileDetailInfo.getMediaid(), 1, 0, Constant.DOWNLOAD_ANNOTATION_FILE);
        }
    }

    public void preViewFile(InterfaceFile.pbui_Item_MeetDirFileDetailInfo fileDetailInfo) {
        if (FileUtil.createDir(Constant.DIR_ANNOTATION_FILE)) {
            File file = new File(Constant.DIR_ANNOTATION_FILE + fileDetailInfo.getName().toStringUtf8());
            if (file.exists()) {
                if (Values.downloadingFiles.contains(fileDetailInfo.getMediaid())) {
                    ToastUtil.show(R.string.currently_downloading);
                } else {
                    FileUtil.openFile(cxt, file);
                }
                return;
            }
            jni.creationFileDownload(Constant.DIR_ANNOTATION_FILE + fileDetailInfo.getName().toStringUtf8(),
                    fileDetailInfo.getMediaid(), 1, 0, Constant.DOWNLOAD_SHOULD_OPEN_FILE);
        }
    }


    public void pushFile(int mediaId) {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = jni.queryDeviceInfo();
            if (deviceDetailInfo == null) {
                return;
            }
            List<InterfaceDevice.pbui_Item_DeviceDetailInfo> pdevList = deviceDetailInfo.getPdevList();
            deviceDetailInfos.clear();
            deviceDetailInfos.addAll(pdevList);
            onlineMembers.clear();
            onLineProjectors.clear();
            InterfaceMember.pbui_Type_MemberDetailInfo attendPeople = jni.queryAttendPeople();
            List<InterfaceMember.pbui_Item_MemberDetailInfo> itemList = attendPeople.getItemList();
            for (int i = 0; i < deviceDetailInfos.size(); i++) {
                InterfaceDevice.pbui_Item_DeviceDetailInfo detailInfo = deviceDetailInfos.get(i);
                int devcieid = detailInfo.getDevcieid();
                int netstate = detailInfo.getNetstate();
                int memberid = detailInfo.getMemberid();
                int facestate = detailInfo.getFacestate();
                if (Constant.isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetProjective_VALUE, devcieid)
                        && netstate == 1) {
                    onLineProjectors.add(detailInfo);
                }
                if (netstate == 1 && facestate == 1 && devcieid != Values.localDeviceId) {
                    for (int j = 0; j < itemList.size(); j++) {
                        InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo = itemList.get(j);
                        if (memberDetailInfo.getPersonid() == memberid) {
                            onlineMembers.add(new DevMember(detailInfo, memberDetailInfo));
                        }
                    }
                }
            }
            view.showPushView(onlineMembers, onLineProjectors, mediaId);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void mediaPlayOperate(int mediaid, List<Integer> devIds, int pos, int res, int triggeruserval, int flag) {
        jni.mediaPlayOperate(mediaid, devIds, pos, res, triggeruserval, flag);
    }

    public void stopPush(List<Integer> res, List<Integer> devIds){
        jni.stopResourceOperate(res,devIds);
    }
}
