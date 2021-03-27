package xlk.paperless.standard.view.admin.fragment.pre.function;


import java.util.List;

/**
 * @author Created by xlk on 2020/10/24.
 * @desc
 */
public interface FunctionInterface {
    /**
     * 更新会议功能列表
     * @param meetFunction 会议功能
     * @param hideMeetFunction
     */
    void updateFunctionRv(List<FunctionBean> meetFunction, List<FunctionBean> hideMeetFunction);
}
