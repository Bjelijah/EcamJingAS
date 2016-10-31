package com.howell.ecamerajing.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.howell.jni.CamJniUtil;

public class AudioService extends Service {
	private static final String CALLBACK_FIELD_0  = "mAudioDataLength";
	private static final String CALLBACK_FIELD_1  = "mAudioData";
	private static final String CALLBACK_METHOD = "audioWrite";
	private static AudioTrack mAudioTrack = null;
	private byte[] mAudioData;
	private int mAudioDataLength;

	public void audioInit() {
		int buffer_size = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer_size*8, AudioTrack.MODE_STREAM);
		mAudioData = new byte[buffer_size*8];
	
	}

	public void audioPlay(){
		mAudioTrack.play();
		
	}

	public void audioStop(){
		if(mAudioTrack != null){
			mAudioTrack.stop();
			mAudioTrack.release();
			mAudioTrack = null;
		}
	}

	public void audioWrite() {
		mAudioTrack.write(mAudioData,0,mAudioDataLength);
	}   

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.i("log123", "audio servie on creat");
		CamJniUtil.AudioInit();
		audioInit();
		super.onCreate();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("log123", "audioService onbind");
	
		CamJniUtil.AudioSetCallbackObject(this, 0);
		CamJniUtil.AudioSetCallbackFieldName(CALLBACK_FIELD_0, 0);
		CamJniUtil.AudioSetCallbackFieldName(CALLBACK_FIELD_1, 1);
		CamJniUtil.AudioSetCallbackMethodName(CALLBACK_METHOD, 0);
		return new AudioBind();
	}
	public class AudioBind extends Binder{
		public void audioPlay(){
			AudioService.this.audioPlay();
		}
	}
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("log123", "audio service unbind");
		audioStop();
		CamJniUtil.AudioStop();
		CamJniUtil.AudioDeinit();
		return super.onUnbind(intent);
	}
}
