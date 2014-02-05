package com.virtual_hotel_agent.search.controllers.events;

import com.virtual_hotel_agent.search.models.chat.ChatItem;

public class ChatItemClicked {

	public ChatItem chatItem;
	
	public ChatItemClicked(ChatItem item) {
		chatItem = item;
	}

}
