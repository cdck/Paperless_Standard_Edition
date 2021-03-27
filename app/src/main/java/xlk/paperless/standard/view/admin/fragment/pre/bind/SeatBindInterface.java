package xlk.paperless.standard.view.admin.fragment.pre.bind;


import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

import xlk.paperless.standard.view.admin.fragment.pre.member.MemberRoleBean;

/**
 * @author Created by xlk on 2020/11/3.
 * @desc
 */
public interface SeatBindInterface {
    /**
     * 更新参会人列表
     */
    void updateMemberList(List<MemberRoleBean> devSeatInfos);

    /**
     * 更新是否隐藏座位图标
     *
     * @param hideIcon =true隐藏
     */
    void updateShowIcon(boolean hideIcon);

    /**
     * 更新设备排位
     *
     * @param seatData 设备排位信息
     */
    void updateSeatData(List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatData);

    /**
     * 更新会场底图
     *
     * @param currentRoomBgFilePath 图片路径
     * @param mediaId               图片媒体id
     */
    void updateRoomBg(String currentRoomBgFilePath, int mediaId);
}
