package xlk.paperless.standard.util;

import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Values;

/**
 * @author xlk
 * @date 2020/3/13
 * @desc
 */
public class PopUtil {

    public static PopupWindow create(View contentView, View parent) {
        return create(contentView, Values.half_width, Values.half_height, parent);
    }

    /**
     * @param contentView 弹框布局
     * @param w           宽
     * @param h           高
     * @param parent      父控件
     * @return PopupWindow
     */
    public static PopupWindow create(View contentView, int w, int h, View parent) {
        return create(contentView, w, h, true, parent, Gravity.CENTER, 0, 0);
    }

    public static PopupWindow create(View contentView, int width, int height, boolean outside, View parent, int gravity, int x, int y) {
        PopupWindow popupWindow = new PopupWindow(contentView, width, height);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        popupWindow.setTouchable(outside);
        // true:设置触摸外面时消失
        popupWindow.setOutsideTouchable(outside);
        popupWindow.setFocusable(outside);
        popupWindow.setAnimationStyle(R.style.pop_Animation);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.showAtLocation(parent, gravity, x, y);
        return popupWindow;
    }

}
