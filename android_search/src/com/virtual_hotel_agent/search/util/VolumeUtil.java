package com.virtual_hotel_agent.search.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

import com.evature.util.Log;
import com.virtual_hotel_agent.search.R;

public class VolumeUtil {

	private static final String TAG = "VolumeUtil";
	
	private static boolean _isBluetooth = false;
	private static boolean _isHeadphone = false;
	private static boolean _isSpeaker = false;
	
	public static int volume;
	public static int maxVolume;
	
	public static interface VolumeListener {
		void onVolumeChange();
	}
	
	private static VolumeListener listener = null;

	public static void checkVolume(Context context) {
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		Log.i(TAG, "Current volume :"+volume+" out of "+maxVolume);
		
		_isBluetooth = audioManager.isBluetoothA2dpOn();
		
		// if (audioManager.isSpeakerphoneOn()) {
		_isSpeaker = !_isBluetooth && !_isHeadphone;
	}

	public static boolean isBluetooth() {
		return _isBluetooth;
	}
	
	public static boolean isHeadphone() {
		return _isHeadphone;
	}
	
	public static void setIsHeadphone(boolean b) {
		_isHeadphone = b;
	}

	public static boolean isSpeaker() {
		return _isSpeaker;
	}

	public static boolean isLowVolume() {
		if (_isBluetooth || _isHeadphone) {
			return volume <= maxVolume / 4;  // earpiece should be at least 25%
		}
		else {
			return volume <= maxVolume / 2;  // speaker should be at least 50%
		}
	}

	public static int getVolumeIcon() {
		if (VolumeUtil.isBluetooth()) {
			Log.i(TAG, "Bluetooth");
			if (VolumeUtil.isLowVolume()) {  
				return R.drawable.bluetooth_warning_icon;
			}
			else {
				return R.drawable.bluetooth_icon;
			}
		} else if (VolumeUtil.isHeadphone()) {
			Log.i(TAG, "Headphones");
			if (VolumeUtil.isLowVolume()) {
				return R.drawable.headphones_warning_icon;
			}
			else {
				return R.drawable.headphones_icon;
			}
		}
		else { 
			Log.i(TAG, "Speaker");
			if (VolumeUtil.isLowVolume()) {
				return R.drawable.speaker_warning_icon;
			}
			else {
				return R.drawable.speaker_icon;
			}
		}
	}
	
	public static int getVolumeIconNoWarning() {
		if (VolumeUtil.isBluetooth()) {
			return R.drawable.bluetooth_icon;
		} else if (VolumeUtil.isHeadphone()) {
			return R.drawable.headphones_icon;
		}
		else { 
			return R.drawable.speaker_icon;
		}
	}

	public static void setVolume(Context context, int volume) {
		VolumeUtil.volume = volume;
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
		
	}

	private static BroadcastReceiver updateHeadsetReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int state = intent.getIntExtra("state", -1);
			Log.d(TAG, "Headset status changed hasHeadphone: "+state);
			VolumeUtil._isHeadphone = state == 1;
			listener.onVolumeChange();
		}
	};
	
	private static ContentObserver updateVolumeContent = new ContentObserver(new Handler()) {
		@Override
	    public void onChange(boolean selfChange) {
			Log.d(TAG, "volume changed selfChange: "+selfChange);
			listener.onVolumeChange();
		}
	};
	
	private static BroadcastReceiver updateBluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Bluetooth status changed");
			listener.onVolumeChange();
		}
	};

	public static void register(Context context, VolumeListener listener) {
		VolumeUtil.listener = listener;
		context.registerReceiver(updateBluetoothReceiver, new IntentFilter(android.bluetooth.BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED));
		context.registerReceiver(updateHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		context.getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, updateVolumeContent );
	}

	public static void unregister(Context context) {
		listener = null;
		context.unregisterReceiver(updateBluetoothReceiver);
		context.unregisterReceiver(updateHeadsetReceiver);
		context.getApplicationContext().getContentResolver().unregisterContentObserver(updateVolumeContent);
	}
	
	
}
