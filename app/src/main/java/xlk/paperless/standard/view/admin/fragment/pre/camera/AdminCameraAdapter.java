package xlk.paperless.standard.view.admin.fragment.pre.camera;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import com.mogujie.tt.protobuf.InterfaceVideo;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/10/23.
 * @desc
 */
public class AdminCameraAdapter extends BaseQuickAdapter<DevCameraBean, BaseViewHolder> {
    private int selectedId;

    public AdminCameraAdapter(int layoutResId, @Nullable List<DevCameraBean> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public InterfaceVideo.pbui_Item_MeetVideoDetailInfo getSelected() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getCamera().getId() == selectedId) {
                return getData().get(i).getCamera();
            }
        }
        return null;
    }

    @Override
    protected void convert(BaseViewHolder helper, DevCameraBean bean) {
        InterfaceVideo.pbui_Item_MeetVideoDetailInfo item = bean.getCamera();
        helper.setText(R.id.item_view_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_view_2, item.getName().toStringUtf8())
                .setText(R.id.item_view_3, item.getDevicename().toStringUtf8())
                .setText(R.id.item_view_4, String.valueOf(item.getSubid()))
                .setText(R.id.item_view_5, item.getAddr().toStringUtf8());
        boolean isSelected = selectedId == item.getId();
        boolean isOnline = bean.isOnline();
        int textColor = isOnline ? getContext().getColor(R.color.online) : getContext().getColor(R.color.light_black);
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
