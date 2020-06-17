package xlk.paperless.standard.util;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import xlk.paperless.standard.R;

/**
 * @author xlk
 * @date 2020/3/13
 * @desc
 */
public class PopUtil {
    /**
     *
     * @param contentView 弹框布局
     * @param w 宽
     * @param h 高
     * @param outside 是否点击外部时消失
     * @param parent 父控件
     * @return PopupWindow
     */
    public static PopupWindow create(View contentView, int w, int h, boolean outside, View parent) {
        PopupWindow popupWindow = new PopupWindow(contentView, w, h);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        popupWindow.setTouchable(true);
        // true:设置触摸外面时消失
        popupWindow.setOutsideTouchable(outside);
        popupWindow.setFocusable(outside);
        popupWindow.setAnimationStyle(R.style.pop_Animation);
        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
        return popupWindow;
    }
}
