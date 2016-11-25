package com.yj.wangjatv.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yj.wangjatv.R;
import com.yj.wangjatv.dialog.OneMsgOneBtnDialog;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.UserInfo;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.utils.StringFilter;
import com.yj.wangjatv.widget.AnyEditTextView;
import com.yj.wangjatv.widget.AnyTextView;

import java.util.Calendar;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 5/21/2016.
 */
public class SignupActivity extends BaseActivity implements View.OnClickListener{

    private ImageButton ib_use_rule;
    private AnyTextView tv_use_rule;
    private AnyEditTextView etv_passwd;
    private AnyEditTextView etv_passwd_confirm;
    private AnyEditTextView etv_nickname;
    private AnyEditTextView etv_email;
    private AnyEditTextView etv_birthday;
    private boolean isDuplicateID = true;
    private boolean isCheckDuplicateID = false;
    private boolean isDuplicateName = true;
    private boolean isCheckDuplicateNickName = false;
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
        setContentView(R.layout.activity_signup);

        StringFilter.setCharacterLimited(this, etv_email, StringFilter.ALLOW_ALPHANUMERIC_MONKEY);
        StringFilter.setCharacterLimited(this, etv_passwd, StringFilter.ALLOW_ALPHANUMERIC);
        StringFilter.setCharacterLimited(this, etv_passwd_confirm, StringFilter.ALLOW_ALPHANUMERIC);
        StringFilter.setCharacterLimited(this, etv_nickname, StringFilter.ALLOW_ALPHANUMERIC_HANGUL);

        etv_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (checkEmail() == true) {
                    etv_email.setTextColor(getResources().getColor(R.color.blue_01bfd6));
                } else {
                    etv_email.setTextColor(getResources().getColor(R.color.black_010101));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etv_passwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (checkPassword() == true) {
                    etv_passwd.setTextColor(getResources().getColor(R.color.blue_01bfd6));
                } else {
                    etv_passwd.setTextColor(getResources().getColor(R.color.black_010101));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etv_passwd_confirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (checkPasswordConfirm() == true) {
                    etv_passwd_confirm.setTextColor(getResources().getColor(R.color.blue_01bfd6));
                } else {
                    etv_passwd_confirm.setTextColor(getResources().getColor(R.color.black_010101));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etv_nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (checkNickname() == true) {
                    etv_nickname.setTextColor(getResources().getColor(R.color.blue_01bfd6));
                } else {
                    etv_nickname.setTextColor(getResources().getColor(R.color.black_010101));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void init() {
        super.init();

        initMenu();

        ib_use_rule = (ImageButton)findViewById(R.id.ib_use_rule);
        tv_use_rule = (AnyTextView)findViewById(R.id.tv_use_rule);
        etv_passwd = (AnyEditTextView)findViewById(R.id.etv_passwd);
        etv_passwd_confirm = (AnyEditTextView)findViewById(R.id.etv_passwd_confirm);
        etv_nickname = (AnyEditTextView)findViewById(R.id.etv_nickname);
        etv_email = (AnyEditTextView)findViewById(R.id.etv_email);
        etv_birthday = (AnyEditTextView)findViewById(R.id.etv_birthday);

        findViewById(R.id.btn_id_duplicate).setOnClickListener(this);
        findViewById(R.id.btn_nickname_duplicate).setOnClickListener(this);
        findViewById(R.id.ib_complete).setOnClickListener(this);
        findViewById(R.id.btn_use_rule).setOnClickListener(this);
        findViewById(R.id.ib_use_rule).setOnClickListener(this);
        findViewById(R.id.btn_birthday).setOnClickListener(this);

        // 영문, 숫자 제한
        StringFilter.setCharacterLimited(this, etv_passwd, StringFilter.ALLOW_ALPHANUMERIC);
        StringFilter.setCharacterLimited(this, etv_passwd_confirm, StringFilter.ALLOW_ALPHANUMERIC);
    }

    private void initMenu() {
        findViewById(R.id.iv_title).setVisibility(View.GONE);
        AnyTextView textView = (AnyTextView)findViewById(R.id.tv_title);
        textView.setVisibility(View.VISIBLE);
        textView.setText(getResources().getString(R.string.activity_signup_title));

        findViewById(R.id.ib_back).setOnClickListener(this);
    }

    private void finishActivity() {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_back:
                finishActivity();
                break;
            case R.id.btn_id_duplicate:
                checkDuplicateID();
                break;
            case R.id.btn_nickname_duplicate:
                checkDuplicateName();
                break;
            case R.id.ib_complete:
                onSignup();
                break;
            case R.id.btn_use_rule: {
                    Intent intent = new Intent(this, RuleActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.ib_use_rule:
                ib_use_rule.setSelected(!ib_use_rule.isSelected());
                break;
            case R.id.btn_birthday:
                Calendar c = Calendar.getInstance();
                m_datePicker = new DatePickerDialog(this, datePickerListener,
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                m_datePicker.show();
                break;
        }
    }

    public void checkDuplicateID() {
        if(CommonUtil.isValidEmail(etv_email.getText().toString()) == false) {
            CommonUtil.showCenterToast(this, R.string.input_invalid_email, Toast.LENGTH_SHORT);
            return;
        }
        isCheckDuplicateID = true;
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);
        server.chkID(this, etv_email.getText().toString() , new Callback<BaseModel>() {
            @Override
            public void onResponse(Response<BaseModel> response) {
                BaseModel model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if(model.status == BaseModel.OK_DATA) {
                        isDuplicateID = false;
                        CommonUtil.showCenterToast(SignupActivity.this, R.string.input_valid_email, Toast.LENGTH_SHORT);
                    }
                    else {
                        isDuplicateID = true;
                        CommonUtil.showCenterToast(SignupActivity.this, R.string.input_duplicate_email, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, SignupActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, SignupActivity.this);
            }
        });
    }

    public void checkDuplicateName() {
        if(etv_nickname.getText().length() < 2) {
            CommonUtil.showCenterToast(this, R.string.password_length_short, Toast.LENGTH_SHORT);
            return;
        }
        isCheckDuplicateNickName = true;
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);
        server.chkName(this, etv_nickname.getText().toString(), new Callback<BaseModel>() {
            @Override
            public void onResponse(Response<BaseModel> response) {
                BaseModel model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        isDuplicateName = false;
                        CommonUtil.showCenterToast(SignupActivity.this, R.string.valid_nickname, Toast.LENGTH_SHORT);
                    } else {
                        isDuplicateName = true;
                        CommonUtil.showCenterToast(SignupActivity.this, R.string.invalid_nickname, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, SignupActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, SignupActivity.this);
            }
        });
    }

    private boolean checkEmail() {
        if(etv_email.getText().length() < 5) {
            //CommonUtil.showCenterToast(this, R.string.email_length_short, Toast.LENGTH_SHORT);
            return false;
        }

        if(CommonUtil.isValidEmail(etv_email.getText().toString()) == false) {
            //CommonUtil.showCenterToast(this, R.string.input_invalid_email, Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }


    private boolean checkPassword() {
        if(etv_passwd.getText().length() < 4) {
            //CommonUtil.showCenterToast(this, R.string.password_length_short, Toast.LENGTH_SHORT);
            return false;
        }
        return true;
    }

    private boolean checkPasswordConfirm() {
        if(etv_passwd_confirm.getText().length() < 4) {
            //CommonUtil.showCenterToast(this, R.string.password_length_short, Toast.LENGTH_SHORT);
            return false;
        }

        if(etv_passwd.getText().toString().equals(etv_passwd_confirm.getText().toString()) == false) {
            return false;
        }

        return true;
    }

    private boolean checkNickname() {
        if(etv_nickname.getText().length() < 2) {
            //CommonUtil.showCenterToast(this, R.string.password_length_short, Toast.LENGTH_SHORT);
            return false;
        }
        return true;
    }

    public void onSignup() {
        if(ib_use_rule.isSelected() == false) {
            CommonUtil.showCenterToast(this, R.string.agree_user_rule, Toast.LENGTH_SHORT);
            return;
        }

        if(checkPassword() == false) {
            CommonUtil.showCenterToast(this, R.string.password_length_short, Toast.LENGTH_SHORT);
            return;
        }

        if(etv_passwd_confirm.getText().length() < 4) {
            CommonUtil.showCenterToast(this, R.string.password_length_short, Toast.LENGTH_SHORT);
            return;
        }

        if(etv_passwd.getText().toString().equals(etv_passwd_confirm.getText().toString()) == false) {
            CommonUtil.showCenterToast(this, R.string.input_password_check, Toast.LENGTH_SHORT);
            return;
        }

        if(checkNickname() == false) {
            CommonUtil.showCenterToast(this, R.string.name_length_short, Toast.LENGTH_SHORT);
            return;
        }

        if(etv_email.getText().length() < 5) {
            CommonUtil.showCenterToast(this, R.string.email_length_short, Toast.LENGTH_SHORT);
            return;
        }

        if(CommonUtil.isValidEmail(etv_email.getText().toString()) == false) {
            CommonUtil.showCenterToast(this, R.string.input_invalid_email, Toast.LENGTH_SHORT);
            return;
        }

        if(isCheckDuplicateID == false) {
            CommonUtil.showCenterToast(this, R.string.check_duplicate_email, Toast.LENGTH_SHORT);
            return;
        }

        if(isCheckDuplicateNickName == false) {
            CommonUtil.showCenterToast(this, R.string.check_duplicate_nickname, Toast.LENGTH_SHORT);
            return;
        }

        if(isDuplicateID == true) {
            CommonUtil.showCenterToast(this, R.string.input_duplicate_email, Toast.LENGTH_SHORT);
            return;
        }

        if(isDuplicateName == true) {
            CommonUtil.showCenterToast(this, R.string.invalid_nickname, Toast.LENGTH_SHORT);
            return;
        }

        if(etv_birthday.getText().length() <= 0) {
            CommonUtil.showCenterToast(this, R.string.check_all_data, Toast.LENGTH_SHORT);
            return;
        }

        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);
        int type = 1;
        String device_id = CommonUtil.getDeviceId(this);
        String push_token = m_app.getPushToken();
        server.signup(this, etv_email.getText().toString(), etv_passwd.getText().toString(), etv_nickname.getText().toString(), etv_birthday.getText().toString(), type, device_id, push_token, new Callback<UserInfo>() {
            @Override
            public void onResponse(Response<UserInfo> response) {
                BaseModel model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {

                        new OneMsgOneBtnDialog(SignupActivity.this, "알림", getResources().getString(R.string.complete_signup), new OneMsgOneBtnDialog.OneMsgOneBtnDialogListner() {
                            @Override
                            public void onClickConfirm() {
                                finishActivity();
                                m_app.set_string_SharedPreferences(KEY_PREF_USER_ID, etv_email.getText().toString());
                                m_app.set_string_SharedPreferences(KEY_PREF_USER_PWD, etv_passwd.getText().toString());
                                SignupActivity.this.m_app.startMainActivity();
                            }
                        }).show();
                    } else {

                        CommonUtil.showCenterToast(SignupActivity.this, R.string.fail_signup, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, SignupActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, SignupActivity.this);
            }
        });
    }
}