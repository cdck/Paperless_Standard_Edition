package xlk.paperless.standard.util;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

/**
 * @author xlk
 * @date 2020/4/9
 * @Description: https://mp.weixin.qq.com/s/d9QCoBP6kV9VSWvVldVVwA
 */
public class ScreenFit {
    private static float sSysDensity;
    private static float sSysScaledDensity;

    public void setCustomDensity(@NonNull Activity aty, @NonNull final Application application) {
        final DisplayMetrics appDisplayMetrics = application.getResources().getDisplayMetrics();
        if (sSysDensity == 0) {
            sSysDensity = appDisplayMetrics.density;
            sSysScaledDensity = appDisplayMetrics.scaledDensity;
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    if (newConfig != null && newConfig.fontScale > 0) {
                        sSysScaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {
                }
            });
        }
        final float targetDensity = appDisplayMetrics.widthPixels / 360;//(暂定宽度为360，也可以改为高度方向等);
        final float targetScaleDensity = targetDensity * (sSysScaledDensity / sSysDensity);
        final int targetDensityDpi = (int) (160 * targetDensity);
        appDisplayMetrics.density = targetDensity;
        appDisplayMetrics.scaledDensity = targetScaleDensity;
        appDisplayMetrics.densityDpi = targetDensityDpi;

        final DisplayMetrics atyDisplayMetrics = aty.getResources().getDisplayMetrics();
        atyDisplayMetrics.density = targetDensity;
        atyDisplayMetrics.scaledDensity = targetScaleDensity;
        atyDisplayMetrics.densityDpi = targetDensityDpi;
    }
}
