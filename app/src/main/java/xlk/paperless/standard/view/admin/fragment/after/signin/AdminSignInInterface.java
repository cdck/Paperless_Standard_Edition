package xlk.paperless.standard.view.admin.fragment.after.signin;

import java.util.List;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc
 */
public interface AdminSignInInterface {
    /**
     * 更新签到列表和签到人数
     * @param signInBeans   签到列表信息
     * @param signInCount   已签到的人数
     */
    void update(List<SignInBean> signInBeans, int signInCount);
}
