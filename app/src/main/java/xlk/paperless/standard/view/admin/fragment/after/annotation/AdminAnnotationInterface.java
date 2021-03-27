package xlk.paperless.standard.view.admin.fragment.after.annotation;

import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.List;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc
 */
public interface AdminAnnotationInterface {
    /**
     * 更新文件列表
     * @param annotationFiles 批注文件数据
     */
    void update(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> annotationFiles);
}
