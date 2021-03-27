package xlk.paperless.standard.view.admin.fragment.reserve.meet;

import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

/**
 * @author Created by xlk on 2020/11/13.
 * @desc
 */
public interface ReserveMeetingInterface {
    void updateRoom(List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> allRooms);

    void updateMeet(List<InterfaceMeet.pbui_Item_MeetMeetInfo> allMeets);
}
