package xlk.paperless.standard.view.admin.fragment.after.vote;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.intrusoft.scatter.ChartData;
import com.intrusoft.scatter.PieChart;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.SubmitMemberAdapter;
import xlk.paperless.standard.adapter.VoteManageAdapter;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.exportbean.ExportSubmitMember;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.JxlUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.fragment.other.vote.VoteManageFragment;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc
 */
public class VoteResultFragment extends BaseFragment implements VoteResultInterface, View.OnClickListener {

    private VoteResultPresenter presenter;
    private RecyclerView rv_vote_result;
    private Button btn_export_pdf;
    private Button btn_view_chat;
    private Button btn_view_details;
    private VoteManageAdapter voteManageAdapter;
    private PopupWindow chartPop;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_vote_result, container, false);
        initView(inflate);
        presenter = new VoteResultPresenter(getContext(), this);
        presenter.queryMember();
        presenter.queryVote();
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    protected void reShow() {
        presenter.queryMember();
        presenter.queryVote();
    }

    public void initView(View rootView) {
        this.rv_vote_result = (RecyclerView) rootView.findViewById(R.id.rv_vote_result);
        this.btn_export_pdf = (Button) rootView.findViewById(R.id.btn_export_pdf);
        this.btn_view_chat = (Button) rootView.findViewById(R.id.btn_view_chat);
        this.btn_view_details = (Button) rootView.findViewById(R.id.btn_view_details);
        btn_export_pdf.setOnClickListener(this);
        btn_view_chat.setOnClickListener(this);
        btn_view_details.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_export_pdf:
                break;
            case R.id.btn_view_chat: {
                if (voteManageAdapter == null || voteManageAdapter.getSelectedVote() == null) {
                    ToastUtil.show(R.string.please_choose_vote);
                    return;
                }
                InterfaceVote.pbui_Item_MeetVoteDetailInfo selectedVote = voteManageAdapter.getSelectedVote();
                if (selectedVote.getVotestate() != InterfaceMacro.Pb_MeetVoteStatus.Pb_vote_notvote_VALUE) {
                    presenter.querySubmittedVoters(selectedVote, false);
                } else {
                    ToastUtil.show(R.string.can_not_choose_notvote);
                }
                break;
            }
            case R.id.btn_view_details: {
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
            default:
                break;
        }
    }

    @Override
    public void updateVoteRv(List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteInfo) {
        if (voteManageAdapter == null) {
            voteManageAdapter = new VoteManageAdapter(R.layout.item_vote_manage, voteInfo);
            rv_vote_result.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_vote_result.setAdapter(voteManageAdapter);

            voteManageAdapter.setOnItemClickListener((adapter, view, position) -> {
                InterfaceVote.pbui_Item_MeetVoteDetailInfo item = voteInfo.get(position);
                voteManageAdapter.setSelect(item.getVoteid());
            });
        } else {
            voteManageAdapter.notifyDataSetChanged();
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

    @Override
    public void showChartPop(InterfaceVote.pbui_Item_MeetVoteDetailInfo vote) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_chart, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showChartPop fragment的大小 width=" + width + ",height=" + height);
        chartPop = new PopupWindow(inflate, width, height);
        chartPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        chartPop.setTouchable(true);
        // true:设置触摸外面时消失
        chartPop.setOutsideTouchable(true);
        chartPop.setFocusable(true);
        chartPop.setAnimationStyle(R.style.pop_Animation);
        chartPop.showAtLocation(btn_view_chat, Gravity.END | Gravity.BOTTOM, 0, 0);
        ChartViewHolder chartViewHolder = new ChartViewHolder(inflate);
        chartViewHolderEvent(chartViewHolder, vote);
    }

    private List<ChartData> chartDatas = new ArrayList<>();
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
                    setChartData(count, selcnt, Color.parseColor("#000000"), ContextCompat.getColor(getContext(),R.color.option_a));
                } else if (i == 1) {
                    holder.pop_option_b_ll.setVisibility(View.VISIBLE);
                    holder.pop_option_b_tv.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, Color.parseColor("#000000"), ContextCompat.getColor(getContext(),R.color.option_b));
                } else if (i == 2) {
                    holder.pop_option_c_ll.setVisibility(View.VISIBLE);
                    holder.pop_option_c_tv.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, Color.parseColor("#000000"), ContextCompat.getColor(getContext(),R.color.option_c));
                } else if (i == 3) {
                    holder.pop_option_d_ll.setVisibility(View.VISIBLE);
                    holder.pop_option_d_tv.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, Color.parseColor("#000000"), ContextCompat.getColor(getContext(),R.color.option_d));
                } else if (i == 4) {
                    holder.pop_option_e_ll.setVisibility(View.VISIBLE);
                    holder.pop_option_e_tv.setText(getString(R.string.vote_count, text, selcnt + ""));
                    setChartData(count, selcnt, Color.parseColor("#000000"), ContextCompat.getColor(getContext(),R.color.option_e));
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
            LogUtil.d(TAG, "setChartData setUplistener :  element --> " + element);
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
