package xlk.paperless.standard.view.admin.fragment.mid.devcontrol;

import java.util.List;

import xlk.paperless.standard.data.bean.DevControlBean;
import xlk.paperless.standard.view.admin.fragment.pre.member.MemberRoleBean;

/**
 * @author Created by xlk on 2020/10/24.
 * @desc
 */
public interface AdminDevControlInterface {
    /**
     * 更新设备列表
     * @param devControlBeans 设备信息
     */
    void updateRv(List<DevControlBean> devControlBeans);

    /**
     * 更新参会人身份列表
     * @param devSeatInfos 参会人身份相关信息
     */
    void updateRoleRv(List<MemberRoleBean> devSeatInfos);
}
