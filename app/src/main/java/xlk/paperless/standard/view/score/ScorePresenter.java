package xlk.paperless.standard.view.score;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFilescorevote;
import com.mogujie.tt.protobuf.InterfaceMacro;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.BasePresenter;

/**
 * @author xlk
 * @date 2020/4/11
 * @Description:
 */
public class ScorePresenter extends BasePresenter {
    private final String TAG = "ScorePresenter-->";
    private final IScore view;
    private final Context cxt;

    public ScorePresenter(Context context, IScore view) {
        this.cxt = context;
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
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FILESCOREVOTE_VALUE:
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    byte[] o1 = (byte[]) msg.getObjs()[0];
                    InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(o1);
                    int id = pbui_meetNotifyMsg.getId();
                    int opermethod = pbui_meetNotifyMsg.getOpermethod();
                    LogUtil.d(TAG, "BusEvent -->" + "自定义文件评分投票  变更 id= " + id + ", opermethod= " + opermethod);
                    if (opermethod == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_STOP_VALUE) {
                        view.close(id);
                    }
                }
                break;
        }
    }

    public void queryScoreById(int scoreId) {
        try {
            InterfaceFilescorevote.pbui_Type_UserDefineFileScore object = jni.queryScoreById(scoreId);
            if (object == null) {
                return;
            }
            InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore item = object.getItem(0);
            view.update(item);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
