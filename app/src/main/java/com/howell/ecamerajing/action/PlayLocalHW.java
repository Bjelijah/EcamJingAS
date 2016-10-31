package com.howell.ecamerajing.action;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.howell.ecamerajing.base.PlayBaseAction;
import com.howell.ecamerajing.service.AudioService;
import com.howell.ecamerajing.service.AudioService.AudioBind;
import com.howell.jni.CamJniUtil;

import java.util.Timer;
import java.util.TimerTask;

public class PlayLocalHW extends PlayBaseAction {
	private static PlayLocalHW mInstance = null;
	private PlayLocalHW() {}
	public static PlayLocalHW getInstance(){
		if (mInstance==null) {
			mInstance = new PlayLocalHW();
		}
		return mInstance;
	}

	private boolean isAsnycOk = false;
	protected boolean isAudioConnected = false;
	private int totMsec = 0, totFrame = 0 ,curPos=0 ,curFrame = 0;
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
	
	private Timer timer = null;
	
	private class MyTimerTask extends TimerTask{
		int totalmsec = -1;
		int asnycNum = 5;
		@Override
		public void run() {
			int msec = CamJniUtil.cameraGetTotalMSec();
//			Log.i("log123", "msec="+msec);
			
			if (totalmsec==msec && msec > 0) {
				asnycNum--;
			}
			if (asnycNum<1) {
				totMsec = totalmsec;
				totFrame = CamJniUtil.cameraGetTotalFrame();
				isAsnycOk = true;
				handler.sendEmptyMessage(PLAY_MSG_ASNYC_INFO);
				endAsnyc();
			}
			totalmsec = msec;
		}
	}
	
	private MyTimerTask timerTask = null ;
	
	private void startAsnyc(){
		timer = new Timer();
		timerTask = new MyTimerTask();
		timer.schedule(timerTask, 0,300);
	}
	
	private void endAsnyc(){
		if (timer!=null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
		if (timerTask!=null) {
			timerTask.cancel();
			timerTask = null;
		}
		pauseVideo(false);
	}
	
	private class PlayTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			CamJniUtil.cameraLoaclPlay(fileName);
			Intent serverIntent = new Intent(context,AudioService.class);
			context.bindService(serverIntent, audioConn,Service.BIND_AUTO_CREATE);
			return true;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				//开始同步数据
				pauseVideo(true);
				startAsnyc();
			}else{
				Log.e("log123", "playlocal hw play error");
			}
			super.onPostExecute(result);
		}
	}
	
	private class SetPosTask extends AsyncTask<Integer, Void, Void>{

		@Override
		protected Void doInBackground(Integer... params) {
			// TODO Auto-generated method stub
//			CamJniUtil.cameraSetFrame(params[0]);
			return null;
		}
		
	}
	
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		Message msg = new Message();
		msg.what = PLAY_MSG_PLAY_PREPARE;
		msg.arg1 = PLAY_VAL_NEED_ASNYC;
		handler.sendMessage(msg);
	}
	
	@Override
	public void playVideo() {
		PlayTask task = new PlayTask();
		task.execute();
	}

	@Override
	public void pauseVideo(boolean bPause) {
	
		if (bPause) {
			CamJniUtil.cameraPause(1);
		}else{
			CamJniUtil.cameraPause(0);
		}
	}

	@Override
	public void stopVideo() {
		if (isAudioConnected) {
			context.unbindService(audioConn);
		}
		CamJniUtil.cameraStop();
		endAsnyc();
	}

	@Override
	public int getVideoTotalSec() {
		if (!isAsnycOk) {
			return 0;
		}
		int totsec = CamJniUtil.cameraGetTotalMSec();
	//	Log.i("123", "totsec="+totsec);
		return totsec;
	}
	
	@Override
	public int getVideoPlayedSec() {
		if (!isAsnycOk) {
			return 0;
		}
		int sec = CamJniUtil.cameraGetPlayedMsc();
//		Log.i("123", "play sec="+sec);
		if (sec == 0 && curPos!=0) {
			return totMsec*curPos /100;
		}
		
		return sec;
	}
	
	@Override
	public int getVideoTotalPos() {
		if (!isAsnycOk) {
			return 0;
		}
		int totalFrame = getTotalFrame();
//		Log.i("123", "video total frame="+totalFrame);
		return totalFrame;
	}
	
	
	
	
	@Override
	public boolean setVideoPos(int pos) {//pos frame
		if (!isAsnycOk) {
			return false;
		}

		
		final float frame = pos;

		pos = (int) (frame/totFrame *100);
		curPos = pos;
		curFrame = (int)frame;
		
//		Thread thread = new Thread(){
//			public void run() {
//				CamJniUtil.cameraSetFrame((int)frame);
//			}
//		};
//		thread.start();
		
//		SetPosTask task = new SetPosTask();
//		task.execute((int)frame);
//		
//		return true;
		
		return CamJniUtil.cameraSetPos(pos)==1?true:false;
//		return CamJniUtil.cameraSetFrame(pos)==1?true:false;
		

		
	}
	
	@Override
	public int getVideoPos() {
		if (!isAsnycOk) {
			return 0;
		}
//		int pos = CamJniUtil.cameraGetPos();
		int pos = CamJniUtil.cameraGetCurFrame();
//		Log.i("123", "get pos="+pos);
		if (pos == 0 && curPos != 0 ) {
			return curFrame;
		}
		return pos;
	}
	
	@Override
	public int getTotalFrame() {
		return CamJniUtil.cameraGetTotalFrame();
	}
	
	
	@Override
	public int getVideoFrame() {
		return CamJniUtil.cameraGetTotalFrame();
	}
	
	@Override
	public boolean setVideoFrame(int frame) {
		return CamJniUtil.cameraSetFrame(frame)==1?true:false;
	}
	
	
}
