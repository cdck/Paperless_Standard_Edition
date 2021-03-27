package xlk.paperless.standard.view.fragment.web;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceBase;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.base.BasePresenter;

/**
 * @author xlk
 * @date 2020/3/20
 * @desc
 */
public class MeetWebPresenter extends BasePresenter {
    private final IMeetWeb view;
    private final Context cxt;
    public List<InterfaceBase.pbui_Item_UrlDetailInfo> urlLists =new ArrayList<>();

    public MeetWebPresenter(Context cxt, IMeetWeb view) {
        super();
        this.cxt = cxt;
        this.view = view;
    }

    @Override
    public void busEvent(EventMessage msg) throws InvalidProtocolBufferException {
        switch (msg.getType()) {
            case InterfaceMacro.Pb_Type.Pb_TYPE_MEET_INTERFACE_DEFAULTURL_VALUE:
                webQuery();
                break;
            default:
                break;
        }
    }

    public void webQuery() {
        try {
            InterfaceBase.pbui_meetUrl meetUrl = jni.queryUrl();
            urlLists.clear();
            if (meetUrl != null){
                urlLists.addAll(meetUrl.getItemList());
            }
            view.updateUrlRv();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
