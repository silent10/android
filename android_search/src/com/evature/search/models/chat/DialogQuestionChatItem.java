package com.evature.search.models.chat;

import java.util.ArrayList;
import java.util.List;

import com.evaapis.EvaApiReply;
import com.evaapis.flow.FlowElement;



public class DialogQuestionChatItem extends ChatItem{ // http://stackoverflow.com/a/2141166/78234
	private boolean mAnswered = false;
	private List<DialogAnswerChatItem> mAnswers = new ArrayList<DialogAnswerChatItem>();
	
	public DialogQuestionChatItem(String chat, EvaApiReply reply, FlowElement flow) {
		super(chat, reply, flow, ChatType.DialogQuestion);
	}

	public boolean isAnswered() {
		return mAnswered;
	}

	public void setAnswered() {
		mAnswered = true;
	}

	public void addAnswer(DialogAnswerChatItem dialogAnswer) {
		mAnswers.add(dialogAnswer);
	}
	
	public List<DialogAnswerChatItem> getAnswers() {
		return mAnswers;
	}
}
