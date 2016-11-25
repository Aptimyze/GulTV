package com.yj.wangjatv.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.Preference;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.viewpagerindicator.TabPageIndicator;
import com.yj.wangjatv.R;
import com.yj.wangjatv.http.APIInterface;
import com.yj.wangjatv.http.APIInterface.*;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.UserInfo;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.utils.StringFilter;
import com.yj.wangjatv.widget.AnyEditTextView;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 5/21/2016.
 */
public class LoginActivity  extends BaseActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    AnyEditTextView etv_email;
    AnyEditTextView etv_passwd;

    private int m_nLoginAttemptCnt = 0;

    private ViewPager mViewPager;
    private TabPageIndicator mIndicator;
    private int []m_arrGuide = {R.drawable.bg_guide_1,R.drawable.bg_guide_2,R.drawable.bg_guide_3,R.drawable.bg_guide_4};
    private ImageView mIvFirstDot;
    private ImageView mIvSecondDot;
    private ImageView mIvThirdDot;
    private ImageView mIvFourthDot;

    private PagerAdapter mGuidAdapter;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
           CommonUtil.killProcess(LoginActivity.this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    public void init() {
        super.init();

        findViewById(R.id.ib_back).setOnClickListener(this);
        findViewById(R.id.ib_login).setOnClickListener(this);
        findViewById(R.id.ib_signup).setOnClickListener(this);
        findViewById(R.id.btn_find_id_passwd).setOnClickListener(this);

        etv_email = (AnyEditTextView)findViewById(R.id.etv_email);
        etv_passwd = (AnyEditTextView)findViewById(R.id.etv_passwd);

        // 영문, 숫자 제한
        StringFilter.setCharacterLimited(this, etv_passwd, StringFilter.ALLOW_ALPHANUMERIC);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
        mIndicator.setOnPageChangeListener(this);

        mGuidAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return m_arrGuide.length;
            }

            class ViewHolder {
                private ImageView iv_guide;
            }

            @Override
            public Object instantiateItem(final ViewGroup container, int position) {
                final int drawable = m_arrGuide[position];

                View convertView = null;
                ViewHolder holder = null;

                convertView = LayoutInflater.from(LoginActivity.this).inflate(R.layout.item_guide, null);
                holder = new ViewHolder();
                holder.iv_guide = (ImageView) convertView.findViewById(R.id.iv_guide);
                convertView.setTag(holder);

                holder.iv_guide.setImageResource(drawable);

                ((ViewPager) container).addView(convertView, 0);

                return convertView;
            }

            @Override
            public void destroyItem(View pager, int position, Object view) {
                ((ViewPager) pager).removeView((View) view);
            }

            @Override
            public boolean isViewFromObject(View pager, Object obj) {
                return pager == obj;
            }

            @Override
            public void restoreState(Parcelable arg0, ClassLoader arg1) {
            }

            @Override
            public Parcelable saveState() {
                return null;
            }

            @Override
            public void startUpdate(View arg0) {
            }

            @Override
            public void finishUpdate(View arg0) {
            }
        };

        mViewPager.setAdapter(mGuidAdapter);
        mIndicator.setViewPager(mViewPager);

        mIvFirstDot = (ImageView) findViewById(R.id.iv_first_dot);
        mIvSecondDot = (ImageView) findViewById(R.id.iv_second_dot);
        mIvThirdDot = (ImageView)  findViewById(R.id.iv_third_dot);
        mIvFourthDot = (ImageView) findViewById(R.id.iv_fourth_dot);
    }

    private void initDots() {
        mIvFirstDot.setBackgroundResource(R.drawable.ic_indicator_off);
        mIvSecondDot.setBackgroundResource(R.drawable.ic_indicator_off);
        mIvThirdDot.setBackgroundResource(R.drawable.ic_indicator_off);
        mIvFourthDot.setBackgroundResource(R.drawable.ic_indicator_off);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int position) {

        initDots();

        switch (position) {
            case 0:
                mIvFirstDot.setBackgroundResource(R.drawable.ic_indicator_on);
                break;
            case 1:
                mIvSecondDot.setBackgroundResource(R.drawable.ic_indicator_on);
                break;
            case 2:
                mIvThirdDot.setBackgroundResource(R.drawable.ic_indicator_on);
                break;
            case 3:
                mIvFourthDot.setBackgroundResource(R.drawable.ic_indicator_on);
                break;
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_back :
                finish();
                break;
            case R.id.ib_login:
                onLogin();
                break;
            case R.id.ib_signup: {
                Intent intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.btn_find_id_passwd: {
                Intent intent = new Intent(this, PwdFindActivity.class);
                startActivity(intent);
            }
                break;
        }
    }

    private void finishActivity() {
        finish();
    }

    public  void onLogin() {
        if(etv_email.getText().length() < 5) {
            CommonUtil.showCenterToast(this, R.string.email_length_short, Toast.LENGTH_SHORT);
            return;
        }

        if(CommonUtil.isValidEmail(etv_email.getText().toString()) == false) {
            CommonUtil.showCenterToast(this, R.string.input_invalid_email, Toast.LENGTH_SHORT);
            return;
        }

        if(etv_passwd.getText().length() < 4) {
            CommonUtil.showCenterToast(this, R.string.password_length_short, Toast.LENGTH_SHORT);
            return;
        }

        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);

        int type = 1;
        String device_id = CommonUtil.getDeviceId(this);
        String push_token = m_app.getPushToken();

        server.login(this, etv_email.getText().toString(), etv_passwd.getText().toString(), type, device_id, push_token, new Callback<UserInfo>() {
            @Override
            public void onResponse(Response<UserInfo> response) {
                UserInfo model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if(model.status == BaseModel.OK_DATA) {
                        m_app.set_string_SharedPreferences(KEY_PREF_USER_ID, etv_email.getText().toString());
                        m_app.set_string_SharedPreferences(KEY_PREF_USER_PWD, etv_passwd.getText().toString());
                        m_app.getUserInfo().copy(model);
                        m_app.getUserInfo().is_loginned = true;

                        finishActivity();
                        m_app.startMainActivity();
                    }
                    else {
                        m_nLoginAttemptCnt++;
                        if(m_nLoginAttemptCnt == 5) {
                            CommonUtil.showCenterToast(LoginActivity.this, R.string.try_login_again, Toast.LENGTH_SHORT);
                            mHandler.sendEmptyMessageDelayed(0, 2000);
                        }
                        else {
                            CommonUtil.showCenterToast(LoginActivity.this, R.string.no_match_email_passwd, Toast.LENGTH_SHORT);
                        }
                    }

                } else {
                    server.processErrorBody(response, LoginActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, LoginActivity.this);
            }
        });
    }
}
