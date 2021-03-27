package xlk.paperless.standard.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import androidx.annotation.NonNull;

import android.view.Window;
import android.view.WindowManager;

/**
 * @author xlk
 * @date 2020/4/14
 * @desc
 */
public class DialogUtil {
    /**
     * 某些android版本无效问题，
     * 也解决了dialog中点击EditText，软键盘弹出时遮挡住dialog的问题
     *
     * @param window
     * 需要悬浮窗权限 SYSTEM_ALERT_WINDOW
     */
    private static void setParamsType(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0新特性
            window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.setType(WindowManager.LayoutParams.TYPE_PHONE);
        } else {
            window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
    }

    public static AlertDialog createDialog(Context cxt, int title, int positive, int negative, @NonNull onDialogClickListener listener) {
        return createDialog(cxt, cxt.getString(title), cxt.getString(positive), cxt.getString(negative), listener);
    }

    public static AlertDialog createDialog(Context cxt, String title, String positive, String negative, @NonNull onDialogClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
        builder.setTitle(title);
        builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.positive(dialog);
            }
        });
        builder.setNegativeButton(negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.negative(dialog);
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                listener.dismiss(dialog);
            }
        });
        AlertDialog dialog = builder.create();
        setParamsType(dialog.getWindow());
        dialog.setCanceledOnTouchOutside(false);//点击外部不消失
        dialog.setCancelable(false);//用户点击返回键使其无效
        dialog.show();//这行代码要在设置宽高的前面，宽高才有用
        return dialog;
    }

    public interface onDialogClickListener {
        void positive(DialogInterface dialog);

        void negative(DialogInterface dialog);

        void dismiss(DialogInterface dialog);
    }
}
