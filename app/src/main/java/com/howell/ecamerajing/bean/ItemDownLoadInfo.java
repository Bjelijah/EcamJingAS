package com.howell.ecamerajing.bean;

import java.io.Serializable;

import com.howell.ecamerajing.utils.Constable;

public class ItemDownLoadInfo implements Constable,Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8902371521630825809L;
	private int downLoadState = PLAYBACK_VAL_DOWNLOAD_NO;//普通 存在 正在下载

	private int downLoadIng = DOWNLOAD_VAL_DOWNLOAD_WAIT;//文字 
	private int downLoadProgress = 0;
	private String downLoadIngInfoStr = "";
	public int getDownLoadState() {
		return downLoadState;
	}

	public void setDownLoadState(int downLoadState) {
		this.downLoadState = downLoadState;
	}

	public int getDownLoadIng() {
		return downLoadIng;
	}

	public void setDownLoadIng(int downLoadIng) {
		this.downLoadIng = downLoadIng;
	}

	public int getDownLoadProgress() {
		return downLoadProgress;
	}

	public void setDownLoadProgress(int downLoadProgress) {
		this.downLoadProgress = downLoadProgress;
	}

	public String getDownLoadIngInfoStr() {
		return downLoadIngInfoStr;
	}

	public void setDownLoadIngInfoStr(String downLoadIngInfoStr) {
		this.downLoadIngInfoStr = downLoadIngInfoStr;
	}
	
	
}
