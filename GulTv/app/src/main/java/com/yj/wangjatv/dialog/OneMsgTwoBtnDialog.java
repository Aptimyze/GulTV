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

import com.yj.wangjatv.Const;
import com.yj.wangjatv.R;


public class OneMsgTwoBtnDialog extends BaseDialog implements Const{

    public interface OneMsgTwoBtnDialogListner {
        public void onClickYes();
        public void onClickNo();
    }

    private TextView tv_Contents;
    private TextView tv_Title;
    private Button ibtn_Yes, ibtn_No;
    private String title, content;

    private OneMsgTwoBtnDialogListner mListner;

    public OneMsgTwoBtnDialog(Context context, String title,String content, OneMsgTwoBtnDialogListner listner) {
        super(context);

        mListner = listner;

        setContentView(R.layout.dialog_onemsg_twobtn);

        this.title = title;
        this.content = content;

        init();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    private void init() {

        tv_Contents = (TextView)findViewById(R.id.textview_dialog_onemsg_twobtn_contents);
        tv_Title = (TextView)findViewById(R.id.textview_dialog_onemsg_twobtn_title);
        ibtn_Yes = (Button)findViewById(R.id.imagebutton_dialog_onemsg_twobtn_yes);
        ibtn_No = (Button)findViewById(R.id.imagebutton_dialog_onemsg_twobtn_no);

        tv_Title.setText( this.title);
        tv_Contents.setText(this.content);

        ibtn_Yes.setOnClickListener(button_clicked);
        ibtn_No.setOnClickListener(button_clicked);
    }

    public void settingFont()
    {
        tv_Contents.setTypeface(mApplicationContext.getTypefaceNanumBoldFont());
    }

    public void setContents(String contents)
    {
        tv_Contents.setText(contents);
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
                case R.id.imagebutton_dialog_onemsg_twobtn_yes:
                    if(mListner != null) {
                        mListner.onClickYes();
                    }
                    dismiss();
                    break;
                case R.id.imagebutton_dialog_onemsg_twobtn_no:

                    if(mListner != null) {
                        mListner.onClickNo();
                    }
                    dismiss();

                    break;
                default:
                    break;
            }
        }
    };

}
