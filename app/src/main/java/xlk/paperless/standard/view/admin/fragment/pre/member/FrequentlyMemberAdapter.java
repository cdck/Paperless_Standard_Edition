package xlk.paperless.standard.view.admin.fragment.pre.member;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfacePerson;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/10/20.
 * @desc
 */
public class FrequentlyMemberAdapter extends BaseQuickAdapter<InterfacePerson.pbui_Item_PersonDetailInfo, BaseViewHolder> {
    List<Integer> checks = new ArrayList<>();

    public FrequentlyMemberAdapter(int layoutResId, @Nullable List<InterfacePerson.pbui_Item_PersonDetailInfo> data) {
        super(layoutResId, data);
    }

    public void setCheck(int id) {
        if (checks.contains(id)) {
            checks.remove(checks.indexOf(id));
        } else {
            checks.add(id);
        }
        notifyDataSetChanged();
    }

    /**
     * 将选中的常用参会人创建成参会人结构体
     */
    public List<InterfaceMember.pbui_Item_MemberDetailInfo> getCheckedMembers() {
        List<InterfaceMember.pbui_Item_MemberDetailInfo> temps = new ArrayList<>();
        for (int i = 0; i < getData().size(); i++) {
            InterfacePerson.pbui_Item_PersonDetailInfo item = getData().get(i);
            if (checks.contains(item.getPersonid())) {
                InterfaceMember.pbui_Item_MemberDetailInfo build = InterfaceMember.pbui_Item_MemberDetailInfo.newBuilder()
                        .setName(item.getName())
                        .setCompany(item.getCompany())
                        .setJob(item.getJob())
                        .setComment(item.getComment())
                        .setPhone(item.getPhone())
                        .setEmail(item.getEmail())
                        .setPassword(item.getPassword())
                        .build();
                temps.add(build);
            }
        }
        return temps;
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfacePerson.pbui_Item_PersonDetailInfo item) {
        helper.setText(R.id.item_tv_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_tv_2, item.getName().toStringUtf8())
                .setText(R.id.item_tv_3, item.getCompany().toStringUtf8())
                .setText(R.id.item_tv_4, item.getJob().toStringUtf8())
                .setText(R.id.item_tv_5, item.getComment().toStringUtf8())
                .setText(R.id.item_tv_6, item.getPhone().toStringUtf8())
                .setText(R.id.item_tv_7, item.getEmail().toStringUtf8())
                .setText(R.id.item_tv_8, item.getPassword().toStringUtf8());
        boolean isSelected = checks.contains(item.getPersonid());
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
