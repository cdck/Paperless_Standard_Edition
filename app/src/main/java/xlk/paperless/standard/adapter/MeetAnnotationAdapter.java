package xlk.paperless.standard.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.bean.SeatMember;

/**
 * @author xlk
 * @date 2020/3/18
 * @Description: 批注查看参会人列表
 */
public class MeetAnnotationAdapter extends BaseQuickAdapter<SeatMember, BaseViewHolder> {
    int selectedDevId = -1;

    public MeetAnnotationAdapter(int layoutResId, @Nullable List<SeatMember> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SeatMember item) {
        helper.setText(R.id.i_m_c_m_name, item.getMemberDetailInfo().getName().toStringUtf8());
        boolean selected = selectedDevId == item.getSeatDetailInfo().getSeatid();
        helper.getView(R.id.i_m_c_m_name).setSelected(selected);
        helper.getView(R.id.i_m_c_m_ll).setSelected(selected);
        helper.getView(R.id.i_m_c_m_iv).setSelected(selected);
    }

    public int getSelectedDevId() {
        return selectedDevId;
    }

    public void notifySelect() {
        boolean have = false;
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getSeatDetailInfo().getSeatid() == selectedDevId) {
                have = true;
                break;
            }
        }
        if (!have) selectedDevId = -1;
        notifyDataSetChanged();
    }

    public void setSelect(int devid) {
        selectedDevId = devid;
        notifyDataSetChanged();
    }
}
