package com.howell.ecamerajing.action;

import com.howell.ecamerajing.base.PlayBaseAction;
import com.howell.ecamerajing.service.AudioService;
import com.howell.ecamerajing.service.AudioService.AudioBind;
import com.howell.jni.CamJniUtil;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class PlayReview extends PlayBaseAction{
	private static PlayReview mInstance = null;
	protected PlayReview(){}
	public static PlayReview getInstance(){
		if (mInstance==null) {
			mInstance = new PlayReview();
		}
		return mInstance;
	}
	protected boolean isAudioConnected = false;
	
	protected ServiceConnection audioConn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i("log123", "audio server off line");
			isAudioConnected = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i("log123", "audio server on line");
			isAudioConnected = true;
			AudioBind audioBind = (AudioBind) service;
			audioBind.audioPlay();
		}
	};
	
	
	protected class PlayTask extends AsyncTask<Integer, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			if (CamJniUtil.cameraLogin(TEST_IP)==-1) {
				handler.sendEmptyMessage(PLAY_MSG_LOGIN_ERROR);
				Log.e("123", "cameraPlay login error");
				return false;
			}
		
			if(CamJniUtil.cameraPlay(params[0], 0, 1, 1, netRectFileItem)==-1){
				Log.e("123", "cameraPlay error");
				return false;
			}
			Intent serverIntent = new Intent(context,AudioService.class);
			context.bindService(serverIntent, audioConn,Service.BIND_AUTO_CREATE);
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if (result) {
				Log.i("123", "play review ok");
			}else{
				Log.e("123", "play review error");
				handler.sendEmptyMessage(PLAY_MSG_PLAY_ERROR);
			}
			
			
			
			super.onPostExecute(result);
		}
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		Message msg = new Message();
		msg.what = PLAY_MSG_PLAY_PREPARE;
		msg.arg1 = PLAY_VAL_NONEED_ASNYC;
		handler.sendMessage(msg);
	}
	@Override
	public void playVideo() {
		// TODO Auto-generated method stub
		Log.i("123", "msg play video");
		PlayTask task = new PlayTask();
		task.execute(0);
	}
	@Override
	public void pauseVideo(boolean bPause) {
		// TODO Auto-generated method stub
		if (bPause) {
			Log.i("123", "bePause");
			CamJniUtil.cameraPause(1);
		}else{
			Log.i("123", "no Pause");
			CamJniUtil.cameraPause(0);
		}
	}
	@Override
	public void stopVideo() {
		// TODO Auto-generated method stub
		if (isAudioConnected) {
			try {
				context.unbindService(audioConn);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		CamJniUtil.cameraStop();
		CamJniUtil.cameraLogout();
	}
	
	
}
