package xlk.paperless.standard.base;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mogujie.tt.protobuf.InterfaceContext;
import com.mogujie.tt.protobuf.InterfaceMacro;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc Presenter父类
 */
public abstract class BasePresenter {
    protected String TAG = this.getClass().getSimpleName() + "-->";
    protected JniHandler jni = JniHandler.getInstance();

    public BasePresenter() {
        register();
    }

    public void register() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void unregister() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void onDestroy() {
        unregister();
    }

    /**
     * 查询当前的管理员id
     */
    protected int queryCurrentAdminId() {
        InterfaceContext.pbui_MeetContextInfo info = jni.queryContextProperty(
                InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_CURADMINID_VALUE);
        int propertyval = info.getPropertyval();
        LogUtil.i(TAG, "queryCurrentAdminId 当前登录的管理员id=" + propertyval);
        return propertyval;
    }

    /**
     * 查询当前登录的管理员名称
     */
    protected String queryCurrentAdminName() {
        InterfaceContext.pbui_MeetContextInfo info = jni.queryContextProperty(
                InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_CURADMINNAME_VALUE);
        String name = info.getPropertytext().toStringUtf8();
        LogUtil.i(TAG, "queryCurrentAdminName 当前登录的管理员名称=" + name);
        return name;
    }

    /**
     * 获取当前会议的会议id
     *
     * @return 会议id
     */
    protected int queryCurrentMeetId() {
        InterfaceContext.pbui_MeetContextInfo info = jni.queryContextProperty(
                InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_CURMEETINGID_VALUE);
        int propertyval = info.getPropertyval();
        LogUtil.i(TAG, "queryCurrentRoomId 当前会议的会议id=" + propertyval);
        return propertyval;
    }

    /**
     * 获取当前会议的会场id
     *
     * @return 会场id
     */
    protected int queryCurrentRoomId() {
        InterfaceContext.pbui_MeetContextInfo info = jni.queryContextProperty(
                InterfaceMacro.Pb_ContextPropertyID.Pb_MEETCONTEXT_PROPERTY_CURROOMID_VALUE);
        int propertyval = info.getPropertyval();
        LogUtil.i(TAG, "queryCurrentRoomId 当前会议的会场id=" + propertyval);
        return propertyval;
    }

    /**
     * EventBus发送的消息交给子类去处理
     *
     * @param msg 消息数据
     * @throws InvalidProtocolBufferException byte数组转指定结构体时的异常，避免子类中一直try catch
     */
    protected abstract void busEvent(EventMessage msg) throws InvalidProtocolBufferException;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(EventMessage msg) throws InvalidProtocolBufferException {
        busEvent(msg);
    }

}
