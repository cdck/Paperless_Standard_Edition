package xlk.paperless.standard.view.admin.fragment.pre.member;

import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfacePerson;

import java.util.List;

/**
 * @author Created by xlk on 2020/10/17.
 * @desc
 */
public interface MemberInterface {
    /**
     * 更新参会人列表
     * @param memberInfos 参会人信息
     */
    void updateMemberRv(List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos);

    /**
     * 更新PopupWindow中的常用人员信息
     */
    void updateFrequentlyMemberRv();

    /**
     * 更新参会人身份信息
     */
    void updateMemberRole();
}
