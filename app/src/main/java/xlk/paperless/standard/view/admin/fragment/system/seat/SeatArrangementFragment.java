package xlk.paperless.standard.view.admin.fragment.system.seat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.blankj.utilcode.util.UriUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.mogujie.tt.protobuf.InterfaceFile;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceRoom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.bean.SeatBean;
import xlk.paperless.standard.ui.CustomSeatView;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.FileUtil;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;

/**
 * @author Created by xlk on 2020/11/3.
 * @desc  座位排布
 */
public class SeatArrangementFragment extends BaseFragment implements SeatArrangementInterface, View.OnClickListener {
    private RecyclerView rv_room;
    private CustomSeatView seat_view;
    private CheckBox cb_show_pic;
    private SeatArrangementPresenter presenter;
    private RoomAdapter roomAdapter;
    private List<SeatBean> seatBeans = new ArrayList<>();
    private PopupWindow picPop;
    private RecyclerView rv_pic;
    private BgPictureAdapter pictureAdapter;
    private final int PICTURE_REQUEST_CODE = 1;
    /**
     * 当前会议室的底图图片路径 和媒体id
     */
    private String currentRoomBgFilePath = "";
    private int currentMediaId = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_seat_arrangement, container, false);
        initView(inflate);
        presenter = new SeatArrangementPresenter(this);
        presenter.queryRoom();
        presenter.queryRoomIcon();
        seat_view.post(() -> seat_view.setViewSize(seat_view.getWidth(), seat_view.getHeight()));
        return inflate;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            presenter.unregister();
        } else {
            presenter.register();
            presenter.queryRoom();
            presenter.queryRoomIcon();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        if (picPop != null && picPop.isShowing()) {
            picPop.dismiss();
        }
    }

    @Override
    public void cleanRoomBg() {
        getActivity().runOnUiThread(() -> {
            LogUtil.i(TAG, "cleanRoomBg 清除会议室底图");
            seat_view.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(seat_view.getViewWidth(), seat_view.getViewHeight());
            seat_view.setLayoutParams(params);
            seat_view.setImgSize(seat_view.getViewWidth(), seat_view.getViewHeight());
            presenter.placeDeviceRankingInfo(getSelectRoomId());
        });
    }

    @Override
    public void updateRoomBg(String filepath, int mediaId) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            currentRoomBgFilePath = filepath;
            currentMediaId = mediaId;
            Drawable drawable = Drawable.createFromPath(filepath);
            seat_view.setBackground(drawable);
            Bitmap bitmap = BitmapFactory.decodeFile(filepath);
            if (bitmap != null) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                seat_view.setLayoutParams(params);
                seat_view.setImgSize(width, height);
                LogUtil.e(TAG, "updateBg 图片宽高 -->" + width + ", " + height);
                presenter.placeDeviceRankingInfo(getSelectRoomId());
                bitmap.recycle();
            }
        });
    }

    private int getSelectRoomId() {
        if (roomAdapter != null) {
            return roomAdapter.getSelectedId();
        }
        return -1;
    }

    @Override
    public void updateRoomRv(List<InterfaceRoom.pbui_Item_MeetRoomDetailInfo> roomData) {
        LogUtil.i(TAG, "updateRoomRv ");
        if (roomAdapter == null) {
            roomAdapter = new RoomAdapter(R.layout.item_arrangement_room, roomData);
            rv_room.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_room.setAdapter(roomAdapter);
            roomAdapter.setOnItemClickListener((adapter, view, position) -> {
                int currentRoomId = roomData.get(position).getRoomid();
                roomAdapter.setSelected(currentRoomId);
                presenter.queryMeetRoomBg(currentRoomId);
            });
        } else {
            roomAdapter.notifyDataSetChanged();
            int selectedId = roomAdapter.getSelectedId();
            if (selectedId != -1) {
                presenter.queryMeetRoomBg(selectedId);
            }
        }
    }

    @Override
    public void updateSeatData(List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatData) {
        LogUtil.i(TAG, "updateSeatData ");
        seatBeans.clear();
        for (int i = 0; i < seatData.size(); i++) {
            InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo info = seatData.get(i);
            SeatBean seatBean = new SeatBean(info.getDevid(), info.getDevname().toStringUtf8(), info.getX(), info.getY(),
                    info.getDirection(), info.getMemberid(), info.getMembername().toStringUtf8(),
                    info.getIssignin(), info.getRole(), info.getFacestate());
            seatBeans.add(seatBean);
        }
        seat_view.addSeat(seatBeans);
    }

    @Override
    public void updatePictureRv() {
        if (pictureAdapter != null) {
            LogUtil.i(TAG, "updatePictureRv ");
            pictureAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateShowIcon(boolean hidePic) {
        LogUtil.i(TAG, "updateShowIcon hidePic=" + hidePic);
        cb_show_pic.setChecked(!hidePic);
        seat_view.setHidePic(hidePic);
    }

    private void initView(View rootView) {
        this.rv_room = (RecyclerView) rootView.findViewById(R.id.rv_room);
        this.seat_view = (CustomSeatView) rootView.findViewById(R.id.seat_view);
        seat_view.setHideMember(true);
        this.cb_show_pic = (CheckBox) rootView.findViewById(R.id.cb_show_pic);
        rootView.findViewById(R.id.cb_show_pic).setOnClickListener(this);
        rootView.findViewById(R.id.btn_auto_sort).setOnClickListener(this);
        rootView.findViewById(R.id.btn_up).setOnClickListener(this);
        rootView.findViewById(R.id.btn_down).setOnClickListener(this);
        rootView.findViewById(R.id.btn_left).setOnClickListener(this);
        rootView.findViewById(R.id.btn_right).setOnClickListener(this);
        rootView.findViewById(R.id.btn_preview).setOnClickListener(this);
        rootView.findViewById(R.id.btn_save_picture).setOnClickListener(this);
        rootView.findViewById(R.id.btn_align_left).setOnClickListener(this);
        rootView.findViewById(R.id.btn_align_bottom).setOnClickListener(this);
        rootView.findViewById(R.id.btn_save_position).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cb_show_pic:
                cb_show_pic.setChecked(cb_show_pic.isChecked());
                presenter.setHideIcon(!cb_show_pic.isChecked());
                break;
            case R.id.btn_auto_sort:
                seat_view.autoSort();
                break;
            case R.id.btn_up:
                seat_view.updateDirection(CustomSeatView.TOP_DIRECTION);
                break;
            case R.id.btn_down:
                seat_view.updateDirection(CustomSeatView.DOWN_DIRECTION);
                break;
            case R.id.btn_left:
                seat_view.updateDirection(CustomSeatView.LEFT_DIRECTION);
                break;
            case R.id.btn_right:
                seat_view.updateDirection(CustomSeatView.RIGHT_DIRECTION);
                break;
            case R.id.btn_preview: {
                if (getSelectRoomId() != -1) {
                    presenter.queryBgPicture();
                    showRoomBgPicture();
                } else {
                    ToastUtil.show(R.string.please_choose_room_first);
                }
                break;
            }
            case R.id.btn_save_picture:
                if (getSelectRoomId() != -1) {
                    jni.setRoomPicture(getSelectRoomId(), currentMediaId, ConvertUtil.s2b(""), 0);
                } else {
                    ToastUtil.show(R.string.please_choose_room_first);
                }
                break;
            case R.id.btn_align_left:
                seat_view.alignLeft();
                break;
            case R.id.btn_align_bottom:
                seat_view.alignBottom();
                break;
            case R.id.btn_save_position:
                if (getSelectRoomId() == -1) {
                    ToastUtil.show(R.string.please_choose_room_first);
                    return;
                }
                List<SeatBean> seatBean = seat_view.getSeatBean();
                List<InterfaceRoom.pbui_Item_MeetRoomDevPosInfo> devs = new ArrayList<>();
                for (int i = 0; i < seatBean.size(); i++) {
                    SeatBean bean = seatBean.get(i);
                    InterfaceRoom.pbui_Item_MeetRoomDevPosInfo build = InterfaceRoom.pbui_Item_MeetRoomDevPosInfo.newBuilder()
                            .setDevid(bean.getDevId())
                            .setX(bean.getX())
                            .setY(bean.getY())
                            .setDirection(bean.getDirection())
                            .build();
                    devs.add(build);
                }
                jni.setPlaceDeviceRankInfo(getSelectRoomId(), devs);
                break;
            default:
                break;
        }
    }

    private void showRoomBgPicture() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pop_bg_picture, null);
        View admin_fl = getActivity().findViewById(R.id.admin_fl);
        int width = admin_fl.getWidth();
        int height = admin_fl.getHeight();
        LogUtil.i(TAG, "showSort fragment的大小 width=" + width + ",height=" + height);
        picPop = new PopupWindow(inflate, width, height);
        picPop.setBackgroundDrawable(new BitmapDrawable());
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        picPop.setTouchable(true);
        // true:设置触摸外面时消失
        picPop.setOutsideTouchable(true);
        picPop.setFocusable(true);
        picPop.setAnimationStyle(R.style.pop_Animation);
        picPop.showAtLocation(seat_view, Gravity.BOTTOM | Gravity.END, 0, 0);
        rv_pic = inflate.findViewById(R.id.rv_pic);
        pictureAdapter = new BgPictureAdapter(R.layout.item_bg_picture, presenter.pictureData);
        rv_pic.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_pic.setAdapter(pictureAdapter);
        pictureAdapter.setOnItemClickListener((adapter, view, position) -> pictureAdapter.setSelected(presenter.pictureData.get(position).getMediaid()));
        inflate.findViewById(R.id.btn_increase).setOnClickListener(v -> chooseLocalFile(PICTURE_REQUEST_CODE));
        inflate.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo selectPic = pictureAdapter.getSelectPic();
            if (selectPic != null) {
                jni.deleteMeetDirFile(0, selectPic);
            } else {
                ToastUtil.show(R.string.please_choose_file_first);
            }
        });
        inflate.findViewById(R.id.btn_determine).setOnClickListener(v -> {
            InterfaceFile.pbui_Item_MeetDirFileDetailInfo selectPic = pictureAdapter.getSelectPic();
            if (selectPic != null) {
                FileUtil.createDir(Constant.DIR_PICTURE);
                jni.creationFileDownload(Constant.DIR_PICTURE + Constant.ROOM_BG_PNG_TAG + ".png",
                        selectPic.getMediaid(), 1, 0, Constant.ROOM_BG_PNG_TAG);
                picPop.dismiss();
            } else {
                ToastUtil.show(R.string.please_choose_file_first);
            }
        });
        inflate.findViewById(R.id.btn_cancel).setOnClickListener(v -> picPop.dismiss());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICTURE_REQUEST_CODE) {
            Uri uri = data.getData();
            File file = UriUtils.uri2File(uri);
            if (file != null) {
                if (file.getName().endsWith(".png")) {
                    String absolutePath = file.getAbsolutePath();
                    LogUtil.i(TAG, "onActivityResult 上传新底图图片=" + absolutePath);
                    jni.uploadFile(0, 0, InterfaceMacro.Pb_MeetFileAttrib.Pb_MEETFILE_ATTRIB_BACKGROUND_VALUE,
                            file.getName(), absolutePath, 0, Constant.UPLOAD_BACKGROUND_IMAGE);
                } else {
                    ToastUtil.show(R.string.please_choose_png);
                }
            }
        }
    }
}
