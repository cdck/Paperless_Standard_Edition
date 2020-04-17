package xlk.paperless.standard.view.fragment.other.bulletin;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.mogujie.tt.protobuf.InterfaceBullet;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.BulletAdapter;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.fragment.BaseFragment;

import static xlk.paperless.standard.util.ConvertUtil.s2b;

/**
 * @author xlk
 * @date 2020/4/8
 * @Description: 公告管理
 */
public class BulletinFragment extends BaseFragment implements IBulletin, View.OnClickListener {
    private RecyclerView f_bulletin_rv;
    private EditText f_bulletin_title;
    private EditText f_bulletin_content;
    private Button f_bulletin_add;
    private Button f_bulletin_del;
    private Button f_bulletin_modify;
    private Button f_bulletin_launch;
    private Button f_bulletin_close;
    private BulletinPresenter presenter;
    private BulletAdapter bulletAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_bulletin, container, false);
        initView(inflate);
        presenter = new BulletinPresenter(getContext(), this);
        presenter.register();
        presenter.queryNotice();
        return inflate;
    }

    private void initView(View inflate) {
        f_bulletin_rv = (RecyclerView) inflate.findViewById(R.id.f_bulletin_rv);
        f_bulletin_title = (EditText) inflate.findViewById(R.id.f_bulletin_title);
        f_bulletin_content = (EditText) inflate.findViewById(R.id.f_bulletin_content);
        f_bulletin_add = (Button) inflate.findViewById(R.id.f_bulletin_add);
        f_bulletin_del = (Button) inflate.findViewById(R.id.f_bulletin_del);
        f_bulletin_modify = (Button) inflate.findViewById(R.id.f_bulletin_modify);
        f_bulletin_launch = (Button) inflate.findViewById(R.id.f_bulletin_launch);
        f_bulletin_close = (Button) inflate.findViewById(R.id.f_bulletin_close);

        f_bulletin_add.setOnClickListener(this);
        f_bulletin_del.setOnClickListener(this);
        f_bulletin_modify.setOnClickListener(this);
        f_bulletin_launch.setOnClickListener(this);
        f_bulletin_close.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.f_bulletin_add:
                String title = f_bulletin_title.getText().toString();
                String content = f_bulletin_content.getText().toString();
                if (title.isEmpty() || content.isEmpty()) {
                    ToastUtil.show(getContext(), R.string.please_enter_info);
                } else {
                    InterfaceBullet.pbui_Item_BulletDetailInfo build = InterfaceBullet.pbui_Item_BulletDetailInfo.newBuilder()
                            .setTitle(s2b(title))
                            .setContent(s2b(content)).build();
                    JniHandler.getInstance().addNotice(build);
                }
                break;
            case R.id.f_bulletin_del:
                if (bulletAdapter != null && bulletAdapter.getChoose() != null) {
                    JniHandler.getInstance().deleteNotice(bulletAdapter.getChoose());
                } else {
                    ToastUtil.show(getContext(), R.string.please_choose_bulletin);
                }
                break;
            case R.id.f_bulletin_modify:
                if (bulletAdapter != null && bulletAdapter.getChoose() != null) {
                    String title1 = f_bulletin_title.getText().toString();
                    String content1 = f_bulletin_content.getText().toString();
                    if (title1.isEmpty() || content1.isEmpty()) {
                        ToastUtil.show(getContext(), R.string.please_enter_info);
                    } else {
                        InterfaceBullet.pbui_Item_BulletDetailInfo build = InterfaceBullet.pbui_Item_BulletDetailInfo.newBuilder()
                                .setTitle(s2b(title1))
                                .setContent(s2b(content1)).build();
                        JniHandler.getInstance().modifNotice(build);
                    }
                } else {
                    ToastUtil.show(getContext(), R.string.please_choose_bulletin);
                }
                break;
            case R.id.f_bulletin_launch:
                if (bulletAdapter != null && bulletAdapter.getChoose() != null) {
                    List<Integer> ids = new ArrayList<>();
//                    ids.add(0);
                    JniHandler.getInstance().pushNotice(bulletAdapter.getChoose(), ids);
                } else {
                    ToastUtil.show(getContext(), R.string.please_choose_bulletin);
                }
                break;
            case R.id.f_bulletin_close:
                if (bulletAdapter != null && bulletAdapter.getChoose() != null) {
                    List<Integer> ids = new ArrayList<>();
                    ids.add(0);
                    JniHandler.getInstance().stopNotice(bulletAdapter.getChoose().getBulletid(), ids);
                } else {
                    ToastUtil.show(getContext(), R.string.please_choose_bulletin);
                }
                break;
        }
    }


    @Override
    public void notifyAdapter() {
        if (bulletAdapter == null) {
            bulletAdapter = new BulletAdapter(R.layout.item_bullet, presenter.bulletInfos);
            f_bulletin_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            f_bulletin_rv.setAdapter(bulletAdapter);
        } else {
            bulletAdapter.notifyDataSetChanged();
        }
        bulletAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                InterfaceBullet.pbui_Item_BulletDetailInfo info = presenter.bulletInfos.get(position);
                bulletAdapter.choose(info.getBulletid());
                updateUI(info);
            }
        });
        InterfaceBullet.pbui_Item_BulletDetailInfo choose = bulletAdapter.getChoose();
        if (choose == null) {
            if (!presenter.bulletInfos.isEmpty()) {
                bulletAdapter.choose(presenter.bulletInfos.get(0).getBulletid());
                updateUI(presenter.bulletInfos.get(0));
            } else {
                updateUI(null);
            }
        }
    }

    private void updateUI(InterfaceBullet.pbui_Item_BulletDetailInfo info) {
        f_bulletin_title.setText("");
        f_bulletin_content.setText("");
        if (info != null) {
            f_bulletin_title.setText(info.getTitle().toStringUtf8());
            f_bulletin_content.setText(info.getContent().toStringUtf8());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unregister();
    }
}
