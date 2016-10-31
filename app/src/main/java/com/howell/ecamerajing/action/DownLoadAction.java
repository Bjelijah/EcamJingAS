package com.howell.ecamerajing.action;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.howell.ecamerajing.bean.NetRectFileItem;
import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamerajing.utils.DownLoadUtil;
import com.howell.ecamerajing.utils.FileUtil;
import com.howell.jni.CamJniUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import junit.framework.Test;



public class DownLoadAction implements Constable{


	private long  fileTotalSize = 0;//文件总大小// 下载开始后  jni回调
	private long fileCurSize = 0;//文件当前下载了的大小

	private int fileTotalFrame = 0;//文件总帧数

	private int bufSize = 3*1024*1024;//最大值          jni 写当前buf size
	private int lastBufSize = 0;
	private byte [] buf=new byte[bufSize];
	//	private char [] charBuf = new char [bufSize];
	private long timeStemp = 0;
	private long lastTime = 0;
	private long firstTime = 0;//由jni 第一帧时间戳 回调
	private long totalSec = 0;
	private long totalFrameLen = 0;
	private int frameLen = 0;
	private RandomAccessFile vfile = null;
	private Context context;
	private NetRectFileItem item;
	private boolean isDownloadOver = false;
	private String testbyteToString(){
		String string = "";
		for(int i=0;i<10;i++){
			string += buf[i];
		}
		return string;
	}
	public DownLoadAction(Context context) {
		this.context = context;
		downloadInit();
	}


	public NetRectFileItem getItem() {
		return item;
	}
	public void setItem(NetRectFileItem item) {
		this.item = item;
	}
	public long getTotalSec() {
		return totalSec;
	}
	public void setTotalSec(long totalSec) {
		this.totalSec = totalSec;
	}
	public void downloadInit(){
		Log.i("123", "download init");
		doOnce = true;
		timeStemp = 0;
		firstTime = 0;
		totalSec = 0;
		isDownloadOver = false;
		CamJniUtil.downloadInit();

		CamJniUtil.downloadSetCallbackObject(this, 0);
		CamJniUtil.downloadSetCallbackFieldName("fileTotalSize", 0);

		CamJniUtil.downloadSetCallbackFieldName("fileCurSize", 1);


		CamJniUtil.downloadSetCallbackFieldName("bufSize", 2);
		CamJniUtil.downloadSetCallbackFieldName("buf", 3);
		CamJniUtil.downloadSetCallbackFieldName("firstTime", 4);
		CamJniUtil.downloadSetCallbackMethodName("write2File", 0);
	}



	public void downloadRelease(){
		Log.e("123", "download release");
		CamJniUtil.downloadDeinit();
		CamJniUtil.cameraLogout();
	}
	@Deprecated
	public void setDownloadInfo(){

	}

	private int getDownLoadFileCurSize(){
		fileCurSize = CamJniUtil.downloadGetPos();
		return (int)fileCurSize;
	}

	private long getDownloadFileCurTimestamp(){
		timeStemp = CamJniUtil.downloadGetCutTimeStamp();
		return timeStemp;
	}

	@SuppressWarnings("unused")
	public String getDownLoadFileinfo(){ // 下载大小   总大小





		if(fileTotalSize==0){
			return "等待中";
		}else{
			String speedStr = DownLoadUtil.getDownloadSpeed(context);
			if(speedStr==null){
				return "下载失败";
			}

			if(DEBUG_TEST==1){
				return (getDownLoadFileCurSize()/1024/1024)+"M"+"  |  "+ (fileTotalSize/1024/1024)+"M" + "        "+
						speedStr;
			}else{
				return "下载中:"+((timeStemp - firstTime) / (totalSec*1000*10))+"%"+"         "+speedStr;
			}
		}
	}

	private boolean doOnce = true;
	private int timeNum = 10;
	@SuppressWarnings("unused")
	public int getProgressPos(){
		
		//		Log.i("123", DownLoadUtil.getDownLoadCurTime(curTime));
		//		if (fileTotalSize==0) {
		//			return 0;
		//		}
		//		if(frameLen==0){
		//			frameLen = CamJniUtil.downloadGetFrameLen();
		//			if (frameLen!=0) {
		//				totalFrameLen = frameLen * item.getTotalFrames();
		//				if(fileTotalSize==0){
		//					fileTotalSize = totalFrameLen;
		//				}
		//			}
		//		}
		//		Log.i("123", DownLoadUtil.getDownLoadCurTime(curTime/1000)+ " curTime=  "+curTime+
		//				" timestemp="+timeStemp+"   firstTime="+firstTime);
		//		Log.i("123", "total sec="+DownLoadUtil.getDownLoadCurTime(totalSec*1000));
		if(DEBUG_TEST==1){
			if (fileTotalSize == 0) {
				return 0;
			}
			if(isDownloadOver){
				isDownloadOver = false;
				Log.i("123", "isDownload over");
				return 100;
			}else{
				//				Log.i("123", "fileCurSize/fileTotalSize="+(int)(fileCurSize*100/fileTotalSize));
				return (int)(fileCurSize*100/fileTotalSize);
			}
		}else{
			getDownloadFileCurTimestamp();
//			if(timeStemp !=0 && doOnce){
//				firstTime = timeStemp;
//				frameLen = CamJniUtil.downloadGetFrameLen();
//				fileTotalSize = frameLen * item.getTotalFrames();
//				doOnce = false;
//			}
			if(firstTime!=0){
				frameLen = CamJniUtil.downloadGetFrameLen();
				fileTotalSize = frameLen * item.getTotalFrames();//FIXME 估算
			}
			
			
			long curTime = timeStemp - firstTime;
			if(curTime < 0 ) curTime = 0;
			
			
			if (lastTime==curTime && (((curTime) / (totalSec*1000*10))>98)) {				
				timeNum--;
				if(timeNum<0){
					isDownloadOver = true;
				}
			}
			
			lastTime = curTime;
			
			if (totalSec == 0) {
				return 0;
			}
			if(isDownloadOver){
				isDownloadOver = false;
				return 100;
			}else{
				Log.i("123", "curTime="+curTime+"     totalsec="+totalSec+"   pos"+ (int) ((curTime*100) / (totalSec*1000*1000)));
				return (int) ((curTime) / (totalSec*1000*10));//FIXME
				//				return 0;
			}
		}
	}


	@SuppressWarnings("unused")
	public void write2File(){

		//		Log.i("123", "bufsize="+ bufSize + " buf="+testbyteToString());
		//		Log.i("123", "cursize="+fileCurSize+" totsize="+fileTotalSize);
		//		if(fileTotalSize!=0){
		//			Log.i("123", "downlaod: "+ (fileCurSize*100/fileTotalSize)+"%" );
		//			Log.i("123", "byte size="+buf.length);
		//			
		//		}


		if (vfile!=null) {
			try {
				FileUtil.write2VideoFile(vfile,DownLoadUtil.getSubByte(buf, 0, bufSize));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if(DEBUG_TEST==1){
			if (lastBufSize!=bufSize && lastBufSize!=0) {
				//over
				//			Log.i("123", "last buf size="+ lastBufSize+"     bufsize="+bufSize);
				isDownloadOver = true;//仅tcp有效
				//			Log.e("123", "isdownloadove = true");
				return;
			}
			lastBufSize = bufSize;
		}
	}


	public boolean startDownLoad(NetRectFileItem item){
		//新下载任务初始化
		fileTotalSize = 0;
		fileCurSize = 0;
		isDownloadOver = false;
		doOnce  = true;
		timeStemp = 0;
		firstTime = 0;
		timeNum = 10;
		//创建文件
		try {
			vfile = FileUtil.createVideoFile(FileUtil.createVideoFilePathName(item));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}



		DownLoadTask task = new DownLoadTask();
		task.execute(item);
		//		if(CamJniUtil.cameraLogin(TEST_IP)==-1){
		//			return false;
		//		}
		//		if(!CamJniUtil.downloadStart(item)){
		//			return false;
		//		}
		//		

		return true;
	}


	public void stopDownLoad(){//停止下载  （取消当前任务）
		CamJniUtil.downloadStop();
		try {
			FileUtil.closeVideoFile(vfile);
			vfile = null;
		} catch (IOException e) {
	
			e.printStackTrace();
		}
	}

	public class DownLoadTask extends AsyncTask<NetRectFileItem, Void, Boolean>{

		@Override
		protected Boolean doInBackground(NetRectFileItem... params) {

			if(CamJniUtil.cameraLogin(TEST_IP)==-1){
				return false;
			}
			if(!CamJniUtil.downloadStart(params[0])){
				return false;
			}
			return true;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Log.i("123", "start download task ok");

				fileTotalSize = CamJniUtil.downloadGetTotalLen();
				//	frameLen = CamJniUtil.downloadGetFrameLen();

			}else{
				Log.e("123", "start download task errror");
			}
			super.onPostExecute(result);
		}
	}

}
