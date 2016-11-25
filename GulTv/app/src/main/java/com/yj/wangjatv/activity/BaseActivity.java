package com.yj.wangjatv.activity;

/**
 * Created by Ralph on 5/20/2016.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.yj.wangjatv.Const;
import com.yj.wangjatv.R;
import com.yj.wangjatv.WangjaTVApp;


public class BaseActivity extends AppCompatActivity implements Const {
    protected WangjaTVApp m_app;

    private ProgressDialog m_dlgProgress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_app = (WangjaTVApp) getApplication();
        m_app.setMainAct(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        init();
    }

    public void init() {

    }

    private void transitionIn() {
        overridePendingTransition(R.anim.fade, R.anim.hold);
    }

    private void transitionOut() {
        overridePendingTransition(R.anim.hold,R.anim.fadeout);
    }

    public void startActivity(Intent paramIntent) {
        super.startActivity(paramIntent);
        transitionIn();
    }

    public void finish(boolean isAnimation) {
        super.finish();
        if (isAnimation)
            transitionOut();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putSerializable("login_user", m_app.m_loginUser);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        m_app.m_loginUser = (UserInfo) savedInstanceState.getSerializable("login_user");
    }

    @Override
    public void onBackPressed() {
//        if (HttpRequestHelper.isShowProgress()) {
//            HttpRequestHelper.cancelProgress();
//        }

        super.onBackPressed();
    }

    // Show loading progress bar from server
    public void showProgress(String title, String msg) {
        if (m_dlgProgress != null && m_dlgProgress.isShowing())
            return;

        m_dlgProgress = new ProgressDialog(this);
        m_dlgProgress.setTitle(title);
        m_dlgProgress.setMessage(msg);
        m_dlgProgress.setCancelable(false);
        m_dlgProgress.setCanceledOnTouchOutside(false);
        m_dlgProgress.setIndeterminate(false);
        m_dlgProgress.show();
    }

    // Hide loading progress bar
    public void hideProgress() {
        if (m_dlgProgress != null && m_dlgProgress.isShowing())
            m_dlgProgress.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public WangjaTVApp getApp() {
        return m_app;
    }
}
