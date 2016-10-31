package com.howell.ecamerajing.base;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;

import com.howell.ecamerajing.action.YV12Renderer;
import com.howell.ecamerajing.bean.NetRectFileItem;
import com.howell.ecamerajing.utils.Constable;

public abstract class PlayBaseAction implements Constable{

	protected Handler handler=null;
	protected NetRectFileItem netRectFileItem = null;	//录像文件结构体
	protected Context context;
	protected String fileName;
	protected GLSurfaceView mGlView;
	protected YV12Renderer renderer=null;
	protected NetRectFileItem item = null;
	public Handler getHandler() {
		return handler;
	}
	public PlayBaseAction setHandler(Handler handler) {
		this.handler = handler;
		return this;
	}
	public NetRectFileItem getNetRectFileItem() {
		return netRectFileItem;
	}
	public PlayBaseAction setNetRectFileItem(NetRectFileItem netRectFileItem) {
		this.netRectFileItem = netRectFileItem;
		return this;
	}
	
	public Context getContext() {
		return context;
	}
	public PlayBaseAction setContext(Context context) {
		this.context = context;
		return this;
	}
	
	public String getFileName() {
		return fileName;
	}
	public PlayBaseAction setFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}
	
	public GLSurfaceView getmGlView() {
		return mGlView;
	}
	public PlayBaseAction setmGlView(GLSurfaceView mGlView) {
		this.mGlView = mGlView;
		return this;
	}
	public YV12Renderer getRenderer() {
		return renderer;
	}
	public void setRenderer(YV12Renderer renderer) {
		this.renderer = renderer;
	}
	
	public NetRectFileItem getItem() {
		return item;
	}
	public void setItem(NetRectFileItem item) {
		this.item = item;
	}
	public abstract void init();
	public abstract void playVideo();
	public abstract void pauseVideo(boolean bPause);
	public abstract void stopVideo();
	

	public void rePlayVideo(){}
	public void rePlayStop(){}
	
	public  int getVideoTotalSec(){
		Log.i("123", "total sec=0");
		return 0;}
	public  int getVideoPlayedSec(){return 0;}
	
	public 	int getVideoTotalPos(){return 0;}
	public  int getVideoPos(){return 0;}
	public  boolean setVideoPos(int pos){return true;}
	
	@Deprecated
	public  int getTotalFrame(){return 0;}
	@Deprecated
	public  int getVideoFrame(){return 0;}
	@Deprecated
	public  boolean setVideoFrame(int frame){return true;}
	
	
	public void setVideoFullMin(int width,int height){}
	public void setVideoFullMax(int width,int height){}
	
	
}
