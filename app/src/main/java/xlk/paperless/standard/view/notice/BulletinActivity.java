package xlk.paperless.standard.view.notice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceBullet;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.HashMap;
import java.util.Set;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.base.BaseActivity;

/**
 * @author xlk
 */
public class BulletinActivity extends BaseActivity implements INotice, View.OnClickListener {
    public static HashMap<Integer, Activity> hashMap = new HashMap<>();
    private int bulletinId;
    private NoticePresenter presenter;
    private TextView notice_ac_title_tv;
    private TextView notice_ac_content_tv;
    private Button notice_ac_close_btn;
    private ImageView notice_ac_logo_iv;
    private ConstraintLayout notice_bg_view;

    public static void jump(int bulletinId, Context context) {
        if (hashMap.containsKey(bulletinId)) {
            Activity activity = hashMap.get(bulletinId);
            activity.finish();
            hashMap.remove(bulletinId);
        } else {
            context.startActivity(new Intent(context, BulletinActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Constant.EXTRA_BULLETIN_ID, bulletinId));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulletin);
        initView();
        bulletinId = getIntent().getIntExtra(Constant.EXTRA_BULLETIN_ID, 0);
        if (bulletinId != 0) {
            hashMap.put(bulletinId, this);
        }
        presenter = new NoticePresenter(this, this);
        presenter.queryInterfaceConfig();
        presenter.queryAssignNotice(bulletinId);
    }

    @Override
    public void updateNoticeBg(Drawable drawable) {
        notice_bg_view.setBackground(drawable);
    }

    @Override
    public void updateNoticeLogo(Drawable drawable) {
        notice_ac_logo_iv.setImageDrawable(drawable);
    }

    @Override
    public void updateBtn(int resid, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info) {
        Button btn = findViewById(resid);
        String fontName = info.getFontname().toStringUtf8();
        int align = info.getAlign();
        int flag = info.getFlag();
        boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
        btn.setVisibility(isShow ? View.VISIBLE : View.GONE);
        int fontflag = info.getFontflag();
        btn.setTextColor(info.getColor());
        btn.setTextSize(info.getFontsize());
        update(resid, info);
        //字体样式
        if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD.getNumber()) {//加粗
            btn.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_LEAN.getNumber()) {//倾斜
            btn.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_UNDERLINE.getNumber()) {//下划线
            btn.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));//暂时用倾斜加粗
        } else {//正常文本
            btn.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
        //对齐方式
        if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT.getNumber()) {//左对齐
            btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT.getNumber()) {//右对齐
            btn.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_HCENTER.getNumber()) {//水平对齐
            btn.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_TOP.getNumber()) {//上对齐
            btn.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_BOTTOM.getNumber()) {//下对齐
            btn.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_VCENTER.getNumber()) {//垂直对齐
            btn.setGravity(Gravity.CENTER_VERTICAL);
        } else {
            btn.setGravity(Gravity.CENTER);
        }
        //字体类型
        Typeface kt_typeface;
        if (!TextUtils.isEmpty(fontName)) {
            switch (fontName) {
                case "楷体":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "kaiti.ttf");
                    break;
                case "隶书":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "lishu.ttf");
                    break;
                case "微软雅黑":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "weiruanyahei.ttf");
                    break;
                case "黑体":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "heiti.ttf");
                    break;
                case "小楷":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "xiaokai.ttf");
                    break;
                default:
                    kt_typeface = Typeface.createFromAsset(getAssets(), "fangsong.ttf");
                    break;
            }
            btn.setTypeface(kt_typeface);
        }
    }

    @Override
    public void updateTv(int resid, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info) {
        TextView tv = findViewById(resid);
        int color = info.getColor();
        int fontsize = info.getFontsize();
        int flag = info.getFlag();
        boolean isShow = (InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE == (flag & InterfaceMacro.Pb_MeetFaceFlag.Pb_MEET_FACEFLAG_SHOW_VALUE));
        tv.setVisibility(isShow ? View.VISIBLE : View.GONE);
        int fontflag = info.getFontflag();
        int align = info.getAlign();
        String fontName = info.getFontname().toStringUtf8();
        tv.setTextColor(color);
        tv.setTextSize(fontsize);
        update(resid, info);
        //字体样式
        if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_BOLD.getNumber()) {//加粗
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_LEAN.getNumber()) {//倾斜
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else if (fontflag == InterfaceMacro.Pb_MeetFaceFontFlag.Pb_MEET_FONTFLAG_UNDERLINE.getNumber()) {//下划线
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));//暂时用倾斜加粗
        } else {//正常文本
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
        //对齐方式
        if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_LEFT.getNumber()) {//左对齐
            tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_RIGHT.getNumber()) {//右对齐
            tv.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_HCENTER.getNumber()) {//水平对齐
            tv.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_TOP.getNumber()) {//上对齐
            tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_BOTTOM.getNumber()) {//下对齐
            tv.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        } else if (align == InterfaceMacro.Pb_FontAlignFlag.Pb_MEET_FONTALIGNFLAG_VCENTER.getNumber()) {//垂直对齐
            tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        } else {
            tv.setGravity(Gravity.CENTER);
        }
        //字体类型
        Typeface kt_typeface;
        if (!TextUtils.isEmpty(fontName)) {
            switch (fontName) {
                case "楷体":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "kaiti.ttf");
                    break;
                case "隶书":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "lishu.ttf");
                    break;
                case "微软雅黑":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "weiruanyahei.ttf");
                    break;
                case "黑体":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "heiti.ttf");
                    break;
                case "小楷":
                    kt_typeface = Typeface.createFromAsset(getAssets(), "xiaokai.ttf");
                    break;
                default:
                    kt_typeface = Typeface.createFromAsset(getAssets(), "fangsong.ttf");
                    break;
            }
            tv.setTypeface(kt_typeface);
        }
    }

    private void update(int resid, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info) {
        float ly = info.getLy();
        float lx = info.getLx();
        float bx = info.getBx();
        float by = info.getBy();
        ConstraintSet set = new ConstraintSet();
        set.clone(notice_bg_view);
        //设置控件的大小
        float width = (bx - lx) / 100 * Values.screen_width;
        float height = (by - ly) / 100 * Values.screen_height;
        set.constrainWidth(resid, (int) width);
        set.constrainHeight(resid, (int) height);
        float biasX, biasY;
        float halfW = (bx - lx) / 2 + lx;
        float halfH = (by - ly) / 2 + ly;

        if (lx == 0) biasX = 0;
        else if (lx > 50) biasX = bx / 100;
        else biasX = halfW / 100;

        if (ly == 0) biasY = 0;
        else if (ly > 50) biasY = by / 100;
        else biasY = halfH / 100;
        set.setHorizontalBias(resid, biasX);
        set.setVerticalBias(resid, biasY);
        set.applyTo(notice_bg_view);
    }

    @Override
    public void updateText(InterfaceBullet.pbui_Item_BulletDetailInfo info) {
        notice_ac_title_tv.setText(info.getTitle().toStringUtf8());
        notice_ac_content_tv.setText(info.getContent().toStringUtf8());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    private void initView() {
        notice_ac_title_tv = (TextView) findViewById(R.id.notice_ac_title_tv);
        notice_ac_content_tv = (TextView) findViewById(R.id.notice_ac_content_tv);
        notice_ac_close_btn = (Button) findViewById(R.id.notice_ac_close_btn);
        notice_ac_logo_iv = (ImageView) findViewById(R.id.notice_ac_logo_iv);
        notice_bg_view = (ConstraintLayout) findViewById(R.id.notice_bg_view);

        notice_ac_close_btn.setOnClickListener(this);
    }

    @Override
    public void clearAll() {
        Set<Integer> integers = hashMap.keySet();
        for (int key : integers) {
            hashMap.get(key).finish();
        }
        hashMap.clear();
    }

    @Override
    public void onBackPressed() {
        clearAll();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.notice_ac_close_btn:
                hashMap.remove(bulletinId);
                finish();
                break;
        }
    }
}
