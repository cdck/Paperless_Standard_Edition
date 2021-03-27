package xlk.paperless.standard.view.admin.fragment.mid.camera;

import java.util.List;

import xlk.paperless.standard.data.bean.VideoDev;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc
 */
public interface AdminCameraControlInterface {
    /**
     * 更新视频列表
     * @param videoDevs 视频信息
     */
    void updateRv(List<VideoDev> videoDevs);

    void updateDecode(Object[] objs);

    void updateYuv(Object[] objs1);

    void stopResWork(int resid);

    void notifyOnLineAdapter();

}
