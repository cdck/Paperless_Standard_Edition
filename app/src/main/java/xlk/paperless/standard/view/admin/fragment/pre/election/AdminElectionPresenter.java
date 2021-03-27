package xlk.paperless.standard.view.admin.fragment.pre.election;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author Created by xlk on 2020/10/23.
 * @desc
 */
public class AdminElectionPresenter extends BasePresenter {
    private final AdminElectionInterface view;
    private List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> electionInfo = new ArrayList<>();

    public AdminElectionPresenter(AdminElectionInterface view) {
        super();
        this.view = view;
    }

    public void queryElection() {
        try {
            InterfaceVote.pbui_Type_MeetVoteDetailInfo info = jni.queryVote();
            electionInfo.clear();
            if (info != null) {
                List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> itemList = info.getItemList();
                for (int i = 0; i < itemList.size(); i++) {
                    InterfaceVote.pbui_Item_MeetVoteDetailInfo item = itemList.get(i);
                    if (item.getMaintype() == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_election_VALUE) {
                        electionInfo.add(item);
                    }
                }
            }
            view.updateElectionRv(electionInfo);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> getElectionInfo() {
        return electionInfo;
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //投票变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTEINFO_VALUE:
                LogUtil.i(TAG, "busEvent 投票变更通知");
                queryElection();
                break;
            default:
                break;
        }
    }
}
