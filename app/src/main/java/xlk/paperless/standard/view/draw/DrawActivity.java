package xlk.paperless.standard.view.draw;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.service.FabService;
import xlk.paperless.standard.ui.ArtBoard;
import xlk.paperless.standard.ui.ColorPickerDialog;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.util.UriUtil;
import xlk.paperless.standard.view.BaseActivity;
import xlk.paperless.standard.view.MyApplication;

import static android.content.Intent.ACTION_OPEN_DOCUMENT;
import static xlk.paperless.standard.util.ConvertUtil.bmp2bs;
import static xlk.paperless.standard.view.draw.DrawPresenter.isSharing;
import static xlk.paperless.standard.view.draw.DrawPresenter.mSrcmemid;
import static xlk.paperless.standard.view.draw.DrawPresenter.mSrcwbid;

public class DrawActivity extends BaseActivity implements IDraw, View.OnClickListener {


    private TextView draw_exit;
    private TextView draw_save;
    private TextView draw_clear;
    private TextView draw_picture;
    private TextView draw_font;
    private TextView draw_color;
    private TextView draw_revoke;
    private FrameLayout draw_fl;
    private TextView draw_round;
    private TextView draw_rect;
    private TextView draw_line;
    private TextView draw_curve;
    private TextView draw_pen;
    private TextView draw_text;
    private TextView draw_eraser;
    private TextView draw_drag;
    private Button draw_launch;
    private Button draw_stop;
    private AppCompatSeekBar draw_seekbar;
    private TextView draw_seekbar_tv;
    private DrawPresenter presenter;
    private final String TAG = "DrawActivity-->";
    List<TextView> selectTvs = new ArrayList<>();
    private ArtBoard artBoard;
    public static boolean isDrawing = false;
    private int IMAGE_CODE = 1;
    private DrawActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        initView();
        context = this;
        isDrawing = true;
        presenter = new DrawPresenter(this, this);
        draw_fl.post(this::initial);
    }

    private void initial() {
        int width = draw_fl.getWidth();
        int height = draw_fl.getHeight();
        LogUtil.d(TAG, "run -->" + "width: " + width + ", height: " + height);
        artBoard = new ArtBoard(getApplicationContext(), width, height);
        draw_fl.addView(artBoard);
        if (FabService.screenShotBitmap != null) {
            presenter.setIsAddBitmap(true);//设置发起同屏时是否发送图片
            artBoard.drawZoomBmp(FabService.screenShotBitmap);
        }
        presenter.register();
        presenter.queryMember();
        artBoard.setDrawTextListener((x, y) -> {
            LogUtil.d(TAG, "showEdtPop -->" + x + "," + y);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            EditText editText = new EditText(context);
            builder.setView(editText);
            builder.setOnDismissListener(dialog -> {
                String text = editText.getText().toString().trim();
                if (!text.isEmpty()) {
                    artBoard.drawText(x, y, text);
                }
            });
            builder.create().show();
        });
        draw_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                artBoard.setPaintWidth(seekBar.getProgress());
                draw_seekbar_tv.setText(String.valueOf(seekBar.getProgress()));
            }
        });
    }

    private void initView() {
        draw_exit = (TextView) findViewById(R.id.draw_exit);
        draw_save = (TextView) findViewById(R.id.draw_save);
        draw_clear = (TextView) findViewById(R.id.draw_clear);
        draw_picture = (TextView) findViewById(R.id.draw_picture);
        draw_font = (TextView) findViewById(R.id.draw_font);
        draw_color = (TextView) findViewById(R.id.draw_color);
        draw_revoke = (TextView) findViewById(R.id.draw_revoke);
        draw_fl = (FrameLayout) findViewById(R.id.draw_fl);
        draw_round = (TextView) findViewById(R.id.draw_round);
        draw_rect = (TextView) findViewById(R.id.draw_rect);
        draw_line = (TextView) findViewById(R.id.draw_line);
        draw_curve = (TextView) findViewById(R.id.draw_curve);
        draw_pen = (TextView) findViewById(R.id.draw_pen);
        draw_text = (TextView) findViewById(R.id.draw_text);
        draw_eraser = (TextView) findViewById(R.id.draw_eraser);
        draw_drag = (TextView) findViewById(R.id.draw_drag);
        draw_launch = (Button) findViewById(R.id.draw_launch);
        draw_stop = (Button) findViewById(R.id.draw_stop);
        setBtnEnable(isSharing);
        draw_seekbar = (AppCompatSeekBar) findViewById(R.id.draw_seekbar);
        draw_seekbar_tv = (TextView) findViewById(R.id.draw_seekbar_tv);

        draw_exit.setOnClickListener(this);
        draw_save.setOnClickListener(this);
        draw_clear.setOnClickListener(this);
        draw_picture.setOnClickListener(this);
        draw_font.setOnClickListener(this);
        draw_color.setOnClickListener(this);
        draw_revoke.setOnClickListener(this);
        draw_round.setOnClickListener(this);
        draw_rect.setOnClickListener(this);
        draw_line.setOnClickListener(this);
        draw_curve.setOnClickListener(this);
        draw_pen.setOnClickListener(this);
        draw_text.setOnClickListener(this);
        draw_eraser.setOnClickListener(this);
        draw_drag.setOnClickListener(this);
        draw_launch.setOnClickListener(this);
        draw_stop.setOnClickListener(this);

        selectTvs.add(draw_round);
        selectTvs.add(draw_rect);
        selectTvs.add(draw_line);
        selectTvs.add(draw_curve);
        selectTvs.add(draw_pen);
        selectTvs.add(draw_text);
        selectTvs.add(draw_eraser);
        selectTvs.add(draw_drag);
    }

    private void setSelect(int index) {
        for (int i = 0; i < selectTvs.size(); i++) {
            TextView tv = selectTvs.get(i);
            if (i == index) {
                tv.setSelected(true);
                artBoard.setDrag(index == 7);
            } else {
                tv.setSelected(false);
            }
        }
    }

    public void setBtnEnable(boolean enable) {
        draw_stop.setEnabled(enable);
        if (enable) {
            draw_stop.setBackground(getResources().getDrawable(R.drawable.shape_btn_pressed));
        } else {
            draw_stop.setBackground(getResources().getDrawable(R.drawable.shape_btn_enable_flase));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.draw_exit:
                finish();
                break;
            case R.id.draw_save:
                saveDig();
                break;
            case R.id.draw_clear:
                artBoard.clear();
                presenter.setIsAddBitmap(false);//已经清空了就不必发送了
                break;
            case R.id.draw_picture:
                Intent i = new Intent(ACTION_OPEN_DOCUMENT);//打开图片
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                startActivityForResult(i, IMAGE_CODE);
                break;
            case R.id.draw_font:

                break;
            case R.id.draw_color:
                new ColorPickerDialog(this, new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void colorChanged(int color) {
                        artBoard.setPaintColor(color);
                    }
                }, Color.BLACK).show();
                break;
            case R.id.draw_revoke:
                artBoard.revoke();
                break;
            case R.id.draw_round:
                setSelect(0);
                artBoard.setDrawType(2);
                break;
            case R.id.draw_rect:
                setSelect(1);
                artBoard.setDrawType(3);
                break;
            case R.id.draw_line:
                setSelect(2);
                artBoard.setDrawType(4);
                break;
            case R.id.draw_curve:
                setSelect(3);
                artBoard.setDrawType(1);
                break;
            case R.id.draw_pen:
                setSelect(4);
                artBoard.setDrawType(1);
                break;
            case R.id.draw_text:
                setSelect(5);
                artBoard.setDrawType(5);
                break;
            case R.id.draw_eraser:
                setSelect(6);
                artBoard.setDrawType(6);
                break;
            case R.id.draw_drag:
                setSelect(7);
                break;
            case R.id.draw_launch:
                presenter.showMultiplayerAnnotation(draw_fl);
                break;
            case R.id.draw_stop:
                finish();
                break;
        }
    }

    private void saveDig() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText edt = new EditText(this);
        builder.setTitle(getResources().getString(R.string.please_enter_file_name));
        edt.setHint(getResources().getString(R.string.please_enter_file_name));
        edt.setText(String.valueOf(System.currentTimeMillis()));
        //编辑光标移动到最后
        edt.setSelection(edt.getText().toString().length());
        builder.setView(edt);
        builder.setPositiveButton(getResources().getString(R.string.save_server), (dialog, which) -> {
            String name = edt.getText().toString().trim();
            if (name.isEmpty()) {
                ToastUtil.show( R.string.please_enter_file_name);
            } else if (!FileUtil.isLegalName(name)) {
                ToastUtil.show( R.string.tip_file_name_unlawfulness);
            } else {
                presenter.savePicture(name, true, artBoard.getCanvasBmp());
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(getResources().getString(R.string.save_local), (dialog, which) -> {
            final String name = edt.getText().toString().trim();
            if (name.isEmpty()) {
                ToastUtil.show( R.string.please_enter_file_name);
            } else if (!FileUtil.isLegalName(name)) {
                ToastUtil.show( R.string.tip_file_name_unlawfulness);
            } else {
                presenter.savePicture(name, false, artBoard.getCanvasBmp());
                ToastUtil.show(getResources().getString(R.string.tip_save_as, Constant.artboard_picture_dir));
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void drawZoomBmp(Bitmap bitmap) {
        artBoard.drawZoomBmp(bitmap);
    }

    @Override
    public void setCanvasSize(int maxX, int maxY) {
        artBoard.setCanvasSize(maxX, maxY);
    }

    @Override
    public void drawPath(Path path, Paint paint) {
        artBoard.drawPath(path, paint);
    }

    @Override
    public void invalidate() {
        artBoard.invalidate();
    }

    @Override
    public void funDraw(Paint paint, float height, int canSee, float fx, float fy, String text) {
        artBoard.funDraw(paint, height, canSee, fx, fy, text);
    }

    @Override
    public void drawText(String ptext, float lx, float ly, Paint paint) {
        artBoard.drawText(ptext, lx, ly, paint);
    }

    @Override
    public void initCanvas() {
        artBoard.initCanvas();
    }

    @Override
    public void drawAgain(List<ArtBoard.DrawPath> pathList) {
        artBoard.drawAgain(pathList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CODE && resultCode == Activity.RESULT_OK) {
            // 获取选中文件的uri
            LogUtil.d(TAG, "onActivityResult: data.toString : " + data.toString());
            Uri uri = data.getData();
            String realPath = null;
            try {
                realPath = UriUtil.getFilePath(getApplicationContext(), uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LogUtil.e(TAG, "DrawBoardActivity.onActivityResult :  选中的文件路径 --->>> " + realPath);
            if (realPath == null) {
                LogUtil.e(TAG, "onActivityResult: 获取该文件的路径失败....");
                ToastUtil.show( R.string.get_file_path_fail);
            } else {
                // 执行操作
                Bitmap dstbmp = BitmapFactory.decodeFile(realPath);
                //将图片绘制到画板中
                Bitmap bitmap = artBoard.drawZoomBmp(dstbmp);
                //保存图片信息
                ArtBoard.DrawPath drawPath = new ArtBoard.DrawPath();
                drawPath.picdata = bmp2bs(bitmap);
                ArtBoard.LocalPathList.add(drawPath);
                if (DrawPresenter.isSharing) {
                    long time = System.currentTimeMillis();
                    int operid = (int) (time / 10);
                    DrawPresenter.localOperids.add(operid);
                    DrawPresenter.LocalSharingPathList.add(drawPath);
                    presenter.addPicture(operid, MyApplication.localMemberId, mSrcmemid, mSrcwbid, time,
                            InterfaceMacro.Pb_MeetPostilFigureType.Pb_WB_FIGURETYPE_PICTURE.getNumber(), 0, 0, bmp2bs(bitmap));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDrawing = false;
        if (FabService.screenShotBitmap != null) {
            FabService.screenShotBitmap.recycle();
            FabService.screenShotBitmap = null;
        }
        presenter.stopShare();
        presenter.unregister();
        ArtBoard.LocalPathList.clear();
        DrawPresenter.LocalSharingPathList.clear();
        DrawPresenter.localOperids.clear();
        DrawPresenter.togetherIDs.clear();
        DrawPresenter.pathList.clear();
        DrawPresenter.tempPicData = null;
        DrawPresenter.savePicData = null;
        mSrcmemid = 0;
        mSrcwbid = 0;
        DrawPresenter.disposePicOpermemberid = 0;
        DrawPresenter.disposePicSrcmemid = 0;
        DrawPresenter.disposePicSrcwbidd = 0;
        ArtBoard.artBoardWidth = 0;
        ArtBoard.artBoardHeight = 0;
        artBoard.destroyDrawingCache();
        artBoard = null;
    }
}
