package xlk.paperless.standard.view.fragment.other.bulletin;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBullet;
import com.mogujie.tt.protobuf.InterfaceMacro;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.BasePresenter;


/**
 * @author xlk
 * @date 2020/4/8
 * @Description:
 */
public class BulletinPresenter extends BasePresenter {
    private final String TAG = "NoticePresenter-->";
    private final IBulletin view;
    private final Context cxt;
    public List<InterfaceBullet.pbui_Item_BulletDetailInfo> bulletInfos = new ArrayList<>();

    public BulletinPresenter(Context context, IBulletin view) {
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

    public void queryNotice() {
        try {
            InterfaceBullet.pbui_BulletDetailInfo detailInfo = jni.queryNotice();
            if (detailInfo == null) {
                return;
            }
            bulletInfos.clear();
            bulletInfos.addAll(detailInfo.getItemList());
            view.notifyAdapter();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void BusEvent(EventMessage msg) {
        switch (msg.getType()) {
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_MEETBULLET_VALUE:
                if (msg.getMethod() == InterfaceMacro.Pb_Method.Pb_METHOD_MEET_INTERFACE_NOTIFY_VALUE) {
                    LogUtil.d(TAG, "BusEvent -->" + "公告变更通知");
                    queryNotice();
                }
                break;
        }
    }
}
