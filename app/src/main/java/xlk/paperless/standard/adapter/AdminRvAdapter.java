package xlk.paperless.standard.adapter;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.bean.AdminFunctionBean;

/**
 * @author Created by xlk on 2020/9/17.
 * @desc 后台管理界面功能列表
 */
public class AdminRvAdapter extends BaseQuickAdapter<AdminFunctionBean, BaseViewHolder> {
    private int selectedPosition = 0;

    public AdminRvAdapter(@Nullable List<AdminFunctionBean> data) {
        super(R.layout.item_admin_function, data);
    }

    public void setSelect(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder helper, AdminFunctionBean item) {
//        TextView item_tv = helper.getView(R.id.item_tv);
//        item_tv.setText(item.getName());
//        Drawable drawable = mContext.getDrawable(item.getDrawableResId());
//        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
//        item_tv.setCompoundDrawables(drawable, null, null, null);
//        item_tv.setCompoundDrawablePadding(5);
//        item_tv.setSelected(layoutPosition == selectedPosition);
        ImageView item_admin_iv = helper.getView(R.id.item_admin_iv);
        item_admin_iv.setImageResource(item.getDrawableResId());
        int layoutPosition = helper.getLayoutPosition();
        helper.itemView.setSelected(layoutPosition == selectedPosition);
    }
}
