package xlk.paperless.standard.view.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceMeet;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.AdminRvAdapter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.AdminFunctionBean;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.base.BaseActivity;
import xlk.paperless.standard.view.admin.fragment.after.annotation.AdminAnnotationFragment;
import xlk.paperless.standard.view.admin.fragment.after.archive.ArchiveFragment;
import xlk.paperless.standard.view.admin.fragment.after.signin.AdminSignInFragment;
import xlk.paperless.standard.view.admin.fragment.after.vote.VoteResultFragment;
import xlk.paperless.standard.view.admin.fragment.after.vote.VoteResultPresenter;
import xlk.paperless.standard.view.admin.fragment.mid.camera.AdminCameraControlFragment;
import xlk.paperless.standard.view.admin.fragment.mid.chat.AdminChatFragment;
import xlk.paperless.standard.view.admin.fragment.mid.devcontrol.AdminDevControlFragment;
import xlk.paperless.standard.view.admin.fragment.mid.votemanage.AdminVoteManageFragment;
import xlk.paperless.standard.view.admin.fragment.mid.votemanage.AdminVoteManagePresenter;
import xlk.paperless.standard.view.admin.fragment.pre.agenda.AdminAgendaFragment;
import xlk.paperless.standard.view.admin.fragment.pre.bind.SeatBindFragment;
import xlk.paperless.standard.view.admin.fragment.pre.camera.AdminCameraManageFragment;
import xlk.paperless.standard.view.admin.fragment.pre.election.AdminElectionFragment;
import xlk.paperless.standard.view.admin.fragment.pre.file.AdminFileFragment;
import xlk.paperless.standard.view.admin.fragment.pre.function.FunctionFragment;
import xlk.paperless.standard.view.admin.fragment.pre.meetingManage.MeetingManageFragment;
import xlk.paperless.standard.view.admin.fragment.pre.member.MemberFragment;
import xlk.paperless.standard.view.admin.fragment.pre.tablecard.TableCardFragment;
import xlk.paperless.standard.view.admin.fragment.pre.vote.AdminVoteFragment;
import xlk.paperless.standard.view.admin.fragment.reserve.meet.ReserveMeetingFragment;
import xlk.paperless.standard.view.admin.fragment.reserve.email.SendEmailFragment;
import xlk.paperless.standard.view.admin.fragment.reserve.task.TaskManagerFragment;
import xlk.paperless.standard.view.admin.fragment.system.device.AdminDeviceManageFragment;
import xlk.paperless.standard.view.admin.fragment.system.member.FrequentlyMemberFragment;
import xlk.paperless.standard.view.admin.fragment.system.other.AdminOtherFragment;
import xlk.paperless.standard.view.admin.fragment.system.room.AdminRoomManageFragment;
import xlk.paperless.standard.view.admin.fragment.system.seat.SeatArrangementFragment;
import xlk.paperless.standard.view.admin.fragment.system.secretary.AdminSecretaryManageFragment;
import xlk.paperless.standard.view.fragment.other.screen.ScreenFragment;
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
    List<AdminFunctionBean> level1FunctionBeans = new ArrayList<>();
    List<AdminFunctionBean> level2FunctionBeans = new ArrayList<>();
    private AdminRvAdapter level1RvAdapter, level2RvAdapter;
    private int mLevel1Index;
    private int mLevel2Index;
    private AdminPresenter presenter;
    private String adminName;
    public static int currentAdminId;
    private int currentMeetId;
    /**
     * 系统设置
     */
    private AdminDeviceManageFragment adminDeviceManageFragment;
    private AdminRoomManageFragment adminRoomManageFragment;
    private SeatArrangementFragment seatArrangementFragment;
    private AdminSecretaryManageFragment adminSecretaryManageFragment;
    private FrequentlyMemberFragment adminMemberFragment;
    private AdminOtherFragment adminOtherFragment;
    /**
     * 会议预约
     */
    private ReserveMeetingFragment reserveMeetingFragment;
    private SendEmailFragment sendEmailFragment;
    private TaskManagerFragment taskManagerFragment;

    /**
     * 会前设置
     */
    private MeetingManageFragment meetingManageFragment;
    private AdminAgendaFragment adminAgendaFragment;
    private MemberFragment memberFragment;
    private AdminFileFragment adminFileFragment;
    private AdminCameraManageFragment adminCameraManageFragment;
    private AdminVoteFragment adminVoteFragment;
    private AdminElectionFragment adminElectionFragment;
    private SeatBindFragment seatBindFragment;
    private TableCardFragment tableCardFragment;
    private FunctionFragment functionFragment;
    /**
     * 会中管理
     */
    private AdminDevControlFragment adminDevControlFragment;
    private AdminVoteManageFragment adminVoteManageFragment;
    private AdminChatFragment adminChatFragment;
    private AdminCameraControlFragment adminCameraControlFragment;
    private ScreenFragment screenFragment;
    /**
     * 会后查看
     */
    private AdminSignInFragment adminSignInFragment;
    private AdminAnnotationFragment adminAnnotationFragment;
    private VoteResultFragment voteResultFragment;
    private ArchiveFragment archiveFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Values.isFromAdminOpenWps = true;
        ScreenFragment.isAdminPage = true;
        initView();
        adminName = getIntent().getStringExtra(Constant.EXTRA_ADMIN_NAME);
        currentAdminId = getIntent().getIntExtra(Constant.EXTRA_ADMIN_ID, -1);
        admin_tv_user.setText(getString(R.string.user_, adminName));
        presenter = new AdminPresenter(this, this);

        presenter.queryLocalDeviceInfo();
        presenter.queryOnline();
        presenter.queryCurrentMeeting();
        //四个大类
        level1FunctionBeans.add(new AdminFunctionBean(R.drawable.menu_system));
        level1FunctionBeans.add(new AdminFunctionBean(R.drawable.menu_order));
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
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.menu_order));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.ic_sendemail));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.ic_taskmanagement));
                break;
            case 2:
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
            case 3:
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_devctrl));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_votectrl));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_electionctrl));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_message));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_cameractrl));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_screenctrl));
                break;
            case 4:
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_checkin));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_notereview));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_votereview));
                level2FunctionBeans.add(new AdminFunctionBean(R.drawable.icon_admin_electionreview));
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
                reserveFragment(level2Index, ft);
                break;
            case 2:
                preFragment(level2Index, ft);
                break;
            case 3:
                midFragment(level2Index, ft);
                break;
            case 4:
                afterFragment(level2Index, ft);
                break;
            default:
                break;
        }
        ft.commitAllowingStateLoss();//允许状态丢失，其他完全一样
//        ft.commit();//出现异常：Can not perform this action after onSaveInstanceState
    }

    private void afterFragment(int level2Index, FragmentTransaction ft) {
        switch (level2Index) {
            case 0:
                if (adminSignInFragment == null) {
                    adminSignInFragment = new AdminSignInFragment();
                    ft.add(R.id.admin_fl, adminSignInFragment);
                }
                ft.show(adminSignInFragment);
                break;
            case 1:
                if (adminAnnotationFragment == null) {
                    adminAnnotationFragment = new AdminAnnotationFragment();
                    ft.add(R.id.admin_fl, adminAnnotationFragment);
                }
                ft.show(adminAnnotationFragment);
                break;
            case 2:
                VoteResultPresenter.isVote = true;
                if (voteResultFragment == null) {
                    voteResultFragment = new VoteResultFragment();
                    ft.add(R.id.admin_fl, voteResultFragment);
                }
                ft.show(voteResultFragment);
                break;
            case 3:
                VoteResultPresenter.isVote = false;
                if (voteResultFragment == null) {
                    voteResultFragment = new VoteResultFragment();
                    ft.add(R.id.admin_fl, voteResultFragment);
                }
                ft.show(voteResultFragment);
                break;
            case 4:
                if (archiveFragment == null) {
                    archiveFragment = new ArchiveFragment();
                    ft.add(R.id.admin_fl, archiveFragment);
                }
                ft.show(archiveFragment);
                break;
            default:
                break;
        }
    }

    private void midFragment(int level2Index, FragmentTransaction ft) {
        switch (level2Index) {
            case 0:
                if (adminDevControlFragment == null) {
                    adminDevControlFragment = new AdminDevControlFragment();
                    ft.add(R.id.admin_fl, adminDevControlFragment);
                }
                ft.show(adminDevControlFragment);
                break;
            case 1:
                AdminVoteManagePresenter.isVote = true;
                if (adminVoteManageFragment == null) {
                    adminVoteManageFragment = new AdminVoteManageFragment();
                    ft.add(R.id.admin_fl, adminVoteManageFragment);
                }
                ft.show(adminVoteManageFragment);
                break;
            case 2:
                AdminVoteManagePresenter.isVote = false;
                if (adminVoteManageFragment == null) {
                    adminVoteManageFragment = new AdminVoteManageFragment();
                    ft.add(R.id.admin_fl, adminVoteManageFragment);
                }
                ft.show(adminVoteManageFragment);
                break;
            case 3:
                if (adminChatFragment == null) {
                    adminChatFragment = new AdminChatFragment();
                    ft.add(R.id.admin_fl, adminChatFragment);
                }
                ft.show(adminChatFragment);
                break;
            case 4:
                if (adminCameraControlFragment == null) {
                    adminCameraControlFragment = new AdminCameraControlFragment();
                    ft.add(R.id.admin_fl, adminCameraControlFragment);
                }
                ft.show(adminCameraControlFragment);
                break;
            case 5:
                if (screenFragment == null) {
                    screenFragment = new ScreenFragment();
                    ft.add(R.id.admin_fl, screenFragment);
                }
                ft.show(screenFragment);
                break;
            default:
                break;
        }
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
            case 1:
                if (adminAgendaFragment == null) {
                    adminAgendaFragment = new AdminAgendaFragment();
                    ft.add(R.id.admin_fl, adminAgendaFragment);
                }
                ft.show(adminAgendaFragment);
                break;
            case 2:
                if (memberFragment == null) {
                    memberFragment = new MemberFragment();
                    ft.add(R.id.admin_fl, memberFragment);
                }
                ft.show(memberFragment);
                break;
            case 3:
                if (adminFileFragment == null) {
                    adminFileFragment = new AdminFileFragment();
                    ft.add(R.id.admin_fl, adminFileFragment);
                }
                ft.show(adminFileFragment);
                break;
            case 4:
                if (adminCameraManageFragment == null) {
                    adminCameraManageFragment = new AdminCameraManageFragment();
                    ft.add(R.id.admin_fl, adminCameraManageFragment);
                }
                ft.show(adminCameraManageFragment);
                break;
            case 5:
                if (adminVoteFragment == null) {
                    adminVoteFragment = new AdminVoteFragment();
                    ft.add(R.id.admin_fl, adminVoteFragment);
                }
                ft.show(adminVoteFragment);
                break;
            case 6:
                if (adminElectionFragment == null) {
                    adminElectionFragment = new AdminElectionFragment();
                    ft.add(R.id.admin_fl, adminElectionFragment);
                }
                ft.show(adminElectionFragment);
                break;
            case 7:
                if (seatBindFragment == null) {
                    seatBindFragment = new SeatBindFragment();
                    ft.add(R.id.admin_fl, seatBindFragment);
                }
                ft.show(seatBindFragment);
                break;
            case 8:
                if (tableCardFragment == null) {
                    tableCardFragment = new TableCardFragment();
                    ft.add(R.id.admin_fl, tableCardFragment);
                }
                ft.show(tableCardFragment);
                break;
            case 9:
                if (functionFragment == null) {
                    functionFragment = new FunctionFragment();
                    ft.add(R.id.admin_fl, functionFragment);
                }
                ft.show(functionFragment);
                break;
            default:
                break;
        }
    }

    private void reserveFragment(int level2Index, FragmentTransaction ft) {
        switch (level2Index) {
            case 0:
                if (reserveMeetingFragment == null) {
                    reserveMeetingFragment = new ReserveMeetingFragment();
                    ft.add(R.id.admin_fl, reserveMeetingFragment);
                }
                ft.show(reserveMeetingFragment);
                break;
            case 1:
                if (sendEmailFragment == null) {
                    sendEmailFragment = new SendEmailFragment();
                    ft.add(R.id.admin_fl, sendEmailFragment);
                }
                ft.show(sendEmailFragment);
                break;
            case 2:
                if (taskManagerFragment == null) {
                    taskManagerFragment = new TaskManagerFragment();
                    ft.add(R.id.admin_fl, taskManagerFragment);
                }
                ft.show(taskManagerFragment);
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
            case 2:
                if (seatArrangementFragment == null) {
                    seatArrangementFragment = new SeatArrangementFragment();
                    ft.add(R.id.admin_fl, seatArrangementFragment);
                }
                ft.show(seatArrangementFragment);
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
                    adminMemberFragment = new FrequentlyMemberFragment();
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
        if (seatArrangementFragment != null) ft.hide(seatArrangementFragment);
        if (adminSecretaryManageFragment != null) ft.hide(adminSecretaryManageFragment);
        if (adminMemberFragment != null) ft.hide(adminMemberFragment);
        if (adminOtherFragment != null) ft.hide(adminOtherFragment);
        //会议预约
        if (reserveMeetingFragment != null) ft.hide(reserveMeetingFragment);
        if (sendEmailFragment != null) ft.hide(sendEmailFragment);
        if (taskManagerFragment != null) ft.hide(taskManagerFragment);
        // 会前设置
        if (meetingManageFragment != null) ft.hide(meetingManageFragment);
        if (adminAgendaFragment != null) ft.hide(adminAgendaFragment);
        if (memberFragment != null) ft.hide(memberFragment);
        if (adminFileFragment != null) ft.hide(adminFileFragment);
        if (adminCameraManageFragment != null) ft.hide(adminCameraManageFragment);
        if (adminVoteFragment != null) ft.hide(adminVoteFragment);
        if (adminElectionFragment != null) ft.hide(adminElectionFragment);
        if (seatBindFragment != null) ft.hide(seatBindFragment);
        if (tableCardFragment != null) ft.hide(tableCardFragment);
        if (functionFragment != null) ft.hide(functionFragment);
        //会中管理
        if (adminDevControlFragment != null) ft.hide(adminDevControlFragment);
        if (adminVoteManageFragment != null) ft.hide(adminVoteManageFragment);
        if (adminChatFragment != null) ft.hide(adminChatFragment);
        if (adminCameraControlFragment != null) ft.hide(adminCameraControlFragment);
        if (screenFragment != null) ft.hide(screenFragment);
        //会后查看
        if (adminSignInFragment != null) ft.hide(adminSignInFragment);
        if (adminAnnotationFragment != null) ft.hide(adminAnnotationFragment);
        if (voteResultFragment != null) ft.hide(voteResultFragment);
        if (archiveFragment != null) ft.hide(archiveFragment);
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
        admin_iv_close.setOnClickListener(v -> exit());
    }

    @Override
    public void updateMeetStatus(InterfaceMeet.pbui_Item_MeetMeetInfo item) {
        if (item == null) {
            currentMeetId = 0;
            admin_tv_meet_status.setText(getString(R.string.meet_status_no_select_meet));
            admin_tv_meet_name.setText(getString(R.string.no_select_meet));
            return;
        }
        currentMeetId = item.getId();
        admin_tv_meet_name.setText(item.getName().toStringUtf8());
        LogUtil.i(TAG, "updateMeetStatus meetId=" + currentMeetId + ",status=" + item.getStatus());
        switch (item.getStatus()) {
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
        Values.isFromAdminOpenWps = false;
        ScreenFragment.isAdminPage = false;
    }
}
