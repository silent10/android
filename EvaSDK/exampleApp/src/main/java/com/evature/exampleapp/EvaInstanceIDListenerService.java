package com.evature.exampleapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.evature.evasdk.appinterface.EvaAppSetup;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.iid.FirebaseInstanceId;


import static com.evature.evasdk.EvaChatTrigger.notifyGcmTokenRefreshed;


public class EvaInstanceIDListenerService extends FirebaseInstanceIdService {
    private static final String TAG = "EvaInstanceIDListnrSvc";


    @Override
    public void onTokenRefresh() {

        refreshToken(this);
    }

    public static void refreshToken(final Context context) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String token = FirebaseInstanceId.getInstance().getToken();
                return token;
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