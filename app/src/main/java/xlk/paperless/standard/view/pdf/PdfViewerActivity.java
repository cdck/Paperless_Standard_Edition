package xlk.paperless.standard.view.pdf;

import androidx.appcompat.app.AppCompatActivity;
import xlk.paperless.standard.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;

public class PdfViewerActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    public static final String FILE_PATH = "file_path";
    private PDFView pdf_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        pdf_view = findViewById(R.id.pdf_view);
        getFilePath(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getFilePath(intent);
    }

    private void getFilePath(Intent intent) {
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
        String filePath = intent.getStringExtra(FILE_PATH);
        displayFromFile(filePath);
    }

    private void displayFromFile(String filePath) {
        LogUtils.d("displayFromFile filePath=" + filePath);
        File file = new File(filePath);
            pdf_view.fromFile(file)
                    .defaultPage(0)
                    .onPageChange(this)
                    .enableSwipe(false)
                    .enableAnnotationRendering(true)
                    .onLoad(this)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(10) // in dp
                    .onPageError(this)
//                .pageFitPolicy(FitPolicy.BOTH)
                    .load();

    }

    @Override
    public void onPageChanged(int page, int pageCount) {
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