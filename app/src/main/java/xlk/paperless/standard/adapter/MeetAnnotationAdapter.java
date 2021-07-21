package xlk.paperless.standard.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.bean.SeatMember;

/**
 * @author xlk
 * @date 2020/3/18
 * @desc 批注查看参会人列表
 */
public class MeetAnnotationAdapter extends BaseQuickAdapter<SeatMember, BaseViewHolder> {
    int selectedDevId = -1;
    private int selectedMemberId=-1;

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
        for (int i = 0; i < getData().size(); i++) {
            if(getData().get(i).getSeatDetailInfo().getSeatid()==selectedDevId){
                return selectedDevId;
            }
        }
        return -1;
    }
    public int getSelectedMemberId() {
        for (int i = 0; i < getData().size(); i++) {
            if(getData().get(i).getSeatDetailInfo().getNameId()==selectedMemberId){
                return selectedMemberId;
            }
        }
        return -1;
    }


    public void notifySelect() {
        boolean have = false;
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getSeatDetailInfo().getSeatid() == selectedDevId) {
                have = true;
                break;
            }
        }
        if (!have) selectedDevId = -1;
        notifyDataSetChanged();
    }

    public void setSelect(int devid,int memberId) {
        selectedDevId = devid;
        selectedMemberId=memberId;
        notifyDataSetChanged();
    }
}
