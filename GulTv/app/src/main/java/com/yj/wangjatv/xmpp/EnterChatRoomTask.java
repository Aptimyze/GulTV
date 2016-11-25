package com.yj.wangjatv.xmpp;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import com.yj.wangjatv.R;
import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.utils.CommonUtil;

import java.util.List;

public class EnterChatRoomTask extends AsyncTask<Integer, Void, Boolean> {
    private static final String TAG = "EnterChatRoomTask";
    private int m_nChatRoomNo;
    private Activity m_pActivity;
    private WangjaTVApp m_pApp;

    public EnterChatRoomTask(Activity activity, WangjaTVApp app) {
        m_pActivity = activity;
        m_pApp = app;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        APIProvider.showWaitingDlg(m_pActivity);
    }

    @Override
    protected Boolean doInBackground(Integer... integers) {
        boolean result = true;
        m_nChatRoomNo = integers[0];

        if (result) {
            List<String> list =  m_pApp.getXmppEndPoint().enterRoom(m_nChatRoomNo);

            if(list == null) {
                result = false;
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        APIProvider.hideProgress();
        if (result) {
            CommonUtil.showCenterToast(m_pActivity, R.string.success_enter_chat_room, Toast.LENGTH_SHORT);
        } else {
            CommonUtil.showCenterToast(m_pActivity, R.string.failed_enter_chat_room, Toast.LENGTH_SHORT);
        }
    }
}