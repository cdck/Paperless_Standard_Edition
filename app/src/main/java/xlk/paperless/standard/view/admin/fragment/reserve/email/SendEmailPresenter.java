package xlk.paperless.standard.view.admin.fragment.reserve.email;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author Created by xlk on 2020/11/14.
 * @desc
 */
public class SendEmailPresenter extends BasePresenter {
    private final SendEmailInterface view;
    public List<InterfaceMember.pbui_Item_MemberDetailInfo> members = new ArrayList<>();
    private InterfaceMeet.pbui_Item_MeetMeetInfo currentMeet;

    public SendEmailPresenter(SendEmailInterface view) {
        super();
        this.view = view;
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //参会人员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE: {
                LogUtil.i(TAG, "BusEvent -->" + "参会人员变更通知");
                queryMember();
                break;
            }
            //会议信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO_VALUE: {
                LogUtil.i(TAG, "busEvent 会议信息变更通知");
                queryCurrentMeet();
                break;
            }
            default:
                break;
        }
    }

    public void queryCurrentMeet() {
        InterfaceMeet.pbui_Type_MeetMeetInfo info = jni.queryMeetingById(queryCurrentMeetId());
        if (info != null) {
            currentMeet = info.getItem(0);
        }
        view.updateMeetName(currentMeet);
    }

    public void queryMember() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo pbui_type_memberDetailInfo = jni.queryAttendPeople();
            members.clear();
            if (pbui_type_memberDetailInfo != null) {
                members.addAll(pbui_type_memberDetailInfo.getItemList());
            }
            view.updateMember();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
