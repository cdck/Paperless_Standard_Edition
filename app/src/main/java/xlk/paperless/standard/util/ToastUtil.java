package xlk.paperless.standard.util;

import android.content.Context;
import android.text.TextUtils;
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

    public static void show(int resid, Object... values) {
        show(applicationContext.getResources().getString(resid, values));
    }

    /**
     * 平台登录验证返回 type=58
     *
     * @param code 参见 InterfaceMacro.Pb_ValidateErrorCode
     */
    public static void errorToast(int code) {
        String msg;
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
            default:
                msg = "";
                break;
        }
        if (!msg.isEmpty()) {
            show(msg);
        }
    }

    /**
     * 平台初始化完毕 type=2 method=10
     *
     * @param errcode 参见 InterfaceMacro.Pb_WalletSystem_ErrorCode
     */
    public static void loginError(int errcode) {
        String msg;
        switch (errcode) {
//            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_NONE_VALUE:
//                msg = applicationContext.getString(R.string.login_error_0);
//                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_NOTBEING_VALUE:
                msg = applicationContext.getString(R.string.login_error_1);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_NOTONLINE_VALUE:
                msg = applicationContext.getString(R.string.login_error_2);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_NOSERVER_VALUE:
                msg = applicationContext.getString(R.string.login_error_3);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_DENIAL_VALUE:
                msg = applicationContext.getString(R.string.login_error_4);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_PASSWORD_VALUE:
                msg = applicationContext.getString(R.string.login_error_5);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_FORMAT_VALUE:
                msg = applicationContext.getString(R.string.login_error_6);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_NOPOWER_VALUE:
                msg = applicationContext.getString(R.string.login_error_7);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_ISBEING_VALUE:
                msg = applicationContext.getString(R.string.login_error_8);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_UPDATEVER_VALUE:
                msg = applicationContext.getString(R.string.login_error_9);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_DBOFFLINE_VALUE:
                msg = applicationContext.getString(R.string.login_error_10);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_NOTCONNTECT_VALUE:
                msg = applicationContext.getString(R.string.login_error_11);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_NORES_VALUE:
                msg = applicationContext.getString(R.string.login_error_12);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_TIMEOUT_VALUE:
                msg = applicationContext.getString(R.string.login_error_13);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_ZERO_VALUE:
                msg = applicationContext.getString(R.string.login_error_14);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_SERVERERROR_VALUE:
                msg = applicationContext.getString(R.string.login_error_15);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_NOSPACE_VALUE:
                msg = applicationContext.getString(R.string.login_error_16);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_DBOPERERROR_VALUE:
                msg = applicationContext.getString(R.string.login_error_17);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_NOTIDENTITY_VALUE:
                msg = applicationContext.getString(R.string.login_error_18);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_MAXDEVICENUM_VALUE:
                msg = applicationContext.getString(R.string.login_error_19);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_MAXERRORTIMES_VALUE:
                msg = applicationContext.getString(R.string.login_error_20);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_PARSEERROR_VALUE:
                msg = applicationContext.getString(R.string.login_error_21);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_PROTOCALNOMATCH_VALUE:
                msg = applicationContext.getString(R.string.login_error_22);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_EXPIRATIONTIME_VALUE:
                msg = applicationContext.getString(R.string.login_error_23);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_COMMVERNOMATCH_VALUE:
                msg = applicationContext.getString(R.string.login_error_24);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_OVERRUN_VALUE:
                msg = applicationContext.getString(R.string.login_error_25);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_NOTOPEN_VALUE:
                msg = applicationContext.getString(R.string.login_error_26);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETURN_ERROR_FORCEDSTOP_VALUE:
                msg = applicationContext.getString(R.string.login_error_27);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETUNR_ERROR_LOGONERROR_VALUE:
                msg = applicationContext.getString(R.string.login_error_28);
                break;
            case InterfaceMacro.Pb_WalletSystem_ErrorCode.Pb_RETUNR_ERROR_MAXONLINENUM_VALUE:
                msg = applicationContext.getString(R.string.login_error_29);
                break;
            default:
                msg = "";
                break;
        }
        if (!TextUtils.isEmpty(msg)) {
            show(msg);
        }
    }
}
