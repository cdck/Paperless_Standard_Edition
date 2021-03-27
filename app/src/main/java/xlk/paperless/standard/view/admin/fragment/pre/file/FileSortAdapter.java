package xlk.paperless.standard.view.admin.fragment.pre.file;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;
import xlk.paperless.standard.util.FileUtil;

/**
 * @author Created by xlk on 2020/10/21.
 * @desc 文件排序adapter
 */
public class FileSortAdapter extends BaseQuickAdapter<InterfaceFile.pbui_Item_MeetDirFileDetailInfo, BaseViewHolder> {
    int selectedId;

    public FileSortAdapter(int layoutResId, @Nullable List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> data) {
        super(layoutResId, data);
    }

    public void setSelectedId(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public InterfaceFile.pbui_Item_MeetDirFileDetailInfo getSelected() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getMediaid() == selectedId) {
                return getData().get(i);
            }
        }
        return null;
    }

    public int getSelectedIndex() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getMediaid() == selectedId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceFile.pbui_Item_MeetDirFileDetailInfo item) {
        helper.setText(R.id.item_view_1, String.valueOf(item.getMediaid()))
                .setText(R.id.item_view_2, item.getName().toStringUtf8())
                .setText(R.id.item_view_3, FileUtil.formatFileSize(item.getSize()))
                .setText(R.id.item_view_4, FileUtil.getFileType(getContext(), item.getName().toStringUtf8()))
                .setText(R.id.item_view_5, item.getUploaderName().toStringUtf8());
        boolean isSelected = selectedId == item.getMediaid();
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor)
                .setTextColor(R.id.item_view_3, textColor)
                .setTextColor(R.id.item_view_4, textColor)
                .setTextColor(R.id.item_view_5, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor)
                .setBackgroundColor(R.id.item_view_3, backgroundColor)
                .setBackgroundColor(R.id.item_view_4, backgroundColor)
                .setBackgroundColor(R.id.item_view_5, backgroundColor);
    }
}
