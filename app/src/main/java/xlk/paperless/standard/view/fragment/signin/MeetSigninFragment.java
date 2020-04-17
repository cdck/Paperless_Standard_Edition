package xlk.paperless.standard.view.fragment.signin;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceRoom;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.view.fragment.BaseFragment;

/**
 * @author xlk
 * @date 2020/3/13
 * @Description: 签到信息
 */
public class MeetSigninFragment extends BaseFragment implements IMeetSignin {

    private MeetSigninPresenter presenter;
    private AbsoluteLayout f_s_absolute;
    private TextView f_s_yd;
    private TextView f_s_yqd;
    private TextView f_s_wqd;
    private int width, height;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_signin, container, false);
        presenter = new MeetSigninPresenter(getContext(), this);
        presenter.register();
        initView(inflate);
        f_s_absolute.post(new Runnable() {
            @Override
            public void run() {
                width = f_s_absolute.getWidth();
                height = f_s_absolute.getHeight();
                start();
            }
        });
        return inflate;
    }

    private void start() {
        presenter.queryMeetRoomBg();
        presenter.queryMember();
        presenter.placeDeviceRankingInfo();
    }

    private void initView(View inflate) {
        f_s_absolute = (AbsoluteLayout) inflate.findViewById(R.id.f_s_absolute);
        f_s_yd = (TextView) inflate.findViewById(R.id.f_s_yd);
        f_s_yqd = (TextView) inflate.findViewById(R.id.f_s_yqd);
        f_s_wqd = (TextView) inflate.findViewById(R.id.f_s_wqd);
    }

    @Override
    public void updateBg(String filepath) {
        Drawable drawable = Drawable.createFromPath(filepath);
        f_s_absolute.setBackground(drawable);
    }

    @Override
    public void updateSignin(int yqd, int yd) {
        f_s_yqd.setText(getString(R.string.yqd_, String.valueOf(yqd)));
        f_s_yd.setText(getString(R.string.yd_, String.valueOf(yd)));
        f_s_wqd.setText(getString(R.string.wqd_, String.valueOf(yd - yqd)));
    }

    @Override
    public void updateView(List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatDetailInfo) {
        f_s_absolute.removeAllViews();
        for (InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo info : seatDetailInfo) {
            addSeat(info);
        }
    }

    private void addSeat(InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.item_seat, null);
        RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams seatLinearParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ImageView item_seat_iv = inflate.findViewById(R.id.item_seat_iv);
        LinearLayout item_seat_ll = inflate.findViewById(R.id.item_seat_ll);
        TextView item_seat_device = inflate.findViewById(R.id.item_seat_device);
        TextView item_seat_member = inflate.findViewById(R.id.item_seat_member);
        switch (item.getDirection()) {
            case 0://朝上
                item_seat_iv.setImageResource(R.drawable.icon_seat_bottom);
                if (item.getIssignin() == 1) {
                    item_seat_member.setTextColor(getContext().getResources().getColor(R.color.black));
                } else {
                    item_seat_member.setTextColor(getContext().getResources().getColor(R.color.red));
                }
                ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                seatLinearParams.addRule(RelativeLayout.BELOW, item_seat_iv.getId());
                seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            case 1://朝下 (文本控件在下)
                item_seat_iv.setImageResource(R.drawable.icon_seat_top);
                if (item.getIssignin() == 1) {
                    item_seat_member.setTextColor(getContext().getResources().getColor(R.color.black));
                } else {
                    item_seat_member.setTextColor(getContext().getResources().getColor(R.color.red));
                }
                seatLinearParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                ivParams.addRule(RelativeLayout.BELOW, item_seat_ll.getId());
                ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            case 2://朝左
                item_seat_iv.setImageResource(R.drawable.icon_seat_right);
                if (item.getIssignin() == 1) {
                    item_seat_member.setTextColor(getContext().getResources().getColor(R.color.black));
                } else {
                    item_seat_member.setTextColor(getContext().getResources().getColor(R.color.red));
                }
                ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                seatLinearParams.addRule(RelativeLayout.BELOW, item_seat_iv.getId());
                seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            case 3://朝右
                item_seat_iv.setImageResource(R.drawable.icon_seat_left);
                if (item.getIssignin() == 1) {
                    item_seat_member.setTextColor(getContext().getResources().getColor(R.color.black));
                } else {
                    item_seat_member.setTextColor(getContext().getResources().getColor(R.color.red));
                }
                ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                seatLinearParams.addRule(RelativeLayout.BELOW, item_seat_iv.getId());
                seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
        }

        String devName = item.getDevname().toStringUtf8();
        String memberName = item.getMembername().toStringUtf8();

        if (!TextUtils.isEmpty(devName)) item_seat_device.setText(devName);
        else item_seat_device.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(memberName)) item_seat_member.setText(memberName);
        else item_seat_member.setVisibility(View.GONE);

        item_seat_iv.setLayoutParams(ivParams);
        item_seat_ll.setLayoutParams(seatLinearParams);
        float x1 = item.getX();
        float y1 = item.getY();
        if (x1 > 1) x1 = 1;
        else if (x1 < 0) x1 = 0;

        if (y1 > 1) y1 = 1;
        else if (y1 < 0) y1 = 0;
        int x = (int) (x1 * width);
        int y = (int) (y1 * height);
        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, x, y);
        inflate.setLayoutParams(params);
        inflate.setLayoutParams(params);
        f_s_absolute.addView(inflate);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unregister();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(!hidden){
            start();
        }
        super.onHiddenChanged(hidden);
    }
}
