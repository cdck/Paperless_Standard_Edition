package xlk.paperless.standard.view.admin.fragment.system.room;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/9/19.
 * @desc
 */
public class AdminRoomAdapter extends BaseQuickAdapter<InterfaceRoom.pbui_Item_MeetRoomDetailInfo, BaseViewHolder> {
    int selectedId = 0;

    public AdminRoomAdapter(int layoutResId, @Nullable List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public int getSelectedId() {
        return selectedId;
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceRoom.pbui_Item_MeetRoomDetailInfo item) {
        helper.setText(R.id.item_table3_tv_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_table3_tv_2, item.getName().toStringUtf8())
                .setText(R.id.item_tv_location, item.getAddr().toStringUtf8())
                .setText(R.id.item_tv_remarks, item.getComment().toStringUtf8());
        int textColor = (selectedId == item.getRoomid()) ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_table3_tv_1, textColor)
                .setTextColor(R.id.item_table3_tv_2, textColor)
                .setTextColor(R.id.item_tv_location, textColor)
                .setTextColor(R.id.item_tv_remarks, textColor);

        int color = (selectedId == item.getRoomid()) ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_table3_tv_1, color)
                .setBackgroundColor(R.id.item_table3_tv_2, color)
                .setBackgroundColor(R.id.item_tv_location, color)
                .setBackgroundColor(R.id.item_tv_remarks, color);
    }
}
