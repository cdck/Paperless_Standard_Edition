package xlk.paperless.standard.view.admin.fragment.mid.chat;

import java.util.List;

import xlk.paperless.standard.data.bean.ChatMessage;
import xlk.paperless.standard.data.bean.DevMember;

/**
 * @author Created by xlk on 2020/10/26.
 * @desc
 */
public interface AdminChatInterface {
    /**
     * 更新聊天信息列表
     * @param chatMessage 聊天信息
     */
    void updateChatRv(List<ChatMessage> chatMessage);

    /**
     * 更新在线参会人
     * @param onlineDevMembers 在线参会人信息
     */
    void updateMemberRv(List<DevMember> onlineDevMembers);
}
