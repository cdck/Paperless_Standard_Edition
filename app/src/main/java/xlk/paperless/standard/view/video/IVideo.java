package xlk.paperless.standard.view.video;

/**
 * @author xlk
 * @date 2020/3/30
 * @desc
 */
public interface IVideo {
    void updateProgressUi(int per, String s, String s1);

    void updateYuv(int w, int h, byte[] y, byte[] u, byte[] v);

    void setCodecType(int type);

    void setCanNotExit();

    void close();

    void notifyOnLineAdapter();
}
