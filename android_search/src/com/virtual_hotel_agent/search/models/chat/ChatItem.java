package com.virtual_hotel_agent.search.models.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.evaapis.crossplatform.EvaApiReply;
import com.evaapis.crossplatform.flow.FlowElement;
import com.evature.util.DLog;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.util.ErrorSpan;


public class ChatItem  implements Serializable { // http://stackoverflow.com/a/2141166/78234
	
	private static final long serialVersionUID = 1L;


	public enum ChatType {
		Me,
		VirtualAgentWelcome,
		VirtualAgent,
		VirtualAgentContinued,
		VirtualAgentError,
		DialogQuestion,
		DialogAnswer
	}
	
	public enum Status {
		None,
		InEdit,
		ToSearch,
		InSearch,
		HasResults
	}

	private static final String TAG = "ChatItem";
	
//	static ChatItem lastActivated = null;
	
	protected transient SpannableString chat = new SpannableString("");
	protected ChatType chatType;
	protected boolean inSession = true;
	
	private Status status;
	
	protected FlowElement flow;
	protected EvaApiReply evaReply;
	public boolean sayitActivated = false;

	public ChatItem(String chat) {
		this(new SpannableString(chat));
	}
	
	public ChatItem(String chat, EvaApiReply evaReply, FlowElement flow, ChatType chatType) {
		this(new SpannableString(chat), evaReply, flow, chatType);
	}
	
	public ChatItem(SpannableString chat) {
		this(chat, null, null, ChatType.Me);
	}
	
	public ChatItem(SpannableString chat, EvaApiReply evaReply, FlowElement flow, ChatType chatType) {
		this.chat = chat;
		this.flow = flow;
		this.evaReply = evaReply;
		this.chatType = chatType;
		if (chatType == ChatType.VirtualAgent && flow != null && flow.Type != FlowElement.TypeEnum.Question) {
			setStatus(Status.ToSearch);
		}
		else {
			setStatus(Status.None);
		}
	}


	private void writeObject(ObjectOutputStream oos) throws IOException {
		// default serialization for everything except the SpannableString chat 
		oos.defaultWriteObject();

		// save the chat
		SpannableStringBuilder logText = new SpannableStringBuilder(chat);
		oos.writeUTF(logText.toString());
		Object[] spans = logText.getSpans(0, logText.length(), Object.class);
		oos.writeInt((int)spans.length);
	    for (int i = 0; i < spans.length; i++){
	    	Object span = spans[i];
	    	int start = logText.getSpanStart(spans[i]);
	    	int end = logText.getSpanEnd(spans[i]);

	    	oos.writeUTF(span.getClass().getName());
	    	oos.writeInt(start);
	    	oos.writeInt(end);
	    	if (span instanceof ForegroundColorSpan) {
	    		oos.writeInt(((ForegroundColorSpan)span).getForegroundColor());
	    	}
	    	else if (span instanceof StyleSpan) {
	    		oos.writeInt(((StyleSpan)span).getStyle());
	    	}
	    }
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		// default deserialization for everything except the SpannableString
		ois.defaultReadObject();

		// load the chat
		String chatText = ois.readUTF();
	    chat = new SpannableString(chatText);
	    int numSpans = ois.readInt();
	    for (int i = 0; i < numSpans; i++){
	    	
	    	String className = ois.readUTF();
	        int start = ois.readInt();
	        int end = ois.readInt();
	        Class<?> clazz = Class.forName(className);
	        if (clazz.equals(ForegroundColorSpan.class)) {
	        	chat.setSpan(new ForegroundColorSpan(ois.readInt()), start, end, 0);
	        }
	        else if (clazz.equals(StyleSpan.class)) {
	        	chat.setSpan(new StyleSpan(ois.readInt()), start, end, 0);
	        }
	        else {
	        	try {
					chat.setSpan(clazz.getConstructor().newInstance(), start, end, 0);
				} catch (InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException e) {
					DLog.e(TAG, "Failed to create span "+className+"  ["+start+"-"+end+"]");
				}
	        }
	        	
	    }
	}


	
	public SpannableString getChat() {
		return chat;
	}
	
	public void setChat(SpannableString chat) {
		this.chat = chat;
	}
	public void setChat(String chat) {
		this.chat = new SpannableString(chat);
	}

	
	public ChatType getType() {
		return chatType;
	}

	
	final static ChatType[] chatTypeValues = ChatType.values();
	public static final String START_NEW_SESSION = "Start new search";

	
//	public void setInSession(boolean inSession) {
//		this.inSession = inSession;
//	}
//
//	public boolean isInSession() {
//		return inSession;
//	}

	public FlowElement getFlowElement() {
		return flow;
	}

	public EvaApiReply getEvaReply() {
		return evaReply;
	}
	
	public void setApiReply(EvaApiReply reply) {
		evaReply = reply;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
//		if (status != Status.HasResults ) {
//			mResults = null;
//		}
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

//	public void setSearchResults(JSONObject result) {
//		mResults = result;
//	}
//
//	public JSONObject getSearchResult() {
//		return mResults;
//	}
}
