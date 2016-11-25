package com.yj.wangjatv.model;

import com.yj.wangjatv.Const;
import com.yj.wangjatv.utils.CommonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by kakaoapps on 2016-01-04.
 */
public class UserInfo extends BaseModel{

    public final static int GRADE_NORMAL = -1;
    public final static int GRADE_BRONZE = 0;
    public final static int GRADE_SILVER = 1;
    public final static int GRADE_GOLD = 2;
    public final static int GRADE_DIAMOND = 3;
    public final static int GRADE_VIP = 4;

    public int user_no;
    public String user_name;
    public String user_id;
    public String user_birthday;
    public String user_sex;
    public String user_email;
    public String user_area;
    public String user_phone;
    public int user_heart_cnt;
    public int user_grade = GRADE_NORMAL;
    public int user_notice_push;
    public int is_joined_xmpp = 0; // not, 1: joined
    public boolean is_loginned = false;

    public void copy(UserInfo info) {
        this.user_no = info.user_no;
        this.user_name = info.user_name;
        this.user_id = info.user_id;
        this.user_birthday = info.user_birthday;
        this.user_sex = info.user_sex;
        this.user_email = info.user_email;
        this.user_area = info.user_area;
        this.user_phone = info.user_phone;
        this.user_notice_push = info.user_notice_push;
        this.user_heart_cnt = info.user_heart_cnt;
        this.user_grade = info.user_grade;
    }

    public JSONObject toJSONObject() {
        JSONObject w_json = new JSONObject();

        try {
            w_json.put("no", user_no);
            w_json.put("nickname", user_name);
            w_json.put("user_grade", user_grade);

        } catch (JSONException e) {
            return null;
        }

        return w_json;
    }

    public static UserInfo fromJSONObject(JSONObject p_json) {
        UserInfo w_userInfo = null;

        try {
            w_userInfo = new UserInfo();
            w_userInfo.user_no = p_json.getInt("no");
            w_userInfo.user_name = p_json.getString("nickname");
            w_userInfo.user_grade = p_json.getInt("user_grade");

        } catch (JSONException e) {
            return null;
        }

        return w_userInfo;
    }

    public boolean isValid() {
        return user_no > 0 && !user_id.isEmpty() &&!user_name.isEmpty();
    }

    public boolean isAdult() {

        Date startDate = CommonUtil.getDateFromString(user_birthday, Const.DEFAULT_DATE_FORMAT_1);
        Date currentDate = new Date();
        long days = CommonUtil.diffOfDate2(startDate, currentDate);

        if(days < 19*365){
            return false;
        }

        return  true;
    }
}
