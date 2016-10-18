package com.windweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.windlweather.app.R;
import com.windweather.app.model.City;
import com.windweather.app.model.CoolWeatherDB;
import com.windweather.app.model.County;
import com.windweather.app.model.Province;
import com.windweather.app.util.HttpCallbackListener;
import com.windweather.app.util.HttpUtil;
import com.windweather.app.util.Utility;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE = 0;
	
	public static final int LEVEL_CITY = 1;
	
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	
	private TextView titleText;
	
	private ListView listView;
	
	private ArrayAdapter<String> adapter;
	
	private CoolWeatherDB coolWeatherDB;
	
	private List<String> dataList = new ArrayList<String>();
	
	private List<Province> provinceList;
	
	private List<City> cityList;
	
	private List<County> countyList;
	
	private Province selectedProvince;
	
	private City selectedCity;
		
	private int currentLevel;
	
	private boolean isFromWeatherActivity;
	
	private boolean refreshCityList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",
				false);
		refreshCityList = getIntent().getBooleanExtra("refresh_city_list",
				false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity
				&& !refreshCityList) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById (R.id.list_view);
		titleText = (TextView) findViewById (R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countyList.get(index).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}			
		});
		queryProvinces();
		
	}

	private void queryProvinces() {
		if (!refreshCityList) {
			provinceList = coolWeatherDB.loadProvinces();
			if (provinceList.size() > 0 ) {
				dataList.clear();
				for (Province province : provinceList) {
					dataList.add(province.getProvinceName());
				}
				adapter.notifyDataSetChanged();
				listView.setSelection(0);
				titleText.setText("�й�");
				currentLevel = LEVEL_PROVINCE;
			} else {
				queryFromServer(null, "province");
			}
		} else {
			refreshCityList = false;
			coolWeatherDB.refreshTable();
			SharedPreferences.Editor editor = PreferenceManager.
					getDefaultSharedPreferences(this).edit();
			editor.putBoolean("city_selected", false);
			editor.commit();
			queryFromServer(null, "province");
			Toast.makeText(ChooseAreaActivity.this, "����ˢ�³����б�", Toast.LENGTH_SHORT).show();
		}		
	}
	
	private void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	private void queryCounties() {
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}		
	}
	
	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code +
					".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvinceResponse(coolWeatherDB, response);
				} else if ("city".equals(type)) {
					result = Utility.handleCityResponse(coolWeatherDB, response,
							selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountyResponse(coolWeatherDB, response, 
							selectedCity.getId());
				}
				if (result) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else {
								queryCounties();
							}
						}						
					});
				}
				
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "Error", 
								Toast.LENGTH_SHORT).show();
					}
				});
			}			
		});
	}

	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Loading");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();		
	}

	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		} 
	}
	
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}