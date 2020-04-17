package xlk.paperless.standard.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.bean.DevMember;

/**
 * @author xlk
 * @date 2020/3/23
 * @Description:
 */
public class DrawMemberAdapter extends BaseQuickAdapter<DevMember, BaseViewHolder> {
    List<Integer> ids=new ArrayList<>();
    public DrawMemberAdapter(int layoutResId, @Nullable List<DevMember> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, DevMember item) {
        helper.setText(R.id.item_single_btn, item.getMemberDetailInfo().getName().toStringUtf8());
        helper.getView(R.id.item_single_btn).setSelected(ids.contains(item.getMemberDetailInfo().getPersonid()));
    }

    public List<Integer> getChooseIds(){
        return ids;
    }

    public void notifyChecks() {
        List<Integer> temp = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            if (ids.contains(mData.get(i).getMemberDetailInfo().getPersonid())) {
                temp.add(mData.get(i).getMemberDetailInfo().getPersonid());
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
                ids.add(mData.get(i).getMemberDetailInfo().getPersonid());
            }
        }
        notifyDataSetChanged();
    }

}