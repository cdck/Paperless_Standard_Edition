package xlk.paperless.standard.view.admin.fragment.system.room;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceDevice;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;

/**
 * @author Created by xlk on 2020/9/19.
 * @desc
 */
public class AdminRoomDevAdapter extends BaseQuickAdapter<InterfaceDevice.pbui_Item_DeviceDetailInfo, BaseViewHolder> {
    private int selectedId;

    public AdminRoomDevAdapter(int layoutResId, @Nullable List<InterfaceDevice.pbui_Item_DeviceDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceDevice.pbui_Item_DeviceDetailInfo item) {
        helper.setText(R.id.item_table3_tv_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_table3_tv_2, item.getDevname().toStringUtf8())
                .setText(R.id.item_table3_tv_3, Constant.getDeviceTypeName(mContext, item.getDevcieid()));

        int textColor = (selectedId == item.getDevcieid()) ? mContext.getColor(R.color.white) : mContext.getColor(R.color.black);
        helper.setTextColor(R.id.item_table3_tv_1, textColor)
                .setTextColor(R.id.item_table3_tv_2, textColor)
                .setTextColor(R.id.item_table3_tv_3, textColor);

        int color = (selectedId == item.getDevcieid()) ? mContext.getColor(R.color.light_blue) : mContext.getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_table3_tv_1, color)
                .setBackgroundColor(R.id.item_table3_tv_2, color)
                .setBackgroundColor(R.id.item_table3_tv_3, color);
    }

    public void setSelected(int devid) {
        selectedId = devid;
        notifyDataSetChanged();
    }
}
