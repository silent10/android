package com.evaapis;

public interface EvaSearchReplyListener {
	void onEvaReply(EvaApiReply reply, Object cookie);
	void newSessionStarted(boolean selfTriggered);
}
