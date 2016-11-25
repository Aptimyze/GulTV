package com.yj.wangjatv.model;

import java.io.Serializable;

/**
 * Created by Ralph on 5/31/2016.
 */
public class BaseModel  implements Serializable {

    public static final int OK_DATA = 0;

    public int status = 0;
    public String msg = "";
}
