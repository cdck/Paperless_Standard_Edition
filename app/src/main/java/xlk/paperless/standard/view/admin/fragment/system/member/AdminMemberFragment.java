package xlk.paperless.standard.view.admin.fragment.system.member;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.mogujie.tt.protobuf.InterfacePerson;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.JxlUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.util.UriUtil;
import xlk.paperless.standard.base.BaseFragment;

/**
 * @author Created by xlk on 2020/9/21.
 * @desc
 */
public class AdminMemberFragment extends BaseFragment implements AdminMemberInterface, View.OnClickListener {
    private final String TAG = "AdminMemberFragment-->";
    final int EXCEL_FILE_REQUEST_CODE = 1;
    private RecyclerView rv_member;
    private EditText edt_name;
    private EditText edt_unit;
    private EditText edt_position;
    private EditText edt_remarks;
    private EditText edt_phone;
    private EditText edt_email;
    private EditText edt_pwd;
    private Button btn_increase;
    private Button btn_modify;
    private Button btn_delete;
    private Button btn_import;
    private Button btn_export;
    private AdminMemberPresenter presenter;
    private AdminMemberAdapter memberAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_member, container, false);
        initView(inflate);
        presenter = new AdminMemberPresenter(getContext(), this);
        presenter.register();
        presenter.queryMember();
        return inflate;
    }

    private void initView(View inflate) {
        rv_member = (RecyclerView) inflate.findViewById(R.id.rv_member);
        edt_name = (EditText) inflate.findViewById(R.id.edt_name);
        edt_unit = (EditText) inflate.findViewById(R.id.edt_unit);
        edt_position = (EditText) inflate.findViewById(R.id.edt_position);
        edt_remarks = (EditText) inflate.findViewById(R.id.edt_remarks);
        edt_phone = (EditText) inflate.findViewById(R.id.edt_phone);
        edt_email = (EditText) inflate.findViewById(R.id.edt_email);
        edt_pwd = (EditText) inflate.findViewById(R.id.edt_pwd);
        btn_increase = (Button) inflate.findViewById(R.id.btn_increase);
        btn_modify = (Button) inflate.findViewById(R.id.btn_modify);
        btn_delete = (Button) inflate.findViewById(R.id.btn_delete);
        btn_import = (Button) inflate.findViewById(R.id.btn_import);
        btn_export = (Button) inflate.findViewById(R.id.btn_export);

        btn_increase.setOnClickListener(this);
        btn_modify.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_import.setOnClickListener(this);
        btn_export.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_increase: {
                String name = edt_name.getText().toString().trim();
                String unit = edt_unit.getText().toString().trim();
                String position = edt_position.getText().toString().trim();
                String remarks = edt_remarks.getText().toString().trim();
                String phone = edt_phone.getText().toString().trim();
                String email = edt_email.getText().toString().trim();
                String pwd = edt_pwd.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    ToastUtil.show(R.string.please_enter_user_name);
                    return;
                }
                InterfacePerson.pbui_Item_PersonDetailInfo build = InterfacePerson.pbui_Item_PersonDetailInfo.newBuilder()
                        .setName(ConvertUtil.s2b(name))
                        .setCompany(ConvertUtil.s2b(unit))
                        .setJob(ConvertUtil.s2b(position))
                        .setComment(ConvertUtil.s2b(remarks))
                        .setPhone(ConvertUtil.s2b(phone))
                        .setEmail(ConvertUtil.s2b(email))
                        .setPassword(ConvertUtil.s2b(pwd)).build();
                presenter.addMember(build);
                break;
            }
            case R.id.btn_modify: {
                String name = edt_name.getText().toString().trim();
                String unit = edt_unit.getText().toString().trim();
                String position = edt_position.getText().toString().trim();
                String remarks = edt_remarks.getText().toString().trim();
                String phone = edt_phone.getText().toString().trim();
                String email = edt_email.getText().toString().trim();
                String pwd = edt_pwd.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    ToastUtil.show(R.string.please_enter_user_name);
                    return;
                }
                InterfacePerson.pbui_Item_PersonDetailInfo.Builder builder = InterfacePerson.pbui_Item_PersonDetailInfo.newBuilder()
                        .setName(ConvertUtil.s2b(name))
                        .setCompany(ConvertUtil.s2b(unit))
                        .setJob(ConvertUtil.s2b(position))
                        .setComment(ConvertUtil.s2b(remarks))
                        .setPhone(ConvertUtil.s2b(phone))
                        .setEmail(ConvertUtil.s2b(email))
                        .setPassword(ConvertUtil.s2b(pwd));
                presenter.modifyMember(builder);
                break;
            }
            case R.id.btn_delete:
                presenter.delMember();
                break;
            case R.id.btn_import:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/*;*.xls");
                String[] mimeTypes = {"application/vnd.ms-excel"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(intent, EXCEL_FILE_REQUEST_CODE);
                break;
            case R.id.btn_export:
                presenter.export();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXCEL_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String realPath = null;
            try {
                realPath = UriUtil.getFilePath(getContext(), uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LogUtil.e(TAG, "onActivityResult 获取的文件路径=" + realPath);
            if (realPath != null && realPath.endsWith(".xls")) {
                List<InterfacePerson.pbui_Item_PersonDetailInfo> memberXls = JxlUtil.readMemberXls(realPath);
                if (!memberXls.isEmpty()) {
                    presenter.addMembers(memberXls);
                } else {
                    LogUtil.e(TAG, "onActivityResult 读取表格结果为空");
                }
            } else {
                ToastUtil.show(R.string.get_file_path_fail);
            }
        }
    }

    @Override
    public void updateMemberRv(List<InterfacePerson.pbui_Item_PersonDetailInfo> memberInfos) {
        if (memberAdapter == null) {
            memberAdapter = new AdminMemberAdapter(R.layout.item_admin_member, memberInfos);
            rv_member.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_member.setAdapter(memberAdapter);
            memberAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    InterfacePerson.pbui_Item_PersonDetailInfo info = memberInfos.get(position);
                    int personid = info.getPersonid();
                    memberAdapter.setSelect(personid);
                    presenter.setSelectMember(personid);
                    edt_name.setText(info.getName().toStringUtf8());
                    edt_position.setText(info.getCompany().toStringUtf8());
                    edt_remarks.setText(info.getComment().toStringUtf8());
                    edt_phone.setText(info.getPhone().toStringUtf8());
                    edt_email.setText(info.getEmail().toStringUtf8());
                    edt_pwd.setText(info.getPassword().toStringUtf8());
                }
            });
        } else {
            memberAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unregister();
        presenter.onDestroy();
    }
}
