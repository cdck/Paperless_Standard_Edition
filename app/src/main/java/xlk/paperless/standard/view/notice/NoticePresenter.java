package xlk.paperless.standard.view.notice;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBullet;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMacro;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.BasePresenter;

/**
 * @author xlk
 * @date 2020/4/8
 * @Description:
 */
public class NoticePresenter extends BasePresenter {
    private final String TAG = "NoticePresenter-->";
    private final INotice view;
    private final Context cxt;

    public NoticePresenter(Context context, INotice view) {
        this.cxt = context;
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
    public void BusEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case Constant.BUS_NOTICE_BG:
                String filepath = (String) msg.getObjs()[0];
                Drawable drawable = Drawable.createFromPath(filepath);
                view.updateNoticeBg(drawable);
                break;
            case Constant.BUS_NOTICE_LOGO:
                String filepath1 = (String) msg.getObjs()[0];
                Drawable drawable1 = Drawable.createFromPath(filepath1);
                view.updateNoticeLogo(drawable1);
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET_VALUE://公告
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_STOP_VALUE) {
                    LogUtil.d(TAG, "BusEvent -->" + "停止公告通知");
                    view.clearAll();
                }
                break;
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETFACECONFIG_VALUE://界面配置变更
                LogUtil.d(TAG, "BusEvent -->" + "界面配置变更通知");
                queryInterfaceConfig();
                break;

        }
    }

    public void queryInterfaceConfig() {
        try {
            InterfaceFaceconfig.pbui_Type_FaceConfigInfo pbui_type_faceConfigInfo = jni.queryInterFaceConfiguration();
            if (pbui_type_faceConfigInfo == null) {
                return;
            }
            List<InterfaceFaceconfig.pbui_Item_FaceOnlyTextItemInfo> onlytextList = pbui_type_faceConfigInfo.getOnlytextList();
            List<InterfaceFaceconfig.pbui_Item_FacePictureItemInfo> pictureList = pbui_type_faceConfigInfo.getPictureList();
            List<InterfaceFaceconfig.pbui_Item_FaceTextItemInfo> textList = pbui_type_faceConfigInfo.getTextList();
            for (int i = 0; i < pictureList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FacePictureItemInfo item = pictureList.get(i);
                int faceid = item.getFaceid();
                int flag = item.getFlag();
                int mediaid = item.getMediaid();
                String userStr = "";
                if (mediaid != 0) {
                    if (faceid == 26) {//公告logo
                        userStr = Constant.NOTICE_LOGO_PNG_TAG;
                    } else if (faceid == 25) {//公告背景图
                        userStr = Constant.NOTICE_BG_PNG_TAG;
                    }
                }
                if (!userStr.isEmpty()) {
                    FileUtil.createDir(Constant.ROOT_DIR);
                    jni.creationFileDownload(Constant.ROOT_DIR + userStr + ".png", mediaid, 1, 0, userStr);
                }
            }
            for (int i = 0; i < textList.size(); i++) {
                InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info = textList.get(i);
                int faceid = info.getFaceid();
                if (faceid == 29) {//公告关闭按钮
                    LogUtil.e(TAG, "update :  公告关闭按钮 --> ");
                    view.updateBtn(R.id.notice_ac_close_btn, info);
                } else if (faceid == 28) {//公告内容
                    LogUtil.e(TAG, "update :  公告内容 --> ");
                    view.updateTv(R.id.notice_ac_content_tv, info);
                } else if (faceid == 27) {//公告标题
                    LogUtil.e(TAG, "update :  公告标题 --> ");
                    view.updateTv(R.id.notice_ac_title_tv, info);
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void queryAssignNotice(int bulletid) {
        try {
            InterfaceBullet.pbui_BulletDetailInfo notice = jni.queryAssignNotice(bulletid);
            if (notice == null) {
                return;
            }
            List<InterfaceBullet.pbui_Item_BulletDetailInfo> itemList = notice.getItemList();
            if (!itemList.isEmpty()) {
                InterfaceBullet.pbui_Item_BulletDetailInfo info = itemList.get(0);
                view.updateText(info);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
