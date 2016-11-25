package com.yj.wangjatv.model;

/**
 * Created by Ralph on 6/6/2016.
 */
public class ChatMessage extends BaseModel{

    public String user_name;
    public String content;
    public int    user_no;
    public int    user_grade;
    public int    forced_exit = 0; // 0, 1
    public int    is_bj = 1; // 0, 1
}
