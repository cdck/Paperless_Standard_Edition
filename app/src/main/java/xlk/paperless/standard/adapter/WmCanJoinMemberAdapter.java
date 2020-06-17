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
 * @date 2020/3/28
 * @desc 可加入同屏的参会人
 */
public class WmCanJoinMemberAdapter extends BaseQuickAdapter<InterfaceDevice.pbui_Item_DeviceResPlay, BaseViewHolder> {

    int id = -1;

    public WmCanJoinMemberAdapter(int layoutResId, @Nullable List<InterfaceDevice.pbui_Item_DeviceResPlay> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceDevice.pbui_Item_DeviceResPlay item) {
        helper.setText(R.id.item_single_btn, item.getName().toStringUtf8());
        helper.getView(R.id.item_single_btn).setSelected(id == item.getDevceid());
    }

    public int getChooseId() {
        return id;
    }

    public void notifyChecks() {
        int temp = -1;
        for (int i = 0; i < mData.size(); i++) {
            if (id == mData.get(i).getDevceid()) {
                temp = mData.get(i).getDevceid();
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
