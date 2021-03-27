package xlk.paperless.standard.view.admin.fragment.pre.bind;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.admin.fragment.pre.member.MemberRoleBean;

/**
 * @author Created by xlk on 2020/11/3.
 * @desc
 */
public class SeatBindPresenter extends BasePresenter {
    private final SeatBindInterface view;
    public List<MemberRoleBean> devSeatInfos = new ArrayList<>();
    private List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatData = new ArrayList<>();

    public SeatBindPresenter(SeatBindInterface view) {
        super();
        this.view = view;
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //会场底图下载完成
            case Constant.BUS_ROOM_BG:
                String currentRoomBgFilePath = (String) msg.getObjects()[0];
                int mediaId = (int) msg.getObjects()[1];
                view.updateRoomBg(currentRoomBgFilePath, mediaId);
                break;
            //参会人员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE: {
                LogUtil.i(TAG, "BusEvent -->" + "参会人员变更通知");
                queryMember();
                break;
            }
            //会议排位变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT_VALUE: {
                LogUtil.i(TAG, "busEvent " + "会议排位变更通知");
//                queryPlaceRanking();
                queryMember();
                break;
            }
            //界面配置变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETFACECONFIG_VALUE: {
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(bytes);
                int id = pbui_meetNotifyMsg.getId();
                int opermethod = pbui_meetNotifyMsg.getOpermethod();
                LogUtil.i(TAG, "BusEvent 界面配置变更通知 id=" + id + ",opermethod=" + opermethod);
                if (id == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_SeatIcoShow_GEO_VALUE) {
                    queryRoomIcon();
                }
                break;
            }
            //会场信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOM_VALUE: {
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(bytes);
                int id = pbui_meetNotifyMsg.getId();
                int opermethod = pbui_meetNotifyMsg.getOpermethod();
                LogUtil.i(TAG, "BusEvent 会场信息变更通知 id=" + id + ",opermethod=" + opermethod);
                if (queryCurrentRoomId() == id && id != 0) {
                    queryMeetRoomBg();
                }
                break;
            }
            default:
                break;
        }
    }

    public void queryMember() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo info = jni.queryAttendPeople();
            devSeatInfos.clear();
            if (info != null) {
                for (int i = 0; i < info.getItemList().size(); i++) {
                    InterfaceMember.pbui_Item_MemberDetailInfo member = info.getItemList().get(i);
                    devSeatInfos.add(new MemberRoleBean(member));
                }
            }
            queryMeetRoomBg();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryMeetRoomBg() {
        try {
            int mediaId = jni.queryMeetRoomProperty(queryCurrentRoomId());
            if (mediaId != 0) {
                FileUtil.createDir(Constant.DIR_PICTURE);
                jni.creationFileDownload(Constant.DIR_PICTURE + Constant.ROOM_BG_PNG_TAG + ".png", mediaId, 1, 0, Constant.ROOM_BG_PNG_TAG);
                return;
            }
            queryPlaceRanking();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * 会场设备排位详细信息
     */
    public void queryPlaceRanking() {
        try {
            InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo info = jni.placeDeviceRankingInfo(queryCurrentRoomId());
            seatData.clear();
            if (info != null) {
                seatData.addAll(info.getItemList());
                for (int j = 0; j < seatData.size(); j++) {
                    InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item = seatData.get(j);
                    LogUtil.i(TAG, "queryPlaceRanking devName=" + item.getDevname().toStringUtf8() + ",devId=" + item.getDevid()
                            + ",memberName=" + item.getMembername().toStringUtf8() + ",memberId=" + item.getMemberid() + ",");
                    for (int i = 0; i < devSeatInfos.size(); i++) {
                        MemberRoleBean bean = devSeatInfos.get(i);
                        if (item.getMemberid() == bean.getMember().getPersonid()) {
                            LogUtil.d(TAG, "queryPlaceRanking " + bean.getMember().getName().toStringUtf8());
                            bean.setSeat(item);
                            break;
                        }
                    }
                }
            }
            view.updateMemberList(devSeatInfos);
            view.updateSeatData(seatData);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryRoomIcon() {
        try {
            InterfaceFaceconfig.pbui_Type_FaceConfigInfo pbui_type_faceConfigInfo = jni.queryInterFaceConfiguration();
            if (pbui_type_faceConfigInfo != null) {
                List<InterfaceFaceconfig.pbui_Item_FaceTextItemInfo> textList = pbui_type_faceConfigInfo.getTextList();
                for (int i = 0; i < textList.size(); i++) {
                    InterfaceFaceconfig.pbui_Item_FaceTextItemInfo item = textList.get(i);
                    int faceid = item.getFaceid();
                    int flag = item.getFlag();
                    if (InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_SeatIcoShow_GEO_VALUE == faceid) {
                        boolean showFlag = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE
                                == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
                        view.updateShowIcon(!showFlag);
                        return;
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * 随机进行绑定席位
     */
    public void randomBind() {
        for (int i = 0; i < devSeatInfos.size(); i++) {
            MemberRoleBean memberRoleBean = devSeatInfos.get(i);
            for (int j = 0; j < seatData.size(); j++) {
                InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item = seatData.get(j);
                if (i == j) {
                    int memberId = memberRoleBean.getMember().getPersonid();
                    int devid = item.getDevid();
                    jni.modifyMeetRanking(memberId, InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal_VALUE, devid);
                    break;
                }
            }
        }
    }

    public void bindSeat(List<ReadJxlBean> readJxlBeans) {
        LogUtil.i(TAG, "bindSeat readJxlBeans.size=" + readJxlBeans.size());
        for (int i = 0; i < readJxlBeans.size(); i++) {
            ReadJxlBean readJxlBean = readJxlBeans.get(i);
            String memberName = readJxlBean.getMemberName();
            int devId = readJxlBean.getDevId();
            String devName = readJxlBean.getDevName();
            LogUtil.i(TAG, "bindSeat memberName=" + memberName + ",devId=" + devId + ",devName=" + devName);
            int memberId = 0;
            for (int j = 0; j < devSeatInfos.size(); j++) {
                MemberRoleBean memberRoleBean = devSeatInfos.get(j);
                String s = memberRoleBean.getMember().getName().toStringUtf8();
                if (s.equals(memberName)) {
                    memberId = memberRoleBean.getMember().getPersonid();
                    break;
                }
            }
            jni.modifyMeetRanking(memberId, InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal_VALUE, devId);
        }
    }

    public void allDismiss() {
        for (int j = 0; j < seatData.size(); j++) {
            InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item = seatData.get(j);
            int devid = item.getDevid();
            jni.modifyMeetRanking(0, InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal_VALUE, devid);
        }
    }

}
