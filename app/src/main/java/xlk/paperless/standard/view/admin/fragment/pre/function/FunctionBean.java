package xlk.paperless.standard.view.admin.fragment.pre.function;


/**
 * @author Created by xlk on 2020/10/24.
 * @desc 会前设置-会议功能结构体，添加移除都需要重新设置position
 */
public class FunctionBean implements Comparable<FunctionBean> {
    int funcode;
    int position;

    public FunctionBean(int position) {
        this.position = position;
    }

    public FunctionBean(int funcode, int position) {
        this.funcode = funcode;
        this.position = position;
    }

    public int getFuncode() {
        return funcode;
    }

    public void setFuncode(int funcode) {
        this.funcode = funcode;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int compareTo(FunctionBean o) {
        return position - o.getPosition();
    }
}
