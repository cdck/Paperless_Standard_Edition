package xlk.paperless.standard.util;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.UriUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2021/4/24.
 * @desc
 */
public class UDiskUtil {
    private static final String TAG = "UDiskUtil-->";

    /**
     * 打开选择U盘
     *
     * @param context 上下文
     */
    public static void openUDisk(Context context) {
        String uDiskPath = getUDiskPath(context);
        if (uDiskPath.isEmpty()) {
            Toast.makeText(context, R.string.please_insert_udisk_first, Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(uDiskPath);
        Uri uri = UriUtils.file2Uri(file);
        LogUtils.i("uri=" + uri);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(uri, "file/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static String getUDiskPath(Context context) {
        String uDiskPath = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            ArrayList<String> uDiskPathApi24 = getUDiskPathByApi24(context);
            for (int i = 0; i < uDiskPathApi24.size(); i++) {
                String path = uDiskPathApi24.get(i);
                LogUtils.i(TAG, "getUDiskPath U盘路径=" + path);
                if (i == 0) {
                    uDiskPath = path;
                }
            }
//            if (uDiskPathApi24.size() == 1) {
//                uDiskPath = uDiskPathApi24.get(0);
//                LogUtils.i(TAG, "getUDiskPath U盘路径=" + uDiskPath);
//            } else if (uDiskPathApi24.size() > 1) {
//                String filePath = uDiskPathApi24.get(0);
//                uDiskPath = filePath.substring(0, filePath.lastIndexOf("/"));
//                LogUtils.e(TAG, "getUDiskPath U盘选取路径=" + uDiskPath);
//            }
        }
        return uDiskPath;
    }

    /**
     * 通过Api24(7.0)及以下版本获取UDisk路径
     *
     * @param context 上下文对象
     * @return U盘的路径集合
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static ArrayList<String> getUDiskPathByApi24(Context context) {
        ArrayList<String> UDiskPaths = new ArrayList<>();
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        //获取所有挂载的设备（内部sd卡、外部sd卡、挂载的U盘）
        List<StorageVolume> volumes = mStorageManager.getStorageVolumes();
        try {
            Class<?> storageVolumeClazz = Class
                    .forName("android.os.storage.StorageVolume");
            //通过反射调用系统hide的方法
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            for (int i = 0; i < volumes.size(); i++) {
                StorageVolume storageVolume = volumes.get(i);//获取每个挂载的StorageVolume
                //通过反射调用getPath、isRemovable
                String storagePath = (String) getPath.invoke(storageVolume); //获取路径
                boolean isRemovableResult = (boolean) isRemovable.invoke(storageVolume);//是否可移除
                String description = storageVolume.getDescription(context);
                LogUtils.d("getUDiskPathApi24", " i=" + i + " ,storagePath=" + storagePath
                        + " ,isRemovableResult=" + isRemovableResult + " ,description=" + description);
                if (isRemovableResult) {
                    //可移除的说明就是U盘
                    UDiskPaths.add(storagePath);
                }
            }
        } catch (Exception e) {
            LogUtils.d(TAG, "getUDiskPathApi24  e:" + e);
        }
        return UDiskPaths;
    }

    /**
     * 通过Api23(6.0)及以下版本获取UDisk路径
     *
     * @param context 上下文对象
     * @return U盘的路径集合
     */
    public static ArrayList<String> getUDiskPathByApi23AndBelow(Context context) {
        ArrayList<String> data = new ArrayList<>();// include sd and usb devices
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            String[] paths = (String[]) StorageManager.class.getMethod("getVolumePaths", (Class<?>) null).invoke(storageManager, (Object) null);
            for (String path : paths) {
                String state = (String) StorageManager.class.getMethod("getVolumeState", String.class).invoke(storageManager, path);
                if (state.equals(Environment.MEDIA_MOUNTED) && !path.contains("emulated")) {
                    LogUtils.d(TAG, "getUSBPaths 路径---------->" + path);
                    data.add(path);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
