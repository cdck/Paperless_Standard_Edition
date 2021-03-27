package xlk.paperless.standard.view.admin.fragment.after.annotation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.admin.fragment.pre.file.FileAdapter;

import static xlk.paperless.standard.data.Constant.RESOURCE_0;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc
 */
public class AdminAnnotationFragment extends BaseFragment implements AdminAnnotationInterface {

    private AdminAnnotationPresenter presenter;
    private RecyclerView rv_file;
    private Button btn_download;
    private Button btn_delete;
    private FileAdapter fileAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_annotation, container, false);
        initView(inflate);
        presenter = new AdminAnnotationPresenter(this);
        presenter.queryFile();
        return inflate;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    protected void reShow() {
        presenter.queryFile();
    }

    private void initView(View inflate) {
        this.rv_file = (RecyclerView) inflate.findViewById(R.id.rv_file);
        this.btn_download = (Button) inflate.findViewById(R.id.btn_download);
        this.btn_delete = (Button) inflate.findViewById(R.id.btn_delete);
        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileAdapter == null || fileAdapter.getChecks().isEmpty()) {
                    ToastUtil.show(R.string.please_choose_file_first);
                    return;
                }
                List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> checks = fileAdapter.getChecks();
                for (int i = 0; i < checks.size(); i++) {
                    InterfaceFile.pbui_Item_MeetDirFileDetailInfo info = checks.get(i);
                    String filePath = Constant.DIR_DATA_FILE + info.getName().toStringUtf8();
                    JniHandler.getInstance().creationFileDownload(
                            filePath, info.getMediaid(), 1, 0, Constant.DOWNLOAD_ADMIN_ANNOTATION_FILE);
                }

            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileAdapter == null || fileAdapter.getChecks().isEmpty()) {
                    ToastUtil.show(R.string.please_choose_file_first);
                    return;
                }
                List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> checks = fileAdapter.getChecks();
                for (InterfaceFile.pbui_Item_MeetDirFileDetailInfo item : checks) {
                    JniHandler.getInstance().deleteMeetDirFile(Constant.ANNOTATION_FILE_DIRECTORY_ID, item);
                }
            }
        });
    }

    @Override
    public void update(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> annotationFiles) {
        if (fileAdapter == null) {
            fileAdapter = new FileAdapter(R.layout.item_admin_file, annotationFiles);
            rv_file.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_file.setAdapter(fileAdapter);
            fileAdapter.addChildClickViewIds(R.id.item_btn_open);
            fileAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    fileAdapter.setCheck(annotationFiles.get(position).getMediaid());
                }
            });
            fileAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
                @Override
                public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                    InterfaceFile.pbui_Item_MeetDirFileDetailInfo dirFile = annotationFiles.get(position);
                    if (FileUtil.isAudioAndVideoFile(dirFile.getName().toStringUtf8())) {
                        List<Integer> devIds = new ArrayList<>();
                        devIds.add(Values.localDeviceId);
                        JniHandler.getInstance().mediaPlayOperate(dirFile.getMediaid(), devIds, 0, RESOURCE_0, 0, 0);
                    } else {
                        FileUtil.openFile(getContext(), Constant.DIR_DATA_FILE, dirFile.getName().toStringUtf8(), dirFile.getMediaid());
                    }
                }
            });
        } else {
            fileAdapter.notifyDataSetChanged();
        }
    }
}
