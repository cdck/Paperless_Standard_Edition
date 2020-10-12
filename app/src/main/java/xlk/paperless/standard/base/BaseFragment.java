package xlk.paperless.standard.base;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import xlk.paperless.standard.R;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.meet.MeetingActivity;

/**
 * @author xlk
 * @date 2020/3/14
 * @desc
 */
public class BaseFragment extends Fragment {

    protected final String TAG = getClass().getSimpleName() + "-->";

    /**
     * 设置展示PopupWindow展示在FrameLayout上
     *
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
        popupWindow.showAtLocation(parent, Gravity.END | Gravity.BOTTOM, 0, 0);
        return popupWindow;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onAttach :   --> ");
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onCreate :   --> ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onActivityCreated :   --> ");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onStart :   --> ");
        super.onStart();
    }

    @Override
    public void onResume() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onResume :   --> ");
        super.onResume();
    }

    @Override
    public void onPause() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onPause :   --> ");
        super.onPause();
    }

    @Override
    public void onStop() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onStop :   --> ");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onDestroyView :   --> ");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onDestroy :   --> ");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onDetach :   --> ");
        super.onDetach();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtil.i("F_life", this.getClass().getSimpleName() + ".onHiddenChanged :   --> " + hidden);
        super.onHiddenChanged(hidden);
    }
}
