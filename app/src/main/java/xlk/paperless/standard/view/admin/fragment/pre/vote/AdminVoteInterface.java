package xlk.paperless.standard.view.admin.fragment.pre.vote;

import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.List;

/**
 * @author Created by xlk on 2020/10/23.
 * @desc
 */
public interface AdminVoteInterface {
    /**
     * 更新投票列表
     * @param voteInfo 投票信息
     */
    void updateVoteRv(List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteInfo);
}
