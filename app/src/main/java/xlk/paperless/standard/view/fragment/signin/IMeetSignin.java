package xlk.paperless.standard.view.fragment.signin;

import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

/**
 * @author xlk
 * @date 2020/3/18
 * @Description:
 */
public interface IMeetSignin {
    void updateBg(String filepath);

    void updateSignin(int yqd, int yd);

    void updateView(List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatDetailInfo);
}
