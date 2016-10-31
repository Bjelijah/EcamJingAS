package com.howell.ecamerajing.action;

import com.howell.jni.CamJniUtil;

import android.graphics.Camera;
import android.os.AsyncTask;
import android.util.Log;

public class PlayPlayback extends PlayReview {
	@SuppressWarnings("unused")
	private final String TAG = getClass().getName();
	private static PlayPlayback mInstance = null;
	private long firstTimeStamp=0;
	private long curTimeStamp = 0;
	private long curTimeStampOffset = 0;
	boolean isStop = false;
	private int pos = 0;
	private PlayPlayback(){}
	public static PlayPlayback getInstance(){
		if (mInstance==null) {
			mInstance = new PlayPlayback();
		}
		return mInstance;
	}
	@Override
	public void playVideo() {
		firstTimeStamp = 0;
		curTimeStamp = 0;
		curTimeStampOffset = 0;
		renderer.setTime(0);
		PlayTask task = new PlayTask();
		task.execute(1);
		isStop = false;
	}
	
	@Override
	public void stopVideo() {
		Log.e("123", "play back stop video");
	
		isStop = true;
		super.stopVideo();
		firstTimeStamp = 0;
		curTimeStamp = 0;
		renderer.setTime(0);
	}
	
	@Override
	public int getVideoTotalSec() {
		Log.i("123", "play play back get totsec:"+item.getTotalSeconds());
//		return CamJniUtil.cameraGetTotalMSec()/1000;
		return item.getTotalSeconds()*1000;
	}

	@Override
	public int getVideoPlayedSec() {
//		return CamJniUtil.cameraGetPlayedMsc()/1000;
		return (int)(curTimeStamp+curTimeStampOffset)*1000;//毫喵
	}
	
	@Override
	public int getVideoPos() {
//		int cur = CamJniUtil.cameraGetCurFrame();
//		int cur = CamJniUtil.cameraGetPos();
	
		
		
		long cur =  renderer.getTime();
//		Log.i("123", "firstTimStamp="+firstTimeStamp+" cur="+cur);
		if (firstTimeStamp == 0&&cur>0) {
		
			firstTimeStamp = cur;
			Log.e("123", "firstTimeStamp = "+firstTimeStamp);
		}
		
		curTimeStamp = cur-firstTimeStamp;
		
//		Log.i("123", "cur fram="+(cur-firstTimeStamp)+" firstStame="+firstTimeStamp);
		
		
		if (isStop) {
			Log.e("123", "get pos is stop");
			firstTimeStamp = 0;
			curTimeStamp = 0;
		}
		return (int)(curTimeStamp + curTimeStampOffset);//秒
//		return CamJniUtil.cameraGetPos();
	}
	
	@Override
	public boolean setVideoPos(int pos) {
		this.pos = pos;//秒数
		Log.i("123", "set video pos :"+this.pos);
//		return CamJniUtil.cameraSetPos(pos)==1?true:false;
		return true;
	}
	
	@Override
	public int getTotalFrame() {
		return CamJniUtil.cameraGetTotalFrame();
	}
	
	@Override
	public int getVideoFrame() {
		return CamJniUtil.cameraGetCurFrame();
	}
	
	@Override
	public boolean setVideoFrame(int frame) {
		return CamJniUtil.cameraSetFrame(frame)==1?true:false;
	}
	
	
	@Override
	public int getVideoTotalPos() {
		
//		Log.i("123", "totalPos -> totsec:"+ item.getTotalSeconds() );
		return item.getTotalSeconds();//秒
//		return item.getTotalFrames();
	}
	
	@Override
	public void rePlayStop() {
		new AsyncTask<Void, Void, Void>(){

			@Override
			protected Void doInBackground(Void... params) {
				if(CamJniUtil.cameraStop()==0){
					Log.e("123", "camera stop error");
				}
				
				CamJniUtil.cameraLogout();
				
				
				return null;
			}
			
		}.execute();
	}
	
	
	
	@Override
	public void rePlayVideo() {
		
		firstTimeStamp = 0;
		curTimeStamp = 0;
		renderer.setTime(0);
		netRectFileItem.setOffsetSeconds(this.pos);
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
			
//				if(CamJniUtil.cameraStop()==0){
//					Log.e("123", "camera stop error");
//					return false;
//				}
				CamJniUtil.cameraLogin(TEST_IP);
				
//				netRectFileItem.setOffsetSeconds(0);
				if (CamJniUtil.cameraPlay(1, 0, 1, 1, netRectFileItem)==-1) {
					Log.e("123", "Camera play error");
					return false;
				}
				return true;
			}
			
			protected void onPostExecute(Boolean result) {
				if (result) {
					Log.i("123","replay ok");
					renderer.setTime(0);
					curTimeStampOffset = pos;
					Log.i("123", "offset = "+curTimeStampOffset);
					CamJniUtil.cameraPause(1);
					handler.sendEmptyMessage(PLAY_MSG_PLAY_PROGRESS);
					
				}else{
					handler.sendEmptyMessage(PLAY_MSG_PLAY_ERROR);
					Log.e("123", "replay error");
				}
			
			};
		}.execute();
	}
}
