package xlk.paperless.standard.view.admin.fragment.reserve.email;

import com.mogujie.tt.protobuf.InterfaceMeet;

/**
 * @author Created by xlk on 2020/11/14.
 * @desc
 */
public interface SendEmailInterface {
    void updateMember();

    void updateMeetName(InterfaceMeet.pbui_Item_MeetMeetInfo currentMeet);
}
