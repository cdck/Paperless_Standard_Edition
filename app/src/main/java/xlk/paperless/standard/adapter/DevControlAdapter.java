package xlk.paperless.standard.adapter;

import androidx.annotation.Nullable;
import android.widget.CheckBox;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.bean.DevControlBean;

/**
 * @author xlk
 * @date 2020/4/1
 * @desc 终端控制adapter
 */
public class DevControlAdapter extends BaseQuickAdapter<DevControlBean, BaseViewHolder> {
    List<Integer> chooseIds = new ArrayList<>();

    public DevControlAdapter(int layoutResId, @Nullable List<DevControlBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, DevControlBean item) {
        boolean isOnline = item.getDeviceInfo().getNetstate() == 1;
        String memberName = "";
        InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo seatInfo = item.getSeatInfo();
        if (seatInfo != null) {
            memberName = seatInfo.getMembername().toStringUtf8();
        }
        int deviceflag = item.getDeviceInfo().getDeviceflag();
        boolean b = InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_OPENOUTSIDE_VALUE == (deviceflag & InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_OPENOUTSIDE_VALUE);
        helper.setText(R.id.item_dev_control_cb, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_dev_control_name, item.getDeviceInfo().getDevname().toStringUtf8())
                .setText(R.id.item_dev_control_type, Constant.getDeviceTypeName(getContext(), item.getDeviceInfo().getDevcieid()))
                .setText(R.id.item_dev_control_id, String.valueOf(item.getDeviceInfo().getDevcieid()))
                .setText(R.id.item_dev_control_state, (isOnline ? getContext().getString(R.string.online) : getContext().getString(R.string.offline)))
                .setText(R.id.item_dev_control_member, memberName)
                .setText(R.id.item_dev_control_interface, Constant.getInterfaceStateName(getContext(), item.getDeviceInfo().getFacestate()));
//                .setText(R.id.item_dev_control_outopen, b ? "√" : "");
        boolean contains = chooseIds.contains(item.getDeviceInfo().getDevcieid());
        CheckBox cb = helper.getView(R.id.item_dev_control_cb);
        cb.setChecked(contains);
        int color = isOnline ? getContext().getResources().getColor(R.color.blue) : getContext().getResources().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_dev_control_cb, color)
                .setTextColor(R.id.item_dev_control_name, color)
                .setTextColor(R.id.item_dev_control_type, color)
                .setTextColor(R.id.item_dev_control_id, color)
                .setTextColor(R.id.item_dev_control_state, color)
                .setTextColor(R.id.item_dev_control_member, color)
                .setTextColor(R.id.item_dev_control_interface, color)
                .setTextColor(R.id.item_dev_control_outopen, color);

    }

    public void choose(int devId) {
        if (chooseIds.contains(devId)) {
            chooseIds.remove(chooseIds.indexOf(devId));
        } else {
            chooseIds.add(devId);
        }
        notifyChoose();
    }

    public List<Integer> getChooseIds() {
        return chooseIds;
    }

    public void notifyChoose() {
        List<Integer> temps = new ArrayList<>();
        for (int i = 0; i < getData().size(); i++) {
            DevControlBean devControlBean = getData().get(i);
            int devcieid = devControlBean.getDeviceInfo().getDevcieid();
            if (chooseIds.contains(devcieid)) {
                temps.add(devcieid);
            }
        }
        chooseIds = temps;
        notifyDataSetChanged();
    }

    public boolean isCheckAll() {
        return chooseIds.size() == getData().size();
    }

    public void setChooseAll(boolean checked) {
        chooseIds.clear();
        if (checked) {
            List<Integer> temps = new ArrayList<>();
            for (int i = 0; i < getData().size(); i++) {
                int devcieid = getData().get(i).getDeviceInfo().getDevcieid();
                temps.add(devcieid);
            }
            chooseIds.addAll(temps);
        }
        notifyDataSetChanged();
    }
}
