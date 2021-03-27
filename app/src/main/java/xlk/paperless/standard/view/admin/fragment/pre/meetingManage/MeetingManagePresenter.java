package xlk.paperless.standard.view.admin.fragment.pre.meetingManage;

import android.content.Context;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author Created by xlk on 2020/10/15.
 * @desc
 */
public class MeetingManagePresenter extends BasePresenter {
    private final MeetingManageInterface view;
    /**
     * 所有会议
     */
    public List<InterfaceMeet.pbui_Item_MeetMeetInfo> meetings = new ArrayList<>();
    /**
     * 所有会议室
     */
    private List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> rooms = new ArrayList<>();

    public MeetingManagePresenter(MeetingManageInterface view) {
        super();
        this.view = view;
    }

    void queryRoom() {
        InterfaceRoom.pbui_Type_MeetRoomDetailInfo infos = jni.queryRoom();
        rooms.clear();
        ArrayList<String> roomNames = new ArrayList<>();
        if (infos != null) {
            List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> itemList = infos.getItemList();
            for (InterfaceRoom.pbui_Item_MeetRoomDetailInfo item : itemList) {
                if(!item.getName().isEmpty()) {
                    roomNames.add(item.getName().toStringUtf8());
                    rooms.add(item);
                }
            }
        }
        view.updateRooms(roomNames);
        queryAllMeeting();
    }

    void queryAllMeeting() {
        InterfaceMeet.pbui_Type_MeetMeetInfo infos = jni.queryAllMeeting();
        meetings.clear();
        if (infos != null) {
            List<InterfaceMeet.pbui_Item_MeetMeetInfo> itemList = infos.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceMeet.pbui_Item_MeetMeetInfo pbui_item_meetMeetInfo = itemList.get(i);
                int id = pbui_item_meetMeetInfo.getId();
                String s = pbui_item_meetMeetInfo.getName().toStringUtf8();
                LogUtil.i(TAG, "queryAllMeeting 会议名称=" + s + ", id=" + id);
            }
            meetings.addAll(itemList);
        }
        view.updateMeetingRv(meetings);
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //会议信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO_VALUE: {
                queryAllMeeting();
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

    public int getCurrentRoom(int roomId) {
        for (int i = 0; i < rooms.size(); i++) {
            if (roomId == rooms.get(i).getRoomid()) {
                return i;
            }
        }
        return 0;
    }

    public void copyMeeting(InterfaceMeet.pbui_Item_MeetMeetInfo item) {
        jni.copyMeeting(item);
    }

    public void addMeeting(InterfaceMeet.pbui_Item_MeetMeetInfo item) {
        jni.addMeeting(item);
    }

    public void modifyMeeting(InterfaceMeet.pbui_Item_MeetMeetInfo item) {
        jni.modifyMeeting(item);
    }

    public void delMeeting(InterfaceMeet.pbui_Item_MeetMeetInfo item) {
        jni.delMeeting(item);
    }

    public void modifyMeetingStatus(int meetId, int status) {
        jni.modifyMeetingStatus(meetId, status);
    }

    public void switchMeeting(int meetId) {
        jni.modifyContextProperties(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_CURMEETINGID_VALUE, meetId);
    }
}
