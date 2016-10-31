package com.howell.ecamerajing.utils;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.Log;

public class NetWorkUtil {
	/**
	 * wifi判断
	 */
	public static State getWifiState(Context context){
		//通过系统拿服务类
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
		//getNetworkInfo填写内容是wif网络信息
		NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		//查看info连接信息,所有的状态返回在state 枚举类中
		State state = info.getState();
		return state;
	}
	
	/**
	 * 移动网络判断
	 */
		public static State getMobileNetState(Context context){
		//通过系统拿服务类
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
		//getNetworkInfo填写内容是wif网络信息
		NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		//查看info连接信息,所有的状态返回在state 枚举类中
		State state = info.getState();
		
		return state;
	}	
		
		public static boolean isNetConnect(Context context){
			State state = NetWorkUtil.getWifiState(context);
			if (state!=null&&state.compareTo(State.CONNECTED)==0) {
				Log.i("log123", "wifi conncet");
				return true;
			}
			 state = NetWorkUtil.getMobileNetState(context);
			if (state!=null&&state.compareTo(State.CONNECTED)==0) {
				Log.i("log123", "mobile connect");
				return true;
			}
			
			Log.i("log123", "disconnect");
			return false;
		}
		
		
}
