package xlk.paperless.standard.view.meet;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.gcssloop.widget.PagerGridLayoutManager;
import com.gcssloop.widget.PagerGridSnapHelper;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMeetfunction;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.ChatMessage;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.base.BaseActivity;
import xlk.paperless.standard.view.App;
import xlk.paperless.standard.view.admin.fragment.after.signin.AdminSignInFragment;
import xlk.paperless.standard.view.draw.DrawActivity;
import xlk.paperless.standard.view.fragment.agenda.MeetAgendaFragment;
import xlk.paperless.standard.view.fragment.annotation.MeetAnnotationFragment;
import xlk.paperless.standard.view.fragment.chat.MeetChatFragment;
import xlk.paperless.standard.view.fragment.data.MeetDataFragment;
import xlk.paperless.standard.view.fragment.live.MeetLiveVideoFragment;
import xlk.paperless.standard.view.fragment.other.bulletin.BulletinFragment;
import xlk.paperless.standard.view.fragment.other.devicecontrol.DeviceControlFragment;
import xlk.paperless.standard.view.fragment.other.election.ElectionManageFragment;
import xlk.paperless.standard.view.fragment.other.screen.ScreenFragment;
import xlk.paperless.standard.view.fragment.other.vote.VoteManageFragment;
import xlk.paperless.standard.view.fragment.score.MeetScoreFragment;
import xlk.paperless.standard.view.fragment.signin.MeetSigninFragment;
import xlk.paperless.standard.view.fragment.web.MeetWebFragment;
import xlk.paperless.standard.view.main.MainActivity;

import static xlk.paperless.standard.data.Constant.FUN_CODE_AGENDA_BULLETIN;
import static xlk.paperless.standard.data.Constant.FUN_CODE_MEET_FILE;
import static xlk.paperless.standard.data.Constant.FUN_CODE_MESSAGE;
import static xlk.paperless.standard.data.Constant.FUN_CODE_POSTIL_FILE;
import static xlk.paperless.standard.data.Constant.FUN_CODE_SIGNIN_RESULT;
import static xlk.paperless.standard.data.Constant.FUN_CODE_SIGN_IN_LIST;
import static xlk.paperless.standard.data.Constant.FUN_CODE_VIDEO_STREAM;
import static xlk.paperless.standard.data.Constant.FUN_CODE_WEB_BROWSER;
import static xlk.paperless.standard.data.Constant.FUN_CODE_WHITEBOARD;
import static xlk.paperless.standard.view.fragment.live.MeetLiveVideoFragment.isManage;
import static xlk.paperless.standard.view.fragment.score.MeetScoreFragment.isScoreManage;

/**
 * @author xlk
 * @date 2020年3月9日
 * @desc
 */
public class MeetingActivity extends BaseActivity implements IMeet, View.OnClickListener {

    private MeetingPresenter presenter;
    private ConstraintLayout meet_root_id;
    private ImageView meet_logo;
    private TextView meet_member;
    private ImageView meet_chat;
    private TextView meet_online;
    private TextView meet_time;
    private TextView meet_date;
    private TextView meet_week;
    private ImageView meet_min;
    private ImageView meet_close;
    private TextView meet_meeting_name;
    private FrameLayout meet_frame_layout;
    private MeetAgendaFragment meetAgendaFragment;
    private MeetAnnotationFragment meetAnnotationFragment;
    private MeetChatFragment meetChatFragment;
    private MeetDataFragment meetDataFragment;
    private MeetLiveVideoFragment meetLiveVideoFragment;
    private MeetScoreFragment meetScoreFragment;
    private MeetSigninFragment meetSigninFragment;
    private MeetWebFragment meetWebFragment;
    private AdminSignInFragment adminSignInFragment;
    private DeviceControlFragment deviceControlFragment;
    private ElectionManageFragment electionManageFragment;
    private ScreenFragment screenFragment;
    private BulletinFragment bulletinFragment;
    public static boolean chatIsShowing = false;
    public static List<ChatMessage> chatMessages = new ArrayList<>();
    public static Badge mBadge;
    private VoteManageFragment voteManageFragment;
    private LinearLayout ll_host_functions;
    private FeatureAdapter featureAdapter;
    private RecyclerView meet_rv_functions;
    private RelativeLayout rl_feature_page;
    private LinearLayout meet_fun_all_ll;
    private PagerGridLayoutManager pagerGridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_test);
        initView();
        presenter = new MeetingPresenter(this, this);
        presenter.initial();
        presenter.initVideoRes();
        initial();
//        if (!XXPermissions.hasPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
//            applyAlertWindowPermission();
//        } else {
            ((App) getApplication()).openFabService(true);
//        }
    }


    /**
     * 申请悬浮窗权限
     */
    private void applyAlertWindowPermission() {
        LogUtils.i("applyAlertWindowPermission");
        XXPermissions.with(this)
                .permission(Manifest.permission.SYSTEM_ALERT_WINDOW)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean all) {
                        LogUtils.e(TAG, "useXX hasPermission  -->" + granted);
                        if(all){
                            ((App) getApplication()).openFabService(true);
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        LogUtils.e(TAG, "useXX noPermission  -->" + denied);
                    }
                });
    }

    private void initial() {
        LogUtil.i(TAG, "initial ");
        presenter.queryInterFaceConfiguration();
        presenter.queryIsOnline();
        presenter.queryDeviceMeetInfo();
        presenter.queryMeetFunction();
        presenter.queryPermission();
    }

    private void initView() {
        meet_root_id = (ConstraintLayout) findViewById(R.id.meet_root_id);
//        meet_root_id.setBackgroundResource(App.isStandard ? R.drawable.bg_icon_red : R.drawable.bg_icon_blue);
        if(!App.isStandard) {
            meet_root_id.setBackgroundResource(R.drawable.bg_icon_blue);
        }

        meet_logo = (ImageView) findViewById(R.id.meet_logo);
        meet_member = (TextView) findViewById(R.id.meet_member);
        meet_chat = (ImageView) findViewById(R.id.meet_chat);
        meet_online = (TextView) findViewById(R.id.meet_online);
        meet_time = (TextView) findViewById(R.id.meet_time);
        meet_date = (TextView) findViewById(R.id.meet_date);
        meet_week = (TextView) findViewById(R.id.meet_week);
        meet_min = (ImageView) findViewById(R.id.meet_min);
        meet_close = (ImageView) findViewById(R.id.meet_close);
        rl_feature_page = (RelativeLayout) findViewById(R.id.rl_feature_page);
        meet_meeting_name = (TextView) findViewById(R.id.meet_meeting_name);
        meet_rv_functions = (RecyclerView) findViewById(R.id.meet_rv_functions);
        meet_fun_all_ll = (LinearLayout) findViewById(R.id.meet_fun_all_ll);
        meet_frame_layout = (FrameLayout) findViewById(R.id.meet_frame_layout);
        ll_host_functions = (LinearLayout) findViewById(R.id.ll_host_functions);
        findViewById(R.id.meet_home).setOnClickListener(v -> {
            if (meet_frame_layout.getVisibility() == View.VISIBLE) {
                meet_frame_layout.setVisibility(View.GONE);
                meet_fun_all_ll.setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.iv_dev_control).setOnClickListener(v -> {
            showFragment(Constant.FUN_CODE_TERMINAL);
        });
        findViewById(R.id.iv_vote_manage).setOnClickListener(v -> {
            showFragment(Constant.FUN_CODE_VOTE);
        });
        findViewById(R.id.iv_election_manage).setOnClickListener(v -> {
            showFragment(Constant.FUN_CODE_ELECTION);
        });
        findViewById(R.id.iv_camera_control).setOnClickListener(v -> {
            showFragment(Constant.FUN_CODE_VIDEO);
        });
        findViewById(R.id.iv_screen_manage).setOnClickListener(v -> {
            showFragment(Constant.FUN_CODE_SCREEN);
        });
        findViewById(R.id.iv_bullet_manage).setOnClickListener(v -> {
            showFragment(Constant.FUN_CODE_BULLETIN);
        });

        if (mBadge == null) {
            /** ************ ******  设置未读消息展示  ****** ************ **/
            mBadge = new QBadgeView(this).bindTarget(meet_chat);
            mBadge.setBadgeGravity(Gravity.END | Gravity.TOP);
            mBadge.setBadgeTextSize(14, true);
            mBadge.setShowShadow(true);
            mBadge.setOnDragStateChangedListener((dragState, badge, targetView) -> {
                //只需要空实现，就可以拖拽消除未读消息
            });
        }

        meet_chat.setOnClickListener(this);
        meet_min.setOnClickListener(this);
        meet_close.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        mBadge = null;
        ((App) getApplication()).openFabService(false);
    }

    @Override
    public void jump2main() {
        LogUtil.d(TAG, "jump2main -->isFinishing = " + isFinishing());
        if (isFinishing()) {
            return;
        }
        presenter.releaseVideoRes();
        finish();
        //.addFlags(FLAG_ACTIVITY_NEW_TASK)导致的问题：如果期间返回到MainActivity后，再进入MeetingActivity，
        // 点击最小化或者Home键回到桌面，再次启动的会进入到MainActivity
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.meet_chat:
                showFragment(FUN_CODE_MESSAGE);
                break;
            //最小化，回到桌面
            case R.id.meet_min:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                break;
            case R.id.meet_close:
                if (meet_frame_layout.getVisibility() == View.VISIBLE) {
                    meet_frame_layout.setVisibility(View.GONE);
                    meet_fun_all_ll.setVisibility(View.VISIBLE);
                } else {
                    exit();
                }
                break;
            default:
                break;
        }
    }

    private void exit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MeetingActivity.this);
        builder.setTitle(R.string.is_exit_to_main);
        builder.setPositiveButton(R.string.determine, (dialog, which) -> {
            dialog.dismiss();
            jump2main();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public void setLogoVisibility(boolean visibility) {
        meet_logo.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setCompanyName(String company) {

    }

    @Override
    public void setCompanyVisibility(boolean isShow) {

    }

    @Override
    public void updateLogoSize(int resid, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info) {
        float bx = info.getBx();
        float by = info.getBy();
        float lx = info.getLx();
        float ly = info.getLy();
        ConstraintSet set = new ConstraintSet();
        set.clone(meet_root_id);
        //设置控件的大小
        float width = (bx - lx) / 100 * Values.screen_width;
        float height = (by - ly) / 100 * Values.screen_height;
        set.constrainWidth(resid, (int) width);
        set.constrainHeight(resid, (int) height);
        LogUtil.d(TAG, "updateLogoSize: 控件大小 当前控件宽= " + width + ", 当前控件高= " + height);
        float biasX, biasY;
        float halfW = (bx - lx) / 2 + lx;
        float halfH = (by - ly) / 2 + ly;

        if (lx == 0) biasX = 0;
        else if (lx > 50) biasX = bx / 100;
        else biasX = halfW / 100;

        if (ly == 0) biasY = 0;
        else if (ly > 50) biasY = by / 100;
        else biasY = halfH / 100;
        LogUtil.d(TAG, "updateLogoSize: biasX= " + biasX + ",biasY= " + biasY);
        set.setHorizontalBias(resid, biasX);
        set.setVerticalBias(resid, biasY);
        set.applyTo(meet_root_id);
    }

    @Override
    public void changeSignInPage(boolean toListPage) {
        showFragment(toListPage ? FUN_CODE_SIGN_IN_LIST : FUN_CODE_SIGNIN_RESULT);
    }

    @Override
    public void updateLogo(Drawable drawable) {
        LogUtil.e(TAG, "updateLogo 设置logo图片 -->" + (drawable != null));
        meet_logo.setImageDrawable(drawable);
    }

    @Override
    public void updateBg(Drawable drawable) {
        if(App.isStandard) {
            meet_root_id.setBackground(drawable);
        }
    }

    @Override
    public void updateOnline(String string) {
        meet_online.setText(string);
    }

    @Override
    public void updateMeetName(InterfaceDevice.pbui_Type_DeviceFaceShowDetail deviceMeetInfo) {
        meet_meeting_name.setText(deviceMeetInfo.getMeetingname().toStringUtf8());
        meet_member.setText(deviceMeetInfo.getMembername().toStringUtf8());
    }

    @Override
    public void updateMemberRole(String roleStr) {
        meet_member.setText(roleStr + Values.localMemberName);
    }

    @Override
    public void hasOtherFunction(boolean isHas) {
        LogUtil.d(TAG, "hasOtherFunction -->" + "是否拥有权限操作其它功能：" + isHas);
        ll_host_functions.setVisibility(isHas ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void updateTime(long millisecond) {
        String[] date = DateUtil.getGTMDate(millisecond);
        String day = date[0];
        String week = date[1];
        String time = date[2];
        meet_date.setText(day);
        meet_week.setText(week);
        meet_time.setText(time);
    }

    @Override
    public void updateFunction(List<InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo> functions) {
        if (featureAdapter == null) {
            featureAdapter = new FeatureAdapter(functions);
            pagerGridLayoutManager = new PagerGridLayoutManager(2, 4, PagerGridLayoutManager.HORIZONTAL);
            meet_rv_functions.setLayoutManager(pagerGridLayoutManager);
            // 2.设置滚动辅助工具
            PagerGridSnapHelper pageSnapHelper = new PagerGridSnapHelper();
            pageSnapHelper.attachToRecyclerView(meet_rv_functions);
//            meet_rv_functions.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
            meet_rv_functions.setAdapter(featureAdapter);
            if (!App.isStandard) {
                boolean hasAgenda = false;
                for (int i = 0; i < functions.size(); i++) {
                    InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo item = functions.get(i);
                    if (item.getFuncode() == FUN_CODE_AGENDA_BULLETIN) {
                        hasAgenda = true;
                        break;
                    }
                }
                if (hasAgenda) {
                    showFragment(FUN_CODE_AGENDA_BULLETIN);
                }
            }
            featureAdapter.setOnItemClickListener((adapter, view, position) -> {
                int funcode = functions.get(position).getFuncode();
                if (funcode == FUN_CODE_WHITEBOARD) {
                    jump2artBoard();
                } else {
                    showFragment(funcode);
                }
            });
        } else {
            featureAdapter.notifyDataSetChanged();
        }
    }


    /**
     * 根据功能码展示相应Fragment
     *
     * @param funcode =0会议议程，=1会议资料，=2共享文件，=3批注查看，=4互动交流
     *                =5视频直播，=6电子白板，=7网页浏览，=8问卷调查，=9签到信息，=31评分查看
     */
    private void showFragment(int funcode) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        hideFragment(ft);
        switch (funcode) {
            case FUN_CODE_AGENDA_BULLETIN://会议议程
                if (meetAgendaFragment == null) {
                    meetAgendaFragment = new MeetAgendaFragment();
                    ft.add(R.id.meet_frame_layout, meetAgendaFragment);
                }
                ft.show(meetAgendaFragment);
                break;
            case FUN_CODE_MEET_FILE://会议资料
                if (meetDataFragment == null) {
                    meetDataFragment = new MeetDataFragment();
                    ft.add(R.id.meet_frame_layout, meetDataFragment);
                }
                ft.show(meetDataFragment);
                break;
            case FUN_CODE_POSTIL_FILE://批注查看
                if (meetAnnotationFragment == null) {
                    meetAnnotationFragment = new MeetAnnotationFragment();
                    ft.add(R.id.meet_frame_layout, meetAnnotationFragment);
                }
                ft.show(meetAnnotationFragment);
                break;
            case FUN_CODE_MESSAGE://互动交流
                if (meetChatFragment == null) {
                    meetChatFragment = new MeetChatFragment();
                    ft.add(R.id.meet_frame_layout, meetChatFragment);
                }
                ft.show(meetChatFragment);
                break;
            case FUN_CODE_VIDEO_STREAM://视频直播
                isManage = false;
                if (meetLiveVideoFragment == null) {
                    meetLiveVideoFragment = new MeetLiveVideoFragment();
                    ft.add(R.id.meet_frame_layout, meetLiveVideoFragment);
                }
                ft.show(meetLiveVideoFragment);
                break;
            case FUN_CODE_WEB_BROWSER://网页浏览
                if (meetWebFragment == null) {
                    meetWebFragment = new MeetWebFragment();
                    ft.add(R.id.meet_frame_layout, meetWebFragment);
                }
                ft.show(meetWebFragment);
                break;
            //签到信息
            case FUN_CODE_SIGNIN_RESULT: {
                if (meetSigninFragment == null) {
                    meetSigninFragment = new MeetSigninFragment();
                    ft.add(R.id.meet_frame_layout, meetSigninFragment);
                }
                ft.show(meetSigninFragment);
                break;
            }
            //签到列表
            case FUN_CODE_SIGN_IN_LIST: {
                if (adminSignInFragment == null) {
                    adminSignInFragment = new AdminSignInFragment();
                    ft.add(R.id.meet_frame_layout, adminSignInFragment);
                }
                ft.show(adminSignInFragment);
                break;
            }
            case 31://评分查看
                isScoreManage = false;
                if (meetScoreFragment == null) {
                    meetScoreFragment = new MeetScoreFragment();
                    ft.add(R.id.meet_frame_layout, meetScoreFragment);
                }
                ft.show(meetScoreFragment);
                break;
            case Constant.FUN_CODE_TERMINAL://终端控制
                if (deviceControlFragment == null) {
                    deviceControlFragment = new DeviceControlFragment();
                    ft.add(R.id.meet_frame_layout, deviceControlFragment);
                }
                ft.show(deviceControlFragment);
                break;
            case Constant.FUN_CODE_VOTE://投票管理
                if (voteManageFragment == null) {
                    voteManageFragment = new VoteManageFragment();
                    ft.add(R.id.meet_frame_layout, voteManageFragment);
                }
                ft.show(voteManageFragment);
                break;
            case Constant.FUN_CODE_ELECTION://选举管理
                if (electionManageFragment == null) {
                    electionManageFragment = new ElectionManageFragment();
                    ft.add(R.id.meet_frame_layout, electionManageFragment);
                }
                ft.show(electionManageFragment);
                break;
            case Constant.FUN_CODE_VIDEO://视频控制
                isManage = true;
                if (meetLiveVideoFragment == null) {
                    meetLiveVideoFragment = new MeetLiveVideoFragment();
                    ft.add(R.id.meet_frame_layout, meetLiveVideoFragment);
                }
                ft.show(meetLiveVideoFragment);
                break;
            case Constant.FUN_CODE_SCREEN://屏幕管理
                if (screenFragment == null) {
                    screenFragment = new ScreenFragment();
                    ft.add(R.id.meet_frame_layout, screenFragment);
                }
                ft.show(screenFragment);
                break;
            case Constant.FUN_CODE_BULLETIN://公告管理
                if (bulletinFragment == null) {
                    bulletinFragment = new BulletinFragment();
                    ft.add(R.id.meet_frame_layout, bulletinFragment);
                }
                ft.show(bulletinFragment);
                break;
            case Constant.FUN_CODE_SCORE://评分管理
                isScoreManage = true;
                if (meetScoreFragment == null) {
                    meetScoreFragment = new MeetScoreFragment();
                    ft.add(R.id.meet_frame_layout, meetScoreFragment);
                }
                ft.show(meetScoreFragment);
                break;
        }
        ft.commitAllowingStateLoss();//允许状态丢失，其他完全一样
//        ft.commit();//出现异常：Can not perform this action after onSaveInstanceState
        meet_frame_layout.setVisibility(View.VISIBLE);
        meet_fun_all_ll.setVisibility(View.GONE);
    }

    private void hideFragment(FragmentTransaction ft) {
        if (meetAgendaFragment != null) ft.hide(meetAgendaFragment);
        if (meetDataFragment != null) ft.hide(meetDataFragment);
        if (meetAnnotationFragment != null) ft.hide(meetAnnotationFragment);
        if (meetChatFragment != null) ft.hide(meetChatFragment);
        if (meetLiveVideoFragment != null) ft.hide(meetLiveVideoFragment);
        if (meetWebFragment != null) ft.hide(meetWebFragment);
        if (meetSigninFragment != null) ft.hide(meetSigninFragment);
        if (meetScoreFragment != null) ft.hide(meetScoreFragment);
        if (deviceControlFragment != null) ft.hide(deviceControlFragment);
        if (voteManageFragment != null) ft.hide(voteManageFragment);
        if (electionManageFragment != null) ft.hide(electionManageFragment);
        if (screenFragment != null) ft.hide(screenFragment);
        if (bulletinFragment != null) ft.hide(bulletinFragment);
        if (adminSignInFragment != null) ft.hide(adminSignInFragment);
    }

    private void jump2artBoard() {
        startActivity(new Intent(this, DrawActivity.class));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initial();
    }

    @Override
    public void onBackPressed() {
        if (meet_frame_layout.getVisibility() == View.VISIBLE) {
            meet_frame_layout.setVisibility(View.GONE);
            meet_fun_all_ll.setVisibility(View.VISIBLE);
        } else {
            exit();
        }
    }
}
