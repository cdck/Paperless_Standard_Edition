package xlk.paperless.standard.service;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceVote;

import xlk.paperless.standard.base.BaseInterface;
import xlk.paperless.standard.data.bean.MeetingInformation;

/**
 * @author xlk
 * @date 2020/3/26
 * @desc
 */
public interface IFab extends BaseInterface {

    void notifyOnLineAdapter();

    void notifyJoinAdapter();

    void showVoteView(InterfaceVote.pbui_Item_MeetOnVotingDetailInfo info);

    void closeVoteView();

    void showOpenCamera(int inviteflag, int operdeviceid);

    void showView(int inviteflag, int operdeviceid);

    void applyPermissionsInform(InterfaceDevice.pbui_Type_MeetRequestPrivilegeNotify info);

    void updateNoteContent(String content);

    void showFabButton();

    void hideAllWindow();

    void showMeetInfo(MeetingInformation meetingInformation);
}
