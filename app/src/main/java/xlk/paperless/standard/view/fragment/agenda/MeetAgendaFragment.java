package xlk.paperless.standard.view.fragment.agenda;

import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceAgenda;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.tencent.smtt.sdk.TbsDownloader;
import com.tencent.smtt.sdk.TbsReaderView;

import java.util.Objects;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;

import static xlk.paperless.standard.data.Values.initX5Finished;
import static xlk.paperless.standard.view.App.applicationContext;


/**
 * @author xlk
 * @date 2020/3/13
 * @desc 会议议程
 */
public class MeetAgendaFragment extends BaseFragment implements IMeetAgenda, TbsReaderView.ReaderCallback {

    private final String TAG = "MeetAgendaFragment-->";
    private ProgressBar f_agenda_bar;
    private TextView f_agenda_tv;
    private ScrollView f_agenda_sv;
    private LinearLayout f_agenda_root;
    private TbsReaderView tbsReaderView;
    private MeetAgendaPresenter presenter;
    /**
     * =true 加载的是系统内核（默认），=false 加载的是X5内核
     */
    public static boolean isNeedRestart = false;
    private LinearLayout ll_agenda_list;
    private RecyclerView rv_agenda, rv_agenda_file;
    private AgendaAdapter agendaAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_agenda, container, false);
        initView(inflate);
        presenter = new MeetAgendaPresenter(getContext(), this);
        presenter.queryAgenda();
        return inflate;
    }

    private void initView(View inflate) {
        f_agenda_root = inflate.findViewById(R.id.f_agenda_root);
        f_agenda_bar = inflate.findViewById(R.id.f_agenda_bar);

        f_agenda_sv = inflate.findViewById(R.id.f_agenda_sv);
        f_agenda_tv = inflate.findViewById(R.id.f_agenda_tv);

        ll_agenda_list = inflate.findViewById(R.id.ll_agenda_list);
        rv_agenda = inflate.findViewById(R.id.rv_agenda);
        rv_agenda_file = inflate.findViewById(R.id.rv_agenda_file);
    }

    @Override
    public void initDefault() {
        ll_agenda_list.setVisibility(View.GONE);
        f_agenda_bar.setVisibility(View.GONE);
        f_agenda_sv.setVisibility(View.GONE);
        f_agenda_tv.setText("当前没有议程");
        if (tbsReaderView != null) {
            f_agenda_root.removeView(tbsReaderView);
            tbsReaderView.onStop();
            tbsReaderView = null;
        }
    }

    @Override
    public void showTimeAgenda() {
        f_agenda_bar.setVisibility(View.GONE);
        f_agenda_sv.setVisibility(View.GONE);
        ll_agenda_list.setVisibility(View.VISIBLE);
        if (agendaAdapter == null) {
            agendaAdapter = new AgendaAdapter(presenter.agendaLists);
            rv_agenda.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_agenda.setAdapter(agendaAdapter);
            agendaAdapter.addChildClickViewIds(R.id.btn_agenda);
            agendaAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    InterfaceAgenda.pbui_ItemAgendaTimeInfo item = presenter.agendaLists.get(position);
                    int agendaid = item.getAgendaid();
                    int dirid = item.getDirid();
//                    agendaAdapter.choose(agendaid);
                }
            });
            agendaAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
                @Override
                public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                    InterfaceAgenda.pbui_ItemAgendaTimeInfo item = presenter.agendaLists.get(position);
                    int status;
                    if (item.getStatus() == InterfaceMacro.Pb_AgendaStatus.Pb_MEETAGENDA_STATUS_IDLE_VALUE) {
                        status = InterfaceMacro.Pb_AgendaStatus.Pb_MEETAGENDA_STATUS_RUNNING_VALUE;
                    } else /*if (item.getStatus() == InterfaceMacro.Pb_AgendaStatus.Pb_MEETAGENDA_STATUS_RUNNING_VALUE)*/ {
                        status = InterfaceMacro.Pb_AgendaStatus.Pb_MEETAGENDA_STATUS_END_VALUE;
                    }
                    jni.modifyTimeAgendaStatus(item, status);
                }
            });
        } else {
            agendaAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setAgendaTv(String text) {
        //也有可能下载X5内核完成，但是议程变成文本类
        f_agenda_bar.setVisibility(View.GONE);
        f_agenda_sv.setVisibility(View.VISIBLE);
        ll_agenda_list.setVisibility(View.GONE);
        f_agenda_tv.setText(text);
    }

    @Override
    public void displayFile(String path) {
        if (!FileUtils.isFileExists(path)) {
            Toast.makeText(getContext(), "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        f_agenda_sv.setVisibility(View.GONE);
        if (initX5Finished) {
            //加载完成
            if (isNeedRestart) {
                //加载的是系统内核
                f_agenda_bar.setVisibility(View.GONE);
                f_agenda_sv.setVisibility(View.VISIBLE);
                f_agenda_tv.setText(getString(R.string.init_x5_failure));
                return;
            }
        } else {
            //没有加载完成
            f_agenda_bar.setVisibility(View.VISIBLE);
            TbsDownloader.startDownload(applicationContext);
            return;
        }
        LogUtil.i(TAG, "displayFile 加载完成，并且加载的是X5内核 tbsReaderView是否为null：" + (tbsReaderView == null));
        /* **** **  加载完成，并且加载的是X5内核  ** **** */
        f_agenda_bar.setVisibility(View.GONE);

        String tempPath = Environment.getExternalStorageDirectory().getPath();
        //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
        /*String bsReaderTemp = tempPath + "/TbsReaderTemp";
        File bsReaderTempFile = new File(bsReaderTemp);
        if (!bsReaderTempFile.exists()) {
            LogUtil.e(TAG, "displayFile 准备创建/storage/emulated/0/TbsReaderTemp！！");
            boolean mkdir = bsReaderTempFile.mkdir();
            if (!mkdir) {
                LogUtil.e(TAG, "displayFile 创建/storage/emulated/0/TbsReaderTemp失败！！！！！");
            }
        }*/
        tbsReaderView = new TbsReaderView(Objects.requireNonNull(getContext()), this);
        f_agenda_root.addView(tbsReaderView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        Bundle bundle = new Bundle();
        bundle.putString("filePath", path);
        bundle.putString("tempPath", tempPath);//bsReaderTemp
        String suffix = path.substring(path.lastIndexOf(".") + 1);
        LogUtil.i(TAG, "displayFile 打开文件 -->" + path + "， 后缀： " + suffix + ", tempPath= " + tempPath);
        try {
            boolean result = tbsReaderView.preOpen(suffix, false);
            LogUtil.e(TAG, "displayFile :  result --> " + result);
            if (result) {
                tbsReaderView.openFile(bundle);
            } else {
                ToastUtil.show(R.string.not_supported);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCallBackAction(Integer integer, Object o, Object o1) {

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            presenter.queryAgenda();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        //不停止掉，下次进入是打开文件会卡在加载中状态
        if (tbsReaderView != null) {
            tbsReaderView.onStop();
            tbsReaderView = null;
        }
    }
}
