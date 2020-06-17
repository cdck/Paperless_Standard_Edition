package xlk.paperless.standard.util;

import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Objects;

import xlk.paperless.standard.BuildConfig;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.WpsModel;

/**
 * @author xlk
 * @date 2020/3/11
 * @desc
 */
public class FileUtil {
    private static final String TAG = "FileUtil-->";

    /**
     * 多级创建目录
     *
     * @param dirPath
     */
    public static boolean createDir(String dirPath) {
        File file = new File(dirPath);
        if (file.exists()) {
            return true;
        } else {
            return file.mkdirs();
        }
    }


    /**
     * 将BitMap保存到指定目录下
     *
     * @param bitmap
     * @param file
     */
    public static void saveBitmap(Bitmap bitmap, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                LogUtil.e(TAG, "saveBitmap :  回收 --> ");
                bitmap.recycle();
            }
        }
    }


    /**
     * 自动转换文件大小 22B 22KB 22MB 22GB
     *
     * @param fileS 文件的大小 file.size() 获取的值
     * @return
     */
    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }


    /**
     * 判断是否为除文档、视频、图片外的其它类文件
     *
     * @param fileName
     * @return
     */
    public static boolean isOtherFile(String fileName) {
        //除去文档/视频/图片其它的后缀名文件
        return !isDocumentFile(fileName) && !isVideoFile(fileName) && !isPictureFile(fileName);
    }

    /**
     * 判断是否为视频文件
     *
     * @param fileName
     * @return
     */
    public static boolean isVideoFile(String fileName) {
        if (!fileName.contains(".")) {//该文件没有后缀
            return false;
        }
        //获取文件的扩展名  mp3/mp4...
        String fileEnd = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        return fileEnd.equals("mp4")
                || fileEnd.equals("3gp")
//                || fileEnd.equals("wav")
//                || fileEnd.equals("mp3")
//                || fileEnd.equals("wmv")
//                || fileEnd.equals("ts")
                || fileEnd.equals("rmvb")
                || fileEnd.equals("mov")
                || fileEnd.equals("m4v")
                || fileEnd.equals("avi")
                || fileEnd.equals("m3u8")
                || fileEnd.equals("3gpp")
                || fileEnd.equals("3gpp2")
                || fileEnd.equals("mkv")
                || fileEnd.equals("flv")
                || fileEnd.equals("divx")
                || fileEnd.equals("f4v")
                || fileEnd.equals("rm")
                || fileEnd.equals("asf")
                || fileEnd.equals("ram")
                || fileEnd.equals("mpg")
                || fileEnd.equals("v8")
                || fileEnd.equals("swf")
                || fileEnd.equals("m2v")
                || fileEnd.equals("asx")
                || fileEnd.equals("ra")
                || fileEnd.equals("naivx")
                || fileEnd.equals("xvid");
    }

    /**
     * 判断是否为图片文件
     *
     * @param fileName
     * @return
     */
    public static boolean isPictureFile(String fileName) {
        if (!fileName.contains(".")) {//该文件没有后缀
            return false;
        }
        //获取文件的扩展名
        String fileEnd = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        return fileEnd.equals("jpg")
                || fileEnd.equals("png")
                || fileEnd.equals("gif")
                || fileEnd.equals("img")
                || fileEnd.equals("bmp")
                || fileEnd.equals("jpeg");
    }

    /**
     * 判断是否为文档类文件
     *
     * @param fileName
     * @return
     */
    public static boolean isDocumentFile(String fileName) {
        if (!fileName.contains(".")) {//该文件没有后缀
            return false;
        }
        //获取文件的扩展名 -->获得的是小写：toLowerCase()  /大写:toUpperCase()
        String fileEnd = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
        return fileEnd.equals("txt")
                || fileEnd.equals("doc")
                || fileEnd.equals("docx")
                || fileEnd.equals("log")
//                || fileEnd.equals("dot")
//                || fileEnd.equals("dotx")
                || fileEnd.equals("ppt")
                || fileEnd.equals("pptx")
                || fileEnd.equals("pps")
                || fileEnd.equals("ppsx")
//                || fileEnd.equals("pot")
//                || fileEnd.equals("potx")
                || fileEnd.equals("xls")
                || fileEnd.equals("xlsx")
//                || fileEnd.equals("xlt")
//                || fileEnd.equals("xltx")
//                || fileEnd.equals("wpt")
                || fileEnd.equals("wps")
//                || fileEnd.equals("csv")
                || fileEnd.equals("pdf");
    }


    public static void openFile(Context context, String dir, String filename, int mediaid) {
        createDir(dir);
        String pathname = dir + filename;
        LogUtil.d(TAG, "openFile -->" + "下载将要打开的文件 pathname= " + pathname);
        JniHandler.getInstance().creationFileDownload(pathname, mediaid, 1, 0, Constant.SHOULD_OPEN_FILE_KEY);
    }

    /**
     * 打开文件
     *
     * @param file
     */
    public static void openFile(Context context, File file) {
        String filename = file.getName();
        LogUtil.e(TAG, "openFile :   --> " + filename);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        if (FileUtil.isVideoFile(filename)) {
            return;
        } else if (FileUtil.isPictureFile(filename)) {
            EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_PREVIEW_IMAGE).objs(file.getAbsolutePath()).build());
            return;
        } else if (FileUtil.isDocumentFile(filename)) {
            //通知注册WPS广播
            EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_WPS_RECEIVER).objs(true).build());
            /** **** **  如果是文档类文件并且不是pdf文件，设置只能使用WPS软件打开  ** **** **/
            Bundle bundle = new Bundle();
            bundle.putString(WpsModel.OPEN_MODE, WpsModel.OpenMode.NORMAL); // 打开模式
//            bundle.putBoolean(WpsModel.ENTER_REVISE_MODE, true); // 以修订模式打开文档

            bundle.putBoolean(WpsModel.SEND_CLOSE_BROAD, true); // 文件关闭时是否发送广播
            bundle.putBoolean(WpsModel.SEND_SAVE_BROAD, true); // 文件保存时是否发送广播
            bundle.putBoolean(WpsModel.HOMEKEY_DOWN, true); // 单机home键是否发送广播
            bundle.putBoolean(WpsModel.BACKKEY_DOWN, true); // 单机back键是否发送广播

            bundle.putBoolean(WpsModel.SAVE_PATH, true); // 文件这次保存的路径
            bundle.putString(WpsModel.THIRD_PACKAGE, WpsModel.PackageName.NORMAL); // 第三方应用的包名，用于对改应用合法性的验证
//            bundle.putBoolean(WpsModel.CLEAR_TRACE, true);// 清除打开记录
//            bundle.putBoolean(CLEAR_FILE, true); //关闭后删除打开文件
            intent.setClassName(WpsModel.PackageName.NORMAL, WpsModel.ClassName.NORMAL);
            intent.putExtras(bundle);
        }
        uriX(context, intent, file);
    }

    /**
     * 抽取成工具方法
     *
     * @param context
     * @param intent
     * @param file
     */
    public static void uriX(Context context, Intent intent, File file) {
        if (Build.VERSION.SDK_INT > 23) {//android 7.0以上时，URI不能直接暴露
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uriForFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(uriForFile, "application/vnd.android.package-archive");
        } else {
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastUtil.show(R.string.no_wps_software_found);
            e.printStackTrace();
        }
    }

    //获取文件真实路径
    public static String getRealPath(Context cxt, Uri uri) {
        String path;
        if ("file".equalsIgnoreCase(uri.getScheme())) //使用第三方应用打开
            path = uri.getPath();
        else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) //4.4以后
            path = getPath(cxt, uri);
        else //4.4以下系统调用方法
            path = getRealPathFromURI(cxt, uri);
        return path;
    }

    // 返回文件选择的全路径
    private static String getRealPathFromURI(Context cxt, Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = cxt.getContentResolver().query(contentUri, proj, null, null, null);
        if (null != cursor && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    private static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {
        final String column = "_data";
        final String[] projection = {column};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * 获取字符串的前/后缀名
     *
     * @param filepath 字符串
     * @param type     =0 获取后缀 =1获取前缀
     *                 eg: a\b\c.txt  =0
     * @return
     */
    public static String getCutStr(String filepath, int type) {
        if ((filepath != null) && (filepath.length() > 0)) {
            int dot = filepath.lastIndexOf('.');
            //该文件名有.符号  且不能是最后一个字符
            if ((dot > -1) && (dot < (filepath.length() - 1))) {
                if (type == 0) {//获取后缀
                    return filepath.substring(dot + 1);
                } else {//获取前缀名称
                    return filepath.substring(0, dot);
                }
            }
        }
        return filepath;
    }

    /**
     * 文件名是否合法
     *
     * @param fileName 传入不包含后缀
     * @return
     */
    public static boolean isLegalName(String fileName) {
        String regex = "[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$";
        return fileName.matches(regex);
    }


    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = Objects.requireNonNull(context.getExternalCacheDir()).getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    public static File createTemporalFileFrom(Context context, InputStream inputStream, String fileName)
            throws IOException {
        File targetFile = null;

        if (inputStream != null) {
            int read;
            byte[] buffer = new byte[8 * 1024];
            //自己定义拷贝文件路径
            targetFile = new File(getDiskCacheDir(context), fileName);
            if (targetFile.exists()) {
                targetFile.delete();
            }
            OutputStream outputStream = new FileOutputStream(targetFile);

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return targetFile;
    }

    /**
     * 根据路径删除文件或目录
     *
     * @param pathName 文件目录
     */
    public static void delFileByPath(String pathName) {
        File file = new File(pathName);
        if (file.exists()) {
            LogUtil.d(TAG, "delFileByPath -->" + "删除文件：" + pathName);
            file.delete();
        }
    }
}
