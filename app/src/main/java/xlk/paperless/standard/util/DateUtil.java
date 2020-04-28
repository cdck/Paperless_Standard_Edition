package xlk.paperless.standard.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author xlk
 * @date 2020/3/10
 * @Description:
 */
public class DateUtil {

    /**
     * 将当前获取的时间戳转换成详细日期时间
     *
     * @param time
     * @return
     */
    public static String nowDate(long time) {
        Date tTime = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(tTime);
    }

    /**
     * @param time 单位 毫秒
     *             时区设置：SimpleDateFormat对象.setTimeZone(TimeZone.getTimeZone("GTM"));
     * @return
     */
    public static String[] getGTMDate(long time) {
        Date tTime = new Date(time);

        SimpleDateFormat day = new SimpleDateFormat("MM月dd日");
        day.setTimeZone(TimeZone.getTimeZone("GTM"));
        String dayt = day.format(tTime);

        SimpleDateFormat week = new SimpleDateFormat("EEEE");//只有一个E 则解析出来是 周几，4个E则是星期几
        week.setTimeZone(TimeZone.getTimeZone("GTM"));
        String weekt = week.format(tTime);

        SimpleDateFormat tim = new SimpleDateFormat("HH:mm:ss");
        tim.setTimeZone(TimeZone.getTimeZone("GTM"));
        String timt = tim.format(tTime);

        return new String[]{dayt, weekt, timt};
    }

    /**
     * @param seconds 秒
     * @return
     */
    public static String getHHss(long seconds) {
        Date date = new Date(seconds * 1000);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(date);
    }

    /**
     * 转成时分秒 00::00:00
     *
     * @param ms 单位：毫秒
     * @return
     */
    public static String convertTime(long ms) {
        String ret = "";
        Date date = new Date(ms);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        timeFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
        ret = timeFormat.format(date);
        return ret;
    }

    /**
     * 将秒数转换成 00:04:58  用于倒计时
     * 时间小于1小时显示分秒，显示样式 00:20
     * 时间大于1小时显示时分秒，显示样式 01:11:12
     * %02d 就是说长度不够2位的时候前面补0，主要是解决05:00这样的显示问题, 不进行补0的话会出现5:0的结果
     *
     * @param seconds 秒数
     * @return
     */
    public static String formatSeconds(long seconds) {
        String standardTime;
        if (seconds <= 0) {
            standardTime = "00:00";
        } else if (seconds < 60) {
            standardTime = String.format(Locale.getDefault(), "00:%02d", seconds % 60);
        } else if (seconds < 3600) {
            standardTime = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
        } else {
            standardTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60);
        }
        return standardTime;
    }
}
