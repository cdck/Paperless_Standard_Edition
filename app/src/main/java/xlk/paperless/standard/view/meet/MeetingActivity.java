package xlk.paperless.standard.view.meet;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMeetfunction;

import java.util.ArrayList;
import java.util.List;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.bean.ChatMessage;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.BaseActivity;
import xlk.paperless.standard.view.MyApplication;
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

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static xlk.paperless.standard.data.Constant.fun_code_agenda_bulletin;
import static xlk.paperless.standard.data.Constant.fun_code_meet_file;
import static xlk.paperless.standard.data.Constant.fun_code_message;
import static xlk.paperless.standard.data.Constant.fun_code_postil_file;
import static xlk.paperless.standard.data.Constant.fun_code_shared_file;
import static xlk.paperless.standard.data.Constant.fun_code_signinresult;
import static xlk.paperless.standard.data.Constant.fun_code_video_stream;
import static xlk.paperless.standard.data.Constant.fun_code_webbrowser;
import static xlk.paperless.standard.data.Constant.fun_code_whiteboard;
import static xlk.paperless.standard.view.fragment.live.MeetLiveVideoFragment.isManage;
import static xlk.paperless.standard.view.fragment.score.MeetScoreFragment.isScoreManage;

/**
 * @author xlk
 * @date 2020年3月9日
 * @desc
 */
public class MeetingActivity extends BaseActivity implements IMeet, View.OnClickListener {

    private final String TAG = "MeetingActivity-->";
    private MeetingPresenter presenter;
    private ConstraintLayout meet_root_id;
    private LinearLayout meet_fun_all_ll;
    private LinearLayout meet_fun_ll;
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
    private LinearLayout meet_others;
    public static int frameLayoutWidth, frameLayoutHeight;//PopupWindow大小时使用
    private MeetAgendaFragment meetAgendaFragment;
    private MeetAnnotationFragment meetAnnotationFragment;
    private MeetChatFragment meetChatFragment;
    private MeetDataFragment meetDataFragment;
    private MeetLiveVideoFragment meetLiveVideoFragment;
    private MeetScoreFragment meetScoreFragment;
    private MeetSigninFragment meetSigninFragment;
    private MeetWebFragment meetWebFragment;
    private DeviceControlFragment deviceControlFragment;
    private ElectionManageFragment electionManageFragment;
    private ScreenFragment screenFragment;
    private BulletinFragment bulletinFragment;
    private int saveFunCode = -1;
    public static boolean chatIsShowing = false;
    public static List<ChatMessage> chatMessages = new ArrayList<>();
    public static Badge mBadge;
    private FragmentTransaction ft;
    List<LinearLayout> funViews = new ArrayList<>();
    private PopupWindow funPop;
    private int funWidth;
    private int funHeight;
    private int firstFunCode;//保存第一个功能的功能码
    private VoteManageFragment voteManageFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        initView();
        ft = getSupportFragmentManager().beginTransaction();
        presenter = new MeetingPresenter(this, this);
        presenter.register();
        meet_frame_layout.post(() -> {
            frameLayoutWidth = meet_frame_layout.getWidth();
            frameLayoutHeight = meet_frame_layout.getHeight();
        });
        meet_fun_all_ll.post(() -> {
            funWidth = meet_fun_all_ll.getWidth();
            funHeight = meet_fun_all_ll.getHeight();
        });
        presenter.initial();
        presenter.initVideoRes();
        initial();
        ((MyApplication) getApplication()).openFabService(true);
    }

    private void initial() {
        presenter.queryInterFaceConfiguration();
        presenter.queryIsOnline();
        presenter.queryDeviceMeetInfo();
        presenter.queryMeetFunction();
        presenter.queryPermission();
    }

    private void initView() {
        meet_root_id = (ConstraintLayout) findViewById(R.id.meet_root_id);
        meet_fun_all_ll = (LinearLayout) findViewById(R.id.meet_fun_all_ll);
        meet_fun_ll = (LinearLayout) findViewById(R.id.meet_fun_ll);
        meet_logo = (ImageView) findViewById(R.id.meet_logo);
        meet_member = (TextView) findViewById(R.id.meet_member);
        meet_chat = (ImageView) findViewById(R.id.meet_chat);
        meet_online = (TextView) findViewById(R.id.meet_online);
        meet_time = (TextView) findViewById(R.id.meet_time);
        meet_date = (TextView) findViewById(R.id.meet_date);
        meet_week = (TextView) findViewById(R.id.meet_week);
        meet_min = (ImageView) findViewById(R.id.meet_min);
        meet_close = (ImageView) findViewById(R.id.meet_close);
        meet_meeting_name = (TextView) findViewById(R.id.meet_meeting_name);
        meet_frame_layout = (FrameLayout) findViewById(R.id.meet_frame_layout);
        meet_others = (LinearLayout) findViewById(R.id.meet_others);
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
        meet_others.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unregister();
        ((MyApplication) getApplication()).openFabService(false);
    }

    @Override
    public void jump2main() {
        if (isFinishing()) return;
        presenter.releaseVideoRes();
        finish();
        startActivity(new Intent(this, MainActivity.class).addFlags(FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.meet_chat:
                setDefaultFun(4);
                break;
            case R.id.meet_min:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.meet_close:
                exit();
                break;
            case R.id.meet_others:
                if (MyApplication.hasAllPermissions) {
                    showOtherFunPop();
                } else {
                    ToastUtil.show(getString(R.string.err_NoPermission));
                }
                break;
        }
    }

    private void showOtherFunPop() {
        View inflate = LayoutInflater.from(this).inflate(R.layout.pop_fun_other, null);
        LogUtil.d(TAG, "showOtherFunPop -->" + funWidth + "," + funHeight);
        funPop = new PopupWindow(inflate, funWidth, funHeight);
        funPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        funPop.setTouchable(true);
        // true:设置触摸外面时消失
        funPop.setOutsideTouchable(true);
        funPop.setFocusable(true);
        funPop.setAnimationStyle(R.style.pop_lr_animation);
        funPop.showAsDropDown(meet_fun_all_ll, -funWidth, 0);
        inflate.findViewById(R.id.pop_fun_terminal_control).setOnClickListener(v -> {
            setDefaultFun(Constant.fun_code_terminal);
            funPop.dismiss();
        });
        inflate.findViewById(R.id.pop_fun_vote_manage).setOnClickListener(v -> {
            setDefaultFun(Constant.fun_code_vote);
            funPop.dismiss();
        });
        inflate.findViewById(R.id.pop_fun_election_manage).setOnClickListener(v -> {
            setDefaultFun(Constant.fun_code_election);
            funPop.dismiss();
        });
        inflate.findViewById(R.id.pop_fun_video_control).setOnClickListener(v -> {
            setDefaultFun(Constant.fun_code_video);
            funPop.dismiss();
        });
        inflate.findViewById(R.id.pop_fun_screen_manage).setOnClickListener(v -> {
            setDefaultFun(Constant.fun_code_screen);
            funPop.dismiss();
        });
        inflate.findViewById(R.id.pop_fun_bulletin_manage).setOnClickListener(v -> {
            setDefaultFun(Constant.fun_code_bulletin);
            funPop.dismiss();
        });
        inflate.findViewById(R.id.pop_fun_score_manage).setOnClickListener(v -> {
            setDefaultFun(Constant.fun_code_score);
            funPop.dismiss();
        });
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
        float width = (bx - lx) / 100 * MyApplication.screen_width;
        float height = (by - ly) / 100 * MyApplication.screen_height;
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
    public void updateLogo(Drawable drawable) {
        LogUtil.e(TAG, "updateLogo 设置logo图片 -->" + (drawable != null));
        meet_logo.setImageDrawable(drawable);
    }

    @Override
    public void updateBg(Drawable drawable) {
        meet_root_id.setBackground(drawable);
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
        meet_member.setText(roleStr + MyApplication.localMemberName);
    }

    @Override
    public void hasOtherFunction(boolean isHas) {
        LogUtil.d(TAG, "hasOtherFunction -->" + "是否拥有权限操作其它功能：" + isHas);
        if (!isHas) {
            if (funPop != null && funPop.isShowing()) {
                funPop.dismiss();
            }
            if (saveFunCode > Constant.fun_code) {
                //之前有权限时在其它界面，权限消失后切换到默认第一个页面去
                setDefaultFun(firstFunCode);
            }
        }
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
        LogUtil.d(TAG, "updateFunction --> saveFunCode= " + saveFunCode);
        funViews.clear();
        meet_fun_ll.removeAllViews();
        //首次进入时，设置第一个为默认选中的功能
        if (saveFunCode == -1 && !functions.isEmpty()) {
            int funcode = functions.get(0).getFuncode();
            if (funcode == fun_code_whiteboard) {
                if (functions.size() > 1) {//如果第一个是电子白板则跳过
                    saveFunCode = functions.get(1).getFuncode();
                }
            } else {
                saveFunCode = funcode;
            }
        }
        boolean has = false;
        for (int i = 0; i < functions.size(); i++) {
            int funcode = functions.get(i).getFuncode();
            if (saveFunCode == funcode) {
                has = true;
                break;
            }
        }
        LogUtil.d(TAG, "updateFunction :  当前功能码 --> " + saveFunCode + ", has = " + has);
        for (int index = 0; index < functions.size(); index++) {
            InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo info = functions.get(index);
            int funcode = info.getFuncode();
            if (funcode == fun_code_shared_file || funcode == Constant.fun_code_voteresult)
                continue;
            if (index == 0) {
                firstFunCode = funcode;
            }
            if (index == 1 && firstFunCode == 6) {//不保存电子白板的功能码
                firstFunCode = funcode;
            }
            LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvParams.setMarginStart(5);
            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
            LinearLayout ll = new LinearLayout(this);
            ImageView iv = new ImageView(this);
            TextView tv = new TextView(this);
            tv.setTextColor(Color.WHITE);
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            iv.setLayoutParams(ivParams);
            tv.setLayoutParams(tvParams);
            llParams.weight = 1;
            ll.setLayoutParams(llParams);
            ll.setGravity(Gravity.CENTER);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.addView(iv);
            ll.addView(tv);
            ll.setClickable(true);
            ll.setBackground(getDrawable(R.drawable.fun_choose_s));
            ll.setSelected(saveFunCode == funcode);
            ll.setId(funcode);
            setFunUI(funcode, iv, tv);
            ll.setOnClickListener(v -> {
                int id = v.getId();
                LogUtil.d(TAG, "onClick -->功能码：" + id);
                setDefaultFun(id);
            });
            funViews.add(ll);
            meet_fun_ll.addView(ll);
        }
        if (has) {
            setDefaultFun(saveFunCode);
        } else {
            hideFragment(ft);
        }
    }

    private void setFunUI(int funcode, ImageView iv, TextView tv) {
        String funName = "";
        switch (funcode) {
            case fun_code_agenda_bulletin:
                funName = getString(R.string.meeting_agenda);
                iv.setImageDrawable(getDrawable(R.drawable.icon_fun_agenda));
                break;
            case fun_code_meet_file:
                funName = getString(R.string.meeting_data);
                iv.setImageDrawable(getDrawable(R.drawable.icon_fun_data));
                break;
            case fun_code_shared_file:
                funName = getString(R.string.meeting_shared_file);
                break;
            case fun_code_postil_file:
                funName = getString(R.string.meeting_annotation_view);
                iv.setImageDrawable(getDrawable(R.drawable.icon_fun_annotation));
                break;
            case fun_code_message:
                funName = getString(R.string.meeting_chat);
                iv.setImageDrawable(getDrawable(R.drawable.icon_fun_chat));
                break;
            case fun_code_video_stream:
                funName = getString(R.string.meeting_live_video);
                iv.setImageDrawable(getDrawable(R.drawable.icon_fun_video));
                break;
            case fun_code_whiteboard:
                funName = getString(R.string.meeting_art_board);
                iv.setImageDrawable(getDrawable(R.drawable.icon_fun_art));
                break;
            case fun_code_webbrowser:
                funName = getString(R.string.meeting_web_browsing);
                iv.setImageDrawable(getDrawable(R.drawable.icon_fun_web));
                break;
            case 8:
                funName = getString(R.string.meeting_questionnaire);
                break;
            case fun_code_signinresult:
                funName = getString(R.string.meeting_sign_in_information);
                iv.setImageDrawable(getDrawable(R.drawable.icon_fun_signin));
                break;
            case 31:
                funName = getString(R.string.meeting_score);
                iv.setImageDrawable(getDrawable(R.drawable.icon_fun_score));
                break;
            default:
                funName = String.valueOf(funcode);
                break;
        }
        LogUtil.d(TAG, "setFunUI -->" + funName);
        tv.setText(funName);
    }

    private void setDefaultFun(int funCode) {
        LogUtil.d(TAG, "setDefaultFun -->" + funCode + ", 保存的功能码= " + saveFunCode);
        boolean has = false;
        for (LinearLayout view : funViews) {
            int code = view.getId();
            if (funCode != 6) {
                view.setSelected(funCode == code);
            }
            if (funCode == code) {
                has = true;
            }
        }
        if (has) {//当前拥有才进行操作
            if (funCode == 6) {
                jump2artBoard();
                return;
            }
            showFragment(funCode);
        } else {
            showFragment(funCode);
//            ToastUtil.show(R.string.no_function);
        }
    }

    /**
     * 根据功能码展示相应Fragment
     *
     * @param funcode =0会议议程，=1会议资料，=2共享文件，=3批注查看，=4互动交流
     *                =5视频直播，=6电子白板，=7网页浏览，=8问卷调查，=9签到信息，=31评分查看
     */
    private void showFragment(int funcode) {
        if (funcode != fun_code_whiteboard) {
            saveFunCode = funcode;
        }
        ft = getSupportFragmentManager().beginTransaction();
        hideFragment(ft);
        switch (funcode) {
            case fun_code_agenda_bulletin://会议议程
                if (meetAgendaFragment == null) {
                    meetAgendaFragment = new MeetAgendaFragment();
                    ft.add(R.id.meet_frame_layout, meetAgendaFragment);
                }
                ft.show(meetAgendaFragment);
                break;
            case fun_code_meet_file://会议资料
                if (meetDataFragment == null) {
                    meetDataFragment = new MeetDataFragment();
                    ft.add(R.id.meet_frame_layout, meetDataFragment);
                }
                ft.show(meetDataFragment);
                break;
            case fun_code_postil_file://批注查看
                if (meetAnnotationFragment == null) {
                    meetAnnotationFragment = new MeetAnnotationFragment();
                    ft.add(R.id.meet_frame_layout, meetAnnotationFragment);
                }
                ft.show(meetAnnotationFragment);
                break;
            case fun_code_message://互动交流
                if (meetChatFragment == null) {
                    meetChatFragment = new MeetChatFragment();
                    ft.add(R.id.meet_frame_layout, meetChatFragment);
                }
                ft.show(meetChatFragment);
                break;
            case fun_code_video_stream://视频直播
                isManage = false;
                if (meetLiveVideoFragment == null) {
                    meetLiveVideoFragment = new MeetLiveVideoFragment();
                    ft.add(R.id.meet_frame_layout, meetLiveVideoFragment);
                }
                ft.show(meetLiveVideoFragment);
                break;
            case fun_code_webbrowser://网页浏览
                if (meetWebFragment == null) {
                    meetWebFragment = new MeetWebFragment();
                    ft.add(R.id.meet_frame_layout, meetWebFragment);
                }
                ft.show(meetWebFragment);
                break;
            case fun_code_signinresult://签到信息
                if (meetSigninFragment == null) {
                    meetSigninFragment = new MeetSigninFragment();
                    ft.add(R.id.meet_frame_layout, meetSigninFragment);
                }
                ft.show(meetSigninFragment);
                break;
            case 31://评分查看
                isScoreManage = false;
                if (meetScoreFragment == null) {
                    meetScoreFragment = new MeetScoreFragment();
                    ft.add(R.id.meet_frame_layout, meetScoreFragment);
                }
                ft.show(meetScoreFragment);
                break;
            case Constant.fun_code_terminal://终端控制
                if (deviceControlFragment == null) {
                    deviceControlFragment = new DeviceControlFragment();
                    ft.add(R.id.meet_frame_layout, deviceControlFragment);
                }
                ft.show(deviceControlFragment);
                break;
            case Constant.fun_code_vote://投票管理
                if (voteManageFragment == null) {
                    voteManageFragment = new VoteManageFragment();
                    ft.add(R.id.meet_frame_layout, voteManageFragment);
                }
                ft.show(voteManageFragment);
                break;
            case Constant.fun_code_election://选举管理
                if (electionManageFragment == null) {
                    electionManageFragment = new ElectionManageFragment();
                    ft.add(R.id.meet_frame_layout, electionManageFragment);
                }
                ft.show(electionManageFragment);
                break;
            case Constant.fun_code_video://视频控制
                isManage = true;
                if (meetLiveVideoFragment == null) {
                    meetLiveVideoFragment = new MeetLiveVideoFragment();
                    ft.add(R.id.meet_frame_layout, meetLiveVideoFragment);
                }
                ft.show(meetLiveVideoFragment);
                break;
            case Constant.fun_code_screen://屏幕管理
                if (screenFragment == null) {
                    screenFragment = new ScreenFragment();
                    ft.add(R.id.meet_frame_layout, screenFragment);
                }
                ft.show(screenFragment);
                break;
            case Constant.fun_code_bulletin://公告管理
                if (bulletinFragment == null) {
                    bulletinFragment = new BulletinFragment();
                    ft.add(R.id.meet_frame_layout, bulletinFragment);
                }
                ft.show(bulletinFragment);
                break;
            case Constant.fun_code_score://评分管理
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
        exit();
    }
}
