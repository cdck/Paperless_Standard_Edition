package xlk.paperless.standard.view.video;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.mogujie.tt.protobuf.InterfaceMacro;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.WmProjectorAdapter;
import xlk.paperless.standard.adapter.WmScreenMemberAdapter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.service.FabService;
import xlk.paperless.standard.ui.video.MyGLSurfaceView;
import xlk.paperless.standard.ui.video.WlOnGlSurfaceViewOncreateListener;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.PopUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BaseActivity;
import xlk.paperless.standard.view.draw.DrawActivity;

import static xlk.paperless.standard.data.Values.haveNewPlayInform;
import static xlk.paperless.standard.data.Values.isMandatoryPlaying;
import static xlk.paperless.standard.view.draw.DrawActivity.isDrawing;

public class VideoActivity extends BaseActivity implements IVideo, WlOnGlSurfaceViewOncreateListener {

    private final String TAG = "VideoActivity-->";
    private ConstraintLayout video_root_layout;
    private MyGLSurfaceView video_view;
    private VideoPresenter presenter;
    private PopupWindow popView;
    private SeekBar seekBar;
    private TextView pop_video_time, pop_video_current_time;
    private WmScreenMemberAdapter memberAdapter;
    private WmProjectorAdapter projectorAdapter;
    private PopupWindow popScreen;
    private VideoActivity cxt;
    private LinearLayout pop_video_schedule;
    private RelativeLayout play_mp3_view;
    private ImageView opticalDisk, plectrum;
    private TextView video_top_title;

    private ObjectAnimator opticalDiskAnimator;
    private ObjectAnimator plectrumAnimator;
    private int playAction;
    private int mStatus = -1;
    private int lastPer;
    private String lastSec;
    private String lastTotal;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        cxt = this;
        initView();
        presenter = new VideoPresenter(this, this);
        presenter.queryMember();
        initAdapter();
        showVideoOrMusicUI(getIntent());
    }

    private void showVideoOrMusicUI(Intent intent) {
        Values.isVideoPlaying = true;
        if (isMandatoryPlaying) {
            setCanNotExit();
        }
        playAction = intent.getIntExtra(Constant.EXTRA_VIDEO_ACTION, -1);
        int subtype = intent.getIntExtra(Constant.EXTRA_VIDEO_SUBTYPE, -1);
        int deivceid = intent.getIntExtra(Constant.EXTRA_VIDEO_DEVICE_ID, -1);
        if (subtype == Constant.MEDIA_FILE_TYPE_MP3) {
            //如果当前播放的是mp3文件，则只显示MP3控件
            play_mp3_view.setVisibility(View.VISIBLE);
            video_view.setVisibility(View.GONE);
            presenter.releasePlay();
        } else {
            play_mp3_view.setVisibility(View.GONE);
            video_view.setVisibility(View.VISIBLE);
            video_view.setOnGlSurfaceViewOncreateListener(this);
            if (playAction == InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_STREAMPLAY_VALUE) {
                String devName = presenter.queryDevName(deivceid);
                LogUtil.i(TAG, "showVideoOrMusicUI devName=" + devName);
                video_top_title.setText(devName);
            }
        }
        if (popView != null && popView.isShowing()) {
            pop_video_schedule.setVisibility(playAction == InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_STREAMPLAY_VALUE ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showVideoOrMusicUI(intent);
    }

    private void initAdapter() {
        memberAdapter = new WmScreenMemberAdapter(R.layout.item_single_button, presenter.onLineMember);
        projectorAdapter = new WmProjectorAdapter(R.layout.item_single_button, presenter.onLineProjectors);
    }

    @Override
    public void notifyOnLineAdapter() {
        if (memberAdapter != null) {
            memberAdapter.notifyDataSetChanged();
            memberAdapter.notifyChecks();
        }
        if (projectorAdapter != null) {
            projectorAdapter.notifyDataSetChanged();
            projectorAdapter.notifyChecks();
        }
    }

    @Override
    public void setCanNotExit() {
        if (popView != null && popView.isShowing()) {
            popView.dismiss();
        }
        video_root_layout.setClickable(false);
    }

    @Override
    public void setCodecType(int type) {
        video_view.setCodecType(type);
    }

    @Override
    public void updateYuv(int w, int h, byte[] y, byte[] u, byte[] v) {
        setCodecType(0);
        video_view.setFrameData(w, h, y, u, v);
    }

    @Override
    public void updateProgressUi(int per, String currentTime, String totalTime) {
        if (seekBar != null && pop_video_time != null && pop_video_current_time != null) {
            lastPer = per;
            lastSec = currentTime;
            lastTotal = totalTime;
            seekBar.setProgress(per);
            pop_video_current_time.setText(currentTime);
            pop_video_time.setText(totalTime);
        }
    }

    private void initView() {
        video_root_layout = (ConstraintLayout) findViewById(R.id.video_root_layout);
        play_mp3_view = (RelativeLayout) findViewById(R.id.play_mp3_view);
        opticalDisk = (ImageView) findViewById(R.id.opticalDisk);
        plectrum = (ImageView) findViewById(R.id.plectrum);
        video_top_title = (TextView) findViewById(R.id.video_top_title);
        video_view = (MyGLSurfaceView) findViewById(R.id.video_view);
        video_root_layout.setOnClickListener(v -> {
            if (popView != null && popView.isShowing()) {
                video_top_title.setVisibility(View.GONE);
                popView.dismiss();
                return;
            }
            createPop();
        });
    }

    private void updateHideTimer() {
        if (timer == null) {
            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    LogUtils.i("定时隐藏掉弹框");
                    if (popView != null && popView.isShowing()) {
                        runOnUiThread(() -> {
                            video_top_title.setVisibility(View.GONE);
                            popView.dismiss();
                        });
                        timer.cancel();
                        timer.purge();
                        timer = null;
                    }
                }
            };
            timer.schedule(timerTask, 5 * 1000);
        } else {
            timer.cancel();
            timer.purge();
            timer = null;
            updateHideTimer();
        }
    }

    private void createPop() {
        View inflate = LayoutInflater.from(this).inflate(R.layout.pop_video_bottom, null);
        popView = new PopupWindow(inflate, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popView.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        popView.setTouchable(true);
        // true:设置触摸外面时消失
        popView.setOutsideTouchable(true);
        popView.setFocusable(true);
        popView.setAnimationStyle(R.style.pop_Animation);
        popView.showAtLocation(video_root_layout, Gravity.BOTTOM, 0, 0);
        video_top_title.setVisibility(View.VISIBLE);
        pop_video_current_time = inflate.findViewById(R.id.pop_video_current_time);
        pop_video_time = inflate.findViewById(R.id.pop_video_time);
        seekBar = inflate.findViewById(R.id.pop_video_seekBar);
        pop_video_schedule = inflate.findViewById(R.id.pop_video_schedule);
        pop_video_schedule.setVisibility(playAction == InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_STREAMPLAY_VALUE ? View.GONE : View.VISIBLE);

        /**  手动设置隐藏PopupWindow时保存的信息  */
        seekBar.setProgress(lastPer);
        pop_video_current_time.setText(lastSec);
        pop_video_time.setText(lastTotal);
        updateHideTimer();
        inflate.findViewById(R.id.pop_video_play).setOnClickListener(v -> {
            presenter.playOrPause();
            updateHideTimer();
        });
        inflate.findViewById(R.id.pop_video_stop).setOnClickListener(v -> {
            presenter.stopPlay();
            popView.dismiss();
            finish();
            updateHideTimer();
        });
        inflate.findViewById(R.id.pop_video_screen_shot).setOnClickListener(v -> {
            presenter.cutVideoImg();
            video_view.cutVideoImg();
            updateHideTimer();
        });
        inflate.findViewById(R.id.pop_video_launch_screen).setOnClickListener(v -> {
            showScreenPop(1);
            updateHideTimer();
        });
        inflate.findViewById(R.id.pop_video_stop_screen).setOnClickListener(v -> {
//            showScreenPop(2);
            presenter.stopPlay();
            updateHideTimer();
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                presenter.setPlayPlace(seekBar.getProgress());
                updateHideTimer();
            }
        });
        popView.setOnDismissListener(() -> video_top_title.setVisibility(View.GONE));
    }

    private void showScreenPop(int type) {
        View inflate = LayoutInflater.from(this).inflate(R.layout.wm_screen_view, null);
        popScreen = PopUtil.create(inflate, video_view);
        CheckBox cb_mandatory = inflate.findViewById(R.id.wm_screen_mandatory);
        TextView title = inflate.findViewById(R.id.wm_screen_title);
        CheckBox cb_attendee = inflate.findViewById(R.id.wm_screen_cb_attendee);
        CheckBox cb_projector = inflate.findViewById(R.id.wm_screen_cb_projector);
        Button launch = inflate.findViewById(R.id.wm_screen_launch);
        Button cancel = inflate.findViewById(R.id.wm_screen_cancel);
        RecyclerView rv_attendee = inflate.findViewById(R.id.wm_screen_rv_attendee);
        RecyclerView rv_projector = inflate.findViewById(R.id.wm_screen_rv_projector);
        title.setText(type == 1 ? getString(R.string.launch_video_title) : getString(R.string.stop_video_title));
        launch.setText(type == 1 ? getString(R.string.launch_screen) : getString(R.string.stop_screen));
        rv_attendee.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        rv_attendee.setAdapter(memberAdapter);
        memberAdapter.setOnItemClickListener((adapter, view, position) -> {
            memberAdapter.choose(presenter.onLineMember.get(position).getDeviceDetailInfo().getDevcieid());
            cb_attendee.setChecked(memberAdapter.isChooseAll());
        });
        cb_attendee.setOnClickListener(v -> {
            boolean checked = cb_attendee.isChecked();
            cb_attendee.setChecked(checked);
            memberAdapter.setChooseAll(checked);
        });
        rv_projector.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        rv_projector.setAdapter(projectorAdapter);
        projectorAdapter.setOnItemClickListener((adapter, view, position) -> {
            projectorAdapter.choose(presenter.onLineProjectors.get(position).getDevcieid());
            cb_projector.setChecked(projectorAdapter.isChooseAll());
        });
        cb_projector.setOnClickListener(v -> {
            boolean checked = cb_projector.isChecked();
            cb_projector.setChecked(checked);
            projectorAdapter.setChooseAll(checked);
        });
        cancel.setOnClickListener(v -> popScreen.dismiss());
        launch.setOnClickListener(v -> {
            List<Integer> ids = memberAdapter.getChooseIds();
            ids.addAll(projectorAdapter.getChooseIds());
            if (ids.isEmpty()) {
                ToastUtil.show(R.string.err_target_NotNull);
            } else {
                if (type == 1) {//发起
                    int value = cb_mandatory.isChecked() ?
                            InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE : 0;
                    presenter.mediaPlayOperate(ids, value);
                } else {//结束
                    presenter.stopPlay();
                }
                popScreen.dismiss();
            }
        });
        //默认全选
        cb_attendee.performClick();
        cb_projector.performClick();
    }

    @Override
    public void updateTopTitle(String title) {
        video_top_title.setText(title);
    }

    @Override
    public void updateAnimator(int status) {
        if (mStatus == status) return;
        mStatus = status;
        LogUtil.i(TAG, "updateAnimator 新的状态：" + mStatus);
        //0=播放中，1=暂停，2=停止,3=恢复
        switch (mStatus) {
            case 0:
                startAnimator();
                break;
            case 1:
                stopAnimator();
                break;
        }
    }

    private void plectrum(float from, float to, long duration) {
        plectrumAnimator = ObjectAnimator.ofFloat(plectrum, "rotation", from, to);
        plectrum.setPivotX(1);
        plectrum.setPivotY(1);
        plectrumAnimator.setDuration(duration);
        plectrumAnimator.start();
    }

    private void startAnimator() {
        LogUtil.i(TAG, "startAnimator ");
        plectrum(0f, 30f, 500);
        opticalDiskAnimator = ObjectAnimator.ofFloat(opticalDisk, "rotation", 0f, 360f);
        opticalDiskAnimator.setDuration(3000);
        opticalDiskAnimator.setRepeatCount(ValueAnimator.INFINITE);
        opticalDiskAnimator.setRepeatMode(ValueAnimator.RESTART);
        opticalDiskAnimator.setInterpolator(new LinearInterpolator());
        opticalDiskAnimator.start();
    }

    private void stopAnimator() {
        LogUtil.i(TAG, "stopAnimator ");
        if (opticalDiskAnimator != null) {
            opticalDiskAnimator.cancel();
            opticalDiskAnimator = null;
        }
        plectrum(30f, 0f, 500L);
    }

    @Override
    public void close() {
        //500毫秒之后再判断是否退出
        haveNewPlayInform = false;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LogUtil.i(TAG, "close :   --->>> haveNewPlayInform= " + haveNewPlayInform);
                if (!haveNewPlayInform) {
                    finish();
                } else {
                    timer.cancel();
                    timer.purge();
                }
            }
        }, 500);
    }

    @Override
    public void onBackPressed() {
        if (!isMandatoryPlaying) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (popView != null && popView.isShowing()) {
            popView.dismiss();
        }
        super.onDestroy();
        Values.isVideoPlaying = false;
        isMandatoryPlaying = false;
//        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_SHOW_FAB).build());
        presenter.onDestroy();
        presenter.stopPlay();
        presenter.releaseMediaRes();
        presenter.releasePlay();
        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_SHOW_FAB).build());
    }

    @Override
    public void onGlSurfaceViewOncreate(Surface surface) {
        presenter.setSurface(surface);
    }

    @Override
    public void onCutVideoImg(Bitmap bitmap) {
        LogUtil.i(TAG, "onCutVideoImg -->" + "截图成功");
        if (bitmap != null) {
            FabService.screenShotBitmap = bitmap;
            if (isDrawing) {
                EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_SCREEN_SHOT).build());
            } else {
                Intent intent = new Intent(cxt, DrawActivity.class);
                startActivity(intent);
            }
        }
    }
}
