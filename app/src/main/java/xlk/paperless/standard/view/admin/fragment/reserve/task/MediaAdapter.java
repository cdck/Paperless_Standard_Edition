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
public class MediaAdapter extends BaseQuickAdapter<InterfaceTask.pbui_Item_MediaTaskDetailInfo, BaseViewHolder> {

    private int selectedId;

    public MediaAdapter(int layoutResId, @Nullable List<InterfaceTask.pbui_Item_MediaTaskDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceTask.pbui_Item_MediaTaskDetailInfo item) {
        helper.setText(R.id.item_view_1, item.getName().toStringUtf8());
        boolean isSelected = selectedId == item.getMediaid();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.black);
        helper.setTextColor(R.id.item_view_1, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor);
    }

    public void setSelect(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public InterfaceTask.pbui_Item_MediaTaskDetailInfo getSelected() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getMediaid() == selectedId) {
                return getData().get(i);
            }
        }
        return null;
    }
}
