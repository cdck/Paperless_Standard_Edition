package xlk.paperless.standard.view.admin;

import com.mogujie.tt.protobuf.InterfaceMeet;

/**
 * @author Created by xlk on 2020/9/17.
 * @desc
 */
public interface AdminInterface {

    /**
     * 更新在线离线UI
     */
    void updateOnlineStatus(boolean onLine);

    /**
     * 更新席位名称
     */
    void updateDeviceName(String devName);

    /**
     * 更新会议状态
     * @param item 当前的会议
     *  =0未开始会议，=1已开始会议，=2已结束会议
     */
    void updateMeetStatus(InterfaceMeet.pbui_Item_MeetMeetInfo item);

}
