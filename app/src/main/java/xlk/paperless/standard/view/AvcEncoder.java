package xlk.paperless.standard.view;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

import xlk.paperless.standard.data.Call;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.util.CodecUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.MathUtil;


/**
 * @author 视屏录制 CameraActivity
 */
class AvcEncoder {
    private final String TAG = "AvcEncoder-->";

    private final String MIME_TYPE = "video/avc";
    private final int TIMEOUT_USEC = 10 * 1000;
    private Call jni = Call.getInstance();
    private MediaCodec mediaCodec;
    private int m_width;
    private int m_height;
    private int m_framerate;// 帧率
    private final int I_FRAME_INTERVAL = 2;// 关键帧间隔  两关键帧之间的其它帧 = m_framerate * 2
    //摄像头采集格式
    private int mImageFormat;
    //编码器颜色格式
    private int mColorFormat;

    private byte[] configbyte;

    private final int channelIndex = 3;//流类型是摄像头
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    public AvcEncoder(int width, int height, int framerate, int bitrate, int imageFormat) throws IOException {
        LogUtil.d(TAG, "AvcEncoder -->width= " + width + ",height= " + height + ",framerate= " + framerate + ",bitrate= " + bitrate + ",imageFormat= " + imageFormat);
        jni.InitAndCapture(0, channelIndex);
        m_width = width;
        m_height = height;
        m_framerate = framerate;
        mImageFormat = imageFormat;
        globalBuffer = new byte[m_width * m_height * 3 / 2];

        // 创建编码器
        mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        showSupportedColorFormat(mediaCodec.getCodecInfo().getCapabilitiesForType(MIME_TYPE));
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, m_width, m_height);
        // 设置比特率
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        // 设置帧率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, m_framerate);
        mColorFormat = CodecUtil.selectColorFormat(CodecUtil.selectCodec(MIME_TYPE), MIME_TYPE);
        LogUtil.d(TAG, "encoder mColorFormat=" + mColorFormat);
//                (mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar ? "yuv420p"
//                        : mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar ? "yuv420sp" : mColorFormat));
        // 设置颜色格式
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);
        // 设置关键帧间隔时间
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);

        mediaCodec.configure(mediaFormat, null, null,
                // 四个参数，第一个是media格式，第二个是解码器播放的surfaceview，第三个是MediaCrypto，第四个是编码解码的标识
                MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
//        createfile();
    }

    private void showSupportedColorFormat(MediaCodecInfo.CodecCapabilities caps) {
        LogUtil.d(TAG, "supported color format: ");
        for (int c : caps.colorFormats) {
            LogUtil.d(TAG, c + "");
        }
    }

//    private  String path = Environment.getExternalStorageDirectory()
//            .getAbsolutePath() + "/test1.h264";
//    private BufferedOutputStream outputStream;
//    FileOutputStream outStream;

//    private void createfile() {
//        File file = new File(path);
//        if (file.exists()) {
//            file.delete();
//        }
//        try {
//            outputStream = new BufferedOutputStream(new FileOutputStream(file));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @SuppressLint("NewApi")
    private void StopEncoder() {
        try {
            LogUtil.d(TAG, "StopEncoder :   --> ");
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //	ByteBuffer[] inputBuffers;
//	ByteBuffer[] outputBuffers;
    private boolean isRuning = false;

    public void stopThread() {
        LogUtil.d(TAG, "stopThread :   --> ");
        isRuning = false;
        StopEncoder();
//        try {
//            outputStream.flush();
//            outputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void startEncoderThread() {
        LogUtil.d(TAG, "startEncoderThread -->");
        Thread encoderThread = new Thread(() -> {
            isRuning = true;
            byte[] input = null;
            long presentationTimeUs;
            long generateIndex = 0;
            byte[] yuv420 = new byte[m_width * m_height * 3 / 2];
            while (isRuning) {
                boolean notEmpty = !CameraActivity.YUVQueue.isEmpty();
                if (notEmpty) {// 如果有数据，取出原始数据并转格式
                    input = CameraActivity.YUVQueue.poll();// 输入源
                    //格式转换
                    //在此判断使用哪种格式转换
                    switch (mColorFormat) {
                        case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                            switch (mImageFormat) {
                                case ImageFormat.NV21:
                                    yuv420 = jni.NV21ToI420(input, m_width, m_height);
                                    break;
                                case ImageFormat.YV12:
                                    swapYV12ToI420(input, yuv420, m_width, m_height);//京东平板使用，京东平板只支持420p
                                    break;
                            }
                            break;
                        case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                            switch (mImageFormat) {
                                case ImageFormat.NV21:
                                    yuv420 = jni.NV21ToNV12(input, m_width, m_height);
                                    break;
                                case ImageFormat.YV12:
                                    yuv420 = jni.YV12ToNV12(input, m_width, m_height);
                                    break;
                            }
                            break;
                    }
                    input = yuv420;
                }
                if (input != null) {
                    try {
                        //-1表示一直等，0表示不等。按常理传-1就行，但实际上在很多机子上会挂掉
                        //传0，丢帧总比挂掉好
                        int inputBufferIndex = mediaCodec.dequeueInputBuffer(0);
                        if (inputBufferIndex >= 0) {
                            //计算得到的时间戳会导致延迟严重
                            //presentationTimeUs = computePresentationTime(generateIndex);
                            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                            inputBuffer.clear();
                            inputBuffer.put(input);
                            presentationTimeUs = System.nanoTime() / 1000;//纳秒转换成微秒
                            //LogUtil.v(TAG, "startEncoderThread:  queueInputBuffer时间戳= " + presentationTimeUs);
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, presentationTimeUs, 0);
                            generateIndex += 1;
                        }
                        sendDatas();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });
        encoderThread.start();
    }

    private void sendDatas() {
        int index = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
        while (index >= 0) {
            LogUtil.v(TAG, " sendDatas Get H264 Buffer Success! flag = " + bufferInfo.flags + ",pts = " + bufferInfo.presentationTimeUs);
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(index);
            byte[] outData = new byte[bufferInfo.size];
            outputBuffer.get(outData);
            //这表示带有此标记的缓存包含编解码器初始化或编解码器特定的数据而不是多媒体数据media data
            if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {// 2
                configbyte = new byte[bufferInfo.size];
                configbyte = outData;
                //这表示带有此标记的（编码的）缓存包含关键帧数据
            } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {// 1
                byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
                System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                LogUtil.v(TAG, "sendDatas :   --> 发送关键帧数据 " + keyframe.length);
                //jni.call(channelIndex, 1, bufferInfo.presentationTimeUs, keyframe);
                timePush(keyframe, true, bufferInfo.presentationTimeUs);
            } else {
                LogUtil.v(TAG, "sendDatas  :   --> 发送其它帧数据 " + outData.length);
                //jni.call(channelIndex, 0, bufferInfo.presentationTimeUs, outData);
                timePush(outData, false, bufferInfo.presentationTimeUs);
            }
            mediaCodec.releaseOutputBuffer(index, false);
            index = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
        }
    }

    private long lastTime = 0;

    private void timePush(byte[] data, boolean is_key_frame, long presentationTimeUs) {
        //最小使用的
        int hm = (int) MathUtil.divide(MathUtil.divide(1000, m_framerate, 0), 2, 0);
        int iskeyframe = is_key_frame ? 1 : 0;
        long useTime = System.currentTimeMillis() - lastTime;
        if (useTime <= hm) {
            try {
                long millis = hm - useTime;
                LogUtil.v(TAG, "timePush-> 睡眠：" + millis + ",当前时间：" + System.currentTimeMillis());
                Thread.sleep(millis);
                LogUtil.v(TAG, "timePush-> 睡眠：" + millis + ",当前时间：" + System.currentTimeMillis());
                lastTime = System.currentTimeMillis();
                jni.call(channelIndex, iskeyframe, presentationTimeUs, data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.v(TAG, "timePush-> 直接发送出去 hm= " + hm + ", useTime= " + useTime);
            lastTime = System.currentTimeMillis();
            jni.call(channelIndex, iskeyframe, presentationTimeUs, data);
        }
    }

    private void swapYV12toNV12(byte[] yv12bytes, byte[] nv12bytes, int width, int height) {
        int nLenY = width * height;
        int nLenU = nLenY / 4;

        System.arraycopy(yv12bytes, 0, nv12bytes, 0, width * height);
        for (int i = 0; i < nLenU; i++) {
            nv12bytes[nLenY + 2 * i] = yv12bytes[nLenY + i];
            nv12bytes[nLenY + 2 * i + 1] = yv12bytes[nLenY + nLenU + i];
        }
    }

    /*
        分辨率为4x4的YUV图像数据

        YUV420P格式             YUV420SP格式
        y0  y1  y2  y3          y0  y1  y2  y3
        y4  y5  y6  y7          y4  y5  y6  y7
        y8  y9  y10 y11         y8  y9  y10 y11
        y12 y13 y14 y15         y12 y13 y14 y15
        u0  u1  u2  u3          u0  v0  u1  v1
        v0  v1  v2  v3          u2  v2  u3  v3

        YUV420P，Y，U，V三个分量都是平面格式，分为I420和YV12。在I420格式（即：YUV）；但YV12则是相反（即：YVU）。
        YUV420SP, Y分量平面格式，UV打包格式, 即NV12。 NV12与NV21类似，U 和 V 交错排列,不同在于UV顺序。
        I420: YYYYYYYY UU VV    =>YUV420P
        YV12: YYYYYYYY VV UU    =>YUV420P
        NV12: YYYYYYYY UVUV     =>YUV420SP
        NV21: YYYYYYYY VUVU     =>YUV420SP
    */
    private byte[] globalBuffer;

    public byte[] nv21ToI420(byte[] data, int width, int height) {
        byte[] ret = globalBuffer;
        int total = width * height;

        ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);
        ByteBuffer bufferU = ByteBuffer.wrap(ret, total, total / 4);
        ByteBuffer bufferV = ByteBuffer.wrap(ret, total + total / 4, total / 4);

        bufferY.put(data, 0, total);
        for (int i = total; i < data.length; i += 2) {
            bufferV.put(data[i]);
            bufferU.put(data[i + 1]);
        }

        return ret;
    }

    private void NV21ToI420(byte[] nv21, byte[] i420, int width, int height) {
        if (nv21 == null || i420 == null) return;
        int framesize = width * height;//y的长度
        int half = framesize / 2;
        int uLen = framesize / 4;//u的长度,v相同
        int vPos = framesize + uLen;//v的起始位置
        System.arraycopy(nv21, 0, i420, 0, framesize);//将y复制到i420中

        for (int i = 0; i < uLen; i++) {
            i420[vPos + i] = nv21[framesize + i * 2];
            i420[framesize + i] = nv21[framesize + i * 2 + 1];
        }
    }

    private void swapYV12ToI420(byte[] yv12bytes, byte[] i420bytes, int width, int height) {
        System.arraycopy(yv12bytes, 0, i420bytes, 0, width * height);
        System.arraycopy(yv12bytes, width * height + width * height / 4, i420bytes, width * height, width * height / 4);
        System.arraycopy(yv12bytes, width * height, i420bytes, width * height + width * height / 4, width * height / 4);
    }

    private void YV12ToI420(byte[] yv12, byte[] i420, int width, int height) {
        if (yv12 == null || i420 == null) return;
        int framesize = width * height;//第一个u的位置
        int uLen = framesize / 4;//u的长度,v相同
        int vPos = framesize + uLen;//v的起始位置
        System.arraycopy(yv12, 0, i420, 0, framesize);//将y复制到i420中

        for (int i = 0; i < uLen; i++) {
            i420[framesize + i] = yv12[vPos + i];
            i420[vPos + i] = yv12[framesize + i];
        }
    }

    //    YV12: YYYYYYYY VV UU    =>YUV420P
    //    NV12: YYYYYYYY UVUV     =>YUV420SP
    private void YV12ToNV12(byte[] yv12, byte[] nv12, int width, int height) {
        if (yv12 == null || nv12 == null) return;
        int framesize = width * height;//第一个u的位置
        int vLen = framesize / 4;//u的长度,v相同
        int uPos = framesize + vLen;//v的起始位置
        System.arraycopy(yv12, 0, nv12, 0, framesize);//将y复制到i420中

        for (int i = 0; i < vLen; i++) {
            nv12[framesize + i * 2] = yv12[uPos + i];
            nv12[framesize + i * 2 + 1] = yv12[framesize + i];
        }
    }

    //        NV21: YYYYYYYY VUVU     =>YUV420SP
    //        NV12: YYYYYYYY UVUV     =>YUV420SP
    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int uLen = framesize / 4;//u的长度,v相同
        int vPos = framesize + uLen;//v的起始位置
        int i = 0, tmpval = 0, half = framesize / 2;
        System.arraycopy(nv21, 0, nv12, 0, framesize);

        for (i = 0; i < half; i += 2) {
            tmpval = framesize + i;//
            nv12[tmpval - 1] = nv21[tmpval];
            nv12[tmpval] = nv21[tmpval - 1];
        }
    }

    /**
     * 生成帧N的呈现时间（以微秒为单位）。
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / m_framerate;
    }
}
