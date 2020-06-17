package xlk.paperless.standard.util;

import android.widget.Toast;

import static xlk.paperless.standard.view.MyApplication.mContext;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc 弹窗提示类
 */
public class ToastUtil {

    private static Toast toast = null;
    private static long oneTime;
    private static long twoTime;
    private static String oldMsg;

    public static void show(String msg) {
        if (toast == null) {
            toast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (msg.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = msg;
                toast.setText(msg);
                toast.show();
            }
        }
        oneTime = twoTime;
    }

    public static void show(int resid) {
        show(mContext.getResources().getString(resid));
    }
}
