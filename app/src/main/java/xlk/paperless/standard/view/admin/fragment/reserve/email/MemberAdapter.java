package xlk.paperless.standard.view.admin.fragment.reserve.email;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/11/14.
 * @desc
 */
public class MemberAdapter extends BaseQuickAdapter<InterfaceMember.pbui_Item_MemberDetailInfo, BaseViewHolder> {
    List<Integer> checks = new ArrayList<>();

    public MemberAdapter(int layoutResId, @Nullable List<InterfaceMember.pbui_Item_MemberDetailInfo> data) {
        super(layoutResId, data);
    }

    public void setSelect(int id) {
        if (checks.contains(id)) {
            checks.remove(checks.indexOf(id));
        } else {
            checks.add(id);
        }
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedMember() {
        ArrayList<String> temps = new ArrayList<>();
        for (int i = 0; i < getData().size(); i++) {
            InterfaceMember.pbui_Item_MemberDetailInfo item = getData().get(i);
            String email = item.getEmail().toStringUtf8();
            if (checks.contains(item.getPersonid())) {
                if(!temps.contains(email)){
                    temps.add(email);
                }
            }
        }
        return temps;
    }

    public boolean isCheckAll() {
        return checks.size() == getData().size();
    }

    public void setCheckAll(boolean check) {
        checks.clear();
        if (check) {
            for (int i = 0; i < getData().size(); i++) {
                checks.add(getData().get(i).getPersonid());
            }
        }
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceMember.pbui_Item_MemberDetailInfo item) {
        helper.setText(R.id.item_view_1, item.getName().toStringUtf8())
                .setText(R.id.item_view_2, item.getEmail().toStringUtf8());
        boolean isSelected = checks.contains(item.getPersonid());
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor);
    }
}
