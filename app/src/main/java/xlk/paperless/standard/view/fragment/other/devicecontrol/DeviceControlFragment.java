package xlk.paperless.standard.view.fragment.other.devicecontrol;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.DevControlAdapter;
import xlk.paperless.standard.util.PopUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BaseFragment;

/**
 * @author xlk
 * @date 2020/4/1
 * @desc 设备控制
 */
public class DeviceControlFragment extends BaseFragment implements View.OnClickListener, IDevControl {
    private CheckBox dev_control_all_ab;
    private LinearLayout linearLayout;
    private RecyclerView f_dev_control_rv;
    private Button dev_control_rise;
    private Button dev_control_stop;
    private Button dev_control_decline;
    private Button dev_control_app_restart;
    private Button dev_control_terminal_restart;
    private Button dev_control_terminal_shoutdown;
    private Button dev_control_wake_on;
    private Button dev_control_open_document;
    private Button dev_control_signin;
    private Button dev_control_set_role;
    private CheckBox dev_control_elevator_cb;
    private CheckBox dev_control_microphone_cb;
    private DeviceControlPresenter presenter;
    private DevControlAdapter devControlAdapter;
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_device_control, container, false);
        initView(inflate);
        presenter = new DeviceControlPresenter(getContext(), this);
        String[] stringArray = getResources().getStringArray(R.array.role);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, stringArray);
        presenter.queryRankInfo();
        return inflate;
    }

    @Override
    public void updateRv() {
        if (devControlAdapter == null) {
            devControlAdapter = new DevControlAdapter(R.layout.item_dev_control, presenter.devControlBeans);
            f_dev_control_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            f_dev_control_rv.setAdapter(devControlAdapter);
        } else {
            devControlAdapter.notifyDataSetChanged();
            devControlAdapter.notifyChoose();
        }
        devControlAdapter.setOnItemClickListener((adapter, view, position) -> {
            devControlAdapter.choose(presenter.devControlBeans.get(position).getDeviceInfo().getDevcieid());
            dev_control_all_ab.setChecked(devControlAdapter.isCheckAll());
        });
        dev_control_all_ab.setOnClickListener(v -> {
            boolean checked = dev_control_all_ab.isChecked();
            dev_control_all_ab.setChecked(checked);
            devControlAdapter.setChooseAll(checked);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    private void initView(View inflate) {
        dev_control_all_ab = (CheckBox) inflate.findViewById(R.id.dev_control_all_ab);
        linearLayout = (LinearLayout) inflate.findViewById(R.id.linearLayout);
        f_dev_control_rv = (RecyclerView) inflate.findViewById(R.id.f_dev_control_rv);
        dev_control_rise = (Button) inflate.findViewById(R.id.dev_control_rise);
        dev_control_stop = (Button) inflate.findViewById(R.id.dev_control_stop);
        dev_control_decline = (Button) inflate.findViewById(R.id.dev_control_decline);
        dev_control_app_restart = (Button) inflate.findViewById(R.id.dev_control_app_restart);
        dev_control_terminal_restart = (Button) inflate.findViewById(R.id.dev_control_terminal_restart);
        dev_control_terminal_shoutdown = (Button) inflate.findViewById(R.id.dev_control_terminal_shoutdown);
        dev_control_wake_on = (Button) inflate.findViewById(R.id.dev_control_wake_on);
        dev_control_open_document = (Button) inflate.findViewById(R.id.dev_control_open_document);
        dev_control_signin = (Button) inflate.findViewById(R.id.dev_control_signin);
        dev_control_set_role = (Button) inflate.findViewById(R.id.dev_control_set_role);
        dev_control_elevator_cb = (CheckBox) inflate.findViewById(R.id.dev_control_elevator_cb);
        dev_control_microphone_cb = (CheckBox) inflate.findViewById(R.id.dev_control_microphone_cb);

        dev_control_rise.setOnClickListener(this);
        dev_control_stop.setOnClickListener(this);
        dev_control_decline.setOnClickListener(this);
        dev_control_app_restart.setOnClickListener(this);
        dev_control_terminal_restart.setOnClickListener(this);
        dev_control_terminal_shoutdown.setOnClickListener(this);
        dev_control_wake_on.setOnClickListener(this);
        dev_control_open_document.setOnClickListener(this);
        dev_control_signin.setOnClickListener(this);
        dev_control_set_role.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (devControlAdapter == null) {
            return;
        }
        if (devControlAdapter.getChooseIds().isEmpty()) {
            ToastUtil.show(R.string.err_target_NotNull);
            return;
        }
        switch (v.getId()) {
            //上升
            case R.id.dev_control_rise: {
                int value = 0;
                if (dev_control_elevator_cb.isChecked()) {
                    value = InterfaceMacro.Pb_LiftFlag.Pb_LIFT_FLAG_MACHICE_VALUE;
                }
                if (dev_control_microphone_cb.isChecked()) {
                    value = value | InterfaceMacro.Pb_LiftFlag.Pb_LIFT_FLAG_MIC_VALUE;
                }
                if (value == 0) {
                    ToastUtil.show(R.string.please_choose_lift_or_mike);
                }
                presenter.rise(value, devControlAdapter.getChooseIds());
                break;
            }
            //停止
            case R.id.dev_control_stop: {
                int value = 0;
                if (dev_control_elevator_cb.isChecked()) {
                    value = InterfaceMacro.Pb_LiftFlag.Pb_LIFT_FLAG_MACHICE_VALUE;
                }
                if (dev_control_microphone_cb.isChecked()) {
                    value = value | InterfaceMacro.Pb_LiftFlag.Pb_LIFT_FLAG_MIC_VALUE;
                }
                if (value == 0) {
                    ToastUtil.show(R.string.please_choose_lift_or_mike);
                }
                presenter.stop(value, devControlAdapter.getChooseIds());
                break;
            }
            //下降
            case R.id.dev_control_decline: {
                int value = 0;
                if (dev_control_elevator_cb.isChecked()) {
                    value = InterfaceMacro.Pb_LiftFlag.Pb_LIFT_FLAG_MACHICE_VALUE;
                }
                if (dev_control_microphone_cb.isChecked()) {
                    value = value | InterfaceMacro.Pb_LiftFlag.Pb_LIFT_FLAG_MIC_VALUE;
                }
                if (value == 0) {
                    ToastUtil.show(R.string.please_choose_lift_or_mike);
                }
                presenter.decline(value, devControlAdapter.getChooseIds());
                break;
            }
            //软件重启
            case R.id.dev_control_app_restart:
                presenter.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_PROGRAMRESTART_VALUE, devControlAdapter.getChooseIds());
                break;
            //终端重启
            case R.id.dev_control_terminal_restart:
                presenter.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_REBOOT_VALUE, devControlAdapter.getChooseIds());
                break;
            //终端关机
            case R.id.dev_control_terminal_shoutdown:
                presenter.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_SHUTDOWN_VALUE, devControlAdapter.getChooseIds());
                break;
            //网络唤醒
            case R.id.dev_control_wake_on:
                jni.wakeOnLan(devControlAdapter.getChooseIds());
                break;
            //外部打开文档
            case R.id.dev_control_open_document:

                break;
            //辅助签到
            case R.id.dev_control_signin:
                presenter.signAlterationOperate(devControlAdapter.getChooseIds());
                break;
            //角色设定
            case R.id.dev_control_set_role:
                showSetRolePop();
                break;
            default:
                break;
        }
    }

    int memberId = -1;
    int devId = -1;

    private void showSetRolePop() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_role_spinner, null);
        PopupWindow popupWindow = PopUtil.create(inflate, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,  dev_control_set_role);
        Spinner spinner = inflate.findViewById(R.id.pop_role_sp);
        spinner.setAdapter(adapter);
        inflate.findViewById(R.id.pop_role_determine).setOnClickListener(v -> {
            int index = spinner.getSelectedItemPosition();
            int role = 0;
            if (index == 1)
                role = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal.getNumber();
            else if (index == 2)
                role = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere.getNumber();
            else if (index == 3)
                role = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary.getNumber();
            else if (index == 4) role = InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin.getNumber();
            if (devControlAdapter.getChooseIds().size() != 1) {
                ToastUtil.show(R.string.must_chooose_one);
                return;
            } else {
                for (int i = 0; i < presenter.devControlBeans.size(); i++) {
                    InterfaceDevice.pbui_Item_DeviceDetailInfo deviceInfo = presenter.devControlBeans.get(i).getDeviceInfo();
                    if (deviceInfo.getDevcieid() == devControlAdapter.getChooseIds().get(0)) {
                        if (deviceInfo.getMemberid() != 0) {
                            memberId = deviceInfo.getMemberid();
                            devId = deviceInfo.getDevcieid();
                            break;
                        } else {
                            ToastUtil.show(R.string.please_choose_member);
                            return;
                        }
                    }
                }
            }
            //证明是参会人
            presenter.modifMeetRanking(memberId, role, devId);
            popupWindow.dismiss();
        });
        inflate.findViewById(R.id.pop_role_cancel).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        popupWindow.setOnDismissListener(() -> {
            memberId = -1;
            devId = -1;
        });

    }
}
