package com.windweather.app.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PlanetFragment extends Fragment {
    public static final String ARG_PLANET_NUMBER = "planet_number";
	private View view;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.planet_layout, container, false);
		int i = getArguments().getInt(ARG_PLANET_NUMBER);
        String planet = getResources().getStringArray(R.array.planet_array)[i];
		TextView text = (TextView) view.findViewById(R.id.planet_title);
		text.setText(planet);
		return view;
	}
	
}
