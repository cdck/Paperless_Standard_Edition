package xlk.paperless.standard.view.fragment.agenda;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;
import java.util.Objects;

import xlk.paperless.standard.R;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;

/**
 * @author xlk
 * @date 2020/3/13
 * @desc 会议议程
 */
public class MeetAgendaFragment extends Fragment implements IMeetAgenda, TbsReaderView.ReaderCallback {

    private final String TAG = "MeetAgendaFragment-->";
    private TextView f_agenda_tv;
    private ScrollView f_agenda_sv;
    private LinearLayout f_agenda_root;
    private TbsReaderView tbsReaderView;
    private MeetAgendaPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_agenda, container, false);
        initView(inflate);
        presenter = new MeetAgendaPresenter(getContext(), this);
        presenter.register();
        presenter.queryAgenda();
        return inflate;
    }

    private void initView(View inflate) {
        f_agenda_tv = (TextView) inflate.findViewById(R.id.f_agenda_tv);
        f_agenda_sv = (ScrollView) inflate.findViewById(R.id.f_agenda_sv);
        f_agenda_root = (LinearLayout) inflate.findViewById(R.id.f_agenda_root);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unregister();
    }

    @Override
    public void initDefault() {
        f_agenda_tv.setText("");
        if (tbsReaderView != null) {
            f_agenda_root.removeView(tbsReaderView);
            tbsReaderView.onStop();
            tbsReaderView = null;
        }
    }

    @Override
    public void setAgendaTv(String text) {
        f_agenda_sv.setVisibility(View.VISIBLE);
        f_agenda_tv.setText(text);
    }

    @Override
    public void displayFile(String path) {
        f_agenda_sv.setVisibility(View.GONE);
        String tempPath = Environment.getExternalStorageDirectory().getPath();
        //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
        String bsReaderTemp = tempPath + "/TbsReaderTemp";
        File bsReaderTempFile = new File(bsReaderTemp);
        if (!bsReaderTempFile.exists()) {
            LogUtil.e(TAG, "displayFile 准备创建/storage/emulated/0/TbsReaderTemp！！");
            boolean mkdir = bsReaderTempFile.mkdir();
            if (!mkdir) {
                LogUtil.e(TAG, "displayFile 创建/storage/emulated/0/TbsReaderTemp失败！！！！！");
            }
        }
        tbsReaderView = new TbsReaderView(Objects.requireNonNull(getContext()), this);
        f_agenda_root.addView(tbsReaderView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Bundle bundle = new Bundle();
        bundle.putString("filePath", path);
        bundle.putString("tempPath", bsReaderTemp);
        String suffix = path.substring(path.lastIndexOf(".") + 1);
        LogUtil.i(TAG, "displayFile 打开文件 -->" + path + "， 后缀： " + suffix + ", tempPath= " + tempPath);
        boolean result = tbsReaderView.preOpen(suffix, false);
        LogUtil.e(TAG, "displayFile :  result --> " + result);
        if (result) {
            try {
                tbsReaderView.openFile(bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ToastUtil.show(R.string.not_supported);
        }
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.queryAgenda();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            presenter.queryAgenda();
        }
    }
}
