package xlk.paperless.standard.view.draw;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.List;

import xlk.paperless.standard.ui.ArtBoard;

/**
 * @author xlk
 * @date 2020/3/13
 * @Description:
 */
public interface IDraw {
    void drawZoomBmp(Bitmap bitmap);

    void setCanvasSize(int maxX, int maxY);

    void drawPath(Path path, Paint paint);

    void invalidate();

    void funDraw(Paint paint, float height, int canSee, float fx, float fy, String text);

    void drawText(String ptext, float lx, float ly, Paint paint);

    void initCanvas();

    void drawAgain(List<ArtBoard.DrawPath> pathList);

    void setBtnEnable(boolean enable);
}
