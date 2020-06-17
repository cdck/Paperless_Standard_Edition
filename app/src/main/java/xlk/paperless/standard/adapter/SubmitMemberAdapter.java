package xlk.paperless.standard.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.bean.SubmitMember;

/**
 * @author xlk
 * @date 2020/4/3
 * @desc 投票和选举查看详情界面
 */
public class SubmitMemberAdapter extends BaseQuickAdapter<SubmitMember, BaseViewHolder> {
    public SubmitMemberAdapter(int layoutResId, @Nullable List<SubmitMember> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SubmitMember item) {
        helper.setText(R.id.number, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.member, item.getMemberInfo().getMembername().toStringUtf8())
                .setText(R.id.answer, item.getAnswer());
    }
}
