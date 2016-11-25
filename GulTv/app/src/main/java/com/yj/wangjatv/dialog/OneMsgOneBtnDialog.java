package com.yj.wangjatv.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yj.wangjatv.R;
import com.yj.wangjatv.utils.StringFilter;


public class OneMsgOneBtnDialog extends BaseDialog {

    public interface OneMsgOneBtnDialogListner {
        public void onClickConfirm();
    }

    private TextView tv_Contents;
    private TextView tv_Title;
    private LinearLayout ll_Confirm;

    private String title;
    private String content;

    private Handler mHandler;
    private  OneMsgOneBtnDialogListner mListner = null;

    public OneMsgOneBtnDialog(Context context, String title,String content, OneMsgOneBtnDialogListner listner) {
        super(context);
        setContentView(R.layout.dialog_onemsg_onebtn);
        mListner = listner;

        this.title = title;
        this.content = content;
        init();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    public void setPasswd(String pwd)
    {
        tv_Contents.setText(String.format(content, pwd));
    }

    private void init() {

        tv_Title =  (TextView)findViewById(R.id.textview_dialog_onemsg_onebtn_title);
        tv_Contents = (TextView)findViewById(R.id.tv_content);

        tv_Title.setText(title);
        tv_Contents.setText(content);
        ll_Confirm = (LinearLayout)findViewById(R.id.ll_confirm);

        ((Button)findViewById(R.id.btn_confirm)) .setOnClickListener(button_clicked);
    }

    public void settingFont()
    {
        tv_Contents.setTypeface(mApplicationContext.getTypefaceNanumBoldFont());
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        dismiss();
    }


    View.OnClickListener button_clicked = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_confirm:

                    if (mListner!=null)
                    {
                        mListner.onClickConfirm();
                    }

                    dismiss();

                    break;
                default:
                    break;
            }
        }
    };

}
