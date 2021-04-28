package xlk.paperless.standard.view.fragment.signin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.mogujie.tt.protobuf.InterfaceRoom;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
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
    /**
     * view的宽高，会根据底图大小变动
     */
    private int width = 1300, height = 760;
    /**
     * 表示显示区域的宽高,是不变的
     */
    private int viewWidth, viewHeight;
    private LinearLayout ll_seat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_signin, container, false);
        presenter = new MeetSigninPresenter(getContext(), this);
        initView(inflate);
        seat_root_ll.post(() -> {
            viewWidth = seat_root_ll.getWidth();
            viewHeight = seat_root_ll.getHeight();
            LogUtils.e("显示宽高=" + viewWidth + "," + viewHeight);
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
        presenter.queryMeetRoomBg();
//        presenter.queryInterFaceConfiguration();
//        presenter.queryMember(); 192.168.1.208
    }

    private void initView(View inflate) {
        f_s_yd = inflate.findViewById(R.id.f_s_yd);
        f_s_yqd = inflate.findViewById(R.id.f_s_yqd);
        f_s_wqd = inflate.findViewById(R.id.f_s_wqd);

        seat_root_ll = inflate.findViewById(R.id.seat_root_ll);
        f_s_absolute = inflate.findViewById(R.id.f_s_absolute);
        ll_seat = inflate.findViewById(R.id.ll_seat);

        inflate.findViewById(R.id.btn_seat).setOnClickListener(v -> {
            EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_SIGN_IN_LIST_PAGE).object(true).build());
        });
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
        RelativeLayout.LayoutParams deviceLayoutParams = new RelativeLayout.LayoutParams(120, 20);
        RelativeLayout.LayoutParams seatLinearParams = new RelativeLayout.LayoutParams(120, 40);
        ImageView item_seat_iv = inflate.findViewById(R.id.item_seat_iv);
//        item_seat_iv.setVisibility(isShow ? View.VISIBLE : View.GONE);
        TextView item_seat_device = inflate.findViewById(R.id.item_seat_device);
        item_seat_device.setTextSize(7);
        LinearLayout item_seat_ll = inflate.findViewById(R.id.item_seat_ll);
        TextView item_seat_member = inflate.findViewById(R.id.item_seat_member);
        item_seat_member.setTextSize(7);

        String devName = item.getDevname().toStringUtf8();
        if (!TextUtils.isEmpty(devName)) {
            item_seat_device.setText(devName);
        } else {
            item_seat_device.setVisibility(View.INVISIBLE);
        }
        boolean isChecked = item.getIssignin() == 1;
        item_seat_iv.setImageResource(isChecked ? R.drawable.icon_signin : R.drawable.icon_un_signin);
        item_seat_device.setSelected(isChecked);
        item_seat_ll.setSelected(isChecked);
        String memberName = item.getMembername().toStringUtf8();
        if (!TextUtils.isEmpty(memberName)) {
            item_seat_member.setText(memberName);
        } else {
            item_seat_member.setVisibility(View.INVISIBLE);
        }

        deviceLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        deviceLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        seatLinearParams.addRule(RelativeLayout.BELOW, item_seat_device.getId());
        seatLinearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        item_seat_device.setLayoutParams(deviceLayoutParams);
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

        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(120, 60, x, y);
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
