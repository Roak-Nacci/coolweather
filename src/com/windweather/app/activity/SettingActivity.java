package com.windweather.app.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.windlweather.app.R;

public class SettingActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.setting_layout);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		int i = bundle.getInt("item_number");
        String item = getResources().getStringArray(R.array.menu_item)[i];
		TextView text = (TextView) findViewById(R.id.menu_item);
		text.setText(item);
	}
	
}
