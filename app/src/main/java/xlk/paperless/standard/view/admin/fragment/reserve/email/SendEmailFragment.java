package xlk.paperless.standard.view.admin.fragment.reserve.email;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.UriUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceMeet;
import com.mogujie.tt.protobuf.InterfaceMember;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.helper.EmailThread;
import xlk.paperless.standard.helper.SharedPreferenceHelper;
import xlk.paperless.standard.util.DateUtil;
import xlk.paperless.standard.util.ToastUtil;

/**
 * @author Created by xlk on 2020/11/13.
 * @desc
 */
public class SendEmailFragment extends BaseFragment implements SendEmailInterface, View.OnClickListener {

    private SendEmailPresenter presenter;
    private RecyclerView rv_member;
    private EditText edt_email_account;
    private EditText edt_email_pwd;
    private EditText edt_send_name;
    private EditText edt_sender_email;
    private EditText edt_recipient;
    private EditText edt_theme;
    private EditText edt_content;
    private Button btn_delete_file;
    private Button btn_add_file;
    private Button btn_send_email;
    private RecyclerView rv_file;
    private CheckBox cb_all;
    private MemberAdapter memberAdapter;
    private int REQUEST_CODE_EMAIL_FILE = 1;
    private List<File> emailFiles = new ArrayList<>();
    private FileAdapter fileAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_send_email, container, false);
        initView(inflate);
        initUserEmail();
        presenter = new SendEmailPresenter(this);
        reShow();
        return inflate;
    }

    private void initUserEmail() {
        String account = (String) SharedPreferenceHelper.getData(getContext(), SharedPreferenceHelper.key_email_account, "");
        String password = (String) SharedPreferenceHelper.getData(getContext(), SharedPreferenceHelper.key_email_password, "");
        String name = (String) SharedPreferenceHelper.getData(getContext(), SharedPreferenceHelper.key_email_name, "");
        String mailbox = (String) SharedPreferenceHelper.getData(getContext(), SharedPreferenceHelper.key_email_mailbox, "");
        edt_email_account.setText(account);
        edt_email_pwd.setText(password);
        edt_send_name.setText(name);
        edt_sender_email.setText(mailbox);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    protected void reShow() {
        presenter.queryMember();
        presenter.queryCurrentMeet();
    }

    @Override
    public void updateMember() {
        if (memberAdapter == null) {
            memberAdapter = new MemberAdapter(R.layout.item_member_email, presenter.members);
            rv_member.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_member.setAdapter(memberAdapter);
            memberAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    InterfaceMember.pbui_Item_MemberDetailInfo item = presenter.members.get(position);
                    int personid = item.getPersonid();
                    memberAdapter.setSelect(personid);
                    cb_all.setChecked(memberAdapter.isCheckAll());
                    updateUI(memberAdapter.getSelectedMember());
                }
            });
        } else {
            memberAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateMeetName(InterfaceMeet.pbui_Item_MeetMeetInfo currentMeet) {
        String meetName = currentMeet == null ? "" : currentMeet.getName().toStringUtf8();
        edt_theme.setText(meetName);
        String content = edt_content.getText().toString().trim();
        if (content.isEmpty() && currentMeet != null) {
            String startTime = DateUtil.secondFormatDateTime(currentMeet.getStartTime());
            String endTime = DateUtil.secondFormatDateTime(currentMeet.getEndTime());
            String useTime = startTime + "-" + endTime;
            String roomName = currentMeet.getRoomname().toStringUtf8();
            edt_content.setText(getString(R.string.default_email_content, meetName, roomName, useTime));
        }
    }

    private void updateUI(ArrayList<String> selectedMember) {
        boolean isFilter = false;
        String members = "";
        for (int i = 0; i < selectedMember.size(); i++) {
            String email = selectedMember.get(i);
            if (RegexUtils.isEmail(email)) {
                members += email + ";";
            } else {
                isFilter = true;
            }
        }
        if (members.isEmpty()) {
            edt_recipient.setText(members);
            return;
        }
        edt_recipient.setText(members);
        edt_recipient.setSelection(members.length());
        if (isFilter) {
            ToastUtil.show(R.string.already_filtered_email);
        }
    }

    public void initView(View rootView) {
        this.rv_member = (RecyclerView) rootView.findViewById(R.id.rv_member);
        this.edt_email_account = (EditText) rootView.findViewById(R.id.edt_email_account);
        this.edt_email_pwd = (EditText) rootView.findViewById(R.id.edt_email_pwd);
        this.edt_send_name = (EditText) rootView.findViewById(R.id.edt_send_name);
        this.edt_sender_email = (EditText) rootView.findViewById(R.id.edt_sender_email);
        this.edt_recipient = (EditText) rootView.findViewById(R.id.edt_recipient);
        this.edt_theme = (EditText) rootView.findViewById(R.id.edt_theme);
        this.edt_content = (EditText) rootView.findViewById(R.id.edt_content);
        this.btn_delete_file = (Button) rootView.findViewById(R.id.btn_delete_file);
        this.btn_add_file = (Button) rootView.findViewById(R.id.btn_add_file);
        this.btn_send_email = (Button) rootView.findViewById(R.id.btn_send_email);
        this.rv_file = (RecyclerView) rootView.findViewById(R.id.rv_file);
        this.cb_all = (CheckBox) rootView.findViewById(R.id.cb_all);
        cb_all.setOnClickListener(this);
        btn_delete_file.setOnClickListener(this);
        btn_add_file.setOnClickListener(this);
        btn_send_email.setOnClickListener(this);
        rv_file.setLayoutManager(new LinearLayoutManager(getContext()));
        fileAdapter = new FileAdapter(R.layout.item_signin_text, emailFiles);
        rv_file.setAdapter(fileAdapter);
        fileAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                fileAdapter.setSelect(position);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_EMAIL_FILE) {
            Uri uri = data.getData();
            File file = UriUtils.uri2File(uri);
            if (file != null) {
                addEmailFile(file);
            }
        }
    }

    private void delEmailFile(int index) {
        if (index == -1) {
            ToastUtil.show(R.string.please_choose_file_first);
            return;
        }
        emailFiles.remove(emailFiles.get(index));
        fileAdapter.notifyDataSetChanged();
    }

    private void addEmailFile(File file) {
        for (int i = 0; i < emailFiles.size(); i++) {
            File file1 = emailFiles.get(i);
            if (file1.getAbsolutePath().equals(file.getAbsolutePath())) {
                ToastUtil.show(R.string.file_already_exists);
                return;
            }
        }
        emailFiles.add(file);
        fileAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cb_all: {
                if (memberAdapter != null) {
                    boolean checked = cb_all.isChecked();
                    cb_all.setChecked(checked);
                    memberAdapter.setCheckAll(checked);
                    updateUI(memberAdapter.getSelectedMember());
                }
                break;
            }
            case R.id.btn_delete_file: {
                delEmailFile(fileAdapter.getSelect());
                break;
            }
            case R.id.btn_add_file: {
                chooseLocalFile(REQUEST_CODE_EMAIL_FILE);
                break;
            }
            case R.id.btn_send_email: {
                String account = edt_email_account.getText().toString().trim();
                String pwd = edt_email_pwd.getText().toString().trim();
                String name = edt_send_name.getText().toString().trim();
                String email = edt_sender_email.getText().toString().trim();
                if (!account.isEmpty() && RegexUtils.isEmail(email)) {
                    SharedPreferenceHelper.setData(getContext(), SharedPreferenceHelper.key_email_account, account);
                } else {
                    ToastUtil.show(R.string.please_enter_account);
                    return;
                }
                if (!pwd.isEmpty()) {
                    SharedPreferenceHelper.setData(getContext(), SharedPreferenceHelper.key_email_password, pwd);
                } else {
                    ToastUtil.show(R.string.please_enter_password);
                    return;
                }
                if (!name.isEmpty()) {
                    SharedPreferenceHelper.setData(getContext(), SharedPreferenceHelper.key_email_name, name);
                } else {
                    ToastUtil.show(R.string.please_enter_user_name);
                    return;
                }
                if (!email.isEmpty() && RegexUtils.isEmail(email)) {
                    SharedPreferenceHelper.setData(getContext(), SharedPreferenceHelper.key_email_mailbox, email);
                } else {
                    ToastUtil.show(R.string.please_enter_account);
                    return;
                }
                String theme = edt_theme.getText().toString().trim();
                String content = edt_content.getText().toString().trim();
                if (theme.isEmpty() || content.isEmpty()) {
                    ToastUtil.show(R.string.please_enter_theme_content);
                    return;
                }
                String trim = edt_recipient.getText().toString().trim();
                if (trim.isEmpty()) {
                    ToastUtil.show(R.string.please_enter_receive_email);
                    return;
                }
                String[] split = trim.split(";");
                List<String> strings = Arrays.asList(split);
                for (int i = 0; i < strings.size(); i++) {
                    String s = strings.get(i);
                    if (!RegexUtils.isEmail(s)) {
                        ToastUtil.show(R.string.err_receive_email_format);
                        return;
                    }
                }
                new EmailThread(account, name, pwd, strings, emailFiles, theme, content).start();
                break;
            }
            default:
                break;
        }
    }
}
