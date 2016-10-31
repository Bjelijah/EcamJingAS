package com.howell.ecamerajing.bean;

import com.howell.ecamerajing.bean.NetRectFileItem;

import android.util.Log;

/**
 * @author 霍之昊 
 *
 * 类说明
 */
public class SeekBarManager {
	private int fileNo;
	private int totalSeconds;
	private long nowTimeStamp;
	private long firstTimeStamp;
	//第一帧数据的时间戳是否已经获取到
	private boolean isFirstTimeStampComing;
	//是否正在拖动进度条
	private boolean isSeekBarTrackingTouch;
	//按返回时进度条停止
	private boolean isSeekBarStopWorking;
	
	private int offsetSeconds;
	
	public SeekBarManager(NetRectFileItem netRectFileItem) {
		// TODO Auto-generated constructor stub
		this.fileNo = netRectFileItem.getFileNo();
		this.totalSeconds = netRectFileItem.getTotalSeconds() * 1000;
		Log.e("","totalSeconds:"+totalSeconds);
		this.nowTimeStamp = 0;
		this.firstTimeStamp = 0;
		this.isFirstTimeStampComing = false;
		this.isSeekBarTrackingTouch = false;
		this.isSeekBarStopWorking = false;
		this.offsetSeconds = 0;
	}

	public int getTotalSeconds() {
		return totalSeconds;
	}

	public void setTotalSeconds(int totalSeconds) {
		this.totalSeconds = totalSeconds;
	}

	public long getNowTimeStamp() {
		return nowTimeStamp;
	}

	public void setNowTimeStamp(long nowTimeStamp) {
		this.nowTimeStamp = nowTimeStamp;
	}

	public long getFirstTimeStamp() {
		return firstTimeStamp;
	}

	public void setFirstTimeStamp(long firstTimeStamp) {
		this.firstTimeStamp = firstTimeStamp + offsetSeconds * 1000;
		isFirstTimeStampComing = true;
	}
	
	public boolean isFirstTimeStampComing(){
		Log.e("", "firstTimeStamp:"+firstTimeStamp);
		return isFirstTimeStampComing;
	}
	
	public long getProgress(){
		Log.e("", "progress:"+(isFirstTimeStampComing ? nowTimeStamp - firstTimeStamp : 0));
		return isFirstTimeStampComing ? nowTimeStamp - firstTimeStamp : 0;
	}

	public boolean isSeekBarTrackingTouch() {
		return isSeekBarTrackingTouch;
	}

	public void setSeekBarTrackingTouch(boolean isSeekBarTrackingTouch) {
		this.isSeekBarTrackingTouch = isSeekBarTrackingTouch;
	}

	public boolean isSeekBarStopWorking() {
		return isSeekBarStopWorking;
	}

	public void setSeekBarStopWorking(boolean isSeekBarStopWorking) {
		this.isSeekBarStopWorking = isSeekBarStopWorking;
	}

	public int getOffsetSeconds() {
		return offsetSeconds;
	}

	public void setOffsetSeconds(int offsetSeconds) {
		this.offsetSeconds = offsetSeconds;
	}

	public int getFileNo() {
		return fileNo;
	}

	public void setFileNo(int fileNo) {
		this.fileNo = fileNo;
	}

}
