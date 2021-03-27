package xlk.paperless.standard.view.admin.fragment.reserve.task;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceTask;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;

import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author Created by xlk on 2020/11/13.
 * @desc
 */
public class TaskManagerFragment extends BaseFragment implements TaskManagerInterface, View.OnClickListener {

    private TaskManagerPresenter presenter;
    private RecyclerView rv_task;
    private CheckBox cb_multiple_choice;
    private Button btn_refresh_task;
    private Button btn_delete_task;
    private RecyclerView rv_media;
    private Button btn_add_media;
    private Button btn_remove_media;
    private RecyclerView rv_device;
    private Button btn_add_device;
    private Button btn_remove_device;
    private EditText edt_task_name;
    private CheckBox cb_shuffle_playback;
    private CheckBox cb_play_order;
    private Button btn_add_task;
    private Button btn_modify_task;
    private Button btn_start_task;
    private Button btn_stop_task;
    private TaskAdapter taskAdapter;
    private List<InterfaceTask.pbui_Item_MediaTaskDetailInfo> currentMedias = new ArrayList<>();
    private List<InterfaceTask.pbui_Item_DeviceTaskDetailInfo> currentDevices = new ArrayList<>();
    private MediaAdapter mediaAdapter;
    private DeviceAdapter deviceAdapter;
    private PopupWindow mediaPop;
    private RecyclerView rv_pop_media;
    private PopMediaAdapter popMediaAdapter;
    private RecyclerView rv_pop_device;
    private PopDeviceAdapter popDeviceAdapter;
    private PopupWindow devicePop;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_task_manager, container, false);
        initView(inflate);
        presenter = new TaskManagerPresenter(this);
        reShow();
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    protected void reShow() {
        presenter.queryTask();
    }

    @Override
    public void updateTask() {
        if (taskAdapter == null) {
            taskAdapter = new TaskAdapter(R.layout.item_task, presenter.tasks);
            rv_task.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_task.setAdapter(taskAdapter);
            taskAdapter.setOnItemClickListener((adapter, view, position) -> {
                int taskid = presenter.tasks.get(position).getTaskid();
                taskAdapter.setSelect(taskid);
                presenter.queryTaskDetail(taskid);
            });
        } else {
            taskAdapter.notifyDataSetChanged();
            if (taskAdapter.getSelectId() != -1) {
                presenter.queryTaskDetail(taskAdapter.getSelectId());
            }
        }
    }

    @Override
    public void updateUI(InterfaceTask.pbui_Item_MeetTaskDetailInfo taskInfo) {
        if (taskInfo == null) {
            currentMedias.clear();
            if (mediaAdapter != null) {
                mediaAdapter.notifyDataSetChanged();
            }
            currentDevices.clear();
            if (deviceAdapter != null) {
                deviceAdapter.notifyDataSetChanged();
            }
            return;
        }
        int playmode = taskInfo.getPlaymode();
        boolean shuffle = playmode == 1;
        cb_shuffle_playback.setChecked(shuffle);
        cb_play_order.setChecked(!shuffle);
        edt_task_name.setText(taskInfo.getTaskname().toStringUtf8());
        currentMedias.clear();
        currentMedias.addAll(taskInfo.getMediaidsList());
        currentDevices.clear();
        currentDevices.addAll(taskInfo.getDeviceidsList());
        LogUtil.i(TAG, "updateUI currentMedias=" + currentMedias.size()+", currentDevices="+currentDevices.size());
        if (mediaAdapter == null) {
            rv_media.setLayoutManager(new LinearLayoutManager(getContext()));
            mediaAdapter = new MediaAdapter(R.layout.item_media_name, currentMedias);
            rv_media.setAdapter(mediaAdapter);
            mediaAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    mediaAdapter.setSelect(currentMedias.get(position).getMediaid());
                }
            });
        } else {
            mediaAdapter.notifyDataSetChanged();
        }

        if (deviceAdapter == null) {
            rv_device.setLayoutManager(new LinearLayoutManager(getContext()));
            deviceAdapter = new DeviceAdapter(R.layout.item_device_name, currentDevices);
            rv_device.setAdapter(deviceAdapter);
            deviceAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    deviceAdapter.setSelect(currentDevices.get(position).getDeviceid());
                }
            });
        } else {
            deviceAdapter.notifyDataSetChanged();
        }
    }

    public void initView(View rootView) {
        this.rv_task = (RecyclerView) rootView.findViewById(R.id.rv_task);
        this.cb_multiple_choice = (CheckBox) rootView.findViewById(R.id.cb_multiple_choice);
        this.btn_refresh_task = (Button) rootView.findViewById(R.id.btn_refresh_task);
        this.btn_delete_task = (Button) rootView.findViewById(R.id.btn_delete_task);
        this.rv_media = (RecyclerView) rootView.findViewById(R.id.rv_media);
        this.btn_add_media = (Button) rootView.findViewById(R.id.btn_add_media);
        this.btn_remove_media = (Button) rootView.findViewById(R.id.btn_remove_media);
        this.rv_device = (RecyclerView) rootView.findViewById(R.id.rv_device);
        this.btn_add_device = (Button) rootView.findViewById(R.id.btn_add_device);
        this.btn_remove_device = (Button) rootView.findViewById(R.id.btn_remove_device);
        this.edt_task_name = (EditText) rootView.findViewById(R.id.edt_task_name);
        this.cb_shuffle_playback = (CheckBox) rootView.findViewById(R.id.cb_shuffle_playback);
        this.cb_play_order = (CheckBox) rootView.findViewById(R.id.cb_play_order);
        this.btn_add_task = (Button) rootView.findViewById(R.id.btn_add_task);
        this.btn_modify_task = (Button) rootView.findViewById(R.id.btn_modify_task);
        this.btn_start_task = (Button) rootView.findViewById(R.id.btn_start_task);
        this.btn_stop_task = (Button) rootView.findViewById(R.id.btn_stop_task);
        cb_multiple_choice.setOnClickListener(this);
        btn_refresh_task.setOnClickListener(this);
        btn_delete_task.setOnClickListener(this);
        btn_add_media.setOnClickListener(this);
        btn_remove_media.setOnClickListener(this);
        btn_add_device.setOnClickListener(this);
        btn_remove_device.setOnClickListener(this);
        cb_shuffle_playback.setOnClickListener(this);
        cb_play_order.setOnClickListener(this);
        btn_add_task.setOnClickListener(this);
        btn_modify_task.setOnClickListener(this);
        btn_start_task.setOnClickListener(this);
        btn_stop_task.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //多选
            case R.id.cb_multiple_choice: {
                break;
            }
            //刷新任务
            case R.id.btn_refresh_task: {
                presenter.queryTask();
                break;
            }
            //删除任务
            case R.id.btn_delete_task: {
                if (taskAdapter != null) {
                    int selectId = taskAdapter.getSelectId();
                    if (selectId != -1) {
                        jni.delTask(selectId);
                        reShow();
                    } else {
                        ToastUtil.show(R.string.please_choose_task_first);
                    }
                }
                break;
            }
            //添加媒体
            case R.id.btn_add_media: {
                presenter.queryReleaseFile();
                showMediaPop();
                break;
            }
            //移除设备
            case R.id.btn_remove_media: {
                if (mediaAdapter != null) {
                    InterfaceTask.pbui_Item_MediaTaskDetailInfo selected = mediaAdapter.getSelected();
                    if (selected != null) {
                        currentMedias.remove(selected);
                        mediaAdapter.notifyDataSetChanged();
                    } else {
                        ToastUtil.show(R.string.please_choose_media);
                    }
                }
                break;
            }
            //添加设备
            case R.id.btn_add_device: {
                presenter.queryDevice();
                showDevicePop();
                break;
            }
            //移除设备
            case R.id.btn_remove_device: {
                if (deviceAdapter != null) {
                    InterfaceTask.pbui_Item_DeviceTaskDetailInfo selected = deviceAdapter.getSelected();
                    if (selected != null) {
                        currentDevices.remove(selected);
                        deviceAdapter.notifyDataSetChanged();
                    } else {
                        ToastUtil.show(R.string.please_choose_device_first);
                    }
                }
                break;
            }
            //随机播放
            case R.id.cb_shuffle_playback: {
                boolean checked = cb_shuffle_playback.isChecked();
                cb_shuffle_playback.setChecked(checked);
                cb_play_order.setChecked(!checked);
                break;
            }
            //顺序播放
            case R.id.cb_play_order: {
                boolean checked = cb_play_order.isChecked();
                cb_play_order.setChecked(checked);
                cb_shuffle_playback.setChecked(!checked);
                break;
            }
            //添加任务
            case R.id.btn_add_task: {
                String taskName = edt_task_name.getText().toString().trim();
                if (taskName.isEmpty() || taskName.length() > InterfaceMacro.Pb_String_LenLimit.Pb_MEET_MAX_TASKNAMELEN_VALUE) {
                    ToastUtil.show(R.string.task_name_err);
                    break;
                }
                boolean checked = cb_shuffle_playback.isChecked();
                LogUtil.i(TAG, "onClick 添加 媒体和设备个数=" + currentMedias.size() + ", " + currentDevices.size());
                InterfaceTask.pbui_Item_MeetTaskDetailInfo build = InterfaceTask.pbui_Item_MeetTaskDetailInfo.newBuilder()
                        .setTaskname(s2b(taskName))
                        .setPlaymode(checked ? 1 : 2)
                        .addAllMediaids(currentMedias)
                        .addAllDeviceids(currentDevices)
                        .build();
                jni.addTask(build);
                reShow();
                break;
            }
            //修改任务
            case R.id.btn_modify_task: {
                if (taskAdapter == null || taskAdapter.getSelect() == null) {
                    ToastUtil.show(R.string.please_choose_task_first);
                    break;
                }
                InterfaceTask.pbui_Item_MeetTaskInfo select = taskAdapter.getSelect();
                String taskName = edt_task_name.getText().toString().trim();
                if (taskName.isEmpty() || taskName.length() > InterfaceMacro.Pb_String_LenLimit.Pb_MEET_MAX_TASKNAMELEN_VALUE) {
                    ToastUtil.show(R.string.task_name_err);
                    break;
                }
                boolean checked = cb_shuffle_playback.isChecked();
                LogUtil.i(TAG, "onClick 媒体和设备个数=" + currentMedias.size() + ", " + currentDevices.size());
                InterfaceTask.pbui_Item_MeetTaskDetailInfo build = InterfaceTask.pbui_Item_MeetTaskDetailInfo.newBuilder()
                        .setTaskid(select.getTaskid())
                        .setTaskname(s2b(taskName))
                        .setPlaymode(checked ? 1 : 2)
                        .addAllMediaids(currentMedias)
                        .addAllDeviceids(currentDevices)
                        .build();
                jni.updateTask(build);
                reShow();
                break;
            }
            //开始任务
            case R.id.btn_start_task: {
                if (taskAdapter == null || taskAdapter.getSelect() == null) {
                    ToastUtil.show(R.string.please_choose_task_first);
                    break;
                }
                InterfaceTask.pbui_Item_MeetTaskInfo select = taskAdapter.getSelect();
                jni.startTask(select.getTaskid());
                break;
            }
            //停止任务
            case R.id.btn_stop_task: {
                if (taskAdapter == null || taskAdapter.getSelect() == null) {
                    ToastUtil.show(R.string.please_choose_task_first);
                    break;
                }
                InterfaceTask.pbui_Item_MeetTaskInfo select = taskAdapter.getSelect();
                jni.stopTask(select.getTaskid());
                break;
            }
            default:
                break;
        }
    }

    private void showDevicePop() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_device, null);
        devicePop = new PopupWindow(inflate, Values.half_width, Values.half_height);
        devicePop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        devicePop.setTouchable(true);
        // true:设置触摸外面时消失
        devicePop.setOutsideTouchable(true);
        devicePop.setFocusable(true);
        devicePop.setAnimationStyle(R.style.pop_Animation);
        devicePop.showAtLocation(btn_add_media, Gravity.CENTER, 0, 0);
        rv_pop_device = inflate.findViewById(R.id.rv_pop_device);
        popDeviceAdapter = new PopDeviceAdapter(R.layout.item_device_id, presenter.releaseDevices);
        rv_pop_device.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_pop_device.setAdapter(popDeviceAdapter);
        popDeviceAdapter.setOnItemClickListener((adapter, view, position) -> {
            InterfaceDevice.pbui_Item_DeviceDetailInfo item = presenter.releaseDevices.get(position);
            popDeviceAdapter.setSelect(item.getDevcieid());
        });
        inflate.findViewById(R.id.btn_add).setOnClickListener(v -> {
            List<InterfaceDevice.pbui_Item_DeviceDetailInfo> select = popDeviceAdapter.getSelect();
            if (select.isEmpty()) {
                ToastUtil.show(R.string.please_choose_device_first);
                return;
            }
            for (int i = 0; i < select.size(); i++) {
                InterfaceDevice.pbui_Item_DeviceDetailInfo item = select.get(i);
                InterfaceTask.pbui_Item_DeviceTaskDetailInfo build = InterfaceTask.pbui_Item_DeviceTaskDetailInfo.newBuilder()
                        .setDeviceid(item.getDevcieid())
                        .setName(item.getDevname())
                        .build();
                currentDevices.add(build);
            }
            deviceAdapter.notifyDataSetChanged();
            devicePop.dismiss();
        });
        inflate.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            devicePop.dismiss();
        });
    }

    @Override
    public void updateReleaseDeviceRv() {
        if (devicePop != null && devicePop.isShowing()) {
            deviceAdapter.notifyDataSetChanged();
        }
    }

    private void showMediaPop() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_media, null);
        mediaPop = new PopupWindow(inflate, Values.half_width, Values.half_height);
        mediaPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        mediaPop.setTouchable(true);
        // true:设置触摸外面时消失
        mediaPop.setOutsideTouchable(true);
        mediaPop.setFocusable(true);
        mediaPop.setAnimationStyle(R.style.pop_Animation);
        mediaPop.showAtLocation(btn_add_media, Gravity.CENTER, 0, 0);
        rv_pop_media = inflate.findViewById(R.id.rv_pop_media);
        popMediaAdapter = new PopMediaAdapter(R.layout.item_meida_id, presenter.releaseFileData);
        rv_pop_media.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_pop_media.setAdapter(popMediaAdapter);
        popMediaAdapter.setOnItemClickListener((adapter, view, position) -> {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo item = presenter.releaseFileData.get(position);
            int mediaid = item.getMediaid();
            popMediaAdapter.setSelect(mediaid);
        });
        inflate.findViewById(R.id.btn_add).setOnClickListener(v -> {
            List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> select = popMediaAdapter.getSelect();
            if (select.isEmpty()) {
                ToastUtil.show(R.string.please_choose_file_first);
                return;
            }
            for (int i = 0; i < select.size(); i++) {
                InterfaceFile.pbui_Item_MeetDirFileDetailInfo item = select.get(i);
                InterfaceTask.pbui_Item_MediaTaskDetailInfo build = InterfaceTask.pbui_Item_MediaTaskDetailInfo.newBuilder()
                        .setMediaid(item.getMediaid())
                        .setName(item.getName())
                        .build();
                currentMedias.add(build);
            }
            mediaAdapter.notifyDataSetChanged();
            mediaPop.dismiss();
        });
        inflate.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            mediaPop.dismiss();
        });
    }

    @Override
    public void updateReleaseFileRv() {
        if (mediaPop != null && mediaPop.isShowing()) {
            popMediaAdapter.notifyDataSetChanged();
        }
    }
}
