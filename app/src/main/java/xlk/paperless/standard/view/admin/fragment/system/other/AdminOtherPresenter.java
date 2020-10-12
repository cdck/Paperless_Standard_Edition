package xlk.paperless.standard.view.admin.fragment.system.other;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.lang.ref.WeakReference;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.base.BasePresenter;

/**
 * @author Created by xlk on 2020/9/22.
 * @desc
 */
public class AdminOtherPresenter extends BasePresenter {
    private final WeakReference<Context> context;
    private final WeakReference<AdminOtherInterface> view;
    private InterfaceBase.pbui_Item_UrlDetailInfo currentWebUrl;

    public AdminOtherPresenter(Context context, AdminOtherInterface view) {
        super();
        this.context = new WeakReference<Context>(context);
        this.view = new WeakReference<AdminOtherInterface>(view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.clear();
        view.clear();
    }

    public void webQuery() {
        try {
            InterfaceBase.pbui_meetUrl pbui_meetUrl = jni.webQuery();
            if (pbui_meetUrl != null) {
                currentWebUrl = pbui_meetUrl.getItemList().get(0);
                LogUtil.i(TAG, "webQuery 获取的网址信息：id=" + currentWebUrl.getId() + ",name=" + currentWebUrl.getName().toStringUtf8() + ",addr=" +
                        currentWebUrl.getAddr().toStringUtf8());
                String addr = currentWebUrl.getAddr().toStringUtf8();
                view.get().updateUrl(addr);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void modifyUrl(String currentUrl) {
        InterfaceBase.pbui_Item_UrlDetailInfo build = InterfaceBase.pbui_Item_UrlDetailInfo.newBuilder()
                .setId(currentWebUrl.getId())
                .setAddr(ConvertUtil.s2b(currentUrl))
                .setName(currentWebUrl.getName())
                .build();
        jni.modifyWebUrl(1, build);
    }

    //查询公司名称
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
                .setText(ConvertUtil.s2b(company)).build();
        byte[] bytes = InterfaceFaceconfig.pbui_Type_FaceConfigInfo.newBuilder()
                .addOnlytext(build)
                .build().toByteArray();
        jni.modifyInterfaceConfig(bytes);
    }

    //查询会议发布文件
    void queryReleaseFile() {
        InterfaceFile.pbui_TypePageResQueryrFileInfo pbui_typePageResQueryrFileInfo = jni.queryFile(
                0, 0, 0, 0, 0,
                InterfaceMacro.Pb_MeetFileAttrib.Pb_MEETFILE_ATTRIB_PUBLISH_VALUE, 0, 0);
        if (pbui_typePageResQueryrFileInfo != null) {
            List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> itemList = pbui_typePageResQueryrFileInfo.getItemList();
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo item = itemList.get(0);
            LogUtil.i(TAG, "queryReleaseFile name=" + item.getName().toStringUtf8() + ",attrib=" + item.getAttrib());
            view.get().updateReleaseFile(item.getName().toStringUtf8());
        } else {
            view.get().updateReleaseFile(context.get().getString(R.string.undefined));
        }
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
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
                    if (isetdefault == 0) {//当前会议

                    } else {//系统全局

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
                } else if (id == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_SHOWFILE_VALUE) {//开会预读文件、视频等
                    queryReleaseFile();
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
                }
                break;
            }
        }
    }
}
