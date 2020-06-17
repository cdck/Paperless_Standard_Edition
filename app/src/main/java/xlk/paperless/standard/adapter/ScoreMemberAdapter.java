package xlk.paperless.standard.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceFilescorevote;

import java.math.BigDecimal;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.bean.ScoreMember;

/**
 * @author xlk
 * @date 2020/4/9
 * @desc
 */
public class ScoreMemberAdapter extends BaseQuickAdapter<ScoreMember, BaseViewHolder> {
    int chooseid = -1;

    public ScoreMemberAdapter(int layoutResId, @Nullable List<ScoreMember> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ScoreMember item) {
        TextView item_score_option1 = helper.getView(R.id.item_score_option1);
        TextView item_score_option2 = helper.getView(R.id.item_score_option2);
        TextView item_score_option3 = helper.getView(R.id.item_score_option3);
        TextView item_score_option4 = helper.getView(R.id.item_score_option4);
        item_score_option1.setText("");
        item_score_option2.setText("");
        item_score_option3.setText("");
        item_score_option4.setText("");
        InterfaceFilescorevote.pbui_Type_Item_FileScoreMemberStatistic score = item.getScore();
        List<Integer> scoreList = score.getScoreList();
        int scoreCount = score.getScoreCount();
        double total = 0;
        double average;
        for (int i = 0; i < scoreList.size(); i++) {
            Integer obj = scoreList.get(i);
            total += obj;
            if (i == 0) item_score_option1.setText(String.valueOf(obj));
            if (i == 1) item_score_option2.setText(String.valueOf(obj));
            if (i == 2) item_score_option3.setText(String.valueOf(obj));
            if (i == 3) item_score_option4.setText(String.valueOf(obj));
        }
        BigDecimal b1 = new BigDecimal(Double.toString(total));
        BigDecimal b2 = new BigDecimal(Double.toString(scoreCount));
        //默认保留两位会有错误，这里设置保留小数点后4位
        average = b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP).doubleValue();

        helper.setText(R.id.item_score_member, item.getMember().getName().toStringUtf8())
                .setText(R.id.item_score_total, String.valueOf(total))
                .setText(R.id.item_score_average, String.valueOf(average));
        boolean selected = item.getMember().getPersonid() == chooseid;
        int color = selected ? mContext.getResources().getColor(R.color.table_selected) : mContext.getResources().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_score_member, color)
                .setBackgroundColor(R.id.item_score_option1, color)
                .setBackgroundColor(R.id.item_score_option2, color)
                .setBackgroundColor(R.id.item_score_option3, color)
                .setBackgroundColor(R.id.item_score_option4, color)
                .setBackgroundColor(R.id.item_score_total, color)
                .setBackgroundColor(R.id.item_score_average, color);
    }

    public void notifyChoose() {
        int temp = -1;
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getMember().getPersonid() == chooseid) {
                temp = chooseid;
            }
        }
        chooseid = temp;
        notifyDataSetChanged();
    }

    public String getOpinion() {
        for (int i = 0; i < mData.size(); i++) {
            ScoreMember scoreMember = mData.get(i);
            if (scoreMember.getMember().getPersonid() == chooseid) {
                return scoreMember.getScore().getContent().toStringUtf8();
            }
        }
        return "";
    }

    public void choose(int memberid) {
        chooseid = memberid;
        notifyDataSetChanged();
    }
}
