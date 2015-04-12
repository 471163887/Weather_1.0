package com.coolweather.app.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
	public static void sendHttpRequest(final String address, final HttpCallBackListener listener)
	{
		new Thread(new Runnable(){
			
			@Override
			public void run() {
				//try 外声明 finally 和 catch块中才能发现、
				HttpURLConnection connection = null;
				try {
					
					URL url = new URL(address);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					
					InputStream in = connection.getInputStream();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(in));
					StringBuilder response = new StringBuilder();
					String line; //= null;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}
					if (listener != null) {
						//回调FINISH方法
						listener.onFinish(response.toString());
					}
				} catch (Exception e) {
					if (listener != null) {
						listener.onError(e);
					}
				} finally{
					if (connection != null) {
						connection.disconnect();
					}
				}
				
			}
			
		}).start();
	}
}
