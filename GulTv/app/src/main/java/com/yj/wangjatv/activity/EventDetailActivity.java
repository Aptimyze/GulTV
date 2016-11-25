package com.yj.wangjatv.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yj.wangjatv.R;
import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.model.Event;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.widget.AnyTextView;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class EventDetailActivity extends BaseActivity implements View.OnClickListener{

    private NetworkImageView m_ivPhoto;
    private NetworkImageView m_ivPhotoDetail;
    private TextView m_tvTitle;
    private TextView m_tvDuration;
    private TextView m_tvContent;
    private TextView m_tvPublishDate;
    private TextView m_tvSelectMemberCnt;
    private Event    m_event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        if (savedInstanceState != null) {
            m_event = (Event) savedInstanceState.getSerializable(KEY_INTENT_EVENT);
        } else {
            m_event = (Event) getIntent().getSerializableExtra(KEY_INTENT_EVENT);
        }

        // Show event info
        showEventInfo();
    }

    @Override
    public void init() {
        super.init();
        initMenu();

        m_ivPhoto = (NetworkImageView) findViewById(R.id.niv_photo);
        m_ivPhotoDetail = (NetworkImageView) findViewById(R.id.niv_detail_photo);

        m_tvTitle = (TextView) findViewById(R.id.tv_event_title);
        m_tvDuration = (TextView) findViewById(R.id.tv_duration);
        m_tvContent = (TextView) findViewById(R.id.tv_content);
        m_tvPublishDate = (TextView) findViewById(R.id.tv_publish_date);
        m_tvSelectMemberCnt = (TextView) findViewById(R.id.tv_select_member_cnt);
    }

    private void initMenu() {
        findViewById(R.id.iv_title).setVisibility(View.GONE);
        AnyTextView textView = (AnyTextView)findViewById(R.id.tv_title);
        textView.setVisibility(View.VISIBLE);
        findViewById(R.id.ib_refresh).setVisibility(View.GONE);
        textView.setTextColor(getResources().getColor(R.color.colorAccent));
        textView.setText(getResources().getString(R.string.event_detail));
        findViewById(R.id.rl_right_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.ib_refresh).setOnClickListener(this);
        findViewById(R.id.ib_back).setOnClickListener(this);
        findViewById(R.id.niv_photo).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_refresh:
                showEventInfo();
                break;
            case R.id.ib_back:
                finish();
                break;
            case R.id.niv_photo:
                onGoEventPage(null);
                break;
        }
    }

    /**
     * Go to home page
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
     * Go to product list page
     *
     * @param view
     */
    public void onList(View view) {
        onBackPressed();
    }

    /**
     * @param view
     */
    public void onGoEventPage(View view) {
        String url = m_event.link_url;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (!url.startsWith("http://") && !url.startsWith("https://"))
                url = "http://" + url;
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Show event info
     */
    private void showEventInfo() {

        m_tvTitle.setText(m_event.title);
        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy.MM.dd");
        Date date = CommonUtil.getDateFromString(m_event.start_date, DEFAULT_DATE_FORMAT);
        Date date1 = CommonUtil.getDateFromString(m_event.end_date, DEFAULT_DATE_FORMAT);
        m_tvDuration.setText(String.format("%s : %s ~ %s",
                getString(R.string.request_duration),
                format.format(date),
                format.format(date1)));
        m_tvContent.setText(m_event.content);


        com.android.volley.toolbox.ImageLoader imageLoader = WangjaTVApp.getInstance().getImageLoader();
        String imagUrl = m_event.photo_url;
        m_ivPhoto.setImageUrl(imagUrl, m_app.getImageLoader());
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

                    final ImageView imageView = m_ivPhoto;


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


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_INTENT_EVENT, m_event);
    }
}
