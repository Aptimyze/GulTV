package com.yj.wangjatv.xmpp;


import com.yj.wangjatv.model.UserInfo;

/**
 * Created by toltori on 2014. 8. 14..
 */
public interface ChatRequestSenderListener {
    public void onChatAccept(UserInfo p_fromUser);

    public void onChatRefuse(UserInfo p_fromUser);

    public void onChatBusy(UserInfo p_fromUser);
}
