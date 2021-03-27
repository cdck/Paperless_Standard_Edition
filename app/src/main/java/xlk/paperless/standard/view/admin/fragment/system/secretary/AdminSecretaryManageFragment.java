package xlk.paperless.standard.view.admin.fragment.system.secretary;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceAdmin;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BaseFragment;

/**
 * @author Created by xlk on 2020/9/21.
 * @desc
 */
public class AdminSecretaryManageFragment extends BaseFragment implements AdminSecretaryManageInterface, View.OnClickListener {

    private AdminSecretaryManagePresenter presenter;
    private RecyclerView rv_admin_user;
    private RecyclerView rv_controllable_venue;
    private Button btn_add_to;
    private Button btn_remove_to;
    private Button btn_save;
    private RecyclerView rv_all_venues;
    private EditText edt_user_name;
    private EditText edt_password;
    private EditText edt_remarks;
    private EditText edt_phone;
    private EditText edt_email;
    private Button btn_create;
    private Button btn_delete;
    private Button btn_modify;
    private SecretaryUserAdapter userAdapter;
    private RoomAdapter controllableRoomAdapter, allRoomAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_secretary_manage, container, false);
        initView(inflate);
        presenter = new AdminSecretaryManagePresenter(getContext(), this);
        presenter.queryAdmin();
        return inflate;
    }

    private void initView(View inflate) {
        rv_admin_user = (RecyclerView) inflate.findViewById(R.id.rv_admin_user);
        rv_controllable_venue = (RecyclerView) inflate.findViewById(R.id.rv_controllable_venue);
        btn_add_to = (Button) inflate.findViewById(R.id.btn_add_to);
        btn_remove_to = (Button) inflate.findViewById(R.id.btn_remove_to);
        btn_save = (Button) inflate.findViewById(R.id.btn_save);
        rv_all_venues = (RecyclerView) inflate.findViewById(R.id.rv_all_venues);
        edt_user_name = (EditText) inflate.findViewById(R.id.edt_user_name);
        edt_password = (EditText) inflate.findViewById(R.id.edt_password);
        edt_remarks = (EditText) inflate.findViewById(R.id.edt_remarks);
        edt_phone = (EditText) inflate.findViewById(R.id.edt_phone);
        edt_email = (EditText) inflate.findViewById(R.id.edt_email);
        btn_create = (Button) inflate.findViewById(R.id.btn_create);
        btn_delete = (Button) inflate.findViewById(R.id.btn_delete);
        btn_modify = (Button) inflate.findViewById(R.id.btn_modify);

        btn_add_to.setOnClickListener(this);
        btn_remove_to.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        btn_create.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_modify.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_to:
                presenter.addRoom();
                break;
            case R.id.btn_remove_to:
                presenter.removeRoom();
                break;
            case R.id.btn_save:

                break;
            case R.id.btn_create: {
                String name = edt_user_name.getText().toString().trim();
                String pwd = edt_password.getText().toString().trim();
                String remarks = edt_remarks.getText().toString();
                String phone = edt_phone.getText().toString().trim();
                String email = edt_email.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    ToastUtil.show(R.string.please_enter_user_name);
                    return;
                }
                if (TextUtils.isEmpty(pwd)) {
                    ToastUtil.show(R.string.password_can_not_blank);
                    return;
                }
                if (presenter.isRepeat(name)) {
                    ToastUtil.show(R.string.err_repeated);
                    return;
                }
                InterfaceAdmin.pbui_Item_AdminDetailInfo build = InterfaceAdmin.pbui_Item_AdminDetailInfo.newBuilder()
                        .setAdminname(ConvertUtil.s2b(name))
                        .setPw(ConvertUtil.s2b(pwd))
                        .setComment(ConvertUtil.s2b(remarks))
                        .setPhone(ConvertUtil.s2b(phone))
                        .setEmail(ConvertUtil.s2b(email)).build();
                presenter.createAdmin(build);
                break;
            }
            case R.id.btn_delete: {
                presenter.delAdmin();
                break;
            }
            case R.id.btn_modify: {
                String name = edt_user_name.getText().toString().trim();
                String pwd = edt_password.getText().toString().trim();
                String remarks = edt_remarks.getText().toString();
                String phone = edt_phone.getText().toString().trim();
                String email = edt_email.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    ToastUtil.show(R.string.please_enter_user_name);
                    return;
                }
                if (TextUtils.isEmpty(pwd)) {
                    ToastUtil.show(R.string.password_can_not_blank);
                    return;
                }
                presenter.modifyAdmin(name, pwd, remarks, phone, email);
                break;
            }
        }
    }

    @Override
    public void updateAdminRv(List<InterfaceAdmin.pbui_Item_AdminDetailInfo> adminInfos) {
        if (userAdapter == null) {
            userAdapter = new SecretaryUserAdapter(R.layout.item_secretary_manage_title, adminInfos);
            rv_admin_user.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_admin_user.setAdapter(userAdapter);
            userAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    InterfaceAdmin.pbui_Item_AdminDetailInfo info = adminInfos.get(position);
                    int adminid = info.getAdminid();
                    userAdapter.setSelect(adminid);
                    presenter.queryAllRooms(adminid);
                    edt_user_name.setText(info.getAdminname().toStringUtf8());
                    edt_password.setText(info.getPw().toStringUtf8());
                    edt_remarks.setText(info.getComment().toStringUtf8());
                    edt_phone.setText(info.getPhone().toStringUtf8());
                    edt_email.setText(info.getEmail().toStringUtf8());
                }
            });
        } else {
            userAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateControllableRoomsRv(List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> controllableRooms) {
        if (controllableRoomAdapter == null) {
            controllableRoomAdapter = new RoomAdapter(R.layout.item_table_3, controllableRooms);
            rv_controllable_venue.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_controllable_venue.setAdapter(controllableRoomAdapter);
            controllableRoomAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    int roomid = controllableRooms.get(position).getRoomid();
                    controllableRoomAdapter.setSelect(roomid);
                    presenter.setControllableRoomId(roomid);
                }
            });
        } else {
            controllableRoomAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateAllRoomsRv(List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> allRooms) {
        if (allRoomAdapter == null) {
            allRoomAdapter = new RoomAdapter(R.layout.item_table_3, allRooms);
            rv_all_venues.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_all_venues.setAdapter(allRoomAdapter);
            allRoomAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    int roomid = allRooms.get(position).getRoomid();
                    allRoomAdapter.setSelect(roomid);
                    presenter.setAllRoomId(roomid);
                }
            });
        } else {
            allRoomAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            presenter.queryAdmin();
        }
    }
}
