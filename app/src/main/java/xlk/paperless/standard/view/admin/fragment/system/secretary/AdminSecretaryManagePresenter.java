package xlk.paperless.standard.view.admin.fragment.system.secretary;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceAdmin;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BasePresenter;

/**
 * @author Created by xlk on 2020/9/21.
 * @desc
 */
public class AdminSecretaryManagePresenter extends BasePresenter {
    private final WeakReference<Context> context;
    private final WeakReference<AdminSecretaryManageInterface> view;
    private List<InterfaceAdmin.pbui_Item_AdminDetailInfo> adminInfos = new ArrayList<>();
    private HashMap<Integer, List<Integer>> allAdminControllableRooms = new HashMap<>();//管理员可控会场
    private List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> controllableRooms = new ArrayList<>();//可控会场
    private List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> allRooms = new ArrayList<>();//剩下的会场
    private int selectAdminId, controllableRoomId, allRoomId;//当前选中的管理员id,选中可控会场id，选中所有会场id

    public AdminSecretaryManagePresenter(Context context, AdminSecretaryManageInterface view) {
        super();
        this.context = new WeakReference<Context>(context);
        this.view = new WeakReference<AdminSecretaryManageInterface>(view);
    }

    public void queryAdmin() {
        InterfaceAdmin.pbui_TypeAdminDetailInfo pbui_typeAdminDetailInfo = jni.queryAdmin();
        adminInfos.clear();
        if (pbui_typeAdminDetailInfo != null) {
            adminInfos.addAll(pbui_typeAdminDetailInfo.getItemList());
        }
        view.get().updateAdminRv(adminInfos);
        for (int i = 0; i < adminInfos.size(); i++) {
            InterfaceAdmin.pbui_Item_AdminDetailInfo info = adminInfos.get(i);
            LogUtil.i(TAG, "queryAdmin 查询到的用户：adminId=" + info.getAdminid() + ", adminName=" + info.getAdminname().toStringUtf8() + ",adminPwd=" + info.getPw().toStringUtf8());
            queryControllableRooms(info.getAdminid());
        }
    }

    public void createAdmin(InterfaceAdmin.pbui_Item_AdminDetailInfo adminInfo) {
        jni.addAdmin(adminInfo);
    }

    public void delAdmin() {
        if (selectAdminId == 0) {
            ToastUtil.show(R.string.please_choose_admin);
            return;
        }
        for (int i = 0; i < adminInfos.size(); i++) {
            if (adminInfos.get(i).getAdminid() == selectAdminId) {
                InterfaceAdmin.pbui_Item_AdminDetailInfo build = InterfaceAdmin.pbui_Item_AdminDetailInfo.newBuilder()
                        .setAdminid(selectAdminId).build();
                jni.delAdmin(build);
                return;
            }
        }
        ToastUtil.show(R.string.please_choose_admin);
    }

    public void modifyAdmin(String name, String pwd, String remarks, String phone, String email) {
        if (selectAdminId == 0) {
            ToastUtil.show(R.string.please_choose_admin);
            return;
        }
        for (int i = 0; i < adminInfos.size(); i++) {
            if (adminInfos.get(i).getAdminid() == selectAdminId) {
                InterfaceAdmin.pbui_Item_AdminDetailInfo build = InterfaceAdmin.pbui_Item_AdminDetailInfo.newBuilder()
                        .setAdminid(selectAdminId)
                        .setAdminname(ConvertUtil.s2b(name))
                        .setPw(ConvertUtil.s2b(pwd))
                        .setComment(ConvertUtil.s2b(remarks))
                        .setPhone(ConvertUtil.s2b(phone))
                        .setEmail(ConvertUtil.s2b(email))
                        .build();
                jni.modifyAdmin(build);
                return;
            }
        }
        ToastUtil.show(R.string.please_choose_admin);
    }

    public void queryControllableRooms(int adminId) {
        InterfaceAdmin.pbui_Type_MeetManagerRoomDetailInfo info = jni.queryAdminRoom(adminId);
        List<Integer> temps = new ArrayList<>();
        if (info != null) {
            temps.addAll(info.getRoomidList());
        }
        LogUtil.i(TAG, "queryControllableRooms adminId=" + adminId + ", 可控的会场id=" + temps);
        allAdminControllableRooms.put(adminId, temps);
    }

    public void queryAllRooms(int adminId) {
        selectAdminId = adminId;
        LogUtil.i(TAG, "queryAllRooms selectAdminId=" + selectAdminId);
        InterfaceRoom.pbui_Type_MeetRoomDetailInfo info = jni.queryRoom();
        controllableRooms.clear();
        allRooms.clear();
        //当前管理员的可控会场id
        List<Integer> currentControllableRooms = allAdminControllableRooms.get(adminId);
        if (info != null) {
            List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> itemList = info.getItemList();
            if (adminId == 1) {//root用户默认可控全部会场
                controllableRooms.addAll(itemList);
            } else {
                for (InterfaceRoom.pbui_Item_MeetRoomDetailInfo item : itemList) {
                    LogUtil.i(TAG, "queryAllRooms 会场id=" + item.getRoomid() + ",会场名称=" + item.getName().toStringUtf8());
                    if (currentControllableRooms != null && currentControllableRooms.contains(item.getRoomid())) {
                        //当前会场在当前的管理员控制下
                        controllableRooms.add(item);
                    } else {
                        //当前会场既不在任何管理员的控制下
                        allRooms.add(item);
                    }
                }
            }
        }
        view.get().updateControllableRoomsRv(controllableRooms);
        view.get().updateAllRoomsRv(allRooms);
    }

    public void addRoom() {
        //先判断选中的用户是否已经不在了
        if (selectAdminId == 0) {
            ToastUtil.show(R.string.please_choose_admin);
            return;
        }
        boolean hasUser = false;
        for (int i = 0; i < adminInfos.size(); i++) {
            if (adminInfos.get(i).getAdminid() == selectAdminId) {
                hasUser = true;
            }
        }
        if (!hasUser) {
            ToastUtil.show(R.string.please_choose_admin);
            return;
        }
        //判断选中的会场是否不在了
        if (allRoomId == 0) {
            ToastUtil.show(R.string.please_choose_room_first);
            return;
        }
        for (int i = 0; i < allRooms.size(); i++) {
            if (allRooms.get(i).getRoomid() == allRoomId) {
                List<Integer> integers = allAdminControllableRooms.get(selectAdminId);
                integers.add(allRoomId);
                jni.saveAdminRoom(selectAdminId, integers);
                return;
            }
        }
        ToastUtil.show(R.string.please_choose_room_first);
    }

    public void removeRoom() {
        //先判断选中的用户是否已经不在了
        if (selectAdminId == 0) {
            ToastUtil.show(R.string.please_choose_admin);
            return;
        }
        boolean hasUser = false;
        for (int i = 0; i < adminInfos.size(); i++) {
            if (adminInfos.get(i).getAdminid() == selectAdminId) {
                hasUser = true;
                break;
            }
        }
        if (!hasUser) {
            ToastUtil.show(R.string.please_choose_admin);
            return;
        }
        //判断选中的会场是否不在了
        if (controllableRoomId == 0) {
            ToastUtil.show(R.string.please_choose_room_first);
            return;
        }
        for (int i = 0; i < controllableRooms.size(); i++) {
            if (controllableRooms.get(i).getRoomid() == controllableRoomId) {
                List<Integer> integers = allAdminControllableRooms.get(selectAdminId);
                integers.remove(integers.indexOf(controllableRoomId));
                jni.saveAdminRoom(selectAdminId, integers);
                return;
            }
        }
        ToastUtil.show(R.string.please_choose_room_first);
    }

    /**
     * 判断用户名是否已经有相同的了
     *
     * @param name 用户名
     * @return
     */
    public boolean isRepeat(String name) {
        for (int i = 0; i < adminInfos.size(); i++) {
            if (adminInfos.get(i).getAdminname().toStringUtf8().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void setAllRoomId(int roomid) {
        allRoomId = roomid;
    }

    public void setControllableRoomId(int roomid) {
        controllableRoomId = roomid;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.clear();
        view.clear();
    }

    @Override
    public void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //管理员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ADMIN_VALUE: {
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    byte[] bytes = (byte[]) msg.getObjects()[0];
                    InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(bytes);
                    int opermethod = pbui_meetNotifyMsg.getOpermethod();
                    int id = pbui_meetNotifyMsg.getId();
                    LogUtil.i(TAG, "BusEvent 管理员变更通知 id=" + id + ", opermethod=" + opermethod);
                    queryAdmin();
                }
                break;
            }
            //会议管理员控制的会场变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MANAGEROOM_VALUE: {
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    byte[] bytes = (byte[]) msg.getObjects()[0];
                    InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(bytes);
                    int opermethod = pbui_meetNotifyMsg.getOpermethod();
                    int id = pbui_meetNotifyMsg.getId();
                    LogUtil.i(TAG, "BusEvent 会议管理员控制的会场变更通知 id=" + id + ", opermethod=" + opermethod);
                    queryControllableRooms(id);
                    for (int i = 0; i < adminInfos.size(); i++) {
                        if (adminInfos.get(i).getAdminid() == selectAdminId) {
                            queryAllRooms(selectAdminId);
                            break;
                        }
                    }
                }
                break;
            }
            //会场信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOM_VALUE: {
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    byte[] bytes = (byte[]) msg.getObjects()[0];
                    InterfaceBase.pbui_MeetNotifyMsg pbui_meetNotifyMsg = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(bytes);
                    int opermethod = pbui_meetNotifyMsg.getOpermethod();
                    int id = pbui_meetNotifyMsg.getId();
                    LogUtil.i(TAG, "BusEvent 会场信息变更通知 id=" + id + ", opermethod=" + opermethod);
                    for (int i = 0; i < adminInfos.size(); i++) {
                        if (adminInfos.get(i).getAdminid() == selectAdminId) {
                            queryAllRooms(selectAdminId);
                            break;
                        }
                    }
                }
                break;
            }
        }
    }
}
