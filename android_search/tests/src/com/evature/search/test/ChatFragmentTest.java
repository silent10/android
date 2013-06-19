package com.evature.search.test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDrawable;
import org.robolectric.shadows.ShadowLog;

import roboguice.RoboGuice;
import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.RoboInjector;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.evature.search.R;
import com.evature.search.controllers.activities.MainActivity;
import com.evature.search.controllers.activities.MainActivity.SwipeyTabsPagerAdapter;
import com.evature.search.models.chat.ChatItem;
import com.evature.search.models.chat.ChatItemList;
import com.evature.search.models.chat.ChatItem.ChatType;
import com.evature.search.views.fragments.ChatFragment;
import com.google.inject.Inject;

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
		
		startFragment(mChatFragment);
		
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
		assertEquals(0, mChatListModel.getItemList().size());

		mChatFragment.addChatItem(new ChatItem("Test 1", ChatType.Eva));
		
		assertEquals(1, mChatListModel.getItemList().size());
		assertEquals("Test 1", mChatListModel.getItemList().get(0).getChat());
		assertEquals(true, mChatListModel.getItemList().get(0).getType() == ChatType.Eva);
	}
}
