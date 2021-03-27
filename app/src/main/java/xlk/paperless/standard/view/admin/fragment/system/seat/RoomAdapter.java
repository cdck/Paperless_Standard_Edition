package xlk.paperless.standard.view.admin.fragment.system.seat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/11/3.
 * @desc 会议排位中会议室列表
 */
public class RoomAdapter extends BaseQuickAdapter<InterfaceRoom.pbui_Item_MeetRoomDetailInfo, BaseViewHolder> {
    int selectedId = -1;

    public RoomAdapter(int layoutResId, @Nullable List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public int getSelectedId() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getRoomid() == selectedId) {
                return getData().get(i).getRoomid();
            }
        }
        return -1;
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceRoom.pbui_Item_MeetRoomDetailInfo item) {
        helper.setText(R.id.item_view_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_view_2, item.getName().toStringUtf8());
        boolean isSelected = selectedId == item.getRoomid();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor);
    }
}
