package xlk.paperless.standard.view.admin.fragment.mid.devcontrol;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.bean.DevControlBean;

/**
 * @author Created by xlk on 2020/10/24.
 * @desc
 */
public class AdminDevControlAdapter extends BaseQuickAdapter<DevControlBean, BaseViewHolder> {
    List<Integer> checkedIds = new ArrayList<>();

    public AdminDevControlAdapter(int layoutResId, @Nullable List<DevControlBean> data) {
        super(layoutResId, data);
    }

    public void setChecked(int id) {
        if (checkedIds.contains(id)) {
            checkedIds.remove(checkedIds.indexOf(id));
        } else {
            checkedIds.add(id);
        }
        notifyDataSetChanged();
    }

    public List<Integer> getCheckIds() {
        return checkedIds;
    }

    public void setCheckAll(boolean check) {
        checkedIds.clear();
        if (check) {
            for (int i = 0; i < getData().size(); i++) {
                checkedIds.add(getData().get(i).getDeviceInfo().getDevcieid());
            }
        }
        notifyDataSetChanged();
    }

    public boolean isCheckedAll() {
        return getData().size() == checkedIds.size();
    }

    @Override
    protected void convert(BaseViewHolder helper, DevControlBean item) {
        InterfaceDevice.pbui_Item_DeviceDetailInfo dev = item.getDeviceInfo();
        boolean online = dev.getNetstate() == 1;
        InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo seatInfo = item.getSeatInfo();
        helper.setText(R.id.item_view_1, String.valueOf(dev.getDevcieid()))
                .setText(R.id.item_view_2, dev.getDevname().toStringUtf8())
                .setText(R.id.item_view_3, Constant.getDeviceTypeName(getContext(), dev.getDevcieid()))
                .setText(R.id.item_view_4, isOut(dev.getDeviceflag()) ? "√" : "")
                .setText(R.id.item_view_5, dev.getIpinfoList().get(0).getIp().toStringUtf8())
                .setText(R.id.item_view_6, online ? getContext().getString(R.string.online) : getContext().getString(R.string.offline))
                .setText(R.id.item_view_7, seatInfo != null ? seatInfo.getMembername().toStringUtf8() : "")
                .setText(R.id.item_view_8, Constant.getInterfaceStateName(getContext(), dev.getFacestate()));
        int textColor = online ? getContext().getColor(R.color.online) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor)
                .setTextColor(R.id.item_view_3, textColor)
                .setTextColor(R.id.item_view_4, textColor)
                .setTextColor(R.id.item_view_5, textColor)
                .setTextColor(R.id.item_view_6, textColor)
                .setTextColor(R.id.item_view_7, textColor)
                .setTextColor(R.id.item_view_8, textColor);
        boolean isSelected = checkedIds.contains(dev.getDevcieid());
        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor)
                .setBackgroundColor(R.id.item_view_3, backgroundColor)
                .setBackgroundColor(R.id.item_view_4, backgroundColor)
                .setBackgroundColor(R.id.item_view_5, backgroundColor)
                .setBackgroundColor(R.id.item_view_6, backgroundColor)
                .setBackgroundColor(R.id.item_view_7, backgroundColor)
                .setBackgroundColor(R.id.item_view_8, backgroundColor);
    }


    public boolean isOut(int flag) {
        return InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_OPENOUTSIDE_VALUE == (flag & InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_OPENOUTSIDE_VALUE);
    }
}
