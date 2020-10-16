package xlk.paperless.standard.view.admin.fragment.system.other;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BaseFragment;

/**
 * @author Created by xlk on 2020/9/22.
 * @desc
 */
public class AdminOtherFragment extends BaseFragment implements AdminOtherInterface, View.OnClickListener {
    private EditText edt_url;
    private Button btn_url_submit;
    private Button btn_update;
    private EditText edt_company;
    private Button btn_company;
    private EditText edt_file;
    private Button btn_file_modify;
    private EditText edt_old_pwd;
    private EditText edt_new_pwd;
    private EditText edt_confirm_pwd;
    private Button btn_submit_modify;
    private Button btn_change_home;
    private Button btn_change_pro;
    private Button btn_change_bulletin;
    private AdminOtherPresenter presenter;
    private int currentAdminId;
    private String currentAdminPwd;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_other, container, false);
        initView(inflate);
        presenter = new AdminOtherPresenter(getContext(), this);
        presenter.webQuery();
        presenter.queryCompany();
        presenter.queryReleaseFile();
        currentAdminId = getActivity().getIntent().getIntExtra(Constant.EXTRA_ADMIN_ID, -1);
        currentAdminPwd = getActivity().getIntent().getStringExtra(Constant.EXTRA_ADMIN_PASSWORD);
        return inflate;
    }

    private void initView(View inflate) {
        edt_url = (EditText) inflate.findViewById(R.id.edt_url);
        btn_url_submit = (Button) inflate.findViewById(R.id.btn_url_submit);
        btn_update = (Button) inflate.findViewById(R.id.btn_update);
        edt_company = (EditText) inflate.findViewById(R.id.edt_company);
        btn_company = (Button) inflate.findViewById(R.id.btn_company);
        edt_file = (EditText) inflate.findViewById(R.id.edt_file);
        btn_file_modify = (Button) inflate.findViewById(R.id.btn_file_modify);
        edt_old_pwd = (EditText) inflate.findViewById(R.id.edt_old_pwd);
        edt_new_pwd = (EditText) inflate.findViewById(R.id.edt_new_pwd);
        edt_confirm_pwd = (EditText) inflate.findViewById(R.id.edt_confirm_pwd);
        btn_submit_modify = (Button) inflate.findViewById(R.id.btn_submit_modify);
        btn_change_home = (Button) inflate.findViewById(R.id.btn_change_home);
        btn_change_pro = (Button) inflate.findViewById(R.id.btn_change_pro);
        btn_change_bulletin = (Button) inflate.findViewById(R.id.btn_change_bulletin);

        btn_url_submit.setOnClickListener(this);
        btn_update.setOnClickListener(this);
        btn_company.setOnClickListener(this);
        btn_file_modify.setOnClickListener(this);
        btn_submit_modify.setOnClickListener(this);
        btn_change_home.setOnClickListener(this);
        btn_change_pro.setOnClickListener(this);
        btn_change_bulletin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_url_submit:
                String currentUrl = edt_url.getText().toString();
                if (TextUtils.isEmpty(currentUrl)) {
                    ToastUtil.show(R.string.conent_isEmpty);
                    return;
                }
                presenter.modifyUrl(currentUrl);
                break;
            case R.id.btn_update:

                break;
            case R.id.btn_company:
                String company = edt_company.getText().toString().trim();
                if (TextUtils.isEmpty(company)) {
                    ToastUtil.show(R.string.conent_isEmpty);
                    break;
                }
                presenter.modifyCompany(company);
                break;
            case R.id.btn_file_modify:

                break;
            case R.id.btn_submit_modify:

                break;
            case R.id.btn_change_home:

                break;
            case R.id.btn_change_pro:

                break;
            case R.id.btn_change_bulletin:

                break;
        }
    }

    @Override
    public void updateUrl(String addr) {
        edt_url.setText(addr);
    }

    @Override
    public void updateCompany(String company) {
        edt_company.setText(company);
    }

    @Override
    public void updateReleaseFile(String fileName) {
        edt_file.setText(fileName);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
