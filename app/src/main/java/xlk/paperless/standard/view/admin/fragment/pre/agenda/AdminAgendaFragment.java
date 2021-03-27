package xlk.paperless.standard.view.admin.fragment.pre.agenda;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.util.UriUtil;

/**
 * @author Created by xlk on 2020/10/20.
 * @desc
 */
public class AdminAgendaFragment extends BaseFragment implements AdminAgendaInterface, View.OnClickListener {

    private AdminAgendaPresenter presenter;
    private CheckBox cb_file;
    private Button btn_choose_file;
    private TextView edt_file_name;
    private CheckBox cb_edit;
    private Button btn_import_agenda;
    private EditText edt_agenda_content;
    private Button btn_save_agenda;
    private PopupWindow agendaFilePop;
    private RecyclerView rv_agenda_file;
    private RelativeLayout progress_bar_rl;
    private AgendaFileAdapter agendaFileAdapter;
    private final int REQUEST_CODE_AGENDA_TEXT = 0;
    private final int REQUEST_CODE_AGENDA_FILE = 1;
    private int currentMediaId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_admin_agenda, container, false);
        initView(inflate);
        presenter = new AdminAgendaPresenter(this);
        presenter.queryAgenda();
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    private void initView(View inflate) {
        cb_file = inflate.findViewById(R.id.cb_file);
        btn_choose_file = inflate.findViewById(R.id.btn_choose_file);
        edt_file_name = inflate.findViewById(R.id.edt_file_name);
        cb_edit = inflate.findViewById(R.id.cb_edit);
        btn_import_agenda = inflate.findViewById(R.id.btn_import_agenda);
        edt_agenda_content = inflate.findViewById(R.id.edt_agenda_content);
        btn_save_agenda = inflate.findViewById(R.id.btn_save_agenda);
        progress_bar_rl = inflate.findViewById(R.id.progress_bar_rl);

        cb_file.setOnClickListener(this);
        cb_edit.setOnClickListener(this);
        btn_choose_file.setOnClickListener(this);
        btn_import_agenda.setOnClickListener(this);
        btn_save_agenda.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cb_file: {
                boolean checked = cb_file.isChecked();
                cb_file.setChecked(checked);
                if (cb_file.isChecked()) {
                    cb_edit.setChecked(!cb_file.isChecked());
                }
                changeUI(!cb_file.isChecked());
                break;
            }
            case R.id.cb_edit: {
                boolean checked = cb_edit.isChecked();
                cb_edit.setChecked(checked);
                if (cb_edit.isChecked()) {
                    cb_file.setChecked(!cb_edit.isChecked());
                }
                changeUI(cb_edit.isChecked());
                break;
            }
            case R.id.btn_choose_file:
                presenter.queryShareFile();
                showAgendaFilePop(presenter.getAgendaFiles());
                break;
            case R.id.btn_import_agenda:
                findLocalFile("text/plain", REQUEST_CODE_AGENDA_TEXT);
                break;
            case R.id.btn_save_agenda:
                if (cb_file.isChecked()) {
                    if (currentMediaId != 0) {
                        presenter.modifyFileAgenda(currentMediaId);
                    } else {
                        ToastUtil.show(R.string.please_choose_file_first);
                    }
                } else {
                    String content = edt_agenda_content.getText().toString().trim();
                    if (TextUtils.isEmpty(content)) {
                        ToastUtil.show(R.string.please_enter_agenda_content);
                        return;
                    }
                    presenter.modifyTextAgenda(content);
                }
                break;
            default:
                break;
        }
    }

    private void showAgendaFilePop(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> agendaFiles) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_agenda_file, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showAgendaFilePop fragment的大小 width=" + width + ",height=" + height);
        agendaFilePop = new PopupWindow(inflate, width * 2 / 3, height / 2);
        agendaFilePop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        agendaFilePop.setTouchable(true);
        // true:设置触摸外面时消失
        agendaFilePop.setOutsideTouchable(true);
        agendaFilePop.setFocusable(true);
        agendaFilePop.setAnimationStyle(R.style.pop_Animation);
        agendaFilePop.showAtLocation(btn_choose_file, Gravity.CENTER, 0, 0);
        rv_agenda_file = inflate.findViewById(R.id.rv_agenda_file);
        agendaFileAdapter = new AgendaFileAdapter(R.layout.item_agenda_file, agendaFiles);
        rv_agenda_file.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_agenda_file.setAdapter(agendaFileAdapter);
        agendaFileAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                agendaFileAdapter.setSelected(agendaFiles.get(position).getMediaid());
            }
        });
        inflate.findViewById(R.id.btn_increase).setOnClickListener(v -> {
            findLocalFile("application/msword;application/pdf", REQUEST_CODE_AGENDA_FILE);
        });
        inflate.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo selected = agendaFileAdapter.getSelected();
            if (selected == null) {
                ToastUtil.show(R.string.please_choose_file_first);
                return;
            }
            presenter.delFile(selected);
        });
        inflate.findViewById(R.id.btn_determine).setOnClickListener(v -> {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo selected = agendaFileAdapter.getSelected();
            if (selected == null) {
                ToastUtil.show(R.string.please_choose_file_first);
                return;
            }
            currentMediaId = selected.getMediaid();
            edt_file_name.setText(selected.getName().toStringUtf8());
            agendaFilePop.dismiss();
        });
        inflate.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            agendaFilePop.dismiss();
        });
    }

    private void findLocalFile(String type, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(type);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void showProgressBar(boolean show) {
        progress_bar_rl.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String filePath = UriUtil.getFilePath(getContext(), uri);
            if (requestCode == REQUEST_CODE_AGENDA_TEXT) {
                LogUtil.i(TAG, "onActivityResult 导入议程内容的文件=" + filePath);
                if (filePath != null) {
                    if (filePath.endsWith(".txt")) {
                        showProgressBar(true);
                        FileUtil.readTxtFile(Constant.BUS_READ_AGENDA_TXT, filePath);
                    } else {
                        ToastUtil.show(R.string.can_only_choose_txt_file);
                    }
                }
            } else if (requestCode == REQUEST_CODE_AGENDA_FILE) {
                LogUtil.i(TAG, "onActivityResult 上传的议程文件=" + filePath);
                if (filePath != null) {
                    if (filePath.endsWith(".doc") || filePath.endsWith(".docx") || filePath.endsWith(".pdf")) {
                        File file = new File(filePath);
                        presenter.uploadFile(InterfaceMacro.Pb_Upload_Flag.Pb_MEET_UPLOADFLAG_ONLYENDCALLBACK_VALUE,
                                1, 0, file.getName(), filePath,
                                0,  Constant.UPLOAD_CHOOSE_FILE);
                    } else {
                        ToastUtil.show(R.string.can_only_choose_agenda_file);
                    }
                }
            }
        }
    }

    private void changeUI(boolean isEdit) {
        cb_file.setChecked(!isEdit);
        cb_edit.setChecked(isEdit);

        btn_choose_file.setBackgroundResource(isEdit ? R.drawable.shape_enable_false : R.drawable.shape_btn_pressed);
        btn_choose_file.setEnabled(!isEdit);
        edt_agenda_content.setVisibility(isEdit ? View.VISIBLE : View.INVISIBLE);
        btn_import_agenda.setEnabled(isEdit);
        btn_import_agenda.setBackgroundResource(!isEdit ? R.drawable.shape_enable_false : R.drawable.shape_btn_pressed);
    }

    @Override
    public void updateAgendaFileRv() {
        if (agendaFilePop != null && agendaFilePop.isShowing()) {
            agendaFileAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateAgendaContent(String agendaContent) {
        changeUI(true);
        edt_agenda_content.setText(agendaContent);
    }

    @Override
    public void updateAgendaFileName(int mediaId, String fileName) {
        changeUI(false);
        currentMediaId = mediaId;
        edt_file_name.setText(fileName);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            presenter.queryAgenda();
        }
    }
}
