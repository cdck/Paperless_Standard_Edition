package xlk.paperless.standard.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static xlk.paperless.standard.util.FileUtil.createTemporalFileFrom;

/**
 * @author by xlk
 * @date 2020/6/4 15:46
 * @desc 根据Uri获取真实文件路径
 */
public class UriUtil {
    private static final String TAG = "UriUtil-->";

    private static void logUri(Uri uri) {
        String temp = "";
        temp += "uri信息打印：" + uri.toString()
                + "\ngetScheme:" + uri.getScheme()
                + "\ngetAuthority:" + uri.getAuthority()
                + "\ngetEncodedAuthority:" + uri.getEncodedAuthority()
                + "\ngetHost:" + uri.getHost()
                + "\ngetEncodedQuery:" + uri.getEncodedQuery()
                + "\ngetLastPathSegment:" + uri.getLastPathSegment()
                + "\ngetPathSegments:" + uri.getPathSegments()
                + "\ngetSchemeSpecificPart:" + uri.getSchemeSpecificPart()
                + "\nisAbsolute:" + uri.isAbsolute()
                + "\nisHierarchical:" + uri.isHierarchical()
                + "\nnormalizeScheme:" + uri.normalizeScheme();
        LogUtil.d(TAG, "logUri " + temp);
    }

    public static String getFilePath(Context context, Uri uri) {
        logUri(uri);
        String path = null;
        // 以 file:// 开头的
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            path = uri.getPath();
            return path;
        }
        // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        if (columnIndex > -1) {
                            path = cursor.getString(columnIndex);
                        }
                    }
                    cursor.close();
                }
                return path;
            } else {
                // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
                //判断该Uri是否是document封装过的
                if (DocumentsContract.isDocumentUri(context, uri)) {
                    LogUtil.e(TAG, "getFilePath Uri是document封装过的 uri.getAuthority() -->" + uri.getAuthority());
                    if (isExternalStorageDocument(uri)) {
                        // ExternalStorageProvider
                        LogUtil.e(TAG, "getFilePath ExternalStorageProvider -->");
                        final String docId = DocumentsContract.getDocumentId(uri);
                        final String[] split = docId.split(":");
                        final String type = split[0];
                        if ("primary".equalsIgnoreCase(type)) {
                            path = Environment.getExternalStorageDirectory() + "/" + split[1];
                            return path;
                        }
                    } else if (isDownloadsDocument(uri)) {
                        // DownloadsProvider
                        LogUtil.e(TAG, "getFilePath DownloadsProvider -->");
                        final String id = DocumentsContract.getDocumentId(uri);
                        final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                                Long.valueOf(id));
                        return getDataColumn(context, contentUri, null, null);
                    } else if (isMediaDocument(uri)) {
                        // MediaProvider
                        LogUtil.e(TAG, "getFilePath MediaProvider -->");
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
                else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
                    LogUtil.e(TAG, "getFilePath MediaStore (and general) -->");
                    //uri的路径  content://com.huawei.hidisk.fileprovider/root/storage/emulated/0/PaperlessStandardEdition/Log/crash-2020-06-16_11%3A20%3A49-1592277649091.log
                    String temp = "content://com.huawei.hidisk.fileprovider/root/storage/emulated/0";
                    if (uri.toString().contains(temp)) {
                        String s = uri.toString();
                        String tempPath = s.replace(temp, "");
                        //tempPath= /PaperlessStandardEdition/Log/crash-2020-06-16_11%3A20%3A49-1592277649091.log
                        Log.e(TAG, "getFilePath :  substring --> " + tempPath);

                        String tempPre = tempPath.substring(0, tempPath.lastIndexOf("/"));
                        //tempPre=  /PaperlessStandardEdition/Log
                        LogUtil.d(TAG, "getFilePath -->" + tempPre);

                        String lastPathSegment = uri.getLastPathSegment();
                        //lastPathSegment= crash-2020-06-16_11:20:49-1592277649091.log

                        String fileName = tempPre + "/" + lastPathSegment;
                        LogUtil.e(TAG, "getFilePath fileName -->" + fileName);

                        File externalStorageDirectory = Environment.getExternalStorageDirectory();
                        LogUtil.e(TAG, "getFilePath :  组合成的文件路径 --> " + externalStorageDirectory.getAbsolutePath() + ", tempPath= " + fileName);
                        //组合成的文件路径= /storage/emulated/0, tempPath= /PaperlessStandardEdition/Log/crash-2020-06-16_11:20:49-1592277649091.log
                        File file = new File(externalStorageDirectory.getAbsolutePath() + fileName);
                        if (file.exists()) {
                            return file.getAbsolutePath();
                        } else {
                            LogUtil.e(TAG, "getFilePath :  文件不存在  " + file.getAbsolutePath());
                        }
                    }
                    return getDataColumn(context, uri, null, null);
                }
                // File
                else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
                    LogUtil.e(TAG, "getFilePath file -->");
                    return uri.getPath();
                }
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = MediaStore.Images.ImageColumns.DATA;
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
