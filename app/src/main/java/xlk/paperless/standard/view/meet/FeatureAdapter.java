package xlk.paperless.standard.view.meet;

import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceMacro;
import com.mogujie.tt.protobuf.InterfaceMeetfunction;

import java.util.List;

import androidx.annotation.Nullable;
import xlk.paperless.standard.R;

/**
 * @author Created by xlk on 2021/3/24.
 * @desc
 */
class FeatureAdapter extends BaseQuickAdapter<InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo, BaseViewHolder> {
    public FeatureAdapter( @Nullable List<InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo> data) {
        super(R.layout.item_meet_feature, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceMeetfunction.pbui_Item_MeetFunConfigDetailInfo item) {
        ImageView iv = helper.getView(R.id.item_view_1);
        switch (item.getFuncode()){
            //会议议程
            case InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_AGENDA_BULLETIN_VALUE: {
                iv.setBackground(getContext().getDrawable(R.drawable.menu_agenda_s));
                break;
            }
            //会议资料
            case InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_MATERIAL_VALUE: {
                iv.setBackground(getContext().getDrawable(R.drawable.menu_file_s));
                break;
            }
            //批注文件
            case InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_POSTIL_VALUE: {
                iv.setBackground(getContext().getDrawable(R.drawable.menu_note_s));
                break;
            }
            //会议交流
            case InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_MESSAGE_VALUE: {
                iv.setBackground(getContext().getDrawable(R.drawable.menu_message_s));
                break;
            }
            //视屏直播
            case InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_VIDEOSTREAM_VALUE: {
                iv.setBackground(getContext().getDrawable(R.drawable.menu_video_s));
                break;
            }
            //电子白板
            case InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_WHITEBOARD_VALUE: {
                iv.setBackground(getContext().getDrawable(R.drawable.menu_whiteboard_s));
                break;
            }
            //网页浏览
            case InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_WEBBROWSER_VALUE: {
                iv.setBackground(getContext().getDrawable(R.drawable.menu_web_s));
                break;
            }
            //签到信息
            case InterfaceMacro.Pb_Meet_FunctionCode.Pb_MEET_FUNCODE_SIGNINRESULT_VALUE: {
                iv.setBackground(getContext().getDrawable(R.drawable.menu_checkin_s));
                break;
            }
        }
    }
}
