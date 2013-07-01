package com.evature.search.controllers.events;

import com.evature.search.models.chat.ChatItem;

public class ChatItemClicked {

	public ChatItem chatItem;
	
	public ChatItemClicked(ChatItem item) {
		chatItem = item;
	}

}
