package com.evature.search.models.chat;

import android.os.Parcel;
import android.os.Parcelable;


public class ChatItem implements Parcelable { // http://stackoverflow.com/a/2141166/78234
	
	public enum ChatType {
		Me,
		Eva,
		DialogQuestion,
		DialogAnswer
	}	
	
	protected String chat = "";
	protected ChatType chatType;
	protected boolean activated = false;

	public ChatItem(String chat) {
		this.chat = chat;
		chatType = ChatType.Me;
	}
	
	public ChatItem(String chat, ChatType chatType) {
		this.chat = chat;
		this.chatType = chatType;
	}

	public String getChat() {
		return chat;
	}
	
	public void setChat(String chat) {
		this.chat = chat;
	}

	
	public ChatType getType() {
		return chatType;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) getType().ordinal());
		dest.writeString(chat);
	}

	// this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
	public static final Parcelable.Creator<ChatItem> CREATOR = new Parcelable.Creator<ChatItem>() {
		public ChatItem createFromParcel(Parcel in) {
			return new ChatItem(in);
		}

		public ChatItem[] newArray(int size) {
			return new ChatItem[size];
		}
	};
	
	final static ChatType[] chatTypeValues = ChatType.values();

	// example constructor that takes a Parcel and gives you an object populated with it's values
	private ChatItem(Parcel in) {
		super();
		chatType = chatTypeValues[in.readByte()];
		chat = in.readString();
	}

	public void setActivated(boolean isActivated) {
		activated = isActivated;
	}

	public boolean isActivated() {
		return activated;
	}
}
