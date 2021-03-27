package xlk.paperless.standard.service;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceVote;
import com.mogujie.tt.protobuf.InterfaceWhiteboard;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.DevMember;
import xlk.paperless.standard.data.bean.JoinPro;
import xlk.paperless.standard.ui.ArtBoard;
import xlk.paperless.standard.util.AppUtil;
import xlk.paperless.standard.util.DialogUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.view.CameraActivity;
import xlk.paperless.standard.view.draw.DrawActivity;
import xlk.paperless.standard.view.draw.DrawPresenter;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static xlk.paperless.standard.view.draw.DrawActivity.isDrawing;
import static xlk.paperless.standard.view.draw.DrawPresenter.disposePicOpermemberid;
import static xlk.paperless.standard.view.draw.DrawPresenter.disposePicSrcmemid;
import static xlk.paperless.standard.view.draw.DrawPresenter.disposePicSrcwbidd;
import static xlk.paperless.standard.view.draw.DrawPresenter.isSharing;
import static xlk.paperless.standard.view.draw.DrawPresenter.mSrcmemid;
import static xlk.paperless.standard.view.draw.DrawPresenter.mSrcwbid;
import static xlk.paperless.standard.view.draw.DrawPresenter.savePicData;
import static xlk.paperless.standard.view.draw.DrawPresenter.tempPicData;

/**
 * @author xlk
 * @date 2020/3/26
 * @desc
 */
public class FabPresenter extends BasePresenter {
    private final String TAG = "FabPresenter-->";
    private final IFab view;
    private final Context cxt;
    public List<InterfaceMember.pbui_Item_MemberDetailInfo> memberDetailInfos = new ArrayList<>();
    public List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceDetailInfos = new ArrayList<>();
    public List<InterfaceDevice.pbui_Item_DeviceDetailInfo> onLineProjectors = new ArrayList<>();
    public List<DevMember> onLineMember = new ArrayList<>();
    public List<InterfaceDevice.pbui_Item_DeviceResPlay> canJoinMembers = new ArrayList<>();
    public List<JoinPro> canJoinPros = new ArrayList<>();

    public FabPresenter(Context context, IFab view) {
        super();
        this.cxt = context;
        this.view = view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case Constant.BUS_EXPORT_NOTE_CONTENT:{
                String content = (String) msg.getObjects()[0];
                view.updateNoteContent(content);
                break;
            }
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_WHITEBOARD_VALUE:
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_ASK_VALUE) {//收到打开白板通知
                    if (!isDrawing) {
                        openArtBoardInform(msg);
                    }
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEINFO_VALUE://设备寄存器变更通知
                LogUtil.i(TAG, "BusEvent -->" + "设备寄存器变更通知");
                queryDevice();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE://参会人员变更通知
                LogUtil.i(TAG, "BusEvent -->" + "参会人员变更通知");
                queryMember();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEMEETSTATUS_VALUE://界面状态变更通知
                LogUtil.i(TAG, "BusEvent -->" + "界面状态变更通知");
                queryMember();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETONVOTING_VALUE://会议发起投票
                LogUtil.i(TAG, "BusEvent -->" + "会议发起投票");
                byte[] o = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(o);
                int opermethod = pbui_meetNotifyMsg.getOpermethod();
                int id = pbui_meetNotifyMsg.getId();
                if (opermethod == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_START_VALUE) {
                    queryInitiateVote(id);
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTEINFO_VALUE://投票变更通知
                byte[] o1 = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg1 = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(o1);
                int opermethod1 = pbui_meetNotifyMsg1.getOpermethod();
                int id1 = pbui_meetNotifyMsg1.getId();
                if (opermethod1 == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_STOP_VALUE) {
                    LogUtil.i(TAG, "BusEvent -->" + "收到结束投票通知 投票ID= " + id1);
                    view.closeVoteView();
                }
                break;
            case Constant.BUS_COLLECT_CAMERA_START:
                int type = (int) msg.getObjects()[0];
                LogUtil.i(TAG, "BusEvent -->" + "收到开始采集摄像头通知 type= " + type);
                if (AppUtil.checkCamera(cxt, 1)) {
                    ToastUtil.show(R.string.opening_camera);
                    Intent intent = new Intent(cxt, CameraActivity.class);
                    intent.putExtra(Constant.EXTRA_CAMERA_TYPE, 1);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    cxt.startActivity(intent);
                } else if (AppUtil.checkCamera(cxt, 0)) {
                    ToastUtil.show(R.string.opening_camera);
                    Intent intent = new Intent(cxt, CameraActivity.class);
                    intent.putExtra(Constant.EXTRA_CAMERA_TYPE, 0);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    cxt.startActivity(intent);
                } else {
                    ToastUtil.show(R.string.not_find_camera);
                }
//                view.showOpenCamera();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEVICEOPER_VALUE://设备交互信息
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_REQUESTINVITE_VALUE) {
                    byte[] o2 = (byte[]) msg.getObjects()[0];
                    InterfaceDevice.pbui_Type_DeviceChat info = InterfaceDevice.pbui_Type_DeviceChat.parseFrom(o2);
                    int inviteflag = info.getInviteflag();
                    int operdeviceid = info.getOperdeviceid();
                    LogUtil.i(TAG, "BusEvent -->" + "收到设备对讲的通知 inviteflag = " + inviteflag + ", operdeviceid= " + operdeviceid);
                    view.showView(inviteflag, operdeviceid);
                } else if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_REQUESTPRIVELIGE_VALUE) {
                    LogUtil.i(TAG, "BusEvent -->" + "收到参会人权限请求");
                    byte[] o2 = (byte[]) msg.getObjects()[0];
                    InterfaceDevice.pbui_Type_MeetRequestPrivilegeNotify info = InterfaceDevice.pbui_Type_MeetRequestPrivilegeNotify.parseFrom(o2);
                    view.applyPermissionsInform(info);
                }
                break;
        }
    }

    public String getMemberNameById(int memberid) {
        for (int i = 0; i < memberDetailInfos.size(); i++) {
            InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo = memberDetailInfos.get(i);
            if (memberDetailInfo.getPersonid() == memberid) {
                return memberDetailInfo.getName().toStringUtf8();
            }
        }
        return "";
    }

    public String getMemberNameByDevid(int devid) {
        for (int i = 0; i < onLineMember.size(); i++) {
            DevMember devMember = onLineMember.get(i);
            if (devMember.getDeviceDetailInfo().getDevcieid() == devid) {
                return devMember.getMemberDetailInfo().getName().toStringUtf8();
            }
        }
        return "";
    }

    /**
     * 查询发起的投票
     *
     * @param voteid
     */
    private void queryInitiateVote(int voteid) {
        try {
            InterfaceVote.pbui_Type_MeetOnVotingDetailInfo info = jni.queryInitiateVote();
            if (info == null) {
                return;
            }
            List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> itemList = info.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceVote.pbui_Item_MeetOnVotingDetailInfo item = itemList.get(i);
                if (item.getVoteid() == voteid) {
                    view.showVoteView(item);
                    break;
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }


    private void openArtBoardInform(EventMessage msg) throws InvalidProtocolBufferException {
        byte[] o = (byte[]) msg.getObjects()[0];
        InterfaceWhiteboard.pbui_Type_MeetStartWhiteBoard object = InterfaceWhiteboard.pbui_Type_MeetStartWhiteBoard.parseFrom(o);
        int operflag = object.getOperflag();//指定操作标志 参见Pb_MeetPostilOperType
        String medianame = object.getMedianame().toStringUtf8();//白板操作描述
        disposePicOpermemberid = object.getOpermemberid();//当前该命令的人员ID
        disposePicSrcmemid = object.getSrcmemid();//发起人的人员ID 白板标识使用
        disposePicSrcwbidd = object.getSrcwbid();//发起人的白板标识 取微秒级的时间作标识 白板标识使用
        if (operflag == InterfaceMacro.Pb_MeetPostilOperType.Pb_MEETPOTIL_FLAG_FORCEOPEN.getNumber()) {
            LogUtil.i(TAG, "eventOpenBoard: 强制打开白板  直接强制同意加入..");
            jni.agreeJoin(Values.localMemberId, disposePicSrcmemid, disposePicSrcwbidd);
            Intent intent = new Intent(cxt, DrawActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            cxt.startActivity(intent);
            isSharing = true;//如果同意加入就设置已经在共享中
            mSrcmemid = disposePicSrcmemid;//设置发起的人员ID
            DrawPresenter.mSrcwbid = disposePicSrcwbidd;//设置白板标识
        } else if (operflag == InterfaceMacro.Pb_MeetPostilOperType.Pb_MEETPOTIL_FLAG_REQUESTOPEN.getNumber()) {
            LogUtil.i(TAG, "eventOpenBoard: 询问打开白板..");
            whetherOpen(disposePicSrcmemid, disposePicSrcwbidd, medianame, disposePicOpermemberid);
        }
    }

    private void whetherOpen(final int srcmemid, final long srcwbidd, String medianame, final int opermemberid) {
        DialogUtil.createDialog(cxt, cxt.getString(R.string.title_whether_agree_join, medianame),
                cxt.getString(R.string.agree), cxt.getString(R.string.reject), new DialogUtil.onDialogClickListener() {
                    @Override
                    public void positive(DialogInterface dialog) {
                        //同意加入
                        jni.agreeJoin(Values.localMemberId, srcmemid, srcwbidd);
                        isSharing = true;//如果同意加入就设置已经在共享中
                        mSrcmemid = srcmemid;//设置发起的人员ID
                        mSrcwbid = srcwbidd;
                        Intent intent1 = new Intent(cxt, DrawActivity.class);
                        if (tempPicData != null) {
                            savePicData = tempPicData;
                            /** **** **  作为接收者保存  ** **** **/
                            ArtBoard.DrawPath drawPath = new ArtBoard.DrawPath();
                            drawPath.operid = Values.operid;
                            drawPath.srcwbid = srcwbidd;
                            drawPath.srcmemid = srcmemid;
                            drawPath.opermemberid = opermemberid;
                            drawPath.picdata = savePicData;
                            Values.operid = 0;
                            tempPicData = null;
                            //将路径保存到共享中绘画信息
                            DrawPresenter.pathList.add(drawPath);
                        }
                        intent1.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        cxt.startActivity(intent1);
                        //自己不是发起人的时候,每次收到绘画通知都要判断是不是同一个发起人和白板标识
                        //并且集合中没有这一号人,将其添加进集合中
                        if (!DrawPresenter.togetherIDs.contains(opermemberid)) {
                            DrawPresenter.togetherIDs.add(opermemberid);
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void negative(DialogInterface dialog) {
                        jni.rejectJoin(Values.localMemberId, srcmemid, srcwbidd);
                        dialog.dismiss();
                    }

                    @Override
                    public void dismiss(DialogInterface dialog) {

                    }
                });
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

    public void queryCanJoin() {
        try {
            InterfaceDevice.pbui_Type_DeviceResPlay pbui_type_deviceResPlay = jni.queryCanJoin();
            canJoinPros.clear();
            canJoinMembers.clear();
            if (pbui_type_deviceResPlay == null) {
                view.notifyJoinAdapter();
                return;
            }
            List<InterfaceDevice.pbui_Item_DeviceResPlay> pdevList = pbui_type_deviceResPlay.getPdevList();
            for (int i = 0; i < pdevList.size(); i++) {
                InterfaceDevice.pbui_Item_DeviceResPlay resPlay = pdevList.get(i);
                int devceid = resPlay.getDevceid();
                boolean isPro = Constant.isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetProjective_VALUE, devceid);
                if (isPro) {//投影机
                    for (InterfaceDevice.pbui_Item_DeviceDetailInfo info : deviceDetailInfos) {
                        if (info.getDevcieid() == devceid) {
                            canJoinPros.add(new JoinPro(resPlay, info));
                        }
                    }
                } else {
                    canJoinMembers.add(resPlay);
                }
            }
            view.notifyJoinAdapter();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
