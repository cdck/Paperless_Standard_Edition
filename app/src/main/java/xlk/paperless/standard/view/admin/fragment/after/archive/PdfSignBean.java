package xlk.paperless.standard.view.admin.fragment.after.archive;

import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

import xlk.paperless.standard.view.admin.fragment.after.signin.SignInBean;

/**
 * @author Created by xlk on 2020/10/29.
 * @desc
 */
public class PdfSignBean {
    InterfaceMeet.pbui_Item_MeetMeetInfo meetInfo;
    InterfaceRoom.pbui_Item_MeetRoomDetailInfo roomInfo;
    List<SignInBean> signInBeans;
    int signInCount;

    public PdfSignBean(InterfaceMeet.pbui_Item_MeetMeetInfo meetInfo, InterfaceRoom.pbui_Item_MeetRoomDetailInfo roomInfo, List<SignInBean> signInBeans, int signInCount) {
        this.meetInfo = meetInfo;
        this.roomInfo = roomInfo;
        this.signInBeans = signInBeans;
        this.signInCount = signInCount;
    }

    public InterfaceMeet.pbui_Item_MeetMeetInfo getMeetInfo() {
        return meetInfo;
    }

    public InterfaceRoom.pbui_Item_MeetRoomDetailInfo getRoomInfo() {
        return roomInfo;
    }

    public List<SignInBean> getSignInBeans() {
        return signInBeans;
    }

    public int getSignInCount() {
        return signInCount;
    }
}
