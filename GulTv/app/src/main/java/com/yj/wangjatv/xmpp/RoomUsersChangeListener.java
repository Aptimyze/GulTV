package com.yj.wangjatv.xmpp;

/**
 * Created by toltori on 2014. 9. 18..
 */
public interface RoomUsersChangeListener {
    public void onRoomUserJoined(int p_nUserId);

    public void onRoomUserLeaved(int p_nUserId);

    public void onRoomUserChatStarted(int p_nUserId);

    public void onRoomUserChatEnded(int p_nUserId);
}
