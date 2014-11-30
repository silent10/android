package com.virtual_hotel_agent.search.models.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.regex.Pattern;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.SpannableString;

import com.evaapis.crossplatform.EvaApiReply;
import com.evaapis.crossplatform.flow.FlowElement;
import com.evature.util.Log;


public class ChatItem  implements Serializable { // http://stackoverflow.com/a/2141166/78234
	
	private static final long serialVersionUID = 1L;


	public enum ChatType {
		Me,
		VirtualAgentWelcome,
		VirtualAgent,
		VirtualAgentContinued,
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

	Pattern removeP = Pattern.compile("^<p[^>]*?>(.*?)</p>$", Pattern.DOTALL);

	private void writeObject(ObjectOutputStream oos) throws IOException {
		// default serialization for everything except the SpannableString chat 
		oos.defaultWriteObject();
		// write the chat as html
		String html = Html.toHtml(chat);
		// Html module converts "chat..." to "<p>chat...</p>\n" when serializing,
		// and the <p> element is converted to "chat...\n\n" when deserializing -
		// this makes the ChatItem show empty space below the text after restore -
		// trimming the restored string isn't simple because it is a "spannedString"
		// solution - remove the <p> wrapping before 
		html = removeP.matcher(html).replaceFirst("$1");
		oos.writeObject(html);
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		// default deserialization for everything except the SpannableString
		ois.defaultReadObject();
		// load the chat from html string
		String html = (String) ois.readObject();
		Log.i(TAG, "Restoring: "+html);
		chat = new SpannableString(Html.fromHtml(html)); //(SpannableString) Html.fromHtml(html);
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
