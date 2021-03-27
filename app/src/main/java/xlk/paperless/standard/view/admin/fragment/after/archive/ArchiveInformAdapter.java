package xlk.paperless.standard.view.admin.fragment.after.archive;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/10/28.
 * @desc
 */
public class ArchiveInformAdapter extends BaseQuickAdapter<ArchiveInform, BaseViewHolder> {
    public ArchiveInformAdapter(int layoutResId, @Nullable List<ArchiveInform> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ArchiveInform item) {
        helper.setText(R.id.item_view_1, item.getContent())
                .setText(R.id.item_view_2, item.getResult());

        int textColor = getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor);

        int backgroundColor = getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor);
    }
}
