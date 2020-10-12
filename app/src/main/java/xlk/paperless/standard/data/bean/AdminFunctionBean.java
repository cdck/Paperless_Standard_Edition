package xlk.paperless.standard.data.bean;

/**
 * @author Created by xlk on 2020/9/17.
 * @desc 后台管理界面功能模块
 */
public class AdminFunctionBean {
    int drawableResId;

    public AdminFunctionBean(int drawableResId) {
        this.drawableResId = drawableResId;
    }

    public int getDrawableResId() {
        return drawableResId;
    }
}
