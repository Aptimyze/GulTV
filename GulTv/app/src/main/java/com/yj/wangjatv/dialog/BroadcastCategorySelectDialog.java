package com.yj.wangjatv.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.yj.wangjatv.Const;
import com.yj.wangjatv.R;
import com.yj.wangjatv.adapter.BroadcastCategoryAdapter;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.model.BroadcastCategory;
import com.yj.wangjatv.model.BroadcastCategoryList;
import com.yj.wangjatv.model.LiveServerInfo;
import com.yj.wangjatv.widget.AnyEditTextView;

import java.util.ArrayList;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 5/26/2016.
 */
public class BroadcastCategorySelectDialog extends BaseDialog implements Const {


    ArrayList<BroadcastCategory> m_arrHobbyItem = new ArrayList<>();

    private OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner mListner;

    GridView m_gvHobby;
    BroadcastCategoryAdapter adapter;

    public BroadcastCategorySelectDialog(Context context) {
        super(context);

        setContentView(R.layout.dialog_select_broadcast_category);
        init();

        getCategories();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    private void init() {
        findViewById(R.id.imagebutton_dialog_onemsg_twobtn_yes).setOnClickListener(button_clicked);
        findViewById(R.id.imagebutton_dialog_onemsg_twobtn_no).setOnClickListener(button_clicked);

        m_gvHobby = (GridView) findViewById(R.id.gv_category);

        adapter = new BroadcastCategoryAdapter(getContext(), m_arrHobbyItem, new BroadcastCategoryAdapter.IListItemClickListener() {
            @Override
            public void OnClickItem(int idx) {
                BroadcastCategory item = adapter.getItem(idx);

                for(int i = 0; i < adapter.getCount();i++) {
                    adapter.getItem(i).m_bSelected = false;
                }
                adapter.m_nSelected = idx;
                item.m_bSelected = true;
                adapter.notifyDataSetChanged();
            }
        });
        m_gvHobby.setAdapter(adapter);

    }

    public void setListener(OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner listner) {
        this.mListner = listner;
    }

    public BroadcastCategory getSelectedCategory() {
        int idx = adapter.m_nSelected;
        if(idx >= 0 && idx <= (adapter.getCount() - 1)) {
            return adapter.getItem(idx);
        }
        return null;
    }


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        dismiss();
    }

    View.OnClickListener button_clicked = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.imagebutton_dialog_onemsg_twobtn_yes:
                    if (mListner != null) {
                        mListner.onClickYes();
                    }
                    dismiss();
                    break;
                case R.id.imagebutton_dialog_onemsg_twobtn_no:

                    if (mListner != null) {
                        mListner.onClickNo();
                    }
                    dismiss();

                    break;
                default:
                    break;
            }
        }
    };

    public void getCategories() {
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this.mContext);
        server.getBroadcastCategoryList(this.mContext, new Callback<BroadcastCategoryList>() {
            @Override
            public void onResponse(Response<BroadcastCategoryList> response) {
                BroadcastCategoryList model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        for(int i = 0; i < model.list.size(); i++) {
                            m_arrHobbyItem.add(model.list.get(i));
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        server.processErrorBody(response, BroadcastCategorySelectDialog.this.mContext);
                    }

                } else {
                    server.processErrorBody(response,BroadcastCategorySelectDialog.this.mContext);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, BroadcastCategorySelectDialog.this.mContext);
            }
        });
    }
}
