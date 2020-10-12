package xlk.paperless.standard.view.admin.fragment.system.room;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

/**
 * @author Created by xlk on 2020/9/19.
 * @desc
 */
public interface AdminRoomManageInterface {

    void updateRoomRv(List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> roomInfos);

    void updateRoomDeviceRv(List<InterfaceDevice.pbui_Item_DeviceDetailInfo> roomDevices);

    void updateAllDeviceRv(List<InterfaceDevice.pbui_Item_DeviceDetailInfo> allDevices);
}
