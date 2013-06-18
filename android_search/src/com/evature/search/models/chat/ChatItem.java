package com.evature.search.models.chat;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatItem implements Parcelable { // http://stackoverflow.com/a/2141166/78234
	
	public static final int CHAT_ME = 0;
	public static final int CHAT_EVA = 1;
	public static final int CHAT_DIALOG = 2;
	public static final int CHAT_CHOICE = 3;
	public static final int CHAT_TYPES = 4;
	
	protected String chat = "";
	protected int chatType;

	public ChatItem(String chat) {
		this.chat = chat;
		chatType = CHAT_ME;
	}
	
	public ChatItem(String chat, int chatType) {
		this.chat = chat;
		this.chatType = chatType;
	}

	public String getChat() {
		return chat;
	}
	
	public void setChat(String chat) {
		this.chat = chat;
	}

	
	public int getType() {
		return chatType;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) getType());
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

	// example constructor that takes a Parcel and gives you an object populated with it's values
	private ChatItem(Parcel in) {
		super();
		chatType = in.readByte();
		chat = in.readString();
	}
}
