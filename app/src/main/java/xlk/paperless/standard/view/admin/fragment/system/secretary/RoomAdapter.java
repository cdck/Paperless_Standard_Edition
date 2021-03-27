package xlk.paperless.standard.view.admin.fragment.system.secretary;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/9/21.
 * @desc 秘书可控会场
 */
public class RoomAdapter extends BaseQuickAdapter<InterfaceRoom.pbui_Item_MeetRoomDetailInfo, BaseViewHolder> {
    private int selectId;

    public RoomAdapter(int layoutResId, @Nullable List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceRoom.pbui_Item_MeetRoomDetailInfo item) {
        helper.setText(R.id.item_table3_tv_1, String.valueOf(item.getRoomid()))
                .setText(R.id.item_table3_tv_2, item.getName().toStringUtf8())
                .setText(R.id.item_table3_tv_3, item.getAddr().toStringUtf8());

        int textColor = (selectId == item.getRoomid()) ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_table3_tv_1, textColor)
                .setTextColor(R.id.item_table3_tv_2, textColor)
                .setTextColor(R.id.item_table3_tv_3, textColor);

        int color = (selectId == item.getRoomid()) ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_table3_tv_1, color)
                .setBackgroundColor(R.id.item_table3_tv_2, color)
                .setBackgroundColor(R.id.item_table3_tv_3, color);
    }

    public void setSelect(int roomid) {
        selectId = roomid;
        notifyDataSetChanged();
    }
}
