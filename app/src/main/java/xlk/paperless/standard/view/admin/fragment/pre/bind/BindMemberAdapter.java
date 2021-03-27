package xlk.paperless.standard.view.admin.fragment.pre.bind;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;
import xlk.paperless.standard.view.admin.fragment.pre.member.MemberRoleBean;

/**
 * @author Created by xlk on 2020/11/3.
 * @desc 座位绑定中参会人列表
 */
public class BindMemberAdapter extends BaseQuickAdapter<MemberRoleBean, BaseViewHolder> {
    private int selectedId;

    public BindMemberAdapter(int layoutResId, @Nullable List<MemberRoleBean> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public int getSelectedId() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getMember().getPersonid() == selectedId) {
                return selectedId;
            }
        }
        return -1;
    }

    @Override
    protected void convert(BaseViewHolder helper, MemberRoleBean item) {
        helper.setText(R.id.item_view_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_view_2, item.getMember().getName().toStringUtf8());
        boolean isSelected = selectedId == item.getMember().getPersonid();

        InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo seat = item.getSeat();
        boolean isBind = seat != null && seat.getMemberid() != 0;
        int textColor = isBind ? getContext().getColor(R.color.online) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor);
    }
}
