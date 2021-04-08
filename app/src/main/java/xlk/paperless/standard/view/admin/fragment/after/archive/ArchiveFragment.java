package xlk.paperless.standard.view.admin.fragment.after.archive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.App;

/**
 * @author Created by xlk on 2020/10/27.
 * @desc
 */
public class ArchiveFragment extends BaseFragment implements ArchiveInterface, View.OnClickListener {

    private ArchivePresenter presenter;
    private CheckBox cb_meeting_info;
    private CheckBox cb_member_info;
    private CheckBox cb_signin_info;
    private CheckBox cb_vote_info;
    private CheckBox cb_share_file;
    private CheckBox cb_annotation_file;
    private CheckBox cb_meeting_date;
    private CheckBox cb_check_all;
    private RecyclerView rv_operate;
    private CheckBox cb_encryption;
    private Button btn_start_archive;
    private Button btn_cancel_archive;
    private ArchiveInformAdapter informAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_archive, container, false);
        initView(inflate);
        presenter = new ArchivePresenter(this);
        reShow();
        return inflate;
    }

    @Override
    protected void reShow() {
        presenter.queryAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    public void initView(View rootView) {
        this.rv_operate = (RecyclerView) rootView.findViewById(R.id.rv_operate);
        this.cb_meeting_info = (CheckBox) rootView.findViewById(R.id.cb_meeting_info);
        this.cb_member_info = (CheckBox) rootView.findViewById(R.id.cb_member_info);
        this.cb_signin_info = (CheckBox) rootView.findViewById(R.id.cb_signin_info);
        this.cb_vote_info = (CheckBox) rootView.findViewById(R.id.cb_vote_info);
        this.cb_share_file = (CheckBox) rootView.findViewById(R.id.cb_share_file);
        this.cb_annotation_file = (CheckBox) rootView.findViewById(R.id.cb_annotation_file);
        this.cb_meeting_date = (CheckBox) rootView.findViewById(R.id.cb_meeting_date);
        this.cb_check_all = (CheckBox) rootView.findViewById(R.id.cb_check_all);
        this.cb_encryption = (CheckBox) rootView.findViewById(R.id.cb_encryption);
        this.btn_start_archive = (Button) rootView.findViewById(R.id.btn_start_archive);
        this.btn_cancel_archive = (Button) rootView.findViewById(R.id.btn_cancel_archive);

        cb_meeting_info.setOnClickListener(this);
        cb_member_info.setOnClickListener(this);
        cb_signin_info.setOnClickListener(this);
        cb_vote_info.setOnClickListener(this);
        cb_share_file.setOnClickListener(this);
        cb_annotation_file.setOnClickListener(this);
        cb_meeting_date.setOnClickListener(this);
        cb_check_all.setOnClickListener(this);

        btn_start_archive.setOnClickListener(this);
        btn_cancel_archive.setOnClickListener(this);
    }

    private boolean isCheckAll() {
        return cb_meeting_info.isChecked() &&
                cb_member_info.isChecked() &&
                cb_signin_info.isChecked() &&
                cb_vote_info.isChecked() &&
                cb_share_file.isChecked() &&
                cb_annotation_file.isChecked() &&
                cb_meeting_date.isChecked();
    }

    private void setCheck(CheckBox cb) {
        cb.setChecked(cb.isChecked());
        cb_check_all.setChecked(isCheckAll());
    }

    private void setCheckAll() {
        cb_check_all.setChecked(cb_check_all.isChecked());
        boolean checked = cb_check_all.isChecked();
        cb_meeting_info.setChecked(checked);
        cb_member_info.setChecked(checked);
        cb_signin_info.setChecked(checked);
        cb_vote_info.setChecked(checked);
        cb_share_file.setChecked(checked);
        cb_annotation_file.setChecked(checked);
        cb_meeting_date.setChecked(checked);
    }

    @Override
    public void showToast(int resid) {
        getActivity().runOnUiThread(() -> ToastUtil.show(resid));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cb_meeting_info:
                setCheck(cb_meeting_info);
                break;
            case R.id.cb_member_info:
                setCheck(cb_member_info);
                break;
            case R.id.cb_signin_info:
                setCheck(cb_signin_info);
                break;
            case R.id.cb_vote_info:
                setCheck(cb_vote_info);
                break;
            case R.id.cb_share_file:
                setCheck(cb_share_file);
                break;
            case R.id.cb_annotation_file:
                setCheck(cb_annotation_file);
                break;
            case R.id.cb_meeting_date:
                setCheck(cb_meeting_date);
                break;
            case R.id.cb_check_all:
                setCheckAll();
                break;
            case R.id.btn_start_archive:
                startArchive();
                break;
            case R.id.btn_cancel_archive:
                break;
            default:
                break;
        }
    }

    private void startArchive() {
        if (presenter.hasStarted()) {
            ToastUtil.show(R.string.please_wait_archive_complete_first);
            return;
        }
        App.threadPool.execute(() -> {
            boolean isEncryption = cb_encryption.isChecked();
            presenter.setEncryption(isEncryption);
            if (cb_check_all.isChecked()) {
                presenter.archiveAll();
            } else {
                presenter.archiveSelected(cb_meeting_info.isChecked(),
                        cb_member_info.isChecked(), cb_signin_info.isChecked(),
                        cb_vote_info.isChecked(), cb_share_file.isChecked(),
                        cb_annotation_file.isChecked(), cb_meeting_date.isChecked());
            }
        });
    }

    @Override
    public void updateArchiveInform(List<ArchiveInform> archiveInforms) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (informAdapter == null) {
                    informAdapter = new ArchiveInformAdapter(R.layout.item_admin_archive_operate, archiveInforms);
                    rv_operate.setLayoutManager(new LinearLayoutManager(getContext()));
                    rv_operate.setAdapter(informAdapter);
                } else {
                    informAdapter.notifyDataSetChanged();
                }
                if (!archiveInforms.isEmpty()) {
                    int lastIndex = archiveInforms.size() - 1;
                    ArchiveInform archiveInform = archiveInforms.get(lastIndex);
                    if (archiveInform.getContent().equals("压缩")) {
                        rv_operate.scrollToPosition(lastIndex);
                    }
                }
            }
        });
    }
}
