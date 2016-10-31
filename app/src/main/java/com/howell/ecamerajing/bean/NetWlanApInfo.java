package com.howell.ecamerajing.bean;

public class NetWlanApInfo {
	
	public static final int WIFI_OPEN			= 0;
	public static final int WIFI_WEP64			= 1;
	public static final int WIFI_WEP128			= 2;
	public static final int WIFI_WPA			= 3;
	public static final int WIFI_WPA2			= 4;
	
	private String ssid = "";
	private String key="";
	private int channel=0;
	private int encrypt=0;
	private int flag=0;
	public String getSsid() {
		return ssid;
	}
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getChannel() {
		return channel;
	}
	public void setChannel(int channel) {
		this.channel = channel;
	}
	public int getEncrypt() {
		return encrypt;
	}
	public void setEncrypt(int encrypt) {
		this.encrypt = encrypt;
	}
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	
}
