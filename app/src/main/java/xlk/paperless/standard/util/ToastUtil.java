package xlk.paperless.standard.util;

import android.content.Context;
import android.widget.Toast;

/**
 * @author xlk
 * @date 2020/3/9
 * @Description: 弹窗提示类
 */
public class ToastUtil {

    private static Toast toast = null;
    private static long oneTime;
    private static long twoTime;
    private static String oldMsg;

    public static void show(Context cxt, String msg) {
        if (toast == null) {
            toast = Toast.makeText(cxt.getApplicationContext(), msg, Toast.LENGTH_SHORT);
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

    public static void show(Context cxt, int resid) {
        show(cxt, cxt.getResources().getString(resid));
    }
}
