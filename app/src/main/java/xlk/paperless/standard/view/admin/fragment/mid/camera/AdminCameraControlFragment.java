package xlk.paperless.standard.view.admin.fragment.mid.camera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceVideo;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.MeetLiveVideoAdapter;
import xlk.paperless.standard.adapter.WmProjectorAdapter;
import xlk.paperless.standard.adapter.WmScreenMemberAdapter;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.VideoDev;
import xlk.paperless.standard.ui.CustomBaseViewHolder;
import xlk.paperless.standard.ui.CustomInterface.ViewClickListener;
import xlk.paperless.standard.ui.video.CustomVideoView;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.PopUtil;
import xlk.paperless.standard.util.ToastUtil;

import static xlk.paperless.standard.data.Constant.RESOURCE_0;
import static xlk.paperless.standard.data.Constant.RESOURCE_1;
import static xlk.paperless.standard.data.Constant.RESOURCE_2;
import static xlk.paperless.standard.data.Constant.RESOURCE_3;
import static xlk.paperless.standard.data.Constant.RESOURCE_4;
import static xlk.paperless.standard.data.Constant.permission_code_screen;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc
 */
public class AdminCameraControlFragment extends BaseFragment implements AdminCameraControlInterface, View.OnClickListener, ViewClickListener {
    private CustomVideoView custom_video_view;
    private RecyclerView rv_member;
    private Button btn_watch_video;
    private Button btn_stop_watch;
    private Button btn_start_screen;
    private Button btn_end_screen;
    private Button btn_start_pro;
    private Button btn_end_pro;
    private AdminCameraControlPresenter presenter;
    List<Integer> ids = new ArrayList<>();
    private int width, height;
    private WmScreenMemberAdapter memberAdapter;
    private WmProjectorAdapter projectorAdapter;
    private MeetLiveVideoAdapter adapter;
    private PopupWindow proPop;
    private PopupWindow screenPop;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_camera_control, container, false);
        initView(inflate);
        presenter = new AdminCameraControlPresenter(this);
        ids.add(RESOURCE_1);
        ids.add(RESOURCE_2);
        ids.add(RESOURCE_3);
        ids.add(RESOURCE_4);
        initAdapter();
        custom_video_view.post(new Runnable() {
            @Override
            public void run() {
                width = custom_video_view.getWidth();
                height = custom_video_view.getHeight();
                start();
            }
        });
        return inflate;
    }

    private void initAdapter() {
        memberAdapter = new WmScreenMemberAdapter(R.layout.item_single_button, presenter.onLineMember);
        projectorAdapter = new WmProjectorAdapter(R.layout.item_single_button, presenter.onLineProjectors);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            stop();
        } else {
            start();
        }
    }

    private void stop() {
        presenter.stopResource(ids);
        custom_video_view.clearAll();
        presenter.releaseVideoRes();
        presenter.unregister();
    }

    private void start() {
        presenter.initVideoRes(width, height);
        custom_video_view.createView(ids);
        presenter.register();
        presenter.queryDeviceInfo();
    }

    public void initView(View rootView) {
        this.custom_video_view = (CustomVideoView) rootView.findViewById(R.id.custom_video_view);
        this.rv_member = (RecyclerView) rootView.findViewById(R.id.rv_member);
        this.btn_watch_video = (Button) rootView.findViewById(R.id.btn_watch_video);
        this.btn_stop_watch = (Button) rootView.findViewById(R.id.btn_stop_watch);
        this.btn_start_screen = (Button) rootView.findViewById(R.id.btn_start_screen);
        this.btn_end_screen = (Button) rootView.findViewById(R.id.btn_end_screen);
        this.btn_start_pro = (Button) rootView.findViewById(R.id.btn_start_pro);
        this.btn_end_pro = (Button) rootView.findViewById(R.id.btn_end_pro);

        custom_video_view.setViewClickListener(this);
        btn_watch_video.setOnClickListener(this);
        btn_stop_watch.setOnClickListener(this);
        btn_start_screen.setOnClickListener(this);
        btn_end_screen.setOnClickListener(this);
        btn_start_pro.setOnClickListener(this);
        btn_end_pro.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_watch_video: {
                if (adapter != null) {
                    VideoDev videoDev = adapter.getSelected();
                    if (videoDev != null) {
                        int selectResId = custom_video_view.getSelectResId();
                        if (selectResId != -1) {
//                            List<Integer> resids = new ArrayList();
//                            resids.add(selectResId);
//                            presenter.stopResource(resids);
                            presenter.watch(videoDev, selectResId);
//                            new Timer().schedule(new TimerTask() {
//                                @Override
//                                public void run() {
//                                    presenter.watch(videoDev, selectResId);
//                                }
//                            }, 500);
                        } else {
                            ToastUtil.show(R.string.please_choose_view);
                        }
                    } else {
                        ToastUtil.show(R.string.please_choose_video_show);
                    }
                }
                break;
            }
            case R.id.btn_stop_watch: {
                int selectResId = custom_video_view.getSelectResId();
                if (selectResId != -1) {
                    List<Integer> ids = new ArrayList<>();
                    ids.add(selectResId);
                    presenter.stopResource(ids);
                } else {
                    ToastUtil.show(R.string.please_choose_stop_view);
                }
                break;
            }
            case R.id.btn_start_screen: {
                if (adapter != null && adapter.getSelected() != null) {
//                    if (Constant.hasPermission(permission_code_screen)) {
                    showScreenPop(true, adapter.getSelected());
//                    } else {
//                        ToastUtil.show(R.string.err_NoPermission);
//                    }
                }
                break;
            }
            case R.id.btn_end_screen: {
                if (adapter != null && adapter.getSelected() != null) {
                    showScreenPop(false, adapter.getSelected());
                }
                break;
            }
            case R.id.btn_start_pro: {
                if (adapter != null && adapter.getSelected() != null) {
                    showProPop(true, adapter.getSelected());
                }
                break;
            }
            case R.id.btn_end_pro: {
                if (adapter != null && adapter.getSelected() != null) {
                    showProPop(false, adapter.getSelected());
                }
                break;
            }
            default:
                break;
        }
    }

    private void showProPop(boolean isStart, VideoDev videoDev) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.wm_pro_view, null);
        proPop = PopUtil.create(inflate, btn_start_pro);
        CustomBaseViewHolder.ProViewHolder holder = new CustomBaseViewHolder.ProViewHolder(inflate);
        proHolderEvent(holder, isStart, videoDev);
    }

    private void proHolderEvent(CustomBaseViewHolder.ProViewHolder holder, boolean isStart, VideoDev videoDev) {
        holder.wm_pro_mandatory.setVisibility(isStart ? View.VISIBLE : View.INVISIBLE);
        holder.wm_pro_title.setText(isStart ? getContext().getString(R.string.launch_pro_title) : getContext().getString(R.string.stop_pro_title));
        holder.wm_pro_launch_pro.setText(isStart ? getContext().getString(R.string.launch_pro) : getContext().getString(R.string.stop_pro));
        holder.wm_pro_rv.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        holder.wm_pro_rv.setAdapter(projectorAdapter);
        projectorAdapter.setOnItemClickListener((adapter, view, position) -> {
            projectorAdapter.choose(presenter.onLineProjectors.get(position).getDevcieid());
            holder.wm_pro_all.setChecked(projectorAdapter.isChooseAll());
        });
        holder.wm_pro_all.setOnClickListener(v -> {
            boolean checked = holder.wm_pro_all.isChecked();
            holder.wm_pro_all.setChecked(checked);
            projectorAdapter.setChooseAll(checked);
        });
        holder.wm_pro_cancel.setOnClickListener(v -> proPop.dismiss());
        holder.wm_pro_launch_pro.setOnClickListener(v -> {
            List<Integer> ids = projectorAdapter.getChooseIds();
            if (ids.isEmpty()) {
                ToastUtil.show(getContext().getString(R.string.please_choose_projector_first));
                return;
            }
            boolean checked = holder.wm_pro_full.isChecked();
            List<Integer> res = new ArrayList<>();
            if (checked) {
                res.add(RESOURCE_0);
            } else {
                if (holder.wm_pro_flow1.isChecked()) res.add(RESOURCE_1);
                if (holder.wm_pro_flow2.isChecked()) res.add(RESOURCE_2);
                if (holder.wm_pro_flow3.isChecked()) res.add(RESOURCE_3);
                if (holder.wm_pro_flow4.isChecked()) res.add(RESOURCE_4);
            }
            if (res.isEmpty()) {
                ToastUtil.show(R.string.please_choose_res_first);
                return;
            }
            int deviceid = videoDev.getVideoDetailInfo().getDeviceid();
            int subid = videoDev.getVideoDetailInfo().getSubid();
            if (isStart) {//发起投影
                boolean isMandatory = holder.wm_pro_mandatory.isChecked();
                int triggeruserval = isMandatory ? InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE
                        : InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_ZERO_VALUE;
                JniHandler.getInstance().streamPlay(deviceid, subid, triggeruserval, res, ids);
            } else {//结束投影
                JniHandler.getInstance().stopResourceOperate(res, ids);
            }
            proPop.dismiss();
        });
    }

    private void showScreenPop(boolean isStart, VideoDev videoDev) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.wm_screen_view, null);
        screenPop = PopUtil.create(inflate,   btn_start_screen);
        CustomBaseViewHolder.ScreenViewHolder holder = new CustomBaseViewHolder.ScreenViewHolder(inflate);
        screenHolderEvent(holder, isStart, videoDev);
    }

    private void screenHolderEvent(CustomBaseViewHolder.ScreenViewHolder holder, boolean isStart, VideoDev videoDev) {
        holder.wm_screen_mandatory.setVisibility(isStart ? View.VISIBLE : View.INVISIBLE);
        if (isStart) {
            holder.wm_screen_launch.setText(getContext().getString(R.string.launch_screen));
            holder.wm_screen_title.setText(getContext().getString(R.string.launch_screen_title));
        } else {
            holder.wm_screen_launch.setText(getContext().getString(R.string.stop_screen));
            holder.wm_screen_title.setText(getContext().getString(R.string.stop_screen_title));
        }
        holder.wm_screen_rv_attendee.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        holder.wm_screen_rv_attendee.setAdapter(memberAdapter);
        memberAdapter.setOnItemClickListener((adapter, view, position) -> {
            memberAdapter.choose(presenter.onLineMember.get(position).getDeviceDetailInfo().getDevcieid());
            holder.wm_screen_cb_attendee.setChecked(memberAdapter.isChooseAll());
        });
        holder.wm_screen_cb_attendee.setOnClickListener(v -> {
            boolean checked = holder.wm_screen_cb_attendee.isChecked();
            holder.wm_screen_cb_attendee.setChecked(checked);
            memberAdapter.setChooseAll(checked);
        });
        holder.wm_screen_rv_projector.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        holder.wm_screen_rv_projector.setAdapter(projectorAdapter);
        projectorAdapter.setOnItemClickListener((adapter, view, position) -> {
            projectorAdapter.choose(presenter.onLineProjectors.get(position).getDevcieid());
            holder.wm_screen_cb_projector.setChecked(projectorAdapter.isChooseAll());
        });
        holder.wm_screen_cb_projector.setOnClickListener(v -> {
            boolean checked = holder.wm_screen_cb_projector.isChecked();
            holder.wm_screen_cb_projector.setChecked(checked);
            projectorAdapter.setChooseAll(checked);
        });
        holder.wm_screen_cancel.setOnClickListener(v -> screenPop.dismiss());
        //发起/结束同屏
        holder.wm_screen_launch.setOnClickListener(v -> {
            List<Integer> ids = memberAdapter.getChooseIds();
            ids.addAll(projectorAdapter.getChooseIds());
            if (ids.isEmpty()) {
                ToastUtil.show(R.string.err_target_NotNull);
            } else {
                List<Integer> temps = new ArrayList<>();
                temps.add(RESOURCE_0);
                int deviceid = videoDev.getVideoDetailInfo().getDeviceid();
                int subid = videoDev.getVideoDetailInfo().getSubid();
                if (isStart) {//发起同屏
                    int triggeruserval = 0;
                    if (holder.wm_screen_mandatory.isChecked()) {//是否强制同屏
                        triggeruserval = InterfaceMacro.Pb_TriggerUsedef.Pb_EXCEC_USERDEF_FLAG_NOCREATEWINOPER_VALUE;
                    }
                    JniHandler.getInstance().streamPlay(deviceid, subid, triggeruserval, temps, ids);
                } else {//结束同屏
                    JniHandler.getInstance().stopResourceOperate(temps, ids);
                }
                screenPop.dismiss();
            }
        });
    }

    long oneTime, twoTime, threeTime, fourTime;

    @Override
    public void click(int res) {
        switch (res) {
            case RESOURCE_1:
                custom_video_view.setSelectResId(res);
                if (System.currentTimeMillis() - oneTime < 500) {
                    custom_video_view.zoom(res);
                } else {
                    oneTime = System.currentTimeMillis();
                }
                break;
            case RESOURCE_2:
                custom_video_view.setSelectResId(res);
                if (System.currentTimeMillis() - twoTime < 500) {
                    custom_video_view.zoom(res);
                } else {
                    twoTime = System.currentTimeMillis();
                }
                break;
            case RESOURCE_3:
                custom_video_view.setSelectResId(res);
                if (System.currentTimeMillis() - threeTime < 500) {
                    custom_video_view.zoom(res);
                } else {
                    threeTime = System.currentTimeMillis();
                }
                break;
            case RESOURCE_4:
                custom_video_view.setSelectResId(res);
                if (System.currentTimeMillis() - fourTime < 500) {
                    custom_video_view.zoom(res);
                } else {
                    fourTime = System.currentTimeMillis();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void updateRv(List<VideoDev> videoDevs) {
        if (adapter == null) {
            adapter = new MeetLiveVideoAdapter(R.layout.item_meet_video, videoDevs);
            rv_member.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_member.setAdapter(adapter);
            adapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> ad, @NonNull View view, int position) {
                    if (videoDevs.get(position).getDeviceDetailInfo().getNetstate() == 1) {
                        InterfaceVideo.pbui_Item_MeetVideoDetailInfo videoDetailInfo = videoDevs.get(position).getVideoDetailInfo();
                        LogUtil.d(TAG, "onItemClick --> Subid= " + videoDetailInfo.getSubid());
                        adapter.setSelected(videoDetailInfo.getDeviceid(), videoDetailInfo.getId());
                    }
                }
            });
        } else {
            adapter.notifyDataSetChanged();
            adapter.notifySelect();
        }
    }

    @Override
    public void updateDecode(Object[] objs) {
        custom_video_view.setVideoDecode(objs);
    }

    @Override
    public void updateYuv(Object[] objs1) {
        custom_video_view.setYuv(objs1);
    }

    @Override
    public void stopResWork(int resid) {
        custom_video_view.stopResWork(resid);
    }

    @Override
    public void notifyOnLineAdapter() {
        if (memberAdapter != null) {
            memberAdapter.notifyDataSetChanged();
            memberAdapter.notifyChecks();
        }
        if (projectorAdapter != null) {
            projectorAdapter.notifyDataSetChanged();
            projectorAdapter.notifyChecks();
        }
    }
}
