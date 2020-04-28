package xlk.paperless.standard.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.protobuf.ByteString;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.view.MyApplication;

import static xlk.paperless.standard.view.draw.DrawPresenter.launchPersonId;
import static xlk.paperless.standard.view.draw.DrawPresenter.mSrcmemid;
import static xlk.paperless.standard.view.draw.DrawPresenter.mSrcwbid;
import static xlk.paperless.standard.view.draw.DrawPresenter.isSharing;
import static xlk.paperless.standard.view.draw.DrawPresenter.pathList;
import static xlk.paperless.standard.view.draw.DrawPresenter.points;
import static xlk.paperless.standard.view.draw.DrawPresenter.LocalSharingPathList;
import static xlk.paperless.standard.view.draw.DrawPresenter.localOperids;


/**
 * @author xlk
 * @date 2020/3/13
 * @Description: 画板
 */
public class ArtBoard extends View {

    private final String TAG = "ArtBoard-->";
    private int screenWidth;
    private int screenHeight;
    private boolean isCreate;
    private Paint mPaint;
    private int paintWidth = 10;//画笔宽度
    private int paintColor = Color.BLACK;//画笔默认颜色

    //设置画图样式
    public static final int DRAW_SLINE = 1;//曲线
    public static final int DRAW_CIRCLE = 2;//圆
    public static final int DRAW_RECT = 3;//矩形
    public static final int DRAW_LINE = 4;//直线
    public static final int DRAW_TEXT = 5;//文本
    public static final int DRAW_ERASER = 6;//橡皮擦
    private int currentDrawGraphics = DRAW_SLINE;//当前画笔默认是曲线
    private Paint mBitmapPaint;
    public static int artBoardWidth;
    public static int artBoardHeight;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private DrawPath drawPath;
    public static final List<DrawPath> LocalPathList = new ArrayList<>();//存放自己操作的path，撤销时使用
    private final int WRAP_WIDTH = 300;
    private final int WRAP_HEIGHT = 300;
    private final JniHandler jni = JniHandler.getInstance();
    private DrawTextListener mListener;
    private boolean drawAgain;
    public boolean drag = false;//是否拖动画板
//    private int currentArtBoardWidth, currentArtBoardHeight;//拖动后画板的宽高

    public ArtBoard(Context context) {
        this(context, null);
    }

    public ArtBoard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArtBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ArtBoard(Context context, int width, int height) {
        this(context);
        isCreate = true;
        screenWidth = width;
        screenHeight = height;
        artBoardWidth = width;
        artBoardHeight = height;
//        currentArtBoardWidth = width;
//        currentArtBoardHeight = height;
        initial();
    }

    private void initial() {
        initPaint();
        initCanvas();
    }

    public void setDrag(boolean drag) {
        this.drag = drag;
    }

    public void setDrawType(int type) {
        this.currentDrawGraphics = type;
        initPaint();
    }

    public void setPaintWidth(int width) {
        paintWidth = width;
        initPaint();
    }

    public void setPaintColor(int color) {
        paintColor = color;
        initPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!isCreate) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            LogUtil.e(TAG, "DrawBoard.onMeasure : --> width= " + width + ", height= " + height);
            if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(WRAP_WIDTH, WRAP_HEIGHT);
            } else if (widthMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(WRAP_WIDTH, height);
            } else if (heightMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(width, WRAP_HEIGHT);
            }
            artBoardWidth = width;
            artBoardHeight = height;
            initial();
        }
    }

    public void initCanvas() {
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        LogUtil.e(TAG, "initCanvas :  --> artBoardWidth= " + artBoardWidth + ", artBoardHeight= " + artBoardHeight);
        mBitmap = Bitmap.createBitmap(artBoardWidth, artBoardHeight, Bitmap.Config.ARGB_8888);
        mBitmap.eraseColor(Color.WHITE);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.WHITE);//设置画布的颜色
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);// 抗锯齿
        mPaint.setDither(true);// 防抖动
        mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置线段连接处的样式为圆弧连接
        mPaint.setStrokeCap(Paint.Cap.ROUND);// 设置两端的线帽为圆的
        mPaint.setStrokeWidth(paintWidth);// 画笔宽度
        switch (currentDrawGraphics) {
            case DRAW_SLINE:
            case DRAW_CIRCLE:
            case DRAW_RECT:
            case DRAW_LINE:
            case DRAW_TEXT:
                mPaint.setColor(paintColor);// 颜色
                break;
            case DRAW_ERASER:
                mPaint.setColor(Color.WHITE);
                break;
        }
    }

    public Bitmap getCanvasBmp() {
        Bitmap srcBitmap = mBitmap;
        return srcBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 在之前画板上画过得显示出来
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);//实时的显示
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (drag) {
            drag(event);
        } else {
            draw(event);
        }
        return true;
    }

    private float downX, downY;//拖动时按下
    private int l = 0, t = 0, r, b;//拖动时

    private void drag(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                float dx = moveX - downX;//负数,说明是向左滑动
                float dy = moveY - downY;//负数,说明是向上滑动
                int left = getLeft();
                int top = getTop();
                //左
                if (left == 0) {
                    if (dx < 0 && getRight() >= screenWidth)
                        l = (int) (left + dx);
                    else l = 0;
                } else if (left < 0) {
                    if (getRight() >= screenWidth) {
                        if (dx < 0)
                            l = (int) (left + dx);
                        else if (dx > 0)
                            l = (int) (left + dx);
                    }
                } else l = 0;
                //上
                if (top == 0) {
                    if (dy < 0 && getBottom() > screenHeight)
                        t = (int) (top + dy);
                    else t = 0;
                } else if (top < 0) {
                    if (dy < 0 && getBottom() > screenHeight)
                        t = (int) (top + dy);
                    else if (dy > 0)
                        t = (int) (top + dy);
                } else t = 0;
                //右
                r = artBoardWidth + l;
                int i1 = screenWidth - r;
                if (i1 > 0) {
                    r = screenWidth;
                    l += i1;
                }
                //下
                b = artBoardHeight + t;
                int i = screenHeight - b;
                if (i > 0) {
                    b = screenHeight;
                    t += i;
                }
                this.layout(l, t, r, b);
//                currentArtBoardWidth = r - l;
//                currentArtBoardHeight = b - t;
//                LogUtil.d(TAG, "drag -->" + "拖动后画板的宽高：" + currentArtBoardWidth + " , " + currentArtBoardHeight);
                break;
        }
    }

    private float startX, startY;
    private float tempX, tempY;//临时坐标点

    private void draw(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                mPath = new Path();
                mPath.moveTo(x, y);
                tempX = x;
                tempY = y;
                initPaint();
                if (currentDrawGraphics == DRAW_SLINE) {//只有绘制曲线的时候才去添加
                    points.add(new PointF(x, y));//添加按下时的点
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                switch (currentDrawGraphics) {
                    case DRAW_SLINE:
                        points.add(new PointF(x, y));//添加移动时的点
                        drawSLine(x, y);//曲线
                        break;
                    case DRAW_CIRCLE://圆
                        drawOval(x, y);
                        break;
                    case DRAW_RECT://矩形
                        drawRect(x, y);
                        break;
                    case DRAW_LINE://直线
                        drawLine(x, y);
                        break;
                    case DRAW_ERASER://橡皮搽
                        if (isSharing) {
                            eraserPath(x, y);
                        } else {
                            eraser(x, y);
                        }
                        break;
                }
                tempX = x;
                tempY = y;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (currentDrawGraphics != DRAW_TEXT /*&& currentDrawGraphics != DRAW_ERASER*/) {
                    drawPath = new DrawPath();
                    drawPath.path = new Path(mPath);
                    drawPath.paint = new Paint(mPaint);
                    LocalPathList.add(drawPath);
                    LogUtil.d(TAG, "onTouchEvent: LocalPathList.size() : " + LocalPathList.size());
                }
                if (isSharing && currentDrawGraphics != DRAW_TEXT && currentDrawGraphics != DRAW_ERASER) {
                    LocalSharingPathList.add(drawPath);
                }
                if (currentDrawGraphics == DRAW_TEXT) {
                    mListener.showEdtPop(x, y);
                } else {
                    mCanvas.drawPath(mPath, mPaint);
                    mPath = null;
                    invalidate();
                }
                if (isSharing) shareDraw(x, y);
                points.clear();//不管有没有同屏都要删除
                break;
        }
    }

    private void shareDraw(float x, float y) {
        List<Float> allpt = new ArrayList<>();
        allpt.add(startX);
        allpt.add(startY);
        allpt.add(x);
        allpt.add(y);
        switch (currentDrawGraphics) {
            case DRAW_RECT:
                addDrawShape(InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_RECTANGLE.getNumber(), allpt);
                break;
            case DRAW_LINE:
                addDrawShape(InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_LINE.getNumber(), allpt);
                break;
            case DRAW_CIRCLE:
                addDrawShape(InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_ELLIPSE.getNumber(), allpt);
                break;
            case DRAW_SLINE:
                addInk(points);
                break;
        }
    }

    //添加墨迹
    private void addInk(List<PointF> allpt) {
        long srcwbid = mSrcwbid;
        int srcmemid = mSrcmemid;
        long utcstamp = System.currentTimeMillis();
        int operid = (int) (utcstamp / 10);
        localOperids.add(operid);
        int opermemberid = MyApplication.localMemberId;
        int figuretype = InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_INK.getNumber();
        jni.addInk(operid, opermemberid, srcmemid, srcwbid, utcstamp, figuretype, paintWidth, paintColor, allpt);
    }

    //添加圆形、矩形、直线
    private void addDrawShape(int type, List<Float> allpt) {
        long utcstamp = System.currentTimeMillis();
        int operid = (int) (utcstamp / 10);
        localOperids.add(operid);
        int opermemberid = MyApplication.localMemberId;
        int srcmemid = mSrcmemid;
        for (int i = 0; i < allpt.size(); i++) {
            LogUtil.e(TAG, "addDrawShape   --->>> " + allpt.get(i));
        }
        jni.addDrawFigure(operid, opermemberid, srcmemid,/*:发起人的人员ID*/
                mSrcwbid, utcstamp, type, paintWidth, paintColor, allpt);
    }

    private void eraserPath(float x, float y) {
        for (int i = 0; i < LocalSharingPathList.size(); i++) {
            DrawPath drawPath = LocalSharingPathList.get(i);
            PathMeasure pm = new PathMeasure(drawPath.path, false);
            float length = pm.getLength();
            Path tempPath = new Path();
            pm.getSegment(0, length, tempPath, false);
            float[] fa = new float[2];
            float sc = 0;
            while (sc < 1) {
                sc += 0.001;
                pm.getPosTan(sc * length, fa, null);
                if (Math.abs((int) fa[0] - (int) x) <= 20 && Math.abs((int) fa[1] - (int) y) <= 20) {
                    sc = 1;
                    LogUtil.e(TAG, "eraser: 查找到对应的坐标,撤销当前path...............................................................................");
                    long srcwbid = mSrcwbid;
                    long utcstamp = System.currentTimeMillis();
                    //操作ID 发送之前最后一个的操作ID
                    int operid = localOperids.get(i);
                    int opermemberid = MyApplication.localMemberId;
                    int srcmemid = mSrcmemid;
                    int figuretype = InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_ZERO.getNumber();
                    jni.whiteBoardDeleteRecord(opermemberid, operid, opermemberid, srcmemid, srcwbid, utcstamp, figuretype);
                    if (localOperids.size() > 0) {
                        //已经撤销后就要删除最后一个
                        localOperids.remove(i);
                    }
                    initCanvas();
                    LocalPathList.remove(drawPath);//删除指定drawpath
                    LocalSharingPathList.remove(i);
                    //删除最后得一个操作，再从新绘制
                    drawAgain(LocalPathList);
                    //将其他人的绘制信息也重新绘制
                    drawAgain(pathList);
                    i--;
                }
            }
        }
    }

    /**
     * 正常橡皮搽功能
     *
     * @param x
     * @param y
     */
    private void eraser(float x, float y) {
        initPaint();
        drawSLine(x, y);
    }

    private void drawLine(float x, float y) {
        float dx = Math.abs(x - tempX);
        float dy = Math.abs(tempY - y);
        if (dx >= 4 || dy >= 4) {
            mPath.reset();
            mPath.moveTo(startX, startY);
            mPath.lineTo(x, y);
        }
    }

    private void drawRect(float x, float y) {
        float dx = Math.abs(x - tempX);
        float dy = Math.abs(tempY - y);
        float sx = startX;//关键代码,
        float sy = startY;//每次进入都保存好起点坐标
        if (dx >= 4 || dy >= 4) {
            mPath.reset();
            if (sx > x) {
                float a = sx;
                sx = x;
                x = a;
            }
            if (sy > y) {
                float b = sy;
                sy = y;
                y = b;
            }
            RectF rectF = new RectF(sx, sy, x, y);
            mPath.addRect(rectF, Path.Direction.CCW);
        }
    }

    private void drawOval(float x, float y) {
        float dx = Math.abs(x - tempX);
        float dy = Math.abs(tempY - y);
        if (dx >= 4 || dy >= 4) {
            mPath.reset();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mPath.addOval(startX, startY, x, y, Path.Direction.CCW);
            } else {
                float r = Math.abs(y - startY) / 2;
                float rx = x - ((x - startX) / 2);//有可能相减是负数
                float ry = y - ((y - startY) / 2);
                mPath.addCircle(rx, ry, r, Path.Direction.CCW);
            }
        }
    }

    private void drawSLine(float x, float y) {
        float dx = Math.abs(x - tempX);
        float dy = Math.abs(tempY - y);
        if (dx >= 4 || dy >= 4) {
            int cx = (int) Math.abs(x - tempX);
            int cy = (int) Math.abs(y - tempY);
            if (cx > 3 || cy > 3) {
                mPath.quadTo(tempX, tempY, (tempX + x) / 2, (tempY + y) / 2);
            }
        }
    }

    public void drawText(final float x, final float y, String drawText) {
        float ex = x;
        float ey = y;
        int size;
//        控制文本的大小（不能太小）
        if (paintWidth < 30) {
            size = 30;
        } else {
            size = paintWidth;
        }
//        控制文本完全显示出来（小于某个值顶部会隐藏）
        if (y < 50) {
            if (size == 30) {
                ey = 30;
            } else if (size < 60) {
                ey = 50;
            } else if (size > 60) {
                ey = 80;
            }
        }
        int finalSize = 30;
        float fx = ex;
        float fy = ey;
        mPaint.setTextSize(finalSize);
        mPaint.setStyle(Paint.Style.FILL);
        Rect rect = new Rect();
        mPaint.getTextBounds(drawText, 0, drawText.length(), rect);
        int width = rect.width();//输入的所有文本的宽度
        int height = rect.height();//文本的高度（用于换行显示）
//        float remainWidth = screenWidth - fx;//可容许显示文本的宽度
//        float remainWidth = currentArtBoardWidth - fx;//可容许显示文本的宽度
        float remainWidth = artBoardWidth - fx;//可容许显示文本的宽度
        LogUtil.e(TAG, "drawText:   --->>> 字符串的宽度： " + width + "  可容许显示的宽度："
                + remainWidth + "  字符串的高度：" + height + "  输入的文本：" + drawText + "  字符串的个数：" + drawText.length());
        int ilka = width / drawText.length();//每个文本的宽度
        int canSee = (int) (remainWidth / ilka);//可以显示的文本个数
        if (canSee > 2) {
            if (remainWidth < width) {// 小于所有文本的宽度(不够显示才往下叠)
                funDraw(mPaint, height, canSee - 1, fx, fy, drawText);
            } else {
                mCanvas.drawText(drawText, fx, fy, mPaint);
            }
            invalidate();
            //将绘制的信息保存到本机绘制列表中
            DrawPath drawPath = new DrawPath();
            drawPath.paint = new Paint(mPaint);
            drawPath.text = drawText;
            drawPath.pointF = new PointF(fx, fy);
            drawPath.height = height;
            drawPath.lw = (int) remainWidth;
            drawPath.rw = width;
            drawPath.cansee = canSee;
            LocalPathList.add(drawPath);
            if (isSharing) {//如果当前在共享中，则发送添加字体
                LocalSharingPathList.add(drawPath);
                addText(x, y, finalSize, drawText);
            }
        }
    }

    private void addText(float x, float y, int finalSize, String drawText) {
        long time = System.currentTimeMillis();
        int operid = (int) (time / 10);
        localOperids.add(operid);
        jni.addText(operid, MyApplication.localMemberId, launchPersonId, mSrcwbid,
                time, InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_FREETEXT.getNumber(),
                finalSize, 2, paintColor, "宋体", x, y, drawText);
    }


    public Bitmap drawZoomBmp(Bitmap bitmap) {
        if (!drawAgain) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            setCanvasSize(width, height);
        }
        drawAgain = false;
        mCanvas.drawBitmap(bitmap, 0, 0, new Paint());
        invalidate();
        return bitmap;
    }

    //设置画板大小
    public void setCanvasSize(int w, int h) {
        if (w > artBoardWidth || h > artBoardHeight) {
            if (w > artBoardWidth) artBoardWidth = w;
            if (h > artBoardHeight) artBoardHeight = h;
        } else return;
        initCanvas();
        drawAgain(LocalPathList);
        drawAgain(pathList);
    }

    //撤销
    public void revoke() {
        if (LocalPathList != null && LocalPathList.size() > 0) {
            initCanvas();
            LocalPathList.remove(LocalPathList.size() - 1);
            //重新绘制本机存储的path
            drawAgain(LocalPathList);
            //将其他人的绘制信息也重新绘制
            drawAgain(pathList);
        }
        if (isSharing && localOperids.size() > 0) {
            long srcwbid = mSrcwbid;
            long utcstamp = System.currentTimeMillis();
            //操作ID 发送之前最后一个的操作ID
            int operid = localOperids.get(localOperids.size() - 1);
            int opermemberid = MyApplication.localMemberId;
            int srcmemid = mSrcmemid;
            int figuretype = InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_ZERO.getNumber();
            jni.whiteBoardDeleteRecord(opermemberid, operid, opermemberid, srcmemid, srcwbid, utcstamp, figuretype);
            localOperids.remove(localOperids.size() - 1);//删除自己同屏时最后产生的操作ID
        }
    }

    public void drawAgain(List<DrawPath> savePath) {
        for (DrawPath next : savePath) {
            if (next.paint != null) {
                if (next.path != null) {
                    mCanvas.drawPath(next.path, next.paint);
                } else if (next.text != null) {
                    if (next.lw < next.rw) {
                        funDraw(next.paint, next.height, next.cansee, (int) next.pointF.x, (int) next.pointF.y, next.text);
                    } else {
                        mCanvas.drawText(next.text, next.pointF.x, next.pointF.y, next.paint);
                    }
                }
            } else if (next.picdata != null) {
                drawAgain = true;
                drawZoomBmp(ConvertUtil.bs2bmp(next.picdata));
            }
        }
        invalidate();
    }

    public void funDraw(Paint paint, float height, int canSee, float fx, float fy, String text) {
        if (text.length() > canSee) {
            String canSeeText = text.substring(0, canSee);
            mCanvas.drawText(canSeeText, fx, fy, paint);
            String substring = text.substring(canSee, text.length());//获得剩下无法显示的文本
            if (substring.length() > 0) {
                funDraw(paint, height, canSee, fx, fy + height, substring);
            }
        } else {
            mCanvas.drawText(text, fx, fy, paint);
        }
    }

    //清空
    public void clear() {
        initCanvas();
        invalidate();
        LocalPathList.clear();//清空自己的操作
        LocalSharingPathList.clear();//清空同屏时自己的操作
        localOperids.clear();//清空同屏时自己产生的操作ID
        drawAgain(pathList);//重新绘制其它用户的操作
        if (isSharing) {
            long utcstamp = System.currentTimeMillis();
            int operid = (int) (utcstamp / 10);
            int opermemberid = MyApplication.localMemberId;
            int srcmemid = mSrcmemid;
            long srcwbid = mSrcwbid;
            int figuretype = InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_ZERO.getNumber();
            //发送清空消息
            jni.whiteBoardClearRecord(operid, opermemberid, srcmemid, srcwbid, utcstamp, figuretype);
        }
    }

    public void drawPath(Path path, Paint paint) {
        mCanvas.drawPath(path, paint);
    }

    public void drawText(String ptext, float lx, float ly, Paint paint) {
        mCanvas.drawText(ptext, lx, ly, paint);
    }

    public static class DrawPath {
        public Paint paint; //画笔
        public Path path = null; //路径
        public int operid;//操作ID
        public int opermemberid;//当前该命令的人员ID
        public long srcwbid;//发起人的白板标识
        public int srcmemid;//发起人的人员ID

        public String text = null;//绘制的文本
        public PointF pointF;//添加文本的起点（x,y）
        public int height;//文本的高度（每个字的高度）
        public int cansee;//可以显示的文本的个数
        public int lw;//可容许显示文本的区域宽度
        public int rw;//所有文本的宽度
        public ByteString picdata;//图片数据
    }

    public void setDrawTextListener(DrawTextListener listener) {
        mListener = listener;
    }

    public interface DrawTextListener {
        void showEdtPop(float x, float y);
    }
}
