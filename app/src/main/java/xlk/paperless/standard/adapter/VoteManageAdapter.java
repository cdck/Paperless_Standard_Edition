package xlk.paperless.standard.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.protobuf.ByteString;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.List;

import xlk.paperless.standard.R;

/**
 * @author xlk
 * @date 2020/4/2
 * @Description:
 */
public class VoteManageAdapter extends BaseQuickAdapter<InterfaceVote.pbui_Item_MeetVoteDetailInfo, BaseViewHolder> {
    InterfaceVote.pbui_Item_MeetVoteDetailInfo selectedVote = null;

    public VoteManageAdapter(int layoutResId, @Nullable List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceVote.pbui_Item_MeetVoteDetailInfo item) {
        View view = helper.getView(R.id.item_vote_manage_root);
        view.setSelected(selectedVote.getVoteid() == item.getVoteid());
        TextView option1 = helper.getView(R.id.item_vote_manage_answer1);
        TextView option2 = helper.getView(R.id.item_vote_manage_answer2);
        TextView option3 = helper.getView(R.id.item_vote_manage_answer3);
        TextView option4 = helper.getView(R.id.item_vote_manage_answer4);
        TextView option5 = helper.getView(R.id.item_vote_manage_answer5);
        option1.setVisibility(View.GONE);
        option2.setVisibility(View.GONE);
        option3.setVisibility(View.GONE);
        option4.setVisibility(View.GONE);
        option5.setVisibility(View.GONE);

        List<InterfaceVote.pbui_SubItem_VoteItemInfo> itemList = item.getItemList();
        for (int i = 0; i < itemList.size(); i++) {
            InterfaceVote.pbui_SubItem_VoteItemInfo info = itemList.get(i);
            String text = info.getText().toStringUtf8();
            String string = text + "：" + String.valueOf(info.getSelcnt() + "票");
            if (i == 0) {
                option1.setText(string);
                option1.setVisibility(View.VISIBLE);
            } else if (i == 1) {
                option2.setText(string);
                option2.setVisibility(View.VISIBLE);
            } else if (i == 2) {
                option3.setText(string);
                option3.setVisibility(View.VISIBLE);
            } else if (i == 3) {
                option4.setText(string);
                option4.setVisibility(View.VISIBLE);
            } else if (i == 4) {
                option5.setText(string);
                option5.setVisibility(View.VISIBLE);
            }
        }
        helper.setText(R.id.item_vote_manage_number, String.valueOf(item.getVoteid()))
                .setText(R.id.item_vote_manage_title, getTitle(item))
                .setText(R.id.item_vote_manage_state, getState(item.getVotestate()));
    }

    public void notitySelect() {
        InterfaceVote.pbui_Item_MeetVoteDetailInfo temp = null;
        for (int i = 0; i < mData.size(); i++) {
            if (selectedVote.getVoteid() == mData.get(i).getVoteid()) {
                temp = mData.get(i);
                break;
            }
        }
        selectedVote = temp;
        notifyDataSetChanged();
    }

    public InterfaceVote.pbui_Item_MeetVoteDetailInfo getSelectedVote() {
        return selectedVote;
    }

    public void setSelect(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote) {
        selectedVote = vote;
        notifyDataSetChanged();
    }

    private String getState(int votestate) {
        if (votestate == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE) {
            return mContext.getString(R.string.state_not_initiated);
        } else if (votestate == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing_VALUE) {
            return mContext.getString(R.string.state_ongoing);
        } else {
            return mContext.getString(R.string.state_has_ended);
        }
    }

    private String getTitle(InterfaceVote.pbui_Item_MeetVoteDetailInfo item) {
        String voteTitle = item.getContent().toStringUtf8();
        voteTitle += "（";
        switch (item.getType()) {
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_SINGLE_VALUE://单选
                voteTitle += mContext.getString(R.string.type_single) + "，";
                break;
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_4_5_VALUE://5选4
                voteTitle += mContext.getString(R.string.type_4_5) + "，";
                break;
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_3_5_VALUE:
                voteTitle += mContext.getString(R.string.type_3_5) + "，";
                break;
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_5_VALUE:
                voteTitle += mContext.getString(R.string.type_2_5) + "，";
                break;
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_3_VALUE:
                voteTitle += mContext.getString(R.string.type_2_3) + "，";
                break;
        }
        if (item.getMode() == InterfaceMacro.Pb_MeetVoteMode.Pb_VOTEMODE_agonymous_VALUE) {//匿名
            voteTitle += mContext.getString(R.string.mode_anonymous);
        } else {
            voteTitle += mContext.getString(R.string.mode_register);
        }
        voteTitle += "）";
        return voteTitle;
    }
}
