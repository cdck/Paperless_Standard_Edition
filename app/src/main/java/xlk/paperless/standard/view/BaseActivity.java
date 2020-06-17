package xlk.paperless.standard.view;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.receiver.NetWorkReceiver;
import xlk.paperless.standard.util.LogUtil;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc
 */
public class BaseActivity extends AppCompatActivity {

    private final String TAG = "BaseActivity-->";
    private NetWorkReceiver netWorkReceiver;
    protected JniHandler jni = JniHandler.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onCreate :   --->>> ");
        super.onCreate(savedInstanceState);
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
        LogUtil.i("BA_life", this.getClass().getSimpleName() + ".onNewIntent :   --->>> ");
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
//        register();
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onPause :   --->>> ");
//        unregister();
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
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onDestroy :   --->>> ");
        super.onDestroy();
    }
}
