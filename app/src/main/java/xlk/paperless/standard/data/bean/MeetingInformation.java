package xlk.paperless.standard.data.bean;

import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceMember;

/**
 * @author Created by xlk on 2021/4/26.
 * @desc 自定义的会议信息
 */
public class MeetingInformation {
    /**
     * 当前会议信息
     */
    private InterfaceMeet.pbui_Item_MeetMeetInfo meetInfo;
    /**
     * 主持人信息
     */
    private InterfaceMember.pbui_Item_MemberDetailInfo hostInfo;
    /**
     * 会议的参会人
     */
    private String members;

    public MeetingInformation(InterfaceMeet.pbui_Item_MeetMeetInfo meetInfo) {
        this.meetInfo = meetInfo;
    }

    public InterfaceMeet.pbui_Item_MeetMeetInfo getMeetInfo() {
        return meetInfo;
    }

    public InterfaceMember.pbui_Item_MemberDetailInfo getHostInfo() {
        return hostInfo;
    }

    public void setHostInfo(InterfaceMember.pbui_Item_MemberDetailInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }
}
