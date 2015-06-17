package com.evature.evasdk.evaapis;


import com.evature.evasdk.evaapis.crossplatform.EvaApiReply;

public interface EvaSearchReplyListener {
	void onEvaReply(EvaApiReply reply, Object cookie);
	void onEvaError(String message, EvaApiReply reply, boolean isServerError, Object cookie);
	void newSessionStarted(boolean selfTriggered);
}
