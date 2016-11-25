package com.yj.wangjatv.model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Ralph on 5/23/2016.
 */
public class Broadcast extends BaseModel {

    public final static int VIDEO_QUALITY_HIGH = 0;
    public final static int VIDEO_QUALITY_NORMAL = 1;
    public final static int VIDEO_QUALITY_LOW = 2;
    public final static int LIVE_TYPE_FREE = 0;
    public final static int LIVE_TYPE_MONEY = 1;
    public final static int LIVE_TYPE_FAN = 2;
    public final static int LIVE_TYPE_ONE = 3;
    public final static int LIVE_TYPE = 0;
    public final static int VOD_TYPE = 1;
    public final static int WAIT_TYPE = 2;

    public final static int EFFECT_NO = 0;
    public final static int EFFECT_BLIND = 1;
    public final static int EFFECT_CAPTURE_DEFEND = 2;

    public final static int EFFECT_USER_DEFAULT = 0;
    public final static int EFFECT_USER_FORCE_EXIT = 1;

    public int no = -1;
    public int allow_adult;
    public int video_quality;
    public int live_type;
    public String lock_password;
    public int type;
    public int category_no;
    public String  category_name;
    public int viewer_cnt;
    public int heart_cnt;
    public int like_cnt;
    public String title;
    public String vod_url;
    public String thumb_url;
    public String reg_time;
    public int max_viewer_cnt;
    public int user_no;
    public int user_grade = UserInfo.GRADE_NORMAL;
    public String user_name;
    public String user_id;
    public int effect;
    public int already_sent;
    public ArrayList<HeartHistory> history_list = new ArrayList<>();
    public boolean is_offline=false;

    public String getBjID() {
        String ret = "";
        int idx = user_id.indexOf("@");
        if(idx > 0) {
            ret = user_id.substring(0, idx);
        }
        return ret;
    }

    public boolean isUroBroadcast() {
        if((type == Broadcast.LIVE_TYPE_FAN || type == Broadcast.LIVE_TYPE_MONEY) && heart_cnt > 0) {
            return true;
        }
        return false;
    }
}

