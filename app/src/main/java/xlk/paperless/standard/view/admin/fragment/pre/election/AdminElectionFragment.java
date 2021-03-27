package xlk.paperless.standard.view.admin.fragment.pre.election;

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

import static xlk.paperless.standard.data.Constant.REQUEST_CODE_IMPORT_ELECTION;
import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author Created by xlk on 2020/10/23.
 * @desc
 */
public class AdminElectionFragment extends BaseFragment implements AdminElectionInterface, View.OnClickListener {

    private AdminElectionPresenter presenter;
    private RecyclerView rv_election;
    private EditText edt_election_content;
    private Spinner sp_type;
    private Spinner sp_register;
    private EditText edt_option1;
    private EditText edt_option2;
    private EditText edt_option3;
    private EditText edt_option4;
    private EditText edt_option5;
    private Button btn_add;
    private Button btn_modify;
    private Button btn_delete;
    private Button btn_export;
    private Button btn_import;
    private AdminElectionAdapter electionAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_election, container, false);
        initView(inflate);
        presenter = new AdminElectionPresenter(this);
        presenter.queryElection();
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    protected void reShow() {
        presenter.queryElection();
    }

    public void initView(View rootView) {
        this.rv_election = (RecyclerView) rootView.findViewById(R.id.rv_election);
        this.edt_election_content = (EditText) rootView.findViewById(R.id.edt_election_content);
        this.sp_type = (Spinner) rootView.findViewById(R.id.sp_type);
        this.sp_register = (Spinner) rootView.findViewById(R.id.sp_register);
        this.edt_option1 = (EditText) rootView.findViewById(R.id.edt_option1);
        this.edt_option2 = (EditText) rootView.findViewById(R.id.edt_option2);
        this.edt_option3 = (EditText) rootView.findViewById(R.id.edt_option3);
        this.edt_option4 = (EditText) rootView.findViewById(R.id.edt_option4);
        this.edt_option5 = (EditText) rootView.findViewById(R.id.edt_option5);
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
                createElection();
                break;
            case R.id.btn_modify:
                modifyElection();
                break;
            case R.id.btn_delete:
                deleteElection();
                break;
            case R.id.btn_export:
                List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> electionInfo = presenter.getElectionInfo();
                if (electionInfo.isEmpty()) {
                    ToastUtil.show(R.string.no_election_info);
                    return;
                }
                JxlUtil.exportVoteInfo(electionInfo, getString(R.string.election_fileName), getString(R.string.election_content));
                break;
            case R.id.btn_import:
                chooseLocalFile(REQUEST_CODE_IMPORT_ELECTION);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMPORT_ELECTION) {
            Uri uri = data.getData();
            String filePath = UriUtil.getFilePath(getContext(), uri);
            if (filePath != null) {
                LogUtil.i(TAG, "onActivityResult filePath=" + filePath);
                if (filePath.endsWith("xls") || filePath.endsWith("xlsx")) {
                    List<InterfaceVote.pbui_Item_MeetOnVotingDetailInfo> infos = JxlUtil.readVoteXls(filePath, InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_election_VALUE);
                    jni.createMultipleVote(infos);
                } else {
                    ToastUtil.show(R.string.please_choose_xls_file);
                }
            }
        }
    }

    private void createElection() {
        String content = edt_election_content.getText().toString().trim();
        if (content.isEmpty()) {
            ToastUtil.show(R.string.please_enter_election_content);
            return;
        }
        String trim1 = edt_option1.getText().toString().trim();
        String trim2 = edt_option2.getText().toString().trim();
        String trim3 = edt_option3.getText().toString().trim();
        String trim4 = edt_option4.getText().toString().trim();
        String trim5 = edt_option5.getText().toString().trim();
        int position = sp_type.getSelectedItemPosition();
        List<ByteString> answers = new ArrayList<>();
        if (position == InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_4_5_VALUE
                || (position == InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_3_5_VALUE)
                || (position == InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_5_VALUE)
        ) {
            //5个选项都必须填写
            if (trim1.isEmpty() || trim2.isEmpty() || trim3.isEmpty() || trim4.isEmpty() || trim5.isEmpty()) {
                ToastUtil.show(R.string.please_enter_5_answer);
                return;
            } else {
                answers.add(s2b(trim1));
                answers.add(s2b(trim2));
                answers.add(s2b(trim3));
                answers.add(s2b(trim4));
                answers.add(s2b(trim5));
            }
        } else if (position == InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_3_VALUE) {
            //最少需要填写3个选项
            if (trim1.isEmpty() || trim2.isEmpty() || trim3.isEmpty()) {
                ToastUtil.show(R.string.please_enter_3_answer);
                return;
            } else {
                answers.add(s2b(trim1));
                answers.add(s2b(trim2));
                answers.add(s2b(trim3));
            }
        } else {
            if (trim1.isEmpty() || trim2.isEmpty()) {
                ToastUtil.show(R.string.min_enter_2_answer);
                return;
            }
            answers.add(s2b(trim1));
            answers.add(s2b(trim2));
            if (!trim3.isEmpty()) {
                answers.add(s2b(trim3));
            }
            if (!trim4.isEmpty()) {
                answers.add(s2b(trim4));
            }
            if (!trim5.isEmpty()) {
                answers.add(s2b(trim5));
            }
        }
        InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder()
                .setContent(s2b(content))
                .setMaintype(InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_election_VALUE)
                .setMode(sp_register.getSelectedItemPosition())
                .setType(sp_type.getSelectedItemPosition())
                .addAllText(answers)
                .setSelectcount(answers.size())
                .build();
        jni.createVote(build);
    }

    private void modifyElection() {
        if (electionAdapter == null) {
            return;
        }
        InterfaceVote.pbui_Item_MeetVoteDetailInfo selected = electionAdapter.getSelected();
        if (selected == null) {
            ToastUtil.show(R.string.please_choose_vote);
            return;
        }
        String content = edt_election_content.getText().toString().trim();
        if (content.isEmpty()) {
            ToastUtil.show(R.string.please_enter_election_content);
            return;
        }
        String trim1 = edt_option1.getText().toString().trim();
        String trim2 = edt_option2.getText().toString().trim();
        String trim3 = edt_option3.getText().toString().trim();
        String trim4 = edt_option4.getText().toString().trim();
        String trim5 = edt_option5.getText().toString().trim();
        int position = sp_type.getSelectedItemPosition();
        LogUtil.i(TAG, "modifyElection positinn=" + position);
        List<ByteString> answers = new ArrayList<>();
        if (position == InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_4_5_VALUE
                || (position == InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_3_5_VALUE)
                || (position == InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_5_VALUE)
        ) {
            //5个选项都必须填写
            if (trim1.isEmpty() || trim2.isEmpty() || trim3.isEmpty() || trim4.isEmpty() || trim5.isEmpty()) {
                ToastUtil.show(R.string.please_enter_5_answer);
                return;
            } else {
                answers.add(s2b(trim1));
                answers.add(s2b(trim2));
                answers.add(s2b(trim3));
                answers.add(s2b(trim4));
                answers.add(s2b(trim5));
            }
        } else if (position == InterfaceMacro.Pb_MeetVote_SelType.Pb_VOTE_TYPE_2_3_VALUE) {
            //最少需要填写3个选项
            if (trim1.isEmpty() || trim2.isEmpty() || trim3.isEmpty()) {
                ToastUtil.show(R.string.please_enter_3_answer);
                return;
            } else {
                answers.add(s2b(trim1));
                answers.add(s2b(trim2));
                answers.add(s2b(trim3));
            }
        } else {
            if (trim1.isEmpty() || trim2.isEmpty()) {
                ToastUtil.show(R.string.min_enter_2_answer);
                return;
            }
            answers.add(s2b(trim1));
            answers.add(s2b(trim2));
            if (!trim3.isEmpty()) {
                answers.add(s2b(trim3));
            }
            if (!trim4.isEmpty()) {
                answers.add(s2b(trim4));
            }
            if (!trim5.isEmpty()) {
                answers.add(s2b(trim5));
            }
        }
        InterfaceVote.pbui_Item_MeetOnVotingDetailInfo build = InterfaceVote.pbui_Item_MeetOnVotingDetailInfo.newBuilder()
                .setVoteid(selected.getVoteid())
                .setMaintype(InterfaceMacro.Pb_MeetVoteType.Pb_VOTE_MAINTYPE_election_VALUE)
                .setContent(s2b(content))
                .setMode(sp_register.getSelectedItemPosition())
                .setType(position)
                .addAllText(answers)
                .setSelectcount(answers.size())
                .build();
        jni.modifyVote(build);
    }

    private void deleteElection() {
        if (electionAdapter == null) {
            return;
        }
        InterfaceVote.pbui_Item_MeetVoteDetailInfo selected = electionAdapter.getSelected();
        if (selected == null) {
            ToastUtil.show(R.string.please_choose_vote);
            return;
        }
        jni.deleteVote(selected.getVoteid());
    }

    @Override
    public void updateElectionRv(List<InterfaceVote.pbui_Item_MeetVoteDetailInfo> electionInfo) {
        if (electionAdapter == null) {
            electionAdapter = new AdminElectionAdapter(R.layout.item_admin_election, electionInfo);
            rv_election.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_election.setAdapter(electionAdapter);
            electionAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    InterfaceVote.pbui_Item_MeetVoteDetailInfo item = electionInfo.get(position);
                    electionAdapter.setSelected(item.getVoteid());
                    edt_election_content.setText(item.getContent().toStringUtf8());
                    edt_option1.setText("");
                    edt_option2.setText("");
                    edt_option3.setText("");
                    edt_option4.setText("");
                    edt_option5.setText("");
                    sp_register.setSelection(item.getMode());
                    sp_type.setSelection(item.getType());
                    for (int i = 0; i < item.getItemList().size(); i++) {
                        String text = item.getItemList().get(i).getText().toStringUtf8();
                        if (i == 0) {
                            edt_option1.setText(text);
                        }
                        if (i == 1) {
                            edt_option2.setText(text);
                        }
                        if (i == 2) {
                            edt_option3.setText(text);
                        }
                        if (i == 3) {
                            edt_option4.setText(text);
                        }
                        if (i == 4) {
                            edt_option5.setText(text);
                        }
                    }
                }
            });
        } else {
            electionAdapter.notifyDataSetChanged();
        }
    }
}
