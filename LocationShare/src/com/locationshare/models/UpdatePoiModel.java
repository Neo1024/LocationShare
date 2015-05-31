package com.locationshare.models;

/**
 * ��װUpdatePoi���������</br>
 * 
 * @author Lixin
 * @date 2015-5-26
 * 
 */
public class UpdatePoiModel {
	private static String ak = "7p6o7822auSTKV1fCERnDzBu";
	private static String geotable_id = "105611";

	private String id; // Poi��id
	private String title; // Poi���� String(256) ��ѡ
	private String address; // ��ַ String(256) ��ѡ
	private String tags; // String(256) ��ѡ
	private String latitude; // �û��ϴ���γ�� Double ��ѡ
	private String longitude; // �û��ϴ��ľ��� Double ��ѡ
	private String Geotable_id;// ��¼������geotable�ı�ʶ String(50) ��ѡ�����ܺ��id
	private String coord_type; /*
								 * �û��ϴ������������ UInt32 1.GPS��γ������ 2.����ּ��ܾ�γ������
								 * 3.�ٶȼ��ܾ�γ������ 4.�ٶȼ���ī�������� ��ѡ
								 */

	public UpdatePoiModel(String id, String title, String address,
			String latitude, String longitude, String coord_type,
			String geotable_id) {
		super();
		this.id = id;
		this.title = title;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.coord_type = coord_type;
		Geotable_id = geotable_id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public String getCoord_type() {
		return coord_type;
	}

	public void setCoord_type(String coord_type) {
		this.coord_type = coord_type;
	}

	public String getGeotable_id() {
		return Geotable_id;
	}

	public void setGeotable_id(String geotable_id) {
		Geotable_id = geotable_id;
	}

}