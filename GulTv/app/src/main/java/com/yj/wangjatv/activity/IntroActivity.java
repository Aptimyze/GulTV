package com.yj.wangjatv.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.yj.wangjatv.Manifest;
import com.yj.wangjatv.R;
import com.yj.wangjatv.gcm.RegistrationIntentService;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.LiveServerInfo;
import com.yj.wangjatv.utils.CommonUtil;

import org.jsoup.Jsoup;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.utils.Log;
import retrofit2.Callback;
import retrofit2.Response;

public class IntroActivity extends BaseActivity {

    private final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private boolean m_bTimeOut = false;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    // GCM check handler
    Handler m_hndGCMCheckHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (m_bTimeOut) {
                checkGCMTokenValidation();
            } else if (m_app.m_strGCMToken != null && m_app.m_strGCMToken.length() > 0) {
                checkGCMTokenValidation();
            }
            m_bTimeOut = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

       if (!LibsChecker.checkVitamioLibs(this))
            return;

        initGCMModule();
    }

    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, new IntentFilter("registrationComplete"));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void checkGCMTokenValidation() {

        if (m_app.m_strGCMToken.isEmpty()) {
            showPushAlert();
            return;
        }

        m_app.set_string_SharedPreferences(KEY_PREF_PUSH_TOKEN, m_app.m_strGCMToken);

        getLiveServer();
    }

    /**
     * Init GCM module
     */
    private void initGCMModule() {
        if (!Build.BRAND.equalsIgnoreCase("generic")) {
            // GCM 토큰을 얻기 위한 receiver
            mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context);
                    boolean sentToken = sharedPreferences
                            .getBoolean("sentTokenToServer", false);
                    if (sentToken) {
                        if (m_bTimeOut) {
                            checkGCMTokenValidation();
                        } else {
                            m_bTimeOut = true;
                        }
                    } else {
                       showPushAlert();
                    }
                }
            };

            // 구글 플래이 서비스 사용여부 체크
            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        } else {
            m_bTimeOut = true;
        }

        m_hndGCMCheckHandler.sendEmptyMessageDelayed(0, 3000);
    }

    private void showPushAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(getResources().getDrawable(
                android.R.drawable.ic_dialog_alert));
        alertDialog.setTitle(R.string.error);

        alertDialog.setMessage(R.string.push_error);
        alertDialog.setPositiveButton(R.string.go_on,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getLiveServer();
                    }
                });


        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,  PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                android.util.Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    private void getLiveServer() {
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);
        server.getBroadcastServer(this, new Callback<LiveServerInfo>() {
            @Override
            public void onResponse(Response<LiveServerInfo> response) {
                LiveServerInfo model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        m_app.set_string_SharedPreferences(KEY_PREF_LIVE_SERVER_IP, model.server_ip);
                        m_app.set_int_SharedPreferences(KEY_PREF_LIVE_SERVER_PORT, model.server_port);
                        m_app.set_string_SharedPreferences(KEY_PREF_LIVE_PREFIX, model.live_prefix);
                        m_app.set_string_SharedPreferences(KEY_PREF_LIVE_APP_NAME, model.live_app_name);
                        m_app.set_string_SharedPreferences(KEY_PREF_LIVE_PROTOCAL, model.protocol);
                        m_app.set_int_SharedPreferences(KEY_PREF_LIVE_BITRATE, model.bitrate);
                        m_app.set_int_SharedPreferences(KEY_PREF_LIVE_VIDEO_W, model.video_w);
                        m_app.set_int_SharedPreferences(KEY_PREF_LIVE_VIDEO_H, model.video_h);
                        m_app.set_string_SharedPreferences(KEY_PREF_RECORD_DIR, model.record_dir);
                        m_app.set_string_SharedPreferences(KEY_PREF_RECORD_TYPE, model.record_type);
                    } else {
                        server.processErrorBody(response, IntroActivity.this);
                    }

                    doVersionCheck();

                } else {
                    server.processErrorBody(response, IntroActivity.this);
                    finish();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                finish();
            }
        });
    }


    @Override
    public void init() {
        super.init();

    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Intent i = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    };

    private void doVersionCheck() {
        String w_strCurVersion = "1.0";
        try {
            w_strCurVersion = m_app.getPackageManager().getPackageInfo(m_app.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        String w_strMarketVersion = w_strCurVersion;
        //if(false) {  // remove version check
        try {
            w_strMarketVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + m_app.getPackageName() + "&hl=en")
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select("div[itemprop=softwareVersion]")
                    .first()
                    .ownText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //}

        if (!w_strCurVersion.equals(w_strMarketVersion)) {
            final String w_strUpdateMessage = "새로운 버전(" + w_strMarketVersion + ")이 발견되었습니다.\n지금 업데이트 하시겠습니까?";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //
                    // 실행중인 앱버전과 마켓의 버전이 다른 경우 최신버전업데이트 진행하도록 유도.
                    //
                    new AlertDialog.Builder(IntroActivity.this).setTitle(R.string.msg_software_update)
                            .setMessage(w_strUpdateMessage)
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final String appPackageName = getPackageName();
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }

                                    //
                                    // 앱 종료.
                                    //
                                    finish();
                                }
                            })
                            .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    CommonUtil.killProcess(IntroActivity.this);
                                }
                            })
                            .show();
                }
            });
        } else {
            mHandler.sendEmptyMessageDelayed(0, 3000);
        }
    }
}
