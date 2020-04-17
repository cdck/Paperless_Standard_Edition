package xlk.paperless.standard.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.util.List;

import xlk.paperless.standard.R;

/**
 * @author xlk
 * @date 2020/3/12
 * @Description: 主页选择绑定参会人adapter
 */
public class MainBindMemberAdapter extends BaseQuickAdapter<InterfaceMember.pbui_Item_MemberDetailInfo, BaseViewHolder> {
    private int chooseId = -1;

    public MainBindMemberAdapter(int layoutResId, @Nullable List<InterfaceMember.pbui_Item_MemberDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceMember.pbui_Item_MemberDetailInfo item) {
        helper.setText(R.id.item_bind_member_btn, item.getName().toStringUtf8());
        Button btn = helper.getView(R.id.item_bind_member_btn);
        boolean b = item.getPersonid() == chooseId;

        btn.setSelected(b);
        btn.setTextColor(b ? mContext.getResources().getColor(R.color.btn_choosed_tv) : mContext.getResources().getColor(R.color.btn_normal_tv));
    }

    public void notifyChoose() {
        boolean has = false;
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getPersonid() == chooseId) {
                has = true;
                break;
            }
        }
        if (!has) {
            chooseId = -1;
        }
        notifyDataSetChanged();
    }

    public int getChooseId() {
        return chooseId;
    }

    public void setChoose(int memberId) {
        this.chooseId = memberId;
        notifyDataSetChanged();
    }
}
