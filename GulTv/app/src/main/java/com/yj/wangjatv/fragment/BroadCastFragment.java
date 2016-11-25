package com.yj.wangjatv.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.yj.wangjatv.R;
import com.yj.wangjatv.WangjaTVApp;
import com.yj.wangjatv.activity.BroadcastActivity;
import com.yj.wangjatv.adapter.BroadcastCategoryAdapter;
import com.yj.wangjatv.dialog.BroadcastCategorySelectDialog;
import com.yj.wangjatv.dialog.GetPhotoDialog;
import com.yj.wangjatv.dialog.OneMsgTwoBtnDialog;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.model.Broadcast;
import com.yj.wangjatv.model.BroadcastCategory;
import com.yj.wangjatv.utils.CommonUtil;
import com.yj.wangjatv.utils.SelectPhotoManager;
import com.yj.wangjatv.utils.StringFilter;
import com.yj.wangjatv.widget.AnyEditTextView;
import com.yj.wangjatv.widget.AnyTextView;

import java.io.File;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 5/21/2016.
 */
public class BroadCastFragment extends BaseFragment implements View.OnClickListener {

    Button[]btn_vidoe_qualities = new Button[3];
    Button []btn_vidoe_types = new Button[4];

    Button btn_select_category = null;
    ImageButton ib_allow_adult = null;
    ImageView iv_allow_adult = null;

    AnyTextView tv_user_name = null;
    AnyEditTextView etv_title = null;
    AnyEditTextView etv_viewer_count = null;
    AnyEditTextView etv_passwd = null;
    AnyEditTextView etv_heart = null;
    AnyTextView etv_grade = null;
    NetworkImageView niv_preview = null;
    ImageView iv_preview = null;

    View ll_fan_grade = null;
    View ll_heart_cnt = null;
    View ll_password = null;
    View ll_count = null;
    BroadcastCategory m_pSelectedCategory = null;

    Broadcast m_pBroadCast = new Broadcast();

    int m_nSelectedGrade = -1;
    CreateChatRoomTask m_asyncTask;

    boolean m_isCreatedBroadcast = false;

    // photo upload
    private SelectPhotoManager m_photoManger = null;
    private Bitmap m_uploadBitmap = null;
    private File m_uploadImageFile = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_broadcast, container, false);
        setView(view, inflater, container);

        StringFilter.setCharacterLimited(getActivity(), etv_title, StringFilter.ALLOW_NUMERIC_HANGUL_SPECIAL);
        // set default
        selectVideoQuality(Broadcast.VIDEO_QUALITY_HIGH);
        selectBroadCastType(Broadcast.LIVE_TYPE_FREE);

        // set data
        tv_user_name.setText(mApplicationContext.getUserInfo().user_name);
        etv_viewer_count.setText(String.format("%d", 300));

        getMyLastLive();
        return view;
    }

    public static BroadCastFragment newInstance() {
        BroadCastFragment fragment = new BroadCastFragment();
        return fragment;
    }

    public void init(View v) {
        super.init(v);

        btn_vidoe_qualities[0] = (Button)v.findViewById(R.id.btn_tap_high_video_quality);
        btn_vidoe_qualities[1] = (Button)v.findViewById(R.id.btn_tap_normal_video_quality);
        btn_vidoe_qualities[2] = (Button)v.findViewById(R.id.btn_tap_low_video_quality);
        ib_allow_adult = (ImageButton)v.findViewById(R.id.ib_allow_adult);
        iv_allow_adult = (ImageView)v.findViewById(R.id.iv_allow_adult);
        btn_vidoe_types[0] = (Button)v.findViewById(R.id.btn_tap_broadcast_type_1);
        btn_vidoe_types[1] = (Button)v.findViewById(R.id.btn_tap_broadcast_type_2);
        btn_vidoe_types[2] = (Button)v.findViewById(R.id.btn_tap_broadcast_type_3);
        btn_vidoe_types[3] = (Button)v.findViewById(R.id.btn_tap_broadcast_type_4);

        btn_select_category = (Button)v.findViewById(R.id.btn_select_category);

        v.findViewById(R.id.ib_allow_adult).setOnClickListener(this);
        v.findViewById(R.id.btn_broadcast).setOnClickListener(this);

        btn_select_category.setOnClickListener(this);
        btn_vidoe_qualities[0].setOnClickListener(this);
        btn_vidoe_qualities[1].setOnClickListener(this);
        btn_vidoe_qualities[2].setOnClickListener(this);

        btn_vidoe_types[0].setOnClickListener(this);
        btn_vidoe_types[1].setOnClickListener(this);
        btn_vidoe_types[2].setOnClickListener(this);
        btn_vidoe_types[3].setOnClickListener(this);

        tv_user_name = (AnyTextView)v.findViewById(R.id.tv_user_name);
        etv_title = (AnyEditTextView)v.findViewById(R.id.etv_title);
        etv_viewer_count = (AnyEditTextView)v.findViewById(R.id.etv_viewer_count);
        etv_passwd = (AnyEditTextView)v.findViewById(R.id.etv_passwd);
        etv_heart = (AnyEditTextView)v.findViewById(R.id.etv_heart);

        ll_fan_grade = v.findViewById(R.id.ll_fan_grade);
        ll_heart_cnt = v.findViewById(R.id.ll_heart_cnt);
        ll_password = v.findViewById(R.id.ll_password);
        ll_count = v.findViewById(R.id.ll_count);
        etv_grade = (AnyTextView)v.findViewById(R.id.tv_grade);
        etv_grade.setOnClickListener(this);

        niv_preview = (NetworkImageView)v.findViewById(R.id.niv_preview);
        iv_preview = (ImageView)v.findViewById(R.id.iv_preview);

        v.findViewById(R.id.btn_take_photo).setOnClickListener(this);
        v.findViewById(R.id.btn_remove_photo).setOnClickListener(this);

        niv_preview.setDefaultImageResId(R.drawable.bg_preview_default);
        niv_preview.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_category:
            {
                final BroadcastCategorySelectDialog dialog = new BroadcastCategorySelectDialog(getActivity());
                dialog.setListener(new OneMsgTwoBtnDialog.OneMsgTwoBtnDialogListner() {
                    @Override
                    public void onClickYes() {
                        BroadcastCategory category = dialog.getSelectedCategory();
                        if(category != null) {
                            m_pSelectedCategory = category;
                            btn_select_category.setText(m_pSelectedCategory.name);
                        }
                    }

                    @Override
                    public void onClickNo() {

                    }
                });
                dialog.show();
            }
                break;
            case R.id.btn_broadcast: {

                    if(m_pSelectedCategory == null) {
                        CommonUtil.showCenterToast(getActivity(), R.string.select_broadcast_category, Toast.LENGTH_SHORT);
                        return;
                    }

                    if(etv_title.getText().toString().isEmpty() == true) {
                        CommonUtil.showCenterToast(getActivity(), R.string.input_broadcast_title, Toast.LENGTH_SHORT);
                        return;
                    }

                    Broadcast broadcast = m_pBroadCast;
                    broadcast.title = etv_title.getText().toString();
                    if(ib_allow_adult.isSelected() == false) {
                        broadcast.allow_adult = 0;
                    }
                    else {
                        broadcast.allow_adult = 1;
                    }
                    broadcast.max_viewer_cnt = Integer.parseInt(etv_viewer_count.getText().toString());
                    for(int i = 0; i < btn_vidoe_qualities.length; i++) {
                        if(btn_vidoe_qualities[i].isSelected() == true) {
                            broadcast.video_quality = i;
                            break;
                        }
                    }
                    broadcast.lock_password = etv_passwd.getText().toString();

                    for(int i = 0; i < btn_vidoe_types.length; i++) {
                        if(btn_vidoe_types[i].isSelected() == true) {
                            broadcast.type = i;
                            break;
                        }
                    }

                    broadcast.category_no = m_pSelectedCategory.no;

                    if(broadcast.type == Broadcast.LIVE_TYPE_MONEY || broadcast.type == Broadcast.LIVE_TYPE_ONE) {
                        try {
                            broadcast.heart_cnt = Integer.parseInt(etv_heart.getText().toString());
                        } catch (Exception e) {
                            broadcast.heart_cnt = 0;
                        }

                        if(broadcast.heart_cnt == 0) {
                            CommonUtil.showCenterToast(getActivity(), R.string.input_heart_cnt, Toast.LENGTH_SHORT);
                            return;
                        }
                    }
                    else {
                        broadcast.heart_cnt = 0;
                    }

                    if(broadcast.type == Broadcast.LIVE_TYPE_FAN) {
                        broadcast.user_grade = m_nSelectedGrade;
                    }
                    else {
                        broadcast.user_grade = -1;
                    }
                    broadcast.user_no = getApplicationContext().getUserInfo().user_no;

                    m_pBroadCast = broadcast;
                    createBroadcast(broadcast);
                }
            break;
            case R.id.ib_allow_adult:
                ib_allow_adult.setSelected(!ib_allow_adult.isSelected());
                iv_allow_adult.setSelected(ib_allow_adult.isSelected());
                break;
            case R.id.btn_tap_high_video_quality:
                selectVideoQuality(0);
                break;
            case R.id.btn_tap_normal_video_quality:
                selectVideoQuality(1);
                break;
            case R.id.btn_tap_low_video_quality:
                selectVideoQuality(2);
                break;
            case R.id.btn_tap_broadcast_type_1:
                selectBroadCastType(0);
                break;
            case R.id.btn_tap_broadcast_type_2:
                selectBroadCastType(1);
                break;
            case R.id.btn_tap_broadcast_type_3:
                selectBroadCastType(2);
                break;
            case R.id.btn_tap_broadcast_type_4:
                selectBroadCastType(3);
                break;
            case R.id.tv_grade: {
                final String []arr_fan = {"브론즈", "실버", "골드", "다이아", "VIP"};
                CommonUtil.showSelectDialog(getActivity(), arr_fan, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_nSelectedGrade = which;
                        etv_grade.setText(arr_fan[which]);
                    }
                });
            }
                break;
            case R.id.btn_take_photo: {
                showPhotoDialog();
            }
            break;
            case R.id.btn_remove_photo: {
                if(m_uploadBitmap != null) {
                    m_uploadBitmap = null;
                    m_uploadImageFile = null;
                    iv_preview.setVisibility(View.GONE);
                    niv_preview.setVisibility(View.VISIBLE);
                    niv_preview.setDefaultImageResId(R.drawable.bg_preview_default);
                }
                else {
                    deleteBroadcastThumb();
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SelectPhotoManager.CROP_FROM_CAMERA
                || requestCode == SelectPhotoManager.PICK_FROM_CAMERA
                || requestCode == SelectPhotoManager.PICK_FROM_FILE) {
            m_photoManger.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showPhotoDialog() {
        // upload photo dialog
        m_photoManger = new SelectPhotoManager(getActivity());
        m_photoManger.setPhotoSelectCallback(new SelectPhotoManager.PhotoSelectCallback() {
            @Override
            public void onSelectImageDone(Bitmap image, File file) {
                if (image != null && file != null) {
                    m_uploadImageFile = file;
                    if (m_uploadBitmap != null)
                        m_uploadBitmap.recycle();
                    m_uploadBitmap = image;
                    iv_preview.setVisibility(View.VISIBLE);
                    iv_preview.setImageBitmap(m_uploadBitmap);
                    niv_preview.setVisibility(View.GONE);
                } else {
                    m_uploadImageFile = null;
                    if (m_uploadBitmap != null)
                        m_uploadBitmap.recycle();
                    m_uploadBitmap = null;
                }
            }

            @Override
            public void onFailedSelectImage(int errorCode, String err) {

            }

            @Override
            public void onDeleteImage() {

            }
        });
        new GetPhotoDialog(getActivity(), new GetPhotoDialog.GetPhotoDialogListener() {
            @Override
            public void onAlbum() {
                m_photoManger.doPickFromGallery();
            }

            @Override
            public void onTake() {
                m_photoManger.doTakePicture();
            }

            @Override
            public void  onCancel() {

            }

        }).show();
    }

    private void selectVideoQuality(int type) {

        for(int i = 0; i < btn_vidoe_qualities.length; i++) {
            btn_vidoe_qualities[i].setSelected(false);
        }

        if(type >= 0 && type < btn_vidoe_qualities.length) {
            btn_vidoe_qualities[type].setSelected(true);
        }
    }

    private void selectBroadCastType(int type) {
        for(int i = 0; i < btn_vidoe_types.length; i++) {
            btn_vidoe_types[i].setSelected(false);
        }
        if(type >= 0 && type < btn_vidoe_types.length) {
            btn_vidoe_types[type].setSelected(true);
            ll_count.setVisibility(View.VISIBLE);
            ll_fan_grade.setVisibility(View.GONE);
            ll_heart_cnt.setVisibility(View.GONE);
            switch (type) {
                case Broadcast.LIVE_TYPE_FREE:
                    ll_fan_grade.setVisibility(View.GONE);
                    ll_heart_cnt.setVisibility(View.GONE);
                    break;
                case Broadcast.LIVE_TYPE_MONEY:
                    ll_heart_cnt.setVisibility(View.VISIBLE);
                    ll_fan_grade.setVisibility(View.GONE);
                    break;
                case Broadcast.LIVE_TYPE_FAN:
                    ll_heart_cnt.setVisibility(View.GONE);
                    ll_fan_grade.setVisibility(View.VISIBLE);
                    break;
                case Broadcast.LIVE_TYPE_ONE:
                    ll_heart_cnt.setVisibility(View.VISIBLE);
                    ll_count.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void getMyLastLive() {
        final APIProvider server = new APIProvider();
        WangjaTVApp app = getApplicationContext();
        APIProvider.showWaitingDlg(getActivity());
        server.getMyLastLive(app, app.getUserInfo().user_no, new retrofit2.Callback<Broadcast>() {
            @Override
            public void onResponse(Response<Broadcast> response) {
                Broadcast model = response.body();
                APIProvider.hideProgress();
                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        m_pBroadCast = model;
                        showBroadcast();
                    } else {
                        CommonUtil.showCenterToast(getActivity(), model.msg, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, getActivity());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, getActivity());
            }
        });
    }

    private void deleteBroadcastThumb() {
        if(m_pBroadCast == null || m_pBroadCast.no == -1) {
            return;
        }

        final APIProvider server = new APIProvider();

        APIProvider.showWaitingDlg(getActivity());
        server.deleteBroadcastThumb(getActivity(), m_pBroadCast.no, m_pBroadCast.thumb_url, new retrofit2.Callback<BaseModel>() {
            @Override
            public void onResponse(Response<BaseModel> response) {
                BaseModel model = response.body();
                APIProvider.hideProgress();
                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        niv_preview.setDefaultImageResId(R.drawable.bg_preview_default);
                        m_pBroadCast.thumb_url = "";
                        CommonUtil.showCenterToast(getActivity(), R.string.success_remove_thumb, Toast.LENGTH_SHORT);
                    } else {
                        CommonUtil.showCenterToast(getActivity(), model.msg, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, getActivity());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, getActivity());
            }
        });
    }

    private void showBroadcast() {
        if(m_pBroadCast == null || m_pBroadCast.no == -1) {
            return;
        }

        etv_title.setText(m_pBroadCast.title);
        etv_viewer_count.setText(String.format("%d", m_pBroadCast.max_viewer_cnt));
        selectVideoQuality(m_pBroadCast.video_quality);
        if(m_pBroadCast.allow_adult == 0) {
            ib_allow_adult.setSelected(false);
            iv_allow_adult.setSelected(false);
        }
        else {
            ib_allow_adult.setSelected(true);
            iv_allow_adult.setSelected(true);
        }


        btn_select_category.setText(m_pBroadCast.category_name);
        selectBroadCastType(m_pBroadCast.type);

        m_pSelectedCategory = new BroadcastCategory();
        m_pSelectedCategory.m_bSelected = true;
        m_pSelectedCategory.no = m_pBroadCast.category_no;
        m_pSelectedCategory.name = m_pBroadCast.category_name;

        if(m_pBroadCast.thumb_url != null && m_pBroadCast.thumb_url.isEmpty() == false) {
            niv_preview.setImageUrl(m_pBroadCast.thumb_url, getApplicationContext().getImageLoader());
        }
        else {
            niv_preview.setDefaultImageResId(R.drawable.bg_preview_default);
        }
    }

    private void uploadPhotoFileImage(File file) {
        if(m_pBroadCast == null || m_pBroadCast.no == -1) {
            return;
        }

        if(file == null){
            startBroadcastActivity(m_pBroadCast);
            return;
        }

        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(getActivity());
        server.uploadBroadcastThumb(getActivity(), m_pBroadCast.no, getApplicationContext().getUserInfo().user_no, file, new Callback<BaseModel>() {
            @Override
            public void onResponse(Response<BaseModel> response) {
                BaseModel model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {
                        startBroadcastActivity(m_pBroadCast);
                    } else {
                        CommonUtil.showCenterToast(getActivity(), R.string.failed_upload, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, getActivity());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, getActivity());
            }
        });
    }


    private void createBroadcast(final Broadcast broadcast) {
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(getActivity());
        server.insertBroadcast(getActivity(), broadcast, new Callback<Broadcast>() {
            @Override
            public void onResponse(Response<Broadcast> response) {
                Broadcast model = response.body();

                APIProvider.hideProgress();

                if (model != null) {
                    if (model.status == BaseModel.OK_DATA) {

                        broadcast.no = model.no;
                        broadcast.reg_time = model.reg_time;

                        if(model.type == 1) {   //insert
                            m_isCreatedBroadcast = true;
                        }
                        else {
                            m_isCreatedBroadcast = false;
                        }

                        uploadPhotoFileImage(m_uploadImageFile);

                    } else {
                        CommonUtil.showCenterToast(getActivity(), R.string.failed_broadcast, Toast.LENGTH_SHORT);
                    }

                } else {
                    server.processErrorBody(response, getActivity());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                APIProvider.hideProgress();
                server.processServerFailure(t, getActivity());
            }
        });
    }

    private void startBroadcastActivity(Broadcast broadcast) {

        if(m_isCreatedBroadcast == true) {
            m_asyncTask = new CreateChatRoomTask();
            m_asyncTask.execute(broadcast.no);
        }
        else {
            Intent intent = new Intent(getActivity(), BroadcastActivity.class);
            intent.putExtra(KEY_INTENT_BROADCAST, broadcast);
            startActivity(intent);
        }
    }

    public class CreateChatRoomTask extends AsyncTask<Integer, Void, Boolean> {
        private static final String TAG = "CreateChatRoomTask";
        private int m_nChatRoomNo;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            APIProvider.showWaitingDlg(getActivity());
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            boolean result = true;
            m_nChatRoomNo = integers[0];

            if (result) {
                result =  BroadCastFragment.this.getApplicationContext().getXmppEndPoint().createRoom(m_nChatRoomNo);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            APIProvider.hideProgress();
            if (result) {
                //updateCreatedRoomFlag(m_nChatRoomNo);
                m_isCreatedBroadcast = false;
                startBroadcastActivity(m_pBroadCast);
            } else {
                CommonUtil.showCenterToast(getActivity(), R.string.failed_create_chat_room, Toast.LENGTH_SHORT);
            }
        }
    }
}
