package xlk.paperless.standard.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.meet.MeetingActivity;

import static xlk.paperless.standard.data.Values.fontScale;

/**
 * @author xlk
 * @date 2020/3/14
 * @desc
 */
public abstract class BaseFragment extends Fragment {

    protected JniHandler jni = JniHandler.getInstance();
    protected final String TAG = getClass().getSimpleName() + "-->";

    /**
     * 设置展示PopupWindow展示在FrameLayout上
     *
     * @param parent
     * @param contentView
     * @return
     */
    protected PopupWindow showPop(View parent, View contentView) {
        View viewById = getActivity().findViewById(R.id.meet_frame_layout);
        PopupWindow popupWindow = new PopupWindow(contentView, viewById.getWidth(), viewById.getHeight());
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

    /**
     * 打开选择本地文件
     *
     * @param requestCode 返回码
     */
    protected void chooseLocalFile(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 打开选择本地文件
     *
     * @param type        指定打开类型
     * @param requestCode 返回码
     */
    protected void chooseLocalFile(String type, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(type);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
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
        if (!hidden) {
            reShow();
        }
    }

    /**
     * Fragment重新显示
     */
    protected void reShow() {

    }

}
