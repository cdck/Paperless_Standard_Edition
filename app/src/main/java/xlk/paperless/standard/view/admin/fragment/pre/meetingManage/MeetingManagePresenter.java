package xlk.paperless.standard.view.admin.fragment.pre.meetingManage;

import android.content.Context;

import com.google.protobuf.InvalidProtocolBufferException;

import xlk.paperless.standard.base.BasePresenter;
import xlk.paperless.standard.data.EventMessage;

/**
 * @author Created by xlk on 2020/10/15.
 * @desc
 */
public class MeetingManagePresenter extends BasePresenter {
    private final Context context;
    private final MeetingManageInterface view;

    public MeetingManagePresenter(Context context, MeetingManageInterface view) {
        super();
        this.context = context;
        this.view = view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void busEvent(EventMessage msg) throws InvalidProtocolBufferException {

    }
}
