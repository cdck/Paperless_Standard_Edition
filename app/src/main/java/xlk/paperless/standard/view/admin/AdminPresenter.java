package xlk.paperless.standard.view.admin;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;

import java.lang.ref.WeakReference;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.base.BasePresenter;

/**
 * @author Created by xlk on 2020/9/17.
 * @desc
 */
public class AdminPresenter extends BasePresenter {

    private final String TAG = "AdminPresenter-->";
    private final WeakReference<Context> cxt;
    private final WeakReference<AdminInterface> view;
    private int currentMeetId;

    AdminPresenter(Context context, AdminInterface view) {
        super();
        this.cxt = new WeakReference<Context>(context);
        this.view = new WeakReference<AdminInterface>(view);
        //  修改本机界面状态
        jni.setInterfaceState(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_ROLE_VALUE,
                InterfaceMacro.Pb_MeetFaceStatus.Pb_MemState_AdminFace_VALUE);
        //缓存会议信息
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO.getNumber());
        //缓存会议目录
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORY.getNumber());
        //会议目录文件
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE.getNumber());
        //缓存会议评分
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_FILESCOREVOTESIGN_VALUE);
        // 缓存会场设备
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE.getNumber());
        // 缓存会议排位
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT.getNumber());
        // 缓存参会人信息
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER.getNumber());
        //缓存参会人权限
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBERPERMISSION.getNumber());
        //缓存投票信息
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTEINFO.getNumber());
        //人员签到
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN.getNumber());
        //公告信息
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET.getNumber());
        //会议视频
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVIDEO.getNumber());
        //会议管理员
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ADMIN.getNumber());
        //会议管理员控制的会场
        jni.cacheData(InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MANAGEROOM.getNumber());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cxt.clear();
        view.clear();
    }

    public int getCurrentMeetId() {
        return currentMeetId;
    }

    void queryCurrentMeeting() {
        try {
            InterfaceDevice.pbui_Type_DeviceFaceShowDetail info = jni.queryDeviceMeetInfo();
            String meetName = "";
            currentMeetId = 0;
            if (info != null) {
                meetName = info.getMeetingname().toStringUtf8();
                currentMeetId = info.getMeetingid();
                LogUtil.i(TAG, "queryCurrentMeeting meetName=" + meetName + ", currentMeetId=" + currentMeetId);
            }
            view.get().updateMeetStatus(currentMeetId, -1);
            if (currentMeetId != 0) {
                queryMeetingById(currentMeetId);
                view.get().updateMeetName(meetName);
            } else {
                view.get().updateMeetName(cxt.get().getString(R.string.no_select_meet));
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    void queryMeetingById(int meetId) {
        try {
            InterfaceMeet.pbui_Type_MeetMeetInfo info = jni.queryMeetFromId(meetId);
            if (info != null) {
                InterfaceMeet.pbui_Item_MeetMeetInfo item = info.getItemList().get(0);
                view.get().updateMeetStatus(meetId, item.getStatus());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    void queryLocalDeviceInfo() {
        InterfaceDevice.pbui_Type_DeviceDetailInfo info = jni.queryDevInfoById(Values.localDeviceId);
        if (info != null) {
            InterfaceDevice.pbui_Item_DeviceDetailInfo detailInfo = info.getPdevList().get(0);
            String devName = detailInfo.getDevname().toStringUtf8();
            view.get().updateDeviceName(devName);
        }
    }

    void queryOnline() {
        try {
            byte[] bytes = jni.queryDevicePropertiesById(InterfaceMacro.Pb_MeetDevicePropertyID.Pb_MEETDEVICE_PROPERTY_NETSTATUS_VALUE,
                    0);
            if (bytes == null) {
                view.get().updateOnlineStatus(false);
                return;
            }
            InterfaceDevice.pbui_DeviceInt32uProperty pbui_deviceInt32uProperty = InterfaceDevice.pbui_DeviceInt32uProperty.parseFrom(bytes);
            int propertyval = pbui_deviceInt32uProperty.getPropertyval();
            view.get().updateOnlineStatus(propertyval == 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //会议信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO_VALUE: {
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(bytes);
                int id = pbui_meetNotifyMsg.getId();
                int opermethod = pbui_meetNotifyMsg.getOpermethod();
                LogUtil.i(TAG, "BusEvent -->" + "会议信息变更通知 id=" + id + ",opermethod=" + opermethod);
                queryCurrentMeeting();
                break;
            }
            //设备会议信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEFACESHOW_VALUE: {
                LogUtil.i(TAG, "BusEvent -->" + "设备会议信息变更通知");
                queryCurrentMeeting();
                break;
            }
            //设备寄存器变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO_VALUE: {
                LogUtil.i(TAG, "BusEvent 设备寄存器变更通知");
                queryLocalDeviceInfo();
                queryOnline();
                break;
            }
        }
    }
}
