package xlk.paperless.standard.view.fragment.signin;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.mogujie.tt.protobuf.InterfaceSignin;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.BasePresenter;
import xlk.paperless.standard.view.MyApplication;

/**
 * @author xlk
 * @date 2020/3/18
 * @desc
 */
public class MeetSigninPresenter extends BasePresenter {
    private final String TAG = "MeetSigninPresenter-->";
    private final IMeetSignin view;
    private final Context cxt;
    private JniHandler jni = JniHandler.getInstance();
    private List<InterfaceMember.pbui_Item_MemberDetailInfo> memberDetailInfos = new ArrayList<>();

    public MeetSigninPresenter(Context cxt, IMeetSignin view) {
        this.cxt = cxt;
        this.view = view;
    }

    @Override
    public void register() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void unregister() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void BusEvent(EventMessage msg) {
        switch (msg.getType()) {
            case Constant.BUS_ROOM_BG:
                String filepath = (String) msg.getObjs()[0];
                view.updateBg(filepath);
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOMDEVICE_VALUE://会场设备信息变更通知
                LogUtil.e(TAG, "BusEvent 会场设备信息变更通知 -->");
                placeDeviceRankingInfo();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOM_VALUE://会场信息变更通知
                LogUtil.e(TAG, "BusEvent 会场信息变更通知 -->");
                queryMeetRoomBg();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN_VALUE://签到变更
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    querySignin();
                }
                break;
        }
    }

    public void queryMeetRoomBg() {
        try {
            int mediaId = jni.queryMeetRoomProperty(MyApplication.localRoomId);
            jni.creationFileDownload(Constant.configuration_picture_dir + Constant.ROOM_BG_PNG_TAG + ".png", mediaId, 1, 0, Constant.ROOM_BG_PNG_TAG);
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
            memberDetailInfos.addAll(memberDetailInfo.getItemList());
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
            if (memberDetailInfos.isEmpty()) return;
            List<InterfaceSignin.pbui_Item_MeetSignInDetailInfo> itemList = signInDetailInfo.getItemList();
            int yqd = 0;
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceSignin.pbui_Item_MeetSignInDetailInfo info = itemList.get(i);
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
            InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo meetRoomDevSeatDetailInfo = jni.placeDeviceRankingInfo(MyApplication.localRoomId);
            if (meetRoomDevSeatDetailInfo == null) {
                return;
            }
            List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> itemList = meetRoomDevSeatDetailInfo.getItemList();
            view.updateView(itemList);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
