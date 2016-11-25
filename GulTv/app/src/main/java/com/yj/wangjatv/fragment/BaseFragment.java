package com.yj.wangjatv.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yj.wangjatv.Const;
import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.activity.BaseActivity;

public abstract class BaseFragment extends Fragment implements Const {


    public int m_id;
    protected View m_view;
    protected LayoutInflater m_inflater;
    protected ViewGroup m_container;
    private Context mContext;
    protected WangjaTVApp mApplicationContext;

    // Set main view
    protected void setView(View view, LayoutInflater inflater, ViewGroup container) {
        m_view = view;
        m_inflater = inflater;
        m_container = container;
        mContext = getActivity();
        mApplicationContext = (WangjaTVApp) getActivity().getApplication();
        initActionBar();
        init(view);
    }

    public void init(View v) {

    }

    public void initActionBar() {
    }

    public void onFragmentResume() {

    }

    public WangjaTVApp getApplicationContext() {
        return mApplicationContext;
    }
}