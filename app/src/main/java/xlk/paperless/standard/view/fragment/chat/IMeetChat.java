package xlk.paperless.standard.view.fragment.chat;

import java.util.List;

import xlk.paperless.standard.data.bean.DevMember;

/**
 * @author xlk
 * @date 2020/3/17
 * @Description:
 */
public interface IMeetChat {
    void updateMemberRv(List<DevMember> onLineMembers);

    void updateMessageRv();

}
