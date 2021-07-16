package xlk.paperless.standard.view.admin.fragment.pre.agenda;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceAgenda;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author Created by xlk on 2020/10/20.
 * @desc
 */
public class AdminAgendaPresenter extends BasePresenter {
    private final AdminAgendaInterface view;
    List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> agendaFiles = new ArrayList<>();

    public AdminAgendaPresenter(AdminAgendaInterface view) {
        super();
        this.view = view;
    }

    public List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> getAgendaFiles() {
        return agendaFiles;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //议程变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETAGENDA_VALUE:
                queryAgenda();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE_VALUE:
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsgForDouble info = InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(bytes);
                int opermethod = info.getOpermethod();
                int id = info.getId();
                int subid = info.getSubid();
                LogUtil.i(TAG, "busEvent 会议目录文件变更通知 id=" + id + ", subid=" + subid + ", opermethod=" + opermethod);
                if (id == Constant.SHARED_FILE_DIRECTORY_ID) {
                    queryShareFile();
                }
                break;
            case Constant.BUS_READ_AGENDA_TXT:
                String content = (String) msg.getObjects()[0];
                view.showProgressBar(false);
                view.updateAgendaContent(content);
                break;
            default:
                break;
        }
    }

    public void uploadFile(int uploadflag, int dirid, int attrib, String newname, String pathname, int userval, String userStr) {
        jni.uploadFile(uploadflag, dirid, attrib, newname, pathname, userval,  userStr);
    }

    public void queryAgenda() {
        try {
            InterfaceAgenda.pbui_meetAgenda meetAgenda = jni.queryAgenda();
            if (meetAgenda == null) {
                return;
            }
            int agendatype = meetAgenda.getAgendatype();
            String agendaContent = meetAgenda.getText().toStringUtf8();
            int mediaId = meetAgenda.getMediaid();
            if (agendatype == InterfaceMacro.Pb_AgendaType.Pb_MEET_AGENDA_TYPE_TEXT_VALUE) {
                view.updateAgendaContent(agendaContent);
            } else if (agendatype == InterfaceMacro.Pb_AgendaType.Pb_MEET_AGENDA_TYPE_FILE_VALUE) {
                byte[] bytes = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(), mediaId);
                InterfaceBase.pbui_CommonTextProperty textProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(bytes);
                String fileName = textProperty.getPropertyval().toStringUtf8();
                LogUtil.i(TAG, "queryAgenda 获取到文件议程 -->" + mediaId + ", 文件名：" + fileName);
                view.updateAgendaFileName(mediaId, fileName);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryShareFile() {
        try {
            InterfaceFile.pbui_Type_MeetDirFileDetailInfo shareDirFile = jni.queryMeetDirFile(1);
            agendaFiles.clear();
            if (shareDirFile != null) {
                List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> itemList = shareDirFile.getItemList();
                for (int i = 0; i < itemList.size(); i++) {
                    InterfaceFile.pbui_Item_MeetDirFileDetailInfo item = itemList.get(i);
                    String fileName = item.getName().toStringUtf8();
                    String suffix = fileName.substring(fileName.lastIndexOf("."));
                    if (suffix.equals(".pdf") || suffix.equals(".doc") || suffix.equals(".docx")) {
                        agendaFiles.add(item);
                    }
                }
            }
            view.updateAgendaFileRv();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void modifyTextAgenda(String content) {
        jni.modifyTextAgenda(content);
    }

    public void modifyFileAgenda(int mediaId) {
        jni.modifyFileAgenda(mediaId);
    }

    public void delFile(InterfaceFile.pbui_Item_MeetDirFileDetailInfo selected) {
        jni.deleteMeetDirFile(1, selected);
    }
}
