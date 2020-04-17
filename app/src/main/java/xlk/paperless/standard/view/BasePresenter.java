package xlk.paperless.standard.view;

import android.os.IBinder;

import com.google.protobuf.InvalidProtocolBufferException;

import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;

/**
 * @author xlk
 * @date 2020/3/9
 * @Description:
 */
public class BasePresenter {
    protected JniHandler jni = JniHandler.getInstance();

    public void register() {

    }


    public void unregister() {

    }


    public void BusEvent(EventMessage msg) throws InvalidProtocolBufferException {

    }
}
