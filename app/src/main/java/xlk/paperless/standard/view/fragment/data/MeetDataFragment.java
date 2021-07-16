package xlk.paperless.standard.view.fragment.data;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.MeetDataDirAdapter;
import xlk.paperless.standard.adapter.MeetDataExportAdapter;
import xlk.paperless.standard.adapter.MeetDataFileAdapter;
import xlk.paperless.standard.adapter.MeetDataFileListAdapter;
import xlk.paperless.standard.adapter.PopPushMemberAdapter;
import xlk.paperless.standard.adapter.PopPushProjectionAdapter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.DevMember;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.PopUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.util.UriUtil;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.view.App;

import static xlk.paperless.standard.data.Constant.permission_code_upload;
import static xlk.paperless.standard.data.Constant.RESOURCE_0;

/**
 * @author xlk
 * @date 2020/3/13
 * @desc 会议资料
 */
public class MeetDataFragment extends BaseFragment implements View.OnClickListener, IMeetData {

    private final String TAG = "MeetDataFragment-->";
    private RecyclerView f_data_dir_rv;
    private Button f_data_upload_file;
    private Button f_data_documentation;
    private Button f_data_picture;
    private Button f_data_video;
    private Button f_data_other;
    private Button f_data_push;
    private Button f_data_export;
    private RecyclerView f_data_file_rv;
    private ListView f_data_file_lv;
    private Button f_data_previous_btn;
    private TextView f_data_page;
    private Button f_data_nextpage_btn;
    private MeetDataPresenter presenter;
    private MeetDataDirAdapter dirAdapter;
    private MeetDataFileAdapter fileAdapter;
    List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> allFileDetailInfos = new ArrayList<>();
    List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> typeFileDetailInfos = new ArrayList<>();
    private MeetDataFileListAdapter fileListAdapter;
    private PopupWindow pushPop;
    private PopPushMemberAdapter pushMemberAdapter;
    private PopPushProjectionAdapter pushProjectionAdapter;
    private int currentDirId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_data, container, false);
        initView(inflate);
        presenter = new MeetDataPresenter(getContext(), this);
        presenter.queryMeetDir();
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    private void initView(View inflate) {
        f_data_dir_rv = inflate.findViewById(R.id.f_data_dir_rv);
        f_data_upload_file = inflate.findViewById(R.id.f_data_upload_file);
        f_data_documentation = inflate.findViewById(R.id.f_data_documentation);
        f_data_picture = inflate.findViewById(R.id.f_data_picture);
        f_data_video = inflate.findViewById(R.id.f_data_video);
        f_data_other = inflate.findViewById(R.id.f_data_other);
        f_data_push = inflate.findViewById(R.id.f_data_push);
        f_data_export = inflate.findViewById(R.id.f_data_export);
        f_data_file_rv = inflate.findViewById(R.id.f_data_file_rv);
        f_data_file_lv = inflate.findViewById(R.id.f_data_file_lv);
        f_data_previous_btn = inflate.findViewById(R.id.f_data_previous_btn);
        f_data_page = inflate.findViewById(R.id.f_data_page);
        f_data_nextpage_btn = inflate.findViewById(R.id.f_data_nextpage_btn);

        f_data_upload_file.setOnClickListener(this);
        f_data_documentation.setOnClickListener(this);
        f_data_picture.setOnClickListener(this);
        f_data_video.setOnClickListener(this);
        f_data_other.setOnClickListener(this);
        f_data_push.setOnClickListener(this);
        f_data_export.setOnClickListener(this);
        f_data_previous_btn.setOnClickListener(this);
        f_data_nextpage_btn.setOnClickListener(this);
    }

    @Override
    public void updateDir(List<InterfaceFile.pbui_Item_MeetDirDetailInfo> dirDetailInfos) {
        if (dirAdapter == null) {
            dirAdapter = new MeetDataDirAdapter(R.layout.item_meet_data_dir, dirDetailInfos);
            f_data_dir_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            f_data_dir_rv.setAdapter(dirAdapter);
        } else {
            dirAdapter.notifyDataSetChanged();
        }
        dirAdapter.setOnItemClickListener((adapter, view, position) -> {
            currentDirId = dirDetailInfos.get(position).getId();
            f_data_upload_file.setVisibility(currentDirId == Constant.SHARED_FILE_DIRECTORY_ID ? View.VISIBLE : View.INVISIBLE);
            dirAdapter.setChoose(currentDirId);
            presenter.queryMeetFileByDir(currentDirId);
        });
        if (!dirDetailInfos.isEmpty()) {
            int dirId = dirDetailInfos.get(0).getId();
            if (currentDirId == -1) {
                presenter.queryMeetFileByDir(dirId);
                currentDirId = dirId;
                dirAdapter.setChoose(dirId);
            } else {
                boolean have = false;
                for (int i = 0; i < dirDetailInfos.size(); i++) {
                    InterfaceFile.pbui_Item_MeetDirDetailInfo info = dirDetailInfos.get(i);
                    if (info.getId() == currentDirId) {
                        have = true;
                    }
                }
                if (have) {
                    presenter.queryMeetFileByDir(currentDirId);
                    dirAdapter.setChoose(currentDirId);
                } else {
                    currentDirId = dirId;
                    presenter.queryMeetFileByDir(dirId);
                    dirAdapter.setChoose(dirId);
                }
            }
        } else {
            currentDirId = -1;
            presenter.clearFile();
        }
        LogUtil.e(TAG, "updateDir currentDirId -->" + currentDirId);
    }

    @Override
    public void updatePage(String page) {
        f_data_page.setText(page);
    }

    @Override
    public void updateFileRv(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> fileDetailInfos) {
        rvFile(fileDetailInfos);
//        lvFile(fileDetailInfos);
    }

    private void lvFile(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> fileDetailInfos) {
        if (fileListAdapter == null) {
            f_data_file_lv.post(() -> {
                int height = f_data_file_lv.getHeight();
                int itemCount = height / 100;
                LogUtil.d(TAG, "lvFile --> 列表的高度：" + height + "，itemCount：" + itemCount);
                fileListAdapter = new MeetDataFileListAdapter(getContext(), fileDetailInfos, itemCount);
                f_data_file_lv.setAdapter(fileListAdapter);
                fileListAdapter.setOnDownloadClickListener(item -> {
                    presenter.downloadFile(item);
                });
                f_data_file_lv.setOnItemClickListener((parent, view, position, id) -> {
                    fileListAdapter.setChoose(fileDetailInfos.get(position).getMediaid());
                });
            });
        } else {
            fileListAdapter.notifyDataSetChanged();
        }
    }

    private void rvFile(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> fileDetailInfos) {
        allFileDetailInfos.clear();
        allFileDetailInfos.addAll(fileDetailInfos);
        typeFileDetailInfos.clear();
        typeFileDetailInfos.addAll(fileDetailInfos);
        if (fileAdapter == null) {
            fileAdapter = new MeetDataFileAdapter(R.layout.item_meet_data_file, typeFileDetailInfos);
            f_data_file_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            f_data_file_rv.setAdapter(fileAdapter);
        } else {
            fileAdapter.notifyDataSetChanged();
        }
        if (App.isStandard) {
            fileAdapter.addChildClickViewIds(R.id.i_m_d_file_download, R.id.i_m_d_file_view);
        } else {
            fileAdapter.addChildClickViewIds(R.id.i_m_d_file_name, R.id.i_m_d_file_download);
        }
        fileAdapter.setOnItemClickListener((adapter, view, position) -> {
            fileAdapter.setChoose(typeFileDetailInfos.get(position).getMediaid());
        });
        fileAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo info = typeFileDetailInfos.get(position);
            if (view.getId() == R.id.i_m_d_file_download) {
                if (Constant.hasPermission(Constant.permission_code_download)) {
                    presenter.downloadFile(info);
                } else {
                    ToastUtil.show(R.string.err_NoPermission);
                }
            } else if (view.getId() == R.id.i_m_d_file_view) {
                LogUtils.i("打开文件："+info.getName().toStringUtf8());
                if (FileUtil.isAudioAndVideoFile(info.getName().toStringUtf8())) {
                    List<Integer> devIds = new ArrayList<>();
                    devIds.add(Values.localDeviceId);
                    JniHandler.getInstance().mediaPlayOperate(info.getMediaid(), devIds, 0, RESOURCE_0, 0, 0);
                } else {
                    FileUtil.openFile(getContext(), Constant.DIR_DATA_FILE, info.getName().toStringUtf8(), info.getMediaid());
                }
            } else if (view.getId() == R.id.i_m_d_file_name) {
                LogUtils.d(TAG, "rvFile -->" + "查看文件");
//                if (Constant.isVideo(info.getMediaid())) {
                if (FileUtil.isAudioAndVideoFile(info.getName().toStringUtf8())) {
                    List<Integer> devIds = new ArrayList<>();
                    devIds.add(Values.localDeviceId);
                    JniHandler.getInstance().mediaPlayOperate(info.getMediaid(), devIds, 0, RESOURCE_0, 0, 0);
                } else {
//                    if (Constant.hasPermission(Constant.permission_code_download)) {
                    FileUtil.openFile(getContext(), Constant.DIR_DATA_FILE, info.getName().toStringUtf8(), info.getMediaid());
//                    } else {
//                        ToastUtil.show(R.string.err_NoPermission);
//                    }
                }
            }
        });
    }

    @Override
    public void showPushView(List<DevMember> onlineMembers,
                             List<InterfaceDevice.pbui_Item_DeviceDetailInfo> onLineProjectors, int mediaId) {
        LogUtil.d(TAG, "showPushView -->" + "展示推送弹框视图");
        if (pushPop != null && pushPop.isShowing()) {
            pushMemberAdapter.notifyDataSetChanged();
            pushMemberAdapter.notifyChecks();
            pushProjectionAdapter.notifyDataSetChanged();
            pushProjectionAdapter.notifyChecks();
        } else {
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_push_view, null);
            pushPop = PopUtil.create(inflate, f_data_upload_file);
            CheckBox pop_push_member_cb = inflate.findViewById(R.id.pop_push_member_cb);
            RecyclerView pop_push_member_rv = inflate.findViewById(R.id.pop_push_member_rv);
            pushMemberAdapter = new PopPushMemberAdapter(R.layout.item_single_button, onlineMembers);
            pop_push_member_rv.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
            pop_push_member_rv.setAdapter(pushMemberAdapter);
            pushMemberAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    pushMemberAdapter.choose(onlineMembers.get(position).getDeviceDetailInfo().getDevcieid());
                    pop_push_member_cb.setChecked(pushMemberAdapter.isChooseAll());
                }
            });
            pop_push_member_cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean checked = pop_push_member_cb.isChecked();
                    pop_push_member_cb.setChecked(checked);
                    pushMemberAdapter.setChooseAll(checked);
                }
            });
            CheckBox pop_push_projection_cb = inflate.findViewById(R.id.pop_push_projection_cb);
            RecyclerView pop_push_projection_rv = inflate.findViewById(R.id.pop_push_projection_rv);
            pushProjectionAdapter = new PopPushProjectionAdapter(R.layout.item_single_button, onLineProjectors);
            pop_push_projection_rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            pop_push_projection_rv.setAdapter(pushProjectionAdapter);
            pushProjectionAdapter.setOnItemClickListener((adapter, view, position) -> {
                pushProjectionAdapter.choose(onLineProjectors.get(position).getDevcieid());
                pop_push_projection_cb.setChecked(pushProjectionAdapter.isChooseAll());
            });
            pop_push_projection_cb.setOnClickListener(v -> {
                boolean checked = pop_push_projection_cb.isChecked();
                pop_push_projection_cb.setChecked(checked);
                pushProjectionAdapter.setChooseAll(checked);
            });
            //推送文件
            inflate.findViewById(R.id.pop_push_determine).setOnClickListener(v -> {
                List<Integer> devIds = pushMemberAdapter.getDevIds();
                devIds.addAll(pushProjectionAdapter.getDevIds());
                if (!devIds.isEmpty()) {
                    pushPop.dismiss();
                    presenter.mediaPlayOperate(mediaId, devIds, 0, RESOURCE_0, 0, InterfaceMacro.Pb_MeetPlayFlag.Pb_MEDIA_PLAYFLAG_ZERO.getNumber());
                } else {
                    ToastUtil.show(R.string.please_choose_push_target);
                }
            });
            //停止推送
            inflate.findViewById(R.id.pop_push_stop).setOnClickListener(v -> {
                List<Integer> devIds = pushMemberAdapter.getDevIds();
                devIds.addAll(pushProjectionAdapter.getDevIds());
                if (!devIds.isEmpty()) {
                    pushPop.dismiss();
                    List<Integer> temps = new ArrayList<>();
                    temps.add(0);
                    presenter.stopPush(temps, devIds);
                } else {
                    ToastUtil.show(R.string.please_choose_push_target);
                }
            });
            //取消
            inflate.findViewById(R.id.pop_push_cancel).setOnClickListener(v -> {
                pushPop.dismiss();
                pushPop = null;
            });
        }
    }

    private void exportFile() {
        if (allFileDetailInfos.isEmpty()) {
            ToastUtil.show(R.string.no_export_file);
            return;
        }
        List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> temps = new ArrayList<>(allFileDetailInfos);
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_export_file, null);
        PopupWindow exportPop = PopUtil.create(inflate, Values.screen_width / 3 * 2, Values.screen_height / 3 * 2, f_data_upload_file);
        RecyclerView pop_export_rv = inflate.findViewById(R.id.pop_export_rv);
        Button pop_export_download = inflate.findViewById(R.id.pop_export_download);
        Button pop_export_back = inflate.findViewById(R.id.pop_export_back);
        MeetDataExportAdapter exportAdapter = new MeetDataExportAdapter(R.layout.item_meet_data_export, temps);
        pop_export_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        pop_export_rv.setAdapter(exportAdapter);
        exportAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                long mediaid = temps.get(position).getMediaid();
                LogUtil.d(TAG, "onItemClick -->" + position + ", mediaid= " + mediaid);
                exportAdapter.setChoose(mediaid);
            }
        });
        pop_export_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> choosedFile = exportAdapter.getChoosedFile();
                for (InterfaceFile.pbui_Item_MeetDirFileDetailInfo info : choosedFile) {
                    presenter.downloadFile(info);
                }
                exportPop.dismiss();
            }
        });
        pop_export_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportPop.dismiss();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            Uri uri = data.getData();
            String path = null;
            try {
                path = UriUtil.getFilePath(getContext(), uri);
//                path = GetFilePath.getPath(getContext(), uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (path != null && !path.equals("")) {
                LogUtil.e(TAG, "onActivityResult :  选中文件的路径 --->>> " + path);
                uploadFileDia(path);
            }
        }
    }

    /**
     * 上传文件
     */
    public void uploadFileDia(@NonNull String path) {
        File file = new File(path);
        if (!file.exists()) return;
        String fileName = file.getName();
        LogUtil.e(TAG, "uploadFileDia : 要上传的文件名：" + fileName);
        String name = fileName, suffix = "";
        if (fileName.contains(".")) {
            name = fileName.substring(0, fileName.lastIndexOf("."));
            suffix = fileName.substring(fileName.lastIndexOf("."));
        }
        final EditText editText = new EditText(getContext());
        //给输入框设置默认文件名
        editText.setText(name);
        LogUtil.e(TAG, "uploadFileDia  输入框文件名： " + name);
        String finalSuffix = suffix;
        new AlertDialog.Builder(Objects.requireNonNull(getContext())).setTitle(getResources().getString(R.string.please_enter_valid_file_name))
                .setView(editText)
                .setPositiveButton(getResources().getString(R.string.determine), (dialogInterface, i) -> {
                    if (!(TextUtils.isEmpty(editText.getText().toString().trim()))) {
                        String shareFileName = editText.getText().toString();
                        presenter.uploadFile(0,
                                currentDirId, 0, shareFileName + finalSuffix, path,
                                0, Constant.UPLOAD_CHOOSE_FILE);
                        dialogInterface.dismiss();
                    } else {
                        ToastUtil.show(R.string.please_enter_valid_file_name);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.f_data_upload_file://上传文件
                if (Constant.hasPermission(permission_code_upload)) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");//无类型限制
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, 1);
                } else {
                    ToastUtil.show(R.string.err_NoPermission);
                }
                break;
            case R.id.f_data_documentation:
                typeFileDetailInfos.clear();
                for (int i = 0; i < allFileDetailInfos.size(); i++) {
                    InterfaceFile.pbui_Item_MeetDirFileDetailInfo info = allFileDetailInfos.get(i);
//                    if(Constant.isDocument(info.getMediaid())){
                    if (FileUtil.isDocumentFile(info.getName().toStringUtf8())) {
                        typeFileDetailInfos.add(info);
                    }
                }
                if (fileAdapter != null) {
                    fileAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.f_data_picture:
                typeFileDetailInfos.clear();
                for (int i = 0; i < allFileDetailInfos.size(); i++) {
                    InterfaceFile.pbui_Item_MeetDirFileDetailInfo info = allFileDetailInfos.get(i);
//                    if(Constant.isPicture(info.getMediaid())){
                    if (FileUtil.isPictureFile(info.getName().toStringUtf8())) {
                        typeFileDetailInfos.add(info);
                    }
                }
                if (fileAdapter != null) {
                    fileAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.f_data_video:
                typeFileDetailInfos.clear();
                for (int i = 0; i < allFileDetailInfos.size(); i++) {
                    InterfaceFile.pbui_Item_MeetDirFileDetailInfo info = allFileDetailInfos.get(i);
//                    if(Constant.isVideo(info.getMediaid())){
                    if (FileUtil.isAudioAndVideoFile(info.getName().toStringUtf8())) {
                        typeFileDetailInfos.add(info);
                    }
                }
                if (fileAdapter != null) {
                    fileAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.f_data_other:
                typeFileDetailInfos.clear();
                for (int i = 0; i < allFileDetailInfos.size(); i++) {
                    InterfaceFile.pbui_Item_MeetDirFileDetailInfo info = allFileDetailInfos.get(i);
//                    if(Constant.isOther(info.getMediaid())){
                    if (FileUtil.isOtherFile(info.getName().toStringUtf8())) {
                        typeFileDetailInfos.add(info);
                    }
                }
                if (fileAdapter != null) {
                    fileAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.f_data_push:
                if (fileAdapter != null) {
                    int chooseId = fileAdapter.getChooseId();
                    if (chooseId == -1) {
                        ToastUtil.show(R.string.please_choose_push_file);
                    } else {
                        presenter.pushFile(chooseId);
                    }
                }
                break;
            //导出资料
            case R.id.f_data_export:
                if (Constant.hasPermission(Constant.permission_code_download)) {
                    exportFile();
                } else {
                    ToastUtil.show(R.string.err_NoPermission);
                }
                break;
            //上一页
            case R.id.f_data_previous_btn:

                break;
            //下一页
            case R.id.f_data_nextpage_btn:

                break;
            default:
                break;
        }
    }
}
