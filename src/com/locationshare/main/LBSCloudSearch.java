package com.locationshare.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 百度云检索使用类
 * 
 * @author Lixin
 * @date 2015-5-25
 * 
 */
public class LBSCloudSearch {
	// 百度云检索API URI
	private static final String URI_CREATE = "http://api.map.baidu.com/geodata/v3/poi/create?"; // POST请求
	private static final String URI_QUERY = "http://api.map.baidu.com/geodata/v3/poi/list?"; // GET请求
	private static final String URI_UPDATE = "http://api.map.baidu.com/geodata/v3/poi/update?"; // POST请求

	// 百度云检索类型
	public static final int SEARCH_TYPE_CREATE = 1;
	public static final int SEARCH_TYPE_QUERY = 2;
	public static final int SEARCH_TYPE_UPDATE = 3;

	// 云检索公钥
	public static String ak = "PshWiFRlSanqCGWBGszIgiaK";
	public static String geotable_id = "105611";

	public static int TIME_OUT = 12000;
	public static int retry = 3;
	public static boolean IsBusy = false;

	/**
	 * 云检索访问
	 * 
	 * @param filterParams
	 *            访问参数
	 * @param handler
	 *            数据回调Handler
	 * @return
	 */
	public static void request(final int searchType,
			final HashMap<String, Object> filterParams, final Handler handler) {
		if (IsBusy || filterParams == null)
			return;

		Thread requestThread = new Thread() {
			public void run() {
				try {
					// 根据过滤选项拼接请求URL
					String requestURL = "";
					if (searchType == SEARCH_TYPE_CREATE) {
						requestURL = URI_CREATE;
					} else if (searchType == SEARCH_TYPE_QUERY) {
						requestURL = URI_QUERY;
					} else if (searchType == SEARCH_TYPE_UPDATE) {
						requestURL = URI_UPDATE;
					}
					requestURL = requestURL + "ak=" + ak + "&geotable_id="
							+ geotable_id;

					String filter = null;
					Iterator iter = filterParams.entrySet().iterator();
					new BasicNameValuePair("str", "I am Post String");
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					while (iter.hasNext()) {
						Map.Entry entry = (Map.Entry) iter.next();
						String key = entry.getKey().toString();
						String value = entry.getValue().toString();
						// 如需要POST请求则可以用此处的params设置参数
						params.add(new BasicNameValuePair(key, value));

						requestURL = requestURL + "&" + key + "=" + value;
					}

					Log.e("LocationShare", "request url:" + requestURL);

					// 根据请求类型判断使用GET或POST
					HttpClient httpclient = new DefaultHttpClient();
					HttpResponse httpResponse = null;
					if (searchType == SEARCH_TYPE_QUERY) {
						HttpGet httpGet = new HttpGet(requestURL);
						httpResponse = httpclient.execute(httpGet);
						Log.e("LocationShare", "GET Response is ok!");
					} else {
						// 创建POST请求并绑定参数
						params.add(new BasicNameValuePair("ak",
								LBSCloudSearch.ak));
						params.add(new BasicNameValuePair("geotable_id",
								LBSCloudSearch.geotable_id));
						HttpPost httpPost = new HttpPost(requestURL);
						httpPost.setEntity(new UrlEncodedFormEntity(params,
								HTTP.UTF_8));
						httpResponse = httpclient.execute(httpPost);
						if (searchType == LBSCloudSearch.SEARCH_TYPE_CREATE) {
							Log.e("LocationShareCreate",
									"------------>Create--->"
											+ httpResponse.getStatusLine()
													.getStatusCode());
						}
					}
					int status = httpResponse.getStatusLine().getStatusCode();
					if (status == HttpStatus.SC_OK) {

						String result = EntityUtils.toString(
								httpResponse.getEntity(), "utf-8");
						// Header a = httpResponse.getEntity().getContentType();
						// Message msgTmp = handler
						// .obtainMessage(MainActivity.MSG_NET_SUCC);
						// msgTmp.obj = result;
						// msgTmp.sendToTarget();
						Message msg = new Message();
						msg.what = MainActivity.MSG_NET_SUCC;
						msg.obj = result;
						handler.sendMessage(msg);
					} else {
						// httpRequest.abort();
						Message msgTmp = handler
								.obtainMessage(MainActivity.MSG_NET_STATUS_ERROR);
						msgTmp.obj = "HttpStatus error";
						msgTmp.sendToTarget();
					}
				} catch (Exception e) {
					Log.e("LocationShare", "网络异常，请检查网络后重试！");
					e.printStackTrace();
				}
			}
		};
		requestThread.start();

		return;
	}
}
