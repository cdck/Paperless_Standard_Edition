package xlk.paperless.standard.view.admin.fragment.after.signin;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceSignin;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;
import xlk.paperless.standard.util.ConvertUtil;
import xlk.paperless.standard.util.DateUtil;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc
 */
public class AdminSignInAdapter extends BaseQuickAdapter<SignInBean, BaseViewHolder> {
    List<Integer> checks = new ArrayList<>();

    public AdminSignInAdapter(int layoutResId, @Nullable List<SignInBean> data) {
        super(layoutResId, data);
    }

    public void setSelected(int id) {
        if (checks.contains(id)) {
            checks.remove(checks.indexOf(id));
        } else {
            checks.add(id);
        }
        notifyDataSetChanged();
    }

    public List<Integer> getChecks() {
        return checks;
    }

    @Override
    protected void convert(BaseViewHolder helper, SignInBean item) {
        InterfaceSignin.pbui_Item_MeetSignInDetailInfo sign = item.getSign();
        long utcseconds = 0;
        if (sign != null) {
            utcseconds = sign.getUtcseconds();
        }
        boolean isSignIn = utcseconds > 0;
        String signInTime = isSignIn ? DateUtil.millisecondFormatDetailedTime(utcseconds * 1000) : "";
        helper.setText(R.id.item_view_1, String.valueOf(helper.getLayoutPosition() + 1))
                .setText(R.id.item_view_2, item.getMember().getName().toStringUtf8())
                .setText(R.id.item_view_3, signInTime)
                .setText(R.id.item_view_4, isSignIn ? getContext().getString(R.string.checked_in) : getContext().getString(R.string.not_checked_in))
        ;
        Button item_view_5 = helper.getView(R.id.item_view_5);
        item_view_5.setBackgroundColor(Color.WHITE);
        item_view_5.setEnabled(false);
        if (isSignIn) {
            int signinType = sign.getSigninType();
            switch (signinType) {
                case InterfaceMacro.Pb_MeetSignType.Pb_signin_direct_VALUE:
                    item_view_5.setText(getContext().getString(R.string.direct_signin));
                    item_view_5.setTextColor(getContext().getColor(R.color.light_black));
                    break;
                case InterfaceMacro.Pb_MeetSignType.Pb_signin_psw_VALUE:
                    item_view_5.setText(getContext().getString(R.string.personal_pwd_signin));
                    item_view_5.setTextColor(getContext().getColor(R.color.light_black));
                    break;
                case InterfaceMacro.Pb_MeetSignType.Pb_signin_photo_VALUE:
                    Bitmap bitmap = ConvertUtil.bs2bmp(sign.getPsigndata());
                    item_view_5.setBackground(new BitmapDrawable(bitmap));
                    item_view_5.setEnabled(true);
//                    addChildClickViewIds(R.id.item_view_5);
                    break;
                case InterfaceMacro.Pb_MeetSignType.Pb_signin_onepsw_VALUE:
                    item_view_5.setText(getContext().getString(R.string.meeting_pwd_signin));
                    item_view_5.setTextColor(getContext().getColor(R.color.light_black));
                    break;
                default:
                    item_view_5.setText("");
                    break;
            }
        }
        boolean isSelected = checks.contains(item.getMember().getPersonid());
        int textColor = isSelected ? getContext().getColor(R.color.white) : getContext().getColor(R.color.light_black);
        helper.setTextColor(R.id.item_view_1, textColor)
                .setTextColor(R.id.item_view_2, textColor)
                .setTextColor(R.id.item_view_3, textColor)
                .setTextColor(R.id.item_view_4, textColor);

        int backgroundColor = isSelected ? getContext().getColor(R.color.light_blue) : getContext().getColor(R.color.white);
        helper.setBackgroundColor(R.id.item_view_1, backgroundColor)
                .setBackgroundColor(R.id.item_view_2, backgroundColor)
                .setBackgroundColor(R.id.item_view_3, backgroundColor)
                .setBackgroundColor(R.id.item_view_4, backgroundColor);
    }
}
