package xlk.paperless.standard.view.admin.fragment.pre.bind;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;

import com.blankj.utilcode.util.UriUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.bean.SeatBean;
import xlk.paperless.standard.ui.CustomSeatView;
import xlk.paperless.standard.util.JxlUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.admin.fragment.pre.member.MemberRoleAdapter;
import xlk.paperless.standard.view.admin.fragment.pre.member.MemberRoleBean;

/**
 * @author Created by xlk on 2020/11/3.
 * @desc
 */
public class SeatBindFragment extends BaseFragment implements SeatBindInterface, View.OnClickListener {

    private SeatBindPresenter presenter;
    private RecyclerView rv_member;
    private CustomSeatView bind_seat_view;
    private BindMemberAdapter memberAdapter;
    private List<SeatBean> seatBeans = new ArrayList<>();
    private PopupWindow memberRolePop;
    private MemberRoleAdapter memberRoleAdapter;
    private RecyclerView rv_member_role;
    private final int REQUEST_CODE_SEAT_XLS = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_seat_bind, container, false);
        initView(inflate);
        presenter = new SeatBindPresenter(this);
        bind_seat_view.setChooseSingle(true);
        bind_seat_view.setCanDrag(true);
        bind_seat_view.setCanDragSeat(false);
        bind_seat_view.post(() -> {
            bind_seat_view.setViewSize(bind_seat_view.getWidth(), bind_seat_view.getHeight());
            presenter.queryMember();
            presenter.queryRoomIcon();
        });
        return inflate;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            presenter.unregister();
        } else {
            presenter.register();
            presenter.queryMember();
            presenter.queryRoomIcon();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        if (memberRolePop != null && memberRolePop.isShowing()) {
            memberRolePop.dismiss();
        }
    }

    private void initView(View rootView) {
        rv_member = (RecyclerView) rootView.findViewById(R.id.rv_member);
        bind_seat_view = (CustomSeatView) rootView.findViewById(R.id.bind_seat_view);
        rootView.findViewById(R.id.btn_member_role).setOnClickListener(this);
        rootView.findViewById(R.id.btn_bind).setOnClickListener(this);
        rootView.findViewById(R.id.btn_unbind).setOnClickListener(this);
        rootView.findViewById(R.id.btn_random_bind).setOnClickListener(this);
        rootView.findViewById(R.id.btn_dismiss).setOnClickListener(this);
        rootView.findViewById(R.id.btn_import).setOnClickListener(this);
        rootView.findViewById(R.id.btn_export).setOnClickListener(this);
    }

    @Override
    public void updateMemberList(List<MemberRoleBean> devSeatInfos) {
        if (memberAdapter == null) {
            memberAdapter = new BindMemberAdapter(R.layout.item_arrangement_room, devSeatInfos);
            rv_member.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_member.setAdapter(memberAdapter);
            memberAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    memberAdapter.setSelected(devSeatInfos.get(position).getMember().getPersonid());
                }
            });
        } else {
            memberAdapter.notifyDataSetChanged();
        }
        if (memberRolePop != null && memberRolePop.isShowing()) {
            LogUtil.i(TAG, "updateMemberRole ");
            memberRoleAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateShowIcon(boolean hideIcon) {
        bind_seat_view.setHidePic(hideIcon);
    }

    @Override
    public void updateRoomBg(String filepath, int mediaId) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            Drawable drawable = Drawable.createFromPath(filepath);
            bind_seat_view.setBackground(drawable);
            Bitmap bitmap = BitmapFactory.decodeFile(filepath);
            if (bitmap != null) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                bind_seat_view.setLayoutParams(params);
                bind_seat_view.setImgSize(width, height);
                LogUtil.e(TAG, "updateBg 图片宽高 -->" + width + ", " + height);
                presenter.queryPlaceRanking();
                bitmap.recycle();
            }
        });
    }

    @Override
    public void updateSeatData(List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatData) {
        seatBeans.clear();
        for (int i = 0; i < seatData.size(); i++) {
            InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo info = seatData.get(i);
            SeatBean seatBean = new SeatBean(info.getDevid(), info.getDevname().toStringUtf8(), info.getX(), info.getY(),
                    info.getDirection(), info.getMemberid(), info.getMembername().toStringUtf8(),
                    info.getIssignin(), info.getRole(), info.getFacestate());
            seatBeans.add(seatBean);
        }
        bind_seat_view.addSeat(seatBeans);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //参会人角色
            case R.id.btn_member_role:
                showMemberRole(presenter.devSeatInfos);
                break;
            //绑定
            case R.id.btn_bind: {
                List<Integer> selectedIds = bind_seat_view.getSelectedIds();
                if (selectedIds.isEmpty()) {
                    ToastUtil.show(R.string.please_choose_seat);
                    break;
                }
                if (selectedIds.size() > 1) {
                    ToastUtil.show(R.string.can_only_choose_one_seat);
                    break;
                }
                int memberId = memberAdapter.getSelectedId();
                if (memberId == -1) {
                    ToastUtil.show(R.string.please_choose_member);
                    break;
                }
                jni.modifyMeetRanking(memberId, InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal_VALUE, selectedIds.get(0));
                break;
            }
            //解除绑定
            case R.id.btn_unbind: {
                List<Integer> selectedIds = bind_seat_view.getSelectedIds();
                if (selectedIds.isEmpty()) {
                    ToastUtil.show(R.string.please_choose_seat);
                    break;
                }
                if (selectedIds.size() > 1) {
                    ToastUtil.show(R.string.can_only_choose_one_seat);
                    break;
                }
                jni.modifyMeetRanking(0, InterfaceMacro.Pb_MeetMemberRole.Pb_role_member_normal_VALUE, selectedIds.get(0));
                break;
            }
            //随机绑定
            case R.id.btn_random_bind:
                presenter.randomBind();
                break;
            //全部解除
            case R.id.btn_dismiss:
                presenter.allDismiss();
                break;
            case R.id.btn_import:
                chooseLocalFile(REQUEST_CODE_SEAT_XLS);
                break;
            case R.id.btn_export:
                if (JxlUtil.exportSeatInfo(presenter.devSeatInfos)) {
                    ToastUtil.show(R.string.export_successful);
                } else {
                    ToastUtil.show(R.string.export_failure);
                }
                break;
            default:
                break;
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
        memberRolePop.showAtLocation(rv_member, Gravity.END | Gravity.BOTTOM, 0, 0);
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
            jni.modifyMeetRanking(selected.getMember().getPersonid(), newRole, selected.getSeat().getDevid());
        });
        inflate.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            memberRolePop.dismiss();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_SEAT_XLS) {
            File file = UriUtils.uri2File(data.getData());
            if (file != null && file.isFile()) {
                if (file.getName().endsWith(".xls")) {
                    List<ReadJxlBean> readJxlBeans = JxlUtil.readSeatInfo(file.getAbsolutePath());
                    presenter.bindSeat(readJxlBeans);
                } else {
                    ToastUtil.show(R.string.please_choose_xls_file);
                }
            }
        }
    }
}
