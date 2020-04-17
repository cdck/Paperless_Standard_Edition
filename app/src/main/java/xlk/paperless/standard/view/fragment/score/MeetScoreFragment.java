package xlk.paperless.standard.view.fragment.score;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceFilescorevote;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.math.BigDecimal;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.MeetScoreAdapter;
import xlk.paperless.standard.adapter.ScoreMemberAdapter;
import xlk.paperless.standard.adapter.WmScreenMemberAdapter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.util.PopUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.MyApplication;

/**
 * @author xlk
 * @date 2020/3/13
 * @Description: 评分查看
 */
public class MeetScoreFragment extends Fragment implements IMeetScore, View.OnClickListener {
    private final String TAG = "MeetScoreFragment-->";
    private RecyclerView f_score_rv;
    private TextView f_score_state;
    private EditText f_score_describe_edt;
    private EditText f_score_file_edt;
    private TextView f_score_register_tv;
    private TextView f_score_yd_tv;
    private TextView f_score_yp_tv;
    private TextView f_score_count_tv;
    private TextView f_score_average_tv;
    private TextView f_score_choose1;
    private TextView f_score_choose2;
    private TextView f_score_choose3;
    private TextView f_score_choose4;
    private EditText f_score_opinion;
    private RecyclerView f_score_member_rv;
    private MeetScorePresenter presenter;
    private MeetScoreAdapter scoreAdapter;
    public static boolean isScoreManage = false;
    private ScoreMemberAdapter scoreMemberAdapter;
    private Button f_score_start, f_score_stop;
    private WmScreenMemberAdapter onlineAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_score, container, false);
        initView(inflate);
        presenter = new MeetScorePresenter(getContext(), this);
        presenter.register();
        initAdapter();
        start();
        return inflate;
    }

    private void initAdapter() {
        onlineAdapter = new WmScreenMemberAdapter(R.layout.item_single_button, presenter.onlineMembers);
    }

    private void start() {
        f_score_start.setVisibility(isScoreManage ? View.VISIBLE : View.GONE);
        f_score_stop.setVisibility(isScoreManage ? View.VISIBLE : View.GONE);
        presenter.queryMember();
        presenter.queryScore();
    }

    private void initView(View inflate) {
        f_score_rv = (RecyclerView) inflate.findViewById(R.id.f_score_rv);
        f_score_state = (TextView) inflate.findViewById(R.id.f_score_state);
        f_score_describe_edt = (EditText) inflate.findViewById(R.id.f_score_describe_edt);
        f_score_file_edt = (EditText) inflate.findViewById(R.id.f_score_file_edt);
        f_score_register_tv = (TextView) inflate.findViewById(R.id.f_score_register_tv);
        f_score_yd_tv = (TextView) inflate.findViewById(R.id.f_score_yd_tv);
        f_score_yp_tv = (TextView) inflate.findViewById(R.id.f_score_yp_tv);
        f_score_count_tv = (TextView) inflate.findViewById(R.id.f_score_count_tv);
        f_score_average_tv = (TextView) inflate.findViewById(R.id.f_score_average_tv);
        f_score_choose1 = (TextView) inflate.findViewById(R.id.f_score_choose1);
        f_score_choose2 = (TextView) inflate.findViewById(R.id.f_score_choose2);
        f_score_choose3 = (TextView) inflate.findViewById(R.id.f_score_choose3);
        f_score_choose4 = (TextView) inflate.findViewById(R.id.f_score_choose4);
        f_score_member_rv = (RecyclerView) inflate.findViewById(R.id.f_score_member_rv);
        f_score_opinion = (EditText) inflate.findViewById(R.id.f_score_opinion);
        f_score_start = (Button) inflate.findViewById(R.id.f_score_start);
        f_score_stop = (Button) inflate.findViewById(R.id.f_score_stop);
        f_score_start.setOnClickListener(this);
        f_score_stop.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unregister();
    }

    @Override
    public void updateOnlineMemberRv() {
        if (onlineAdapter != null) {
            onlineAdapter.notifyDataSetChanged();
            onlineAdapter.notifyChecks();
        }
    }

    @Override
    public void updateScoreRv(List<InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore> scoreInfos) {
        if (scoreAdapter == null) {
            scoreAdapter = new MeetScoreAdapter(R.layout.item_score_list, scoreInfos);
            scoreAdapter.setHasStableIds(true);
            f_score_rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            f_score_rv.setAdapter(scoreAdapter);
        } else {
            scoreAdapter.notifyDataSetChanged();
            scoreAdapter.notifyChoose();
            setDefault(scoreAdapter.getChooseScore());
        }
        scoreAdapter.setOnItemClickListener((adapter, view, position) -> {
            scoreAdapter.setChoose(scoreInfos.get(position).getVoteid());
            updateRight(scoreInfos.get(position));
        });
        if (scoreAdapter.getChooseId() == -1) {
            if (!scoreInfos.isEmpty()) {
                setDefault(scoreInfos.get(0));
            } else {
                setDefault(null);
            }
        }
    }

    private void setDefault(InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore score) {
        if (score != null) {
            scoreAdapter.setChoose(score.getVoteid());
            updateRight(score);
        } else {
            clearRight();
        }
    }

    private void clearRight() {
        f_score_state.setText("");
        f_score_describe_edt.setText("");
        f_score_file_edt.setText("");
        f_score_register_tv.setText("");
        f_score_yd_tv.setText("");
        f_score_yp_tv.setText("");
        f_score_choose1.setText("");
        f_score_choose2.setText("");
        f_score_choose3.setText("");
        f_score_choose4.setText("");
        f_score_count_tv.setText("");
        f_score_average_tv.setText("");
        setOpinion("");
        presenter.querySubmittedVoters(-1);
    }

    private void updateRight(InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore score) {
        f_score_state.setText(getVoteState(score.getVotestate()));
        f_score_describe_edt.setText(score.getContent().toStringUtf8());
        f_score_file_edt.setText(Constant.getFileName(score.getFileid()));
        f_score_register_tv.setText(score.getMode() == InterfaceMacro.Pb_MeetVoteMode.Pb_VOTEMODE_agonymous_VALUE ? getString(R.string.no) : getString(R.string.yes));
        f_score_yd_tv.setText(String.valueOf(score.getShouldmembernum()));
        f_score_yp_tv.setText(String.valueOf(score.getRealmembernum()));
        f_score_choose1.setText(getScore(score, 0));
        f_score_choose2.setText(getScore(score, 1));
        f_score_choose3.setText(getScore(score, 2));
        f_score_choose4.setText(getScore(score, 3));
        double total = getTotal(score);
        f_score_count_tv.setText(String.valueOf(total));
        double average;
        BigDecimal b1 = new BigDecimal(Double.toString(total));
        BigDecimal b2 = new BigDecimal(Double.toString(score.getSelectcount()));
        //默认保留两位会有错误，这里设置保留小数点后4位
        average = b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        f_score_average_tv.setText(String.valueOf(average));
        setOpinion("");
        presenter.querySubmittedVoters(score.getVoteid());
    }

    @Override
    public void updateRightRv() {
        if (scoreMemberAdapter == null) {
            scoreMemberAdapter = new ScoreMemberAdapter(R.layout.item_score_member, presenter.scoreMembers);
            f_score_member_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            f_score_member_rv.setAdapter(scoreMemberAdapter);
        } else {
            scoreMemberAdapter.notifyDataSetChanged();
            scoreAdapter.notifyChoose();
            setOpinion(scoreMemberAdapter.getOpinion());
        }
        scoreMemberAdapter.setOnItemClickListener((adapter, view, position) -> {
            scoreMemberAdapter.choose(presenter.scoreMembers.get(position).getMember().getPersonid());
            setOpinion(scoreMemberAdapter.getOpinion());
        });
    }

    private void setOpinion(String opinion) {
        if (opinion.isEmpty()) {
            f_score_opinion.setText("");
        } else {
            f_score_opinion.setText(getContext().getString(R.string.score_opinion_, opinion));
        }
    }

    private double getTotal(InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore item) {
        double total = 0;
        List<Integer> itemsumscoreList = item.getItemsumscoreList();
        for (int i : itemsumscoreList) {
            total += i;
        }
        return total;
    }

    private String getScore(InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore item, int index) {
        int selectcount = item.getSelectcount();
        if (selectcount < (index + 1)) {//要获取的文本大于有效选项个数
            return "";
        }
        return item.getVoteText(index).toStringUtf8();
    }

    private String getVoteState(int state) {
        if (state == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE) {
            return getString(R.string.state_not_initiated);
        } else if (state == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing_VALUE) {
            return getString(R.string.state_ongoing);
        } else {
            return getString(R.string.state_has_ended);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.f_score_start:
                if (scoreAdapter != null) {
                    InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore chooseScore = scoreAdapter.getChooseScore();
                    if (chooseScore != null) {
                        if (presenter.noVoteingScore()) {
                            if (chooseScore.getVotestate() == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE) {
                                scoreOper(chooseScore.getVoteid());
                            } else {
                                ToastUtil.show(getContext(), R.string.please_choose_notvote_score);
                            }
                        } else {
                            ToastUtil.show(getContext(), R.string.please_stop_voteing_first);
                        }
                    } else {
                        ToastUtil.show(getContext(), R.string.please_choose_notvote_score);
                    }
                }
                break;
            case R.id.f_score_stop:
                if (scoreAdapter != null) {
                    InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore chooseScore = scoreAdapter.getChooseScore();
                    if (chooseScore != null) {
                        if (chooseScore.getVotestate() == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing_VALUE) {
                            JniHandler.getInstance().stopScore(scoreAdapter.getChooseId());
                        } else {
                            ToastUtil.show(getContext(), R.string.please_choose_votein_score);
                        }
                    }
                }
                break;
        }
    }

    private void scoreOper(int voteid) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_online_member, null);
        PopupWindow popupWindow = PopUtil.create(inflate, MyApplication.screen_width / 2, MyApplication.screen_height / 2, true, f_score_start);
        RecyclerView pop_score_rv = inflate.findViewById(R.id.pop_score_rv);
        CheckBox pop_score_check_all = inflate.findViewById(R.id.pop_score_check_all);
        pop_score_rv.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        pop_score_rv.setAdapter(onlineAdapter);
        onlineAdapter.setOnItemClickListener((adapter, view, position) -> {
            onlineAdapter.choose(presenter.onlineMembers.get(position).getDeviceDetailInfo().getDevcieid());
            pop_score_check_all.setChecked(onlineAdapter.isChooseAll());
        });
        pop_score_check_all.setOnClickListener(v -> {
            boolean checked = pop_score_check_all.isChecked();
            pop_score_check_all.setChecked(checked);
            onlineAdapter.setChooseAll(checked);
        });
        inflate.findViewById(R.id.pop_score_start).setOnClickListener(v -> {
            List<Integer> chooseMemberIds = onlineAdapter.getChooseMemberIds();
            if (chooseMemberIds.isEmpty()) {
                ToastUtil.show(getContext(), R.string.please_choose_target_first);
                return;
            }
            JniHandler.getInstance().startScore(voteid, 0, 0, chooseMemberIds);
            popupWindow.dismiss();
        });
        inflate.findViewById(R.id.pop_score_cancel).setOnClickListener(v -> popupWindow.dismiss());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {

        } else {
            start();
        }
        super.onHiddenChanged(hidden);
    }

}
