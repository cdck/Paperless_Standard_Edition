package xlk.paperless.standard.adapter;

import androidx.annotation.Nullable;

import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;

/**
 * @author xlk
 * @date 2020/4/2
 * @desc
 */
public class VoteManageAdapter extends BaseQuickAdapter<InterfaceVote.pbui_Item_MeetVoteDetailInfo, BaseViewHolder> {
    int selectedVoteId;

    public VoteManageAdapter(int layoutResId, @Nullable List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceVote.pbui_Item_MeetVoteDetailInfo item) {
        View view = helper.getView(R.id.item_vote_manage_root);
        view.setSelected(selectedVoteId == item.getVoteid());
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
        List<InterfaceVote.pbui_SubItem_VoteItemInfo> itemList = disposeItemList(item.getItemList());
        for (int i = 0; i < itemList.size(); i++) {
            InterfaceVote.pbui_SubItem_VoteItemInfo info = itemList.get(i);
            String text = info.getText().toStringUtf8().trim();
            int selcnt = info.getSelcnt();
            String string = text + "：" + selcnt + "票";
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
    //去除掉答案是空文本的选项
    private List<InterfaceVote.pbui_SubItem_VoteItemInfo> disposeItemList(List<InterfaceVote.pbui_SubItem_VoteItemInfo> infos) {
        List<InterfaceVote.pbui_SubItem_VoteItemInfo> items = new ArrayList<>();
        for (int i = 0; i < infos.size(); i++) {
            InterfaceVote.pbui_SubItem_VoteItemInfo item = infos.get(i);
            String trim = item.getText().toStringUtf8().trim();
            if (!trim.isEmpty()) {
                items.add(item);
            }
        }
        return items;
    }

    public InterfaceVote.pbui_Item_MeetVoteDetailInfo getSelectedVote() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getVoteid() == selectedVoteId) {
                return getData().get(i);
            }
        }
        return null;
    }

    public void setSelect(int id) {
        selectedVoteId = id;
        notifyDataSetChanged();
    }

    private String getState(int votestate) {
        if (votestate == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE) {
            return getContext().getString(R.string.state_not_initiated);
        } else if (votestate == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing_VALUE) {
            return getContext().getString(R.string.state_ongoing);
        } else {
            return getContext().getString(R.string.state_has_ended);
        }
    }

    private String getTitle(InterfaceVote.pbui_Item_MeetVoteDetailInfo item) {
        String voteTitle = item.getContent().toStringUtf8();
        voteTitle += "（";
        switch (item.getType()) {
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_SINGLE_VALUE://单选
                voteTitle += getContext().getString(R.string.type_single) + "，";
                break;
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_4_5_VALUE://5选4
                voteTitle += getContext().getString(R.string.type_4_5) + "，";
                break;
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_3_5_VALUE:
                voteTitle += getContext().getString(R.string.type_3_5) + "，";
                break;
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_5_VALUE:
                voteTitle += getContext().getString(R.string.type_2_5) + "，";
                break;
            case InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_3_VALUE:
                voteTitle += getContext().getString(R.string.type_2_3) + "，";
                break;
        }
        if (item.getMode() == InterfaceMacro.Pb_MeetVoteMode.Pb_VOTEMODE_agonymous_VALUE) {//匿名
            voteTitle += getContext().getString(R.string.mode_anonymous);
        } else {
            voteTitle += getContext().getString(R.string.mode_register);
        }
        voteTitle += "）";
        return voteTitle;
    }
}
