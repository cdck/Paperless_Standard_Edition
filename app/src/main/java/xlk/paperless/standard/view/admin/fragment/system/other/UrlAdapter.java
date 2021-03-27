package xlk.paperless.standard.view.admin.fragment.system.other;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceBase;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/11/14.
 * @desc
 */
public class UrlAdapter extends BaseQuickAdapter<InterfaceBase.pbui_Item_UrlDetailInfo, BaseViewHolder> {
    private int selectedId;

    public UrlAdapter(int layoutResId, @Nullable List<InterfaceBase.pbui_Item_UrlDetailInfo> data) {
        super(layoutResId, data);
    }

    public void setSelect(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public int getSelectedId() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getId() == selectedId) {
                return selectedId;
            }
        }
        return -1;
    }

    public InterfaceBase.pbui_Item_UrlDetailInfo getSelectUrl() {
        for (int i = 0; i < getData().size(); i++) {
            InterfaceBase.pbui_Item_UrlDetailInfo item = getData().get(i);
            if (item.getId() == selectedId) {
                return item;
            }
        }
        return null;
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceBase.pbui_Item_UrlDetailInfo item) {
        helper.setText(R.id.item_view_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_view_2, item.getName().toStringUtf8())
                .setText(R.id.item_view_3, item.getAddr().toStringUtf8());
        boolean isSelected = selectedId == item.getId();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor)
                .setTextColor(R.id.item_view_3, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor)
                .setBackgroundColor(R.id.item_view_3, backgroundColor);
    }
}
