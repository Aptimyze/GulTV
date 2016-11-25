package com.yj.wangjatv.http;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.yj.wangjatv.R;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.utils.CommonUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.vov.vitamio.utils.FileUtils;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.Response;

/**
 * Created by KCJ on 12/9/2015.
 */
public class APIProvider {
    public static final String API_URL                = "http://1.234.63.171";

    // 로그인 API
    public static final String API_DIR = "/api/";
    public static final String API_LOGIN                            =  API_DIR+"login.php";
    public static final String API_LOGOUT                           =  API_DIR+"logout.php";
    public static final String API_SIGN_UP                          =  API_DIR+"signup.php";
    public static final String API_GET_BROADCAST_LIST               =  API_DIR+"getBroadcastList.php";
    public static final String API_GET_USER_BROADCAST_LIST          =  API_DIR+"getUserBroadcastList.php";
    public static final String API_GET_BROADCAST_SERVER             =  API_DIR+"getBroadcastServer.php";
    public static final String API_GET_BROADCAST_CATEGORY_LIST      =  API_DIR+"getBroadcastCategoryList.php";
    public static final String API_START_END_BROADCAST              =  API_DIR+"startendBroadcast.php";
    public static final String API_INSERT_BROADCAST                 =  API_DIR+"insertBroadcast.php";
    public static final String API_DELETE_BROADCAST                 =  API_DIR+"deleteBroadcast.php";
    public static final String API_CHK_ID                           =  API_DIR+"chk_id.php";
    public static final String API_CHK_NAME                         =  API_DIR+"chk_name.php";
    public static final String API_FIND_PWD                         =  API_DIR+"findPwd.php";
    public static final String API_GET_EVENT_LIST                   =  API_DIR+"getEventList.php";
    public static final String API_CHK_BROADCAST_PWD                =  API_DIR+"chk_broadcast_pwd.php";
    public static final String API_SEND_HEART                       =  API_DIR+"sendHeart.php";
    public static final String API_FAV_BROADCAST                    =  API_DIR+"favBroadcast.php";
    public static final String API_LIKE_BROADCAST                   =  API_DIR+"likeBroadcast.php";
    public static final String API_FAV_BROADCAST_LIST               =  API_DIR+"getFavBroadcastList.php";
    public static final String API_DELETE_FAV_BROADCAST             =  API_DIR+"deleteFavBroadcast.php";
    public static final String API_GET_NOTICE_LIST                  =  API_DIR+"getNotice.php";
    public static final String API_MODIFY_PROFILE                   =  API_DIR+"modifyProfile.php";
    public static final String API_GET_AGREEMENT                    =  API_DIR+"getAgreement.php";
    public static final String API_BOUGHT_ITEM                      =  API_DIR+"boughtItem.php";
    public static final String API_UPDATE_XMPP_JOIN_FLAG            =  API_DIR+"updateXmppJoinFlag.php";
    public static final String API_GET_MY_LAST_LIVE                 =  API_DIR+"getMyLastLive.php";
    public static final String API_GET_SERVER_TIME                  =  API_DIR+"getServerTime.php";
    public static final String API_SET_ALARM                        =  API_DIR+"setAlarm.php";
    public static final String API_IN_OUT_BROADCAST                 =  API_DIR+"inoutBroadcast.php";
    public static final String API_UPDATE_BROADCAST_USER_FLAG       =  API_DIR+"updateBroadcastUserFlag.php";
    public static final String API_CHK_ENTER_BROADCAST               =  API_DIR+"chk_enter_broadcast.php";
    public static final String API_GET_BROADCAST_USER_CNT            =  API_DIR+"getBroadcastUserCnt.php";
    public static final String API_UPLOAD_BROADCAST_THUMB            =  API_DIR+"uploadBroadcastThumb.php";
    public static final String API_DELETE_BROADCAST_THUMB            =  API_DIR+"deleteBroadcastThumb.php";

    public static String getApiUrl(String relativeURL) {
        return   APIProvider.API_URL + relativeURL;
    }
    
    
    public final String TAG = "APIProvider";

    private final String KEY    = "doqroqkfONLY%5^6";

    // 서버 요청 타임아웃 시간
    int TIME_OUT = 180; // 단위는 초

    interface NAME {
        String EMAIL                = "user_id";
        String PASSWD               = "user_pwd";
        String USER_NAME            = "user_name";
        String USER_BIRTH           = "user_birth";
        String USER_GRADE           = "user_grade";
        String ORDER                = "order";
        String TYPE                 = "type";
        String PAGE_CNT             = "pgnum";
        String START                = "start";
        String FLAG                 = "flag";
        String BROADCAST            = "broadcast_no";
        String USER_NO              = "user_no";
        String TITLE                = "title";
        String VIDEO_QUALITY        = "video_quality";
        String ALLOW_ADULT          = "allow_adult";
        String MAX_VIEWER_CNT       = "max_viewer_cnt";
        String LOCK_PASSWORD        = "lock_password";
        String CATEGORY_NO          = "category_no";
        String DEVICE_TYPE          = "device_type";
        String DEVICE_ID            = "device_f_id";
        String DEVICE_PUSH_TOKEN    = "device_push_token";
        String RECEIVER_NO          = "receiver_no";
        String HEART_CNT            = "heart_cnt";
        String TEXT_TYPE            = "text_type";
        String PURCHASE_DATA        = "purchase_data";
        String DATA_SIGNATURE       = "data_signature";
        String CUR_VOD_NO           = "cur_vod_no";
        String NOTICE               = "notice";
        String EFFECT               = "effect";
        String THUMB                = "thumb";
    }

    public class RestParams {
        // IN PARAM
        public String restURL; // REST SERVER URL
        public ArrayList<Object> arrParam = new ArrayList<>();

        // OUT PARAM
        public String resultString;
        public String resultCookie;

        public int retVal;
        public String errMsg;

        public RestParams() {
            initParam();
        }

        public void initParam() {
            restURL = "";
            arrParam = new ArrayList<>();
            resultString = "";

            retVal = 0;
            errMsg = "";
        }

        public String getRestRelativeURL() {
            if(restURL == null) {
                return "";
            }
            int startIdx = restURL.indexOf(APIProvider.API_URL);
            String url = "";
            if(startIdx >= 0) {
                url = restURL.substring(APIProvider.API_URL.length());
            }
            return url;
        }
    }


    public static String cookie = "";

    private static boolean m_bLoading = false;
    private static ProgressDialog m_dlgProgress = null;

    public class MyCookieManager extends CookieManager {

        @Override
        public void put(URI uri, Map<String, List<String>> stringListMap) throws IOException {
            super.put(uri, stringListMap);
            if (stringListMap != null && stringListMap.get("Set-Cookie") != null)
                for (String string : stringListMap.get("Set-Cookie")) {
                    if (string.contains("ci_session")) {
                        cookie = string;
                    }
                }
        }
    }

    private String usecTime() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static String encodeURIComponent(String s)
    {
        String result = null;

        try
        {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%26", "&")
                    .replaceAll("\\%3D", "=")
                    .replaceAll("\\%7E", "~");
        }

        // This exception should never occur.
        catch (UnsupportedEncodingException e)
        {
            result = s;
        }

        return result;
    }

    public void login(Context context, String email, String passwd, int type, String device_id, String device_token, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_LOGIN;

        param.arrParam.add(email);
        param.arrParam.add(passwd);
        param.arrParam.add(type);
        param.arrParam.add(device_id);
        param.arrParam.add(device_token);

        callServer(context, param, callback);
    }

    public void getBroadcasList(Context context, int user_no, int type, int order, int start, int pagenum, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_GET_BROADCAST_LIST;

        param.arrParam.add(user_no);
        param.arrParam.add(type);
        param.arrParam.add(order);
        param.arrParam.add(start);
        param.arrParam.add(pagenum);

        callServer(context, param, callback);
    }

    public void getBroadcastServer(Context context, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_GET_BROADCAST_SERVER;

        callServer(context, param, callback);
    }

    public void getBroadcastCategoryList(Context context, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_GET_BROADCAST_CATEGORY_LIST;

        callServer(context, param, callback);
    }

    public void getBroadcastUserCnt(Context context,int broadcast_no, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_GET_BROADCAST_USER_CNT;
        param.arrParam.add(broadcast_no);
        callServer(context, param, callback);
    }

    public void starendBroadcast(Context context, int broadcast_no, int flag, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_START_END_BROADCAST;

        param.arrParam.add(broadcast_no);
        param.arrParam.add(flag);

        callServer(context, param, callback);
    }

    public void insertBroadcast(Context context, Broadcast broadcast, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_INSERT_BROADCAST;

        param.arrParam.add(broadcast.user_no);
        param.arrParam.add(broadcast.title);
        param.arrParam.add(broadcast.max_viewer_cnt);
        param.arrParam.add(broadcast.video_quality);
        param.arrParam.add(broadcast.allow_adult);
        param.arrParam.add(broadcast.lock_password);
        param.arrParam.add(broadcast.category_no);
        param.arrParam.add(broadcast.type);
        param.arrParam.add(broadcast.user_grade);
        param.arrParam.add(broadcast.heart_cnt);

        callServer(context, param, callback);
    }

    public void deleteBroadcast(Context context, int no,  final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_DELETE_BROADCAST;

        param.arrParam.add(no);

        callServer(context, param, callback);
    }

    public void chkID(Context context, String id,  final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_CHK_ID;

        param.arrParam.add(id);

        callServer(context, param, callback);
    }

    public void chkName(Context context, String name,  final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_CHK_NAME;

        param.arrParam.add(name);

        callServer(context, param, callback);
    }

    public void signup(Context context, String id,String name, String pwd, String birth, int type, String device_id, String device_token, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_SIGN_UP;

        param.arrParam.add(id);
        param.arrParam.add(name);
        param.arrParam.add(pwd);
        param.arrParam.add(birth);
        param.arrParam.add(type);
        param.arrParam.add(device_id);
        param.arrParam.add(device_token);

        callServer(context, param, callback);
    }

    public void findPWd(Context context, String email,String birth,  final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_FIND_PWD;

        param.arrParam.add(email);
        param.arrParam.add(birth);

        callServer(context, param, callback);
    }

    public void getEventList(Context context, int type, int start, int pagenum, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_GET_EVENT_LIST;

        param.arrParam.add(type);
        param.arrParam.add(start);
        param.arrParam.add(pagenum);

        callServer(context, param, callback);
    }

    public void checkBroadcastPwd(Context context, int no, String pwd, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_CHK_BROADCAST_PWD;

        param.arrParam.add(no);
        param.arrParam.add(pwd);

        callServer(context, param, callback);
    }

    public void likeBroadcast(Context context, int no, int broadcast_no, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_LIKE_BROADCAST;

        param.arrParam.add(no);
        param.arrParam.add(broadcast_no);

        callServer(context, param, callback);
    }

    public void fanBroadcast(Context context, int no, int broadcast_no, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_FAV_BROADCAST;

        param.arrParam.add(no);
        param.arrParam.add(broadcast_no);

        callServer(context, param, callback);
    }

    public void sendHeart(Context context, int no, int receiver, int heart,int b_no, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_SEND_HEART;

        param.arrParam.add(no);
        param.arrParam.add(receiver);
        param.arrParam.add(heart);
        param.arrParam.add(b_no);

        callServer(context, param, callback);
    }

    public void getFavBroadcasList(Context context, int user_no,int type, int order, int start, int pagenum, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_FAV_BROADCAST_LIST;

        param.arrParam.add(user_no);
        param.arrParam.add(type);
        param.arrParam.add(order);
        param.arrParam.add(start);
        param.arrParam.add(pagenum);

        callServer(context, param, callback);
    }

    public void deletefavBroadcast(Context context, int no, int broadcast_no, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_DELETE_FAV_BROADCAST;

        param.arrParam.add(no);
        param.arrParam.add(broadcast_no);

        callServer(context, param, callback);
    }

    public void getNoticeList(Context context, int start, int pagenum, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_GET_NOTICE_LIST;

        param.arrParam.add(start);
        param.arrParam.add(pagenum);

        callServer(context, param, callback);
    }

    public void modifyProfile(Context context, String id,  String name, String pwd, String birth, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_MODIFY_PROFILE;

        param.arrParam.add(id);
        param.arrParam.add(name);
        param.arrParam.add(pwd);
        param.arrParam.add(birth);

        callServer(context, param, callback);
    }

    public void getAgreement(Context context, String type, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_GET_AGREEMENT;

        param.arrParam.add(type);

        callServer(context, param, callback);
    }

    public void boughtItem(Context context, int user_no, String data, String sig, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_BOUGHT_ITEM;

        param.arrParam.add(user_no);
        param.arrParam.add(data);
        param.arrParam.add(sig);

        callServer(context, param, callback);
    }

    public void updateXmppJoinFlag(Context context, int user_no, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_UPDATE_XMPP_JOIN_FLAG;

        param.arrParam.add(user_no);

        callServer(context, param, callback);
    }

    public void getMyLastLive(Context context, int user_no, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_GET_MY_LAST_LIVE;

        param.arrParam.add(user_no);

        callServer(context, param, callback);
    }

    public void getServerTime(Context context,final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_GET_SERVER_TIME;

        callServer(context, param, callback);
    }

    public void getUserBroadcasList(Context context, int user_no, int type, int vod_no, int order, int start, int pagenum, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_GET_USER_BROADCAST_LIST;

        param.arrParam.add(user_no);
        param.arrParam.add(type);
        param.arrParam.add(vod_no);
        param.arrParam.add(order);
        param.arrParam.add(start);
        param.arrParam.add(pagenum);

        callServer(context, param, callback);
    }

    public void setAlarm(Context context, int user_no, int notice, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_SET_ALARM;

        param.arrParam.add(user_no);
        param.arrParam.add(notice);

        callServer(context, param, callback);
    }

    public void inoutBroadcast(Context context, int no, int user_no, int type, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_IN_OUT_BROADCAST;

        param.arrParam.add(no);
        param.arrParam.add(user_no);
        param.arrParam.add(type);

        callServer(context, param, callback);
    }

    public void updateBroadcastUserFlag(Context context, int no, int user_no, int effect, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_UPDATE_BROADCAST_USER_FLAG;

        param.arrParam.add(no);
        param.arrParam.add(user_no);
        param.arrParam.add(effect);

        callServer(context, param, callback);
    }

    public void chkEnterBroadcast(Context context, int no, int user_no, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_CHK_ENTER_BROADCAST;

        param.arrParam.add(no);
        param.arrParam.add(user_no);

        callServer(context, param, callback);
    }

    public void uploadBroadcastThumb(Context context, int no, int user_no, File file, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_UPLOAD_BROADCAST_THUMB;

        param.arrParam.add(no);
        param.arrParam.add(user_no);
        param.arrParam.add(file);

        callServer(context, param, callback);
    }

    public void deleteBroadcastThumb(Context context, int no, String thumb_url, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_DELETE_BROADCAST_THUMB;

        param.arrParam.add(no);
        param.arrParam.add(thumb_url);
        callServer(context, param, callback);
    }

    public void logout(Context context, int no, final retrofit2.Callback callback) {
        APIProvider.RestParams param = new RestParams();
        param.restURL = APIProvider.API_LOGOUT;

        param.arrParam.add(no);

        callServer(context, param, callback);
    }

    private void callServer(Context context, RestParams inParam, final retrofit2.Callback callback) {
        RestParams outParams = inParam;

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                /*.addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        okhttp3.Response response = chain.proceed(chain.request());
                        Log.w("Retrofit@Response", response.body().string());
                        return response;
                    }
                })*/
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(APIProvider.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIInterface service = retrofit.create(APIInterface.class);

        Call call = null;
        MyCookieManager myCookieManager = new MyCookieManager();

        if(inParam.restURL.equals(API_LOGIN)) {
            call = service.login((String)inParam.arrParam.get(0), (String)inParam.arrParam.get(1), (int)inParam.arrParam.get(2), (String)inParam.arrParam.get(3), (String)inParam.arrParam.get(4));
        }
        else if(inParam.restURL.equals(API_GET_BROADCAST_LIST)) {
            call = service.getBroadcastList((int) inParam.arrParam.get(0), (int) inParam.arrParam.get(1), (int) inParam.arrParam.get(2), (int)inParam.arrParam.get(3), (int)inParam.arrParam.get(4));
        }
        else if(inParam.restURL.equals(API_GET_BROADCAST_SERVER)) {
            call = service.getBroadcastServer();
        }
        else if(inParam.restURL.equals(API_GET_BROADCAST_CATEGORY_LIST)) {
            call = service.getBroadcastListCategory();
        }
        else  if(inParam.restURL.equals(API_START_END_BROADCAST)) {
            call = service.startendBroadcast((int) inParam.arrParam.get(0), (int)inParam.arrParam.get(1));
        }
        else  if(inParam.restURL.equals(API_INSERT_BROADCAST)) {
            call = service.insertBroadcast((int) inParam.arrParam.get(0), (String) inParam.arrParam.get(1), (int)inParam.arrParam.get(2), (int)inParam.arrParam.get(3)
                    , (int)inParam.arrParam.get(4), (String)inParam.arrParam.get(5), (int)inParam.arrParam.get(6), (int)inParam.arrParam.get(7)
                    , (int)inParam.arrParam.get(8), (int)inParam.arrParam.get(9));
        }
        else if(inParam.restURL.equals(API_DELETE_BROADCAST)) {
            call = service.deleteBroadcast((int) inParam.arrParam.get(0));
        }
        else if(inParam.restURL.equals(API_CHK_ID)) {
            call = service.chkID((String) inParam.arrParam.get(0));
        }
        else if(inParam.restURL.equals(API_CHK_NAME)) {
            call = service.chkName((String) inParam.arrParam.get(0));
        }
        else if(inParam.restURL.equals(API_SIGN_UP)) {
            call = service.signup((String) inParam.arrParam.get(0), (String) inParam.arrParam.get(1),(String) inParam.arrParam.get(2),(String) inParam.arrParam.get(3),
                    (int)inParam.arrParam.get(4), (String)inParam.arrParam.get(5), (String)inParam.arrParam.get(6));
        }
        else if(inParam.restURL.equals(API_FIND_PWD)) {
            call = service.findPwd((String) inParam.arrParam.get(0), (String) inParam.arrParam.get(1));
        }
        else if(inParam.restURL.equals(API_GET_EVENT_LIST)) {
            call = service.getEventList((int) inParam.arrParam.get(0), (int) inParam.arrParam.get(1), (int) inParam.arrParam.get(2));
        }
        else if(inParam.restURL.equals(API_CHK_BROADCAST_PWD)) {
            call = service.chkBroadcastPwd((int) inParam.arrParam.get(0), (String) inParam.arrParam.get(1));
        }
        else if(inParam.restURL.equals(API_LIKE_BROADCAST)) {
            call = service.likeBroadcast((int) inParam.arrParam.get(0), (int) inParam.arrParam.get(1));
        }
        else if(inParam.restURL.equals(API_FAV_BROADCAST)) {
            call = service.fanBroadcast((int) inParam.arrParam.get(0), (int) inParam.arrParam.get(1));
        }
        else if(inParam.restURL.equals(API_SEND_HEART)) {
            call = service.sendHeart((int) inParam.arrParam.get(0), (int) inParam.arrParam.get(1), (int) inParam.arrParam.get(2), (int) inParam.arrParam.get(3));
        }
        else if(inParam.restURL.equals(API_FAV_BROADCAST_LIST)) {
            call = service.getFavBroadcastList((int) inParam.arrParam.get(0), (int) inParam.arrParam.get(1), (int) inParam.arrParam.get(2), (int) inParam.arrParam.get(3), (int) inParam.arrParam.get(4));
        }
        else if(inParam.restURL.equals(API_DELETE_FAV_BROADCAST)) {
            call = service.deletefavBroadcast((int) inParam.arrParam.get(0), (int) inParam.arrParam.get(1));
        }
        else if(inParam.restURL.equals(API_GET_NOTICE_LIST)) {
            call = service.getNoticeList((int) inParam.arrParam.get(0), (int) inParam.arrParam.get(1));
        }
        else if(inParam.restURL.equals(API_MODIFY_PROFILE)) {
            call = service.modifyProfile((String) inParam.arrParam.get(0), (String) inParam.arrParam.get(1), (String) inParam.arrParam.get(2), (String) inParam.arrParam.get(3));
        }
        else if(inParam.restURL.equals(API_GET_AGREEMENT)) {
            call = service.getAgreement((String)inParam.arrParam.get(0));
        }
        else if(inParam.restURL.equals(API_BOUGHT_ITEM)) {
            call = service.boughtItem((int) inParam.arrParam.get(0), (String)inParam.arrParam.get(1), (String)inParam.arrParam.get(2));
        }
        else if(inParam.restURL.equals(API_UPDATE_XMPP_JOIN_FLAG)) {
            call = service.updateXmppJoinFlag((int) inParam.arrParam.get(0));
        }
        else if(inParam.restURL.equals(API_GET_MY_LAST_LIVE)) {
            call = service.getMyLastLive((int) inParam.arrParam.get(0));
        }
        else if(inParam.restURL.equals(API_GET_SERVER_TIME)) {
            call = service.getServerTime();
        }
        else if(inParam.restURL.equals(API_GET_USER_BROADCAST_LIST)) {
            call = service.getUserBroadcastList((int) inParam.arrParam.get(0), (int) inParam.arrParam.get(1), (int) inParam.arrParam.get(2), (int) inParam.arrParam.get(3), (int) inParam.arrParam.get(4) ,(int) inParam.arrParam.get(5));
        }
        else if(inParam.restURL.equals(API_SET_ALARM)) {
            call = service.setAlarm((int) inParam.arrParam.get(0), (int) inParam.arrParam.get(1));
        }
        else if(inParam.restURL.equals(API_IN_OUT_BROADCAST)) {
            call = service.inoutBroadcast((int) inParam.arrParam.get(0), (int) inParam.arrParam.get(1),(int) inParam.arrParam.get(2));
        }
        else if(inParam.restURL.equals(API_UPDATE_BROADCAST_USER_FLAG)) {
            call = service.updateBroadcastUserFlag((int) inParam.arrParam.get(0), (int) inParam.arrParam.get(1), (int) inParam.arrParam.get(2));
        }
        else if(inParam.restURL.equals(API_CHK_ENTER_BROADCAST)) {
            call = service.chkEnterBroadcast((int) inParam.arrParam.get(0), (int) inParam.arrParam.get(1));
        }
        else if(inParam.restURL.equals(API_GET_BROADCAST_USER_CNT)) {
            call = service.getBroadcastUserCnt((int) inParam.arrParam.get(0));
        }
        else if(inParam.restURL.equals(API_UPLOAD_BROADCAST_THUMB)) {
            // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
            // use the FileUtils to get the actual file by uri
            File file = (File)inParam.arrParam.get(2);

            // create RequestBody instance from file
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("image/*"), file);

            RequestBody no = RequestBody.create(MediaType.parse("text/plain"), String.format("%d", (int)inParam.arrParam.get(0)));
            RequestBody user_no = RequestBody.create(MediaType.parse("text/plain"), String.format("%d", (int)inParam.arrParam.get(1)));

            Map<String, RequestBody> requestBodyMap = new HashMap<>();
            requestBodyMap.put(NAME.BROADCAST, no);
            requestBodyMap.put(NAME.USER_NO, user_no);
            requestBodyMap.put(String.format("thumb\";filename=\"%s\" ", file.getName()), requestFile);
            call = service.uploadBroadcastThumb(requestBodyMap);
        }
        else if(inParam.restURL.equals(API_DELETE_BROADCAST_THUMB)) {
            call = service.deleteBroadcastThumb((int) inParam.arrParam.get(0),(String) inParam.arrParam.get(1));
        }
        else if(inParam.restURL.equals(API_LOGOUT)) {
            call = service.logout((int) inParam.arrParam.get(0));
        }

        if(call != null) {
            call.enqueue(callback);
        }
    }

    public void processErrorBody(Response body, Context context) {
        //404 or the response cannot be converted to User.
        ResponseBody responseBody = body.errorBody();
        if (responseBody != null) {
            /*try {
                Log.e(TAG, "BIT::::" + responseBody.string());
                CommonUtil.showCenterToast(context, responseBody.string(), Toast.LENGTH_SHORT);
            }
            catch (Exception e) {
                e.printStackTrace();
            }*/
            CommonUtil.showCenterToast(context, R.string.data_parse_error, Toast.LENGTH_SHORT);
        } else {
            CommonUtil.showCenterToast(context, R.string.data_fetch_error, Toast.LENGTH_SHORT);
        }
    }

    public void processServerFailure(Throwable throwable, Context context) {
        CommonUtil.showCenterToast(context, R.string.server_problem, Toast.LENGTH_SHORT);
    }



    public static void showWaitingDlg(Context context) {
        showProgress(context, "", context.getString(R.string.please_wait));
    }

    // Show loading progress bar from server
    public static void showProgress(Context ctx, String title, String msg) {
        if (isShowProgress())
            return;

        m_bLoading = true;
        m_dlgProgress = new ProgressDialog(ctx);
        m_dlgProgress.setTitle(title);
        m_dlgProgress.setMessage(msg);
        m_dlgProgress.setCancelable(true);
        m_dlgProgress.setCanceledOnTouchOutside(false);
        m_dlgProgress.setIndeterminate(false);
        m_dlgProgress.setOnCancelListener(new DialogInterface.OnCancelListener(){

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                cancelProgress();
            }
        });
        m_dlgProgress.show();
    }

    // Whether there is data loading state
    public static boolean isShowProgress() {
        return m_bLoading;
    }

    public static void cancelProgress() {
        m_bLoading = false;
    }

    // Hide loading progress bar
    public static void hideProgress() {
        m_bLoading = false;
        if (m_dlgProgress != null && m_dlgProgress.isShowing())
            m_dlgProgress.dismiss();
    }
}
