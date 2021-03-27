package xlk.paperless.standard.view.admin.fragment.mid.votemanage;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.bean.SubmitMember;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author Created by xlk on 2020/10/24.
 * @desc
 */
public class AdminVoteManagePresenter extends BasePresenter {
    private final AdminVoteManageInterface view;
    private List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteInfo = new ArrayList<>();
    private List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> memberInfos = new ArrayList<>();
    public List<SubmitMember> submitMembers = new ArrayList<>();
    /**
     * =true查询投票，=false查询选举
     */
    public static boolean isVote = true;

    public AdminVoteManagePresenter(AdminVoteManageInterface view) {
        super();
        this.view = view;
    }

    public void queryVote() {
        try {
            InterfaceVote.pbui_Type_MeetVoteDetailInfo voteDetailInfo = jni.queryVoteByType(isVote
                    ? InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote_VALUE
                    : InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_election_VALUE);
            voteInfo.clear();
            if (voteDetailInfo != null) {
                voteInfo.addAll(voteDetailInfo.getItemList());
            }
            view.updateVoteRv(voteInfo);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> getVoteInfo() {
        return voteInfo;
    }

    public void queryMember() {
        try {
            InterfaceMember.pbui_Type_MeetMemberDetailInfo info = jni.queryAttendPeopleDetailed();
            memberInfos.clear();
            if (info != null) {
                memberInfos.addAll(info.getItemList());
            }
            view.updateMemberRv(memberInfos);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> getMemberInfos() {
        return memberInfos;
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //投票变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTEINFO_VALUE:
                LogUtil.d(TAG, "BusEvent -->" + "投票变更通知");
                queryVote();
                break;
            //参会人员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE:
                //参会人员权限变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBERPERMISSION_VALUE:
                //界面状态变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEMEETSTATUS_VALUE:
                queryMember();
            default:
                break;
        }
    }

    public void launchVote(List<Integer> memberIds, int voteid, int timeouts) {
        jni.launchVote(memberIds, voteid, timeouts);
    }

    public void stopVote(int voteid) {
        jni.stopVote(voteid);
    }

    public void querySubmittedVoters(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote) {
        try {
            InterfaceVote.pbui_Type_MeetVoteSignInDetailInfo info = jni.querySubmittedVoters(vote.getVoteid());
            if (info == null) {
                return;
            }
            submitMembers.clear();
            List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo = vote.getItemList();
            List<InterfaceVote.pbui_Item_MeetVoteSignInDetailInfo> submittedMembers = info.getItemList();
            for (int i = 0; i < submittedMembers.size(); i++) {
                InterfaceVote.pbui_Item_MeetVoteSignInDetailInfo item = submittedMembers.get(i);
                InterfaceMember.pbui_Item_MeetMemberDetailInfo memberInfo = null;
                String chooseText = "";
                for (int j = 0; j < memberInfos.size(); j++) {
                    InterfaceMember.pbui_Item_MeetMemberDetailInfo memebrItem = memberInfos.get(j);
                    LogUtil.i(TAG, "querySubmittedVoters " + memebrItem.getMemberid()
                            + "," + memebrItem.getMembername().toStringUtf8()
                            + ",投票人员id=" + item.getId());
                    if (memebrItem.getMemberid() == item.getId()) {
                        memberInfo = memebrItem;
                        break;
                    }
                }
                if (memberInfo == null) {
                    LogUtil.d(TAG, "querySubmittedVoters -->" + "没有找打提交人名字");
                    break;
                }
                int selcnt = item.getSelcnt();
                //int变量的二进制表示的字符串
                String string = Integer.toBinaryString(selcnt);
                //查找字符串中为1的索引位置
                int length = string.length();
                int selectedItem = 0;
                for (int j = 0; j < length; j++) {
                    char c = string.charAt(j);
                    //将 char 装换成int型整数
                    int a = c - '0';
                    if (a == 1) {
                        //索引从0开始
                        selectedItem = length - j - 1;
                        for (int k = 0; k < optionInfo.size(); k++) {
                            if (k == selectedItem) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(k);
                                String text = voteOptionsInfo.getText().toStringUtf8();
                                if (chooseText.length() == 0) {
                                    chooseText = text;
                                } else {
                                    chooseText += " | " + text;
                                }
                            }
                        }
                    }
                }
                submitMembers.add(new SubmitMember(memberInfo, item, chooseText));
            }
            view.showSubmittedPop(vote);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public String[] queryYd(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote) {
        InterfaceBase.pbui_CommonInt32uProperty yingDaoInfo = jni.queryVoteSubmitterProperty(vote.getVoteid(), 0, InterfaceMacro.Pb_MeetVotePropertyID.Pb_MEETVOTE_PROPERTY_ATTENDNUM.getNumber());
        InterfaceBase.pbui_CommonInt32uProperty yiTouInfo = jni.queryVoteSubmitterProperty(vote.getVoteid(), 0, InterfaceMacro.Pb_MeetVotePropertyID.Pb_MEETVOTE_PROPERTY_VOTEDNUM.getNumber());
        InterfaceBase.pbui_CommonInt32uProperty shiDaoInfo = jni.queryVoteSubmitterProperty(vote.getVoteid(), 0, InterfaceMacro.Pb_MeetVotePropertyID.Pb_MEETVOTE_PROPERTY_CHECKINNUM.getNumber());
        int yingDao = yingDaoInfo == null ? 0 : yingDaoInfo.getPropertyval();
        int yiTou = yiTouInfo == null ? 0 : yiTouInfo.getPropertyval();
        int shiDao = shiDaoInfo == null ? 0 : shiDaoInfo.getPropertyval();
        String yingDaoStr = "应到：" + yingDao + "人 ";
        String shiDaoStr = "实到：" + shiDao + "人 ";
        String yiTouStr = "已投：" + yiTou + "人 ";
        String weiTouStr = "未投：" + (yingDao - yiTou) + "人";
        LogUtil.d(TAG, "queryYd :  应到人数: " + yingDaoStr + "，实到：" + shiDaoStr + ", 已投人数: " + yiTouStr + "， 未投：" + weiTouStr);
        return new String[]{yingDaoStr, shiDaoStr, yiTouStr, weiTouStr};
    }

}
