package xlk.paperless.standard.view.admin.fragment.pre.member;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfacePerson;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author Created by xlk on 2020/10/17.
 * @desc
 */
public class MemberPresenter extends BasePresenter {
    private final Context context;
    private final MemberInterface view;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos = new ArrayList<>();
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> sortMemberInfos = new ArrayList<>();
    /**
     * 接口获取的参会人权限数据
     */
    List<MemberPermissionBean> memberPermissionBeans = new ArrayList<>();
    /**
     * 调用打开PopupWindow时复制的参会人权限数据,有变更时不会进行更新
     */
    List<MemberPermissionBean> showMemberPermissionBeans = new ArrayList<>();
    /**
     * 所有的常用人员信息
     */
    private List<InterfacePerson.pbui_Item_PersonDetailInfo> frequentlyMembers = new ArrayList<>();
    private List<MemberRoleBean> devSeatInfos = new ArrayList<>();

    public MemberPresenter(Context context, MemberInterface view) {
        super();
        this.context = context;
        this.view = view;
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //参会人员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE: {
                LogUtil.i(TAG, "BusEvent -->" + "参会人员变更通知");
                queryAttendPeople();
                break;
            }
            //常用人员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_PEOPLE_VALUE: {
                LogUtil.i(TAG, "BusEvent -->" + "常用人员变更通知");
                queryFrequentlyMember();
                break;
            }
            //会议排位变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT_VALUE: {
                LogUtil.i(TAG, "busEvent " + "会议排位变更通知");
                queryPlaceRanking();
                break;
            }
            default:
                break;
        }
    }

    /**
     * 查询常用人员
     */
    public void queryFrequentlyMember() {
        InterfacePerson.pbui_Type_PersonDetailInfo info = jni.queryFrequentlyMember();
        frequentlyMembers.clear();
        if (info != null) {
            frequentlyMembers.addAll(info.getItemList());
        }
        view.updateFrequentlyMemberRv();
    }

    public List<InterfacePerson.pbui_Item_PersonDetailInfo> getFrequentlyMembers() {
        return frequentlyMembers;
    }

    /**
     * 查询参会人员
     */
    public void queryAttendPeople() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo info = jni.queryAttendPeople();
            memberInfos.clear();
            devSeatInfos.clear();
            if (info != null) {
                for (int i = 0; i < info.getItemList().size(); i++) {
                    InterfaceMember.pbui_Item_MemberDetailInfo member = info.getItemList().get(i);
                    memberInfos.add(member);
                    devSeatInfos.add(new MemberRoleBean(member));
                }
            }
            view.updateMemberRv(memberInfos);
            queryAttendPeoplePermission();
            queryPlaceRanking();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * 会场设备排位详细信息
     */
    public void queryPlaceRanking() {
        try {
            InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo info = jni.placeDeviceRankingInfo(queryCurrentRoomId());
            if (info != null) {
                for (int i = 0; i < devSeatInfos.size(); i++) {
                    MemberRoleBean bean = devSeatInfos.get(i);
                    for (int j = 0; j < info.getItemList().size(); j++) {
                        InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item = info.getItemList().get(j);
                        if (item.getMemberid() == bean.getMember().getPersonid()) {
                            bean.setSeat(item);
                            break;
                        }
                    }
                }
            }
            view.updateMemberRole();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public List<MemberRoleBean> getDevSeatInfos() {
        return devSeatInfos;
    }

    public void queryAttendPeoplePermission() {
        try {
            InterfaceMember.pbui_Type_MemberPermission permission = jni.queryAttendPeoplePermissions();
            memberPermissionBeans.clear();
            if (permission != null) {
                List<InterfaceMember.pbui_Item_MemberPermission> itemList = permission.getItemList();
                //按照memberInfos的顺序进行添加
                for (int i = 0; i < memberInfos.size(); i++) {
                    InterfaceMember.pbui_Item_MemberDetailInfo member = memberInfos.get(i);
                    for (int j = 0; j < itemList.size(); j++) {
                        InterfaceMember.pbui_Item_MemberPermission item = itemList.get(j);
                        if (member.getPersonid() == item.getMemberid()) {
                            memberPermissionBeans.add(new MemberPermissionBean(item.getMemberid(), item.getPermission(), member.getName().toStringUtf8()));
                            break;
                        }
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void createMember(InterfaceMember.pbui_Item_MemberDetailInfo build) {
        jni.createMember(build);
    }

    public void createMultipleMember(List<InterfaceMember.pbui_Item_MemberDetailInfo> build) {
        jni.createMultipleMember(build);
    }

    public void modifyMember(InterfaceMember.pbui_Item_MemberDetailInfo build) {
        jni.modifyFrequentlyMember(build);
    }

    public void modifyMemberSort(List<Integer> memberIds) {
        jni.modifyMemberSort(memberIds);
    }

    public void delMember(InterfaceMember.pbui_Item_MemberDetailInfo build) {
        jni.delMember(build);
    }

    public void savePermission() {
        List<InterfaceMember.pbui_Item_MemberPermission> temps = new ArrayList<>();
        for (int i = 0; i < showMemberPermissionBeans.size(); i++) {
            MemberPermissionBean item = showMemberPermissionBeans.get(i);
            InterfaceMember.pbui_Item_MemberPermission build = InterfaceMember.pbui_Item_MemberPermission.newBuilder()
                    .setMemberid(item.getMemberId())
                    .setPermission(item.getPermission())
                    .build();
            temps.add(build);
        }
        jni.saveAttendPeoplePermissions(temps);
    }

    public List<InterfaceMember.pbui_Item_MemberDetailInfo> getMembers() {
        sortMemberInfos.clear();
        sortMemberInfos.addAll(memberInfos);
        return sortMemberInfos;
    }

    public List<MemberPermissionBean> getMemberPermissions() {
        showMemberPermissionBeans.clear();
        showMemberPermissionBeans.addAll(memberPermissionBeans);
        return showMemberPermissionBeans;
    }

    public void createFrequentlyMember(InterfacePerson.pbui_Item_PersonDetailInfo build) {
        jni.addFrequentlyMember(build);
    }

    public void modifyMemberRole(int memberid, int newRole, int devid) {
        jni.modifyMeetRanking(memberid, newRole, devid);
    }
}
