package com.yj.wangjatv.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yj.wangjatv.Const;
import com.yj.wangjatv.R;
import com.yj.wangjatv.widget.AnyEditTextView;


public class InputPasswordDialog extends BaseDialog implements Const{

    private AnyEditTextView etv_Contents;
    private TextView tv_Title;
    private Button ibtn_Yes, ibtn_No;

    private OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner mListner;

    public InputPasswordDialog(Context context) {
        super(context);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.dialog_input_passwd);
        init();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    private void init() {

        etv_Contents = (AnyEditTextView)findViewById(R.id.etv_passwd);
        tv_Title = (TextView)findViewById(R.id.textview_dialog_onemsg_twobtn_title);
        ibtn_Yes = (Button)findViewById(R.id.imagebutton_dialog_onemsg_twobtn_yes);
        ibtn_No = (Button)findViewById(R.id.imagebutton_dialog_onemsg_twobtn_no);

        ibtn_Yes.setOnClickListener(button_clicked);
        ibtn_No.setOnClickListener(button_clicked);
    }

    public String getPassword() {
        return etv_Contents.getText().toString();
    }
    public void setListner( OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner listner) {
        this.mListner = listner;
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
