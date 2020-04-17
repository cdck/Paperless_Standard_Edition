package xlk.paperless.standard.view.fragment.score;

import com.mogujie.tt.protobuf.InterfaceFilescorevote;

import java.util.List;

/**
 * @author xlk
 * @date 2020/3/20
 * @Description:
 */
public interface IMeetScore {
    void updateScoreRv(List<InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore> scoreInfos);

    void updateRightRv();

    void updateOnlineMemberRv();
}
