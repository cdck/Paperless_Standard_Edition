package xlk.paperless.standard.adapter;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;

/**
 * @author xlk
 * @date 2020/3/17
 * @desc 会议资料界面-导出资料时弹框的adapter
 */
public class MeetDataExportAdapter extends BaseQuickAdapter<InterfaceFile.pbui_Item_MeetDirFileDetailInfo, BaseViewHolder> {
    private List<Long> ids = new ArrayList<>();

    public MeetDataExportAdapter(int layoutResId, @Nullable List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> data) {
        super(layoutResId, data);
    }

    public List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> getChoosedFile() {
        List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> files = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo info = mData.get(i);
            if (ids.contains(info.getMediaid())) {
                files.add(info);
            }
        }
        return files;
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceFile.pbui_Item_MeetDirFileDetailInfo item) {
        long mediaid = item.getMediaid();
        long id = mediaid & 0xffffffffL;//一定要在后面加大写的L(表示Long类型)
        helper.setText(R.id.i_m_d_export_number, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.i_m_d_export_name, item.getName().toStringUtf8())
                .setText(R.id.i_m_d_export_id, String.valueOf(id));
        boolean contains = ids.contains(mediaid);
        Resources resources = mContext.getResources();
        TextView i_m_d_export_number = helper.getView(R.id.i_m_d_export_number);
        TextView i_m_d_export_name = helper.getView(R.id.i_m_d_export_name);
        TextView i_m_d_export_id = helper.getView(R.id.i_m_d_export_id);
        int is_choose_bg = resources.getColor(R.color.table_tv_content);
        int no_choose_bg = resources.getColor(R.color.white);
        i_m_d_export_number.setBackgroundColor(contains ? is_choose_bg : no_choose_bg);
        i_m_d_export_name.setBackgroundColor(contains ? is_choose_bg : no_choose_bg);
        i_m_d_export_id.setBackgroundColor(contains ? is_choose_bg : no_choose_bg);
    }

    public void setChoose(long id) {
        if (ids.contains(id)) {
            ids.remove(ids.indexOf(id));
        } else {
            ids.add(id);
        }
        notifyDataSetChanged();
    }
}
