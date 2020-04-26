package xlk.paperless.standard.data;

import android.content.Context;
import android.os.Environment;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.view.MyApplication;

/**
 * @author xlk
 * @date 2020/3/9
 * @Description: 常量类
 */
public class Constant {
    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PaperlessStandardEdition/";
    public static final String INI_NAME = "client.ini";
    public static final String DEV_NAME = "client.dev";

    //目录
    private static final String file_dir = ROOT_DIR + "/File/";
    public static final String crash_log_dir = ROOT_DIR + "/Log/";
    public static final String configuration_picture_dir = ROOT_DIR + "/picture/";
    public static final String data_file_dir = file_dir + "/MeetingFile/";
    public static final String annotation_file_dir = file_dir + "/AnnotationFile/";
    public static final String agenda_file_dir = file_dir + "/AgendaFile/";
    public static final String artboard_picture_dir = file_dir + "/ArtboardPicture/";
    public static final String export_dir = file_dir + "/export/";
    public static final String cache_file_dir = ROOT_DIR + "/Cache/";


    //图片名称
    public static final String MAIN_BG_PNG_TAG = "mian_bg";
    public static final String MAIN_LOGO_PNG_TAG = "mian_logo";
    public static final String SUB_BG_PNG_TAG = "sub_bg";
    public static final String ROOM_BG_PNG_TAG = "room_bg";
    public static final String NOTICE_BG_PNG_TAG = "notice_bg";
    public static final String NOTICE_LOGO_PNG_TAG = "notice_logo";

    //下载标识
    public static final String MEETING_FILE_KEY = "MEETING_FILE_KEY";//下载会议文件时的标识
    public static final String ANNOTATION_FILE_KEY = "ANNOTATION_FILE_KEY";//下载批注文件时的标识
    public static final String SHOULD_OPEN_FILE_KEY = "SHOULD_OPEN_FILE_KEY";//下载完成应该打开的文件标识
    public static final String AGENDA_FILE_KEY = "AGENDA_FILE_KEY";//会议议程的文件

    //上传文件时的标识
    public static final int USER_VAL_UPLOAD = 1;
    public static final String UPLOAD_MDF = "upload_mdf";
    public static final String upload_draw_pic = "upload_draw_pic";
    public static final String upload_wps_file = "upload_wps_file";

    //自定义广播action
    public static String action_screen_recording = "screen_recording";//开始屏幕录制
    public static String action_stop_screen_recording = "stop_screen_recording";//停止屏幕录制

    //自定义Intent传值Extra名
    public static final String extra_camrea_type = "extra_camrea_type";//传入摄像头前置/后置
    public static final String extra_inviteflag = "extra_inviteflag";//设备对讲
    public static final String extra_operdeviceid = "extra_operdeviceid";//设备对讲

    //EventBus自定义发送的Type
    private static final int BUS_BASE = 1000000;
    public static final int BUS_MAIN_LOGO = BUS_BASE + 1;//logo图标下载完成
    public static final int BUS_MAIN_BG = BUS_BASE + 2;//主页背景图下载完成
    public static final int BUS_ROOM_BG = BUS_BASE + 3;//会议室背景图下载完成
    public static final int BUS_VIDEO_DECODE = BUS_BASE + 4;//后台播放数据
    public static final int BUS_YUV_DISPLAY = BUS_BASE + 5;//后台播放数据
    public static final int BUS_CLOSE_VIDEO = BUS_BASE + 6;//
    public static final int BUS_AGENDA_FILE = BUS_BASE + 7;//议程文件下载完成
    public static final int BUS_SHARE_PIC = BUS_BASE + 8;//添加图片通知
    public static final int BUS_SCREEN_SHOT = BUS_BASE + 9;//绘制截图
    public static final int BUS_SUB_BG = BUS_BASE + 10;//子界面北极图下载完成
    public static final int BUS_NOTICE_LOGO = BUS_BASE + 11;//后台下载公告logo完成
    public static final int BUS_NOTICE_BG = BUS_BASE + 12;//后台下载公告背景完成
    public static final int BUS_MANDATORY = BUS_BASE + 13;//后台收到强制性播放了
    public static final int BUS_COLLECT_CAMERA_START = BUS_BASE + 14;//后台收到开始采集摄像头通知
    public static final int BUS_COLLECT_CAMERA_STOP = BUS_BASE + 15;//后台收到停止采集摄像头通知
    public static final int BUS_CHAT_STATE = BUS_BASE + 16;//发送视频聊天的状态
    public static final int BUS_NET_WORK = BUS_BASE + 17;//网络状态改变
    public static final int BUS_PREVIEW_IMAGE = BUS_BASE + 18;//发送查看图片通知
    public static final int BUS_WPS_RECEIVER = BUS_BASE + 19;//通知是否注册WPS广播监听

    //自定义其它功能的功能码
    public static final int fun_code = 200000;
    public static final int fun_code_terminal = fun_code + 1;
    public static final int fun_code_vote = fun_code + 2;
    public static final int fun_code_election = fun_code + 3;
    public static final int fun_code_video = fun_code + 4;
    public static final int fun_code_screen = fun_code + 5;
    public static final int fun_code_bulletin = fun_code + 6;
    public static final int fun_code_score = fun_code + 7;

    //投票时提交，用于签到参与投票
    public static final int PB_VOTE_SELFLAG_CHECKIN = 0x80000000;


    //编码类型
    /**
     * VP8 video (i.e. video in .webm)
     */
    public static final String AMIME_VIDEO_VP8 = "video/x-vnd.on2.vp8";
    /**
     * VP9 video (i.e. video in .webm)
     */
    public static final String AMIME_VIDEO_VP9 = "video/x-vnd.on2.vp9";
    /**
     * SCREEN_HEIGHT.264/AVC video
     */
    public static final String AMIME_VIDEO_AVC = "video/avc";
    /**
     * SCREEN_HEIGHT.265/HEVC video
     */
    public static final String AMIME_VIDEO_HEVC = "video/hevc";
    /**
     * MPEG4 video
     */
    public static final String AMIME_VIDEO_MPEG4 = "video/mp4v-es";


    //文件类别

    //  大类
    /**
     * 音频
     */
    public static int MEDIA_FILE_TYPE_AUDIO = 0x00000000;
    /**
     * 视频
     */
    public static int MEDIA_FILE_TYPE_VIDEO = 0x20000000;
    /**
     * 录制
     */
    public static int MEDIA_FILE_TYPE_RECORD = 0x40000000;
    /**
     * 图片
     */
    public static int MEDIA_FILE_TYPE_PICTURE = 0x60000000;
    /**
     * 升级
     */
    public static int MEDIA_FILE_TYPE_UPDATE = 0xe0000000;
    /**
     * 临时文件
     */
    public static int MEDIA_FILE_TYPE_TEMP = 0x80000000;
    /**
     * 其它文件
     */
    public static int MEDIA_FILE_TYPE_OTHER = 0xa0000000;
    public static int MAIN_TYPE_BITMASK = 0xe0000000;

    //  小类
    /**
     * PCM文件
     */
    public static int MEDIA_FILE_TYPE_PCM = 0x01000000;
    /**
     * MP3文件
     */
    public static int MEDIA_FILE_TYPE_MP3 = 0x02000000;
    /**
     * WAV文件
     */
    public static int MEDIA_FILE_TYPE_ADPCM = 0x03000000;
    /**
     * FLAC文件
     */
    public static int MEDIA_FILE_TYPE_FLAC = 0x04000000;
    /**
     * MP4文件
     */
    public static int MEDIA_FILE_TYPE_MP4 = 0x07000000;
    /**
     * MKV文件
     */
    public static int MEDIA_FILE_TYPE_MKV = 0x08000000;
    /**
     * RMVB文件
     */
    public static int MEDIA_FILE_TYPE_RMVB = 0x09000000;
    /**
     * RM文件
     */
    public static int MEDIA_FILE_TYPE_RM = 0x0a000000;
    /**
     * AVI文件
     */
    public static int MEDIA_FILE_TYPE_AVI = 0x0b000000;
    /**
     * bmp文件
     */
    public static int MEDIA_FILE_TYPE_BMP = 0x0c000000;
    /**
     * jpeg文件
     */
    public static int MEDIA_FILE_TYPE_JPEG = 0x0d000000;
    /**
     * png文件
     */
    public static int MEDIA_FILE_TYPE_PNG = 0x0e000000;
    /**
     * 其它文件
     */
    public static int MEDIA_FILE_TYPE_OTHER_SUB = 0x10000000;

    public static int SUB_TYPE_BITMASK = 0x1f000000;


    /**
     * 判断本机是否拥有某权限
     *
     * @param code 权限对应: 1 同屏权限, 2 投影权限, 3 上传权限, 4 下载权限, 5 投票权限
     * @return
     */
    public static boolean hasPermission(int code) {
        if (MyApplication.localPermissions != null) {//本机权限集合
            return MyApplication.localPermissions.contains(code);
        } else {
            return false;
        }
    }

    /**
     * 获取选中的项数
     *
     * @param permission 10进制int型数据
     * @return
     */
    public static List<Integer> getChoose(int permission) {
        List<Integer> ls = new ArrayList<>();
        //将10进制转换成2进制字符串 010001
        String to2 = Integer.toBinaryString(permission);
        int length = to2.length();
        for (int j = 0; j < length; j++) {
            char c = to2.charAt(j);
            //将 char 转换成int型整数
            int a = c - '0';
            if (a == 1) {
                //从右往左数 <--
                //举个栗子： 000100  得到的是第3个
                int i1 = length - j;
                ls.add(i1);
            }
        }
        return ls;
    }


    /**
     * 计算媒体ID
     *
     * @param path 文件路径
     * @return 媒体ID
     */
    public static int getMediaId(String path) {
        //其它
        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
            return MEDIA_FILE_TYPE_OTHER | MEDIA_FILE_TYPE_OTHER_SUB;
        }
        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
            return MEDIA_FILE_TYPE_RECORD | MEDIA_FILE_TYPE_OTHER_SUB;
        }
        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
            return MEDIA_FILE_TYPE_UPDATE | MEDIA_FILE_TYPE_OTHER_SUB;
        }
        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
            return MEDIA_FILE_TYPE_TEMP | MEDIA_FILE_TYPE_OTHER_SUB;
        }
        //
        if (FileUtil.isDocumentFile(path) || FileUtil.isOtherFile(path)) {
            return MAIN_TYPE_BITMASK | SUB_TYPE_BITMASK;
        }
        //音频
        if (FileUtil.isVideoFile(path)) {
            return MEDIA_FILE_TYPE_AUDIO | MEDIA_FILE_TYPE_PCM;
        }
        if (FileUtil.isVideoFile(path)) {
            return MEDIA_FILE_TYPE_AUDIO | MEDIA_FILE_TYPE_MP3;
        }
        if (FileUtil.isVideoFile(path)) {
            return MEDIA_FILE_TYPE_AUDIO | MEDIA_FILE_TYPE_ADPCM;
        }
        if (FileUtil.isVideoFile(path)) {
            return MEDIA_FILE_TYPE_AUDIO | MEDIA_FILE_TYPE_FLAC;
        }
        if (FileUtil.isVideoFile(path)) {
            return MEDIA_FILE_TYPE_AUDIO | MEDIA_FILE_TYPE_MP4;
        }
        //视屏
        if (FileUtil.isVideoFile(path)) {
            return MEDIA_FILE_TYPE_VIDEO | MEDIA_FILE_TYPE_MKV;
        }
        if (FileUtil.isVideoFile(path)) {
            return MEDIA_FILE_TYPE_VIDEO | MEDIA_FILE_TYPE_RMVB;
        }
        if (FileUtil.isVideoFile(path)) {
            return MEDIA_FILE_TYPE_VIDEO | MEDIA_FILE_TYPE_AVI;
        }
        if (FileUtil.isVideoFile(path)) {
            return MEDIA_FILE_TYPE_VIDEO | MEDIA_FILE_TYPE_RM;
        }
        //图片
        if (FileUtil.isPictureFile(path)) {
            return MEDIA_FILE_TYPE_PICTURE | MEDIA_FILE_TYPE_BMP;
        }
        if (FileUtil.isPictureFile(path)) {
            return MEDIA_FILE_TYPE_PICTURE | MEDIA_FILE_TYPE_JPEG;
        }
        if (FileUtil.isPictureFile(path)) {
            return MEDIA_FILE_TYPE_PICTURE | MEDIA_FILE_TYPE_PNG;
        }
        return 0;
    }

    /**
     * 获取指定的MimeType
     *
     * @param codecId 后台回调ID
     * @return MimeType
     */
    public static String getMimeType(int codecId) {
        switch (codecId) {
            case 12:
                return AMIME_VIDEO_MPEG4;
            case 13:
                return AMIME_VIDEO_MPEG4;
            case 27:
                return AMIME_VIDEO_AVC;
            case 28:
                return AMIME_VIDEO_AVC;
            case 139:
                return AMIME_VIDEO_VP8;
            case 140:
                return AMIME_VIDEO_VP8;
            case 167:
                return AMIME_VIDEO_VP9;
            case 168:
                return AMIME_VIDEO_VP9;
            case 173:
                return AMIME_VIDEO_HEVC;
            case 174:
                return AMIME_VIDEO_HEVC;
            default:
                return AMIME_VIDEO_AVC;
        }
    }

    /**
     * 数学除法运算
     *
     * @param a     被除数
     * @param b     除数
     * @param scale 小数位
     * @return
     */
    public static double divide(double a, double b, int scale) {
        BigDecimal d = new BigDecimal(Double.toString(a));
        BigDecimal e = new BigDecimal(Double.toString(b));
        return d.divide(e, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String getFileName(int mediaId) {
        String fileName = "";
        byte[] bytes = JniHandler.getInstance().queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(), mediaId);
        try {
            InterfaceBase.pbui_CommonTextProperty pbui_commonTextProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(bytes);
            fileName = pbui_commonTextProperty.getPropertyval().toStringUtf8();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * 判断设备是否是当前类型
     *
     * @param type  类型
     * @param devId 设备ID
     * @return
     */
    public static boolean isThisDevType(int type, int devId) {
        return (devId & Constant.DEVICE_MEET_ID_MASK) == type;
    }

    public static int DEVICE_MEET_ID_MASK = 0xfffc0000;

    public static String getDeviceTypeName(Context context, int deviceId) {
        if (InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetDBServer_VALUE == (deviceId & DEVICE_MEET_ID_MASK)) {
            //会议数据库设备
            return context.getString(R.string.database_dev);
        } else if (InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetService_VALUE == (DEVICE_MEET_ID_MASK & deviceId)) {
            //会议茶水设备
            return context.getString(R.string.tea_dev);
        } else if (InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetProjective_VALUE == (DEVICE_MEET_ID_MASK & deviceId)) {
            //会议投影设备
            return context.getString(R.string.pro_dev);
        } else if (InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetCapture_VALUE == (DEVICE_MEET_ID_MASK & deviceId)) {
            //会议流采集设备
            return context.getString(R.string.capture_dev);
        } else if (InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetClient_VALUE == (DEVICE_MEET_ID_MASK & deviceId)) {
            //会议终端设备
            return context.getString(R.string.client_dev);
        } else if (InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetVideoClient_VALUE == (DEVICE_MEET_ID_MASK & deviceId)) {
            //会议视屏对讲客户端
            return context.getString(R.string.video_chat_dev);
        } else if (InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetPublish_VALUE == (DEVICE_MEET_ID_MASK & deviceId)) {
            //会议发布
            return context.getString(R.string.release_dev);
        } else if (InterfaceMacro.Pb_DeviceIDType.Pb_DEVICE_MEET_PHPCLIENT_VALUE == (DEVICE_MEET_ID_MASK & deviceId)) {
            //PHP中转数据设备
        } else if (InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetShare_VALUE == (DEVICE_MEET_ID_MASK & deviceId)) {
            //会议一键同屏设备
        }
        return "";
    }

    public static String getInterfaceStateName(Context context, int state) {
        if (state == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MainFace_VALUE) {
            return context.getString(R.string.face_main);
        } else if (state == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_MemFace_VALUE) {
            return context.getString(R.string.face_meet);
        } else if (state == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_AdminFace_VALUE) {
            return context.getString(R.string.face_back);
        } else if (state == InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_VoteFace_VALUE) {
            return context.getString(R.string.face_vote);
        }
        return "";
    }


}
