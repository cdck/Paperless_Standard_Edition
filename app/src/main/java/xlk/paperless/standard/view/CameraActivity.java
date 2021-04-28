package xlk.paperless.standard.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.blankj.utilcode.util.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.LogUtil;

import static android.media.MediaCodecList.ALL_CODECS;
import static xlk.paperless.standard.data.Constant.EXTRA_CAMERA_TYPE;
import static xlk.paperless.standard.data.Values.camera_height;
import static xlk.paperless.standard.data.Values.camera_width;

public class CameraActivity extends Activity implements SurfaceHolder.Callback,
        PreviewCallback {

    private final String TAG = "CameraLog-->";
    private SurfaceView surfaceview;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Parameters parameters;

    private int framerate = 15;
    private int imageFormat = -1;
    private static int yuvqueuesize = 10;

    public static final ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<>(
            yuvqueuesize);
    private AvcEncoder avcCodec;
    private int camera_type;
    private SurfaceTexture texture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        camera_type = getIntent().getIntExtra(EXTRA_CAMERA_TYPE, 0);//默认是0,后置摄像头
        surfaceview = findViewById(R.id.surfaceview);
        surfaceHolder = surfaceview.getHolder();
        surfaceHolder.addCallback(this);
        texture = new SurfaceTexture(10);//实现后台录制
        EventBus.getDefault().register(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = getBackCamera(camera_type);
        startcamera(camera);
        try {
//            camera_width * camera_height
            avcCodec = new AvcEncoder(camera_width, camera_height, framerate, 500 * 1000, imageFormat);
            LogUtils.d(TAG, "surfaceCreated -->");
            avcCodec.startEncoderThread();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtil.d(TAG, "CameraActivity.surfaceChanged :   --> ");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtil.d(TAG, "CameraActivity.surfaceDestroyed :   --> ");
        //如果不添加下面这句则会报错:java.lang.RuntimeException: Camera is being used after Camera.release() was called
        holder.removeCallback(this);
    }

    private void exitCamera() {
        LogUtil.d(TAG, "CameraActivity.exitCamera :   --> ");
        if (null != camera) {
            avcCodec.stopThread();
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getEventMessage(EventMessage message) {
        switch (message.getType()) {
            case Constant.BUS_COLLECT_CAMERA_STOP:
                LogUtil.i(TAG, "CameraActivity.getEventMessage :  停止摄像通知 --> ");
                exitCamera();
                finish();
                break;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        LogUtil.v(TAG, "rawDataLen=" + data.length);
        putYUVData(data, data.length);
    }

    private void putYUVData(byte[] buffer, int length) {
        if (YUVQueue.size() >= 5) {
            YUVQueue.poll();
        }
        YUVQueue.add(buffer);
    }

    private boolean SupportAvcCodec() {
        MediaCodecList d = new MediaCodecList(ALL_CODECS);
        MediaCodecInfo[] codecInfos = d.getCodecInfos();
        for (MediaCodecInfo info : codecInfos) {
            String[] types = info.getSupportedTypes();
            for (String type : types) {
                LogUtil.d(TAG, "SupportAvcCodec:" + type);
                if (type.equalsIgnoreCase("video/avc")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void startcamera(Camera mCamera) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(this);
                if (parameters == null) {
                    parameters = mCamera.getParameters();
                }
                if (camera_type == 0)
                    parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                checkSupportColorFormat();
                logParameters(parameters);
                LogUtil.d(TAG, "startcamera Camera PreviewFormat=" + (imageFormat == ImageFormat.NV21 ? "NV21" : imageFormat == ImageFormat.YV12 ? "YV12" : imageFormat));
                parameters.setPreviewFrameRate(framerate);
                parameters.setPreviewFormat(imageFormat);//NV21 YV12
                parameters.setPreviewSize(camera_width, camera_height);

                boolean supported = parameters.isVideoStabilizationSupported();
                if (supported) {
                    //这个方法可以帮助提高录制视频的稳定性。使用的时候通过 #isVideoStabilizationSupported 来判断是否可以使用。
                    // 如果妄自使用的话，会造成页面黑屏，PreviewCallback 没有任何回调
                    parameters.setVideoStabilization(true);
                }
                mCamera.setParameters(parameters);
                //mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.setPreviewTexture(texture);
                mCamera.startPreview();
                //开启预览后就移动Activity到后台
                moveTaskToBack(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void logParameters(Parameters parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("支持的色彩效果:");
        List<String> supportedColorEffects = parameters.getSupportedColorEffects();
        for (String s : supportedColorEffects) {
            sb.append(s).append("、");
        }
        sb.append("\n");
        sb.append("视频录制的首选或推荐的预览大小:");
        Camera.Size preferredPreviewSizeForVideo = parameters.getPreferredPreviewSizeForVideo();
        sb.append(preferredPreviewSizeForVideo.width).append("-").append(preferredPreviewSizeForVideo.height);
        sb.append("\n");
        sb.append("支持的预览fps（每秒帧数）范围:");
        List<int[]> supportedPreviewFpsRange = parameters.getSupportedPreviewFpsRange();
        for (int[] i : supportedPreviewFpsRange) {
            for (int j = 0; j < i.length; j++) {
                sb.append(i[j]).append("#");
            }
            sb.append("、");
        }
        sb.append("\n");
        sb.append("获取支持的预览格式:");
        List<Integer> supportedPreviewFormats = parameters.getSupportedPreviewFormats();
        for (int i : supportedPreviewFormats) {
            sb.append(i).append("、");
        }
        sb.append("\n");
        sb.append("获取MediaRecorder可以使用的支持的视频帧大小:");
        List<Camera.Size> supportedVideoSizes = parameters.getSupportedVideoSizes();
        for (Camera.Size i : supportedVideoSizes) {
            sb.append(i.width).append("-").append(i.height).append("、");
        }
        List<Integer> supportedPreviewFrameRates = parameters.getSupportedPreviewFrameRates();
        sb.append("\n");
        sb.append("获取支持的预览帧速率:");
        for (int i : supportedPreviewFrameRates) {
            sb.append(i).append("、");
        }



        LogUtils.e(TAG,"打印Parameters信息：\n" + sb.toString());
    }

    private void checkSupportColorFormat() {
        List<Integer> previewFormatsSizes = parameters.getSupportedPreviewFormats();
        if (-1 != previewFormatsSizes.indexOf(ImageFormat.YV12)) {
            imageFormat = ImageFormat.YV12;
        } else if (-1 != previewFormatsSizes.indexOf(ImageFormat.NV21)) {
            imageFormat = ImageFormat.NV21;
        } else {
            imageFormat = -1;
        }
    }

    private Camera getBackCamera(int type) {
        int cameras = Camera.getNumberOfCameras();
        LogUtil.d(TAG, "camera num:" + cameras + ", type= " + type);
        Camera c = null;
        try {
            c = Camera.open(type);
            setCameraOrientation(c);
            c.cancelAutoFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    private void setCameraOrientation(Camera camera) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(0, info);
        int rotation = this.getWindowManager().getDefaultDisplay()
                .getRotation();
        LogUtil.d(TAG, "rotation : " + rotation);
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        LogUtil.d(TAG, "degrees : " + degrees);
        int result;
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.i("BA_life", this.getClass().getSimpleName() + ".onNewIntent :   --->>> ");
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onStart :   --->>> ");
        super.onStart();
    }

    @Override
    protected void onResume() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onResume :   --->>> ");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onPause :   --->>> ");
        super.onPause();
    }

    @Override
    protected void onStop() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onStop :   --->>> ");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onRestart :   --->>> ");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onDestroy :   --->>> ");
        exitCamera();
        super.onDestroy();
    }
}
