package xlk.paperless.standard.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import xlk.paperless.standard.R;

/**
 * @author xlk
 * @date 2020/4/24
 * @desc
 */
public class CustomBaseViewHolder {
    public static class MenuViewHolder {
        public View rootView;
        public Button wm_menu_note;
        public Button wm_menu_soft;
        public Button wm_menu_hand;
        public Button wm_menu_service;
        public Button wm_menu_start_projection;
        public Button wm_menu_back;
        public Button wm_menu_stop_projection;
        public Button wm_menu_start_screen;
        public Button wm_menu_join_screen;
        public Button wm_menu_stop_screen;
        public Button wm_menu_screenshot;

        public MenuViewHolder(View rootView) {
            this.rootView = rootView;
            this.wm_menu_note = (Button) rootView.findViewById(R.id.wm_menu_note);
            this.wm_menu_soft = (Button) rootView.findViewById(R.id.wm_menu_soft);
            this.wm_menu_hand = (Button) rootView.findViewById(R.id.wm_menu_hand);
            this.wm_menu_service = (Button) rootView.findViewById(R.id.wm_menu_service);
            this.wm_menu_start_projection = (Button) rootView.findViewById(R.id.wm_menu_start_projection);
            this.wm_menu_back = (Button) rootView.findViewById(R.id.wm_menu_back);
            this.wm_menu_stop_projection = (Button) rootView.findViewById(R.id.wm_menu_stop_projection);
            this.wm_menu_start_screen = (Button) rootView.findViewById(R.id.wm_menu_start_screen);
            this.wm_menu_join_screen = (Button) rootView.findViewById(R.id.wm_menu_join_screen);
            this.wm_menu_stop_screen = (Button) rootView.findViewById(R.id.wm_menu_stop_screen);
            this.wm_menu_screenshot = (Button) rootView.findViewById(R.id.wm_menu_screenshot);
        }

    }

    public static class ServiceViewHolder {
        public View rootView;
        public TextView textView;
        public ImageView wm_service_close;
        public Button wm_service_pen;
        public Button wm_service_pager;
        public Button wm_service_tea;
        public Button wm_service_calculate;
        public Button wm_service_waiter;
        public Button wm_service_clean;
        public EditText wm_service_edt;
        public Button wm_service_send;

        public ServiceViewHolder(View rootView) {
            this.rootView = rootView;
            this.textView = (TextView) rootView.findViewById(R.id.textView);
            this.wm_service_close = (ImageView) rootView.findViewById(R.id.wm_service_close);
            this.wm_service_pen = (Button) rootView.findViewById(R.id.wm_service_pen);
            this.wm_service_pager = (Button) rootView.findViewById(R.id.wm_service_pager);
            this.wm_service_tea = (Button) rootView.findViewById(R.id.wm_service_tea);
            this.wm_service_calculate = (Button) rootView.findViewById(R.id.wm_service_calculate);
            this.wm_service_waiter = (Button) rootView.findViewById(R.id.wm_service_waiter);
            this.wm_service_clean = (Button) rootView.findViewById(R.id.wm_service_clean);
            this.wm_service_edt = (EditText) rootView.findViewById(R.id.wm_service_edt);
            this.wm_service_send = (Button) rootView.findViewById(R.id.wm_service_send);
        }

    }

    public static class ScreenViewHolder {
        public View rootView;
        public CheckBox wm_screen_mandatory;
        public TextView wm_screen_title;
        public TextView textView2;
        public CheckBox wm_screen_cb_attendee;
        public Button wm_screen_launch;
        public Button wm_screen_cancel;
        public CheckBox wm_screen_cb_projector;
        public RecyclerView wm_screen_rv_attendee;
        public RecyclerView wm_screen_rv_projector;

        public ScreenViewHolder(View rootView) {
            this.rootView = rootView;
            this.wm_screen_mandatory = (CheckBox) rootView.findViewById(R.id.wm_screen_mandatory);
            this.wm_screen_title = (TextView) rootView.findViewById(R.id.wm_screen_title);
            this.textView2 = (TextView) rootView.findViewById(R.id.textView2);
            this.wm_screen_cb_attendee = (CheckBox) rootView.findViewById(R.id.wm_screen_cb_attendee);
            this.wm_screen_launch = (Button) rootView.findViewById(R.id.wm_screen_launch);
            this.wm_screen_cancel = (Button) rootView.findViewById(R.id.wm_screen_cancel);
            this.wm_screen_cb_projector = (CheckBox) rootView.findViewById(R.id.wm_screen_cb_projector);
            this.wm_screen_rv_attendee = (RecyclerView) rootView.findViewById(R.id.wm_screen_rv_attendee);
            this.wm_screen_rv_projector = (RecyclerView) rootView.findViewById(R.id.wm_screen_rv_projector);
        }

    }

    public static class ProViewHolder {
        public View rootView;
        public CheckBox wm_pro_mandatory;
        public TextView wm_pro_title;
        public CheckBox wm_pro_all;
        public RecyclerView wm_pro_rv;
        public CheckBox wm_pro_full;
        public CheckBox wm_pro_flow1;
        public CheckBox wm_pro_flow2;
        public CheckBox wm_pro_flow3;
        public CheckBox wm_pro_flow4;
        public Button wm_pro_launch_pro;
        public Button wm_pro_cancel;

        public ProViewHolder(View rootView) {
            this.rootView = rootView;
            this.wm_pro_mandatory = (CheckBox) rootView.findViewById(R.id.wm_pro_mandatory);
            this.wm_pro_title = (TextView) rootView.findViewById(R.id.wm_pro_title);
            this.wm_pro_all = (CheckBox) rootView.findViewById(R.id.wm_pro_all);
            this.wm_pro_rv = (RecyclerView) rootView.findViewById(R.id.wm_pro_rv);
            this.wm_pro_full = (CheckBox) rootView.findViewById(R.id.wm_pro_full);
            this.wm_pro_flow1 = (CheckBox) rootView.findViewById(R.id.wm_pro_flow1);
            this.wm_pro_flow2 = (CheckBox) rootView.findViewById(R.id.wm_pro_flow2);
            this.wm_pro_flow3 = (CheckBox) rootView.findViewById(R.id.wm_pro_flow3);
            this.wm_pro_flow4 = (CheckBox) rootView.findViewById(R.id.wm_pro_flow4);
            this.wm_pro_launch_pro = (Button) rootView.findViewById(R.id.wm_pro_launch_pro);
            this.wm_pro_cancel = (Button) rootView.findViewById(R.id.wm_pro_cancel);
        }

    }

    public static class VoteViewHolder {
        public View rootView;
        public TextView wm_vote_title;
        public TextView wm_vote_type;
        public CheckBox checkBox1;
        public CheckBox checkBox2;
        public CheckBox checkBox3;
        public CheckBox checkBox4;
        public CheckBox checkBox5;
        public Chronometer wm_vote_chronometer;
        public Button wm_vote_submit;

        public VoteViewHolder(View rootView) {
            this.rootView = rootView;
            this.wm_vote_title = (TextView) rootView.findViewById(R.id.wm_vote_title);
            this.wm_vote_type = (TextView) rootView.findViewById(R.id.wm_vote_type);
            this.checkBox1 = (CheckBox) rootView.findViewById(R.id.checkBox1);
            this.checkBox2 = (CheckBox) rootView.findViewById(R.id.checkBox2);
            this.checkBox3 = (CheckBox) rootView.findViewById(R.id.checkBox3);
            this.checkBox4 = (CheckBox) rootView.findViewById(R.id.checkBox4);
            this.checkBox5 = (CheckBox) rootView.findViewById(R.id.checkBox5);
            this.wm_vote_chronometer = (Chronometer) rootView.findViewById(R.id.wm_vote_chronometer);
            this.wm_vote_submit = (Button) rootView.findViewById(R.id.wm_vote_submit);
        }

    }
}
