package xlk.paperless.standard.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mogujie.tt.protobuf.InterfaceIM;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.bean.ChatMessage;
import xlk.paperless.standard.util.DateUtil;

/**
 * Created by Administrator on 2017/11/13.
 * 会议聊天 消息列表
 */

public class MulitpleItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private List<ChatMessage> data;


    public MulitpleItemAdapter(Context context, List<ChatMessage> data) {
        this.data = data;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new LeftViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_chat_left, parent, false));
        } else {
            return new RightViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_chat_right, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //根据
        ChatMessage chatMessage = data.get(position);
        InterfaceIM.pbui_Type_MeetIM message = chatMessage.getMessage();
        if (holder instanceof LeftViewHolder) {
            //消息类型
            int msgtype = message.getMsgtype();
            //参会人员角色
            int role = message.getRole();
            String msg = message.getMsg().toStringUtf8();
            int memberid = message.getMemberid();
            long utcsecond = message.getUtcsecond();
            String time = DateUtil.getHHss(utcsecond);
            String name = message.getMembername().toStringUtf8();
            String title = mContext.getResources().getString(R.string.time, name, time);
            mContext.getResources().getString(R.string.me_time);
            ((LeftViewHolder) holder).i_m_c_l_message_title.setText(title);
            ((LeftViewHolder) holder).i_m_c_l_message_content.setText(msg);
        } else {
            long utcsecond = message.getUtcsecond();
            String time = DateUtil.getHHss(utcsecond);
            String title = mContext.getResources().getString(R.string.me_time, time);
            ((RightViewHolder) holder).i_m_c_r_message_title.setText(title);
            ((RightViewHolder) holder).i_m_c_r_message_content.setText(message.getMsg().toStringUtf8());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        //true 为接收的消息
        ChatMessage chatMessage = data.get(position);
        return chatMessage.getType();
    }

    //接收 --->>> 其他人发送的消息
    class LeftViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView i_m_c_l_message_title;
        TextView i_m_c_l_message_content;

        public LeftViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            i_m_c_l_message_title = itemView.findViewById(R.id.i_m_c_l_message_title);
            i_m_c_l_message_content = itemView.findViewById(R.id.i_m_c_l_message_content);
        }
    }

    //发送 --->>> 第一人称发送的消息
    class RightViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView i_m_c_r_message_title;
        TextView i_m_c_r_message_content;

        public RightViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            i_m_c_r_message_title = itemView.findViewById(R.id.i_m_c_r_message_title);
            i_m_c_r_message_content = itemView.findViewById(R.id.i_m_c_r_message_content);
        }
    }
}
