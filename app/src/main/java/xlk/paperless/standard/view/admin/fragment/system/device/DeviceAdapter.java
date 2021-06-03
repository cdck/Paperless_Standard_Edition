package xlk.paperless.standard.view.admin.fragment.system.device;

import android.graphics.Color;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;

/**
 * @author Created by xlk on 2020/9/18.
 * @desc 系统设置-设备管理
 */
public class DeviceAdapter extends BaseQuickAdapter<InterfaceDevice.pbui_Item_DeviceDetailInfo, BaseViewHolder> {
    int selectedId = 0;

    public DeviceAdapter(int layoutResId, @Nullable List<InterfaceDevice.pbui_Item_DeviceDetailInfo> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public InterfaceDevice.pbui_Item_DeviceDetailInfo getSelected() {
        for (int i = 0; i < getData().size(); i++) {
            if (getData().get(i).getDevcieid() == selectedId) {
                return getData().get(i);
            }
        }
        return null;
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceDevice.pbui_Item_DeviceDetailInfo item) {
        boolean online = item.getNetstate() == 1;
        InterfaceDevice.pbui_SubItem_DeviceIpAddrInfo ipinfo = item.getIpinfo(0);
        int deviceflag = item.getDeviceflag();
        boolean isGuestMode = (deviceflag & InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_GUESTMODE_VALUE) == InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_GUESTMODE_VALUE;
        boolean isVisaWaiver = (deviceflag & InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_DIRECTENTER_VALUE) == InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_DIRECTENTER_VALUE;

        helper.setText(R.id.item_tv_number, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_tv_dev_id, String.valueOf(item.getDevcieid()))
                .setText(R.id.item_tv_dev_name, String.valueOf(item.getDevname().toStringUtf8()))
                .setText(R.id.item_table3_tv_3, Constant.getDeviceTypeName(getContext(), item.getDevcieid()))
                .setText(R.id.item_tv_dev_version, item.getHardversion() + "." + item.getSoftversion())
                .setText(R.id.item_tv_dev_ip, ipinfo.getIp().toStringUtf8())
                .setText(R.id.item_tv_port, String.valueOf(ipinfo.getPort()))
                .setText(R.id.item_tv_guest_mode, isGuestMode ? getContext().getString(R.string.yes) : getContext().getString(R.string.no))
                .setText(R.id.item_tv_visa_waiver, isVisaWaiver ? getContext().getString(R.string.yes) : getContext().getString(R.string.no))
                .setText(R.id.item_tv_online, online ? getContext().getString(R.string.online) : getContext().getString(R.string.offline))
                .setText(R.id.item_tv_interface, Constant.getInterfaceStateName(getContext(), item.getFacestate()))
                .setText(R.id.item_tv_lift_id, String.valueOf(item.getLiftgroupres0()))
                .setText(R.id.item_tv_mike_id, String.valueOf(item.getLiftgroupres1()));

        int clolr = online ? Color.BLUE : Color.BLACK;
        helper.setTextColor(R.id.item_tv_number, clolr).setTextColor(R.id.item_tv_dev_id, clolr)
                .setTextColor(R.id.item_tv_dev_name, clolr).setTextColor(R.id.item_table3_tv_3, clolr)
                .setTextColor(R.id.item_tv_dev_version, clolr).setTextColor(R.id.item_tv_dev_ip, clolr)
                .setTextColor(R.id.item_tv_port, clolr).setTextColor(R.id.item_tv_guest_mode, clolr).setTextColor(R.id.item_tv_visa_waiver, clolr)
                .setTextColor(R.id.item_tv_online, clolr).setTextColor(R.id.item_tv_interface, clolr)
                .setTextColor(R.id.item_tv_lift_id, clolr).setTextColor(R.id.item_tv_mike_id, clolr);

        boolean isSelected = selectedId == item.getDevcieid();
        int selectedColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_tv_number, selectedColor).setBackgroundColor(R.id.item_tv_dev_id, selectedColor)
                .setBackgroundColor(R.id.item_tv_dev_name, selectedColor).setBackgroundColor(R.id.item_table3_tv_3, selectedColor)
                .setBackgroundColor(R.id.item_tv_dev_version, selectedColor).setBackgroundColor(R.id.item_tv_dev_ip, selectedColor)
                .setBackgroundColor(R.id.item_tv_port, selectedColor).setBackgroundColor(R.id.item_tv_guest_mode, selectedColor).setBackgroundColor(R.id.item_tv_visa_waiver, selectedColor)
                .setBackgroundColor(R.id.item_tv_online, selectedColor).setBackgroundColor(R.id.item_tv_interface, selectedColor)
                .setBackgroundColor(R.id.item_tv_lift_id, selectedColor).setBackgroundColor(R.id.item_tv_mike_id, selectedColor);
    }
}
