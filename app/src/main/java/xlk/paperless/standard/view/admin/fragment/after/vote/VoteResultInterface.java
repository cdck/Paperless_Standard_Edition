package xlk.paperless.standard.view.admin.fragment.after.vote;

import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.List;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc
 */
public interface VoteResultInterface {
    /**
     * 更新投票列表
     * @param voteInfo  投票/选举信息
     */
    void updateVoteRv(List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteInfo);

    void showSubmittedPop(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote);

    void showChartPop(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote);
}
