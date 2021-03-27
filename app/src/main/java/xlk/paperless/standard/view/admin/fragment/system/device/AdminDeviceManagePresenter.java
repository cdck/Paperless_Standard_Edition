package xlk.paperless.standard.view.admin.fragment.system.device;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.base.BasePresenter;

/**
 * @author Created by xlk on 2020/9/18.
 * @desc
 */
public class AdminDeviceManagePresenter extends BasePresenter {

    private final WeakReference<Context> cxt;
    private final WeakReference<AdminDeviceManageInterface> view;
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceInfos = new ArrayList<>();
    public List<InterfaceDevice.pbui_Item_DeviceDetailInfo> clientDevices = new ArrayList<>();

    public AdminDeviceManagePresenter(Context context, AdminDeviceManageInterface view) {
        super();
        this.cxt = new WeakReference<Context>(context);
        this.view = new WeakReference<AdminDeviceManageInterface>(view);
    }

    public void queryDevice() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo info = jni.queryDeviceInfo();
            deviceInfos.clear();
            clientDevices.clear();
            if (info != null) {
                List<InterfaceDevice.pbui_Item_DeviceDetailInfo> pdevList = info.getPdevList();
                deviceInfos.addAll(pdevList);
                for (int i = 0; i < pdevList.size(); i++) {
                    InterfaceDevice.pbui_Item_DeviceDetailInfo item = pdevList.get(i);
                    if (Constant.isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetClient_VALUE, item.getDevcieid())
                            && item.getNetstate() == 1) {
                        clientDevices.add(item);
                    }
                }
            }
            view.get().updateDeviceRv(deviceInfos);
            view.get().updateClientRv();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void modifyDevice(int modflag, int devId, String devName, int lift0, int lift1, int deviceflag, InterfaceDevice.pbui_SubItem_DeviceIpAddrInfo ipInfo) {
        jni.modifyDevice(modflag, devId, devName, lift0, lift1, deviceflag, ipInfo);
    }

    public void deleteDevice(int devcieid) {
        jni.deleteDevice(devcieid);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cxt.clear();
        view.clear();
    }

    @Override
    public void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE_VALUE://会场设备信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO_VALUE://设备寄存器变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEMEETSTATUS_VALUE://界面状态变更通知
                queryDevice();
                break;
            default:
                break;
        }
    }
}
