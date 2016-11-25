package com.yj.wangjatv.dialog;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;

import com.yj.wangjatv.R;

public class GetPhotoDialog extends Dialog implements View.OnClickListener {

    private GetPhotoDialogListener m_listener;

    public GetPhotoDialog(Context context, GetPhotoDialogListener listener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.dialog_get_photo);

        m_listener = listener;

        findViewById(R.id.bt_album).setOnClickListener(this);
        findViewById(R.id.bt_take).setOnClickListener(this);
        findViewById(R.id.bt_cancel).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(m_listener != null) {
            m_listener.onCancel();
        }
    }

    /**
     * Show the gallery
     */
    private void onAlbum() {

    }

    /**
     * Show the camera
     */
    private void onTake() {

    }

    public void onClick(View v) {
        dismiss();

        switch (v.getId()) {
            case R.id.bt_album:
                m_listener.onAlbum();
                break;
            case R.id.bt_take:
                m_listener.onTake();
                break;
            case R.id.bt_cancel:
                m_listener.onCancel();
                break;
        }
    }

    public interface GetPhotoDialogListener {
        void onAlbum();

        void onTake();

        void onCancel();
    }
}
