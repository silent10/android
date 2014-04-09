package com.evaapis.android;

import com.evaapis.crossplatform.EvaApiReply;

public interface EvaSearchReplyListener {
	void onEvaReply(EvaApiReply reply, Object cookie);
	void onEvaError(String message, boolean isServerError, Object cookie);
	void newSessionStarted(boolean selfTriggered);
}
