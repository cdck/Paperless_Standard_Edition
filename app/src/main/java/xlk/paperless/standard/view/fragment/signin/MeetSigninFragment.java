package xlk.paperless.standard.view.fragment.signin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import xlk.paperless.standard.ui.CustomAbsoluteLayout;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.base.BaseFragment;

/**
 * @author xlk
 * @date 2020/3/13
 * @desc: 签到信息
 */
public class MeetSigninFragment extends BaseFragment implements IMeetSignin {

    private final String TAG = "MeetSigninFragment-->";
    private MeetSigninPresenter presenter;
    private CustomAbsoluteLayout f_s_absolute;
    private LinearLayout seat_root_ll;
    private TextView f_s_yd;
    private TextView f_s_yqd;
    private TextView f_s_wqd;
    private int width = 1300, height = 760;//view的宽高，会根据底图大小变动
    private int viewWidth, viewHeight;//表示显示区域的宽高,是不变的

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_signin, container, false);
        presenter = new MeetSigninPresenter(getContext(), this);
        initView(inflate);
        seat_root_ll.post(() -> {
            viewWidth = seat_root_ll.getWidth();
            viewHeight = seat_root_ll.getHeight();
            f_s_absolute.setScreen(viewWidth, viewHeight);
            start();
        });
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    private void start() {
        presenter.queryInterFaceConfiguration();
//        presenter.queryMember();
    }

    private void initView(View inflate) {
        seat_root_ll = inflate.findViewById(R.id.seat_root_ll);
        f_s_absolute = inflate.findViewById(R.id.f_s_absolute);
        f_s_yd = inflate.findViewById(R.id.f_s_yd);
        f_s_yqd = inflate.findViewById(R.id.f_s_yqd);
        f_s_wqd = inflate.findViewById(R.id.f_s_wqd);
    }

    @Override
    public void updateBg(String filepath) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            Drawable drawable = Drawable.createFromPath(filepath);
            f_s_absolute.setBackground(drawable);
            Bitmap bitmap = BitmapFactory.decodeFile(filepath);
            if (bitmap != null) {
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                f_s_absolute.setLayoutParams(params);
                LogUtil.e(TAG, "updateBg 图片宽高 -->" + width + ", " + height);
                presenter.placeDeviceRankingInfo();
                bitmap.recycle();
            }
        });
    }

    @Override
    public void updateSignin(int yqd, int yd) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            f_s_yqd.setText(getString(R.string.yqd_, String.valueOf(yqd)));
            f_s_yd.setText(getString(R.string.yd_, String.valueOf(yd)));
            f_s_wqd.setText(getString(R.string.wqd_, String.valueOf(yd - yqd)));
        });
    }

    @Override
    public void updateView(List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatDetailInfo, boolean isShow) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            LogUtil.e(TAG, "updateView  -->");
            f_s_absolute.removeAllViews();
            for (InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo info : seatDetailInfo) {
                LogUtil.d(TAG, "updateView -->左上角坐标：（" + info.getX() + "," + info.getY() + "）, 设备= " + info.getDevname().toStringUtf8());
                addSeat(info, isShow);
            }
        });
    }

    private void addSeat(InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo item, boolean isShow) {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.item_seat, null);
        RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(30, 30);
//                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams seatLinearParams = new RelativeLayout.LayoutParams(120, 40);
        ImageView item_seat_iv = inflate.findViewById(R.id.item_seat_iv);
        LinearLayout item_seat_ll = inflate.findViewById(R.id.item_seat_ll);
        TextView item_seat_device = inflate.findViewById(R.id.item_seat_device);
        TextView item_seat_member = inflate.findViewById(R.id.item_seat_member);
        switch (item.getDirection()) {
            //上
            case 0:
                item_seat_iv.setImageResource(R.drawable.icon_seat_bottom);
                ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                seatLinearParams.addRule(RelativeLayout.BELOW, item_seat_iv.getId());
                seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            //下
            case 1:
                item_seat_iv.setImageResource(R.drawable.icon_seat_top);
                seatLinearParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                ivParams.addRule(RelativeLayout.BELOW, item_seat_ll.getId());
                ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            //左
            case 2:
                item_seat_iv.setImageResource(R.drawable.icon_seat_right);
                ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                seatLinearParams.addRule(RelativeLayout.BELOW, item_seat_iv.getId());
                seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            //右
            case 3:
                item_seat_iv.setImageResource(R.drawable.icon_seat_left);
                ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                ivParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                seatLinearParams.addRule(RelativeLayout.BELOW, item_seat_iv.getId());
                seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            default:
                break;
        }
        item_seat_iv.setVisibility(isShow ? View.VISIBLE : View.GONE);

        String devName = item.getDevname().toStringUtf8();
        if (!TextUtils.isEmpty(devName)) {
            item_seat_device.setText(devName);
        } else {
            item_seat_device.setVisibility(View.GONE);
        }

        String memberName = item.getMembername().toStringUtf8();
        if (!TextUtils.isEmpty(memberName)) {
            item_seat_member.setText(memberName);
            item_seat_member.setTextColor((item.getIssignin() == 1)
                    ? Color.argb(100, 0, 180, 0)
                    : Color.argb(100, 225, 0, 0));
        } else {
            item_seat_member.setVisibility(View.GONE);
        }

        item_seat_iv.setLayoutParams(ivParams);
        item_seat_ll.setLayoutParams(seatLinearParams);
        //左上角x坐标
        float x1 = item.getX();
        //左上角y坐标
        float y1 = item.getY();
        if (x1 > 1) {
            x1 = 1;
        } else if (x1 < 0) {
            x1 = 0;
        }
        if (y1 > 1) {
            y1 = 1;
        } else if (y1 < 0) {
            y1 = 0;
        }
        int x = (int) (x1 * width);
        int y = (int) (y1 * height);

        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
                120, 70,
                x, y);
        inflate.setLayoutParams(params);
        f_s_absolute.addView(inflate);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            start();
        }
        super.onHiddenChanged(hidden);
    }
}
