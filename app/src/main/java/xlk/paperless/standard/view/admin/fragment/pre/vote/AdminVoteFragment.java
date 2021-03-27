package xlk.paperless.standard.view.admin.fragment.pre.vote;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.google.protobuf.ByteString;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceVote;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.util.JxlUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.util.UriUtil;

import static xlk.paperless.standard.data.Constant.REQUEST_CODE_IMPORT_VOTE;
import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author Created by xlk on 2020/10/23.
 * @desc
 */
public class AdminVoteFragment extends BaseFragment implements AdminVoteInterface, View.OnClickListener {
    private RecyclerView rv_vote;
    private EditText edt_vote_content;
    private Spinner sp;
    private Button btn_add;
    private Button btn_modify;
    private Button btn_delete;
    private Button btn_export;
    private Button btn_import;
    private AdminVotePresenter presenter;
    private AdminVoteAdapter voteAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_vote, container, false);
        initView(inflate);
        presenter = new AdminVotePresenter(this);
        presenter.queryVote();
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public void reShow() {
        presenter.queryVote();
    }

    public void initView(View rootView) {
        this.rv_vote = (RecyclerView) rootView.findViewById(R.id.rv_vote);
        this.edt_vote_content = (EditText) rootView.findViewById(R.id.edt_vote_content);
        this.sp = (Spinner) rootView.findViewById(R.id.sp);
        this.btn_add = (Button) rootView.findViewById(R.id.btn_add);
        this.btn_modify = (Button) rootView.findViewById(R.id.btn_modify);
        this.btn_delete = (Button) rootView.findViewById(R.id.btn_delete);
        this.btn_export = (Button) rootView.findViewById(R.id.btn_export);
        this.btn_import = (Button) rootView.findViewById(R.id.btn_import);
        btn_add.setOnClickListener(this);
        btn_modify.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_export.setOnClickListener(this);
        btn_import.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                createVote();
                break;
            case R.id.btn_modify:
                modifyVote();
                break;
            case R.id.btn_delete:
                deleteVote();
                break;
            case R.id.btn_export:
                List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteInfo = presenter.getVoteInfo();
                if (voteInfo.isEmpty()) {
                    ToastUtil.show(R.string.no_vote_info);
                    return;
                }
                JxlUtil.exportVoteInfo(voteInfo, getString(R.string.vote_fileName), getString(R.string.vote_content));
                break;
            case R.id.btn_import:
                chooseLocalFile(REQUEST_CODE_IMPORT_VOTE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMPORT_VOTE) {
            Uri uri = data.getData();
            String filePath = UriUtil.getFilePath(getContext(), uri);
            if (filePath != null) {
                LogUtil.i(TAG, "onActivityResult filePath=" + filePath);
                if (filePath.endsWith(".xls") || filePath.endsWith(".xlsx")) {
                    List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> infos = JxlUtil.readVoteXls(filePath,
                            InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote_VALUE);
                    jni.createMultipleVote(infos);
                } else {
                    ToastUtil.show(R.string.please_choose_xls_file);
                }
            }
        }
    }

    private void deleteVote() {
        if (voteAdapter == null) {
            return;
        }
        if (voteAdapter.getSelected() == null) {
            ToastUtil.show(R.string.please_choose_vote);
            return;
        }
        jni.deleteVote(voteAdapter.getSelected().getVoteid());
    }

    private void modifyVote() {
        String content = edt_vote_content.getText().toString().trim();
        if (content.isEmpty()) {
            ToastUtil.show(R.string.please_enter_vote_content);
            return;
        }
        if (voteAdapter == null) {
            return;
        }
        InterfaceVote.pbui_Item_MeetVoteDetailInfo selected = voteAdapter.getSelected();
        if (selected == null) {
            ToastUtil.show(R.string.please_choose_vote);
            return;
        }
        List<ByteString> answers = new ArrayList<>();
        answers.add(s2b("赞成"));
        answers.add(s2b("反对"));
        answers.add(s2b("弃权"));
        InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder()
                .setVoteid(selected.getVoteid())
                .setContent(s2b(content))
                .setMaintype(selected.getMaintype())
                .setMode(sp.getSelectedItemPosition())
                .setType(selected.getType())
                .setTimeouts(selected.getTimeouts())
                .setSelectcount(answers.size())
                .addAllText(answers)
                .build();
        jni.modifyVote(build);
    }

    private void createVote() {
        String content = edt_vote_content.getText().toString().trim();
        if (content.isEmpty()) {
            ToastUtil.show(R.string.please_enter_vote_content);
            return;
        }
        List<ByteString> answers = new ArrayList<>();
        answers.add(s2b("赞成"));
        answers.add(s2b("反对"));
        answers.add(s2b("弃权"));
        InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder()
                .setContent(s2b(content))
                .setMaintype(InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_vote_VALUE)
                .setMode(sp.getSelectedItemPosition())
                .setType(InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_SINGLE_VALUE)
                .setSelectcount(answers.size())
                .addAllText(answers)
                .build();
        jni.createVote(build);
    }

    @Override
    public void updateVoteRv(List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> voteInfo) {
        if (voteAdapter == null) {
            voteAdapter = new AdminVoteAdapter(R.layout.item_admin_vote, voteInfo);
            rv_vote.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_vote.setAdapter(voteAdapter);
            voteAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    InterfaceVote.pbui_Item_MeetVoteDetailInfo item = voteInfo.get(position);
                    voteAdapter.setSelected(item.getVoteid());
                    edt_vote_content.setText(item.getContent().toStringUtf8());
                    sp.setSelection(item.getMode());
                }
            });
        } else {
            voteAdapter.notifyDataSetChanged();
        }
    }
}
