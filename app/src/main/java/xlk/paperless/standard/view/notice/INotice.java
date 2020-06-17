package xlk.paperless.standard.view.notice;

import android.graphics.drawable.Drawable;

import com.mogujie.tt.protobuf.InterfaceBullet;
import com.mogujie.tt.protobuf.InterfaceFaceconfig;

/**
 * @author xlk
 * @date 2020/4/8
 * @desc
 */
public interface INotice {
    void updateText(InterfaceBullet.pbui_Item_BulletDetailInfo info);

    void updateBtn(int btn, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info);

    void updateTv(int tv, InterfaceFaceconfig.pbui_Item_FaceTextItemInfo info);

    void updateNoticeBg(Drawable drawable);

    void updateNoticeLogo(Drawable drawable);

    void clearAll();
}
