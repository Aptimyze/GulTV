package com.yj.wangjatv.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yj.wangjatv.R;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.BroadcastList;
import com.yj.wangjatv.model.Event;
import com.yj.wangjatv.model.EventList;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.widget.AnyTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.vov.vitamio.utils.Log;
import okhttp3.Interceptor;
import retrofit2.Callback;
import retrofit2.Response;


public class EventListActivity extends BaseActivity implements AbsListView.OnScrollListener, View.OnClickListener {

    private TextView[] m_tvTab = new TextView[2];

    private ListAdapter m_adapter;
    private int m_nCurPage;
    private boolean m_isLoadingEnd = true;
    private boolean m_isLockListView = false;

    private  int m_nSelectedTab = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        onTabSelected(0);
    }

    @Override
    public void init() {
        super.init();
        initMenu();

        m_tvTab[0] = (TextView) findViewById(R.id.tv_tab1);
        m_tvTab[1] = (TextView) findViewById(R.id.tv_tab2);

        ListView listView = (ListView) findViewById(R.id.listView);
        m_adapter = new ListAdapter(this, new ArrayList<Event>());
        listView.setAdapter(m_adapter);
        listView.setOnScrollListener(this);

        findViewById(R.id.tv_tab1).setOnClickListener(this);
        findViewById(R.id.tv_tab2).setOnClickListener(this);
    }

    private void initMenu() {
        findViewById(R.id.iv_title).setVisibility(View.GONE);
        AnyTextView textView = (AnyTextView)findViewById(R.id.tv_title);
        textView.setVisibility(View.VISIBLE);
        textView.setTextColor(getResources().getColor(R.color.colorAccent));
        textView.setText(getResources().getString(R.string.event_detail));
        findViewById(R.id.rl_right_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.ib_refresh).setOnClickListener(this);
        findViewById(R.id.ib_back).setOnClickListener(this);
    }


    /**
     * Go to main page
     *
     * @param view
     */
    public void onHome(View view) {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    /**
     * When the tab was selected
     */
    private void onTabSelected(int index) {
        if (m_tvTab[index].isSelected())
            return;

        m_tvTab[index].setSelected(true);
        m_tvTab[1 - index].setSelected(false);
        for (int i = 0; i < m_tvTab.length; i++) {
            if (index == i) {
                m_tvTab[i].setTextColor(getResources().getColor(R.color.colorAccent));
            } else {
                m_tvTab[i].setTextColor(getResources().getColor(R.color.gray_646565));
            }
        }
        m_nSelectedTab = index;
        m_nCurPage = 0;
        m_adapter.clear();
        m_adapter.notifyDataSetChanged();
        getEventList();
    }

    /**
     * Go to event detail page
     *
     * @param position
     */
    private void onEventSelected(int position) {
        Intent i = new Intent(this, EventDetailActivity.class);
        i.putExtra("event", m_adapter.getItem(position));
        startActivity(i);
    }

    /**
     * Get event list
     */
    private void getEventList() {
        m_isLockListView = true;
        m_isLoadingEnd = false;
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);
        server.getEventList(this, m_nSelectedTab, m_adapter.getCount(), 20, new Callback<EventList>() {
            @Override
            public void onResponse(Response<EventList> response) {
                m_isLockListView = false;
                m_isLoadingEnd = true;
                EventList model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        for (int i = 0; i < model.list.size(); i++) {
                            m_adapter.add(model.list.get(i));
                        }
                        m_adapter.notifyDataSetChanged();
                    } else {
                        CommonUtil.showCenterToast(EventListActivity.this, R.string.data_parse_error, Toast.LENGTH_SHORT);
                    }
                } else {
                    server.processErrorBody(response, EventListActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                m_isLockListView = false;
                m_isLoadingEnd = true;
                APIProvider.hideProgress();
                server.processServerFailure(t, EventListActivity.this);
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
            case R.id.ib_refresh:
                m_adapter.clear();
                getEventList();
                break;
            case R.id.ib_back:
                finish();
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
            getEventList();
        }
    }

    public class ListAdapter extends ArrayAdapter<Event> {
        private LayoutInflater m_inflater;

        public ListAdapter(Context context,
                           List<Event> items) {
            super(context, 0, items);
            m_inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ItemHolder holder;

            if (row == null) {
                row = m_inflater.inflate(R.layout.item_event_list, parent,
                        false);
                holder = new ItemHolder(row);
                row.setTag(holder);
            } else {
                holder = (ItemHolder) row.getTag();
            }

            holder.showData(position);

            return row;
        }

        private class ItemHolder implements OnClickListener {
            private NetworkImageView m_ivPhoto;
            private TextView m_tvTitle;
            private TextView m_tvContent;
            private TextView m_tvPublishDate;
            private TextView m_tvDuration;

            private int m_nPos;

            public ItemHolder(View v) {
                m_ivPhoto = (NetworkImageView) v.findViewById(R.id.niv_photo);
                m_tvTitle = (TextView) v.findViewById(R.id.tv_title);
                m_tvDuration = (TextView) v.findViewById(R.id.tv_duration);
                m_tvContent = (TextView) v.findViewById(R.id.tv_content);
                m_tvPublishDate = (TextView) v.findViewById(R.id.tv_publish_date);

                v.findViewById(R.id.ib_background).setOnClickListener(this);
            }

            public void showData(int pos) {
                m_nPos = pos;

                Event item = getItem(pos);

                m_ivPhoto.setImageUrl(item.photo_url, m_app.getImageLoader());

                m_tvTitle.setText(item.title);
                m_tvContent.setText(item.content);
                SimpleDateFormat format = new SimpleDateFormat(
                        "yyyy.MM.dd");
                Date date = CommonUtil.getDateFromString(item.start_date, DEFAULT_DATE_FORMAT);
                Date date1 = CommonUtil.getDateFromString(item.end_date, DEFAULT_DATE_FORMAT);
                m_tvDuration.setText(String.format("%s : %s ~ %s",
                        getString(R.string.request_duration),
                        format.format(date),
                        format.format(date1)));
            }

            @Override
            public void onClick(View v) {
                onEventSelected(m_nPos);
            }
        }
    }
}
