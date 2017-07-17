package com.evature.exampleapp;

import android.os.Bundle;
import android.util.Log;

import com.evature.evasdk.EvaChatTrigger;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


public class EvaGcmListenerService extends FirebaseMessagingService {
    private final static String TAG = "MsgListener";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        Map<String,String> data = remoteMessage.getData();
        String rid = data.get("rid");
        try {
            int index = Integer.parseInt(data.get("index"));
            String streamingResult = data.get("streaming_result");
            String isFinalStr = data.get("is_final");
            boolean isFinal = false;
            if (isFinalStr != null) {
                isFinal = Boolean.parseBoolean(isFinalStr);
            }

            Log.v(TAG, "streaming_result: " + streamingResult +"  index: "+index+ " is_final: "+isFinal+" from: "+from);
            EvaChatTrigger.notifyPartialTranscription(this, streamingResult, index, rid, isFinal);
        }
        catch(NumberFormatException e) {
            Log.e(TAG, "Error parsing index '"+data.get("index")+"'", e);
        }
    }
}
