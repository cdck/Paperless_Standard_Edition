package xlk.paperless.standard.view.admin.fragment.pre.meetingManage;

import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.base.BaseInterface;

/**
 * @author Created by xlk on 2020/10/15.
 * @desc
 */
public interface MeetingManageInterface extends BaseInterface {
    /**
     * 更新会议列表
     * @param meetings 会议数据
     */
    void updateMeetingRv(List<InterfaceMeet.pbui_Item_MeetMeetInfo> meetings);

    /**
     * 更新会议室Spinner
     * @param roomNames 所有会议室的名称
     */
    void updateRooms(ArrayList<String> roomNames);
}
