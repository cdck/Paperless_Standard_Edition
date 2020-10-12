package xlk.paperless.standard.view.admin.fragment.system.device;

import com.mogujie.tt.protobuf.InterfaceDevice;

import java.util.List;

import xlk.paperless.standard.base.BaseInterface;

/**
 * @author Created by xlk on 2020/9/18.
 * @desc
 */
public interface AdminDeviceManageInterface extends BaseInterface {

    /**
     * 更新设备列表
     */
    void updateDeviceRv(List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceInfos);
}
