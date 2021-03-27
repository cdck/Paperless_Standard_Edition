package xlk.paperless.standard.view.admin.fragment.pre.file;

import com.mogujie.tt.protobuf.InterfaceMember;

/**
 * @author Created by xlk on 2020/10/21.
 * @desc
 */
public class MemberDirPermissionBean {
    InterfaceMember.pbui_Item_MemberDetailInfo member;
    /**
     * 是否在黑名单
     */
    boolean blacklist;

    public MemberDirPermissionBean(InterfaceMember.pbui_Item_MemberDetailInfo member) {
        this.member = member;
    }

    public InterfaceMember.pbui_Item_MemberDetailInfo getMember() {
        return member;
    }

    public void setMember(InterfaceMember.pbui_Item_MemberDetailInfo member) {
        this.member = member;
    }

    public boolean isBlacklist() {
        return blacklist;
    }

    public void setBlacklist(boolean blacklist) {
        this.blacklist = blacklist;
    }

    public boolean isSame(InterfaceMember.pbui_Item_MemberDetailInfo item) {
        return member.getPersonid() == item.getPersonid();
    }
}
