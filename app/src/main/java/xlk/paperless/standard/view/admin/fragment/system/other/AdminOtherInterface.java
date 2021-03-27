package xlk.paperless.standard.view.admin.fragment.system.other;

import java.util.List;

/**
 * @author Created by xlk on 2020/9/22.
 * @desc
 */
public interface AdminOtherInterface {

    /**
     * 更新网址
     */
    void updateUrl();

    /**
     * 更新公司名
     *
     * @param company 公司名
     */
    void updateCompany(String company);

    /**
     * 更新升级文件列表
     */
    void updateUpGradeFileRv();

    /**
     * 更新会议发布文件列表
     */
    void updateReleaseFileRv();

    /**
     * 更新logo图标
     *
     * @param filePath 图片路径
     */
    void updateMainLogoImg(String filePath);

    /**
     * 更新主界面背景图
     *
     * @param filePath 图片路径
     */
    void updateMainBgImg(String filePath);

    /**
     * 更新公告背景图
     *
     * @param filePath 图片路径
     */
    void updateNoticeBgImg(String filePath);

    /**
     * 更新公告Logo图标
     *
     * @param filePath 图片路径
     */
    void updateNoticeLogoImg(String filePath);

    /**
     * 更新投影界面背景图
     *
     * @param filePath 图片路径
     */
    void updateProjectiveBgImg(String filePath);

    /**
     * 更新投影界面logo图标
     *
     * @param filePath 图片路径
     */
    void updateProjectiveLogoImg(String filePath);

    /**
     * 更新背景图片列表
     */
    void updatePictureRv();

    /**
     * 更新界面数据
     *
     * @param mainInterfaceBeans       主界面
     * @param projectiveInterfaceBeans 投影界面
     * @param noticeInterfaceBeans     公告界面
     */
    void updateInterface(List<MainInterfaceBean> mainInterfaceBeans, List<MainInterfaceBean> projectiveInterfaceBeans, List<MainInterfaceBean> noticeInterfaceBeans);

    void updateCurrentReleaseFileName(String fileName);
}
