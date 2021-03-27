package xlk.paperless.standard.view.admin.fragment.after.annotation;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc
 */
public class AdminAnnotationPresenter extends BasePresenter {
    private final AdminAnnotationInterface view;
    private List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> annotationFiles = new ArrayList<>();

    public AdminAnnotationPresenter(AdminAnnotationInterface view) {
        super();
        this.view = view;
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //会议目录文件变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE_VALUE:
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
                    byte[] o = (byte[]) msg.getObjects()[0];
                    InterfaceBase.pbui_MeetNotifyMsgForDouble pbui_meetNotifyMsgForDouble = InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(o);
                    if (pbui_meetNotifyMsgForDouble.getId() == Constant.ANNOTATION_FILE_DIRECTORY_ID) {
                        queryFile();
                    }
                }
                break;
            default:
                break;
        }
    }

    public void queryFile() {
        try {
            InterfaceFile.pbui_Type_MeetDirFileDetailInfo dirFileDetailInfo = jni.queryMeetDirFile(Constant.ANNOTATION_FILE_DIRECTORY_ID);
            annotationFiles.clear();
            if (dirFileDetailInfo != null) {
                annotationFiles.addAll(dirFileDetailInfo.getItemList());
            }
            view.update(annotationFiles);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
