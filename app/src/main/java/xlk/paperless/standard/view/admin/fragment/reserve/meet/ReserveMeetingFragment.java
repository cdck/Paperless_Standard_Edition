package xlk.paperless.standard.view.admin.fragment.reserve.meet;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;

import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author Created by xlk on 2020/11/13.
 * @desc
 */
public class ReserveMeetingFragment extends BaseFragment implements ReserveMeetingInterface, View.OnClickListener {

    private ReserveMeetingPresenter presenter;
    private Spinner sp_room;
    private RecyclerView rv_meet;
    private EditText edt_meet_name;
    private Spinner sp_secret;
    private TextView tv_start_time;
    private TextView tv_end_time;
    private EditText edt_reservation_person;
    private Button btn_create_reserve;
    private Button btn_modify_reserve;
    private Button btn_delete_reserve;
    private Button btn_inform_start;
    private Button btn_inform_extension;
    private Button btn_inform_cancel;
    private TimePickerView mStartTimePickerView;
    private TimePickerView mEndTimePickerView;
    private long currentStartTime;
    private long currentEndTime;
    private int currentRoomId;
    private ReserveMeetingAdapter reserveMeetingAdapter;
    List<InterfaceMeet.pbui_Item_MeetMeetInfo> currentMeets = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_reserve_meet, container, false);
        initView(inflate);
        initStartTimePicker();
        initEndTimePicker();
        presenter = new ReserveMeetingPresenter(this);
        reShow();
        return inflate;
    }

    @Override
    protected void reShow() {
        presenter.queryRoom();
        presenter.queryMeet();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    private void initStartTimePicker() {
        mStartTimePickerView = new TimePickerBuilder(getContext(), new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {
                currentStartTime = date.getTime() / 1000;
                tv_start_time.setText(DateUtil.millisecondFormatDateTime(date.getTime()));
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
                tv_end_time.setText(DateUtil.millisecondFormatDateTime(date.getTime()));
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

    @Override
    public void updateRoom(List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> allRooms) {
        List<String> rooms = new ArrayList<>();
        for (int i = 0; i < allRooms.size(); i++) {
            InterfaceRoom.pbui_Item_MeetRoomDetailInfo room = allRooms.get(i);
            if (!room.getName().toStringUtf8().isEmpty()) {
                rooms.add(room.getName().toStringUtf8());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, rooms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_room.setAdapter(adapter);
        sp_room.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                InterfaceRoom.pbui_Item_MeetRoomDetailInfo item = allRooms.get(position);
                LogUtil.i(TAG, "updateRoom 选中会议室=" + item.getName().toStringUtf8());
                currentRoom(item.getRoomid());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (currentRoomId == 0) {
            if (!allRooms.isEmpty()) {
                InterfaceRoom.pbui_Item_MeetRoomDetailInfo item = allRooms.get(0);
                currentRoom(item.getRoomid());
            }
        }
    }

    @Override
    public void updateMeet(List<InterfaceMeet.pbui_Item_MeetMeetInfo> allMeets) {
        currentMeets.clear();
        for (int i = 0; i < allMeets.size(); i++) {
            InterfaceMeet.pbui_Item_MeetMeetInfo item = allMeets.get(i);
            if (item.getRoomId() == currentRoomId) {
                currentMeets.add(item);
            }
        }
        if (reserveMeetingAdapter == null) {
            reserveMeetingAdapter = new ReserveMeetingAdapter(R.layout.item_reserve_meeting, currentMeets);
            rv_meet.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_meet.setAdapter(reserveMeetingAdapter);
            reserveMeetingAdapter.setOnItemClickListener((adapter, view, position) -> {
                InterfaceMeet.pbui_Item_MeetMeetInfo meetInfo = currentMeets.get(position);
                reserveMeetingAdapter.setSelected(meetInfo.getId());
                currentMeet(meetInfo);
            });
        } else {
            reserveMeetingAdapter.notifyDataSetChanged();
        }
    }

    private void currentMeet(InterfaceMeet.pbui_Item_MeetMeetInfo info) {
        currentStartTime = info.getStartTime();
        currentEndTime = info.getEndTime();
        LogUtil.i(TAG, "currentMeet 更新底部显示UI currentStartTime=" + currentStartTime + ",currentEndTime=" + currentEndTime);
        edt_meet_name.setText(info.getName().toStringUtf8());
        sp_secret.setSelection(info.getSecrecy() == 1 ? 1 : 0);
        tv_start_time.setText(DateUtil.secondFormatDateTime(info.getStartTime()));
        tv_end_time.setText(DateUtil.secondFormatDateTime(info.getEndTime()));
        edt_reservation_person.setText(info.getOrdername().toStringUtf8());
    }

    private void currentRoom(int id) {
        currentRoomId = id;
        currentMeets.clear();
        for (int i = 0; i < presenter.allMeets.size(); i++) {
            InterfaceMeet.pbui_Item_MeetMeetInfo item = presenter.allMeets.get(i);
            if (item.getRoomId() == currentRoomId) {
                currentMeets.add(item);
            }
        }
        if (reserveMeetingAdapter != null) {
            reserveMeetingAdapter.notifyDataSetChanged();
        }
    }

    public void initView(View rootView) {
        this.sp_room = (Spinner) rootView.findViewById(R.id.sp_room);
        this.rv_meet = (RecyclerView) rootView.findViewById(R.id.rv_meet);
        this.edt_meet_name = (EditText) rootView.findViewById(R.id.edt_meet_name);
        this.sp_secret = (Spinner) rootView.findViewById(R.id.sp_secret);
        this.tv_start_time = (TextView) rootView.findViewById(R.id.tv_start_time);
        this.tv_end_time = (TextView) rootView.findViewById(R.id.tv_end_time);
        this.edt_reservation_person = (EditText) rootView.findViewById(R.id.edt_reservation_person);
        this.btn_create_reserve = (Button) rootView.findViewById(R.id.btn_create_reserve);
        this.btn_modify_reserve = (Button) rootView.findViewById(R.id.btn_modify_reserve);
        this.btn_delete_reserve = (Button) rootView.findViewById(R.id.btn_delete_reserve);
        this.btn_inform_start = (Button) rootView.findViewById(R.id.btn_inform_start);
        this.btn_inform_extension = (Button) rootView.findViewById(R.id.btn_inform_extension);
        this.btn_inform_cancel = (Button) rootView.findViewById(R.id.btn_inform_cancel);

        tv_start_time.setOnClickListener(this);
        tv_end_time.setOnClickListener(this);

        btn_create_reserve.setOnClickListener(this);
        btn_modify_reserve.setOnClickListener(this);
        btn_delete_reserve.setOnClickListener(this);
        btn_inform_start.setOnClickListener(this);
        btn_inform_extension.setOnClickListener(this);
        btn_inform_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_start_time: {
                if (currentStartTime != 0) {
                    Calendar instance = Calendar.getInstance();
                    instance.setTimeInMillis(currentStartTime * 1000);
                    mStartTimePickerView.setDate(instance);
                }
                mStartTimePickerView.show(tv_start_time);
                break;
            }
            case R.id.tv_end_time: {
                if (currentEndTime != 0) {
                    Calendar instance = Calendar.getInstance();
                    instance.setTimeInMillis(currentEndTime * 1000);
                    mEndTimePickerView.setDate(instance);
                }
                mEndTimePickerView.show(tv_end_time);
                break;
            }
            case R.id.btn_create_reserve: {
                createMeeting();
                break;
            }
            case R.id.btn_modify_reserve: {
                modifyMeeting();
                break;
            }
            case R.id.btn_delete_reserve: {
                deleteMeeting();
                break;
            }
            case R.id.btn_inform_start: {
                InterfaceMeet.pbui_Item_MeetMeetInfo selected = reserveMeetingAdapter.getSelected();
                if (selected == null) {
                    ToastUtil.show(R.string.please_choose_meeting_first);
                    break;
                }
                jni.informMeeting(InterfaceMember.Pb_MeetSMSOper.Pb_MEET_SMS_OPERTEMP_START_VALUE);
                break;
            }
            case R.id.btn_inform_extension: {
                InterfaceMeet.pbui_Item_MeetMeetInfo selected = reserveMeetingAdapter.getSelected();
                if (selected == null) {
                    ToastUtil.show(R.string.please_choose_meeting_first);
                    break;
                }
                jni.informMeeting(InterfaceMember.Pb_MeetSMSOper.Pb_MEET_SMS_OPERTEMP_LATE_VALUE);
                break;
            }
            case R.id.btn_inform_cancel: {
                InterfaceMeet.pbui_Item_MeetMeetInfo selected = reserveMeetingAdapter.getSelected();
                if (selected == null) {
                    ToastUtil.show(R.string.please_choose_meeting_first);
                    break;
                }
                jni.informMeeting(InterfaceMember.Pb_MeetSMSOper.Pb_MEET_SMS_OPERTEMP_CANCLE_VALUE);
                break;
            }
            default:
                break;
        }
    }

    private void deleteMeeting() {
        InterfaceMeet.pbui_Item_MeetMeetInfo selected = reserveMeetingAdapter.getSelected();
        if (selected == null) {
            ToastUtil.show(R.string.please_choose_meeting_first);
            return;
        }
        jni.delMeeting(selected);
    }

    private void modifyMeeting() {
        InterfaceMeet.pbui_Item_MeetMeetInfo selected = reserveMeetingAdapter.getSelected();
        if (selected == null) {
            ToastUtil.show(R.string.please_choose_meeting_first);
            return;
        }
        String meetingName = edt_meet_name.getText().toString().trim();
        if (TextUtils.isEmpty(meetingName)) {
            ToastUtil.show(R.string.please_enter_meeting_name);
            return;
        }
        if (tv_start_time.getText().toString().trim().isEmpty()
                || tv_end_time.getText().toString().trim().isEmpty()) {
            ToastUtil.show(R.string.please_choose_time);
            return;
        }
        LogUtil.i(TAG, "modifyMeeting currentStartTime=" + currentStartTime + ",currentEndTime=" + currentEndTime);
        if (currentStartTime > currentEndTime) {
            ToastUtil.show(R.string.err_start_time);
            return;
        }
        String trim = edt_reservation_person.getText().toString().trim();
        InterfaceMeet.pbui_Item_MeetMeetInfo build = InterfaceMeet.pbui_Item_MeetMeetInfo.newBuilder()
                .setId(selected.getId())
                .setName(s2b(meetingName))
                .setRoomId(selected.getRoomId())
                .setRoomname(selected.getRoomname())
                .setSecrecy(sp_secret.getSelectedItemPosition())
                .setStartTime(currentStartTime)
                .setEndTime(currentEndTime)
                .setSigninType(selected.getSigninType())
                .setManagerid(selected.getManagerid())
                .setOnepswSignin(selected.getOnepswSignin())
                .setStatus(selected.getStatus())
                .setOrdername(s2b(trim))
                .build();
        jni.modifyMeeting(build);
    }

    private void createMeeting() {
        String name = edt_meet_name.getText().toString().trim();
        if (name.isEmpty()) {
            ToastUtil.show(R.string.please_enter_meeting_name);
            return;
        }
        if (tv_start_time.getText().toString().trim().isEmpty()
                || tv_end_time.getText().toString().trim().isEmpty()) {
            ToastUtil.show(R.string.please_choose_time);
            return;
        }
        if (currentStartTime > currentEndTime) {
            ToastUtil.show(R.string.err_start_time);
            return;
        }
        InterfaceMeet.pbui_Item_MeetMeetInfo build = InterfaceMeet.pbui_Item_MeetMeetInfo.newBuilder()
                .setName(s2b(name))
                .setRoomId(currentRoomId)
                .setSecrecy(sp_secret.getSelectedItemPosition())
                .setStartTime(currentStartTime)
                .setEndTime(currentEndTime)
                .setOrdername(s2b(edt_reservation_person.getText().toString()))
                .build();
        jni.addMeeting(build);
    }
}
