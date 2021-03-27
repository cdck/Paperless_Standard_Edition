package xlk.paperless.standard.base;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UriUtils;
import com.google.protobuf.InvalidProtocolBufferException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.receiver.NetWorkReceiver;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc
 */
public class BaseActivity extends AppCompatActivity {

    protected final String TAG = getClass().getSimpleName() + "-->";
    private NetWorkReceiver netWorkReceiver;
    protected JniHandler jni = JniHandler.getInstance();
    private final int REQUEST_CODE_EXPORT_NOTE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onCreate :   --->>> " + this);
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(EventMessage msg) throws InvalidProtocolBufferException {
        BusEvent(msg);
        switch (msg.getType()) {
            case Constant.BUS_CHOOSE_NOTE_FILE: {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_EXPORT_NOTE);
                break;
            }
            default:
                break;
        }
    }

    protected void BusEvent(EventMessage msg) throws InvalidProtocolBufferException {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EXPORT_NOTE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            File file = UriUtils.uri2File(uri);
            if (file != null) {
                if (file.getName().endsWith(".txt")) {
                    String content = FileIOUtils.readFile2String(file);
                    EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_EXPORT_NOTE_CONTENT).objects(content).build());
                } else {
                    ToastUtils.showShort(R.string.please_choose_txt_file);
                }
            }
        }
    }

    private void register() {
        LogUtil.d(TAG, "register -->" + "注册网络监听广播 " + this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netWorkReceiver = new NetWorkReceiver();
        registerReceiver(netWorkReceiver, filter);
    }

    private void unregister() {
        LogUtil.d(TAG, "unregister -->" + "反注册网络监听广播 " + this);
        unregisterReceiver(netWorkReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onNewIntent :   --->>> ");
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onStart :   --->>> ");
        super.onStart();
        register();
    }

    @Override
    protected void onResume() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onResume :   --->>> ");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onPause :   --->>> ");
        super.onPause();
    }

    @Override
    protected void onStop() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onStop :   --->>> ");
        super.onStop();
        unregister();
    }

    @Override
    protected void onRestart() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onRestart :   --->>> ");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onDestroy :   --->>> " + this);
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
