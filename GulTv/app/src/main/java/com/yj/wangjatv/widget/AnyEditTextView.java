package com.yj.wangjatv.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;


public class AnyEditTextView extends EditText {

    public AnyEditTextView(Context context) {
        super(context);
    }

    public AnyEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        com.yj.wangjatv.utils.CommonUtil.setTypeface(attrs, this);
    }

    public AnyEditTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        com.yj.wangjatv.utils.CommonUtil.setTypeface(attrs, this);
    }

}
