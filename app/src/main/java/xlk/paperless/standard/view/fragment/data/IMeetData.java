package xlk.paperless.standard.view.fragment.data;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.List;

import xlk.paperless.standard.data.bean.DevMember;

/**
 * @author xlk
 * @date 2020/3/14
 * @desc
 */
public interface IMeetData {
    void updateDir(List<InterfaceFile.pbui_Item_MeetDirDetailInfo> dirDetailInfos);

    void updatePage(String page);

    void updateFileRv(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> fileDetailInfos);

    void showPushView(List<DevMember> onlineMembers, List<InterfaceDevice.pbui_Item_DeviceDetailInfo> onLineProjectors, int mediaId);
}
