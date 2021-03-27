package xlk.paperless.standard.view.fragment.annotation;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupWindow;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.MeetAnnotationAdapter;
import xlk.paperless.standard.adapter.MeetDataExportAdapter;
import xlk.paperless.standard.adapter.MeetDataFileAdapter;
import xlk.paperless.standard.adapter.PopPushMemberAdapter;
import xlk.paperless.standard.adapter.PopPushProjectionAdapter;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.DevMember;
import xlk.paperless.standard.data.bean.SeatMember;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.PopUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BaseFragment;

import static xlk.paperless.standard.data.Constant.RESOURCE_0;

/**
 * @author xlk
 * @date 2020/3/13
 * @desc 批注查看
 */
public class MeetAnnotationFragment extends BaseFragment implements View.OnClickListener, IMeetAnnotation {
    private final String TAG = "MeetAnnotationFragment-->";
    private RecyclerView f_annotation_member_rv;
    private Button f_annotation_documentation;
    private Button f_annotation_picture;
    private Button f_annotation_video;
    private Button f_annotation_other;
    private Button f_annotation_push;
    private Button f_annotation_export;
    private RecyclerView f_annotation_file_rv;
    private MeetAnnotationPresenter presenter;
    private MeetAnnotationAdapter memberAdapter;
    private int currentMemberId;
    //    private String currentMemberName = "";
    List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> allFiles = new ArrayList<>();
    List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> currentFiles = new ArrayList<>();
    private MeetDataFileAdapter fileAdapter;
    /**
     * =0,1,2,3 为指定类别 =其它值 表示全部
     */
    private int currentType = -1;
    private PopupWindow pushPop;
    private PopPushMemberAdapter pushMemberAdapter;
    private PopPushProjectionAdapter pushProjectionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_annotation, container, false);
        initView(inflate);
        presenter = new MeetAnnotationPresenter(getContext(), this);
        presenter.queryMember();
        presenter.queryFile();
        return inflate;
    }

    private void initView(View inflate) {
        f_annotation_member_rv = (RecyclerView) inflate.findViewById(R.id.f_annotation_member_rv);
        f_annotation_documentation = (Button) inflate.findViewById(R.id.f_annotation_documentation);
        f_annotation_picture = (Button) inflate.findViewById(R.id.f_annotation_picture);
        f_annotation_video = (Button) inflate.findViewById(R.id.f_annotation_video);
        f_annotation_other = (Button) inflate.findViewById(R.id.f_annotation_other);
        f_annotation_push = (Button) inflate.findViewById(R.id.f_annotation_push);
        f_annotation_export = (Button) inflate.findViewById(R.id.f_annotation_export);
        f_annotation_file_rv = (RecyclerView) inflate.findViewById(R.id.f_annotation_file_rv);

        f_annotation_documentation.setOnClickListener(this);
        f_annotation_picture.setOnClickListener(this);
        f_annotation_video.setOnClickListener(this);
        f_annotation_other.setOnClickListener(this);
        f_annotation_push.setOnClickListener(this);
        f_annotation_export.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.f_annotation_documentation:
                currentType = 0;
                showFile();
                break;
            case R.id.f_annotation_picture:
                currentType = 1;
                showFile();
                break;
            case R.id.f_annotation_video:
                currentType = 2;
                showFile();
                break;
            case R.id.f_annotation_other:
                currentType = 3;
                showFile();
                break;
            case R.id.f_annotation_push:
                if (fileAdapter != null) {
                    int chooseId = fileAdapter.getChooseId();
                    if (chooseId == -1) {
                        ToastUtil.show(R.string.please_choose_push_file);
                    } else {
                        presenter.pushFile(chooseId);
                    }
                }
                break;
            case R.id.f_annotation_export:
                if (Constant.hasPermission(Constant.permission_code_download)) {
                    exportFile();
                } else {
                    ToastUtil.show(R.string.err_NoPermission);
                }
                break;
            default:
                break;
        }
    }

    private void exportFile() {
        if (currentFiles.isEmpty()) {
            ToastUtil.show(R.string.no_export_file);
            return;
        }
        List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> temps = new ArrayList<>(currentFiles);
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_export_file, null);
        PopupWindow exportPop = PopUtil.create(inflate, Values.screen_width / 3 * 2, Values.screen_height / 3 * 2,  f_annotation_export);
        RecyclerView pop_export_rv = inflate.findViewById(R.id.pop_export_rv);
        Button pop_export_download = inflate.findViewById(R.id.pop_export_download);
        Button pop_export_back = inflate.findViewById(R.id.pop_export_back);
        MeetDataExportAdapter exportAdapter = new MeetDataExportAdapter(R.layout.item_meet_data_export, temps);
        pop_export_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        pop_export_rv.setAdapter(exportAdapter);
        exportAdapter.setOnItemClickListener((adapter, view, position) -> {
            long mediaid = temps.get(position).getMediaid();
            LogUtil.d(TAG, "onItemClick -->" + position + ", mediaid= " + mediaid);
            exportAdapter.setChoose(mediaid);
        });
        pop_export_download.setOnClickListener(v -> {
            if (Constant.hasPermission(Constant.permission_code_download)) {
                List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> choosedFile = exportAdapter.getChoosedFile();
                for (InterfaceFile.pbui_Item_MeetDirFileDetailInfo info : choosedFile) {
                    presenter.downloadFile(info);
                }
                exportPop.dismiss();
            } else {
                ToastUtil.show(R.string.err_NoPermission);
            }
        });
        pop_export_back.setOnClickListener(v -> exportPop.dismiss());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public void updateMemberRv(List<SeatMember> seatMembers) {
        if (memberAdapter == null) {
            memberAdapter = new MeetAnnotationAdapter(R.layout.item_chat_member, seatMembers);
            f_annotation_member_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            f_annotation_member_rv.setAdapter(memberAdapter);
            memberAdapter.setOnItemClickListener((adapter, view, position) -> {
                currentType = -1;//设置显示全部
                int currentDevId = seatMembers.get(position).getSeatDetailInfo().getSeatid();
                currentMemberId = seatMembers.get(position).getMemberDetailInfo().getPersonid();
                memberAdapter.setSelect(currentDevId);
                LogUtil.i(TAG, "updateMemberRv currentDevId=" + currentDevId + ",currentMemberId=" + currentMemberId);
                if (presenter.hasPermission(currentDevId)) {
                    showFile();
                } else {
                    clean();
                    presenter.sendAttendRequestPermissions(currentDevId, InterfaceMacro.Pb_MemberPermissionPropertyID.Pb_memperm_postilview_VALUE);
                }
            });
        } else {
            memberAdapter.notifyDataSetChanged();
            memberAdapter.notifySelect();
            if (memberAdapter.getSelectedDevId() == -1) {
                currentMemberId = 0;
            }
        }
    }

    @Override
    public void updateFileRv(List<InterfaceFile.pbui_Item_MeetDirFileDetailInfo> fileDetailInfos) {
        allFiles.clear();
        allFiles.addAll(fileDetailInfos);
//        for (int i = 0; i < allFiles.size(); i++) {
//            InterfaceFile.pbui_Item_MeetDirFileDetailInfo item = allFiles.get(i);
//            LogUtil.i(TAG, "updateFileRv 文件=" + item.getName().toStringUtf8() + ", 上传者=" + item.getUploaderName().toStringUtf8());
//        }
        if (memberAdapter != null) {
            if (currentMemberId != 0) {
                showFile();
            }
        }
    }

    public void showFile() {
        if (currentMemberId == 0) {
            return;
        }
        currentFiles.clear();
        if (!presenter.hasPermission(memberAdapter.getSelectedDevId())) {
            clean();
            return;
        }
        for (int i = 0; i < allFiles.size(); i++) {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo info = allFiles.get(i);
            int uploaderid = info.getUploaderid();
            String uploadName = info.getUploaderName().toStringUtf8();
            if (uploaderid == currentMemberId) {
                switch (currentType) {
                    case 0:
//                        if (Constant.isDocument(info.getMediaid())) {
                        if (FileUtil.isDocumentFile(info.getName().toStringUtf8())) {
                            currentFiles.add(info);
                        }
                        break;
                    case 1:
//                        if (Constant.isPicture(info.getMediaid())) {
                        if (FileUtil.isPictureFile(info.getName().toStringUtf8())) {
                            currentFiles.add(info);
                        }
                        break;
                    case 2:
//                        if (Constant.isVideo(info.getMediaid())) {
                        if (FileUtil.isAudioAndVideoFile(info.getName().toStringUtf8())) {
                            currentFiles.add(info);
                        }
                        break;
                    case 3:
//                        if (Constant.isOther(info.getMediaid())) {
                        if (FileUtil.isOtherFile(info.getName().toStringUtf8())) {
                            currentFiles.add(info);
                        }
                        break;
                    default:
                        currentFiles.add(info);
                        break;
                }
            }
        }
        LogUtil.i(TAG, "showFile currentMemberId=" + currentMemberId + ", currentFiles=" + currentFiles.size());
        if (fileAdapter == null) {
            fileAdapter = new MeetDataFileAdapter(R.layout.item_meet_data_file, currentFiles);
            f_annotation_file_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            f_annotation_file_rv.setAdapter(fileAdapter);
            fileAdapter.addChildClickViewIds(R.id.i_m_d_file_view,R.id.i_m_d_file_download);
            fileAdapter.setOnItemChildClickListener((adapter, view, position) -> {
                if (view.getId() == R.id.i_m_d_file_download) {
                    presenter.downloadFile(currentFiles.get(position));
                } else if (view.getId() == R.id.i_m_d_file_view) {
                    presenter.preViewFile(currentFiles.get(position));
                }
            });
            fileAdapter.setOnItemClickListener((adapter, view, position) ->
                    fileAdapter.setChoose(currentFiles.get(position).getMediaid())
            );
        } else {
            fileAdapter.notifyDataSetChanged();
        }
    }

    private void clean() {
        LogUtil.i(TAG, "clean " + (fileAdapter != null));
        if (fileAdapter != null) {
            currentFiles.clear();
            fileAdapter.notifyDataSetChanged();
            fileAdapter.setChoose(-1);
        }
    }

    @Override
    public void showPushView(List<DevMember> onlineMembers, List<InterfaceDevice.pbui_Item_DeviceDetailInfo> onLineProjectors, int mediaId) {
        if (pushPop != null && pushPop.isShowing()) {
            pushMemberAdapter.notifyDataSetChanged();
            pushMemberAdapter.notifyChecks();
            pushProjectionAdapter.notifyDataSetChanged();
            pushProjectionAdapter.notifyChecks();
        } else {
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_push_view, null);
            pushPop = PopUtil.create(inflate,  f_annotation_push);
            CheckBox pop_push_member_cb = inflate.findViewById(R.id.pop_push_member_cb);
            RecyclerView pop_push_member_rv = inflate.findViewById(R.id.pop_push_member_rv);
            pushMemberAdapter = new PopPushMemberAdapter(R.layout.item_single_button, onlineMembers);
            pop_push_member_rv.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
            pop_push_member_rv.setAdapter(pushMemberAdapter);
            pushMemberAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    pushMemberAdapter.choose(onlineMembers.get(position).getDeviceDetailInfo().getDevcieid());
                    pop_push_member_cb.setChecked(pushMemberAdapter.isChooseAll());
                }
            });
            pop_push_member_cb.setOnClickListener(v -> {
                boolean checked = pop_push_member_cb.isChecked();
                pop_push_member_cb.setChecked(checked);
                pushMemberAdapter.setChooseAll(checked);
            });
            CheckBox pop_push_projection_cb = inflate.findViewById(R.id.pop_push_projection_cb);
            RecyclerView pop_push_projection_rv = inflate.findViewById(R.id.pop_push_projection_rv);
            pushProjectionAdapter = new PopPushProjectionAdapter(R.layout.item_single_button, onLineProjectors);
            pop_push_projection_rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            pop_push_projection_rv.setAdapter(pushProjectionAdapter);
            pushProjectionAdapter.setOnItemClickListener((adapter, view, position) -> {
                pushProjectionAdapter.choose(onlineMembers.get(position).getDeviceDetailInfo().getDevcieid());
                pop_push_projection_cb.setChecked(pushProjectionAdapter.isChooseAll());
            });
            pop_push_projection_cb.setOnClickListener(v -> {
                boolean checked = pop_push_projection_cb.isChecked();
                pop_push_projection_cb.setChecked(checked);
                pushProjectionAdapter.setChooseAll(checked);
            });
            inflate.findViewById(R.id.pop_push_determine).setOnClickListener(v -> {
                List<Integer> devIds = pushMemberAdapter.getDevIds();
                devIds.addAll(pushProjectionAdapter.getDevIds());
                if (!devIds.isEmpty()) {
                    pushPop.dismiss();
                    presenter.mediaPlayOperate(mediaId, devIds, 0, RESOURCE_0, 0, InterfaceMacro.Pb_MeetPlayFlag.Pb_MEDIA_PLAYFLAG_ZERO.getNumber());
                } else {
                    ToastUtil.show(R.string.please_choose_push_target);
                }
            });

            //停止推送
            inflate.findViewById(R.id.pop_push_stop).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<Integer> devIds = pushMemberAdapter.getDevIds();
                    devIds.addAll(pushProjectionAdapter.getDevIds());
                    if (!devIds.isEmpty()) {
                        pushPop.dismiss();
                        List<Integer> temps = new ArrayList<>();
                        temps.add(0);
                        presenter.stopPush(temps, devIds);
                    } else {
                        ToastUtil.show(R.string.please_choose_push_target);
                    }
                }
            });
            inflate.findViewById(R.id.pop_push_cancel).setOnClickListener(v -> {
                pushPop.dismiss();
                pushPop = null;
            });
        }
    }
}
