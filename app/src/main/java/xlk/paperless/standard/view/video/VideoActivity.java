package xlk.paperless.standard.view.video;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.WmProjectorAdapter;
import xlk.paperless.standard.adapter.WmScreenMemberAdapter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.service.BackstageService;
import xlk.paperless.standard.service.FabService;
import xlk.paperless.standard.ui.video.MyGLSurfaceView;
import xlk.paperless.standard.ui.video.WlOnGlSurfaceViewOncreateListener;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.PopUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.BaseActivity;
import xlk.paperless.standard.view.MyApplication;
import xlk.paperless.standard.view.draw.DrawActivity;

import static xlk.paperless.standard.service.BackstageService.haveNewPlayInform;
import static xlk.paperless.standard.view.draw.DrawActivity.isDrawing;

public class VideoActivity extends BaseActivity implements IVideo {

    private final String TAG = "VideoActivity-->";
    private ConstraintLayout video_root_layout;
    private MyGLSurfaceView video_view;
    private VideoPresenter presenter;
    private PopupWindow popView;
    private SeekBar seekBar;
    private TextView pop_video_time;
    private int lastPer;
    private String lastCurrentTime;
    private String lastTotalTime;
    private WmScreenMemberAdapter memberAdapter;
    private WmProjectorAdapter projectorAdapter;
    private PopupWindow popScreen;
    private VideoActivity cxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        BackstageService.isVideoPlaying = true;
        cxt = this;
        initView();
        if (BackstageService.isMandatoryPlaying) {
            setCanNotExit();
        }
        presenter = new VideoPresenter(this, this);
        presenter.queryMember();
        initAdapter();
        video_view.setOnGlSurfaceViewOncreateListener(new WlOnGlSurfaceViewOncreateListener() {
            @Override
            public void onGlSurfaceViewOncreate(Surface surface) {
                presenter.setSurface(surface);
                presenter.register();
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
        });
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
        video_view.setClickable(false);
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
        if (seekBar != null && pop_video_time != null) {
            lastPer = per;
            lastCurrentTime = currentTime;
            lastTotalTime = currentTime;
            seekBar.setProgress(per);
            pop_video_time.setText(currentTime + " / " + totalTime);
        }
    }

    private void initView() {
        video_root_layout = (ConstraintLayout) findViewById(R.id.video_root_layout);
        video_view = (MyGLSurfaceView) findViewById(R.id.video_view);
        video_view.setOnClickListener(v -> {
            if (popView != null && popView.isShowing()) {
                popView.dismiss();
            } else {
                createPop();
            }
        });
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
        pop_video_time = inflate.findViewById(R.id.pop_video_time);
        inflate.findViewById(R.id.pop_video_play).setOnClickListener(v -> {
            presenter.playOrPause();
        });
        inflate.findViewById(R.id.pop_video_stop).setOnClickListener(v -> {
            presenter.stopPlay();
            popView.dismiss();
            finish();
        });
        inflate.findViewById(R.id.pop_video_screen_shot).setOnClickListener(v -> {
            presenter.cutVideoImg();
            video_view.cutVideoImg();
        });
        inflate.findViewById(R.id.pop_video_launch_screen).setOnClickListener(v -> {
            showScreenPop(1);
        });
        inflate.findViewById(R.id.pop_video_stop_screen).setOnClickListener(v -> {
//            showScreenPop(2);
            presenter.stopPlay();
        });
        seekBar = inflate.findViewById(R.id.pop_video_seekBar);
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
            }
        });
    }

    private void showScreenPop(int type) {
        View inflate = LayoutInflater.from(this).inflate(R.layout.wm_screen_view, null);
        popScreen = PopUtil.create(inflate, MyApplication.screen_width / 2, MyApplication.screen_height / 2, true, video_view);
        inflate.findViewById(R.id.wm_screen_mandatory).setVisibility(View.INVISIBLE);
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
                ToastUtil.show(this, R.string.err_target_NotNull);
            } else {
                if (type == 1) {//发起
                    presenter.mediaPlayOperate(ids);
                } else {//结束
                    presenter.stopPlay();
                }
                popScreen.dismiss();
            }
        });

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
        if (!BackstageService.isMandatoryPlaying) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (popView != null && popView.isShowing()) {
            popView.dismiss();
        }
        super.onDestroy();
        BackstageService.isVideoPlaying = false;
        BackstageService.isMandatoryPlaying = false;
        presenter.unregister();
        presenter.stopPlay();
        presenter.releaseMediaRes();
    }

}
