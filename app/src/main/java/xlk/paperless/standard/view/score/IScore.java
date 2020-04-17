package xlk.paperless.standard.view.score;

import com.mogujie.tt.protobuf.InterfaceFilescorevote;

/**
 * @author xlk
 * @date 2020/4/11
 * @Description:
 */
public interface IScore {
    void update(InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore info);

    void close(int id);
}
