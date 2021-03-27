package xlk.paperless.standard.view.admin.fragment.pre.function;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;

/**
 * @author Created by xlk on 2020/10/24.
 * @desc
 */
public class FunctionAdapter extends BaseQuickAdapter<FunctionBean, BaseViewHolder> {

    int selectedId = -1;

    public FunctionAdapter(int layoutResId, @Nullable List<FunctionBean> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public FunctionBean getSelected() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getFuncode() == selectedId) {
                return getData().get(i);
            }
        }
        return null;
    }

    @Override
    protected void convert(BaseViewHolder helper, FunctionBean item) {
        helper.setText(R.id.item_view_1, Constant.getFunctionString(getContext(), item.getFuncode()));
        boolean isSelected = selectedId == item.getFuncode();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_view_1, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor);
    }
}
