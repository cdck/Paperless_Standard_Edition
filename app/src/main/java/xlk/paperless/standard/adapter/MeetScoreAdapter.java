package xlk.paperless.standard.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFilescorevote;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.math.BigDecimal;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author xlk
 * @date 2020/3/20
 * @desc 评分查看 左边评分列表
 */
public class MeetScoreAdapter extends BaseQuickAdapter<InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore, BaseViewHolder> {
    private int chooseId = -1;

    public MeetScoreAdapter(int layoutResId, @Nullable List<InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore item) {
        double average;
        double total = getTotal(item);
        BigDecimal b1 = new BigDecimal(Double.toString(total));
        BigDecimal b2 = new BigDecimal(Double.toString(item.getSelectcount()));
        //默认保留两位会有错误，这里设置保留小数点后4位
        average = b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP).doubleValue();

        helper.setText(R.id.item_score_desc, item.getContent().toStringUtf8())
                .setText(R.id.item_score_file, JniHandler.getInstance().getFileName(item.getFileid()))
                .setText(R.id.item_score_state, getVoteState(item.getVotestate()))
                .setText(R.id.item_score_people, item.getShouldmembernum() + " | " + item.getRealmembernum())
                .setText(R.id.item_score_register, getMode(item.getMode()))
                .setText(R.id.item_score_1, getScore(item, 0))
                .setText(R.id.item_score_2, getScore(item, 1))
                .setText(R.id.item_score_3, getScore(item, 2))
                .setText(R.id.item_score_4, getScore(item, 3))
                .setText(R.id.item_score_total, String.valueOf(total))
                .setText(R.id.item_score_average, String.valueOf(average));
        boolean isSelected = item.getVoteid() == chooseId;
        LogUtil.d(TAG, "convert --> isSelected= " + isSelected + ", item.getVoteid()= " + item.getVoteid() + ", selectedId= " + chooseId);
        helper.getView(R.id.item_score_desc).setSelected(isSelected);
        helper.getView(R.id.item_score_file).setSelected(isSelected);
        helper.getView(R.id.item_score_state).setSelected(isSelected);
        helper.getView(R.id.item_score_people).setSelected(isSelected);
        helper.getView(R.id.item_score_register).setSelected(isSelected);
        helper.getView(R.id.item_score_1).setSelected(isSelected);
        helper.getView(R.id.item_score_2).setSelected(isSelected);
        helper.getView(R.id.item_score_3).setSelected(isSelected);
        helper.getView(R.id.item_score_4).setSelected(isSelected);
        helper.getView(R.id.item_score_total).setSelected(isSelected);
        helper.getView(R.id.item_score_average).setSelected(isSelected);
    }

    public void notifyChoose() {
        int id = -1;
        for (InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore item : mData) {
            if (item.getVoteid() == chooseId) {
                id = chooseId;
                break;
            }
        }
        chooseId = id;
        notifyDataSetChanged();
    }

    public InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore getChooseScore() {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getVoteid() == chooseId) {
                return mData.get(i);
            }
        }
        return null;
    }

    public int getChooseId() {
        return chooseId;
    }

    public void setChoose(int voteid) {
        chooseId = voteid;
        notifyDataSetChanged();
    }

    private double getTotal(InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore item) {
        double total = 0;
        List<Integer> itemsumscoreList = item.getItemsumscoreList();
        for (int i : itemsumscoreList) {
            total += i;
        }
        return total;
    }

    private String getScore(InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore item, int index) {
        int selectcount = item.getSelectcount();
        if (selectcount < (index + 1)) {//要获取的文本大于有效选项个数
            return "";
        }
        return item.getVoteText(index).toStringUtf8();
    }

    private String getMode(int mode) {
        if (mode == InterfaceMacro.Pb_MeetVoteMode.Pb_VOTEMODE_agonymous_VALUE) {
            return mContext.getString(R.string.no);
        } else {
            return mContext.getString(R.string.yes);
        }
    }

    private String getVoteState(int state) {
        if (state == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE) {
            return mContext.getString(R.string.state_not_initiated);
        } else if (state == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing_VALUE) {
            return mContext.getString(R.string.state_ongoing);
        } else {
            return mContext.getString(R.string.state_has_ended);
        }
    }
}
