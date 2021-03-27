package xlk.paperless.standard.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.bean.DevMember;

/**
 * @author xlk
 * @date 2020/3/14
 * @desc 文件推送参会人列表adapter
 */
public class PopPushMemberAdapter extends BaseQuickAdapter<DevMember, BaseViewHolder> {
    private List<Integer> devIds = new ArrayList<>();

    public PopPushMemberAdapter(int layoutResId, @Nullable List<DevMember> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, DevMember item) {
        helper.setText(R.id.item_single_btn, item.getMemberDetailInfo().getName().toStringUtf8());
        helper.getView(R.id.item_single_btn).setSelected(devIds.contains(item.getDeviceDetailInfo().getDevcieid()));
    }

    public List<Integer> getDevIds(){
        return devIds;
    }

    public void notifyChecks() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < getData().size(); i++) {
            if (devIds.contains(getData().get(i).getDeviceDetailInfo().getDevcieid())) {
                ids.add(getData().get(i).getDeviceDetailInfo().getDevcieid());
            }
        }
        devIds = ids;
        notifyDataSetChanged();
    }

    public void choose(int devId) {
        if (devIds.contains(devId)) {
            devIds.remove(devIds.indexOf(devId));
        } else {
            devIds.add(devId);
        }
        notifyDataSetChanged();
    }

    public boolean isChooseAll() {
        return getData().size() == devIds.size();
    }

    public void setChooseAll(boolean isAll) {
        devIds.clear();
        if (isAll) {
            for (int i = 0; i < getData().size(); i++) {
                devIds.add(getData().get(i).getDeviceDetailInfo().getDevcieid());
            }
        }
        notifyDataSetChanged();
    }

}
