package com.locationshare.models;

/**
 * 封装QueryPoi请求的数据</br>
 * 
 * @author Lixin
 * @date 2015-5-26
 **/
public class QueryPoiModel {
	private static String ak = "7p6o7822auSTKV1fCERnDzBu";
	private static String geotable_id = "105611";

	private String title; // Poi名称 String(256) 可选
	private String Address; // 地址 String(256) 可选
	private String tags; // String(256) 可选
	private String latitude; // 用户上传的纬度 Double 必选
	private String longitude; // 用户上传的经度 Double 必选
	private String Geotable_id;// 记录关联的geotable的标识 String(50) 必选，加密后的id
	private String bounds;
	private String page_index;
	private String page_size;
	private String coord_type;/*
							 * 用户上传的坐标的类型 UInt32 1.GPS经纬度坐标 2.国测局加密经纬度坐标
							 * 3.百度加密经纬度坐标 4.百度加密墨卡托坐标 必选
							 */

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAddress() {
		return Address;
	}

	public void setAddress(String address) {
		Address = address;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getGeotable_id() {
		return Geotable_id;
	}

	public void setGeotable_id(String geotable_id) {
		Geotable_id = geotable_id;
	}

	public String getBounds() {
		return bounds;
	}

	public void setBounds(String bounds) {
		this.bounds = bounds;
	}

	public String getPage_index() {
		return page_index;
	}

	public void setPage_index(String page_index) {
		this.page_index = page_index;
	}

	public String getPage_size() {
		return page_size;
	}

	public void setPage_size(String page_size) {
		this.page_size = page_size;
	}

	public String getCoord_type() {
		return coord_type;
	}

	public void setCoord_type(String coord_type) {
		this.coord_type = coord_type;
	}

}
