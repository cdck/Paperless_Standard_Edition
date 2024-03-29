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

/**
 * @author xlk
 * @date 2020/4/25
 * @desc 网络状态变更广播接收者
 */
public class NetWorkReceiver extends BroadcastReceiver {
    private final String TAG = "NetWorkReceiver-->";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            LogUtil.d(TAG, "网络状态改变");
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            int isAvailable;
            if (info != null && info.isAvailable()) {
                String name = info.getTypeName();
                isAvailable = 1;
                LogUtil.d(TAG, "当前网络名称：" + name);
            } else {
                isAvailable = 0;
                LogUtil.d(TAG, "没有可用网络");
            }
            EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_NET_WORK).objects(isAvailable).build());
        }
    }
}
