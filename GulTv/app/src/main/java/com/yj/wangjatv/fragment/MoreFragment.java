package com.yj.wangjatv.fragment;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yj.wangjatv.R;
import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.activity.GuideActivity;
import com.yj.wangjatv.activity.NoticeActivity;
import com.yj.wangjatv.activity.ProfileModifyActivity;
import com.yj.wangjatv.activity.PurchaseItemActivity;
import com.yj.wangjatv.dialog.OneMsgOneBtnDialog;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.utils.CommonUtil;

import java.util.ArrayList;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 5/21/2016.
 */
public class MoreFragment extends BaseFragment implements View.OnClickListener{

    ImageButton ib_auto_login, ib_g_connect, ib_auto_rotate, ib_set_alarm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        setView(view, inflater, container);

        setting();
        return view;
    }

    public static MoreFragment newInstance() {
        MoreFragment fragment = new MoreFragment();
        return fragment;
    }

    private void setting() {
        boolean isAutologin = getApplicationContext().get_boolean_SharedPreferences(KEY_PREF_SETTING_AUTOLOGIN, true);
        boolean is3g4g = getApplicationContext().get_boolean_SharedPreferences(KEY_PREF_SETTING_3G4G, true);
        boolean isAutoRotat = getApplicationContext().get_boolean_SharedPreferences(KEY_PREF_SETTING_AUTOROTATE, false);
        boolean isAlarm = getApplicationContext().getUserInfo().user_notice_push==0?false:true;

        ib_auto_login.setSelected(isAutologin);
        ib_g_connect.setSelected(is3g4g);
        ib_auto_rotate.setSelected(isAutoRotat);
        ib_set_alarm.setSelected(isAlarm);
    }

    public void init(View v) {
        super.init(v);

        v.findViewById(R.id.btn_modify_profile).setOnClickListener(this);
        v.findViewById(R.id.btn_app_intro).setOnClickListener(this);
        v.findViewById(R.id.btn_item_purchase).setOnClickListener(this);
        v.findViewById(R.id.btn_use_rule).setOnClickListener(this);
        v.findViewById(R.id.btn_auto_login).setOnClickListener(this);
        v.findViewById(R.id.btn_g_connect).setOnClickListener(this);
        v.findViewById(R.id.btn_auto_rotate).setOnClickListener(this);
        v.findViewById(R.id.btn_set_alarm).setOnClickListener(this);

        v.findViewById(R.id.btn_save_setting).setOnClickListener(this);

        ib_auto_login = (ImageButton) v.findViewById(R.id.ib_auto_login);
        ib_g_connect = (ImageButton) v.findViewById(R.id.ib_g_connect);
        ib_auto_rotate = (ImageButton) v.findViewById(R.id.ib_auto_rotate);
        ib_set_alarm = (ImageButton) v.findViewById(R.id.ib_set_alarm);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_save_setting: {
                saveSetting();
                OneMsgOneBtnDialog msgOneBtnDialog = new OneMsgOneBtnDialog(getActivity(),"알림", getResources().getString(R.string.complete_save_setting), new OneMsgOneBtnDialog.OneMsgOneBtnDialogListner() {
                    @Override
                    public void onClickConfirm() {

                    }
                });
                msgOneBtnDialog.show();
            }
                break;
            case R.id.btn_modify_profile: {
                Intent intent = new Intent(getActivity(), ProfileModifyActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.btn_app_intro: {
                Intent intent = new Intent(getActivity(), GuideActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.btn_use_rule: {
                Intent intent = new Intent(getActivity(), NoticeActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.btn_item_purchase: {
                Intent intent = new Intent(getActivity(), PurchaseItemActivity.class);
                startActivity(intent);
            }
            case R.id.btn_auto_login:
                ib_auto_login.setSelected(!ib_auto_login.isSelected());
                break;
            case R.id.btn_g_connect:
                ib_g_connect.setSelected(!ib_g_connect.isSelected());
                break;
            case R.id.btn_auto_rotate:
                ib_auto_rotate.setSelected(!ib_auto_rotate.isSelected());
                break;
            case R.id.btn_set_alarm:
                ib_set_alarm.setSelected(!ib_set_alarm.isSelected());
                break;
        }
    }

    private void saveSetting() {
        boolean isAutoLogin = ib_auto_login.isSelected();
        boolean is3g4g = ib_g_connect.isSelected();
        boolean isAutoRotate = ib_auto_rotate.isSelected();
        boolean isAlarm = ib_set_alarm.isSelected();

        WangjaTVApp app = getApplicationContext();
        app.set_boolean_SharedPreferences(KEY_PREF_SETTING_AUTOLOGIN, isAutoLogin);
        app.set_boolean_SharedPreferences(KEY_PREF_SETTING_3G4G, is3g4g);
        app.set_boolean_SharedPreferences(KEY_PREF_SETTING_AUTOROTATE, isAutoRotate);

        CommonUtil.setMobileDataEnabled(app, is3g4g);
        CommonUtil.setAutoRoate(app, isAutoRotate);

        setAlarm(isAlarm);
    }


    private void setAlarm(final boolean isAlarm) {
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(getActivity());

        int notice = 0;
        if(isAlarm == true) {
            notice = 1;
        }

        server.setAlarm(getActivity(), getApplicationContext().getUserInfo().user_no, notice, new Callback<BaseModel>() {
            @Override
            public void onResponse(Response<BaseModel> response) {
                BaseModel model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {

                        if(isAlarm == true) {
                            getApplicationContext().getUserInfo().user_notice_push = 1;
                        }
                        else {
                            getApplicationContext().getUserInfo().user_notice_push = 0;
                        }
                    } else {
                        CommonUtil.showCenterToast(getActivity(), model.msg, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, getActivity());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, getActivity());
            }
        });
    }
}
