package com.virtual_hotel_agent.search.models.chat;

import com.virtual_hotel_agent.search.models.chat.ChatItem.Status;





public class DialogAnswerChatItem extends ChatItem{ // http://stackoverflow.com/a/2141166/78234
	int mIndex = -1;
	private boolean mChosen = false;
	DialogQuestionChatItem mQuestion; 
	
	public DialogAnswerChatItem(DialogQuestionChatItem question, int index, String chat) {
		super(chat, null, null, ChatType.DialogAnswer);
		mIndex = index;
		mQuestion = question;
		mQuestion.addAnswer(this);
	}

	public int getIndex() {
		return mIndex;
	}

	public boolean isChosen() {
		return mChosen;
	}

	public void setChosen() {
		mQuestion.setAnswered();
		this.mChosen = true;
	}

	public DialogQuestionChatItem getQuestion() {
		return mQuestion;
	}
}
