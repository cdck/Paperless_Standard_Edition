package xlk.paperless.standard.view.admin.fragment.pre.meetingManage;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMeet;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/11/12.
 * @desc 过滤会议
 */
public class FilterMeetAdapter extends BaseQuickAdapter<InterfaceMeet.pbui_Item_MeetMeetInfo, BaseViewHolder> {

    public FilterMeetAdapter(int layoutResId, @Nullable List<InterfaceMeet.pbui_Item_MeetMeetInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceMeet.pbui_Item_MeetMeetInfo item) {
        helper.setText(R.id.item_view_1, item.getName().toStringUtf8());
        int textColor = getContext().getColor(R.color.black);
        helper.setTextColor(R.id.item_view_1, textColor);
        int backgroundColor = getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor);
    }
}
