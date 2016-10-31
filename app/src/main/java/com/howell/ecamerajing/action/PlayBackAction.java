package com.howell.ecamerajing.action;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.howell.ecamerajing.bean.NetRectFileItem;
import com.howell.ecamerajing.bean.Pagination;
import com.howell.ecamerajing.bean.Record;
import com.howell.ecamerajing.bean.SystimeInfo;
import com.howell.ecamerajing.service.DownLoadService;
import com.howell.ecamerajing.utils.Constable;
import com.howell.jni.CamJniUtil;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class PlayBackAction implements Constable{
	private static PlayBackAction mInstance = null;
	private JpgAction jpgAction = null;
	private int num=0;
	private PlayBackAction(){
		jpgAction = JpgAction.getInstance();
	}


	public static PlayBackAction getInstance(){
		if (mInstance==null) {
			mInstance = new PlayBackAction();
		}
		return mInstance;
	}
	private int fileHandle;	//获取录像列表的句柄
	private int userHandle;	//登录摄像机的句柄
	private Context context;
	private Handler handler;
	private NetRectFileItem[] netRectFileItem;
	private NetRectFileItem cur_item = null;//jpg
	private byte [] jpgBuf = new byte[128*1024];
	private int itemSize = 0;

	public Context getContext() {
		return context;
	}
	public PlayBackAction setContext(Context context) {
		this.context = context;
		return this;
	}
	public Handler getHandler() {
		return handler;
	}
	public PlayBackAction setHandler(Handler handler) {
		this.handler = handler;
		return this;
	}

	public NetRectFileItem[] getNetRectFileItem() {
		return netRectFileItem;
	}
	public int getItemSize() {
		return itemSize;
	}
	public void setItemSize(int itemSize) {
		this.itemSize = itemSize;
	}
	@SuppressWarnings("unused")
	public void getPlayBackList(Pagination page){
		if (page.page_no>=page.page_count && page.page_no!=0) {
			page.setCur_size(0);
			setItemSize(0);
			handler.sendEmptyMessage(PLAYBACK_MSG_LIST_PREPARE);
			return;
		}

		if (DEBUG_TEST==0) {
			GetPlayBackListByPageTask task = new GetPlayBackListByPageTask(page);
			task.execute();
		}else{
			test2();
		}
		
		
		//		test(page);//FIXME

	}


	@Deprecated
	public void getPlayBackJpg(int index){		
		jpgAction.setCurItem(netRectFileItem[index]);
		GetPlayBackJpgTask task = new GetPlayBackJpgTask();
		task.execute(index);
	}

	private boolean test_once = false;
	private void test2(){
		if (test_once) {
			setItemSize(0);
			handler.sendEmptyMessage(PLAYBACK_MSG_LIST_PREPARE);
			return;
		}
		test_once = true;
		SystimeInfo beg = new SystimeInfo();
		SystimeInfo end = new SystimeInfo();
		beg.setwYear((short)2016);
		beg.setwMonth((short)4);
		beg.setwDay((short)12);
		beg.setwDayofWeek((short)2);
		beg.setwHour((short)8);
		beg.setwMinute((short)0);
		beg.setwSecond((short)0);
		beg.setwMilliseconds((short)0);

		end.setwYear((short)2016);
		end.setwMonth((short)4);
		end.setwDay((short)12);
		end.setwDayofWeek((short)2);
		end.setwHour((short)23);
		end.setwMinute((short)0);
		end.setwSecond((short)0);
		end.setwMilliseconds((short)0);


		GetPlayBackListByTimeTask task = new GetPlayBackListByTimeTask(beg,end);
		task.execute();
	}

	private void test(Pagination page){
		int len=0;
		page.setPage_count(2);
		page.setTotal_size(14);
		Log.i("123", "page_no="+page.page_no+ " page_count="+page.page_count);
		if (page.page_no >= page.page_count) {
			page.setCur_size(0);
			handler.sendEmptyMessage(PLAYBACK_MSG_LIST_PREPARE);
			return;
		}

		if (page.page_no==0) {
			num = 0;
			len = 10;
			page.setCur_size(10);
		}else if (page.page_no==1) {
			len = 4;
			page.setCur_size(4);
		}else{
			Log.e("123", "page pageno="+page.page_no);
		}

		netRectFileItem = new NetRectFileItem[10];

		for(int i=0;i<len;i++){
			netRectFileItem[i] = new NetRectFileItem();
			netRectFileItem[i].setBegYear((short)(2010+num));
			netRectFileItem[i].setBegMonth((short)4);
			netRectFileItem[i].setBegDay((short)5);
			netRectFileItem[i].setBegHour((short)13);
			netRectFileItem[i].setBegMinute((short)15);
			netRectFileItem[i].setBegSecond((short)52);
			netRectFileItem[i].setFileNo(i);
			netRectFileItem[i].setOffsetSeconds(0);
			netRectFileItem[i].setTotalFrames(15642);
			netRectFileItem[i].setTotalSeconds(3600);
			num++;
		}

		handler.sendEmptyMessage(PLAYBACK_MSG_LIST_PREPARE);
	}


	public void startDownloadService(NetRectFileItem item){
		Intent intent = new Intent(context,DownLoadService.class);
		Bundle bundle = new Bundle();

		bundle.putSerializable("item", item);
		intent.putExtra("downLoadInfo", bundle);

		context.startService(intent);
	}

	public void stopDownLoadService(){
		Intent intent = new Intent(context,DownLoadService.class);
		context.stopService(intent);
	}
	private Record getRecordInfo(){
		Record record =  new Record();

		SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyy年MM月dd日   HH:mm:ss     ");     
		Date   curDate   =   new   Date(System.currentTimeMillis());//获取当前时间     
		SimpleDateFormat   format   =   new   SimpleDateFormat   ("yyyy"); 
		String   str   =   format.format(curDate); 
		int year = Integer.parseInt(str);
		format = new SimpleDateFormat("MM");
		str = format.format(curDate);
		int month = Integer.parseInt(str);
		format = new SimpleDateFormat("dd");
		str = format.format(curDate);
		int day = Integer.parseInt(str);
		format = new SimpleDateFormat("HH");
		str = format.format(curDate);
		int hour = Integer.parseInt(str);
		format = new SimpleDateFormat("mm");
		str = format.format(curDate);
		int min = Integer.parseInt(str);
		format = new SimpleDateFormat("ss");
		str = format.format(curDate);
		int sec = Integer.parseInt(str);

		record.setBegYear((short)(year-1));
		record.setBegMonth((short)month);
		record.setBegDay((short)day);
		record.setBegHour((short)hour);
		record.setBegMinute((short)min);
		record.setBegSecond((short)sec);
		

		record.setEndYear((short)(year));
		record.setEndMonth((short)month);
		record.setEndDay((short)day);
		record.setEndHour((short)hour);
		record.setEndMinute((short)min);
		record.setEndSecond((short)sec);
		
		
		
		
		return record;
	}


	private class GetPlayBackListByPageTask extends AsyncTask<Void, Void, Boolean>{
		private Pagination pagination;


		public GetPlayBackListByPageTask(Pagination pagination) {
			this.pagination = pagination;
		}


		@Override
		protected Boolean doInBackground(Void... params) {
			// 1) login
			int uh = -1;
			if ((uh=CamJniUtil.cameraLogin(TEST_IP))==-1) {
				Log.e("123", "get play back list task login error");
				return false;
			}else{
				userHandle = uh;
			}
			// 2)stop record
			if (CamJniUtil.stopRecord(uh, 0)==0) {
				Log.e("123", "get play back list task stop record error");
//				return false;
			}
			// 3) set list info from page
			int fh = -1;
			Record record = getRecordInfo();
			if((fh =CamJniUtil.getListByPage(userHandle, 0, 1,record, 0, 1, pagination))==-1){
				Log.e("123", "get play back list task get list by page error");
				return false;
			}else{
				fileHandle = fh;
			}
			Log.i("123", "cur size="+pagination.cur_size);
			//set size
			setItemSize(pagination.cur_size);
			// 4) get list 
			netRectFileItem = CamJniUtil.getReplayList(fileHandle,pagination.cur_size);
			if (netRectFileItem == null) {
				Log.e("123", "get play back list task get file item error");
				return false;
			}else{
				CamJniUtil.closeFileList(fileHandle);
				CamJniUtil.cameraLogout();
				fileHandle = -1;
				userHandle = -1;
			}
			return true;
		}


		@Override
		protected void onPostExecute(Boolean result) {

			if (result) {
				handler.sendEmptyMessage(PLAYBACK_MSG_LIST_PREPARE);
			}else{
				//get play back list task error
				Log.e("123", "get play back list task error");
				handler.sendEmptyMessage(PLAYBACK_MSG_GETLIST_ERROR);
			}
			super.onPostExecute(result);
		}
	}

	private class GetPlayBackListByTimeTask extends AsyncTask<Void, Void, Boolean>{
		private SystimeInfo beg,end;

		public GetPlayBackListByTimeTask(SystimeInfo beg,SystimeInfo end){
			this.beg = beg;
			this.end = end;
		}


		@Override
		protected Boolean doInBackground(Void... params) {
			int uh = -1;
			if ((uh=CamJniUtil.cameraLogin(TEST_IP))==-1) {
				Log.e("123", "get play back list task login error");
				return false;
			}else{
				Log.i("123", "play back list login ok");
				userHandle = uh;
			}
			// 2)stop record
			//			if (CamJniUtil.stopRecord(uh, 0)==0) {
			//				Log.e("123", "get play back list task stop record error");
			//				return false;
			//			}
			// 3) set list info from page
			int fh = -1;
			if((fh =CamJniUtil.getFileList(uh, 0, beg, end))==-1){
				Log.e("123", "get play back list task get list by page error");
				return false;
			}else{
				fileHandle = fh;
			}
			int count = CamJniUtil.getFileListCount(fileHandle);
			Log.i("123", "file list count = "+count);
			//set size
			setItemSize(count);
			// 4) get list 
			netRectFileItem = CamJniUtil.getReplayList(fileHandle,count);
			if (netRectFileItem == null) {
				Log.e("123", "get play back list task get file item error");
				return false;
			}else{
				CamJniUtil.closeFileList(fileHandle);
				CamJniUtil.cameraLogout();
				fileHandle = -1;
				userHandle = -1;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				handler.sendEmptyMessage(PLAYBACK_MSG_LIST_PREPARE);
			}else{
				//get play back list task error
				Log.e("123", "get play back list task error");
				handler.sendEmptyMessage(PLAYBACK_MSG_GETLIST_ERROR);
			}
			super.onPostExecute(result);
		}

	}

	public void startRecord(){
		new AsyncTask<Void, Void, Void>(){
			
			@Override
			protected Void doInBackground(Void... params) {
				int uh = -1;
				if ((uh=CamJniUtil.cameraLogin(TEST_IP))!=-1) {
					if(CamJniUtil.startRecord(uh, 0)==0){
						Log.e("123", "start record error");
					}
				}
				return null;
			}
			protected void onPostExecute(Void result) {
				CamJniUtil.cameraLogout();
				
			};
		}.execute();
	}
	
	
	public class GetPlayBackJpgTask extends AsyncTask<Integer, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Integer... params) {

			return jpgAction.getPlayBackJpg(userHandle, netRectFileItem[params[0]]);
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				handler.sendEmptyMessage(PLAYBACK_MSG_JPG_REFRESH);
			}

			super.onPostExecute(result);
		}
	}

}
