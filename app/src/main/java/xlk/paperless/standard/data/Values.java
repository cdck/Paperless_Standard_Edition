package xlk.paperless.standard.data;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mogujie.tt.protobuf.InterfaceMember;

import java.util.ArrayList;
import java.util.List;

/**
 * @author by xlk
 * @date 2020/8/3 15:28
 * @desc 存放全局静态变量
 */
public class Values {
    /**
     * 是否过滤秘书身份的人员
     */
    public static boolean isFilterSecretary = false;
    /**
     * =-1 默认，=0离线，=1在线
     */
    public static int isOneline = -1;
    /**
     * 平台初始化是否结束
     */
    public static boolean initializationIsOver;
    /**
     * 画板的操作ID
     */
    public static int operid;
    /**
     * 屏幕宽高
     */
    public static int screen_width, screen_height;
    /**
     * 屏幕一半的宽高
     */
    public static int half_width, half_height;
    /**
     * 屏幕的三分之二
     */
    public static int width_2_3, height_2_3;
    /**
     * 像素
     */
    public static int camera_width = 1280, camera_height = 720;
    /**
     * 本机参会人权限
     */
    public static int localPermission;
    /**
     * 本机是否有所有权限（管理员、秘书、主持人）
     */
    public static boolean hasAllPermissions;
    /**
     * 所有参会人的权限
     */
    public static List<InterfaceMember.pbui_Item_MemberPermission> allPermissions;
    /**
     * 签到类型
     */
    public static int localSigninType;
    /**
     * 本机的参会人ID
     */
    public static int localMemberId = 0;
    /**
     * 本机当前参加的会议ID
     */
    public static int localMeetingId = 0;
    /**
     * 本机设备ID
     */
    public static int localDeviceId = 0;
    /**
     * 本机角色
     */
    public static int localRole;
    /**
     * 本机会议室ID
     */
    public static int localRoomId;
    public static String localMeetingName = "";
    public static String localMemberName = "";
    public static String localDeviceName = "";
    public static String localRoomName = "";
    public static LocalBroadcastManager lbm;
    /**
     * 是否加载完成（不等于成功加载X5，也可能加载的是系统内核）
     */
    public static boolean initX5Finished = false;
    /**
     * 存放正在下载中的媒体ID，下载退出后进行删除
     */
    public static List<Integer> downloadingFiles = new ArrayList<>();
    /**
     * 是否正在播放
     */
    public static boolean isVideoPlaying;
    /**
     * 是否正在被强制性播放中
     */
    public static boolean isMandatoryPlaying;
    /**
     * 是否有新的播放
     */
    public static boolean haveNewPlayInform;
    /**
     * 通过WPS打开文档是否从管理员后台打开
     */
    public static boolean isFromAdminOpenWps = false;
    /**
     * 是否是后台管理界面
     */
    public static boolean isAdminPage = false;
    public static float fontScale = 1.0f;
    /**
     * 本机的设备标志
     */
    public static int localDeviceFlag = -1;
    public static boolean isFirstIn=true;
}
