package xlk.paperless.standard.view.admin.fragment.system.member;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfacePerson;

import java.util.List;

import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/9/22.
 * @desc
 */
public class FrequentlyMemberAdapter extends BaseQuickAdapter<InterfacePerson.pbui_Item_PersonDetailInfo, BaseViewHolder> {
    private int selectId;

    public FrequentlyMemberAdapter(int layoutResId, @Nullable List<InterfacePerson.pbui_Item_PersonDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfacePerson.pbui_Item_PersonDetailInfo item) {
        helper.setText(R.id.item_tv_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_tv_2, item.getName().toStringUtf8())
                .setText(R.id.item_tv_3, item.getCompany().toStringUtf8())
                .setText(R.id.item_tv_4, item.getJob().toStringUtf8())
                .setText(R.id.item_tv_5, item.getComment().toStringUtf8())
                .setText(R.id.item_tv_6, item.getPhone().toStringUtf8())
                .setText(R.id.item_tv_7, item.getEmail().toStringUtf8());
        int textColor = (selectId == item.getPersonid()) ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_tv_1, textColor)
                .setTextColor(R.id.item_tv_2, textColor)
                .setTextColor(R.id.item_tv_3, textColor)
                .setTextColor(R.id.item_tv_4, textColor)
                .setTextColor(R.id.item_tv_5, textColor)
                .setTextColor(R.id.item_tv_6, textColor)
                .setTextColor(R.id.item_tv_7, textColor);

        int backgroundColor = (selectId == item.getPersonid()) ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_tv_1, backgroundColor)
                .setBackgroundColor(R.id.item_tv_2, backgroundColor)
                .setBackgroundColor(R.id.item_tv_3, backgroundColor)
                .setBackgroundColor(R.id.item_tv_4, backgroundColor)
                .setBackgroundColor(R.id.item_tv_5, backgroundColor)
                .setBackgroundColor(R.id.item_tv_6, backgroundColor)
                .setBackgroundColor(R.id.item_tv_7, backgroundColor);
    }

    public void setSelect(int personid) {
        selectId = personid;
        notifyDataSetChanged();
    }
}
