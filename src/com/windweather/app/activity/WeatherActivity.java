package com.windweather.app.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.windlweather.app.R;
import com.windweather.app.util.HttpCallbackListener;
import com.windweather.app.util.HttpUtil;
import com.windweather.app.util.Utility;

public class WeatherActivity extends FragmentActivity implements OnClickListener, OnItemClickListener{
	
	
	private ListView mDrawerList;
	
	private DrawerLayout mDrawerLayout;

	private String[] mPlanetTitles;

	private Button switchCity;
	
	private Button refreshWeather;

	private LinearLayout weatherInfoLayout;
	
	private RelativeLayout weatherMainLayout;
	
	private TextView cityNameText;

	private TextView publishText;
	
	private TextView weatherDespText;
	
	private TextView temp1Text;
	
	private TextView temp2Text;
	
	private TextView currentDateText;
	
	private Button refreshCityList;
	
	private ImageView weatherImage;
	
	private Time time;
	
	private boolean DAY_NIGHT = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		weatherMainLayout = (RelativeLayout) findViewById(R.id.weather_main_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		publishText = (TextView) findViewById(R.id.publish_text);
		currentDateText = (TextView) findViewById(R.id.current_date);
		weatherImage = (ImageView) findViewById(R.id.weather_image);

		time = new Time();
		
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);		
		refreshWeather.setOnClickListener(this);
		refreshCityList = (Button) findViewById(R.id.refresh_city_list);
		refreshCityList.setOnClickListener(this);
		
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
    	mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    	mPlanetTitles = getResources().getStringArray(R.array.menu_item);
    	mDrawerList.setAdapter(new ArrayAdapter<String>(this,
    			R.layout.drawer_list_item, mPlanetTitles));
    	mDrawerList.setOnItemClickListener(this);
    	

    			
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			publishText.setText("ͬ����...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			showWeather();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, SettingActivity.class);
		Bundle args = new Bundle();
		args.putInt("item_number", position);
		intent.putExtras(args);
		mDrawerList.setItemChecked(position, true);
		setTitle(mPlanetTitles[position]);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("ͬ����...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
		case R.id.refresh_city_list:
			refreshCityList();
			break;
		default:
			break;
		}
	}
	
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city" + countyCode
				+ ".xml";
		queryFromServer(address, "countyCode");
	}
		
	private void queryWeatherInfo(String weatherCode) {
		String httpUrl = "http://apis.baidu.com/apistore/weatherservice/cityid";
		String httpArg = "cityid=" + weatherCode;
		String address = httpUrl + "?" + httpArg;
		queryFromServer(address, "weatherCode");
	}
	
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override 
			public void onFinish(final String response) {
				if ("countyCode".equals(type)){
					if (!TextUtils.isEmpty(response)) {
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}


			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override 
					public void run() {
						publishText.setText("ͬ��ʧ��");
					}
				});
			}
		});
	}
	
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("����" + prefs.getString("publish_time", "") + 
				"  " + prefs.getString("date", "") + "����");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherImage.setImageResource(chooseWeatherImage(prefs));
		time.setToNow();
		DAY_NIGHT = (time.hour <= 18) ;
		changeBackground(DAY_NIGHT);
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);		
	}
	
	private int chooseWeatherImage(SharedPreferences prefs) {
		int imageId = 0;
		if (prefs.getString("weather_desp","").contains("ѩ")) {
			imageId = R.drawable.snow;
		} else if (prefs.getString("weather_desp","").contains("��")) {
			imageId = R.drawable.rain;
		} else if (prefs.getString("weather_desp","").contains("��")) {
			imageId = R.drawable.foggy;
		} else if (prefs.getString("weather_desp","").contains("����")
				&& prefs.getString("weather_desp","").contains("��")) {
			imageId = R.drawable.cloudy;
		} else if (prefs.getString("weather_desp","").contains("��")) {
			imageId = R.drawable.sunny;
		} else {
			imageId = R.drawable.cloudy;
		}
		return imageId;		
	}
	
	private void refreshCityList() {
//		changeBackground(DAY_NIGHT);
//		if (DAY_NIGHT) {
//			DAY_NIGHT = false;
//		} else {
//			DAY_NIGHT = true;
//		} 
		Intent intent = new Intent(this, ChooseAreaActivity.class);
		intent.putExtra("refresh_city_list", true);
		startActivity(intent);
		finish();
	}
	
	private void changeBackground(boolean DAY_NIGHT) {
		if (DAY_NIGHT) {
			weatherMainLayout.setBackgroundResource(R.drawable.day2);
		} else {
			weatherMainLayout.setBackgroundResource(R.drawable.night);
		}
	}
}
