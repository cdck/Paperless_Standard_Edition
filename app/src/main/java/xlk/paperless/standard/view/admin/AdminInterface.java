package xlk.paperless.standard.view.admin;

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
     * @param meetId 当前的会议id
     * @param status =0未开始会议，=1已开始会议，=2已结束会议
     */
    void updateMeetStatus(int meetId,int status);

    /**
     * 更新会议名称
     * @param meetName 会议名称
     */
    void updateMeetName(String meetName);
}
