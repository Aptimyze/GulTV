package com.yj.wangjatv.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.yj.wangjatv.Const;
import com.yj.wangjatv.WangjaTVApp;

/**
 * Created by Ralph on 5/20/2016.
 */
public class BaseDialog  extends Dialog implements Const {
    protected WangjaTVApp mApplicationContext;

    protected Context mContext;

    public BaseDialog(Context context) {
       super(context, android.R.style.Theme_Translucent_NoTitleBar);
       requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.mContext = context;

        mApplicationContext = WangjaTVApp.getInstance();
    }
}
