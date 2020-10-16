package xlk.paperless.standard.view;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsDownloader;
import com.tencent.smtt.sdk.TbsListener;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.helper.ActivityStackManager;
import xlk.paperless.standard.helper.MyRejectedExecutionHandler;
import xlk.paperless.standard.service.BackstageService;
import xlk.paperless.standard.service.FabService;
import xlk.paperless.standard.util.CrashHandler;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.fragment.agenda.MeetAgendaFragment;

import static xlk.paperless.standard.data.Values.lbm;

/**
 * @author xlk
 * @date 2020/3/9
 */
public class MyApplication extends Application {

    static {
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

    private static final String TAG = "MyApplication-->";

    /**
     * 是否写入到文件中
     */
    public static final boolean read2file = false;
    /**
     * 是否可以登录到后台管理
     */
    public static final boolean canLoginAdmin = false;

    /**
     * 屏幕录制需要的信息
     */
    public static int mResult;
    public static Intent mIntent;
    public static MediaProjectionManager mMediaProjectionManager;
    public static MediaProjection mMediaProjection;

    /**
     * 服务
     */
    private Intent backstageService;
    private boolean backstageServiceIsOpen;
    private Intent fabService;
    private boolean FabServiceIsOpen;
    private ScreenRecorder recorder;

    /**
     * 屏幕录制最大宽高和dpi 和 摄像头最大宽高
     */
    public static final int MAX_WIDTH = 1280, MAX_HEIGHT = 720, MAX_DPI = 320;
    public static int width, height, dpi, maxBitRate = 500 * 1000;

    public static Context applicationContext;
    public static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            1,
            Runtime.getRuntime().availableProcessors() + 1,
            10L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new NamingThreadFactory("paperless-standard-threadPool-"),
            new MyRejectedExecutionHandler()
    );

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        ActivityStackManager.getInstance().init(this);
        loadX5();
        initScreenParam();
        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ACTION_SCREEN_RECORDING);
        filter.addAction(Constant.ACTION_STOP_SCREEN_RECORDING);
        lbm.registerReceiver(receiver, filter);
    }

    public static QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
        @Override
        public void onCoreInitFinished() {
            //x5内核初始化完成回调接口，此接口回调并表示已经加载起来了x5，有可能特殊情况下x5内核加载失败，切换到系统内核。
            LogUtil.i(TAG, "x5内核 onCoreInitFinished-->");
        }

        @Override
        public void onViewInitFinished(boolean b) {
            Values.initX5Finished = true;
            //ToastUtil.showToast(usedX5 ? R.string.tencent_x5_load_successfully : R.string.tencent_x5_load_failed);
            //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
            LogUtil.d(TAG, "x5内核 onViewInitFinished: 加载X5内核是否成功: " + b);
            MeetAgendaFragment.isNeedRestart = !b;
            EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_X5_INSTALL).build());
        }
    };

    public static void loadX5() {
        boolean canLoadX5 = QbSdk.canLoadX5(applicationContext);
        LogUtil.i(TAG, "x5内核  是否可以加载X5内核 -->" + canLoadX5);
        if (canLoadX5) {
            initX5();
        } else {
            QbSdk.setDownloadWithoutWifi(true);
            QbSdk.setTbsListener(new TbsListener() {
                @Override
                public void onDownloadFinish(int i) {
                    LogUtil.d(TAG, "x5内核 onDownloadFinish -->下载X5内核：" + i);
                }

                @Override
                public void onInstallFinish(int i) {
                    LogUtil.d(TAG, "x5内核 onInstallFinish -->安装X5内核：" + i);
                    if (i == TbsListener.ErrorCode.INSTALL_SUCCESS_AND_RELEASE_LOCK) {
                        initX5();
                    }
                }

                @Override
                public void onDownloadProgress(int i) {
                    LogUtil.d(TAG, "x5内核 onDownloadProgress -->下载X5内核：" + i);
                }
            });
            MyApplication.threadPool.execute(() -> {
                //判断是否要自行下载内核
//                boolean needDownload = TbsDownloader.needDownload(mContext, TbsDownloader.DOWNLOAD_OVERSEA_TBS);
//                LogUtil.i(TAG, "loadX5 是否需要自行下载X5内核" + needDownload);
//                if (needDownload) {
//                    // 根据实际的网络情况下，选择是否下载或是其他操作
//                    // 例如: 只有在wifi状态下，自动下载，否则弹框提示
//                    // 启动下载
//                    TbsDownloader.startDownload(mContext);
//                }
                TbsDownloader.startDownload(applicationContext);
            });
        }
    }

    public static void initX5() {
        //目前线上sdk存在部分情况下initX5Enviroment方法没有回调，您可以不用等待该方法回调直接使用x5内核。
        QbSdk.initX5Environment(applicationContext, cb);
        //如果您需要得知内核初始化状态，可以使用QbSdk.preinit接口代替
//        QbSdk.preInit(applicationContext, cb);
    }

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
            Values.screen_width = metric.widthPixels;
            Values.screen_height = metric.heightPixels;
            LogUtil.e(TAG, "initScreenParam :  屏幕宽高 --> " + Values.screen_width + "," + Values.screen_height);
            width = metric.widthPixels;
            height = metric.heightPixels;
//            if (width > MAX_WIDTH) {
//                width = MAX_WIDTH;
//            }
//            if (height > MAX_HEIGHT) {
//                height = MAX_HEIGHT;
//            }
            //屏幕密度（0.75 / 1.0 / 1.5）
            float density = metric.density;
            //屏幕密度DPI（120 / 160 / 240）
            dpi = metric.densityDpi;
            LogUtil.e(TAG, "initScreenParam :  dpi --> " + dpi);
            if (dpi > MAX_DPI) {
                dpi = MAX_DPI;
            }
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int type = intent.getIntExtra(Constant.EXTRA_COLLECTION_TYPE, 0);
            LogUtil.e(TAG, "onReceive :   --> type= " + type + " , action = " + action);
            if (action.equals(Constant.ACTION_SCREEN_RECORDING)) {
                LogUtil.e(TAG, "screen_shot --> ");
                screenRecording();
            } else if (action.equals(Constant.ACTION_STOP_SCREEN_RECORDING)) {
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
            if (mMediaProjection == null) {
                return;
            }
            if (recorder != null) {
                recorder.quit();
            }
            if (recorder == null) {
                recorder = new ScreenRecorder(width, height, maxBitRate, dpi, mMediaProjection, Constant.ROOT_DIR + "/录屏数据.mp4");
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

}
