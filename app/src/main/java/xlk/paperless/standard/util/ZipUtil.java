package xlk.paperless.standard.util;

import android.text.TextUtils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Created by xlk on 2020/11/18.
 * @desc
 */

public class ZipUtil {
    private static final String TAG = "ZipUtil-->";

    /**
     * 对文件列表压缩加密
     *
     * @param srcfile
     * @param destZipFile
     * @param password
     * @return
     */
    public static File doZipFilesWithPassword(File[] srcfile, String destZipFile, String password) {
        if (srcfile == null || srcfile.length == 0) {
            return null;
        }
        ZipParameters parameters = new ZipParameters();
        // 压缩方式
        parameters.setCompressionMethod(CompressionMethod.DEFLATE);
        // 压缩级别
        parameters.setCompressionLevel(CompressionLevel.FASTEST);
        // 加密方式
        if (!TextUtils.isEmpty(password)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(EncryptionMethod.AES);
            //下面的行是可选的。默认情况下使用AES 256。您可以覆盖它以使用AES128。AES192仅支持提取。
            parameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

        }
        ArrayList<File> existFileList = new ArrayList<File>();
        for (int i = 0; i < srcfile.length; i++) {
            if (srcfile[i] != null) {
                existFileList.add(srcfile[i]);
            }
        }
        try {
            ZipFile zipFile = new ZipFile(destZipFile);
            if (!TextUtils.isEmpty(password)) {
                zipFile.setPassword(password.toCharArray());
            }
            zipFile.addFiles(existFileList, parameters);
            return zipFile.getFile();
        } catch (ZipException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 对文件夹加密
     *
     * @param folder
     * @param destZipFile
     * @param password
     * @return
     */
    public static File doZipFilesWithPassword(File folder, String destZipFile, String password) {
        if (!folder.exists()) {
            return null;
        }
        ZipParameters parameters = new ZipParameters();
        // 压缩方式
        parameters.setCompressionMethod(CompressionMethod.AES_INTERNAL_ONLY);
        // 压缩级别
        parameters.setCompressionLevel(CompressionLevel.NORMAL);
        // 加密方式
        if (!TextUtils.isEmpty(password)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(EncryptionMethod.AES);
        }
        try {
            ZipFile zipFile = new ZipFile(destZipFile);
            if (!TextUtils.isEmpty(password)) {
                zipFile.setPassword(password.toCharArray());
            }
            zipFile.setCharset(Charset.forName("GBK"));
            zipFile.addFolder(folder, parameters);
            LogUtil.i(TAG, "doZipFilesWithPassword " + password);
            return zipFile.getFile();
        } catch (ZipException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 单文件压缩并加密
     *
     * @param file        要压缩的zip文件
     * @param destZipFile zip保存路径
     * @param password    密码   可以为null
     * @return
     */
    public static File doZipSingleFileWithPassword(File file, String destZipFile, String password) {
        if (!file.exists()) {
            return null;
        }
        ZipParameters parameters = new ZipParameters();
        // 压缩方式
        parameters.setCompressionMethod(CompressionMethod.DEFLATE);
        // 压缩级别
        parameters.setCompressionLevel(CompressionLevel.FASTEST);
        // 加密方式
        if (!TextUtils.isEmpty(password)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(EncryptionMethod.AES);
        }
        try {
            ZipFile zipFile = new ZipFile(destZipFile);
            if (!TextUtils.isEmpty(password)) {
                zipFile.setPassword(password.toCharArray());
            }
            zipFile.setCharset(Charset.forName("GBK"));
            zipFile.addFile(file, parameters);
            return zipFile.getFile();
        } catch (ZipException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 解压文件
     * File：目标zip文件
     * password：密码，如果没有可以传null
     * path：解压到的目录路径
     */
    public static boolean unZip(File file, String password, String path) {
        boolean res = false;
        try {
            ZipFile zipFile = new ZipFile(file);
            if (zipFile.isEncrypted()) {
                if (password != null && !password.isEmpty()) {
                    zipFile.setPassword(password.toCharArray());
                }
            }
            zipFile.extractAll(path);
            res = true;
        } catch (ZipException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return res;
    }


}
