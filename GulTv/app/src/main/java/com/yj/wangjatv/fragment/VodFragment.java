package com.yj.wangjatv.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yj.wangjatv.R;
import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.activity.LiveActivity;
import com.yj.wangjatv.activity.MainActivity;
import com.yj.wangjatv.activity.PurchaseItemActivity;
import com.yj.wangjatv.activity.VodPlayerActivity;
import com.yj.wangjatv.adapter.BroadcastAdapter;
import com.yj.wangjatv.dialog.OneMsgTwoBtnDialog;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.model.BroadcastList;
import com.yj.wangjatv.model.HeartHistory;
import com.yj.wangjatv.utils.CommonUtil;

import java.util.ArrayList;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 5/21/2016.
 */
public class VodFragment extends BaseFragment implements  AbsListView.OnScrollListener, View.OnClickListener{

    private TextView[] m_tvTab = new TextView[3];

    private BroadcastAdapter m_adapter;
    private boolean m_isLoadingEnd = false;
    private boolean m_isLockListView = false;
    private int m_nSelectedTab = 0;


    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_vod, container, false);
        setView(view, inflater, container);

        onTabSelected(0);

        return view;
    }

    public static VodFragment newInstance() {
        VodFragment fragment = new VodFragment();
        return fragment;
    }

    public void init(View v) {
        super.init(v);

        m_tvTab[0] = (TextView) v.findViewById(R.id.tv_tab1);
        m_tvTab[1] = (TextView) v.findViewById(R.id.tv_tab2);
        m_tvTab[2] = (TextView) v.findViewById(R.id.tv_tab3);

        ListView listView = (ListView) v.findViewById(R.id.listView);
        m_adapter = new BroadcastAdapter(getActivity(), new ArrayList<Broadcast>(), BroadcastAdapter.BROADCAST_VOD,
                new BroadcastAdapter.BroadcastAdapterListner() {
                    @Override
                    public void onClickBroadcast(Broadcast broadcast) {
                        checkUroBroadcast(broadcast, broadcast.heart_cnt);
                    }
                });
        listView.setAdapter(m_adapter);
        listView.setOnScrollListener(this);

        v.findViewById(R.id.tv_tab1).setOnClickListener(this);
        v.findViewById(R.id.tv_tab2).setOnClickListener(this);
        v.findViewById(R.id.tv_tab3).setOnClickListener(this);
    }


    /**
     * When the tab was selected
     */
    private void onTabSelected(int index) {
        if (m_tvTab[index].isSelected())
            return;

        for (int i = 0; i < m_tvTab.length; i++) {
            if (index == i) {
                m_tvTab[i].setTextColor(getResources().getColor(R.color.colorAccent));
                m_tvTab[i].setSelected(true);
            } else {
                m_tvTab[i].setTextColor(getResources().getColor(R.color.gray_646565));
                m_tvTab[i].setSelected(false);
            }
        }
        m_nSelectedTab = index;

        m_adapter.clear();
        m_adapter.notifyDataSetChanged();
        getBroadcastList(0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_ACTIVITY_LIVE) {
            if(data != null) {
                Broadcast broadcast = (Broadcast)data.getSerializableExtra(KEY_INTENT_LIVE);

                if(broadcast == null) {
                    return;
                }


                for(int i = 0; i < m_adapter.getCount(); i++) {
                    if(m_adapter.getItem(i).no == broadcast.no) {
                        m_adapter.getItem(i).history_list = broadcast.history_list;
                        break;
                    }
                }

                m_adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Get event list
     */
    private void getBroadcastList(int totalItemCount) {
        m_isLockListView = true;
        m_isLoadingEnd = true;
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(getActivity());
        WangjaTVApp m_app = getApplicationContext();
        server.getBroadcasList(getActivity(), m_app.getUserInfo().user_no, 1, m_nSelectedTab, totalItemCount, 20, new Callback<BroadcastList>() {
            @Override
            public void onResponse(Response<BroadcastList> response) {
                m_isLockListView = false;
                m_isLoadingEnd = true;
                BroadcastList model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        for (int i = 0; i < model.list.size(); i++) {
                            m_adapter.add(model.list.get(i));
                        }
                        m_adapter.notifyDataSetChanged();
                    } else {
                        CommonUtil.showCenterToast(getActivity(), R.string.data_parse_error, Toast.LENGTH_SHORT);
                    }
                } else {
                    server.processErrorBody(response, getActivity());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                m_isLockListView = false;
                m_isLoadingEnd = true;
                APIProvider.hideProgress();
                server.processServerFailure(t, getActivity());
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tab1:
                onTabSelected(0);
                break;
            case R.id.tv_tab2:
                onTabSelected(1);
                break;
            case R.id.tv_tab3:
                onTabSelected(2);
                break;
            case R.id.ib_refresh:

                break;
            case R.id.ib_back:

                break;
        }
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

        if (firstVisibleItem >= count && totalItemCount != 0
                && !m_isLockListView) {
            getBroadcastList(totalItemCount);
        }
    }

    public void refresh() {
        m_adapter.clear();
        m_adapter.notifyDataSetChanged();
        getBroadcastList(0);
    }

    private boolean isHasHistory(Broadcast broadcast, int heart_cnt) {
        if(broadcast.already_sent == 0) {
            return false;
        }
        return true;
    }

    private void checkUroBroadcast(final Broadcast broadcast, int heart_cnt) {
        if(broadcast.user_no == getApplicationContext().getUserInfo().user_no) {
            CommonUtil.showCenterToast(getActivity(), R.string.your_vod, Toast.LENGTH_SHORT);
            return;
        }

        int cnt = heart_cnt;
        WangjaTVApp app = VodFragment.this.getApplicationContext();

        final boolean hasHistory = isHasHistory(broadcast, heart_cnt);
        if(hasHistory == false && cnt > 0) {
            if (app.getUserInfo().user_heart_cnt < cnt) {
                String title = getResources().getString(R.string.dlg_inform_title);
                String content = String.format(getResources().getString(R.string.dlg_fan_purchse_heart_content), (broadcast.heart_cnt - app.getUserInfo().user_heart_cnt));
                new OneMsgTwoBtnDialog(VodFragment.this.getActivity(), title, content, new OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner() {
                    @Override
                    public void onClickYes() {
                        Intent intent = new Intent(VodFragment.this.getActivity(), PurchaseItemActivity.class);
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
                new OneMsgTwoBtnDialog(VodFragment.this.getActivity(), title, content, new OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner() {
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

    public void startVodActivity(Broadcast broadcast, boolean hasHistory) {
        if(broadcast.user_no == getApplicationContext().getUserInfo().user_no) {
            CommonUtil.showCenterToast(getActivity(), R.string.your_vod, Toast.LENGTH_SHORT);
            return;
        }
        Intent intent = new Intent(this.getActivity(), VodPlayerActivity.class);
        intent.putExtra(KEY_INTENT_LIVE, broadcast);
        intent.putExtra(KEY_INTENT_HAS_HISTORY, hasHistory);
        this.getActivity().startActivityForResult(intent, INTENT_ACTIVITY_VOD);
    }
}
