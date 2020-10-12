package xlk.paperless.standard.data.bean;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceVideo;

/**
 * @author xlk
 * @date 2020/3/18
 * @desc 会议视频和设备
 */
public class VideoDev {
    InterfaceVideo.pbui_Item_MeetVideoDetailInfo videoDetailInfo;
    InterfaceDevice.pbui_Item_DeviceDetailInfo deviceDetailInfo;

    public VideoDev(InterfaceVideo.pbui_Item_MeetVideoDetailInfo videoDetailInfo, InterfaceDevice.pbui_Item_DeviceDetailInfo deviceDetailInfo) {
        this.videoDetailInfo = videoDetailInfo;
        this.deviceDetailInfo = deviceDetailInfo;
    }

    public InterfaceVideo.pbui_Item_MeetVideoDetailInfo getVideoDetailInfo() {
        return videoDetailInfo;
    }

    public InterfaceDevice.pbui_Item_DeviceDetailInfo getDeviceDetailInfo() {
        return deviceDetailInfo;
    }

    public String getName() {
        return videoDetailInfo.getDevicename().toStringUtf8() + "-" + videoDetailInfo.getName().toStringUtf8();
    }
}
