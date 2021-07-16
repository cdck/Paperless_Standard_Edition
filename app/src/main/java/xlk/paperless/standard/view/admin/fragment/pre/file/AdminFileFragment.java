package xlk.paperless.standard.view.admin.fragment.pre.file;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeet;

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
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.util.UriUtil;

import static xlk.paperless.standard.data.Constant.RESOURCE_0;
import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author Created by xlk on 2020/10/21.
 * @desc
 */
public class AdminFileFragment extends BaseFragment implements AdminFileInterface, View.OnClickListener {

    private RecyclerView rv_dir;
    private RecyclerView rv_file;
    private EditText edt_dir_name, edt_file_name;
    private Button btn_dir_increase;
    private Button btn_dir_modify;
    private Button btn_dir_del;
    private Button btn_dir_permission;
    private Button btn_dir_sort;
    private Button btn_file_increase;
    private Button btn_file_modify;
    private Button btn_file_del;
    private Button btn_file_sort;
    private Button btn_file_history;
    private AdminFilePresenter presenter;
    private DirAdapter dirAdapter;
    private FileAdapter fileAdapter;
    private PopupWindow dirPermissionPop, sortFilePop, sortDirPop;
    private RecyclerView rv_permission_dir, rv_dir_permission_member;
    private DirNameAdapter dirNameAdapter;
    private DirPermissionMemberAdapter dirPermissionMemberAdapter;
    private CheckBox cbAll;
    private final int LOCAL_FILE_REQUEST_CODE = 1;
    private RecyclerView rv_sort_file_dir, rv_sort_file;
    private DirAdapter sortFiledirAdapter;
    private FileSortAdapter fileSortAdapter;
    private DirAdapter sortDirAdapter;
    private PopupWindow historyPop;
    private RecyclerView rv_history_meeting;
    private RecyclerView rv_history_dir;
    private RecyclerView rv_history_file;
    private HistoryMeetAdapter meetAdapter;
    private DirAdapter historyDirAdapter;
    private FileSortAdapter historyFileAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_admin_file, container, false);
        initView(inflate);
        presenter = new AdminFilePresenter(this);
        presenter.queryDir();
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
            presenter.queryDir();
        }
    }

    private void initView(View rootView) {
        this.rv_dir = (RecyclerView) rootView.findViewById(R.id.rv_dir);
        this.rv_file = (RecyclerView) rootView.findViewById(R.id.rv_file);
        this.edt_dir_name = (EditText) rootView.findViewById(R.id.edt_dir_name);
        this.edt_file_name = (EditText) rootView.findViewById(R.id.edt_file_name);
        this.btn_dir_increase = (Button) rootView.findViewById(R.id.btn_dir_increase);
        this.btn_dir_modify = (Button) rootView.findViewById(R.id.btn_dir_modify);
        this.btn_dir_del = (Button) rootView.findViewById(R.id.btn_dir_del);
        this.btn_dir_permission = (Button) rootView.findViewById(R.id.btn_dir_permission);
        this.btn_dir_sort = (Button) rootView.findViewById(R.id.btn_dir_sort);
        this.btn_file_increase = (Button) rootView.findViewById(R.id.btn_file_increase);
        this.btn_file_modify = (Button) rootView.findViewById(R.id.btn_file_modify);
        this.btn_file_del = (Button) rootView.findViewById(R.id.btn_file_del);
        this.btn_file_sort = (Button) rootView.findViewById(R.id.btn_file_sort);
        this.btn_file_history = (Button) rootView.findViewById(R.id.btn_file_history);

        this.btn_dir_increase.setOnClickListener(this);
        this.btn_dir_modify.setOnClickListener(this);
        this.btn_dir_del.setOnClickListener(this);
        this.btn_dir_permission.setOnClickListener(this);
        this.btn_dir_sort.setOnClickListener(this);
        this.btn_file_increase.setOnClickListener(this);
        this.btn_file_modify.setOnClickListener(this);
        this.btn_file_del.setOnClickListener(this);
        this.btn_file_sort.setOnClickListener(this);
        this.btn_file_history.setOnClickListener(this);
    }

    @Override
    public void updateDirRv(List<InterfaceFile.pbui_Item_MeetDirDetailInfo> dirInfos) {
        //更新页面中的目录
        if (dirAdapter == null) {
            dirAdapter = new DirAdapter(R.layout.item_admin_file_dir, dirInfos);
            rv_dir.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_dir.setAdapter(dirAdapter);
            dirAdapter.setOnItemClickListener((adapter, view, position) -> {
                InterfaceFile.pbui_Item_MeetDirDetailInfo dirItem = dirInfos.get(position);
                int dirId = dirItem.getId();
                dirAdapter.setSelected(dirId);
                presenter.setCurrentDirId(dirId);
                presenter.queryFileByDir(dirId);
                edt_dir_name.setText(dirItem.getName().toStringUtf8());
            });
            if (!dirInfos.isEmpty()) {
                int dirId = dirInfos.get(0).getId();
                dirAdapter.setSelected(dirId);
                presenter.setCurrentDirId(dirId);
                presenter.queryFileByDir(dirId);
            }
        } else {
            dirAdapter.notifyDataSetChanged();
        }
        //更新目录权限PopupWindow中的目录
        if (dirPermissionPop != null && dirPermissionPop.isShowing()) {
            LogUtil.i(TAG, "updateDirRv dirNameAdapter 更新");
            dirNameAdapter.notifyDataSetChanged();
        }
        //更新历史资料的目录信息
        if (historyPop != null && historyPop.isShowing()) {
            historyDirAdapter = new DirAdapter(R.layout.item_admin_file_dir, dirInfos);
            rv_history_dir.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_history_dir.setAdapter(historyDirAdapter);
            historyDirAdapter.setOnItemClickListener((adapter, view, position) -> {
                int dirId = dirInfos.get(position).getId();
                historyDirAdapter.setSelected(dirId);
                presenter.setCurrentHistoryDirId(dirId);
                presenter.queryFileByDir(dirId);
            });
            if (!dirInfos.isEmpty()) {
                int dirId = dirInfos.get(0).getId();
                historyDirAdapter.setSelected(dirId);
                presenter.setCurrentHistoryDirId(dirId);
                presenter.queryFileByDir(dirId);
            }
        }
    }

    @Override
    public void updateDirFileRv(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> dirFiles) {
        if (fileAdapter == null) {
            fileAdapter = new FileAdapter(R.layout.item_admin_file, dirFiles);
            rv_file.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_file.setAdapter(fileAdapter);
            fileAdapter.addChildClickViewIds(R.id.item_btn_open);
            fileAdapter.setOnItemClickListener((adapter, view, position) -> {
                int mediaid = dirFiles.get(position).getMediaid();
                fileAdapter.setCheck(mediaid);
                //获取最后一个选中的
                InterfaceFile.pbui_Item_MeetDirFileDetailInfo lastCheckFile = fileAdapter.getLastCheckFile();
                edt_file_name.setText(lastCheckFile != null ? lastCheckFile.getName().toStringUtf8() : "");
            });
            fileAdapter.setOnItemChildClickListener((adapter, view, position) -> {
                InterfaceFile.pbui_Item_MeetDirFileDetailInfo dirFile = dirFiles.get(position);
                if (FileUtil.isAudioAndVideoFile(dirFile.getName().toStringUtf8())) {
                    List<Integer> devIds = new ArrayList<>();
                    devIds.add(Values.localDeviceId);
                    JniHandler.getInstance().mediaPlayOperate(dirFile.getMediaid(), devIds, 0, RESOURCE_0, 0, 0);
                } else {
                    FileUtil.openFile(getContext(), Constant.DIR_DATA_FILE, dirFile.getName().toStringUtf8(), dirFile.getMediaid());
                }
            });
        } else {
            fileAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dir_increase:
                createDir();
                break;
            case R.id.btn_dir_modify:
                modifyDir();
                break;
            case R.id.btn_dir_del:
                deleteDir();
                break;
            case R.id.btn_dir_permission:
                showDirPermissionPop(presenter.getDirData());
                break;
            case R.id.btn_dir_sort:
                showSortDirPop(presenter.getSortDirData());
                break;
            case R.id.btn_file_increase:
                if (dirAdapter != null && dirAdapter.getSelected() != null) {
                    chooseLocalFile();
                } else {
                    ToastUtil.show(R.string.please_choose_dir_first);
                }
                break;
            case R.id.btn_file_modify:
                modifyFile();
                break;
            case R.id.btn_file_del:
                deleteFile();
                break;
            case R.id.btn_file_sort:
                showSortFilePop(presenter.getDirData());
                break;
            case R.id.btn_file_history:
                if (dirAdapter != null && dirAdapter.getSelected() != null) {
                    showOtherMeetFile(dirAdapter.getSelected().getId());
                } else {
                    ToastUtil.show(R.string.please_choose_dir_first);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 选中要导入的目录
     * 查询所有的会议，过滤掉当前的会议
     * 点击某一个会议后进行设置当前会议，再查询会议目录
     * 选中目录后再查询会议目录文件
     * 确定导入时将当前选中的文件添加到要导入的目录
     */
    private void showOtherMeetFile(int dirId) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_history_meeting, null);
        historyPop = new PopupWindow(inflate, Values.screen_width - 100, Values.screen_height - 100);
        historyPop.setBackgroundDrawable(new BitmapDrawable());
        //设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        historyPop.setTouchable(true);
        //true:设置触摸外面时消失
        historyPop.setOutsideTouchable(true);
        historyPop.setFocusable(true);
        historyPop.setAnimationStyle(R.style.pop_Animation);
        historyPop.showAtLocation(btn_file_history, Gravity.CENTER, 0, 0);
        rv_history_meeting = inflate.findViewById(R.id.rv_meeting);
        meetAdapter = new HistoryMeetAdapter(R.layout.item_table_2, presenter.meetings);
        rv_history_meeting.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_history_meeting.setAdapter(meetAdapter);
        meetAdapter.setOnItemClickListener((adapter, view, position) -> {
            int id = presenter.meetings.get(position).getId();
            meetAdapter.setSelected(id);
            presenter.setCurrentHistoryDirId(0);
            presenter.switchMeeting(id);
        });
        rv_history_dir = inflate.findViewById(R.id.rv_dir);
        rv_history_file = inflate.findViewById(R.id.rv_file);
        inflate.findViewById(R.id.btn_confirm_import).setOnClickListener(v -> {
            if (historyDirAdapter != null) {
                InterfaceFile.pbui_Item_MeetDirFileDetailInfo selected = historyFileAdapter.getSelected();
                if (selected != null) {
                    presenter.exit();
                    jni.addFile2Dir(dirId, selected);
                    historyPop.dismiss();
                } else {
                    ToastUtil.show(R.string.please_choose_file_first);
                }
            }
        });
        inflate.findViewById(R.id.btn_exit).setOnClickListener(v -> {
            historyPop.dismiss();
        });
        historyPop.setOnDismissListener(() -> {
            presenter.setCurrentHistoryDirId(0);
            presenter.exit();
        });
    }

    @Override
    public void updateMeetingRv(List<InterfaceMeet.pbui_Item_MeetMeetInfo> meetings) {
        if (historyPop != null && historyPop.isShowing()) {
            meetAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateHistoryDirFileRv(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> dirFiles) {
        if (historyPop != null && historyPop.isShowing()) {
            historyFileAdapter = new FileSortAdapter(R.layout.item_sort_file, dirFiles);
            rv_history_file.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_history_file.setAdapter(historyFileAdapter);
            historyFileAdapter.addChildClickViewIds(R.id.item_btn_open);
            historyFileAdapter.setOnItemClickListener((adapter, view, position) -> {
                int mediaid = dirFiles.get(position).getMediaid();
                historyFileAdapter.setSelectedId(mediaid);
            });
        }
    }

    private void showSortDirPop(List<InterfaceFile.pbui_Item_MeetDirDetailInfo> sortDirData) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_sort_dir, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showOtherMeetFile fragment的大小 width=" + width + ",height=" + height);
        sortDirPop = new PopupWindow(inflate, width * 2 / 3, height * 2 / 3);
        sortDirPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        sortDirPop.setTouchable(true);
        // true:设置触摸外面时消失
        sortDirPop.setOutsideTouchable(true);
        sortDirPop.setFocusable(true);
        sortDirPop.setAnimationStyle(R.style.pop_Animation);
        sortDirPop.showAtLocation(btn_file_history, Gravity.CENTER, 0, 0);
        RecyclerView rv_sort_dir = inflate.findViewById(R.id.rv_sort_dir);
        if (sortDirAdapter == null) {
            sortDirAdapter = new DirAdapter(R.layout.item_admin_file_dir, sortDirData);
            rv_sort_dir.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_sort_dir.setAdapter(sortDirAdapter);
            sortDirAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    sortDirAdapter.setSelected(sortDirData.get(position).getId());
                }
            });
        } else {
            sortDirAdapter.notifyDataSetChanged();
        }
        inflate.findViewById(R.id.btn_move_up).setOnClickListener(v -> {
            InterfaceFile.pbui_Item_MeetDirDetailInfo selected = sortDirAdapter.getSelected();
            if (selected == null) {
                ToastUtil.show(R.string.please_choose_dir_first);
                return;
            }
            if (selected.getId() == 1 || selected.getId() == 2) {
                //不能移动共享文件目录和批注文件目录
                ToastUtil.show(R.string.cannot_move_this_directory);
                return;
            }
            int index = 0;
            for (int i = 0; i < sortDirData.size(); i++) {
                if (selected.getId() == sortDirData.get(i).getId()) {
                    index = i;
                    break;
                }
            }
            if (index == 0) {
                //要上移的目标已经是第一项，则移动到最下方
                sortDirData.remove(index);
                sortDirData.add(selected);
            } else {
                int preDirId = sortDirData.get(index - 1).getId();
                if (preDirId == 1 || preDirId == 2) {
                    //上一个已经是共享目录和批注目录了，所以不能再移动了
                    ToastUtil.show(R.string.cannot_move_up);
                } else {
                    Collections.swap(sortDirData, index, index - 1);
                }
            }
            sortDirAdapter.notifyDataSetChanged();
        });
        inflate.findViewById(R.id.btn_move_down).setOnClickListener(v -> {
            InterfaceFile.pbui_Item_MeetDirDetailInfo selected = sortDirAdapter.getSelected();
            if (selected == null) {
                ToastUtil.show(R.string.please_choose_dir_first);
                return;
            }
            if (selected.getId() == 1 || selected.getId() == 2) {
                //不能移动共享文件目录和批注文件目录
                ToastUtil.show(R.string.cannot_move_this_directory);
                return;
            }
            int index = 0;
            for (int i = 0; i < sortDirData.size(); i++) {
                if (selected.getId() == sortDirData.get(i).getId()) {
                    index = i;
                    break;
                }
            }
            if (index == sortDirData.size() - 1) {
                //要下移的目标已经是最后一项，则进行移动到最上方
                ToastUtil.show(R.string.cannot_move_down);
            } else {
                Collections.swap(sortDirData, index, index + 1);
            }
            sortDirAdapter.notifyDataSetChanged();
        });
        inflate.findViewById(R.id.btn_save).setOnClickListener(v -> {
            List<InterfaceFile.pbui_Item_MeetingDirPosItem> temps = new ArrayList<>();
            for (int i = 0; i < sortDirData.size(); i++) {
                InterfaceFile.pbui_Item_MeetDirDetailInfo item = sortDirData.get(i);
                InterfaceFile.pbui_Item_MeetingDirPosItem build = InterfaceFile.pbui_Item_MeetingDirPosItem.newBuilder()
                        .setDirid(item.getId())
                        .setPos(i)
                        .build();
                temps.add(build);
            }
            jni.modifyMeetDirSort(temps);
            sortDirPop.dismiss();
        });
        inflate.findViewById(R.id.btn_exit).setOnClickListener(v -> {
            sortDirPop.dismiss();
        });
    }

    private void showSortFilePop(List<InterfaceFile.pbui_Item_MeetDirDetailInfo> dirInfos) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_sort_file, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showDirPermissionPop fragment的大小 width=" + width + ",height=" + height);
        sortFilePop = new PopupWindow(inflate, width, height);
        sortFilePop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        sortFilePop.setTouchable(true);
        // true:设置触摸外面时消失
        sortFilePop.setOutsideTouchable(true);
        sortFilePop.setFocusable(true);
        sortFilePop.setAnimationStyle(R.style.pop_Animation);
        sortFilePop.showAtLocation(btn_file_sort, Gravity.END | Gravity.BOTTOM, 0, 0);
        rv_sort_file_dir = inflate.findViewById(R.id.rv_sort_file_dir);
        rv_sort_file = inflate.findViewById(R.id.rv_sort_file);
        sortFiledirAdapter = new DirAdapter(R.layout.item_file_sort_dir, dirInfos);
        rv_sort_file_dir.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_sort_file_dir.setAdapter(sortFiledirAdapter);
        sortFilePop.setOnDismissListener(() -> {
            //PopupWindow隐藏后重置选中的目录id
            presenter.setCurrentSortFileDirId(0);
            fileSortAdapter = null;
        });
        sortFiledirAdapter.setOnItemClickListener((adapter, view, position) -> {
            int dirId = dirInfos.get(position).getId();
            sortFiledirAdapter.setSelected(dirId);
            presenter.setCurrentSortFileDirId(dirId);
            presenter.queryFileByDir(dirId);
        });
        inflate.findViewById(R.id.btn_move_up).setOnClickListener(v -> {
            if (fileSortAdapter != null) {
                InterfaceFile.pbui_Item_MeetDirFileDetailInfo selected = fileSortAdapter.getSelected();
                if (selected == null) {
                    ToastUtil.show(R.string.please_choose_file_first);
                    return;
                }
                int index = fileSortAdapter.getSelectedIndex();
                if (index == -1) {
                    return;
                }
                List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> sortDirFile = presenter.getSortDirFile();
                if (index == 0) {
                    //当前选中的文件已经在第一个，则要将其放在最后
                    sortDirFile.remove(index);
                    sortDirFile.add(selected);
                } else {
                    Collections.swap(sortDirFile, index, index - 1);
                }
                fileSortAdapter.notifyDataSetChanged();
            }
        });
        inflate.findViewById(R.id.btn_move_down).setOnClickListener(v -> {
            if (fileSortAdapter != null) {
                InterfaceFile.pbui_Item_MeetDirFileDetailInfo selected = fileSortAdapter.getSelected();
                if (selected == null) {
                    ToastUtil.show(R.string.please_choose_file_first);
                    return;
                }
                int index = fileSortAdapter.getSelectedIndex();
                if (index == -1) {
                    return;
                }
                List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> sortDirFile = presenter.getSortDirFile();
                if (index == sortDirFile.size() - 1) {
                    //当前选中的文件已经在第一个，则要将其放在最后
                    List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> temps = new ArrayList<>();
                    sortDirFile.remove(index);
                    temps.add(selected);
                    temps.addAll(sortDirFile);
                    sortDirFile.clear();
                    sortDirFile.addAll(temps);
                    temps.clear();
                } else {
                    Collections.swap(sortDirFile, index, index + 1);
                }
                fileSortAdapter.notifyDataSetChanged();
            }
        });
        inflate.findViewById(R.id.btn_save).setOnClickListener(v -> {
            if (presenter.modifyMeetDirFileSort()) {
                sortFilePop.dismiss();
            }
        });
        inflate.findViewById(R.id.btn_exit).setOnClickListener(v -> {
            sortFilePop.dismiss();
        });
    }

    @Override
    public void updateSortFileRv(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> sortDirFiles) {
        if (sortFilePop != null && sortFilePop.isShowing()) {
            LogUtil.i(TAG, "updateSortFileRv ");
            if (fileSortAdapter == null) {
                fileSortAdapter = new FileSortAdapter(R.layout.item_sort_file, sortDirFiles);
                rv_sort_file.setLayoutManager(new LinearLayoutManager(getContext()));
                rv_sort_file.setAdapter(fileSortAdapter);
                fileSortAdapter.addChildClickViewIds(R.id.item_btn_open);
                fileSortAdapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                        fileSortAdapter.setSelectedId(sortDirFiles.get(position).getMediaid());
                    }
                });
            } else {
                fileSortAdapter.notifyDataSetChanged();
            }
        }
    }

    private void deleteFile() {
        if (fileAdapter != null && fileAdapter.getLastCheckFile() != null) {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo lastCheckFile = fileAdapter.getLastCheckFile();
            presenter.deleteMeetDirFile(lastCheckFile);
        } else {
            ToastUtil.show(R.string.please_choose_file_first);
        }
    }

    private void modifyFile() {
        String fileName = edt_file_name.getText().toString().trim();
        if (fileName.isEmpty()) {
            ToastUtil.show(R.string.please_choose_file_first);
            return;
        }
        if (fileAdapter != null && fileAdapter.getLastCheckFile() != null) {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo lastCheckFile = fileAdapter.getLastCheckFile();
            InterfaceFile.pbui_Item_ModMeetDirFile build = InterfaceFile.pbui_Item_ModMeetDirFile.newBuilder()
                    .setMediaid(lastCheckFile.getMediaid())
                    .setName(s2b(fileName))
                    .setAttrib(lastCheckFile.getAttrib())
                    .build();
            presenter.modifyMeetDirFileName(build);
        } else {
            ToastUtil.show(R.string.please_choose_file_first);
        }
    }

    private void chooseLocalFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, LOCAL_FILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == LOCAL_FILE_REQUEST_CODE) {
            Uri uri = data.getData();
            String filePath = UriUtil.getFilePath(getContext(), uri);
            if (filePath != null) {
                int dirId = dirAdapter.getSelected().getId();
                File file = new File(filePath);
                JniHandler.getInstance().uploadFile(InterfaceMacro.Pb_Upload_Flag.Pb_MEET_UPLOADFLAG_ONLYENDCALLBACK_VALUE,
                        dirId, 0, file.getName(), filePath,
                        0, Constant.UPLOAD_CHOOSE_FILE);
            }
        }
    }

    /**
     * 目录权限
     */
    private void showDirPermissionPop(List<InterfaceFile.pbui_Item_MeetDirDetailInfo> dirInfos) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_dir_permission, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showDirPermissionPop fragment的大小 width=" + width + ",height=" + height);
        dirPermissionPop = new PopupWindow(inflate, width, height);
        dirPermissionPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        dirPermissionPop.setTouchable(true);
        // true:设置触摸外面时消失
        dirPermissionPop.setOutsideTouchable(true);
        dirPermissionPop.setFocusable(true);
        dirPermissionPop.setAnimationStyle(R.style.pop_Animation);
        dirPermissionPop.showAtLocation(btn_dir_permission, Gravity.END | Gravity.BOTTOM, 0, 0);
        dirPermissionPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                dirPermissionMemberAdapter = null;
            }
        });
        rv_permission_dir = inflate.findViewById(R.id.rv_permission_dir);
        dirNameAdapter = new DirNameAdapter(R.layout.item_dir_permission, dirInfos);
        rv_permission_dir.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_permission_dir.setAdapter(dirNameAdapter);
        dirNameAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                int dirId = dirInfos.get(position).getId();
                dirNameAdapter.setSelected(dirId);
                presenter.queryDirPermission(dirId);
            }
        });
        cbAll = inflate.findViewById(R.id.item_view_1);
        rv_dir_permission_member = inflate.findViewById(R.id.rv_dir_permission_member);
        cbAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbAll.setChecked(cbAll.isChecked());
                if (dirPermissionMemberAdapter != null) {
                    dirPermissionMemberAdapter.setCheckAll(cbAll.isChecked());
                } else {
                    ToastUtil.show(R.string.please_choose_dir_first);
                }
            }
        });
        inflate.findViewById(R.id.btn_save).setOnClickListener(v -> {
            if (dirPermissionMemberAdapter != null) {
                presenter.saveDirPermission(dirPermissionMemberAdapter.getChecks());
            } else {
                ToastUtil.show(R.string.please_choose_dir_first);
            }
        });
        inflate.findViewById(R.id.btn_back).setOnClickListener(v -> {
            dirPermissionPop.dismiss();
        });
    }

    @Override
    public void updateMemberPermission(List<MemberDirPermissionBean> memberDirPermissionBeans) {
        if (dirPermissionPop != null && dirPermissionPop.isShowing()) {
            if (dirPermissionMemberAdapter == null) {
                dirPermissionMemberAdapter = new DirPermissionMemberAdapter(R.layout.item_dir_permission_member, memberDirPermissionBeans);
                rv_dir_permission_member.setLayoutManager(new LinearLayoutManager(getContext()));
                rv_dir_permission_member.setAdapter(dirPermissionMemberAdapter);
                dirPermissionMemberAdapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                        dirPermissionMemberAdapter.setCheck(memberDirPermissionBeans.get(position).getMember().getPersonid());
                        cbAll.setChecked(dirPermissionMemberAdapter.isCheckAll());
                    }
                });
            } else {
                dirPermissionMemberAdapter.notifyDataSetChanged();
            }
        }
    }

    private void createDir() {
        String dirName = edt_dir_name.getText().toString().trim();
        if (dirName.isEmpty()) {
            ToastUtil.show(R.string.please_enter_dir_name);
            return;
        }
        InterfaceFile.pbui_Item_MeetDirDetailInfo build = InterfaceFile.pbui_Item_MeetDirDetailInfo.newBuilder()
                .setName(s2b(dirName))
                .build();
        presenter.createDir(build);
    }

    private void modifyDir() {
        String dirName = edt_dir_name.getText().toString().trim();
        if (dirName.isEmpty()) {
            ToastUtil.show(R.string.please_enter_dir_name);
            return;
        }
        if (dirAdapter != null && dirAdapter.getSelected() != null) {
            InterfaceFile.pbui_Item_MeetDirDetailInfo selected = dirAdapter.getSelected();
            InterfaceFile.pbui_Item_MeetDirDetailInfo build = InterfaceFile.pbui_Item_MeetDirDetailInfo.newBuilder()
                    .setName(s2b(dirName))
                    .setId(selected.getId())
                    .setParentid(selected.getParentid())
                    .setFilenum(selected.getFilenum())
                    .build();
            presenter.modifyDir(build);
        } else {
            ToastUtil.show(R.string.please_choose_dir_first);
        }
    }

    private void deleteDir() {
        if (dirAdapter != null && dirAdapter.getSelected() != null) {
            InterfaceFile.pbui_Item_MeetDirDetailInfo selected = dirAdapter.getSelected();
            presenter.deleteDir(selected);
        } else {
            ToastUtil.show(R.string.please_choose_dir_first);
        }
    }
}
