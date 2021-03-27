package xlk.paperless.standard.view.admin.fragment.pre.camera;

import java.util.List;

/**
 * @author Created by xlk on 2020/10/22.
 * @desc
 */
public interface AdminCameraManageInterface {
    /**
     * 更新会议可用摄像机列表
     *
     * @param availableCameras 可用摄像机数据
     */
    void updateAvailableCamera(List<DevCameraBean> availableCameras);

    /**
     * 更新会议室全部摄像机列表
     *
     * @param allCameras 全部摄像机数据
     */
    void updateAllCamera(List<DevCameraBean> allCameras);
}
