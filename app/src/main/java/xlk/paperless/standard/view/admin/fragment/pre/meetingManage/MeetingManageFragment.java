package xlk.paperless.standard.view.admin.fragment.pre.meetingManage;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.helper.AfterTextWatcher;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;

import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author Created by xlk on 2020/10/14.
 * @desc
 */
public class MeetingManageFragment extends BaseFragment implements MeetingManageInterface, View.OnClickListener {
    private EditText edt_search_meeting;
    private Button btn_switch_meeting;
    private Button btn_copy_meeting;
    private RecyclerView rv_meeting;
    private EditText edt_meet_name;
    private Spinner sp_room;
    private Spinner sp_confidential;
    private TextView edt_start_time;
    private TextView edt_end_time;
    private Spinner sp_sign_in;
    private EditText edt_meet_booker;
    private Button btn_increase;
    private Button btn_modify;
    private Button btn_delete;
    private Button btn_start_meet;
    private Button btn_end_meet;
    private Button btn_pause_meet;
    private Button btn_keep_template;
    private MeetingManagePresenter presenter;
    private MeetingAdapter meetingAdapter;
    private ArrayAdapter<String> roomSpAdapter;
    private int currentRoomId;
    private TimePickerView mStartTimePickerView, mEndTimePickerView;
    /**
     * 当前的开始时间和结束时间，单位是秒
     */
    private long currentStartTime, currentEndTime;
    List<InterfaceMeet.pbui_Item_MeetMeetInfo> currentMeetings = new ArrayList<>();
    private PopupWindow filterMeetPop;
    private RecyclerView rv_filter_meet;
    List<InterfaceMeet.pbui_Item_MeetMeetInfo> filterMeetings = new ArrayList<>();
    private FilterMeetAdapter filterMeetAdapter;
    private List<Button> filterBtns = new ArrayList<>();
    /**
     * 默认是显示当前会议
     */
    private int currentTypeIndex = 0;
    private LinearLayout ll_status;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_meeting_manage, container, false);
        initView(inflate);
        presenter = new MeetingManagePresenter(this);
        initStartTimePicker();
        initEndTimePicker();
        presenter.queryRoom();
        return inflate;
    }

    private void initStartTimePicker() {
        mStartTimePickerView = new TimePickerBuilder(getContext(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                currentStartTime = date.getTime() / 1000;
                edt_start_time.setText(DateUtil.millisecondFormatDateTime(date.getTime()));
            }
        })
                .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                    @Override
                    public void onTimeSelectChanged(Date date) {
                    }
                })
                .setType(new boolean[]{true, true, true, true, true, false})
                //默认设置false ，内部实现将DecorView 作为它的父控件
                .isDialog(true)
                .addOnCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                })
                //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
                .setItemVisibleCount(5)
                .setLineSpacingMultiplier(2.0f)
                .isAlphaGradient(true)
                .build();
    }

    private void initEndTimePicker() {
        mEndTimePickerView = new TimePickerBuilder(getContext(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                currentEndTime = date.getTime() / 1000;
                edt_end_time.setText(DateUtil.millisecondFormatDateTime(date.getTime()));
            }
        })
                .setTimeSelectChangeListener(new OnTimeSelectChangeListener() {
                    @Override
                    public void onTimeSelectChanged(Date date) {
                    }
                })
                .setType(new boolean[]{true, true, true, true, true, false})
                //默认设置false ，内部实现将DecorView 作为它的父控件
                .isDialog(true)
                .addOnCancelClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                })
                //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
                .setItemVisibleCount(5)
                .setLineSpacingMultiplier(2.0f)
                .isAlphaGradient(true)
                .build();
    }

    private void initView(View inflate) {
        Button btn_filter_current = inflate.findViewById(R.id.btn_filter_current);
        Button btn_filter_history = inflate.findViewById(R.id.btn_filter_history);
        Button btn_filter_template = inflate.findViewById(R.id.btn_filter_template);
        Button btn_filter_all = inflate.findViewById(R.id.btn_filter_all);
        filterBtns.add(btn_filter_current);
        filterBtns.add(btn_filter_history);
        filterBtns.add(btn_filter_template);
        filterBtns.add(btn_filter_all);
        btn_filter_current.setOnClickListener(this);
        btn_filter_history.setOnClickListener(this);
        btn_filter_template.setOnClickListener(this);
        btn_filter_all.setOnClickListener(this);

        ll_status = (LinearLayout) inflate.findViewById(R.id.ll_status);

        inflate.findViewById(R.id.iv_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        inflate.findViewById(R.id.iv_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edt_search_meeting.setText("");
            }
        });

        edt_search_meeting = (EditText) inflate.findViewById(R.id.edt_search_meeting);
        btn_switch_meeting = (Button) inflate.findViewById(R.id.btn_switch_meeting);
        btn_copy_meeting = (Button) inflate.findViewById(R.id.btn_copy_meeting);
        rv_meeting = (RecyclerView) inflate.findViewById(R.id.rv_meeting);
        edt_meet_name = (EditText) inflate.findViewById(R.id.edt_meet_name);
        sp_room = (Spinner) inflate.findViewById(R.id.sp_room);
        sp_confidential = (Spinner) inflate.findViewById(R.id.sp_confidential);
        edt_start_time = (TextView) inflate.findViewById(R.id.edt_start_time);
        edt_end_time = (TextView) inflate.findViewById(R.id.edt_end_time);
        sp_sign_in = (Spinner) inflate.findViewById(R.id.sp_sign_in);
        edt_meet_booker = (EditText) inflate.findViewById(R.id.edt_meet_booker);
        btn_increase = (Button) inflate.findViewById(R.id.btn_increase);
        btn_modify = (Button) inflate.findViewById(R.id.btn_modify);
        btn_delete = (Button) inflate.findViewById(R.id.btn_delete);
        btn_start_meet = (Button) inflate.findViewById(R.id.btn_start_meet);
        btn_end_meet = (Button) inflate.findViewById(R.id.btn_end_meet);
        btn_pause_meet = (Button) inflate.findViewById(R.id.btn_pause_meet);
        btn_keep_template = (Button) inflate.findViewById(R.id.btn_keep_template);

        edt_start_time.setOnClickListener(this);
        edt_end_time.setOnClickListener(this);

        btn_switch_meeting.setOnClickListener(this);
        btn_copy_meeting.setOnClickListener(this);
        btn_increase.setOnClickListener(this);
        btn_modify.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_start_meet.setOnClickListener(this);
        btn_end_meet.setOnClickListener(this);
        btn_pause_meet.setOnClickListener(this);
        btn_keep_template.setOnClickListener(this);
        edt_search_meeting.addTextChangedListener(new AfterTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString().trim();
                LogUtil.i(TAG, "afterTextChanged content=" + content);
                if (!content.isEmpty()) {
                    filterMeeting(content);
                    if (filterMeetPop == null || !filterMeetPop.isShowing()) {
                        showMeetingPop(edt_search_meeting.getWidth());
                    } else {
                        filterMeetAdapter.notifyDataSetChanged();
                    }
                } else {
                    if (filterMeetPop != null && filterMeetPop.isShowing()) {
                        filterMeetPop.dismiss();
                    }
                }
            }
        });
    }

    private void setSelected(int index) {
        currentTypeIndex = index;
        ll_status.setVisibility(currentTypeIndex == 2 ? View.INVISIBLE : View.VISIBLE);
        for (int i = 0; i < filterBtns.size(); i++) {
            Button button = filterBtns.get(i);
            button.setSelected(index == i);
        }
        currentMeetings.clear();
        for (int i = 0; i < presenter.meetings.size(); i++) {
            InterfaceMeet.pbui_Item_MeetMeetInfo item = presenter.meetings.get(i);
            int status = item.getStatus();
            switch (index) {
                case 0:
                    if (status == 0 || status == 1 || status == 3) {
                        currentMeetings.add(item);
                    }
                    break;
                case 1:
                    if (status == 2) {
                        currentMeetings.add(item);
                    }
                    break;
                case 2:
                    if (status == 4) {
                        currentMeetings.add(item);
                    }
                    break;
                case 3:
                    currentMeetings.add(item);
                    break;
                default:
                    break;
            }
        }
        if (meetingAdapter != null) {
            meetingAdapter.notifyDataSetChanged();
        }
    }

    private void filterMeeting(String content) {
        filterMeetings.clear();
        for (int i = 0; i < currentMeetings.size(); i++) {
            InterfaceMeet.pbui_Item_MeetMeetInfo item = currentMeetings.get(i);
            if (item.getName().toStringUtf8().contains(content)) {
                filterMeetings.add(item);
            }
        }
    }

    private void showMeetingPop(int width) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_filter_meeting, null);
        LogUtil.i(TAG, "showMeetingPop fragment的大小 width=" + width + ",height=" + width);
        filterMeetPop = new PopupWindow(inflate, width, 200);
        filterMeetPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        filterMeetPop.setTouchable(true);
        // true:设置触摸外面时消失
        filterMeetPop.setOutsideTouchable(false);
        filterMeetPop.setFocusable(false);
        filterMeetPop.setAnimationStyle(R.style.pop_Animation);
        filterMeetPop.showAsDropDown(edt_search_meeting, 0, 0);
        rv_filter_meet = inflate.findViewById(R.id.rv_filter_meet);
        filterMeetAdapter = new FilterMeetAdapter(R.layout.item_signin_text, filterMeetings);
        rv_filter_meet.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_filter_meet.setAdapter(filterMeetAdapter);
        filterMeetAdapter.setOnItemClickListener((adapter, view, position) -> {
            InterfaceMeet.pbui_Item_MeetMeetInfo item = filterMeetings.get(position);
            updateUI(item);
            edt_search_meeting.setText("");
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_filter_current: {
                setSelected(0);
                break;
            }
            case R.id.btn_filter_history: {
                setSelected(1);
                break;
            }
            case R.id.btn_filter_template: {
                setSelected(2);
                break;
            }
            case R.id.btn_filter_all: {
                setSelected(3);
                break;
            }
            //切换到会议编辑
            case R.id.btn_switch_meeting: {
                if (meetingAdapter != null && meetingAdapter.getSelectedMeeting() != null) {
                    presenter.switchMeeting(meetingAdapter.getSelectedMeeting().getId());
                } else {
                    ToastUtil.show(R.string.please_choose_meeting_first);
                }
                break;
            }
            //复制会议
            case R.id.btn_copy_meeting: {
                if (meetingAdapter != null && meetingAdapter.getSelectedMeeting() != null) {
                    presenter.copyMeeting(meetingAdapter.getSelectedMeeting());
                } else {
                    ToastUtil.show(R.string.please_choose_meeting_first);
                }
                break;
            }
            //开始时间
            case R.id.edt_start_time: {
                if (currentStartTime != 0) {
                    Calendar instance = Calendar.getInstance();
                    instance.setTimeInMillis(currentStartTime * 1000);
                    mStartTimePickerView.setDate(instance);
                }
                mStartTimePickerView.show(edt_start_time);
                break;
            }
            //结束时间
            case R.id.edt_end_time: {
                if (currentEndTime != 0) {
                    Calendar instance = Calendar.getInstance();
                    instance.setTimeInMillis(currentEndTime * 1000);
                    mEndTimePickerView.setDate(instance);
                }
                mEndTimePickerView.show(edt_end_time);
                break;
            }
            //增加
            case R.id.btn_increase: {
                addMeeting();
                break;
            }
            //修改
            case R.id.btn_modify: {
                if (meetingAdapter != null && meetingAdapter.getSelectedMeeting() != null) {
                    modifyMeeting(meetingAdapter.getSelectedMeeting());
                } else {
                    ToastUtil.show(R.string.please_choose_meeting_first);
                }
                break;
            }
            //删除
            case R.id.btn_delete: {
                if (meetingAdapter != null && meetingAdapter.getSelectedMeeting() != null) {
                    presenter.delMeeting(meetingAdapter.getSelectedMeeting());
                } else {
                    ToastUtil.show(R.string.please_choose_meeting_first);
                }
                break;
            }
            //开始会议
            case R.id.btn_start_meet: {
                if (meetingAdapter != null && meetingAdapter.getSelectedMeeting() != null) {
                    presenter.modifyMeetingStatus(meetingAdapter.getSelectedMeeting().getId(),
                            InterfaceMacro.Pb_MeetStatus.Pb_MEETING_STATUS_Start_VALUE);
                } else {
                    ToastUtil.show(R.string.please_choose_meeting_first);
                }
                break;
            }
            //结束会议
            case R.id.btn_end_meet: {
                if (meetingAdapter != null && meetingAdapter.getSelectedMeeting() != null) {
                    presenter.modifyMeetingStatus(meetingAdapter.getSelectedMeeting().getId(),
                            InterfaceMacro.Pb_MeetStatus.Pb_MEETING_STATUS_End_VALUE);
                } else {
                    ToastUtil.show(R.string.please_choose_meeting_first);
                }
                break;
            }
            //暂停会议
            case R.id.btn_pause_meet: {
                if (meetingAdapter != null && meetingAdapter.getSelectedMeeting() != null) {
                    presenter.modifyMeetingStatus(meetingAdapter.getSelectedMeeting().getId(),
                            InterfaceMacro.Pb_MeetStatus.Pb_MEETING_STATUS_PAUSE_VALUE);
                } else {
                    ToastUtil.show(R.string.please_choose_meeting_first);
                }
                break;
            }
            //收藏模板
            case R.id.btn_keep_template: {
                if (meetingAdapter != null && meetingAdapter.getSelectedMeeting() != null) {
                    presenter.modifyMeetingStatus(meetingAdapter.getSelectedMeeting().getId(),
                            InterfaceMacro.Pb_MeetStatus.Pb_MEETING_MODEL_VALUE);
                } else {
                    ToastUtil.show(R.string.please_choose_meeting_first);
                }
                break;
            }
            default:
                break;
        }
    }

    private void modifyMeeting(InterfaceMeet.pbui_Item_MeetMeetInfo item) {
        String meetingName = edt_meet_name.getText().toString().trim();
        if (TextUtils.isEmpty(meetingName)) {
            ToastUtil.show(R.string.please_enter_meeting_name);
            return;
        }
        String orderName = edt_meet_booker.getText().toString().trim();
        if (edt_start_time.getText().toString().trim().isEmpty()
                || edt_end_time.getText().toString().trim().isEmpty()) {
            ToastUtil.show(R.string.please_choose_time);
            return;
        }
        LogUtil.i(TAG, "modifyMeeting currentStartTime=" + currentStartTime + ",currentEndTime=" + currentEndTime);
        if (currentStartTime > currentEndTime) {
            ToastUtil.show(R.string.err_start_time);
            return;
        }
        InterfaceMeet.pbui_Item_MeetMeetInfo build = InterfaceMeet.pbui_Item_MeetMeetInfo.newBuilder()
                .setId(item.getId())
                .setName(s2b(meetingName))
                .setRoomId(currentRoomId)
                .setSecrecy(sp_confidential.getSelectedItemPosition())
                .setStartTime(currentStartTime)
                .setEndTime(currentEndTime)
                .setSigninType(sp_sign_in.getSelectedItemPosition())
                .setOrdername(s2b(orderName))
                .build();
        presenter.modifyMeeting(build);
    }

    private void addMeeting() {
        String meetingName = edt_meet_name.getText().toString().trim();
        if (TextUtils.isEmpty(meetingName)) {
            ToastUtil.show(R.string.please_enter_meeting_name);
            return;
        }
        if (edt_start_time.getText().toString().trim().isEmpty()
                || edt_end_time.getText().toString().trim().isEmpty()) {
            ToastUtil.show(R.string.please_choose_time);
            return;
        }
        if (currentStartTime > currentEndTime) {
            ToastUtil.show(R.string.err_start_time);
            return;
        }
        String orderName = edt_meet_booker.getText().toString().trim();
        InterfaceMeet.pbui_Item_MeetMeetInfo build = InterfaceMeet.pbui_Item_MeetMeetInfo.newBuilder()
                .setName(s2b(meetingName))
                .setRoomId(currentRoomId)
                .setSecrecy(sp_confidential.getSelectedItemPosition())
                .setStartTime(currentStartTime)
                .setEndTime(currentEndTime)
                .setSigninType(sp_sign_in.getSelectedItemPosition())
                .setOrdername(s2b(orderName))
                .build();
        presenter.addMeeting(build);
    }

    @Override
    public void updateRooms(ArrayList<String> roomNames) {
        if (roomSpAdapter == null) {
            roomSpAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_checked_text,
                    roomNames);
            roomSpAdapter.setDropDownViewResource(R.layout.spinner_item_text);
            sp_room.setAdapter(roomSpAdapter);
        } else {
            roomSpAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateMeetingRv(List<InterfaceMeet.pbui_Item_MeetMeetInfo> meetings) {
        setSelected(currentTypeIndex);
        if (meetingAdapter == null) {
            meetingAdapter = new MeetingAdapter(R.layout.item_admin_meeting, currentMeetings);
            rv_meeting.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_meeting.setAdapter(meetingAdapter);
            meetingAdapter.setOnItemClickListener((adapter, view, position) -> {
                InterfaceMeet.pbui_Item_MeetMeetInfo item = currentMeetings.get(position);
                updateUI(item);
            });
            if (!currentMeetings.isEmpty()) {
                InterfaceMeet.pbui_Item_MeetMeetInfo firstMeeting = currentMeetings.get(0);
                updateUI(firstMeeting);
            } else {
                LogUtil.e(TAG, "updateMeetingRv 首次创建时没有会议");
            }
        } else {
            meetingAdapter.notifyDataSetChanged();
            if (currentRoomId == 0) {
                if (!currentMeetings.isEmpty()) {
                    InterfaceMeet.pbui_Item_MeetMeetInfo firstMeeting = currentMeetings.get(0);
                    updateUI(firstMeeting);
                } else {
                    LogUtil.e(TAG, "updateMeetingRv 更新时没有会议");
                }
            }
        }
    }

    private void updateUI(InterfaceMeet.pbui_Item_MeetMeetInfo item) {
        currentRoomId = item.getRoomId();
        currentStartTime = item.getStartTime();
        currentEndTime = item.getEndTime();
        meetingAdapter.setSelected(item.getId());
        LogUtil.i(TAG, "updateUI 更新底部显示UI currentStartTime=" + currentStartTime + ",currentEndTime=" + currentEndTime);

        edt_meet_name.setText(item.getName().toStringUtf8());
        sp_room.setSelection(presenter.getCurrentRoom(item.getRoomId()));
        sp_confidential.setSelection(item.getSecrecy());
        edt_start_time.setText(DateUtil.secondFormatDateTime(item.getStartTime()));
        edt_end_time.setText(DateUtil.secondFormatDateTime(item.getEndTime()));
        try {
            sp_sign_in.setSelection(item.getSigninType());
        } catch (Exception e) {
            LogUtil.e(TAG, "updateUI " + e);
            sp_sign_in.setSelection(0);
        }
        edt_meet_booker.setText(item.getOrdername().toStringUtf8());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            presenter.queryRoom();
        }
    }
}
