package com.howell.ecamerajing.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.howell.ecamerajing.action.DownLoadAction;
import com.howell.ecamerajing.bean.NetRectFileItem;
import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamerajing.utils.FileUtil;

import java.util.ArrayList;

public class DownLoadService extends Service implements Constable{

	
	private ArrayList<NetRectFileItem> downloadList;
	private TaskInfoReceive taskInfoReceive;
	private TaskCancelReceive taskCancelReceive;
	private TaskCancelAllReceive taskCancelAllReceive;
	private int progressPos;//下载进度
	private DownLoadAction downLoadAction;

	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what) {
			case DOWNLOAD_SERVICE_MSG_TASK_START:
				downLoad();
				break;
			case DOWNLOAD_SERVICE_MSG_TASK_END:
				//结束任务 发送广播
				sendTaskInfoBroadcast(DOWNLOAD_RECEIVE_ACTION_TASK_PBL);
				sendTaskInfoBroadcast(DOWNLOAD_RECEIVE_ACTION_TASK_DLL);
				stopSelf();
				break;
			case DOWNLOAD_SERVICE_MSG_GET_POS:
				//获取当前进度
				progressPos = getPos();
				if (progressPos>=100) {//下载完成
					downLoadAction.stopDownLoad();
					
					
					handler.removeMessages(DOWNLOAD_SERVICE_MSG_GET_POS);
					downloadList.remove(0);
					//更新ui
					sendTaskInfoBroadcast(DOWNLOAD_RECEIVE_ACTION_TASK_PBL);
					sendTaskInfoBroadcast(DOWNLOAD_RECEIVE_ACTION_TASK_DLL);
				
				
					
					//下载下一个
					downLoad();
					
					
					break;
				}
				
				//pos send to ui  and filesize and curfilesize FIXME
				sendTaskProgressBroadcast();
				
				handler.sendEmptyMessageDelayed(DOWNLOAD_SERVICE_MSG_GET_POS, 500);
				break;
			
			case DOWNLOAD_SERVICE_MSG_SERVICE_END:
			{
				stopSelf();	
			}
				break;
			default:
				break;
			}
		}
	};
	
	private int getPos(){
		int pos=0;
//		pos = progressPos+=1;
		pos = downLoadAction.getProgressPos();
		return pos;
	}
	
	
	private void downLoad(){
		if (downloadList.isEmpty()) {
			//无下载任务；
			handler.sendEmptyMessage(DOWNLOAD_SERVICE_MSG_TASK_END);
			Log.e("123", "downloadlist = empty");
		
			return;
		}
		//下载
		doDownLoad();
	}
	
	private void doDownLoad(){
		/**
		 * 下载任务
		 */
		//init
		progressPos = 0;
		//下载 pos=0
		boolean ret = false;
		downLoadAction.setItem(downloadList.get(0));
		downLoadAction.setTotalSec(downloadList.get(0).getTotalSeconds());
		ret = downLoadAction.startDownLoad(downloadList.get(0));
		
		
		
		if (!ret) {
			Log.e("123", "download start error");
			return;
		}
		
		//
		handler.sendEmptyMessageDelayed(DOWNLOAD_SERVICE_MSG_GET_POS, 1000);
	}
	
	public void cancelAll(){
		handler.removeMessages(DOWNLOAD_SERVICE_MSG_GET_POS);
		if(downloadList.isEmpty()){
			stopSelf();
			return;
		}
		
		NetRectFileItem bar = downloadList.get(0);
		
		
		for(int i=downloadList.size()-1;i>-1;i--){
			Log.i("123", "cancel all i="+i);
			downloadList.remove(i);
			if (i==0) {
				downLoadAction.stopDownLoad();
				FileUtil.delVideoFileCach(FileUtil.getVideoCachePath()+bar.getFileNameToString()+".hw");
				handler.sendEmptyMessageDelayed(DOWNLOAD_SERVICE_MSG_SERVICE_END, 1000);
				break;
			}
			
			
		}
		
		
		
	}
	
	private void cancelDownLoad(int pos){
		//若为当前下载任务则 取消任务
		if (pos<0||pos>downloadList.size()) {
			return;
		}
		NetRectFileItem bar = downloadList.get(pos);
		
		downloadList.remove(pos);
		if (pos==0) {
			//cancel downloading task
			downLoadAction.stopDownLoad();
			//删除下载了一半的文件
			FileUtil.delVideoFileCach(FileUtil.getVideoCachePath()+bar.getFileNameToString()+".hw");
			
			handler.removeMessages(DOWNLOAD_SERVICE_MSG_GET_POS);
			
			
			//start next task
		
			downLoad();
		}
		
		
	}
	
	private void sendTaskProgressBroadcast(){
		Intent intent = new Intent(DOWNLOAD_RECEICVE_ACTION_TASK_PROGRESS);
		intent.putExtra(DOWNLOAD_RECEIVE_KEY_PROGRESS_POS, progressPos);
		intent.putExtra(DOWNLOAD_RECEIVE_KEY_FILE_INFO, downLoadAction.getDownLoadFileinfo());
		sendBroadcast(intent);
	}
	
	
	private void sendTaskInfoBroadcast(String act){
		Intent intent = new Intent(act);
		intent.putExtra(DOWNLOAD_RECEIVE_KEY_TASK_COUNT, downloadList.size());
	
		for(int i=0;i<downloadList.size();i++){
			intent.putExtra("item"+i, downloadList.get(i));
		}
		sendBroadcast(intent);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i("123", "download server bind");
		return null;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.i("123", "download server unbind");
		return super.onUnbind(intent);
	}
	
	private void registerTaskInfoReceive(){
		taskInfoReceive = new TaskInfoReceive();
		IntentFilter filter = new IntentFilter();
		filter.addAction(DOWNLOAD_RECEIVE_ACTION_TASK_INFO);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(taskInfoReceive, filter);
	}
	
	private void registerTaskCancelReceive(){
		taskCancelReceive = new TaskCancelReceive();
		IntentFilter filter = new IntentFilter();
		filter.addAction(DOWNLOAD_RECEIVE_ACTION_TASK_CANCEL);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(taskCancelReceive, filter);
	}
	
	private void registerTaskCancelAllReceive(){
		taskCancelAllReceive = new TaskCancelAllReceive();
		IntentFilter filter = new IntentFilter();
		filter.addAction(DOWNLOAD_RECEIVE_ACTION_TASK_CANCEL_ALL);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(taskCancelAllReceive, filter);
	}
	
	@Override
	public void onCreate() {
		Log.i("123", "download service on create");
		//注册receive
		registerTaskInfoReceive();
		registerTaskCancelReceive();
		registerTaskCancelAllReceive();
		downloadList = new ArrayList<NetRectFileItem>();
		handler.sendEmptyMessageDelayed(DOWNLOAD_SERVICE_MSG_TASK_START, 500);
		downLoadAction = new DownLoadAction(this);
//		downLoadAction.downloadInit();
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.i("123", "flags="+flags+"startid="+startId);
		
		NetRectFileItem item;
		Bundle bundle;
		try {
			bundle = intent.getBundleExtra("downLoadInfo");
			item = (NetRectFileItem)bundle.getSerializable("item");
//			Log.i("123", "service item="+item.toString());
			downloadList.add(item);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//广播 告知service 中 task的个数
		sendTaskInfoBroadcast(DOWNLOAD_RECEIVE_ACTION_TASK_PBL);
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	@Override
	public void onDestroy() {
		Log.e("123", "download service on destroy");
		//注销receive
		unregisterReceiver(taskInfoReceive);
		unregisterReceiver(taskCancelReceive);
		unregisterReceiver(taskCancelAllReceive);
		downLoadAction.downloadRelease();
		//remove message
		handler.removeMessages(DOWNLOAD_SERVICE_MSG_GET_POS);
		
		super.onDestroy();
	}

	
	public class TaskInfoReceive extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int val = intent.getIntExtra(DOWNLOAD_SERVICE_KEY_CONTEXT, -1);
			if (val == DOWNLOAD_SERVICE_VAL_CONTEXT_PBL) {//from playbacklist
				sendTaskInfoBroadcast(DOWNLOAD_RECEIVE_ACTION_TASK_PBL);
			}else if(val == DOWNLOAD_SERVICE_VAL_CONTEXT_DLL){//from downloadlist
				sendTaskInfoBroadcast(DOWNLOAD_RECEIVE_ACTION_TASK_DLL);
			}	
		}
	}
	
	public class TaskCancelReceive extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int pos = intent.getIntExtra(DOWNLOAD_RECEIVE_KEY_TASK_CANCEL, -1);
			boolean ret = false;
			if (pos>-1 && pos < downloadList.size()) {
				cancelDownLoad(pos);
				ret = true;
			}
			
			if (ret) {
				//发送更新消息
				sendTaskInfoBroadcast(DOWNLOAD_RECEIVE_ACTION_TASK_PBL);
				sendTaskInfoBroadcast(DOWNLOAD_RECEIVE_ACTION_TASK_DLL);
			}	
		}
	}
	
	public class TaskCancelAllReceive extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.e("123", "service cancel all");
			
			cancelAll();
			
			
		}
		
	}
	
	
	
}
