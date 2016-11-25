package com.yj.wangjatv.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.yj.wangjatv.utils.CommonUtil;

public class AnyTextView extends TextView {

    public AnyTextView(Context context) {
        super(context);
    }

    public AnyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        CommonUtil.setTypeface(attrs, this);
    }

    public AnyTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        CommonUtil.setTypeface(attrs, this);
    }
}
