package xlk.paperless.standard.data.bean;

import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;

/**
 * @author xlk
 * @date 2020/3/18
 * @Description:
 */
public class SeatMember {
    InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo;
    InterfaceRoom.pbui_Item_MeetSeatDetailInfo seatDetailInfo;

    public SeatMember(InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo, InterfaceRoom.pbui_Item_MeetSeatDetailInfo seatDetailInfo) {
        this.memberDetailInfo = memberDetailInfo;
        this.seatDetailInfo = seatDetailInfo;
    }

    public InterfaceMember.pbui_Item_MemberDetailInfo getMemberDetailInfo() {
        return memberDetailInfo;
    }

    public InterfaceRoom.pbui_Item_MeetSeatDetailInfo getSeatDetailInfo() {
        return seatDetailInfo;
    }
}
