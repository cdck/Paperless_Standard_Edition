package xlk.paperless.standard.view.fragment.live;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2021/4/27.
 * @desc
 */
public class VideoFileAdapter extends BaseQuickAdapter<InterfaceFile.pbui_Item_MeetDirFileDetailInfo, BaseViewHolder> {
    private int selectedMediaId = -1;

    public VideoFileAdapter(@Nullable List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> data) {
        super(R.layout.item_video_file, data);
    }

    public void choose(int mediaid) {
        selectedMediaId = mediaid;
        notifyDataSetChanged();
    }

    public int getSelectedMediaId() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getMediaid() == selectedMediaId) {
                {
                    return selectedMediaId;
                }
            }
        }
        return -1;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, InterfaceFile.pbui_Item_MeetDirFileDetailInfo item) {
        holder.setText(R.id.mtv_name, item.getName().toStringUtf8());
        holder.getView(R.id.item_root_view).setBackgroundColor(selectedMediaId == item.getMediaid()
                ? getContext().getColor(R.color.vote_selected)
                : getContext().getColor(R.color.transparent));
    }
}
