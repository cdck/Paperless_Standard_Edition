package xlk.paperless.standard.data.exportbean;

import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.List;

/**
 * @author by xlk
 * @date 2020/6/15 14:45
 * @desc 说明
 */
public class ExportVoteInfo {
    List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteInfos;
    String createTime;


    public List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> getVoteInfos() {
        return voteInfos;
    }

    public String getCreateTime() {
        return createTime;
    }
}
