package xlk.paperless.standard.view.admin.fragment.pre.file;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/10/21.
 * @desc
 */
public class DirAdapter extends BaseQuickAdapter<InterfaceFile.pbui_Item_MeetDirDetailInfo, BaseViewHolder> {
    private int selectedId;

    public DirAdapter(int layoutResId, @Nullable List<InterfaceFile.pbui_Item_MeetDirDetailInfo> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public InterfaceFile.pbui_Item_MeetDirDetailInfo getSelected() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getId() == selectedId) {
                return getData().get(i);
            }
        }
        return null;
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceFile.pbui_Item_MeetDirDetailInfo item) {
        helper.setText(R.id.item_view_1, String.valueOf(item.getId()))
                .setText(R.id.item_view_2, item.getName().toStringUtf8());
        boolean isSelected = selectedId == item.getId();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor);
    }
}
