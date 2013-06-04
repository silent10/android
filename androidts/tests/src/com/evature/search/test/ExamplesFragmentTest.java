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

import android.support.v4.view.ViewPager;
import android.widget.ListView;
import android.widget.TextView;

import com.evature.search.R;
import com.evature.search.controllers.activities.MainActivity;
import com.evature.search.controllers.activities.MainActivity.SwipeyTabsPagerAdapter;
import com.evature.search.views.fragments.ExamplesFragment;

@RunWith(RobolectricTestRunner.class)
public class ExamplesFragmentTest {
	
	ExamplesFragment examplesFragment;
	
	@Before
    public void setup() {
		ShadowLog.stream = System.out;
		MainActivity activity = Robolectric.buildActivity(MainActivity.class).create().get();

//		examplesFragment = new ExamplesFragment();
//
//        FragmentManager fragmentManager = activity.getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.add(examplesFragment, null);
//        fragmentTransaction.commit();
		ViewPager viewPager = (ViewPager) activity.findViewById(R.id.viewpager);
        
    	SwipeyTabsPagerAdapter adapter = (SwipeyTabsPagerAdapter) viewPager.getAdapter();
		
    	examplesFragment = (ExamplesFragment) adapter.instantiateItem(viewPager, 1);
		
		
        assertThat(examplesFragment, notNullValue());
        assertThat(examplesFragment.getActivity(), notNullValue());
        
    }

	@Test
	public void testExamples() {
		

        ListView examplesListView = (ListView) examplesFragment.getView().findViewById(R.id.examples_list);

        // ListViews only create as many children as will fit in their bounds, so make it big...
        examplesListView.layout(0, 0, 100, 1000);

        TextView nameRow = (TextView) examplesListView.getChildAt(1);
        assertEquals(nameRow.getText().toString(), "3 Star hotels in NYC");
		

		// show examples tab
//    	viewPager.setCurrentItem(1);
//		View examplesView = viewPager.getChildAt(1);
//		ListView examplesListView = ((ListView) examplesView.findViewById(R.id.examples_list));
		// click example #2  (position 1)
		final TextView exampleView = (TextView)examplesListView.getAdapter().getView(1, null, null);
		assertEquals("3 Star hotels in NYC", exampleView.getText());
//		activity.runOnUiThread(new Runnable() {
//		    public void run() {
				exampleView.performClick();
//		    }
//		  });
		
		// wait for button click to be handled
//		try {
//			int slept = 0;
//			while (chatListModel.getItemList().size() == 0 && slept < 200) {
//				slept++;
//				Thread.sleep(100);
//			}
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}

		// should have one item in chat
//		assertEquals(1, chatListModel.getItemList().size());
//		assertEquals(chatListModel.getItemList().get(0), "3 Star hotels in NYC"); // TODO: shouldn't be hard coded here
//		
//		// wait for Eva to return
//		try {
//			Thread.sleep(2500);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		assertEquals(2, chatListModel.getItemList().size());
	}
    

}
