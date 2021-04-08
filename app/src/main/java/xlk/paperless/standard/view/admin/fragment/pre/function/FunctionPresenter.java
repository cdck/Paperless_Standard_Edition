package xlk.paperless.standard.view.admin.fragment.pre.function;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeetfunction;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author Created by xlk on 2020/10/24.
 * @desc
 */
public class FunctionPresenter extends BasePresenter {
    private final FunctionInterface view;
    private List<FunctionBean> allMeetFunction = new ArrayList<>();
    private List<FunctionBean> meetFunction = new ArrayList<>();
    private List<FunctionBean> hideMeetFunction = new ArrayList<>();

    public FunctionPresenter(FunctionInterface view) {
        super();
        this.view = view;
        initAllFunction();
    }

    private void initAllFunction() {
        for (int i = 0; i < 8; i++) {
            FunctionBean item = new FunctionBean(i);
            switch (i) {
                case 0:
                    item.setFuncode(InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_AGENDA_BULLETIN_VALUE);
                    break;
                case 1:
                    item.setFuncode(InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_MATERIAL_VALUE);
                    break;
                case 2:
                    item.setFuncode(InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_POSTIL_VALUE);
                    break;
                case 3:
                    item.setFuncode(InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_MESSAGE_VALUE);
                    break;
                case 4:
                    item.setFuncode(InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_VIDEOSTREAM_VALUE);
                    break;
                case 5:
                    item.setFuncode(InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_WHITEBOARD_VALUE);
                    break;
                case 6:
                    item.setFuncode(InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_WEBBROWSER_VALUE);
                    break;
                case 7:
                    item.setFuncode(InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_SIGNINRESULT_VALUE);
                    break;
                default:
                    break;
            }
            allMeetFunction.add(item);
        }
    }

    public void queryFunction() {
        try {
            InterfaceMeetfunction.pbui_Type_MeetFunConfigDetailInfo info = jni.queryMeetFunction();
            meetFunction.clear();
            hideMeetFunction.clear();
            if (info != null) {
                for (int i = 0; i < info.getItemList().size(); i++) {
                    InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo item = info.getItemList().get(i);
                    meetFunction.add(new FunctionBean(item.getFuncode(), item.getPosition()));
                }
            }
            for (int i = 0; i < allMeetFunction.size(); i++) {
                FunctionBean item = allMeetFunction.get(i);
                boolean isHide = true;
                for (int j = 0; j < meetFunction.size(); j++) {
                    if (meetFunction.get(j).getFuncode() == item.getFuncode()) {
                        isHide = false;
                        break;
                    }
                }
                LogUtil.e(TAG, "queryFunction isHide=" + isHide);
                if (isHide) {
                    item.setPosition(hideMeetFunction.size());
                    hideMeetFunction.add(item);
                }
            }
            view.updateFunctionRv(meetFunction, hideMeetFunction);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public List<FunctionBean> getMeetFunction() {
        return meetFunction;
    }

    public List<FunctionBean> getHideMeetFunction() {
        return hideMeetFunction;
    }


    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //会议功能变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FUNCONFIG_VALUE:
                LogUtil.i(TAG, "busEvent 会议功能变更通知");
                queryFunction();
                break;
            default:
                break;
        }
    }
}
