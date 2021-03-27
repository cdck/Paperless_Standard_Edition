package xlk.paperless.standard.view.admin.fragment.pre.election;

import android.widget.TextView;

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
public class AdminElectionAdapter extends BaseQuickAdapter<InterfaceVote.pbui_Item_MeetVoteDetailInfo, BaseViewHolder> {
    private int selectedId;

    public AdminElectionAdapter(int layoutResId, @Nullable List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> data) {
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
                .setText(R.id.item_view_3, Constant.getVoteType(getContext(), item.getType()))
                .setText(R.id.item_view_4, item.getMode() == 1 ? getContext().getString(R.string.yes) : getContext().getString(R.string.no))
                .setText(R.id.item_view_5, Constant.getVoteStateName(getContext(), item.getVotestate()));
        TextView item_tv_6 = helper.getView(R.id.item_view_6);
        TextView item_tv_7 = helper.getView(R.id.item_view_7);
        TextView item_tv_8 = helper.getView(R.id.item_view_8);
        TextView item_tv_9 = helper.getView(R.id.item_view_9);
        TextView item_tv_10 = helper.getView(R.id.item_view_10);
        item_tv_6.setText("");
        item_tv_7.setText("");
        item_tv_8.setText("");
        item_tv_9.setText("");
        item_tv_10.setText("");
        List<InterfaceVote.pbui_SubItem_VoteItemInfo> itemList = item.getItemList();
        for (int i = 0; i < itemList.size(); i++) {
            InterfaceVote.pbui_SubItem_VoteItemInfo info = itemList.get(i);
            String text = info.getText().toStringUtf8();
            if (i == 0) {
                item_tv_6.setText(text);
            }
            if (i == 1) {
                item_tv_7.setText(text);
            }
            if (i == 2) {
                item_tv_8.setText(text);
            }
            if (i == 3) {
                item_tv_9.setText(text);
            }
            if (i == 4) {
                item_tv_10.setText(text);
            }
        }
        boolean isSelected = selectedId == item.getVoteid();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor)
                .setTextColor(R.id.item_view_3, textColor)
                .setTextColor(R.id.item_view_4, textColor)
                .setTextColor(R.id.item_view_5, textColor)
                .setTextColor(R.id.item_view_6, textColor)
                .setTextColor(R.id.item_view_7, textColor)
                .setTextColor(R.id.item_view_8, textColor)
                .setTextColor(R.id.item_view_9, textColor)
                .setTextColor(R.id.item_view_10, textColor)
        ;

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor)
                .setBackgroundColor(R.id.item_view_3, backgroundColor)
                .setBackgroundColor(R.id.item_view_4, backgroundColor)
                .setBackgroundColor(R.id.item_view_5, backgroundColor)
                .setBackgroundColor(R.id.item_view_6, backgroundColor)
                .setBackgroundColor(R.id.item_view_7, backgroundColor)
                .setBackgroundColor(R.id.item_view_8, backgroundColor)
                .setBackgroundColor(R.id.item_view_9, backgroundColor)
                .setBackgroundColor(R.id.item_view_10, backgroundColor)
        ;
    }
}
