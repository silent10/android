package com.evature.evasdk.evaapis.crossplatform;

import com.evature.evasdk.util.DLog;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class EvaChat  implements Serializable {

	private static final String TAG = "EvaChat";

	public Boolean hello = null;
	public Boolean yes = null;
	public Boolean no = null;
	public Boolean meaningOfLife = null;
	public Boolean who = null;
	public String name = null;
	public boolean newSession;

	public EvaChat(JSONObject jChat, List<String> parseErrors) {
		DLog.d(TAG, "CTOR");
		try {
			if (jChat.has("Hello"))
				hello = jChat.getBoolean("Hello");
			if (jChat.has("Yes"))
				yes = jChat.getBoolean("Yes");
			if (jChat.has("No"))
				no = jChat.getBoolean("No");
			if (jChat.has("Meaning of Life"))
				meaningOfLife = jChat.getBoolean("Meaning of Life");
			if (jChat.has("Who/What"))
				who = jChat.getBoolean("Who/What");
			if (jChat.has("Name"))
				name = jChat.getString("Name");
			
			newSession = jChat.optBoolean("New Session", false);
		} catch (JSONException e) {
			DLog.e(TAG, "Parse JSON", e);
			parseErrors.add("Error during parsing eva chat: "+e.getMessage());
		}
	}
}
