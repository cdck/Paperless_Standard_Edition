package xlk.paperless.standard.view.fragment.other.vote;

import com.mogujie.tt.protobuf.InterfaceVote;

/**
 * @author xlk
 * @date 2020/4/2
 * @desc
 */
public interface IVoteManage {
    void updateRv();

    void updateMemberRv();

    void showSubmittedPop(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote);

    void showChartPop(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote);
}
