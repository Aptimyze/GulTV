package com.yj.wangjatv.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.yj.wangjatv.Const;
import com.yj.wangjatv.R;

import com.red5pro.streaming.R5Connection;
import com.red5pro.streaming.R5Stream;
import com.red5pro.streaming.R5StreamProtocol;
import com.red5pro.streaming.config.R5Configuration;
import com.red5pro.streaming.event.R5ConnectionEvent;
import com.red5pro.streaming.event.R5ConnectionListener;
import com.red5pro.streaming.source.R5Camera;
import com.red5pro.streaming.source.R5Microphone;
import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.adapter.ChattingAdapter;
import com.yj.wangjatv.dialog.ChattingDialog;
import com.yj.wangjatv.dialog.GetPhotoDialog;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.model.ChatMessage;
import com.yj.wangjatv.model.UserInfo;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.utils.SelectPhotoManager;
import com.yj.wangjatv.xmpp.EnterChatRoomTask;
import com.yj.wangjatv.xmpp.MessagingListener;
import com.yj.wangjatv.xmpp.XmppPacket;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 6/2/2016.
 */
public class BroadcastActivity extends BaseActivity implements View.OnClickListener, SurfaceHolder.Callback, MessagingListener {

    // Red5Pro parameters
    private int cameraSelection = 0;
    private Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    private List<Camera.Size> sizes = new ArrayList<Camera.Size>();
    public static Camera.Size selected_size = null;
    public static String selected_item = null;
    public static int preferedResolution = 0;
    private R5Camera r5Cam;
    private R5Microphone r5Mic;
    private SurfaceView surfaceForCamera;
    protected Camera camera;
    protected boolean isPublishing = false;

    R5Stream stream;

    // data
    private Broadcast m_pBroadcast = null;

    // UI
    private View m_vContent = null;

    // for chatting
    private ArrayList<ChatMessage>  m_arrChatList = new ArrayList<>();
    private ChattingAdapter m_adpChatting = null;
    private ChattingDialog m_dlgChatting = null;
    private Handler m_hUIThread = new Handler();
    private boolean m_isSent = false;
    private int     m_nSelectedIdx = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);

        m_pBroadcast = (Broadcast)getIntent().getSerializableExtra(KEY_INTENT_BROADCAST);
        if(m_pBroadcast == null) {
            finish();
            return;
        }

        m_pBroadcast.user_no = m_app.getUserInfo().user_no;

        // init chatting
        m_adpChatting = new ChattingAdapter(this, m_arrChatList, new ChattingAdapter.IListItemClickListener() {
            @Override
            public void OnClickItem(int idx) {
                m_dlgChatting.dismiss();
            }

            @Override
            public void OnClickForce(int idx) {
                m_nSelectedIdx = idx;
                updateBroadcastUserFlag(Broadcast.EFFECT_USER_FORCE_EXIT);
            }
        }, ChattingAdapter.CHATTING_LIST_TYPE_BROADCAST);

        m_dlgChatting = new ChattingDialog(this, m_adpChatting, new ChattingDialog.ChattingDialogListner(){
            @Override
            public void onDismiss() {
                m_adpChatting.notifyDataSetChanged();
            }

            @Override
            public void onSendText(String text, ListView listView) {
                BroadcastActivity.this.sendText(text, 0);
                if(listView != null) {
                    listView.smoothScrollToPosition(m_adpChatting.getCount() - 1);
                    listView.setSelection(m_adpChatting.getCount() - 1);
                }
            }
        });

        m_vContent.setKeepScreenOn(true);

        initXmpp();

        showCamera();
    }

    @Override
    public void init() {
        super.init();

        m_vContent = findViewById(android.R.id.content);

        findViewById(R.id.ib_back).setOnClickListener(this);
        findViewById(R.id.btn_broadcast).setOnClickListener(this);
        findViewById(R.id.btn_camera).setOnClickListener(this);
        findViewById(R.id.btn_chatting).setOnClickListener(this);
        findViewById(R.id.btn_event).setOnClickListener(this);
    }

    /*****************************************************************
     * Override Functions
     *******************************************************************/

    @Override
    public void onBackPressed() {
        if(isPublishing == true) {
            stopPublishing();
            stopCamera();
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        stopPublishing();
        stopCamera();
        finalizeXmpp();
        super.onDestroy();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_back:
                finishActivity();
                break;
            case R.id.btn_broadcast: {
                Button btnBroadcast = (Button)findViewById(R.id.btn_broadcast);
                if (isPublishing) {
                    btnBroadcast.setText(getResources().getString(R.string.tab_broadcast));
                    stopPublishing();
                } else {
                    btnBroadcast.setText(getResources().getString(R.string.pause_broadcast));
                    startPublishing();
                }
            }
            break;
            case R.id.btn_camera:
                toggleCamera();
                break;
            case R.id.btn_chatting:
                m_dlgChatting.show();
                break;
            case R.id.btn_event:

                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        try{
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }
        catch(Exception e){
            e.printStackTrace();
        };

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {}

    /*****************************************************************
     * API Functions
     *******************************************************************/



    private void startendStream(boolean flag) {
        final APIProvider server = new APIProvider();
        int nFlag = 0;
        if(flag == false) {
            nFlag = 0; //end
        }
        else {
            nFlag = 1; // start
        }
        //APIProvider.showWaitingDlg(this);
        final boolean finalFlag = flag;
        server.starendBroadcast(this, m_pBroadcast.no, nFlag, new Callback<BaseModel>() {
            @Override
            public void onResponse(Response<BaseModel> response) {
                BaseModel model = response.body();

                //APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        if (finalFlag == true) {
                            CommonUtil.showCenterToast(BroadcastActivity.this, R.string.start_broadcast, Toast.LENGTH_SHORT);
                        } else {
                            CommonUtil.showCenterToast(BroadcastActivity.this, R.string.end_broadcast, Toast.LENGTH_SHORT);
                        }
                    } else {
                        CommonUtil.showCenterToast(BroadcastActivity.this, R.string.failed_broadcast, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, BroadcastActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                //APIProvider.hideProgress();
                server.processServerFailure(t, BroadcastActivity.this);
            }
        });
    }


    private void updateBroadcastUserFlag(final int flag) {
        if(m_nSelectedIdx < 0 || m_nSelectedIdx >= m_adpChatting.getCount() ){
            return;
        }

        final ChatMessage msg = m_adpChatting.getItem(m_nSelectedIdx);

        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);

        server.updateBroadcastUserFlag(this, m_pBroadcast.no, msg.user_no, flag, new Callback<BaseModel>() {
            @Override
            public void onResponse(Response<BaseModel> response) {
                BaseModel model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (flag == Broadcast.EFFECT_USER_FORCE_EXIT) {
                        String strMsg = String.format("%s님을 퇴장시켰습니다.", msg.user_name);
                        sendText(strMsg, flag);
                    }
                } else {
                    server.processErrorBody(response, BroadcastActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, BroadcastActivity.this);
            }
        });
    }

    private void deleteBroadCast() {
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);
        server.deleteBroadcast(this, m_pBroadcast.no, new Callback<Broadcast>() {
            @Override
            public void onResponse(Response<Broadcast> response) {
                Broadcast model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        finish();
                    } else {
                        CommonUtil.showCenterToast(BroadcastActivity.this, R.string.invalid_nickname, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, BroadcastActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, BroadcastActivity.this);
            }
        });
    }

    /*****************************************************************
     * main Functions
     *******************************************************************/

    private void toggleCamera() {
        cameraSelection = (cameraSelection + 1) % 2;
        try {
            Camera.getCameraInfo(cameraSelection, cameraInfo);
            cameraSelection = cameraInfo.facing;
        }
        catch(Exception e) {
            // can't find camera at that index, set default
            cameraSelection = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        stopCamera();
        showCamera();
    }

    private void showCamera() {
        if(camera == null) {
            camera = Camera.open(cameraSelection);
            camera.setDisplayOrientation(90);
            sizes=camera.getParameters().getSupportedPreviewSizes();
            SurfaceView sufi = (SurfaceView) findViewById(R.id.surfaceView);
            if(sufi.getHolder().isCreating()) {
                sufi.getHolder().addCallback(this);
            }
            else {
                sufi.getHolder().addCallback(this);
                this.surfaceCreated(sufi.getHolder());
            }
        }
    }

    private void stopCamera() {
        if(camera != null) {
            SurfaceView sufi = (SurfaceView) findViewById(R.id.surfaceView);
            sufi.getHolder().removeCallback(this);
            sizes.clear();

            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    //called by record button
    private void startPublishing() {
        if(!isPublishing) {

            Handler mHand = new Handler();

            int port = m_app.get_int_SharedPreferences(KEY_PREF_LIVE_SERVER_PORT);
            String ip = m_app.get_string_SharedPreferences(KEY_PREF_LIVE_SERVER_IP);
            String app_name = m_app.get_string_SharedPreferences(KEY_PREF_LIVE_APP_NAME);
            int bitrate = m_app.get_int_SharedPreferences(KEY_PREF_LIVE_BITRATE);

            stream = new R5Stream(new R5Connection(new R5Configuration(R5StreamProtocol.RTSP, ip,  port, app_name, 1.0f)));
            stream.setLogLevel(R5Stream.LOG_LEVEL_DEBUG);

            stream.connection.addListener(new R5ConnectionListener() {
                @Override
                public void onConnectionEvent(R5ConnectionEvent event) {
                    Log.d("publish", "connection event code " + event.value() + "\n");
                    switch(event.value()){
                        case 0://open

                            break;
                        case 1://close

                            break;
                        case 2://error
                            deleteBroadCast();
                            break;

                    }
                }
            });

            stream.setListener(new R5ConnectionListener() {
                @Override
                public void onConnectionEvent(R5ConnectionEvent event) {
                    switch (event) {
                        case CONNECTED:
                            break;
                        case DISCONNECTED:
                            break;
                        case START_STREAMING:
                            startendStream(true);
                            break;
                        case STOP_STREAMING:
                            break;
                        case CLOSE:
                            startendStream(false);
                            break;
                        case TIMEOUT:
                            break;
                        case ERROR:
                            break;
                    }
                }
            });

            camera.stopPreview();

            //assign the surface to show the camera output
            this.surfaceForCamera = (SurfaceView) findViewById(R.id.surfaceView);
            stream.setView((SurfaceView) findViewById(R.id.surfaceView));

            int width = 320;
            int height = 240;

            // select video quality
            switch (m_pBroadcast.video_quality) {
                case Broadcast.VIDEO_QUALITY_HIGH:
                    width = 640;
                    height = 480;
                break;
                case Broadcast.VIDEO_QUALITY_NORMAL:
                    width = 320;
                    height = 240;
                    break;
                case Broadcast.VIDEO_QUALITY_LOW:
                    width = 160;
                    height = 120;
                    break;
            }

            //add the camera for streaming
            if(selected_item != null) {
                Log.d("publisher","selected_item "+selected_item);
                String bits[] = selected_item.split("x");
                int pW= Integer.valueOf(bits[0]);
                int pH=  Integer.valueOf(bits[1]);
                if((pW/2) %16 !=0){
                    pW=width;
                    pH=height;
                }
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewSize(pW, pH);
                camera.setParameters(parameters);
                r5Cam = new R5Camera(camera,pW,pH);

                r5Cam.setBitrate(bitrate);
            }
            else {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewSize(width, height);

                camera.setParameters(parameters);
                r5Cam = new R5Camera(camera,width,height);
                r5Cam.setBitrate(bitrate);
            }

            if(cameraSelection==1) {
                r5Cam.setOrientation(270);
            }
            else {
                r5Cam.setOrientation(90);
            }
            r5Mic = new R5Microphone();

            if(true) {
                stream.attachCamera(r5Cam);
            }

            if(true) {
                stream.attachMic(r5Mic);
            }


            isPublishing = true;

            String live_name = m_app.getLiveName(m_pBroadcast.no, m_pBroadcast.reg_time);
            stream.publish(live_name, R5Stream.RecordType.Record);
            camera.startPreview();
        }
    }

    private void stopPublishing() {
        if(stream!=null) {
            stream.stop();
        }
        isPublishing = false;
    }

    private void finishActivity() {
        finish();
    }

    /*****************************************************************
     * Chatting Functions
     *******************************************************************/

    /**
     * Init xmpp
     */
    private void initXmpp() {
        EnterChatRoomTask thread = new EnterChatRoomTask(this, m_app);
        thread.execute(m_pBroadcast.no);

        m_app.getXmppEndPoint().addMessagingListener(this);
    }

    /**
     * Finalize xmpp
     */
    private void finalizeXmpp() {
        //
        // 채팅방의 다른 유저들에게 내상태(채딩끝났음)를 통지.
        //
        m_app.getXmppEndPoint().notifyChatEnded();

        m_app.getXmppEndPoint().leaveRoom();

        //
        // 유저의 상태를 "채팅대기"로 업데이트.
        //
//        m_app.getNet().setBusy(this, m_app.getMe().UserId, 0, null);
        m_app.getXmppEndPoint().removeMessagingListener(this);
    }

    @Override
    public void onNewMessageText(XmppPacket packet, String p_strId, String p_strContent) {
        if (m_isSent == true && packet != null && packet.FromUser != null && packet.FromUser.user_no == m_app.getUserInfo().user_no) {
            // sent successfully
            m_isSent = false;
            return;
        }
        if(packet.FromUser  == null) {
            return;
        }

        UserInfo fromUser = packet.FromUser;
        ChatMessage msg = new ChatMessage();
        msg.user_no = packet.FromUser.user_no;
        msg.content = p_strContent;
        msg.user_grade =fromUser.user_grade;
        msg.user_name =fromUser.user_name;
        msg.forced_exit = packet.is_forced_exit;
        msg.is_bj = packet.is_bj;

        m_adpChatting.add(msg);
        m_adpChatting.notifyDataSetChanged();
    }

    @Override
    public void onNewMessageImage(XmppPacket packet, String p_strId, String p_strContent) {
    }

    @Override
    public void onNewMessageRequest(UserInfo p_fromUserInfo, String p_strId) {
        Log.d("onNewMessageRequest", "peer id =" + p_strId);
    }

    @Override
    public void onPing(UserInfo p_fromUserInfo, String p_strId) {
        Log.d("onNewMessageRequest", "peer id =" + p_strId);
    }

    @Override
    public void onPong(UserInfo p_fromUserInfo, String p_strId) {
        Log.d("onNewMessageRequest", "peer id =" + p_strId);
    }

    private void sendText(String text, int flag) {
        String strMsg = text;

        XmppPacket pkt = null;
        pkt = new XmppPacket(XmppPacket.PacketType.PacketType_Message, XmppPacket.PacketSubType.PacketSubType_MessageText, m_app.getUserInfo(), strMsg);

        if(flag == Broadcast.EFFECT_USER_FORCE_EXIT) {
            pkt.is_forced_exit = 1;
            pkt.exit_user_no = m_adpChatting.getItem(m_nSelectedIdx).user_no;
        }
        else {
            pkt.is_forced_exit = 0;
            pkt.exit_user_no = 0;
        }

        pkt.is_bj = 1;

        org.jivesoftware.smack.packet.Message msg = new org.jivesoftware.smack.packet.Message();
        msg.setType(org.jivesoftware.smack.packet.Message.Type.groupchat);
        msg.setBody(pkt.toJSONString());

        m_app.getXmppEndPoint().sendMessage(msg);

        ChatMessage chatMessageText = new ChatMessage();
        chatMessageText.user_no = m_app.getUserInfo().user_no;
        chatMessageText.content = strMsg;
        chatMessageText.user_name =  m_app.getUserInfo().user_name;
        chatMessageText.user_grade =  m_app.getUserInfo().user_grade;
        chatMessageText.forced_exit = pkt.is_forced_exit;
        chatMessageText.is_bj = pkt.is_bj;

        m_adpChatting.add(chatMessageText);
        m_adpChatting.notifyDataSetChanged();

        m_isSent = true;

        if(m_dlgChatting  != null) {
            m_dlgChatting.list_view.smoothScrollToPosition(m_adpChatting.getCount() - 1);
            m_dlgChatting.list_view.setSelection(m_adpChatting.getCount() - 1);
        }
    }
}