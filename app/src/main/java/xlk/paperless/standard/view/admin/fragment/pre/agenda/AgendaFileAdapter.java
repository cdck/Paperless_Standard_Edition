package xlk.paperless.standard.view.admin.fragment.pre.agenda;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/10/20.
 * @desc
 */
public class AgendaFileAdapter extends BaseQuickAdapter<InterfaceFile.pbui_Item_MeetDirFileDetailInfo, BaseViewHolder> {
    private int selectedId;

    public AgendaFileAdapter(int layoutResId, @Nullable List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceFile.pbui_Item_MeetDirFileDetailInfo item) {
        helper.setText(R.id.item_tv_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_tv_2, item.getName().toStringUtf8())
                .setText(R.id.item_tv_3, String.valueOf(item.getMediaid()));
        boolean isSelected = selectedId == item.getMediaid();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_tv_1, textColor)
                .setTextColor(R.id.item_tv_2, textColor)
                .setTextColor(R.id.item_tv_3, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_tv_1, backgroundColor)
                .setBackgroundColor(R.id.item_tv_2, backgroundColor)
                .setBackgroundColor(R.id.item_tv_3, backgroundColor);
    }

    public void setSelected(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public InterfaceFile.pbui_Item_MeetDirFileDetailInfo getSelected() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getMediaid() == selectedId) {
                return getData().get(i);
            }
        }
        return null;
    }
}
