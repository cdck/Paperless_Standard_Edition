package xlk.paperless.standard.view.fragment.chat;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceIM;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.ChatMessage;
import xlk.paperless.standard.data.bean.DevMember;
import xlk.paperless.standard.base.BasePresenter;

import static xlk.paperless.standard.view.meet.MeetingActivity.chatIsShowing;
import static xlk.paperless.standard.view.meet.MeetingActivity.chatMessages;

/**
 * @author xlk
 * @date 2020/3/17
 * @desc
 */
public class MeetChatPresenter extends BasePresenter {
    private final String TAG = "MeetChatPresenter-->";
    private JniHandler jni = JniHandler.getInstance();
    private final Context cxt;
    private final IMeetChat view;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberDetailInfos = new ArrayList<>();
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceDetailInfos = new ArrayList<>();
    private List<DevMember> mChatonLineMember = new ArrayList<>();


    public MeetChatPresenter(Context cxt, IMeetChat view) {
        super();
        this.cxt = cxt;
        this.view = view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETIM_VALUE://会议交流
                if (chatIsShowing) {
                    byte[] o = (byte[]) msg.getObjects()[0];
                    InterfaceIM.pbui_Type_MeetIM meetIM = InterfaceIM.pbui_Type_MeetIM.parseFrom(o);
                    if (meetIM.getMsgtype() == 0) {//文本类消息
                        chatMessages.add(new ChatMessage(0, meetIM));
                        view.updateMessageRv();
                    }
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEMEETSTATUS_VALUE://界面状态变更通知
                int o = (int) msg.getObjects()[1];
                if (o > 0) {
                    queryMember();
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE://参会人变更通知
                queryMember();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO_VALUE://设备寄存器变更通知
                int o1 = (int) msg.getObjects()[1];
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE
                        && o1 > 0) {
                    queryMember();
                }
                break;
        }
    }

    public void queryMember() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo attendPeople = jni.queryAttendPeople();
            if (attendPeople == null) {
                return;
            }
            memberDetailInfos.clear();
            memberDetailInfos.addAll(attendPeople.getItemList());
            queryDevice();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryDevice() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = jni.queryDeviceInfo();
            if (deviceDetailInfo == null) {
                return;
            }
            deviceDetailInfos.clear();
            deviceDetailInfos.addAll(deviceDetailInfo.getPdevList());
            mChatonLineMember.clear();
            for (int i = 0; i < deviceDetailInfos.size(); i++) {
                InterfaceDevice.pbui_Item_DeviceDetailInfo detailInfo = deviceDetailInfos.get(i);
                int devcieid = detailInfo.getDevcieid();
                int memberid = detailInfo.getMemberid();
                int facestate = detailInfo.getFacestate();
                int netstate = detailInfo.getNetstate();
                if (facestate == 1 && netstate == 1 && devcieid != Values.localDeviceId) {
                    for (int j = 0; j < memberDetailInfos.size(); j++) {
                        InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo = memberDetailInfos.get(j);
                        int personid = memberDetailInfo.getPersonid();
                        if (personid == memberid) {
                            mChatonLineMember.add(new DevMember(detailInfo, memberDetailInfo));
                        }
                    }
                }
            }
            view.updateMemberRv(mChatonLineMember);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void sendChatMessage(String msg, int number, List<Integer> ids) {
        jni.sendChatMessage(msg,number,ids);
    }
}
