package xlk.paperless.standard.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * @author xlk
 * @date 2020/3/9
 * @Description:
 */
public class ConvertUtil {
    /**
     * String 转 ByteString
     *
     * @param name
     * @return
     */
    public static ByteString s2b(String name) {
        return ByteString.copyFrom(name, Charset.forName("UTF-8"));
    }

    /**
     * ByteString 转 String
     *
     * @param string
     * @return
     */
    public static String b2s(ByteString string) {
        return string.toStringUtf8();
    }


    public static Bitmap bs2bmp(ByteString bs) {
        byte[] bytes = bs.toByteArray();
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        //byte[] bytes = picdata.toByteArray();
        //ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        //return BitmapFactory.decodeStream(inputStream);
    }

    /**
     * 将BitMap转为byte数组
     *
     * @param bitmap
     * @return
     */
    public static byte[] bmp2byte(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }


    public static ByteString bmp2bs(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] datas = baos.toByteArray();
        return ByteString.copyFrom(datas);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
