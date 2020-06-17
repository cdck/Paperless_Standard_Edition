package xlk.paperless.standard.view.fragment.other.election;


import com.mogujie.tt.protobuf.InterfaceVote;

/**
 * @author xlk
 * @date 2020/4/7
 * @desc
 */
public interface IElectionManage {
    void updateRv();

    void updateMemberRv();

    void showSubmittedPop(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote);

    void showChartPop(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote);
}
