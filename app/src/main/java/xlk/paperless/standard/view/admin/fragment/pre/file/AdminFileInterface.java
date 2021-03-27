package xlk.paperless.standard.view.admin.fragment.pre.file;

import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMeet;

import java.util.List;

/**
 * @author Created by xlk on 2020/10/21.
 * @desc
 */
public interface AdminFileInterface {
    /**
     * 更新目录列表
     *
     * @param dirInfos 目录信息
     */
    void updateDirRv(List<InterfaceFile.pbui_Item_MeetDirDetailInfo> dirInfos);

    /**
     * 更新目录文件列表
     *
     * @param dirFiles 当前目录下的文件信息
     */
    void updateDirFileRv(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> dirFiles);

    /**
     * 更新会议目录权限
     *
     * @param memberDirPermissionBeans 参会人的目录权限信息
     */
    void updateMemberPermission(List<MemberDirPermissionBean> memberDirPermissionBeans);

    /**
     * 更新文件排序中的文件列表
     *
     * @param sortDirFiles 目录下文件信息
     */
    void updateSortFileRv(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> sortDirFiles);

    /**
     * 更新PopupWindow中的会议列表
     *
     * @param meetings 所有会议
     */
    void updateMeetingRv(List<InterfaceMeet.pbui_Item_MeetMeetInfo> meetings);

    /**
     * 更新历史资料中的文件列表
     *
     * @param dirFiles 目录文件
     */
    void updateHistoryDirFileRv(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> dirFiles);
}
