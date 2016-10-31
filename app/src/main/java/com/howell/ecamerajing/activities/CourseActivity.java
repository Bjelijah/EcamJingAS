package com.howell.ecamerajing.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamerajing.view.MyGifView;
import com.howell.ecamjing.R;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CourseActivity extends Activity implements Constable, OnCompletionListener {
	
	private MediaPlayer mp = null;
	
	List<MyMusic> data;
	private Button btNext,btWifi;
	private TextView tvTop,tvText1,tvText2,tvText3,tvTextAsk;
	private MyGifView gif;
	private RelativeLayout rl;
	private ImageView ivWifi;
	private int step = 0;
	
	private Handler handler = new Handler(){
		
		public void handleMessage(android.os.Message msg){
			switch (msg.what) {
			case COURSE_MSG_GIF_FINISH:
				Log.i("123", "msg gif finish");
				break;
				
			case COURSE_MSG_STEP_FINISH:
				Log.i("123", "music finish  step= "+step);
				showBtn();
				break;
				
			default:
				break;
			}
		}
		
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course);
		step = 0;
		rl = (RelativeLayout)findViewById(R.id.course_rl_background);
		ivWifi = (ImageView)findViewById(R.id.course_iv_wifi);
		gif = (MyGifView)findViewById(R.id.my_gif_view);
		gif.setHandler(handler);
		data = initData();
	
		tvText1 = (TextView)findViewById(R.id.course_tv_text1);
		tvText2 = (TextView)findViewById(R.id.course_tv_text2);
		tvText3 = (TextView)findViewById(R.id.course_tv_text3);
		tvTextAsk = (TextView)findViewById(R.id.course_tv_question);
//		tvTextAsk.setVisibility(View.GONE);

		btNext = (Button)findViewById(R.id.course_btn);

	
		btNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				step++;
				showCourse();
			}
		});
//		btNext.setVisibility(View.GONE);
		
		tvTop = (TextView)findViewById(R.id.course_tv_top);
		tvTop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (step==0) {//退出
					finish();
				}else{
					step--;
					showCourse();
				}
			}
		});
		
		btWifi = (Button)findViewById(R.id.course_btn_wifi);
		btWifi.setVisibility(View.GONE);
		btWifi.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("123", "set wifi");
				
				startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			}
		});
		showCourse();
	}
	private void showBtn(){
		if (step==0) {
			tvTextAsk.setVisibility(View.VISIBLE);
			tvTextAsk.setText(getResources().getString(R.string.course_ask_step0));
			btNext.setVisibility(View.VISIBLE);
			btNext.setText(getResources().getString(R.string.course_btn_step0));
			btWifi.setVisibility(View.GONE);
			
		}else if(step == 1){
			tvTextAsk.setVisibility(View.VISIBLE);
			tvTextAsk.setText(getResources().getString(R.string.course_ask_step1));
			btNext.setVisibility(View.VISIBLE);
			btNext.setText(getResources().getString(R.string.course_btn_step1));
			btWifi.setVisibility(View.GONE);
			
		}else if(step == 2){
			tvTextAsk.setVisibility(View.VISIBLE);
			tvTextAsk.setText(getResources().getString(R.string.course_ask_step2));
			btNext.setVisibility(View.VISIBLE);
			btNext.setText(getResources().getString(R.string.course_btn_set_ok));
			btWifi.setVisibility(View.VISIBLE);
		}else{
			Log.e("123", "show btn errror step");
		}
	}
	
	
	@SuppressLint("NewApi")
	private void showCourse(){
		if(step==0){
			tvTop.setText(getResources().getString(R.string.course_top));
			tvText1.setText(getResources().getString(R.string.course_text1_step0));
			tvText2.setText(getResources().getString(R.string.course_text2_step0));
			tvText3.setText(getResources().getString(R.string.course_text3_step0));
			ivWifi.setVisibility(View.GONE);
//			tvTextAsk.setVisibility(View.GONE);
			tvTextAsk.setText(getResources().getString(R.string.course_ask_step0));
//			btNext.setVisibility(View.GONE);
			btNext.setText(getResources().getString(R.string.course_btn_step0));
			btWifi.setVisibility(View.GONE);
			rl.setBackgroundColor(getResources().getColor(R.color.course_gray_step0));
			gif.setMovieResource(R.raw.step_1_view);
			gif.setPaused(false);
		}else if(step==1){
			tvTop.setText(getResources().getString(R.string.course_last_step));
			tvText1.setText(getResources().getString(R.string.course_text1_step1));
			tvText2.setText(getResources().getString(R.string.course_text2_step1));
			tvText3.setText(getResources().getString(R.string.course_text3_step1));
			ivWifi.setVisibility(View.GONE);
//			tvTextAsk.setVisibility(View.GONE);
			tvTextAsk.setText(getResources().getString(R.string.course_ask_step1));
//			btNext.setVisibility(View.GONE);
			btNext.setText(getResources().getString(R.string.course_btn_step1));
			btWifi.setVisibility(View.GONE);
			rl.setBackgroundColor(getResources().getColor(R.color.course_gray_step1));
			gif.setMovieResource(R.raw.step_2_view);
			gif.setPaused(false);
		}else if(step == 2){
			rl.setBackgroundColor(getResources().getColor(R.color.course_gray_step1));
			tvTop.setText(getResources().getString(R.string.course_last_step));
			tvText1.setText(getResources().getString(R.string.course_text1_step2));
			tvText2.setText(getResources().getString(R.string.course_text2_step2));
			tvText3.setText(getResources().getString(R.string.course_text3_step2));
			ivWifi.setVisibility(View.VISIBLE);
//			tvTextAsk.setVisibility(View.GONE);
			tvTextAsk.setText(getResources().getString(R.string.course_ask_step2));
//			btNext.setVisibility(View.GONE);
			btNext.setText(getResources().getString(R.string.course_btn_set_ok));
//			btWifi.setVisibility(View.GONE);
			btWifi.setVisibility(View.VISIBLE);
			Log.i("123", "show course");
			gif.setMovie(null);
			
//			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.raw.course_wifi);
//			Log.i("123", "w: "+bitmap.getWidth()+"   h: "+bitmap.getHeight());
//			gif.setBitmap(bitmap);
//			gif.showBitmap();
//			Thread thread = new Thread(){
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					gif.postInvalidate();
//					
//					super.run();
//				}
//			};
//			thread.run();
			
//			gif.setMovieResource(R.drawable.course_wifi);
//			gif.setBackground(getResources().getDrawable(R.drawable.course_wifi));
		}else if(step == 3){
			finish();
		}else{
			Log.e("123", "show course error step");
		}
		
		mpPlay();
		
		
	}
	
	
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mpStop();
		super.onDestroy();
	}
	
	
	private void mpLoad(){//加载
		if (mp==null) {
			mp = new MediaPlayer();
			mp.setOnCompletionListener(this);
		}
		mp.reset();
		try {
			mp.setDataSource(data.get(step).getFd(),data.get(step).getStartOffset(),data.get(step).getLen());
			mp.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void mpStop(){
		if (mp!=null) {
			mp.stop();
			mp.release();
			mp=null;
		}
	}
	
	private void mpPlay(){
		if (step > 2) {
			Log.e("123", "all played");
			return;
		}
		
		mpLoad();
		mp.start();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
		
		handler.sendEmptyMessage(COURSE_MSG_STEP_FINISH);
	}
	
	private List<MyMusic> initData(){
		List<MyMusic> data = new ArrayList<MyMusic>();
		
		data.add(new MyMusic(this.getResources().openRawResourceFd(R.raw.step_1)));
		data.add(new MyMusic(this.getResources().openRawResourceFd(R.raw.step_2)));
		data.add(new MyMusic(this.getResources().openRawResourceFd(R.raw.step_3)));
		return data;
	}
	
	public class MyMusic{
		AssetFileDescriptor fd;
		public MyMusic(AssetFileDescriptor fd){
			this.fd = fd;
		}
		public FileDescriptor getFd(){
			return this.fd.getFileDescriptor();
		}
		public long getStartOffset(){
			return fd.getStartOffset();
		}
		public long getLen(){
			return fd.getLength();
		}
		
	}
	
}
