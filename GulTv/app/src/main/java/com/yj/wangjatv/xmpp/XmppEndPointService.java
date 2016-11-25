package com.yj.wangjatv.xmpp;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.yj.wangjatv.Const;
import com.yj.wangjatv.R;
import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.UserInfo;
import com.yj.wangjatv.utils.CommonUtil;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.json.JSONObject;

import retrofit2.Callback;
import retrofit2.Response;


/**
 * XmppEndPointService : Xmpp통신채널을 설립, 해제, 쪽지수신처리를 담당하는 서비스.
 * 폰의 기동시 자동으로 시작된다.
 */
public class XmppEndPointService extends Service implements MessagingListener, ConnectionListener {

    //////////////////////////////////
    // 데이터멤버.
    //////////////////////////////////

    private static final String TAG = "XmppEndPointService";
    private WangjaTVApp m_app = null;
    private XmppEndPoint m_xmppEndpoint = null;
    private Handler m_hUIThread = null;
    private int m_nTimeCount = 0;


    //////////////////////////////////
    // 이벤트핸들러.
    //////////////////////////////////

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent p_intent, int p_nFlags, int p_nServiceId) {
        super.onStartCommand(p_intent, p_nFlags, p_nServiceId);

        //
        // 등록된 유저정보가 있으면 Xmpp채널 수립.
        //
        m_hUIThread = new Handler();
        m_app = (WangjaTVApp) getApplicationContext();
        if (m_app != null && m_app.getUserInfo() != null && m_app.getUserInfo().isValid()) {
            // XMPP로그인.
            loginXmpp();
        } else {
            String email = m_app.get_string_SharedPreferences(Const.KEY_PREF_USER_ID);
            String password = m_app.get_string_SharedPreferences(Const.KEY_PREF_USER_PWD);
            if (email.isEmpty()) {
                return START_STICKY;
            }
            m_appLoginHandler.sendEmptyMessage(0);
        }
        m_roomJoinedCheckHandler.sendEmptyMessage(0);

        return START_STICKY;
    }

    Handler m_appLoginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            final String email = m_app.get_string_SharedPreferences(Const.KEY_PREF_USER_ID);
            final String password = m_app.get_string_SharedPreferences(Const.KEY_PREF_USER_PWD);
            final APIProvider server = new APIProvider();

            int type = 1;
            String device_id = CommonUtil.getDeviceId(m_app);
            String push_token = m_app.getPushToken();

            server.login(m_app,email, password, type, device_id, push_token, new retrofit2.Callback<UserInfo>() {
                @Override
                public void onResponse(Response<UserInfo> response) {
                    UserInfo model = response.body();

                    if (model != null) {
                        if(model.status == BaseModel.OK_DATA) {
                            m_app.set_string_SharedPreferences(Const.KEY_PREF_USER_ID, email);
                            m_app.set_string_SharedPreferences(Const.KEY_PREF_USER_PWD, password);
                            m_app.getUserInfo().copy(model);
                            m_app.getUserInfo().is_loginned = true;
                            loginXmpp();
                        }
                        else {
                            m_appLoginHandler.sendEmptyMessageDelayed(0, 5000);
                        }

                    } else {
                        m_appLoginHandler.sendEmptyMessageDelayed(0, 5000);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    m_appLoginHandler.sendEmptyMessageDelayed(0, 5000);
                }
            });
        }
    };

    Handler m_roomJoinedCheckHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (m_xmppEndpoint == null) {
                return;
            }

            if (m_xmppEndpoint.isConnected()) {
                if (m_xmppEndpoint.isAuthenticated()) {
                } else {
                }
            } else {
            }
            m_roomJoinedCheckHandler.sendEmptyMessageDelayed(0, 60000);

            m_nTimeCount++;

            if (m_nTimeCount > 30) {
                m_nTimeCount = 0;
                m_xmppLoginHandler.sendEmptyMessageDelayed(0, 100);
            }
        }
    };

    Handler m_xmppLoginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            loginXmpp();
        }
    };


    @Override
    public void onDestroy() {

        //
        // Xmpp채널 해방.
        //
        if (m_app != null && m_xmppEndpoint != null) {
            m_xmppEndpoint.doDisconnect();
            m_xmppEndpoint.removeMessagingListener(this);
            m_xmppEndpoint.setMessageReading(false, null);
        }

        super.onDestroy();
    }

    private void loginXmpp() {
        // Disconnect if now is connected state
        if (m_app.getXmppEndPoint().isConnected()) {
            m_app.getXmppEndPoint().doDisconnect();
            m_app.initXmpp();
        }

        new Thread() {
            @Override
            public void run() {
                m_nTimeCount = 0;

                // Connect to xmpp
                m_xmppEndpoint = m_app.getXmppEndPoint();
                boolean w_bSuccess = m_xmppEndpoint.doConnect();
                if (!w_bSuccess) {
                    m_hUIThread.post(new Runnable() {
                        @Override
                        public void run() {
                            m_xmppLoginHandler.sendEmptyMessageDelayed(0, 100);
                        }
                    });
                } else {
                    m_xmppEndpoint.addConnectionListener(XmppEndPointService.this);

                    String strXmppUserID = XmppEndPoint.getXMPPUserID(m_app.getUserInfo());

                    if (m_app.getUserInfo().is_joined_xmpp == 0) {
                        while (true) {
                            try {

                                w_bSuccess = m_xmppEndpoint.createAccount(strXmppUserID, strXmppUserID);
//                                w_bSuccess = true;
                                if (w_bSuccess) {
                                    break;
                                } else {
                                    w_bSuccess = m_xmppEndpoint.login(strXmppUserID, strXmppUserID);
                                    //w_bSuccess = m_xmppEndpoint.login("test1", "1");
                                    if (w_bSuccess)
                                        break;
                                }
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (!m_xmppEndpoint.isAuthenticated()) {
                            w_bSuccess = m_xmppEndpoint.login(strXmppUserID, strXmppUserID);
                            //w_bSuccess = m_xmppEndpoint.login("test1", "1");
                            if (!w_bSuccess) {
                                m_hUIThread.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        m_xmppLoginHandler.sendEmptyMessageDelayed(0, 100);
                                    }
                                });
                                return;
                            }
                        }
                        m_updateXmppJoinFlagHandler.sendEmptyMessage(0);
                        /*
                        remove by yj. why is it needed?
                        while (true) {
                            RoomInfo room = new RoomInfo();
                            room.name = Constant.CHAT_ROOM_NAME;
                            room.password = Constant.CHAT_ROOM_NAME;

                            if (m_xmppEndpoint.enterRoom(room) == null) {
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                m_hUIThread.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(XmppEndPointService.this, "Room joined", Toast.LENGTH_SHORT).show();

                                        m_updateXmppJoinFlagHandler.sendEmptyMessage(0);
                                    }
                                });
                                break;
                            }
                        }*/
                    } else {
                        w_bSuccess = m_xmppEndpoint.login(strXmppUserID, strXmppUserID);
                        if (!w_bSuccess) {
                            m_hUIThread.post(new Runnable() {
                                @Override
                                public void run() {
                                    m_xmppLoginHandler.sendEmptyMessageDelayed(0, 100);
                                }
                            });
                            return;
                        }

                        //
                        // xmpp수신 메시지처리부 등록.
                        //
                        m_xmppEndpoint.addMessagingListenerAt(0, XmppEndPointService.this);
                        m_xmppEndpoint.setMessageReading(false, null);
                    }
                }
            }
        }.start();
    }

    Handler m_updateXmppJoinFlagHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            final APIProvider server = new APIProvider();

            server.updateXmppJoinFlag(m_app, m_app.getUserInfo().user_no, new retrofit2.Callback<BaseModel>() {
                @Override
                public void onResponse(Response<BaseModel> response) {
                    BaseModel model = response.body();

                    if (model != null) {
                        if (model.status == BaseModel.OK_DATA) {
                            m_app.getUserInfo().is_joined_xmpp = 1;
                            m_xmppEndpoint.addMessagingListenerAt(0, XmppEndPointService.this);
                            m_xmppEndpoint.setMessageReading(false, null);
                        } else {
                            m_appLoginHandler.sendEmptyMessageDelayed(0, 5000);
                        }

                    } else {
                        m_appLoginHandler.sendEmptyMessageDelayed(0, 5000);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    m_appLoginHandler.sendEmptyMessageDelayed(0, 5000);
                }
            });
        }
    };


    //////////////////////////////////////////////////////////////////////////////////////
    // Xmpp MessagingListener implementation
    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * 새로운 메시지가 도착하였다는것을 알림.
     *
     * @param p_strMessage 새 메시지 내용.
     * @since ver 1.0 Aug 29 2014
     */
    private void notifyNewMessage(String p_strMessage) {
        //
        // 알림통지를 등록.
        //
//        NotificationManager w_notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification w_notification = new Notification(R.mipmap.ic_launcher, p_strMessage, System.currentTimeMillis());
//        w_notification.flags = Notification.FLAG_AUTO_CANCEL;
//
////        if (m_app.m_loginUser.NotifySound) {
//        w_notification.defaults |= Notification.DEFAULT_SOUND;
////        }
////        if (m_app.m_loginUser.NotifyVibrate) {
//        w_notification.defaults |= Notification.DEFAULT_VIBRATE;
////        }
//
//        Intent w_notificationIntent = null;
//        w_notificationIntent = new Intent(this, MainActivity.class);
//        w_notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        w_notificationIntent.putExtra("Notification", "NewMessage");
//
//        PendingIntent w_pendingIntentToApp = PendingIntent.getActivity(this, 0, w_notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        w_notification.setLatestEventInfo(this, this.getString(R.string.app_name), p_strMessage, w_pendingIntentToApp);
//        w_notificationManager.notify(1, w_notification);
    }

    @Override
    public void connected(XMPPConnection connection) {

    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {

    }

    @Override
    public void connectionClosed() {
        m_xmppLoginHandler.sendEmptyMessageDelayed(0, 100);
    }

    @Override
    public void connectionClosedOnError(Exception e) {

    }

    @Override
    public void reconnectionSuccessful() {

    }

    @Override
    public void reconnectingIn(int seconds) {

    }

    @Override
    public void reconnectionFailed(Exception e) {

    }

    @Override
    public void onNewMessageText(XmppPacket packet, String p_strId, String p_strContent) {
        notifyNewMessage(p_strContent);
    }

    public void onNewMessageImage(XmppPacket packet, String p_strId, String p_strContent) {
        notifyNewMessage(p_strContent);
    }

    @Override
    public void onNewMessageRequest(UserInfo p_fromUserInfo, String p_strId) {

    }

    @Override
    public void onPing(UserInfo p_fromUserInfo, String p_strId) {

    }

    @Override
    public void onPong(UserInfo p_fromUserInfo, String p_strId) {

    }
}
