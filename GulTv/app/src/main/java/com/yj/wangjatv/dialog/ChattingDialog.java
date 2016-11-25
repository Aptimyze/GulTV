package com.yj.wangjatv.dialog;

import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.yj.wangjatv.R;
import com.yj.wangjatv.adapter.ChattingAdapter;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.widget.AnyEditTextView;
import com.yj.wangjatv.widget.AnyTextView;
import com.yj.wangjatv.xmpp.MessagingListener;

import java.util.List;

/**
 * Created by Ralph on 6/6/2016.
 */
public class ChattingDialog extends BaseDialog implements View.OnClickListener{

    public interface ChattingDialogListner {
        public void onDismiss();
        public void onSendText(String text, ListView listView);
    }

    public ListView list_view;
    private ChattingAdapter adapter;
    private AnyEditTextView etv_text;

    private ChattingDialogListner mListner = null;

    public ChattingDialog(Context context, ChattingAdapter adapter, ChattingDialogListner listner) {
        super(context);
        setContentView(R.layout.dialog_chatting);

        this.adapter = adapter;
        this.mListner = listner;

        list_view = (ListView)findViewById(R.id.list_view);
        list_view.setAdapter(this.adapter);

        adapter.notifyDataSetChanged();

        findViewById(R.id.btn_send).setOnClickListener(this);

        etv_text = (AnyEditTextView)findViewById(R.id.etv_text);
    }

    @Override
    public void onBackPressed() {
        if( ChattingDialog.this.mListner != null) {
            ChattingDialog.this.mListner.onDismiss();
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                if( ChattingDialog.this.mListner != null) {

                    if(etv_text.getText().length() == 0) {
                        CommonUtil.showCenterToast(getContext(), R.string.input_chatting_text, Toast.LENGTH_SHORT);
                        return;
                    }

                    ChattingDialog.this.mListner.onSendText(etv_text.getText().toString(), list_view);
                    etv_text.setText("");
                }
            break;
        }
    }

}
