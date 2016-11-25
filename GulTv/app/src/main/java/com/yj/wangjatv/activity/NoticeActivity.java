package com.yj.wangjatv.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yj.wangjatv.R;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.EventList;
import com.yj.wangjatv.model.Notice;
import com.yj.wangjatv.model.NoticeList;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.widget.AnyTextView;

import java.util.ArrayList;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by john on 2016-02-11.
 */
public class NoticeActivity extends BaseActivity implements View.OnClickListener{

    private ExpandableListView elv;
    private boolean m_isLoadingEnd = true;
    private boolean m_isLockListView = false;

    private ArrayList<Notice> list = new ArrayList<Notice>();
    private NoticeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        adapter = new NoticeAdapter(this, list);
        elv.setAdapter(adapter);
        getNoticeList();
    }

    @Override
    public void init(){
        super.init();
        initMenu();
        elv = (ExpandableListView)findViewById(R.id.elv);
        elv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (m_isLoadingEnd)
                    return;
                if (list.size() > 0 && elv.getLastVisiblePosition() >= totalItemCount - 1 && m_isLockListView == false) {
                    getNoticeList();
                }
            }
        });
    }

    private void initMenu() {
        findViewById(R.id.iv_title).setVisibility(View.GONE);
        AnyTextView textView = (AnyTextView)findViewById(R.id.tv_title);
        textView.setVisibility(View.VISIBLE);
        textView.setText(getResources().getString(R.string.activity_notice_title));
        findViewById(R.id.ib_back).setOnClickListener(this);
    }

    private void getNoticeList(){
        m_isLockListView = true;
        m_isLoadingEnd = false;
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);
        server.getNoticeList(this, adapter.getGroupCount(), 20, new Callback<NoticeList>() {
            @Override
            public void onResponse(Response<NoticeList> response) {
                NoticeList model = response.body();

                APIProvider.hideProgress();
                m_isLockListView = false;
                m_isLoadingEnd = true;
                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        if(adapter.getGroupCount() == 0) {
                            adapter = new NoticeAdapter(NoticeActivity.this, list);
                            elv.setAdapter(adapter);
                        }
                        if(model.list != null) {
                            for (int i = 0; i < model.list.size(); i++) {
                                adapter.mList.add(model.list.get(i));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        CommonUtil.showCenterToast(NoticeActivity.this, R.string.data_parse_error, Toast.LENGTH_SHORT);
                    }
                } else {
                    server.processErrorBody(response, NoticeActivity.this);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, NoticeActivity.this);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
        }
    }

    public class NoticeAdapter extends BaseExpandableListAdapter {

        private Context mContext;
        public ArrayList<Notice> mList;
        private LayoutInflater inflater;

        public NoticeAdapter(Context context, ArrayList<Notice> list){
            mContext = context;
            mList = list;
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getGroupCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mList.get(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null) v = this.inflater.inflate(R.layout.item_notice_group, null);
            Notice item = mList.get(groupPosition);
            TextView tv_titel = (TextView)v.findViewById(R.id.tv_title);
            tv_titel.setText(item.subject);
            tv_titel = (TextView)v.findViewById(R.id.subject_tv);
            tv_titel.setText(item.regdt);
            return v;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mList.get(groupPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }


        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null) v = this.inflater.inflate(R.layout.item_notice_child, null);

            Notice item = mList.get(groupPosition);
            TextView tv_titel = (TextView)v.findViewById(R.id.tv_content);
            tv_titel.setText(item.content);
            final NetworkImageView imageView = (NetworkImageView)v.findViewById(R.id.niv_photo);
            if(item.upfile1 == null || item.upfile1.length() <= 0) {
                imageView.setVisibility(View.GONE);
            }
            else {
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageUrl(item.upfile1, m_app.getImageLoader());

                ImageLoader imageLoader = m_app.getImageLoader();
                String imagUrl = item.upfile1;
                ImageLoader.ImageContainer newContainer = imageLoader.get(imagUrl, new ImageLoader.ImageListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }

                    @Override
                    public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                        // If this was an immediate response that was delivered inside of a layout
                        // pass do not set the image immediately as it will trigger a requestLayout
                        // inside of a layout. Instead, defer setting the image by posting back to
                        // the main thread.
                        if (isImmediate) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onResponse(response, false);
                                }
                            });
                            return;
                        }

                        if (response.getBitmap() != null) {
                            Bitmap bitmap = response.getBitmap();
                            final int bmpWidth = bitmap.getWidth();
                            final int bmpHeight = bitmap.getHeight();

                            ViewTreeObserver vto = imageView.getViewTreeObserver();
                            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                    int width = imageView.getMeasuredWidth();
                                    int height = imageView.getMeasuredHeight();
                                    float ratio = (float) bmpHeight / bmpWidth;
                                    width = View.MeasureSpec.getSize(width);
                                    height = (int) (width * ratio);

                                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)imageView.getLayoutParams();
                                    params.height = height;
                                    imageView.setLayoutParams(params);
                                }
                            });
                        }
                    }
                });
            }
            return v;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
