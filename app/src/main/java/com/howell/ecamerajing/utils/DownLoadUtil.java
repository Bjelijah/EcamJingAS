package com.howell.ecamerajing.utils;


import android.content.Context;
import android.net.TrafficStats;

import com.howell.ecamerajing.bean.NetRectFileItem;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class DownLoadUtil implements Constable{

	public static String getDownLoadTotalDuration(NetRectFileItem item){
		int totalsec = item.getTotalSeconds();
		//		Log.i("123", "downloadUtil totalsec = "+totalsec);
		//		long msec = totalsec*1000;
		//		Date date = new Date(0);
		//		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		//		date.setTime(msec);
		//		String formatTime = sdf.format(date);


		int secVal = totalsec%60;
		int hourVal = totalsec/3600; 
		int minVal = (totalsec - hourVal*3600 ) / 60;

		return String.format("%02d:%02d:%02d", hourVal,minVal,secVal);	
	}

	public static String getDownLoadCurTime(long msec){
		long sec = msec/1000;
		int secVal = (int)sec%60;
		int minVal = (int)sec/60;
		return String.format("%02d : %02d", minVal,secVal);
	}
	
	
	public static String getDownLoadStartTime(NetRectFileItem item){
		return item.getBegYear() + "."+ item.getBegMonth() + "." + item.getBegDay()
		+ " " +item.getBegHour() + ":" + item.getBegMinute() + ":" + item.getBegSecond();
	}

	public static String getDownLoadState(NetRectFileItem item){
		if (item.getItemInfo().getDownLoadIng() == DOWNLOAD_VAL_DOWNLOAD_WAIT) {
			return "等待中";
		}else{
			return item.getItemInfo().getDownLoadIngInfoStr();
		}

	}

	public static byte[] chars2Bytes(char [] chars){
		Charset cs = Charset.forName("UTF-8");
		CharBuffer cb = CharBuffer.allocate(chars.length);
		cb.put(chars);
		cb.flip();
		ByteBuffer bb = cs.encode(cb);
		return bb.array();
	}

	public static char[] getChars (byte[] bytes) {
		Charset cs = Charset.forName ("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate (bytes.length);
		bb.put (bytes);
		bb.flip ();
		CharBuffer cb = cs.decode (bb);
		return cb.array();
	}
	
	public static byte [] getSubByte(byte[] data,int beginOffSet,int len){
		byte [] bs = new byte [len];
		for(int i=beginOffSet;i<beginOffSet+len;i++){
			bs[i-beginOffSet] = data[i];
		}
		return bs;
	}
	
	private static long lastTotalRxBytes = 0;
	private static long lastTimeStamp = 0;
	
	private static long getTotalRxBytes(Context context){
		return TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED?0:(TrafficStats.getTotalRxBytes()/1024);
	}
	
	public static String getDownloadSpeed(Context context){
		long nowTotalRxBytes = getTotalRxBytes(context);
		long nowTimeStemp = System.currentTimeMillis();
		long speed = (nowTotalRxBytes - lastTotalRxBytes)*1000 / (nowTimeStemp - lastTimeStamp);
		lastTimeStamp = nowTimeStemp;
		lastTotalRxBytes = nowTotalRxBytes;
		if(speed == 0 ){
			if(!isNetConnect(context)){
				return null;
			}
		}
		
		return String.valueOf(speed) + "kb/s";
	}
	
	public static boolean isNetConnect(Context context){
		return NetWorkUtil.isNetConnect(context);
	} 
	
	
}
