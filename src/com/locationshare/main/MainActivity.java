package com.locationshare.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.locationshare.R;
import com.locationshare.main.MyOrientationListener.OnOrientationListener;

public class MainActivity extends Activity {

	public static final int MSG_NET_TIMEOUT = 100;
	public static final int MSG_NET_STATUS_ERROR = 200;
	public static final int MSG_NET_SUCC = 1;

	MapView mMapView = null;
	BaiduMap mBaiduMap = null;
	LocationClient mLocationClient = null;
	BDLocationListener myListener = null;
	BitmapDescriptor mCurrentMarker = null;
	MyLocationConfiguration.LocationMode mCurrentMode = null;
	MyOrientationListener myOrientationListener = null;
	double mLatitude = 0;
	double mLongitude = 0;
	double otherLatitude = 0;
	double otherLongitude = 0;
	float mRadius = 0;
	double mDirction = 0;
	double otherDirection = 0;
	boolean isFristLocation = true;
	MyLocationData locData = null;
	String ownPhoneNumber;
	String otherPhoneNumber;
	int status = -1;
	int total = -1;
	int ownPoiId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);

		Intent intent = getIntent();
		// Toast.makeText(getApplicationContext(),
		// intent.getExtras().getString("phoneNumber"), 3000).show();
		ownPhoneNumber = intent.getExtras().getString("ownPhoneNumber");
		otherPhoneNumber = intent.getExtras().getString("otherPhoneNumber");

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();

		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);

		// 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
		mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
		MyLocationConfiguration config = new MyLocationConfiguration(
				mCurrentMode, true, null);
		mBaiduMap.setMyLocationConfigeration(config);

		myListener = new MyLocationListener();
		mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
		mLocationClient.registerLocationListener(myListener); // 注册监听函数

		// 设置LocationClient的option
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(5000);// 设置发起定位请求的间隔时间为9000ms
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		option.setOpenGps(true);// 打开gps
		mLocationClient.setLocOption(option);

		// 首先查询自己的位置信息以判断云存储中是否有自己的位置信息记录，若不存在则创建
		// 并且同时获取自己的poiId用于更新自己的位置信息时使用
		HashMap<String, Object> filterParams = getQueryOwnPoiParams();
		LBSCloudSearch.request(LBSCloudSearch.SEARCH_TYPE_QUERY, filterParams,
				createHandler);

		// 开始定位
		mLocationClient.start();
		initOritationListener();
		myOrientationListener.start();
	}

	/*
	 * 处理网络请求
	 */
	private Handler createHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NET_TIMEOUT:
				break;
			case MSG_NET_STATUS_ERROR:
				break;
			case MSG_NET_SUCC:
				String result = msg.obj.toString();
				Log.e("LocationShare", "handleMessage is ok:" + result);
				try {
					JSONObject json = new JSONObject(result);
					parser(json);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理

		// 退出时销毁定位
		if (mLocationClient != null && mLocationClient.isStarted()) {
			mLocationClient.stop();
		}
		mMapView.onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	protected void onStart() {

		super.onStart();
	}

	@Override
	protected void onStop() {

		super.onStop();
	}

	/**
	 * 初始化方向传感器
	 */
	private void initOritationListener() {
		myOrientationListener = new MyOrientationListener(
				getApplicationContext());
		myOrientationListener
				.setOnOrientationListener(new OnOrientationListener() {
					@Override
					public void onOrientationChanged(float x) {
						mDirction = x;
					}
				});
	}

	/**
	 * 
	 * 监听函数
	 */
	class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null || mMapView == null) {
				System.out.println("null");
				return;
			}
			// 获取自己的定位信息
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			mRadius = location.getRadius();

			// 将自己的定位信息更新到云存储中
			 HashMap<String, Object> filterParams = getUpdatePoiParams();
			 LBSCloudSearch.request(LBSCloudSearch.SEARCH_TYPE_UPDATE,
			 filterParams, createHandler);

			// 获取对方存储在云存储中的定位信息，并更新otherLatitude，otherLongitude，otherDirction
			filterParams = getQueryOtherPoiParams();
			LBSCloudSearch.request(LBSCloudSearch.SEARCH_TYPE_QUERY,
					filterParams, createHandler);

			// 构造定位数据
			locData = new MyLocationData.Builder().accuracy(mRadius)
					.direction((float) otherDirection).latitude(otherLatitude)
					.longitude(otherLongitude).build();
			// 设置定位数据
			mBaiduMap.setMyLocationData(locData);

			if (isFristLocation) {
				isFristLocation = false;
				LatLng ll = new LatLng(otherLatitude, otherLongitude);
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
			// 当不需要定位图层时关闭定位图层
			// mBaiduMap.setMyLocationEnabled(false);
		}
	}

	/**
	 * 设置创建Poi的请求参数</br>
	 * 
	 * @return
	 */
	private HashMap<String, Object> getCreatePoiParams() {
		HashMap<String, Object> filterParams = new HashMap<String, Object>();

		filterParams.put("title", ownPhoneNumber);
		filterParams.put("latitude", mLatitude);
		filterParams.put("longitude", mLongitude);
		filterParams.put("dircetion", mDirction);
		filterParams.put("coord_type", 3); // 默认使用百度经纬度

		return filterParams;
	}

	/**
	 * 设置查询对方Poi的请求参数</br>
	 * 
	 * @return
	 */
	private HashMap<String, Object> getQueryOtherPoiParams() {
		HashMap<String, Object> filterParams = new HashMap<String, Object>();

		filterParams.put("title", otherPhoneNumber);

		return filterParams;
	}

	/**
	 * 设置查询自己Poi的请求参数</br>
	 * 
	 * @return
	 */
	private HashMap<String, Object> getQueryOwnPoiParams() {
		HashMap<String, Object> filterParams = new HashMap<String, Object>();

		filterParams.put("title", ownPhoneNumber);

		return filterParams;
	}

	/**
	 * 设置更新Poi的请求参数</br>
	 * 
	 * @return
	 */
	private HashMap<String, Object> getUpdatePoiParams() {
		HashMap<String, Object> filterParams = new HashMap<String, Object>();

		filterParams.put("title", ownPhoneNumber);
		filterParams.put("latitude", mLatitude);
		filterParams.put("longitude", mLongitude);
		filterParams.put("dirction", mDirction);
		filterParams.put("id", ownPoiId);
		filterParams.put("coord_type", 3); // 默认使用百度经纬度

		return filterParams;
	}

	/*
	 * 解析返回数据
	 */
	private void parser(JSONObject json) throws JSONException {
		Log.e("LocationShare",
				"AllMsg: " + json + "Into parsor:" + json.optJSONArray("pois"));
		JSONArray poisArr = json.optJSONArray("pois");
		if (poisArr != null) {
			JSONObject temp = (JSONObject) poisArr.get(0);
			otherLatitude = temp.getJSONArray("location").optDouble(1);
			otherLongitude = temp.getJSONArray("location").optDouble(0);
			otherDirection = temp.getDouble("direction");
			// 如果号码是自己的号码，表明返回的数据是自己的位置信息，在此处设置poi的id的供更新数时使用
			if (temp.getString("title").equals(ownPhoneNumber)) {
				ownPoiId = temp.getInt("id");
			}
			Log.e("LocationShare",
					"Into parsor---------->" + temp.getJSONArray("location")
							+ "  direction---->" + otherDirection + "id------>" + temp.getInt("id"));
		}
		//根据total判断自己的位置信息在数据库中是否有记录，若没有则创建
		//程序中有三种数据库操作，query，create，update，只有query会返回total参数
		if (json.optInt("total", -1) == 0) {
			HashMap<String, Object> filterParams = getCreatePoiParams();
			LBSCloudSearch.request(LBSCloudSearch.SEARCH_TYPE_CREATE,
					filterParams, createHandler);
			Log.e("LocationShareCreate", "------------->创建位置信息请求！");
		}
		total = json.optInt("total");
		status = json.optInt("status");
		isFristLocation = true;

		Log.e("LocationShare", " parser ends:" + " otherLatitude:"
				+ otherLatitude + " otherLongitude：" + otherLongitude
				+ " otherDirection：" + otherDirection + " total：" + total
				+ " status：" + status + " ownPoiId:" + ownPoiId);

	}
}
