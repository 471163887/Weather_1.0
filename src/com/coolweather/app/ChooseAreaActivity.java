package com.coolweather.app;

import android.app.Activity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.*;
import com.coolweather.app.R;
import com.coolweather.app.util.*;

//import android.R;
//如果你在R.java还没有来得及自动生成的时候选择了fix imports，那么Eclipse就会自动帮你导入
//android.R这个包，就会覆盖掉本地的R.java，所以就导致了你不能正常引用R.layout.activity_main.

import android.app.ProgressDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private TextView titleText; 
	private ProgressDialog progressDialog;
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text); 
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				if(currentLevel == LEVEL_PROVINCE){
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(position);
					queryCounties();
				}
				
			}
			
		});
		queryProvinces();
	}
	/**
	 * 查询全国所有省份，先从数据库查询，如果无 则去服务器上查询
	 */
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvince();
		
		if(provinceList.size() > 0) {
			
			dataList.clear();
			for(Province province: provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
			Toast.makeText(ChooseAreaActivity.this, "已将数据存入了SQLite中!", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
			queryFromServer(null, "province");
		}
		
	}
	/**
	 * 查询全国所有市，先从数据库查询，如果无 则去服务器上查询
	 */
	private void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size() > 0) {
			dataList.clear();
			for(City city: cityList)
				dataList.add(city.getCityName());
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "province");
		}
	}
	/**
	 * 查询全国所有县，先从数据库查询，如果无 则去服务器上查询
	 */
	private void queryCounties() {
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size() > 0) {
			dataList.clear();
			for(County county: countyList)
				dataList.add(county.getCountyName());
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityCode());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	/**
	 * 根据传入的代号和类型从服务器上查询省市县数据
	 */
	private void queryFromServer(final String code, final String type) {
		String address;
		Toast.makeText(ChooseAreaActivity.this, "加载成功!", Toast.LENGTH_SHORT).show();
		if( !TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city" + code +".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		
		
	showProgressDialog();
	
	//HttpUtil httpUtil;
		//静态方法才能这样调用！！！
	
	HttpUtil.sendHttpRequest(address,new HttpCallBackListener(){

		@Override
		public void onFinish(String response) {
			boolean result = false;
			if("province".equals(type)) {
				result = Utility.handleProvincesResponse(coolWeatherDB, response);
			} else if("city".equals(type)) {
				result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
			} else if("province".equals(type)) {
				result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
			}
			if(result){
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
					//通过runOnUiThread 返回主线程！
						closeProgressDialog();
						if("province".equals(type)) {
							queryProvinces();
						} else if("city".equals(type)) {
							queryCities();
						} else if("province".equals(type)) {
							queryCounties();
						}
						
					}
					
				});
			}
			
		}
		
		@Override
		public void onError(Exception e) {
			// 通过runOnUiThread 返回主线程！
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					closeProgressDialog();
					Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					
				}
				
			});
			
		}
		
	}); 
	}
	
	/**
	 * 显示对话框
	 */
	private void showProgressDialog(){
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("加载中........");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * 关闭对话框
	 */
	private void closeProgressDialog(){
		if(progressDialog != null){
			//progressDialog.closeOptionsMenu();
			progressDialog.dismiss();
		}

	}
	/**
	 * 捕获back键  判断应该返回那一级 OR 退出
	 */
	@Override
	public void onBackPressed() {
		if(currentLevel == LEVEL_COUNTY){
			queryCities();
		} else if(currentLevel == LEVEL_CITY){
			queryProvinces();
		}else if(currentLevel == LEVEL_PROVINCE){
			finish();
		}
	}
	
}
