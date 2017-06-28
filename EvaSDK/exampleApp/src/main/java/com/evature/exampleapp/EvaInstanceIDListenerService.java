package com.evature.exampleapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.evature.evasdk.EvaChatTrigger;
import com.evature.evasdk.appinterface.EvaAppSetup;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import java.io.IOException;

import static com.evature.evasdk.EvaChatScreenComponent.TOKEN_REFRESHED_EVENT;
import static com.evature.evasdk.EvaChatTrigger.notifyGcmTokenRefreshed;


public class EvaInstanceIDListenerService extends InstanceIDListenerService {
    private static final String TAG = "EvaInstanceIDListnrSvc";


    @Override
    public void onTokenRefresh() {

        refreshToken(this);
    }

    @Nullable
    public static void refreshToken(final Context context) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                InstanceID instanceID = InstanceID.getInstance(context);
                try {
                    String token = instanceID.getToken(context.getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    return token;
                } catch (IOException e) {
                    Log.e(TAG, "Exception fetching token ", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String token) {
                Log.d(TAG, "GCM token = " + token);
                if (token != null) {
                    EvaAppSetup.gcmToken = token;
                    notifyGcmTokenRefreshed(context, token);
                }
            }
        }.execute(null, null, null);
    }
}