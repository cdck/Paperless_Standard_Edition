package xlk.paperless.standard.view.admin.fragment.system.secretary;

import com.mogujie.tt.protobuf.InterfaceAdmin;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

/**
 * @author Created by xlk on 2020/9/21.
 * @desc
 */
public interface AdminSecretaryManageInterface {
    void updateAdminRv(List<InterfaceAdmin.pbui_Item_AdminDetailInfo> adminInfos);

    void updateControllableRoomsRv(List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> controllableRooms);

    void updateAllRoomsRv(List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> allRooms);
}
