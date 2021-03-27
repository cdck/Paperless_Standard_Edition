package xlk.paperless.standard.view.admin.fragment.pre.function;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceMeetfunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;

/**
 * @author Created by xlk on 2020/10/24.
 * @desc
 */
public class FunctionFragment extends BaseFragment implements FunctionInterface, View.OnClickListener {

    private FunctionPresenter presenter;
    private FunctionAdapter functionAdapter, hideFunctionAdapter;
    private RecyclerView rv_current;
    private Button btn_move_up;
    private Button btn_move_down;
    private Button btn_add_all;
    private Button btn_add;
    private Button btn_remove_all;
    private Button btn_remove;
    private RecyclerView rv_all;
    private Button btn_save;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_function, container, false);
        initView(inflate);
        presenter = new FunctionPresenter(this);
        presenter.queryFunction();
        return inflate;
    }

    @Override
    protected void reShow() {
        presenter.queryFunction();
    }

    private void log(List<FunctionBean> meetFunction) {
        LogUtil.d(TAG, "开始打印---- ");
        for (int i = 0; i < meetFunction.size(); i++) {
            FunctionBean bean = meetFunction.get(i);
            LogUtil.i(TAG, "log position=" + bean.getPosition() + ", funcode=" + bean.getFuncode());
        }
    }

    @Override
    public void updateFunctionRv(List<FunctionBean> meetFunction,
                                 List<FunctionBean> hideMeetFunction) {
        LogUtil.e(TAG, "updateFunctionRv ");
        if (functionAdapter == null) {
            functionAdapter = new FunctionAdapter(R.layout.item_admin_function, meetFunction);
            rv_current.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_current.setAdapter(functionAdapter);
            functionAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    functionAdapter.setSelected(meetFunction.get(position).getFuncode());
                }
            });
        } else {
            functionAdapter.notifyDataSetChanged();
        }
        if (hideFunctionAdapter == null) {
            hideFunctionAdapter = new FunctionAdapter(R.layout.item_admin_function, hideMeetFunction);
            rv_all.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_all.setAdapter(hideFunctionAdapter);
            hideFunctionAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    hideFunctionAdapter.setSelected(hideMeetFunction.get(position).getFuncode());
                }
            });
        } else {
            hideFunctionAdapter.notifyDataSetChanged();
        }
    }

    public void initView(View rootView) {
        this.rv_current = (RecyclerView) rootView.findViewById(R.id.rv_current);
        this.btn_move_up = (Button) rootView.findViewById(R.id.btn_move_up);
        this.btn_move_down = (Button) rootView.findViewById(R.id.btn_move_down);
        this.btn_add_all = (Button) rootView.findViewById(R.id.btn_add_all);
        this.btn_add = (Button) rootView.findViewById(R.id.btn_add);
        this.btn_remove_all = (Button) rootView.findViewById(R.id.btn_remove_all);
        this.btn_remove = (Button) rootView.findViewById(R.id.btn_remove);
        this.rv_all = (RecyclerView) rootView.findViewById(R.id.rv_all);
        this.btn_save = (Button) rootView.findViewById(R.id.btn_save);
        btn_move_up.setOnClickListener(this);
        btn_move_down.setOnClickListener(this);
        btn_add_all.setOnClickListener(this);
        btn_add.setOnClickListener(this);
        btn_remove_all.setOnClickListener(this);
        btn_remove.setOnClickListener(this);
        btn_save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_move_up:
                moveUp();
                break;
            case R.id.btn_move_down:
                moveDown();
                break;
            case R.id.btn_add_all:
                addAll();
                break;
            case R.id.btn_add:
                add();
                break;
            case R.id.btn_remove_all:
                removeAll();
                break;
            case R.id.btn_remove:
                remove();
                break;
            case R.id.btn_save:
                save();
                break;
            default:
                break;
        }
    }

    private void save() {
        List<FunctionBean> meetFunction = presenter.getMeetFunction();
        List<InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo> items = new ArrayList<>();
        for (int i = 0; i < meetFunction.size(); i++) {
            InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo build = InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo.newBuilder()
                    .setFuncode(meetFunction.get(i).getFuncode())
                    .setPosition(meetFunction.get(i).getPosition())
                    .build();
            items.add(build);
        }
        jni.modifyMeetFunction(InterfaceMeetfunction.Pb_FunCon_ModifyFlag.Pb_FUNCONFIG_MODFLAG_SETDEFAULT_VALUE, items);
    }

    private void remove() {
        if (functionAdapter == null) {
            return;
        }
        FunctionBean selected = functionAdapter.getSelected();
        if (selected == null) {
            ToastUtil.show(R.string.please_choose_function);
            return;
        }
        List<FunctionBean> hideMeetFunction = presenter.getHideMeetFunction();
        List<FunctionBean> meetFunction = presenter.getMeetFunction();
        selected.setPosition(hideMeetFunction.size());
        hideMeetFunction.add(selected);
        meetFunction.remove(selected);
        for (int i = 0; i < meetFunction.size(); i++) {
            meetFunction.get(i).setPosition(i);
        }
        Collections.sort(hideMeetFunction);
        functionAdapter.notifyDataSetChanged();
        hideFunctionAdapter.notifyDataSetChanged();
    }

    private void removeAll() {
        List<FunctionBean> meetFunction = presenter.getMeetFunction();
        if (meetFunction.isEmpty()) {
            return;
        }
        List<FunctionBean> hideMeetFunction = presenter.getHideMeetFunction();
        final int size = hideMeetFunction.size();
        for (int i = 0; i < meetFunction.size(); i++) {
            FunctionBean bean = meetFunction.get(i);
            bean.setPosition(size + i);
            hideMeetFunction.add(bean);
        }
        meetFunction.clear();
        Collections.sort(hideMeetFunction);
        functionAdapter.notifyDataSetChanged();
        hideFunctionAdapter.notifyDataSetChanged();
    }

    private void add() {
        if (hideFunctionAdapter == null) {
            return;
        }
        FunctionBean selected = hideFunctionAdapter.getSelected();
        if (selected == null) {
            ToastUtil.show(R.string.please_choose_function);
            return;
        }
        List<FunctionBean> meetFunction = presenter.getMeetFunction();
        List<FunctionBean> hideMeetFunction = presenter.getHideMeetFunction();
        selected.setPosition(meetFunction.size());
        meetFunction.add(selected);
        hideMeetFunction.remove(selected);
        Collections.sort(meetFunction);
        functionAdapter.notifyDataSetChanged();
        hideFunctionAdapter.notifyDataSetChanged();
    }

    private void addAll() {
        List<FunctionBean> hideMeetFunction = presenter.getHideMeetFunction();
        if (hideMeetFunction.isEmpty()) {
            return;
        }
        List<FunctionBean> meetFunction = presenter.getMeetFunction();
        final int size = meetFunction.size();
        for (int i = 0; i < hideMeetFunction.size(); i++) {
            FunctionBean hideItem = hideMeetFunction.get(i);
            hideItem.setPosition(size + i);
            meetFunction.add(hideItem);
        }
        hideMeetFunction.clear();
        Collections.sort(meetFunction);
        functionAdapter.notifyDataSetChanged();
        hideFunctionAdapter.notifyDataSetChanged();
    }

    private void moveUp() {
        if (functionAdapter == null) {
            return;
        }
        FunctionBean current = functionAdapter.getSelected();
        if (current == null) {
            ToastUtil.show(R.string.please_choose_function);
            return;
        }
        int position = current.getPosition();
        if (position == 0) {
            //已经在最顶端就不需要上移了
            return;
        }
        List<FunctionBean> meetFunction = presenter.getMeetFunction();
        FunctionBean previous = meetFunction.get(position - 1);
        current.setPosition(position - 1);
        previous.setPosition(position);
        Collections.sort(meetFunction);
        functionAdapter.notifyDataSetChanged();
    }

    private void moveDown() {
        if (functionAdapter == null) {
            return;
        }
        FunctionBean current = functionAdapter.getSelected();
        if (current == null) {
            ToastUtil.show(R.string.please_choose_function);
            return;
        }
        int position = current.getPosition();
        List<FunctionBean> meetFunction = presenter.getMeetFunction();
        if (position == meetFunction.size() - 1) {
            //已经是最低端则不需要再下移
            return;
        }
        FunctionBean next = meetFunction.get(position + 1);
        current.setPosition(position + 1);
        next.setPosition(position);
        Collections.sort(meetFunction);
        functionAdapter.notifyDataSetChanged();
    }
}
