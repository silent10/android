package com.evature.search.test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDrawable;
import org.robolectric.shadows.ShadowLog;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evature.search.R;
import com.evature.search.models.chat.ChatItem;
import com.evature.search.models.chat.ChatItem.ChatType;
import com.evature.search.models.chat.ChatItemList;
import com.evature.search.views.adapters.ChatAdapter;

@RunWith(RobolectricTestRunner.class)
public class ChatAdapterTest {

	ChatItemList mChatListModel;
	ChatAdapter mAdapter;
	
	@Before
    public void setup() {
		ShadowLog.stream = System.out;

        mChatListModel = new ChatItemList();
        mAdapter = new ChatAdapter(new Activity(), R.layout.row_eva_chat, R.id.label, mChatListModel);
	}
	
	@Test
	public void testView() {
        assertEquals(0, mChatListModel.size());
		
		mAdapter.add(new ChatItem("TEST 111", null, null, ChatType.Eva));
		assertEquals(1, mChatListModel.size());
		
		mAdapter.add(new ChatItem("222 TEST"));
		assertEquals(2, mChatListModel.size());
		
        View child0 = mAdapter.getView(0, null, new LinearLayout(new Activity()));
 		ImageView chatRowIcon = (ImageView) child0.findViewById(R.id.icon);
 		ShadowDrawable shadowChatDrawable = Robolectric.shadowOf(chatRowIcon.getDrawable());
 		assertThat(shadowChatDrawable.getCreatedFromResId(), equalTo(R.drawable.eva_head));
 		assertEquals("TEST 111", ((TextView)child0.findViewById(R.id.label)).getText()); 		
 		
 		View child1 = mAdapter.getView(1, null, new LinearLayout(new Activity()));
 		assertEquals(">", ((TextView)child1.findViewById(R.id.icon)).getText());
 		assertEquals("222 TEST", ((TextView)child1.findViewById(R.id.label)).getText());

	}
}
