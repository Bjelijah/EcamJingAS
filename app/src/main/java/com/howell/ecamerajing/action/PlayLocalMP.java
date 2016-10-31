package com.howell.ecamerajing.action;

import java.io.IOException;

import com.howell.ecamerajing.base.PlayBaseAction;
import com.howell.ecamerajing.utils.PhoneConfigUtil;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Message;
import android.view.Gravity;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

public class PlayLocalMP extends PlayBaseAction implements OnBufferingUpdateListener, OnCompletionListener {
	private static PlayLocalMP mInstance = null;
	private PlayLocalMP() {}
	public static PlayLocalMP getInstance(){
		if (mInstance==null) {
			mInstance = new PlayLocalMP();
		}
		return mInstance;
	}
	private MediaPlayer mp;
	
	public void init(){
		initMP();
		initMPFile();
	}
	private void initMP(){
		mp = new MediaPlayer();
		mp.setAudioStreamType(AudioManager.STREAM_MUSIC); 
		mp.setOnBufferingUpdateListener(this);
		mp.setOnCompletionListener(this);
	}
	
	private void initMPFile(){
		mp.reset();
		try {
			mp.setDataSource(fileName);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		playPrepare();
		
	}
	
	private void playPrepare(){
		mp.prepareAsync();
		mp.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
//				Log.i("log123","mp max="+mp.getDuration());
				int width = mp.getVideoWidth();//1280
				int height = mp.getVideoHeight();//720
				
				
			
				
//				Log.i("123","width="+width+" height="+height );
				Message msg = new Message();
				msg.what = PLAY_MSG_PLAY_PREPARE;
				msg.arg1 = PLAY_VAL_NONEED_ASNYC;
				handler.sendMessage(msg);
			//	mp.start();
				
				
				
				
			}
		});
		mp.setDisplay(mGlView.getHolder());
		
		
		int width = PhoneConfigUtil.getPhoneWidth(context);
		int height = PhoneConfigUtil.getPhoneHeight(context);
		
		if (width>height) {//满屏
			setVideoFullMax(0,0);
		}else{
			setVideoFullMin(0, 0);
		}
		
	}
	

	private void stopMP(){
		mp.stop();
	}
	
	@Override
	public void playVideo() {
		mp.start();
	}

	@Override
	public void pauseVideo(boolean bPause) {//
		if (bPause) {
			mp.pause();
		}else{
			mp.start();
		}
		
	}

	@Override
	public void stopVideo() {
		stopMP();
	}
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		Toast.makeText(context, "play end", Toast.LENGTH_SHORT).show();
	}

	@Override
	public int getVideoPos() {
		return mp.getCurrentPosition();
	}
	@Override
	public boolean setVideoPos(int pos) {
		// TODO Auto-generated method stub
		mp.seekTo(pos);
		return true;
	}
	
	@Override
	public int getVideoTotalSec() {
		return mp.getDuration();
		
	}
	
	@Override
	public int getVideoTotalPos(){
		return getVideoTotalSec();
		
	}
	@Override
	public int getVideoPlayedSec(){
		 return mp.getCurrentPosition();
	}
	
	@Override
	public void setVideoFullMin(int width, int height) {
		width = PhoneConfigUtil.getPhoneWidth(context);
		height = PhoneConfigUtil.getPhoneHeight(context);
		if (width>height) {
			return;
		}
		int newHeight = width*9/16;
		 LayoutParams params = (LayoutParams) mGlView.getLayoutParams();
		 params.height = newHeight;
		 params.width = width;
		 params.gravity = Gravity.CENTER;
	}
	
	@Override
	public void setVideoFullMax(int width, int height) {
		LayoutParams params = (LayoutParams) mGlView.getLayoutParams();
		params.width = LayoutParams.FILL_PARENT;
		params.height = LayoutParams.FILL_PARENT;
		params.gravity = Gravity.CENTER;
	}
}
