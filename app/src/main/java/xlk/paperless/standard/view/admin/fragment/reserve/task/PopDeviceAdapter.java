package xlk.paperless.standard.view.admin.fragment.reserve.task;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceDevice;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2020/11/17.
 * @desc
 */
public class PopDeviceAdapter extends BaseQuickAdapter<InterfaceDevice.pbui_Item_DeviceDetailInfo, BaseViewHolder> {
    List<Integer> checks = new ArrayList<>();
    public PopDeviceAdapter(int layoutResId, @Nullable List<InterfaceDevice.pbui_Item_DeviceDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceDevice.pbui_Item_DeviceDetailInfo item) {
        helper.setText(R.id.item_view_1,item.getDevname().toStringUtf8())
                .setText(R.id.item_view_2, String.valueOf(item.getDevcieid()));
        boolean isSelected = checks.contains(item.getDevcieid());
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor);
    }


    public void setSelect(int id) {
        if (checks.contains(id)) {
            checks.remove(checks.indexOf(id));
        } else {
            checks.add(id);
        }
        notifyDataSetChanged();
    }

    public List<InterfaceDevice.pbui_Item_DeviceDetailInfo> getSelect() {
        List<InterfaceDevice.pbui_Item_DeviceDetailInfo> temps = new ArrayList<>();
        for (int i = 0; i < getData().size(); i++) {
            InterfaceDevice.pbui_Item_DeviceDetailInfo item = getData().get(i);
            if (checks.contains(item.getDevcieid())) {
                temps.add(item);
            }
        }
        return temps;
    }
}
