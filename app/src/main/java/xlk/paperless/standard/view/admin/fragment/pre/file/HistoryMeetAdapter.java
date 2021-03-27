package xlk.paperless.standard.view.admin.fragment.pre.file;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMeet;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/11/13.
 * @desc
 */
public class HistoryMeetAdapter extends BaseQuickAdapter<InterfaceMeet.pbui_Item_MeetMeetInfo, BaseViewHolder> {
    private int selectedId;

    public HistoryMeetAdapter(int layoutResId, @Nullable List<InterfaceMeet.pbui_Item_MeetMeetInfo> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceMeet.pbui_Item_MeetMeetInfo item) {
        helper.setText(R.id.item_view_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_view_2, item.getName().toStringUtf8());
        boolean isSelected = selectedId == item.getId();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor);
    }
}
