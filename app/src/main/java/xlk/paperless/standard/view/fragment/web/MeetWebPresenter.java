package xlk.paperless.standard.view.fragment.web;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceMacro;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.base.BasePresenter;

/**
 * @author xlk
 * @date 2020/3/20
 * @desc
 */
public class MeetWebPresenter extends BasePresenter {
    private final IMeetWeb view;
    private final Context cxt;

    public MeetWebPresenter(Context cxt, IMeetWeb view) {
        super();
        this.cxt = cxt;
        this.view = view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEFAULTURL_VALUE:
                webQuery();
                break;
        }
    }

    public void webQuery() {
        try {
            InterfaceBase.pbui_meetUrl meetUrl = jni.webQuery();
            if (meetUrl == null) return;
            InterfaceBase.pbui_Item_UrlDetailInfo info = meetUrl.getItemList().get(0);
            String urlAddr = info.getAddr().toStringUtf8();
            view.loadUrl(urlAddr);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
