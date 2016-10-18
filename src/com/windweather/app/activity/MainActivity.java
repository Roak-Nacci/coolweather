package com.windweather.app.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends FragmentActivity {
	
	private ListView mDrawerList;
	
	private DrawerLayout mDrawerLayout;

	private String[] mPlanetTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_main);
    	mDrawerList = (ListView) findViewById(R.id.left_drawer);
    	mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    	mPlanetTitles = getResources().getStringArray(R.array.planet_array);
    	mDrawerList.setAdapter(new ArrayAdapter<String>(this,
    			R.layout.drawer_list_item, mPlanetTitles));
    	mDrawerList.setOnItemClickListener(new OnItemClickListener() {
    		@Override
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			
    			// update the main content by replacing fragments
    			Fragment fragment = new PlanetFragment();
    			Bundle args = new Bundle();
    	        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
    			fragment.setArguments(args);
    			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    			// update selected item and title, then close the drawer
    			mDrawerList.setItemChecked(position, true);
    			setTitle(mPlanetTitles[position]);
    			mDrawerLayout.closeDrawer(mDrawerList);
 		}
    	});
    }
}
