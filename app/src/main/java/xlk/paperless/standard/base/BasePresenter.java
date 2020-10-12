package xlk.paperless.standard.base;

import com.google.protobuf.InvalidProtocolBufferException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;

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
     * EventBus发送的消息交给子类去处理
     * @param msg 消息数据
     * @throws InvalidProtocolBufferException byte数组转指定结构体时的异常，避免子类中一直try catch
     */
    protected abstract void busEvent(EventMessage msg) throws InvalidProtocolBufferException;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(EventMessage msg) throws InvalidProtocolBufferException {
        busEvent(msg);
    }
}
