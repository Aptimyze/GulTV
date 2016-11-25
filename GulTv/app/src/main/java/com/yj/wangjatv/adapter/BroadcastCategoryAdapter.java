package com.yj.wangjatv.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yj.wangjatv.R;
import com.yj.wangjatv.model.BroadcastCategory;

import java.util.List;
import android.content.Context;

/**
 * Created by Ralph on 5/26/2016.
 */

public class BroadcastCategoryAdapter extends ArrayAdapter<BroadcastCategory> {

    public interface IListItemClickListener {
        void OnClickItem(int idx);
    }

    private IListItemClickListener m_itemListener;

    private LayoutInflater m_inflater;

    public int m_nSelected = -1;

    public BroadcastCategoryAdapter(Context context,
                           List<BroadcastCategory> items,
                           IListItemClickListener listener) {
        super(context, 0, items);
        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        m_itemListener = listener;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemHolder holder;

        if (row == null) {
            row = m_inflater.inflate(R.layout.item_broadcast_category, parent,
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
        private ImageView m_ivSelect;

        private int m_nPos;

        public ItemHolder(View v, int pos) {
            m_tvTitle = (TextView) v.findViewById(R.id.tv_favorite_item);
            m_ivSelect = (ImageView) v.findViewById(R.id.iv_favorite_item);

            v.findViewById(R.id.ib_favorite_item).setOnClickListener(this);
        }

        public void showData(int pos) {
            m_nPos = pos;
            BroadcastCategory item = getItem(m_nPos);
            int []red_drawable = {R.drawable.ic_category_talk_red, R.drawable.ic_category_sport_red, R.drawable.ic_category_music_red, R.drawable.ic_category_drama_red, R.drawable.ic_category_game_red, R.drawable.ic_category_19_red, R.drawable.ic_category_ani_red, R.drawable.ic_category_other_red};
            int []gray_drawable = {R.drawable.ic_category_talk_gray, R.drawable.ic_category_sport_gray, R.drawable.ic_category_music_gray, R.drawable.ic_category_drama_gray, R.drawable.ic_category_game_gray, R.drawable.ic_category_19_gray, R.drawable.ic_category_ani_gray, R.drawable.ic_category_other_gray};
            if(m_nPos < 0 || m_nPos >= red_drawable.length) {
                m_ivSelect.setVisibility(View.GONE);
            }
            else {
                m_ivSelect.setImageResource(item.m_bSelected ? red_drawable[m_nPos] : gray_drawable[m_nPos]);
            }

            m_tvTitle.setText(item.name);
            m_tvTitle.setTextColor(item.m_bSelected ? getContext().getResources().getColor(R.color.colorAccent) : getContext().getResources().getColor(R.color.gray_323232));
        }

        @Override
        public void onClick(View v) {
            m_itemListener.OnClickItem(m_nPos);
        }
    }

}