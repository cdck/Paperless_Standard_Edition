package xlk.paperless.standard.view.camera;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.logging.Handler;

import xlk.paperless.standard.R;
import xlk.paperless.standard.util.LogUtil;

public class Camera2Activity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView camera_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        initView();
        camera_view.getHolder().addCallback(this);
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String cameraid : cameraIdList) {
                //获取相机的相关参数
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraid);
                //获取摄像头方向
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                //是否支持闪光灯
                Boolean isSupport = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                //获取摄像头拍照方向
                int orientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                //获取最大的数字调焦值，也就是zoom最大值
                Float maxZoom = cameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
                //获取最小的调焦距离，某些手机上获取到的该values为null或者0.0。前摄像头大部分有固定焦距，无法调节
                Float minZoom = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
                //获取摄像头支持某些特性的程度
                Integer hardwareLevel = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        camera_view = (SurfaceView) findViewById(R.id.camera_view);
    }

    private void initCamera2() {
        HandlerThread handlerThread = new HandlerThread("camera2");
        handlerThread.start();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera2();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
