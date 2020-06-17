package xlk.paperless.standard.view.fragment;

import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import xlk.paperless.standard.R;
import xlk.paperless.standard.view.meet.MeetingActivity;

/**
 * @author xlk
 * @date 2020/3/14
 * @desc
 */
public class BaseFragment extends Fragment {

    /**
     * 设置展示PopupWindow展示在FrameLayout上
     * @param parent
     * @param contentView
     * @return
     */
    protected PopupWindow showPop(View parent, View contentView) {
        PopupWindow popupWindow = new PopupWindow(contentView, MeetingActivity.frameLayoutWidth, MeetingActivity.frameLayoutHeight);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        popupWindow.setTouchable(true);
        // true:设置触摸外面时消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.pop_Animation);
        popupWindow.showAtLocation(parent, Gravity.START | Gravity.BOTTOM, 0, 0);
        return popupWindow;
    }
}
