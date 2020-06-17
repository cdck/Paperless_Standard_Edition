package xlk.paperless.standard.util;

import java.math.BigDecimal;

/**
 * @author by xlk
 * @date 2020/6/11 10:02
 * @desc 数学运算工具类
 */
public class MathUtil {
    /**
     * 除法运算
     * @param a 被除数
     * @param b 除数
     * @param scale 小数位
     * @return
     */
    public static double divide(double a, double b, int scale) {
        BigDecimal d = new BigDecimal(Double.toString(a));
        BigDecimal e = new BigDecimal(Double.toString(b));
        return d.divide(e, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
