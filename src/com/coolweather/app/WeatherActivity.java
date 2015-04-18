package com.coolweather.app;

import com.coolweather.app.util.HttpCallBackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity {
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);
		//��ʼ�����ռ�
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_data);
		
		String countyCode = getIntent().getStringExtra("county_code");
		if(!TextUtils.isEmpty(countyCode)){
			//���ؼ����ž�ȥ��ѯ����
			publishText.setText("ͬ����....");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else {
			showWeather();
		}
	}
	/**
	 * ��ѯ�ؼ���������Ӧ������
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}
	/**
	 *  ��ѯ�������Ŷ�Ӧ������
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo" + weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}
	/**
	 * ���ݴ���ĵ�ַ������ȥ���������ѯ�������Ż���������Ϣ��
	 */
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallBackListener(){

			@Override
			public void onFinish(final String response) {
				if("countyCode".equals(type)) {
					if(!TextUtils.isEmpty(response)){
						//�ӷ��������ص������н�������������
						String[] array = response.split("\\|");
						if(array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if("weatherCode".equals(type)){
					Utility.handlWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							showWeather();
							
						}
						
					});
				}
					
				
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						publishText.setText("ͬ��ʧ��");
						
					}
					
				});
				
			}			
		});
	}
	/**
	 * ��sharedPreferences �ļ��ж�ȡ�洢��������Ϣ ����ʾ
	 */
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText(prefs.getString("publish_time", "")+"����");
		currentDateText.setText(prefs.getString("current_data", ""));
		
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}