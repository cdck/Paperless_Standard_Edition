package xlk.paperless.standard.view.admin.fragment.pre.camera;

import com.mogujie.tt.protobuf.InterfaceVideo;

/**
 * @author Created by xlk on 2020/10/23.
 * @desc
 */
public class DevCameraBean implements Comparable<DevCameraBean> {
    boolean isOnline;
    InterfaceVideo.pbui_Item_MeetVideoDetailInfo camera;

    public DevCameraBean(InterfaceVideo.pbui_Item_MeetVideoDetailInfo camera) {
        this.camera = camera;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public InterfaceVideo.pbui_Item_MeetVideoDetailInfo getCamera() {
        return camera;
    }

    @Override
    public int compareTo(DevCameraBean o) {
        int i = camera.getDeviceid() - o.getCamera().getDeviceid();
        if (i == 0) {
            return camera.getSubid() - o.getCamera().getSubid();
        }
        return i;
    }
}
