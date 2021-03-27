package xlk.paperless.standard.view.fragment.other.devicecontrol;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.DevControlBean;
import xlk.paperless.standard.base.BasePresenter;


/**
 * @author xlk
 * @date 2020/4/1
 * @desc
 */
public class DeviceControlPresenter extends BasePresenter {
    private final Context cxt;
    private final IDevControl view;
    public List<DevControlBean> devControlBeans = new ArrayList<>();
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceInfos = new ArrayList<>();
    private List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatInfos = new ArrayList<>();

    public DeviceControlPresenter(Context cxt, IDevControl view) {
        super();
        this.cxt = cxt;
        this.view = view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE_VALUE://会场设备信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEMEETSTATUS_VALUE://界面状态变更通知
                queryRankInfo();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO_VALUE://设备寄存器变更通知
                queryDevice();
                break;
            default:break;
        }
    }

    public void queryDevice() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = jni.queryDeviceInfo();
            if (deviceDetailInfo == null) {
                return;
            }
            devControlBeans.clear();
            deviceInfos.clear();
            deviceInfos.addAll(deviceDetailInfo.getPdevList());
            for (int i = 0; i < deviceInfos.size(); i++) {
                InterfaceDevice.pbui_Item_DeviceDetailInfo dev = deviceInfos.get(i);
                int devcieid = dev.getDevcieid();
                //是否是茶水设备
                boolean isTea = Constant.isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetService_VALUE,devcieid);
                //是否是会议数据库设备
                boolean isDatabase = Constant.isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetDBServer_VALUE,devcieid);
                if (isTea || isDatabase) {//公共设备
                    devControlBeans.add(new DevControlBean(dev));
                } else {
                    for (int j = 0; j < seatInfos.size(); j++) {
                        InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo seat = seatInfos.get(j);
                        if (seat.getDevid() == devcieid) {
                            devControlBeans.add(new DevControlBean(dev, seat));
                            break;
                        }
                    }
                }
            }
            view.updateRv();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryRankInfo() {
        try {
            InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo seatDetailInfo = jni.placeDeviceRankingInfo(Values.localRoomId);
            if (seatDetailInfo == null) {
                return;
            }
            seatInfos.clear();
            seatInfos.addAll(seatDetailInfo.getItemList());
            queryDevice();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void rise(int value,List<Integer> ids){
        jni.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTUP_VALUE, value, 0, ids);
    }

    public void stop(int value,List<Integer> ids){
        jni.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTSTOP_VALUE, value, 0, ids);
    }

    public void decline(int value,List<Integer> ids){
        jni.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_LIFTDOWN_VALUE, value, 0, ids);
    }

    public void executeTerminalControl(int value, List<Integer> ids) {
        jni.executeTerminalControl(value, 0, 0, ids);
    }

    //辅助签到
    public void signAlterationOperate(List<Integer> ids) {
        jni.assistedSignIn(ids);
    }

    public void modifMeetRanking(int memberid, int role, Integer devId) {
        jni.modifyMeetRanking(memberid, role, devId);
    }
}
