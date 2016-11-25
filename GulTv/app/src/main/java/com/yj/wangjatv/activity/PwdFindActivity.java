package com.yj.wangjatv.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.yj.wangjatv.R;
import com.yj.wangjatv.dialog.OneMsgOneBtnDialog;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.FindPwd;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.utils.StringFilter;
import com.yj.wangjatv.widget.AnyEditTextView;
import com.yj.wangjatv.widget.AnyTextView;

import java.util.Calendar;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 5/31/2016.
 */
public class PwdFindActivity  extends BaseActivity implements View.OnClickListener{

    private AnyEditTextView etv_email;
    private AnyEditTextView etv_birthday;

    private DatePickerDialog m_datePicker;

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener(){
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            if (m_datePicker == null) {
                return;
            }
            m_datePicker = null;

            etv_birthday.setText(String.format("%d-%02d-%02d", year, monthOfYear+1, dayOfMonth));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwd_find);

        StringFilter.setCharacterLimited(this, etv_email, StringFilter.ALLOW_ALPHANUMERIC_MONKEY);
    }

    @Override
    public void init() {
        super.init();
        initMenu();

        etv_email = (AnyEditTextView)findViewById(R.id.etv_email);
        etv_birthday = (AnyEditTextView)findViewById(R.id.etv_birthday);
        findViewById(R.id.ib_complete).setOnClickListener(this);
        findViewById(R.id.btn_birthday).setOnClickListener(this);
    }

    private void initMenu() {
        findViewById(R.id.iv_title).setVisibility(View.GONE);
        AnyTextView textView = (AnyTextView)findViewById(R.id.tv_title);
        textView.setVisibility(View.VISIBLE);
        textView.setText(getResources().getString(R.string.activity_find_pwd));

        findViewById(R.id.ib_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.ib_complete:
                onFindPwd();
                break;
            case R.id.btn_birthday:
                Calendar c = Calendar.getInstance();
                m_datePicker = new DatePickerDialog(this, datePickerListener,
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                m_datePicker.show();
                break;
        }
    }

    public void onFindPwd() {
        if(etv_email.getText().length() < 5) {
            CommonUtil.showCenterToast(this, R.string.email_length_short, Toast.LENGTH_SHORT);
            return ;
        }

        if(CommonUtil.isValidEmail(etv_email.getText().toString()) == false) {
            CommonUtil.showCenterToast(this, R.string.input_invalid_email, Toast.LENGTH_SHORT);
            return ;
        }

        if(etv_birthday.getText().toString().isEmpty() == true) {
            CommonUtil.showCenterToast(this, R.string.input_birthday, Toast.LENGTH_SHORT);
            return ;
        }

        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);
        server.findPWd(this, etv_email.getText().toString(), etv_birthday.getText().toString(), new Callback<FindPwd>() {
            @Override
            public void onResponse(Response<FindPwd> response) {
                FindPwd model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        String title = getResources().getString(R.string.dialog_find_pwd_title);
                        String content = getResources().getString(R.string.dialog_find_pwd_content);
                       OneMsgOneBtnDialog dialog = new OneMsgOneBtnDialog(PwdFindActivity.this, title, content, new OneMsgOneBtnDialog.OneMsgOneBtnDialogListner() {
                            @Override
                            public void onClickConfirm() {
                                finish();
                            }
                        });
                        dialog.setPasswd(model.newPwd);
                        dialog.show();

                    } else {
                        CommonUtil.showCenterToast(PwdFindActivity.this, model.msg, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, PwdFindActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, PwdFindActivity.this);
            }
        });
    }
}