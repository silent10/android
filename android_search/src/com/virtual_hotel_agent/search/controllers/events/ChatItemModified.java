package com.virtual_hotel_agent.search.controllers.events;

import com.virtual_hotel_agent.search.models.chat.ChatItem;

public class ChatItemModified {

	public ChatItem chatItem;
	public boolean startRecord;
	public boolean editLastUtterance;

	/***
	 * if startRecord - this chatItem requests a recording to start - and modify
	 * the chatItem with the results else - this chatItem text was modified if
	 * chatItem is null - last utterance was deleted
	 */
	public ChatItemModified(ChatItem chatItem, boolean startRecord,
			boolean editLastUtterance) {
		this.chatItem = chatItem;
		this.startRecord = startRecord;
		this.editLastUtterance = editLastUtterance;
	}

}
