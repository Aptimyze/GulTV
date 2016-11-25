package com.yj.wangjatv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yj.wangjatv.R;
import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.model.BroadcastCategory;
import com.yj.wangjatv.model.ChatMessage;

import java.util.List;

/**
 * Created by Ralph on 6/6/2016.
 */
public class ChattingAdapter extends ArrayAdapter<ChatMessage> {
    public interface IListItemClickListener {
        void OnClickItem(int idx);
        void OnClickForce(int idx);
    }

    public static final int CHATTING_LIST_TYPE_LIVE = 0; // a
    public static final int CHATTING_LIST_TYPE_DIALOG = 1;
    public static final int CHATTING_LIST_TYPE_BROADCAST = 2;
    private IListItemClickListener m_itemListener;

    private LayoutInflater m_inflater;

    public int m_nSelected = -1;
    public int m_nListType = 0; // 0:activity 1:dialog

    public ChattingAdapter(Context context,
                                    List<ChatMessage> items,
                                    IListItemClickListener listener, int list_type) {
        super(context, 0, items);
        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        m_itemListener = listener;
        this.m_nListType = list_type;
    }

    public void setItemClickListener(IListItemClickListener listener) {
        this.m_itemListener = listener;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemHolder holder;

        if (row == null) {
            row = m_inflater.inflate(R.layout.item_chatting_list, parent,
                    false);
            holder = new ItemHolder(row, position);
            row.setTag(holder);
        } else {
            holder = (ItemHolder) row.getTag();
        }

        holder.showData(position);

        return row;
    }

    private class ItemHolder implements View.OnClickListener {
        private TextView m_tvTitle;
        private TextView m_tvContent;
        private Button  m_btnForceExit;
        private Button  m_btnBj;

        private int m_nPos;

        public ItemHolder(View v, int pos) {
            m_tvTitle = (TextView) v.findViewById(R.id.tv_user_name);
            m_tvContent = (TextView) v.findViewById(R.id.tv_message);
            m_btnForceExit = (Button) v.findViewById(R.id.btn_force_exit);
            m_btnBj = (Button)v.findViewById(R.id.btn_bj);

            v.setOnClickListener(this);
            m_btnForceExit.setOnClickListener(this);
        }

        public void showData(int pos) {
            m_nPos = pos;
            ChatMessage item = getItem(m_nPos);

            int mainColor = R.color.Black;
            int []arr_color = {R.color.White, R.color.green, R.color.green, R.color.green, R.color.blue, R.color.yellow};

            if(item.user_grade >= 0 && item.user_grade <= arr_color.length) {
                mainColor = arr_color[item.user_grade];
            }

            m_btnForceExit.setVisibility(View.GONE);

            if(m_nListType == CHATTING_LIST_TYPE_BROADCAST) {
                mainColor = R.color.White;

                WangjaTVApp app = WangjaTVApp.getInstance();
                if(item.user_no != app.getUserInfo().user_no) {
                    m_btnForceExit.setVisibility(View.VISIBLE);
                }
                else {
                    m_btnForceExit.setVisibility(View.GONE);
                }
            }
            else if(m_nListType == CHATTING_LIST_TYPE_LIVE) {
                mainColor = R.color.Black;
            }
            else if(m_nListType == CHATTING_LIST_TYPE_DIALOG) {
                mainColor = R.color.White;
            }

            if(item.forced_exit == 1) {
                mainColor = R.color.colorAccent;
            }

            if(item.is_bj == 1) {
                m_btnBj.setVisibility(View.VISIBLE);
            }
            else {
                m_btnBj.setVisibility(View.GONE);
            }

            m_tvTitle.setTextColor( getContext().getResources().getColor(mainColor));
            m_tvContent.setTextColor(getContext().getResources().getColor(mainColor));
            m_tvTitle.setText(item.user_name);
            m_tvContent.setText(item.content);
        }

        @Override
        public void onClick(View v) {
            if(m_itemListener != null) {
                if(v.getId() == R.id.btn_force_exit) {
                    m_itemListener.OnClickForce(m_nPos);
                }
                else {
                    m_itemListener.OnClickItem(m_nPos);
                }
            }
        }
    }
}

