package xlk.paperless.standard.view.admin.fragment.mid.devcontrol;

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

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.bean.DevControlBean;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.admin.fragment.pre.member.MemberRoleAdapter;
import xlk.paperless.standard.view.admin.fragment.pre.member.MemberRoleBean;

/**
 * @author Created by xlk on 2020/10/24.
 * @desc
 */
public class AdminDevControlFragment extends BaseFragment implements AdminDevControlInterface, View.OnClickListener {
    private RecyclerView rv_device;
    private CheckBox cb_check_all;
    private CheckBox cb_check_lift;
    private CheckBox cb_check_mike;
    private Button btn_restart_app;
    private Button btn_restart;
    private Button btn_shutdown;
    private Button btn_singin;
    private Button btn_rise;
    private Button btn_stop;
    private Button btn_decline;
    private Button btn_member_role;
    private Button btn_wake_online;
    private Button btn_document;
    private AdminDevControlPresenter presenter;
    private AdminDevControlAdapter devControlAdapter;
    private PopupWindow memberRolePop;
    private RecyclerView rv_member_role;
    private MemberRoleAdapter memberRoleAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_device_control, container, false);
        initView(inflate);
        presenter = new AdminDevControlPresenter(this);
        presenter.queryRankInfo();
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    protected void reShow() {
        presenter.queryRankInfo();
    }

    public void initView(View rootView) {
        this.rv_device = (RecyclerView) rootView.findViewById(R.id.rv_device);
        this.cb_check_all = (CheckBox) rootView.findViewById(R.id.cb_check_all);
        this.cb_check_lift = (CheckBox) rootView.findViewById(R.id.cb_check_lift);
        this.cb_check_mike = (CheckBox) rootView.findViewById(R.id.cb_check_mike);
        this.btn_restart_app = (Button) rootView.findViewById(R.id.btn_restart_app);
        this.btn_restart = (Button) rootView.findViewById(R.id.btn_restart);
        this.btn_shutdown = (Button) rootView.findViewById(R.id.btn_shutdown);
        this.btn_singin = (Button) rootView.findViewById(R.id.btn_singin);
        this.btn_rise = (Button) rootView.findViewById(R.id.btn_rise);
        this.btn_stop = (Button) rootView.findViewById(R.id.btn_stop);
        this.btn_decline = (Button) rootView.findViewById(R.id.btn_decline);
        this.btn_member_role = (Button) rootView.findViewById(R.id.btn_member_role);
        this.btn_wake_online = (Button) rootView.findViewById(R.id.btn_wake_online);
        this.btn_document = (Button) rootView.findViewById(R.id.btn_document);
        this.cb_check_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb_check_all.setChecked(cb_check_all.isChecked());
                devControlAdapter.setCheckAll(cb_check_all.isChecked());
            }
        });
        this.btn_member_role.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.queryAttendPeople();
                showMemberRole(presenter.getDevSeatInfos());
            }
        });
        this.btn_restart_app.setOnClickListener(this);
        this.btn_restart.setOnClickListener(this);
        this.btn_shutdown.setOnClickListener(this);
        this.btn_singin.setOnClickListener(this);
        this.btn_rise.setOnClickListener(this);
        this.btn_stop.setOnClickListener(this);
        this.btn_decline.setOnClickListener(this);
        this.btn_wake_online.setOnClickListener(this);
        this.btn_document.setOnClickListener(this);
    }

    @Override
    public void updateRv(List<DevControlBean> devControlBeans) {
        if (devControlAdapter == null) {
            devControlAdapter = new AdminDevControlAdapter(R.layout.item_admin_device_coltrol, devControlBeans);
            rv_device.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_device.setAdapter(devControlAdapter);
            devControlAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    devControlAdapter.setChecked(devControlBeans.get(position).getDeviceInfo().getDevcieid());
                    cb_check_all.setChecked(devControlAdapter.isCheckedAll());
                }
            });
        } else {
            devControlAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateRoleRv(List<MemberRoleBean> devSeatInfos) {
        if (memberRolePop != null && memberRolePop.isShowing()) {
            LogUtil.i(TAG, "updateRoleRv ");
            memberRoleAdapter.notifyDataSetChanged();
        }
    }

    private void showMemberRole(List<MemberRoleBean> devSeatInfos) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_member_role, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showMemberRole fragment的大小 width=" + width + ",height=" + height);
        memberRolePop = new PopupWindow(inflate, width, height);
        memberRolePop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        memberRolePop.setTouchable(true);
        // true:设置触摸外面时消失
        memberRolePop.setOutsideTouchable(true);
        memberRolePop.setFocusable(true);
        memberRolePop.setAnimationStyle(R.style.pop_Animation);
        memberRolePop.showAtLocation(btn_member_role, Gravity.END | Gravity.BOTTOM, 0, 0);
        rv_member_role = inflate.findViewById(R.id.rv_member_role);
        Spinner sp_role = inflate.findViewById(R.id.sp_role);
        memberRoleAdapter = new MemberRoleAdapter(R.layout.item_member_role, devSeatInfos);
        rv_member_role.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_member_role.setAdapter(memberRoleAdapter);
        memberRoleAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                MemberRoleBean item = devSeatInfos.get(position);
                memberRoleAdapter.setSelected(item.getMember().getPersonid());
                int index;
                int role = item.getSeat() != null ? item.getSeat().getRole() : 0;
                switch (role) {
                    case InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal_VALUE:
                        index = 1;
                        break;
                    case InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere_VALUE:
                        index = 2;
                        break;
                    case InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE:
                        index = 3;
                        break;
                    case InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin_VALUE:
                        index = 4;
                        break;
                    default:
                        index = 0;
                        break;
                }
                sp_role.setSelection(index);
            }
        });
        inflate.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            MemberRoleBean selected = memberRoleAdapter.getSelected();
            if (selected == null) {
                ToastUtil.show(R.string.please_choose_member);
                return;
            }
            if (selected.getSeat() == null) {
                ToastUtil.show(R.string.please_choose_bind_member);
                return;
            }
            int index = sp_role.getSelectedItemPosition();
            int newRole;
            switch (index) {
                case 1:
                    newRole = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal_VALUE;
                    break;
                case 2:
                    newRole = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere_VALUE;
                    break;
                case 3:
                    newRole = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE;
                    break;
                case 4:
                    newRole = InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin_VALUE;
                    break;
                default:
                    newRole = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_nouser_VALUE;
                    break;
            }
            presenter.modifyMemberRole(selected.getMember().getPersonid(), newRole, selected.getSeat().getDevid());
        });
        inflate.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            memberRolePop.dismiss();
        });
    }

    @Override
    public void onClick(View v) {
        if (devControlAdapter == null) {
            return;
        }
        if (devControlAdapter.getCheckIds().isEmpty()) {
            ToastUtil.show(R.string.please_choose_device_first);
            return;
        }
        switch (v.getId()) {
            //软件重启
            case R.id.btn_restart_app:
                presenter.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_PROGRAMRESTART_VALUE,
                        devControlAdapter.getCheckIds());
                break;
            //设备重启
            case R.id.btn_restart:
                presenter.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_REBOOT_VALUE,
                        devControlAdapter.getCheckIds());
                break;
            //关机
            case R.id.btn_shutdown:
                presenter.executeTerminalControl(InterfaceMacro.Pb_DeviceControlFlag.Pb_DEVICECONTORL_SHUTDOWN_VALUE,
                        devControlAdapter.getCheckIds());
                break;
            //辅助签到
            case R.id.btn_singin:
                presenter.signAlterationOperate(devControlAdapter.getCheckIds());
                break;
            //上升
            case R.id.btn_rise: {
                int value = 0;
                if (cb_check_lift.isChecked()) {
                    value = InterfaceMacro.Pb_LiftFlag.Pb_LIFT_FLAG_MACHICE_VALUE;
                }
                if (cb_check_mike.isChecked()) {
                    value = value | InterfaceMacro.Pb_LiftFlag.Pb_LIFT_FLAG_MIC_VALUE;
                }
                if (value == 0) {
                    ToastUtil.show(R.string.please_choose_lift_or_mike);
                }
                presenter.rise(value, devControlAdapter.getCheckIds());
                break;
            }
            //停止
            case R.id.btn_stop: {
                int value = 0;
                if (cb_check_lift.isChecked()) {
                    value = InterfaceMacro.Pb_LiftFlag.Pb_LIFT_FLAG_MACHICE_VALUE;
                }
                if (cb_check_mike.isChecked()) {
                    value = value | InterfaceMacro.Pb_LiftFlag.Pb_LIFT_FLAG_MIC_VALUE;
                }
                if (value == 0) {
                    ToastUtil.show(R.string.please_choose_lift_or_mike);
                }
                presenter.stop(value, devControlAdapter.getCheckIds());
                break;
            }
            //下降
            case R.id.btn_decline: {
                int value = 0;
                if (cb_check_lift.isChecked()) {
                    value = InterfaceMacro.Pb_LiftFlag.Pb_LIFT_FLAG_MACHICE_VALUE;
                }
                if (cb_check_mike.isChecked()) {
                    value = value | InterfaceMacro.Pb_LiftFlag.Pb_LIFT_FLAG_MIC_VALUE;
                }
                if (value == 0) {
                    ToastUtil.show(R.string.please_choose_lift_or_mike);
                }
                presenter.decline(value, devControlAdapter.getCheckIds());
                break;
            }
            //网络唤醒
            case R.id.btn_wake_online:
                presenter.wakeOnLan(devControlAdapter.getCheckIds());
                break;
            //外部文档打开
            case R.id.btn_document:
                // TODO: 2020/10/24 重新设置设备的flag 修改设备信息
//                presenter.externalOpen(devControlAdapter.getCheckIds());
                break;
            default:
                break;
        }
    }
}
