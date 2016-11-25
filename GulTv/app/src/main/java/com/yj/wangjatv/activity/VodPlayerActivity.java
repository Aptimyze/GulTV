package com.yj.wangjatv.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.cunoraz.gifview.library.GifView;
import com.red5pro.streaming.R5Connection;
import com.red5pro.streaming.R5Stream;
import com.red5pro.streaming.R5StreamProtocol;
import com.red5pro.streaming.config.R5Configuration;
import com.red5pro.streaming.event.R5ConnectionEvent;
import com.red5pro.streaming.event.R5ConnectionListener;
import com.red5pro.streaming.view.R5VideoView;
import com.yj.wangjatv.R;
import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.adapter.BroadcastAdapter;
import com.yj.wangjatv.dialog.OneMsgTwoBtnDialog;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.model.BroadcastList;
import com.yj.wangjatv.model.HeartHistory;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.widget.AnyTextView;

import java.util.ArrayList;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 6/2/2016.
 */
public class VodPlayerActivity extends BaseActivity implements View.OnClickListener,AbsListView.OnScrollListener,  OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback {

        private TextView[] m_tvTab = new TextView[3];

    private boolean m_isLoadingEnd = true;
    private ListView m_listView = null;
    private boolean m_isLockListView = false;

    private Broadcast m_pBroadcast = null;
    private ArrayList<Broadcast> m_arrBroadcast = new ArrayList<>();
    private BroadcastAdapter m_adpBroadcst = null;

        private int mVideoWidth;
        private int mVideoHeight;
        private MediaPlayer mMediaPlayer;
        private SurfaceView mPreview;
        private SurfaceHolder holder;
        private String path;
        private static final String MEDIA = "media";
        private static final int LOCAL_AUDIO = 1;
        private static final int STREAM_AUDIO = 2;
        private static final int RESOURCES_AUDIO = 3;
        private static final int LOCAL_VIDEO = 4;
        private static final int STREAM_VIDEO = 5;
        private boolean mIsVideoSizeKnown = false;
        private boolean mIsVideoReadyToBePlayed = false;

        private RoundCornerProgressBar v_red_slider;
        private AnyTextView tv_title;
        private AnyTextView tv_time;
        private ImageButton ib_mute;
        private ImageButton ib_play;
        private int m_playCount = 0;
        private boolean isPlaying = false;
        Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 0) {
                    m_playCount++;

                    if(mMediaPlayer == null) {
                        m_playCount = 0;
                        isPlaying = false;
                        return;
                    }

                    int totolSecond = (int)(mMediaPlayer.getDuration() / 1000);
                    if (totolSecond < 0 || m_playCount >= totolSecond)  {
                        m_playCount = totolSecond;
                        isPlaying = false;
                        return;
                    }

                    v_red_slider.setProgress(m_playCount);
                    v_red_slider.invalidate();
                    int minute = m_playCount / 60;
                    int second = m_playCount % 60;
                    String time = "";
                    if (minute >= 0 && minute <= 9) {
                        time = time + "0" + minute;
                    } else {
                        time = time + minute;
                    }
                    if (second >= 0 && second <= 9) {
                        time = time + ":0" + second;
                    } else {
                        time = time + ":" + second;
                    }

                    tv_time.setText(time);
                    sendEmptyMessageDelayed(0, 1000);
                }
            }
        };

        private void restart() {
            m_playCount = 0;
            mHandler.sendEmptyMessage(0);
        }

        private void pause() {
            mHandler.sendEmptyMessage(1);
        }

        private void start() {
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }

        boolean isSent = false;
        boolean isCreated = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_vod);

            //onTabSelected(0);
            findViewById(R.id.ll_tabbar);

            m_pBroadcast = (Broadcast)getIntent().getSerializableExtra(KEY_INTENT_LIVE);

            if(m_pBroadcast == null) {
                CommonUtil.showCenterToast(this, R.string.network_connect_error, Toast.LENGTH_SHORT);
                return;
            }

            boolean isHasHistory = getIntent().getBooleanExtra(KEY_INTENT_HAS_HISTORY, false);
            if(isHasHistory == false &&  m_pBroadcast.heart_cnt != 0) {
                if(m_pBroadcast.heart_cnt <= m_app.getUserInfo().user_heart_cnt) {
                    isSent = false;
                    sendHeart(m_pBroadcast.heart_cnt);
                    return;
                }
                else {
                    CommonUtil.showCenterToast(this, R.string.dialog_inform_heart, Toast.LENGTH_SHORT);
                    finish();
                    return;
                }
            }

            initUI();
        }

        private void initUI(){
            isSent = true;

            if(m_pBroadcast.effect == Broadcast.EFFECT_CAPTURE_DEFEND) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }

            tv_title.setText(m_pBroadcast.title);

            m_adpBroadcst = new BroadcastAdapter(this, m_arrBroadcast,BroadcastAdapter.BROADCAST_VOD, new BroadcastAdapter.BroadcastAdapterListner() {
                @Override
                public void onClickBroadcast(Broadcast broadcast) {
                    checkUroBroadcast(broadcast, broadcast.heart_cnt);
                }
            });
            m_listView.setAdapter(m_adpBroadcst);

            getBroadcastList(0);
        }

        @Override
        public void init() {
            super.init();

            initMenu();

            m_tvTab[0] = (TextView) findViewById(R.id.tv_tap_chatting);
            m_tvTab[1] = (TextView) findViewById(R.id.tv_tap_broadcast);
            m_tvTab[2] = (TextView) findViewById(R.id.tv_tap_fav);

            v_red_slider = (RoundCornerProgressBar)findViewById(R.id.v_slider);
            mPreview = (SurfaceView) findViewById(R.id.surface);
            holder = mPreview.getHolder();
            holder.addCallback(this);
            holder.setFormat(PixelFormat.RGBA_8888);
            tv_time = (AnyTextView)findViewById(R.id.tv_time);
            ib_mute = (ImageButton)findViewById(R.id.ib_sound);
            ib_mute.setOnClickListener(this);
            ib_mute.setSelected(true);
            ib_play = (ImageButton)findViewById(R.id.ib_play);
            ib_play.setOnClickListener(this);
            ib_play.setSelected(true);
            tv_title = (AnyTextView)findViewById(R.id.tv_vod_title);

            m_listView = (ListView)findViewById(R.id.list_view);
            m_listView.setOnScrollListener(this);

            findViewById(R.id.cl_video).setOnClickListener(this);
        }

    private void initMenu() {
        findViewById(R.id.iv_title).setVisibility(View.GONE);
        AnyTextView textView = (AnyTextView)findViewById(R.id.tv_title);
        textView.setVisibility(View.VISIBLE);
        textView.setTextColor(getResources().getColor(R.color.gray_323232));
        textView.setText(getResources().getString(R.string.tab_vod));
        findViewById(R.id.rl_right_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.ib_refresh).setOnClickListener(this);
        findViewById(R.id.ib_back).setOnClickListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            m_listView.setVisibility(View.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            m_listView.setVisibility(View.VISIBLE);
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
                mMediaPlayer.seekTo(0);
                mMediaPlayer.start();
                restart();
                break;
            case R.id.ib_back:
                finishActivity();
                break;
            case R.id.ib_play:
                if(ib_play.isSelected() == true) {
                    pause();
                    mMediaPlayer.pause();
                }
                else {
                    start();
                    mMediaPlayer.start();
                }
                ib_play.setSelected(!ib_play.isSelected());
                break;
            case R.id.ib_sound:
                ib_mute.setSelected(!ib_mute.isSelected());
                if(ib_mute.isSelected() == true) {
                    mMediaPlayer.setVolume(0, 0);
                }
                else {
                    mMediaPlayer.setVolume(6, 6);
                }
                ib_mute.setSelected(!ib_mute.isSelected());
                break;
            case R.id.cl_video:
                if(findViewById(R.id.ll_top).getVisibility() == View.VISIBLE) {
                    findViewById(R.id.ll_top).setVisibility(View.GONE);
                }
                else {
                    findViewById(R.id.ll_top).setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    /**
     * When the tab was selected
     */
    private void onTabSelected(int index) {
        if (m_tvTab[index].isSelected())
            return;

        for (int i = 0; i < m_tvTab.length; i++) {
            if (index == i) {
                m_tvTab[i].setSelected(true);
                m_tvTab[i].setTextColor(getResources().getColor(R.color.colorAccent));
            } else {
                m_tvTab[i].setSelected(false);
                m_tvTab[i].setTextColor(getResources().getColor(R.color.gray_646565));
            }
        }
    }

    private String getMediaUrl() {
        String dir = m_app.get_string_SharedPreferences(KEY_PREF_RECORD_DIR);
        String type = m_app.get_string_SharedPreferences(KEY_PREF_RECORD_TYPE);
        String live_name = m_app.getLiveName(m_pBroadcast.no, m_pBroadcast.reg_time);
        String media_url = String.format("%s%s.%s", dir, live_name, type);
        return media_url;
    }


    private void playVideo(Integer Media) {
        doCleanUp();
        try {

            switch (Media) {
                case LOCAL_VIDEO:
				/*
				 * TODO: Set the path variable to a local media file path.
				 */
                    path = "";
                    if (path == "") {
                        // Tell the user to provide a media file URL.
                        CommonUtil.showCenterToast(this, R.string.failed_play_vod, Toast.LENGTH_SHORT);
                        return;
                    }
                    break;
                case STREAM_VIDEO:
				/*
				 * TODO: Set path variable to progressive streamable mp4 or
				 * 3gpp format URL. Http protocol should be used.
				 * Mediaplayer can only play "progressive streamable
				 * contents" which basically means: 1. the movie atom has to
				 * precede all the media data atoms. 2. The clip has to be
				 * reasonably interleaved.
				 *
				 */

                    path = getMediaUrl();
                    if (path == "") {
                        // Tell the user to provide a media file URL.
                        CommonUtil.showCenterToast(this, R.string.failed_play_vod, Toast.LENGTH_SHORT);
                        return;
                    }

                    break;

            }

            // Create a new media player and set the listeners
            mMediaPlayer = new MediaPlayer(this);
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.setDisplay(holder);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);

            setVolumeControlStream(AudioManager.STREAM_MUSIC);

        } catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
        }
    }

    public void onBufferingUpdate(MediaPlayer arg0, int percent) {
        // Log.d(TAG, "onBufferingUpdate percent:" + percent);

    }

    public void onCompletion(MediaPlayer arg0) {
        Log.d(TAG, "onCompletion called");
        CommonUtil.showCenterToast(this, R.string.complete_play_vod, Toast.LENGTH_SHORT);
    }

    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.v(TAG, "onVideoSizeChanged called");
        if (width == 0 || height == 0) {
            Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
            return;
        }
        mIsVideoSizeKnown = true;
        mVideoWidth = width;
        mVideoHeight = height;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }
    }

    public void onPrepared(MediaPlayer mediaplayer) {
        Log.d(TAG, "onPrepared called");
        mIsVideoReadyToBePlayed = true;
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback();
        }
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
        Log.d(TAG, "surfaceChanged called");

    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.d(TAG, "surfaceDestroyed called");
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated called");
        isCreated = true;
        playVideo();

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
        doCleanUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        doCleanUp();
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void doCleanUp() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        mIsVideoReadyToBePlayed = false;
        mIsVideoSizeKnown = false;
    }

    private void startVideoPlayback() {
        Log.v(TAG, "startVideoPlayback");
        holder.setFixedSize(mVideoWidth, mVideoHeight);
        mMediaPlayer.start();

        if(isPlaying == false) {
            isPlaying = true;
            long duration = mMediaPlayer.getDuration();
            int allsecond = (int) duration / 1000;
            v_red_slider.setMax(allsecond);
            start();
        }
    }

    private void playVideo() {
        if(isCreated == true && isSent == true) {
            playVideo(STREAM_VIDEO);
        }
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
                        m_app.getUserInfo().user_heart_cnt -= heart;

                        m_pBroadcast.already_sent = 1;
                        isSent = true;
                        CommonUtil.showCenterToast(VodPlayerActivity.this, R.string.msg_send_heart, Toast.LENGTH_SHORT);

                        initUI();
                        playVideo();
                    } else {
                        CommonUtil.showCenterToast(VodPlayerActivity.this, R.string.msg_fail_send_heart, Toast.LENGTH_SHORT);
                        finish();
                    }

                } else {
                    server.processErrorBody(response, VodPlayerActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, VodPlayerActivity.this);
            }
        });
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
                getBroadcastList(totalItemCount);
        }
    }

    /**
     * Get event list
     */
    private void getBroadcastList(int totalItemCount) {
        m_isLoadingEnd = false;
        m_isLockListView = true;
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);
        server.getUserBroadcasList(this, m_pBroadcast.user_no, 1, m_pBroadcast.no, 1, totalItemCount, 20, new Callback<BroadcastList>() {
            @Override
            public void onResponse(Response<BroadcastList> response) {
                m_isLoadingEnd = true;
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
                        CommonUtil.showCenterToast(VodPlayerActivity.this, R.string.data_parse_error, Toast.LENGTH_SHORT);
                    }
                } else {
                    server.processErrorBody(response, VodPlayerActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                m_isLoadingEnd = true;
                APIProvider.hideProgress();
                server.processServerFailure(t, VodPlayerActivity.this);
            }
        });
    }

    private boolean isHasHistory(Broadcast broadcast, int heart_cnt) {
        if(broadcast.already_sent == 0) {
            return false;
        }
        return true;
    }

    private void checkUroBroadcast(final Broadcast broadcast, int heart_cnt) {
        if(broadcast.user_no == m_app.getUserInfo().user_no) {
            CommonUtil.showCenterToast(this, R.string.your_vod, Toast.LENGTH_SHORT);
            return;
        }

        int cnt = heart_cnt;
        WangjaTVApp app = m_app;

        final boolean hasHistory = isHasHistory(broadcast, heart_cnt);
        if(hasHistory == false && cnt > 0) {
            if (app.getUserInfo().user_heart_cnt < cnt) {
                String title = getResources().getString(R.string.dlg_inform_title);
                String content = String.format(getResources().getString(R.string.dlg_fan_purchse_heart_content), (broadcast.heart_cnt - app.getUserInfo().user_heart_cnt));
                new OneMsgTwoBtnDialog(this, title, content, new OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner() {
                    @Override
                    public void onClickYes() {
                        Intent intent = new Intent(VodPlayerActivity.this, PurchaseItemActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onClickNo() {

                    }
                }).show();
                return;
            }
            else {
                String title = getResources().getString(R.string.dlg_inform_title);
                String content = String.format(getResources().getString(R.string.msg_send_heart_for_broadcast), (broadcast.heart_cnt));
                new OneMsgTwoBtnDialog(this, title, content, new OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner() {
                    @Override
                    public void onClickYes() {
                        startVodActivity(broadcast, hasHistory);
                    }

                    @Override
                    public void onClickNo() {

                    }
                }).show();
                return;
            }
        }

        startVodActivity(broadcast, hasHistory);
    }

    private void finishActivity() {
        Intent intent = new Intent();
        intent.putExtra(KEY_INTENT_LIVE, m_pBroadcast);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void startVodActivity(Broadcast broadcast, boolean hasHistory) {
        if(broadcast.user_no == m_app.getUserInfo().user_no) {
            CommonUtil.showCenterToast(this, R.string.your_vod, Toast.LENGTH_SHORT);
            return;
        }

        finishActivity();

        Intent intent = new Intent(MainActivity.getInstance(), VodPlayerActivity.class);
        intent.putExtra(KEY_INTENT_LIVE, broadcast);
        intent.putExtra(KEY_INTENT_HAS_HISTORY, hasHistory);
        MainActivity.getInstance().startActivityForResult(intent , INTENT_ACTIVITY_VOD);
    }
}