package com.yj.wangjatv.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.yj.wangjatv.Const;
import com.yj.wangjatv.R;
import com.yj.wangjatv.dialog.DialogMessageActivity;
import com.yj.wangjatv.fragment.BroadCastFragment;
import com.yj.wangjatv.fragment.FavFragment;
import com.yj.wangjatv.fragment.LiveFragment;
import com.yj.wangjatv.fragment.MoreFragment;
import com.yj.wangjatv.fragment.VodFragment;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.EventList;
import com.yj.wangjatv.model.LiveServerInfo;
import com.yj.wangjatv.model.UserInfo;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.utils.SelectPhotoManager;
import com.yj.wangjatv.xmpp.XmppEndPointService;

import java.util.HashMap;

import io.vov.vitamio.LibsChecker;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements TabHost.OnTabChangeListener, View.OnClickListener {

    ///////////////////////////////////////
    // Constants
    ///////////////////////////////////////

    private static final String TAB_LIVE = "Live";
    public static final String  TAB_VOD = "Vod";
    private static final String TAB_FAV = "Favorite";
    private static final String TAB_BROADCAST = "BroadCast";
    private static final String TAB_MORE = "More";

    private static MainActivity g_MainActivity = null;


    public TabHost m_tabHost;
    private String m_strLastTabId = "";
    private HashMap<String, Fragment> m_tabFragments = new HashMap<String, Fragment>();

    SlidingMenu menu;

    public static MainActivity getInstance() {
        if(g_MainActivity == null) {
            g_MainActivity = new MainActivity();
        }
        return g_MainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setting
        CommonUtil.setMobileDataEnabled(m_app, m_app.get_boolean_SharedPreferences(KEY_PREF_SETTING_3G4G, true));
        CommonUtil.setAutoRoate(m_app, m_app.get_boolean_SharedPreferences(KEY_PREF_SETTING_AUTOROTATE, false));

        boolean autologin = m_app.get_boolean_SharedPreferences(KEY_PREF_SETTING_AUTOLOGIN, true);
        if(m_app.getUserInfo().is_loginned == false && autologin == true) {
            onLogin();
        }
        else {
            if(m_app.getUserInfo().is_loginned == true) {
                startService(new Intent(MainActivity.this, XmppEndPointService.class));
            }
        }

        g_MainActivity = this;
    }

    @Override
    public void init() {
        super.init();

        setupTabHost(0);

        findViewById(R.id.ib_menu).setOnClickListener(this);
        findViewById(R.id.ib_refresh).setOnClickListener(this);

        initLeftSildeMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    static class DummyTabFactory implements TabHost.TabContentFactory { // Makes the content of a tab when it is selected.
        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == INTENT_ACTIVITY_FLAG_PROFILE) {
            if(resultCode == RESULT_OK) {
                refresh();
            }
        }
        else if (requestCode == SelectPhotoManager.CROP_FROM_CAMERA
                || requestCode == SelectPhotoManager.PICK_FROM_CAMERA
                || requestCode == SelectPhotoManager.PICK_FROM_FILE) {
            if(m_strLastTabId.equals(TAB_BROADCAST) == true) {
               BroadCastFragment broadCastFragment = (BroadCastFragment) m_tabFragments.get(TAB_BROADCAST);
                broadCastFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
        else if(requestCode == INTENT_ACTIVITY_LIVE) {
            if(m_strLastTabId.equals(TAB_LIVE) == true) {
                LiveFragment broadCastFragment = (LiveFragment) m_tabFragments.get(TAB_LIVE);
                broadCastFragment.onActivityResult(requestCode, resultCode, data);
            }
            else if(m_strLastTabId.equals(TAB_FAV) == true) {
                FavFragment broadCastFragment = (FavFragment) m_tabFragments.get(TAB_FAV);
                broadCastFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
        else if(requestCode == INTENT_ACTIVITY_VOD) {
            if(m_strLastTabId.equals(TAB_VOD) == true) {
                VodFragment broadCastFragment = (VodFragment) m_tabFragments.get(TAB_VOD);
                broadCastFragment.onActivityResult(requestCode, resultCode, data);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupTabHost(int tabNum) {
        m_tabHost = (TabHost) findViewById(android.R.id.tabhost);
        m_tabHost.setup();
        m_tabHost.setOnTabChangedListener(this);

        LayoutInflater w_inflater = LayoutInflater.from(this);
        TabHost.TabSpec w_tab1 = m_tabHost.newTabSpec(TAB_LIVE);
        View w_tabbutton1 = w_inflater.inflate(R.layout.item_tab_live, (ViewGroup) m_tabHost.findViewById(android.R.id.tabs), false);
        w_tab1.setIndicator(w_tabbutton1);
        w_tab1.setContent(new DummyTabFactory(this));
        m_tabHost.addTab(w_tab1);

        TabHost.TabSpec w_tab2 = m_tabHost.newTabSpec(TAB_VOD);
        View w_tabbutton2 = w_inflater.inflate(R.layout.item_tab_vod, (ViewGroup) m_tabHost.findViewById(android.R.id.tabs), false);
        w_tab2.setIndicator(w_tabbutton2);
        w_tab2.setContent(new DummyTabFactory(this));
        m_tabHost.addTab(w_tab2);

        TabHost.TabSpec w_tab3 = m_tabHost.newTabSpec(TAB_FAV);
        View w_tabbutton3 = w_inflater.inflate(R.layout.item_tab_fav, (ViewGroup) m_tabHost.findViewById(android.R.id.tabs), false);
        w_tab3.setIndicator(w_tabbutton3);
        w_tab3.setContent(new DummyTabFactory(this));
        m_tabHost.addTab(w_tab3);

        TabHost.TabSpec w_tab4 = m_tabHost.newTabSpec(TAB_BROADCAST);
        View w_tabbutton4 = w_inflater.inflate(R.layout.item_tab_broadcast, (ViewGroup) m_tabHost.findViewById(android.R.id.tabs), false);
        w_tab4.setIndicator(w_tabbutton4);
        w_tab4.setContent(new DummyTabFactory(this));
        m_tabHost.addTab(w_tab4);

        TabHost.TabSpec w_tab5 = m_tabHost.newTabSpec(TAB_MORE);
        View w_tabbutton5 = w_inflater.inflate(R.layout.item_tab_more, (ViewGroup) m_tabHost.findViewById(android.R.id.tabs), false);
        w_tab5.setIndicator(w_tabbutton5);
        w_tab5.setContent(new DummyTabFactory(this));
        m_tabHost.addTab(w_tab5);

        m_tabHost.setCurrentTab(tabNum);
    }

    private void initLeftSildeMenu() {
        // init main view
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        menu.setFadeDegree(0.35f);
        menu.setBehindOffsetRes(R.dimen.sliding_margine);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setOnCloseListener(new SlidingMenu.OnCloseListener() {
            @Override
            public void onClose() {

            }
        });
        menu.setMenu(R.layout.sidemenu_left);
        findViewById(R.id.btn_app_intro).setOnClickListener(this);
        findViewById(R.id.btn_item_purchase).setOnClickListener(this);
        findViewById(R.id.btn_use_rule).setOnClickListener(this);
        findViewById(R.id.btn_live).setOnClickListener(this);
        findViewById(R.id.btn_vod).setOnClickListener(this);
        findViewById(R.id.btn_fav).setOnClickListener(this);
        findViewById(R.id.btn_broadcast).setOnClickListener(this);
        findViewById(R.id.btn_more).setOnClickListener(this);
        findViewById(R.id.btn_event).setOnClickListener(this);
    }

    ///////////////////////////////////////
    // Tab change event handler
    ///////////////////////////////////////

    @Override
    public void onTabChanged(String tabId) {

        if(tabId.equals(TAB_LIVE) == false) {
            if (m_app.checkLoginned(this) == false) {
                return;
            }
        }

        if (!m_strLastTabId.equals(tabId)) {
            android.support.v4.app.FragmentTransaction w_ft = getSupportFragmentManager().beginTransaction();

            if (!m_strLastTabId.isEmpty()) {
                if (m_tabFragments.get(m_strLastTabId) != null) {
                    w_ft.remove(m_tabFragments.get(m_strLastTabId));
                }
            }

            if (m_tabFragments.get(tabId) == null) {
                if (tabId.equals(TAB_LIVE)) {
                    m_tabFragments.put(tabId, LiveFragment.newInstance());
                }
                else if (tabId.equals(TAB_VOD)) {
                    m_tabFragments.put(tabId, VodFragment.newInstance());
                }
                else if (tabId.equals(TAB_FAV)) {
                    m_tabFragments.put(tabId, FavFragment.newInstance());
                }
                else if (tabId.equals(TAB_BROADCAST)) {
                    m_tabFragments.put(tabId, BroadCastFragment.newInstance());
                }
                else if (tabId.equals(TAB_MORE)) {
                    m_tabFragments.put(tabId, MoreFragment.newInstance());
                }
            }

            if(tabId.equals(TAB_MORE) == true || tabId.equals(TAB_BROADCAST) == true) {
                findViewById(R.id.ib_refresh).setVisibility(View.GONE);
            }
            else {
                findViewById(R.id.ib_refresh).setVisibility(View.VISIBLE);
            }

            m_strLastTabId = tabId;
            w_ft.add(R.id.fl_realtabcontent, m_tabFragments.get(tabId));
            w_ft.commitAllowingStateLoss();
        }
    }

    // BACK key handler
    Handler m_hndBackKey = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0)
                m_bFinish = false;
        }
    };

    boolean m_bFinish = false;

    @Override
    public void onBackPressed() {
        if (menu.isMenuShowing()) {
            menu.toggle();
        } else {
            if (!m_bFinish) {
                m_bFinish = true;
                Toast.makeText(this,
                        getResources().getString(R.string.app_finish_message),
                        Toast.LENGTH_SHORT).show();
                m_hndBackKey.sendEmptyMessageDelayed(0, 2000);
            } else {
                CommonUtil.killProcess(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() != R.id.ib_menu && menu.isMenuShowing() == true) {
            menu.toggle();
        }

        switch (v.getId()) {
            case R.id.ib_menu: {
                if(menu.isMenuShowing() == false) {
                    if (m_app.checkLoginned(this) == false) {
                        return;
                    }
                    menu.showMenu();
                }
                else {
                    menu.toggle();
                }
            }
                break;
            case R.id.ib_refresh: {
                refresh();
            }
            break;
            // SlideMenu
            case R.id.btn_app_intro: {
                Intent intent = new Intent(this, ProfileModifyActivity.class);
                startActivityForResult(intent, INTENT_ACTIVITY_FLAG_PROFILE);
            }
            break;
            case R.id.btn_item_purchase: {
                Intent intent = new Intent(this, PurchaseItemActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.btn_use_rule: {
                Intent intent = new Intent(this, NoticeActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.btn_live: {
                m_tabHost.setCurrentTab(0);
                //onTabChanged(TAB_LIVE);
            }
            break;
            case R.id.btn_vod: {
                m_tabHost.setCurrentTab(1);
                //onTabChanged(TAB_VOD);
            }
            break;
            case R.id.btn_fav: {
                m_tabHost.setCurrentTab(2);
                //onTabChanged(TAB_FAV);
            }
            break;
            case R.id.btn_broadcast: {
                m_tabHost.setCurrentTab(3);
                //onTabChanged(TAB_BROADCAST);
            }
            break;
            case R.id.btn_more: {
                m_tabHost.setCurrentTab(4);
                //onTabChanged(TAB_MORE);
            }
            break;
            case R.id.btn_event: {
                Intent intent = new Intent(this, EventListActivity.class);
                startActivity(intent);
            }
            break;
        }
    }

    private void refresh() {
        if(m_strLastTabId.equals(TAB_LIVE)) {
            LiveFragment fragment = (LiveFragment)m_tabFragments.get(TAB_LIVE);
            fragment.refresh();
        }
        else if(m_strLastTabId.equals(TAB_VOD)) {
            VodFragment fragment = (VodFragment)m_tabFragments.get(TAB_LIVE);
            fragment.refresh();
        }
        else if(m_strLastTabId.equals(TAB_FAV)) {
            FavFragment fragment = (FavFragment)m_tabFragments.get(TAB_FAV);
            fragment.refresh();
        }
    }

    private void getLiveList(int order, int start, int pagenum) {

    }


    public  void onLogin() {

        String email = m_app.get_string_SharedPreferences(KEY_PREF_USER_ID);
        String passwd = m_app.get_string_SharedPreferences(KEY_PREF_USER_PWD);

        if(email == null || email.isEmpty() == true) {
            return;
        }

        if(passwd == null || passwd.isEmpty() == true) {
            return;
        }

        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);

        int type = 1;
        String device_id = CommonUtil.getDeviceId(this);
        String push_token = m_app.getPushToken();
        server.login(this, email, passwd, type, device_id, push_token, new Callback<UserInfo>() {
            @Override
            public void onResponse(Response<UserInfo> response) {
                UserInfo model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        m_app.getUserInfo().copy(model);
                        m_app.getUserInfo().is_loginned = true;

                        startService(new Intent(MainActivity.this, XmppEndPointService.class));
                    } else {

                    }

                } else {
                    server.processErrorBody(response, MainActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, MainActivity.this);
            }
        });
    }
}
