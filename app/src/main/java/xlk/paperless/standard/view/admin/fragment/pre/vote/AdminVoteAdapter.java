package xlk.paperless.standard.view.admin.fragment.pre.vote;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;

/**
 * @author Created by xlk on 2020/10/23.
 * @desc
 */
public class AdminVoteAdapter extends BaseQuickAdapter<InterfaceVote.pbui_Item_MeetVoteDetailInfo, BaseViewHolder> {
    int selectedId;

    public AdminVoteAdapter(int layoutResId, @Nullable List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public InterfaceVote.pbui_Item_MeetVoteDetailInfo getSelected() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getVoteid() == selectedId) {
                return getData().get(i);
            }
        }
        return null;
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceVote.pbui_Item_MeetVoteDetailInfo item) {
        helper.setText(R.id.item_view_1, String.valueOf(item.getVoteid()))
                .setText(R.id.item_view_2, item.getContent().toStringUtf8())
                .setText(R.id.item_view_3, item.getMode() == 1 ? getContext().getString(R.string.yes) : getContext().getString(R.string.no))
                .setText(R.id.item_view_4, Constant.getVoteStateName(getContext(), item.getVotestate()));
        boolean isSelected = selectedId == item.getVoteid();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor)
                .setTextColor(R.id.item_view_3, textColor)
                .setTextColor(R.id.item_view_4, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor)
                .setBackgroundColor(R.id.item_view_3, backgroundColor)
                .setBackgroundColor(R.id.item_view_4, backgroundColor);
    }
}
