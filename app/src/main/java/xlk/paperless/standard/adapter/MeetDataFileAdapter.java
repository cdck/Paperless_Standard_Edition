package xlk.paperless.standard.adapter;

import android.view.View;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.view.App;

/**
 * @author xlk
 * @date 2020/3/14
 * @desc 会议资料中的文件adapter
 */
public class MeetDataFileAdapter extends BaseQuickAdapter<InterfaceFile.pbui_Item_MeetDirFileDetailInfo, BaseViewHolder> {

    private int chooseId = -1;

    public MeetDataFileAdapter(int layoutResId, @Nullable List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceFile.pbui_Item_MeetDirFileDetailInfo item) {
        helper.setText(R.id.i_m_d_file_number, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.i_m_d_file_name, item.getName().toStringUtf8())
                .setText(R.id.i_m_d_file_size, FileUtil.formatFileSize(item.getSize()));
        helper.getView(R.id.i_m_d_file_view).setVisibility(App.isStandard ? View.VISIBLE : View.GONE);
        helper.getView(R.id.i_m_d_file_root).setSelected(chooseId == item.getMediaid());
    }

    public int getChooseId() {
        return chooseId;
    }

    public void setChoose(int mediaId) {
        chooseId = mediaId;
        notifyDataSetChanged();
    }
}
