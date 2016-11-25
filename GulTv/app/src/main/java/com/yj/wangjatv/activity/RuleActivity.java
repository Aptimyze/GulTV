package com.yj.wangjatv.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.yj.wangjatv.R;
import com.yj.wangjatv.dialog.OneMsgOneBtnDialog;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.Agreement;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.widget.AnyTextView;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 6/3/2016.
 */
public class RuleActivity extends BaseActivity implements View.OnClickListener {
    private TextView tv1, tv2, provisionTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule);
        int type = getIntent().getIntExtra("type", 1);
        if(type == 1) {
            onClick(tv1);
        }
        else {
            onClick(tv2);
        }
    }

    @Override
    public void init() {
        super.init();
        initMenu();
        tv1 = (TextView)findViewById(R.id.provision_tv1);
        tv2 = (TextView)findViewById(R.id.provision_tv2);
        provisionTv = (TextView)findViewById(R.id.provision_tv);

        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
    }

    private void initMenu() {
        findViewById(R.id.iv_title).setVisibility(View.GONE);
        AnyTextView textView = (AnyTextView)findViewById(R.id.tv_title);
        textView.setVisibility(View.VISIBLE);
        textView.setTextColor(getResources().getColor(R.color.colorAccent));
        textView.setText(getResources().getString(R.string.activity_rule));
        findViewById(R.id.ib_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int type = 1;
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.provision_tv1:
                tv1.setSelected(true);
                tv2.setSelected(false);
                type = 1;
                getProvision(type);
                break;
            case R.id.provision_tv2:
                tv1.setSelected(false);
                tv2.setSelected(true);
                type = 2;
                getProvision(type);
                break;
        }
    }

    private void getProvision(int type){
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);

        String strType =  type == 1 ? "use_info" : "pr_info";
        server.getAgreement(this, strType, new Callback<Agreement>() {
            @Override
            public void onResponse(Response<Agreement> response) {
                Agreement model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    String content = "";
                    if (model.status == BaseModel.OK_DATA) {
                        provisionTv.setText(model.text_info);
                    } else {
                        server.processErrorBody(response, RuleActivity.this);
                    }
                } else {
                    server.processErrorBody(response, RuleActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, RuleActivity.this);
            }
        });
    }
}
