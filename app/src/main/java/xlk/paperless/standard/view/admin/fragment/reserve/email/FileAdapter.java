package xlk.paperless.standard.view.admin.fragment.reserve.email;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.io.File;
import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/11/16.
 * @desc
 */
public class FileAdapter extends BaseQuickAdapter<File, BaseViewHolder> {
    private int index = -1;

    public FileAdapter(int layoutResId, @Nullable List<File> data) {
        super(layoutResId, data);
    }

    public void setSelect(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    public int getSelect() {
        return index > getData().size() - 1 ? -1 : index;
    }

    @Override
    protected void convert(BaseViewHolder helper, File item) {
        helper.setText(R.id.item_view_1, item.getAbsolutePath());
        boolean isSelected = index == helper.getLayoutPosition();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.black);
        helper.setTextColor(R.id.item_view_1, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor);
    }
}
