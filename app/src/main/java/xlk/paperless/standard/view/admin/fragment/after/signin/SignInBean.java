package xlk.paperless.standard.view.admin.fragment.after.signin;

import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceSignin;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc 会后查看-签到信息
 */
public class SignInBean {
    InterfaceMember.pbui_Item_MemberDetailInfo member;
    InterfaceSignin.pbui_Item_MeetSignInDetailInfo sign;

    public SignInBean(InterfaceMember.pbui_Item_MemberDetailInfo member) {
        this.member = member;
    }

    public InterfaceMember.pbui_Item_MemberDetailInfo getMember() {
        return member;
    }

    public void setMember(InterfaceMember.pbui_Item_MemberDetailInfo member) {
        this.member = member;
    }

    public InterfaceSignin.pbui_Item_MeetSignInDetailInfo getSign() {
        return sign;
    }

    public void setSign(InterfaceSignin.pbui_Item_MeetSignInDetailInfo sign) {
        this.sign = sign;
    }
}
