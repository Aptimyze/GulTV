package com.yj.wangjatv;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.yj.wangjatv.activity.BroadcastActivity;
import com.yj.wangjatv.activity.LiveActivity;
import com.yj.wangjatv.activity.LoginActivity;
import com.yj.wangjatv.activity.MainActivity;
import com.yj.wangjatv.activity.PurchaseItemActivity;
import com.yj.wangjatv.dialog.InputPasswordDialog;
import com.yj.wangjatv.dialog.OneMsgTwoBtnDialog;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.model.HeartHistory;
import com.yj.wangjatv.model.UserInfo;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.utils.LruBitmapCache;
import com.yj.wangjatv.xmpp.XmppEndPoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;


public class WangjaTVApp extends MultiDexApplication implements Const {

    private static WangjaTVApp appInstance;

    public Activity m_act;
    public Typeface typeface_Nanum_R = null;
    public Typeface typeface_Nanum_B = null;
    public LiveActivity.LiveActivityListener liveListner = null;

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    //GCM 토큰값
    public String m_strGCMToken = "";

    public UserInfo mUserInfo = new UserInfo();

    private XmppEndPoint m_xmppEndPoint = null;

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
        initXmpp();
    }

    public void initXmpp() {
        m_xmppEndPoint = new XmppEndPoint(this, XMPP_SERVER_NAME, XMPP_SERVER_PORT, XMPP_CONFERENCE_SERVICE);
    }

    public XmppEndPoint getXmppEndPoint() {
        return  m_xmppEndPoint;
    }

    public static WangjaTVApp getInstance()
    {
        return appInstance;
    }

    public void setMainAct(Activity act) {
        m_act = act;
    }


    /**
     * @brief 폰트 셋팅
     */
    public void setFont(){
        // 폰트를 쓰레드로 돌린다.
        AsyncTask<Void, Integer, Void> mTask = new AsyncTask<Void, Integer, Void>() {

            @Override
            protected void onPreExecute() {

            }

            @Override
            protected Void doInBackground(Void... params) {
                // TODO Auto-generated method stub

                if (typeface_Nanum_R == null) {
                    typeface_Nanum_R = Typeface.createFromAsset(getAssets(),"NanumBarunGothic.ttf");
                }

                if (typeface_Nanum_B == null) {
                    typeface_Nanum_B = Typeface.createFromAsset(getAssets(),"NanumBarunGothicBold.ttf");
                }

                return null;
            }

            // publishProgress()
            @Override
            protected void onProgressUpdate(Integer... progress) {
                // mProgress.setProgress(progress[0]);
            }

            @Override
            protected void onPostExecute(Void result) {

            }

            @Override
            protected void onCancelled() {
                // dialog.dismiss();
            }
        };
        mTask.execute();
    }


    public Typeface getTypefaceNanumRegularFont(){
        if(typeface_Nanum_R != null){
            return typeface_Nanum_R;
        }else{
            setFont();
            return typeface_Nanum_R;
        }
    }

    public Typeface getTypefaceNanumBoldFont(){
        if(typeface_Nanum_B != null){
            return typeface_Nanum_B;
        }else{
            setFont();
            return typeface_Nanum_B;
        }
    }


    public void set_boolean_SharedPreferences(String key, boolean value) {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean get_boolean_SharedPreferences(String key) {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        return prefs.getBoolean(key, false);
    }

    public boolean get_boolean_SharedPreferences(String key, boolean value) {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        return prefs.getBoolean(key, value);
    }


    public void set_string_SharedPreferences(String key, String value) {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String get_string_SharedPreferences(String key) {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        return prefs.getString(key, "");
    }


    public void set_int_SharedPreferences(String key, int value) {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public int get_int_SharedPreferences(String key) {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        return prefs.getInt(key, 0);
    }


    public String getURLEncode(String str)
    {
        String result = "";
        try{
            result = URLEncoder.encode(str, "UTF-8");;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public boolean checkLoginned(Activity activity) {
        if(mUserInfo.is_loginned == false) {
            Intent intent = new Intent(activity, LoginActivity.class);
            activity.startActivity(intent);
            return false;
        }

        return true;
    }

    public boolean isApplicationBroughtToBackground() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(getPackageName())) {
                return true;
            }
        }

        return false;
    }

    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public String getLiveName(int no, String time) {
        String live_name = get_string_SharedPreferences(KEY_PREF_LIVE_PREFIX);

        String trim_time = time.replace(" ", "");
        trim_time = trim_time.replace(":", "_");
        trim_time = trim_time.replace(":", "_");
        trim_time = trim_time.replace("-", "_");
        live_name = String.format("%s%d__%s", live_name, no, trim_time);
        return live_name;
    }

    public String getPushToken() {
        String token = m_strGCMToken;
        token = get_string_SharedPreferences(KEY_PREF_PUSH_TOKEN);
        return token;
    }


    //
    //  Start Live Logic
    //
    public void playBroadcast(final Activity activity, final Broadcast broadcast) {
        // 1. 로그인된 유저인가를 판정. 로그인 안되어 있으면 로그인창으로 이행한다.
        if(checkLoginned(activity) == false) {
            return;
        }

        // 2. 시청인원이 넘쳐있을때... 입장불가.
        //   그러나 날자입장권 구입했을때는 가능... TODO
        //
        if(broadcast.viewer_cnt >= broadcast.max_viewer_cnt) {
            CommonUtil.showCenterToast(activity, R.string.msg_full_live_room, Toast.LENGTH_SHORT);
            return;
        }

        //
        // 19세이상
        //
        if(broadcast.allow_adult == 1 && mUserInfo.isAdult() == false) {
            CommonUtil.showCenterToast(activity, R.string.msg_live_allow_adult, Toast.LENGTH_SHORT);
            return;
        }

        WangjaTVApp app = this;
        final Broadcast pBroadcast = broadcast;
        int cnt = broadcast.heart_cnt;

        // 2. 비밀번호 라이브인가를 판정한다. 비밀번호가 있으면 비밀번호비교를 진행하고 라이브유로검사를 진행.
        if(broadcast.lock_password != null && broadcast.lock_password.isEmpty() == false) {
            final InputPasswordDialog dlg = new InputPasswordDialog(activity);
            dlg.setListner(new OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner(){
                @Override
                public void onClickNo() {

                }

                @Override
                public void onClickYes() {
                    String pwd = dlg.getPassword();

                    if(pwd == null || pwd.isEmpty() == true) {
                        CommonUtil.showCenterToast(activity, R.string.input_password, Toast.LENGTH_SHORT);
                    }
                    else {
                        checkPwd(activity, pBroadcast, pwd);
                    }
                }
            });
            dlg.show();

            return;
        }

        // 유로검사를 진행한다. 이미진행한 내역이 존재하면 그저 들어간다.
        checkUroBroadcast(activity, broadcast, cnt);
        return;
    }

    private boolean isHasHistory(Broadcast broadcast, int heart_cnt) {
        int sum_heart = 0;
        int cnt = broadcast.history_list.size();
        for(int i = 0; i < cnt; i++) {
            sum_heart += ((HeartHistory)broadcast.history_list.get(i)).heart_cnt;
        }

        if(sum_heart < heart_cnt) {
            return false;
        }

        return true;
    }

    private boolean startLiveActivity(Activity activity, Broadcast broadcast, boolean hasHistory) {
        if(broadcast.user_no == getUserInfo().user_no) {
            CommonUtil.showCenterToast(activity, R.string.your_live, Toast.LENGTH_SHORT);
            return false;
        }

        Intent intent = new Intent(activity, LiveActivity.class);
        intent.putExtra(KEY_INTENT_LIVE, broadcast);
        intent.putExtra(KEY_INTENT_HAS_HISTORY, hasHistory);
        activity.startActivityForResult(intent, INTENT_ACTIVITY_LIVE);

        if(liveListner != null) {
            liveListner.finishBeforeActivity();
        }
        return  true;
    }


    private void checkUroBroadcast(final Activity activity, final Broadcast broadcast, int heart_cnt) {
        int cnt = heart_cnt;
        WangjaTVApp app = this;

        final boolean hasHistory = isHasHistory(broadcast, heart_cnt);
        if(hasHistory == false && cnt > 0) {
            if (app.getUserInfo().user_heart_cnt < cnt) {
                String title = getResources().getString(R.string.dlg_inform_title);
                String content = String.format(getResources().getString(R.string.dlg_fan_purchse_heart_content), (broadcast.heart_cnt - app.getUserInfo().user_heart_cnt));
                new OneMsgTwoBtnDialog(activity, title, content, new OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner() {
                    @Override
                    public void onClickYes() {
                        Intent intent = new Intent(activity, PurchaseItemActivity.class);
                        activity.startActivity(intent);
                    }

                    @Override
                    public void onClickNo() {

                    }
                }).show();
                return;
            }
            else {
                String title = getResources().getString(R.string.dlg_inform_title);
                String content = String.format(getResources().getString(R.string.msg_send_heart_for_broadcast), (broadcast.heart_cnt));
                new OneMsgTwoBtnDialog(activity, title, content, new OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner() {
                    @Override
                    public void onClickYes() {
                        startLiveActivity(activity, broadcast, hasHistory);
                    }

                    @Override
                    public void onClickNo() {

                    }
                }).show();
                return;
            }
        }

        // 이미 전송이력이 있을경우
        startLiveActivity(activity, broadcast, hasHistory);
    }

    /*****************************************************************
     * API Functions
     *******************************************************************/

    private void checkPwd(final Activity activity, final Broadcast broadcast, String pwd) {
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(activity);
        server.checkBroadcastPwd(activity, broadcast.no, pwd, new Callback<BaseModel>() {
            @Override
            public void onResponse(Response<BaseModel> response) {
                BaseModel model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        checkUroBroadcast(activity, broadcast, broadcast.heart_cnt);
                    } else {
                        CommonUtil.showCenterToast(activity, model.msg, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, activity);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, activity);
            }
        });
    }
}
