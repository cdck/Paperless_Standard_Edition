package xlk.paperless.standard.view.fragment.web;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.WebView;
import com.wang.avi.AVLoadingIndicatorView;

import xlk.paperless.standard.R;
import xlk.paperless.standard.ui.X5WebView;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.base.BaseFragment;

/**
 * @author xlk
 * @date 2020/3/13
 * @desc 网页浏览
 */
public class MeetWebFragment extends BaseFragment implements View.OnClickListener, IMeetWeb {
    private final String TAG = "MeetWebFragment-->";
    private ImageView f_web_back;
    private ImageView f_web_jump;
    private ImageView f_web_home;
    private EditText f_web_edt;
    private Button f_web_go;
    private AVLoadingIndicatorView f_web_loading;
    private X5WebView f_web_x5view;
    private MeetWebPresenter presenter;
    private final String HOME_URL = "http://www.baidu.com/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_web, container, false);
        initView(inflate);
        presenter = new MeetWebPresenter(getContext(), this);
        initEvent();
        presenter.webQuery();
        return inflate;
    }

    private void initView(View inflate) {
        f_web_back = (ImageView) inflate.findViewById(R.id.f_web_back);
        f_web_jump = (ImageView) inflate.findViewById(R.id.f_web_jump);
        f_web_home = (ImageView) inflate.findViewById(R.id.f_web_home);
        f_web_edt = (EditText) inflate.findViewById(R.id.f_web_edt);
        f_web_go = (Button) inflate.findViewById(R.id.f_web_go);
        f_web_loading = (AVLoadingIndicatorView) inflate.findViewById(R.id.f_web_loading);
        f_web_x5view = (X5WebView) inflate.findViewById(R.id.f_web_x5view);

        f_web_back.setOnClickListener(this);
        f_web_jump.setOnClickListener(this);
        f_web_home.setOnClickListener(this);
        f_web_go.setOnClickListener(this);
    }

    @Override
    public void loadUrl(String urlAddr) {
        f_web_x5view.loadUrl(uriHttpFirst(urlAddr));
    }

    //地址HTTP协议判断，无HTTP打头的，增加http://，并返回。
    private String uriHttpFirst(String strUri) {
        if (strUri.indexOf("http://", 0) != 0 && strUri.indexOf("https://", 0) != 0) {
            strUri = "http://" + strUri;
        }
        return strUri;
    }

    private void initEvent() {
        f_web_x5view.setWebViewClient(new com.tencent.smtt.sdk.WebViewClient() {
            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                f_web_loading.setVisibility(View.VISIBLE);
                super.onPageStarted(webView, s, bitmap);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
                return super.shouldOverrideUrlLoading(webView, webResourceRequest);
            }

            @Override
            public void onPageFinished(com.tencent.smtt.sdk.WebView webView, String s) {
                LogUtil.e(TAG, "WebBrowseFragment.onPageFinished : 加载结束 url --> " + s);
                f_web_edt.setText(s != null ? s : "");
                f_web_loading.setVisibility(View.GONE);
                super.onPageFinished(webView, s);
            }

//            @Override
//            public void onReceivedSslError(com.tencent.smtt.sdk.WebView webView, com.tencent.smtt.export.external.interfaces.SslErrorHandler sslErrorHandler, com.tencent.smtt.export.external.interfaces.SslError sslError) {
//                LogUtil.e(TAG, "WebBrowseFragment.onReceivedSslError :   --> ");
//                sslErrorHandler.proceed();//接受所有网站的证书
//                super.onReceivedSslError(webView, sslErrorHandler, sslError);
//            }
        });
    }

    @Override
    public void onResume() {
        LogUtil.i("F_life", "WebBrowseFragment.onResume :   --> ");
        super.onResume();
        if (f_web_x5view != null) {
            f_web_x5view.onResume();
            f_web_x5view.resumeTimers();
            f_web_x5view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        LogUtil.i("F_life", "WebBrowseFragment.onPause :   --> ");
        super.onPause();
        f_web_x5view.onPause();
        if (f_web_x5view != null) {
            String videoJs = "javascript: var v = document.getElementsByTagName('video'); for(var i=0;i<v.length;i++){v[i].pause();} ";
            f_web_x5view.loadUrl(videoJs);//遍历所有的Vedio标签，主动调用暂停方法
            f_web_x5view.onPause();
            f_web_x5view.pauseTimers();
            f_web_x5view.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        if (f_web_x5view != null) {
            f_web_x5view.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            //webview停止加载
            f_web_x5view.stopLoading();
            //webview销毁
            f_web_x5view.destroy();
            //webview清理内存
            f_web_x5view.clearCache(true);
            //webview清理历史记录
            f_web_x5view.clearHistory();
        }
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.f_web_back:
                f_web_x5view.goBack();
                break;
            case R.id.f_web_jump:
                f_web_x5view.goForward();
                break;
            case R.id.f_web_home:
                f_web_x5view.loadUrl(HOME_URL);
                break;
            case R.id.f_web_go:
                String url = f_web_edt.getText().toString();
                f_web_x5view.loadUrl(uriHttpFirst(url));
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            f_web_x5view.setAlpha(0);
            onPause();
        } else {
            f_web_x5view.setAlpha(1);
            onResume();
        }
        super.onHiddenChanged(hidden);
    }
}
