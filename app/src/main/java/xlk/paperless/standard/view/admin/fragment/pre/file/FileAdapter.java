package xlk.paperless.standard.view.admin.fragment.pre.file;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;
import xlk.paperless.standard.util.FileUtil;

/**
 * @author Created by xlk on 2020/10/21.
 * @desc
 */
public class FileAdapter extends BaseQuickAdapter<InterfaceFile.pbui_Item_MeetDirFileDetailInfo, BaseViewHolder> {
    List<Integer> checks = new ArrayList<>();

    public FileAdapter(int layoutResId, @Nullable List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> data) {
        super(layoutResId, data);
    }

    public void setCheck(int id) {
        if (checks.contains(id)) {
            checks.remove(checks.indexOf(id));
        } else {
            checks.add(id);
        }
        notifyDataSetChanged();
    }

    public List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> getChecks() {
        List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> temps = new ArrayList<>();
        for (int i = 0; i < getData().size(); i++) {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo item = getData().get(i);
            if (checks.contains(item.getMediaid())) {
                temps.add(item);
            }
        }
        return temps;
    }

    public InterfaceFile.pbui_Item_MeetDirFileDetailInfo getLastCheckFile() {
        if (checks.isEmpty()) {
            return null;
        } else {
            Integer id = checks.get(checks.size() - 1);
            for (int i = 0; i < getData().size(); i++) {
                if (getData().get(i).getMediaid() == id) {
                    return getData().get(i);
                }
            }
        }
        return null;
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceFile.pbui_Item_MeetDirFileDetailInfo item) {
        int mediaid = item.getMediaid();
        String fileName = item.getName().toStringUtf8();
        helper.setText(R.id.item_tv_1, String.valueOf(mediaid))
                .setText(R.id.item_tv_2, fileName)
                .setText(R.id.item_tv_3, FileUtil.formatFileSize(item.getSize()))
                .setText(R.id.item_tv_4, FileUtil.getFileType(getContext(), fileName))
                .setText(R.id.item_tv_5, item.getUploaderName().toStringUtf8());
        boolean isSelected = checks.contains(mediaid);
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_tv_1, textColor)
                .setTextColor(R.id.item_tv_2, textColor)
                .setTextColor(R.id.item_tv_3, textColor)
                .setTextColor(R.id.item_tv_4, textColor)
                .setTextColor(R.id.item_tv_5, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_tv_1, backgroundColor)
                .setBackgroundColor(R.id.item_tv_2, backgroundColor)
                .setBackgroundColor(R.id.item_tv_3, backgroundColor)
                .setBackgroundColor(R.id.item_tv_4, backgroundColor)
                .setBackgroundColor(R.id.item_tv_5, backgroundColor);
    }
}
