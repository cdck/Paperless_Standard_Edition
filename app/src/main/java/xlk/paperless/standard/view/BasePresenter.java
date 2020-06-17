package xlk.paperless.standard.view;

import android.content.Context;
import android.os.IBinder;

import com.google.protobuf.InvalidProtocolBufferException;

import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.ui.CustomInterface.BaseInterface;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc
 */
public class BasePresenter {
    protected JniHandler jni = JniHandler.getInstance();

    public void register() {

    }


    public void unregister() {

    }

    public void onDestroy(){

    }

    public void BusEvent(EventMessage msg) throws InvalidProtocolBufferException {

    }
}
