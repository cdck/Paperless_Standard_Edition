package xlk.paperless.standard.view.admin.fragment.system.device;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceDevice;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;

/**
 * @author Created by xlk on 2020/11/11.
 * @desc
 */
public class ClientDeviceAdapter extends BaseQuickAdapter<InterfaceDevice.pbui_Item_DeviceDetailInfo, BaseViewHolder> {
    List<Integer> checkedIds = new ArrayList<>();

    public ClientDeviceAdapter(int layoutResId, @Nullable List<InterfaceDevice.pbui_Item_DeviceDetailInfo> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        if (checkedIds.contains(id)) {
            checkedIds.remove(checkedIds.indexOf(id));
        } else {
            checkedIds.add(id);
        }
        notifyDataSetChanged();
    }

    public List<Integer> getCheckedIds(){
        return checkedIds;
    }

    public boolean isSelectAll() {
        return checkedIds.size() == getData().size();
    }

    public void setSelectAll(boolean checked) {
        checkedIds.clear();
        if (checked) {
            for (int i = 0; i < getData().size(); i++) {
                checkedIds.add(getData().get(i).getDevcieid());
            }
        }
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceDevice.pbui_Item_DeviceDetailInfo item) {
        boolean isOnline = item.getNetstate() == 1;
        helper.setText(R.id.item_view_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_view_2, item.getDevname().toStringUtf8())
                .setText(R.id.item_view_3, Constant.getDeviceTypeName(getContext(), item.getDevcieid()))
                .setText(R.id.item_view_4, isOnline ? getContext().getResources().getString(R.string.online) : getContext().getResources().getString(R.string.offline));
        boolean isSelected = checkedIds.contains(item.getDevcieid());
        int textColor = isOnline ? getContext().getColor(R.color.online) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor)
                .setTextColor(R.id.item_view_3, textColor)
                .setTextColor(R.id.item_view_4, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor)
                .setBackgroundColor(R.id.item_view_3, backgroundColor)
                .setBackgroundColor(R.id.item_view_4, backgroundColor);
    }
}
