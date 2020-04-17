package xlk.paperless.standard.data.bean;

import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceVote;

/**
 * @author xlk
 * @date 2020/4/3
 * @Description: 投票提交人
 */
public class SubmitMember {
    InterfaceMember.pbui_Item_MeetMemberDetailInfo memberInfo;
    InterfaceVote.pbui_Item_MeetVoteSignInDetailInfo voteInfo;
    String answer;

    public SubmitMember(InterfaceMember.pbui_Item_MeetMemberDetailInfo memberInfo, InterfaceVote.pbui_Item_MeetVoteSignInDetailInfo voteInfo, String answer) {
        this.memberInfo = memberInfo;
        this.voteInfo = voteInfo;
        this.answer = answer;
    }

    public InterfaceMember.pbui_Item_MeetMemberDetailInfo getMemberInfo() {
        return memberInfo;
    }

    public InterfaceVote.pbui_Item_MeetVoteSignInDetailInfo getVoteInfo() {
        return voteInfo;
    }

    public String getAnswer() {
        return answer;
    }
}
