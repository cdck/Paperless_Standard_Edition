package xlk.paperless.standard.util;

import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Values;

/**
 * @author xlk
 * @date 2020/3/13
 * @desc
 */
public class PopUtil {
    /**
     * @param contentView 弹框布局
     * @param w           宽
     * @param h           高
     * @param parent      父控件
     * @return PopupWindow
     */
    public static PopupWindow create(View contentView, int w, int h, View parent) {
        PopupWindow popupWindow = new PopupWindow(contentView, w, h);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        popupWindow.setTouchable(true);
        // true:设置触摸外面时消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.pop_Animation);
        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
        return popupWindow;
    }

    public static PopupWindow create(View contentView, View parent) {
        PopupWindow popupWindow = new PopupWindow(contentView, Values.half_width, Values.half_height);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        popupWindow.setTouchable(true);
        // true:设置触摸外面时消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.pop_Animation);
        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
        return popupWindow;
    }

    public static PopupWindow create(View contentView, int width, int height, View parent, int gravity) {
        PopupWindow popupWindow = new PopupWindow(contentView, width, height);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        popupWindow.setTouchable(true);
        // true:设置触摸外面时消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.pop_Animation);
        popupWindow.showAtLocation(parent, gravity, 0, 0);
        return popupWindow;
    }

}
