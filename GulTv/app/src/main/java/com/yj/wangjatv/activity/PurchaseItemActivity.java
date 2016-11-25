package com.yj.wangjatv.activity;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

import com.example.android.trivialdrivesample.util.IabHelper;
import com.android.vending.billing.IInAppBillingService;
import com.example.android.trivialdrivesample.util.IabResult;
import com.yj.wangjatv.R;
import com.yj.wangjatv.http.APIProvider;
import com.yj.wangjatv.model.Agreement;
import com.yj.wangjatv.model.BaseModel;
import com.yj.wangjatv.utils.CommonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Ralph on 5/26/2016.
 */
public class PurchaseItemActivity extends BaseActivity implements View.OnClickListener{

    private static final String IAB_PURCHASE_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgQolN8YPMQPiZyBEjPuxBtVDsjNlR8TAcfAiaVOzGGOy2OC3NatqYwtsPoeo5YxPBJT+gY5qlbitSeagmhqQdn4iPgFM1wKbntxO+qiKxrm4qok25UN8xsylI7DI+82csZPCuMIkUQMIt7B0JNyek08O3nXfwh9bQ9IJPkSumz5pvnTLDtZSD6U78KnJjqk+biIX8FVqMS2RFka1vvpx230rCV5Ka2dAbmb58kBKnaBGRHWi1eYcC683vqDhPop2nDjPIPbUg1EeDMBIjSYJLGKH8AEzQyHem1Qm6Tcn1n/fEYtD8ptEVYZOQ2iBh13GKHRMLZ5Cn28Ug1seCgVcDwIDAQAB";
    IabHelper mHelper;
    IInAppBillingService mService;
    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_item);

        //
        // Google Play Billing 초기화.
        //
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        String base64EncodedPublicKey = IAB_PURCHASE_PUBLIC_KEY;

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(false);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Toast.makeText(PurchaseItemActivity.this, "구매중 오류가 발생하였습니다.", Toast.LENGTH_LONG).show();
                }

                //
                // 결제하였으나 포인트받지 못한 내역을 처리.
                //
                consumeAll();
            }
        });
    }

    @Override
    public void init() {
        super.init();

        findViewById(R.id.ll_heart_1).setOnClickListener(this);
        findViewById(R.id.ll_heart_2).setOnClickListener(this);
        findViewById(R.id.ll_heart_3).setOnClickListener(this);
        findViewById(R.id.ll_heart_4).setOnClickListener(this);
        findViewById(R.id.ll_heart_5).setOnClickListener(this);
        findViewById(R.id.ll_heart_6).setOnClickListener(this);

        findViewById(R.id.ll_ticket_1).setOnClickListener(this);
        findViewById(R.id.ll_ticket_2).setOnClickListener(this);
        findViewById(R.id.ll_ticket_3).setOnClickListener(this);
        findViewById(R.id.ll_ticket_4).setOnClickListener(this);
        findViewById(R.id.ll_ticket_5).setOnClickListener(this);
        findViewById(R.id.ll_ticket_6).setOnClickListener(this);

        findViewById(R.id.ib_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.ll_heart_1:
                boughtItem(0,1);
                break;
            case R.id.ll_heart_2:
                boughtItem(0,2);
                break;
            case R.id.ll_heart_3:
                boughtItem(0,3);
                break;
            case R.id.ll_heart_4:
                boughtItem(0,4);
                break;
            case R.id.ll_heart_5:
                boughtItem(0,5);
                break;
            case R.id.ll_heart_6:
                boughtItem(0,6);
                break;
            case R.id.ll_ticket_1:
                boughtItem(1,1);
                break;
            case R.id.ll_ticket_2:
                boughtItem(1,2);
                break;
            case R.id.ll_ticket_3:
                boughtItem(1, 3);
                break;
            case R.id.ll_ticket_4:
                boughtItem(1,4);
                break;
            case R.id.ll_ticket_5:
                boughtItem(1,5);
                break;
            case R.id.ll_ticket_6:
                boughtItem(1,6);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            final String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                boughtItem(purchaseData, dataSignature);
            }
        }
    }

    // type: 0 하트 1 티켓
    private void boughtItem(final int type, final int index) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String w_strItemId = "item_" +type+ "_" + index;

                try {
                    Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), w_strItemId, "inapp", String.valueOf(index));
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Toast.makeText(PurchaseItemActivity.this, "GooglePlay에서 하트 또는 티켓 구매조작에 실패하였습니다.", Toast.LENGTH_LONG).show();
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    Toast.makeText(PurchaseItemActivity.this, "GooglePlay에 하트 또는 티켓 구매를 요청하는데 실패하였습니다.", Toast.LENGTH_LONG).show();
                }
            }
        }).start();
    }

    private void boughtItem(String data,String sig) {
        final APIProvider server = new APIProvider();
        APIProvider.showWaitingDlg(this);
        try {
            JSONObject jo = new JSONObject(data);
            String sku = jo.getString("productId");
            final String w_strPurchaseToken = jo.getString("purchaseToken");

            server.boughtItem(this, m_app.getUserInfo().user_no, data, sig, new Callback<BaseModel>() {
                @Override
                public void onResponse(Response<BaseModel> response) {
                    BaseModel model = response.body();

                    APIProvider.hideProgress();

                    if (model != null) {
                        if (model.status == BaseModel.OK_DATA) {
                            CommonUtil.showCenterToast( PurchaseItemActivity.this, "구매했습니다.", Toast.LENGTH_SHORT);
                            // 구입한 항목을 Consume하기.
                            //
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mService.consumePurchase(3, getPackageName(), w_strPurchaseToken);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                        Toast.makeText(PurchaseItemActivity.this, "구매한 항목을 소비하는데 실패하였습니다.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }).start();
                        } else {
                            CommonUtil.showCenterToast( PurchaseItemActivity.this, model.msg, Toast.LENGTH_SHORT);
                        }
                    } else {
                        server.processErrorBody(response, PurchaseItemActivity.this);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    APIProvider.hideProgress();
                    server.processServerFailure(t, PurchaseItemActivity.this);
                }
            });
        } catch (JSONException e) {
            Toast.makeText(this, "구매결과를 해석하는데 실패하였습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    //
    // 모든 Iab항목들을 컨슘.
    //
    private void consumeAll() {
        Bundle ownedItems = null;
        try {
            ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        int response = ownedItems.getInt("RESPONSE_CODE");
        if (response == 0) {
            ArrayList<String> ownedSkus =
                    ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
            ArrayList<String> purchaseDataList =
                    ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
            ArrayList<String> signatureList =
                    ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");

            for (int i = 0; i < purchaseDataList.size(); ++i) {
                String purchaseData = purchaseDataList.get(i);

                if (i >= signatureList.size())      // 예외처리.
                    continue;
                ;
                String signature = signatureList.get(i);
                boughtItem(purchaseData, signature);
            }
        }
    }
}

