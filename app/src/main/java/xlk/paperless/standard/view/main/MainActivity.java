package xlk.paperless.standard.view.main;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acker.simplezxing.activity.CaptureActivity;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.mogujie.tt.protobuf.InterfaceAdmin;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.MainBindMemberAdapter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.helper.SharedPreferenceHelper;
import xlk.paperless.standard.ui.ArtBoard;
import xlk.paperless.standard.util.AppUtil;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.IniUtil;
import xlk.paperless.standard.util.PopUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BaseActivity;
import xlk.paperless.standard.view.App;
import xlk.paperless.standard.view.admin.AdminActivity;
import xlk.paperless.standard.view.meet.MeetingActivity;

import static xlk.paperless.standard.data.Constant.EXTRA_ADMIN_ID;
import static xlk.paperless.standard.data.Constant.EXTRA_ADMIN_NAME;
import static xlk.paperless.standard.data.Constant.EXTRA_ADMIN_PASSWORD;
import static xlk.paperless.standard.data.Values.camera_height;
import static xlk.paperless.standard.data.Values.camera_width;
import static xlk.paperless.standard.util.ConvertUtil.s2b;
import static xlk.paperless.standard.view.App.MAX_HEIGHT;
import static xlk.paperless.standard.view.App.MAX_WIDTH;
import static xlk.paperless.standard.view.App.mIntent;
import static xlk.paperless.standard.view.App.mMediaProjectionManager;
import static xlk.paperless.standard.view.App.mResult;

/**
 * @author xlk
 * @date 2020年3月9日
 */
public class MainActivity extends BaseActivity implements IMain, View.OnClickListener {

    private final String TAG = "MainActivity-->";
    private MainPresenter presenter;
    private TextView week_tv_main, date_tv_main, time_tv_main, company_tv_main, member_tv_main,
            meet_tv_main, seat_tv_main, post_tv_main, unit_tv_main, member_role, app_version, meet_state, note_info;
    private Button enter_btn_main, set_btn_main;
    private ImageView logo_iv_main, iv_set_main, iv_close_main;
    private RelativeLayout date_relative_main;
    private ConstraintLayout main_root_layout;

    private PopupWindow createMemberPop;
    private long last;
    private PopupWindow bindMemberPop;
    private MainBindMemberAdapter adapter;
    private int result;
    private Intent intent;
    private boolean toSetting = false;
    private AlertDialog netDialog;
    private PopupWindow loginPop;
    private String loginPwd;
    private String loginUser;
    private Button loginBtn;
    private PopupWindow upgradePop;
    private PopupWindow configPop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initPermissions();
    }

    private void initPermissions() {
        String[] pers = new String[]{
                Permission.WRITE_EXTERNAL_STORAGE,
                Permission.READ_EXTERNAL_STORAGE,
                Permission.RECORD_AUDIO,
                Permission.CAMERA,
                Permission.READ_PHONE_STATE
        };
        ArrayList<String> nos = new ArrayList<>();
        for (String per : pers) {
            if (ContextCompat.checkSelfPermission(this, per) == PackageManager.PERMISSION_DENIED) {
                nos.add(per);
            }
        }
        if (!nos.isEmpty()) {
            String[] applys = nos.toArray(new String[nos.size()]);
            ActivityCompat.requestPermissions(this, applys, 1);
        } else {
            start();
        }

        /*XXPermissions.with(this)
                // 可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                // 支持请求6.0悬浮窗权限8.0请求安装权限 , Permission.REQUEST_INSTALL_PACKAGES
                .permission(
                        Permission.WRITE_EXTERNAL_STORAGE,
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.RECORD_AUDIO,
                        Permission.CAMERA,
                        Permission.READ_PHONE_STATE
                )
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean all) {
                        LogUtils.d(TAG, "hasPermission -->：all=" + all + ",获取的权限=" + granted.toString());
                        if (all) start();
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        LogUtils.d(TAG, "noPermission -->未获取的权限：" + denied.toString());
                        initPermissions();
                    }
                });*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean isAllGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                // 所有的权限都授予了
                LogUtils.e("所有的权限都授予了");
            } else {
                initPermissions();
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                //容易判断错
                //MyDialog("提示", "某些权限未开启,请手动开启", 1) ;
            }
        }
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
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        LogUtils.e(TAG, "useXX noPermission  -->" + denied);
                    }
                });
    }

    private void start() {
        LogUtils.d(TAG, "start --> 开始 ");
        ((App) getApplication()).openBackstageService(true);
        presenter = new MainPresenter(this, this);
        request();
    }

    private void initial() {
        try {
            initCameraSize();
        } catch (Exception e) {
            LogUtils.e(TAG, "initial --> 相机使用失败：" + e.toString());
            e.printStackTrace();
        }
        presenter.initConfFile();
        checkNetWork();
    }

    @Override
    public void showUpgradeDialog(String content, InterfaceBase.pbui_Type_MeetUpdateNotify info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                File file = new File(info.getUpdatepath().toStringUtf8() + "/update.apk");
                LogUtils.i(TAG, "showUpgradeDialog 更新内容=" + content);
                View inflate = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_upgrade, null);
                upgradePop = new PopupWindow(inflate, Values.half_width, Values.half_height);
                upgradePop.setBackgroundDrawable(new BitmapDrawable());
                // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
                upgradePop.setTouchable(true);
                // true:设置触摸外面时消失
                upgradePop.setOutsideTouchable(false);
                upgradePop.setFocusable(false);
                upgradePop.setAnimationStyle(R.style.pop_Animation);
                upgradePop.showAtLocation(main_root_layout, Gravity.CENTER, 0, 0);
                TextView tv_new_version = inflate.findViewById(R.id.tv_new_version);
                TextView tv_old_version = inflate.findViewById(R.id.tv_old_version);
                TextView tv_content = inflate.findViewById(R.id.tv_content);
                tv_new_version.setText(info.getNewhardver() + "." + info.getNewsoftver());
                tv_old_version.setText(info.getLocalhardver() + "." + info.getLocalsoftver());
                tv_content.setText(content);
                //下次更新
                inflate.findViewById(R.id.btn_next_time).setOnClickListener(v -> {
                    upgradePop.dismiss();
                });
                //立即更新
                inflate.findViewById(R.id.btn_upgrade).setOnClickListener(v -> {
                    AppUtils.installApp(file);
                    upgradePop.dismiss();
                });
            }
        });
    }

    @Override
    public void checkNetWork() {
        Values.isOneline = AppUtil.isNetworkAvailable(this) ? 1 : 0;
        if (Values.isOneline == 1) {
            LogUtils.d(TAG, "checkNetWork -->" + "网络可用");
            if (netDialog != null && netDialog.isShowing()) {
                netDialog.dismiss();
            }
            if (!Values.initializationIsOver) {
                LogUtils.i(TAG, "checkNetWork 进行初始化");
                presenter.initialization();
            } else {//已经初始化完毕了
                initialized();
            }
        } else {
            LogUtils.d(TAG, "checkNetWork -->" + "网络不可用");
            showNetDialog();
        }
    }

    private void showNetDialog() {
        if (netDialog != null) {
            if (!netDialog.isShowing()) {
                netDialog.show();
            }
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.please_check_network));
        builder.setPositiveButton(R.string.open_network, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                toSetting = true;
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        netDialog = builder.create();
        netDialog.show();
    }

    @Override
    protected void onRestart() {
        LogUtils.d(TAG, "onRestart -->是否从设置界面回来：" + toSetting);
        if (toSetting) {
            toSetting = false;
            initial();
        }
        super.onRestart();
    }

    private void initCameraSize() {
        int type = 1;
        LogUtils.d(TAG, "initCameraSize :   --> ");
        //获取摄像机的个数 一般是前/后置两个
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras < 2) {
            LogUtils.d(TAG, "initCameraSize: 该设备只有后置像头");
            //如果没有2个则说明只有后置像头
            type = 0;
        }
        ArrayList<Integer> supportW = new ArrayList<>();
        ArrayList<Integer> supportH = new ArrayList<>();
        int largestW = 0, largestH = 0;
        Camera c = Camera.open(type);
        Camera.Parameters param = null;
        if (c != null) {
            param = c.getParameters();
        }
        if (param == null) {
            return;
        }
        for (int i = 0; i < param.getSupportedPreviewSizes().size(); i++) {
            int w = param.getSupportedPreviewSizes().get(i).width, h = param.getSupportedPreviewSizes().get(i).height;
            LogUtils.d(TAG, "initCameraSize: w=" + w + " h=" + h);
            supportW.add(w);
            supportH.add(h);
        }
        for (int i = 0; i < supportH.size(); i++) {
            try {
                largestW = supportW.get(i);
                largestH = supportH.get(i);
                LogUtils.d(TAG, "initCameraSize :   --> largestW= " + largestW + " , largestH=" + largestH);
                MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", largestW, largestH);
                if (MediaCodec.createEncoderByType("video/avc").getCodecInfo().getCapabilitiesForType("video/avc").isFormatSupported(mediaFormat)) {
                    if (largestW * largestH > camera_width * camera_height) {
                        camera_width = largestW;
                        camera_height = largestH;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.setPreviewCallback(null);
                    c.stopPreview();
                    c.release();
                    c = null;
                }
            }
        }
        LogUtils.d(TAG, "initCameraSize -->" + "前置像素：" + camera_width + " X " + camera_height);
        if (camera_width * camera_height > MAX_WIDTH * MAX_HEIGHT) {
            camera_width = MAX_WIDTH;
            camera_height = MAX_HEIGHT;
        }
    }

    private void request() {
        try {
            MediaProjectionManager manager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mMediaProjectionManager = manager;
            if (intent != null && result != 0) {
                LogUtils.d(TAG, "request :  用户同意捕获屏幕 --->>> ");
                mResult = result;
                mIntent = intent;
                initial();
            } else {
                /** **** **  第一次时保存 manager  ** **** **/
                startActivityForResult(manager.createScreenCaptureIntent(), 10086);
            }
        } catch (Exception e) {
            e.printStackTrace();
            initial();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CODE && resultCode == RESULT_OK) {
            LogUtils.d(TAG, "onActivityResult :  进入扫描结果.... --> ");
            if (null != data) {
                String stringExtra = data.getStringExtra(CaptureActivity.EXTRA_SCAN_RESULT);
//                {"meetid":"128","roomid":"12"}
                String a = stringExtra.substring(11);// 128","roomid":"12"}
                String meetingid = a.substring(0, a.indexOf("\""));// 128
                String roomid = a.substring(a.indexOf(":") + 2, a.lastIndexOf("\""));// :"12
                LogUtils.d(TAG, "onActivityResult :  二维码结果 --> " + stringExtra + ",meetingid= " + meetingid + ",roomid=  " + roomid);
                try {
                    int meetingId = Integer.parseInt(meetingid);
                    int roomId = Integer.parseInt(roomid);
                    presenter.bindMeeting(meetingId, roomId);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == 10086) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    result = resultCode;
                    intent = data;
                    mResult = result;
                    mIntent = intent;
                    //保存 MediaProjection 对象,解决每次录制屏幕时需要权限的问题
                    App.mMediaProjection = mMediaProjectionManager.getMediaProjection(mResult, mIntent);
                    LogUtils.d(TAG, "onActivityResult :  用户同意捕获屏幕.. ");
                    initial();
                }
            } else {
                request();
            }
        }
    }

    private int count = 1;
    private long lastClickTime = 0;

    private void initView() {
        iv_set_main = (ImageView) findViewById(R.id.iv_set_main);
        iv_set_main.setOnClickListener(this);
        iv_close_main = (ImageView) findViewById(R.id.iv_close_main);
        iv_close_main.setOnClickListener(this);
        seat_tv_main = (TextView) findViewById(R.id.seat_tv_main);
        post_tv_main = (TextView) findViewById(R.id.post_tv_main);
        unit_tv_main = (TextView) findViewById(R.id.unit_tv_main);
        meet_tv_main = (TextView) findViewById(R.id.meet_tv_main);
        member_tv_main = (TextView) findViewById(R.id.member_tv_main);
        logo_iv_main = (ImageView) findViewById(R.id.logo_iv_main);
        company_tv_main = (TextView) findViewById(R.id.company_tv_main);
        seat_tv_main.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTime < 500) {
                count++;
                if (count == 5) {
                    LogUtils.i(TAG, "initView canLoginAdmin=" + App.canLoginAdmin);
                    App.canLoginAdmin = !App.canLoginAdmin;
                    count = 1;
                }
            } else {
                count = 1;
            }
            lastClickTime = System.currentTimeMillis();
        });

//        slideview_main = (SlideView) findViewById(R.id.slideview_main);

        enter_btn_main = (Button) findViewById(R.id.enter_btn_main);
        enter_btn_main.setOnClickListener(this);
        enter_btn_main.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LogUtils.i(TAG, "onLongClick 激活长按");
                App.canLoginAdmin = !App.canLoginAdmin;
                if (App.canLoginAdmin) {
                    ToastUtil.show(R.string.login_backstage_is_enabled);
                } else {
                    ToastUtil.show(R.string.login_backstage_is_closed);
                }
                return true;
            }
        });

        set_btn_main = (Button) findViewById(R.id.set_btn_main);
        set_btn_main.setOnClickListener(this);
        time_tv_main = (TextView) findViewById(R.id.time_tv_main);
        date_tv_main = (TextView) findViewById(R.id.date_tv_main);
        week_tv_main = (TextView) findViewById(R.id.week_tv_main);

        member_role = (TextView) findViewById(R.id.member_role);
        meet_state = (TextView) findViewById(R.id.meet_state);
        app_version = (TextView) findViewById(R.id.app_version);
        note_info = (TextView) findViewById(R.id.note_info);

        date_relative_main = (RelativeLayout) findViewById(R.id.date_relative_main);
        main_root_layout = (ConstraintLayout) findViewById(R.id.main_root_layout);
        main_root_layout.setBackgroundResource(App.isStandard ? R.drawable.bg_icon_red : R.drawable.bg_icon_blue);

    }

    @Override
    public void initialized() {
        LogUtils.d(TAG, "initialized -->");
        presenter.setInterfaceState();
        presenter.initStream();
        presenter.queryContextProperty();
    }

    @Override
    public void updateMeetingState(int state) {
        //会议状态，0为未开始会议，1为已开始会议，2为已结束会议，其它表示未加入会议无状态
        switch (state) {
            case 0:
                meet_state.setText(getString(R.string.state_meet_not));
                break;
            case 1:
                meet_state.setText(getString(R.string.state_meet_start));
                break;
            case 2:
                meet_state.setText(getString(R.string.state_meet_end));
                break;
            default:
                meet_state.setText("");
                break;
        }
    }

    @Override
    public void updateNote(String noteinfo) {
        note_info.setText(noteinfo);
    }

    @Override
    public void updateMemberRole(String role) {
        member_role.setText(role);
    }

    @Override
    public void updateVersion(String versionName) {
        app_version.setText(getString(R.string.curr_version, versionName));
    }

    @Override
    public void updateTime(long millisecond) {
        String[] date = DateUtil.getGTMDate(millisecond);
        String day = date[0];
        String week = date[1];
        String time = date[2];
        date_tv_main.setText(day);
        week_tv_main.setText(week);
        time_tv_main.setText(time);
    }

    @Override
    public void isShowLogo(boolean isShow) {
        logo_iv_main.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateCompany(String company) {
        LogUtils.i(TAG, "updateCompany 公司=" + company);
        company_tv_main.setText(company);
    }

    @Override
    public void updateSeatName(String seatName) {
        Values.localDeviceName = seatName;
        LogUtils.d(TAG, "updateSeatName 席位：" + seatName);
        seat_tv_main.setText(getString(R.string.set_name_, seatName));
    }

    @Override
    public void updateUnit(String text) {
        LogUtils.d(TAG, "updateJob -->" + "单位：" + text);
        unit_tv_main.setText(getString(R.string.unit_name_, text));
    }

    @Override
    public void updateUI(InterfaceDevice.pbui_Type_DeviceFaceShowDetail devMeetInfo) {
        LogUtils.d(TAG, "updateUI 参会人id=" + Values.localMemberId + "，会议id=" + Values.localMeetingId);
        if (Values.localMemberId > 0) {
            if (bindMemberPop != null && bindMemberPop.isShowing()) {
                LogUtils.d(TAG, "已经绑定了参会人了隐藏掉绑定参会人弹框");
                bindMemberPop.dismiss();
            }
            if (createMemberPop != null && createMemberPop.isShowing()) {
                LogUtils.d(TAG, "已经绑定了参会人了隐藏掉新建参会人弹框");
                createMemberPop.dismiss();
            }
        }
        member_tv_main.setText(devMeetInfo.getMembername().toStringUtf8());
        meet_tv_main.setText(devMeetInfo.getMeetingname().toStringUtf8());
        unit_tv_main.setText(getString(R.string.unit_name_, devMeetInfo.getCompany().toStringUtf8()));
        post_tv_main.setText(getString(R.string.job_name_, devMeetInfo.getJob().toStringUtf8()));
    }

    @Override
    public void updateBackground(Drawable drawable) {
        main_root_layout.setBackground(drawable);
    }

    @Override
    public void updateLogo(Drawable drawable) {
        logo_iv_main.setImageDrawable(drawable);
    }

    @Override
    public void update(int resId, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo itemInfo) {
        float lx = itemInfo.getLx();
        float ly = itemInfo.getLy();
        float bx = itemInfo.getBx();
        float by = itemInfo.getBy();
        ConstraintSet set = new ConstraintSet();
        set.clone(main_root_layout);
        //设置控件的大小
        float width = (bx - lx) / 100 * Values.screen_width;
        float height = (by - ly) / 100 * Values.screen_height;
        set.constrainWidth(resId, (int) width);
        set.constrainHeight(resId, (int) height);
//        LogUtils.d(TAG, "update: 控件大小 当前控件宽= " + width + ", 当前控件高= " + height);
        float biasX, biasY;
        float halfW = (bx - lx) / 2 + lx;
        float halfH = (by - ly) / 2 + ly;

        if (lx == 0) biasX = 0;
        else if (lx > 50) biasX = bx / 100;
        else biasX = halfW / 100;

        if (ly == 0) biasY = 0;
        else if (ly > 50) biasY = by / 100;
        else biasY = halfH / 100;
//        LogUtils.d(TAG, "update: biasX= " + biasX + ",biasY= " + biasY);
        set.setHorizontalBias(resId, biasX);
        set.setVerticalBias(resId, biasY);
        set.applyTo(main_root_layout);
    }

    @Override
    public void updateTv(int resId, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo itemInfo) {
        update(resId, itemInfo);
        int faceid = itemInfo.getFaceid();
        int flag = itemInfo.getFlag();
        boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
        int fontsize = itemInfo.getFontsize();
        int color = itemInfo.getColor();
        int align = itemInfo.getAlign();
        int fontflag = itemInfo.getFontflag();
        String fontName = itemInfo.getFontname().toStringUtf8();
        TextView tv = findViewById(resId);
        tv.setTextColor(color);
        tv.setTextSize(fontsize);
        tv.setVisibility(isShow ? View.VISIBLE : View.GONE);
        //字体样式
        if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD.getNumber()) {//加粗
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_LEAN.getNumber()) {//倾斜
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_UNDERLINE.getNumber()) {//下划线
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));//暂时用倾斜加粗
        } else {//正常文本
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
        //对齐方式
        if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT.getNumber()) {//左对齐
            tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT.getNumber()) {//右对齐
            tv.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_HCENTER.getNumber()) {//水平对齐
            tv.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_TOP.getNumber()) {//上对齐
            tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_BOTTOM.getNumber()) {//下对齐
            tv.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_VCENTER.getNumber()) {//垂直对齐
            tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        } else {
            tv.setGravity(Gravity.CENTER);
        }
        //字体类型
        Typeface kt_typeface;
        if (!TextUtils.isEmpty(fontName)) {
            switch (fontName) {
                case "楷体":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "kaiti.ttf");
                    break;
                case "隶书":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "lishu.ttf");
                    break;
                case "微软雅黑":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "weiruanyahei.ttf");
                    break;
                case "黑体":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "heiti.ttf");
                    break;
                case "小楷":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "xiaokai.ttf");
                    break;
                default:
                    kt_typeface = Typeface.createFromAsset(getAssets(), "fangsong.ttf");
                    break;
            }
            tv.setTypeface(kt_typeface);
        }
    }

    @Override
    public void updateBtn(int resId, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo itemInfo) {
//        update(resId, itemInfo);
        int faceid = itemInfo.getFaceid();
        int flag = itemInfo.getFlag();
        boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
        int fontsize = itemInfo.getFontsize();
        int color = itemInfo.getColor();
        int align = itemInfo.getAlign();
        int fontflag = itemInfo.getFontflag();
        String fontName = itemInfo.getFontname().toStringUtf8();
        Button btn = findViewById(resId);
        btn.setTextColor(color);
        btn.setTextSize(fontsize);
        btn.setVisibility(isShow ? View.VISIBLE : View.GONE);
        //字体样式
        if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD.getNumber()) {//加粗
            btn.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_LEAN.getNumber()) {//倾斜
            btn.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_UNDERLINE.getNumber()) {//下划线
            btn.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));//暂时用倾斜加粗
        } else {//正常文本
            btn.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
        //对齐方式
        if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT.getNumber()) {//左对齐
            btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT.getNumber()) {//右对齐
            btn.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_HCENTER.getNumber()) {//水平对齐
            btn.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_TOP.getNumber()) {//上对齐
            btn.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_BOTTOM.getNumber()) {//下对齐
            btn.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_VCENTER.getNumber()) {//垂直对齐
            btn.setGravity(Gravity.CENTER_VERTICAL);
        } else {
            btn.setGravity(Gravity.CENTER);
        }
        //字体类型
        Typeface kt_typeface;
        if (!TextUtils.isEmpty(fontName)) {
            switch (fontName) {
                case "楷体":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "kaiti.ttf");
                    break;
                case "隶书":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "lishu.ttf");
                    break;
                case "微软雅黑":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "weiruanyahei.ttf");
                    break;
                case "黑体":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "heiti.ttf");
                    break;
                case "小楷":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "xiaokai.ttf");
                    break;
                default:
                    kt_typeface = Typeface.createFromAsset(getAssets(), "fangsong.ttf");
                    break;
            }
            btn.setTypeface(kt_typeface);
        }
    }

    @Override
    public void updateEnterView(int resId, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo itemInfo) {
        update(resId, itemInfo);
//        int faceid = itemInfo.getFaceid();
//        int flag = itemInfo.getFlag();
//        boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
//        int fontsize = itemInfo.getFontsize();
//        int color = itemInfo.getColor();
//        int align = itemInfo.getAlign();
//        int fontflag = itemInfo.getFontflag();
//        String fontName = itemInfo.getFontname().toStringUtf8();
//        SlideView slideView = findViewById(resId);
//        btn.setTextColor(color);
//        btn.setTextSize(fontsize);
//        //字体样式
//        if (flag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD.getNumber()) {//加粗
//            btn.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
//        } else if (flag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_LEAN.getNumber()) {//倾斜
//            btn.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
//        } else if (flag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_UNDERLINE.getNumber()) {//下划线
//            btn.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));//暂时用倾斜加粗
//        } else {//正常文本
//            btn.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
//        }
        //对齐方式
//        if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT.getNumber()) {//左对齐
//            btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
//        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT.getNumber()) {//右对齐
//            btn.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
//        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_HCENTER.getNumber()) {//水平对齐
//            btn.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_TOP.getNumber()) {//上对齐
//            btn.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
//        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_BOTTOM.getNumber()) {//下对齐
//            btn.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
//        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_VCENTER.getNumber()) {//垂直对齐
//            btn.setGravity(Gravity.CENTER_VERTICAL);
//        } else {
//            btn.setGravity(Gravity.CENTER);
//        }
        //字体类型
//        Typeface kt_typeface;
//        if (!TextUtils.isEmpty(fontName)) {
//            if (fontName.equals("楷体")) {
//                kt_typeface = Typeface.createFromAsset(getAssets(), "kaiti.ttf");
//            } else if (fontName.equals("宋体")) {
//                kt_typeface = Typeface.createFromAsset(getAssets(), "fangsong.ttf");
//            } else if (fontName.equals("隶书")) {
//                kt_typeface = Typeface.createFromAsset(getAssets(), "lishu.ttf");
//            } else if (fontName.equals("微软雅黑")) {
//                kt_typeface = Typeface.createFromAsset(getAssets(), "weiruanyahei.ttf");
//            } else {
//                kt_typeface = Typeface.createFromAsset(getAssets(), "fangsong.ttf");
//            }
//            btn.setTypeface(kt_typeface);
//        }
    }

    @Override
    public void updateDate(int resId, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo itemInfo) {
        update(resId, itemInfo);
        int faceid = itemInfo.getFaceid();
        int flag = itemInfo.getFlag();
        boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
        date_relative_main.setVisibility(isShow ? View.VISIBLE : View.GONE);
        int fontsize = itemInfo.getFontsize();
        int color = itemInfo.getColor();
        int align = itemInfo.getAlign();
        int fontflag = itemInfo.getFontflag();
        String fontName = itemInfo.getFontname().toStringUtf8();
        time_tv_main.setTextColor(color);
        time_tv_main.setTextSize(fontsize);
        date_tv_main.setTextColor(color);
        date_tv_main.setTextSize(fontsize);
        week_tv_main.setTextColor(color);
        week_tv_main.setTextSize(fontsize);
        Typeface typeface = null;
        //字体样式
        if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD.getNumber()) {//加粗
            typeface = Typeface.defaultFromStyle(Typeface.BOLD);
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_LEAN.getNumber()) {//倾斜
            typeface = Typeface.defaultFromStyle(Typeface.ITALIC);
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_UNDERLINE.getNumber()) {//下划线
            typeface = Typeface.defaultFromStyle(Typeface.BOLD_ITALIC);
        } else {//正常文本
            typeface = Typeface.defaultFromStyle(Typeface.NORMAL);
        }
        time_tv_main.setTypeface(typeface);
        date_tv_main.setTypeface(typeface);
        week_tv_main.setTypeface(typeface);
        //对齐方式
        if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT.getNumber()) {//左对齐
            date_relative_main.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            time_tv_main.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            date_tv_main.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            week_tv_main.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT.getNumber()) {//右对齐
            date_relative_main.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            time_tv_main.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            date_tv_main.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            week_tv_main.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_HCENTER.getNumber()) {//水平对齐
            date_relative_main.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            time_tv_main.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            date_tv_main.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            week_tv_main.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_TOP.getNumber()) {//上对齐
            date_relative_main.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            time_tv_main.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            date_tv_main.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            week_tv_main.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_BOTTOM.getNumber()) {//下对齐
            date_relative_main.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            time_tv_main.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            date_tv_main.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            week_tv_main.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_VCENTER.getNumber()) {//垂直对齐
            date_relative_main.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            time_tv_main.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            date_tv_main.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            week_tv_main.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        } else {
            date_relative_main.setGravity(Gravity.CENTER);
            time_tv_main.setGravity(Gravity.CENTER);
            date_tv_main.setGravity(Gravity.CENTER);
            week_tv_main.setGravity(Gravity.CENTER);
        }
        //字体类型
        Typeface kt_typeface;
        if (!TextUtils.isEmpty(fontName)) {
            switch (fontName) {
                case "楷体":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "kaiti.ttf");
                    break;
                case "黑体":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "heiti.ttf");
                    break;
                case "隶书":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "lishu.ttf");
                    break;
                case "微软雅黑":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "weiruanyahei.ttf");
                    break;
                case "小楷":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "xiaokai.ttf");
                    break;
                default:
                    kt_typeface = Typeface.createFromAsset(getAssets(), "fangsong.ttf");
                    break;
            }
            time_tv_main.setTypeface(kt_typeface);
            date_tv_main.setTypeface(kt_typeface);
            week_tv_main.setTypeface(kt_typeface);
        }
    }

    @Override
    protected void onDestroy() {
        if (netDialog != null && netDialog.isShowing()) {
            netDialog.dismiss();
        }
        if (loginPop != null && loginPop.isShowing()) {
            loginPop.dismiss();
        }
        if (configPop != null && configPop.isShowing()) {
            configPop.dismiss();
        }
        super.onDestroy();
        presenter.unregister();
        presenter.onDestroy();
        presenter = null;
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - last < 1500) {
            exit();
        } else {
            last = System.currentTimeMillis();
            ToastUtil.show(R.string.click_again_exit);
        }
    }

    private void exit() {
        ((App) getApplication()).onDestroy();
        finish();
        System.exit(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.enter_btn_main:
                if (AppUtil.isNetworkAvailable(this)) {
                    LogUtils.i("当前的会议ID=" + Values.localMeetingId + ",当前参会人ID=" + Values.localMemberId);
                    if (Values.localMeetingId == 0) {
//                    if (meet_tv_main.getText().toString().trim().isEmpty()) {
                        ToastUtil.show(R.string.join_meeting_first);
//                        jump2scan();
                    } else if (Values.localMemberId == 0) {
//                    } else if (member_tv_main.getText().toString().trim().isEmpty()) {
                        jump2bind();
                    } else {
                        readySignIn();
                    }
                } else {
                    showNetDialog();
                }
                break;
            case R.id.set_btn_main:
//                setConfiguration();
                break;
            case R.id.iv_set_main:
                if (App.canLoginAdmin) {
                    showLoginPop();
                } else {
                    setConfiguration();
                }
                break;
            case R.id.iv_close_main:
                exit();
                break;
            default:
                break;
        }
    }

    private void showLoginPop() {
        LogUtils.i(TAG, "showLoginPop ");
        boolean spIsRemember = (boolean) SharedPreferenceHelper.getData(this, SharedPreferenceHelper.key_remember, false);
        String spUser = (String) SharedPreferenceHelper.getData(this, SharedPreferenceHelper.key_user, "");
        String spPwd = (String) SharedPreferenceHelper.getData(this, SharedPreferenceHelper.key_password, "");
        View inflate = LayoutInflater.from(this).inflate(R.layout.pop_login_layout, null);
        loginPop = PopUtil.create(inflate, Values.screen_width * 3 / 5, Values.screen_height * 3 / 5, set_btn_main);
        CheckBox login_cb_remember = (CheckBox) inflate.findViewById(R.id.login_cb_remember);
        TextInputEditText login_tie_user = (TextInputEditText) inflate.findViewById(R.id.login_tie_user);
        TextInputEditText login_tie_pwd = (TextInputEditText) inflate.findViewById(R.id.login_tie_pwd);
        login_cb_remember.setChecked(spIsRemember);
        login_tie_user.setText(spUser);
        if (spIsRemember) {
            login_tie_pwd.setText(spPwd);
        }
        loginBtn = inflate.findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(v -> {
            loginUser = login_tie_user.getText().toString().trim();
            loginPwd = login_tie_pwd.getText().toString().trim();
            if (loginUser.isEmpty()) {
                ToastUtil.show(R.string.please_enter_user_name);
                return;
            }
            if (loginPwd.isEmpty()) {
                ToastUtil.show(R.string.please_enter_password);
                return;
            }
            boolean isRemember = login_cb_remember.isChecked();
            SharedPreferenceHelper.setData(MainActivity.this, SharedPreferenceHelper.key_remember, isRemember);
            if (isRemember) {
                SharedPreferenceHelper.setData(MainActivity.this, SharedPreferenceHelper.key_user, loginUser);
                SharedPreferenceHelper.setData(MainActivity.this, SharedPreferenceHelper.key_password, loginPwd);
            }
            presenter.login(loginUser, loginPwd, 1, 0);
        });
        inflate.findViewById(R.id.login_btn_set).setOnClickListener(v -> {
            setConfiguration();
        });
        inflate.findViewById(R.id.login_iv_close).setOnClickListener(v -> {
            loginPop.dismiss();
        });
    }

    @Override
    public void loginStatus(InterfaceAdmin.pbui_Type_AdminLogonStatus info) {
        //管理员登陆状态
        int err = info.getErr();
        int adminid = info.getAdminid();
        String adminname = info.getAdminname().toStringUtf8();
        int sessionid = info.getSessionid();
        LogUtils.i(TAG, "loginStatus adminid:" + adminid + ",adminname:" + adminname + ",sessionid:" + sessionid + ",err:" + err);
        switch (err) {
            //登陆成功
            case InterfaceMacro.Pb_AdminLogonStatus.Pb_ADMINLOGON_ERR_NONE_VALUE:
                ToastUtil.show(R.string.login_successful);
                Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                intent.putExtra(EXTRA_ADMIN_ID, adminid);
                intent.putExtra(EXTRA_ADMIN_NAME, adminname);
                intent.putExtra(EXTRA_ADMIN_PASSWORD, loginPwd);
                if (Values.localMeetingId != 0) {
                    jni.modifyContextProperties(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_CURMEETINGID_VALUE
                            , Values.localMeetingId);
                }
                presenter.unregister();
                finish();
                startActivity(intent);
                break;
            //密码错误
            case InterfaceMacro.Pb_AdminLogonStatus.Pb_ADMINLOGON_ERR_PSW_VALUE:
                ToastUtil.show(R.string.wrong_password);
                break;
            //服务器异常
            case InterfaceMacro.Pb_AdminLogonStatus.Pb_ADMINLOGON_ERR_EXCPT_SV_VALUE:
                ToastUtil.show(R.string.server_exception);
                break;
            //数据库异常
            case InterfaceMacro.Pb_AdminLogonStatus.Pb_ADMINLOGON_ERR_EXCPT_DB_VALUE:
                ToastUtil.show(R.string.database_exception);
                break;
            default:
                break;
        }
    }

    @Override
    public void readySignIn() {
        if (!XXPermissions.hasPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            applyAlertWindowPermission();
        } else {
            signIn();
        }
    }

    private void setConfiguration() {
        if (!IniUtil.iniFile.exists()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.configurationFileCorrupted)
                    .setPositiveButton(R.string.determine, (dialog, which) -> {
                        AppUtil.restartApplication(this);
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        } else {
            showConfigurationView();
        }
    }

    private void showConfigurationView() {
        IniUtil iniUtil = IniUtil.getInstance();
        View inflate = LayoutInflater.from(this).inflate(R.layout.pop_config_view, null);
        configPop = PopUtil.create(inflate, ScreenUtils.getScreenWidth() * 3 / 5, ScreenUtils.getScreenHeight() * 3 / 5, set_btn_main);
        EditText pop_config_ip = inflate.findViewById(R.id.pop_config_ip);
        EditText pop_config_port = inflate.findViewById(R.id.pop_config_port);
        EditText pop_config_bitrate = inflate.findViewById(R.id.pop_config_bitrate);
        CheckBox pop_config_microphone = inflate.findViewById(R.id.pop_config_microphone);
        CheckBox pop_config_multicase = inflate.findViewById(R.id.pop_config_multicase);
        CheckBox pop_config_tcp = inflate.findViewById(R.id.pop_config_tcp);
        CheckBox pop_config_disablebsf = inflate.findViewById(R.id.pop_config_disablebsf);

        String nowIp = iniUtil.get("areaaddr", "area0ip");
        String nowPort = iniUtil.get("areaaddr", "area0port");
        String videoaudio = iniUtil.get("debug", "videoaudio");
        String streamprotol = iniUtil.get("selfinfo", "streamprotol");
        String disablemulticast = iniUtil.get("debug", "disablemulticast");
        //是否开启编码过滤
        String encodingFiltering = iniUtil.get("nosdl", "disablebsf");

        if (videoaudio == null || videoaudio.isEmpty()) {
            LogUtils.d(TAG, "showConfigurationView -->" + "设置麦克风默认值");
            iniUtil.put("debug", "videoaudio", 1);
            iniUtil.store();//修改后提交
        }
        if (streamprotol == null || streamprotol.isEmpty()) {
            LogUtils.d(TAG, "showConfigurationView :  设置TCP模式默认值 --> ");
            iniUtil.put("selfinfo", "streamprotol", 1);
            iniUtil.store();//修改后提交
        }
        if (disablemulticast == null || disablemulticast.isEmpty()) {
            LogUtils.d(TAG, "showConfigurationView :  设置组播默认值 --> ");
            iniUtil.put("debug", "disablemulticast", 1);
            iniUtil.store();//修改后提交
        }
        if (encodingFiltering == null || encodingFiltering.isEmpty()) {
            LogUtils.d(TAG, "showConfigurationView -->" + "设置编码过滤默认值");
            iniUtil.put("nosdl", "disablebsf", 0);
            iniUtil.store();//修改后提交
        }

        String maxBitRateStr = iniUtil.get("OtherConfiguration", "maxBitRate");
        int defaultMax = Constant.MINIMUM_BITRATE;
        if (maxBitRateStr != null && !maxBitRateStr.isEmpty()) {
            defaultMax = Integer.parseInt(maxBitRateStr);
            if (defaultMax < Constant.MINIMUM_BITRATE) defaultMax = Constant.MINIMUM_BITRATE;
            else if (defaultMax > Constant.MAXIMUM_BITRATE) defaultMax = Constant.MAXIMUM_BITRATE;
        }
        String ipStr = "";
        String portStr = "";
        if (nowIp != null && !nowIp.isEmpty()) {
            String[] ipSplit = nowIp.split("\\.");
            for (int i = 0; i < ipSplit.length; i++) {
                if (i == 0) {
                    ipStr = ipSplit[i];
                } else {
                    ipStr += "." + ipSplit[i];
                }
            }
        }
        if (nowPort != null && !nowPort.isEmpty()) {
            portStr = nowPort;
        }

        pop_config_bitrate.setText(String.valueOf(defaultMax));
        pop_config_ip.setText(ipStr);
        pop_config_port.setText(portStr);

        streamprotol = iniUtil.get("selfinfo", "streamprotol");
        disablemulticast = iniUtil.get("debug", "disablemulticast");
        videoaudio = iniUtil.get("debug", "videoaudio");
        encodingFiltering = iniUtil.get("nosdl", "disablebsf");

        pop_config_tcp.setChecked(isEnable(streamprotol));//是否启用TCP模式
        pop_config_multicase.setChecked(isEnable(disablemulticast));//是否禁用组播
        pop_config_microphone.setChecked(isEnable(videoaudio));//是否打开麦克风
        pop_config_disablebsf.setChecked(!isEnable(encodingFiltering));//是否开启编码过滤

        inflate.findViewById(R.id.pop_config_determine).setOnClickListener(v -> {
            String newIP = pop_config_ip.getText().toString().trim();
            if (!RegexUtils.isIP(newIP)) {
                ToastUtil.show(R.string.ip_format_error);
                return;
            }
            String newPort = pop_config_port.getText().toString().trim();
            String newMaxBitRate = pop_config_bitrate.getText().toString().trim();
            if (!newIP.isEmpty() && !newPort.isEmpty() && !newMaxBitRate.isEmpty()) {
                int maxRate = Integer.parseInt(newMaxBitRate);
                if (maxRate < Constant.MINIMUM_BITRATE) {
                    ToastUtil.show(R.string.err_tooLittle);
                    return;
                }
                if (maxRate > Constant.MAXIMUM_BITRATE) {
                    ToastUtil.show(R.string.error_tooMush);
                    return;
                }
                iniUtil.put("OtherConfiguration", "maxBitRate", maxRate);
                iniUtil.put("areaaddr", "area0ip", newIP);
                iniUtil.put("areaaddr", "area0port", newPort);
                iniUtil.put("selfinfo", "streamprotol", pop_config_tcp.isChecked() ? 1 : 0);
                iniUtil.put("debug", "disablemulticast", pop_config_multicase.isChecked() ? 1 : 0);
                iniUtil.put("debug", "videoaudio", pop_config_microphone.isChecked() ? 1 : 0);
                iniUtil.put("nosdl", "disablebsf", pop_config_disablebsf.isChecked() ? 0 : 1);
                iniUtil.store();//修改后提交
                /** **** **  app重启  ** **** **/
                AppUtils.relaunchApp(true);
            } else {
                ToastUtil.show(R.string.errorContentNull);
            }
        });
        inflate.findViewById(R.id.pop_config_cancel).setOnClickListener(v -> {
            configPop.dismiss();
        });
    }

    private boolean isEnable(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            int a = Integer.parseInt(str);
            return a == 1;
        } catch (NumberFormatException e) {
            LogUtils.e(TAG, " isEnable 转换异常");
            e.printStackTrace();
        }
        return false;
    }

    public void signIn() {
        try {
            LogUtils.d(TAG, "signIn :  signinType --> " + Values.localSigninType);
            if (Values.localSigninType == InterfaceMacro.Pb_MeetSignType.Pb_signin_direct.getNumber()) {
                //直接签到
                presenter.sendSign(0, InterfaceMacro.Pb_MeetSignType.Pb_signin_direct.getNumber(), "", s2b(""));
            } else if (Values.localSigninType == InterfaceMacro.Pb_MeetSignType.Pb_signin_psw.getNumber()) {
                //个人密码签到
                enterPassword(false);
            } else if (Values.localSigninType == InterfaceMacro.Pb_MeetSignType.Pb_signin_onepsw.getNumber()) {
                //会议密码签到:和个人密码签到一样，不同的是签到类型，后台会自己判断
                enterPassword(false);
            } else if (Values.localSigninType == InterfaceMacro.Pb_MeetSignType.Pb_signin_photo.getNumber()) {
                //拍照手写签到:展示画板，点击确定后将画板保存为图片数据进行签到
                showDrawBoard("");
            } else if (Values.localSigninType == InterfaceMacro.Pb_MeetSignType.Pb_signin_onepsw_photo.getNumber()) {
                //会议密码+拍照(手写):需要先输入密码，再绘制签名
                enterPassword(true);
            } else if (Values.localSigninType == InterfaceMacro.Pb_MeetSignType.Pb_signin_psw_photo.getNumber()) {
                //个人密码+拍照(手写)
                enterPassword(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDrawBoard(String pwd) {
        View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_enter_password_draw, null);
        PopupWindow popupWindow = PopUtil.create(inflate, Values.screen_width / 3 * 2, Values.half_height, enter_btn_main);
        ArtBoard artBoard = inflate.findViewById(R.id.pwd_draw_board);
        Button pwd_draw_revoke = inflate.findViewById(R.id.pwd_draw_revoke);
        Button pwd_draw_clear = inflate.findViewById(R.id.pwd_draw_clear);
        Button pwd_draw_determine = inflate.findViewById(R.id.pwd_draw_determine);
        Button pwd_draw_cancel = inflate.findViewById(R.id.pwd_draw_cancel);
        pwd_draw_revoke.setOnClickListener(v -> artBoard.revoke());
        pwd_draw_clear.setOnClickListener(v -> artBoard.clear());
        popupWindow.setOnDismissListener(() -> {
            if (artBoard != null) {
                artBoard.clear();
            }
        });
        pwd_draw_determine.setOnClickListener(v -> {
            if (artBoard.isNotEmpty()) {
                Bitmap canvasBmp = artBoard.getCanvasBmp();
                popupWindow.dismiss();
                presenter.sendSign(0, Values.localSigninType, pwd, ConvertUtil.bmp2bs(canvasBmp));
                artBoard.clear();//不清理在画板界面就会存在于 LocalPathList集合中
                canvasBmp.recycle();
            } else {
                ToastUtil.show(R.string.please_sign_first);
            }
        });
        pwd_draw_cancel.setOnClickListener(v -> popupWindow.dismiss());
    }

    /**
     * 是否需要签名
     *
     * @param haspic =true 需要先输入密码，再绘制签名
     */
    private void enterPassword(boolean haspic) {
        View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_enter_password, null);
        PopupWindow popupWindow = PopUtil.create(inflate, Values.half_width, ViewGroup.LayoutParams.WRAP_CONTENT, enter_btn_main);
        EditText enter_pwd_edt = inflate.findViewById(R.id.enter_pwd_edt);
        Button enter_pwd_determine = inflate.findViewById(R.id.enter_pwd_determine);
        Button enter_pwd_cancel = inflate.findViewById(R.id.enter_pwd_cancel);
        enter_pwd_determine.setOnClickListener(v -> {
            String pwd = enter_pwd_edt.getText().toString().trim();
            if (!pwd.isEmpty()) {
                if (haspic) {
                    showDrawBoard(pwd);
                } else {
                    presenter.sendSign(0, Values.localSigninType, pwd, s2b(""));
                }
                popupWindow.dismiss();
            } else {
                ToastUtil.show(R.string.password_can_not_blank);
            }
        });
        enter_pwd_cancel.setOnClickListener(v -> popupWindow.dismiss());
    }

    private void jump2bind() {
        LogUtils.i(TAG, "jump2bind ");
        presenter.queryAttendPeople();
    }

    @Override
    public void showBindMemberView(List<InterfaceMember.pbui_Item_MemberDetailInfo> chooseMemberDetailInfos) {
        LogUtils.d(TAG, "showBindMemberView -->" + "展示选择绑定参会人弹框");
        if (bindMemberPop != null && bindMemberPop.isShowing()) {
            adapter.notifyDataSetChanged();
            adapter.notifyChoose();
            return;
        }
        View inflate = LayoutInflater.from(this).inflate(R.layout.pop_bind_member, null);
        bindMemberPop = PopUtil.create(inflate, enter_btn_main);
        adapter = new MainBindMemberAdapter(R.layout.item_bind_member, chooseMemberDetailInfos);
        RecyclerView pop_bind_member_rv = inflate.findViewById(R.id.pop_bind_member_rv);
        pop_bind_member_rv.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        pop_bind_member_rv.setAdapter(adapter);
        adapter.setOnItemClickListener((ad, view, position) -> {
            int personid = chooseMemberDetailInfos.get(position).getPersonid();
            LogUtils.d(TAG, "onItemClick -->position= " + position + ", personid= " + personid);
            adapter.setChoose(personid);
        });
        Button pop_bind_member_determine = inflate.findViewById(R.id.pop_bind_member_determine);
        Button pop_bind_member_create = inflate.findViewById(R.id.pop_bind_member_create);
        Button pop_bind_member_cancel = inflate.findViewById(R.id.pop_bind_member_cancel);
        pop_bind_member_determine.setOnClickListener(v -> {
            LogUtils.d(TAG, "onClick -->" + "确定");
            if (adapter.getChooseId() != -1) {
                bindMemberPop.dismiss();
                presenter.joinMeeting(adapter.getChooseId());
            } else {
                ToastUtil.show(R.string.err_unselected_member);
            }
        });
        pop_bind_member_create.setOnClickListener(v -> {
            bindMemberPop.dismiss();
            showCreateMemberView();
        });
        pop_bind_member_cancel.setOnClickListener(v -> bindMemberPop.dismiss());
    }

    private void showCreateMemberView() {
        View inflate = LayoutInflater.from(this).inflate(R.layout.pop_create_member, null);
        createMemberPop = PopUtil.create(inflate, enter_btn_main);
        EditText pop_create_member_company = inflate.findViewById(R.id.pop_create_member_company);
        EditText pop_create_member_name = inflate.findViewById(R.id.pop_create_member_name);
        EditText pop_create_member_position = inflate.findViewById(R.id.pop_create_member_position);
        EditText pop_create_member_phone = inflate.findViewById(R.id.pop_create_member_phone);
        EditText pop_create_member_email = inflate.findViewById(R.id.pop_create_member_email);
        EditText pop_create_member_password = inflate.findViewById(R.id.pop_create_member_password);
        Button pop_create_member_determine = inflate.findViewById(R.id.pop_create_member_determine);
        Button pop_create_member_cancel = inflate.findViewById(R.id.pop_create_member_cancel);
        pop_create_member_determine.setOnClickListener(v -> {
            String company = pop_create_member_company.getText().toString().trim();
            String name = pop_create_member_name.getText().toString().trim();
            String position = pop_create_member_position.getText().toString().trim();
            String phone = pop_create_member_phone.getText().toString().trim();
            String email = pop_create_member_email.getText().toString().trim();
            String password = pop_create_member_password.getText().toString().trim();
            if (!name.isEmpty()) {
                createMemberPop.dismiss();
                InterfaceMember.pbui_Item_MemberDetailInfo build = InterfaceMember.pbui_Item_MemberDetailInfo.newBuilder()
                        .setPassword(s2b(password))
                        .setEmail(s2b(email))
                        .setPhone(s2b(phone))
                        .setJob(s2b(position))
                        .setCompany(s2b(company))
                        .setName(s2b(name))
                        .setPersonid(0)
                        .setComment(s2b("")).build();
                presenter.addAttendPeople(build);
            } else {
                ToastUtil.show(R.string.name_is_required);
            }
        });
        pop_create_member_cancel.setOnClickListener(v -> createMemberPop.dismiss());
    }

    private void jump2scan() {
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(CaptureActivity.KEY_NEED_BEEP, CaptureActivity.VALUE_BEEP);
        bundle.putBoolean(CaptureActivity.KEY_NEED_VIBRATION, CaptureActivity.VALUE_VIBRATION);
        bundle.putBoolean(CaptureActivity.KEY_NEED_EXPOSURE, CaptureActivity.VALUE_NO_EXPOSURE);
        bundle.putByte(CaptureActivity.KEY_FLASHLIGHT_MODE, CaptureActivity.VALUE_FLASHLIGHT_OFF);
        bundle.putByte(CaptureActivity.KEY_ORIENTATION_MODE, CaptureActivity.VALUE_ORIENTATION_AUTO);
        bundle.putBoolean(CaptureActivity.KEY_SCAN_AREA_FULL_SCREEN, CaptureActivity.VALUE_SCAN_AREA_FULL_SCREEN);
        bundle.putBoolean(CaptureActivity.KEY_NEED_SCAN_HINT_TEXT, CaptureActivity.VALUE_SCAN_HINT_TEXT);
        intent.putExtra(CaptureActivity.EXTRA_SETTING_BUNDLE, bundle);
        startActivityForResult(intent, CaptureActivity.REQ_CODE);
    }

    @Override
    public void jump2meet() {
        presenter.unregister();
        startActivity(new Intent(MainActivity.this, MeetingActivity.class));
        finish();
    }

}
