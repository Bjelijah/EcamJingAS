package com.howell.ecamerajing.bean;

import java.io.Serializable;

import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamerajing.utils.FileUtil;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

/**
 * @author 霍之昊 
 *
 * 类说明:JNI层为兼容老协议而使用的新结构体
 */
public class NetRectFileItem implements Serializable,Constable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int fileNo;
	private short begYear;
	private short begMonth;
	private short begDay;
	private short begHour;
	private short begMinute;
	private short begSecond;
	private int totalSeconds;//秒
	private int totalFrames;
	private int offsetSeconds;
	
	private short endYear;
	private short endMonth;
	private short endDay;
	private short endHour;
	private short endMinute;
	private short endSecond;
//	private byte [] jpgBuf = new byte[3*1024*1024];
	private boolean bitmapExists = false;//文件夹下存在缓存图片
	
	private int bitmapWidth = 0 , bitmapHeight = 0;
	
	private ItemDownLoadInfo itemInfo = new ItemDownLoadInfo(); 
	public NetRectFileItem(int fileNo, short begYear, short begMonth,
			short begDay, short begHour, short begMinute, short begSecond,
			int totalSeconds, int totalFrames, int offsetSeconds) {
		super();
		this.fileNo = fileNo;
		this.begYear = begYear;
		this.begMonth = begMonth;
		this.begDay = begDay;
		this.begHour = begHour;
		this.begMinute = begMinute;
		this.begSecond = begSecond;
		this.totalSeconds = totalSeconds;
		this.totalFrames = totalFrames;
		this.offsetSeconds = offsetSeconds;
	}
	public void setBitmapSize(int width,int height){
		this.bitmapWidth = width;
		this.bitmapHeight = height;
	}
	public int getBitmapWidth(){
		return this.bitmapWidth;
	}
	public int getBitmapHeight(){
		return this.bitmapHeight;
	}
	public String getFileNameToString(){
		return String.format("%04d", begYear)+String.format("%02d", begMonth)+String.format("%02d", begDay)+String.format("%02d",  begHour)+ String.format("%02d",begMinute)+String.format("%02d", begSecond);
	}
	
	public NetRectFileItem() {
		super();
	}
	public int getFileNo() {
		return fileNo;
	}
	public void setFileNo(int fileNo) {
		this.fileNo = fileNo;
	}
	public short getBegYear() {
		return begYear;
	}
	public void setBegYear(short begYear) {
		this.begYear = begYear;
	}
	public short getBegMonth() {
		return begMonth;
	}
	public void setBegMonth(short begMonth) {
		this.begMonth = begMonth;
	}
	public short getBegDay() {
		return begDay;
	}
	public void setBegDay(short begDay) {
		this.begDay = begDay;
	}
	public short getBegHour() {
		return begHour;
	}
	public void setBegHour(short begHour) {
		this.begHour = begHour;
	}
	public short getBegMinute() {
		return begMinute;
	}
	public void setBegMinute(short begMinute) {
		this.begMinute = begMinute;
	}
	public short getBegSecond() {
		return begSecond;
	}
	public void setBegSecond(short begSecond) {
		this.begSecond = begSecond;
	}
	public int getTotalSeconds() {
		return totalSeconds;
	}
	public void setTotalSeconds(int totalSeconds) {
		this.totalSeconds = totalSeconds;
	}
	public int getTotalFrames() {
		return totalFrames;
	}
	public void setTotalFrames(int totalFrames) {
		this.totalFrames = totalFrames;
	}
	public int getOffsetSeconds() {
		return offsetSeconds;
	}
	public void setOffsetSeconds(int offsetSeconds) {
		this.offsetSeconds = offsetSeconds;
	}
	
	public short getEndYear() {
		return endYear;
	}
	public void setEndYear(short endYear) {
		this.endYear = endYear;
	}
	public short getEndMonth() {
		return endMonth;
	}
	public void setEndMonth(short endMonth) {
		this.endMonth = endMonth;
	}
	public short getEndDay() {
		return endDay;
	}
	public void setEndDay(short endDay) {
		this.endDay = endDay;
	}
	public short getEndHour() {
		return endHour;
	}
	public void setEndHour(short endHour) {
		this.endHour = endHour;
	}
	public short getEndMinute() {
		return endMinute;
	}
	public void setEndMinute(short endMinute) {
		this.endMinute = endMinute;
	}
	public short getEndSecond() {
		return endSecond;
	}
	public void setEndSecond(short endSecond) {
		this.endSecond = endSecond;
	}
	public int getDownLoadState() {
		return itemInfo.getDownLoadState();
	}
	public void setDownLoadState(int downLoadState) {
		itemInfo.setDownLoadState(downLoadState);
	}
	
	public ItemDownLoadInfo getItemInfo() {
		return itemInfo;
	}
	public void setItemInfo(ItemDownLoadInfo itemInfo) {
		this.itemInfo = itemInfo;
	}	
	
	
	
	public boolean isBitmapExists() {
		return bitmapExists;
	}
	public void setBitmapExists(boolean bitmapExists) {
		this.bitmapExists = bitmapExists;
	}
	@Override
	public String toString() {
		return "NetRectFileItem [fileNo=" + fileNo + ", begYear=" + begYear
				+ ", begMonth=" + begMonth + ", begDay=" + begDay
				+ ", begHour=" + begHour + ", begMinute=" + begMinute
				+ ", begSecond=" + begSecond + ", totalSeconds=" + totalSeconds
				+ ", totalFrames=" + totalFrames + ", offsetSeconds="
				+ offsetSeconds + "]";
	}
	public boolean equals(Object o) {
		NetRectFileItem bar = (NetRectFileItem)o; 
		boolean ret = false;
		
		ret = ((this.begYear==bar.begYear) &&
		(this.begMonth == bar.begMonth) &&
		(this.begDay == bar.begDay)&&
		(this.begHour == bar.begHour) &&
		(this.begMinute == bar.begMinute) &&
		(this.begSecond == bar.begSecond) &&
		(this.totalSeconds == bar.totalSeconds) &&
		(this.totalFrames == bar.totalFrames) &&
		(this.offsetSeconds == bar.offsetSeconds)
		);
		
//		Log.i("123", "this="+this.toString()+"  o="+bar.toString()+"ret = "+ret);
		
		
		return ret;
	}
	public int hashCode() {
		return this.hashCode();
	}
	
	
}
