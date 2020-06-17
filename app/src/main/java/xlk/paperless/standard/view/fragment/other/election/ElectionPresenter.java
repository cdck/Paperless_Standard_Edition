package xlk.paperless.standard.view.fragment.other.election;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceVote;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.bean.SubmitMember;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.BasePresenter;


/**
 * @author xlk
 * @date 2020/4/7
 * @desc
 */
public class ElectionPresenter extends BasePresenter {
    private final String TAG = "ElectionPresenter-->";
    private final IElectionManage view;
    private final Context cxt;
    public List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteInfos = new ArrayList<>();
    public List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> memberInfos = new ArrayList<>();
    public List<SubmitMember> submitMembers = new ArrayList<>();

    public ElectionPresenter(Context context, IElectionManage view) {
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
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTEINFO_VALUE://投票变更通知
                LogUtil.d(TAG, "BusEvent -->" + "投票变更通知");
                queryVote();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE://参会人员变更通知
                LogUtil.d(TAG, "BusEvent -->" + "参会人员变更通知");
                queryMember();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT_VALUE://会议排位变更通知
                LogUtil.d(TAG, "BusEvent -->" + "会议排位变更通知");
                queryMember();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEMEETSTATUS_VALUE://界面状态变更通知
                LogUtil.d(TAG, "BusEvent -->" + "界面状态变更通知");
                queryMember();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBERPERMISSION_VALUE://参会人员权限变更通知
                LogUtil.d(TAG, "BusEvent -->" + "参会人员权限变更通知");
                queryMember();
                break;
        }
    }

    public void queryVote() {
        try {
            InterfaceVote.pbui_Type_MeetVoteDetailInfo voteDetailInfo = jni.queryVoteByType(InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_election_VALUE);
            voteInfos.clear();
            if (voteDetailInfo != null) {
                voteInfos.addAll(voteDetailInfo.getItemList());
            }
            view.updateRv();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryMember() {
        try {
            InterfaceMember.pbui_Type_MeetMemberDetailInfo info = jni.queryAttendPeopleDetailed();
            memberInfos.clear();
            if (info != null) {
                memberInfos.addAll(info.getItemList());
            }
            view.updateMemberRv();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void createVote(InterfaceVote.pbui_Item_MeetOnVotingDetailInfo vote) {
        jni.createVote(vote);
    }

    public void modifyVote(InterfaceVote.pbui_Item_MeetOnVotingDetailInfo vote) {
        jni.modifyVote(vote);
    }

    public void deleteVote(int voteId) {
        jni.deleteVote(voteId);
    }

    public void launchVote(List<Integer> memberIds, int voteid, int timeouts) {
        jni.launchVote(memberIds, voteid, timeouts);
    }

    public void stopVote(int voteid) {
        jni.stopVote(voteid);
    }


    public void querySubmittedVoters(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote, boolean isDetails) {
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
                    if (memberInfos.get(j).getMemberid() == item.getId()) {
                        memberInfo = memberInfos.get(j);
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
                        selectedItem = length - j - 1;//索引从0开始
                        for (int k = 0; k < optionInfo.size(); k++) {
                            if (k == selectedItem) {
                                InterfaceVote.pbui_SubItem_VoteItemInfo voteOptionsInfo = optionInfo.get(k);
                                String text = voteOptionsInfo.getText().toStringUtf8();
                                if (chooseText.length() == 0) chooseText = text;
                                else chooseText += " | " + text;
                            }
                        }
                    }
                }
                submitMembers.add(new SubmitMember(memberInfo, item, chooseText));
            }
            if (isDetails) {
                view.showSubmittedPop(vote);
            } else {
                view.showChartPop(vote);
            }
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
        String yingDaoStr = "应到："+yingDao + "人 ";
        String shiDaoStr = "实到："+shiDao + "人 ";
        String yiTouStr = "已投："+yiTou + "人 ";
        String weiTouStr = "未投："+(yingDao - yiTou) + "人";
        LogUtil.d(TAG, "queryYd :  应到人数: " + yingDaoStr + "，实到：" + shiDaoStr + ", 已投人数: " + yiTouStr + "， 未投：" + weiTouStr);
        return new String[]{yingDaoStr, shiDaoStr, yiTouStr, weiTouStr};
    }

    public String getType(int type) {
        if (type == 0) {
            return cxt.getString(R.string.type_multi);
        } else if (type == 1) {
            return cxt.getString(R.string.type_single);
        } else if (type == 2) {
            return cxt.getString(R.string.type_4_5);
        } else if (type == 3) {
            return cxt.getString(R.string.type_3_5);
        } else if (type == 4) {
            return cxt.getString(R.string.type_2_5);
        } else if (type == 5) {
            return cxt.getString(R.string.type_2_3);
        }
        return "";
    }

    public String getVoteState(int votestate) {
        switch (votestate) {
            case InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE:
                return cxt.getString(R.string.state_not_initiated);
            case InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing_VALUE:
                return cxt.getString(R.string.state_ongoing);
            case InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_endvote_VALUE:
                return cxt.getString(R.string.state_has_ended);
        }
        return "";
    }
}
