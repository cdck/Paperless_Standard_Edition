package xlk.paperless.standard.view.admin.fragment.system.device;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BaseFragment;

/**
 * @author Created by xlk on 2020/9/18.
 * @desc
 */
public class AdminDeviceManageFragment extends BaseFragment implements AdminDeviceManageInterface, View.OnClickListener {
    private final String TAG = "AdminDeviceManageFragment-->";
    private RecyclerView rv_device;
    private TextInputEditText tie_dev_name;
    private TextInputEditText tie_dev_ip;
    private TextInputEditText tie_lift_id;
    private TextInputEditText tie_mike_id;
    private Button btn_modify;
    private Button btn_delete;
    private Button btn_visitors;
    private Button btn_deploy;
    private AdminDeviceManagePresenter presenter;
    private DeviceAdapter deviceAdapter;
    private InterfaceDevice.pbui_Item_DeviceDetailInfo selectedDevice;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_device_manage, container, false);
        initView(inflate);
        presenter = new AdminDeviceManagePresenter(getContext(), this);
        presenter.queryDevice();
        return inflate;
    }

    private void initView(View inflate) {
        rv_device = (RecyclerView) inflate.findViewById(R.id.rv_device);
        tie_dev_name = (TextInputEditText) inflate.findViewById(R.id.tie_dev_name);
        tie_dev_ip = (TextInputEditText) inflate.findViewById(R.id.tie_dev_ip);
        tie_lift_id = (TextInputEditText) inflate.findViewById(R.id.tie_lift_id);
        tie_mike_id = (TextInputEditText) inflate.findViewById(R.id.tie_mike_id);
        btn_modify = (Button) inflate.findViewById(R.id.btn_modify);
        btn_delete = (Button) inflate.findViewById(R.id.btn_delete);
        btn_visitors = (Button) inflate.findViewById(R.id.btn_visitors);
        btn_deploy = (Button) inflate.findViewById(R.id.btn_deploy);

        btn_modify.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_visitors.setOnClickListener(this);
        btn_deploy.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (selectedDevice == null) {
            ToastUtil.show(R.string.please_choose_device_first);
            return;
        }
        switch (v.getId()) {
            case R.id.btn_modify:
                String currentDevName = tie_dev_name.getText().toString();
                String currentDevIp = tie_dev_ip.getText().toString().trim();
                String currentLiftId = tie_lift_id.getText().toString().trim();
                String currentMikeId = tie_mike_id.getText().toString().trim();
                if (currentDevName.isEmpty() || currentDevIp.isEmpty() || currentLiftId.isEmpty() || currentMikeId.isEmpty()) {
                    ToastUtil.show(R.string.please_enter_all_content);
                    return;
                }
                int liftId = Integer.parseInt(currentLiftId);
                int mikeId = Integer.parseInt(currentMikeId);
                InterfaceDevice.pbui_SubItem_DeviceIpAddrInfo build = InterfaceDevice.pbui_SubItem_DeviceIpAddrInfo.newBuilder().setIp(ConvertUtil.s2b(currentDevIp)).build();
                int modflag = InterfaceMacro.Pb_DeviceModifyFlag.Pb_DEVICE_MODIFYFLAG_NAME_VALUE
                        | InterfaceMacro.Pb_DeviceModifyFlag.Pb_DEVICE_MODIFYFLAG_IPADDR_VALUE
                        | InterfaceMacro.Pb_DeviceModifyFlag.Pb_DEVICE_MODIFYFLAG_LIFTRES_VALUE;
                presenter.modifyDevice(modflag, selectedDevice.getDevcieid(), currentDevName, liftId, mikeId, selectedDevice.getDeviceflag(), build);
                break;
            case R.id.btn_delete:
                if (selectedDevice.getNetstate() == 0) {
                    presenter.deleteDevice(selectedDevice.getDevcieid());
                } else {
                    ToastUtil.show(R.string.err_delete_offline);
                }
                break;
            case R.id.btn_visitors:
                break;
            case R.id.btn_deploy:
                break;
        }
    }

    @Override
    public void updateDeviceRv(List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceInfos) {
        if (deviceAdapter == null) {
            deviceAdapter = new DeviceAdapter(R.layout.item_admin_device_manage, deviceInfos);
            rv_device.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_device.setAdapter(deviceAdapter);
            deviceAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    selectedDevice = deviceInfos.get(position);
                    LogUtil.i(TAG, "onItemClick 选中设备= id：" + selectedDevice.getDevcieid() + ",名称：" + selectedDevice.getDevname().toStringUtf8());
                    deviceAdapter.setSelected(selectedDevice.getDevcieid());
                    updateBottomUI(selectedDevice);
                }
            });
        } else {
            deviceAdapter.notifyDataSetChanged();
        }
    }

    private void updateBottomUI(InterfaceDevice.pbui_Item_DeviceDetailInfo info) {
        tie_dev_name.setText(info.getDevname().toStringUtf8());
        List<InterfaceDevice.pbui_SubItem_DeviceIpAddrInfo> ipinfoList = info.getIpinfoList();
        if (!ipinfoList.isEmpty()) {
            InterfaceDevice.pbui_SubItem_DeviceIpAddrInfo item = ipinfoList.get(0);
            tie_dev_ip.setText(item.getIp().toStringUtf8());
        }
        tie_lift_id.setText(String.valueOf(info.getLiftgroupres0()));
        tie_mike_id.setText(String.valueOf(info.getLiftgroupres1()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
