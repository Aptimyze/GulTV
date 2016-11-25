package com.yj.wangjatv.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cunoraz.gifview.library.GifView;
import com.red5pro.streaming.media.R5AudioController;
import com.yj.wangjatv.Const;
import com.yj.wangjatv.R;
import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.adapter.BroadcastAdapter;
import com.yj.wangjatv.adapter.ChattingAdapter;
import com.yj.wangjatv.dialog.ChattingDialog;
import com.yj.wangjatv.dialog.HeartDialog;
import com.yj.wangjatv.dialog.InputPasswordDialog;
import com.yj.wangjatv.dialog.OneMsgOneBtnDialog;
import com.yj.wangjatv.dialog.OneMsgTwoBtnDialog;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.model.BroadcastList;
import com.yj.wangjatv.model.BroadcastUserEffect;
import com.yj.wangjatv.model.ChatMessage;
import com.yj.wangjatv.model.HeartHistory;
import com.yj.wangjatv.model.UserInfo;
import com.yj.wangjatv.model.ViewerCnt;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.utils.StringFilter;
import com.yj.wangjatv.widget.AnyTextView;

import com.red5pro.streaming.R5Connection;
import com.red5pro.streaming.R5Stream;
import com.red5pro.streaming.R5StreamProtocol;
import com.red5pro.streaming.config.R5Configuration;
import com.red5pro.streaming.event.R5ConnectionEvent;
import com.red5pro.streaming.event.R5ConnectionListener;
import com.red5pro.streaming.view.R5VideoView;
import com.yj.wangjatv.xmpp.EnterChatRoomTask;
import com.yj.wangjatv.xmpp.MessagingListener;
import com.yj.wangjatv.xmpp.XmppPacket;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 5/25/2016.
 */
public class LiveActivity  extends BaseActivity implements  AbsListView.OnScrollListener, View.OnClickListener, MessagingListener {

    public interface LiveActivityListener {
        public void finishBeforeActivity();
    }

    public final static int PAGE_ITEM_CNT = 20;
    public final static int CHECK_DELAY_TIME = 3000; // 3s

    private TextView[] m_tvTab = new TextView[3];

    private Broadcast m_pBroadcast = null;

    private R5VideoView m_vVideoView = null;
    private boolean m_isOfflined = false;

    // for red5
    R5Stream stream;

    public boolean isStreaming = false;

    private boolean m_isAutoOK = false;
    private ImageButton m_ibRight = null;

    private AnyTextView tv_live_title = null;
    private View ll_menu = null;

    private int m_nSelectedTab = 0;
    private boolean m_isLoadingEnd = true;
    private ListView m_listView = null;
    private boolean m_isLockListView = false;

    // for list
    private ArrayList<Broadcast>    m_arrBroadcast = new ArrayList<>();
    private BroadcastAdapter        m_adpBroadcst = null;
    private ArrayList<Broadcast>    m_arrFavBroadcast = new ArrayList<>();
    private BroadcastAdapter        m_adpFavBroadcst = null;

    // for ui
    private ImageView iv_effect;

    // for chatting
    private ArrayList<ChatMessage>  m_arrChatList = new ArrayList<>();
    private ChattingAdapter         m_adpChatting = null;
    private ChattingDialog          m_dlgChatting = null;
    private Handler m_hUIThread = new Handler();

    Handler m_hndCntCheckHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                checkViewerCnt();
                sendEmptyMessageDelayed(0, CHECK_DELAY_TIME);
            }
        }
    };
    /*****************************************************************
     * Override Functions
     *******************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        m_pBroadcast = (Broadcast)getIntent().getSerializableExtra(KEY_INTENT_LIVE);

        if(m_pBroadcast == null) {
            CommonUtil.showCenterToast(this, R.string.network_connect_error, Toast.LENGTH_SHORT);
            return;
        }

        // init adapters.

        m_adpBroadcst = new BroadcastAdapter(this, m_arrBroadcast,BroadcastAdapter.BROADCAST_LIVE_1, new BroadcastAdapter.BroadcastAdapterListner() {
            @Override
            public void onClickBroadcast(Broadcast broadcast) {

                if(broadcast.no == m_pBroadcast.no) {
                    CommonUtil.showCenterToast(LiveActivity.this, R.string.msg_current_live, Toast.LENGTH_SHORT);
                    return;
                }

                m_app.liveListner = new LiveActivityListener() {
                    @Override
                    public void finishBeforeActivity() {
                        finishActivity();
                        m_app.liveListner = null;
                    }
                };
                m_app.playBroadcast(MainActivity.getInstance(), broadcast);
            }
        });

        m_adpFavBroadcst = new BroadcastAdapter(this, m_arrFavBroadcast, BroadcastAdapter.BROADCAST_FAV_LIST, new BroadcastAdapter.BroadcastAdapterListner() {
            @Override
            public void onClickBroadcast(Broadcast broadcast) {

                if(broadcast.no == m_pBroadcast.no) {
                    CommonUtil.showCenterToast(LiveActivity.this, R.string.msg_current_live, Toast.LENGTH_SHORT);
                    return;
                }

                m_app.liveListner = new LiveActivityListener() {
                    @Override
                    public void finishBeforeActivity() {
                        finishActivity();
                        m_app.liveListner = null;
                    }
                };
                m_app.playBroadcast(MainActivity.getInstance(), broadcast);
            }
        });

        // init chatting
        m_adpChatting = new ChattingAdapter(this, m_arrChatList, new ChattingAdapter.IListItemClickListener() {
            @Override
            public void OnClickItem(int idx) {
                m_dlgChatting.dismiss();
            }

            @Override
            public void OnClickForce(int idx) {

            }
        }, ChattingAdapter.CHATTING_LIST_TYPE_LIVE);

        m_dlgChatting = new ChattingDialog(this, m_adpChatting, new ChattingDialog.ChattingDialogListner(){
            @Override
            public void onDismiss() {
                m_adpChatting.m_nListType = ChattingAdapter.CHATTING_LIST_TYPE_LIVE;
                m_adpChatting.notifyDataSetChanged();
            }

            @Override
            public void onSendText(String text, ListView listView) {
                LiveActivity.this.sendText(text);
                listView.smoothScrollToPosition(m_adpChatting.getCount() - 1);
                listView.setSelection(m_adpChatting.getCount() - 1);
            }
        });

        chkBroadCastUserEffect();
    }

    private void  initLive() {
        initXmpp();

        // show data
        AnyTextView textView = (AnyTextView)findViewById(R.id.tv_title);
        textView.setText(String.format("[%s]%s", m_pBroadcast.category_name, m_pBroadcast.title));

        m_isAutoOK = false;
        toggleAutoOK();

        textView = (AnyTextView)findViewById(R.id.tv_user_name);
        textView.setText(m_pBroadcast.user_name);
        textView = (AnyTextView)findViewById(R.id.tv_member_cnt);
        textView.setText(String.format("%d/%d", m_pBroadcast.viewer_cnt, m_pBroadcast.max_viewer_cnt));

        if(m_pBroadcast.effect == Broadcast.EFFECT_BLIND) {
            iv_effect.setVisibility(View.VISIBLE);
        }
        else {
            iv_effect.setVisibility(View.GONE);
        }

        if(m_pBroadcast.effect == Broadcast.EFFECT_CAPTURE_DEFEND) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        findViewById(R.id.rl_slider).setVisibility(View.GONE);

        // init first tap
        onTabSelected(0);

        // 이력이 있으면 하트전송없이 보여준다.
        boolean isHasHistory = getIntent().getBooleanExtra(KEY_INTENT_HAS_HISTORY, false);
        if(isHasHistory == false &&  m_pBroadcast.heart_cnt != 0) {
            if((m_pBroadcast.type == Broadcast.LIVE_TYPE_FAN || m_pBroadcast.type == Broadcast.LIVE_TYPE_MONEY)) {
                if(m_pBroadcast.heart_cnt <= m_app.getUserInfo().user_heart_cnt) {
                    sendHeart(m_pBroadcast.heart_cnt);
                    return;
                }
                else {
                    CommonUtil.showCenterToast(this, R.string.dialog_inform_heart, Toast.LENGTH_SHORT);
                    finishActivity();
                    return;
                }
            }
        }

        // start live
        startStream();
    }

    @Override
    public void init() {
        super.init();

        initMenu();

        m_tvTab[0] = (TextView) findViewById(R.id.tv_tap_chatting);
        m_tvTab[1] = (TextView) findViewById(R.id.tv_tap_broadcast);
        m_tvTab[2] = (TextView) findViewById(R.id.tv_tap_fav);

        findViewById(R.id.btn_tab_chatting).setOnClickListener(this);
        findViewById(R.id.btn_tab_broadcast).setOnClickListener(this);
        findViewById(R.id.btn_tab_fav).setOnClickListener(this);
        findViewById(R.id.btn_chatting).setOnClickListener(this);
        findViewById(R.id.btn_heart).setOnClickListener(this);
        findViewById(R.id.btn_like).setOnClickListener(this);
        findViewById(R.id.btn_fav).setOnClickListener(this);

        m_vVideoView = (R5VideoView)findViewById(R.id.v_video);
        m_vVideoView.setOnClickListener(this);

        ll_menu = findViewById(R.id.ll_menu);
        tv_live_title = (AnyTextView)findViewById(R.id.tv_live_title);
        tv_live_title.setVisibility(View.GONE);
        m_listView = (ListView)findViewById(R.id.list_view);
        m_listView.setOnScrollListener(this);
        iv_effect = (ImageView)findViewById(R.id.iv_effect);
    }

    private void initMenu() {
        findViewById(R.id.iv_title).setVisibility(View.GONE);
        AnyTextView textView = (AnyTextView)findViewById(R.id.tv_title);
        textView.setVisibility(View.VISIBLE);
        textView.setTextColor(getResources().getColor(R.color.gray_323232));
        textView.setText(getResources().getString(R.string.tab_live));
        findViewById(R.id.rl_right_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.ib_refresh).setOnClickListener(this);
        findViewById(R.id.ib_back).setOnClickListener(this);

        m_ibRight = (ImageButton)findViewById(R.id.ib_refresh);
    }

    public void finishActivity() {
        inoutBroadCast(1);
    }

    @Override
    public void onBackPressed() {
        finishActivity();

        //super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.ll_center).setVisibility(View.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            findViewById(R.id.ll_center).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_tab_chatting:
                onTabSelected(0);
                break;
            case R.id.btn_tab_broadcast:
                onTabSelected(1);
                break;
            case R.id.btn_tab_fav:
                onTabSelected(2);
                break;
            case R.id.ib_refresh:
                toggleAutoOK();
                break;
            case R.id.ib_back:
                onBackPressed();
                break;
            case R.id.v_video: {
                if(ll_menu.getVisibility() == View.VISIBLE) {
                    ll_menu.setVisibility(View.GONE);
                    tv_live_title.setVisibility(View.GONE);
                }
                else {
                    ll_menu.setVisibility(View.VISIBLE);
                    tv_live_title.setVisibility(View.GONE);
                }
            }
                break;
            case R.id.btn_chatting: {

                m_adpChatting.m_nListType = ChattingAdapter.CHATTING_LIST_TYPE_DIALOG;
                m_dlgChatting.show();
            }
                break;
            case R.id.btn_heart: {
                final HeartDialog dlg = new HeartDialog(this, m_app.getUserInfo().user_heart_cnt, null);
                dlg.setListner( new HeartDialog.HeartDialogListner(){
                    @Override
                    public void onClickNo() {

                    }

                    @Override
                    public void onClickHeart(int cnt) {
                        if(cnt != 0 &&  cnt > m_app.getUserInfo().user_heart_cnt) {
                            startPurchaseActivity();
                            return;
                        }
                        if(cnt == 0) {
                            return;
                        }

                        sendHeart(cnt);
                        dlg.dismiss();
                    }

                    @Override
                    public void onClickPurchase() {
                        startPurchaseActivity();
                    }
                });

                dlg.show();
            }
                break;
            case R.id.btn_like:
                likeBroadCast();
                break;
            case R.id.btn_fav:
                fanBroadCast();
                break;
        }
    }

    /**
     * When the tab was selected
     */
    private void onTabSelected(int index) {
        m_nSelectedTab = index;

        if (m_tvTab[index].isSelected())
            return;

        for (int i = 0; i < m_tvTab.length; i++) {
            m_tvTab[i].setSelected(false);
            if (index == i) {
                m_tvTab[index].setSelected(true);
                m_tvTab[index].setTextColor(getResources().getColor(R.color.colorAccent));
            } else {
                m_tvTab[i].setSelected(false);
                m_tvTab[i].setTextColor(getResources().getColor(R.color.gray_646565));
            }
        }

        switch (index) {
            case 0:
                m_listView.setAdapter(m_adpChatting);
                m_adpChatting.notifyDataSetChanged();
                break;
            case 1:
                m_listView.setAdapter(m_adpBroadcst);
                m_adpBroadcst.notifyDataSetChanged();
                break;
            case 2:
                m_listView.setAdapter(m_adpFavBroadcst);
                m_adpFavBroadcst.notifyDataSetChanged();
                break;
        }

        refresh();
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView arg0, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        int count = totalItemCount - visibleItemCount;

        if (m_isLoadingEnd)
            return;

        if (firstVisibleItem >= count && totalItemCount != 0 && m_isLockListView == false) {
            switch (m_nSelectedTab) {
                case 0:

                    break;
                case 1:
                    getBroadcastList(totalItemCount);
                    break;
                case 2:
                    getFanBroadcastList(totalItemCount);
                    break;
            }
        }
    }

    /*****************************************************************
     * Main Functions
     *******************************************************************/

    private void toggleAutoOK() {
        if(m_isAutoOK) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            m_ibRight.setImageResource(R.drawable.bg_auto_ok);
            m_isAutoOK = false;
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            m_ibRight.setImageResource(R.drawable.bg_auto_lock);
            m_isAutoOK = true;
        }
    }

    private void toggleStream() {
        if(isStreaming) {
            stopStream();
            muteSound(false);
        }
        else {
            startStream();
        }
    }

    private void setStreaming(boolean ok) {
        isStreaming = ok;
    }

    private void startStream() {

        //grab the main view where our video object resides
        View v = this.findViewById(android.R.id.content);

        v.setKeepScreenOn(true);

        int port = m_app.get_int_SharedPreferences(KEY_PREF_LIVE_SERVER_PORT);
        String ip = m_app.get_string_SharedPreferences(KEY_PREF_LIVE_SERVER_IP);
        String app_name = m_app.get_string_SharedPreferences(KEY_PREF_LIVE_APP_NAME);

        //setup the stream with the user config settings
        stream = new R5Stream(new R5Connection(new R5Configuration(R5StreamProtocol.RTSP, ip, port, app_name, 2.0f)));

        //set log level to be informative
        stream.setLogLevel(R5Stream.LOG_LEVEL_INFO);

        //set up our listener
        stream.setListener(new R5ConnectionListener() {
            @Override
            public void onConnectionEvent(R5ConnectionEvent r5event) {
                //this is getting called from the network thread, so handle appropriately
                final R5ConnectionEvent event = r5event;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Context context = getApplicationContext();
                        CharSequence text = event.message;
                        int duration = Toast.LENGTH_SHORT;

                        if(event == R5ConnectionEvent.START_STREAMING) {
                            m_hndCntCheckHandler.sendEmptyMessageDelayed(0, CHECK_DELAY_TIME);
                        }
                        else if(event == R5ConnectionEvent.NET_STATUS) {
                            if(event.message != null) {
                                if( event.message.equals("NetStream.Play.UnpublishNotify") == true) {
                                    CommonUtil.showCenterToast(context, R.string.offlined_live, Toast.LENGTH_SHORT);
                                    m_isOfflined = true;
                                }
                                else {
                                    if(m_isOfflined == true) {
                                        CommonUtil.showCenterToast(context, R.string.reopened_live, Toast.LENGTH_SHORT);
                                    }
                                }
                            }
                        }
                        else if(event == R5ConnectionEvent.ERROR) {
                            CommonUtil.showCenterToast(context, R.string.no_live_playing, Toast.LENGTH_SHORT);
                        }
                        else if(event == R5ConnectionEvent.CLOSE){
                            m_hndCntCheckHandler.sendEmptyMessageDelayed(1, 0);
                            CommonUtil.showCenterToast(context, R.string.complete_live, Toast.LENGTH_SHORT);

                            onBackPressed();
                        }
                    }
                });
            }
        });

        //attach the stream
        m_vVideoView.attachStream(stream);

        String live_name = m_app.getLiveName(m_pBroadcast.no, m_pBroadcast.reg_time);
        //start the stream
        stream.play(live_name);
        //update the state for the toggle button
        setStreaming(true);

        if(m_pBroadcast.effect == Broadcast.EFFECT_BLIND) {
            //stream.audioController.StopController();
            muteSound(true);
        }
    }

    private void stopStream() {

        if(stream != null) {
            View v = this.findViewById(android.R.id.content);
            m_vVideoView.attachStream(null);
            stream.stop();

            stream = null;
        }
        setStreaming(false);
    }


    private void muteSound(boolean isMute) {
        // your click actions go here
        Log.i("onToggleClicked", "ToggleClick Event Started");
        //an AudioManager object, to change the volume settings
        AudioManager amanager;
        amanager = (AudioManager)getSystemService(AUDIO_SERVICE);

        // Is the toggle on?
        boolean on = isMute;

        if (on) {
            Log.i("onToggleIsChecked", "ToggleClick Is On");
            //turn ringer silent
            amanager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            Log.i("RINGER_MODE_SILENT", "Set to true");

            //turn off sound, disable notifications
            amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
            Log.i("STREAM_SYSTEM", "Set to true");
            //notifications
            amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            Log.i("STREAM_NOTIFICATION", "Set to true");
            //alarm
            amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
            Log.i("STREAM_ALARM", "Set to true");
            //ringer
            amanager.setStreamMute(AudioManager.STREAM_RING, true);
            Log.i("STREAM_RING", "Set to true");
            //media
            amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            Log.i("STREAM_MUSIC", "Set to true");

        } else {
            Log.i("onToggleIsChecked", "ToggleClick Is Off");

            //turn ringer silent
            amanager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            Log.i(".RINGER_MODE_NORMAL", "Set to true");

            // turn on sound, enable notifications
            amanager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
            Log.i("STREAM_SYSTEM", "Set to False");
            //notifications
            amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            Log.i("STREAM_NOTIFICATION", "Set to False");
            //alarm
            amanager.setStreamMute(AudioManager.STREAM_ALARM, false);
            Log.i("STREAM_ALARM", "Set to False");
            //ringer
            amanager.setStreamMute(AudioManager.STREAM_RING, false);
            Log.i("STREAM_RING", "Set to False");
            //media
            amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            Log.i("STREAM_MUSIC", "Set to False");
        }
        Log.i("onToggleClicked", "ToggleClick Event Ended");
    }

    private void startPurchaseActivity() {
        String title = getResources().getString(R.string.dlg_inform_title);
        String content = getResources().getString(R.string.dialog_inform_purchase_activity);
        OneMsgTwoBtnDialog dlg = new OneMsgTwoBtnDialog(this, title, content, new OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner() {
            @Override
            public void onClickNo() {

            }

            @Override
            public void onClickYes() {
                onBackPressed();
                Intent intent = new Intent(m_app, PurchaseItemActivity.class);
                startActivity(intent);
            }
        });

        dlg.show();
    }

    public void refresh() {
        m_isLoadingEnd = false;

        switch (m_nSelectedTab) {
            case 0:
                m_arrChatList.clear();
                m_adpChatting.notifyDataSetChanged();

                break;
            case 1:
                m_arrBroadcast.clear();
                m_adpBroadcst.notifyDataSetChanged();
                getBroadcastList(0);
                break;
            case 2:
                m_arrFavBroadcast.clear();
                m_adpFavBroadcst.notifyDataSetChanged();
                getFanBroadcastList(0);
                break;
        }
    }

    private void forceExit(String text) {
        // forced exit.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.dlg_confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finishActivity();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void sendHeart(final int heart) {
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);

        server.sendHeart(this, m_app.getUserInfo().user_no, m_pBroadcast.user_no, heart, m_pBroadcast.no, new Callback<BaseModel>() {
            @Override
            public void onResponse(Response<BaseModel> response) {
                BaseModel model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        final GifView gifView1 = (GifView) findViewById(R.id.iv_heart);
                        gifView1.setVisibility(View.VISIBLE);

                        gifView1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                gifView1.setVisibility(View.GONE);
                            }
                        });

                        m_app.getUserInfo().user_heart_cnt -= heart;

                        HeartHistory history = new HeartHistory();
                        history.heart_cnt = heart;
                        m_pBroadcast.history_list.add(history);

                        if (isStreaming == false) {
                            startStream();
                        } else {
                            String str = String.format("하트[%d]개를 전송했습니다.", heart);
                            sendText(str);
                        }

                        CommonUtil.showCenterToast(LiveActivity.this, R.string.msg_send_heart, Toast.LENGTH_SHORT);
                    } else {
                        CommonUtil.showCenterToast(LiveActivity.this, R.string.msg_fail_send_heart, Toast.LENGTH_SHORT);
                        finish();
                    }

                } else {
                    server.processErrorBody(response, LiveActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, LiveActivity.this);
            }
        });
    }

    private void likeBroadCast() {
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);

        server.likeBroadcast(this, m_app.getUserInfo().user_no, m_pBroadcast.no, new Callback<BaseModel>() {
            @Override
            public void onResponse(Response<BaseModel> response) {
                BaseModel model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        CommonUtil.showCenterToast(LiveActivity.this, R.string.msg_add_like, Toast.LENGTH_SHORT);
                    } else {
                        CommonUtil.showCenterToast(LiveActivity.this, R.string.msg_duplicate_like, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, LiveActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, LiveActivity.this);
            }
        });
    }

    private void fanBroadCast() {
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);

        server.fanBroadcast(this, m_app.getUserInfo().user_no, m_pBroadcast.no, new Callback<BaseModel>() {
            @Override
            public void onResponse(Response<BaseModel> response) {
                BaseModel model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    String content = "";
                    if (model.status == BaseModel.OK_DATA) {
                        content = getResources().getString(R.string.msg_add_fan);
                    } else {
                        content = getResources().getString(R.string.msg_duplicate_fan);
                    }

                    OneMsgOneBtnDialog dlg = new OneMsgOneBtnDialog(LiveActivity.this, getResources().getString(R.string.dlg_inform_title), content, new OneMsgOneBtnDialog.OneMsgOneBtnDialogListner() {
                        @Override
                        public void onClickConfirm() {

                        }
                    });
                    dlg.show();
                } else {
                    server.processErrorBody(response, LiveActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, LiveActivity.this);
            }
        });
    }

    // type 0: start 1:end
    private void inoutBroadCast(final int type) {
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);

        server.inoutBroadcast(this, m_pBroadcast.no, m_app.getUserInfo().user_no, type, new Callback<BaseModel>() {
            @Override
            public void onResponse(Response<BaseModel> response) {
                BaseModel model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (type == 0) {
                        initLive();
                    } else if (type == 1) {
                        finalizeXmpp();
                        if (isStreaming == true) {
                            toggleStream();
                        }

                        Intent intent = new Intent();
                        intent.putExtra(KEY_INTENT_LIVE, m_pBroadcast);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } else {
                    server.processErrorBody(response, LiveActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, LiveActivity.this);
            }
        });
    }

    private void chkBroadCastUserEffect() {
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);

        server.chkEnterBroadcast(this, m_pBroadcast.no, m_app.getUserInfo().user_no, new Callback<BroadcastUserEffect>() {
            @Override
            public void onResponse(Response<BroadcastUserEffect> response) {
                BroadcastUserEffect model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.effect != 0) {
                        forceExit(getResources().getString(R.string.msg_cannot_enter_live_force));
                        return;
                    }
                    inoutBroadCast(0);
                } else {
                    server.processErrorBody(response, LiveActivity.this);
                    finishActivity();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, LiveActivity.this);
                finishActivity();
            }
        });
    }

    private void getFanBroadcastList(int totalItemCount) {
        m_isLockListView = true;
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);
        server.getFavBroadcasList(this, m_app.getUserInfo().user_no, 0, 1, totalItemCount, PAGE_ITEM_CNT, new Callback<BroadcastList>() {
            @Override
            public void onResponse(Response<BroadcastList> response) {
                m_isLockListView = false;
                BroadcastList model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        for (int i = 0; i < model.list.size(); i++) {
                            m_adpFavBroadcst.add(model.list.get(i));
                        }
                        m_adpFavBroadcst.notifyDataSetChanged();
                    } else {
                        CommonUtil.showCenterToast(LiveActivity.this, R.string.data_parse_error, Toast.LENGTH_SHORT);
                    }

                    if (model.list.size() < PAGE_ITEM_CNT) {
                        m_isLoadingEnd = true;
                    }
                } else {
                    server.processErrorBody(response, LiveActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                m_isLockListView = false;
                APIProvider.hideProgress();
                server.processServerFailure(t, LiveActivity.this);
            }
        });
    }

    /**
     * Get event list
     */
    private void getBroadcastList(int totalItemCount) {
        m_isLockListView = true;
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);
        server.getBroadcasList(this, m_app.getUserInfo().user_no, 0, 1, totalItemCount, PAGE_ITEM_CNT, new Callback<BroadcastList>() {
            @Override
            public void onResponse(Response<BroadcastList> response) {
                m_isLockListView = false;
                BroadcastList model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        for (int i = 0; i < model.list.size(); i++) {
                            m_adpBroadcst.add(model.list.get(i));
                        }
                        m_adpBroadcst.notifyDataSetChanged();
                    } else {
                        CommonUtil.showCenterToast(LiveActivity.this, R.string.data_parse_error, Toast.LENGTH_SHORT);
                    }
                    if (model.list.size() < PAGE_ITEM_CNT) {
                        m_isLoadingEnd = true;
                    }
                } else {
                    server.processErrorBody(response, LiveActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                m_isLockListView = false;
                APIProvider.hideProgress();
                server.processServerFailure(t, LiveActivity.this);
            }
        });
    }

    /**
     * Get Viewer cnt
     */
    private void checkViewerCnt() {
        final APIProvider server = new APIProvider();

        server.getBroadcastUserCnt(this, m_pBroadcast.no, new Callback<ViewerCnt>() {
            @Override
            public void onResponse(Response<ViewerCnt> response) {
                ViewerCnt model = response.body();
                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        m_pBroadcast.viewer_cnt = model.viewer_cnt;
                        AnyTextView textView = (AnyTextView)findViewById(R.id.tv_member_cnt);
                        textView.setText(String.format("%d/%d", m_pBroadcast.viewer_cnt, m_pBroadcast.max_viewer_cnt));
                    } else {
                        CommonUtil.showCenterToast(LiveActivity.this, R.string.data_parse_error, Toast.LENGTH_SHORT);
                    }
                } else {
                    server.processErrorBody(response, LiveActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                server.processServerFailure(t, LiveActivity.this);
            }
        });

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
        if(packet == null) {
            return;
        }
        if(packet.FromUser == null) {
            return;
        }

        if (packet.FromUser.user_no == m_app.getUserInfo().user_no) {
            // sent successfully
            return;
        }

        UserInfo fromUser = packet.FromUser;
        ChatMessage msg = new ChatMessage();
        msg.user_no = fromUser.user_no;
        msg.content = p_strContent;
        msg.user_grade = fromUser.user_grade;
        msg.user_name = fromUser.user_name;
        msg.forced_exit = packet.is_forced_exit;
        msg.is_bj = packet.is_bj;

        if(packet.is_forced_exit == 1 && m_app.getUserInfo().user_no == packet.exit_user_no){
            forceExit(getResources().getString(R.string.msg_force_exit_live));
            return;
        }

        m_adpChatting.add(msg);
        m_adpChatting.notifyDataSetChanged();

        m_listView.smoothScrollToPosition(m_adpChatting.getCount() - 1);
        m_listView.setSelection(m_adpChatting.getCount() - 1);
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

    private void sendText(String text) {
        String strMsg = text;

        XmppPacket pkt = null;
        pkt = new XmppPacket(XmppPacket.PacketType.PacketType_Message, XmppPacket.PacketSubType.PacketSubType_MessageText, m_app.getUserInfo(), strMsg);

        org.jivesoftware.smack.packet.Message msg = new org.jivesoftware.smack.packet.Message();
        msg.setType(org.jivesoftware.smack.packet.Message.Type.groupchat);
        msg.setBody(pkt.toJSONString());

        m_app.getXmppEndPoint().sendMessage(msg);

        ChatMessage chatMessageText = new ChatMessage();
        chatMessageText.user_no = m_app.getUserInfo().user_no;
        chatMessageText.content = strMsg;
        chatMessageText.user_name =  m_app.getUserInfo().user_name;
        chatMessageText.user_grade =  m_app.getUserInfo().user_grade;
        chatMessageText.is_bj = 0;

        m_adpChatting.add(chatMessageText);
        m_adpChatting.notifyDataSetChanged();

        m_listView.smoothScrollToPosition(m_adpChatting.getCount() - 1);
        m_listView.setSelection(m_adpChatting.getCount() - 1);

        if(m_dlgChatting  != null) {
            m_dlgChatting.list_view.smoothScrollToPosition(m_adpChatting.getCount() - 1);
            m_dlgChatting.list_view.setSelection(m_adpChatting.getCount() - 1);
        }
    }
}