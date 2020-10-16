package xlk.paperless.standard.view.admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.AdminRvAdapter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.bean.AdminFunctionBean;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.base.BaseActivity;
import xlk.paperless.standard.view.admin.fragment.pre.meetingManage.MeetingManageFragment;
import xlk.paperless.standard.view.admin.fragment.system.device.AdminDeviceManageFragment;
import xlk.paperless.standard.view.admin.fragment.system.member.AdminMemberFragment;
import xlk.paperless.standard.view.admin.fragment.system.other.AdminOtherFragment;
import xlk.paperless.standard.view.admin.fragment.system.room.AdminRoomManageFragment;
import xlk.paperless.standard.view.admin.fragment.system.secretary.AdminSecretaryManageFragment;
import xlk.paperless.standard.view.main.MainActivity;

/**
 * @author Administrator
 */
public class AdminActivity extends BaseActivity implements AdminInterface {

    private final String TAG = "AdminActivity-->";
    private TextView admin_tv_meet_status;
    private TextView admin_tv_meet_name;
    private TextView admin_tv_user, admin_tv_seat, admin_tv_online;
    private ImageView admin_iv_close;
    private RecyclerView admin_rv_level1;
    private RecyclerView admin_rv_level2;
    private FrameLayout admin_fl;
    private LinearLayout admin_ll;
    List<AdminFunctionBean> level1FunctionBeans = new ArrayList<>();
    List<AdminFunctionBean> level2FunctionBeans = new ArrayList<>();
    private AdminRvAdapter level1RvAdapter, level2RvAdapter;
    private int mLevel1Index;
    private int mLevel2Index;
    private AdminPresenter presenter;
    private String adminName;
    private int adminId;
    private AdminDeviceManageFragment adminDeviceManageFragment;
    private AdminRoomManageFragment adminRoomManageFragment;
    private AdminSecretaryManageFragment adminSecretaryManageFragment;
    private AdminMemberFragment adminMemberFragment;
    private AdminOtherFragment adminOtherFragment;

    private MeetingManageFragment meetingManageFragment;

    private int currentMeetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        initView();
        adminName = getIntent().getStringExtra(Constant.EXTRA_ADMIN_NAME);
        adminId = getIntent().getIntExtra(Constant.EXTRA_ADMIN_ID, -1);
        admin_tv_user.setText(getString(R.string.user_, adminName));
        presenter = new AdminPresenter(this, this);

        presenter.queryCurrentMeeting();
        presenter.queryLocalDeviceInfo();
        presenter.queryOnline();
        //四个大类
        level1FunctionBeans.add(new AdminFunctionBean(R.drawable.menu_system));
        level1FunctionBeans.add(new AdminFunctionBean(R.drawable.menu_pre));
        level1FunctionBeans.add(new AdminFunctionBean(R.drawable.menu_mid));
        level1FunctionBeans.add(new AdminFunctionBean(R.drawable.menu_end));
        level1RvAdapter = new AdminRvAdapter(level1FunctionBeans);
        admin_rv_level1.setLayoutManager(new LinearLayoutManager(this));
        admin_rv_level1.setAdapter(level1RvAdapter);
        level1RvAdapter.setOnItemClickListener((adapter, view, position) -> {
            level1RvAdapter.setSelect(position);
            showLevel2(position);
        });
        showLevel2(mLevel1Index);
    }


    private void showLevel2(int index) {
        if (mLevel1Index != index) {
            mLevel2Index = 0;
            if (level2RvAdapter != null) {
                level2RvAdapter.setSelect(0);
            }
        }
        mLevel1Index = index;
        level2FunctionBeans.clear();
        switch (mLevel1Index) {
            case 0:
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_dev));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_room));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_seat));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_secretary));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_people));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_other));
                break;
            case 1:
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_meet));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_agenda));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_member));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_file));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_camera));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_voteset));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_electionset));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_seatset));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_tablecardset));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_functionset));
                break;
            case 2:
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_devctrl));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_votectrl));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_electionctrl));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_message));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_cameractrl));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_screenctrl));
                break;
            case 3:
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_checkin));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_notereview));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_votereview));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_electionreview));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_cameractrl));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_meetintzip));
                break;
            default:
                break;
        }
        if (level2RvAdapter == null) {
            level2RvAdapter = new AdminRvAdapter(level2FunctionBeans);
            admin_rv_level2.setLayoutManager(new LinearLayoutManager(this));
            admin_rv_level2.setAdapter(level2RvAdapter);
            level2RvAdapter.setOnItemClickListener((adapter, view, position) -> {
                mLevel2Index = position;
                level2RvAdapter.setSelect(position);
                showFragment(mLevel1Index, mLevel2Index);
            });
        } else {
            level2RvAdapter.notifyDataSetChanged();
        }
        showFragment(mLevel1Index, mLevel2Index);
    }

    private void showFragment(int level1Index, int level2Index) {
        LogUtil.i(TAG, "showFragment level1Index=" + level1Index + ",level2Index=" + level2Index);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        hideFragment(ft);
        switch (level1Index) {
            case 0:
                sysFragment(level2Index, ft);
                break;
            case 1:
                preFragment(level2Index, ft);
                break;
            default:
                break;
        }
        ft.commitAllowingStateLoss();//允许状态丢失，其他完全一样
//        ft.commit();//出现异常：Can not perform this action after onSaveInstanceState
    }

    private void preFragment(int level2Index, FragmentTransaction ft) {

        switch (level2Index) {
            case 0:
                if (meetingManageFragment == null) {
                    meetingManageFragment = new MeetingManageFragment();
                    ft.add(R.id.admin_fl, meetingManageFragment);
                }
                ft.show(meetingManageFragment);
                break;
            default:
                break;
        }
    }

    private void sysFragment(int level2Index, FragmentTransaction ft) {
        switch (level2Index) {
            case 0:
                if (adminDeviceManageFragment == null) {
                    adminDeviceManageFragment = new AdminDeviceManageFragment();
                    ft.add(R.id.admin_fl, adminDeviceManageFragment);
                }
                ft.show(adminDeviceManageFragment);
                break;
            case 1:
                if (adminRoomManageFragment == null) {
                    adminRoomManageFragment = new AdminRoomManageFragment();
                    ft.add(R.id.admin_fl, adminRoomManageFragment);
                }
                ft.show(adminRoomManageFragment);
                break;
            case 3:
                if (adminSecretaryManageFragment == null) {
                    adminSecretaryManageFragment = new AdminSecretaryManageFragment();
                    ft.add(R.id.admin_fl, adminSecretaryManageFragment);
                }
                ft.show(adminSecretaryManageFragment);
                break;
            case 4:
                if (adminMemberFragment == null) {
                    adminMemberFragment = new AdminMemberFragment();
                    ft.add(R.id.admin_fl, adminMemberFragment);
                }
                ft.show(adminMemberFragment);
                break;
            case 5:
                if (adminOtherFragment == null) {
                    adminOtherFragment = new AdminOtherFragment();
                    ft.add(R.id.admin_fl, adminOtherFragment);
                }
                ft.show(adminOtherFragment);
                break;
            default:
                break;
        }
    }

    private void hideFragment(FragmentTransaction ft) {
        //系统设置
        if (adminDeviceManageFragment != null) ft.hide(adminDeviceManageFragment);
        if (adminRoomManageFragment != null) ft.hide(adminRoomManageFragment);
        if (adminSecretaryManageFragment != null) ft.hide(adminSecretaryManageFragment);
        if (adminMemberFragment != null) ft.hide(adminMemberFragment);
        if (adminOtherFragment != null) ft.hide(adminOtherFragment);
        // 会前设置
        if (meetingManageFragment != null) ft.hide(meetingManageFragment);
    }

    private void initView() {
        admin_tv_meet_status = (TextView) findViewById(R.id.admin_tv_meet_status);
        admin_tv_meet_name = (TextView) findViewById(R.id.admin_tv_meet_name);
        admin_tv_user = (TextView) findViewById(R.id.admin_tv_user);
        admin_tv_seat = (TextView) findViewById(R.id.admin_tv_seat);
        admin_tv_online = (TextView) findViewById(R.id.admin_tv_online);
        admin_iv_close = (ImageView) findViewById(R.id.admin_iv_close);
        admin_rv_level1 = (RecyclerView) findViewById(R.id.admin_rv_level1);
        admin_rv_level2 = (RecyclerView) findViewById(R.id.admin_rv_level2);
        admin_fl = (FrameLayout) findViewById(R.id.admin_fl);
        admin_ll = (LinearLayout) findViewById(R.id.admin_ll);
        admin_iv_close.setOnClickListener(v -> exit());
    }

    @Override
    public void updateMeetStatus(int meetId, int status) {
        currentMeetId = meetId;
        LogUtil.i(TAG, "updateMeetStatus meetId=" + meetId + ",status=" + status);
        switch (status) {
            case 0:
                admin_tv_meet_status.setText(getString(R.string.meet_status_not_initiated));
                break;
            case 1:
                admin_tv_meet_status.setText(getString(R.string.meet_status_ongoing));
                break;
            case 2:
                admin_tv_meet_status.setText(getString(R.string.meet_status_isover));
                break;
            default:
                admin_tv_meet_status.setText(getString(R.string.meet_status_no_select_meet));
                break;
        }
    }

    @Override
    public void updateMeetName(String meetName) {
        admin_tv_meet_name.setText(meetName);
    }

    @Override
    public void updateDeviceName(String devName) {
        admin_tv_seat.setText(getString(R.string.set_name_, devName));
    }

    @Override
    public void updateOnlineStatus(boolean onLine) {
        admin_tv_online.setText(onLine ? getString(R.string.online) : getString(R.string.offline));
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    private void exit() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
