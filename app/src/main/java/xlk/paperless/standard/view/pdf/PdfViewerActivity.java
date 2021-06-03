package xlk.paperless.standard.view.pdf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import xlk.paperless.standard.R;
import xlk.paperless.standard.helper.AfterTextWatcher;
import xlk.paperless.standard.util.PopUtil;
import xlk.paperless.standard.util.ToastUtil;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;

public class PdfViewerActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    public static final String FILE_PATH = "file_path";
    private PDFView pdf_view;
    private int currentPageIndex = 0;
    private int mPageCount = 0;
    private DrawerLayout drawer_layout;
    private LinearLayout ll_side_slip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        initView();
        getFilePath(getIntent());
    }

    private void initView() {
        drawer_layout = findViewById(R.id.drawer_layout);
        ll_side_slip = findViewById(R.id.ll_side_slip);
        drawer_layout.setScrimColor(Color.TRANSPARENT);
        //首次进入默认打开侧滑菜单
        drawer_layout.openDrawer(ll_side_slip);
        pdf_view = findViewById(R.id.pdf_view);
        findViewById(R.id.ll_home).setOnClickListener(v -> {
            pdf_view.jumpTo(0, true);
        });
        findViewById(R.id.ll_pre).setOnClickListener(v -> {
            if (currentPageIndex > 0) {
                pdf_view.jumpTo(currentPageIndex - 1, true);
            }
        });
        findViewById(R.id.ll_next).setOnClickListener(v -> {
            if (currentPageIndex < mPageCount - 1) {
                pdf_view.jumpTo(currentPageIndex + 1, true);
            }
        });
        findViewById(R.id.ll_end).setOnClickListener(v -> {
            pdf_view.jumpTo(mPageCount - 1, true);
        });
        findViewById(R.id.ll_jump).setOnClickListener(v -> {
            showJumpPagePop();
        });
    }

    private void showJumpPagePop() {
        drawer_layout.closeDrawers();
        View inflate = LayoutInflater.from(this).inflate(R.layout.pop_enter_page, null, false);
        PopupWindow popupWindow = PopUtil.create(inflate, ScreenUtils.getScreenWidth() / 3, ScreenUtils.getScreenHeight() / 3, pdf_view);
        TextView tv_number = inflate.findViewById(R.id.tv_number);
        tv_number.setText((currentPageIndex + 1) + " / " + mPageCount);
        EditText edt_pdf_page = inflate.findViewById(R.id.edt_pdf_page);
        edt_pdf_page.addTextChangedListener(new AfterTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().isEmpty()) {
                    runOnUiThread(() -> {
                        int n = Integer.parseInt(s.toString().trim());
                        if (n > mPageCount) {
                            n = mPageCount;
                            edt_pdf_page.setText(String.valueOf(n));
                        }
                        if (n < 1) {
                            n = 1;
                            edt_pdf_page.setText(String.valueOf(n));
                        }
                        tv_number.setText(n + " / " + mPageCount);
                    });
                }
            }
        });
        inflate.findViewById(R.id.btn_cancel).setOnClickListener(v -> popupWindow.dismiss());
        inflate.findViewById(R.id.btn_jump).setOnClickListener(v -> {
            String trim = edt_pdf_page.getText().toString().trim();
            if (trim.isEmpty()) {
                ToastUtil.show(R.string.please_enter_page_number);
                return;
            }
            int n = Integer.parseInt(trim);
            pdf_view.jumpTo(n - 1, true);
            popupWindow.dismiss();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getFilePath(intent);
    }

    private void getFilePath(Intent intent) {
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
        try {
            String filePath = intent.getStringExtra(FILE_PATH);
            displayFromFile(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void displayFromFile(String filePath) {
        LogUtils.d("displayFromFile filePath=" + filePath);
        File file = new File(filePath);
        pdf_view.fromFile(file)
                .defaultPage(0)
                .onPageChange(this)
                .enableSwipe(true)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        currentPageIndex = page;
        mPageCount = pageCount;
        LogUtils.i("当前页码：" + page + ",总页数：" + pageCount);
    }

    @Override
    public void loadComplete(int nbPages) {
        LogUtils.i("加载完成 " + nbPages);
    }

    @Override
    public void onPageError(int page, Throwable t) {
        LogUtils.e("加载第【" + page + "】页出现异常：" + t.toString());
    }
}