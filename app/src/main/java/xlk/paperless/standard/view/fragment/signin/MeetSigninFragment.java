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

import com.mogujie.tt.protobuf.InterfaceRoom;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.EventMessage;
import xlk.paperless.standard.data.bean.SeatBean;
import xlk.paperless.standard.ui.CustomAbsoluteLayout;
import xlk.paperless.standard.ui.CustomSeatView;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.view.App;

/**
 * @author xlk
 * @date 2020/3/13
 * @desc: 签到信息
 */
public class MeetSigninFragment extends BaseFragment implements IMeetSignin {

    private final String TAG = "MeetSigninFragment-->";
    private MeetSigninPresenter presenter;
    private TextView f_s_yd;
    private TextView f_s_yqd;
    private TextView f_s_wqd;
    /**
     * view的宽高，会根据底图大小变动
     */
    private int width = 1300, height = 760;
    private CustomSeatView seat_view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_signin, container, false);
        presenter = new MeetSigninPresenter(getContext(), this);
        initView(inflate);
        seat_view.setCanChoose(false);
        seat_view.setCanDragSeat(false);
        seat_view.post(() -> {
            seat_view.setViewSize(seat_view.getWidth(), seat_view.getHeight());
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
    }

    private void initView(View inflate) {
        seat_view = inflate.findViewById(R.id.seat_view);
        f_s_yd = inflate.findViewById(R.id.f_s_yd);
        f_s_yqd = inflate.findViewById(R.id.f_s_yqd);
        f_s_wqd = inflate.findViewById(R.id.f_s_wqd);
        inflate.findViewById(R.id.btn_seat).setOnClickListener(v -> {
            EventBus.getDefault().post(new EventMessage.Builder().type(Constant.BUS_SIGN_IN_LIST_PAGE).objects(true).build());
        });
    }

    @Override
    public void updateBg(String filepath) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            Drawable drawable = Drawable.createFromPath(filepath);
            seat_view.setBackground(drawable);
            Bitmap bitmap = BitmapFactory.decodeFile(filepath);
            if (bitmap != null) {
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                seat_view.setLayoutParams(params);
                seat_view.setImgSize(width, height);
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

    private List<SeatBean> seatBeans = new ArrayList<>();

    @Override
    public void updateView(List<InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo> seatDetailInfo, boolean isShow) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            LogUtil.e(TAG, "updateView  -->");
            seatBeans.clear();
            for (InterfaceRoom.pbui_Item_MeetRoomDevSeatDetailInfo info : seatDetailInfo) {
                SeatBean seatBean = new SeatBean(info.getDevid(), info.getDevname().toStringUtf8(), info.getX(), info.getY(),
                        info.getDirection(), info.getMemberid(), info.getMembername().toStringUtf8(),
                        info.getIssignin(), info.getRole(), info.getFacestate());
                seatBeans.add(seatBean);
                LogUtil.d(TAG, "updateView -->左上角坐标：（" + info.getX() + "," + info.getY() + "）, 设备= " + info.getDevname().toStringUtf8());
            }
            seat_view.addSeat(seatBeans);
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            start();
        }
        super.onHiddenChanged(hidden);
    }
}
