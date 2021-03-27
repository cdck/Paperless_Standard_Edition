package xlk.paperless.standard.view.admin.fragment.pre.member;

import android.widget.CheckBox;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.util.ToastUtil;

/**
 * @author Created by xlk on 2020/10/19.
 * @desc 会前设置-参会人员中权限
 */
public class MemberPermissionAdapter extends BaseQuickAdapter<MemberPermissionBean, BaseViewHolder> {
    List<Integer> selectedIds = new ArrayList<>();

    public MemberPermissionAdapter(int layoutResId, @Nullable List<MemberPermissionBean> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(selectedIds.indexOf(id));
        } else {
            selectedIds.add(id);
        }
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder helper, MemberPermissionBean item) {
        int permission = item.getPermission();
        boolean hasScreenPermission = Constant.isHasPermission(permission, Constant.permission_code_screen);
        boolean hasProjectionPermission = Constant.isHasPermission(permission, Constant.permission_code_projection);
        boolean hasUploadPermission = Constant.isHasPermission(permission, Constant.permission_code_upload);
        boolean hasDownloadPermission = Constant.isHasPermission(permission, Constant.permission_code_download);
        boolean hasVotePermission = Constant.isHasPermission(permission, Constant.permission_code_vote);
        CheckBox checkBox = helper.getView(R.id.item_tv_1);
        checkBox.setChecked(selectedIds.contains(item.getMemberId()));
        helper.setText(R.id.item_tv_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_tv_2, item.getName())
                .setText(R.id.item_tv_3, hasScreenPermission ? "√" : "")
                .setText(R.id.item_tv_4, hasProjectionPermission ? "√" : "")
                .setText(R.id.item_tv_5, hasUploadPermission ? "√" : "")
                .setText(R.id.item_tv_6, hasDownloadPermission ? "√" : "")
                .setText(R.id.item_tv_7, hasVotePermission ? "√" : "");

        int textColor = getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_tv_1, textColor)
                .setTextColor(R.id.item_tv_2, textColor)
                .setTextColor(R.id.item_tv_3, textColor)
                .setTextColor(R.id.item_tv_4, textColor)
                .setTextColor(R.id.item_tv_5, textColor)
                .setTextColor(R.id.item_tv_6, textColor)
                .setTextColor(R.id.item_tv_7, textColor);

        int backgroundColor = getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_tv_1, backgroundColor)
                .setBackgroundColor(R.id.item_tv_2, backgroundColor)
                .setBackgroundColor(R.id.item_tv_3, backgroundColor)
                .setBackgroundColor(R.id.item_tv_4, backgroundColor)
                .setBackgroundColor(R.id.item_tv_5, backgroundColor)
                .setBackgroundColor(R.id.item_tv_6, backgroundColor)
                .setBackgroundColor(R.id.item_tv_7, backgroundColor);
    }

    public boolean isCheckAll() {
        return selectedIds.size() == getData().size();
    }

    public void setCheckAll(boolean checked) {
        selectedIds.clear();
        if (checked) {
            for (int i = 0; i < getData().size(); i++) {
                selectedIds.add(getData().get(i).getMemberId());
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 对选中参会人进行添加权限
     *
     * @param code See Constant.permission_code_screen
     */
    public void addPermission(int code) {
        if (selectedIds.isEmpty()) {
            ToastUtil.show(R.string.please_choose_member);
            return;
        }
        for (int i = 0; i < getData().size(); i++) {
            MemberPermissionBean bean = getData().get(i);
            if (selectedIds.contains(bean.getMemberId())) {
                bean.setPermission(bean.getPermission() | code);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 对选中参会人进行删除权限
     *
     * @param code See Constant.permission_code_screen
     */
    public void delPermission(int code) {
        if (selectedIds.isEmpty()) {
            ToastUtil.show(R.string.please_choose_member);
            return;
        }
        for (int i = 0; i < getData().size(); i++) {
            MemberPermissionBean bean = getData().get(i);
            if (selectedIds.contains(bean.getMemberId())) {
                int permission = bean.getPermission();
                if ((permission & code) == 0) {
                    //原来没有这个权限的就跳过，不然异或操作就会进行添加
                    break;
                }
                bean.setPermission(permission ^ code);
            }
        }
        notifyDataSetChanged();
    }
}
