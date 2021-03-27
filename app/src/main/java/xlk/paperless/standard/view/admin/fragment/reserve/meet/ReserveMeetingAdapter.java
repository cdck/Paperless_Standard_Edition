package xlk.paperless.standard.view.admin.fragment.reserve.meet;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;
import xlk.paperless.standard.util.DateUtil;

/**
 * @author Created by xlk on 2020/11/13.
 * @desc
 */
public class ReserveMeetingAdapter extends BaseQuickAdapter<InterfaceMeet.pbui_Item_MeetMeetInfo, BaseViewHolder> {
    private int selectedId;

    public ReserveMeetingAdapter(int layoutResId, @Nullable List<InterfaceMeet.pbui_Item_MeetMeetInfo> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public InterfaceMeet.pbui_Item_MeetMeetInfo getSelected() {
        for (int i = 0; i < getData().size(); i++) {
            InterfaceMeet.pbui_Item_MeetMeetInfo item = getData().get(i);
            if (item.getId() == selectedId) {
                return item;
            }
        }
        return null;
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceMeet.pbui_Item_MeetMeetInfo item) {
        helper.setText(R.id.item_view_1, String.valueOf(item.getId()))
                .setText(R.id.item_view_2, item.getName().toStringUtf8())
                .setText(R.id.item_view_3, getStatus(item.getStatus()))
                .setText(R.id.item_view_4, item.getSecrecy() == 1 ? getContext().getString(R.string.yes) : getContext().getString(R.string.no))
                .setText(R.id.item_view_5, DateUtil.secondFormatDateTime(item.getStartTime()))
                .setText(R.id.item_view_6, DateUtil.secondFormatDateTime(item.getEndTime()))
                .setText(R.id.item_view_7, item.getOrdername().toStringUtf8());
        boolean isSelected = selectedId == item.getId();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor)
                .setTextColor(R.id.item_view_3, textColor)
                .setTextColor(R.id.item_view_4, textColor)
                .setTextColor(R.id.item_view_5, textColor)
                .setTextColor(R.id.item_view_6, textColor)
                .setTextColor(R.id.item_view_7, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor)
                .setBackgroundColor(R.id.item_view_3, backgroundColor)
                .setBackgroundColor(R.id.item_view_4, backgroundColor)
                .setBackgroundColor(R.id.item_view_5, backgroundColor)
                .setBackgroundColor(R.id.item_view_6, backgroundColor)
                .setBackgroundColor(R.id.item_view_7, backgroundColor);
    }

    /**
     * 获取会议状态
     *
     * @param status 会议状态，0为未开始会议，1为已开始会议，2为已结束会议
     */
    private String getStatus(int status) {
        if (status == InterfaceMacro.Pb_MeetStatus.Pb_MEETING_STATUS_Start_VALUE) {
            return getContext().getString(R.string.Ongoing);
        }
        return getContext().getString(R.string.not_started);
    }
}
