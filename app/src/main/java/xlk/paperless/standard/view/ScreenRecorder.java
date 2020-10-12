package xlk.paperless.standard.view;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.text.TextUtils;
import android.view.Surface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.MathUtil;

import static xlk.paperless.standard.view.MyApplication.read2file;

/**
 * @author Gowcage
 */
public class ScreenRecorder extends Thread {
    private final String TAG = "ScreenRecorder-->";
    private static final String MIME_TYPE = "video/avc";// h.264编码
    private static final int FRAME_RATE = 18;// 帧率
    private static final int I_FRAME_INTERVAL = 2;// 关键帧间隔  两关键帧之间的其它帧 = 18*2
    private static final int TIMEOUT_US = 10 * 1000;// 超时
    private final String savePath;

    private int width;
    private int height;
    private int bitrate;
    private int dpi;
    private AtomicBoolean quit = new AtomicBoolean(false);

    private JniHandler jni = JniHandler.getInstance();

    private MediaProjection projection;
    private VirtualDisplay display;
    private Surface mSurface;
    private MediaCodec encoder;
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    private final int channelIndex = 2;

    public ScreenRecorder(int width, int height, int bitrate, int dpi, MediaProjection projection, String savePath) {
        LogUtil.v(TAG, "ScreenRecorder: width:" + width + ", height:" + height + ", bitrate: " + bitrate);
        jni.InitAndCapture(0, channelIndex);
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
        this.dpi = dpi;
        this.projection = projection;
        this.savePath = savePath;
    }

    public void quit() {
        quit.set(true);
    }

    @Override
    public void run() {
        super.run();
        try {
            try {
                // 初始化编码器
                prepareEncoder();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // 4:创建VirtualDisplay实例,DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC / DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
            display = projection.createVirtualDisplay("MainScreen", width, height, dpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, null);
            LogUtil.v(TAG, "created virtual display: " + display);
            // 录制虚拟屏幕
            recordVirtualDisplay();
        } finally {
            release();
        }
    }

    /**
     * 初始化编码器
     */
    private void prepareEncoder() throws IOException {
        LogUtil.v(TAG, "prepareEncoder---------------------------");
        // 创建MediaCodec实例 这里创建的是编码器
        encoder = MediaCodec.createEncoderByType(MIME_TYPE);
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        // 码率 越高越清晰 仅编码器需要设置
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        format.setInteger("max-bitrate", bitrate);
        // 颜色格式
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        // COLOR_FormatSurface这里表明数据将是一个graphicBuffer元数据
        // 将一个Android surface进行mediaCodec编码
        // 帧数 越高越流畅,24以下会卡顿
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        //画面静止时不会发送数据，屏幕内容有变化才会刷新
        //仅在以“表面输入”模式配置视频编码器时适用。相关值为long，并给出以微秒为单位的时间，
        //设置如果之后没有新帧可用，则先前提交给编码器的帧在 1000000 / FRAME_RATE 微秒后重复（一次）
        format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / FRAME_RATE);
        //某些设备不支持设置Profile和Level，而应该采用默认设置
//        format.setInteger(MediaFormat.KEY_PROFILE, 8);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            format.setInteger(MediaFormat.KEY_LEVEL, 65536);
//            format.setInteger(MediaFormat.KEY_STRIDE, width);
//            format.setInteger(MediaFormat.KEY_SLICE_HEIGHT, height);
//        }
        //设置CBR模式
//        format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        //format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
        // 关键帧间隔时间s
        // IFRAME_INTERVAL是指的帧间隔，它指的是，关键帧的间隔时间。通常情况下，设置成多少问题都不大。
        // 比如设置成10，那就是10秒一个关键帧。但是，如果有需求要做视频的预览，那最好设置成1
        // 因为如果设置成10，会发现，10秒内的预览都是一个截图
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
        LogUtil.v(TAG, "created video format: " + format);
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        // 这一步非常关键，它设置的，是MediaCodec的编码源，也就是说，要告诉Encoder解码哪些流。
        mSurface = encoder.createInputSurface();
        LogUtil.v(TAG, "created input surface: " + mSurface);
        encoder.start();// 开始编码
        createFile();
    }

    public byte[] configbyte;

    /**
     * 录制虚拟屏幕
     */
    private void recordVirtualDisplay() {
        LogUtil.v(TAG, "recordVirtualDisplay---------------------------");
        while (!quit.get()) {
            //从输出队列中取出编码操作之后的数据
            //输出流队列中取数据索引,返回已成功解码的输出缓冲区的索引
            int index = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
            while (index >= 0) {
                LogUtil.v(TAG, "Get H264 Buffer Success! flag = " + bufferInfo.flags + ", pts = " + bufferInfo.presentationTimeUs + "");
                ByteBuffer outputBuffer = encoder.getOutputBuffer(index);
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
                //这表示带有此标记的缓存包含编解码器初始化或编解码器特定的数据而不是多媒体数据media data
                if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                    LogUtil.v(TAG, "get config byte!");
                    configbyte = new byte[bufferInfo.size];
                    configbyte = outData;
                    //这表示带有此标记的（编码的）缓存包含关键帧数据
                } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                    byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
                    System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                    System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                    LogUtil.v(TAG, "recordVirtualDisplay :   --> 发送关键帧数据 " + keyframe.length);
                    timePush(keyframe, true, bufferInfo.presentationTimeUs);
                } else {
                    LogUtil.v(TAG, "recordVirtualDisplay :  else 路线 --> 发送其它帧数据：" + outData.length);
                    timePush(outData, false, bufferInfo.presentationTimeUs);
                }
                encoder.releaseOutputBuffer(index, false);
                index = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
            }
        }
    }

    long lastTime = 0;

    private void timePush(byte[] data, boolean is_key_frame, long presentationTimeUs) {
        //最小使用的
        int hm = (int) MathUtil.divide(MathUtil.divide(1000, FRAME_RATE, 0), 2, 0);
        int iskeyframe = is_key_frame ? 1 : 0;
        // 20 = 120 -100
        long useTime = System.currentTimeMillis() - lastTime;
        if (useTime <= hm) {
            try {
                long millis = hm - useTime;
                LogUtil.v(TAG, "timePush-> 睡眠：" + millis + ",当前时间：" + System.currentTimeMillis());
                Thread.sleep(millis);
                LogUtil.v(TAG, "timePush-> 睡眠：" + millis + ",当前时间：" + System.currentTimeMillis());
                lastTime = System.currentTimeMillis();
                read2File(data);
                jni.call(channelIndex, iskeyframe, presentationTimeUs, data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.v(TAG, "timePush-> 直接发送出去 hm= " + hm + ", useTime= " + useTime);
            lastTime = System.currentTimeMillis();
            read2File(data);
            jni.call(channelIndex, iskeyframe, presentationTimeUs, data);
        }
    }

    /**
     * 释放资源
     */
    private void release() {
        try {
            LogUtil.v(TAG, "release---------------------------");
            if (encoder != null) {
                encoder.stop();
                encoder.release();
                encoder = null;
            }
            if (display != null) {
                display.release();
            }
            if (outputStream != null) {
                outputStream.close();
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BufferedOutputStream outputStream;

    private void createFile() {
        if (read2file && !TextUtils.isEmpty(savePath)) {
            File file = new File(savePath);
            if (file.exists()) {
                file.delete();
            }
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(file));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void read2File(byte[] outData) {
        try {
            if (outputStream != null) {
                LogUtil.v(TAG, "read2File 录屏数据写入文件");
                outputStream.write(outData, 0, outData.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
