package com.yj.wangjatv.xmpp;

import com.yj.wangjatv.model.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by toltori on 2014. 8. 14..
 */
public class XmppPacket {

    public static enum PacketType {
        PacketType_Message,
    }

    public static enum PacketSubType {
        PacketSubType_MessageText,
        PacketSubType_MessageImage,
        PacketSubType_MessageRequest,
        PacketSubType_Ping,
        PacketSubType_Pong,
    }

    public PacketType Type;
    public PacketSubType SubType;
    public UserInfo FromUser;
    public UserInfo ToUser;
    public String Content;
    public int is_forced_exit = 0; // 0, 1
    public int exit_user_no = 0;
    public int is_bj = 0; // 0, 1

    public XmppPacket(PacketType p_packetType, PacketSubType p_packetSubType, UserInfo p_fromUser, UserInfo p_toUser) {
        Type = p_packetType;
        SubType = p_packetSubType;
        FromUser = p_fromUser;
        ToUser = p_toUser;
        Content = "";
    }

    public XmppPacket(PacketType p_packetType, PacketSubType p_packetSubType, UserInfo p_fromUser, UserInfo p_toUser, String p_strContent) {
        Type = p_packetType;
        SubType = p_packetSubType;
        FromUser = p_fromUser;
        ToUser = p_toUser;
        Content = p_strContent;
    }

    public XmppPacket(PacketType p_packetType, PacketSubType p_packetSubType, UserInfo p_fromUser, String p_strContent) {
        Type = p_packetType;
        SubType = p_packetSubType;
        FromUser = p_fromUser;
        Content = p_strContent;
    }

    private JSONObject toJSONObject() {
        JSONObject w_json = new JSONObject();

        try {
            w_json.put("type", new Integer(Type.ordinal()));
            w_json.put("subtype", new Integer(SubType.ordinal()));
            w_json.put("from_user", FromUser.toJSONObject());
            if (ToUser != null) {
                w_json.put("to_user", ToUser.toJSONObject());
            }
            w_json.put("content", Content);
            w_json.put("is_forced_exit", is_forced_exit);
            w_json.put("exit_user_no", exit_user_no);
            w_json.put("is_bj", is_bj);
        } catch (JSONException e) {
            return null;
        }

        return w_json;
    }

    public String toJSONString() {
        JSONObject w_json = toJSONObject();
        if (w_json == null) {
            return "";
        }

        return w_json.toString();
    }

    public static XmppPacket fromJSONString(String p_strJson) {
        XmppPacket w_pkt = null;
        try {
            JSONObject w_json = new JSONObject(p_strJson);
            if (w_json == null)
                return null;

            int w_nType = w_json.getInt("type");
            int w_nSubType = w_json.getInt("subtype");
            JSONObject w_jsonFromUser = w_json.getJSONObject("from_user");
            UserInfo w_fromUser = w_jsonFromUser == null ? null : UserInfo.fromJSONObject(w_jsonFromUser);

            UserInfo w_toUser = null;
            if (w_json.has("to_user")) {
                JSONObject w_jsonToUser = w_json.getJSONObject("to_user");
                w_toUser = w_jsonToUser == null ? null : UserInfo.fromJSONObject(w_jsonToUser);
            }

            String w_strContent = w_json.getString("content");

            w_pkt = new XmppPacket(PacketType.values()[w_nType], PacketSubType.values()[w_nSubType], w_fromUser, w_toUser, w_strContent);
            w_pkt.is_forced_exit = w_json.getInt("is_forced_exit");
            w_pkt.exit_user_no = w_json.getInt("exit_user_no");
            w_pkt.is_bj = w_json.getInt("is_bj");

        } catch (JSONException e) {
            return null;
        }

        return w_pkt;
    }
}
