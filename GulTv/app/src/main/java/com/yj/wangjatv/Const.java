package com.yj.wangjatv;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.io.File;

public interface Const {

    String TAG = "Consulting";

    final boolean IS_TESETING = true;

    // xmpp 관련
    String XMPP_SERVER_NAME = "rentin.cafe24.com";
    String XMPP_CONFERENCE_SERVICE = "conference." + XMPP_SERVER_NAME;
    //    int XMPP_SERVER_PORT = 9090;
    int XMPP_SERVER_PORT = 5222;

    String TAG_CHAT = "wangjatv_talk";
    String TAG_CHATROOM = "wangjatv_talk_room";

    final String DEVICE_ANDROID = "1";

    //데이터 베이스 버전
    final int DATABASE_BASE_VERSION = 1;
    final int DATABASE_FAVORITE_VERSION = 1;

    //gcm 관련
    String GCM_SENDERID = "1059664305050";
    String GCM_APIKEY = "AIzaSyDKp34njpo55hbY9ZLXZiCxVQ2tbQ85ITk";
    final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    final String REGISTRATION_COMPLETE = "registrationComplete";

    boolean DEBUG = false;

    String SDCARD_FOLDER = "WangjaTV";
    String PhOTO_FOLDER = SDCARD_FOLDER +  File.separator + "Photo";
    DisplayImageOptions IMG_DISPOPTION1 = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.mipmap.ic_launcher)
            .showImageForEmptyUri(R.mipmap.ic_launcher)
            .showImageOnFail(R.mipmap.ic_launcher)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .build();


    String KEY_INTENT_EVENT = "event";
    String KEY_INTENT_LIVE = "live";
    String KEY_INTENT_BROADCAST = "broadcast";
    String KEY_INTENT_HAS_HISTORY = "has_history";
    String KEY_INTENT_MSG = "msg";
    String KEY_INTENT_DIALOG_TYPE = "type";

    String KEY_PREF_PUSH_TOKEN = "push_token";
    String KEY_PREF_USER_ID = "user_id";
    String KEY_PREF_USER_PWD = "user_pwd";
    String KEY_PREF_LIVE_SERVER_IP = "live_server_ip";
    String KEY_PREF_LIVE_SERVER_PORT = "live_server_port";
    String KEY_PREF_LIVE_PREFIX = "live_prefix";
    String KEY_PREF_LIVE_APP_NAME = "live_app_name";
    String KEY_PREF_LIVE_PROTOCAL = "live_protocol";
    String KEY_PREF_LIVE_BITRATE = "live_bitrate";
    String KEY_PREF_LIVE_VIDEO_W = "live_videow";
    String KEY_PREF_LIVE_VIDEO_H = "live_videoh";
    String KEY_PREF_RECORD_DIR = "record_dir";
    String KEY_PREF_RECORD_TYPE = "flv";

    String KEY_PREF_SETTING_AUTOLOGIN = "setting_autologin";
    String KEY_PREF_SETTING_3G4G = "setting_3g4g";
    String KEY_PREF_SETTING_AUTOROTATE = "setting_autorotate";

    String DEFAULT_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
    String DEFAULT_DATE_FORMAT_1 = "yyyy-MM-dd";

    int  INTENT_ACTIVITY_FLAG_PROFILE = 1000;
    int  INTENT_ACTIVITY_LIVE  = 1001;
    int  INTENT_ACTIVITY_VOD  = 1002;
}

