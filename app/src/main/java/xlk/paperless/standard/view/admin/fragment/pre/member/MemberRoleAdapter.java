package xlk.paperless.standard.view.admin.fragment.pre.member;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;

/**
 * @author Created by xlk on 2020/10/20.
 * @desc
 */
public class MemberRoleAdapter extends BaseQuickAdapter<MemberRoleBean, BaseViewHolder> {
    private int selectedId = -1;

    public MemberRoleAdapter(int layoutResId, @Nullable List<MemberRoleBean> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public MemberRoleBean getSelected() {
        for (int i = 0; i < getData().size(); i++) {
            MemberRoleBean item = getData().get(i);
            if (item.getMember().getPersonid() == selectedId) {
                return item;
            }
        }
        return null;
    }

    @Override
    protected void convert(BaseViewHolder helper, MemberRoleBean item) {
        InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo seat = item.getSeat();
        helper.setText(R.id.item_tv_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_tv_2, item.getMember().getName().toStringUtf8())
                .setText(R.id.item_tv_3, seat != null ? seat.getDevname().toStringUtf8() : "")
                .setText(R.id.item_tv_4, Constant.getMemberRoleName(getContext(), seat != null ? seat.getRole() : 0));

        boolean isSelected = selectedId == item.getMember().getPersonid();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_tv_1, textColor)
                .setTextColor(R.id.item_tv_2, textColor)
                .setTextColor(R.id.item_tv_3, textColor)
                .setTextColor(R.id.item_tv_4, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_tv_1, backgroundColor)
                .setBackgroundColor(R.id.item_tv_2, backgroundColor)
                .setBackgroundColor(R.id.item_tv_3, backgroundColor)
                .setBackgroundColor(R.id.item_tv_4, backgroundColor);
    }

}
