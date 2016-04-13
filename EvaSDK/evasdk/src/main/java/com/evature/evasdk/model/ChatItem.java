package com.evature.evasdk.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;

import com.evature.evasdk.model.appmodel.AppSearchModel;
import com.evature.evasdk.util.DLog;

/****
 * The model for a chat row  - to be shown on screen
 */
public class ChatItem  implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public enum ChatType {
		User,
		Eva,
		EvaWelcome,
		MultiChoiceQuestion,
		MultiChoiceAnswer
	}
	final static ChatType[] chatTypeValues = ChatType.values();
	
	public enum Status {
		NONE,
		EDITING,
		SEARCHING,
		HAS_RESULTS
	}
	
	protected transient Spannable chat;
	private   String subLabel; // will be shown below the text in muted color
	private   ChatType chatType;
	private   Status status;
	private   String evaReplyId;
	private AppSearchModel searchModel;
	private boolean alreadyAnimated = false;
	

	public ChatItem(String chat) {
		this(new SpannableString(chat));
	}
	
	public ChatItem(String chat, String evaReplyId, ChatType chatType) {
		this(chat == null? null : new SpannableString(chat), evaReplyId, chatType);
	}
	
	public ChatItem(SpannableString chat) {
		this(chat, null, ChatType.User);
	}
	
	public ChatItem(SpannableString chat, String evaReplyId, ChatType chatType) {
		this.chat = chat;
		//this.flow = flow;
		this.evaReplyId = evaReplyId;
		this.chatType = chatType;
		setStatus(Status.NONE);
	}

	public Spannable getChat() {
		return chat;
	}
	
	public void setChat(Spannable chat) {
		this.chat = chat;
	}
	public void setChat(String chat) {
		this.chat = new SpannableString(chat);
	}

	public void setSubLabel(String text) {
		this.subLabel = text;
	}
	
	public String getSubLabel() {
		return subLabel;
	}
	
	public ChatType getType() {
		return chatType;
	}
	
	private static final String TAG = "ChatItem";

	public String getEvaReplyId() {
		return evaReplyId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		// default serialization for everything except the SpannableString chat 
		oos.defaultWriteObject();
		// write the chat as html
		if (chat == null) {
			chat = new SpannableString("");
		}
		String html = Html.toHtml(chat);
		// Html module converts "chat..." to "<p>chat...</p>\n" when serializing,
		// and the <p> element is converted to "chat...\n\n" when deserializing -
		// this makes the ChatItem show empty space below the text after restore -
		// trimming the restored string isn't simple because it is a "spannedString"
		// solution - remove the <p> wrapping before 
		html = html.replaceAll("<p[^>]*?>(.*?)</p>", "$1");
		oos.writeObject(html);
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		// default deserialization for everything except the SpannableString
		ois.defaultReadObject();
		// load the chat from html string
		String html = (String) ois.readObject();
		DLog.i(TAG, "Restoring: " + html);
		chat = new SpannableString(Html.fromHtml(html)); //(SpannableString) Html.fromHtml(html);
	}

	@Override
	public String toString() {
		String chatTypeStr = "";
		String string = chatType.name();
		for (int i=0; i<string.length(); i++) {
			char charAt = string.charAt(i);
			if (charAt >= 'A' && charAt <= 'Z') {
				chatTypeStr += charAt;
			}
		}
		String statusStr = "";
		string = status.name();
		for (int i=0; i<string.length(); i++) {
			char charAt = string.charAt(i);
			if (charAt >= 'A' && charAt <= 'Z') {
				statusStr += charAt;
			}
		}
		return chatTypeStr +": "+statusStr+": "+chat;
	}
	

	public void setAlreadyAnimated() {
		alreadyAnimated  = true;
	}
	public void clearAlreadyAnimated() {
		alreadyAnimated  = false;
	}
	public boolean wasAlreadyAnimated() {
		return alreadyAnimated;
	}

	public AppSearchModel getSearchModel() {
		return searchModel;
	}
	public void setSearchModel(AppSearchModel searchModel) {
		this.searchModel = searchModel;
	}

}
