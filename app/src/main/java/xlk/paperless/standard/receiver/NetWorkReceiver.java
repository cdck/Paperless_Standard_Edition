package xlk.paperless.standard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.greenrobot.eventbus.EventBus;

import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.MyApplication;

/**
 * @author xlk
 * @date 2020/4/25
 * @Description: 网络状态变更广播接收者
 */
public class NetWorkReceiver extends BroadcastReceiver {
    private final String TAG = "NetWorkReceiver-->";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            LogUtil.d(TAG, "网络状态改变");
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                String name = info.getTypeName();
                LogUtil.d(TAG, "当前网络名称：" + name);
            } else {
                LogUtil.d(TAG, "没有可用网络");
            }
            EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_NET_WORK).build());
        }
    }
}
