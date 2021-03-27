package xlk.paperless.standard.ui;

import android.content.Context;
import android.graphics.Region;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceTablecard;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.admin.fragment.pre.tablecard.TableCardBean;

/**
 * @author Created by xlk on 2020/11/5.
 * @desc
 */
public class TableCardView extends AbsoluteLayout {
    private final String TAG = "TableCardView-->";
    private int viewWidth, viewHeight;
    private int left, top, right, bottom;
    List<TableCardBean> mData = new ArrayList<>();
    private List<View> views = new ArrayList<>();
    List<Region> regions = new ArrayList<>();
    private float touchDownX;
    private float touchDownY;
    private int touchIndex;

    public TableCardView(Context context) {
        this(context, null);
    }

    public TableCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TableCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TableCardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

    public List<InterfaceTablecard.pbui_Item_MeetTableCardDetailInfo> getTableCardData() {
        List<InterfaceTablecard.pbui_Item_MeetTableCardDetailInfo> temps = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            TableCardBean info = mData.get(i);
            InterfaceTablecard.pbui_Item_MeetTableCardDetailInfo build = InterfaceTablecard.pbui_Item_MeetTableCardDetailInfo.newBuilder()
                    .setFontname(ConvertUtil.s2b(info.getFontName()))
                    .setFontsize(info.getFontSize())
                    .setFontcolor(info.getFontColor())
                    .setLx(info.getLx())
                    .setLy(info.getLy())
                    .setRx(info.getRx())
                    .setRy(info.getRy())
                    .setFlag(info.getFlag())
                    .setAlign(info.getAlign())
                    .setType(info.getType())
                    .build();
            temps.add(build);
        }
        return temps;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownX = event.getX();
                touchDownY = event.getY();
                touchIndex = getTouchView((int) touchDownX, (int) touchDownY);
                break;
            case MotionEvent.ACTION_MOVE:
                if (touchIndex != -1) {
                    float moveX = event.getX();
                    float moveY = event.getY();
                    float dx = moveX - touchDownX;
                    float dy = moveY - touchDownY;
                    View view = views.get(touchIndex);
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
                    view.layout(layoutParams.x, layoutParams.y, layoutParams.x + width, layoutParams.y + height);
                    touchDownX = moveX;
                    touchDownY = moveY;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (touchIndex != -1) {
                    View view = views.get(touchIndex);
                    int width = view.getWidth();
                    int height = view.getHeight();
                    LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
                    updateRegion(touchIndex, layoutParams.x, layoutParams.y, width, height);
                    updateData(touchIndex, layoutParams.x, layoutParams.y, width, height);
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void updateData(int touchIndex, int x, int y, int width, int height) {
        if (viewWidth > 0 && viewHeight > 0) {
            TableCardBean tableCardBean = mData.get(touchIndex);
            float lx = (float) x / viewWidth * 100;
            float ly = (float) y / viewHeight * 100;
            float rx = ((float) (x + width)) / viewWidth * 100;
            float ry = ((float) (y + height)) / viewHeight * 100;
            LogUtil.i(TAG, "updateData lx=" + lx + ",ly=" + ly + ",rx=" + rx + ",ry=" + ry);
            tableCardBean.setLx(lx);
            tableCardBean.setLy(ly);
            tableCardBean.setRx(rx);
            tableCardBean.setRy(ry);
        }
    }

    private void updateRegion(int touchIndex, int x, int y, int width, int height) {
        Region region = regions.get(touchIndex);
        region.set(x, y, x + width, y + height);
    }

    /**
     * 获取手指按下时触摸的位置索引
     */
    public int getTouchView(int x, int y) {
        for (int i = 0; i < regions.size(); i++) {
            if (regions.get(i).contains(x, y)) {
                TableCardBean tableCardBean = mData.get(i);
                int flag = tableCardBean.getFlag();
                //如果是隐藏的则不进行拖动，直接返回-1
                if ((flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE)
                        != InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE) {
                    LogUtil.i(TAG, "getTouchView 按下时的区域view是隐藏状态");
                    return -1;
                } else {
                    return i;
                }
            }
        }
        return -1;
    }

    public String getNameByType(int type) {
        switch (type) {
            case InterfaceMacro.Pb_TableCardType.Pb_conf_memname_VALUE:
                return "姓名";
            case InterfaceMacro.Pb_TableCardType.Pb_conf_job_VALUE:
                return "职位";
            case InterfaceMacro.Pb_TableCardType.Pb_conf_company_VALUE:
                return "单位";
            case InterfaceMacro.Pb_TableCardType.Pb_conf_position_VALUE:
                return "座位名";
            default:
                return "会议名称";
        }
    }

    private Typeface getTypeface(String fontName, boolean isbold) {
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
        if (isbold) {
            return Typeface.create(typeface, Typeface.BOLD);
        }
        return typeface;
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

    public void initView(List<TableCardBean> tableCardData) {
        removeAllViews();
        views.clear();
        regions.clear();
        mData.clear();
        mData.addAll(tableCardData);
        LogUtil.i(TAG, "initView 当前宽高=" + viewWidth + "," + viewHeight);
        for (int i = 0; i < mData.size(); i++) {
            TableCardBean item = mData.get(i);
            int flag = item.getFlag();
            boolean isBold = (flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE)
                    == InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE;
            boolean isShow = (flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE)
                    == InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE;
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.custom_table_card, null);
            inflate.setId(i);
            TextView table_card_tv = inflate.findViewById(R.id.table_card_tv);
            RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            table_card_tv.setText(getNameByType(item.getType()));
            table_card_tv.setTextColor(item.getFontColor());
            table_card_tv.setTypeface(getTypeface(item.getFontName(), isBold));
//            table_card_tv.setTypeface(isBold ? Typeface.defaultFromStyle(Typeface.BOLD) : Typeface.defaultFromStyle(Typeface.NORMAL));
            table_card_tv.setTextSize(item.getFontSize() / 2);
            int align = item.getAlign();
            setAlign(tvParams, align);
            table_card_tv.setLayoutParams(tvParams);

            float lx = item.getLx();
            float ly = item.getLy();
            float rx = item.getRx();
            float ry = item.getRy();
            //内容高度百分比
            float dy = ry - ly;
            //获取到内容的高度
            int contentHeight = (int) (viewHeight * (dy / 100));
            float v = new BigDecimal(dy).setScale(0, BigDecimal.ROUND_HALF_UP).floatValue();
            if (lx < 0) {
                lx = 0;
            }
            if (ly < 0) {
                ly = 0;
            }
            int x = (int) (viewWidth * (lx / 100));
            int y = (int) (viewHeight * (ly / 100));

            LayoutParams params = new LayoutParams(viewWidth, contentHeight, x, y);
            inflate.setLayoutParams(params);
            Region region = new Region(x, y, x + viewWidth, y + contentHeight);
            inflate.setVisibility(isShow ? VISIBLE : GONE);
            views.add(inflate);
            regions.add(region);
            addView(inflate);
        }
    }

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

    /**
     * 更新显示内容
     *
     * @param index    =0第一行，=1第二行，=3第三行
     * @param position Spinner选中的索引位
     */
    public void updateViewText(int index, int position) {
        int type = position + 1;
        View view = views.get(index);
        TextView table_card_tv = view.findViewById(R.id.table_card_tv);
        table_card_tv.setText(getNameByType(type));
        TableCardBean tableCardBean = mData.get(index);
        tableCardBean.setType(type);
        invalidate();
    }

    /**
     * 更新TextView的字体
     *
     * @param index    =0第一行，=1第二行，=3第三行
     * @param fontName 字体名称
     */
    public void updateViewFont(int index, String fontName) {
        View view = views.get(index);
        TextView table_card_tv = view.findViewById(R.id.table_card_tv);
        Typeface oldTypeFace = table_card_tv.getTypeface();
        boolean bold = oldTypeFace.isBold();
        Typeface typeface = getTypeface(fontName, bold);
        table_card_tv.setTypeface(typeface);
        TableCardBean tableCardBean = mData.get(index);
        tableCardBean.setFontName(fontName);
        invalidate();
    }

    /**
     * 更新是否加粗
     *
     * @param index    =0第一行，=1第二行，=3第三行
     * @param position =0不加粗，=1加粗
     */
    public void updateViewBold(int index, int position) {
        View view = views.get(index);
        TextView table_card_tv = view.findViewById(R.id.table_card_tv);
        boolean isBold = position == 1;
        Typeface typeface = table_card_tv.getTypeface();
        Typeface typeface1 = Typeface.create(typeface, isBold ? Typeface.BOLD : Typeface.NORMAL);
        table_card_tv.setTypeface(typeface1);
        TableCardBean tableCardBean = mData.get(index);
        int flag = tableCardBean.getFlag();
        int newFlag = flag;
        if (isBold) {
            if ((flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE) !=
                    InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE) {
                newFlag += InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE;
            }
        } else {
            if ((flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE) ==
                    InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE) {
                newFlag -= InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_BOLD_VALUE;
            }
        }
        tableCardBean.setFlag(newFlag);
        invalidate();
    }

    /**
     * 更新对齐方式
     *
     * @param index    =0第一行，=1第二行，=3第三行
     * @param position spinner索引
     */
    public void updateViewAlign(int index, int position) {
        View view = views.get(index);
        TextView table_card_tv = view.findViewById(R.id.table_card_tv);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) table_card_tv.getLayoutParams();
        int align = getAlignByPosition(position);
        setAlign(params, align);
        table_card_tv.setLayoutParams(params);
        TableCardBean tableCardBean = mData.get(index);
        tableCardBean.setAlign(align);
        invalidate();
    }

    public void updateViewHide(int index, int position) {
        LogUtil.i(TAG, "updateViewHide index=" + index + ",position=" + position);
        View view = views.get(index);
        boolean isHide = position == 1;
        view.setVisibility(isHide ? GONE : VISIBLE);
        TableCardBean tableCardBean = mData.get(index);
        int flag = tableCardBean.getFlag();
        int newFlag = flag;
        if (isHide) {
            //切换到隐藏
            if ((flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE)
                    == InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE) {
                LogUtil.d(TAG, "updateViewHide 原来是显示状态");
                newFlag -= InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE;
            }
        } else {
            //切换到显示
            //判断原来是否是显示
            if ((flag & InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE)
                    != InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE) {
                LogUtil.d(TAG, "updateViewHide 原来是隐藏状态");
                newFlag += InterfaceMacro.Pb_TableCardFlag.Pb_MEET_TABLECARDFLAG_SHOW_VALUE;
            }
        }
        LogUtil.i(TAG, "updateViewHide flag=" + flag + ",newFlag=" + newFlag);
        tableCardBean.setFlag(newFlag);
        invalidate();
    }

    /**
     * 更新字体颜色
     */
    public void updateViewColor(int index, int color) {
        if (views.size() <= index) return;
        View view = views.get(index);
        TableCardBean tableCardBean = mData.get(index);
        TextView table_card_tv = view.findViewById(R.id.table_card_tv);
        table_card_tv.setTextColor(color);
        tableCardBean.setFontColor(color);
        invalidate();
    }

    public void updateViewTextSize(int index, int size) {
        if (views.size() <= index) return;
        View view = views.get(index);
        TextView table_card_tv = view.findViewById(R.id.table_card_tv);
        table_card_tv.setTextSize(size / 2);
        TableCardBean tableCardBean = mData.get(index);
        tableCardBean.setFontSize(size);
        invalidate();
    }

    public void updateViewHeight(int index, float scale) {
        if (views.size() <= 0) return;
        LogUtil.i(TAG, "updateViewHeight viewWidth=" + viewWidth + ",viewHeight=" + viewHeight + ",scale=" + scale);
        View view = views.get(index);
        int width = view.getWidth();
        //计算出新的高度 scale一定不能是整数，不然只会取整
        int newHeight = (int) (viewHeight * (scale / 100));
        LogUtil.i(TAG, "updateViewHeight width=" + width + ",newHeight=" + newHeight);
        //更新数据
        TableCardBean tableCardBean = mData.get(index);
        tableCardBean.setRy(scale);
        //更新区域
        Region region = regions.get(index);
        int lx = (int) tableCardBean.getLx();
        int ly = (int) tableCardBean.getLy();
        region.set(lx, ly, lx + width, ly + newHeight);
        //更新View视图
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = newHeight;
        view.layout(layoutParams.x, layoutParams.y, layoutParams.x + width, layoutParams.y + newHeight);
    }

    public void setDefaultLocation() {
        for (int i = 0; i < mData.size(); i++) {
            TableCardBean info = mData.get(i);
            info.setLx(0);
            info.setLy(i * 34);
            info.setRx(100);
            info.setRy((i + 1) * 34);
        }
        refresh();
    }

    private void refresh() {
        LogUtil.i(TAG, "refresh ");
        List<TableCardBean> temps = new ArrayList<>();
        temps.addAll(mData);
        initView(temps);
    }
}
