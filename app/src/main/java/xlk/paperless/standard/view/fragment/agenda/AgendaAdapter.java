package xlk.paperless.standard.view.fragment.agenda;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceAgenda;
import com.mogujie.tt.protobuf.InterfaceMacro;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import androidx.cardview.widget.CardView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.util.DateUtil;

/**
 * @author Created by xlk on 2021/4/27.
 * @desc
 */
public class AgendaAdapter extends BaseQuickAdapter<InterfaceAgenda.pbui_ItemAgendaTimeInfo, BaseViewHolder> {
    private int agendaId = -1;

    public AgendaAdapter(@Nullable List<InterfaceAgenda.pbui_ItemAgendaTimeInfo> data) {
        super(R.layout.item_agenda, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, InterfaceAgenda.pbui_ItemAgendaTimeInfo item) {
        String sTime = DateUtil.secondFormatDateTime(item.getStartutctime());
        String eTime = DateUtil.secondFormatDateTime(item.getEndutctime());
        holder.setText(R.id.tv_number, String.valueOf(holder.getLayoutPosition() + 1))
                .setText(R.id.tv_agenda_content, item.getDesctext().toStringUtf8())
                .setText(R.id.tv_agenda_time, sTime + " - " + eTime);
        CardView item_agenda_view = holder.getView(R.id.item_agenda_view);
        TextView tv_agenda_status = holder.getView(R.id.tv_agenda_status);
        ImageView iv_status_icon = holder.getView(R.id.iv_status_icon);
        Button btn_agenda = holder.getView(R.id.btn_agenda);
        int status = item.getStatus();
        if (status == InterfaceMacro.Pb_AgendaStatus.Pb_MEETAGENDA_STATUS_IDLE_VALUE) {
            iv_status_icon.setImageResource(R.drawable.icon_agenda_notstart);
            tv_agenda_status.setText(R.string.not_started);
            tv_agenda_status.setTextColor(getContext().getColor(R.color.agenda_not_started_color));
            btn_agenda.setVisibility(View.VISIBLE);
            btn_agenda.setSelected(false);
            btn_agenda.setText(getContext().getString(R.string.start));
        } else if (status == InterfaceMacro.Pb_AgendaStatus.Pb_MEETAGENDA_STATUS_RUNNING_VALUE) {
            iv_status_icon.setImageResource(R.drawable.icon_agenda_ongoing);
            tv_agenda_status.setText(R.string.Ongoing);
            tv_agenda_status.setTextColor(getContext().getColor(R.color.agenda_processing_color));
            btn_agenda.setVisibility(View.VISIBLE);
            btn_agenda.setSelected(true);
            btn_agenda.setText(getContext().getString(R.string.end));
        } else {
            iv_status_icon.setImageResource(R.drawable.icon_agenda_end);
            tv_agenda_status.setText(R.string.over);
            tv_agenda_status.setTextColor(getContext().getColor(R.color.agenda_over_color));
            btn_agenda.setVisibility(View.INVISIBLE);
        }
        item_agenda_view.setCardBackgroundColor(agendaId == item.getAgendaid()
                ? getContext().getColor(R.color.item_selected_bg_color)
                : getContext().getColor(R.color.transparent));
    }

    public void choose(int agendaid) {
        agendaId = agendaid;
        notifyDataSetChanged();
    }
}
