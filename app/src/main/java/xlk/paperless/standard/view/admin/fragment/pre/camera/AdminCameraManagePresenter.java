package xlk.paperless.standard.view.admin.fragment.pre.camera;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceContext;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceVideo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author Created by xlk on 2020/10/22.
 * @desc
 */
public class AdminCameraManagePresenter extends BasePresenter {
    private final AdminCameraManageInterface view;
    private List<DevCameraBean> availableCameras = new ArrayList<>();
    private List<DevCameraBean> allCameras = new ArrayList<>();

    public AdminCameraManagePresenter(AdminCameraManageInterface view) {
        super();
        this.view = view;
    }

    public void queryMeetVideo() {
        InterfaceVideo.pbui_Type_MeetVideoDetailInfo info = jni.queryMeetVideo();
        availableCameras.clear();
        if (info != null) {
            for (int i = 0; i < info.getItemList().size(); i++) {
                InterfaceVideo.pbui_Item_MeetVideoDetailInfo item = info.getItemList().get(i);

                availableCameras.add(new DevCameraBean(item));
            }
        }
        queryAllCamera();
    }

    public void queryAllCamera() {
        InterfaceVideo.pbui_Type_MeetVideoDetailInfo info = jni.queryPlaceStream(queryCurrentRoomId());
        allCameras.clear();
        if (info != null) {
            List<InterfaceVideo.pbui_Item_MeetVideoDetailInfo> itemList = info.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceVideo.pbui_Item_MeetVideoDetailInfo item = itemList.get(i);
                int deviceid1 = item.getDeviceid();
                int id1 = item.getId();
                int subId1 = item.getSubid();
                boolean isAvailable = false;
                for (int j = 0; j < availableCameras.size(); j++) {
                    int deviceid = availableCameras.get(j).getCamera().getDeviceid();
                    int id = availableCameras.get(j).getCamera().getId();
                    int subId = availableCameras.get(j).getCamera().getSubid();
                    if (deviceid1 == deviceid && (subId1 == subId)) {
                        isAvailable = true;
                    }
                }
                if (!isAvailable) {
                    allCameras.add(new DevCameraBean(item));
                }
            }
        }
        queryDevice();
    }

    public void queryDevice() {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo info = jni.queryDeviceInfo();
            if (info != null) {
                for (int i = 0; i < info.getPdevList().size(); i++) {
                    InterfaceDevice.pbui_Item_DeviceDetailInfo dev = info.getPdevList().get(i);
                    for (int j = 0; j < availableCameras.size(); j++) {
                        if (availableCameras.get(j).getCamera().getDeviceid() == dev.getDevcieid()) {
                            availableCameras.get(j).setOnline(dev.getNetstate() == 1);
                        }
                    }
                    for (int j = 0; j < allCameras.size(); j++) {
                        if (allCameras.get(j).getCamera().getDeviceid() == dev.getDevcieid()) {
                            allCameras.get(j).setOnline(dev.getNetstate() == 1);
                        }
                    }
                }
            }
            Collections.sort(availableCameras);
            Collections.sort(allCameras);
            view.updateAvailableCamera(availableCameras);
            view.updateAllCamera(allCameras);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //会议视频变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVIDEO_VALUE:
                LogUtil.i(TAG, "busEvent 会议视频变更通知");
                queryMeetVideo();
                break;
            //会场设备信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE_VALUE:
                LogUtil.i(TAG, "busEvent 会场设备信息变更通知");
                queryAllCamera();
                break;
            //设备寄存器变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO_VALUE:
                queryDevice();
                break;
            default:
                break;
        }
    }
}
