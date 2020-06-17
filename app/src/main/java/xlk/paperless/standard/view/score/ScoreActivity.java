package xlk.paperless.standard.view.score;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.protobuf.ByteString;
import com.mogujie.tt.protobuf.InterfaceFilescorevote;

import java.util.ArrayList;
import java.util.List;

import xlk.paperless.standard.R;
import xlk.paperless.standard.data.Constant;
import xlk.paperless.standard.data.JniHandler;
import xlk.paperless.standard.util.LogUtil;
import xlk.paperless.standard.util.ToastUtil;
import xlk.paperless.standard.view.BaseActivity;
import xlk.paperless.standard.view.MyApplication;

public class ScoreActivity extends BaseActivity implements IScore, View.OnClickListener {

    private final String TAG = "ScoreActivity-->";
    private TextView score_desc_tv;
    private TextView score_file_tv;
    private TextView score_register_tv;
    private TextView score_option1_tv;
    private EditText score_option1_edt;
    private TextView score_option2_tv;
    private EditText score_option2_edt;
    private TextView score_option3_tv;
    private EditText score_option3_edt;
    private TextView score_option4_tv;
    private EditText score_option4_edt;
    private EditText score_opinion_edt;
    private Button score_submit;
    private Button score_give_up;
    private ScorePresenter presenter;
    private TextView scope1, scope2, scope3, scope4;
    private int voteid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        initView();
        presenter = new ScorePresenter(this, this);
        presenter.register();
        voteid = getIntent().getIntExtra("voteid", 0);
        presenter.queryScoreById(voteid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unregister();
    }

    private void initView() {
        score_desc_tv = (TextView) findViewById(R.id.score_desc_tv);
        score_file_tv = (TextView) findViewById(R.id.score_file_tv);
        score_register_tv = (TextView) findViewById(R.id.score_register_tv);
        score_option1_tv = (TextView) findViewById(R.id.score_option1_tv);
        score_option1_edt = (EditText) findViewById(R.id.score_option1_edt);
        score_option2_tv = (TextView) findViewById(R.id.score_option2_tv);
        score_option2_edt = (EditText) findViewById(R.id.score_option2_edt);
        score_option3_tv = (TextView) findViewById(R.id.score_option3_tv);
        score_option3_edt = (EditText) findViewById(R.id.score_option3_edt);
        score_option4_tv = (TextView) findViewById(R.id.score_option4_tv);
        score_option4_edt = (EditText) findViewById(R.id.score_option4_edt);
        score_opinion_edt = (EditText) findViewById(R.id.score_opinion_edt);
        scope1 = (TextView) findViewById(R.id.scope1);
        scope2 = (TextView) findViewById(R.id.scope2);
        scope3 = (TextView) findViewById(R.id.scope3);
        scope4 = (TextView) findViewById(R.id.scope4);
        score_submit = (Button) findViewById(R.id.score_submit);
        score_give_up = (Button) findViewById(R.id.score_give_up);

        score_submit.setOnClickListener(this);
        score_give_up.setOnClickListener(this);
    }

    @Override
    public void close(int id) {
        if (voteid == id) {
            finish();
        }
    }

    @Override
    public void update(InterfaceFilescorevote.pbui_Type_Item_UserDefineFileScore info) {
        score_desc_tv.setText(info.getContent().toStringUtf8());
        score_file_tv.setText(JniHandler.getInstance().getFileName(info.getFileid()));
        score_register_tv.setText(info.getMode() == 1 ? getString(R.string.mode_register) : getString(R.string.mode_anonymous));
        int selectcount = info.getSelectcount();
        List<ByteString> voteTextList = info.getVoteTextList();
        setVisible(false, 0);
        setVisible(false, 1);
        setVisible(false, 2);
        setVisible(false, 3);
        for (int i = 0; i < voteTextList.size(); i++) {
            ByteString bytes = voteTextList.get(i);
            setVisible(true, i);
            if (i == 0) score_option1_tv.setText(bytes.toStringUtf8());
            if (i == 1) score_option2_tv.setText(bytes.toStringUtf8());
            if (i == 2) score_option3_tv.setText(bytes.toStringUtf8());
            if (i == 3) score_option4_tv.setText(bytes.toStringUtf8());
        }
    }

    public void setVisible(boolean visible, int index) {
        switch (index) {
            case 0:
                score_option1_tv.setVisibility(visible ? View.VISIBLE : View.GONE);
                score_option1_edt.setVisibility(visible ? View.VISIBLE : View.GONE);
                scope1.setVisibility(visible ? View.VISIBLE : View.GONE);
                break;
            case 1:
                score_option2_tv.setVisibility(visible ? View.VISIBLE : View.GONE);
                score_option2_edt.setVisibility(visible ? View.VISIBLE : View.GONE);
                scope2.setVisibility(visible ? View.VISIBLE : View.GONE);
                break;
            case 2:
                score_option3_tv.setVisibility(visible ? View.VISIBLE : View.GONE);
                score_option3_edt.setVisibility(visible ? View.VISIBLE : View.GONE);
                scope3.setVisibility(visible ? View.VISIBLE : View.GONE);
                break;
            case 3:
                score_option4_tv.setVisibility(visible ? View.VISIBLE : View.GONE);
                score_option4_edt.setVisibility(visible ? View.VISIBLE : View.GONE);
                scope4.setVisibility(visible ? View.VISIBLE : View.GONE);
                break;
        }
    }

    public boolean isMorethan() {
        String option1 = score_option1_edt.getText().toString().trim();
        String option2 = score_option2_edt.getText().toString().trim();
        String option3 = score_option3_edt.getText().toString().trim();
        String option4 = score_option4_edt.getText().toString().trim();
        if (!option1.isEmpty() && (Integer.parseInt(option1) > 100)) {
            return true;
        }
        if (!option2.isEmpty() && (Integer.parseInt(option2) > 100)) {
            return true;
        }
        if (!option3.isEmpty() && (Integer.parseInt(option3) > 100)) {
            return true;
        }
        return !option4.isEmpty() && (Integer.parseInt(option4) > 100);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.score_submit:
                if (!isMorethan()) {
                    String opinion = score_opinion_edt.getText().toString().trim();
                    int length = opinion.length();
                    LogUtil.d(TAG, "输入的意见文本长度 -->" + length);
                    if (length > 50) {
                        //InterfaceFilescorevote.Pb_FILESCOREVOTE_LenLimit.Pb_MEET_FILESCORE_VOTECONTENT_MAXLEN_VALUE
                        ToastUtil.show(getString(R.string.opinion_text_more_than, String.valueOf(50)));
                    } else {
                        String option1 = score_option1_edt.getText().toString().trim();
                        String option2 = score_option2_edt.getText().toString().trim();
                        String option3 = score_option3_edt.getText().toString().trim();
                        String option4 = score_option4_edt.getText().toString().trim();
                        List<Integer> allscore = new ArrayList<>();
                        allscore.add(Integer.parseInt(option1));
                        allscore.add(Integer.parseInt(option2));
                        allscore.add(Integer.parseInt(option3));
                        allscore.add(Integer.parseInt(option4));
                        JniHandler.getInstance().submitScore(voteid, MyApplication.localMemberId, opinion, allscore);
                        finish();
                    }
                } else {
                    ToastUtil.show(R.string.more_than_100);
                }
                break;
            case R.id.score_give_up:
                finish();
                break;
        }
    }
}
