package com.evature.search.test;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.ViewPager;

import com.google.inject.Inject;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity.SwipeyTabsPagerAdapter;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItemList;
import com.virtual_hotel_agent.search.models.chat.ChatItem.ChatType;
import com.virtual_hotel_agent.search.views.fragments.ChatFragment;

@RunWith(RobolectricTestRunner.class)
public class ChatFragmentTest {

	@Inject ChatItemList mChatListModel;
//	@Inject ChatFragment mChatFragment;
	ChatFragment mChatFragment;	
	
	MainActivity mActivity;

	@Before
    public void setup() {
		ShadowLog.stream = System.out;

		RoboInjector injector = RoboGuice.getInjector(Robolectric.application);
        injector.injectMembers(this);
        
        mActivity = Robolectric.buildActivity(MainActivity.class).create().start().get();
		ViewPager viewPager = (ViewPager) mActivity.findViewById(R.id.viewpager);
		
    	SwipeyTabsPagerAdapter adapter = (SwipeyTabsPagerAdapter) viewPager.getAdapter();
		
    	mChatFragment = (ChatFragment) adapter.instantiateItem(viewPager, 1);
		FragmentManager.enableDebugLogging(true);
		
		//startFragment(mChatFragment);
		
        assertThat(mChatFragment, notNullValue());
        assertThat(mChatFragment.getActivity(), notNullValue());
        assertThat(mChatFragment.getView(), notNullValue());
	}
	
	public void startFragment( Fragment fragment )
    {
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add( fragment, null );
        fragmentTransaction.commit();
    }
	
	@Test
	public void testChatList() {
		assertEquals(0, mChatListModel.size());

		mChatListModel.add(new ChatItem("Test 1", null, null, ChatType.VirtualAgent));
		
		assertEquals(1, mChatListModel.size());
		assertEquals("Test 1", mChatListModel.get(0).getChat());
		assertEquals(true, mChatListModel.get(0).getType() == ChatType.VirtualAgent);
	}
}
