package xlk.paperless.standard.view.admin.fragment.pre.member;


/**
 * @author Created by xlk on 2020/10/19.
 * @desc 参会人权限
 */
public class MemberPermissionBean {
    int memberId;
    int permission;
    String name;

    public MemberPermissionBean(int memberId, int permission, String name) {
        this.memberId = memberId;
        this.permission = permission;
        this.name = name;
    }

    public int getMemberId() {
        return memberId;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MemberPermissionBean{" +
                "memberId=" + memberId +
                ", permission=" + permission +
                ", name='" + name + '\'' +
                '}';
    }
}
