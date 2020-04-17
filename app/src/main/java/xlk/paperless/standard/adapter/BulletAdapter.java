package xlk.paperless.standard.adapter;

import android.support.annotation.Nullable;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceBullet;

import java.util.List;

import xlk.paperless.standard.R;

/**
 * @author xlk
 * @date 2020/4/8
 * @Description: 公告管理adapter
 */
public class BulletAdapter extends BaseQuickAdapter<InterfaceBullet.pbui_Item_BulletDetailInfo, BaseViewHolder> {
    private int chooseId = -1;

    public BulletAdapter(int layoutResId, @Nullable List<InterfaceBullet.pbui_Item_BulletDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceBullet.pbui_Item_BulletDetailInfo item) {
        helper.setText(R.id.item_bullet_number, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_bullet_title, item.getTitle().toStringUtf8())
                .setText(R.id.item_bullet_content, item.getContent().toStringUtf8());
        int color = (chooseId == item.getBulletid()) ? mContext.getResources().getColor(R.color.bullet_select_t) : mContext.getResources().getColor(R.color.bullet_select_f);
        LinearLayout view = helper.getView(R.id.item_bullet_root);
        view.setBackgroundColor(color);
    }

    public InterfaceBullet.pbui_Item_BulletDetailInfo getChoose() {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getBulletid() == chooseId) {
                return mData.get(i);
            }
        }
        return null;
    }

    public void notifyChoose() {
        int temp = -1;
        for (int i = 0; i < mData.size(); i++) {
            if (chooseId == mData.get(i).getBulletid()) {
                temp = chooseId;
            }
        }
        chooseId = temp;
        notifyDataSetChanged();
    }

    public void choose(int bullitId) {
        chooseId = bullitId;
        notifyDataSetChanged();
    }
}
