package com.giggs.apps.chaos.billing;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import com.android.vending.billing.IInAppBillingService;
import com.giggs.apps.chaos.activities.interfaces.OnBillingServiceConnectedListener;
import com.giggs.apps.chaos.game.data.ArmiesData;

public class InAppBillingHelper {

    private static final int BILLING_API_VERSION = 3;
    private static final String IN_APP_PURCHASE_TYPE = "inapp";
    public static final int BILLING_REQUEST_CODE = 3000;

    private Activity mActivity;
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;

    public InAppBillingHelper(Activity activity, final OnBillingServiceConnectedListener callback) {
        this.mActivity = activity;
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
                callback.onBillingServiceConnected();
            }
        };
        mActivity.bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), mServiceConn,
                Context.BIND_AUTO_CREATE);
    }

    public IInAppBillingService getmService() {
        return mService;
    }

    public ServiceConnection getmServiceConn() {
        return mServiceConn;
    }

    public List<Integer> getAvailableArmies(Context context) {
        List<Integer> lstArmiesAvailable = new ArrayList<Integer>();
        lstArmiesAvailable.add(ArmiesData.HUMAN.ordinal());
        lstArmiesAvailable.add(ArmiesData.ORCS.ordinal());
        lstArmiesAvailable.add(ArmiesData.UNDEAD.ordinal());

        try {
            SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            Bundle ownedItems = mService.getPurchases(BILLING_API_VERSION, mActivity.getPackageName(),
                    IN_APP_PURCHASE_TYPE, null);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                for (int i = 0; i < ownedSkus.size(); i++) {
                    String sku = ownedSkus.get(i);
                    if (sku.equals("army_dwarf") || mSharedPrefs.getBoolean("army_dwarf", false)) {
                        lstArmiesAvailable.add(ArmiesData.DWARF.ordinal());
                        if (!mSharedPrefs.getBoolean("army_dwarf", false)) {
                            mSharedPrefs.edit().putBoolean("army_dwarf", true).commit();
                        }
                    } else if (sku.equals("army_chaos") || mSharedPrefs.getBoolean("army_chaos", false)) {
                        lstArmiesAvailable.add(ArmiesData.CHAOS.ordinal());
                        if (!mSharedPrefs.getBoolean("army_chaos", false)) {
                            mSharedPrefs.edit().putBoolean("army_chaos", true).commit();
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return lstArmiesAvailable;
    }

    public void purchaseItem(String productId) {
        try {
            Bundle buyIntentBundle = mService.getBuyIntent(3, mActivity.getPackageName(), productId,
                    IN_APP_PURCHASE_TYPE, "doodidadoodoodida");
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
            mActivity.startIntentSenderForResult(pendingIntent.getIntentSender(), BILLING_REQUEST_CODE, new Intent(),
                    Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (SendIntentException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        if (mServiceConn != null) {
            mActivity.unbindService(mServiceConn);
        }
    }

}
