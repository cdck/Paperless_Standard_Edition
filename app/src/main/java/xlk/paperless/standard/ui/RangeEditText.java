package xlk.paperless.standard.ui;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xlk.paperless.standard.util.ToastUtil;

/**
 * @author xlk
 * @date 2020/4/11
 * @Description: 自定义限制输入 0-100范围的值
 */
public class RangeEditText extends EditText implements TextWatcher {

    private String outStr="";

    public RangeEditText(Context context) {
        this(context, null);
    }

    public RangeEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RangeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        String edit = s.toString();
        if (edit.length() == 2 && Integer.parseInt(edit) >= 10) {
            outStr = edit;
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String words = s.toString();
        //首先内容进行非空判断，空内容（""和null）不处理
        if (!TextUtils.isEmpty(words)) {
            //1-100的正则验证
            Pattern p = Pattern.compile("^(100|[1-9]\\d|\\d)$");
            Matcher m = p.matcher(words);
            if (m.find() || ("").equals(words)) {
                //这个时候输入的是合法范围内的值
            } else {
                if (words.length() > 2) {
                    //若输入不合规，且长度超过2位，继续输入只显示之前存储的outStr
                    setText(outStr);
                    //重置输入框内容后默认光标位置会回到索引0的地方，要改变光标位置
                    setSelection(2);
                }
                ToastUtil.show(getContext(), "请输入范围在1-100之间的数");
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        //这里的处理是不让输入0开头的值
        String words = s.toString();
        //首先内容进行非空判断，空内容（""和null）不处理
        if (!TextUtils.isEmpty(words)) {
            if (Integer.parseInt(s.toString()) <= 0) {
                setText("");
                ToastUtil.show(getContext(), "请输入范围在1-100之间的数");
            }
        }
    }
}
