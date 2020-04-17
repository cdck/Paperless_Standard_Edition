package xlk.paperless.standard.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.bean.JoinPro;

/**
 * @author xlk
 * @date 2020/3/28
 * @Description: 可加入同屏的投影机
 */
public class WmCanJoinProAdapter extends BaseQuickAdapter<JoinPro, BaseViewHolder> {

    int id = -1;

    public WmCanJoinProAdapter(int layoutResId, @Nullable List<JoinPro> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, JoinPro item) {
        helper.setText(R.id.item_single_btn, item.getDevice().getDevname().toStringUtf8());
        helper.getView(R.id.item_single_btn).setSelected(id == item.getResPlay().getDevceid());
    }

    public int getChooseId() {
        return id;
    }

    public void notifyChecks() {
        int temp = -1;
        for (int i = 0; i < mData.size(); i++) {
            if (id == mData.get(i).getResPlay().getDevceid()) {
                temp = mData.get(i).getResPlay().getDevceid();
            }
        }
        id = temp;
        notifyDataSetChanged();
    }

    public void choose(int devId) {
        if (id == devId) id = -1;
        else id = devId;
        notifyDataSetChanged();
    }
}
