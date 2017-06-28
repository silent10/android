package com.evature.exampleapp;

import android.os.Bundle;
import android.util.Log;

import com.evature.evasdk.EvaChatTrigger;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONArray;
import org.json.JSONException;


public class EvaGcmListenerService extends GcmListenerService {
    private final static String TAG = "GCMListener";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "From: " + from);

        String rid = data.getString("rid");
        try {
            int index = Integer.parseInt(data.getString("index"));
            String streamingResult = data.getString("streaming_result");
            boolean isFinal = Boolean.parseBoolean(data.getString("is_final", "false"));
            Log.d(TAG, "streaming_result: " + streamingResult +"  index: "+index+ " is_final: "+isFinal);
            EvaChatTrigger.notifyPartialTranscription(this, streamingResult, index, rid, isFinal);
        }
        catch(NumberFormatException e) {
            Log.e(TAG, "Error parsing index '"+data.getString("index")+"'", e);
        }
    }
}
