package com.evature.search.models.chat;

import android.text.SpannableString;

import com.evaapis.EvaApiReply;
import com.evaapis.flow.FlowElement;


public class ChatItem {//implements Parcelable { // http://stackoverflow.com/a/2141166/78234
	
	public enum ChatType {
		Me,
		Eva,
		DialogQuestion,
		DialogAnswer
	}
	
	static ChatItem lastActivated = null;
	
	protected SpannableString chat = new SpannableString("");
	protected ChatType chatType;
	protected boolean inSession = true;
	
	protected FlowElement flow = null;
	protected EvaApiReply evaReply = null;

	public ChatItem(String chat) {
		this(new SpannableString(chat));
	}
	
	public ChatItem(String chat, EvaApiReply evaReply, FlowElement flow, ChatType chatType) {
		this(new SpannableString(chat), evaReply, flow, chatType);
	}
	
	public ChatItem(SpannableString chat) {
		this.chat = chat;
		chatType = ChatType.Me;
	}
	
	public ChatItem(SpannableString chat, EvaApiReply evaReply, FlowElement flow, ChatType chatType) {
		this.chat = chat;
		this.flow = flow;
		this.evaReply = evaReply;
		this.chatType = chatType;
	}

	public SpannableString getChat() {
		return chat;
	}
	
	public void setChat(SpannableString chat) {
		this.chat = chat;
	}

	
	public ChatType getType() {
		return chatType;
	}

//	@Override
//	public int describeContents() {
//		return 0;
//	}

//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeByte((byte) getType().ordinal());
//		dest.writeByte((byte) (inSession ? 1 : 0));
//		dest.writeString(chat.);
//	}
//
//	// this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
//	public static final Parcelable.Creator<ChatItem> CREATOR = new Parcelable.Creator<ChatItem>() {
//		public ChatItem createFromParcel(Parcel in) {
//			return new ChatItem(in);
//		}
//
//		public ChatItem[] newArray(int size) {
//			return new ChatItem[size];
//		}
//	};
	
	final static ChatType[] chatTypeValues = ChatType.values();

	// example constructor that takes a Parcel and gives you an object populated with it's values
//	private ChatItem(Parcel in) {
//		super();
//		chatType = chatTypeValues[in.readByte()];
//		inSession = in.readByte() == 1;
//		chat = in.readString();
//	}

	public void setInSession(boolean inSession) {
		this.inSession = inSession;
	}

	public boolean isInSession() {
		return inSession;
	}

	public FlowElement getFlowElement() {
		return flow;
	}

	public EvaApiReply getEvaReply() {
		return evaReply;
	}
}
