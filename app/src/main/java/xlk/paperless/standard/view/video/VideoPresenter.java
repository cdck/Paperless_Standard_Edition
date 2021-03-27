package xlk.paperless.standard.view.video;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Range;
import android.view.Surface;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfacePlaymedia;
import com.mogujie.tt.protobuf.InterfaceStop;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.DevMember;
import xlk.paperless.standard.data.bean.MediaBean;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.view.MyApplication;

import static xlk.paperless.standard.data.Constant.BUS_VIDEO_DECODE;
import static xlk.paperless.standard.data.Constant.BUS_YUV_DISPLAY;
import static xlk.paperless.standard.data.Constant.getMimeType;
import static xlk.paperless.standard.data.Constant.RESOURCE_0;
import static xlk.paperless.standard.view.MyApplication.read2file;

/**
 * @author xlk
 * @date 2020/3/30
 * @desc
 */
public class VideoPresenter extends BasePresenter {
    private final String TAG = "VideoPresenter-->";
    private final Context cxt;
    private final IVideo view;
    private Surface mSurface;
    private int mStatus;
    private int mMediaId;
    private int currentPre;
    private int length;
    LinkedBlockingQueue<MediaBean> queue = new LinkedBlockingQueue<>();
    private MediaCodec mediaCodec;
    private int initW, initH;
    private MediaCodec.BufferInfo info;
    private MediaFormat mediaFormat;
    private String saveMimeType = "";
    private boolean isStop;
    private long lastPushTime;//最后有数据的时间
    long framepersecond = 80;//估计每秒的播放时间 单位：毫秒
    private releaseThread timeThread;
    private boolean isShareing;//是否正在同屏中
    private List<Integer> currentShareIds = new ArrayList<>();

    public List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceDetailInfos = new ArrayList<>();
    public List<InterfaceMember.pbui_Item_MemberDetailInfo> memberDetailInfos = new ArrayList<>();
    public List<InterfaceDevice.pbui_Item_DeviceDetailInfo> onLineProjectors = new ArrayList<>();
    public List<DevMember> onLineMember = new ArrayList<>();
    private int mValue;

    public VideoPresenter(Context cxt, IVideo view) {
        super();
        this.cxt = cxt;
        this.view = view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void queryMember() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo attendPeople = jni.queryAttendPeople();
            if (attendPeople == null) {
                return;
            }
            memberDetailInfos = attendPeople.getItemList();
            queryDevice();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryDevice() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = jni.queryDeviceInfo();
            if (deviceDetailInfo == null) {
                return;
            }
            deviceDetailInfos.clear();
            deviceDetailInfos.addAll(deviceDetailInfo.getPdevList());
            onLineProjectors.clear();
            onLineMember.clear();
            for (int i = 0; i < deviceDetailInfos.size(); i++) {
                InterfaceDevice.pbui_Item_DeviceDetailInfo detailInfo = deviceDetailInfos.get(i);
                int devcieid = detailInfo.getDevcieid();
                int memberid = detailInfo.getMemberid();
                int netstate = detailInfo.getNetstate();
                int facestate = detailInfo.getFacestate();
                if (devcieid == Values.localDeviceId) {
                    continue;
                }
                if (netstate == 1) {//在线
                    if (Constant.isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetProjective_VALUE, devcieid)) {//在线的投影机
                        onLineProjectors.add(detailInfo);
                    } else {//查找在线参会人
                        if (facestate == 1) {//确保在会议界面
                            for (int j = 0; j < memberDetailInfos.size(); j++) {
                                InterfaceMember.pbui_Item_MemberDetailInfo info = memberDetailInfos.get(j);
                                if (info.getPersonid() == memberid) {
                                    onLineMember.add(new DevMember(detailInfo, info));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            view.notifyOnLineAdapter();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case Constant.BUS_MANDATORY:
                view.setCanNotExit();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEDIAPLAYPOSINFO_VALUE://播放进度通知
                playInfo(msg);
                break;
            case BUS_YUV_DISPLAY:
                Object[] objs = msg.getObjects();
                int res = (int) objs[0];
                int w = (int) objs[1];
                int h = (int) objs[2];
                byte[] y = (byte[]) objs[3];
                byte[] u = (byte[]) objs[4];
                byte[] v = (byte[]) objs[5];
                view.updateYuv(w, h, y, u, v);
                break;
            case BUS_VIDEO_DECODE:
                videoInfos(msg);
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_STOPPLAY_VALUE:
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    byte[] o = (byte[]) msg.getObjects()[0];
                    InterfaceStop.pbui_Type_MeetStopPlay stopPlay = InterfaceStop.pbui_Type_MeetStopPlay.parseFrom(o);
                    if (stopPlay.getRes() == 0) {
                        LogUtil.d(TAG, "BusEvent -->" + "停止播放通知");
                        view.close();
                    }
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO_VALUE://设备寄存器变更通知
                LogUtil.d(TAG, "BusEvent -->" + "设备寄存器变更通知");
                queryDevice();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE://参会人员变更通知
                LogUtil.d(TAG, "BusEvent -->" + "参会人员变更通知");
                queryMember();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEMEETSTATUS_VALUE://界面状态变更通知
                LogUtil.d(TAG, "BusEvent -->" + "界面状态变更通知");
                queryMember();
                break;
        }
    }

    private void videoInfos(EventMessage msg) {
        Object[] objs = msg.getObjects();
        int iskeyframe = (int) objs[0];
        int res = (int) objs[1];
        int codecid = (int) objs[2];
        int width = (int) objs[3];
        int height = (int) objs[4];
        byte[] packet = (byte[]) objs[5];
        long pts = (long) objs[6];
        byte[] codecdata = (byte[]) objs[7];
        String mimeType = getMimeType(codecid);
        if (packet != null) {
            lastPushTime = System.currentTimeMillis();
            length = packet.length;
            LogUtil.d(TAG, "getEventMessage :  mimeType --> " + mimeType + "，宽高：" + width + "," + height + ", pts=" + pts);
            if (!saveMimeType.equals(mimeType) || initW != width || initH != height || mediaCodec == null) {
                if (mediaCodec != null) {
                    //调用stop方法使其进入 uninitialzed 状态，这样才可以重新配置MediaCodec
                    mediaCodec.stop();
                }
                saveMimeType = mimeType;
                initCodec(width, height, codecdata);
            }
            read2file(packet, codecdata);
        }
        mediaCodecDecode(packet, length, pts, iskeyframe);
        if (timeThread == null && !isStop) {
            timeThread = new releaseThread();
            timeThread.start();
        }
    }

    /**
     * 初始化解码器
     *
     * @param w         宽
     * @param h         高
     * @param codecdata pps/sps 编码配置数据
     */
    private void initCodec(int w, int h, byte[] codecdata) {
        try {
            view.setCodecType(1);
            //1.创建了一个编解码器，此时编解码器处于未初始化状态（Uninitialized）
            mediaCodec = MediaCodec.createDecoderByType(saveMimeType);
            /**  宽高要判断是否是解码器所支持的范围  */
            MediaCodecInfo.CodecCapabilities capabilitiesForType = mediaCodec.getCodecInfo().getCapabilitiesForType(saveMimeType);
            MediaCodecInfo.VideoCapabilities videoCapabilities = capabilitiesForType.getVideoCapabilities();
            Range<Integer> supportedWidths = videoCapabilities.getSupportedWidths();
            Integer upper = supportedWidths.getUpper();
            Integer lower = supportedWidths.getLower();
            Range<Integer> supportedHeights = videoCapabilities.getSupportedHeights();
            Integer upper1 = supportedHeights.getUpper();
            Integer lower1 = supportedHeights.getLower();
            initW = w;
            initH = h;
            w = supportedWidths.clamp(w);
            h = supportedHeights.clamp(h);
            LogUtil.e(TAG, "initCodec :   --> " + upper + ", " + lower + " ,,高：" + upper1 + ", " + lower1);
            initMediaFormat(w, h, codecdata);
            boolean formatSupported = capabilitiesForType.isFormatSupported(mediaFormat);
            LogUtil.i(TAG, "initCodec :  是否支持 --> " + formatSupported);
            info = new MediaCodec.BufferInfo();
            try {
                //2.对编解码器进行配置，这将使编解码器转为配置状态（Configured）
                mediaCodec.configure(mediaFormat, mSurface, null, 0);
            } catch (IllegalArgumentException e) {
                String message = e.getMessage();
                String string = e.toString();
                String localizedMessage = e.getLocalizedMessage();
                LogUtil.e(TAG, "initCodec  configure方法异常捕获 --> \n" + message + "\n toString: " + string + "\n localizedMessage: " + localizedMessage);
                e.printStackTrace();
            } catch (MediaCodec.CodecException e) {
                //可能是由于media内容错误、硬件错误、资源枯竭等原因所致
                //可恢复错误（recoverable errors）：如果isRecoverable() 方法返回true,然后就可以调用stop(),configure(...),以及start()方法进行修复
                //短暂错误（transient errors）：如果isTransient()方法返回true,资源短时间内不可用，这个方法可能会在一段时间之后重试。
                //isRecoverable()和isTransient()方法不可能同时都返回true。
                LogUtil.e(TAG, "initCodec :   -->可恢复错误： " + e.isRecoverable() + ",短暂错误：" + e.isTransient());
            }
            //3.调用start()方法使其转入执行状态（Executing）
            mediaCodec.start();
            initMediaMuxer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedOutputStream outputStream;

    private void initMediaMuxer() throws IOException {
        if (!read2file) return;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.mp4");
        if (file.exists()) {
            file.delete();
        }
//        muxer = new MediaMuxer(savepath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        outputStream = new BufferedOutputStream(new FileOutputStream(file));
    }

    public void read2file(byte[] outData, byte[] codecdata) {
        if (!read2file) return;
        try {
            outputStream.write(outData, 0, outData.length);
            outputStream.write(codecdata, 0, codecdata.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMediaFormat(int w, int h, byte[] codecdata) {
        LogUtil.e(TAG, "initMediaFormat :   --> " + (mediaFormat == null));
        mediaFormat = MediaFormat.createVideoFormat(saveMimeType, w, h);
        mediaFormat.setInteger(MediaFormat.KEY_WIDTH, w);
        mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, h);
        //设置最大输出大小
        mediaFormat.setLong(MediaFormat.KEY_MAX_INPUT_SIZE, w * h);
        if (codecdata != null) {
            mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(codecdata));
            mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(codecdata));
        }
        mediaCodec.getCodecInfo().getCapabilitiesForType(saveMimeType).isFormatSupported(mediaFormat);
    }

    private void mediaCodecDecode(byte[] bytes, int size, long pts, int iskeyframe) {
        if (isStop) return;
        if (bytes != null && bytes.length > 0) {
            //把网络接收到的视频数据先加入到队列中
            queue.offer(new MediaBean(bytes, size, pts, iskeyframe));
        } else {
            //bytes为null也不能立马返回，需要处理从视频队列中送数据到解码buffer 和 解码好的视频的显示
        }
        int queuesize = queue.size();
        LogUtil.i(TAG, " mediaCodecDecode -->queuesize: " + queuesize);
        if (queuesize > 500) {
            //当解码速度太慢，导致视频数据积累太多，这种情况下要处理丢包，丢包的策略把前面的关键帧组全部丢包，保留后面两个关键帧组
            //丢帧必须按照I帧P帧连续的丢，否则会造成花屏的情况
            int keyframenum = 0;
            MediaBean poll;
            //先统计队列中有多少个关键帧
            for (int ni = 0; ni < queuesize; ++ni) {
                poll = queue.peek();
                if (poll.getIskeyframe() == 1)
                    keyframenum++;
            }
            for (int ni = 0; ni < queuesize; ++ni) {
                poll = queue.peek();
                if (poll.getIskeyframe() == 1) {
                    keyframenum--;
                    if (keyframenum < 2) {
                        //将该帧放回队列头，因为丢包已经丢了前面的关键帧组，保留后面的两个组
                        break;
                    }
                }
                LogUtil.e(TAG, "mediaCodecDecode 其它帧在此丢掉 -->");
                //其它帧在此丢掉,不处理
                queue.poll();
            }
            //重新计算队列大小
            queuesize = queue.size();
        }
        //判断解码器是否初始化完成
        if (mediaCodec == null)
            return;
        //队列中有视频帧，检查解码队列中是否有空闲可用的buffer，有则取视频帧送进去解码
        if (queuesize > 0) {
            int inputBufferIndex = -1;
            try {
                inputBufferIndex = mediaCodec.dequeueInputBuffer(0);
                if (inputBufferIndex >= 0) {
                    //有空闲可用的解码buffer
                    ByteBuffer byteBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                    byteBuffer.clear();
                    //将视频队列中的头取出送到解码队列中
                    MediaBean poll = queue.poll();
                    byteBuffer.put(poll.getBytes());
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, poll.getSize(), poll.getPts(), 0);
                }
            } catch (IllegalStateException e) {
                //如果解码出错，需要提示用户或者程序自动重新初始化解码
                mediaCodec = null;
                return;
            }
        }
        //判断解码显示buffer是否初始化完成
        if (info == null)
            return;
        //判断下一帧的播放时间是否已经到了
//        if (System.currentTimeMillis() - lastplaytime < framepersecond) {
//            return;
//        }
        int index = mediaCodec.dequeueOutputBuffer(info, 0);
        if (index >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(index);
            outputBuffer.position(info.offset);
            outputBuffer.limit(info.offset + info.size);
            LogUtil.i(TAG, "mediaCodecDecode --> dequeueOutputBuffer：查看info：" + index
                    + "\nflags：" + info.flags + ", offset：" + info.offset + ", size：" + info.size
                    + ", presentationTimeUs：" + info.presentationTimeUs);
//            mediaCodec.releaseOutputBuffer(index, info.presentationTimeUs);
            //如果配置编码器时指定了有效的surface，传true将此输出缓冲区显示在surface
            mediaCodec.releaseOutputBuffer(index, true);
        }
    }

    private void playInfo(EventMessage msg) throws InvalidProtocolBufferException {
        byte[] datas = (byte[]) msg.getObjects()[0];
        InterfacePlaymedia.pbui_Type_PlayPosCb playPos = InterfacePlaymedia.pbui_Type_PlayPosCb.parseFrom(datas);
        int mediaId = playPos.getMediaId();
        //当status>0时，为文件ID号
        int status = playPos.getStatus();
        int per = playPos.getPer();
        int sec = playPos.getSec();
        this.mMediaId = mediaId;
        this.mStatus = status;
        currentPre = per;
        //只有在播放中才更新进度相关UI
        if (status == 0) {
            byte[] timedata = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_TIME.getNumber(),
                    this.mMediaId);
            InterfaceBase.pbui_CommonInt32uProperty commonInt32uProperty = InterfaceBase.pbui_CommonInt32uProperty.parseFrom(timedata);
            int propertyval = commonInt32uProperty.getPropertyval();
            view.updateProgressUi(per, DateUtil.convertTime((long) sec * 1000), DateUtil.convertTime((long) propertyval));

            byte[] fileName = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(),
                    this.mMediaId);
            InterfaceBase.pbui_CommonTextProperty pbui_commonTextProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(fileName);
            view.updateTopTitle(pbui_commonTextProperty.getPropertyval().toStringUtf8());
        }
        if (status == 0 || status == 1) view.updateAnimator(status);
    }

    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    void releaseMediaRes() {
        LogUtil.e(TAG, "releaseMediaRes :   --> ");
        isStop = true;
        List<Integer> a = new ArrayList<>();
        List<Integer> b = new ArrayList<>();
        a.add(RESOURCE_0);
        b.add(Values.localDeviceId);
        /** ************ ******  停止资源操作  ****** ************ **/
        jni.stopResourceOperate(a, b);
    }

    public void releasePlay() {
        if (timeThread != null) {
            timeThread.interrupt();
            timeThread = null;
        }
        releaseMediaCodec();
    }

    /**
     * 释放资源
     */
    private void releaseMediaCodec() {
        MyApplication.threadPool.execute(() -> {
            if (mediaCodec != null) {
                try {
                    LogUtil.e(TAG, "releaseMediaCodec :   --> ");
                    mediaCodec.reset();
                    //调用stop()方法使编解码器返回到未初始化状态（Uninitialized），此时这个编解码器可以再次重新配置
                    mediaCodec.stop();
                    //调用flush()方法使编解码器重新返回到刷新子状态（Flushed）
                    mediaCodec.flush();
                    //使用完编解码器后，你必须调用release()方法释放其资源
                    mediaCodec.release();
                } catch (MediaCodec.CodecException e) {
                    LogUtil.e(TAG, "run :  CodecException --> " + e.getMessage());
                } catch (IllegalStateException e) {
                    LogUtil.e(TAG, "run :  IllegalStateException --> " + e.getMessage());
                } catch (Exception e) {
                    LogUtil.e(TAG, "run :  Exception --> " + e.getMessage());
                }
            }
            mediaCodec = null;
            mediaFormat = null;
        });
    }

    /**
     * 跟去播放状态判断是否播放中
     *
     * @return true 播放中
     */
    private boolean isPlaying() {
        switch (mStatus) {
            //播放中
            case 0:
                return true;
            //暂停
            case 1:
                return false;
            //停止
            case 2:
                return false;
            //恢复
            case 3:
                return true;
            default:
                return true;
        }
    }

    //截图时确保只有播放中才暂停播放
    public void cutVideoImg() {
        if (isPlaying()) {
            List<Integer> devIds = new ArrayList<>();
            devIds.add(Values.localDeviceId);
            jni.setPlayStop(RESOURCE_0, devIds);
        }
    }

    public void playOrPause() {
        List<Integer> devIds = new ArrayList<>();
        devIds.add(Values.localDeviceId);
        if (isShareing) {
            devIds.addAll(currentShareIds);
        }
        if (isPlaying()) {
            jni.setPlayStop(RESOURCE_0, devIds);
        } else {
            jni.setPlayRecover(RESOURCE_0, devIds);
        }
    }

    public void setPlayPlace(int progress) {
        List<Integer> devIds = new ArrayList<>();
        devIds.add(Values.localDeviceId);
        if (isShareing) {
            devIds.addAll(currentShareIds);
        }
        jni.setPlayPlace(RESOURCE_0, progress, devIds, mValue, 0);
    }

    //停止同屏
    public void stopPlay() {
        if (isShareing) {
            List<Integer> res = new ArrayList<>();
            res.add(RESOURCE_0);
            jni.stopResourceOperate(res, currentShareIds);
            currentShareIds.clear();
            isShareing = false;
        }
    }

    //发起同屏
    public void mediaPlayOperate(List<Integer> ids, int value) {
        for (int id : ids) {
            if (!currentShareIds.contains(id)) {
                currentShareIds.add(id);
            }
        }
        isShareing = true;
        List<Integer> temps = new ArrayList<>(currentShareIds);
        temps.add(Values.localDeviceId);
        mValue = value;
        jni.mediaPlayOperate(mMediaId, temps, currentPre, RESOURCE_0, value, 0);
    }

    public String queryDevName(int deivceid) {
        InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = jni.queryDevInfoById(deivceid);
        if (deviceDetailInfo != null) {
            InterfaceDevice.pbui_Item_DeviceDetailInfo pdev = deviceDetailInfo.getPdev(0);
            return pdev.getDevname().toStringUtf8();
        }
        return "";
    }

    class releaseThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isStop) {
                //距离上次有数据的时间超过了framepersecond毫秒就进行手动发送
                if (System.currentTimeMillis() - lastPushTime >= framepersecond) {
//                    if (System.currentTimeMillis() - lastPushTime >= 10 * 1000) {
//                        LogUtil.d(TAG, "releaseThread -->" + "没有数据的持续时间超过10秒了，执行退出操作");
//                        view.close();
//                    } else {
                    LogUtil.v(TAG, "releaseThread 手动发送空数据 -->");
                    EventBus.getDefault().post(new EventMessage.Builder()
                            .type(Constant.BUS_VIDEO_DECODE)
                            .objects(0, 0, 0, 0, 0, null, 1L, null)
                            .build()
                    );
                    try {
                        sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    }
                }
            }
        }
    }
}
