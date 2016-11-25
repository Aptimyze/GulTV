package com.yj.wangjatv.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.yj.wangjatv.utils.CommonUtil;


public class AnyButton extends Button {

    public AnyButton(Context context) {
        super(context);
    }

    public AnyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        CommonUtil.setTypeface(attrs, this);
    }

    public AnyButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        CommonUtil.setTypeface(attrs, this);
    }
}
