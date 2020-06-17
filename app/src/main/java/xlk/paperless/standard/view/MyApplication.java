package xlk.paperless.standard.view;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.mogujie.tt.protobuf.InterfaceMember;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import com.tencent.smtt.utils.TbsLog;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.receiver.NetWorkReceiver;
import xlk.paperless.standard.service.BackstageService;
import xlk.paperless.standard.service.FabService;
import xlk.paperless.standard.util.CrashHandler;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author xlk
 * @date 2020/3/9
 */
public class MyApplication extends Application {

    public static boolean isOneline;//=false离线，=true在线

    static {
        {
            System.loadLibrary("avcodec-57");
            System.loadLibrary("avdevice-57");
            System.loadLibrary("avfilter-6");
            System.loadLibrary("avformat-57");
            System.loadLibrary("avutil-55");
            System.loadLibrary("postproc-54");
            System.loadLibrary("swresample-2");
            System.loadLibrary("swscale-4");
            System.loadLibrary("SDL2");
            System.loadLibrary("main");
            System.loadLibrary("NetClient");
            System.loadLibrary("Codec");
            System.loadLibrary("ExecProc");
            System.loadLibrary("Device-OpenSles");
            System.loadLibrary("meetcoreAnd");
            System.loadLibrary("PBmeetcoreAnd");
            System.loadLibrary("meetAnd");
            System.loadLibrary("native-lib");
            System.loadLibrary("z");
        }
    }

    private final String TAG = "MyApplication-->";

    public static boolean initializationIsOver;//初始化是否结束
    public static int operid;

    public static int screen_width, screen_height;//屏幕宽高
    public static int camera_width = 1280, camera_height = 720;//像素
    public static List<Integer> localPermissions;//本机参会人的权限集合
    public static boolean hasAllPermissions;

    public static int localSigninType;//签到类型
    public static int localMemberId = -1;//本机的参会人ID
    public static int localMeetingId = -1;//本机当前参加的会议ID
    public static int localDeviceId = -1;//本机设备ID
    public static int localRole;//本机角色
    public static int localRoomId;//本机会议室ID
    public static String localMeetingName = "";
    public static String localMemberName = "";
    public static String localDeviceName = "";
    public static String localRoomName = "";
    private Intent backstageService;
    private boolean backstageServiceIsOpen;
    private Intent fabService;
    private boolean FabServiceIsOpen;
    public static LocalBroadcastManager lbm;
    private ScreenRecorder recorder;
    private int width, height, dpi, maxBitRate = 500 * 1000;
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        initX5();
        initScreenParam();
        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.action_screen_recording);
        filter.addAction(Constant.action_stop_screen_recording);
        lbm.registerReceiver(receiver, filter);
    }


    private void initX5() {
        //非wifi情况下，主动下载x5内核，将产生24M左右的流量
        QbSdk.setDownloadWithoutWifi(true);
        boolean b = QbSdk.canLoadX5(getApplicationContext());
        LogUtil.d(TAG, "initX5 -->" + "是否可以加载X5内核：" + b);
//        QbSdk.setOnlyDownload(true);
        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                LogUtil.d(TAG, "onDownloadFinish -->下载X5内核完成：" + i);
            }

            @Override
            public void onInstallFinish(int i) {
                LogUtil.d(TAG, "onInstallFinish -->安装X5内核进度：" + i);
            }

            @Override
            public void onDownloadProgress(int i) {
                LogUtil.d(TAG, "onDownloadProgress -->下载X5内核进度：" + i);
            }
        });

        //目前线上sdk存在部分情况下initX5Enviroment方法没有回调，您可以不用等待该方法回调直接使用x5内核。
//        QbSdk.initX5Environment(getApplicationContext(), cb);
        //如果您需要得知内核初始化状态，可以使用QbSdk.preinit接口代替
        QbSdk.preInit(getApplicationContext(), cb);
    }

    //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
    QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
        @Override
        public void onCoreInitFinished() {
            //x5内核初始化完成回调接口，此接口回调并表示已经加载起来了x5，有可能特殊情况下x5内核加载失败，切换到系统内核。
            LogUtil.i(TAG, " onCoreInitFinished-->");
        }

        @Override
        public void onViewInitFinished(boolean b) {
            //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
            LogUtil.d(TAG, "onViewInitFinished: 加载X5内核是否成功: " + b);
        }
    };

    public void onDestroy() {
        openBackstageService(false);
        lbm.unregisterReceiver(receiver);
    }

    private void initScreenParam() {
        LogUtil.e(TAG, "initScreenParam :   --> ");
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager window = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        if (window != null) {
            window.getDefaultDisplay().getMetrics(metric);
            screen_width = metric.widthPixels;
            screen_height = metric.heightPixels;
            LogUtil.e(TAG, "initScreenParam :  屏幕宽高 --> " + screen_width + "," + screen_height);
            width = metric.widthPixels;
            height = metric.heightPixels;
            if (width > 1280) {
                width = 1280;
            }
            if (height > 720) {
                height = 720;
            }
            //屏幕密度（0.75 / 1.0 / 1.5）
            float density = metric.density;
            //屏幕密度DPI（120 / 160 / 240）
            dpi = metric.densityDpi;
            LogUtil.e(TAG, "initScreenParam :  dpi --> " + dpi);
            if (dpi > 320) {
                dpi = 320;
            }
        }
    }

    public void setMaxBitRate(int type) {
        if (type < 100) maxBitRate = 100 * 1000;
        else if (type > 10000) maxBitRate = 10000 * 1000;
        else maxBitRate = type * 1000;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int type = intent.getIntExtra("type", 0);
            LogUtil.e(TAG, "onReceive :   --> type= " + type + " , action = " + action);
            if (action.equals(Constant.action_screen_recording)) {
                LogUtil.e(TAG, "screen_shot --> ");
                screenRecording();
            } else if (action.equals(Constant.action_stop_screen_recording)) {
                LogUtil.e(TAG, "stop_screen_shot --> ");
                if (stopRecord()) {
                    LogUtil.i(TAG, "stopStreamInform: 屏幕录制已停止..");
                } else {
                    LogUtil.e(TAG, "stopStreamInform :  屏幕录制停止失败 --> ");
                }
            }
        }
    };

    private boolean stopRecord() {
        if (recorder != null) {
            recorder.quit();
            recorder = null;
            return true;
        } else {
            return false;
        }
    }

    private void screenRecording() {
        if (stopRecord()) {
            LogUtil.i(TAG, "capture: 屏幕录制已停止");
        } else {
            if (mMediaProjection == null) return;
            if (recorder != null) {
                recorder.quit();
            }
            if (recorder == null) {
                recorder = new ScreenRecorder(width, height, maxBitRate, dpi, mMediaProjection, "");
            }
            recorder.start();//启动录屏线程
            LogUtil.i(TAG, "capture: 开启屏幕录制");
        }
    }

    public void openBackstageService(boolean open) {
        if (open && !backstageServiceIsOpen) {
            if (backstageService == null) {
                backstageService = new Intent(this, BackstageService.class);
            }
            startService(backstageService);
            backstageServiceIsOpen = true;
            LogUtil.d(TAG, "openBackstageService -->" + "打开后台服务");
        } else if (!open && backstageServiceIsOpen) {
            if (backstageService != null) {
                stopService(backstageService);
                backstageServiceIsOpen = false;
                LogUtil.d(TAG, "openBackstageService -->" + "关闭后台服务");
            } else {
                LogUtil.d(TAG, "openBackstageService -->" + "backstageService为空，不需要关闭");
            }
        }
    }

    public void openFabService(boolean open) {
        if (open && !FabServiceIsOpen) {
            if (fabService == null) {
                fabService = new Intent(this, FabService.class);
            }
            startService(fabService);
            FabServiceIsOpen = true;
            LogUtil.d(TAG, "openFabService -->" + "打开后台服务");
        } else if (!open && FabServiceIsOpen) {
            if (fabService != null) {
                stopService(fabService);
                FabServiceIsOpen = false;
                LogUtil.d(TAG, "openFabService -->" + "关闭后台服务");
            } else {
                LogUtil.d(TAG, "openFabService -->" + "fabService为空，不需要关闭");
            }
        }
    }

    //屏幕录制需要
    public static int mResult;
    public static Intent mIntent;
    public static MediaProjectionManager mMediaProjectionManager;
    public static MediaProjection mMediaProjection;

}
