package xlk.paperless.standard.view.admin.fragment.system.seat;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author Created by xlk on 2020/11/3.
 * @desc
 */
public class SeatArrangementPresenter extends BasePresenter {

    private final SeatArrangementInterface view;
    /**
     * 所有的会议室
     */
    private List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> roomData = new ArrayList<>();
    /**
     * 所有的设备
     */
    private List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatData = new ArrayList<>();
    /**
     * 当前选中的会议室id
     */
    private int currentRoomId;
    /**
     * 所有背景图片文件
     */
    public List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> pictureData = new ArrayList<>();

    public SeatArrangementPresenter(SeatArrangementInterface view) {
        super();
        this.view = view;
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case Constant.BUS_ROOM_BG:
                String currentRoomBgFilePath = (String) msg.getObjects()[0];
                int mediaId = (int) msg.getObjects()[1];
                view.updateRoomBg(currentRoomBgFilePath, mediaId);
                break;
            //会场信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOM_VALUE: {
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(bytes);
                int id = pbui_meetNotifyMsg.getId();
                int opermethod = pbui_meetNotifyMsg.getOpermethod();
                LogUtil.i(TAG, "BusEvent 会场信息变更通知 id=" + id + ",opermethod=" + opermethod);
                if (currentRoomId != 0 && currentRoomId == id) {
                    queryMeetRoomBg(id);
                }
                queryRoom();
                break;
            }
            //会场设备信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE_VALUE: {
                byte[] o = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsgForDouble pbui_meetNotifyMsgForDouble = InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(o);
                int id = pbui_meetNotifyMsgForDouble.getId();
                int subid = pbui_meetNotifyMsgForDouble.getSubid();
                int opermethod = pbui_meetNotifyMsgForDouble.getOpermethod();
                LogUtil.i(TAG, "BusEvent 会场设备信息变更通知 -->id=" + id + ",subId=" + subid + ",opermethod=" + opermethod);
                if (currentRoomId == id && currentRoomId != 0) {
                    placeDeviceRankingInfo(id);
                }
                break;
            }
            //会议目录文件变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE_VALUE: {
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    byte[] bytes = (byte[]) msg.getObjects()[0];
                    InterfaceBase.pbui_MeetNotifyMsgForDouble info = InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(bytes);
                    int opermethod = info.getOpermethod();
                    int id = info.getId();
                    int subid = info.getSubid();
                    LogUtil.i(TAG, "BusEvent 会议目录文件变更通知 id=" + id + ",subId=" + subid + ",opermethod=" + opermethod);
                    queryBgPicture();
                }
                break;
            }
            //界面配置变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETFACECONFIG_VALUE: {
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(bytes);
                int id = pbui_meetNotifyMsg.getId();
                int opermethod = pbui_meetNotifyMsg.getOpermethod();
                LogUtil.i(TAG, "BusEvent 界面配置变更通知 id=" + id + ",opermethod=" + opermethod);
                if (id == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_SeatIcoShow_GEO_VALUE) {
                    queryRoomIcon();
                }
                break;
            }
            default:
                break;
        }
    }

    public void queryRoom() {
        InterfaceRoom.pbui_Type_MeetRoomDetailInfo pbui_type_meetRoomDetailInfo = jni.queryRoom();
        roomData.clear();
        if (pbui_type_meetRoomDetailInfo != null) {
            List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> itemList = pbui_type_meetRoomDetailInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceRoom.pbui_Item_MeetRoomDetailInfo item = itemList.get(i);
                LogUtil.i(TAG, "queryRoom 会场id=" + item.getRoomid() + ",会场名称=" + item.getName().toStringUtf8());
                if (!item.getName().toStringUtf8().isEmpty()) {
                    roomData.add(item);
                }
            }
        }
        view.updateRoomRv(roomData);
    }

    public void queryMeetRoomBg(int roomId) {
        try {
            int mediaId = jni.queryMeetRoomProperty(roomId);
            if (mediaId != 0) {
                FileUtil.createDir(Constant.DIR_PICTURE);
                jni.creationFileDownload(Constant.DIR_PICTURE + Constant.ROOM_BG_PNG_TAG + ".png", mediaId, 1, 0, Constant.ROOM_BG_PNG_TAG);
                return;
            }
            view.cleanRoomBg();
            placeDeviceRankingInfo(roomId);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询指定会议室的会场排位
     *
     * @param roomid 会议室id
     */
    public void placeDeviceRankingInfo(int roomid) {
        LogUtil.i(TAG, "placeDeviceRankingInfo roomid=" + roomid);
        try {
            currentRoomId = roomid;
            InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo info = jni.placeDeviceRankingInfo(roomid);
            seatData.clear();
            if (info != null) {
                seatData.addAll(info.getItemList());
            }
            view.updateSeatData(seatData);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryBgPicture() {
        InterfaceFile.pbui_TypePageResQueryrFileInfo pbui_typePageResQueryrFileInfo = jni.queryFile(0, InterfaceMacro.Pb_MeetFileQueryFlag.Pb_MEET_FILETYPE_QUERYFLAG_ATTRIB_VALUE
                , 0, 0, 0, InterfaceMacro.Pb_MeetFileAttrib.Pb_MEETFILE_ATTRIB_BACKGROUND_VALUE, 1, 0);
        pictureData.clear();
        if (pbui_typePageResQueryrFileInfo != null) {
            pictureData.addAll(pbui_typePageResQueryrFileInfo.getItemList());
            for (int i = 0; i < pictureData.size(); i++) {
                String name = pictureData.get(i).getName().toStringUtf8();
                LogUtil.i(TAG, "queryBgPicture 背景图片文件名=" + name);
            }
        }
        LogUtil.i(TAG, "queryBgPicture itemList.size=" + pictureData.size());
        view.updatePictureRv();
    }

    public void queryRoomIcon() {
        try {
            InterfaceFaceconfig.pbui_Type_FaceConfigInfo pbui_type_faceConfigInfo = jni.queryInterFaceConfiguration();
            if (pbui_type_faceConfigInfo != null) {
                List<InterfaceFaceconfig.pbui_Item_FaceTextItemInfo> textList = pbui_type_faceConfigInfo.getTextList();
                for (int i = 0; i < textList.size(); i++) {
                    InterfaceFaceconfig.pbui_Item_FaceTextItemInfo item = textList.get(i);
                    int faceid = item.getFaceid();
                    int flag = item.getFlag();
                    if (InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_SeatIcoShow_GEO_VALUE == faceid) {
                        boolean showFlag = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE
                                == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
                        view.updateShowIcon(!showFlag);
                        return;
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置显示/隐藏座位图标
     *
     * @param hideIcon =true设置隐藏，=false设置显示
     */
    public void setHideIcon(boolean hideIcon) {
        LogUtil.i(TAG, "setHideIcon hideIcon=" + hideIcon);
        InterfaceFaceconfig.pbui_Item_FaceTextItemInfo.Builder builder = InterfaceFaceconfig.pbui_Item_FaceTextItemInfo.newBuilder();
        builder.setFaceid(InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_SeatIcoShow_GEO_VALUE);
        if (!hideIcon) {
            //设置显示
            builder.setFlag(InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE
                    | InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_TEXT_VALUE);
        } else {
            //设置隐藏
            builder.setFlag(InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_TEXT_VALUE);
        }
        InterfaceFaceconfig.pbui_Item_FaceTextItemInfo build = builder.build();
        byte[] bytes = InterfaceFaceconfig.pbui_Type_FaceConfigInfo.newBuilder()
                .addText(build)
                .build().toByteArray();
        jni.modifyInterfaceConfig(bytes);
    }
}
