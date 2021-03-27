package xlk.paperless.standard.view.admin.fragment.after.archive;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ZipUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceAdmin;
import com.mogujie.tt.protobuf.InterfaceAgenda;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceBullet;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfaceRoom;
import com.mogujie.tt.protobuf.InterfaceSignin;
import com.mogujie.tt.protobuf.InterfaceVote;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.JxlUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.MyApplication;
import xlk.paperless.standard.view.admin.fragment.after.signin.SignInBean;
import xlk.paperless.standard.view.admin.fragment.pre.member.MemberRoleBean;

import static xlk.paperless.standard.data.Constant.ANNOTATION_FILE_DIRECTORY_ID;
import static xlk.paperless.standard.data.Constant.SHARED_FILE_DIRECTORY_ID;

/**
 * @author Created by xlk on 2020/10/27.
 * @desc
 */
public class ArchivePresenter extends BasePresenter {
    private final ArchiveInterface view;
    /**
     * 会议公告
     */
    private List<InterfaceBullet.pbui_Item_BulletDetailInfo> noticeData = new ArrayList<>();
    /**
     * 议程的文本内容
     */
    private String agendaContent;
    /**
     * 议程的文件id
     */
    private int agendaMediaId;
    /**
     * 当前的议程类型
     */
    private int agendaType;
    /**
     * 当前的会议信息
     */
    private InterfaceMeet.pbui_Item_MeetMeetInfo currentMeetInfo;
    /**
     * 当前的会场（会议室）信息
     */
    private InterfaceRoom.pbui_Item_MeetRoomDetailInfo currentRoomInfo;
    /**
     * 当前登录的管理员信息
     */
    private InterfaceAdmin.pbui_Item_AdminDetailInfo currentAdminInfo;
    /**
     * 参会人员信息（包含参会人角色）
     */
    private List<MemberRoleBean> devSeatInfos = new ArrayList<>();
    /**
     * 签到信息
     */
    private List<SignInBean> signInData = new ArrayList<>();
    /**
     * 投票信息
     */
    private List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteData = new ArrayList<>();
    /**
     * 选举信息
     */
    private List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> electionData = new ArrayList<>();
    /**
     * 共享文件信息
     */
    private List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> shareFileData = new ArrayList<>();
    /**
     * 批注文件信息
     */
    private List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> annotationFileData = new ArrayList<>();
    /**
     * 会议资料信息（其它目录下的文件）
     */
    private List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> otherFileData = new ArrayList<>();
    /**
     * 操作通知
     */
    private List<ArchiveInform> archiveInforms = new ArrayList<>();
    /**
     * 存档操作时的任务tag,不为空则说明正在下载文件，还未开始压缩
     */
    private List<String> archiveTasks = new ArrayList<>();
    /**
     * =true表示正在进行压缩阶段
     */
    private boolean isCompressing;
    /**
     * =true表示压缩时进行加密处理
     */
    private boolean isEncryption;


    public ArchivePresenter(ArchiveInterface view) {
        super();
        this.view = view;
    }

    public void queryAll() {
        queryNotice();
        queryAgenda();
        queryMeetById();
        queryRoom();
        queryAdmin();
        queryMember();
        querySignin();
        queryVote();
        queryDir();
    }

    /**
     * 添加任务
     *
     * @param tag 任务tag
     */
    private void addTask(String tag) {
        if (!archiveTasks.contains(tag)) {
            LogUtil.i(TAG, "addTask 添加任务=" + tag);
            archiveTasks.add(tag);
        }
    }

    /**
     * 移除任务
     *
     * @param tag 任务tag
     */
    private void removeTask(String tag) {
        if (archiveTasks.contains(tag)) {
            LogUtil.d(TAG, "removeTask 移除任务=" + tag);
            archiveTasks.remove(tag);
            if (archiveTasks.isEmpty()) {
                zipArchiveDir();
            }
        }
    }

    
    public boolean hasStarted(){
        return isCompressing || !archiveTasks.isEmpty();
    }
    
    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            //公告变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET_VALUE: {
                queryNotice();
                break;
            }
            //议程变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETAGENDA_VALUE: {
                queryAgenda();
                break;
            }
            //会议信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETINFO_VALUE: {
                queryMeetById();
                break;
            }
            //会场信息变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ROOM_VALUE: {
                LogUtil.i(TAG, "BusEvent 会场信息变更通知");
                queryRoom();
                break;
            }
            //管理员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ADMIN_VALUE: {
                LogUtil.i(TAG, "busEvent 管理员变更通知");
                queryAdmin();
                break;
            }
            //会议排位变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSEAT_VALUE: {
                LogUtil.i(TAG, "busEvent " + "会议排位变更通知");
                queryPlaceRanking();
                break;
            }
            //参会人员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEMBER_VALUE: {
                LogUtil.i(TAG, "BusEvent -->" + "参会人员变更通知");
                queryMember();
                break;
            }
            //签到变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETSIGN_VALUE: {
                LogUtil.i(TAG, "busEvent 签到变更通知");
                querySignin();
                break;
            }
            //投票变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETVOTEINFO_VALUE: {
                LogUtil.d(TAG, "BusEvent -->" + "投票变更通知");
                queryVote();
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
                queryDirFile(id);
                break;
            }
            //会议目录变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORY_VALUE: {
                LogUtil.i(TAG, "busEvent 会议目录变更通知");
                queryDir();
                break;
            }
            //归档下载议程文件完成
            case Constant.ARCHIVE_BUS_AGENDA_FILE: {
                archiveInforms.add(new ArchiveInform("会议议程信息导出完成", "100%"));
                view.updateArchiveInform(archiveInforms);
                break;
            }
            //归档下载的文件下载进度
            case Constant.ARCHIVE_BUS_DOWNLOAD_FILE: {
                Object[] objects = msg.getObjects();
                int mediaId = (int) objects[0];
                String fileName = (String) objects[1];
                int progress = (int) objects[2];
                for (int i = 0; i < archiveInforms.size(); i++) {
                    ArchiveInform archiveInform = archiveInforms.get(i);
                    if (archiveInform.getId() == mediaId) {
                        archiveInform.setContent("开始下载文件：" + fileName);
                        archiveInform.setResult("下载进度：" + progress + "%");
                        break;
                    }
                }
                view.updateArchiveInform(archiveInforms);
                if (progress == 100) {
                    removeTask(String.valueOf(mediaId));
                } else {
                    addTask(String.valueOf(mediaId));
                }
                break;
            }
            default:
                break;
        }
    }


    public void zipArchiveDir() {
        MyApplication.threadPool.execute(() -> {
            try {
                Thread.sleep(500);
                if (!archiveTasks.isEmpty()) {
                    LogUtil.i(TAG, "run 还有正在下载的文件");
                    return;
                }
                if (isCompressing) {
                    LogUtil.i(TAG, "zipArchiveDir 当前正在压缩...");
                    return;
                }
                File srcFile = new File(Constant.DIR_ARCHIVE_TEMP);
                if (!srcFile.exists()) {
                    LogUtil.e(TAG, "zipArchiveDir 没有找到这个目录=" + Constant.DIR_ARCHIVE_TEMP);
                    return;
                }
                isCompressing = true;
                LogUtil.i(TAG, "run 开始压缩 当前线程=" + Thread.currentThread().getId());
                archiveInforms.add(new ArchiveInform("开始压缩", "进行中..."));
                view.updateArchiveInform(archiveInforms);
                FileUtil.createDir(Constant.DIR_ARCHIVE_ZIP);
                String zipFilePath = Constant.DIR_ARCHIVE_ZIP + "会议归档.zip";
                File zipFile = new File(zipFilePath);
                if (zipFile.exists()) {
                    zipFilePath = Constant.DIR_ARCHIVE_ZIP + "会议归档-" + DateUtil.nowDate() + ".zip";
                }
//                System.out.println("当前文件名编码格式：" + getEncoding(zipFilePath));
//                Properties initProp = new Properties(System.getProperties());
//                Charset charset = Charset.defaultCharset();
//                System.out.println("charset:" + charset.name() + ",toString=" + charset.toString());
//                System.out.println("当前系统编码:" + initProp.getProperty("file.encoding"));
//                System.out.println("当前系统语言:" + initProp.getProperty("user.language"));

//                if (isEncryption) {
//                    File file = new File(Constant.DIR_ARCHIVE_TEMP);
//                    ZipUtil.doZipFilesWithPassword(file, zipFilePath, "123456");
//                } else {
                    ZipUtils.zipFile(Constant.DIR_ARCHIVE_TEMP, zipFilePath);
//                }
                for (int i = 0; i < archiveInforms.size(); i++) {
                    ArchiveInform archiveInform = archiveInforms.get(i);
                    if (archiveInform.getContent().equals("开始压缩")) {
                        archiveInform.setContent("压缩完毕");
                        archiveInform.setResult("100%");
                        break;
                    }
                }
                view.updateArchiveInform(archiveInforms);
                LogUtil.i(TAG, "run 压缩完毕");
                FileUtil.delDirFile(Constant.DIR_ARCHIVE_TEMP);
//                FileUtils.deleteAllInDir(Constant.DIR_ARCHIVE_TEMP);

                isCompressing = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static String getEncoding(String str) {
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是GB2312
                String s = encode;
                return s; //是的话，返回“GB2312“，以下代码同理
            }
        } catch (Exception exception) {
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是ISO-8859-1
                String s1 = encode;
                return s1;
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是UTF-8
                String s2 = encode;
                return s2;
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是GBK
                String s3 = encode;
                return s3;
            }
        } catch (Exception exception3) {
        }
        return "";
    }

    private void zip(ZipOutputStream zout, File target, String name, BufferedOutputStream bos) throws IOException {
        //判断是不是目录
        if (target.isDirectory()) {
            File[] files = target.listFiles();
            //空目录
            if (files.length == 0) {
                zout.putNextEntry(new ZipEntry(name + "/"));
            /*  开始编写新的ZIP文件条目，并将流定位到条目数据的开头。
              关闭当前条目，如果仍然有效。 如果没有为条目指定压缩方法，
              将使用默认压缩方法，如果条目没有设置修改时间，将使用当前时间。*/
            }
            for (File f : files) {
                //递归处理
                zip(zout, f, name + "/" + f.getName(), bos);
            }
        } else {
            zout.putNextEntry(new ZipEntry(name));
            InputStream inputStream = new FileInputStream(target);
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            byte[] bytes = new byte[1024];
            int len = -1;
            while ((len = bis.read(bytes)) != -1) {
                bos.write(bytes, 0, len);
            }
            bis.close();
        }
    }

    private void queryDir() {
        try {
            InterfaceFile.pbui_Type_MeetDirDetailInfo dir = jni.queryMeetDir();
            if (dir != null) {
                for (int i = 0; i < dir.getItemList().size(); i++) {
                    InterfaceFile.pbui_Item_MeetDirDetailInfo item = dir.getItemList().get(i);
                    queryDirFile(item.getId());
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryDirFile(int dirId) {
        try {
            InterfaceFile.pbui_Type_MeetDirFileDetailInfo pbui_type_meetDirFileDetailInfo = jni.queryMeetDirFile(dirId);
            if (dirId == ANNOTATION_FILE_DIRECTORY_ID) {
                annotationFileData.clear();
                if (pbui_type_meetDirFileDetailInfo != null) {
                    annotationFileData.addAll(pbui_type_meetDirFileDetailInfo.getItemList());
                }
            } else if (dirId == SHARED_FILE_DIRECTORY_ID) {
                shareFileData.clear();
                if (pbui_type_meetDirFileDetailInfo != null) {
                    shareFileData.addAll(pbui_type_meetDirFileDetailInfo.getItemList());
                }
            } else {
                otherFileData.clear();
                if (pbui_type_meetDirFileDetailInfo != null) {
                    otherFileData.addAll(pbui_type_meetDirFileDetailInfo.getItemList());
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryVote() {
        try {
            InterfaceVote.pbui_Type_MeetVoteDetailInfo pbui_type_meetVoteDetailInfo = jni.queryVote();
            if (pbui_type_meetVoteDetailInfo != null) {
                List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> itemList = pbui_type_meetVoteDetailInfo.getItemList();
                for (int i = 0; i < itemList.size(); i++) {
                    InterfaceVote.pbui_Item_MeetVoteDetailInfo item = pbui_type_meetVoteDetailInfo.getItem(i);
                    if (item.getMaintype() == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote_VALUE) {
                        voteData.add(item);
                    } else if (item.getMaintype() == InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_election_VALUE) {
                        electionData.add(item);
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryMember() {
        try {
            InterfaceMember.pbui_Type_MemberDetailInfo pbui_type_memberDetailInfo = jni.queryAttendPeople();
            devSeatInfos.clear();
            signInData.clear();
            if (pbui_type_memberDetailInfo != null) {
                List<InterfaceMember.pbui_Item_MemberDetailInfo> itemList = pbui_type_memberDetailInfo.getItemList();
                for (int i = 0; i < itemList.size(); i++) {
                    devSeatInfos.add(new MemberRoleBean(itemList.get(i)));
                    signInData.add(new SignInBean(itemList.get(i)));
                }
            }
            querySignin();
            queryPlaceRanking();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryPlaceRanking() {
        try {
            InterfaceRoom.pbui_Type_MeetRoomDevSeatDetailInfo info = jni.placeDeviceRankingInfo(Values.localRoomId);
            if (info != null) {
                for (int i = 0; i < devSeatInfos.size(); i++) {
                    MemberRoleBean bean = devSeatInfos.get(i);
                    for (int j = 0; j < info.getItemList().size(); j++) {
                        InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item = info.getItemList().get(j);
                        if (item.getMemberid() == bean.getMember().getPersonid()) {
                            bean.setSeat(item);
                            break;
                        }
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void querySignin() {
        try {
            InterfaceSignin.pbui_Type_MeetSignInDetailInfo pbui_type_meetSignInDetailInfo = jni.querySignin();
            if (pbui_type_meetSignInDetailInfo != null) {
                List<InterfaceSignin.pbui_Item_MeetSignInDetailInfo> itemList = pbui_type_meetSignInDetailInfo.getItemList();
                for (int i = 0; i < signInData.size(); i++) {
                    SignInBean signInBean = signInData.get(i);
                    for (int j = 0; j < itemList.size(); j++) {
                        InterfaceSignin.pbui_Item_MeetSignInDetailInfo item = itemList.get(j);
                        if (item.getNameId() == signInBean.getMember().getPersonid()) {
                            signInBean.setSign(item);
                            break;
                        }
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryAdmin() {
        int currentAdminId = queryCurrentAdminId();
        InterfaceAdmin.pbui_TypeAdminDetailInfo pbui_typeAdminDetailInfo = jni.queryAdmin();
        if (pbui_typeAdminDetailInfo != null) {
            List<InterfaceAdmin.pbui_Item_AdminDetailInfo> itemList = pbui_typeAdminDetailInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceAdmin.pbui_Item_AdminDetailInfo pbui_item_adminDetailInfo = itemList.get(i);
                if (pbui_item_adminDetailInfo.getAdminid() == currentAdminId) {
                    currentAdminInfo = pbui_item_adminDetailInfo;
                    break;
                }
            }
        }
    }

    private void queryAgenda() {
        try {
            InterfaceAgenda.pbui_meetAgenda meetAgenda = jni.queryAgenda();
            if (meetAgenda != null) {
                agendaType = meetAgenda.getAgendatype();
                agendaContent = meetAgenda.getText().toStringUtf8();
                agendaMediaId = meetAgenda.getMediaid();
                LogUtil.i(TAG, "queryAgenda agendaMediaId=" + agendaMediaId + ",agendaContent=" + agendaContent.length());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryNotice() {
        try {
            InterfaceBullet.pbui_BulletDetailInfo pbui_bulletDetailInfo = jni.queryNotice();
            noticeData.clear();
            if (pbui_bulletDetailInfo != null) {
                noticeData.addAll(pbui_bulletDetailInfo.getItemList());
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryMeetById() {
        try {
            InterfaceMeet.pbui_Item_MeetMeetInfo info = jni.queryMeetFromId(queryCurrentMeetId());
            currentMeetInfo = info;
            queryRoom();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void queryRoom() {
        InterfaceRoom.pbui_Item_MeetRoomDetailInfo room = jni.queryRoomById(queryCurrentRoomId());
        currentRoomInfo = room;
    }

    /**
     * 设置是否加密压缩
     *
     * @param isEncryption =true 需要加密
     */
    public void setEncryption(boolean isEncryption) {
        if (isCompressing || !archiveTasks.isEmpty()) {
            return;
        }
        this.isEncryption = isEncryption;
        LogUtil.i(TAG, "setEncryption 是否加密=" + isEncryption);
    }

    /**
     * 归档全部信息
     */
    public void archiveAll() {
//        if (isCompressing || !archiveTasks.isEmpty()) {
//            view.showToast(R.string.please_wait_archive_complete_first);
//            return;
//        }
        archiveInforms.clear();
        archiveTasks.clear();
        long l = System.currentTimeMillis();
        archiveMeetInfo();
        archiveMemberInfo();
        archiveSignInfo();
        archiveVoteInfo();
        archiveShareInfo();
        archiveAnnotationInfo();
        archiveMeetData();
        LogUtil.i(TAG, "archiveAll 归档总用时：" + (System.currentTimeMillis() - l));
    }

    /**
     * 归档选中的项
     *
     * @param meetInfo       会议基本信息
     * @param memberInfo     参会人员信息
     * @param signInfo       会议签到结果
     * @param voteInfo       会议投票结果
     * @param shareInfo      会议共享文件
     * @param annotationInfo 会议批注文件
     * @param meetData       会议资料
     */
    public void archiveSelected(boolean meetInfo, boolean memberInfo, boolean signInfo, boolean voteInfo,
                                boolean shareInfo, boolean annotationInfo, boolean meetData) {
//        if (isCompressing || !archiveTasks.isEmpty()) {
//            view.showToast(R.string.please_wait_archive_complete_first);
//            return;
//        }
        archiveInforms.clear();
        archiveTasks.clear();
        if (meetInfo) {
            archiveMeetInfo();
        }
        if (memberInfo) {
            archiveMemberInfo();
        }
        if (signInfo) {
            archiveSignInfo();
        }
        if (voteInfo) {
            archiveVoteInfo();
        }
        if (shareInfo) {
            archiveShareInfo();
        }
        if (annotationInfo) {
            archiveAnnotationInfo();
        }
        if (meetData) {
            archiveMeetData();
        }
    }

    /**
     * 归档会议基本信息
     */
    private void archiveMeetInfo() {
        addTask("归档会议基本信息");
        long l = System.currentTimeMillis();
        //会议基本信息
        meetInfo2file();
        // 会议议程信息
        if (agendaType == InterfaceMacro.Pb_AgendaType.Pb_MEET_AGENDA_TYPE_TEXT_VALUE) {
            read2file("会议议程信息.txt", agendaContent);
        } else if (agendaType == InterfaceMacro.Pb_AgendaType.Pb_MEET_AGENDA_TYPE_FILE_VALUE) {
            downloadAgendaFile();
        }
        // 会议公告信息
        notice2file();
        LogUtil.i(TAG, "归档会议基本信息 用时：" + (System.currentTimeMillis() - l));
        removeTask("归档会议基本信息");
    }

    /**
     * 归档参会人信息
     */
    private void archiveMemberInfo() {
        if (devSeatInfos.isEmpty()) {
            return;
        }
        long l = System.currentTimeMillis();
        addTask("归档参会人信息");
        if (JxlUtil.exportMemberInfo(devSeatInfos)) {
            LogUtil.i(TAG, "归档参会人信息 用时=" + (System.currentTimeMillis() - l));
            archiveInforms.add(new ArchiveInform("参会人员信息导出完成", "100%"));
            view.updateArchiveInform(archiveInforms);
            removeTask("归档参会人信息");
        }
    }

    /**
     * 归档签到信息
     */
    private void archiveSignInfo() {
        if (signInData.isEmpty()) {
            return;
        }
        addTask("归档签到信息");
        long l = System.currentTimeMillis();
        JxlUtil.exportArchiveSignIn(signInData);
        LogUtil.i(TAG, "归档签到信息 用时=" + (System.currentTimeMillis() - l));
        archiveInforms.add(new ArchiveInform("签到信息导出完成", "100%"));
        view.updateArchiveInform(archiveInforms);
        removeTask("归档签到信息");
    }

    /**
     * 归档投票结果
     */
    private void archiveVoteInfo() {
        addTask("归档投票结果");
        long l = System.currentTimeMillis();
        if (!voteData.isEmpty()) {
            JxlUtil.exportArchiveVote(voteData, devSeatInfos.size(), true);
        }
        if (!electionData.isEmpty()) {
            JxlUtil.exportArchiveVote(electionData, devSeatInfos.size(), false);
        }
        LogUtil.i(TAG, "归档投票结果 用时：" + (System.currentTimeMillis() - l));
        archiveInforms.add(new ArchiveInform("投票信息导出完成", "100%"));
        view.updateArchiveInform(archiveInforms);
        removeTask("归档投票结果");
    }

    /**
     * 归档共享文件
     */
    private void archiveShareInfo() {
        if (shareFileData.isEmpty()) {
            return;
        }
        FileUtil.createDir(Constant.DIR_ARCHIVE_TEMP + "共享文件/");
        for (int i = 0; i < shareFileData.size(); i++) {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo item = shareFileData.get(i);
            String fileName = item.getName().toStringUtf8();
            archiveInforms.add(new ArchiveInform(item.getMediaid(), "开始下载文件：" + fileName, "0%"));
            view.updateArchiveInform(archiveInforms);
            addTask(String.valueOf(item.getMediaid()));
            jni.creationFileDownload(Constant.DIR_ARCHIVE_TEMP + "共享文件/" + fileName, item.getMediaid(), 1, 0, Constant.ARCHIVE_DOWNLOAD_FILE);
        }
    }

    /**
     * 归档批注文件
     */
    private void archiveAnnotationInfo() {
        if (annotationFileData.isEmpty()) {
            return;
        }
        FileUtil.createDir(Constant.DIR_ARCHIVE_TEMP + "批注文件/");
        for (int i = 0; i < annotationFileData.size(); i++) {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo item = annotationFileData.get(i);
            String fileName = item.getName().toStringUtf8();
            archiveInforms.add(new ArchiveInform(item.getMediaid(), "开始下载文件：" + fileName, "0%"));
            view.updateArchiveInform(archiveInforms);
            addTask(String.valueOf(item.getMediaid()));
            jni.creationFileDownload(Constant.DIR_ARCHIVE_TEMP + "批注文件/" + fileName, item.getMediaid(), 1, 0, Constant.ARCHIVE_DOWNLOAD_FILE);
        }
    }

    /**
     * 归档会议资料
     */
    private void archiveMeetData() {
        if (otherFileData.isEmpty()) {
            return;
        }
        FileUtil.createDir(Constant.DIR_ARCHIVE_TEMP + "其它文件/");
        for (int i = 0; i < otherFileData.size(); i++) {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo item = otherFileData.get(i);
            String fileName = item.getName().toStringUtf8();
            archiveInforms.add(new ArchiveInform(item.getMediaid(), "开始下载文件：" + fileName, "0%"));
            view.updateArchiveInform(archiveInforms);
            addTask(String.valueOf(item.getMediaid()));
            jni.creationFileDownload(Constant.DIR_ARCHIVE_TEMP + "其它文件/" + fileName, item.getMediaid(), 1, 0, Constant.ARCHIVE_DOWNLOAD_FILE);
        }
    }

    /**
     * 下载议程文件
     */
    private void downloadAgendaFile() {
        byte[] bytes = jni.queryFileProperty(InterfaceMacro.Pb_MeetFilePropertyID.Pb_MEETFILE_PROPERTY_NAME.getNumber(), agendaMediaId);
        InterfaceBase.pbui_CommonTextProperty textProperty = null;
        try {
            textProperty = InterfaceBase.pbui_CommonTextProperty.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        String fileName = textProperty.getPropertyval().toStringUtf8();
        LogUtil.i(TAG, "downloadAgendaFile 获取到文件议程 -->媒体id=" + agendaMediaId + ", 文件名=" + fileName);
        FileUtil.createDir(Constant.DIR_ARCHIVE_TEMP);
        File file = new File(Constant.DIR_ARCHIVE_TEMP + fileName);
        if (file.exists()) {
            if (Values.downloadingFiles.contains(agendaMediaId)) {
                view.showToast(R.string.currently_downloading);
            }
        } else {
            addTask(String.valueOf(agendaMediaId));
            jni.creationFileDownload(Constant.DIR_ARCHIVE_TEMP + fileName, agendaMediaId, 1, 0,
                    Constant.ARCHIVE_AGENDA_FILE);
        }
    }

    /**
     * 会议信息写入到文件中
     */
    private void meetInfo2file() {
        if (currentMeetInfo != null) {
            String content = "";
            content += "会议名称：" + currentMeetInfo.getName().toStringUtf8()
                    + "\n使用会场：" + currentRoomInfo.getName().toStringUtf8()
                    + "\n会场地址：" + currentRoomInfo.getAddr().toStringUtf8()
                    + "\n会议保密：" + (currentMeetInfo.getSecrecy() == 1 ? "是" : "否")
                    + "\n会议开始时间：" + DateUtil.millisecondFormatDetailedTime(currentMeetInfo.getStartTime() * 1000)
                    + "\n会议结束时间：" + DateUtil.millisecondFormatDetailedTime(currentMeetInfo.getEndTime() * 1000)
                    + "\n签到方式：" + Constant.getMeetSignInTypeName(currentMeetInfo.getSigninType())
                    + "\n会议管理员：" + (currentAdminInfo != null ? currentAdminInfo.getAdminname().toStringUtf8() : queryCurrentAdminName())
                    + "\n管理员描述：" + (currentAdminInfo != null ? currentAdminInfo.getComment().toStringUtf8() : "")
            ;
            read2file("会议基本信息.txt", content);
        }
    }

    /**
     * 将公告写入文件中
     */
    private void notice2file() {
        if (noticeData.isEmpty()) {
            return;
        }
        String content = "";
        for (int i = 0; i < noticeData.size(); i++) {
            InterfaceBullet.pbui_Item_BulletDetailInfo item = noticeData.get(i);
            content += "标题：" + item.getTitle().toStringUtf8() + "\n" + "内容：" + item.getContent().toStringUtf8() + "\n\n";
        }
        read2file("会议公告信息.txt", content);
    }

    /**
     * 将文本内容写入到文件中
     *
     * @param fileName 自定义的带后缀文件名
     * @param content  文本内容
     */
    private void read2file(String fileName, String content) {
        try {
            File file = new File(Constant.DIR_ARCHIVE_TEMP + fileName);
            FileUtil.createDir(Constant.DIR_ARCHIVE_TEMP);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos));
            bufferedWriter.write(content);
            bufferedWriter.close();
            if ("会议基本信息.txt".equals(fileName)) {
                archiveInforms.add(new ArchiveInform("会议基本信息导出完成", "100%"));
            } else if ("会议议程信息.txt".equals(fileName)) {
                archiveInforms.add(new ArchiveInform("会议议程信息导出完成", "100%"));
            } else if ("会议公告信息.txt".equals(fileName)) {
                archiveInforms.add(new ArchiveInform("会议公告信息导出完成", "100%"));
            }
            view.updateArchiveInform(archiveInforms);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
