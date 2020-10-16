package xlk.paperless.standard.view.fragment.data;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.DevMember;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BasePresenter;

/**
 * @author xlk
 * @date 2020/3/14
 * @desc
 */
public class MeetDataPresenter extends BasePresenter {
    private final String TAG = "MeetDataPresenter-->";
    private final Context cxt;
    private final IMeetData view;
    private JniHandler jni = JniHandler.getInstance();
    private List<InterfaceFile.pbui_Item_MeetDirDetailInfo> dirDetailInfos = new ArrayList<>();
    private List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> fileDetailInfos = new ArrayList<>();
    List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceDetailInfos = new ArrayList<>();
    List<DevMember> onlineMembers = new ArrayList<>();
    List<InterfaceDevice.pbui_Item_DeviceDetailInfo> onLineProjectors = new ArrayList<>();

    public MeetDataPresenter(Context cxt, IMeetData view) {
        super();
        this.cxt = cxt;
        this.view = view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void busEvent(EventMessage msg) {
        switch (msg.getType()) {
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORY_VALUE://会议目录
                queryMeetDir();
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE_VALUE:
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY.getNumber()) {
                    LogUtil.d(TAG, "BusEvent -->" + "会议目录文件变更通知");
                    queryMeetDir();
                }
                break;
        }
    }

    public void queryMeetDir() {
        try {
            InterfaceFile.pbui_Type_MeetDirDetailInfo dirDetailInfo = jni.queryMeetDir();
            dirDetailInfos.clear();
            if (dirDetailInfo == null) {
                view.updateDir(dirDetailInfos);
                return;
            }
            List<InterfaceFile.pbui_Item_MeetDirDetailInfo> itemList = dirDetailInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceFile.pbui_Item_MeetDirDetailInfo item = itemList.get(i);
                if (item.getParentid() != 0 || item.getId() == Constant.ANNOTATION_FILE_DIRECTORY_ID)
                    continue;//过滤掉子目录和批注文件目录
                LogUtil.i(TAG, "queryMeetDir  -->" + item.getId() + " , " + item.getName().toStringUtf8());
                dirDetailInfos.add(item);
            }
            view.updateDir(dirDetailInfos);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void clearFile() {
        fileDetailInfos.clear();
        view.updateFileRv(fileDetailInfos);
    }

    public void queryMeetFileByDir(int dirId) {
        try {
            InterfaceFile.pbui_Type_MeetDirFileDetailInfo fileDetailInfo = jni.queryMeetDirFile(dirId);
            fileDetailInfos.clear();
            if (fileDetailInfo == null) {
                view.updatePage(cxt.getResources().getString(R.string.default_page));
                view.updateFileRv(fileDetailInfos);
                return;
            }
            fileDetailInfos.addAll(fileDetailInfo.getItemList());
            view.updateFileRv(fileDetailInfos);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(InterfaceFile.pbui_Item_MeetDirFileDetailInfo fileDetailInfo) {
        LogUtil.d(TAG, "downloadFile -->" + "下载文件：" + fileDetailInfo.getName().toStringUtf8());
        if (FileUtil.createDir(Constant.DIR_DATA_FILE)) {
            String pathname = Constant.DIR_DATA_FILE + fileDetailInfo.getName().toStringUtf8();
            File file = new File(pathname);
            if (!file.exists()) {
                jni.creationFileDownload(pathname, fileDetailInfo.getMediaid(), 1, 0, Constant.DOWNLOAD_MEETING_FILE);
            } else {
                if (Values.downloadingFiles.contains(fileDetailInfo.getMediaid())) {
                    ToastUtil.show(R.string.currently_downloading);
                } else {
                    ToastUtil.show(R.string.already_exists_locally);
                }
            }
        }
    }


    public void pushFile(int mediaId) {
        try {
            InterfaceDevice.pbui_Type_DeviceDetailInfo deviceDetailInfo = jni.queryDeviceInfo();
            if (deviceDetailInfo == null) {
                return;
            }
            List<InterfaceDevice.pbui_Item_DeviceDetailInfo> pdevList = deviceDetailInfo.getPdevList();
            deviceDetailInfos.clear();
            deviceDetailInfos.addAll(pdevList);
            onlineMembers.clear();
            onLineProjectors.clear();
            InterfaceMember.pbui_Type_MemberDetailInfo attendPeople = jni.queryAttendPeople();
            List<InterfaceMember.pbui_Item_MemberDetailInfo> itemList = attendPeople.getItemList();
            for (int i = 0; i < deviceDetailInfos.size(); i++) {
                InterfaceDevice.pbui_Item_DeviceDetailInfo detailInfo = deviceDetailInfos.get(i);
                int devcieid = detailInfo.getDevcieid();
                int netstate = detailInfo.getNetstate();
                int memberid = detailInfo.getMemberid();
                int facestate = detailInfo.getFacestate();
                if (Constant.isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetProjective_VALUE, devcieid)
                        && netstate == 1) {
                    onLineProjectors.add(detailInfo);
                }
                if (netstate == 1 && facestate == 1 && devcieid != Values.localDeviceId) {
                    for (int j = 0; j < itemList.size(); j++) {
                        InterfaceMember.pbui_Item_MemberDetailInfo memberDetailInfo = itemList.get(j);
                        if (memberDetailInfo.getPersonid() == memberid) {
                            onlineMembers.add(new DevMember(detailInfo, memberDetailInfo));
                        }
                    }
                }
            }
            view.showPushView(onlineMembers, onLineProjectors, mediaId);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void mediaPlayOperate(int mediaid, List<Integer> devIds, int pos, int res, int triggeruserval, int flag) {
        jni.mediaPlayOperate(mediaid, devIds, pos, res, triggeruserval, flag);
    }

    public void stopPush(List<Integer> res, List<Integer> devIds){
        jni.stopResourceOperate(res,devIds);
    }

    public void uploadFile(int uploadflag, int dirid, int attrib, String newname, String pathname, int userval, int mediaid, String userStr) {
        jni.uploadFile(uploadflag, dirid, attrib, newname, pathname, userval, mediaid, userStr);
    }

}
