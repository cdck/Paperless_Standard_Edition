package xlk.paperless.standard.base;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UriUtils;
import com.google.protobuf.InvalidProtocolBufferException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.receiver.NetWorkReceiver;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.UDiskUtil;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc
 */
public class BaseActivity extends AppCompatActivity {

    protected final String TAG = getClass().getSimpleName() + "-->";
    private NetWorkReceiver netWorkReceiver;
    protected JniHandler jni = JniHandler.getInstance();
    private final int REQUEST_CODE_EXPORT_NOTE = 1;
    private final int REQUEST_CODE_OPEN_UDISK = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onCreate :   --->>> " + this);
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMessage(EventMessage msg) throws InvalidProtocolBufferException {
        BusEvent(msg);
        switch (msg.getType()) {
            case Constant.BUS_CHOOSE_NOTE_FILE: {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_EXPORT_NOTE);
                break;
            }
            case Constant.BUS_OPEN_UDISK: {
                String uDiskPath = UDiskUtil.getUDiskPath1(this);
                LogUtils.e("收到通知打开U盘 uDiskPath=" + uDiskPath);
                if (uDiskPath.isEmpty()) {
                    Toast.makeText(this, R.string.please_insert_udisk_first, Toast.LENGTH_LONG).show();
                    break;
                }
                File file = new File(uDiskPath);
//                File file = new File(Constant.DIR_FILES);
                Uri uri = UriUtils.file2Uri(file);
                LogUtils.i("uri=" + uri);
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                //如果当前Context对象是Activity 一定不能添加下面这行
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(uri, "*/*");//无类型限制
                startActivityForResult(intent, REQUEST_CODE_OPEN_UDISK);
                break;
            }
            default:
                break;
        }
    }

    protected void BusEvent(EventMessage msg) throws InvalidProtocolBufferException {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_EXPORT_NOTE) {
                Uri uri = data.getData();
                File file = UriUtils.uri2File(uri);
                if (file != null) {
                    if (file.getName().endsWith(".txt")) {
                        String content = FileIOUtils.readFile2String(file);
                        EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_EXPORT_NOTE_CONTENT).objects(content).build());
                    } else {
                        ToastUtils.showShort(R.string.please_choose_txt_file);
                    }
                }
            } else if (requestCode == REQUEST_CODE_OPEN_UDISK) {
                Uri uri = data.getData();
                File file = UriUtils.uri2File(uri);
                if (file != null) {
                    LogUtils.e("选择的U盘文件=" + file.getAbsolutePath());
                    FileUtil.openLocalFile(this, file);
                } else {
                    LogUtils.e("选择的U盘文件为null");
                }
            }
        }
    }

    private void register() {
        LogUtil.d(TAG, "register -->" + "注册网络监听广播 " + this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netWorkReceiver = new NetWorkReceiver();
        registerReceiver(netWorkReceiver, filter);
    }

    private void unregister() {
        LogUtil.d(TAG, "unregister -->" + "反注册网络监听广播 " + this);
        unregisterReceiver(netWorkReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onNewIntent :   --->>> ");
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onStart :   --->>> ");
        super.onStart();
        register();
    }

    @Override
    protected void onResume() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onResume :   --->>> ");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onPause :   --->>> ");
        super.onPause();
    }

    @Override
    protected void onStop() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onStop :   --->>> ");
        super.onStop();
        unregister();
    }

    @Override
    protected void onRestart() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onRestart :   --->>> ");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        LogUtil.i("A_life", this.getClass().getSimpleName() + ".onDestroy :   --->>> " + this);
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
