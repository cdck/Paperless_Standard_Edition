package xlk.paperless.standard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;

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
        LogUtils.e("wps广播="+action);
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
                EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_WPS_RECEIVER).objects(false).build());
                String openFile = intent.getStringExtra(WpsModel.ReciverExtra.OPENFILE);
                String thirdPackage = intent.getStringExtra(WpsModel.ReciverExtra.THIRDPACKAGE);
                String savePath = intent.getStringExtra(WpsModel.ReciverExtra.SAVEPATH);
                LogUtils.e(TAG, "onReceive :  保存键广播 --> openfile： " + openFile + "\n thirdPackage：" + thirdPackage + "\n savePath：" + savePath);
                File file = new File(savePath);
                String fileName = file.getName();
                File fileByPath = FileUtils.getFileByPath(savePath);
                if(!fileByPath.getParent().endsWith(".recovery")) {
                    JniHandler.getInstance().uploadFile(0, ANNOTATION_FILE_DIRECTORY_ID, 0, fileName, savePath, 0, Constant.UPLOAD_WPS_FILE);
                }
                jump2meet(context);
                break;
            default:
                break;
        }
    }


    private void jump2meet(Context context) {
        LogUtils.i("jump2meet Values.isFromAdminOpenWps="+Values.isFromAdminOpenWps);
        if (Values.isFromAdminOpenWps) {
            Intent intent = new Intent(context, AdminActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
//            ActivityUtils.startActivity(MeetingActivity.class);
            Intent intent = new Intent(context, MeetingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
