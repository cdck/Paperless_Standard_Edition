package xlk.paperless.standard.view.meet;

import android.graphics.drawable.Drawable;

import com.mogujie.tt.protobuf.InterfaceDevice;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;
import com.mogujie.tt.protobuf.InterfaceMeetfunction;

import java.util.List;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc
 */
public interface IMeet {

    void updateFunction(List<InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo> functions);

    void setLogoVisibility(boolean visibility);

    void updateLogo(Drawable drawable);

    void updateTime(long tiem);

    void updateOnline(String string);

    void updateMeetName(InterfaceDevice.pbui_Type_DeviceFaceShowDetail deviceMeetInfo);

    void hasOtherFunction(boolean isHas);

    void jump2main();

    void setCompanyName(String company);

    void setCompanyVisibility(boolean isShow);

    void updateBg(Drawable drawable);

    void updateMemberRole(String roleStr);

    void updateLogoSize(int logoiv, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info);
}
