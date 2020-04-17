package xlk.paperless.standard.adapter;

import android.support.annotation.Nullable;
import android.widget.CheckBox;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.util.ToastUtil;

/**
 * @author xlk
 * @date 2020/4/3
 * @Description: 投票管理点击投票选择加入投票的参会人
 */
public class VoteManageMemberAdapter extends BaseQuickAdapter<InterfaceMember.pbui_Item_MeetMemberDetailInfo, BaseViewHolder> {
    List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> canChooseInfos = new ArrayList<>();
    List<Integer> ids = new ArrayList<>();

    public VoteManageMemberAdapter(int layoutResId, @Nullable List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceMember.pbui_Item_MeetMemberDetailInfo item) {
        boolean ishas = Constant.getChoose(item.getPermission()).contains(5);
        boolean isonline = item.getMemberdetailflag() == InterfaceMember.Pb_MemberDetailFlag.Pb_MEMBERDETAIL_FLAG_ONLINE_VALUE;
        boolean isCan = false;
        int facestatus = item.getFacestatus();
        String state;
        if (item.getDevid() == 0) {
            state = mContext.getString(R.string.not_bind_dev);
        } else {
            if (isonline) {
                state = mContext.getString(R.string.online);
                if (facestatus != InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MemFace_VALUE) {
                    state += " / " + mContext.getString(R.string.not_on_meet);
                }
                if (ishas) {//有权限
                    isCan = true;
                }
            } else {
                state = mContext.getString(R.string.offline);
            }
        }
        helper.setText(R.id.item_vote_member_number, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_vote_member_name, item.getMembername().toStringUtf8())
                .setText(R.id.item_vote_member_seat, item.getDevname().toStringUtf8())
                .setText(R.id.item_vote_member_state, state)
                .setText(R.id.item_vote_member_permissions, ishas ? "√" : mContext.getResources().getString(R.string.no_permission));
        int textColor = isCan ? mContext.getResources().getColor(R.color.text_blue) : mContext.getResources().getColor(R.color.text_color_black);
        helper.setTextColor(R.id.item_vote_member_number, textColor)
                .setTextColor(R.id.item_vote_member_name, textColor)
                .setTextColor(R.id.item_vote_member_seat, textColor)
                .setTextColor(R.id.item_vote_member_state, textColor)
                .setTextColor(R.id.item_vote_member_permissions, textColor);
        CheckBox cb = helper.getView(R.id.item_vote_member_number);
        cb.setChecked(ids.contains(item.getMemberid()));
    }

    public void notifyChoose() {
        List<Integer> temps = new ArrayList<>();
        canChooseInfos.clear();
        for (int i = 0; i < mData.size(); i++) {
            InterfaceMember.pbui_Item_MeetMemberDetailInfo info = mData.get(i);
            if (isCanChoose(info)) {
                canChooseInfos.add(info);
                if (ids.contains(info.getMemberid())) {
                    temps.add(info.getMemberid());
                }
            }
        }
        ids = temps;
        notifyDataSetChanged();
    }

    private boolean isCanChoose(InterfaceMember.pbui_Item_MeetMemberDetailInfo info) {
        boolean online = info.getMemberdetailflag() == InterfaceMember.Pb_MemberDetailFlag.Pb_MEMBERDETAIL_FLAG_ONLINE_VALUE;
        return info.getDevid() != 0 && online && Constant.getChoose(info.getPermission()).contains(5);
    }

    public List<Integer> getChoose() {
        return ids;
    }

    public void setChoose(int memberId) {
        if (ids.contains(memberId)) {
            ids.remove(ids.indexOf(memberId));
        } else {
            boolean isCan = false;
            for (int i = 0; i < mData.size(); i++) {
                InterfaceMember.pbui_Item_MeetMemberDetailInfo info = mData.get(i);
                if (memberId == info.getMemberid()) {
                    isCan = isCanChoose(info);
                    break;
                }
            }
            if (isCan) {
                ids.add(memberId);
            } else {
                ToastUtil.show(mContext, R.string.can_not_vote);
            }
        }
        notifyDataSetChanged();
    }

    public boolean isChooseAll() {
        notifyChoose();
        return ids.size() == canChooseInfos.size();
    }

    public void setChooseAll(boolean all) {
        notifyChoose();
        ids.clear();
        if (all) {
            for (int i = 0; i < canChooseInfos.size(); i++) {
                ids.add(canChooseInfos.get(i).getMemberid());
            }
        }
        notifyChoose();
    }
}
