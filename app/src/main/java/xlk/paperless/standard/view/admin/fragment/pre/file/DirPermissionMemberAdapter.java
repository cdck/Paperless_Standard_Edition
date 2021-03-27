package xlk.paperless.standard.view.admin.fragment.pre.file;

import android.widget.CheckBox;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/10/21.
 * @desc
 */
public class DirPermissionMemberAdapter extends BaseQuickAdapter<MemberDirPermissionBean, BaseViewHolder> {
    List<Integer> checks = new ArrayList<>();

    public DirPermissionMemberAdapter(int layoutResId, @Nullable List<MemberDirPermissionBean> data) {
        super(layoutResId, data);
    }

    public void setCheck(int id) {
        for (int i = 0; i < getData().size(); i++) {
            MemberDirPermissionBean item = getData().get(i);
            if (item.getMember().getPersonid() == id) {
                item.setBlacklist(!item.isBlacklist());
                break;
            }
        }
        notifyDataSetChanged();
    }

    public List<Integer> getChecks() {
        checks.clear();
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).isBlacklist()) {
                //添加是黑名单的
                checks.add(getData().get(i).getMember().getPersonid());
            }
        }
        return checks;
    }

    public void setCheckAll(boolean all) {
        for (int i = 0; i < getData().size(); i++) {
            MemberDirPermissionBean item = getData().get(i);
            item.setBlacklist(!all);
        }
        notifyDataSetChanged();
    }

    public boolean isCheckAll() {
        int count = 0;
        for (int i = 0; i < getData().size(); i++) {
            MemberDirPermissionBean item = getData().get(i);
            if (!item.isBlacklist()) {
                count++;
            }
        }
        return count != 0 && count == getData().size();
    }

    @Override
    protected void convert(BaseViewHolder helper, MemberDirPermissionBean bean) {
        InterfaceMember.pbui_Item_MemberDetailInfo item = bean.getMember();
        CheckBox cb = helper.getView(R.id.item_view_1);
        cb.setChecked(!bean.isBlacklist());
        helper.setText(R.id.item_view_1, getContext().getString(R.string.accessible))
                .setText(R.id.item_view_2, item.getName().toStringUtf8())
                .setText(R.id.item_view_3, item.getCompany().toStringUtf8())
                .setText(R.id.item_view_4, item.getJob().toStringUtf8());
        int textColor = getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor)
                .setTextColor(R.id.item_view_3, textColor)
                .setTextColor(R.id.item_view_4, textColor);
        int backgroundColor = getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor)
                .setBackgroundColor(R.id.item_view_3, backgroundColor)
                .setBackgroundColor(R.id.item_view_4, backgroundColor);
    }

}
