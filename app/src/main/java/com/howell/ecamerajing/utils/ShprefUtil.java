package com.howell.ecamerajing.utils;




import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.Set;

/**
 * shprefence ������
 * @author cbj
 *
 */
public class ShprefUtil {
	Context context;
	SharedPreferences sp; 
	String pathName;
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public SharedPreferences getSp() {
		return sp;
	}
	public void setSp(SharedPreferences sp) {
		this.sp = sp;
	}
	public String getPathName() {
		return pathName;
	}
	public void setPathName(String pathName) {
		this.pathName = pathName;
		sp = context.getSharedPreferences(pathName, 0);//context.mode_private
	}
	public ShprefUtil() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ShprefUtil(Context context) {
		super();
		this.context = context;
		pathName = "spdemo";
		sp = context.getSharedPreferences(pathName, 0);
	}
	public void saveSharedPreferences(String key,Boolean boo){		
		Editor editor = sp.edit();
		editor.putBoolean(key, boo);
		editor.commit();
	}
	public void saveSharedPreferences(String key,float flo){	
		Editor editor = sp.edit();
		editor.putFloat(key, flo);
		editor.commit();
	}
	public void saveSharedPreferences(String key,int num){		
		Editor editor = sp.edit();
		editor.putInt(key, num);
		editor.commit();
	}
	public void saveSharedPreferences(String key,long lon){	
		Editor editor = sp.edit();
		editor.putLong(key, lon);
		editor.commit();
		Log.i("123", "����long"+lon);
	}
	public void saveSharedPreferences(String key,String str){	
		Editor editor = sp.edit();
		editor.putString(key, str);
		editor.commit();
		Log.i("123", "����string"+str);
	}
	public void saveSharedPreferences(String key,Set<String> set){	
		Editor editor = sp.edit();
		editor.putStringSet(key, set);
		editor.commit();
	}
	public Boolean loadSharedPreferences(String key,boolean def){
		return sp.getBoolean(key, def);
	}
	public float loadSharedPreferences(String key,float def){
		return sp.getFloat(key, def);
	}
	public int loadSharedPreferences(String key,int def){
		return sp.getInt(key, def);
	}
	public long loadSharedPreferences(String key,long def){
		return sp.getLong(key, def);
	}
	public String loadSharedPreferences(String key,String def){
		
		return sp.getString(key, def);
	}
	public Set<String> loadSharedPreferences(String key,Set<String> def){
		return sp.getStringSet(key, def);
	}
	public void loadSharedPreferences(String key){
		Editor editor = sp.edit();
		editor.remove(key);
		editor.commit();
	}
	public void loadSharedPreferences(){
		Editor editor = sp.edit();
		editor.clear();
		editor.commit();
	}
}
