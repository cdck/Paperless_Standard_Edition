package xlk.paperless.standard.view.admin.fragment.system.seat;

import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

/**
 * @author Created by xlk on 2020/11/3.
 * @desc
 */
public interface SeatArrangementInterface {
    /**
     * 更新会议室
     *
     * @param roomData 会议室数据
     */
    void updateRoomRv(List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> roomData);

    /**
     * 更新排位信息
     *
     * @param seatData 当前选中会议室的排位信息
     */
    void updateSeatData(List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatData);

    /**
     * 下载完成会场底图进行更新
     *
     * @param filepath 图片路径
     * @param mediaId
     */
    void updateRoomBg(String filepath, int mediaId);

    /**
     * 更新背景图片列表
     */
    void updatePictureRv();

    /**
     * 更新是否需要显示座位图标
     *
     * @param isShow =true需要座位显示
     */
    void updateShowIcon(boolean isShow);

    void cleanRoomBg();
}
