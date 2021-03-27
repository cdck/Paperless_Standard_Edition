package xlk.paperless.standard.view.admin.fragment.system.secretary;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceAdmin;

import java.util.List;

import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/9/21.
 * @desc 系统设置-秘书管理-管理员
 */
public class SecretaryUserAdapter extends BaseQuickAdapter<InterfaceAdmin.pbui_Item_AdminDetailInfo, BaseViewHolder> {
    private int selectedId;

    public SecretaryUserAdapter(int layoutResId, @Nullable List<InterfaceAdmin.pbui_Item_AdminDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceAdmin.pbui_Item_AdminDetailInfo item) {
        helper.setText(R.id.item_tv_username, item.getAdminname().toStringUtf8())
                .setText(R.id.item_tv_pwd, item.getPw().toStringUtf8())
                .setText(R.id.item_tv_remarks, item.getComment().toStringUtf8())
                .setText(R.id.item_tv_phone, item.getPhone().toStringUtf8())
                .setText(R.id.item_tv_email, item.getEmail().toStringUtf8());

        int textColor = (selectedId == item.getAdminid()) ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_tv_username, textColor)
                .setTextColor(R.id.item_tv_pwd, textColor)
                .setTextColor(R.id.item_tv_remarks, textColor)
                .setTextColor(R.id.item_tv_phone, textColor)
                .setTextColor(R.id.item_tv_email, textColor);

        int backgroundColor = (selectedId == item.getAdminid()) ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_tv_username, backgroundColor)
                .setBackgroundColor(R.id.item_tv_pwd, backgroundColor)
                .setBackgroundColor(R.id.item_tv_remarks, backgroundColor)
                .setBackgroundColor(R.id.item_tv_phone, backgroundColor)
                .setBackgroundColor(R.id.item_tv_email, backgroundColor);
    }

    public void setSelect(int adminid) {
        selectedId = adminid;
        notifyDataSetChanged();
    }
}
