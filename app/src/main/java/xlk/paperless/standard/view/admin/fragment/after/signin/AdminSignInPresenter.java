package xlk.paperless.standard.view.admin.fragment.after.signin;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceContext;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.mogujie.tt.protobuf.InterfaceSignin;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.admin.fragment.after.archive.PdfSignBean;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc
 */
public class AdminSignInPresenter extends BasePresenter {
    private final AdminSignInInterface view;
    List<SignInBean> signInBeans = new ArrayList<>();
    private InterfaceMeet.pbui_Item_MeetMeetInfo currentMeetInfo;
    private InterfaceRoom.pbui_Item_MeetRoomDetailInfo currentRoomInfo;
    private int signInCount;

    public AdminSignInPresenter(AdminSignInInterface view) {
        super();
        this.view = view;
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //参会人员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE:
                LogUtil.i(TAG, "busEvent 参会人员变更通知");
                queryAttendPeople();
                break;
            //签到变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN_VALUE:
                LogUtil.i(TAG, "busEvent 签到变更通知");
                querySignIn();
                break;
            //会议信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO_VALUE: {
                queryMeetById();
                break;
            }
            //会场信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOM_VALUE: {
                LogUtil.i(TAG, "BusEvent 会场信息变更通知");
                queryRoom();
                break;
            }
            default:
                break;
        }
    }

    /**
     * 导出成PDF需要的数据
     */
    public PdfSignBean getPdfData() {
        return new PdfSignBean(currentMeetInfo, currentRoomInfo, signInBeans, signInCount);
    }

    private void queryMeetById() {
        try {
            InterfaceMeet.pbui_Item_MeetMeetInfo info = jni.queryMeetFromId(queryCurrentMeetId());
            currentMeetInfo = info;
            queryRoom();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryRoom() {
        InterfaceRoom.pbui_Item_MeetRoomDetailInfo room = jni.queryRoomById(queryCurrentRoomId());
        currentRoomInfo = room;
    }

    public void queryAttendPeople() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo info = jni.queryAttendPeople();
            signInBeans.clear();
            if (info != null) {
                List<InterfaceMember.pbui_Item_MemberDetailInfo> itemList = info.getItemList();
                for (int i = 0; i < itemList.size(); i++) {
                    signInBeans.add(new SignInBean(itemList.get(i)));
                }
            }
            querySignIn();
            queryMeetById();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void querySignIn() {
        try {
            InterfaceSignin.pbui_Type_MeetSignInDetailInfo info = jni.querySignin();
            //已经签到的人数
            signInCount = 0;
            if (info != null) {
                List<InterfaceSignin.pbui_Item_MeetSignInDetailInfo> itemList = info.getItemList();
                for (int i = 0; i < signInBeans.size(); i++) {
                    SignInBean bean = signInBeans.get(i);
                    //删除签到信息后从变更通知处查询，需要先置空
                    bean.setSign(null);
                    for (int j = 0; j < itemList.size(); j++) {
                        InterfaceSignin.pbui_Item_MeetSignInDetailInfo item = itemList.get(j);
                        if (item.getNameId() == bean.getMember().getPersonid()) {
                            bean.setSign(item);
                            if (item.getUtcseconds() > 0) {
                                signInCount++;
                            }
                            break;
                        }
                    }
                }
            } else {
                //变更通知处查询，没有任何签到信息需要置空
                for (int i = 0; i < signInBeans.size(); i++) {
                    signInBeans.get(i).setSign(null);
                }
            }
            view.update(signInBeans, signInCount);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public int getCurrentMeetingId() {
        InterfaceContext.pbui_MeetContextInfo info = jni.queryContextProperty(
                InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_CURMEETINGID_VALUE);
        return info.getPropertyval();
    }

    public void deleteSignIn(List<Integer> memberIds) {
        jni.deleteSignIn(getCurrentMeetingId(), memberIds);
    }
}
