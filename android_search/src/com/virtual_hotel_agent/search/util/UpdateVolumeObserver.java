package com.virtual_hotel_agent.search.util;

import com.evature.util.DLog;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;


public class UpdateVolumeObserver  extends ContentObserver {
    private static final String TAG = "UpdateVolumeObserver";
	int previousVolume;
    Context context;

    public UpdateVolumeObserver(Context c, Handler handler) {
        super(handler);
        context=c;

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

        int delta=previousVolume-currentVolume;

        if(delta>0)
        {
            DLog.d(TAG, "Decreased");
            previousVolume=currentVolume;
        }
        else if(delta<0)
        {
            DLog.d(TAG, "Increased");
            previousVolume=currentVolume;
        }
    }
}