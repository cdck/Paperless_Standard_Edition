package xlk.paperless.standard.view.admin.fragment.mid.votemanage;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.Spinner;

import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.SubmitMemberAdapter;
import xlk.paperless.standard.adapter.VoteManageAdapter;
import xlk.paperless.standard.adapter.VoteManageMemberAdapter;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.exportbean.ExportSubmitMember;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.JxlUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;

/**
 * @author Created by xlk on 2020/10/24.
 * @desc
 */
public class AdminVoteManageFragment extends BaseFragment implements AdminVoteManageInterface, View.OnClickListener {
    private RecyclerView rv_vote_manage;
    private Spinner sp_time;
    private Button btn_launch_vote;
    private Button btn_end_vote;
    private Button btn_view_details;
    private AdminVoteManagePresenter presenter;
    private VoteManageAdapter voteManageAdapter;
    private PopupWindow memberPop;
    private VoteManageMemberAdapter memberAdapter;
    private RecyclerView pop_vote_rv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_vote_manage, container, false);
        initView(inflate);
        presenter = new AdminVoteManagePresenter(this);
        reShow();
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    private void changeViewUi() {
        if (AdminVoteManagePresenter.isVote) {
            btn_launch_vote.setText(getString(R.string.launch_vote));
            btn_end_vote.setText(getString(R.string.stop_vote));
        } else {
            btn_launch_vote.setText(getString(R.string.launch_election));
            btn_end_vote.setText(getString(R.string.stop_election));
        }
    }

    @Override
    protected void reShow() {
        presenter.queryMember();
        presenter.queryVote();
        changeViewUi();
    }

    public void initView(View rootView) {
        rv_vote_manage = (RecyclerView) rootView.findViewById(R.id.rv_vote_manage);
        sp_time = (Spinner) rootView.findViewById(R.id.sp_time);
        btn_launch_vote = (Button) rootView.findViewById(R.id.btn_launch_vote);
        btn_end_vote = (Button) rootView.findViewById(R.id.btn_end_vote);
        btn_view_details = (Button) rootView.findViewById(R.id.btn_view_details);
        btn_launch_vote.setOnClickListener(this);
        btn_end_vote.setOnClickListener(this);
        btn_view_details.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_launch_vote: {
                if (voteManageAdapter != null && voteManageAdapter.getSelectedVote() != null) {
                    if (voteManageAdapter.getSelectedVote().getVotestate() != InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE) {
                        ToastUtil.show(R.string.please_choose_not_vote);
                        return;
                    }
                    for (int i = 0; i < presenter.getVoteInfo().size(); i++) {
                        InterfaceVote.pbui_Item_MeetVoteDetailInfo info = presenter.getVoteInfo().get(i);
                        if (info.getVotestate() == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing_VALUE) {
                            ToastUtil.show(R.string.please_stop_vote_first);
                            return;
                        }
                    }
                    presenter.queryMember();
                    showMemberPop(presenter.getMemberInfos());
                }
                break;
            }
            case R.id.btn_end_vote: {
                if (voteManageAdapter != null && voteManageAdapter.getSelectedVote() != null) {
                    if (voteManageAdapter.getSelectedVote().getVotestate() != InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing_VALUE) {
                        ToastUtil.show(R.string.only_stop_voteing);
                        return;
                    }
                    presenter.stopVote(voteManageAdapter.getSelectedVote().getVoteid());
                }
                break;
            }
            case R.id.btn_view_details: {
                if (voteManageAdapter != null && voteManageAdapter.getSelectedVote() != null) {
                    InterfaceVote.pbui_Item_MeetVoteDetailInfo selectedVote = voteManageAdapter.getSelectedVote();
                    if (selectedVote.getMode() != InterfaceMacro.Pb_MeetVoteMode.Pb_VOTEMODE_agonymous_VALUE) {
                        if (selectedVote.getVotestate() != InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE) {
                            presenter.querySubmittedVoters(selectedVote);
                        } else {
                            ToastUtil.show(R.string.can_not_choose_notvote);
                        }
                    } else {
                        ToastUtil.show(R.string.please_choose_registered_vote);
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    private void showMemberPop(List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> memberInfo) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_vote_member, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showMemberRole fragment的大小 width=" + width + ",height=" + height);
        memberPop = new PopupWindow(inflate, width, height);
        memberPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        memberPop.setTouchable(true);
        // true:设置触摸外面时消失
        memberPop.setOutsideTouchable(true);
        memberPop.setFocusable(true);
        memberPop.setAnimationStyle(R.style.pop_Animation);
        memberPop.showAtLocation(btn_launch_vote, Gravity.END | Gravity.BOTTOM, 0, 0);

        CheckBox pop_vote_all = inflate.findViewById(R.id.pop_vote_all);
        pop_vote_rv = inflate.findViewById(R.id.pop_vote_rv);
        memberAdapter = new VoteManageMemberAdapter(R.layout.item_vote_manage_member, memberInfo);
        pop_vote_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        pop_vote_rv.setAdapter(memberAdapter);
        memberAdapter.setOnItemClickListener((adapter, view, position) -> {
            memberAdapter.setChoose(memberInfo.get(position).getMemberid());
            pop_vote_all.setChecked(memberAdapter.isChooseAll());
        });
        pop_vote_all.setOnClickListener(v -> {
            boolean checked = pop_vote_all.isChecked();
            pop_vote_all.setChecked(checked);
            memberAdapter.setChooseAll(checked);
        });
        inflate.findViewById(R.id.pop_vote_determine).setOnClickListener(v -> {
            List<Integer> memberIds = memberAdapter.getChoose();
            if (memberIds.isEmpty()) {
                ToastUtil.show(R.string.please_choose_member);
                return;
            }
            InterfaceVote.pbui_Item_MeetVoteDetailInfo selectedVote = voteManageAdapter.getSelectedVote();
            if (selectedVote != null && selectedVote.getVotestate() == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE) {
                int voteid = selectedVote.getVoteid();
                int timeouts = getTimeouts();
                presenter.launchVote(memberIds, voteid, timeouts);
            } else {
                ToastUtil.show(R.string.vote_changed);
            }
            memberPop.dismiss();
        });
        inflate.findViewById(R.id.pop_vote_cancel).setOnClickListener(v -> {
            memberPop.dismiss();
        });
    }

    private int getTimeouts() {
        int position = sp_time.getSelectedItemPosition();
        int timeouts = 0;
        switch (position) {
            case 0:
                timeouts = 10;
                break;
            case 1:
                timeouts = 30;
                break;
            case 2:
                timeouts = 60;
                break;
            case 3:
                timeouts = 120;
                break;
            case 4:
                timeouts = 300;
                break;
            case 5:
                timeouts = 900;
                break;
            case 6:
                timeouts = 1800;
                break;
            case 7:
                timeouts = 36000;
                break;
            default:
                break;
        }
        return timeouts;
    }

    @Override
    public void updateVoteRv(List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteInfo) {
        if (voteManageAdapter == null) {
            voteManageAdapter = new VoteManageAdapter(R.layout.item_vote_manage, voteInfo);
            rv_vote_manage.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_vote_manage.setAdapter(voteManageAdapter);
        } else {
            voteManageAdapter.notifyDataSetChanged();
        }
        voteManageAdapter.setOnItemClickListener((adapter, view, position) -> {
            InterfaceVote.pbui_Item_MeetVoteDetailInfo item = voteInfo.get(position);
            voteManageAdapter.setSelect(item.getVoteid());
            int timeouts = item.getTimeouts();
            if (timeouts <= 10) {
                position = 0;
            } else if (timeouts <= 30) {
                position = 1;
            } else if (timeouts <= 60) {
                position = 2;
            } else if (timeouts <= 120) {
                position = 3;
            } else if (timeouts <= 300) {
                position = 4;
            } else if (timeouts <= 900) {
                position = 5;
            } else if (timeouts <= 1800) {
                position = 6;
            } else {
                position = 7;
            }
            sp_time.setSelection(position);
        });
    }

    @Override
    public void updateMemberRv(List<InterfaceMember.pbui_Item_MeetMemberDetailInfo> memberInfo) {
        if (memberPop != null && memberPop.isShowing()) {
            memberAdapter.notifyDataSetChanged();
            memberAdapter.notifyChoose();
        }
    }

    @Override
    public void showSubmittedPop(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_submitted_member, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showSubmittedPop fragment的大小 width=" + width + ",height=" + height);
        PopupWindow popupWindow = new PopupWindow(inflate, width, height);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        popupWindow.setTouchable(true);
        // true:设置触摸外面时消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.pop_Animation);
        popupWindow.showAtLocation(btn_view_details, Gravity.END | Gravity.BOTTOM, 0, 0);
        SubmitMemberAdapter adapter = new SubmitMemberAdapter(R.layout.item_submit_member, presenter.submitMembers);
        RecyclerView submit_member_rv = inflate.findViewById(R.id.submit_member_rv);
        submit_member_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        submit_member_rv.setAdapter(adapter);
        inflate.findViewById(R.id.submit_member_back).setOnClickListener(v -> popupWindow.dismiss());
        inflate.findViewById(R.id.submit_member_export).setOnClickListener(v -> {
            String[] strings = presenter.queryYd(vote);
            String createTime = DateUtil.nowDate();
            ExportSubmitMember exportSubmitMember = new ExportSubmitMember(vote.getContent().toStringUtf8(), createTime, strings[0], strings[1], strings[2], strings[3], presenter.submitMembers);
            JxlUtil.exportSubmitMember(exportSubmitMember);
        });
    }
}
