package xlk.paperless.standard.view.admin.fragment.reserve.task;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/11/17.
 * @desc
 */
public class PopMediaAdapter extends BaseQuickAdapter<InterfaceFile.pbui_Item_MeetDirFileDetailInfo, BaseViewHolder> {
    List<Integer> checks = new ArrayList<>();

    public PopMediaAdapter(int layoutResId, @Nullable List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceFile.pbui_Item_MeetDirFileDetailInfo item) {
        helper.setText(R.id.item_view_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_view_2, item.getName().toStringUtf8())
                .setText(R.id.item_view_3, String.valueOf(item.getMediaid()));
        boolean isSelected = checks.contains(item.getMediaid());
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor)
                .setTextColor(R.id.item_view_3, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor)
                .setBackgroundColor(R.id.item_view_3, backgroundColor);
    }

    public void setSelect(int mediaid) {
        if (checks.contains(mediaid)) {
            checks.remove(checks.indexOf(mediaid));
        } else {
            checks.add(mediaid);
        }
        notifyDataSetChanged();
    }

    public List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> getSelect() {
        List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> temps = new ArrayList<>();
        for (int i = 0; i < getData().size(); i++) {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo item = getData().get(i);
            if (checks.contains(item.getMediaid())) {
                temps.add(item);
            }
        }
        return temps;
    }
}
