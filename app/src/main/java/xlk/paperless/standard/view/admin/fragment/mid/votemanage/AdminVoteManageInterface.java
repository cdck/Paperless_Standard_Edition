package xlk.paperless.standard.view.admin.fragment.mid.votemanage;

import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.List;

/**
 * @author Created by xlk on 2020/10/24.
 * @desc
 */
public interface AdminVoteManageInterface {
    /**
     * 更新投票列表
     * @param voteInfo 投票信息
     */
    void updateVoteRv(List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteInfo);

    /**
     * 更新加入投票的参会人列表
     * @param memberInfo 参会人信息
     */
    void updateMemberRv(List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> memberInfo);

    /**
     * 查看详情
     * @param vote 投票信息
     */
    void showSubmittedPop(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote);
}
