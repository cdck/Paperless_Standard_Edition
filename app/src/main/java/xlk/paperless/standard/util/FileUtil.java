package xlk.paperless.standard.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;

import androidx.core.content.FileProvider;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import xlk.paperless.standard.BuildConfig;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.WpsModel;
import xlk.paperless.standard.view.App;
import xlk.paperless.standard.view.pdf.PdfViewerActivity;

import static cc.shinichi.library.tool.file.FileUtil.getMIMEType;

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
     * 根据文件名判断文件类型
     *
     * @param context  上下文
     * @param fileName 文件名
     */
    public static String getFileType(Context context, String fileName) {
        if (isDocumentFile(fileName)) {
            return context.getString(R.string.documentation);
        } else if (isPictureFile(fileName)) {
            return context.getString(R.string.picture);
        } else if (isAudioAndVideoFile(fileName)) {
            return context.getString(R.string.video);
        } else {
            return context.getString(R.string.other);
        }
    }

    /**
     * 判断是否为除文档、视频、图片外的其它类文件
     *
     * @param fileName
     * @return
     */
    public static boolean isOtherFile(String fileName) {
        //除去文档/视频/图片其它的后缀名文件
        return !isDocumentFile(fileName) && !isAudioAndVideoFile(fileName) && !isPictureFile(fileName);
    }

    public static boolean isVideoFile(String fileName) {
        if (!fileName.contains(".")) {//该文件没有后缀
            return false;
        }
        //获取文件的扩展名  mp3/mp4...
        String fileEnd = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
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
     * 判断是否为视频文件
     *
     * @param fileName
     * @return
     */
    public static boolean isAudioAndVideoFile(String fileName) {
        if (!fileName.contains(".")) {//该文件没有后缀
            return false;
        }
        //获取文件的扩展名  mp3/mp4...
        String fileEnd = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return fileEnd.equals("mp4")
                || fileEnd.equals("3gp")
//                || fileEnd.equals("wav")
                || fileEnd.equals("mp3")
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
        File file = new File(pathname);
        if (!file.exists()) {
            LogUtil.d(TAG, "openFile -->" + "下载将要打开的文件 pathname= " + pathname);
            JniHandler.getInstance().creationFileDownload(pathname, mediaid, 1, 0,
                    Constant.DOWNLOAD_SHOULD_OPEN_FILE);
        } else {
            if (Values.downloadingFiles.contains(mediaid)) {
                ToastUtil.show(R.string.currently_downloading);
            } else {
                openFile(context, file);
            }
        }
    }

    /**
     * 打开文件
     *
     * @param file
     */
    public static void openFile(Context context, File file) {
        String filename = file.getName();
        LogUtil.e(TAG, "openFile :   --> " + file.getAbsolutePath());
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        if (FileUtil.isAudioAndVideoFile(filename)) {
            return;
        } else if (FileUtil.isPictureFile(filename)) {
            EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_PREVIEW_IMAGE).objects(file.getAbsolutePath()).build());
            return;
        } else if (FileUtil.isDocumentFile(filename)) {
            LogUtils.e("当前是【" + (App.isStandard ? "标准版无纸化" : "方图版无纸化") + "】，将要打开=" + file.getAbsolutePath());
            if (!App.isStandard) {
                if (filename.endsWith(".pdf")) {
                    Intent intent1 = new Intent(context, PdfViewerActivity.class);
                    if (!(context instanceof Activity)) {
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    intent1.putExtra(PdfViewerActivity.FILE_PATH, file.getAbsolutePath());
                    context.startActivity(intent1);
                    return;
                }
            }
            //通知注册WPS广播
            EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_WPS_RECEIVER).objects(true).build());
            /** **** **  如果是文档类文件并且不是pdf文件，设置只能使用WPS软件打开  ** **** **/
            Bundle bundle = new Bundle();
            bundle.putString(WpsModel.OPEN_MODE, WpsModel.OpenMode.NORMAL); // 打开模式
//            bundle.putString(WpsModel.OPEN_MODE, WpsModel.OpenMode.READ_ONLY); // 打开模式
//            bundle.putBoolean(WpsModel.ENTER_REVISE_MODE, true); // 以修订模式打开文档

//            bundle.putBoolean("isShowView", false); // 是否显示wps界面
            bundle.putBoolean(WpsModel.SEND_CLOSE_BROAD, true); // 文件关闭时是否发送广播
            bundle.putBoolean(WpsModel.SEND_SAVE_BROAD, true); // 文件保存时是否发送广播
            bundle.putBoolean(WpsModel.HOMEKEY_DOWN, true); // 单机home键是否发送广播
            bundle.putBoolean(WpsModel.BACKKEY_DOWN, true); // 单机back键是否发送广播
            bundle.putBoolean("SendUnSaveBroad", true); // 单机back键是否发送广播
            bundle.putBoolean(WpsModel.SAVE_PATH, true); // 文件这次保存的路径
            bundle.putString(WpsModel.THIRD_PACKAGE, WpsModel.PackageName.NORMAL); // 第三方应用的包名，用于对改应用合法性的验证
//            bundle.putBoolean(WpsModel.CLEAR_TRACE, true);// 清除打开记录
//            bundle.putBoolean(WpsModel.CLEAR_BUFFER, true);// 关闭文件时是否请空临时文件
//            bundle.putBoolean(CLEAR_FILE, true); //关闭后删除打开文件
            intent.setClassName(WpsModel.PackageName.NORMAL, WpsModel.ClassName.NORMAL);
            intent.putExtras(bundle);
        }
        uriX(context, intent, file);
    }

    /**
     * 打开文件
     */
    public static void openLocalFile(Context context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = null;
        //7.0以后，用了Content Uri 替换了原本的File Uri
        if (Build.VERSION.SDK_INT > 23) {//android 7.0以上时，URI不能直接暴露
            // 方式一
//            uri = getImageContentUri(context, file);
            // 方式二
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        //获取文件file的MIME类型
        String type = getMIMEType(file);
        //设置intent的data和Type属性。
        intent.setDataAndType(uri, type);
        //跳转
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            ToastUtils.showShort(R.string.no_application_can_open);
        }
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


    /**
     * 读取文件内容
     *
     * @param action      读取完成后EventBus发送时的type
     * @param strFilePath txt文件的路径
     */
    public static void readTxtFile(int action, String strFilePath) {
        App.threadPool.execute(new Runnable() {
            @Override
            public void run() {
                long l = System.currentTimeMillis();
                String path = strFilePath;
                //文件内容字符串
                String content = "";
                //打开文件
                File file = new File(path);
                //如果path是传递过来的参数，可以做一个非目录的判断
                if (file.isDirectory()) {
                    Log.d(TAG, "readTxtFile The File doesn't not exist.");
                } else {
                    try {
                        InputStream instream = new FileInputStream(file);
                        if (instream != null) {
                            InputStreamReader inputreader = new InputStreamReader(instream);
                            BufferedReader buffreader = new BufferedReader(inputreader);
                            String line;
                            //分行读取
                            while ((line = buffreader.readLine()) != null) {
                                content += line + "\n";
                            }
                            instream.close();
                        }
                    } catch (java.io.FileNotFoundException e) {
                        Log.d(TAG, "readTxtFile The File doesn't not exist.");
                    } catch (IOException e) {
                        Log.d(TAG, "readTxtFile " + e.getMessage());
                    }
                }
                LogUtil.i(TAG, "readTxtFile 用时：" + (System.currentTimeMillis() - l));
                EventBus.getDefault().post(new EventMessage.Builder().type(action).objects(content).build());
            }
        });
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

    /**
     * 根据路径删除文件或目录
     *
     * @param pathName 文件目录
     */
    public static void delFileByPath(String pathName) {
        File file = new File(pathName);
        if (file.isFile() && file.exists()) {
            LogUtil.d(TAG, "delFileByPath -->" + "删除文件：" + pathName);
            file.delete();
        }
    }

    /**
     * 删除文件、删除目录和子目录文件
     *
     * @param filePath 文件或目录路径
     */
    public static void delDirFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File subFile : files) {
                delDirFile(subFile.getAbsolutePath());
            }
            file.delete();
        } else {
            file.delete();
//            delFileByPath(filePath);
        }
        LogUtil.i(TAG, "delDirFile 删除成功=" + filePath);
    }
}
