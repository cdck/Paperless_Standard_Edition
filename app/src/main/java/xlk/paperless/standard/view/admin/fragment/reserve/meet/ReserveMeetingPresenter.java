package xlk.paperless.standard.view.admin.fragment.reserve.meet;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author Created by xlk on 2020/11/13.
 * @desc
 */
public class ReserveMeetingPresenter extends BasePresenter {
    private final ReserveMeetingInterface view;
    public List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> allRooms = new ArrayList<>();
    public List<InterfaceMeet.pbui_Item_MeetMeetInfo> allMeets = new ArrayList<>();

    public ReserveMeetingPresenter(ReserveMeetingInterface view) {
        super();
        this.view = view;
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //会议信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO_VALUE: {
                LogUtil.i(TAG, "busEvent 会议信息变更通知");
                queryMeet();
                break;
            }
            //会场信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOM_VALUE: {
                LogUtil.i(TAG, "BusEvent 会场信息变更通知");
                queryRoom();
                break;
            }
            default:
                break;
        }
    }

    public void queryMeet() {
        InterfaceMeet.pbui_Type_MeetMeetInfo pbui_type_meetMeetInfo = jni.queryAllMeeting();
        allMeets.clear();
        if (pbui_type_meetMeetInfo != null) {
            List<InterfaceMeet.pbui_Item_MeetMeetInfo> itemList = pbui_type_meetMeetInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceMeet.pbui_Item_MeetMeetInfo item = itemList.get(i);
                if (item.getStatus() != InterfaceMacro.Pb_MeetStatus.Pb_MEETING_STATUS_End_VALUE) {
                    allMeets.add(item);
                }
            }
        }
        view.updateMeet(allMeets);
    }

    public void queryRoom() {
        InterfaceRoom.pbui_Type_MeetRoomDetailInfo pbui_type_meetRoomDetailInfo = jni.queryRoom();
        allRooms.clear();
        if (pbui_type_meetRoomDetailInfo != null) {
            List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> itemList = pbui_type_meetRoomDetailInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                if (!itemList.get(i).getName().toStringUtf8().isEmpty()) {
                    allRooms.add(itemList.get(i));
                }
            }
        }
        view.updateRoom(allRooms);
    }
}
