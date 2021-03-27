package xlk.paperless.standard.view.admin.fragment.system.other;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceAdmin;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.view.admin.AdminActivity;

import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author Created by xlk on 2020/9/22.
 * @desc
 */
public class AdminOtherPresenter extends BasePresenter {
    private final WeakReference<Context> context;
    private final WeakReference<AdminOtherInterface> view;
    private InterfaceAdmin.pbui_Item_AdminDetailInfo localAdminInfo;
    public List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> updateFileData = new ArrayList<>();
    public List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> releaseFileData = new ArrayList<>();
    /**
     * 主界面信息
     */
    public List<MainInterfaceBean> mainInterfaceBeans = new ArrayList<>();
    /**
     * 投影界面信息
     */
    public List<MainInterfaceBean> projectiveInterfaceBeans = new ArrayList<>();
    /**
     * 公告界面信息
     */
    public List<MainInterfaceBean> noticeInterfaceBeans = new ArrayList<>();
    /**
     * 所有背景图片文件
     */
    public List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> pictureData = new ArrayList<>();
    private Timer timer;
    private TimerTask task;
    public List<InterfaceBase.pbui_Item_UrlDetailInfo> urlLists = new ArrayList<>();

    public AdminOtherPresenter(Context context, AdminOtherInterface view) {
        super();
        this.context = new WeakReference<Context>(context);
        this.view = new WeakReference<AdminOtherInterface>(view);
        queryInterFaceConfiguration();
        queryBgPicture();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.clear();
        view.clear();
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case Constant.BUS_MAIN_BG: {
                String filePath = (String) msg.getObjects()[0];
                view.get().updateMainBgImg(filePath);
                break;
            }
            case Constant.BUS_MAIN_LOGO: {
                String filePath = (String) msg.getObjects()[0];
                view.get().updateMainLogoImg(filePath);
                break;
            }
            case Constant.BUS_PROJECTIVE_BG: {
                String filePath = (String) msg.getObjects()[0];
                view.get().updateProjectiveBgImg(filePath);
                break;
            }
            case Constant.BUS_PROJECTIVE_LOGO: {
                String filePath = (String) msg.getObjects()[0];
                view.get().updateProjectiveLogoImg(filePath);
                break;
            }
            case Constant.BUS_NOTICE_BG: {
                String filePath = (String) msg.getObjects()[0];
                view.get().updateNoticeBgImg(filePath);
                break;
            }
            case Constant.BUS_NOTICE_LOGO: {
                String filePath = (String) msg.getObjects()[0];
                view.get().updateNoticeLogoImg(filePath);
                break;
            }
            //上传会议发布文件完毕
            case Constant.BUS_UPLOAD_RELEASE_FILE_FINISH: {
                LogUtil.i(TAG, "busEvent 上传会议发布文件完毕");
                queryReleaseFile();
                break;
            }
            //网页变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEFAULTURL_VALUE: {
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    byte[] bytes = (byte[]) msg.getObjects()[0];
                    InterfaceBase.pbui_meetUrl pbui_meetUrl = InterfaceBase.pbui_meetUrl.parseFrom(bytes);
                    int isetdefault = pbui_meetUrl.getIsetdefault();
                    List<InterfaceBase.pbui_Item_UrlDetailInfo> itemList = pbui_meetUrl.getItemList();
                    LogUtil.i(TAG, "BusEvent 网页变更通知: isetdefault=" + isetdefault + ",size=" + itemList.size());
                    for (int i = 0; i < itemList.size(); i++) {
                        InterfaceBase.pbui_Item_UrlDetailInfo item = itemList.get(i);
                        String name = item.getName().toStringUtf8();
                        String addr = item.getAddr().toStringUtf8();
                        int id = item.getId();
                        LogUtil.i(TAG, "BusEvent name=" + name + ",addr=" + addr + ",id=" + id);
                    }
                    webQuery();
                }
                break;
            }
            //界面配置变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETFACECONFIG_VALUE: {
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg info = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(bytes);
                int id = info.getId();
                int opermethod = info.getOpermethod();
                LogUtil.i(TAG, "BusEvent -->" + "界面配置变更通知 id=" + id + ", opermethod=" + opermethod);
                if (id == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COLTDTEXT_VALUE) {//公司名称
                    queryCompany();
                }
                executeLater();
                break;
            }
            //会议目录文件变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETDIRECTORYFILE_VALUE: {
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsgForDouble info = InterfaceBase.pbui_MeetNotifyMsgForDouble.parseFrom(bytes);
                int opermethod = info.getOpermethod();
                int id = info.getId();
                int subid = info.getSubid();
                LogUtil.i(TAG, "BusEvent 会议目录文件变更通知 id=" + id + ",subId=" + subid + ",opermethod=" + opermethod);
                queryUpdateFile();
                queryBgPicture();
                break;
            }
            //管理员变更通知
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_ADMIN_VALUE: {
                byte[] bytes = (byte[]) msg.getObjects()[0];
                InterfaceBase.pbui_MeetNotifyMsg info = InterfaceBase.pbui_MeetNotifyMsg.parseFrom(bytes);
                int id = info.getId();
                int opermethod = info.getOpermethod();
                LogUtil.i(TAG, "BusEvent -->" + "管理员变更通知 id=" + id + ", opermethod=" + opermethod);
                queryAdmin();
                break;
            }
            default:
                break;
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
        view.get().updatePictureRv();
    }

    private void executeLater() {
        //解决短时间内收到很多通知，查询很多次的问题
        if (timer == null) {
            timer = new Timer();
            LogUtil.i(TAG, "executeLater 创建timer");
            task = new TimerTask() {
                @Override
                public void run() {
                    queryInterFaceConfiguration();
                    task.cancel();
                    timer.cancel();
                    task = null;
                    timer = null;
                }
            };
            LogUtil.i(TAG, "executeLater 500毫秒之后查询");
            timer.schedule(task, 500);
        }
    }

    private boolean isMainInterface(int faceid) {
        return
                //logo图标
                faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_LOGO_GEO_VALUE
                        //会议名称
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEETNAME_VALUE
                        //参会人名称
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERNAME_VALUE
                        //参会人单位
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERCOMPANY_VALUE
                        //参会人职业
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERJOB_VALUE
                        //座席名称
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_SEATNAME_VALUE
                        //日期时间
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_TIMER_VALUE
                        //单位名称
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COMPANY_VALUE
                        //公司名称 onlyText
//                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COLTDTEXT_VALUE
                        //会议状态
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_topstatus_GEO_VALUE
                        //进入会议按钮
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_checkin_GEO_VALUE
                        //进入后台
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_manage_GEO_VALUE
                        //备注
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_remark_GEO_VALUE
                        //角色
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_role_GEO_VALUE
                        //版本
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_ver_GEO_VALUE
                ;
    }

    private boolean isProjectiveInterface(int faceid) {
        return
                //背景
//                faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_MIANBG_VALUE
                //logo
//                || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_LOGO_VALUE
                //会议名称
                faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_MEETNAME_VALUE
                        //坐席名称
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_SEATNAME_VALUE
                        //日期时间
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_TIMER_VALUE
                        //单位名称
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_COMPANY_VALUE
                        //公司名称
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_COMPANYNAME_VALUE
                        //会议状态
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_STATUS_VALUE
                        //应到
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_SIGN_ALL_VALUE
                        //已到
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_SIGN_IN_VALUE
                        //未到
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_SIGN_OUT_VALUE
                ;
    }

    private boolean isNoticeInterface(int faceid) {

        return
                //公告背景
//                faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinBK_VALUE
                //公告logo
//                || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinLogo_VALUE
                //公告标题
                faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinTitle_VALUE
                        //公告内容
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinContent_VALUE
                        //公告按钮
                        || faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinBtn_VALUE
                ;
    }

    public void queryInterFaceConfiguration() {
        try {
            InterfaceFaceconfig.pbui_Type_FaceConfigInfo info = jni.queryInterFaceConfiguration();
            mainInterfaceBeans.clear();
            projectiveInterfaceBeans.clear();
            noticeInterfaceBeans.clear();
            if (info != null) {
                List<InterfaceFaceconfig.pbui_Item_FaceTextItemInfo> textList = info.getTextList();
                List<InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo> onlytextList = info.getOnlytextList();
                List<InterfaceFaceconfig.pbui_Item_FacePictureItemInfo> pictureList = info.getPictureList();
                for (int i = 0; i < pictureList.size(); i++) {
                    InterfaceFaceconfig.pbui_Item_FacePictureItemInfo item = pictureList.get(i);
                    int faceid = item.getFaceid();
                    int flag = item.getFlag();
                    int mediaid = item.getMediaid();
                    LogUtil.i(TAG, "queryInterFaceConfiguration pictureList faceid=" + faceid + ",mediaid=" + mediaid);
                    String userStr = "";
                    //主界面背景
                    if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MAINBG_VALUE) {
                        userStr = Constant.MAIN_BG_PNG_TAG;
                        //logo图标
                    } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_LOGO_VALUE) {
                        userStr = Constant.MAIN_LOGO_PNG_TAG;
                        //投影界面背景图
                    } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_MIANBG_VALUE) {
                        userStr = Constant.PROJECTIVE_BG_PNG_TAG;
                        //投影界面logo图标
                    } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_LOGO_VALUE) {
                        userStr = Constant.PROJECTIVE_LOGO_PNG_TAG;
                        //公告背景图
                    } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinBK_VALUE) {
                        userStr = Constant.NOTICE_BG_PNG_TAG;
                        //公告logo图标
                    } else if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinLogo_VALUE) {
                        userStr = Constant.NOTICE_LOGO_PNG_TAG;
                    }
                    if (!TextUtils.isEmpty(userStr)) {
                        FileUtil.createDir(Constant.DIR_PICTURE);
                        jni.creationFileDownload(Constant.DIR_PICTURE + userStr + ".png", mediaid, 1, 0, userStr);
                    }
                    if (faceid == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_SHOWFILE_VALUE) {
                        String fileName = jni.queryFileNameByMediaId(mediaid);
                        LogUtil.i(TAG, "queryInterFaceConfiguration 会议发布文件名=" + fileName);
                        view.get().updateCurrentReleaseFileName(fileName);
                    }
//                    if (isMainInterface(faceid)) {
//                        mainInterfaceBeans.add(new MainInterfaceBean(faceid, flag));
//                    } else if (isNoticeInterface(faceid)) {
//                        noticeInterfaceBeans.add(new MainInterfaceBean(faceid, flag));
//                    } else if (isProjectiveInterface(faceid)) {
//                        projectiveInterfaceBeans.add(new MainInterfaceBean(faceid, flag));
//                    }
                }
//                for (int i = 0; i < onlytextList.size(); i++) {
//                    InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo item = onlytextList.get(i);
//                    int faceid = item.getFaceid();
//                    int flag = item.getFlag();
//                    if (isMainInterface(faceid)) {
//                        mainInterfaceBeans.add(new MainInterfaceBean(faceid, flag));
//                    }
//                }
                for (int i = 0; i < textList.size(); i++) {
                    InterfaceFaceconfig.pbui_Item_FaceTextItemInfo item = textList.get(i);
                    int faceid = item.getFaceid();
                    LogUtil.d(TAG, "queryInterFaceConfiguration textList faceid=" + faceid);
                    int flag = item.getFlag();
                    MainInterfaceBean bean = new MainInterfaceBean(faceid, flag);
                    bean.setAlign(item.getAlign());
                    bean.setColor(item.getColor());
                    bean.setFontFlag(item.getFontflag());
                    bean.setFontName(item.getFontname().toStringUtf8());
                    bean.setFontSize(item.getFontsize());
                    bean.setLx(item.getLx());
                    bean.setLy(item.getLy());
                    bean.setBx(item.getBx());
                    bean.setBy(item.getBy());
                    if (isMainInterface(faceid)) {
                        mainInterfaceBeans.add(bean);
                    } else if (isProjectiveInterface(faceid)) {
                        projectiveInterfaceBeans.add(bean);
                    } else if (isNoticeInterface(faceid)) {
                        noticeInterfaceBeans.add(bean);
                    }
                }
            }
            MainInterfaceBean proLogo = new MainInterfaceBean(InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_LOGO_VALUE, 1);
            proLogo.setAlign(4);
            proLogo.setColor(Color.rgb(250, 0, 0));
            proLogo.setFontFlag(0);
            proLogo.setFontName("");
            proLogo.setFontSize(20);
            proLogo.setLx(0);
            proLogo.setLy(0);
            proLogo.setBx(20);
            proLogo.setBy(15);
            projectiveInterfaceBeans.add(proLogo);
            MainInterfaceBean noticeLogo = new MainInterfaceBean(InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinLogo_VALUE, 1);
            noticeLogo.setAlign(4);
            noticeLogo.setColor(Color.rgb(250, 0, 0));
            noticeLogo.setFontFlag(0);
            noticeLogo.setFontName("");
            noticeLogo.setFontSize(20);
            noticeLogo.setLx(0);
            noticeLogo.setLy(0);
            noticeLogo.setBx(20);
            noticeLogo.setBy(15);
            noticeInterfaceBeans.add(noticeLogo);
            view.get().updateInterface(mainInterfaceBeans, projectiveInterfaceBeans, noticeInterfaceBeans);
            LogUtil.e(TAG, "queryInterFaceConfiguration mainInterfaceBeans.size=" + mainInterfaceBeans.size()
                    + ",noticeInterfaceBeans.size=" + noticeInterfaceBeans.size()
                    + ",projectiveInterfaceBeans.size=" + projectiveInterfaceBeans.size());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void webQuery() {
        try {
            InterfaceBase.pbui_meetUrl pbui_meetUrl = jni.queryUrl();
            urlLists.clear();
            if (pbui_meetUrl != null) {
                urlLists.addAll(pbui_meetUrl.getItemList());
            }
            view.get().updateUrl();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询公司名称
     */
    void queryCompany() {
        InterfaceFaceconfig.pbui_Type_FaceConfigInfo pbui_type_faceConfigInfo = jni.queryInterFaceConfigurationById(
                InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COLTDTEXT_VALUE);
        String company = "";
        if (pbui_type_faceConfigInfo != null) {
            List<InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo> onlytextList = pbui_type_faceConfigInfo.getOnlytextList();
            for (InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo item : onlytextList) {
                if (item.getFaceid() == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COLTDTEXT_VALUE) {
                    company = item.getText().toStringUtf8();
                    break;
                }
            }
        }
        view.get().updateCompany(company);
    }

    /**
     * 修改公司名
     */
    void modifyCompany(String company) {
        InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo build = InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo.newBuilder()
                .setFaceid(InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COLTDTEXT_VALUE)
                .setFlag(InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_ONLYTEXT_VALUE)
                .setText(s2b(company)).build();
        byte[] bytes = InterfaceFaceconfig.pbui_Type_FaceConfigInfo.newBuilder()
                .addOnlytext(build)
                .build().toByteArray();
        jni.modifyInterfaceConfig(bytes);
    }

    //查询会议发布文件
    void queryReleaseFile() {
        InterfaceFile.pbui_TypePageResQueryrFileInfo pbui_typePageResQueryrFileInfo = jni.queryFile(
                0, InterfaceMacro.Pb_MeetFileQueryFlag.Pb_MEET_FILETYPE_QUERYFLAG_ATTRIB_VALUE, 0, 0, 0,
                InterfaceMacro.Pb_MeetFileAttrib.Pb_MEETFILE_ATTRIB_PUBLISH_VALUE, 1, 0);
        //过滤掉媒体id小于0的文件，测试查看结果是：文档类和其它类的文件媒体id都<0
        releaseFileData.clear();
        if (pbui_typePageResQueryrFileInfo != null) {
            List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> itemList = pbui_typePageResQueryrFileInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceFile.pbui_Item_MeetDirFileDetailInfo item = itemList.get(i);
                String fileName = item.getName().toStringUtf8();
                if (FileUtil.isAudioAndVideoFile(fileName)) {
                    releaseFileData.add(item);
                }
            }
        }
        LogUtil.i(TAG, "queryReleaseFile releaseFileData.size=" + releaseFileData.size());
        view.get().updateReleaseFileRv();
    }

    void queryAdmin() {
        InterfaceAdmin.pbui_TypeAdminDetailInfo pbui_typeAdminDetailInfo = jni.queryAdmin();
        localAdminInfo = null;
        if (pbui_typeAdminDetailInfo != null) {
            List<InterfaceAdmin.pbui_Item_AdminDetailInfo> itemList = pbui_typeAdminDetailInfo.getItemList();
            for (int i = 0; i < itemList.size(); i++) {
                InterfaceAdmin.pbui_Item_AdminDetailInfo item = itemList.get(i);
                if (item.getAdminid() == AdminActivity.currentAdminId) {
                    LogUtil.i(TAG, "queryAdmin 获取登录的管理员信息 " + item.getAdminid() + "," + item.getAdminname().toStringUtf8());
                    localAdminInfo = item;
                    return;
                }
            }
        }
    }

    public void modifyAdminPassword(String oldPwd, String newPwd) {
        if (localAdminInfo == null) {
            LogUtil.e(TAG, "modifyAdminPassword 没有查找到本机的管理员信息");
            return;
        }
        String newMd5Pwd = ConvertUtil.md5(newPwd);
        String oldMd5Pwd = ConvertUtil.md5(oldPwd);
        LogUtil.i(TAG, "modifyAdminPassword newMd5Pwd=" + newMd5Pwd + ",oldMd5Pwd=" + oldMd5Pwd);
        jni.modifyAdminPwd(localAdminInfo.getAdminname().toStringUtf8(), newMd5Pwd, oldMd5Pwd);
    }

    /**
     * 查询升级文件
     */
    public void queryUpdateFile() {
        InterfaceFile.pbui_TypePageResQueryrFileInfo object = jni.queryFile(
                0, InterfaceMacro.Pb_MeetFileQueryFlag.Pb_MEET_FILETYPE_QUERYFLAG_ATTRIB_VALUE, 0, 0, 0,
                InterfaceMacro.Pb_MeetFileAttrib.Pb_MEETFILE_ATTRIB_DEVICEUPDATE_VALUE, 1, 0);
        updateFileData.clear();
        if (object != null) {
            updateFileData.addAll(object.getItemList());
        }
        LogUtil.i(TAG, "queryUpdateFile updateFileData.size=" + updateFileData.size());
        view.get().updateUpGradeFileRv();
    }

    public void saveMainBg(int mediaid) {
        if (mediaid != 0) {
            InterfaceFaceconfig.pbui_Item_FacePictureItemInfo build = InterfaceFaceconfig.pbui_Item_FacePictureItemInfo.newBuilder()
                    .setFaceid(InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MAINBG_VALUE)
                    .setMediaid(mediaid)
                    .build();
            byte[] bytes = InterfaceFaceconfig.pbui_Type_FaceConfigInfo.newBuilder()
                    .addPicture(build)
                    .build().toByteArray();
            jni.modifyInterfaceConfig(bytes);
        }
    }

    public void saveMainLogo(int mediaid) {
        InterfaceFaceconfig.pbui_Item_FacePictureItemInfo build = InterfaceFaceconfig.pbui_Item_FacePictureItemInfo.newBuilder()
                .setFaceid(InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_LOGO_VALUE)
                .setMediaid(mediaid)
                .build();
        byte[] bytes = InterfaceFaceconfig.pbui_Type_FaceConfigInfo.newBuilder()
                .addPicture(build)
                .build().toByteArray();
        jni.modifyInterfaceConfig(bytes);
    }

    public void saveProjectiveBg(int mediaid) {
        if (mediaid != 0) {
            InterfaceFaceconfig.pbui_Item_FacePictureItemInfo build = InterfaceFaceconfig.pbui_Item_FacePictureItemInfo.newBuilder()
                    .setFaceid(InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_MIANBG_VALUE)
                    .setMediaid(mediaid)
                    .build();
            byte[] bytes = InterfaceFaceconfig.pbui_Type_FaceConfigInfo.newBuilder()
                    .addPicture(build)
                    .build().toByteArray();
            jni.modifyInterfaceConfig(bytes);
        }
    }

    public void saveProjectiveLogo(int mediaid) {
        InterfaceFaceconfig.pbui_Item_FacePictureItemInfo build = InterfaceFaceconfig.pbui_Item_FacePictureItemInfo.newBuilder()
                .setFaceid(InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_LOGO_VALUE)
                .setMediaid(mediaid)
                .build();
        byte[] bytes = InterfaceFaceconfig.pbui_Type_FaceConfigInfo.newBuilder()
                .addPicture(build)
                .build().toByteArray();
        jni.modifyInterfaceConfig(bytes);
    }

    public void saveNoticeBg(int mediaid) {
        if (mediaid != 0) {
            InterfaceFaceconfig.pbui_Item_FacePictureItemInfo build = InterfaceFaceconfig.pbui_Item_FacePictureItemInfo.newBuilder()
                    .setFaceid(InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinBK_VALUE)
                    .setMediaid(mediaid)
                    .build();
            byte[] bytes = InterfaceFaceconfig.pbui_Type_FaceConfigInfo.newBuilder()
                    .addPicture(build)
                    .build().toByteArray();
            jni.modifyInterfaceConfig(bytes);
        }
    }

    public void saveNoticeLogo(int mediaid) {
        InterfaceFaceconfig.pbui_Item_FacePictureItemInfo build = InterfaceFaceconfig.pbui_Item_FacePictureItemInfo.newBuilder()
                .setFaceid(InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinLogo_VALUE)
                .setMediaid(mediaid)
                .build();
        byte[] bytes = InterfaceFaceconfig.pbui_Type_FaceConfigInfo.newBuilder()
                .addPicture(build)
                .build().toByteArray();
        jni.modifyInterfaceConfig(bytes);
    }

    /**
     * 保存会议发布文件
     *
     * @param mediaid =0取消，=其它为媒体id
     */
    public void saveReleaseFile(int mediaid) {
        InterfaceFaceconfig.pbui_Item_FacePictureItemInfo build = InterfaceFaceconfig.pbui_Item_FacePictureItemInfo.newBuilder()
                .setFaceid(InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_SHOWFILE_VALUE)
                .setFlag(InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE)
                .setMediaid(mediaid)
                .build();
        byte[] bytes = InterfaceFaceconfig.pbui_Type_FaceConfigInfo.newBuilder()
                .addPicture(build)
                .build().toByteArray();
        jni.modifyInterfaceConfig(bytes);
    }

    public void saveInterfaceConfig(List<MainInterfaceBean> data) {
        List<InterfaceFaceconfig.pbui_Item_FaceTextItemInfo> allText = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            MainInterfaceBean info = data.get(i);
            LogUtil.i(TAG, "saveInterfaceConfig 保存界面配置 item=" + info.toString());
            int faceId = info.getFaceId();
            if (faceId == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_LOGO_VALUE
                    || faceId == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinLogo_VALUE) {
                continue;
            }
            InterfaceFaceconfig.pbui_Item_FaceTextItemInfo build = InterfaceFaceconfig.pbui_Item_FaceTextItemInfo.newBuilder()
                    .setFaceid(faceId)
                    .setFlag(info.getFlag())
                    .setFontsize(info.getFontSize())
                    .setColor(info.getColor())
                    .setAlign(info.getAlign())
                    .setFontflag(info.getFontFlag())
                    .setFontname(s2b(info.getFontName()))
                    .setLx(info.getLx())
                    .setLy(info.getLy())
                    .setBx(info.getBx())
                    .setBy(info.getBy())
                    .build();
            allText.add(build);
        }
        byte[] bytes = InterfaceFaceconfig.pbui_Type_FaceConfigInfo.newBuilder()
                .addAllText(allText)
                .build().toByteArray();
        jni.modifyInterfaceConfig(bytes);
    }

}
