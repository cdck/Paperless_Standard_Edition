package xlk.paperless.standard.view.admin.fragment.pre.member;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/10/19.
 * @desc 会前设置-参会人adapter
 */
public class AdminMemberAdapter extends BaseQuickAdapter<InterfaceMember.pbui_Item_MemberDetailInfo, BaseViewHolder> {
    /**
     * 是否多选
     */
    private boolean isMultiple;
    /**
     * 多选时选中的人员id
     */
    List<Integer> checks = new ArrayList<>();
    /**
     * 单选时选中的人员id
     */
    private int selectedId;

    public AdminMemberAdapter(int layoutResId, @Nullable List<InterfaceMember.pbui_Item_MemberDetailInfo> data, boolean isMultiple) {
        super(layoutResId, data);
        this.isMultiple = isMultiple;
    }


    public void setSelected(int id) {
        if (isMultiple) {
            if (checks.contains(id)) {
                checks.remove(checks.indexOf(id));
            } else {
                checks.add(id);
            }
        } else {
            selectedId = id;
        }
        notifyDataSetChanged();
    }


    public InterfaceMember.pbui_Item_MemberDetailInfo getSelectedMember() {
        for (int i = 0; i < getData().size(); i++) {
            if (selectedId == getData().get(i).getPersonid()) {
                return getData().get(i);
            }
        }
        return null;
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceMember.pbui_Item_MemberDetailInfo item) {
        helper.setText(R.id.item_tv_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_tv_2, item.getName().toStringUtf8())
                .setText(R.id.item_tv_3, item.getCompany().toStringUtf8())
                .setText(R.id.item_tv_4, item.getJob().toStringUtf8())
                .setText(R.id.item_tv_5, item.getComment().toStringUtf8())
                .setText(R.id.item_tv_6, item.getPhone().toStringUtf8())
                .setText(R.id.item_tv_7, item.getEmail().toStringUtf8())
                .setText(R.id.item_tv_8, item.getPassword().toStringUtf8());
        boolean isSelected = isMultiple ? checks.contains(item.getPersonid()) : selectedId == item.getPersonid();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_tv_1, textColor)
                .setTextColor(R.id.item_tv_2, textColor)
                .setTextColor(R.id.item_tv_3, textColor)
                .setTextColor(R.id.item_tv_4, textColor)
                .setTextColor(R.id.item_tv_5, textColor)
                .setTextColor(R.id.item_tv_6, textColor)
                .setTextColor(R.id.item_tv_7, textColor)
                .setTextColor(R.id.item_tv_8, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_tv_1, backgroundColor)
                .setBackgroundColor(R.id.item_tv_2, backgroundColor)
                .setBackgroundColor(R.id.item_tv_3, backgroundColor)
                .setBackgroundColor(R.id.item_tv_4, backgroundColor)
                .setBackgroundColor(R.id.item_tv_5, backgroundColor)
                .setBackgroundColor(R.id.item_tv_6, backgroundColor)
                .setBackgroundColor(R.id.item_tv_7, backgroundColor)
                .setBackgroundColor(R.id.item_tv_8, backgroundColor);
    }

}
