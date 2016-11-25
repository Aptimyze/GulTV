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
import android.widget.Toast;

import com.yj.wangjatv.Const;
import com.yj.wangjatv.R;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.utils.StringFilter;
import com.yj.wangjatv.widget.AnyEditTextView;


public class HeartDialog extends BaseDialog implements Const{

    public interface HeartDialogListner {
        public void onClickHeart(int sendcnt);
        public void onClickPurchase();
        public void onClickNo();
    }

    private AnyEditTextView etv_Contents;
    private TextView tv_Title;
    private Button btn_heart, btn_purchase, btn_no;

    public   int heart;
    private HeartDialogListner mListner;

    public HeartDialog(Context context,int heart, HeartDialogListner listner) {
        super(context);

        mListner = listner;
        this.heart = heart;
        setContentView(R.layout.dialog_heart);
        init();

        tv_Title.setText(String.format(context.getResources().getString(R.string.my_heart_count), heart));
        etv_Contents.setText(String.format("%d", heart));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    private void init() {

        etv_Contents = (AnyEditTextView)findViewById(R.id.etv_heart);
        tv_Title = (TextView)findViewById(R.id.textview_dialog_onemsg_twobtn_title);
        btn_heart = (Button)findViewById(R.id.btn_heart);
        btn_purchase = (Button)findViewById(R.id.btn_purchase);
        btn_no = (Button)findViewById(R.id.imagebutton_dialog_onemsg_twobtn_no);
        btn_heart.setOnClickListener(button_clicked);
        btn_purchase.setOnClickListener(button_clicked);
        btn_no.setOnClickListener(button_clicked);
    }

    public void setListner(HeartDialogListner listner) {
        this.mListner = listner;
    }

    public String getPassword() {
        return etv_Contents.getText().toString();
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
                case R.id.btn_heart:
                    if(mListner != null) {
                        String heartCnt = etv_Contents.getText().toString();
                        int sendcnt = 0;
                        try {
                            sendcnt = Integer.parseInt(heartCnt);
                        }
                        catch (Exception e) {

                        }
                        mListner.onClickHeart(sendcnt);
                    }
                    break;
                case R.id.btn_purchase:
                    if(mListner != null) {
                        mListner.onClickPurchase();
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
