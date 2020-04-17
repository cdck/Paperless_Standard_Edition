package xlk.paperless.standard.data.bean;

import com.mogujie.tt.protobuf.InterfaceFilescorevote;
import com.mogujie.tt.protobuf.InterfaceMember;

/**
 * @author xlk
 * @date 2020/4/9
 * @Description:
 */
public class ScoreMember {
    InterfaceMember.pbui_Item_MemberDetailInfo member;
    InterfaceFilescorevote.pbui_Type_Item_FileScoreMemberStatistic score;

    public ScoreMember(InterfaceMember.pbui_Item_MemberDetailInfo member, InterfaceFilescorevote.pbui_Type_Item_FileScoreMemberStatistic score) {
        this.member = member;
        this.score = score;
    }

    public InterfaceMember.pbui_Item_MemberDetailInfo getMember() {
        return member;
    }

    public InterfaceFilescorevote.pbui_Type_Item_FileScoreMemberStatistic getScore() {
        return score;
    }
}
