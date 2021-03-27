package xlk.paperless.standard.view.admin.fragment.mid.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.mogujie.tt.protobuf.InterfaceMacro;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xlk.paperless.standard.R;
import xlk.paperless.standard.adapter.MeetChatMemberAdapter;
import xlk.paperless.standard.adapter.MulitpleItemAdapter;
import xlk.paperless.standard.base.BaseFragment;
import xlk.paperless.standard.data.bean.ChatMessage;
import xlk.paperless.standard.data.bean.DevMember;
import xlk.paperless.standard.util.ToastUtil;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc
 */
public class AdminChatFragment extends BaseFragment implements AdminChatInterface, View.OnClickListener {

    private AdminChatPresenter presenter;
    private RecyclerView rv_member;
    private CheckBox cb_check_all;
    private RecyclerView rv_chat;
    private EditText edt_content;
    private Button btn_send;
    private MulitpleItemAdapter chatAdapter;
    private MeetChatMemberAdapter memberAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_chat, container, false);
        initView(inflate);
        presenter = new AdminChatPresenter(this);
        presenter.queryMember();
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    protected void reShow() {
        presenter.queryMember();
    }

    public void initView(View rootView) {
        this.rv_member = (RecyclerView) rootView.findViewById(R.id.rv_member);
        this.cb_check_all = (CheckBox) rootView.findViewById(R.id.cb_check_all);
        this.rv_chat = (RecyclerView) rootView.findViewById(R.id.rv_chat);
        this.edt_content = (EditText) rootView.findViewById(R.id.edt_content);
        this.btn_send = (Button) rootView.findViewById(R.id.btn_send);
        cb_check_all.setOnClickListener(this);
        btn_send.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cb_check_all:
                cb_check_all.setChecked(cb_check_all.isChecked());
                if (memberAdapter != null) {
                    memberAdapter.setCheckAll(cb_check_all.isChecked());
                }
                break;
            case R.id.btn_send:
                if (memberAdapter == null || memberAdapter.getCheck().isEmpty()) {
                    ToastUtil.show(R.string.choose_member_first);
                    return;
                }
                String content = edt_content.getText().toString().trim();
                if (content.isEmpty()) {
                    ToastUtil.show(R.string.please_enter_content_first);
                    return;
                }
                if (content.length() > InterfaceMacro.Pb_String_LenLimit.Pb_MEETIM_CHAR_MSG_MAXLEN_VALUE) {
                    ToastUtil.show(R.string.exceed_max_words);
                    return;
                }
                presenter.sendChatMessage(content, InterfaceMacro.Pb_MeetIMMSG_TYPE.Pb_MEETIM_CHAT_Message.getNumber(), memberAdapter.getCheck());
                edt_content.setText("");
                break;
            default:
                break;
        }
    }

    @Override
    public void updateMemberRv(List<DevMember> onlineDevMembers) {
        if (memberAdapter == null) {
            memberAdapter = new MeetChatMemberAdapter(R.layout.item_chat_member, onlineDevMembers);
            rv_member.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_member.setAdapter(memberAdapter);
            memberAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                    memberAdapter.setCheck(onlineDevMembers.get(position).getMemberDetailInfo().getPersonid());
                    cb_check_all.setChecked(memberAdapter.isCheckAll());
                }
            });
        } else {
            memberAdapter.notifyDataSetChanged();
            memberAdapter.notifyCheck();
            cb_check_all.setChecked(memberAdapter.isCheckAll());
        }
    }

    @Override
    public void updateChatRv(List<ChatMessage> chatMessage) {
        if (chatAdapter == null) {
            chatAdapter = new MulitpleItemAdapter(getContext(), chatMessage);
            rv_chat.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_chat.setAdapter(chatAdapter);
        } else {
            chatAdapter.notifyDataSetChanged();
        }
        if (!chatMessage.isEmpty()) {
            rv_chat.scrollToPosition(chatMessage.size() - 1);
        }
    }
}
