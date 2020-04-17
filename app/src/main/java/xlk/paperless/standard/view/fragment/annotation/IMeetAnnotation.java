package xlk.paperless.standard.view.fragment.annotation;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.List;

import xlk.paperless.standard.data.bean.DevMember;
import xlk.paperless.standard.data.bean.SeatMember;

/**
 * @author xlk
 * @date 2020/3/18
 * @Description:
 */
public interface IMeetAnnotation {
    void updateMemberRv(List<SeatMember> memberDetailInfos);

    void updateFileRv(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> fileDetailInfos);

    void showPushView(List<DevMember> onlineMembers, List<InterfaceDevice.pbui_Item_DeviceDetailInfo> onLineProjectors, int mediaId);
}
