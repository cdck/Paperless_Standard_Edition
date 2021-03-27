package xlk.paperless.standard.view.fragment.chat;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceIM;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.MeetChatMemberAdapter;
import xlk.paperless.standard.adapter.MulitpleItemAdapter;
import xlk.paperless.standard.data.Values;
import xlk.paperless.standard.data.bean.ChatMessage;
import xlk.paperless.standard.data.bean.DevMember;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.chatonline.ChatVideoActivity;
import xlk.paperless.standard.base.BaseFragment;

import static xlk.paperless.standard.util.ConvertUtil.s2b;
import static xlk.paperless.standard.view.meet.MeetingActivity.chatIsShowing;
import static xlk.paperless.standard.view.meet.MeetingActivity.chatMessages;
import static xlk.paperless.standard.view.meet.MeetingActivity.mBadge;

/**
 * @author xlk
 * @date 2020/3/13
 * @desc 互动交流
 */
public class MeetChatFragment extends BaseFragment implements View.OnClickListener, IMeetChat {
    private final String TAG = "MeetChatFragment-->";
    private RecyclerView m_c_f_member_rv;
    private RecyclerView m_c_f_msg_rv;
    private CheckBox m_c_f_cb;
    private EditText m_c_f_edt;
    private Button m_c_f_send;
    private Button m_c_f_video_chat;
    private MeetChatPresenter presenter;
    private MeetChatMemberAdapter memberAdapter;
    private MulitpleItemAdapter chatAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_chat, container, false);
        chatIsShowing = true;
        initView(inflate);
        presenter = new MeetChatPresenter(getContext(), this);
        presenter.queryMember();
        return inflate;
    }

    private void initView(View inflate) {
        m_c_f_member_rv = (RecyclerView) inflate.findViewById(R.id.m_c_f_member_rv);
        m_c_f_msg_rv = (RecyclerView) inflate.findViewById(R.id.m_c_f_msg_rv);
        m_c_f_cb = (CheckBox) inflate.findViewById(R.id.m_c_f_cb);
        m_c_f_edt = (EditText) inflate.findViewById(R.id.m_c_f_edt);
        m_c_f_send = (Button) inflate.findViewById(R.id.m_c_f_send);
        m_c_f_video_chat = (Button) inflate.findViewById(R.id.m_c_f_video_chat);
        mBadge.setBadgeNumber(0);
        updateMessageRv();

        m_c_f_cb.setOnClickListener(this);
        m_c_f_send.setOnClickListener(this);
        m_c_f_video_chat.setOnClickListener(this);
    }

    @Override
    public void updateMessageRv() {
        if (chatAdapter == null) {
            chatAdapter = new MulitpleItemAdapter(getContext(), chatMessages);
            m_c_f_msg_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            m_c_f_msg_rv.setAdapter(chatAdapter);
        } else {
            chatAdapter.notifyDataSetChanged();
        }
        //移动到最后一条记录
        if (!chatMessages.isEmpty()) {
            m_c_f_msg_rv.smoothScrollToPosition(chatMessages.size() - 1);
        }
    }

    @Override
    public void updateMemberRv(List<DevMember> onLineMembers) {
        if (memberAdapter == null) {
            memberAdapter = new MeetChatMemberAdapter(R.layout.item_chat_member, onLineMembers);
            m_c_f_member_rv.setLayoutManager(new LinearLayoutManager(getContext()));
            m_c_f_member_rv.setAdapter(memberAdapter);
            memberAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    memberAdapter.setCheck(onLineMembers.get(position).getMemberDetailInfo().getPersonid());
                    m_c_f_cb.setChecked(memberAdapter.isCheckAll());
                }
            });
        } else {
            memberAdapter.notifyDataSetChanged();
            memberAdapter.notifyCheck();
            m_c_f_cb.setChecked(memberAdapter.isCheckAll());
        }
    }

    @Override
    public void onClick(View v) {
        if (memberAdapter == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.m_c_f_cb:
                boolean checked = m_c_f_cb.isChecked();
                m_c_f_cb.setChecked(checked);
                memberAdapter.setCheckAll(checked);
                break;
            case R.id.m_c_f_send:
                List<Integer> check = memberAdapter.getCheck();
                if (check.isEmpty()) {
                    ToastUtil.show(R.string.choose_member_first);
                } else {
                    String trim = m_c_f_edt.getText().toString().trim();
                    if (trim.isEmpty()) {
                        ToastUtil.show(R.string.please_enter_message);
                    } else if (trim.length() > InterfaceMacro.Pb_String_LenLimit.Pb_MEETIM_CHAR_MSG_MAXLEN_VALUE) {
                        ToastUtil.show(R.string.exceed_max_words);
                    } else {
                        presenter.sendChatMessage(trim, InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Message.getNumber(), check);
                        InterfaceIM.pbui_Type_MeetIM build = InterfaceIM.pbui_Type_MeetIM.newBuilder()
                                .setMsgtype(0)
                                .setRole(Values.localRole)
                                .setMemberid(Values.localMemberId)
                                .setMsg(s2b(trim))
                                .setUtcsecond(System.currentTimeMillis() / 1000)//需要换算成秒单位
                                .setMeetname(s2b(Values.localMeetingName))
                                .setRoomname(s2b(Values.localRoomName))
                                .setMembername(s2b(Values.localMemberName))
                                .setSeatename(s2b(Values.localDeviceName))
                                .addAllUserids(check)
                                .build();
                        chatMessages.add(new ChatMessage(1, build));
                        chatAdapter.notifyDataSetChanged();
                        m_c_f_msg_rv.scrollToPosition(chatMessages.size() - 1);
                        //清除输入框内容
                        m_c_f_edt.setText("".trim());
                    }
                }
                break;
            case R.id.m_c_f_video_chat:
                jump2LiveVideo();
                break;
        }
    }

    private void jump2LiveVideo() {
        startActivity(new Intent(getContext(), ChatVideoActivity.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        chatIsShowing = !hidden;
        if (chatAdapter != null) {
            chatAdapter.notifyDataSetChanged();
            if (!chatMessages.isEmpty()) {
                m_c_f_msg_rv.scrollToPosition(chatMessages.size() - 1);
            }
        }
        if (chatIsShowing) {
            mBadge.setBadgeNumber(0);
            presenter.queryMember();
        }
    }

}
