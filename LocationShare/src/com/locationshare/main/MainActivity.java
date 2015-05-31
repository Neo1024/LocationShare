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

		// ��ͼ��ʼ��
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();

		// ������λͼ��
		mBaiduMap.setMyLocationEnabled(true);

		// ���ö�λͼ������ã���λģʽ���Ƿ���������Ϣ���û��Զ��嶨λͼ�꣩
		mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
		MyLocationConfiguration config = new MyLocationConfiguration(
				mCurrentMode, true, null);
		mBaiduMap.setMyLocationConfigeration(config);

		myListener = new MyLocationListener();
		mLocationClient = new LocationClient(getApplicationContext()); // ����LocationClient��
		mLocationClient.registerLocationListener(myListener); // ע���������

		// ����LocationClient��option
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// ���ö�λģʽ
		option.setCoorType("bd09ll");// ���صĶ�λ����ǰٶȾ�γ��,Ĭ��ֵgcj02
		option.setScanSpan(5000);// ���÷���λ����ļ��ʱ��Ϊ9000ms
		option.setIsNeedAddress(true);// ���صĶ�λ���������ַ��Ϣ
		option.setNeedDeviceDirect(true);// ���صĶ�λ��������ֻ���ͷ�ķ���
		option.setOpenGps(true);// ��gps
		mLocationClient.setLocOption(option);

		// ���Ȳ�ѯ�Լ���λ����Ϣ���ж��ƴ洢���Ƿ����Լ���λ����Ϣ��¼�����������򴴽�
		// ����ͬʱ��ȡ�Լ���poiId���ڸ����Լ���λ����Ϣʱʹ��
		HashMap<String, Object> filterParams = getQueryOwnPoiParams();
		LBSCloudSearch.request(LBSCloudSearch.SEARCH_TYPE_QUERY, filterParams,
				createHandler);

		// ��ʼ��λ
		mLocationClient.start();
		initOritationListener();
		myOrientationListener.start();
	}

	/*
	 * ������������
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
		// ��activityִ��onDestroyʱִ��mMapView.onDestroy()��ʵ�ֵ�ͼ�������ڹ���

		// �˳�ʱ���ٶ�λ
		if (mLocationClient != null && mLocationClient.isStarted()) {
			mLocationClient.stop();
		}
		mMapView.onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// ��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���
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
	 * ��ʼ�����򴫸���
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
	 * ��������
	 */
	class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null || mMapView == null) {
				System.out.println("null");
				return;
			}
			// ��ȡ�Լ��Ķ�λ��Ϣ
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			mRadius = location.getRadius();

			// ���Լ��Ķ�λ��Ϣ���µ��ƴ洢��
			 HashMap<String, Object> filterParams = getUpdatePoiParams();
			 LBSCloudSearch.request(LBSCloudSearch.SEARCH_TYPE_UPDATE,
			 filterParams, createHandler);

			// ��ȡ�Է��洢���ƴ洢�еĶ�λ��Ϣ��������otherLatitude��otherLongitude��otherDirction
			filterParams = getQueryOtherPoiParams();
			LBSCloudSearch.request(LBSCloudSearch.SEARCH_TYPE_QUERY,
					filterParams, createHandler);

			// ���춨λ����
			locData = new MyLocationData.Builder().accuracy(mRadius)
					.direction((float) otherDirection).latitude(otherLatitude)
					.longitude(otherLongitude).build();
			// ���ö�λ����
			mBaiduMap.setMyLocationData(locData);

			if (isFristLocation) {
				isFristLocation = false;
				LatLng ll = new LatLng(otherLatitude, otherLongitude);
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
			// ������Ҫ��λͼ��ʱ�رն�λͼ��
			// mBaiduMap.setMyLocationEnabled(false);
		}
	}

	/**
	 * ���ô���Poi���������</br>
	 * 
	 * @return
	 */
	private HashMap<String, Object> getCreatePoiParams() {
		HashMap<String, Object> filterParams = new HashMap<String, Object>();

		filterParams.put("title", ownPhoneNumber);
		filterParams.put("latitude", mLatitude);
		filterParams.put("longitude", mLongitude);
		filterParams.put("dircetion", mDirction);
		filterParams.put("coord_type", 3); // Ĭ��ʹ�ðٶȾ�γ��

		return filterParams;
	}

	/**
	 * ���ò�ѯ�Է�Poi���������</br>
	 * 
	 * @return
	 */
	private HashMap<String, Object> getQueryOtherPoiParams() {
		HashMap<String, Object> filterParams = new HashMap<String, Object>();

		filterParams.put("title", otherPhoneNumber);

		return filterParams;
	}

	/**
	 * ���ò�ѯ�Լ�Poi���������</br>
	 * 
	 * @return
	 */
	private HashMap<String, Object> getQueryOwnPoiParams() {
		HashMap<String, Object> filterParams = new HashMap<String, Object>();

		filterParams.put("title", ownPhoneNumber);

		return filterParams;
	}

	/**
	 * ���ø���Poi���������</br>
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
		filterParams.put("coord_type", 3); // Ĭ��ʹ�ðٶȾ�γ��

		return filterParams;
	}

	/*
	 * ������������
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
			// ����������Լ��ĺ��룬�������ص��������Լ���λ����Ϣ���ڴ˴�����poi��id�Ĺ�������ʱʹ��
			if (temp.getString("title").equals(ownPhoneNumber)) {
				ownPoiId = temp.getInt("id");
			}
			Log.e("LocationShare",
					"Into parsor---------->" + temp.getJSONArray("location")
							+ "  direction---->" + otherDirection + "id------>" + temp.getInt("id"));
		}
		//����total�ж��Լ���λ����Ϣ�����ݿ����Ƿ��м�¼����û���򴴽�
		//���������������ݿ������query��create��update��ֻ��query�᷵��total����
		if (json.optInt("total", -1) == 0) {
			HashMap<String, Object> filterParams = getCreatePoiParams();
			LBSCloudSearch.request(LBSCloudSearch.SEARCH_TYPE_CREATE,
					filterParams, createHandler);
			Log.e("LocationShareCreate", "------------->����λ����Ϣ����");
		}
		total = json.optInt("total");
		status = json.optInt("status");
		isFristLocation = true;

		Log.e("LocationShare", " parser ends:" + " otherLatitude:"
				+ otherLatitude + " otherLongitude��" + otherLongitude
				+ " otherDirection��" + otherDirection + " total��" + total
				+ " status��" + status + " ownPoiId:" + ownPoiId);

	}
}
