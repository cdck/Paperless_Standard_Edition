package xlk.paperless.standard.view.main;

import android.graphics.drawable.Drawable;

import com.mogujie.tt.protobuf.InterfaceAdmin;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.io.File;
import java.util.List;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc
 */
public interface IMain {
    //初始化完成
    void initialized();

    //更新席位名称
    void updateSeatName(String seatName);

    //更新UI控件文本
    void updateUI(InterfaceDevice.pbui_Type_DeviceFaceShowDetail devMeetInfo);

    //更新时间
    void updateTime(long millisecond);

    //是否显示logo图标
    void isShowLogo(boolean isShow);

    //更新公司名称
    void updateCompany(String company);

    //更新文本控件
    void updateTv(int resId, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo itemInfo);

    //更新时间日期控件
    void updateDate(int resId, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo itemInfo);

    //更新指定控件
    void update(int resId, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo itemInfo);

    //更新按钮控件
    void updateBtn(int resId, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo itemInfo);

    //更新背景图片
    void updateBackground(Drawable drawable);

    //更新logo图标
    void updateLogo(Drawable drawable);

    //更新进入会议的滑动控件
    void updateEnterView(int resId, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo itemInfo);

    //显示绑定参会人视图
    void showBindMemberView(List<InterfaceMember.pbui_Item_MemberDetailInfo> chooseMemberDetailInfos);

    void jump2meet();

    //签到进入会议
    void readySignIn();

    //更新参会人单位
    void updateUnit(String text);

    void updateVersion(String versionName);

    void updateMeetingState(int state);

    void updateMemberRole(String role);

    void updateNote(String noteinfo);

    void checkNetWork();

    /**
     * 管理员登录返回
     */
    void loginStatus(InterfaceAdmin.pbui_Type_AdminLogonStatus info);

    /**
     * 展示升级弹窗
     *
     * @param content 升级说明内容
     * @param info    升级通知信息
     */
    void showUpgradeDialog(String content, InterfaceBase.pbui_Type_MeetUpdateNotify info);
}
