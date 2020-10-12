package xlk.paperless.standard.view.fragment.other.vote;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.intrusoft.scatter.ChartData;
import com.intrusoft.scatter.PieChart;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.SubmitMemberAdapter;
import xlk.paperless.standard.adapter.VoteManageAdapter;
import xlk.paperless.standard.adapter.VoteManageMemberAdapter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.exportbean.ExportSubmitMember;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.JxlUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.util.UriUtil;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.view.meet.MeetingActivity;

import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author xlk
 * @date 2020/4/2
 * @desc:
 */
public class VoteManageFragment extends BaseFragment implements View.OnClickListener, IVoteManage {
    private final String TAG = "VoteManageFragment-->";
    private RecyclerView vote_manage_rv;
    private EditText vote_manage_title;
    private CheckBox vote_manage_register;
    private Button vote_manage_add;
    private Button vote_manage_modify;
    private Button vote_manage_delete;
    private Button vote_manage_export;
    private Button vote_manage_import;
    private Spinner vote_manage_time_sp;
    private Button vote_manage_details;
    private Button vote_manage_chart;
    private Button vote_manage_launch;
    private Button vote_manage_stop;
    private VoteManagePresenter presenter;
    private VoteManageAdapter voteManageAdapter;
    private ArrayAdapter<String> spAdapter;
    private VoteManageMemberAdapter memberAdapter;
    private List<ChartData> chartDatas = new ArrayList<>();
    private PopupWindow chartPop;
    private PopupWindow memberPop;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_vote_manage, container, false);
        initView(inflate);
        presenter = new VoteManagePresenter(getContext(), this);
        String[] stringArray = getResources().getStringArray(R.array.countdown_spinner);
        spAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, stringArray);
        vote_manage_time_sp.setAdapter(spAdapter);
        presenter.queryVote();
        presenter.querySecretary();
//        presenter.queryMember();
        return inflate;
    }

    @Override
    public void updateRv() {
        if (voteManageAdapter == null) {
            voteManageAdapter = new VoteManageAdapter(R.layout.item_vote_manage, presenter.voteInfos);
            vote_manage_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            vote_manage_rv.setAdapter(voteManageAdapter);
        } else {
            voteManageAdapter.notifyDataSetChanged();
            voteManageAdapter.notitySelect();
        }
        voteManageAdapter.setOnItemClickListener((adapter, view, position) -> {
            InterfaceVote.pbui_Item_MeetVoteDetailInfo voteInfo = presenter.voteInfos.get(position);
            voteManageAdapter.setSelect(voteInfo);
            updateUI(voteInfo);
        });
        if (voteManageAdapter.getSelectedVote() == null) {
            if (!presenter.voteInfos.isEmpty()) {
                InterfaceVote.pbui_Item_MeetVoteDetailInfo info = presenter.voteInfos.get(0);
                voteManageAdapter.setSelect(info);
                updateUI(info);
            } else {
                updateUI(null);
            }
        }
    }

    private void updateUI(InterfaceVote.pbui_Item_MeetVoteDetailInfo info) {
        if (info == null) {
            vote_manage_title.setText("");
            vote_manage_register.setText("");
            vote_manage_time_sp.setSelection(4, true);
            return;
        }
        vote_manage_title.setText(info.getContent().toStringUtf8());
        vote_manage_register.setChecked(info.getMode() == InterfaceMacro.Pb_MeetVoteMode.Pb_VOTEMODE_signed_VALUE);
        int timeouts = info.getTimeouts();
        LogUtil.d(TAG, "updateUI --> 超时值：" + timeouts);
        int position = 0;
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
        vote_manage_time_sp.setSelection(position, true);
    }

    @Override
    public void updateMemberRv() {
        if (memberAdapter == null) {
            memberAdapter = new VoteManageMemberAdapter(R.layout.item_vote_manage_member, presenter.memberInfos);
        } else {
            memberAdapter.notifyDataSetChanged();
            memberAdapter.notifyChoose();
        }
    }

    private void initView(View inflate) {
        vote_manage_rv = inflate.findViewById(R.id.vote_manage_rv);
        vote_manage_title = inflate.findViewById(R.id.vote_manage_title);
        vote_manage_register = inflate.findViewById(R.id.vote_manage_register);
        vote_manage_add = inflate.findViewById(R.id.vote_manage_add);
        vote_manage_modify = inflate.findViewById(R.id.vote_manage_modify);
        vote_manage_delete = inflate.findViewById(R.id.vote_manage_delete);
        vote_manage_export = inflate.findViewById(R.id.vote_manage_export);
        vote_manage_import = inflate.findViewById(R.id.vote_manage_import);
        vote_manage_time_sp = inflate.findViewById(R.id.vote_manage_time_sp);
        vote_manage_details = inflate.findViewById(R.id.vote_manage_details);
        vote_manage_chart = inflate.findViewById(R.id.vote_manage_chart);
        vote_manage_launch = inflate.findViewById(R.id.vote_manage_launch);
        vote_manage_stop = inflate.findViewById(R.id.vote_manage_stop);

        vote_manage_add.setOnClickListener(this);
        vote_manage_modify.setOnClickListener(this);
        vote_manage_delete.setOnClickListener(this);
        vote_manage_export.setOnClickListener(this);
        vote_manage_import.setOnClickListener(this);
        vote_manage_details.setOnClickListener(this);
        vote_manage_chart.setOnClickListener(this);
        vote_manage_launch.setOnClickListener(this);
        vote_manage_stop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vote_manage_add:
                voteOper(true, 0);
                break;
            case R.id.vote_manage_modify:
                if (voteManageAdapter != null && voteManageAdapter.getSelectedVote() != null) {
                    for (int i = 0; i < presenter.voteInfos.size(); i++) {
                        InterfaceVote.pbui_Item_MeetVoteDetailInfo detailInfo = presenter.voteInfos.get(i);
                        if (detailInfo.getVoteid() == voteManageAdapter.getSelectedVote().getVoteid()) {
                            if (detailInfo.getVotestate() != InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE) {
                                ToastUtil.show(R.string.modify_not_vote);
                                return;
                            } else {
                                voteOper(false, detailInfo.getVoteid());
                            }
                            break;
                        }
                    }
                } else {
                    ToastUtil.show(R.string.please_choose_vote);
                }
                break;
            case R.id.vote_manage_delete:
                if (voteManageAdapter != null && voteManageAdapter.getSelectedVote() != null) {
                    presenter.deleteVote(voteManageAdapter.getSelectedVote().getVoteid());
                } else {
                    ToastUtil.show(R.string.please_choose_vote);
                }
                break;
            case R.id.vote_manage_export:
                if (!presenter.voteInfos.isEmpty()) {
                    JxlUtil.exportVoteInfo(presenter.voteInfos, getString(R.string.vote_fileName), getString(R.string.vote_content));
                } else {
                    ToastUtil.show(R.string.no_vote_info);
                }
                break;
            case R.id.vote_manage_import:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/plain");//.xls文件
//                intent.setType("file/*.xls");// {".xls", "application/vnd.ms-excel"}
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
                break;
            //查看详情
            case R.id.vote_manage_details: {
                if (voteManageAdapter != null && voteManageAdapter.getSelectedVote() != null) {
                    InterfaceVote.pbui_Item_MeetVoteDetailInfo selectedVote = voteManageAdapter.getSelectedVote();
                    if (selectedVote.getMode() != InterfaceMacro.Pb_MeetVoteMode.Pb_VOTEMODE_agonymous_VALUE) {
                        if (selectedVote.getVotestate() != InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE) {
                            presenter.querySubmittedVoters(selectedVote, true);
                        } else {
                            ToastUtil.show(R.string.can_not_choose_notvote);
                        }
                    } else {
                        ToastUtil.show(R.string.please_choose_registered_vote);
                    }
                }
                break;
            }
            //查看图表
            case R.id.vote_manage_chart: {
                if (voteManageAdapter != null && voteManageAdapter.getSelectedVote() != null) {
                    InterfaceVote.pbui_Item_MeetVoteDetailInfo selectedVote = voteManageAdapter.getSelectedVote();
                    if (selectedVote.getVotestate() != InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE) {
                        presenter.querySubmittedVoters(selectedVote, false);
                    } else {
                        ToastUtil.show(R.string.can_not_choose_notvote);
                    }
                }
                break;
            }
            //发起投票
            case R.id.vote_manage_launch: {
                if (voteManageAdapter != null && voteManageAdapter.getSelectedVote() != null) {
                    if (voteManageAdapter.getSelectedVote().getVotestate() != InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE) {
                        ToastUtil.show(R.string.please_choose_not_vote);
                        return;
                    }
                    for (int i = 0; i < presenter.voteInfos.size(); i++) {
                        InterfaceVote.pbui_Item_MeetVoteDetailInfo info = presenter.voteInfos.get(i);
                        if (info.getVotestate() == InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing_VALUE) {
                            ToastUtil.show(R.string.please_stop_vote_first);
                            return;
                        }
                    }
                    showMember();
                }
                break;
            }
            //结束投票
            case R.id.vote_manage_stop: {
                if (voteManageAdapter != null && voteManageAdapter.getSelectedVote() != null) {
                    if (voteManageAdapter.getSelectedVote().getVotestate() != InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_voteing_VALUE) {
                        ToastUtil.show(R.string.only_stop_voteing);
                        return;
                    }
                    presenter.stopVote(voteManageAdapter.getSelectedVote().getVoteid());
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            Uri uri = data.getData();
            try {
                String path = UriUtil.getFilePath(getContext(), uri);
                LogUtil.i(TAG, "onActivityResult -->path = " + path);
                if (path != null && !path.isEmpty()) {
                    if (path.lastIndexOf(".") > 0 && path.substring(path.lastIndexOf(".")).equals(".xls")) {
                        List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> infos = JxlUtil.readVoteXls(path, InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote_VALUE);
                        for (int i = 0; i < infos.size(); i++) {
                            presenter.createVote(infos.get(i));
                        }
                    } else {
                        ToastUtil.show(R.string.must_be_xls);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void showChartPop(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_chart, null);
        chartPop = showPop(vote_manage_stop, inflate);
        ChartViewHolder chartViewHolder = new ChartViewHolder(inflate);
        chartViewHolderEvent(chartViewHolder, vote);
    }

    int countPre = 0;//一共占用的百分比数

    private void chartViewHolderEvent(ChartViewHolder holder, InterfaceVote.pbui_Item_MeetVoteDetailInfo vote) {
        countPre = 0;
        chartDatas.clear();
        /** **** **  先隐藏所有的选项  ** **** **/
        holder.pop_option_a_ll.setVisibility(View.GONE);
        holder.pop_option_b_ll.setVisibility(View.GONE);
        holder.pop_option_c_ll.setVisibility(View.GONE);
        holder.pop_option_d_ll.setVisibility(View.GONE);
        holder.pop_option_e_ll.setVisibility(View.GONE);
        //饼状图形 需要先隐藏
        holder.pop_chart.setVisibility(View.GONE);
        int voteid = vote.getVoteid();
        String[] strings = presenter.queryYd(vote);
        String type = presenter.getType(vote.getType());
        String state = presenter.getVoteState(vote.getVotestate());
        String mode = vote.getMode() == 0 ? getString(R.string.mode_anonymous) : getString(R.string.mode_register);
        holder.pop_chart_type.setText("（" + type + " " + mode + " " + state + "）" + strings[0] + strings[1] + strings[2] + strings[3]);
        holder.pop_chart_title.setText(vote.getContent().toStringUtf8());
        List<InterfaceVote.pbui_SubItem_VoteItemInfo> optionInfo = vote.getItemList();
        int count = getCount(optionInfo);
        for (int i = 0; i < optionInfo.size(); i++) {
            InterfaceVote.pbui_SubItem_VoteItemInfo info = optionInfo.get(i);
            String text = info.getText().toStringUtf8();
            int selcnt = info.getSelcnt();
            if (!TextUtils.isEmpty(text)) {
                if (i == 0) {
                    holder.pop_option_a_ll.setVisibility(View.VISIBLE);
                    holder.pop_option_a_tv.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, Color.parseColor("#000000"), Color.parseColor("#FF0000"));
                } else if (i == 1) {
                    holder.pop_option_b_ll.setVisibility(View.VISIBLE);
                    holder.pop_option_b_tv.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, Color.parseColor("#000000"), Color.parseColor("#00FF00"));
                } else if (i == 2) {
                    holder.pop_option_c_ll.setVisibility(View.VISIBLE);
                    holder.pop_option_c_tv.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, Color.parseColor("#000000"), Color.parseColor("#0000FF"));
                } else if (i == 3) {
                    holder.pop_option_d_ll.setVisibility(View.VISIBLE);
                    holder.pop_option_d_tv.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, Color.parseColor("#000000"), Color.parseColor("#00FFFF"));
                } else if (i == 4) {
                    holder.pop_option_e_ll.setVisibility(View.VISIBLE);
                    holder.pop_option_e_tv.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, Color.parseColor("#000000"), Color.parseColor("#FF00FF"));
                }
            }
        }
        if (countPre > 0 && countPre < 100) {//因为没有除尽,有余下的空白区域
            ChartData lastChartData = chartDatas.get(chartDatas.size() - 1);//先获取到最后一条的数据
            chartDatas.remove(chartDatas.size() - 1);//删除掉集合中的最后一个
            //使用原数据重新添加,但是修改所占比例大小,这样就能确保不会出现空白部分
            chartDatas.add(new ChartData(lastChartData.getDisplayText(), lastChartData.getPartInPercent() + (100 - countPre), lastChartData.getTextColor(), lastChartData.getBackgroundColor()));
        }
        //如果没有数据会报错
        if (chartDatas.isEmpty()) {
            chartDatas.add(new ChartData(getResources().getString(R.string.null_str), 100, Color.parseColor("#FFFFFF"), Color.parseColor("#7D7D7D")));
        }
        holder.pop_chart.setChartData(chartDatas);
        holder.pop_chart.setVisibility(View.VISIBLE);
        holder.pop_chart_close.setOnClickListener(v -> chartPop.dismiss());
    }

    private int setChartData(float count, int selcnt, int colora, int colorb) {
        if (selcnt > 0) {
            float element = (float) selcnt / count;
            LogUtil.d(TAG, "FabService.setUplistener :  element --> " + element);
            int v = (int) (element * 100);
            String str = v + "%";
            countPre += v;
            chartDatas.add(new ChartData(str, v, colora, colorb));
        }
        return countPre;
    }

    private int getCount(List<InterfaceVote.pbui_SubItem_VoteItemInfo> itemList) {
        int count = 0;
        for (int i = 0; i < itemList.size(); i++) {
            InterfaceVote.pbui_SubItem_VoteItemInfo info = itemList.get(i);
            count += info.getSelcnt();
        }
        LogUtil.e(TAG, "getCount :  当前投票票数总数 --> " + count);
        return count;
    }

    @Override
    public void showSubmittedPop(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_submitted_member, null);
        PopupWindow popupWindow = new PopupWindow(inflate, MeetingActivity.frameLayoutWidth, MeetingActivity.frameLayoutHeight);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        popupWindow.setTouchable(true);
        // true:设置触摸外面时消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.pop_Animation);
        popupWindow.showAtLocation(vote_manage_stop, Gravity.END | Gravity.BOTTOM, 0, 0);
        SubmitMemberAdapter adapter = new SubmitMemberAdapter(R.layout.item_submit_member, presenter.submitMembers);
        RecyclerView submit_member_rv = inflate.findViewById(R.id.submit_member_rv);
        submit_member_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        submit_member_rv.setAdapter(adapter);
        inflate.findViewById(R.id.submit_member_back).setOnClickListener(v -> popupWindow.dismiss());
        inflate.findViewById(R.id.submit_member_export).setOnClickListener(v -> {
            String[] strings = presenter.queryYd(vote);
            String createTime = DateUtil.nowDate(System.currentTimeMillis());
            ExportSubmitMember exportSubmitMember = new ExportSubmitMember(vote.getContent().toStringUtf8(), createTime, strings[0], strings[1], strings[2], strings[3], presenter.submitMembers);
            JxlUtil.exportSubmitMember(exportSubmitMember);
        });
    }

    // TODO: 2020/10/12  虚拟按键显示或隐藏时需要动态改变已经显示的PopupWindow的大小
    private void updatePopupWindowSize() {
        if (memberPop != null && memberPop.isShowing()) {

        }
    }

    private void showMember() {
        presenter.queryMember();
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_vote_member, null);
        memberPop = new PopupWindow(inflate, MeetingActivity.frameLayoutWidth, MeetingActivity.frameLayoutHeight);
        memberPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        memberPop.setTouchable(true);
        // true:设置触摸外面时消失
        memberPop.setOutsideTouchable(true);
        memberPop.setFocusable(true);
        memberPop.setAnimationStyle(R.style.pop_Animation);
        memberPop.showAtLocation(vote_manage_stop, Gravity.END | Gravity.BOTTOM, 0, 0);

        CheckBox pop_vote_all = inflate.findViewById(R.id.pop_vote_all);
        RecyclerView pop_vote_rv = inflate.findViewById(R.id.pop_vote_rv);
        pop_vote_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        pop_vote_rv.setAdapter(memberAdapter);
        memberAdapter.setOnItemClickListener((adapter, view, position) -> {
            memberAdapter.setChoose(presenter.memberInfos.get(position).getMemberid());
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

    private void voteOper(boolean isCreate, int voteid) {
        String content = vote_manage_title.getText().toString().trim();
        if (content.isEmpty()) {
            ToastUtil.show(R.string.vote_content_empty);
            return;
        }
        if (content.length() > Constant.MAX_TITLE_LENGTH) {
            ToastUtil.show(getString(R.string.err_title_max_length, Constant.MAX_TITLE_LENGTH + ""));
            return;
        }
        int timeouts = getTimeouts();
        List<ByteString> all = new ArrayList<>();
        all.add(s2b(getString(R.string.approve)));
        all.add(s2b(getString(R.string.against)));
        all.add(s2b(getString(R.string.abstain)));
        InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.Builder builder = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder();
        builder.setContent(s2b(content))
                .setMaintype(InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote_VALUE)
                .setMode(vote_manage_register.isChecked() ? InterfaceMacro.Pb_MeetVoteMode.Pb_VOTEMODE_signed_VALUE : InterfaceMacro.Pb_MeetVoteMode.Pb_VOTEMODE_agonymous_VALUE)
                .setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_SINGLE_VALUE)
                .setTimeouts(timeouts)
                .setSelectcount(3)
                .addAllText(all);
        if (!isCreate) {
            builder.setVoteid(voteid);
        }
        InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = builder.build();
        if (isCreate) {
            presenter.createVote(build);
        } else {
            presenter.modifyVote(build);
        }
    }

    private int getTimeouts() {
        int position = vote_manage_time_sp.getSelectedItemPosition();
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
        }
        return timeouts;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    public static class ChartViewHolder {
        public View rootView;
        public ImageView pop_chart_close;
        public TextView pop_chart_type;
        public TextView pop_chart_title;
        public PieChart pop_chart;
        public TextView pop_option_a_tv;
        public LinearLayout pop_option_a_ll;
        public TextView pop_option_b_tv;
        public LinearLayout pop_option_b_ll;
        public TextView pop_option_c_tv;
        public LinearLayout pop_option_c_ll;
        public TextView pop_option_d_tv;
        public LinearLayout pop_option_d_ll;
        public TextView pop_option_e_tv;
        public LinearLayout pop_option_e_ll;
        public LinearLayout linearLayout3;

        public ChartViewHolder(View rootView) {
            this.rootView = rootView;
            this.pop_chart_close = (ImageView) rootView.findViewById(R.id.pop_chart_close);
            this.pop_chart_type = (TextView) rootView.findViewById(R.id.pop_chart_type);
            this.pop_chart_title = (TextView) rootView.findViewById(R.id.pop_chart_title);
            this.pop_chart = (PieChart) rootView.findViewById(R.id.pop_chart);
            this.pop_option_a_tv = (TextView) rootView.findViewById(R.id.pop_option_a_tv);
            this.pop_option_a_ll = (LinearLayout) rootView.findViewById(R.id.pop_option_a_ll);
            this.pop_option_b_tv = (TextView) rootView.findViewById(R.id.pop_option_b_tv);
            this.pop_option_b_ll = (LinearLayout) rootView.findViewById(R.id.pop_option_b_ll);
            this.pop_option_c_tv = (TextView) rootView.findViewById(R.id.pop_option_c_tv);
            this.pop_option_c_ll = (LinearLayout) rootView.findViewById(R.id.pop_option_c_ll);
            this.pop_option_d_tv = (TextView) rootView.findViewById(R.id.pop_option_d_tv);
            this.pop_option_d_ll = (LinearLayout) rootView.findViewById(R.id.pop_option_d_ll);
            this.pop_option_e_tv = (TextView) rootView.findViewById(R.id.pop_option_e_tv);
            this.pop_option_e_ll = (LinearLayout) rootView.findViewById(R.id.pop_option_e_ll);
            this.linearLayout3 = (LinearLayout) rootView.findViewById(R.id.linearLayout3);
        }
    }
}
