package xlk.paperless.standard.view.admin.fragment.pre.file;


import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;

/**
 * @author Created by xlk on 2020/10/21.
 * @desc
 */
public class AdminFilePresenter extends BasePresenter {
    private final AdminFileInterface view;
    /**
     * 目录数据
     */
    public List<InterfaceFile.pbui_Item_MeetDirDetailInfo> dirData = new ArrayList<>();
    /**
     * 排序时使用的目录数据
     */
    private List<InterfaceFile.pbui_Item_MeetDirDetailInfo> sortDirData = new ArrayList<>();
    /**
     * 目录下的文件数据
     */
    public List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> dirFiles = new ArrayList<>();
    /**
     * 文件排序时使用
     */
    private List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> sortDirFiles = new ArrayList<>();
    private List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> historyDirFiles = new ArrayList<>();

    private List<MemberDirPermissionBean> memberDirPermissionBeans = new ArrayList<>();
    /**
     * 目录权限中选中的目录id
     */
    private int currentPermissionDirId;
    /**
     * 当前页面中选中的目录id
     */
    private int currentDirId;
    /**
     * 文件排序中选中的目录id
     */
    private int currentSortFileDirId;
    /**
     * 所有的会议
     */
    public List<InterfaceMeet.pbui_Item_MeetMeetInfo> meetings = new ArrayList<>();
    /**
     * 当前的会议id
     */
    private final int currentMeetId;
    private int currentHistoryDirId;

    public AdminFilePresenter(AdminFileInterface view) {
        super();
        this.view = view;
        queryMember();
        queryAllMeeting();
        currentMeetId = queryCurrentMeetId();
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //会议目录变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORY_VALUE: {
                LogUtil.i(TAG, "busEvent 会议目录变更通知");
                queryDir();
                break;
            }
            //会议目录文件变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE_VALUE: {
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsgForDouble info = InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(bytes);
                int opermethod = info.getOpermethod();
                int id = info.getId();
                int subid = info.getSubid();
                LogUtil.i(TAG, "busEvent 会议目录文件变更通知 id=" + id + ",subid=" + subid + ",opermethod=" + opermethod);
                if (id != 0 && (currentDirId == id || currentSortFileDirId == id)) {
                    queryFileByDir(id);
                }
                break;
            }
            //会议目录权限变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYRIGHT_VALUE: {
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg info = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(bytes);
                int id = info.getId();
                int opermethod = info.getOpermethod();
                LogUtil.i(TAG, "busEvent 会议目录权限变更通知 id=" + id + ",opermethod=" + opermethod);
                if (id != 0 && id == currentPermissionDirId) {
                    queryDirPermission(id);
                }
                break;
            }
            //参会人员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE: {
                LogUtil.i(TAG, "BusEvent -->" + "参会人员变更通知");
                queryMember();
                break;
            }
//            //打开下载完成的图片
//            case Constant.BUS_PREVIEW_IMAGE: {
//                String filepath = (String) msg.getObjects()[0];
//                LogUtil.i(TAG, "BusEvent 将要打开的图片路径：" + filepath);
//                int index = 0;
//                if (!picPath.contains(filepath)) {
//                    picPath.add(filepath);
//                    index = picPath.size() - 1;
//                } else {
//                    for (int i = 0; i < picPath.size(); i++) {
//                        if (picPath.get(i).equals(filepath)) {
//                            index = i;
//                        }
//                    }
//                }
//                previewImage(index);
//                break;
//            }
            default:
                break;
        }
    }

    /**
     * 查询参会人员
     */
    public void queryMember() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo info = jni.queryAttendPeople();
            List<MemberDirPermissionBean> temps = new ArrayList<>();
            temps.addAll(memberDirPermissionBeans);
            memberDirPermissionBeans.clear();
            if (info != null) {
                for (int i = 0; i < info.getItemList().size(); i++) {
                    InterfaceMember.pbui_Item_MemberDetailInfo item = info.getItemList().get(i);
                    MemberDirPermissionBean e = new MemberDirPermissionBean(item);
                    for (int j = 0; j < temps.size(); j++) {
                        MemberDirPermissionBean bean = temps.get(j);
                        if (bean.getMember().getPersonid() == item.getPersonid()) {
                            e.setBlacklist(bean.isBlacklist());
                            break;
                        }
                    }
                    memberDirPermissionBeans.add(e);
                }
            }
            //已经选中目录才更新
            if (currentPermissionDirId != 0) {
                view.updateMemberPermission(memberDirPermissionBeans);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryDirPermission(int dirId) {
        currentPermissionDirId = dirId;
        InterfaceFile.pbui_Type_MeetDirRightDetailInfo dirPermission = jni.queryMeetDirPermission(dirId);
        if (dirPermission != null) {
            List<Integer> memberidList = dirPermission.getMemberidList();
            LogUtil.i(TAG, "queryDirPermission " + memberidList);
            for (int i = 0; i < memberDirPermissionBeans.size(); i++) {
                MemberDirPermissionBean bean = memberDirPermissionBeans.get(i);
                bean.setBlacklist(memberidList.contains(bean.getMember().getPersonid()));
            }
        } else {
            for (int i = 0; i < memberDirPermissionBeans.size(); i++) {
                MemberDirPermissionBean bean = memberDirPermissionBeans.get(i);
                bean.setBlacklist(false);
            }
        }
        view.updateMemberPermission(memberDirPermissionBeans);
    }

    public void saveDirPermission(List<Integer> memberIds) {
        if (currentPermissionDirId == 0) {
            ToastUtil.show(R.string.please_choose_dir_first);
            return;
        }
        jni.saveMeetDirPermission(currentPermissionDirId, memberIds);
        ToastUtil.show(R.string.save_successful);
    }

    public void queryDir() {
        try {
            InterfaceFile.pbui_Type_MeetDirDetailInfo dir = jni.queryMeetDir();
            dirData.clear();
            if (dir != null) {
                dirData.addAll(dir.getItemList());
            }
            view.updateDirRv(dirData);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public List<InterfaceFile.pbui_Item_MeetDirDetailInfo> getDirData() {
        return dirData;
    }

    public List<InterfaceFile.pbui_Item_MeetDirDetailInfo> getSortDirData() {
        sortDirData.clear();
        sortDirData.addAll(dirData);
        return sortDirData;
    }

    /**
     * 设置文件排序中选中的目录id
     *
     * @param dirId 目录id
     */
    public void setCurrentSortFileDirId(int dirId) {
        currentSortFileDirId = dirId;
        LogUtil.i(TAG, "setCurrentSortFileDirId dirId=" + dirId);
    }

    /**
     * 设置页面中选中的目录id
     *
     * @param dirId 目录id
     */
    public void setCurrentDirId(int dirId) {
        currentDirId = dirId;
        LogUtil.i(TAG, "setCurrentDirId dirId=" + dirId);
    }

    public void setCurrentHistoryDirId(int dirId) {
        currentHistoryDirId = dirId;
        LogUtil.i(TAG, "setCurrentHistoryDirId dirId=" + dirId);
    }

    public void queryFileByDir(int dirId) {
        try {
            InterfaceFile.pbui_Type_MeetDirFileDetailInfo files = jni.queryMeetDirFile(dirId);
            dirFiles.clear();
            sortDirFiles.clear();
            historyDirFiles.clear();
            if (files != null) {
                dirFiles.addAll(files.getItemList());
                sortDirFiles.addAll(files.getItemList());
                if (currentHistoryDirId == dirId) {
                    historyDirFiles.addAll(files.getItemList());
                }
            }
            LogUtil.i(TAG, "queryFileByDir dirId=" + dirId);
            if (currentDirId == dirId) {
                //更新页面中的目录文件
                view.updateDirFileRv(dirFiles);
            }
            //不能用else 因为有可能都选择同一个目录
            if (currentSortFileDirId == dirId) {
                //更新文件排序PopupWindow中的目录文件
                view.updateSortFileRv(sortDirFiles);
            }
            view.updateHistoryDirFileRv(historyDirFiles);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> getSortDirFile() {
        return sortDirFiles;
    }

    public void createDir(InterfaceFile.pbui_Item_MeetDirDetailInfo build) {
        jni.createMeetDir(build);
    }

    public void modifyDir(InterfaceFile.pbui_Item_MeetDirDetailInfo build) {
        jni.modifyMeetDir(build);
    }

    public void deleteDir(InterfaceFile.pbui_Item_MeetDirDetailInfo build) {
        jni.deleteMeetDir(build);
    }

    public void modifyMeetDirFileName(InterfaceFile.pbui_Item_ModMeetDirFile build) {
        if (currentDirId != 0) {
            jni.modifyMeetDirFileName(currentDirId, build);
        } else {
            ToastUtil.show(R.string.please_choose_dir_first);
        }
    }

    /**
     * 修改目录文件的排序
     *
     * @return 返回true才隐藏PopupWindow
     */
    public boolean modifyMeetDirFileSort() {
        if (currentSortFileDirId == 0) {
            ToastUtil.show(R.string.please_choose_dir_first);
            return false;
        }
        List<Integer> fileIds = new ArrayList<>();
        for (int i = 0; i < sortDirFiles.size(); i++) {
            fileIds.add(sortDirFiles.get(i).getMediaid());
        }
        jni.modifyMeetDirFileSort(currentSortFileDirId, fileIds);
        return true;
    }

    public void deleteMeetDirFile(InterfaceFile.pbui_Item_MeetDirFileDetailInfo build) {
        if (currentDirId != 0) {
            jni.deleteMeetDirFile(currentDirId, build);
        } else {
            ToastUtil.show(R.string.please_choose_dir_first);
        }
    }

    /**
     * 切换会议编辑，修改当前会议id
     *
     * @param meetId 会议id
     */
    public void switchMeeting(int meetId) {
        jni.modifyContextProperties(InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_CURMEETINGID_VALUE, meetId);
        queryDir();
    }

    public void exit() {
        LogUtil.i(TAG, "exit currentMeetId=" + currentMeetId);
        switchMeeting(currentMeetId);
    }

    public void queryAllMeeting() {
        InterfaceMeet.pbui_Type_MeetMeetInfo infos = jni.queryAllMeeting();
        meetings.clear();
        if (infos != null) {
            List<InterfaceMeet.pbui_Item_MeetMeetInfo> itemList = infos.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                if (itemList.get(i).getId() != currentMeetId) {
                    meetings.add(itemList.get(i));
                }
            }
        }
        view.updateMeetingRv(meetings);
    }
}
