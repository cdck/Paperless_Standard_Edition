package xlk.paperless.standard.util;

import android.util.Log;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc 日志打印工具类
 */
public class LogUtil {
    private static final boolean log_enable = true;
    private static final int log_level_e = 0;
    private static final int log_level_w = 1;
    private static final int log_level_i = 2;
    private static final int log_level_d = 3;
    private static final int log_level_v = 4;
    private static final int log_current_level = 4;
    private static final String TAG = "xlk_log";

    public static void d(String tag, String msg) {
        if (log_enable && log_current_level >= log_level_d) {
            Log.d(TAG, tag + ">" + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (log_enable && log_current_level >= log_level_i) {
            Log.i(TAG, tag + ">" + msg);
        }
    }

    public static void e(String tag, String msg) {
        if (log_enable && log_current_level >= log_level_e) {
            Log.e(TAG, tag + ">" + msg);
        }
    }

    public static void v(String tag, String msg) {
        if (log_enable && log_current_level >= log_level_v) {
            Log.v(TAG, tag + ">" + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (log_enable && log_current_level >= log_level_w) {
            Log.w(TAG, tag + ">" + msg);
        }
    }
}
