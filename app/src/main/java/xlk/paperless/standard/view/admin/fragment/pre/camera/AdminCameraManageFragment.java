package xlk.paperless.standard.view.admin.fragment.pre.camera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceVideo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.util.ToastUtil;

import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author Created by xlk on 2020/10/22.
 * @desc
 */
public class AdminCameraManageFragment extends BaseFragment implements AdminCameraManageInterface, View.OnClickListener {
    private RecyclerView rv_available_camera, rv_all_camera;
    private Button btn_add, btn_remove, btn_modify;
    private EditText edt_name;
    private AdminCameraManagePresenter presenter;
    private AdminCameraAdapter availableAdapter, allAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_admin_camera_manage, container, false);
        initView(inflate);
        presenter = new AdminCameraManagePresenter(this);
        presenter.queryMeetVideo();
        return inflate;
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
            presenter.queryMeetVideo();
        }
    }

    private void initView(View inflate) {
        rv_available_camera = inflate.findViewById(R.id.rv_available_camera);
        btn_add = inflate.findViewById(R.id.btn_add);
        btn_remove = inflate.findViewById(R.id.btn_remove);
        rv_all_camera = inflate.findViewById(R.id.rv_all_camera);
        edt_name = inflate.findViewById(R.id.edt_name);
        btn_modify = inflate.findViewById(R.id.btn_modify);
        btn_add.setOnClickListener(this);
        btn_remove.setOnClickListener(this);
        btn_modify.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add: {
                if (allAdapter.getSelected() != null) {
                    jni.addMeetVideo(allAdapter.getSelected());
                } else {
                    ToastUtil.show(R.string.please_choose_first);
                }
                break;
            }
            case R.id.btn_remove:
                if (availableAdapter.getSelected() != null) {
                    jni.deleteMeetVideo(availableAdapter.getSelected());
                } else {
                    ToastUtil.show(R.string.please_choose_first);
                }
                break;
            case R.id.btn_modify:
                String name = edt_name.getText().toString().trim();
                if (name.isEmpty()) {
                    ToastUtil.show(R.string.please_enter_name);
                    return;
                }
                if (availableAdapter.getSelected() != null) {
                    InterfaceVideo.pbui_Item_MeetVideoDetailInfo selected = availableAdapter.getSelected();
                    InterfaceVideo.pbui_Item_MeetVideoDetailInfo build = InterfaceVideo.pbui_Item_MeetVideoDetailInfo.newBuilder()
                            .setId(selected.getId())
                            .setAddr(selected.getAddr())
                            .setDeviceid(selected.getDeviceid())
                            .setDevicename(selected.getDevicename())
                            .setSubid(selected.getSubid())
                            .setName(s2b(name))
                            .build();
                    jni.modifyMeetVideo(build);
                } else {
                    ToastUtil.show(R.string.please_choose_first);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void updateAvailableCamera(List<DevCameraBean> availableCameras) {
        if (availableAdapter == null) {
            availableAdapter = new AdminCameraAdapter(R.layout.item_admin_camera, availableCameras);
            rv_available_camera.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_available_camera.setAdapter(availableAdapter);
            availableAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    availableAdapter.setSelected(availableCameras.get(position).getCamera().getId());
                    edt_name.setText(availableCameras.get(position).getCamera().getName().toStringUtf8());
                }
            });
        } else {
            availableAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateAllCamera(List<DevCameraBean> allCameras) {
        if (allAdapter == null) {
            allAdapter = new AdminCameraAdapter(R.layout.item_admin_camera, allCameras);
            rv_all_camera.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_all_camera.setAdapter(allAdapter);
            allAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    allAdapter.setSelected(allCameras.get(position).getCamera().getId());
                    edt_name.setText("");
                }
            });
        } else {
            allAdapter.notifyDataSetChanged();
        }
    }
}
