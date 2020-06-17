package xlk.paperless.standard.view.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import xlk.paperless.standard.util.AppUtil;

/**
 * @author xlk
 * @date 2020/4/16
 * @desc
 */
public class CameraDemo extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceTexture texture;

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {

        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };

    public CameraDemo(Context context) {
        this(context, null);
    }

    public CameraDemo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraDemo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initial(int type) {
        texture = new SurfaceTexture(10);//实现后台录制
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
