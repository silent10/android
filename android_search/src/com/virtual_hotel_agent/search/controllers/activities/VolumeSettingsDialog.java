/*****
 * BugReport Dialog - based on ACRA's Crash Report Dialog  (org.acra.CrashReportDialog)
 */
package com.virtual_hotel_agent.search.controllers.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.evature.util.Log;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.util.VolumeUtil;
import com.virtual_hotel_agent.search.util.VolumeUtil.VolumeListener;

public class VolumeSettingsDialog extends Activity 
	implements 
		VolumeListener,
		DialogInterface.OnDismissListener,
		DialogInterface.OnClickListener,
		OnSeekBarChangeListener
{
	protected static final String TAG = "VolumeSettingsDialog";
	AlertDialog mDialog;
	private View volumeWarning;
	private ImageView volumeWarningIcon;
	private SeekBar volumeSeekBar;
	private RadioButton bluetoothOption;
	private ImageView volumeIcon;
	private RadioButton headphonesOption;


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("Volume Settings");
		
		View volumeSettingsView = View.inflate(this, R.layout.volume_settings_dialog, null);
		volumeSeekBar = (SeekBar) volumeSettingsView.findViewById(R.id.seekBar_volume);
		bluetoothOption = (RadioButton) volumeSettingsView.findViewById(R.id.radioButton_bluetooth);
		headphonesOption = (RadioButton) volumeSettingsView.findViewById(R.id.radioButton_headphones);
		volumeIcon = (ImageView) volumeSettingsView.findViewById(R.id.volume_icon);
		
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		bluetoothOption.setEnabled(bluetoothAdapter != null && bluetoothAdapter.isEnabled());

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		VolumeUtil.checkVolume(this);
		volumeSeekBar.setMax(VolumeUtil.maxVolume);
		volumeSeekBar.setProgress(VolumeUtil.volume);
		volumeSeekBar.setOnSeekBarChangeListener( this);
		
		volumeWarning = volumeSettingsView.findViewById(R.id.volume_warning_panel);
		volumeWarningIcon = (ImageView) volumeSettingsView.findViewById(R.id.imageView_volume_warning);
		updateView();
		
		dialogBuilder.setView(volumeSettingsView);
		dialogBuilder.setPositiveButton(R.string._ok, this);
		this.mDialog = dialogBuilder.create();
		this.mDialog.setOnDismissListener(this);
		this.mDialog.setOwnerActivity(this);

		this.mDialog.setCanceledOnTouchOutside(true);
		this.mDialog.show();
	}
	
	@Override 
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		VolumeUtil.register(this, this);
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		VolumeUtil.unregister(this);
		super.onPause();
	}

	
	public void onDismiss(DialogInterface dialog) {
		finish();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		
		VolumeUtil.setVolume(this, seekBar.getProgress());
		updateView();
	}

	private void updateView() {
		volumeSeekBar.setMax(VolumeUtil.maxVolume);
		volumeSeekBar.setProgress(VolumeUtil.volume);

		if (VolumeUtil.isLowVolume()) {
			volumeWarning.setVisibility(View.VISIBLE);
			volumeWarningIcon.setImageResource(VolumeUtil.getVolumeIcon());
		}
		else {
			volumeWarning.setVisibility(View.GONE);
		}
		
		bluetoothOption.setSelected(VolumeUtil.isBluetooth());
		
		if (VolumeUtil.isHeadphone()) {
			headphonesOption.setEnabled(true);
			headphonesOption.setChecked(true);
		}
		
		volumeIcon.setImageResource(VolumeUtil.getVolumeIconNoWarning());
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		finish();
	}

	@Override
	public void onVolumeChange() {
		VolumeUtil.checkVolume(this);
		updateView();
	}

}
