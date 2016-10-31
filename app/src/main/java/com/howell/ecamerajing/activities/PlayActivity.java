package com.howell.ecamerajing.activities;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.howell.ecamerajing.action.PlayLocalHW;
import com.howell.ecamerajing.action.PlayLocalMP;
import com.howell.ecamerajing.action.PlayPlayback;
import com.howell.ecamerajing.action.PlayReview;
import com.howell.ecamerajing.action.YV12Renderer;
import com.howell.ecamerajing.base.PlayBaseAction;
import com.howell.ecamerajing.bean.NetRectFileItem;
import com.howell.ecamerajing.bean.SeekBarManager;
import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamerajing.utils.DialogUtil;
import com.howell.ecamerajing.utils.FileUtil;
import com.howell.ecamjing.R;

public class PlayActivity extends Activity implements OnSeekBarChangeListener,
Constable,Callback,OnBufferingUpdateListener,OnCompletionListener,OnGestureListener{

	private static final int PROGRESS_CHANGE 				= 0xa1;	//回放进度条改变
	private static final int SHOW_FINISH_DIALOG 			= 0xa2;	//UI线程显示对话框
	private static final int LOCAL_AVI_PLAY_POS_CHANGE   	= 0xa3;//本地播放进度条改变
	private static final int LOCAL_HW_PLAY_POS_CHANGE		= 0xa4;
	private static final int LOCAL_AVI_PLAY_POS_SET			= 0xa5;
	private static final int LOCAL_HW_PLAY_POS_SET			= 0xa6;
	private static final int MSG_PROGRESS_HIDE				= 0xa7;
	
	//common
	
	private YV12Renderer renderer;
	private GLSurfaceView mGlView;	//opengl surfaceview
	private SeekBar seekbar;		//录像文件进度条
	private RelativeLayout controlLayout,playLayout;
	private LinearLayout backLayout,fullLayout;
	private TextView tvPlayed,tvTotal,tvName;
	private ImageView ivPlay ,ivFull;
	
	
	private int progress = 0;
	
	private GestureDetector mGestureDetector;
	
	private int playMode; //0 预览 1回放 2本地播放	
	private PlayBaseAction playAction;
	private NetRectFileItem netRectFileItem;	//录像文件结构体
	
	private PlayCtrlLayoutManager ctrlMgr;
	
	
	
	
	
	//	private int isPlayBack;		//预览还是回放 

	//	private ECamAPJNI jni;		//jni接口
	private SeekBarManager sbManager;	//管理控制进度条
	
	private String localFile = "";
	private boolean isPlayPause = false;
	
	
	private boolean isAudioConnected 	= false;
	private boolean isCamPlay 			= false;
	private boolean isCamLogin			= false;
	private boolean bAsyncOK			= true;
	private Dialog waitDialog			= null;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		ctrlMgr = new PlayCtrlLayoutManager();
		mGestureDetector = new GestureDetector(this,this);
		mGlView = (GLSurfaceView)findViewById(R.id.glsurface_view);
		mGlView.setDrawingCacheEnabled(true);
		mGlView.setEGLContextClientVersion(2);
		renderer = new YV12Renderer(this,mGlView,handler);
		mGlView.setRenderer(renderer);
		mGlView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		mGlView.getHolder().addCallback((Callback)this);
		mGlView.getHolder().setKeepScreenOn(true);
		playModeInit();
		
	}

	@Deprecated
	private void screenshot(){
		if(playMode!=PLAY_MODE_PALYBACK){
			return;
		}
		
		
//		Bitmap bitmap = mGlView.getDrawingCache();
		int bitmapWidth = netRectFileItem.getBitmapWidth();
		int bitmapHeight = netRectFileItem.getBitmapHeight();
		 Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
		 mGlView.draw(new Canvas(bitmap));

		if (bitmap==null) {
			Log.e("123", "screenshot error");
			return;
		}
		if (netRectFileItem==null) {
			Log.e("123", "net rect file item == null");
			return;
		}
		if (!netRectFileItem.isBitmapExists()) {			
			FileUtil.saveImageBufToDisk(bitmap, netRectFileItem.getFileNameToString(),netRectFileItem.getBitmapWidth(),netRectFileItem.getBitmapHeight());
		}
	}
	
	private void playModeInit(){
		Intent intent = getIntent();
		playMode = intent.getIntExtra("playMode", 0);
		switch (playMode) {
		case PLAY_MODE_REVIEW:
		{
			controlLayout.setVisibility(View.GONE);
			ctrlMgr.setUse(false);
			netRectFileItem = new NetRectFileItem();
			playAction = PlayReview.getInstance();
			playAction.setContext(this).setHandler(handler).setNetRectFileItem(netRectFileItem).setRenderer(renderer);
			
		}
			break;
		case PLAY_MODE_PALYBACK://FIXME
		{
			netRectFileItem = (NetRectFileItem) intent.getSerializableExtra("netRectFileItem");
//			seekbar.setVisibility(View.VISIBLE);
//			if(netRectFileItem != null){
//				Log.e("netRectFileItem", netRectFileItem.toString());
//				sbManager = new SeekBarManager(netRectFileItem);
//				seekbar.setMax(sbManager.getTotalSeconds());
//			}
//			seekbar.setOnSeekBarChangeListener(this);
//			handler.sendEmptyMessage(PROGRESS_CHANGE);
//			PlayTask t = new PlayTask();
//			t.execute(1);
			
			ctrlMgr.setUse(true);
			seekbar.setOnSeekBarChangeListener(this);
			controlLayout.setVisibility(View.VISIBLE);
			playAction = PlayPlayback.getInstance();
			playAction.setContext(this).setHandler(handler).setNetRectFileItem(netRectFileItem).setRenderer(renderer);
			playAction.setItem(netRectFileItem);
		}
			break;	
		case PLAY_MODE_LOCAL_AVI:
		{
			seekbar.setOnSeekBarChangeListener(this);
			localFile = intent.getStringExtra("localFile");
			controlLayout.setVisibility(View.VISIBLE);
			ctrlMgr.setUse(true);
			playAction = PlayLocalMP.getInstance();
			playAction.setContext(this).setHandler(handler).setFileName(localFile).setmGlView(mGlView).setRenderer(renderer);
			
		}
			break;
		case PLAY_MODE_LOCAL_HW:
		{
			seekbar.setOnSeekBarChangeListener(this);
			localFile = intent.getStringExtra("localFile");
			controlLayout.setVisibility(View.GONE);
			ctrlMgr.setUse(true);
			playAction = PlayLocalHW.getInstance();
			playAction.setContext(this).setHandler(handler).setFileName(localFile).setRenderer(renderer);

		}
			break;
		default:
			break;
		}
	}
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏
			ctrlMgr.setbFull(false);
			playAction.setVideoFullMax(0, 0);
		}else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			ctrlMgr.setbFull(true);
			playAction.setVideoFullMin(0, 0);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {	
			Log.i("123", "KEYCODE_BACK");
			playAction.stopVideo();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			//set time
			this.progress = progress;
			if(playMode!= PLAY_MODE_PALYBACK){
				ctrlMgr.setPlayedTime(playAction.getVideoPlayedSec());
				Message msg = new Message();
				msg.what = PLAY_MSG_SET_POS;
				msg.arg1 = progress;
				handler.sendMessage(msg);
			}else{
				Log.i("123", "progress = "+progress+"   "+ctrlMgr.sec2FormatTime(progress*1000));
				ctrlMgr.setPlayedTime(progress*1000);
				playAction.setVideoPos(progress);
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		handler.removeMessages(PLAY_MSG_PLAY_PROGRESS);
//		startTimeTask();
		if(playMode == PLAY_MODE_PALYBACK){
			playAction.rePlayStop();
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {

		if (playMode == PLAY_MODE_PALYBACK) {
			playAction.rePlayVideo();
		}else{
			handler.sendEmptyMessage(PLAY_MSG_PLAY_PROGRESS);
		}
	}




	
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
			case PLAY_MSG_LOGIN_ERROR:
			{
				new AlertDialog.Builder(PlayActivity.this)   
				.setMessage("登录失败")                 
				.setPositiveButton("确定", new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						finish();
					}
				})   
				.show();
			}
			break;
			
			case PLAY_MSG_PLAY_PREPARE:
			{
				//设置信息
				
				Log.i("log123","arg1="+msg.arg1);
				if(msg.arg1 == PLAY_VAL_NONEED_ASNYC){
					ctrlMgr.init();
				}else if(msg.arg1 == PLAY_VAL_NEED_ASNYC){
					Log.i("log123","PLAY_VAL_NEED_ASNYC");
					ctrlMgr.setbAsynHide(true);
					controlLayout.setVisibility(View.GONE);
					bAsyncOK = false;
					
					//wait dailog show
					waitDialog = DialogUtil.postWaitDialog(PlayActivity.this);
					waitDialog.show();
				}
				
				
				//播放
				playAction.playVideo();
				
				
				
				handler.sendEmptyMessage(PLAY_MSG_PLAY_PROGRESS);
				handler.sendEmptyMessageDelayed(PLAY_MSG_HIDE_CONTROL, 3000);
				isPlayPause = false;
			}
			break;
			
			case PLAY_MSG_PLAY_PROGRESS:
			{
//				Log.i("123","get vid pos="+playAction.getVideoPos()+" max="+playAction.getVideoTotalPos());
				
				
				ctrlMgr.setPos(playAction.getVideoPos());
				ctrlMgr.setPlayedTime(playAction.getVideoPlayedSec());
				
				
				handler.sendEmptyMessageDelayed(PLAY_MSG_PLAY_PROGRESS, 200);
//				int sec = playAction.getVideoTotalSec();
//				Log.i("123","PLAY_MSG_PLAY_PROGRESS :"+ctrlMgr.sec2FormatTime(sec));
			}
			break;
			
			case PLAY_MSG_SET_POS:
			{
				playAction.setVideoPos(msg.arg1);
			}
			break;
			
			case PLAY_MSG_HIDE_CONTROL:
			{
				//FIXME
				controlLayout.setVisibility(View.GONE);
			}
			break;
			
			case PLAY_MSG_ASNYC_INFO:
			{
				ctrlMgr.init();
				waitDialog.cancel();
			}
			break;
			case PLAY_MSG_GET_FRAME:
			{
				screenshot();
			}
			break;
			case PLAY_MSG_PLAY_ERROR:
			{
				Log.e("123","get msg play error");
				handler.removeMessages(PLAY_MSG_PLAY_PROGRESS);
				Toast.makeText(PlayActivity.this, "播放失败 ", Toast.LENGTH_SHORT).show();
			}
			break;
			default:
				break;
			}
		};
	};
	

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
		playAction.init();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.i("123", "surface change width="+width+" height="+height);
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
//		stopMP();
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {

	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Toast.makeText(this, "play end", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {

		boolean bShow = ctrlMgr.isbCtrlLayoutShow();
		if (isPlayPause) {//当前是暂停的
			ctrlMgr.setbCtrlLayoutShow(!bShow);	
		}else{
			ctrlMgr.setbCtrlLayoutShow(!bShow);	
			if (!bShow) {
				playAction.pauseVideo(true);
				isPlayPause = true;
			}
			
		}
		
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		handler.removeMessages(LOCAL_AVI_PLAY_POS_CHANGE);
		handler.removeMessages(PROGRESS_CHANGE);
		handler.removeMessages(SHOW_FINISH_DIALOG);
		handler.removeMessages(LOCAL_HW_PLAY_POS_CHANGE);
		handler.removeMessages(PLAY_MSG_PLAY_PROGRESS);
		super.onDestroy();
	}
	

	
	
	private class PlayCtrlLayoutManager implements View.OnClickListener{
		private int totalSec;
		private boolean bUsed;
		private boolean bCtrlLayoutShow;
		private boolean bAsynHide = false;//未同步信息前先隐藏
		private boolean bFull = false;
		
		public PlayCtrlLayoutManager(){
			controlLayout = (RelativeLayout)findViewById(R.id.play_control_common);
			tvPlayed = (TextView)findViewById(R.id.play_tv_playedsec);
			tvTotal = (TextView)findViewById(R.id.play_tv_totalsec);
			seekbar = (SeekBar)findViewById(R.id.player_seekbar);	
			tvName = (TextView)findViewById(R.id.play_tv_name);
			playLayout = (RelativeLayout)findViewById(R.id.play_control_center);
			playLayout.setOnClickListener(this);
			backLayout = (LinearLayout)findViewById(R.id.play_ll_back);
			backLayout.setOnClickListener(this);
			fullLayout = (LinearLayout)findViewById(R.id.play_ll_full);
			fullLayout.setOnClickListener(this);
			ivPlay = (ImageView)findViewById(R.id.play_iv_play);
			ivPlay.setOnClickListener(this);
			ivFull = (ImageView)findViewById(R.id.play_iv_full);
			
		}
		
		public void setUse(boolean bUse){
			this.bUsed = bUse;
		}
		
		public boolean isbCtrlLayoutShow() {
			return bCtrlLayoutShow;
		}
		
		public boolean isbAsynHide() {
			return bAsynHide;
		}

		public void setbAsynHide(boolean bAsynHide) {
			this.bAsynHide = bAsynHide;
		}

		public boolean isbFull() {
			return bFull;
		}

		@SuppressLint("NewApi")
		public void setbFull(boolean bFull) {
			this.bFull = bFull;
			int id = 0;
			if (bFull) {//显示为fullbtn   横屏
				id = R.mipmap.btn_play_fullmax;
			}else{
				id = R.mipmap.btn_play_fullmin;
			}
			ivFull.setBackground(PlayActivity.this.getResources().getDrawable(id));
		}

		public void setbCtrlLayoutShow(boolean bCtrlLayoutShow) {
			if (!bUsed)    return;
			if (bAsynHide) return;
			this.bCtrlLayoutShow = bCtrlLayoutShow;
			if (bCtrlLayoutShow) {
				playLayout.setVisibility(View.VISIBLE);
				setIvPlay(true);
				handler.removeMessages(PLAY_MSG_HIDE_CONTROL);
			}
			controlLayout.setVisibility(bCtrlLayoutShow?View.VISIBLE:View.GONE);
		}

		public String sec2FormatTime(int msec){
//			Date date = new Date(0);
//			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//			date.setTime(msec);
//			String formatTime = sdf.format(date);
			
			long sec = msec/1000;
			
			int secVal = (int) (sec%60);
			int minVal = (int) (sec/60);
			String minStr;
			if (minVal<100) {
				minStr = String.format("%02d", minVal);				
			}else{
				minStr = minVal+"";
			}
			String secStr = String.format("%02d", secVal);
			return minStr+":"+secStr;
		}
		
		public void init(){
			if (!bUsed) {
				return;
			}
			bAsynHide = false;
			Log.e("123", "ctrlmgr  init");
			tvName.setText(FileUtil.getNameOfVideo(localFile));	
			tvTotal.setText(sec2FormatTime(playAction.getVideoTotalSec()));
			seekbar.setMax(playAction.getVideoTotalPos());
		}
		
		public int getPos(){
			if (!bUsed) {
				return 0;
			}
			return seekbar.getProgress();
		}
		
		public void setPos(int pos){
			if (!bUsed) {
				return;
			}
			seekbar.setProgress(pos);
		}
		
		public void setPlayedTime(int msec){
			if (!bUsed) {
				return;
			}
			tvPlayed.setText(sec2FormatTime(msec));
		}
		public void hidePlayLayout(){
			if (!bUsed) {
				return;
			}
			playLayout.setVisibility(View.GONE);
			setIvPlay(false);
		}
		
		@SuppressLint("NewApi")
		public void setIvPlay(boolean bPlay){
			int id = 0;
			if (bPlay) {//set play icon
				id = R.mipmap.btn_play_play;
				
			}else{
				id = R.mipmap.btn_play_pause;
			}
			ivPlay.setBackground(PlayActivity.this.getResources().getDrawable(id));
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.play_control_center:
			{
				if (isPlayPause) {
					//播放
					isPlayPause = false;
					playAction.pauseVideo(false);
					hidePlayLayout();
					handler.sendEmptyMessageDelayed(PLAY_MSG_HIDE_CONTROL, 5000);
				}
			}
				break;
			case R.id.play_ll_back:
			{
				playAction.stopVideo();
				finish();
			}
			
			case R.id.play_ll_full:
			{
				setbFull(!bFull);
				if (bFull) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//
					
				}else{
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横
				}	
			}
			break;
			default:
				break;
			}
		}
	}	
}
