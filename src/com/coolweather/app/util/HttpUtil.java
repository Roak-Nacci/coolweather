package com.coolweather.app.util;

import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpUtil {
	
	public static void sendHttpRequest(final String address,
			final HttpCallbackListener listener) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				Log.d("HttpUtil", "address = " + address.toString());
				try {
//					URL url = new URL(address);
//					connection = (HttpURLConnection) url.openConnection();
//					connection.setRequestMethod("GET");
//					connection.setConnectTimeout(8000);
//					connection.setReadTimeout(8000);
//					InputStream in = connection.getInputStream();
//					BufferedReader reader = new BufferedReader(new InputStreamReader(in, "GB2312"));
//					StringBuilder response = new StringBuilder();
//					String line;
//					while ((line = reader.readLine()) != null) {
//						response.append(line);
//					}

					HttpClient httpClient = new DefaultHttpClient();
					HttpGet httpGet = new HttpGet(address);
					HttpResponse httpResponse = httpClient.execute(httpGet);
					String response = "";
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = httpResponse.getEntity();
						response = EntityUtils.toString(entity, "utf-8");
					}
					Log.d("HttpUtil", "listener");
					if (listener != null) {
						listener.onFinish(response.toString());
					}
				} catch (Exception e) {
					if (listener != null) {
						listener.onError(e);
						Log.d("HttpUtil", e.toString());
					}
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();
	}
	
}
