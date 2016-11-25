package com.yj.wangjatv.xmpp;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.Const;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.model.UserInfo;
import com.yj.wangjatv.WangjaTVApp;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateListener;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by toltori on 2014. 8. 14..
 */
public class XmppEndPoint extends Thread implements ConnectionListener {

    public enum ChatStatus {
        ChatStatus_None,
        ChatStatus_Ready,
        ChatStatus_Sender_WaitAcept,
        ChatStatus_WaitAccept,
        ChatStatus_WaitStart,
        ChatStatus_Chatting
    }


    ////////////////////////////////////
    // 데이터멤버.
    ////////////////////////////////////
    private WangjaTVApp m_app;
    private Handler m_hUIHandler;
    private String m_strConferenceService;

    private AbstractXMPPConnection m_xmppConnection;
    private XMPPTCPConnectionConfiguration m_xmppConfiguration;
    private MultiUserChat m_muc;
    private PresenceListener m_mucPresencePacketListener;
    private MessageListener m_mucMessageListener;
    private RoomUsersChangeListener m_roomUsersChangeListener;

    private ChatStatus m_chatStatus;
    private ArrayList<UserInfo> m_lstChatRequesters;    // 나에게 대화요청을 보내온 유저목록.
    private ArrayList<UserInfo> m_lstChatRequestPeers;  // 내가 대화요청을 보낸 유저목록.
    private ChatRequestSenderListener m_chatRequestSenderListener;
    private ChatRequestReceiverListener m_chatRequestReceiverListener;

    private boolean m_bMessageReading;
    private UserInfo m_messageReadingPeerUser;  // 메시지대화창에서 대화중인 상대방 유저.
    private ArrayList<MessagingListener> m_lstMessagingListeners = new ArrayList<MessagingListener>();   // XMPP로 수신되는 메시지를 처리하는 처리부 객체목록.

    public boolean directVideo = false;
    public boolean zzalbeBtn = false;

    public ConnectionListener m_connectionListener = null;
    public boolean isManuallyClosed = false;

    public Chat m_chat = null;

    ////////////////////////////////////
    // xmpp이벤트 핸들러등록.
    ////////////////////////////////////

    public ArrayList<UserInfo> getChatRequesters() {
        return m_lstChatRequesters;
    }

    public UserInfo getLastChatRequester() {
        if (m_lstChatRequesters == null || m_lstChatRequesters.size() == 0) {
            return null;
        }
        return m_lstChatRequesters.get(m_lstChatRequesters.size() - 1);
    }

    public void setChatRequestSenderListener(ChatRequestSenderListener p_listener) {
        m_chatRequestSenderListener = p_listener;
    }

    public void setChatRequestReceiverListener(ChatRequestReceiverListener p_listener) {
        m_chatRequestReceiverListener = p_listener;
    }

    public boolean getMessageReading() {
        return m_bMessageReading;
    }

    public void setMessageReading(boolean p_bMessageReading, UserInfo p_messageReadingPeerUser) {
        m_bMessageReading = p_bMessageReading;
        m_messageReadingPeerUser = p_messageReadingPeerUser;
    }

    public UserInfo getMessageReadingPeerUser() {
        return m_messageReadingPeerUser;
    }

    //
    // XmppEndPointServce객체는 MessagingListener목록중 우선도가 가장 높게 설정되여야 하며
    // addMessagingListener()함수를 이용하여 첫위치에 등록.
    //
    public void addMessagingListenerAt(int p_nIndex, MessagingListener p_listener) {
        m_lstMessagingListeners.add(p_nIndex, p_listener);
    }

    public void addMessagingListener(MessagingListener p_listener) {
        m_lstMessagingListeners.add(p_listener);
    }

    public void removeMessagingListener(MessagingListener p_listener) {
        m_lstMessagingListeners.remove(p_listener);
    }

    //
    // XMPP로 수신되는 메시지를 처리하는 처리부가 있는가 판정.
    //
    // 푸시메시지처리부(GCMIntentService.java/onMessage())에서 호출됨.
    // 상대방으로부터 쪽지푸시메시지를 받았을때, XMPP의 쪽지처리부가 동작하지 않는다면 직접 Notification을 현시.
    //
    public boolean isThereAnyMessagingListener() {
        return m_lstMessagingListeners != null && !m_lstMessagingListeners.isEmpty();
    }

    public void setRoomUsersChangeListener(RoomUsersChangeListener p_listener) {
        m_roomUsersChangeListener = p_listener;
    }

    public boolean getRoomUsersChangeListener() {
        if (m_roomUsersChangeListener == null) return false;

        return true;
    }

    /**
     * Create the chat room
     */
    public boolean createRoom(String roomName, String nickname) {
        try {
            // Get the MultiUserChatManager
            MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(m_xmppConnection);

            // Get a MultiUserChat using MultiUserChatManager
            MultiUserChat muc = manager.getMultiUserChat(roomName);
            muc.create(nickname);
            // Send an empty room configuration form which indicates that we want
            // an instant room
//            muc.sendConfigurationForm(new Form(DataForm.Type.submit));

            Form form = muc.getConfigurationForm();
            Form submitForm = form.createAnswerForm();
            List<FormField> fields = submitForm.getFields();
            for (int i = 0; i < fields.size(); i++) {
                FormField field = (FormField) fields.get(i);
                if (!FormField.Type.hidden.equals(field.getType())
                        && field.getVariable() != null) {
                    submitForm.setDefaultAnswer(field.getVariable());
                }
            }
            submitForm.setAnswer("muc#roomconfig_publicroom", true);
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);
            muc.sendConfigurationForm(submitForm);

            return true;
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean createRoom(int nRoomId) {
        String roomName = getRoomName(nRoomId);
        String nickName = Const.TAG_CHATROOM + nRoomId;
        return createRoom(roomName, nickName);
    }

    private String getRoomName(int nRoomId) {
        return Const.TAG_CHATROOM + nRoomId + "@" + m_strConferenceService;
    }

    public void getHistory() {
//        while (true) {
//            Message msg = null;
//            try {
//                msg = m_muc.nextMessage(SmackConfiguration.getDefaultPacketReplyTimeout());
//            } catch (MUCNotJoinedException e) {
//                e.printStackTrace();
//            }
//
//            if (msg == null)
//                return;
//        }
    }


    ////////////////////////////////////
    // 유저의 액션.
    ////////////////////////////////////

    /*
    * 방에 입장한 유저목록수 리턴
    */

    public int getMembersInChatRoom(int nRoomId) {
        String w_strRoomName = getRoomName(nRoomId);
        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(m_xmppConnection);
        MultiUserChat muc = manager.getMultiUserChat(w_strRoomName);
        return muc.getOccupantsCount();
    }

    /**
     * 방에 입장한후 방유저아이디목록 리턴.
     *
     * @param p_roomInfo
     * @return
     */
    public List<String> enterRoom(Broadcast p_roomInfo) {
        List<String> w_lstMembers = new ArrayList<String>();
//        m_chatStatus = ChatStatus.ChatStatus_Ready;
        m_chatStatus = ChatStatus.ChatStatus_Chatting;
        m_lstChatRequesters = new ArrayList<UserInfo>();
        m_lstChatRequestPeers = new ArrayList<UserInfo>();

        String w_strRoomName = getRoomName(p_roomInfo.no);
        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(m_xmppConnection);
        m_muc = manager.getMultiUserChat(w_strRoomName);
        try {
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(0);
            String strID = getXMPPUserID(m_app.getUserInfo());
            //m_muc.join(strID, strID, history, SmackConfiguration.getDefaultPacketReplyTimeout() * 4);
            m_muc.join(m_app.getUserInfo().user_name, strID, history, SmackConfiguration.getDefaultPacketReplyTimeout() * 4);
        } catch (SmackException e) {
            e.printStackTrace();
            return null;
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            return null;
        }

        if (m_mucPresencePacketListener == null) {
            m_mucPresencePacketListener = new PresenceListener() {
                @Override
                public void processPresence(Presence presence) {
                    if (m_roomUsersChangeListener != null) {
                        Presence w_presencePacket = presence;

                        String w_strFromUserJid = w_presencePacket.getFrom();
                        String w_strFromUserId = w_strFromUserJid.substring(w_strFromUserJid.lastIndexOf('/') + 1 + Const.TAG_CHAT.length());

                        final int w_nFromUserId = Integer.parseInt(w_strFromUserId);

                        if (w_presencePacket.getType() == Presence.Type.available) {

                            //
                            // New user joined
                            //
                            m_hUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (m_roomUsersChangeListener != null)
                                        m_roomUsersChangeListener.onRoomUserJoined(w_nFromUserId);
                                }
                            });
                        } else if (w_presencePacket.getType() == Presence.Type.unavailable) {
                            //
                            // 채팅 요청보낸 유저가 방에서 나간 경우, 채팅요청대기열에서 제거.
                            //
                            removeChatRequestedPeer(w_nFromUserId);

                            //
                            // A user leaved.
                            //
                            m_hUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (m_roomUsersChangeListener != null)
                                        m_roomUsersChangeListener.onRoomUserLeaved(w_nFromUserId);
                                }
                            });
                        }
                    }
                }
            };
        }

        if (m_mucMessageListener == null) {
            m_mucMessageListener = new MessageListener() {
                @Override
                public void processMessage(Message message) {
                    if (m_roomUsersChangeListener != null) {
                        Message w_messagePacket = message;

                        String w_strFromUserJid = w_messagePacket.getFrom();
                        String w_strFromUserId = w_strFromUserJid.substring(w_strFromUserJid.lastIndexOf('/') + 1 + Const.TAG_CHAT.length());

                        try {
                            final int w_nFromUserId = Integer.parseInt(w_strFromUserId);
                            if (w_nFromUserId != m_app.getUserInfo().user_no) {
                                String w_strBody = w_messagePacket.getBody();
                                if (w_strBody != null) {
                                    if (w_strBody.equals("ChatStarted")) {
                                        //
                                        // Chatting started
                                        //
                                        m_hUIHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (m_roomUsersChangeListener != null)
                                                    m_roomUsersChangeListener.onRoomUserChatStarted(w_nFromUserId);
                                            }
                                        });
                                    } else if (w_strBody.equals("ChatEnded")) {
                                        //
                                        // Chatting ended
                                        //
                                        m_hUIHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (m_roomUsersChangeListener != null)
                                                    m_roomUsersChangeListener.onRoomUserChatEnded(w_nFromUserId);
                                            }
                                        });
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            };
        }

        m_muc.addParticipantListener(m_mucPresencePacketListener);
        m_muc.addMessageListener(m_mucMessageListener);

        return w_lstMembers;
    }

    public List<String> enterRoom(int nRoomId) {
        Broadcast room = new Broadcast();
        room.no = nRoomId;
        return enterRoom(room);
    }

    public void createChat(String userJID) {
        Roster roster = Roster.getInstanceFor(m_xmppConnection);
        Collection<RosterEntry> entries = roster.getEntries();
        for (RosterEntry entry : entries) {
            System.out.println(entry);
        }
    }

    public void sendChatMessage(UserInfo p_toUserInfo, String p_strContent) {
        ChatManager chatManager = ChatManager.getInstanceFor(m_xmppConnection);
        m_chat = chatManager.createChat(getJidName(p_toUserInfo), new ChatStateListener() {
            @Override
            public void stateChanged(Chat chat, ChatState state) {
                switch (state) {
                    case active:
                        Log.d("state", "active");
                        break;
                    case composing:
                        Log.d("state", "composing");
                        break;
                    case paused:
                        Log.d("state", "paused");
                        break;
                    case inactive:
                        Log.d("state", "inactive");
                        break;
                    case gone:
                        Log.d("state", "gone");
                        break;
                }
            }

            @Override
            public void processMessage(Chat chat, Message message) {
                Log.d("processMessage", "processMessage");
            }
        });

        XmppPacket w_pkt = new XmppPacket(XmppPacket.PacketType.PacketType_Message, XmppPacket.PacketSubType.PacketSubType_MessageText, m_app.getUserInfo(), p_toUserInfo, p_strContent);

        Message w_msg = new Message();
        w_msg.setType(Message.Type.chat);
        w_msg.setTo(getJidName(p_toUserInfo));
        w_msg.setBody(w_pkt.toJSONString());

        try {
            ChatStateManager mgr = ChatStateManager.getInstance(m_xmppConnection);
            mgr.setCurrentState(ChatState.paused, m_chat);

            m_chat.sendMessage(w_msg);

            PingManager pingManager = PingManager.getInstanceFor(m_xmppConnection);
            try {
                pingManager.ping(getJidName(p_toUserInfo));
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            }

        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public boolean isJoinedRoom() {
        if (m_muc == null)
            return false;

        return m_muc.isJoined();
    }

    public List<String> getMembers() {
        return m_muc.getOccupants();
    }

    public void leaveRoom() {
        m_chatStatus = ChatStatus.ChatStatus_None;
        m_lstChatRequesters = null;
        m_lstChatRequestPeers = null;

        if (m_muc == null)
            return;
        try {
            m_muc.leave();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        m_muc.removeParticipantListener(m_mucPresencePacketListener);
        m_muc.removeMessageListener(m_mucMessageListener);
    }

    /**
     * 이미 채팅요청을 보낸 사용자인가 체크.
     *
     * @param p_peerUserInfo
     * @return
     */
    public boolean isChatRequestedPeer(UserInfo p_peerUserInfo) {
        if (m_lstChatRequestPeers != null) {
            for (UserInfo w_peerUserInfo : m_lstChatRequestPeers) {
                if (w_peerUserInfo.user_no == p_peerUserInfo.user_no)
                    return true;
            }
        }

        return false;
    }

    /**
     * 해당 유저가 채팅요청보낸 유저라면 목록에서 제거.
     *
     * @param p_nPeerUserId
     */
    public void removeChatRequestedPeer(int p_nPeerUserId) {
        UserInfo w_peerToRemove = null;
        if (m_lstChatRequestPeers != null) {
            for (UserInfo w_peerUserInfo : m_lstChatRequestPeers) {
                if (w_peerUserInfo.user_no == p_nPeerUserId)
                    w_peerToRemove = w_peerUserInfo;
            }
        }

        if (w_peerToRemove != null)
            m_lstChatRequestPeers.remove(w_peerToRemove);
    }


    //
    // 채팅방의 다른 유저들에게 내가 대화를 시작/종료하였다는것을 통지.
    //
    public void notifyChatStarted() {
        if (m_muc != null) {
            try {
                m_muc.sendMessage("ChatStarted");
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyChatEnded() {
        if (m_muc != null) {
            try {
                m_muc.sendMessage("ChatEnded");
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(Message message) {
        if (m_muc != null) {
            try {
                m_muc.sendMessage(message);
            } catch (/*SmackException.NotConnectedException*/Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendFileOnGroupChat(File file) {

    }

    public OutgoingFileTransfer sendFile(File file, String description, String userID) {
        FileTransferManager manager = FileTransferManager.getInstanceFor(m_xmppConnection);
        OutgoingFileTransfer outgoingFileTransfer = manager.createOutgoingFileTransfer(userID);
        try {
            outgoingFileTransfer.sendFile(file, description);
        } catch (SmackException e) {
            return null;
        }
        return outgoingFileTransfer;
    }

    public void processReceivingFile(int action) {
        FileTransferManager manager = FileTransferManager.getInstanceFor(m_xmppConnection);
        manager.addFileTransferListener(new FileTransferListener() {
            @Override
            public void fileTransferRequest(FileTransferRequest request) {
//                if (shouldAccept(request)) {
//                    IncomingFileTransfer transfer = request.accept();
//                    try {
//                        transfer.recieveFile(new File("shakespeare_complete_works.txt"));
//                        while (!transfer.isDone()) {
//                            if (transfer.getStatus().equals(FileTransfer.Status.error)) {
//                                Log.d("XMPP FILETRANSFER", "FILE SENT ERROR!!!" + transfer.getError());
//                            } else {
//                                Log.d("XMPP FILETRANSFER", "STATUS = " + transfer.getStatus());
//                                Log.d("XMPP FILETRANSFER", "PROGRESS = " + transfer.getProgress());
//                            }
//                        }
//                    } catch (SmackException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    try {
//                        request.reject();
//                    } catch (SmackException.NotConnectedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        });
        switch (action) {
            case 0: // accept
                break;
            case 1: // reject
                break;
            case 2: // ignore
        }
    }

    ////////////////////////////////////
    // 초기화.
    ////////////////////////////////////

    public XmppEndPoint(Context p_context, String p_strServerIp, int p_nPort, String p_strConferenceService) {
        m_app = (WangjaTVApp) p_context.getApplicationContext();
        m_strConferenceService = p_strConferenceService;
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setServiceName(p_strServerIp);
        configBuilder.setPort(p_nPort);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        m_xmppConfiguration = configBuilder.build();
        m_chatStatus = ChatStatus.ChatStatus_None;
        m_hUIHandler = new Handler();

//        SmackAndroid.init(m_app);
    }

    public boolean isConnected() {
        return m_xmppConnection != null && m_xmppConnection.isConnected();
    }

    public boolean createAccount(String p_strId, String p_strPassword) {
        try {
            AccountManager mAccount = AccountManager.getInstance(m_xmppConnection);

            if (mAccount.supportsAccountCreation()) {
                mAccount.createAccount(p_strId, p_strPassword);
            }
            return true;
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            if (e.getXMPPError().getCondition() == XMPPError.Condition.conflict) {
                return true;
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean doConnect() {
        m_xmppConnection = new XMPPTCPConnection(m_xmppConfiguration);
        PingManager.getInstanceFor(m_xmppConnection).setPingInterval(30000);
        try {
            m_xmppConnection.connect();
            ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(m_xmppConnection);
            if (!reconnectionManager.isAutomaticReconnectEnabled()) {
                reconnectionManager.enableAutomaticReconnection();
            }
            start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void addConnectionListener(ConnectionListener l) {
        m_connectionListener = l;
        m_xmppConnection.addConnectionListener(this);
    }

    public boolean login(String p_strId, String p_strPassword) {
        if (p_strId != null && !p_strId.isEmpty() && p_strPassword != null && !p_strPassword.isEmpty()) {
            try {
                m_xmppConnection.login(p_strId, p_strPassword);
                return true;
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        } else {
            return true;
        }
    }

    public boolean isAuthenticated() {
        return m_xmppConnection.isAuthenticated();
    }

    public void doDisconnect() {
        if (m_xmppConnection != null && m_xmppConnection.isConnected()) {
            isManuallyClosed = true;
            m_xmppConnection.disconnect();
        }
        this.interrupt();
    }

    @Override
    public void run() {
        StanzaFilter filter = new AndFilter(new StanzaTypeFilter(Message.class));
        PacketCollector collector = m_xmppConnection.createPacketCollector(filter);
        while (true) {
            Stanza packet = null;
            try {
                packet = collector.nextResult();
            } catch (Exception e) {
                continue;
            }
            if (packet != null && packet instanceof Message) {
                Message message = (Message) packet;

                if (message != null && message.getBody() != null) {
                    //System.out.println("Received message from "  + packet.getFrom() + " : " + (message != null ? message.getBody() : "NULL"));

                    //
                    // XMPP 메시지 처리.
                    //
                    if (message.getBody() != null) {
                        XmppPacket w_xmpppkt = XmppPacket.fromJSONString(message.getBody());
                        if (w_xmpppkt != null)
                            processXmppMessage(w_xmpppkt, message.getStanzaId());
                    }
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * 수신된 xmpp파켓 해석 & 처리.
     *
     * @param p_pkt 수신된 파켓.
     */
    private void processXmppMessage(final XmppPacket p_pkt, final String p_strMsgId) {
        //
        // 메시지 파켓 처리.
        //
        m_hUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (p_pkt != null && p_pkt.ToUser == null) { // if Group Chat
                    for (MessagingListener w_listener : m_lstMessagingListeners) {
                        if (p_pkt.SubType == XmppPacket.PacketSubType.PacketSubType_MessageText) {
                            w_listener.onNewMessageText(p_pkt, p_strMsgId, p_pkt.Content);
                        }else if (p_pkt.SubType == XmppPacket.PacketSubType.PacketSubType_MessageImage) {
                            w_listener.onNewMessageImage(p_pkt, p_strMsgId, p_pkt.Content);
                        }
                        else if (p_pkt.SubType == XmppPacket.PacketSubType.PacketSubType_Ping) {
                            w_listener.onPing(p_pkt.FromUser, p_strMsgId);
                        } else if (p_pkt.SubType == XmppPacket.PacketSubType.PacketSubType_Pong) {
                            w_listener.onPong(p_pkt.FromUser, p_strMsgId);
                        } else if (p_pkt.SubType == XmppPacket.PacketSubType.PacketSubType_MessageRequest) {
                            w_listener.onNewMessageRequest(p_pkt.FromUser, p_strMsgId);
                        }
                    }
                }
            }
        });
    }

    public String sendMessageRequest(UserInfo p_toUser) {
        XmppPacket w_pkt = new XmppPacket(XmppPacket.PacketType.PacketType_Message, XmppPacket.PacketSubType.PacketSubType_MessageRequest, m_app.getUserInfo(), p_toUser, "");

        Message w_msg = new Message();
        w_msg.setType(Message.Type.chat);
        w_msg.setTo(getJidName(p_toUser));
        w_msg.setBody(w_pkt.toJSONString());

        try {
            m_xmppConnection.sendStanza(w_msg);
            return w_msg.getStanzaId();
        } catch (SmackException.NotConnectedException e) {
            return null;
        }
    }


    public String sendMessageText(UserInfo p_toUserInfo, String p_strContent) {
        XmppPacket w_pkt = new XmppPacket(XmppPacket.PacketType.PacketType_Message, XmppPacket.PacketSubType.PacketSubType_MessageText, m_app.getUserInfo(), p_toUserInfo, p_strContent);

        Message w_msg = new Message();
        w_msg.setType(Message.Type.chat);
        w_msg.setTo(getJidName(p_toUserInfo));
        w_msg.setBody(w_pkt.toJSONString());

        try {
            m_xmppConnection.sendStanza(w_msg);
            return w_msg.getStanzaId();
        } catch (SmackException.NotConnectedException e) {
            return null;
        }
    }

    public String sendPing(UserInfo p_toUserInfo) {
        XmppPacket w_pkt = new XmppPacket(XmppPacket.PacketType.PacketType_Message, XmppPacket.PacketSubType.PacketSubType_Ping, m_app.getUserInfo(), p_toUserInfo, "");

        Message w_msg = new Message();
        w_msg.setType(Message.Type.chat);
        w_msg.setTo(getJidName(p_toUserInfo));
        w_msg.setBody(w_pkt.toJSONString());

        try {
            m_xmppConnection.sendStanza(w_msg);
            return w_msg.getStanzaId();
        } catch (SmackException.NotConnectedException e) {
            return null;
        }
    }

    public String sendPong(UserInfo p_toUserInfo) {
        XmppPacket w_pkt = new XmppPacket(XmppPacket.PacketType.PacketType_Message, XmppPacket.PacketSubType.PacketSubType_Pong, m_app.getUserInfo(), p_toUserInfo, "");

        Message w_msg = new Message();
        w_msg.setType(Message.Type.chat);
        w_msg.setTo(getJidName(p_toUserInfo));
        w_msg.setBody(w_pkt.toJSONString());

        try {
            m_xmppConnection.sendStanza(w_msg);
            return w_msg.getStanzaId();
        } catch (SmackException.NotConnectedException e) {
            return null;
        }
    }

    ////////////////////////////////////
    // 방조함수.
    ////////////////////////////////////
    public String getJidName(UserInfo p_userInfo) {
        return String.format("%s%d@%s", Const.TAG_CHAT, p_userInfo.user_no, m_xmppConfiguration.getServiceName());
    }


    public static String getXMPPUserID(UserInfo p_userInfo) {
        return String.format("%s%d", Const.TAG_CHAT, p_userInfo.user_no);
    }

    public static String getUserIDFromXMPPID(String p_xmppID) {
        return p_xmppID.substring(Const.TAG_CHAT.length() - 1, p_xmppID.length());
    }

    private void showToast(final String p_strMessage) {
        m_hUIHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(m_app, p_strMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void connected(XMPPConnection connection) {
        if (m_connectionListener != null) {
            m_connectionListener.connected(connection);
        }
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        if (m_connectionListener != null) {
            m_connectionListener.connected(connection);
        }
    }

    @Override
    public void connectionClosed() {
        if (!isManuallyClosed && m_connectionListener != null) {
            m_connectionListener.connectionClosed();
        }
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        if (m_connectionListener != null) {
            m_connectionListener.connectionClosedOnError(e);
        }
    }

    @Override
    public void reconnectionSuccessful() {
        if (m_connectionListener != null) {
            m_connectionListener.reconnectionSuccessful();
        }
    }

    @Override
    public void reconnectingIn(int seconds) {
        if (m_connectionListener != null) {
            m_connectionListener.reconnectingIn(seconds);
        }
    }

    @Override
    public void reconnectionFailed(Exception e) {
        if (m_connectionListener != null) {
            m_connectionListener.reconnectionFailed(e);
        }
    }
}
