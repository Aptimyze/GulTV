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
import com.yj.wangjatv.activity.PurchaseItemActivity;
import com.yj.wangjatv.adapter.BroadcastAdapter;
import com.yj.wangjatv.dialog.HeartDialog;
import com.yj.wangjatv.dialog.InputPasswordDialog;
import com.yj.wangjatv.dialog.OneMsgOneBtnDialog;
import com.yj.wangjatv.dialog.OneMsgTwoBtnDialog;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.model.BroadcastList;
import com.yj.wangjatv.model.HeartHistory;
import com.yj.wangjatv.model.UserInfo;
import com.yj.wangjatv.utils.CommonUtil;

import java.util.ArrayList;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 5/21/2016.
 */
public class FavFragment extends BaseFragment implements  AbsListView.OnScrollListener, View.OnClickListener{

    private TextView[] m_tvTab = new TextView[3];

    private BroadcastAdapter m_adapter;
    private boolean m_isLoadingEnd = true;
    private boolean m_isLockListView = false;
    private int m_nSelectedTab = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fav, container, false);
        setView(view, inflater, container);

        onTabSelected(0);

        return view;
    }

    public static FavFragment newInstance() {
        FavFragment fragment = new FavFragment();
        return fragment;
    }

    public void init(View v) {
        super.init(v);

        m_tvTab[0] = (TextView)  v.findViewById(R.id.tv_tab1);
        m_tvTab[1] = (TextView) v.findViewById(R.id.tv_tab2);
        m_tvTab[2] = (TextView) v.findViewById(R.id.tv_tab3);

        ListView listView = (ListView) v.findViewById(R.id.listView);
        m_adapter = new BroadcastAdapter(getActivity(), new ArrayList<Broadcast>(), BroadcastAdapter.BROADCAST_FAV_LIST_1,
                new BroadcastAdapter.FavBroadcastAdapterListner() {
                    @Override
                    public void onDelete(Broadcast broadcast) {
                        deleteFavBroadcast(broadcast);
                    }

                    @Override
                    public void onStar(Broadcast broadcast) {
                        final Broadcast pBroadcast = broadcast;
                        final HeartDialog dlg = new HeartDialog(FavFragment.this.getActivity(), getApplicationContext().getUserInfo().user_heart_cnt, null);
                        dlg.setListner( new HeartDialog.HeartDialogListner(){
                            @Override
                            public void onClickNo() {

                            }

                            @Override
                            public void onClickHeart(int cnt) {
                                if(cnt != 0 &&  cnt > getApplicationContext().getUserInfo().user_heart_cnt) {
                                    startPurchaseActivity();
                                    return;
                                }
                                if(cnt == 0) {
                                    return;
                                }

                                sendHeart(pBroadcast, cnt);
                                dlg.dismiss();
                            }

                            @Override
                            public void onClickPurchase() {
                                startPurchaseActivity();
                            }
                        });

                        dlg.show();
                    }

                    @Override
                    public void onPlay(Broadcast broadcast) {
                       getApplicationContext().playBroadcast(FavFragment.this.getActivity(), broadcast);
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
        refresh();
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

    public void refresh() {
        m_adapter.clear();
        m_adapter.notifyDataSetChanged();
        getBroadcastList(0);
    }

    /**
     * Get event list
     */
    private void getBroadcastList(int totalItemCount) {
        m_isLockListView = true;
        m_isLoadingEnd = false;
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(getActivity());

        int type = -1; //전체
        if(m_nSelectedTab == 0) {
            type = -1;
        }
        else if(m_nSelectedTab == 1) {
            type = 0;
        }
        else if(m_nSelectedTab == 2) {
            type = 2;
        }
        server.getFavBroadcasList(getActivity(), getApplicationContext().getUserInfo().user_no, type, 0, totalItemCount, 20, new Callback<BroadcastList>() {
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

    private void sendHeart(Broadcast broadcast, int heart) {
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(FavFragment.this.getActivity());

        server.sendHeart(FavFragment.this.getActivity(), getApplicationContext().getUserInfo().user_no, broadcast.user_no, heart, broadcast.no, new Callback<BaseModel>() {
            @Override
            public void onResponse(Response<BaseModel> response) {
                BaseModel model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        CommonUtil.showCenterToast(FavFragment.this.getActivity(), R.string.msg_send_heart, Toast.LENGTH_SHORT);
                    } else {
                        CommonUtil.showCenterToast(FavFragment.this.getActivity(), R.string.msg_fail_send_heart, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, FavFragment.this.getActivity());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, FavFragment.this.getActivity());
            }
        });
    }

    private void startPurchaseActivity() {
        String title = getResources().getString(R.string.dlg_inform_title);
        String content = getResources().getString(R.string.dialog_inform_purchase_activity);
        OneMsgTwoBtnDialog dlg = new OneMsgTwoBtnDialog(FavFragment.this.getActivity(), title, content, new OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner() {
            @Override
            public void onClickNo() {

            }

            @Override
            public void onClickYes() {
                Intent intent = new Intent(getApplicationContext(), PurchaseItemActivity.class);
                startActivity(intent);
            }
        });

        dlg.show();
    }

    private void deleteFavBroadcast(final Broadcast broadCast) {
            final APIProvider server = new APIProvider();
            APIProvider.showWaitingDlg(FavFragment.this.getActivity());

            server.deletefavBroadcast(FavFragment.this.getActivity(), getApplicationContext().getUserInfo().user_no, broadCast.no, new Callback<BaseModel>() {
                @Override
                public void onResponse(Response<BaseModel> response) {
                    BaseModel model = response.body();

                    APIProvider.hideProgress();

                    if (model != null) {
                        m_adapter.remove(broadCast);
                        m_adapter.notifyDataSetChanged();
                    } else {
                        server.processErrorBody(response, FavFragment.this.getActivity());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    APIProvider.hideProgress();
                    server.processServerFailure(t, FavFragment.this.getActivity());
                }
            });
    }

}
