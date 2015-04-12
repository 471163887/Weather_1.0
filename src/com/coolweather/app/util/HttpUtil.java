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
				//try ������ finally �� catch���в��ܷ��֡�
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
						//�ص�FINISH����
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
