package xlk.paperless.standard.view.admin.fragment.pre.election;

import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.List;

/**
 * @author Created by xlk on 2020/10/23.
 * @desc
 */
public interface AdminElectionInterface {
    /**
     * 更新选举列表
     * @param electionInfo 选举数据
     */
    void updateElectionRv(List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> electionInfo);
}
