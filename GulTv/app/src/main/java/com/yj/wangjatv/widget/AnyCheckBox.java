package com.yj.wangjatv.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.yj.wangjatv.utils.CommonUtil;


/**
 * Created by KCJ on 10/13/2015.
 */
public class AnyCheckBox extends CheckBox {

    public AnyCheckBox(Context context) {
        super(context);
    }

    public AnyCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        CommonUtil.setTypeface(attrs, this);
    }

    public AnyCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        CommonUtil.setTypeface(attrs, this);
    }
}