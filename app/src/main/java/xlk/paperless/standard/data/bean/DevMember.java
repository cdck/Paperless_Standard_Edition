package xlk.paperless.standard.data.bean;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMember;

/**
 * @author xlk
 * @date 2020/3/14
 * @desc 在线参会人
 */
public class DevMember {
    InterfaceDevice.pbui_Item_DeviceDetailInfo deviceDetailInfo;
    InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo;

    public DevMember(InterfaceDevice.pbui_Item_DeviceDetailInfo deviceDetailInfo, InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo) {
        this.deviceDetailInfo = deviceDetailInfo;
        this.memberDetailInfo = memberDetailInfo;
    }

    public InterfaceDevice.pbui_Item_DeviceDetailInfo getDeviceDetailInfo() {
        return deviceDetailInfo;
    }

    public void setDeviceDetailInfo(InterfaceDevice.pbui_Item_DeviceDetailInfo deviceDetailInfo) {
        this.deviceDetailInfo = deviceDetailInfo;
    }

    public InterfaceMember.pbui_Item_MemberDetailInfo getMemberDetailInfo() {
        return memberDetailInfo;
    }

    public void setMemberDetailInfo(InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo) {
        this.memberDetailInfo = memberDetailInfo;
    }
}
