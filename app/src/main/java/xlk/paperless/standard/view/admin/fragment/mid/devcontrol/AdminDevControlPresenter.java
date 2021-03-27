package xlk.paperless.standard.view.admin.fragment.mid.devcontrol;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceContext;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.bean.DevControlBean;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.admin.fragment.pre.member.MemberRoleBean;

/**
 * @author Created by xlk on 2020/10/24.
 * @desc
 */
public class AdminDevControlPresenter extends BasePresenter {
    private final AdminDevControlInterface view;
    private List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatInfo = new ArrayList<>();
    public List<DevControlBean> devControlBeans = new ArrayList<>();
    private List<MemberRoleBean> devSeatInfos = new ArrayList<>();

    public AdminDevControlPresenter(AdminDevControlInterface view) {
        super();
        this.view = view;
        queryAttendPeople();
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //会场设备信息变更通知和界面状态变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE_VALUE:
                //设备当前会议
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEMEETSTATUS_VALUE:
                //会议排位变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT_VALUE:
                queryRankInfo();
                break;
            //设备寄存器变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO_VALUE:
                queryDevice();
                break;
            //参会人员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE: {
                LogUtil.i(TAG, "BusEvent -->" + "参会人员变更通知");
                queryAttendPeople();
                break;
            }
            default:
                break;
        }
    }

    public void queryAttendPeople() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo info = jni.queryAttendPeople();
            devSeatInfos.clear();
            if (info != null) {
                List<InterfaceMember.pbui_Item_MemberDetailInfo> itemList = info.getItemList();
                for (int j = 0; j < itemList.size(); j++) {
                    InterfaceMember.pbui_Item_MemberDetailInfo item = itemList.get(j);
                    devSeatInfos.add(new MemberRoleBean(item));
                }
            }
            queryRankInfo();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryRankInfo() {
        try {
            InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo seatDetailInfo = jni.placeDeviceRankingInfo(queryCurrentRoomId());
            seatInfo.clear();
            if (seatDetailInfo != null) {
                seatInfo.addAll(seatDetailInfo.getItemList());
                for (int i = 0; i < devSeatInfos.size(); i++) {
                    MemberRoleBean bean = devSeatInfos.get(i);
                    for (int j = 0; j < seatInfo.size(); j++) {
                        InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item = seatInfo.get(j);
                        if (item.getMemberid() == bean.getMember().getPersonid()) {
                            LogUtil.i(TAG, "queryRankInfo 更新座位 " + bean.getMember().getName().toStringUtf8());
                            bean.setSeat(item);
                            break;
                        }
                    }
                }
            }
            LogUtil.i(TAG, "queryRankInfo seatInfo.size=" + seatInfo.size());
            view.updateRoleRv(devSeatInfos);
            queryDevice();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public List<MemberRoleBean> getDevSeatInfos() {
        return devSeatInfos;
    }

    private void queryDevice() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = jni.queryDeviceInfo();
            devControlBeans.clear();
            if (deviceDetailInfo != null) {
                List<InterfaceDevice.pbui_Item_DeviceDetailInfo> pdevList = deviceDetailInfo.getPdevList();
                for (int i = 0; i < pdevList.size(); i++) {
                    InterfaceDevice.pbui_Item_DeviceDetailInfo dev = pdevList.get(i);
                    int devcieid = dev.getDevcieid();
                    //是否是茶水设备
                    boolean isTea = Constant.isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetService_VALUE, devcieid);
                    //是否是会议数据库设备
                    boolean isDatabase = Constant.isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetDBServer_VALUE, devcieid);
                    //公共设备需要添加进去
                    if (isTea || isDatabase) {
                        devControlBeans.add(new DevControlBean(dev));
                    } else {
                        for (int j = 0; j < seatInfo.size(); j++) {
                            InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo seat = seatInfo.get(j);
                            if (seat.getDevid() == devcieid) {
                                devControlBeans.add(new DevControlBean(dev, seat));
                                break;
                            }
                        }
                    }
                }
            }
            view.updateRv(devControlBeans);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * 操作
     *
     * @param value
     * @param ids
     */
    public void executeTerminalControl(int value, List<Integer> ids) {
        jni.executeTerminalControl(value, 0, 0, ids);
    }

    public void rise(int value, List<Integer> ids) {
        jni.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTUP_VALUE, value, 0, ids);
    }

    public void stop(int value, List<Integer> ids) {
        jni.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTSTOP_VALUE, value, 0, ids);
    }

    public void decline(int value, List<Integer> ids) {
        jni.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTDOWN_VALUE, value, 0, ids);
    }

    /**
     * 辅助签到
     *
     * @param ids 设备id
     */
    public void signAlterationOperate(List<Integer> ids) {
        jni.assistedSignIn(ids);
    }

    public void wakeOnLan(List<Integer> ids) {
        jni.wakeOnLan(ids);
    }

    public void modifyMemberRole(int memberid, int newRole, int devid) {
        jni.modifyMeetRanking(memberid, newRole, devid);
    }
}
