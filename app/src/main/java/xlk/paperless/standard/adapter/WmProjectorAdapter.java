package xlk.paperless.standard.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceDevice;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;

/**
 * @author xlk
 * @date 2020/3/27
 * @Description:悬浮框中在线投影机
 */
public class WmProjectorAdapter extends BaseQuickAdapter<InterfaceDevice.pbui_Item_DeviceDetailInfo,BaseViewHolder> {
    List<Integer> ids = new ArrayList<>();

    public WmProjectorAdapter(int layoutResId, @Nullable List<InterfaceDevice.pbui_Item_DeviceDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceDevice.pbui_Item_DeviceDetailInfo item) {
        helper.setText(R.id.item_single_btn, item.getDevname().toStringUtf8());
        helper.getView(R.id.item_single_btn).setSelected(ids.contains(item.getDevcieid()));
    }

    public List<Integer> getChooseIds() {
        return ids;
    }

    public void notifyChecks() {
        List<Integer> temp = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            if (ids.contains(mData.get(i).getDevcieid())) {
                temp.add(mData.get(i).getDevcieid());
            }
        }
        ids = temp;
        notifyDataSetChanged();
    }

    public void choose(int devId) {
        if (ids.contains(devId)) {
            ids.remove(ids.indexOf(devId));
        } else {
            ids.add(devId);
        }
        notifyDataSetChanged();
    }

    public boolean isChooseAll() {
        return mData.size() == ids.size();
    }

    public void setChooseAll(boolean isAll) {
        ids.clear();
        if (isAll) {
            for (int i = 0; i < mData.size(); i++) {
                ids.add(mData.get(i).getDevcieid());
            }
        }
        notifyDataSetChanged();
    }
}
