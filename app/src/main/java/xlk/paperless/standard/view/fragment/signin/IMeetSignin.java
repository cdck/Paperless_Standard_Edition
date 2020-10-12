package xlk.paperless.standard.view.fragment.signin;

import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

/**
 * @author xlk
 * @date 2020/3/18
 * @desc
 */
public interface IMeetSignin {
    void updateBg(String filepath);

    void updateSignin(int yqd, int yd);

    /**
     * @param isShow 是否要显示席位图标
     */
    void updateView(List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatDetailInfo, boolean isShow);

}
