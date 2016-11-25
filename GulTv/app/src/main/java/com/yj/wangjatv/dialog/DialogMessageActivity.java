package com.yj.wangjatv.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.yj.wangjatv.Const;
import com.yj.wangjatv.R;
import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.activity.BaseActivity;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.utils.CommonUtil;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 7/7/2016.
 */
public class DialogMessageActivity extends Activity {

    int m_nType = 0; // 0:force exit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.dialog_message_activity);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        String msg = getIntent().getStringExtra(Const.KEY_INTENT_MSG);
        m_nType = getIntent().getIntExtra(Const.KEY_INTENT_DIALOG_TYPE, 0);
        displayAlert(msg);
    }

    private void displayAlert(String msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg).setCancelable(
                false).setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final Activity activity = DialogMessageActivity.this;
                        final WangjaTVApp m_app = (WangjaTVApp) getApplication();
                        m_app.set_string_SharedPreferences(Const.KEY_PREF_USER_ID, "");
                        m_app.set_string_SharedPreferences(Const.KEY_PREF_USER_PWD, "");
                        m_app.getUserInfo().is_loginned = false;
                        CommonUtil.killProcess(activity);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
