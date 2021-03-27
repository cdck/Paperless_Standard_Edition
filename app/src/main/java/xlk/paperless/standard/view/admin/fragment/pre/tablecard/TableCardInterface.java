package xlk.paperless.standard.view.admin.fragment.pre.tablecard;

import com.mogujie.tt.protobuf.InterfaceTablecard;

/**
 * @author Created by xlk on 2020/11/4.
 * @desc
 */
public interface TableCardInterface {

    /**
     * 更新UI
     * @param info 桌牌信息
     */
    void updateTableCard(InterfaceTablecard.pbui_Type_MeetTableCardDetailInfo info);

    /**
     * 更新桌牌的背景图片
     * @param filePath 图片路径
     * @param mediaid
     */
    void updateTableCardBg(String filePath, int mediaid);

    /**
     * 清除背景
     */
    void clearBgImage();

    /**
     * 更新背景图片文件
     */
    void updatePictureRv();
}
