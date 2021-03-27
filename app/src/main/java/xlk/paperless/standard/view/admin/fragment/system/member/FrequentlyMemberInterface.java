package xlk.paperless.standard.view.admin.fragment.system.member;

import com.mogujie.tt.protobuf.InterfacePerson;

import java.util.List;

/**
 * @author Created by xlk on 2020/9/21.
 * @desc
 */
public interface FrequentlyMemberInterface {
    void updateMemberRv(List<InterfacePerson.pbui_Item_PersonDetailInfo> memberInfos);
}
