package xlk.paperless.standard.view.admin.fragment.pre.meetingManage;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;
import xlk.paperless.standard.util.DateUtil;

/**
 * @author Created by xlk on 2020/10/17.
 * @desc 会议管理
 */
public class MeetingAdapter extends BaseQuickAdapter<InterfaceMeet.pbui_Item_MeetMeetInfo, BaseViewHolder> {


    private int selectedId;

    public void setSelected(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public MeetingAdapter(int layoutResId, @Nullable List<InterfaceMeet.pbui_Item_MeetMeetInfo> data) {
        super(layoutResId, data);
    }


    public InterfaceMeet.pbui_Item_MeetMeetInfo getSelectedMeeting() {
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
        helper.setText(R.id.tv_number, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.tv_name, item.getName().toStringUtf8())
                .setText(R.id.tv_status, getStatus(item.getStatus()))
                .setText(R.id.tv_room, item.getRoomname().toStringUtf8())
                .setText(R.id.tv_confidential, item.getSecrecy() == 1 ? getContext().getString(R.string.yes) : getContext().getString(R.string.no))
                .setText(R.id.tv_start_time, DateUtil.secondFormatDateTime(item.getStartTime()))
                .setText(R.id.tv_end_time, DateUtil.secondFormatDateTime(item.getEndTime()))
                .setText(R.id.tv_reservation, item.getOrdername().toStringUtf8());
        int textColor = (selectedId == item.getId()) ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.tv_number, textColor)
                .setTextColor(R.id.tv_name, textColor)
                .setTextColor(R.id.tv_status, textColor)
                .setTextColor(R.id.tv_room, textColor)
                .setTextColor(R.id.tv_confidential, textColor)
                .setTextColor(R.id.tv_start_time, textColor)
                .setTextColor(R.id.tv_end_time, textColor)
                .setTextColor(R.id.tv_reservation, textColor);

        int color = (selectedId == item.getId()) ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.tv_number, color)
                .setBackgroundColor(R.id.tv_name, color)
                .setBackgroundColor(R.id.tv_status, color)
                .setBackgroundColor(R.id.tv_room, color)
                .setBackgroundColor(R.id.tv_confidential, color)
                .setBackgroundColor(R.id.tv_start_time, color)
                .setBackgroundColor(R.id.tv_end_time, color)
                .setBackgroundColor(R.id.tv_reservation, color);
    }


    /**
     * 获取会议状态
     *
     * @param status 会议状态，0为未开始会议，1为已开始会议，2为已结束会议
     */
    private String getStatus(int status) {
        switch (status) {
            case InterfaceMacro.Pb_MeetStatus.Pb_MEETING_STATUS_Ready_VALUE:
                return getContext().getString(R.string.not_started);
            case InterfaceMacro.Pb_MeetStatus.Pb_MEETING_STATUS_Start_VALUE:
                return getContext().getString(R.string.Ongoing);
            case InterfaceMacro.Pb_MeetStatus.Pb_MEETING_STATUS_End_VALUE:
                return getContext().getString(R.string.over);
            case InterfaceMacro.Pb_MeetStatus.Pb_MEETING_STATUS_PAUSE_VALUE:
                return getContext().getString(R.string.meet_pause);
            case InterfaceMacro.Pb_MeetStatus.Pb_MEETING_MODEL_VALUE:
                return getContext().getString(R.string.template);
            default:
                return "";
        }
    }
}
