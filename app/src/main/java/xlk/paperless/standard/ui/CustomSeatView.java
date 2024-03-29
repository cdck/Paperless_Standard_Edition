package xlk.paperless.standard.ui;

import android.content.Context;
import android.graphics.Region;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;
import xlk.paperless.standard.data.bean.SeatBean;
import xlk.paperless.standard.util.ToastUtil;

/**
 * @author Created by xlk on 2020/10/31.
 * @desc 会议室席位设置，可拖动席位
 */
public class CustomSeatView extends AbsoluteLayout {

    private final String TAG = "CustomSeatView -->";
    /**
     * 设备朝向
     */
    public static final int TOP_DIRECTION = 0;
    public static final int DOWN_DIRECTION = 1;
    public static final int LEFT_DIRECTION = 2;
    public static final int RIGHT_DIRECTION = 3;

    /**
     * 底图的宽高
     */
    private int width = 1300, height = 760;
    /**
     * 显示区域的宽高
     */
    private int viewWidth, viewHeight;
    /**
     * 席位的固定宽高
     */
    private final int SEAT_WIDTH = 120;
    private final int SEAT_HEIGHT = 60;
    /**
     * 席位之间的间距
     */
    private final int PADDING = 2;
    /**
     * 拖动底图时按下的坐标
     */
    private float touchDownX, touchDownY;
    private int left, top, right, bottom;
    /**
     * 所有的座位View
     */
    List<View> subViews = new ArrayList<>();
    /**
     * 存放选中的view的id
     */
    List<Integer> selectedIds = new ArrayList<>();
    /**
     * 所有座位所占的区域
     */
    List<Region> regions = new ArrayList<>();
    /**
     * 拖动底图时按下的时间
     */
    private long touchDownTime;
    /**
     * 按下时获取到的座位索引
     */
    int touchSeatIndex = -1;
    /**
     * 存放席位数据
     */
    private ArrayList<SeatBean> seatBeans = new ArrayList<>();
    /**
     * 是否隐藏参会人名称,默认不隐藏
     */
    private boolean hideMember = false;
    /**
     * 是否隐藏座位图标,默认不隐藏
     */
    private boolean hidePic = false;
    /**
     * 只能选中一个席位，默认false可以选中多个
     */
    private boolean chooseSingle = false;
    /**
     * 是否可以拖动底图,默认true可拖动
     */
    private boolean canDrag = true;
    /**
     * 是否可以拖动席位,默认true可拖动
     */
    private boolean canDragSeat = true;
    /**
     * 是否可以选中席位
     */
    private boolean canChoose = true;

    public void setCanChoose(boolean canChoose) {
        this.canChoose = canChoose;
    }

    public void setCanDrag(boolean drag) {
        canDrag = drag;
    }

    public void setCanDragSeat(boolean canDragSeat) {
        this.canDragSeat = canDragSeat;
    }

    public void setHideMember(boolean hide) {
        hideMember = hide;
    }

    public void setHidePic(boolean hide) {
        if (hide != hidePic) {
            hidePic = hide;
            refresh();
        }
    }

    public void setChooseSingle(boolean value) {
        chooseSingle = value;
    }

    /**
     * 设置显示区域的宽高
     */
    public void setViewSize(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        Log.i(TAG, "setViewSize 显示区域大小=" + viewWidth + "," + viewHeight);
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    /**
     * 设置底图的宽高
     */
    public void setImgSize(int width, int height) {
        this.width = width;
        this.height = height;
        Log.i(TAG, "setImgSize 底图区域大小=" + width + "," + height);
    }

    public List<Integer> getSelectedIds() {
        return selectedIds;
    }

    /**
     * 设置选中席位
     */
    public void setSelected(int id) {
        if (chooseSingle) {
            View clickView = null;
            selectedIds.clear();
            for (int i = 0; i < subViews.size(); i++) {
                View view = subViews.get(i);
                view.setSelected(false);
                if (view.getId() == id) {
                    clickView = view;
                    selectedIds.add(id);
                }
            }
            if (clickView != null) {
                clickView.setSelected(true);
            }
        } else {
            for (int i = 0; i < subViews.size(); i++) {
                View view = subViews.get(i);
                int viewId = view.getId();
                if (viewId == id) {
                    if (selectedIds.contains(viewId)) {
                        selectedIds.remove(selectedIds.indexOf(viewId));
                        view.setSelected(false);
                    } else {
                        selectedIds.add(viewId);
                        view.setSelected(true);
                    }
                    break;
                }
            }
        }
    }

    /**
     * 获取当前点击的席位索引
     *
     * @param x x坐标
     * @param y y坐标
     * @return 返回-1表示没有点击到席位
     */
    private int getCurrentClickSeat(int x, int y) {
        for (int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);
            if (region.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    public CustomSeatView(Context context) {
        this(context, null);
    }

    public CustomSeatView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSeatView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomSeatView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 重新设置席位信息
     *
     * @param data 席位数据
     */
    public void addSeat(List<SeatBean> data) {
        removeAllViews();
        seatBeans.clear();
        seatBeans.addAll(data);
        subViews.clear();
        regions.clear();
        Log.i(TAG, "addSeat 最新宽高=" + width + "," + height + ",data.size=" + data.size());
        for (int i = 0; i < seatBeans.size(); i++) {
            SeatBean seatBean = seatBeans.get(i);
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.item_seat, null);

            RelativeLayout.LayoutParams deviceLayoutParams = new RelativeLayout.LayoutParams(120, 20);
            RelativeLayout.LayoutParams seatLinearParams = new RelativeLayout.LayoutParams(120, 40);
            ImageView item_seat_iv = inflate.findViewById(R.id.item_seat_iv);
//        item_seat_iv.setVisibility(isShow ? View.VISIBLE : View.GONE);
            TextView item_seat_device = inflate.findViewById(R.id.item_seat_device);
            item_seat_device.setTextSize(7);
            LinearLayout item_seat_ll = inflate.findViewById(R.id.item_seat_ll);
            TextView item_seat_member = inflate.findViewById(R.id.item_seat_member);
            item_seat_member.setTextSize(7);

            String devName = seatBean.getDevName();
            if (!TextUtils.isEmpty(devName)) {
                item_seat_device.setText(devName);
            } else {
                item_seat_device.setVisibility(View.INVISIBLE);
            }
            boolean isChecked = seatBean.getIssignin() == 1;
            item_seat_iv.setImageResource(isChecked ? R.drawable.icon_signin : R.drawable.icon_un_signin);
            item_seat_device.setSelected(isChecked);
            item_seat_ll.setSelected(isChecked);
            String memberName = seatBean.getMemberName();
            if (!TextUtils.isEmpty(memberName)) {
                item_seat_member.setText(memberName);
            } else {
                item_seat_member.setVisibility(View.INVISIBLE);
            }

            deviceLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            deviceLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            seatLinearParams.addRule(RelativeLayout.BELOW, item_seat_device.getId());
            seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            item_seat_device.setLayoutParams(deviceLayoutParams);
            item_seat_ll.setLayoutParams(seatLinearParams);
            //左上角x坐标
            float x1 = seatBean.getX();
            //左上角y坐标
            float y1 = seatBean.getY();
            if (x1 > 1) {
                x1 = 1;
            } else if (x1 < 0) {
                x1 = 0;
            }
            if (y1 > 1) {
                y1 = 1;
            } else if (y1 < 0) {
                y1 = 0;
            }
            int x = (int) (x1 * width);
            int y = (int) (y1 * height);
            if (x > width - SEAT_WIDTH) {
                Log.d(TAG, "addSeat :  X轴超过 --> ");
                x = width - SEAT_WIDTH;
            }
            if (y > height - SEAT_HEIGHT) {
                Log.i(TAG, "addSeat :  Y轴超过 --> ");
                y = height - SEAT_HEIGHT;
            }
            seatBean.setX((float) x / width);
            seatBean.setY((float) y / height);
            LayoutParams params = new LayoutParams(
                    SEAT_WIDTH, SEAT_HEIGHT,
                    x, y);
            inflate.setLayoutParams(params);
            int devId = seatBean.getDevId();
            inflate.setId(devId);
            Region region = new Region(x, y, x + SEAT_WIDTH, y + SEAT_HEIGHT);
            if (selectedIds.contains(devId)) {
                inflate.setSelected(true);
            }
            regions.add(region);
            subViews.add(inflate);
            addView(inflate);

//            int x = (int) (x1 * width);
//            int y = (int) (y1 * height);
//
//            AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(120, 60, x, y);
//            inflate.setLayoutParams(params);
//            addView(inflate);
            /*RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(30, 30);
            RelativeLayout.LayoutParams seatLinearParams = new RelativeLayout.LayoutParams(SEAT_WIDTH, 40);
            ImageView item_seat_iv = inflate.findViewById(R.id.item_seat_iv);
            LinearLayout item_seat_ll = inflate.findViewById(R.id.item_seat_ll);
            TextView item_seat_device = inflate.findViewById(R.id.item_seat_device);
            TextView item_seat_member = inflate.findViewById(R.id.item_seat_member);
            switch (seatBean.getDirection()) {
                //上
                case TOP_DIRECTION:
                    item_seat_iv.setImageResource(R.drawable.icon_seat_bottom);
                    ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    seatLinearParams.addRule(RelativeLayout.BELOW, item_seat_iv.getId());
                    seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    break;
                //下
                case DOWN_DIRECTION:
                    item_seat_iv.setImageResource(R.drawable.icon_seat_top);
                    seatLinearParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    ivParams.addRule(RelativeLayout.BELOW, item_seat_ll.getId());
                    ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    break;
                //左
                case LEFT_DIRECTION:
                    item_seat_iv.setImageResource(R.drawable.icon_seat_right);
                    ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    seatLinearParams.addRule(RelativeLayout.BELOW, item_seat_iv.getId());
                    seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    break;
                //右
                case RIGHT_DIRECTION:
                    item_seat_iv.setImageResource(R.drawable.icon_seat_left);
                    ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    seatLinearParams.addRule(RelativeLayout.BELOW, item_seat_iv.getId());
                    seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    break;
                default:
                    break;
            }
            item_seat_device.setText(seatBean.getDevName());
            item_seat_member.setText(seatBean.getMemberName());

            item_seat_iv.setLayoutParams(ivParams);
            item_seat_ll.setLayoutParams(seatLinearParams);
            item_seat_member.setVisibility(hideMember ? GONE : VISIBLE);
            item_seat_iv.setVisibility(hidePic ? GONE : VISIBLE);

            //左上角x坐标
            float x1 = seatBean.getX();
            //左上角y坐标
            float y1 = seatBean.getY();
            Log.e(TAG, "addSeat -->设备id=" + seatBean.getDevId() + ",坐标百分比=" + x1 + "," + y1);
            if (x1 > 1) {
                x1 = 1;
            } else if (x1 < 0) {
                x1 = 0;
            }
            if (y1 > 1) {
                y1 = 1;
            } else if (y1 < 0) {
                y1 = 0;
            }
            int x = (int) (x1 * width);
            int y = (int) (y1 * height);
            if (x > width - SEAT_WIDTH) {
                Log.d(TAG, "addSeat :  X轴超过 --> ");
                x = width - SEAT_WIDTH;
            }
            if (y > height - SEAT_HEIGHT) {
                Log.i(TAG, "addSeat :  Y轴超过 --> ");
                y = height - SEAT_HEIGHT;
            }
            seatBean.setX((float) x / width);
            seatBean.setY((float) y / height);
            LayoutParams params = new LayoutParams(
                    SEAT_WIDTH, SEAT_HEIGHT,
                    x, y);
            inflate.setLayoutParams(params);
            int devId = seatBean.getDevId();
            inflate.setId(devId);
            Region region = new Region(x, y, x + SEAT_WIDTH, y + SEAT_HEIGHT);
            if (selectedIds.contains(devId)) {
                inflate.setSelected(true);
            }
            regions.add(region);
            subViews.add(inflate);
            addView(inflate);
             */
        }
    }

    /**
     * 拖动席位后更新区域
     *
     * @param index 索引位
     * @param left  新的区域左边坐标
     * @param top   新的区域上边坐标
     */
    private void updateRegion(int index, int left, int top) {
        Region region = regions.get(index);
        region.set(left, top, left + SEAT_WIDTH, top + SEAT_HEIGHT);
    }

    /**
     * 更新某席位的数据
     *
     * @param index 索引
     * @param x1    新的x坐标
     * @param y1    新的y坐标
     */
    private void updateSeatBean(int index, float x1, float y1) {
        SeatBean seatBean = seatBeans.get(index);
        float x = x1 / width;
        float y = y1 / height;
        seatBean.setX(x);
        seatBean.setY(y);
        Log.i(TAG, "updateSeatBean 设备id=" + seatBean.getDevId() + ",更新列表数据 x=" + x + ",y=" + y + ",width=" + width + ",height=" + height);
    }

    /**
     * 获取最新的席位数据
     *
     * @return 返回拖动修改过的数据
     */
    public List<SeatBean> getSeatBean() {
        List<SeatBean> temps = new ArrayList<>();
        temps.addAll(seatBeans);
        return temps;
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
        this.width = width;
        this.height = height;
        Log.d(TAG, "onMeasure --> width=" + width + ", height=" + height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        left = l;
        top = t;
        right = r;
        bottom = b;
        Log.d(TAG, "onLayout --> " + left + "," + top + "," + right + "," + bottom);
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //是否可以左右拖动
        boolean canDragX = viewWidth < width;
        //是否可以上下拖动
        boolean canDragY = viewHeight < height;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                touchDownX = event.getX();
                touchDownY = event.getY();
                touchSeatIndex = getCurrentClickSeat((int) touchDownX, (int) touchDownY);
                touchDownTime = System.currentTimeMillis();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (canDrag) {
                    float moveX = event.getX();
                    float moveY = event.getY();
                    float dx = moveX - touchDownX;
                    float dy = moveY - touchDownY;
                    if (canDragSeat && touchSeatIndex != -1) {
                        dragSeatView(moveX, moveY, dx, dy);
                    } else {
                        dragParentView(canDragX, canDragY, dx, dy);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                Log.i(TAG, "move up  显示宽高：" + viewWidth + "," + viewHeight
                        + ",底图宽高：" + width + "," + height
                        + ",canDragX：" + canDragX + ",canDragY：" + canDragY);
                Log.e(TAG, "move up --> " + left + "," + top + "," + right + "," + bottom);
                if (System.currentTimeMillis() - touchDownTime < 200) {
                    //点击
                    if (canChoose && touchSeatIndex != -1) {
                        setSelected(subViews.get(touchSeatIndex).getId());
                    }
                }
                break;
            }
            default:
                break;
        }
        return true;
    }

    private void dragParentView(boolean canMoveX, boolean canMoveY, float dx, float dy) {
        if (canMoveX) {
            //左
            if (left == 0) {
                //当前左边已经封顶了
                if (dx < 0 && right >= viewWidth) {
                    //还往左边移动
                    left = (int) (left + dx);
                } else {
                    left = 0;
                }
            } else if (left < 0) {
                //当前已经超过最左边
                if (right >= viewWidth) {
                    if (dx < 0) {
                        this.left = (int) (left + dx);
                    } else if (dx > 0) {
                        this.left = (int) (left + dx);
                    }
                }
            } else {
                this.left = 0;
            }
            //右
            right = width + this.left;
            int i1 = viewWidth - right;
            if (i1 > 0) {
                right = viewWidth;
                this.left += i1;
            }
        } else {
            //不可以拖动X轴
            left = 0;
            right = width;
        }
        if (canMoveY) {
            //上
            if (top == 0) {
                if (dy < 0 && bottom > viewHeight) {
                    top = (int) (top + dy);
                } else {
                    top = 0;
                }
            } else if (top < 0) {
                if (dy < 0 && bottom > viewHeight) {
                    top = (int) (top + dy);
                } else if (dy > 0) {
                    top = (int) (top + dy);
                }
            } else {
                top = 0;
            }
            //下
            bottom = height + top;
            int i = viewHeight - bottom;
            if (i > 0) {
                bottom = viewHeight;
                top += i;
            }
        } else {
            //不可以拖动Y轴
            top = 0;
            bottom = height;
        }
        this.layout(left, top, right, bottom);
    }

    private void dragSeatView(float moveX, float moveY, float dx, float dy) {
        //获取当前选中的座位view
        View view = subViews.get(touchSeatIndex);
        float x = view.getX();
        float y = view.getY();
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.x = (int) x;
        layoutParams.y = (int) y;
        Log.e(TAG, "dragSeatView :  --> 按下的坐标=【" + touchDownX + "," + touchDownY
                + "】,触摸坐标=【" + moveX + "," + moveY + "】, 座位原坐标=【" + layoutParams.x + "," + layoutParams.y + "】");
        //必须要重新设置该view的坐标
        layoutParams.x += dx;
        layoutParams.y += dy;
        if (layoutParams.x < 0) {
            layoutParams.x = 0;
        }
        if (layoutParams.x > width - SEAT_WIDTH) {
            layoutParams.x = width - SEAT_WIDTH;
        }
        if (layoutParams.y < 0) {
            layoutParams.y = 0;
        }
        if (layoutParams.y > height - SEAT_HEIGHT) {
            layoutParams.y = height - SEAT_HEIGHT;
        }
        Log.i(TAG, "dragSeatView 座位新坐标=" + layoutParams.x + "," + layoutParams.y);
        view.layout(layoutParams.x, layoutParams.y, layoutParams.x + SEAT_WIDTH, layoutParams.y + SEAT_HEIGHT);
        //更新 regions
        updateRegion(touchSeatIndex, layoutParams.x, layoutParams.y);
        //更新数据
        updateSeatBean(touchSeatIndex, layoutParams.x, layoutParams.y);
        touchDownX = moveX;
        touchDownY = moveY;
        Log.d(TAG, "dragSeatView :  move end --> ");
    }

    /**
     * 自动排序
     */
    public void autoSort() {
        //当前最大行数（加点间距）
        final int rowCount = height / (SEAT_HEIGHT + PADDING);
        //当前最大列数（加点间距）
        final int columnCount = width / (SEAT_WIDTH + PADDING);
        Log.e(TAG, "sort 最大行数和列数=" + rowCount + "," + columnCount + ",seatBeans.size=" + seatBeans.size());
        //记录当前行
        int currentRow = 1;
        int currentColumn = 0;
        boolean isFirst = true;
        for (int i = 1; i < seatBeans.size() + 1; i++) {
            View view = subViews.get(i - 1);
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if ((i - columnCount * currentRow) <= 0) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    //自增一列
                    currentColumn++;
                }
            } else {
                if (currentRow >= rowCount) {
                    //已经是最多行数了，则行数和列数保持不变（一直叠加到最后一个区域）
                    Log.e(TAG, "已经是最多行数了，则行数和列数保持不变（一直叠加到最后一个区域）");
                } else {
                    //换了一行了，行数进行增加，列数回到第一列
                    currentRow++;
                    currentColumn = 0;
                }
            }
            //当前的左上角X坐标值=当前列数*座位宽度
            int currentViewX = (currentColumn) * (SEAT_WIDTH + PADDING);
            //当前的左上角Y坐标值=上一行数*座位高度
            int currentViewY = (currentRow - 1) * (SEAT_HEIGHT + PADDING);
            layoutParams.x = currentViewX;
            layoutParams.y = currentViewY;
            view.layout(currentViewX, currentViewY, currentViewX + SEAT_WIDTH, currentViewY + SEAT_HEIGHT);
            updateRegion(i - 1, currentViewX, currentViewY);
            updateSeatBean(i - 1, currentViewX, currentViewY);
        }
        refresh();
    }

    /**
     * 更新选中座位的设备朝向
     *
     * @param value TOP_DIRECTION
     */
    public void updateDirection(int value) {
        boolean isSelected = false;
        for (int i = 0; i < seatBeans.size(); i++) {
            View view = subViews.get(i);
            if (view.isSelected()) {
                isSelected = true;
                SeatBean seatBean = seatBeans.get(i);
                seatBean.setDirection(value);
            }
        }
        if (isSelected) {
            refresh();
        } else {
            ToastUtil.show(R.string.please_choose_seat);
        }
    }


    /**
     * 设置选中席位左侧对齐
     */
    public void alignLeft() {
        float minX = -1;
        List<Integer> indexs = new ArrayList<>();
        for (int i = 0; i < seatBeans.size(); i++) {
            View view = subViews.get(i);
            SeatBean seatBean = seatBeans.get(i);
            boolean selected = view.isSelected();
            float x = seatBean.getX();
            if (selected) {
                indexs.add(i);
                if (minX == -1) {
                    minX = x;
                } else {
                    minX = Math.min(minX, x);
                }
            }
        }
        Log.d(TAG, "alignLeft :  所有选中的席位最靠左的值是 --> " + minX);
        if (indexs.size() < 2) {
            Log.e(TAG, "alignLeft :  至少需要选中两个席位 --> ");
            return;
        }
        for (int i = 0; i < indexs.size(); i++) {
            Integer index = indexs.get(i);
            SeatBean seatBean = seatBeans.get(index);
            seatBean.setX(minX);
        }
        refresh();
    }

    /**
     * 设置选中席位底部对齐
     */
    public void alignBottom() {
        float maxY = -1;
        List<Integer> indexs = new ArrayList<>();
        for (int i = 0; i < seatBeans.size(); i++) {
            View view = subViews.get(i);
            SeatBean seatBean = seatBeans.get(i);
            boolean selected = view.isSelected();
            float y = seatBean.getY();
            if (selected) {
                indexs.add(i);
                if (maxY == -1) {
                    maxY = y;
                } else {
                    maxY = Math.max(maxY, y);
                }
            }
        }
        Log.d(TAG, "alignLeft :  所有选中的席位最靠左的值是 --> " + maxY);
        if (indexs.size() < 2) {
            Log.e(TAG, "alignLeft :  至少需要选中两个席位 --> ");
            return;
        }
        for (int i = 0; i < indexs.size(); i++) {
            Integer index = indexs.get(i);
            SeatBean seatBean = seatBeans.get(index);
            seatBean.setY(maxY);
        }
        refresh();
    }

    public void refresh() {
        ArrayList<SeatBean> temps = new ArrayList<>();
        temps.addAll(seatBeans);
        addSeat(temps);
    }

}
