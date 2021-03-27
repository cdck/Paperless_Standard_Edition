package xlk.paperless.standard.ui;

import android.content.Context;
import android.graphics.Region;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.admin.fragment.system.other.MainInterfaceBean;

/**
 * @author Created by xlk on 2020/11/7.
 * @desc
 */
public class InterfaceDragView extends AbsoluteLayout {
    private final String TAG = "InterfaceDragView-->";
    int viewWidth, viewHeight;
    int left, top, right, bottom;
    List<MainInterfaceBean> mData = new ArrayList<>();
    HashMap<Integer, View> views = new HashMap<>();
    List<RegionBean> regions = new ArrayList<>();
    private float touchDownX, touchDownY;
    /**
     * 当前选中的faceID
     */
    private int touchFaceId;
    private ViewClickListener listener;
    private boolean isShowAll = false;
    /**
     * logo图标的文件路径
     */
    private String logoFilePath;


    private class RegionBean {
        int faceId;
        Region region;

        public RegionBean(int faceId, Region region) {
            this.faceId = faceId;
            this.region = region;
        }

        public int getFaceId() {
            return faceId;
        }

        public void setFaceId(int faceId) {
            this.faceId = faceId;
        }

        public Region getRegion() {
            return region;
        }

        public void setRegion(Region region) {
            this.region = region;
        }
    }

    public interface ViewClickListener {
        void onClick(MainInterfaceBean data, int width, int height);

        void onHide(boolean hide);
    }

    public void setViewClickListener(ViewClickListener viewClickListener) {
        this.listener = viewClickListener;
    }

    public InterfaceDragView(Context context) {
        this(context, null);
    }

    public InterfaceDragView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InterfaceDragView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public InterfaceDragView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 动态设置setLayoutParams后会执行
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        this.viewWidth = width;
        this.viewHeight = height;
        LogUtil.d(TAG, "onMeasure --> width=" + width + ", height=" + height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        left = l;
        top = t;
        right = r;
        bottom = b;
        LogUtil.d(TAG, "onLayout --> " + left + "," + top + "," + right + "," + bottom);
        super.onLayout(changed, left, top, right, bottom);
    }

    private boolean isLogo(int faceId) {
        return faceId == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_LOGO_VALUE
                || faceId == InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinLogo_VALUE;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownX = event.getX();
                touchDownY = event.getY();
                touchFaceId = getTouchView((int) touchDownX, (int) touchDownY);
                if (touchFaceId != 0) {
                    //设置不能拖动投影和公告的logo图标
                    boolean isLogo = isLogo(touchFaceId);
                    if (isLogo) {
                        touchFaceId = 0;
                    } else {
                        MainInterfaceBean data = getData(touchFaceId);
                        int flag = data.getFlag();
                        if (!isShowFlag(flag) && !isShowAll) {
                            touchFaceId = 0;
                        }
                    }
                    if (listener != null) {
                        listener.onHide(isLogo);
                    }
                }
                LogUtil.i(TAG, "onTouchEvent touchFaceId=" + touchFaceId);
                break;
            case MotionEvent.ACTION_MOVE:
                if (touchFaceId != 0) {
                    float moveX = event.getX();
                    float moveY = event.getY();
                    float dx = moveX - touchDownX;
                    float dy = moveY - touchDownY;
                    View view = views.get(touchFaceId);
                    int width = view.getWidth();
                    int height = view.getHeight();
                    LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
                    layoutParams.x += dx;
                    layoutParams.y += dy;
                    if (layoutParams.x < 0) {
                        layoutParams.x = 0;
                    }
                    if (layoutParams.y < 0) {
                        layoutParams.y = 0;
                    }
                    if (layoutParams.x > viewWidth - width) {
                        layoutParams.x = viewWidth - width;
                    }
                    if (layoutParams.y > viewHeight - height) {
                        layoutParams.y = viewHeight - height;
                    }
                    view.layout(layoutParams.x, layoutParams.y, layoutParams.x + width, layoutParams.y + height);
//                    updateRegion(touchFaceId, layoutParams.x, layoutParams.y, width, height);
//                    updateData(touchFaceId, layoutParams.x, layoutParams.y, width, height);
                    touchDownX = moveX;
                    touchDownY = moveY;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (touchFaceId != 0) {
                    View view = views.get(touchFaceId);
                    int width = view.getWidth();
                    int height = view.getHeight();
                    LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
                    updateRegion(touchFaceId, layoutParams.x, layoutParams.y, width, height);
                    updateData(touchFaceId, layoutParams.x, layoutParams.y, width, height);
                    updateBottomUI(touchFaceId, width, height);
                }
                break;
        }
        return true;
    }

    /**
     * 更新PopupWindow中的底部信息
     *
     * @param faceId 界面id
     */
    private void updateBottomUI(int faceId, int width, int height) {
        if (listener != null) {
            MainInterfaceBean data = getData(faceId);
            LogUtil.i(TAG, "updateBottomUI faceId=" + faceId + ",width=" + width + ",height=" + height);
            listener.onClick(data, width, height);
        }
    }

    /**
     * 更新区域范围数据
     *
     * @param faceId 界面id
     * @param x      左上角x坐标
     * @param y      左上角y坐标
     * @param width  宽
     * @param height 高
     */
    private void updateRegion(int faceId, int x, int y, int width, int height) {
        RegionBean bean = getRegion(faceId);
        if (bean != null) {
            bean.getRegion().set(x, y, x + width, y + height);
        }
    }

    private RegionBean getRegion(int faceId) {
        for (RegionBean item : regions) {
            if (item.getFaceId() == faceId) {
                return item;
            }
        }
        return null;
    }

    /**
     * 更新数据
     *
     * @param faceId 界面id
     * @param x      左上角x坐标
     * @param y      左上角y坐标
     * @param width  宽
     * @param height 高
     */
    private void updateData(int faceId, int x, int y, int width, int height) {
        MainInterfaceBean data = getData(faceId);
        if (data != null) {
            float lx = (float) x / this.viewWidth * 100;
            float ly = (float) y / this.viewHeight * 100;
            float bx = ((float) (x + width)) / viewWidth * 100;
            float by = ((float) (y + height)) / viewHeight * 100;
            data.setLx(lx);
            data.setLy(ly);
            data.setBx(bx);
            data.setBy(by);
            LogUtil.i(TAG, "updateData lx=" + lx + ",ly=" + ly + ",bx=" + bx + ",by=" + by);
        }
    }

    /**
     * 根据界面id返回数据
     *
     * @param faceId 界面id
     */
    private MainInterfaceBean getData(int faceId) {
        for (int i = 0; i < mData.size(); i++) {
            MainInterfaceBean bean = mData.get(i);
            if (bean.getFaceId() == faceId) {
                return bean;
            }
        }
        return null;
    }

    /**
     * 获取手指按下时选中的view
     *
     * @param x 手指按下的坐标x
     * @param y 手指按下的坐标y
     * @return 返回界面id
     */
    private int getTouchView(int x, int y) {
        for (RegionBean item : regions) {
            if (item.getRegion().contains(x, y)) {
                return item.getFaceId();
            }
        }
        return 0;
    }

    /**
     * 获取faceID对应的中文名称
     *
     * @param faceId 界面id
     */
    private String getNameByFaceId(int faceId) {
        switch (faceId) {
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_LOGO_VALUE:
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinLogo_VALUE:
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_LOGO_GEO_VALUE:
                return getContext().getString(R.string.logo_pic);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_MEETNAME_VALUE:
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEETNAME_VALUE:
                return getContext().getString(R.string.meeting_name);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERNAME_VALUE:
                return getContext().getString(R.string.member_name);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_COMPANY_VALUE:
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERCOMPANY_VALUE:
                return getContext().getString(R.string.member_unit);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_MEMBERJOB_VALUE:
                return getContext().getString(R.string.member_job);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_SEATNAME_VALUE:
                return getContext().getString(R.string.seat_name);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_TIMER_VALUE:
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_TIMER_VALUE:
                return getContext().getString(R.string.time_date);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_COMPANYNAME_VALUE:
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_COMPANY_VALUE:
                return getContext().getString(R.string.company_name);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_STATUS_VALUE:
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_topstatus_GEO_VALUE:
                return getContext().getString(R.string.meeting_status);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_checkin_GEO_VALUE:
                return getContext().getString(R.string.enterMeeting);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_manage_GEO_VALUE:
                return getContext().getString(R.string.secretary_login);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_remark_GEO_VALUE:
                return getContext().getString(R.string.remarks);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_role_GEO_VALUE:
                return getContext().getString(R.string.member_role);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_ver_GEO_VALUE:
                return getContext().getString(R.string.device_version);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_SEATNAME_VALUE:
                return getContext().getString(R.string.projective_name);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_SIGN_ALL_VALUE:
                return getContext().getString(R.string.check_in_all);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_SIGN_IN_VALUE:
                return getContext().getString(R.string.already_checked_in);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_SIGN_OUT_VALUE:
                return getContext().getString(R.string.not_checked_in);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinTitle_VALUE:
                return getContext().getString(R.string.notice_title);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinContent_VALUE:
                return getContext().getString(R.string.notice_content);
            case InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinBtn_VALUE:
                return getContext().getString(R.string.close_button);
            default:
                return "";
        }
    }

    /**
     * 设置对齐方式
     *
     * @param params LayoutParams
     * @param align  InterfaceMacro#Pb_FontAlignFlag
     */
    private void setAlign(RelativeLayout.LayoutParams params, int align) {
        LogUtil.i(TAG, "setAlign align=" + align);
        //需要先清除之前可能添加的属性,避免之前靠左现在想靠右就失效的问题
        params.removeRule(RelativeLayout.ALIGN_PARENT_START);
        params.removeRule(RelativeLayout.ALIGN_PARENT_END);
        params.removeRule(RelativeLayout.CENTER_IN_PARENT);
        switch (align) {
            //左对齐
            case InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT_VALUE:
                params.addRule(RelativeLayout.ALIGN_PARENT_START);
                break;
            //右对齐
            case InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT_VALUE:
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                break;
//            //水平对齐
//            case InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_HCENTER_VALUE:
//                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
//                break;
//            //上对齐
//            case InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_TOP_VALUE:
//                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                break;
//            //下对齐
//            case InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_BOTTOM_VALUE:
//                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                break;
//            //垂直对齐
//            case InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_VCENTER_VALUE:
//                params.addRule(RelativeLayout.CENTER_VERTICAL);
//                break;
            //默认居中
            default:
                //居中对齐
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                break;
        }
    }

    private int getAlignByPosition(int position) {
        switch (position) {
            case 1:
                return InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT_VALUE;
            case 2:
                return InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT_VALUE;
            default:
                return InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_HCENTER_VALUE;
        }
    }

    /**
     * 根据名称获取字体
     *
     * @param fontName 字体名称
     */
    private Typeface getTypeface(String fontName) {
        Typeface typeface;
        switch (fontName) {
            case "楷体":
                typeface = Typeface.createFromAsset(getContext().getAssets(), "kaiti.ttf");
                break;
            case "黑体":
                typeface = Typeface.createFromAsset(getContext().getAssets(), "heiti.ttf");
                break;
            case "隶书":
                typeface = Typeface.createFromAsset(getContext().getAssets(), "lishu.ttf");
                break;
            case "微软雅黑":
                typeface = Typeface.createFromAsset(getContext().getAssets(), "weiruanyahei.ttf");
                break;
            case "小楷":
                typeface = Typeface.createFromAsset(getContext().getAssets(), "xiaokai.ttf");
                break;
            default:
                typeface = Typeface.createFromAsset(getContext().getAssets(), "fangsong.ttf");
                break;
        }
        return typeface;
    }

    public void initInterfaceView(List<MainInterfaceBean> data) {
        removeAllViews();
        views.clear();
        regions.clear();
        mData.clear();
        mData.addAll(data);
        LogUtil.i(TAG, "initInterfaceView 当前宽高=" + viewWidth + " x " + viewHeight);
        for (int i = 0; i < mData.size(); i++) {
            MainInterfaceBean item = mData.get(i);
            LogUtil.e(TAG, "initInterfaceView item=" + item.toString());
            int faceId = item.getFaceId();
            int fontSize = item.getFontSize();
            int align = item.getAlign();
            int fontFlag = item.getFontFlag();
            boolean isBold = isBoldFlag(fontFlag);
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.item_interface, null);
            inflate.setId(faceId);
            TextView tv_interface = inflate.findViewById(R.id.tv_interface);
            RelativeLayout.LayoutParams tvParams = (RelativeLayout.LayoutParams) tv_interface.getLayoutParams();
            tv_interface.setText(getNameByFaceId(faceId));
            tv_interface.setTextColor(item.getColor());
            tv_interface.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            tv_interface.setTypeface(getTypeface(item.getFontName()));
            tv_interface.setTypeface(isBold ? Typeface.defaultFromStyle(Typeface.BOLD) : Typeface.defaultFromStyle(Typeface.NORMAL));
            setAlign(tvParams, align);
//            tv_interface.setLayoutParams(tvParams);
            float lx = item.getLx();
            float ly = item.getLy();
            float bx = item.getBx();
            float by = item.getBy();
            int x1 = (int) (viewWidth * (lx / 100f));
            int y1 = (int) (viewHeight * (ly / 100f));
            int x2 = (int) (viewWidth * (bx / 100f));
            int y2 = (int) (viewHeight * (by / 100f));
            int viewWidth = x2 - x1;
            int viewHeight = y2 - y1;
            LogUtil.i(TAG, "initInterfaceView faceid=" + faceId + ",视图大小=" + viewWidth + "," + viewHeight + ",左上角坐标=" + x1 + "," + y1);
            LayoutParams params = new LayoutParams(
                    viewWidth, viewHeight,
                    x1, y1);
            if (!isShowAll) {
                int flag = item.getFlag();
                boolean isShow = isShowFlag(flag);
                inflate.setVisibility(isShow ? VISIBLE : GONE);
            }
            inflate.setLayoutParams(params);
            addView(inflate);
            regions.add(new RegionBean(faceId, new Region(x1, y1, x2, y2)));
            views.put(faceId, inflate);
        }
    }

    private boolean isBoldFlag(int fontFlag) {
        return (fontFlag & InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD_VALUE)
                == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD_VALUE;
    }

    private boolean isShowFlag(int flag) {
        return (flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE)
                == InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE;
    }

    /**
     * 更新选中项的字体颜色
     *
     * @param color 颜色值
     */
    public void updateTextColor(int color) {
        if (touchFaceId != 0) {
            View view = views.get(touchFaceId);
            TextView tv_interface = view.findViewById(R.id.tv_interface);
            tv_interface.setTextColor(color);
            MainInterfaceBean bean = getData(touchFaceId);
            if (bean != null) {
                bean.setColor(color);
            }
        }
        invalidate();
    }

    public void updateTextSize(int size) {
        if (touchFaceId != 0) {
            View view = views.get(touchFaceId);
            TextView tv_interface = view.findViewById(R.id.tv_interface);
            tv_interface.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            MainInterfaceBean bean = getData(touchFaceId);
            if (bean != null) {
                bean.setFontSize(size);
            }
        }
        invalidate();
    }

    public void updateViewWidth(int newWidth) {
        if (touchFaceId != 0) {
            View view = views.get(touchFaceId);
            int height = view.getHeight();
            //更新数据
            MainInterfaceBean data = getData(touchFaceId);
            float lx = data.getLx();
            //左上角坐标加上新的宽度/总宽度*100 得到的是百分比值
            float bx = ((lx / 100f) * viewWidth + newWidth) / viewWidth * 100;
            data.setBx(bx);
            //更新区域
            RegionBean bean = getRegion(touchFaceId);
            int left = (int) ((lx / 100) * viewWidth);
            int top = (int) (data.getLy() / 100 * viewHeight);
            bean.getRegion().set(left, top, left + newWidth, top + height);
            //更新视图
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            layoutParams.width = newWidth;
            view.layout(layoutParams.x, layoutParams.y, layoutParams.x + newWidth, layoutParams.y + height);
        }
    }

    public void updateViewHeight(int newHeight) {
        if (touchFaceId != 0) {
            View view = views.get(touchFaceId);
            int width = view.getWidth();
            //更新数据
            MainInterfaceBean data = getData(touchFaceId);
            float ly = data.getLy();
            float by = ((ly / 100f) * viewHeight + newHeight) / viewHeight * 100;
            data.setBy(by);
            //更新区域
            RegionBean bean = getRegion(touchFaceId);
            int left = (int) ((data.getLx() / 100f) * viewWidth);
            int top = (int) (ly / 100f * viewHeight);
            bean.getRegion().set(left, top, left + width, top + newHeight);
            //更新视图
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            layoutParams.height = newHeight;
            view.layout(layoutParams.x, layoutParams.y, layoutParams.x + width, layoutParams.y + newHeight);
        }
    }

    /**
     * 更新logo图标
     *
     * @param filePath 图片路径
     */
    public void updateLogoImg(int tag, String filePath) {
        int faceId;
        switch (tag) {
            //主界面
            case 1:
                faceId = InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_LOGO_GEO_VALUE;
                break;
            //投影界面
            case 2:
                faceId = InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACEID_PROJECTIVE_LOGO_VALUE;
                break;
            //公告界面
            default:
                faceId = InterfaceMacro.Pb_MeetFaceID.Pb_MEET_FACE_BulletinLogo_VALUE;
                break;
        }
        View view = views.get(faceId);
        if (view != null) {
            RelativeLayout ll_interface = view.findViewById(R.id.ll_interface);
            TextView tv_interface = view.findViewById(R.id.tv_interface);
            tv_interface.setText("");
            logoFilePath = filePath;
            Drawable drawable = Drawable.createFromPath(filePath);
            ll_interface.setBackground(drawable);
        }
    }

    /**
     * 更新是否加粗
     *
     * @param position Spinner索引
     */
    public void updateTextBold(int position) {
        if (touchFaceId != 0) {
            View view = views.get(touchFaceId);
            TextView tv_interface = view.findViewById(R.id.tv_interface);
            boolean isBold = position == 1;
            tv_interface.setTypeface(isBold ? Typeface.defaultFromStyle(Typeface.BOLD) : Typeface.defaultFromStyle(Typeface.NORMAL));
            MainInterfaceBean bean = getData(touchFaceId);
            if (bean != null) {
                int fontFlag = bean.getFontFlag();
                LogUtil.i(TAG, "updateTextBold fontFlag=" + fontFlag + ",faceid=" + touchFaceId);
                if (isBold) {
                    if ((fontFlag & InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD_VALUE)
                            != InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD_VALUE) {
                        LogUtil.i(TAG, "updateTextBold 设置加粗");
                        fontFlag += InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD_VALUE;
                    }
                } else {
                    if (isBoldFlag(fontFlag)) {
                        LogUtil.i(TAG, "updateTextBold 取消加粗");
                        fontFlag -= InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD_VALUE;
                    }
                }
                LogUtil.d(TAG, "updateTextBold 修改后的fontFlag=" + fontFlag);
                bean.setFontFlag(fontFlag);
            }
            invalidate();
        }
    }

    /**
     * 更新显示或隐藏
     *
     * @param position Spinner索引值
     */
    public void updateViewShow(int position) {
        if (touchFaceId != 0) {
            boolean isShow = position == 1;
            if (!isShowAll) {
                View view = views.get(touchFaceId);
                view.setVisibility(isShow ? VISIBLE : GONE);
            }
            MainInterfaceBean data = getData(touchFaceId);
            int flag = data.getFlag();
            //获取原来是否显示
            boolean original = isShowFlag(flag);
            if (isShow) {
                if (!original) {
                    //从隐藏变显示，则添加
                    flag += InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE;
                }
            } else {
                if (original) {
                    //从显示变隐藏，则减去
                    flag -= InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE;
                }
            }
            data.setFlag(flag);
            invalidate();
        }
    }

    /**
     * 更新字体
     */
    public void updateTextFont(String fontName) {
        if (touchFaceId != 0) {
            MainInterfaceBean data = getData(touchFaceId);
            String oldFontName = data.getFontName();
            if (!oldFontName.equals(fontName)) {
                View view = views.get(touchFaceId);
                TextView tv_interface = view.findViewById(R.id.tv_interface);
                Typeface typeface = getTypeface(fontName);
                tv_interface.setTypeface(typeface);
                data.setFontName(fontName);
                invalidate();
            }
        }
    }

    /**
     * 更新对齐方式
     */
    public void updateTextAlign(int position) {
        if (touchFaceId != 0) {
            MainInterfaceBean data = getData(touchFaceId);
            int align = getAlignByPosition(position);
            if (data.getAlign() != align) {
                View view = views.get(touchFaceId);
                TextView tv_interface = view.findViewById(R.id.tv_interface);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) tv_interface.getLayoutParams();
                setAlign(layoutParams, align);
                tv_interface.setLayoutParams(layoutParams);
                data.setAlign(align);
            }
        }
    }

    /**
     * 复位
     */
    public void reset() {
        for (int i = 0; i < mData.size(); i++) {
            MainInterfaceBean info = mData.get(i);
            info.setLx(1);
            info.setLy(1);
            info.setBx(20);
            info.setBy(20);
        }
        refresh();
    }

    /**
     * 是否显示隐藏状态的view
     *
     * @param tag
     * @param checked =true全部显示
     */
    public void showAll(int tag, boolean checked) {
        isShowAll = checked;
        refresh();
        updateLogoImg(tag, logoFilePath);
    }

    private void refresh() {
        ArrayList<MainInterfaceBean> temps = new ArrayList<>();
        temps.addAll(mData);
        initInterfaceView(temps);
    }

    public List<MainInterfaceBean> getData() {
        List<MainInterfaceBean> temps = new ArrayList<>();
        temps.addAll(mData);
        return temps;
    }
}
