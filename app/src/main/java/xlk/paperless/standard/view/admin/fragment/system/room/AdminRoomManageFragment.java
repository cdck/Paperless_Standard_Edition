package xlk.paperless.standard.view.admin.fragment.system.room;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BaseFragment;

/**
 * @author Created by xlk on 2020/9/19.
 * @desc 系统设置-会议室管理
 */
public class AdminRoomManageFragment extends BaseFragment implements AdminRoomManageInterface, View.OnClickListener {
    private RecyclerView rv_room;
    private RecyclerView rv_room_dev;
    private Button btn_add_to;
    private Button btn_remove_to;
    private RecyclerView rv_all_dev;
    private EditText edt_name;
    private EditText edt_address;
    private EditText edt_remarks;
    private Button btn_add;
    private Button btn_delete;
    private Button btn_modify;
    private AdminRoomManagePresenter presenter;
    private AdminRoomAdapter roomAdapter;
    private AdminRoomDevAdapter adminRoomDevAdapter, allDevAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_room_manage, container, false);
        initView(inflate);
        presenter = new AdminRoomManagePresenter(getContext(),this);
        presenter.queryRoom();
        return inflate;
    }

    private void initView(View inflate) {
        rv_room = (RecyclerView) inflate.findViewById(R.id.rv_room);
        rv_room_dev = (RecyclerView) inflate.findViewById(R.id.rv_room_dev);
        btn_add_to = (Button) inflate.findViewById(R.id.btn_add_to);
        btn_remove_to = (Button) inflate.findViewById(R.id.btn_remove_to);
        rv_all_dev = (RecyclerView) inflate.findViewById(R.id.rv_all_dev);
        edt_name = (EditText) inflate.findViewById(R.id.edt_name);
        edt_address = (EditText) inflate.findViewById(R.id.edt_address);
        edt_remarks = (EditText) inflate.findViewById(R.id.edt_remarks);
        btn_add = (Button) inflate.findViewById(R.id.btn_add);
        btn_delete = (Button) inflate.findViewById(R.id.btn_delete);
        btn_modify = (Button) inflate.findViewById(R.id.btn_modify);

        btn_add_to.setOnClickListener(this);
        btn_remove_to.setOnClickListener(this);
        btn_add.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_modify.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_to:
                presenter.add();
                break;
            case R.id.btn_remove_to:
                presenter.remove();
                break;
            case R.id.btn_add: {
                String name = edt_name.getText().toString().trim();
                String address = edt_address.getText().toString().trim();
                String remarks = edt_remarks.getText().toString();
                if (name.isEmpty() || address.isEmpty()) {
                    ToastUtil.show(R.string.please_enter_name_address);
                    return;
                }
                presenter.addRoom(name, address, remarks);
                break;
            }
            case R.id.btn_delete:
                presenter.delRoom();
                break;
            case R.id.btn_modify: {
                String name = edt_name.getText().toString().trim();
                String address = edt_address.getText().toString().trim();
                String remarks = edt_remarks.getText().toString();
                if (name.isEmpty() || address.isEmpty()) {
                    ToastUtil.show(R.string.please_enter_name_address);
                    return;
                }
                presenter.modifyRoom(name, address, remarks);
                break;
            }
        }
    }

    @Override
    public void updateRoomRv(List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> roomInfos) {
        if (roomAdapter == null) {
            roomAdapter = new AdminRoomAdapter(R.layout.item_admin_room, roomInfos);
            rv_room.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_room.setAdapter(roomAdapter);
            roomAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    InterfaceRoom.pbui_Item_MeetRoomDetailInfo room = roomInfos.get(position);
                    int roomid = room.getRoomid();
                    roomAdapter.setSelected(roomid);
                    presenter.queryAllDevice(roomid);
                    edt_name.setText(room.getName().toStringUtf8());
                    edt_address.setText(room.getAddr().toStringUtf8());
                    edt_remarks.setText(room.getComment().toStringUtf8());
                }
            });
        } else {
            roomAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateRoomDeviceRv(List<InterfaceDevice.pbui_Item_DeviceDetailInfo> roomDevices) {
        if (adminRoomDevAdapter == null) {
            adminRoomDevAdapter = new AdminRoomDevAdapter(R.layout.item_table_3, roomDevices);
            rv_room_dev.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_room_dev.setAdapter(adminRoomDevAdapter);
            adminRoomDevAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    int devcieid = roomDevices.get(position).getDevcieid();
                    adminRoomDevAdapter.setSelected(devcieid);
                    presenter.setSelectedLeftDevId(devcieid);
                }
            });
        } else {
            adminRoomDevAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateAllDeviceRv(List<InterfaceDevice.pbui_Item_DeviceDetailInfo> allDevices) {
        if (allDevAdapter == null) {
            allDevAdapter = new AdminRoomDevAdapter(R.layout.item_table_3, allDevices);
            rv_all_dev.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_all_dev.setAdapter(allDevAdapter);
            allDevAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    int devcieid = allDevices.get(position).getDevcieid();
                    allDevAdapter.setSelected(devcieid);
                    presenter.setSelectedRightDevId(devcieid);
                }
            });
        } else {
            allDevAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
