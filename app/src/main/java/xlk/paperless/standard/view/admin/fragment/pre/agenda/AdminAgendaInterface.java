package xlk.paperless.standard.view.admin.fragment.pre.agenda;

/**
 * @author Created by xlk on 2020/10/20.
 * @desc
 */
public interface AdminAgendaInterface {

    /**
     * 更新议程文本内容
     * @param agendaContent 文本内容
     */
    void updateAgendaContent(String agendaContent);

    /**
     * 更新议程文件名
     * @param mediaId 文件id
     * @param fileName 文件名
     */
    void updateAgendaFileName(int mediaId, String fileName);

    /**
     * 更新PopupWindow议程文件
     */
    void updateAgendaFileRv();

    /**
     * 是否展示加载框
     * @param show =true展示
     */
    void showProgressBar(boolean show);
}
