package com.yj.wangjatv.http;

/**
 * Created by KCJ on 12/9/2015.
 */

import com.yj.wangjatv.model.Agreement;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.model.BroadcastCategoryList;
import com.yj.wangjatv.model.BroadcastList;
import com.yj.wangjatv.model.BroadcastUserEffect;
import com.yj.wangjatv.model.EventList;
import com.yj.wangjatv.model.FindPwd;
import com.yj.wangjatv.model.LiveServerInfo;
import com.yj.wangjatv.model.Notice;
import com.yj.wangjatv.model.NoticeList;
import com.yj.wangjatv.model.ServerTime;
import com.yj.wangjatv.model.UserInfo;
import com.yj.wangjatv.model.ViewerCnt;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

/**
 * Created by KCJ on 12/9/2015.
 */
public interface APIInterface {
    @FormUrlEncoded
    @POST(APIProvider.API_LOGIN)
    Call<UserInfo> login(@Field(APIProvider.NAME.EMAIL) String email, @Field(APIProvider.NAME.PASSWD) String passwd, @Field(APIProvider.NAME.DEVICE_TYPE) int type, @Field(APIProvider.NAME.DEVICE_ID) String id, @Field(APIProvider.NAME.DEVICE_PUSH_TOKEN) String token);

    @FormUrlEncoded
    @POST(APIProvider.API_GET_BROADCAST_LIST)
    Call<BroadcastList> getBroadcastList(@Field(APIProvider.NAME.USER_NO) int user_no, @Field(APIProvider.NAME.TYPE) int type,  @Field(APIProvider.NAME.ORDER) int order, @Field(APIProvider.NAME.START) int start, @Field(APIProvider.NAME.PAGE_CNT) int pagenum);

    @GET(APIProvider.API_GET_BROADCAST_SERVER)
    Call<LiveServerInfo> getBroadcastServer();

    @GET(APIProvider.API_GET_BROADCAST_CATEGORY_LIST)
    Call<BroadcastCategoryList> getBroadcastListCategory();

    @FormUrlEncoded
    @POST(APIProvider.API_START_END_BROADCAST)
    Call<BaseModel> startendBroadcast(@Field(APIProvider.NAME.BROADCAST) int no, @Field(APIProvider.NAME.FLAG) int flag);

    @FormUrlEncoded
    @POST(APIProvider.API_INSERT_BROADCAST)
    Call<Broadcast> insertBroadcast(@Field(APIProvider.NAME.USER_NO) int user_no, @Field(APIProvider.NAME.TITLE) String title, @Field(APIProvider.NAME.MAX_VIEWER_CNT) int cnt,
                                    @Field(APIProvider.NAME.VIDEO_QUALITY) int quality,@Field(APIProvider.NAME.ALLOW_ADULT) int adult,@Field(APIProvider.NAME.LOCK_PASSWORD) String password,
                                    @Field(APIProvider.NAME.CATEGORY_NO) int category,@Field(APIProvider.NAME.TYPE) int type,@Field(APIProvider.NAME.USER_GRADE) int grade,@Field(APIProvider.NAME.HEART_CNT) int heart_cnt);
    @FormUrlEncoded
    @POST(APIProvider.API_DELETE_BROADCAST)
    Call<BaseModel> deleteBroadcast(@Field(APIProvider.NAME.BROADCAST) int no);

    @FormUrlEncoded
    @POST(APIProvider.API_CHK_ID)
    Call<BaseModel> chkID(@Field(APIProvider.NAME.EMAIL) String id);

    @FormUrlEncoded
    @POST(APIProvider.API_CHK_NAME)
    Call<BaseModel> chkName(@Field(APIProvider.NAME.USER_NAME) String name);

    @FormUrlEncoded
    @POST(APIProvider.API_SIGN_UP)
    Call<UserInfo> signup(@Field(APIProvider.NAME.EMAIL) String user_id,@Field(APIProvider.NAME.PASSWD) String pwd,@Field(APIProvider.NAME.USER_NAME) String name,@Field(APIProvider.NAME.USER_BIRTH) String birth
            , @Field(APIProvider.NAME.DEVICE_TYPE) int type, @Field(APIProvider.NAME.DEVICE_ID) String id, @Field(APIProvider.NAME.DEVICE_PUSH_TOKEN) String token);

    @FormUrlEncoded
    @POST(APIProvider.API_FIND_PWD)
    Call<FindPwd> findPwd(@Field(APIProvider.NAME.EMAIL) String id,@Field(APIProvider.NAME.USER_BIRTH) String user_birth);

    @FormUrlEncoded
    @POST(APIProvider.API_GET_EVENT_LIST)
    Call<EventList> getEventList(@Field(APIProvider.NAME.TYPE) int type, @Field(APIProvider.NAME.START) int start, @Field(APIProvider.NAME.PAGE_CNT) int pagenum);

    @FormUrlEncoded
    @POST(APIProvider.API_CHK_BROADCAST_PWD)
    Call<BaseModel> chkBroadcastPwd(@Field(APIProvider.NAME.BROADCAST) int no,@Field(APIProvider.NAME.LOCK_PASSWORD) String pwd);

    @FormUrlEncoded
    @POST(APIProvider.API_SEND_HEART)
    Call<BaseModel> sendHeart(@Field(APIProvider.NAME.USER_NO) int no,@Field(APIProvider.NAME.RECEIVER_NO)int receiver_no, @Field(APIProvider.NAME.HEART_CNT)int heart, @Field(APIProvider.NAME.BROADCAST)int b_no);

    @FormUrlEncoded
    @POST(APIProvider.API_LIKE_BROADCAST)
    Call<BaseModel> likeBroadcast(@Field(APIProvider.NAME.USER_NO) int no,@Field(APIProvider.NAME.BROADCAST)int broadcast_no);

    @FormUrlEncoded
    @POST(APIProvider.API_FAV_BROADCAST)
    Call<BaseModel> fanBroadcast(@Field(APIProvider.NAME.USER_NO) int no,@Field(APIProvider.NAME.BROADCAST)int broadcast_no);

    @FormUrlEncoded
    @POST(APIProvider.API_FAV_BROADCAST_LIST)
    Call<BroadcastList> getFavBroadcastList(@Field(APIProvider.NAME.USER_NO) int user_no, @Field(APIProvider.NAME.TYPE) int type,  @Field(APIProvider.NAME.ORDER) int order, @Field(APIProvider.NAME.START) int start, @Field(APIProvider.NAME.PAGE_CNT) int pagenum);

    @FormUrlEncoded
    @POST(APIProvider.API_DELETE_FAV_BROADCAST)
    Call<BaseModel> deletefavBroadcast(@Field(APIProvider.NAME.USER_NO) int no,@Field(APIProvider.NAME.BROADCAST)int broadcast_no);

    @FormUrlEncoded
    @POST(APIProvider.API_GET_NOTICE_LIST)
    Call<NoticeList> getNoticeList(@Field(APIProvider.NAME.START) int start, @Field(APIProvider.NAME.PAGE_CNT) int pagenum);

    @FormUrlEncoded
    @POST(APIProvider.API_MODIFY_PROFILE)
    Call<BaseModel> modifyProfile(@Field(APIProvider.NAME.EMAIL) String id,@Field(APIProvider.NAME.PASSWD) String pwd,@Field(APIProvider.NAME.USER_NAME) String name,@Field(APIProvider.NAME.USER_BIRTH) String birth);

    @FormUrlEncoded
    @POST(APIProvider.API_GET_AGREEMENT)
    Call<Agreement> getAgreement(@Field(APIProvider.NAME.TEXT_TYPE) String type);

    @FormUrlEncoded
    @POST(APIProvider.API_BOUGHT_ITEM)
    Call<BaseModel> boughtItem(@Field(APIProvider.NAME.USER_NO) int user_no, @Field(APIProvider.NAME.PURCHASE_DATA) String data, @Field(APIProvider.NAME.DATA_SIGNATURE) String sig);

    @FormUrlEncoded
    @POST(APIProvider.API_UPDATE_XMPP_JOIN_FLAG)
    Call<BaseModel> updateXmppJoinFlag(@Field(APIProvider.NAME.USER_NO) int user_no);

    @FormUrlEncoded
    @POST(APIProvider.API_GET_MY_LAST_LIVE)
    Call<Broadcast> getMyLastLive(@Field(APIProvider.NAME.USER_NO) int user_no);

    @FormUrlEncoded
    @POST(APIProvider.API_GET_SERVER_TIME)
    Call<ServerTime> getServerTime();

    @FormUrlEncoded
    @POST(APIProvider.API_GET_USER_BROADCAST_LIST)
    Call<BroadcastList> getUserBroadcastList(@Field(APIProvider.NAME.USER_NO) int user_no, @Field(APIProvider.NAME.TYPE) int type, @Field(APIProvider.NAME.CUR_VOD_NO) int vod_no, @Field(APIProvider.NAME.ORDER) int order, @Field(APIProvider.NAME.START) int start, @Field(APIProvider.NAME.PAGE_CNT) int pagenum);

    @FormUrlEncoded
    @POST(APIProvider.API_SET_ALARM)
    Call<BaseModel> setAlarm(@Field(APIProvider.NAME.USER_NO) int no, @Field(APIProvider.NAME.NOTICE) int notice);

    @FormUrlEncoded
    @POST(APIProvider.API_IN_OUT_BROADCAST)
    Call<BaseModel> inoutBroadcast(@Field(APIProvider.NAME.BROADCAST) int no, @Field(APIProvider.NAME.USER_NO) int user_no, @Field(APIProvider.NAME.TYPE) int type);

    @FormUrlEncoded
    @POST(APIProvider.API_UPDATE_BROADCAST_USER_FLAG)
    Call<BaseModel> updateBroadcastUserFlag(@Field(APIProvider.NAME.BROADCAST) int no, @Field(APIProvider.NAME.USER_NO) int user_no, @Field(APIProvider.NAME.EFFECT) int effect);

    @FormUrlEncoded
    @POST(APIProvider.API_CHK_ENTER_BROADCAST)
    Call<BroadcastUserEffect> chkEnterBroadcast(@Field(APIProvider.NAME.BROADCAST) int no, @Field(APIProvider.NAME.USER_NO) int user_no);

    @FormUrlEncoded
    @POST(APIProvider.API_GET_BROADCAST_USER_CNT)
    Call<ViewerCnt> getBroadcastUserCnt(@Field(APIProvider.NAME.BROADCAST) int no);

    @Multipart
    @POST(APIProvider.API_UPLOAD_BROADCAST_THUMB)
    Call<BaseModel> uploadBroadcastThumb(@PartMap Map<String, RequestBody> params);

    @FormUrlEncoded
    @POST(APIProvider.API_DELETE_BROADCAST_THUMB)
    Call<BaseModel> deleteBroadcastThumb(@Field(APIProvider.NAME.BROADCAST) int no, @Field(APIProvider.NAME.THUMB) String thumb_url);

    @FormUrlEncoded
    @POST(APIProvider.API_LOGOUT)
    Call<BaseModel> logout(@Field(APIProvider.NAME.USER_NO) int no);
}
