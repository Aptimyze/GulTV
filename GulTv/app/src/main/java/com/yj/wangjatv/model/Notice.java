package com.yj.wangjatv.model;

/**
 * Created by john on 2016-02-11.
 */
public class Notice  extends BaseModel {
    public String subject;
    public String writer;
    public String content;
    public String regdt;
    public String upfile1;
    public String upfile2;
    public String upfile3;
    public String upfile4;
    public String upfile5;
    public int total_rows;


    public String getImage(){
        String upfile = "";
        if(upfile1 != null && upfile1.isEmpty() == false) {
            upfile = upfile1;
            return upfile;
        }

        if(upfile2 != null && upfile2.isEmpty() == false) {
            upfile = upfile2;
            return upfile;
        }

        if(upfile3 != null && upfile3.isEmpty() == false) {
            upfile = upfile3;
            return upfile;
        }

        if(upfile4 != null && upfile4.isEmpty() == false) {
            upfile = upfile4;
            return upfile;
        }

        if(upfile5 != null && upfile5.isEmpty() == false) {
            upfile = upfile5;
            return upfile;
        }
        return upfile;
    }
}
