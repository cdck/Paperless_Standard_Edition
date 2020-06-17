package xlk.paperless.standard.view.fragment.live;

import java.util.List;

import xlk.paperless.standard.data.bean.VideoDev;

/**
 * @author xlk
 * @date 2020/3/18
 * @desc
 */
public interface IMeetLiveVideo {
    void updateRv(List<VideoDev> videoDevs);

    void updateDecode(Object[] objs);

    void updateYuv(Object[] objs1);

    void stopResWork(int resid);

    void notifyOnLineAdapter();
}
