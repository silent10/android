package com.evature.android_demo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.evaapis.android.EvaBaseActivity;
import com.evaapis.android.EvaComponent;
import com.evaapis.crossplatform.EvaApiReply;
import com.evaapis.crossplatform.EvaWarning;
import com.evature.util.Log;

public class MainActivity extends EvaBaseActivity implements OnSharedPreferenceChangeListener {
	
	// default values  - can be overwritten by preferences
	private static final String API_KEY = "e7567517-0a1b-4a89-bd56-1b18915353f9";
	private static final String SITE_CODE = "androiddev";
	private static final String TAG = "MainActivity";
	
	// GUI elements
	TextView responseText;
	EditText freeText;
	Button recordButton;
	Button startOverButton;
	Button searchText;

	public class LanguageDetailsChecker extends BroadcastReceiver
	{
	    private List<String> supportedLanguages;

	    private String languagePreference;

	    @Override
	    public void onReceive(Context context, Intent intent)
	    {
	        Bundle results = getResultExtras(true);
	        String text = "";
	        if (results.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE))
	        {
	            languagePreference =
	                    results.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE);
	            text += "Prefrence: "+languagePreference +"\n";
	        }
	        if (results.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES))
	        {
	            supportedLanguages =
	                    results.getStringArrayList(
	                            RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);
	            text += "Supported Languages:\n";
	            for (String supported: supportedLanguages) {
	            	text += " - "+supported+"\n";
	            }
	        }
            responseText.setText(text);
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.DEBUG = BuildConfig.DEBUG;
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPreferences.getString(EvaComponent.VRSERV_PREF_KEY, "-").equals("-")) {
			Editor edit = sharedPreferences.edit();
			edit.putString(EvaComponent.VRSERV_PREF_KEY, "google_local");
			edit.apply();
		}
		super.onCreate(savedInstanceState);
		eva.setSiteCode(sharedPreferences.getString("eva_site_code", SITE_CODE));
		eva.setApiKey(sharedPreferences.getString("eva_key", API_KEY));
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		eva.registerPreferenceListener();
		setContentView(R.layout.activity_main);
		
		startOverButton = (Button) findViewById(R.id.button_new_session);
		
		recordButton = (Button) findViewById(R.id.button_start);
        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if ("google_local".equals(eva.getVrService())) {
        			//eva.searchWithLocalVoiceRecognition(4, null);
            		eva.searchWithLocalVoiceRecognitionCustomDialog(4, null);
        			return;
        		}
            	
            	searchWithVoice("voice");
            }
        });
        
        freeText = (EditText)findViewById(R.id.editText_free_text);
        searchText = (Button) findViewById(R.id.button_search_text);
        searchText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchWithText(freeText.getText().toString(), "text", false);
				freeText.setText("");
			}
		});
        
        startOverButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetSession();
			}
		});
        responseText = (TextView) findViewById(R.id.textView_response_text);
        
        Intent detailsIntent =  new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
        sendOrderedBroadcast(
                detailsIntent, null, new LanguageDetailsChecker(), null, Activity.RESULT_OK, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onEvaError(String message, boolean isServerError, Object cookie) {
		Toast.makeText(this, "Error: "+message, Toast.LENGTH_LONG).show();
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) { // user pressed the menu button
		switch (item.getItemId()) {
			case R.id.action_settings: // Did the user select "settings"?
				startActivity(new Intent(this, DemoPreferences.class));
				return true;
		}
		return false;
	}

	Pattern sayitPatten = Pattern.compile("\"SayIt\": \"(.*?)[^\\\\]\"", Pattern.MULTILINE);
	Pattern processedTextPatten = Pattern.compile("\"ProcessedText\": \"(.*?)[^\\\\]\"", Pattern.MULTILINE);
	
	@Override
	public void onEvaReply(EvaApiReply reply, Object cookie) {
		try {
			startOverButton.setVisibility(View.VISIBLE);
			String replyStr = reply.JSONReply.toString(2);
//			Log.d(TAG, "Reply is: "+replyStr);
			SpannableString replySpan = new SpannableString(replyStr);
			
			Resources resources = getResources();
			// highlight sayit
			int col = resources.getColor(R.color.sayit);
			Matcher matcher = sayitPatten.matcher(replyStr);
			while (matcher.find()) {
//				Log.d(TAG, "Found sayit: "+matcher.start(1) + "-" + (matcher.end(1)+1));
				replySpan.setSpan( new ForegroundColorSpan(col), matcher.start(1), matcher.end(1)+1, 0);
			}
			
			// highlight processed text and warnings
			col = resources.getColor(R.color.procssed);
			int warn = resources.getColor(R.color.warn);
			matcher = processedTextPatten.matcher(replyStr);
			while (matcher.find()) {
				replySpan.setSpan( new ForegroundColorSpan(col), matcher.start(1), matcher.end(1)+1, 0);
				
				// highlight warnings - parts of the processed text not parsed
				for (EvaWarning warning: reply.evaWarnings) {
					if (warning.position == -1) {
						continue;
					}
					int start = matcher.start(1)+warning.position;
					int end = matcher.start(1)+warning.position+warning.text.length();
//					Log.d(TAG, "Found warning: "+start + "-" + (end+1));
					replySpan.setSpan( new ForegroundColorSpan(warn), start, end, 0);
					replySpan.setSpan( new StyleSpan(Typeface.ITALIC), start, end, 0);
				}
			}
			
			
			responseText.setText(replySpan);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void newSessionStarted(boolean selfTriggered) {
		startOverButton.setVisibility(View.INVISIBLE);
		responseText.setText("New session started");
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		eva.setSiteCode(sharedPreferences.getString("eva_site_code", SITE_CODE));
		eva.setApiKey(sharedPreferences.getString("eva_key", API_KEY));
		
		String language = sharedPreferences.getString("eva_language", "");
		if (language.equals("")) {
			language = "en";
		}
		String locale = sharedPreferences.getString("eva_locale", "");
		if (locale.equals("")) {
			locale = "US";
		}
		eva.setPreferedLanguage(language);
		eva.setLocale(locale);
	}

	

}
