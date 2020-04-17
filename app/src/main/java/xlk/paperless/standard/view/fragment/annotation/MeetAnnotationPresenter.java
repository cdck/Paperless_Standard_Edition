package xlk.paperless.standard.view.fragment.annotation;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.bean.DevMember;
import xlk.paperless.standard.data.bean.SeatMember;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.BasePresenter;
import xlk.paperless.standard.view.MyApplication;

/**
 * @author xlk
 * @date 2020/3/18
 * @Description:
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
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT_VALUE://会议排位变更通知
                queryMeetRanking();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE://参会人员变更通知
                queryMember();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE_VALUE://会议目录文件变更通知
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
                    byte[] o = (byte[]) msg.getObjs()[0];
                    InterfaceBase.pbui_MeetNotifyMsgForDouble pbui_meetNotifyMsgForDouble = InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(o);
                    if (pbui_meetNotifyMsgForDouble.getId() == 2) {
                        queryFile();
                    }
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE://设备交互
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_RESPONSEPRIVELIGE.getNumber()) {
                    //收到参会人员权限请求回复
                    byte[] o = (byte[]) msg.getObjs()[0];
                    InterfaceDevice.pbui_Type_MeetRequestPrivilegeResponse object = InterfaceDevice.pbui_Type_MeetRequestPrivilegeResponse.parseFrom(o);
                    int returncode = object.getReturncode();
                    int deviceid = object.getDeviceid();
                    int memberid = object.getMemberid();
                    if (returncode == 1) {//查看批注文件权限有了
                        if (!saveConsentDevices.contains(deviceid)) {
                            saveConsentDevices.add(deviceid);
                        }
                        String name = null;
                        for (int i = 0; i < memberDetailInfos.size(); i++) {
                            if (memberDetailInfos.get(i).getPersonid() == memberid) {
                                name = memberDetailInfos.get(i).getName().toStringUtf8();
                                ToastUtil.show(cxt, cxt.getString(R.string.agreed_postilview, name));
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
                                ToastUtil.show(cxt, cxt.getString(R.string.reject_postilview, name));
                                break;
                            }
                        }
                    }
                }
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
            for (int i = 0; i < memberDetailInfos.size(); i++) {
                if (memberDetailInfos.get(i).getPersonid() == MyApplication.localMemberId) {
                    memberDetailInfos.remove(i);
                    break;
                }
            }
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
            InterfaceFile.pbui_Type_MeetDirFileDetailInfo dirFileDetailInfo = jni.queryMeetDirFile(2);
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
        return saveConsentDevices.contains(devId);
    }


    public void downloadFile(InterfaceFile.pbui_Item_MeetDirFileDetailInfo fileDetailInfo) {
        LogUtil.d(TAG, "downloadFile -->" + "下载文件：" + fileDetailInfo.getName().toStringUtf8());
        if (FileUtil.createDir(Constant.annotation_file_dir)) {
            jni.creationFileDownload(Constant.annotation_file_dir + fileDetailInfo.getName().toStringUtf8(),
                    fileDetailInfo.getMediaid(), 1, 0, Constant.ANNOTATION_FILE_KEY);
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
                if (Constant.isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetProjective_VALUE,devcieid)
                        && netstate == 1) {
                    onLineProjectors.add(detailInfo);
                }
                if (netstate == 1 && facestate == 1 && devcieid != MyApplication.localDeviceId) {
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

}
