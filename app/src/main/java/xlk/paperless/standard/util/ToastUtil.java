package xlk.paperless.standard.util;

import android.content.Context;
import android.widget.Toast;

import com.mogujie.tt.protobuf.InterfaceMacro;

import xlk.paperless.standard.R;

import static xlk.paperless.standard.view.MyApplication.applicationContext;


/**
 * @author xlk
 * @date 2020/3/9
 * @desc 弹窗提示类
 * LENGTH_LONG = 3500; // 3.5 seconds
 * LENGTH_SHORT = 2000; // 2 seconds
 */
public class ToastUtil {

    private static Toast toast;
    private static long oneTime;

    public static void show(String msg) {
        try {
            LogUtil.d("ToastUtil", "showToast： " + msg);
            if (toast == null) {
                toast = Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT);
                toast.show();
                oneTime = System.currentTimeMillis();
            } else {
                if (System.currentTimeMillis() - oneTime >= 1500) {
                    toast.cancel();
                    toast = Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT);
                    toast.show();
                    oneTime = System.currentTimeMillis();
                } else {
                    toast.setText(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void show(int resid) {
        show(applicationContext.getResources().getString(resid));
    }

    public static void errorToast(int code) {
        String msg = "";
        switch (code) {
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_NONE_VALUE:
                msg = applicationContext.getString(R.string.error_0);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_EXPIRATION_VALUE:
                msg = applicationContext.getString(R.string.error_1);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_OPER_VALUE:
                msg = applicationContext.getString(R.string.error_2);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_ENTERPRISE_VALUE:
                msg = applicationContext.getString(R.string.error_3);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_NODEVICEID_VALUE:
                msg = applicationContext.getString(R.string.error_4);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_NOALLOWIN_VALUE:
                msg = applicationContext.getString(R.string.error_5);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_FILEERROR_VALUE:
                msg = applicationContext.getString(R.string.error_6);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_INVALID_VALUE:
                msg = applicationContext.getString(R.string.error_7);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_IDOCCUPY_VALUE:
                msg = applicationContext.getString(R.string.error_8);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_NOTBEING_VALUE:
                msg = applicationContext.getString(R.string.error_9);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_ONLYDEVICEID_VALUE:
                msg = applicationContext.getString(R.string.error_10);
                break;
            case InterfaceMacro.Pb_ValidateErrorCode.Pb_PARSER_ERROR_DEVICETYPENOMATCH_VALUE:
                msg = applicationContext.getString(R.string.error_11);
                break;
        }
        if (!msg.isEmpty()) {
            show(msg);
        }
    }
}
