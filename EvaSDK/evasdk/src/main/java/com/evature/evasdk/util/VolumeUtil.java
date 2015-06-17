package com.evature.evasdk.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

import com.evature.evasdk.R;

public class VolumeUtil {

	private static final String TAG = "VolumeUtil";
	
	private static boolean _isBluetoothEnabled = false;
	private static boolean _isHeadphoneEnabled = false;
	private static boolean _isSpeaker = false;
	
	public enum AudioDevice {
		Speaker,
		Bluetooth,
		Headphone
	};
	
	public static AudioDevice routedToDevice;
	private static boolean forcedAudio = false;
	
	public static int volume;
	public static int maxVolume;
	
	public static interface VolumeListener {
		void onVolumeChange();
	}
	
	public static int currentStream = AudioManager.STREAM_MUSIC;
	private static VolumeListener listener = null;

	public static void checkVolume(Activity context) {
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		volume = audioManager.getStreamVolume(currentStream);
		maxVolume = audioManager.getStreamMaxVolume(currentStream);
		DLog.i(TAG, "Current volume :" + volume + " out of " + maxVolume + "  mode = " + audioManager.getMode() + " stream = " + currentStream);
	 	
		_isBluetoothEnabled = audioManager.isBluetoothA2dpOn();

		if (!forcedAudio) {
			if (_isBluetoothEnabled) {
				routedToDevice = AudioDevice.Bluetooth;
			}
			else if (_isHeadphoneEnabled) {
				routedToDevice = AudioDevice.Headphone;
			}
			else {
				routedToDevice = AudioDevice.Speaker;
			}
		}
	}

	public static boolean isBluetoothEnabled() {
		return _isBluetoothEnabled;
	}
	
	public static boolean isHeadphoneEnabled() {
		return _isHeadphoneEnabled;
	}
	
	public static void setIsHeadphone(boolean b) {
		_isHeadphoneEnabled = b;
	}

	public static boolean isSpeaker() {
		return _isSpeaker;
	}

	public static boolean isLowVolume() {
		if (routedToDevice == AudioDevice.Speaker) {
			return volume <= maxVolume / 2;  // speaker should be at least 50%
		}
		else {
			return volume <= maxVolume / 4;  // earpiece should be at least 25%
		}
	}

	public static int getVolumeIcon() {
		if (routedToDevice == AudioDevice.Bluetooth) {
			DLog.i(TAG, "Bluetooth");
			if (VolumeUtil.isLowVolume()) {  
				return R.drawable.evature_bluetooth_warning_icon;
			}
			else {
				return R.drawable.evature_bluetooth_icon;
			}
		} else if (routedToDevice == AudioDevice.Headphone) {
			DLog.i(TAG, "Headphones");
			if (VolumeUtil.isLowVolume()) {
				return R.drawable.evature_headphones_warning_icon;
			}
			else {
				return R.drawable.evature_headphones_icon;
			}
		}
		else { 
			DLog.i(TAG, "Speaker");
			if (VolumeUtil.isLowVolume()) {
				return R.drawable.evature_speaker_warning_icon;
			}
			else {
				return R.drawable.evature_speaker_icon;
			}
		}
	}
	
	public static int getVolumeIconNoWarning() {
		if (routedToDevice == AudioDevice.Bluetooth) {
			return R.drawable.evature_bluetooth_icon;
		} else if (routedToDevice == AudioDevice.Headphone) {
			return R.drawable.evature_headphones_icon;
		}
		else { 
			return R.drawable.evature_speaker_icon;
		}
	}

	public static void setVolume(Activity context, int volume) {
		VolumeUtil.volume = volume;
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(currentStream, volume, 0);
		
	}

	private static BroadcastReceiver updateHeadsetReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int state = intent.getIntExtra("state", -1);
			DLog.d(TAG, "Headset status changed hasHeadphone: "+state);
			VolumeUtil._isHeadphoneEnabled = state == 1;
			if (listener != null)
				listener.onVolumeChange();
		}
	};
	
	private static ContentObserver updateVolumeContent = new ContentObserver(new Handler()) {
		@Override
	    public void onChange(boolean selfChange) {
			DLog.d(TAG, "volume changed selfChange: "+selfChange);
			if (listener != null)
				listener.onVolumeChange();
		}
	};
	
	private static BroadcastReceiver updateBluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			DLog.d(TAG, "Bluetooth status changed");
			if (listener != null)
				listener.onVolumeChange();
		}
	};

	public static void register(Activity context, VolumeListener listener) {
		VolumeUtil.listener = listener;
		context.setVolumeControlStream(VolumeUtil.currentStream);
//		context.registerReceiver(updateBluetoothReceiver, new IntentFilter(android.bluetooth.BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED));
		context.registerReceiver(updateHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		context.getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, updateVolumeContent );
	}

	public static void unregister(Context context) {
		listener = null;
		//context.unregisterReceiver(updateBluetoothReceiver);
		context.unregisterReceiver(updateHeadsetReceiver);
		context.getApplicationContext().getContentResolver().unregisterContentObserver(updateVolumeContent);
	}
	
	// I failed to force audio to speaker/headphones/bluetooth in a bug free way!  Android is making it ridiculously difficult !
//
//	public  static void setAudioToSpeaker(Activity context) {
//		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//		audioManager.setMode(AudioManager.MODE_NORMAL);
//		audioManager.stopBluetoothSco();
//		audioManager.setBluetoothScoOn(false);
//		audioManager.setSpeakerphoneOn(true);
//		context.setVolumeControlStream(AudioManager.STREAM_MUSIC);
//		currentStream = AudioManager.STREAM_MUSIC;
//		routedToDevice = AudioDevice.Speaker;
//		forcedAudio = true;
//		DLog.d(TAG, "Force Speaker");
//	    checkVolume(context);
//	    if (listener != null) {
//	    	listener.onVolumeChange();
//	    }
//	}
//
//	public static void setAudioToHeadphones(Activity context) {
//		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//		audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//		audioManager.stopBluetoothSco();
//		audioManager.setBluetoothScoOn(false);
//		audioManager.setSpeakerphoneOn(false);
//		context.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//		currentStream = AudioManager.STREAM_VOICE_CALL;
//		routedToDevice = AudioDevice.Headphone;
//		forcedAudio = true;
//		DLog.d(TAG, "Force to Earpiece");
//	    checkVolume(context);
//	    if (listener != null) {
//	    	listener.onVolumeChange();
//	    }
//	}
//	
//	public static void setAudioToBluetooth(Activity context) {
//		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//		audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//		audioManager.startBluetoothSco();
//		audioManager.setBluetoothScoOn(true);
//		context.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//		currentStream = AudioManager.STREAM_VOICE_CALL;
//		routedToDevice = AudioDevice.Bluetooth;
//		forcedAudio = true;
//	    DLog.d(TAG, "Force to Bluetooth");
//	    checkVolume(context);
//	    if (listener != null) {
//	    	listener.onVolumeChange();
//	    }
//	}

	public static boolean isHeadphoneChosen() {
		if (forcedAudio) {
			return routedToDevice == AudioDevice.Headphone;
		}
		// no forced audio
		return isHeadphoneEnabled();
	}

	public static boolean isBluetoothChosen() {
		if (forcedAudio) {
			return routedToDevice == AudioDevice.Bluetooth;
		}
		return isBluetoothEnabled();
	}
	
	
	
}
