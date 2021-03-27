package xlk.paperless.standard.view.fragment.score;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFilescorevote;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.bean.DevMember;
import xlk.paperless.standard.data.bean.ScoreMember;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.base.BasePresenter;

import static com.mogujie.tt.protobuf.InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE;

/**
 * @author xlk
 * @date 2020/3/20
 * @desc
 */
public class MeetScorePresenter extends BasePresenter {
    private final String TAG = "MeetScorePresenter-->";
    private final IMeetScore view;
    private final Context cxt;
    private List<InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore> scoreInfos = new ArrayList<>();
    private List<InterfaceFilescorevote.pbui_Type_Item_FileScoreMemberStatistic> scoreMemberStatistics = new ArrayList<>();
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos = new ArrayList<>();
    private List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceInfos = new ArrayList<>();
    public List<ScoreMember> scoreMembers = new ArrayList<>();
    public List<DevMember> onlineMembers = new ArrayList<>();

    public MeetScorePresenter(Context cxt, IMeetScore view) {
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
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FILESCOREVOTE_VALUE:
                if (msg.getMethod() == Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    LogUtil.d(TAG, "BusEvent -->" + "投票变更通知");
                    queryScore();
                }
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
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FILESCOREVOTESIGN_VALUE://会议评分
                byte[] data = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(data);
                int id = pbui_meetNotifyMsg.getId();
                int opermethod = pbui_meetNotifyMsg.getOpermethod();
                LogUtil.d(TAG, "BusEvent -->" + "会议评分变更通知 id= " + id + ", opermethod= " + opermethod);
                if (opermethod == 1) {
                    querySubmittedVoters(id);
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
            memberInfos.clear();
            memberInfos.addAll(attendPeople.getItemList());
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
            deviceInfos.clear();
            deviceInfos.addAll(deviceDetailInfo.getPdevList());
            onlineMembers.clear();
            for (int i = 0; i < deviceInfos.size(); i++) {
                InterfaceDevice.pbui_Item_DeviceDetailInfo detailInfo = deviceInfos.get(i);
                int memberid = detailInfo.getMemberid();
                int netstate = detailInfo.getNetstate();
                int facestate = detailInfo.getFacestate();
                if (netstate == 1 && facestate == 1) {
                    for (int j = 0; j < memberInfos.size(); j++) {
                        InterfaceMember.pbui_Item_MemberDetailInfo member = memberInfos.get(j);
                        if (member.getPersonid() == memberid) {
                            onlineMembers.add(new DevMember(detailInfo, member));
                            break;
                        }
                    }
                }
            }
            view.updateOnlineMemberRv();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryScore() {
        InterfaceFilescorevote.pbui_Type_UserDefineFileScore fileScore = jni.queryScoreFile();
        scoreInfos.clear();
        if (fileScore != null) {
            scoreInfos.addAll(fileScore.getItemList());
        }
        view.updateScoreRv(scoreInfos);
    }

    public void querySubmittedVoters(int voteid) {
        try {
            InterfaceFilescorevote.pbui_Type_UserDefineFileScoreMemberStatistic info = jni.queryScoreSubmittedScore(voteid);
            if (info == null) {
                LogUtil.d(TAG, "querySubmittedVoters -->查询指定评分提交人失败 " + memberInfos.size() + ", voteid= " + voteid);
                scoreMembers.clear();
                view.updateRightRv();
                return;
            }
            scoreMemberStatistics.clear();
            scoreMemberStatistics.addAll(info.getItemList());
            scoreMembers.clear();
            for (int i = 0; i < scoreMemberStatistics.size(); i++) {
                InterfaceFilescorevote.pbui_Type_Item_FileScoreMemberStatistic item = scoreMemberStatistics.get(i);
                for (int j = 0; j < memberInfos.size(); j++) {
                    InterfaceMember.pbui_Item_MemberDetailInfo member = memberInfos.get(j);
                    if (member.getPersonid() == item.getMemberid()) {
                        scoreMembers.add(new ScoreMember(member, item));
                        break;
                    }
                }
            }
            view.updateRightRv();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    //判断是否没有正在进行的评分
    public boolean noVoteingScore() {
        for (int i = 0; i < scoreInfos.size(); i++) {
            InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore info = scoreInfos.get(i);
            if (info.getVotestate() == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing_VALUE) {
                return false;
            }
        }
        return true;
    }
}
