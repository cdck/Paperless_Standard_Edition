package xlk.paperless.standard.view.admin.fragment.pre.member;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.UriUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMember;
import com.mogujie.tt.protobuf.InterfacePerson;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.ui.CustomBaseViewHolder;
import xlk.paperless.standard.util.JxlUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;

import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author Created by xlk on 2020/10/17.
 * @desc
 */
public class MemberFragment extends BaseFragment implements MemberInterface, View.OnClickListener {

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
    private Button btn_sort;
    private Button btn_member_permission;
    private Button btn_import;
    private Button btn_export;
    private Button btn_import_frequently;
    private Button btn_export_frequently;
    private Button btn_member_role;
    private MemberPresenter presenter;
    private AdminMemberAdapter memberAdapter;
    private PopupWindow sortPop, permissionPop, frequentlyPop, memberRolePop;
    private AdminMemberAdapter popMemberAdapter;
    private RecyclerView rv_member_sort;
    private MemberPermissionAdapter permissionAdapter;
    private RecyclerView rv_member_permission;
    private RecyclerView rv_pop_frequently;
    private FrequentlyMemberAdapter frequentlyMemberAdapter;
    private MemberRoleAdapter memberRoleAdapter;
    private RecyclerView rv_member_role;
    private final int REQUEST_CODE_MEMBER_XLS = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_member, container, false);
        initView(inflate);
        presenter = new MemberPresenter(getContext(), this);
        presenter.queryAttendPeople();
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        dismissPopupWindow();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            presenter.queryAttendPeople();
        }
    }

    private void dismissPopupWindow() {
        if (sortPop != null && sortPop.isShowing()) {
            sortPop.dismiss();
        }
        if (permissionPop != null && permissionPop.isShowing()) {
            permissionPop.dismiss();
        }
        if (frequentlyPop != null && frequentlyPop.isShowing()) {
            frequentlyPop.dismiss();
        }
        if (memberRolePop != null && memberRolePop.isShowing()) {
            memberRolePop.dismiss();
        }
    }

    @Override
    public void updateMemberRv(List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos) {
        if (memberAdapter == null) {
            memberAdapter = new AdminMemberAdapter(R.layout.item_admin_member, memberInfos, false);
            rv_member.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_member.setAdapter(memberAdapter);
            memberAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    memberAdapter.setSelected(memberInfos.get(position).getPersonid());
                    updateUI(memberInfos.get(position));
                }
            });
        } else {
            memberAdapter.notifyDataSetChanged();
        }
    }

    private void updateUI(InterfaceMember.pbui_Item_MemberDetailInfo info) {
        edt_name.setText(info.getName().toStringUtf8());
        edt_unit.setText(info.getCompany().toStringUtf8());
        edt_position.setText(info.getJob().toStringUtf8());
        edt_remarks.setText(info.getComment().toStringUtf8());
        edt_phone.setText(info.getPhone().toStringUtf8());
        edt_email.setText(info.getEmail().toStringUtf8());
        edt_pwd.setText(info.getPassword().toStringUtf8());
    }

    public void initView(View rootView) {
        this.rv_member = (RecyclerView) rootView.findViewById(R.id.rv_member);
        this.edt_name = (EditText) rootView.findViewById(R.id.edt_name);
        this.edt_unit = (EditText) rootView.findViewById(R.id.edt_unit);
        this.edt_position = (EditText) rootView.findViewById(R.id.edt_position);
        this.edt_remarks = (EditText) rootView.findViewById(R.id.edt_remarks);
        this.edt_phone = (EditText) rootView.findViewById(R.id.edt_phone);
        this.edt_email = (EditText) rootView.findViewById(R.id.edt_email);
        this.edt_pwd = (EditText) rootView.findViewById(R.id.edt_pwd);
        this.btn_increase = (Button) rootView.findViewById(R.id.btn_increase);
        this.btn_modify = (Button) rootView.findViewById(R.id.btn_modify);
        this.btn_delete = (Button) rootView.findViewById(R.id.btn_delete);
        this.btn_sort = (Button) rootView.findViewById(R.id.btn_sort);
        this.btn_member_permission = (Button) rootView.findViewById(R.id.btn_member_permission);
        this.btn_import = (Button) rootView.findViewById(R.id.btn_import);
        this.btn_export = (Button) rootView.findViewById(R.id.btn_export);
        this.btn_import_frequently = (Button) rootView.findViewById(R.id.btn_import_frequently);
        this.btn_export_frequently = (Button) rootView.findViewById(R.id.btn_export_frequently);
        this.btn_member_role = (Button) rootView.findViewById(R.id.btn_member_role);
        btn_increase.setOnClickListener(this);
        btn_modify.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_sort.setOnClickListener(this);
        btn_member_permission.setOnClickListener(this);
        btn_import.setOnClickListener(this);
        btn_export.setOnClickListener(this);
        btn_import_frequently.setOnClickListener(this);
        btn_export_frequently.setOnClickListener(this);
        btn_member_role.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_increase: {
                createMember();
                break;
            }
            case R.id.btn_modify: {
                modifyMember();
                break;
            }
            case R.id.btn_delete: {
                delMember();
                break;
            }
            case R.id.btn_sort: {
                showSort(presenter.getMembers());
                break;
            }
            case R.id.btn_member_permission: {
                showMemberPermission(presenter.getMemberPermissions());
                break;
            }
            case R.id.btn_import: {
                chooseLocalFile(REQUEST_CODE_MEMBER_XLS);
                break;
            }
            case R.id.btn_export: {
                if (JxlUtil.exportMemberInfo(presenter.getDevSeatInfos())) {
                    ToastUtil.show(R.string.export_successful);
                } else {
                    ToastUtil.show(R.string.export_failure);
                }
                break;
            }
            case R.id.btn_import_frequently: {
                presenter.queryFrequentlyMember();
                showFrequently(presenter.getFrequentlyMembers());
                break;
            }
            //导出到常用人员
            case R.id.btn_export_frequently: {
                createFrequentlyMember();
                break;
            }
            //参会人角色
            case R.id.btn_member_role: {
                showMemberRole(presenter.getDevSeatInfos());
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_MEMBER_XLS) {
            File file = UriUtils.uri2File(data.getData());
            if (file != null && file.isFile()) {
                if (file.getName().endsWith(".xls")) {
                    List<InterfaceMember.pbui_Item_MemberDetailInfo> members = JxlUtil.readMemberInfo(file.getAbsolutePath());
                    jni.createMultipleMember(members);
                }
            }
        }
    }

    private void showMemberRole(List<MemberRoleBean> devSeatInfos) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_member_role, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showMemberRole fragment的大小 width=" + width + ",height=" + height);
        memberRolePop = new PopupWindow(inflate, width, height);
        memberRolePop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        memberRolePop.setTouchable(true);
        // true:设置触摸外面时消失
        memberRolePop.setOutsideTouchable(true);
        memberRolePop.setFocusable(true);
        memberRolePop.setAnimationStyle(R.style.pop_Animation);
        memberRolePop.showAtLocation(btn_increase, Gravity.END | Gravity.BOTTOM, 0, 0);
        rv_member_role = inflate.findViewById(R.id.rv_member_role);
        Spinner sp_role = inflate.findViewById(R.id.sp_role);
        memberRoleAdapter = new MemberRoleAdapter(R.layout.item_member_role, devSeatInfos);
        rv_member_role.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_member_role.setAdapter(memberRoleAdapter);
        memberRoleAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                MemberRoleBean item = devSeatInfos.get(position);
                memberRoleAdapter.setSelected(item.getMember().getPersonid());
                int index;
                int role = item.getSeat() != null ? item.getSeat().getRole() : 0;
                switch (role) {
                    case InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal_VALUE:
                        index = 1;
                        break;
                    case InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere_VALUE:
                        index = 2;
                        break;
                    case InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE:
                        index = 3;
                        break;
                    case InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin_VALUE:
                        index = 4;
                        break;
                    default:
                        index = 0;
                        break;
                }
                sp_role.setSelection(index);
            }
        });
        inflate.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            MemberRoleBean selected = memberRoleAdapter.getSelected();
            if (selected == null) {
                ToastUtil.show(R.string.please_choose_member);
                return;
            }
            if (selected.getSeat() == null) {
                ToastUtil.show(R.string.please_choose_bind_member);
                return;
            }
            int index = sp_role.getSelectedItemPosition();
            int newRole;
            switch (index) {
                case 1:
                    newRole = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal_VALUE;
                    break;
                case 2:
                    newRole = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_compere_VALUE;
                    break;
                case 3:
                    newRole = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_secretary_VALUE;
                    break;
                case 4:
                    newRole = InterfaceMacro.Pb_MeetMemberRole.Pb_role_admin_VALUE;
                    break;
                default:
                    newRole = InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_nouser_VALUE;
                    break;
            }
            presenter.modifyMemberRole(selected.getMember().getPersonid(), newRole, selected.getSeat().getDevid());
        });
        inflate.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            memberRolePop.dismiss();
        });
    }

    @Override
    public void updateMemberRole() {
        if (memberRolePop != null && memberRolePop.isShowing()) {
            LogUtil.i(TAG, "updateMemberRole ");
            memberRoleAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 展示常用人员PopupWindow
     *
     * @param frequentlyMembers 常用人员信息
     */
    private void showFrequently(List<InterfacePerson.pbui_Item_PersonDetailInfo> frequentlyMembers) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_frequently_member, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showFrequently fragment的大小 width=" + width + ",height=" + height);
        frequentlyPop = new PopupWindow(inflate, width, height);
        frequentlyPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        frequentlyPop.setTouchable(true);
        // true:设置触摸外面时消失
        frequentlyPop.setOutsideTouchable(true);
        frequentlyPop.setFocusable(true);
        frequentlyPop.setAnimationStyle(R.style.pop_Animation);
        frequentlyPop.showAtLocation(btn_increase, Gravity.END | Gravity.BOTTOM, 0, 0);
        rv_pop_frequently = inflate.findViewById(R.id.rv_pop_frequently);

        frequentlyMemberAdapter = new FrequentlyMemberAdapter(R.layout.item_admin_member, frequentlyMembers);
        rv_pop_frequently.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_pop_frequently.setAdapter(frequentlyMemberAdapter);
        frequentlyMemberAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                frequentlyMemberAdapter.setCheck(frequentlyMembers.get(position).getPersonid());
            }
        });
        inflate.findViewById(R.id.btn_add).setOnClickListener(v -> {
            List<InterfaceMember.pbui_Item_MemberDetailInfo> checkedMembers = frequentlyMemberAdapter.getCheckedMembers();
            if (checkedMembers.isEmpty()) {
                ToastUtil.show(R.string.please_choose_member);
                return;
            }
            presenter.createMultipleMember(checkedMembers);
            frequentlyPop.dismiss();
        });
        inflate.findViewById(R.id.btn_back).setOnClickListener(v -> {
            frequentlyPop.dismiss();
        });
    }

    @Override
    public void updateFrequentlyMemberRv() {
        if (frequentlyPop != null && frequentlyPop.isShowing()) {
            LogUtil.i(TAG, "updateFrequentlyMemberRv ");
            frequentlyMemberAdapter.notifyDataSetChanged();
        }
    }

    private void showMemberPermission(List<MemberPermissionBean> memberPermissions) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_member_permission, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showMemberPermission fragment的大小 width=" + width + ",height=" + height);
        permissionPop = new PopupWindow(inflate, width, height);
        permissionPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        permissionPop.setTouchable(true);
        // true:设置触摸外面时消失
        permissionPop.setOutsideTouchable(true);
        permissionPop.setFocusable(true);
        permissionPop.setAnimationStyle(R.style.pop_Animation);
        permissionPop.showAtLocation(btn_increase, Gravity.END | Gravity.BOTTOM, 0, 0);
        CustomBaseViewHolder.PermissionViewHolder holder = new CustomBaseViewHolder.PermissionViewHolder(inflate);
        permissionHolderEvent(holder, memberPermissions);
    }

    private void permissionHolderEvent(CustomBaseViewHolder.PermissionViewHolder holder, List<MemberPermissionBean> memberPermissions) {
        holder.rv_member_permission.setLayoutManager(new LinearLayoutManager(getContext()));
        permissionAdapter = new MemberPermissionAdapter(R.layout.item_member_permission, memberPermissions);
        rv_member_permission = holder.rv_member_permission;
        rv_member_permission.setAdapter(permissionAdapter);
        holder.item_tv_1.setOnClickListener(v -> {
            holder.item_tv_1.setChecked(holder.item_tv_1.isChecked());
            permissionAdapter.setCheckAll(holder.item_tv_1.isChecked());
        });
        permissionAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                permissionAdapter.setSelected(memberPermissions.get(position).getMemberId());
                holder.item_tv_1.setChecked(permissionAdapter.isCheckAll());
            }
        });
        holder.btn_save.setOnClickListener(v -> {
            presenter.savePermission();
            permissionPop.dismiss();
        });
        holder.btn_back.setOnClickListener(v -> {
            permissionPop.dismiss();
        });
        holder.btn_add_screen.setOnClickListener(v -> {
            permissionAdapter.addPermission(Constant.permission_code_screen);
        });
        holder.btn_add_projection.setOnClickListener(v -> {
            permissionAdapter.addPermission(Constant.permission_code_projection);
        });
        holder.btn_add_upload.setOnClickListener(v -> {
            permissionAdapter.addPermission(Constant.permission_code_upload);
        });
        holder.btn_add_download.setOnClickListener(v -> {
            permissionAdapter.addPermission(Constant.permission_code_download);
        });
        holder.btn_add_vote.setOnClickListener(v -> {
            permissionAdapter.addPermission(Constant.permission_code_vote);
        });
        holder.btn_del_screen.setOnClickListener(v -> {
            permissionAdapter.delPermission(Constant.permission_code_screen);
        });
        holder.btn_del_projection.setOnClickListener(v -> {
            permissionAdapter.delPermission(Constant.permission_code_projection);
        });
        holder.btn_del_upload.setOnClickListener(v -> {
            permissionAdapter.delPermission(Constant.permission_code_upload);
        });
        holder.btn_del_download.setOnClickListener(v -> {
            permissionAdapter.delPermission(Constant.permission_code_download);
        });
        holder.btn_del_vote.setOnClickListener(v -> {
            permissionAdapter.delPermission(Constant.permission_code_vote);
        });
    }

    private void showSort(List<InterfaceMember.pbui_Item_MemberDetailInfo> memberInfos) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_sort_member, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showSort fragment的大小 width=" + width + ",height=" + height);
        sortPop = new PopupWindow(inflate, width, height);
        sortPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        sortPop.setTouchable(true);
        // true:设置触摸外面时消失
        sortPop.setOutsideTouchable(true);
        sortPop.setFocusable(true);
        sortPop.setAnimationStyle(R.style.pop_Animation);
        sortPop.showAtLocation(btn_increase, Gravity.END | Gravity.BOTTOM, 0, 0);
        rv_member_sort = inflate.findViewById(R.id.rv_member_sort);
        popMemberAdapter = new AdminMemberAdapter(R.layout.item_admin_member, memberInfos, false);
        rv_member_sort.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_member_sort.setAdapter(popMemberAdapter);
        popMemberAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                popMemberAdapter.setSelected(memberInfos.get(position).getPersonid());
            }
        });
        inflate.findViewById(R.id.btn_move_up).setOnClickListener(v -> {
            InterfaceMember.pbui_Item_MemberDetailInfo selectedMember = popMemberAdapter.getSelectedMember();
            if (selectedMember == null) {
                ToastUtil.show(R.string.please_choose_member);
                return;
            }
            int index = 0;
            for (int i = 0; i < memberInfos.size(); i++) {
                if (selectedMember.getPersonid() == memberInfos.get(i).getPersonid()) {
                    index = i;
                    break;
                }
            }
            if (index == 0) {
                //要上移的目标已经是第一项，则移动到最下方
                memberInfos.remove(index);
                memberInfos.add(selectedMember);
            } else {
                Collections.swap(memberInfos, index, index - 1);
            }
            popMemberAdapter.notifyDataSetChanged();
        });
        inflate.findViewById(R.id.btn_move_down).setOnClickListener(v -> {
            InterfaceMember.pbui_Item_MemberDetailInfo selectedMember = popMemberAdapter.getSelectedMember();
            if (selectedMember == null) {
                ToastUtil.show(R.string.please_choose_member);
                return;
            }
            int index = 0;
            for (int i = 0; i < memberInfos.size(); i++) {
                if (selectedMember.getPersonid() == memberInfos.get(i).getPersonid()) {
                    index = i;
                    break;
                }
            }
            if (index == memberInfos.size() - 1) {
                //要下移的目标已经是最后一项，则进行移动到最上方
                List<InterfaceMember.pbui_Item_MemberDetailInfo> temps = new ArrayList<>();
                memberInfos.remove(index);
                temps.add(selectedMember);
                temps.addAll(memberInfos);
                memberInfos.clear();
                memberInfos.addAll(temps);
                temps.clear();
            } else {
                Collections.swap(memberInfos, index, index + 1);
            }
            popMemberAdapter.notifyDataSetChanged();
        });
        inflate.findViewById(R.id.btn_save).setOnClickListener(v -> {
            List<Integer> ids = new ArrayList<>();
            for (int i = 0; i < memberInfos.size(); i++) {
                ids.add(memberInfos.get(i).getPersonid());
            }
            presenter.modifyMemberSort(ids);
            sortPop.dismiss();
        });
        inflate.findViewById(R.id.btn_close).setOnClickListener(v -> {
            sortPop.dismiss();
        });
    }

    private void delMember() {
        if (memberAdapter == null) {
            return;
        }
        InterfaceMember.pbui_Item_MemberDetailInfo selectedMember = memberAdapter.getSelectedMember();
        if (selectedMember == null) {
            ToastUtil.show(R.string.please_choose_member);
            return;
        }
        presenter.delMember(selectedMember);
    }

    private void modifyMember() {
        if (memberAdapter == null) {
            return;
        }
        InterfaceMember.pbui_Item_MemberDetailInfo selectedMember = memberAdapter.getSelectedMember();
        if (selectedMember == null) {
            ToastUtil.show(R.string.please_choose_member);
            return;
        }
        String name = edt_name.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtil.show(R.string.err_name_empty);
            return;
        }
        String email = edt_email.getText().toString();
        if (!RegexUtils.isEmail(email)) {
            ToastUtil.show(R.string.email_format_error);
            return;
        }
        String phone = edt_phone.getText().toString();
        if (!RegexUtils.isMobileSimple(phone)) {
            ToastUtil.show(R.string.phone_format_error);
            return;
        }
        InterfaceMember.pbui_Item_MemberDetailInfo build = InterfaceMember.pbui_Item_MemberDetailInfo.newBuilder()
                .setPersonid(selectedMember.getPersonid())
                .setName(s2b(name))
                .setCompany(s2b(edt_unit.getText().toString()))
                .setJob(s2b(edt_position.getText().toString()))
                .setComment(s2b(edt_remarks.getText().toString()))
                .setPhone(s2b(phone))
                .setEmail(s2b(email))
                .setPassword(s2b(edt_pwd.getText().toString()))
                .build();
        presenter.modifyMember(build);
    }

    private void createMember() {
        String name = edt_name.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtil.show(R.string.err_name_empty);
            return;
        }
        String email = edt_email.getText().toString();
        if (!RegexUtils.isEmail(email)) {
            ToastUtil.show(R.string.email_format_error);
            return;
        }
        String phone = edt_phone.getText().toString();
        if (!RegexUtils.isMobileSimple(phone)) {
            ToastUtil.show(R.string.phone_format_error);
            return;
        }
        InterfaceMember.pbui_Item_MemberDetailInfo build = InterfaceMember.pbui_Item_MemberDetailInfo.newBuilder()
                .setName(s2b(name))
                .setCompany(s2b(edt_unit.getText().toString()))
                .setJob(s2b(edt_position.getText().toString()))
                .setComment(s2b(edt_remarks.getText().toString()))
                .setPhone(s2b(phone))
                .setEmail(s2b(email))
                .setPassword(s2b(edt_pwd.getText().toString()))
                .build();
        presenter.createMember(build);
    }

    private void createFrequentlyMember() {
        String name = edt_name.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtil.show(R.string.err_name_empty);
            return;
        }

        String email = edt_email.getText().toString();
        if (!RegexUtils.isEmail(email)) {
            ToastUtil.show(R.string.email_format_error);
            return;
        }
        String phone = edt_phone.getText().toString();
        if (!RegexUtils.isMobileSimple(phone)) {
            ToastUtil.show(R.string.phone_format_error);
            return;
        }
        InterfacePerson.pbui_Item_PersonDetailInfo build = InterfacePerson.pbui_Item_PersonDetailInfo.newBuilder()
                .setName(s2b(name))
                .setCompany(s2b(edt_unit.getText().toString()))
                .setJob(s2b(edt_position.getText().toString()))
                .setComment(s2b(edt_remarks.getText().toString()))
                .setPhone(s2b(phone))
                .setEmail(s2b(email))
                .setPassword(s2b(edt_pwd.getText().toString()))
                .build();
        presenter.createFrequentlyMember(build);
    }
}
