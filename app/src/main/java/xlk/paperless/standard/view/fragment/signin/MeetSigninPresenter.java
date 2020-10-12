package xlk.paperless.standard.view.fragment.signin;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.mogujie.tt.protobuf.InterfaceSignin;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.base.BasePresenter;

/**
 * @author xlk
 * @date 2020/3/18
 * @desc
 */
public class MeetSigninPresenter extends BasePresenter {
    private final IMeetSignin view;
    private final Context cxt;
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberDetailInfos = new ArrayList<>();
    private List<Integer> filterMembers = new ArrayList<>();
    private Timer timer;
    private TimerTask task;
    private boolean isShow = true;
    private boolean isDownLoad;


    public MeetSigninPresenter(Context cxt, IMeetSignin view) {
        super();
        this.cxt = cxt;
        this.view = view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case Constant.BUS_ROOM_BG:
                String filepath = (String) msg.getObjects()[0];
                isDownLoad = true;
                view.updateBg(filepath);
                break;
            //会场设备信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE_VALUE:
                LogUtil.e(TAG, "BusEvent 会场设备信息变更通知 -->");
                executeLater();
                break;
            //会场信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOM_VALUE:
                LogUtil.e(TAG, "BusEvent 会场信息变更通知 -->");
                queryMeetRoomBg();
                break;
            //签到变更
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN_VALUE:
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    querySignin();
                }
                break;
            //界面配置变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETFACECONFIG_VALUE:
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(bytes);
                int id = pbui_meetNotifyMsg.getId();
                int opermethod = pbui_meetNotifyMsg.getOpermethod();
                LogUtil.i(TAG, "BusEvent 界面配置变更通知 id=" + id + ",opermethod=" + opermethod);
//                if (id == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_SeatIcoShow_GEO_VALUE) {
//                    InterfaceFaceconfig.pbui_Type_FaceConfigInfo info = jni.queryInterFaceConfigurationById(id);
//                    if (info != null) {
//                        List<InterfaceFaceconfig.pbui_Item_FaceTextItemInfo> textList = info.getTextList();
//                        InterfaceFaceconfig.pbui_Item_FaceTextItemInfo item = textList.get(0);
//                        int flag = item.getFlag();
//                        boolean showFlag = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
//                    }
//                }
                queryInterFaceConfiguration();
                break;
            default:
                break;
        }
    }

    private void executeLater() {
        //解决短时间内收到很多通知，查询很多次的问题
        if (timer == null) {
            timer = new Timer();
            LogUtil.i(TAG, "创建timer");
            task = new TimerTask() {
                @Override
                public void run() {
                    placeDeviceRankingInfo();
                    task.cancel();
                    timer.cancel();
                    task = null;
                    timer = null;
                }
            };
            LogUtil.i(TAG, "500毫秒之后查询");
            timer.schedule(task, 500);
        }
    }

    /**
     * 1.获取排位图表是否需要显示（小板凳子）
     */
    public void queryInterFaceConfiguration() {
        try {
            InterfaceFaceconfig.pbui_Type_FaceConfigInfo info = jni.queryInterFaceConfiguration();
            if (info == null) {
                return;
            }
            List<InterfaceFaceconfig.pbui_Item_FaceTextItemInfo> textList = info.getTextList();
            for (int i = 0; i < textList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FaceTextItemInfo item = textList.get(i);
                LogUtil.i(TAG, "queryInterFaceConfiguration faceId=" + item.getFaceid());
                //判断排位图表是否显示
                if (item.getFaceid() == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_SeatIcoShow_GEO_VALUE) {
                    int flag = item.getFlag();
                    boolean showFlag = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
                    LogUtil.i(TAG, "queryInterFaceConfiguration 排位图标是否显示=" + showFlag + " ,当前是否显示=" + isShow + " ,是否已经下载过了底图=" + isDownLoad);
                    if (isDownLoad) {
                        //底图已经下载过了，说明不是第一次
                        if (isShow == showFlag) {
                            //排位图标显示状态没改变
                            return;
                        } else {
                            //排位图标显示状态改变
                            isShow = showFlag;
                            placeDeviceRankingInfo();
                        }
                    } else {
                        //第一次进入先下载底图
                        isShow = showFlag;
                        queryMeetRoomBg();
                    }
                    break;
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryMeetRoomBg() {
        try {
            int mediaId = jni.queryMeetRoomProperty(Values.localRoomId);
            if (mediaId != 0) {
                FileUtil.createDir(Constant.dir_picture);
                jni.creationFileDownload(Constant.dir_picture + Constant.ROOM_BG_PNG_TAG + ".png", mediaId, 1, 0, Constant.ROOM_BG_PNG_TAG);
                return;
            }
            placeDeviceRankingInfo();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryMember() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo memberDetailInfo = jni.queryAttendPeople();
            if (memberDetailInfo == null) {
                return;
            }
            memberDetailInfos.clear();
            List<InterfaceMember.pbui_Item_MemberDetailInfo> itemList = memberDetailInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceMember.pbui_Item_MemberDetailInfo item = itemList.get(i);
                if (!filterMembers.contains(item.getPersonid())) {
                    memberDetailInfos.add(item);
                }
            }
            querySignin();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void querySignin() {
        try {
            InterfaceSignin.pbui_Type_MeetSignInDetailInfo signInDetailInfo = jni.querySignin();
            if (signInDetailInfo == null) {
                return;
            }
            if (memberDetailInfos.isEmpty()) {
                return;
            }
            List<InterfaceSignin.pbui_Item_MeetSignInDetailInfo> itemList = signInDetailInfo.getItemList();
            int yqd = 0;
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceSignin.pbui_Item_MeetSignInDetailInfo info = itemList.get(i);
                if (filterMembers.contains(info.getNameId())) {
                    continue;
                }
                long utcseconds = info.getUtcseconds();
                String[] gtmDate = DateUtil.getGTMDate(utcseconds * 1000);
                String dateTime = gtmDate[0] + "  " + gtmDate[2];
                for (InterfaceMember.pbui_Item_MemberDetailInfo item : memberDetailInfos) {
                    if (item.getPersonid() == info.getNameId()) {
                        if (!dateTime.isEmpty()) {
                            yqd++;
                        }
                    }
                }
            }
            view.updateSignin(yqd, memberDetailInfos.size());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void placeDeviceRankingInfo() {
        try {
            InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo meetRoomDevSeatDetailInfo = jni.placeDeviceRankingInfo(Values.localRoomId);
            if (meetRoomDevSeatDetailInfo == null) {
                return;
            }
            filterMembers.clear();
            List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> itemList = meetRoomDevSeatDetailInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item = itemList.get(i);
                if (Values.isFilterSecretary && item.getRole() == InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE) {
                    filterMembers.add(item.getMemberid());
                }
            }
            view.updateView(itemList, isShow);
            queryMember();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
