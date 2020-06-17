package xlk.paperless.standard.data;

import android.content.Intent;

import com.mogujie.tt.protobuf.InterfaceMacro;

import org.greenrobot.eventbus.EventBus;

import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.MyApplication;

import static xlk.paperless.standard.view.MyApplication.lbm;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc
 */
public class Call {

    private static Call mag;

    private Call() {

    }

    public static Call getInstance() {
        if (mag == null) {
            synchronized (Call.class) {
                if (mag == null) {
                    mag = new Call();
                }
            }
        }
        return mag;
    }

    //初始化无纸化接口
    //data 参考无纸化接口对照表
    //成功返回0 失败返回-1
    public native int Init_walletSys(byte[] data);

    //无纸化功能接口中调用
    //type 功能类型
    //method 功能类型的方法
    //data  方法需要的参数 参考无纸化接口对照表
    //成功返回对应的数组 失败返回null数组
    public native byte[] call_method(int type, int method, byte[] data);

    public native void enablebackgroud(int type);

    /**
     * 初始化桌面、摄像头采集
     *
     * @param type         流类型
     * @param channelindex 流通道索引值
     * @return 成功返回0 失败返回-1
     */
    public native int InitAndCapture(int type, int channelindex);

    //初始化桌面、摄像头采集
    //type 流类型
    //data 采集数据
    //成功返回0 失败返回-1
    public native int call(int type, int iskeyframe, long pts, byte[] data);

    public native byte[] NV21ToI420(byte[] data, int w, int h);

    public native byte[] NV21ToNV12(byte[] data, int w, int h);

    public native byte[] YV12ToNV12(byte[] data, int w, int h);

    //JNI获取桌面、摄像头的参数
    //type 流类型
    //oper 参数标识 用于区分获取的数据类型
    //成功返回操作属性对应的值 失败返回-1
    public static int COLOR_FORMAT = 0;

    public int callback(int type, int oper) {
        LogUtil.i("Call", "callback -->" + "oper= " + oper + ", type= " + type);
        switch (oper) {
            case 1://pixel format
            {
                if (type == 2) return 1;
                return COLOR_FORMAT;
            }
            case 2://width
            {
//                return 1920;1836
                switch (type) {
                    case 2:
                        return MyApplication.screen_width;
                    case 3:
                        return MyApplication.camera_width;
                }

            }
            case 3://height
            {
//                return 1080;1200
                switch (type) {
                    case 2:
                        return MyApplication.screen_height;
                    case 3:
                        return MyApplication.camera_height;
                }

            }
            case 4://start capture
            {
                LogUtil.i("Call", "callback -->" + "通知采集流 type= " + type);
                if (type == 2) {
                    Intent intent = new Intent();
                    intent.setAction(Constant.action_screen_recording);
                    intent.putExtra("type", type);
                    lbm.sendBroadcast(intent);
                } else if (type == 3) {
                    EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_COLLECT_CAMERA_START).objs(type).build());
                }
                return 0;
            }
            case 5://releaseMediaCodec capture
            {
                LogUtil.i("Call", "callback -->" + "通知停止采集流 type= " + type);
                if (type == 2) {
                    Intent intent = new Intent();
                    intent.setAction(Constant.action_stop_screen_recording);
                    intent.putExtra("type", type);
                    lbm.sendBroadcast(intent);
                } else if (type == 3) {
                    EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_COLLECT_CAMERA_STOP).build());
                }
                return 0;
            }
        }
        return 0;
    }

    /**
     * 解码后的视频数据,这里对应的是YUV2420每个平面的大小,如{1920, 960, 960}
     *
     * @return
     */
    public int callback_yuvdisplay(int res, int w, int h, byte[] y, byte[] u, byte[] v) {
        LogUtil.v("xlk_log", "callback_yuvdisplay -->" + "后台YUV数据");
        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_YUV_DISPLAY).objs(res, w, h, y, u, v).build());
        return 0;
    }

    /**
     * @param res     播放资源ID
     * @param codecid 解码器类型 (h264=27,h265=173,mpeg4=12,vp8=139,vp9=167)
     * @param w       视频的宽
     * @param h       视频的高
     * @param packet
     */
    public int callback_videodecode(int isKeyframe, int res, int codecid, int w, int h, byte[] packet, long pts, byte[] codecdata) {
        if (packet != null) {
            LogUtil.v("xlk_log", "callback_videodecode -->" + "后台videodecode数据");
            EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_VIDEO_DECODE)
                    .objs(isKeyframe, res, codecid, w, h, packet, pts, codecdata).build());
        }
        return 0;
    }

    public int callback_startdisplay(int res) {
        return 0;
    }

    public int callback_stopdisplay(int res) {
        return 0;
    }

    public void error_ret(int type, int method, int ret) {

    }

    //无纸化功能接口回调接口
    //type 功能类型
    //method 功能类型的方法
    //data  方法需要的参数 参考无纸化接口对照表
    //datalen data有数据时 datalen就有长度
    //返回0即可
    public int callback_method(int type, int method, byte[] data, int datalen) {
        if (type == InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE &&
                method == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_QUERYPROPERTY_VALUE) {
            LogUtil.e("后台回调", "queryMemberProperty 查询属性---- ");
        }
        EventBus.getDefault().post(new EventMessage.Builder().type(type).method(method).objs(data, datalen).build());
        return 0;
    }
}
