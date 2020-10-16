package xlk.paperless.standard.view.admin.fragment.pre.meetingManage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xlk.paperless.standard.R;
import xlk.paperless.standard.base.BaseFragment;

/**
 * @author Created by xlk on 2020/10/14.
 * @desc
 */
public class MeetingManageFragment extends BaseFragment implements MeetingManageInterface{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.admin_fragment_meeting_manage, container, false);
        return inflate;
    }
}
