package xlk.paperless.standard.data.bean;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceRoom;

/**
 * @author xlk
 * @date 2020/4/1
 * @desc 设备控制adapter使用
 */
public class DevControlBean {
    InterfaceDevice.pbui_Item_DeviceDetailInfo deviceInfo;
    InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo seatInfo;

    public DevControlBean(InterfaceDevice.pbui_Item_DeviceDetailInfo deviceInfo, InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo seatInfo) {
        this.deviceInfo = deviceInfo;
        this.seatInfo = seatInfo;
    }

    public DevControlBean(InterfaceDevice.pbui_Item_DeviceDetailInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public InterfaceDevice.pbui_Item_DeviceDetailInfo getDeviceInfo() {
        return deviceInfo;
    }

    public InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo getSeatInfo() {
        return seatInfo;
    }
}
