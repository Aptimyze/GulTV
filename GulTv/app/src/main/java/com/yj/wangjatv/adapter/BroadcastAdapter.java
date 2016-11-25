package com.yj.wangjatv.adapter;

import android.content.Context;
import android.media.Image;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.yj.wangjatv.Const;
import com.yj.wangjatv.R;
import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.activity.BaseActivity;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.model.BroadcastCategoryList;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.utils.StringFilter;
import com.yj.wangjatv.widget.AnyTextView;

import java.util.List;

/**
 * Created by Ralph on 6/2/2016.
 */
public class BroadcastAdapter extends ArrayAdapter<Broadcast> {

    public static final int BROADCAST_LIVE = 0;
    public static final int BROADCAST_VOD = 1;
    public static final int BROADCAST_LIVE_1 = 2;
    public static final int BROADCAST_FAV_LIST = 3;
    public static final int BROADCAST_FAV_LIST_1 = 4;

    private LayoutInflater m_inflater;
    private int type; // 0:live 1:vod, 2:live_1 3:fav_list

    private BroadcastAdapterListner m_listner;
    private FavBroadcastAdapterListner m_favlistner;

    public interface BroadcastAdapterListner {
        public void onClickBroadcast(Broadcast broadcast);
    }

    public interface FavBroadcastAdapterListner {
        public void onDelete(Broadcast broadcast);
        public void onPlay(Broadcast broadcast);
        public void onStar(Broadcast broadcast);
    }

    public BroadcastAdapter(Context context,
                       List<Broadcast> items, int type, BroadcastAdapterListner listner) {
        super(context, 0, items);
        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.type = type;
        this.m_listner = listner;
    }

    public BroadcastAdapter(Context context,
                            List<Broadcast> items, int type, FavBroadcastAdapterListner listner) {
        super(context, 0, items);
        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.type = type;
        this.m_favlistner = listner;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemHolder holder;

        if (row == null) {
            if( this.type == BROADCAST_LIVE) {
                row = m_inflater.inflate(R.layout.item_live_list, parent, false);
            }
            else if(this.type == BROADCAST_VOD) {
                row = m_inflater.inflate(R.layout.item_live_list, parent, false);
            }
            else if(this.type == BROADCAST_LIVE_1) {
                row = m_inflater.inflate(R.layout.item_live_list_1, parent, false);
            }
            else if(this.type == BROADCAST_FAV_LIST) {
                row = m_inflater.inflate(R.layout.item_live_list_1, parent, false);
            }
            else if(this.type == BROADCAST_FAV_LIST_1) {
                row = m_inflater.inflate(R.layout.item_fav_list, parent, false);
            }

            holder = new ItemHolder(row);
            row.setTag(holder);
        } else {
            holder = (ItemHolder) row.getTag();
        }

        holder.showData(position);

        return row;
    }

    private class ItemHolder implements View.OnClickListener {
        private NetworkImageView m_ivPhoto;
        private TextView m_tvTitle;
        private ImageView iv_live_mark;
        private ImageView iv_fan_club;
        private ImageView iv_lock;
        private AnyTextView tv_category;
        private AnyTextView tv_explain;
        private AnyTextView tv_user_name;
        private AnyTextView tv_member_cnt;
        private AnyTextView tv_time;
        private View rl_control;
        private ImageView iv_broadcasting;
        private int m_nPos;
        private ImageButton ib_background;
        private ImageButton ib_play;
        private View linearLayout1;

        public ItemHolder(View v) {
            m_ivPhoto = (NetworkImageView) v.findViewById(R.id.niv_photo);
            m_tvTitle = (TextView) v.findViewById(R.id.tv_title);
            tv_category = (AnyTextView) v.findViewById(R.id.tv_category);
            tv_explain = (AnyTextView) v.findViewById(R.id.tv_explain);
            tv_user_name = (AnyTextView) v.findViewById(R.id.tv_user_name);
            iv_live_mark = (ImageView) v.findViewById(R.id.iv_live_mark);
            iv_fan_club = (ImageView) v.findViewById(R.id.iv_fan_club);
            iv_lock = (ImageView) v.findViewById(R.id.iv_lock);
            tv_member_cnt = (AnyTextView) v.findViewById(R.id.tv_member_cnt);
            tv_time = (AnyTextView) v.findViewById(R.id.tv_time);
            rl_control = v.findViewById(R.id.rl_control);
            iv_broadcasting = (ImageView)v.findViewById(R.id.iv_broadcasting);
            ib_background = (ImageButton)v.findViewById(R.id.ib_background);
            ib_background.setOnClickListener(this);
            ib_play = (ImageButton)v.findViewById(R.id.ib_play);
            linearLayout1 =v.findViewById(R.id.linearLayout1);

            if( v.findViewById(R.id.ib_delete) != null) {
                v.findViewById(R.id.ib_delete).setOnClickListener(this);
            }
            if( v.findViewById(R.id.ib_play) != null) {
                v.findViewById(R.id.ib_play).setOnClickListener(this);
            }
            if( v.findViewById(R.id.ib_star) != null) {
                v.findViewById(R.id.ib_star).setOnClickListener(this);
            }
            if( v.findViewById(R.id.ib_reward) != null) {
                v.findViewById(R.id.ib_reward).setOnClickListener(this);
            }
            if(rl_control != null) {
                rl_control.setVisibility(View.GONE);
            }
        }

        public void showData(int pos) {
            m_nPos = pos;

            Broadcast item = getItem(pos);

            BaseActivity activity = (BaseActivity)getContext();
            WangjaTVApp app =  activity.getApp();

            m_ivPhoto.setImageUrl(item.thumb_url, app.getImageLoader());
            m_tvTitle.setText(item.title);

            if(BroadcastAdapter.this.type == BROADCAST_LIVE || BroadcastAdapter.this.type == BROADCAST_FAV_LIST_1) {
                if (item.live_type == Broadcast.LIVE_TYPE) {
                    iv_live_mark.setVisibility(View.VISIBLE);
                } else {
                    iv_live_mark.setVisibility(View.GONE);
                }

                if (item.type == Broadcast.LIVE_TYPE_FAN) {
                    iv_fan_club.setVisibility(View.VISIBLE);
                } else {
                    iv_fan_club.setVisibility(View.GONE);
                }

                if (item.lock_password == null || item.lock_password.isEmpty() == true) {
                    iv_lock.setVisibility(View.GONE);
                } else {
                    iv_lock.setVisibility(View.VISIBLE);
                }
            }
            else if(BroadcastAdapter.this.type == BROADCAST_VOD) {
                iv_live_mark.setVisibility(View.GONE);
                iv_fan_club.setVisibility(View.GONE);
                iv_lock.setVisibility(View.GONE);

                if(linearLayout1 != null) {
                    if (item.already_sent == 1) {
                        linearLayout1.setBackgroundColor(getContext().getResources().getColor(R.color.yellow));
                    }
                    else {
                        linearLayout1.setBackgroundColor(getContext().getResources().getColor(R.color.White));
                    }
                }
            }

            if(iv_broadcasting != null) {
                if(item.live_type == Broadcast.LIVE_TYPE) {
                    iv_broadcasting.setSelected(true);
                    ib_play.setVisibility(View.VISIBLE);
                }
                else {
                    iv_broadcasting.setSelected(false);
                    ib_play.setVisibility(View.GONE);
                }
            }
            tv_category.setText(String.format("[%s]", item.category_name));

            //content
            String time = item.reg_time;
            int date = CommonUtil.getDateOfMonth(time, Const.DEFAULT_DATE_FORMAT);
            int month = CommonUtil.getMonthOfYear(time, Const.DEFAULT_DATE_FORMAT);
            int day = CommonUtil.getDayOfWeek(time, Const.DEFAULT_DATE_FORMAT);

            String []yoil = {"일", "월", "화", "수", "목", "금", "토"};
            String content = String.format("%s님의 ", item.user_name);
            if((month+1) < 10) {
                content = content + "0" + (month+1);
            }
            else {
                content = content + (month+1);
            }

            if(date < 10) {
                content = content + ".0" + date;
            }
            else {
                content = content + "." + date;
            }

            if(day >= 1 && day <= 7) {
                content = content + "(" + yoil[day - 1] + ")" + " 방송입니다.";
            }

            Spannable wordtoSpan = new SpannableString(content);
            wordtoSpan.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.colorAccent)), 0, item.user_name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv_explain.setText(wordtoSpan);

            tv_user_name.setText(String.format("%s(%s)",item.user_name, item.getBjID()));
            tv_member_cnt.setText(String.format("%d/%d", item.viewer_cnt, item.max_viewer_cnt));

            tv_time.setText(item.reg_time.subSequence(item.reg_time.length()-8, item.reg_time.length()-3));
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ib_background:
                    if(BroadcastAdapter.this.type == BROADCAST_FAV_LIST_1) {
                        rl_control.setVisibility(View.VISIBLE);
                        ib_background.setVisibility(View.GONE);
                    }
                    else {
                        if (BroadcastAdapter.this.m_listner != null) {
                            BroadcastAdapter.this.m_listner.onClickBroadcast(BroadcastAdapter.this.getItem(m_nPos));
                        }
                    }
                    break;
                case R.id.ib_play:
                    if(BroadcastAdapter.this.m_favlistner != null) {
                        BroadcastAdapter.this.m_favlistner.onPlay(BroadcastAdapter.this.getItem(m_nPos));
                    }
                    break;
                case R.id.ib_delete:
                    if(BroadcastAdapter.this.m_favlistner != null) {
                        BroadcastAdapter.this.m_favlistner.onDelete(BroadcastAdapter.this.getItem(m_nPos));
                    }
                    break;
                case R.id.ib_star:
                    if(BroadcastAdapter.this.m_favlistner != null) {
                        BroadcastAdapter.this.m_favlistner.onStar(BroadcastAdapter.this.getItem(m_nPos));
                    }
                    break;
                case R.id.ib_reward:
                    rl_control.setVisibility(View.GONE);
                    ib_background.setVisibility(View.VISIBLE);
                    break;
            }

        }
    }
}