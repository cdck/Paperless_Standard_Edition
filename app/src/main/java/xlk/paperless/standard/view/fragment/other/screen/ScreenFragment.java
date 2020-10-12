package xlk.paperless.standard.view.fragment.other.screen;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.WmProjectorAdapter;
import xlk.paperless.standard.adapter.WmScreenMemberAdapter;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.DevMember;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.base.BaseFragment;

import static xlk.paperless.standard.data.Constant.resource_0;

/**
 * @author xlk
 * @date 2020/4/8
 * @desc
 */
public class ScreenFragment extends BaseFragment implements IScreen, View.OnClickListener {

    private RecyclerView f_screen_rv_target;
    private RecyclerView f_screen_rv_pro;
    private RecyclerView f_screen_rv_source;
    private CheckBox f_screen_pro_cb;
    private CheckBox f_screen_target_cb;
    private Button f_screen_preview;
    private Button f_screen_stop_preview;
    private CheckBox f_screen_mandatory_cb;
    private Button f_screen_launch;
    private Button f_screen_stop;
    private ScreenPresenter presenter;
    private WmScreenMemberAdapter sourceMemberAdapter;
    private WmScreenMemberAdapter targetMemberAdapter;
    private WmProjectorAdapter projectorAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_screen, container, false);
        initView(inflate);
        presenter = new ScreenPresenter(getContext(), this);
        initAdapter();
        presenter.queryDeviceInfo();
        return inflate;
    }

    private void initView(View inflate) {
        f_screen_rv_target = (RecyclerView) inflate.findViewById(R.id.f_screen_rv_target);
        f_screen_rv_pro = (RecyclerView) inflate.findViewById(R.id.f_screen_rv_pro);
        f_screen_rv_source = (RecyclerView) inflate.findViewById(R.id.f_screen_rv_source);
        f_screen_pro_cb = (CheckBox) inflate.findViewById(R.id.f_screen_pro_cb);
        f_screen_target_cb = (CheckBox) inflate.findViewById(R.id.f_screen_target_cb);
        f_screen_preview = (Button) inflate.findViewById(R.id.f_screen_preview);
        f_screen_stop_preview = (Button) inflate.findViewById(R.id.f_screen_stop_preview);
        f_screen_mandatory_cb = (CheckBox) inflate.findViewById(R.id.f_screen_mandatory_cb);
        f_screen_launch = (Button) inflate.findViewById(R.id.f_screen_launch);
        f_screen_stop = (Button) inflate.findViewById(R.id.f_screen_stop);

        f_screen_preview.setOnClickListener(this);
        f_screen_stop_preview.setOnClickListener(this);
        f_screen_launch.setOnClickListener(this);
        f_screen_stop.setOnClickListener(this);
    }

    private void initAdapter() {
        sourceMemberAdapter = new WmScreenMemberAdapter(R.layout.item_single_button, presenter.sourceMembers);
        f_screen_rv_source.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        f_screen_rv_source.setAdapter(sourceMemberAdapter);
        sourceMemberAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                sourceMemberAdapter.clearChoose();
                sourceMemberAdapter.choose(presenter.sourceMembers.get(position).getDeviceDetailInfo().getDevcieid());
            }
        });

        targetMemberAdapter = new WmScreenMemberAdapter(R.layout.item_single_button, presenter.targetMembers);
        f_screen_rv_target.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        f_screen_rv_target.setAdapter(targetMemberAdapter);
        targetMemberAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                targetMemberAdapter.choose(presenter.targetMembers.get(position).getDeviceDetailInfo().getDevcieid());
                f_screen_target_cb.setChecked(targetMemberAdapter.isChooseAll());
            }
        });
        f_screen_target_cb.setOnClickListener(v -> {
            boolean checked = f_screen_target_cb.isChecked();
            f_screen_target_cb.setChecked(checked);
            targetMemberAdapter.setChooseAll(checked);
        });

        projectorAdapter = new WmProjectorAdapter(R.layout.item_single_button, presenter.onLineProjectors);
        f_screen_rv_pro.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        f_screen_rv_pro.setAdapter(projectorAdapter);
        projectorAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                projectorAdapter.choose(presenter.onLineProjectors.get(position).getDevcieid());
                f_screen_pro_cb.setChecked(projectorAdapter.isChooseAll());
            }
        });
        f_screen_pro_cb.setOnClickListener(v -> {
            boolean checked = f_screen_pro_cb.isChecked();
            f_screen_pro_cb.setChecked(checked);
            projectorAdapter.setChooseAll(checked);
        });
    }

    @Override
    public void notifyOnLineAdapter() {
        if (sourceMemberAdapter != null) {
            sourceMemberAdapter.notifyDataSetChanged();
            sourceMemberAdapter.notifyChecks();
        }
        if (targetMemberAdapter != null) {
            targetMemberAdapter.notifyDataSetChanged();
            targetMemberAdapter.notifyChecks();
        }
        if (projectorAdapter != null) {
            projectorAdapter.notifyDataSetChanged();
            projectorAdapter.notifyChecks();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.f_screen_preview:
                DevMember choose = sourceMemberAdapter.getChoose();
                if (choose != null) {
                    int devcieid = choose.getDeviceDetailInfo().getDevcieid();
                    List<Integer> temps = new ArrayList<>();
                    temps.add(resource_0);
                    List<Integer> ids = new ArrayList<>();
                    ids.add(Values.localDeviceId);
                    JniHandler.getInstance().streamPlay(devcieid, 2, 0, temps, ids);
                } else {
                    ToastUtil.show(R.string.please_choose_source);
                }
                break;
            case R.id.f_screen_stop_preview:

                break;
            case R.id.f_screen_launch:
                startScreen();
                break;
            case R.id.f_screen_stop:
                stopScreen();
                break;
        }
    }

    private void stopScreen() {
        List<Integer> ids = targetMemberAdapter.getChooseIds();
        ids.addAll(projectorAdapter.getChooseIds());
        if (!ids.isEmpty()) {
            List<Integer> temps = new ArrayList<>();
            temps.add(0);
            JniHandler.getInstance().stopResourceOperate(temps, ids);
        } else {
            ToastUtil.show(R.string.please_choose_stop_target);
        }
    }

    private void startScreen() {
        DevMember choose = sourceMemberAdapter.getChoose();
        if (choose != null) {
            List<Integer> ids = targetMemberAdapter.getChooseIds();
            ids.addAll(projectorAdapter.getChooseIds());
            if (!ids.isEmpty()) {
                List<Integer> temps = new ArrayList<>();
                temps.add(resource_0);
                int devcieid = choose.getDeviceDetailInfo().getDevcieid();
                int triggeruserval = f_screen_mandatory_cb.isChecked() ? 1 : 0;
                JniHandler.getInstance().streamPlay(devcieid, 2, triggeruserval, temps, ids);
            } else {
                ToastUtil.show(R.string.please_choose_target);
            }
        } else {
            ToastUtil.show(R.string.please_choose_source);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }
}
