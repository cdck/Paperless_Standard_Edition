package xlk.paperless.standard.view.admin.fragment.system.device;

import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.JsonBean;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.IniUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.PopUtil;
import xlk.paperless.standard.util.ToastUtil;

import static xlk.paperless.standard.data.Constant.isThisDevType;

/**
 * @author Created by xlk on 2020/9/18.
 * @desc
 */
public class AdminDeviceManageFragment extends BaseFragment implements AdminDeviceManageInterface, View.OnClickListener {
    private RecyclerView rv_device;
    private TextInputEditText tie_dev_name;
    private TextInputEditText tie_dev_ip;
    private TextInputEditText tie_lift_id;
    private TextInputEditText tie_mike_id;
    private Button btn_modify;
    private Button btn_delete;
    private Button btn_visitors;
    private Button btn_deploy;
    private AdminDeviceManagePresenter presenter;
    private DeviceAdapter deviceAdapter;
    private InterfaceDevice.pbui_Item_DeviceDetailInfo selectedDevice;
    private PopupWindow paramPop;
    private ClientDeviceAdapter clientDeviceAdapter;
    private List<File> currentFiles = new ArrayList<>();
    private LocalFileAdapter localFileAdapter;
    private RecyclerView rv_current_file;
    private EditText edt_current_dir;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_device_manage, container, false);
        initView(inflate);
        presenter = new AdminDeviceManagePresenter(getContext(), this);
        presenter.queryDevice();
        return inflate;
    }

    private void initView(View inflate) {
        rv_device = (RecyclerView) inflate.findViewById(R.id.rv_device);
        tie_dev_name = (TextInputEditText) inflate.findViewById(R.id.tie_dev_name);
        tie_dev_ip = (TextInputEditText) inflate.findViewById(R.id.tie_dev_ip);
        tie_lift_id = (TextInputEditText) inflate.findViewById(R.id.tie_lift_id);
        tie_mike_id = (TextInputEditText) inflate.findViewById(R.id.tie_mike_id);
        btn_modify = (Button) inflate.findViewById(R.id.btn_modify);
        btn_delete = (Button) inflate.findViewById(R.id.btn_delete);
        btn_visitors = (Button) inflate.findViewById(R.id.btn_visitors);
        btn_deploy = (Button) inflate.findViewById(R.id.btn_deploy);

        inflate.findViewById(R.id.btn_visa_waiver).setOnClickListener(this);
        btn_modify.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_visitors.setOnClickListener(this);
        btn_deploy.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        selectedDevice = deviceAdapter.getSelected();
        switch (v.getId()) {
            case R.id.btn_modify: {
                if (deviceAdapter == null || deviceAdapter.getSelected() == null) {
                    ToastUtil.show(R.string.please_choose_device_first);
                    break;
                }
                String currentDevName = tie_dev_name.getText().toString();
                String currentDevIp = tie_dev_ip.getText().toString().trim();
                String currentLiftId = tie_lift_id.getText().toString().trim();
                String currentMikeId = tie_mike_id.getText().toString().trim();
                if (currentDevName.isEmpty() || currentDevIp.isEmpty() || currentLiftId.isEmpty() || currentMikeId.isEmpty()) {
                    ToastUtil.show(R.string.please_enter_all_content);
                    return;
                }
                int liftId = Integer.parseInt(currentLiftId);
                int mikeId = Integer.parseInt(currentMikeId);
                InterfaceDevice.pbui_SubItem_DeviceIpAddrInfo build = InterfaceDevice.pbui_SubItem_DeviceIpAddrInfo.newBuilder().setIp(ConvertUtil.s2b(currentDevIp)).build();
                int modflag = InterfaceMacro.Pb_DeviceModifyFlag.Pb_DEVICE_MODIFYFLAG_NAME_VALUE
                        | InterfaceMacro.Pb_DeviceModifyFlag.Pb_DEVICE_MODIFYFLAG_IPADDR_VALUE
                        | InterfaceMacro.Pb_DeviceModifyFlag.Pb_DEVICE_MODIFYFLAG_LIFTRES_VALUE;
                presenter.modifyDevice(modflag, selectedDevice.getDevcieid(), currentDevName, liftId, mikeId, selectedDevice.getDeviceflag(), build);
                break;
            }
            case R.id.btn_delete: {
                if (deviceAdapter == null || deviceAdapter.getSelected() == null) {
                    ToastUtil.show(R.string.please_choose_device_first);
                    break;
                }
                if (selectedDevice.getNetstate() == 0) {
                    presenter.deleteDevice(selectedDevice.getDevcieid());
                } else {
                    ToastUtil.show(R.string.err_delete_offline);
                }
                break;
            }
            //设置访客模式
            case R.id.btn_visitors: {
                if (deviceAdapter == null || deviceAdapter.getSelected() == null) {
                    ToastUtil.show(R.string.please_choose_device_first);
                    break;
                }
                InterfaceDevice.pbui_Item_DeviceDetailInfo selected = deviceAdapter.getSelected();
                int devcieid = selected.getDevcieid();
                if (isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetClient_VALUE, devcieid)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.device_mode_choose);
                    builder.setMessage(R.string.device_mode_Introduction);
                    builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                        int deviceflag = selected.getDeviceflag();
                        int newFlag = deviceflag | InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_GUESTMODE_VALUE;
                        jni.modifyDeviceParam(devcieid, newFlag);
                    });
                    builder.setNegativeButton(R.string.no, (dialog, which) -> {
                        int deviceflag = selected.getDeviceflag();
                        int newFlag = deviceflag;
                        if ((deviceflag & InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_GUESTMODE_VALUE)
                                == InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_GUESTMODE_VALUE) {
                            newFlag -= InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_GUESTMODE_VALUE;
                        }
                        jni.modifyDeviceParam(devcieid, newFlag);
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    ToastUtil.show(R.string.please_choose_client_dev);
                }
                break;
            }
            //设置免签到模式
            case R.id.btn_visa_waiver:{
                LogUtils.e("","");
                if (deviceAdapter == null || deviceAdapter.getSelected() == null) {
                    ToastUtil.show(R.string.please_choose_device_first);
                    break;
                }
                InterfaceDevice.pbui_Item_DeviceDetailInfo selected = deviceAdapter.getSelected();
                int devcieid = selected.getDevcieid();
                if (isThisDevType(InterfaceMacro.Pb_DeviceIDType.Pb_DeviceIDType_MeetClient_VALUE, devcieid)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.visa_waiver_choose);
                    builder.setMessage(R.string.visa_waiver_tips);
                    builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                        int deviceflag = selected.getDeviceflag();
                        int newFlag = deviceflag | InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_DIRECTENTER_VALUE;
                        jni.modifyDeviceParam(devcieid, newFlag);
                    });
                    builder.setNegativeButton(R.string.no, (dialog, which) -> {
                        int deviceflag = selected.getDeviceflag();
                        int newFlag = deviceflag;
                        if ((deviceflag & InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_DIRECTENTER_VALUE)
                                == InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_DIRECTENTER_VALUE) {
                            newFlag -= InterfaceMacro.Pb_MeetDeviceFlag.Pb_MEETDEVICE_FLAG_DIRECTENTER_VALUE;
                        }
                        jni.modifyDeviceParam(devcieid, newFlag);
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    ToastUtil.show(R.string.please_choose_client_dev);
                }
                break;
            }
            //参数配置
            case R.id.btn_deploy:
                showParamConfigPop();
                break;
        }
    }

    private void showParamConfigPop() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_param_config, null);
        paramPop = PopUtil.create(inflate,Values.screen_width - 100, Values.screen_height - 100,btn_deploy);
        ViewHolder holder = new ViewHolder(inflate);
        HolderEvent(holder);
    }

    private void HolderEvent(ViewHolder holder) {
        defaultLocalIni(holder);
        clientDeviceAdapter = new ClientDeviceAdapter(R.layout.item_client_4, presenter.clientDevices);
        holder.rv_client.setLayoutManager(new LinearLayoutManager(getContext()));
        holder.rv_client.setAdapter(clientDeviceAdapter);
        clientDeviceAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                clientDeviceAdapter.setSelected(presenter.clientDevices.get(position).getDevcieid());
                boolean selectAll = clientDeviceAdapter.isSelectAll();
                holder.cb_client_all.setChecked(selectAll);
            }
        });
        //设备全选
        holder.cb_client_all.setOnClickListener(v -> {
            boolean checked = holder.cb_client_all.isChecked();
            holder.cb_client_all.setChecked(checked);
            clientDeviceAdapter.setSelectAll(checked);
        });

        holder.cb_ip.setOnClickListener(v -> {
            boolean checked = holder.cb_ip.isChecked();
            holder.cb_ip.setChecked(checked);
            holder.tv_ip.setEnabled(checked);
            holder.edt_ip.setEnabled(checked);
        });
        holder.cb_port.setOnClickListener(v -> {
            boolean checked = holder.cb_port.isChecked();
            holder.cb_port.setChecked(checked);
            holder.tv_port.setEnabled(checked);
            holder.edt_port.setEnabled(checked);
        });
        holder.cb_cache_dir.setOnClickListener(v -> {
            boolean checked = holder.cb_cache_dir.isChecked();
            holder.cb_cache_dir.setChecked(checked);
            holder.tv_cache_dir.setEnabled(checked);
            holder.edt_cache_dir.setEnabled(checked);
            holder.btn_cache_dir.setEnabled(checked);
        });
        holder.edt_cache_dir.setKeyListener(null);
        //选择目录
        holder.btn_cache_dir.setOnClickListener(v -> {
            showChooseDir(holder.edt_cache_dir);
        });
        holder.cb_cache_size.setOnClickListener(v -> {
            boolean checked = holder.cb_cache_size.isChecked();
            holder.cb_cache_size.setChecked(checked);
            holder.tv_cache_size.setEnabled(checked);
            holder.edt_cache_size.setEnabled(checked);
        });
        holder.cb_coding_open.setOnClickListener(v -> {
            boolean checked = holder.cb_coding_open.isChecked();
            holder.cb_coding_open.setChecked(checked);
            holder.cb_coding.setEnabled(checked);
        });
        holder.cb_decoding_open.setOnClickListener(v -> {
            boolean checked = holder.cb_decoding_open.isChecked();
            holder.cb_decoding_open.setChecked(checked);
            holder.cb_decoding.setEnabled(checked);
        });
        holder.cb_debug_open.setOnClickListener(v -> {
            boolean checked = holder.cb_debug_open.isChecked();
            holder.cb_debug_open.setChecked(checked);
            holder.cb_debug.setEnabled(checked);
        });
        holder.cb_camera_open.setOnClickListener(v -> {
            boolean checked = holder.cb_camera_open.isChecked();
            holder.cb_camera_open.setChecked(checked);
            holder.cb_camera.setEnabled(checked);
        });
        holder.cb_mike_open.setOnClickListener(v -> {
            boolean checked = holder.cb_mike_open.isChecked();
            holder.cb_mike_open.setChecked(checked);
            holder.cb_mike.setEnabled(checked);
        });
        holder.cb_multicast_open.setOnClickListener(v -> {
            boolean checked = holder.cb_multicast_open.isChecked();
            holder.cb_multicast_open.setChecked(checked);
            holder.cb_multicast.setEnabled(checked);
        });
        holder.cb_upload_open.setOnClickListener(v -> {
            boolean checked = holder.cb_upload_open.isChecked();
            holder.cb_upload_open.setChecked(checked);
            holder.cb_upload.setEnabled(checked);
        });
        holder.sp_coding_mode.setEnabled(false);
        holder.cb_coding_mode.setOnClickListener(v -> {
            boolean checked = holder.cb_coding_mode.isChecked();
            holder.cb_coding_mode.setChecked(checked);
            holder.tv_coding_mode.setEnabled(checked);
            holder.sp_coding_mode.setEnabled(checked);
            holder.cb_tcp.setEnabled(checked);
        });
        holder.sp_screen_stream.setEnabled(false);
        holder.sp_screen_size.setEnabled(false);
        holder.cb_screen_stream.setOnClickListener(v -> {
            boolean checked = holder.cb_screen_stream.isChecked();
            holder.cb_screen_stream.setChecked(checked);
            holder.tv_screen_stream.setEnabled(checked);
            holder.sp_screen_stream.setEnabled(checked);
            holder.sp_screen_size.setEnabled(checked);
        });
        holder.sp_camera_stream.setEnabled(false);
        holder.sp_camera_size.setEnabled(false);
        holder.cb_camera_stream.setOnClickListener(v -> {
            boolean checked = holder.cb_camera_stream.isChecked();
            holder.cb_camera_stream.setChecked(checked);
            holder.tv_camera_stream.setEnabled(checked);
            holder.sp_camera_stream.setEnabled(checked);
            holder.sp_camera_size.setEnabled(checked);
        });
        //全部启用
        holder.cb_parameter_all.setOnClickListener(v -> {
            boolean checked = holder.cb_parameter_all.isChecked();
            holder.cb_parameter_all.setChecked(checked);
            setEnableAll(holder, checked);
        });
        //取消
        holder.btn_cancel.setOnClickListener(v -> paramPop.dismiss());
        //修改
        holder.btn_modify.setOnClickListener(v -> {
            boolean isRestart = holder.cb_restart_app.isChecked();
            List<Integer> checkedIds = clientDeviceAdapter.getCheckedIds();
            if (checkedIds.isEmpty()) {
                ToastUtil.show(R.string.please_choose_client_dev);
                return;
            }
            String restart = isRestart ? "1" : "0";
            String jsonStr = "{\"restart\":" + restart + "," + "\"item\":[";
            String itemStr = "";
            String ip = holder.edt_ip.getText().toString().trim();
            if (holder.cb_ip.isChecked()) {
                if (ip.isEmpty()) {
                    ToastUtil.show(R.string.please_enter_ip);
                    return;
                }
                itemStr += "{"
                        + "\"section\":\"areaaddr\","
                        + "\"key\":\"area0ip\","
                        + "\"value\":\"" + ip + "\""
                        + "}"
                ;
            }
            String port = holder.edt_port.getText().toString().trim();
            if (holder.cb_port.isChecked()) {
                if (port.isEmpty()) {
                    ToastUtil.show(R.string.please_enter_port);
                    return;
                }
                if (!itemStr.isEmpty()) {
                    itemStr += ",";
                }
                itemStr += "{"
                        + "\"section\":\"areaaddr\","
                        + "\"key\":\"area0port\","
                        + "\"value\":\"" + port + "\""
                        + "}"
                ;
            }
            //缓存位置
            String cache_dir = holder.edt_cache_dir.getText().toString().trim();
            if (holder.cb_cache_dir.isChecked()) {
                if (cache_dir.isEmpty()) {
                    ToastUtil.show(R.string.please_enter_cache_dir);
                    return;
                }
                if (!itemStr.isEmpty()) {
                    itemStr += ",";
                }
                itemStr += "{"
                        + "\"section\":\"Buffer Dir\","
                        + "\"key\":\"configdir\","
                        + "\"value\":\"" + cache_dir + "\""
                        + "}"
                ;
            }
            //缓存大小
            String cache_size = holder.edt_cache_size.getText().toString().trim();
            if (holder.cb_cache_size.isChecked()) {
                if (cache_size.isEmpty()) {
                    ToastUtil.show(R.string.please_enter_cache_size);
                    return;
                }
                if (!itemStr.isEmpty()) {
                    itemStr += ",";
                }
                itemStr += "{"
                        + "\"section\":\"Buffer Dir\","
                        + "\"key\":\"mediadirsize\","
                        + "\"value\":\"" + cache_size + "\""
                        + "}"
                ;
            }
            //硬件编码
            if (holder.cb_coding_open.isChecked()) {
                boolean checked = holder.cb_coding.isChecked();
                if (!itemStr.isEmpty()) {
                    itemStr += ",";
                }
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"hwencode\","
                        + "\"value\":\"" + (checked ? "1" : "0") + "\""
                        + "}";
            }
            //硬件解码
            if (holder.cb_decoding_open.isChecked()) {
                boolean checked = holder.cb_decoding.isChecked();
                if (!itemStr.isEmpty()) {
                    itemStr += ",";
                }
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"hwdecode\","
                        + "\"value\":\"" + (checked ? "1" : "0") + "\""
                        + "}";
            }
            //启动调试
            if (holder.cb_debug_open.isChecked()) {
                boolean checked = holder.cb_debug.isChecked();
                if (!itemStr.isEmpty()) {
                    itemStr += ",";
                }
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"console\","
                        + "\"value\":\"" + (checked ? "1" : "0") + "\""
                        + "}";
            }
            //是否开启USB视频设备采集（摄像头）
            if (holder.cb_camera_open.isChecked()) {
                boolean checked = holder.cb_camera.isChecked();
                if (!itemStr.isEmpty()) {
                    itemStr += ",";
                }
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"camaracap\","
                        + "\"value\":\"" + (checked ? "1" : "0") + "\""
                        + "}";
            }
            //是否开启麦克风
            if (holder.cb_mike_open.isChecked()) {
                boolean checked = holder.cb_mike.isChecked();
                if (!itemStr.isEmpty()) {
                    itemStr += ",";
                }
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"videoaudio\","
                        + "\"value\":\"" + (checked ? "1" : "0") + "\""
                        + "}";
            }
            //禁用组播
            if (holder.cb_multicast_open.isChecked()) {
                boolean checked = holder.cb_multicast.isChecked();
                if (!itemStr.isEmpty()) {
                    itemStr += ",";
                }
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"disablemulticast\","
                        + "\"value\":\"" + (checked ? "1" : "0") + "\""
                        + "}";
            }
            //上传高清视频时转换
            if (holder.cb_upload_open.isChecked()) {
                boolean checked = holder.cb_upload.isChecked();
                if (!itemStr.isEmpty()) {
                    itemStr += ",";
                }
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"mediatranscode\","
                        + "\"value\":\"" + (checked ? "1" : "0") + "\""
                        + "}";
            }
            //编码模式选择和TCP模式是否开启
            if (holder.cb_coding_mode.isChecked()) {
                if (!itemStr.isEmpty()) {
                    itemStr += ",";
                }
                int position = holder.sp_coding_mode.getSelectedItemPosition();
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"encmode\","
                        + "\"value\":\"" + position + "\""
                        + "},";
                boolean checked = holder.cb_tcp.isChecked();
                itemStr += "{"
                        + "\"section\":\"selfinfo\","
                        + "\"key\":\"streamprotol\","
                        + "\"value\":\"" + (checked ? "1" : "0") + "\""
                        + "}";
            }
            //桌面同屏流
            if (holder.cb_screen_stream.isChecked()) {
                if (!itemStr.isEmpty()) {
                    itemStr += ",";
                }
                int index = holder.sp_screen_stream.getSelectedItemPosition();
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"video0\","
                        + "\"value\":\"" + (index == 2 ? -1 : index) + "\""
                        + "},";

                int selectedItemPosition = holder.sp_screen_size.getSelectedItemPosition();
                String width, height;
                if (selectedItemPosition == 1) {
                    width = "1280";
                    height = "720";
                } else if (selectedItemPosition == 2) {
                    width = "720";
                    height = "640";
                } else if (selectedItemPosition == 3) {
                    width = "480";
                    height = "320";
                } else {
                    width = "1920";
                    height = "1080";
                }
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"stream2width\","
                        + "\"value\":\"" + width + "\""
                        + "},";
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"stream2height\","
                        + "\"value\":\"" + height + "\""
                        + "}";
            }
            //摄像头流
            if (holder.cb_camera_stream.isChecked()) {
                if (!itemStr.isEmpty()) {
                    itemStr += ",";
                }
                int index = holder.sp_camera_stream.getSelectedItemPosition();
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"video1\","
                        + "\"value\":\"" + (index == 2 ? -1 : index) + "\""
                        + "},";
                int selectedItemPosition = holder.sp_camera_size.getSelectedItemPosition();
                String width, height;
                if (selectedItemPosition == 1) {
                    width = "1280";
                    height = "720";
                } else if (selectedItemPosition == 2) {
                    width = "720";
                    height = "640";
                } else if (selectedItemPosition == 3) {
                    width = "480";
                    height = "320";
                } else {
                    width = "1920";
                    height = "1080";
                }
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"stream3width\","
                        + "\"value\":\"" + width + "\""
                        + "},";
                itemStr += "{"
                        + "\"section\":\"debug\","
                        + "\"key\":\"stream3height\","
                        + "\"value\":\"" + height + "\""
                        + "}";
            }
            if (itemStr.isEmpty()) {
                ToastUtil.show(R.string.please_choose_modify_param);
                return;
            }
            jsonStr += itemStr + "]}";
            jni.remoteConfig(checkedIds, jsonStr);
        });
    }

    private FileFilter dirFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory() && !pathname.getName().startsWith(".");
        }
    };

    private void showChooseDir(EditText edt) {
        String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        currentFiles.clear();
        currentFiles.addAll(FileUtils.listFilesInDirWithFilter(rootDir, dirFilter));
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_local_file, null);
        PopupWindow dirPop = PopUtil.create(inflate,btn_deploy);
        edt_current_dir = inflate.findViewById(R.id.edt_current_dir);
        edt_current_dir.setKeyListener(null);
        edt_current_dir.setText(rootDir);

        rv_current_file = inflate.findViewById(R.id.rv_current_file);
        localFileAdapter = new LocalFileAdapter(R.layout.item_local_file, currentFiles);
        rv_current_file.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_current_file.setAdapter(localFileAdapter);
        localFileAdapter.setOnItemClickListener((adapter, view, position) -> {
            File file = currentFiles.get(position);
            edt_current_dir.setText(file.getAbsolutePath());
            edt_current_dir.setSelection(edt_current_dir.getText().toString().length());
            List<File> files = FileUtils.listFilesInDirWithFilter(file, dirFilter);
            currentFiles.clear();
            currentFiles.addAll(files);
            localFileAdapter.notifyDataSetChanged();
        });
        inflate.findViewById(R.id.iv_back).setOnClickListener(v -> {
            String dirPath = edt_current_dir.getText().toString().trim();
            if (dirPath.equals(rootDir)) {
                ToastUtil.show(R.string.current_dir_root);
                return;
            }
            File file = new File(dirPath);
            File parentFile = file.getParentFile();
            edt_current_dir.setText(parentFile.getAbsolutePath());
            LogUtil.i(TAG, "showChooseDir 上一级的目录=" + parentFile.getAbsolutePath());
            List<File> files = FileUtils.listFilesInDirWithFilter(parentFile, dirFilter);
            currentFiles.clear();
            currentFiles.addAll(files);
            localFileAdapter.notifyDataSetChanged();
        });
        inflate.findViewById(R.id.btn_determine).setOnClickListener(v -> {
            String text = edt_current_dir.getText().toString();
            edt.setText(text);
            edt.setSelection(text.length());
            dirPop.dismiss();
        });
        inflate.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            dirPop.dismiss();
        });
    }

    /**
     * 将本机的ini文件内容显示到布局中
     */
    private void defaultLocalIni(ViewHolder holder) {
        IniUtil ini = IniUtil.getInstance();
        String ip = ini.get("areaaddr", "area0ip");
        holder.edt_ip.setText(ip);
        String port = ini.get("areaaddr", "area0port");
        holder.edt_port.setText(port);
        String configdir = ini.get("Buffer Dir", "configdir");
        String mediadirsize = ini.get("Buffer Dir", "mediadirsize");
        holder.edt_cache_dir.setText(configdir);
        //设置光标在最末尾
        holder.edt_cache_dir.setSelection(configdir.length());
        holder.edt_cache_size.setText(mediadirsize);

        //是否开启显卡硬编码 0关闭 1开启 默认0
        String hwencode = ini.get("debug", "hwencode");
        holder.cb_coding.setChecked(hwencode.equals("1"));

        //是否开启显卡硬解码 0关闭 1开启 默认0
        String hwdecode = ini.get("debug", "hwdecode");
        holder.cb_decoding.setChecked(hwdecode.equals("1"));

        //是否开启调试窗口 0关闭 1开启 默认0
        String console = ini.get("debug", "console");
        holder.cb_debug.setChecked(console.equals("1"));

        //是否开启USB视频设备采集 0关闭 1开启
        String camaracap = ini.get("debug", "camaracap");
        holder.cb_camera.setChecked(camaracap.equals("1"));

        //是否启动桌面采集声卡回放 0关闭 1开启 启用将扬声器输出捕获并附加到2号视频通道 当videoaudio=1启用时 注:优先使用输入音频附加
        String shareaudio = ini.get("debug", "shareaudio");
        holder.cb_mike.setChecked(shareaudio.equals("1"));

        //等于1表示禁用组播
        String disablemulticast = ini.get("debug", "disablemulticast");
        holder.cb_multicast.setChecked(disablemulticast.equals("1"));

        //上传超高清视频时转换流畅格式 0关闭 1开启
        String mediatranscode = ini.get("debug", "mediatranscode");
        holder.cb_upload.setChecked(mediatranscode.equals("1"));

        //设置视频流stream(index)[width|height]最大宽高 宽高同时设置才会生效
        String stream2width = ini.get("debug", "stream2width");
        String stream2height = ini.get("debug", "stream2height");
        if (!stream2width.isEmpty() && !stream2height.isEmpty()) {
            try {
                int width = Integer.parseInt(stream2width);
                int height = Integer.parseInt(stream2height);
                int index = 0;
                if (width == 1920 && height == 1080) {
                    index = 0;
                } else if (width == 1280 && height == 720) {
                    index = 1;
                } else if (width == 720 && height == 480) {
                    index = 2;
                } else if (width == 480 && height == 360) {
                    index = 3;
                }
                holder.sp_screen_stream.setSelection(index);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //设置视频流stream(index)[width|height]最大宽高 宽高同时设置才会生效
        String stream3width = ini.get("debug", "stream3width");
        String stream3height = ini.get("debug", "stream3height");
        if (!stream3width.isEmpty() && !stream3height.isEmpty()) {
            try {
                int width = Integer.parseInt(stream3width);
                int height = Integer.parseInt(stream3height);
                int index = 0;
                if (width == 1920 && height == 1080) {
                    index = 0;
                } else if (width == 1280 && height == 720) {
                    index = 1;
                } else if (width == 720 && height == 480) {
                    index = 2;
                } else if (width == 480 && height == 360) {
                    index = 3;
                }
                holder.sp_camera_size.setSelection(index);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //设置流编码模式0高质量 1中等 2低带宽 默认0
        String encmode = ini.get("debug", "encmode");
        try {
            int mode = Integer.parseInt(encmode);
            holder.sp_coding_mode.setSelection(mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //是否启用TCP传输流数据 1表示开启 0表示关闭 默认0
        String tcp = ini.get("selfinfo", "streamprotol");
        holder.cb_tcp.setChecked(tcp.equals("1"));

    }

    private void setEnableAll(ViewHolder holder, boolean checked) {
        holder.cb_ip.setChecked(checked);
        holder.tv_ip.setEnabled(checked);
        holder.edt_ip.setEnabled(checked);
        holder.cb_port.setChecked(checked);
        holder.tv_port.setEnabled(checked);
        holder.edt_port.setEnabled(checked);
        holder.cb_cache_dir.setChecked(checked);
        holder.tv_cache_dir.setEnabled(checked);
        holder.edt_cache_dir.setEnabled(checked);
        holder.btn_cache_dir.setEnabled(checked);
        holder.cb_cache_size.setChecked(checked);
        holder.tv_cache_size.setEnabled(checked);
        holder.edt_cache_size.setEnabled(checked);
        holder.cb_coding_open.setChecked(checked);
        holder.cb_coding.setEnabled(checked);
        holder.cb_decoding_open.setChecked(checked);
        holder.cb_decoding.setEnabled(checked);
        holder.cb_debug_open.setChecked(checked);
        holder.cb_debug.setEnabled(checked);
        holder.cb_camera_open.setChecked(checked);
        holder.cb_camera.setEnabled(checked);
        holder.cb_mike_open.setChecked(checked);
        holder.cb_mike.setEnabled(checked);
        holder.cb_multicast_open.setChecked(checked);
        holder.cb_multicast.setEnabled(checked);
        holder.cb_upload_open.setChecked(checked);
        holder.cb_upload.setEnabled(checked);
        holder.cb_coding_mode.setChecked(checked);
        holder.tv_coding_mode.setEnabled(checked);
        holder.sp_coding_mode.setEnabled(checked);
        holder.cb_tcp.setEnabled(checked);
        holder.cb_screen_stream.setChecked(checked);
        holder.tv_screen_stream.setEnabled(checked);
        holder.sp_screen_stream.setEnabled(checked);
        holder.sp_screen_size.setEnabled(checked);
        holder.cb_camera_stream.setChecked(checked);
        holder.tv_camera_stream.setEnabled(checked);
        holder.sp_camera_stream.setEnabled(checked);
        holder.sp_camera_size.setEnabled(checked);
    }

    @Override
    public void updateClientRv() {
        if (paramPop != null && paramPop.isShowing()) {
            clientDeviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateDeviceRv(List<InterfaceDevice.pbui_Item_DeviceDetailInfo> deviceInfos) {
        if (deviceAdapter == null) {
            deviceAdapter = new DeviceAdapter(R.layout.item_admin_device_manage, deviceInfos);
            rv_device.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_device.setAdapter(deviceAdapter);
            deviceAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    selectedDevice = deviceInfos.get(position);
                    LogUtil.i(TAG, "onItemClick 选中设备= id：" + selectedDevice.getDevcieid() + ",名称：" + selectedDevice.getDevname().toStringUtf8());
                    deviceAdapter.setSelected(selectedDevice.getDevcieid());
                    updateBottomUI(selectedDevice);
                }
            });
        } else {
            deviceAdapter.notifyDataSetChanged();
        }
    }

    private void updateBottomUI(InterfaceDevice.pbui_Item_DeviceDetailInfo info) {
        tie_dev_name.setText(info.getDevname().toStringUtf8());
        List<InterfaceDevice.pbui_SubItem_DeviceIpAddrInfo> ipinfoList = info.getIpinfoList();
        if (!ipinfoList.isEmpty()) {
            InterfaceDevice.pbui_SubItem_DeviceIpAddrInfo item = ipinfoList.get(0);
            tie_dev_ip.setText(item.getIp().toStringUtf8());
        }
        tie_lift_id.setText(String.valueOf(info.getLiftgroupres0()));
        tie_mike_id.setText(String.valueOf(info.getLiftgroupres1()));
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
            presenter.queryDevice();
        }
    }

    public static class ViewHolder {
        public View rootView;
        public RecyclerView rv_client;
        public CheckBox cb_ip;
        public TextView tv_ip;
        public EditText edt_ip;
        public CheckBox cb_port;
        public TextView tv_port;
        public EditText edt_port;
        public CheckBox cb_cache_dir;
        public TextView tv_cache_dir;
        public EditText edt_cache_dir;
        public Button btn_cache_dir;
        public CheckBox cb_cache_size;
        public TextView tv_cache_size;
        public EditText edt_cache_size;
        public CheckBox cb_coding_open;
        public CheckBox cb_coding;
        public CheckBox cb_decoding_open;
        public CheckBox cb_decoding;
        public CheckBox cb_debug_open;
        public CheckBox cb_debug;
        public CheckBox cb_camera_open;
        public CheckBox cb_camera;
        public CheckBox cb_mike_open;
        public CheckBox cb_mike;
        public CheckBox cb_multicast_open;
        public CheckBox cb_multicast;
        public CheckBox cb_upload_open;
        public CheckBox cb_upload;
        public CheckBox cb_coding_mode;
        public TextView tv_coding_mode;
        public Spinner sp_coding_mode;
        public CheckBox cb_tcp;
        public CheckBox cb_screen_stream;
        public TextView tv_screen_stream;
        public Spinner sp_screen_stream;
        public Spinner sp_screen_size;
        public CheckBox cb_camera_stream;
        public TextView tv_camera_stream;
        public Spinner sp_camera_stream;
        public Spinner sp_camera_size;
        public CheckBox cb_client_all;
        public CheckBox cb_parameter_all;
        public CheckBox cb_restart_app;
        public Button btn_cancel;
        public Button btn_modify;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.rv_client = (RecyclerView) rootView.findViewById(R.id.rv_client);
            this.cb_ip = (CheckBox) rootView.findViewById(R.id.cb_ip);
            this.tv_ip = (TextView) rootView.findViewById(R.id.tv_ip);
            this.edt_ip = (EditText) rootView.findViewById(R.id.edt_ip);
            this.cb_port = (CheckBox) rootView.findViewById(R.id.cb_port);
            this.tv_port = (TextView) rootView.findViewById(R.id.tv_port);
            this.edt_port = (EditText) rootView.findViewById(R.id.edt_port);
            this.cb_cache_dir = (CheckBox) rootView.findViewById(R.id.cb_cache_dir);
            this.tv_cache_dir = (TextView) rootView.findViewById(R.id.tv_cache_dir);
            this.edt_cache_dir = (EditText) rootView.findViewById(R.id.edt_cache_dir);
            this.btn_cache_dir = (Button) rootView.findViewById(R.id.btn_cache_dir);
            this.cb_cache_size = (CheckBox) rootView.findViewById(R.id.cb_cache_size);
            this.tv_cache_size = (TextView) rootView.findViewById(R.id.tv_cache_size);
            this.edt_cache_size = (EditText) rootView.findViewById(R.id.edt_cache_size);
            this.cb_coding_open = (CheckBox) rootView.findViewById(R.id.cb_coding_open);
            this.cb_coding = (CheckBox) rootView.findViewById(R.id.cb_coding);
            this.cb_decoding_open = (CheckBox) rootView.findViewById(R.id.cb_decoding_open);
            this.cb_decoding = (CheckBox) rootView.findViewById(R.id.cb_decoding);
            this.cb_debug_open = (CheckBox) rootView.findViewById(R.id.cb_debug_open);
            this.cb_debug = (CheckBox) rootView.findViewById(R.id.cb_debug);
            this.cb_camera_open = (CheckBox) rootView.findViewById(R.id.cb_camera_open);
            this.cb_camera = (CheckBox) rootView.findViewById(R.id.cb_camera);
            this.cb_mike_open = (CheckBox) rootView.findViewById(R.id.cb_mike_open);
            this.cb_mike = (CheckBox) rootView.findViewById(R.id.cb_mike);
            this.cb_multicast_open = (CheckBox) rootView.findViewById(R.id.cb_multicast_open);
            this.cb_multicast = (CheckBox) rootView.findViewById(R.id.cb_multicast);
            this.cb_upload_open = (CheckBox) rootView.findViewById(R.id.cb_upload_open);
            this.cb_upload = (CheckBox) rootView.findViewById(R.id.cb_upload);
            this.cb_coding_mode = (CheckBox) rootView.findViewById(R.id.cb_coding_mode);
            this.tv_coding_mode = (TextView) rootView.findViewById(R.id.tv_coding_mode);
            this.sp_coding_mode = (Spinner) rootView.findViewById(R.id.sp_coding_mode);
            this.cb_tcp = (CheckBox) rootView.findViewById(R.id.cb_tcp);
            this.cb_screen_stream = (CheckBox) rootView.findViewById(R.id.cb_screen_stream);
            this.tv_screen_stream = (TextView) rootView.findViewById(R.id.tv_screen_stream);
            this.sp_screen_stream = (Spinner) rootView.findViewById(R.id.sp_screen_stream);
            this.sp_screen_size = (Spinner) rootView.findViewById(R.id.sp_screen_size);
            this.cb_camera_stream = (CheckBox) rootView.findViewById(R.id.cb_camera_stream);
            this.tv_camera_stream = (TextView) rootView.findViewById(R.id.tv_camera_stream);
            this.sp_camera_stream = (Spinner) rootView.findViewById(R.id.sp_camera_stream);
            this.sp_camera_size = (Spinner) rootView.findViewById(R.id.sp_camera_size);
            this.cb_client_all = (CheckBox) rootView.findViewById(R.id.cb_client_all);
            this.cb_parameter_all = (CheckBox) rootView.findViewById(R.id.cb_parameter_all);
            this.cb_restart_app = (CheckBox) rootView.findViewById(R.id.cb_restart_app);
            this.btn_cancel = (Button) rootView.findViewById(R.id.btn_cancel);
            this.btn_modify = (Button) rootView.findViewById(R.id.btn_modify);
        }

    }
}
