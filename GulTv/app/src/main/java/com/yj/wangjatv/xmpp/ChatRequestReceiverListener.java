package com.yj.wangjatv.xmpp;


import com.yj.wangjatv.model.UserInfo;

/**
 * Created by toltori on 2014. 8. 14..
 */
public interface ChatRequestReceiverListener {
    public void onNewChatRequest(UserInfo p_fromUser);

    public void onChatStart(UserInfo p_fromUser, boolean p_bVideo);

    public void onChatBusy(UserInfo p_fromUser);
}
