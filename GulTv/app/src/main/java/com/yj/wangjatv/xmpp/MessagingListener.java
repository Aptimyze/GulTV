package com.yj.wangjatv.xmpp;

import com.yj.wangjatv.model.UserInfo;

/**
 * Created by toltori on 2014. 8. 26..
 */
public interface MessagingListener {
    void onNewMessageText(XmppPacket packet, String p_strId, String p_strContent);
    void onNewMessageImage(XmppPacket packet, String p_strId, String p_strContent);

    void onNewMessageRequest(UserInfo p_fromUserInfo, String p_strId);

    void onPing(UserInfo p_fromUserInfo, String p_strId);

    void onPong(UserInfo p_fromUserInfo, String p_strId);
}
