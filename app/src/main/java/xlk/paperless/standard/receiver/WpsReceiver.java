package xlk.paperless.standard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.WpsModel;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.admin.AdminActivity;
import xlk.paperless.standard.view.meet.MeetingActivity;

import static xlk.paperless.standard.data.Constant.ANNOTATION_FILE_DIRECTORY_ID;

/**
 * @author xlk
 * @date 2020/4/26
 * @desc wps 文件处理的广播
 */
public class WpsReceiver extends BroadcastReceiver {
    private final String TAG = "WpsReceiver-->";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        switch (action) {
            //关闭文件时的广播
            case WpsModel.Reciver.ACTION_CLOSE:
                //通知注销掉WPS广播
                EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_WPS_RECEIVER).objects(false).build());
                String closeFile = intent.getStringExtra(WpsModel.ReciverExtra.CLOSEFILE);
                String thirdPackage1 = intent.getStringExtra(WpsModel.ReciverExtra.THIRDPACKAGE);
                LogUtil.e(TAG, "onReceive :  关闭文件收到广播 --> closeFile：" + closeFile + ", \n thirdPackage：" + thirdPackage1);
                jump2meet(context);
                break;
            //home键广播
            case WpsModel.Reciver.ACTION_HOME:
                //通知注销掉WPS广播
                EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_WPS_RECEIVER).objects(false).build());
                break;
            //保存文件时的广播
            case WpsModel.Reciver.ACTION_SAVE:
                String openFile = intent.getStringExtra(WpsModel.ReciverExtra.OPENFILE);
                String thirdPackage = intent.getStringExtra(WpsModel.ReciverExtra.THIRDPACKAGE);
                String savePath = intent.getStringExtra(WpsModel.ReciverExtra.SAVEPATH);
                LogUtil.e(TAG, "onReceive :  保存键广播 --> openfile： " + openFile + "\n thirdPackage：" + thirdPackage + "\n savePath：" + savePath);
                File file = new File(savePath);
                String fileName = file.getName();
                JniHandler.getInstance().uploadFile(0, ANNOTATION_FILE_DIRECTORY_ID, 0, fileName, savePath, 0, Constant.UPLOAD_WPS_FILE);
                break;
            default:
                break;
        }
    }


    private void jump2meet(Context context) {
        if (Values.isFromAdminOpenWps) {
            Intent intent = new Intent(context, AdminActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(context, MeetingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
