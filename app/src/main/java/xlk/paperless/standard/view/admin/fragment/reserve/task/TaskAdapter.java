package xlk.paperless.standard.view.admin.fragment.reserve.task;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceTask;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/11/16.
 * @desc
 */
public class TaskAdapter extends BaseQuickAdapter<InterfaceTask.pbui_Item_MeetTaskInfo, BaseViewHolder> {
    private int selectId;

    public TaskAdapter(int layoutResId, @Nullable List<InterfaceTask.pbui_Item_MeetTaskInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceTask.pbui_Item_MeetTaskInfo item) {
        helper.setText(R.id.item_view_1, item.getTaskname().toStringUtf8());
        boolean isSelected = selectId == item.getTaskid();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.black);
        helper.setTextColor(R.id.item_view_1, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor);
    }

    public void setSelect(int taskid) {
        selectId = taskid;
        notifyDataSetChanged();
    }

    public int getSelectId() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getTaskid() == selectId) {
                return selectId;
            }
        }
        return -1;
    }

    public InterfaceTask.pbui_Item_MeetTaskInfo getSelect() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getTaskid() == selectId) {
                return getData().get(i);
            }
        }
        return null;
    }
}
