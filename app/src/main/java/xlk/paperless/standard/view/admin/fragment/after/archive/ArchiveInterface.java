package xlk.paperless.standard.view.admin.fragment.after.archive;

import java.util.List;

/**
 * @author Created by xlk on 2020/10/27.
 * @desc
 */
public interface ArchiveInterface {

    /**
     * 更新操作通知
     * @param archiveInforms 操作通知信息
     */
    void updateArchiveInform(List<ArchiveInform> archiveInforms);

    void showToast(int resid);
}
