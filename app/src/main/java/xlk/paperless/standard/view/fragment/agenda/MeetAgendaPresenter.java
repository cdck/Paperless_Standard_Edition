package xlk.paperless.standard.view.fragment.agenda;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceAgenda;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceMacro;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.BasePresenter;

/**
 * @author xlk
 * @date 2020/3/20
 * @Description:
 */
public class MeetAgendaPresenter extends BasePresenter {
    private final String TAG = "MeetAgendaPresenter-->";
    private final Context cxt;
    private final IMeetAgenda view;

    public MeetAgendaPresenter(Context cxt, IMeetAgenda view) {
        this.cxt = cxt;
        this.view = view;
    }

    @Override
    public void register() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void unregister() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void BusEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case Constant.BUS_AGENDA_FILE://议程文件下载完成
                String path = (String) msg.getObjs()[0];
                view.displayFile(path);
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETAGENDA_VALUE://议程变更通知
                queryAgenda();
                break;
        }
    }

    public void queryAgenda() {
        view.initDefault();
        try {
            InterfaceAgenda.pbui_meetAgenda meetAgenda = jni.queryAgenda();
            if (meetAgenda == null) {
                return;
            }
            int agendatype = meetAgenda.getAgendatype();
            if (agendatype == InterfaceMacro.Pb_AgendaType.Pb_MEET_AGENDA_TYPE_TEXT.getNumber()) {//文本
                String text = meetAgenda.getText().toStringUtf8();
                LogUtil.e(TAG, "fun_queryAgenda 获取到文本议程 --> " + text);
                view.setAgendaTv(text);
            } else if (agendatype == InterfaceMacro.Pb_AgendaType.Pb_MEET_AGENDA_TYPE_FILE.getNumber()) {//文件
                int mediaid = meetAgenda.getMediaid();
                byte[] bytes = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(), mediaid);
                InterfaceBase.pbui_CommonTextProperty textProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(bytes);
                String fileName = textProperty.getPropertyval().toStringUtf8();
                LogUtil.i(TAG, "fun_queryAgenda 获取到文件议程 -->" + mediaid + ", 文件名：" + fileName);
                FileUtil.createDir(Constant.agenda_file_dir);
                File file = new File(Constant.agenda_file_dir + fileName);
                if (file.exists()) {
                    view.displayFile(file.getAbsolutePath());
                } else {
                    jni.creationFileDownload(Constant.agenda_file_dir + fileName, mediaid, 1, 0, Constant.AGENDA_FILE_KEY);
                }
            } else if (agendatype == InterfaceMacro.Pb_AgendaType.Pb_MEET_AGENDA_TYPE_TIME.getNumber()) {//时间轴式议程
                List<InterfaceAgenda.pbui_ItemAgendaTimeInfo> itemList = meetAgenda.getItemList();
                for (InterfaceAgenda.pbui_ItemAgendaTimeInfo item : itemList) {
                    int dirid = item.getDirid();
                    int agendaid = item.getAgendaid();
                    String content = item.getDesctext().toStringUtf8();
                    int status = item.getStatus();
                    LogUtil.i(TAG, "fun_queryAgenda 获取到时间轴式议程 -->" + dirid + ", content: " + content + ", status: " + status);
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
