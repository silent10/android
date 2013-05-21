package com.evature.search.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatItem implements Parcelable { // http://stackoverflow.com/a/2141166/78234
	private String chat = "";
	private boolean eva = false;

	public ChatItem(String chat, boolean eva) {
		this.chat = chat;
		this.eva = eva;
	}

	public String getChat() {
		return chat;
	}

	public boolean isEva() {
		return eva;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(chat);
		dest.writeByte((byte) (eva ? 1 : 0)); // http://stackoverflow.com/a/7089687/78234
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
		chat = in.readString();
		eva = in.readByte() == 1;
	}
}
