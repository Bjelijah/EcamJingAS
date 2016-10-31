package com.howell.ecamerajing.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.howell.ecamerajing.adapter.DownLoadListAdapter;
import com.howell.ecamerajing.bean.NetItemInfo;
import com.howell.ecamerajing.bean.NetRectFileItem;
import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamerajing.utils.FileUtil;
import com.howell.ecamjing.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.ArrayList;

public class DownLoadListActivity extends Activity implements Constable{
	
	private ArrayList<NetItemInfo> dlList = new ArrayList<NetItemInfo>();
	private DownLoadListAdapter adapter;
	private DisplayImageOptions options;
	private ListView listView;
	
	private DownLoadTaskReceiver myTaskReceiver;
	private DownLoadPosReceiver myPosReceiver;
	private LinearLayout llBack;
	
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what) {
			case 0:
				
				break;

			default:
				break;
			}
		}
	};
	
	public void registerTaskReceiver(){
		myTaskReceiver = new DownLoadTaskReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(DOWNLOAD_RECEIVE_ACTION_TASK_DLL);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(myTaskReceiver, filter);
	}
	
	public void registerTaskProgress(){
		myPosReceiver = new DownLoadPosReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(DOWNLOAD_RECEICVE_ACTION_TASK_PROGRESS);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(myPosReceiver, filter);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_list);
		
		
		//注册广播 
		registerTaskReceiver();
		registerTaskProgress();
		
		sendGetTaskInfoBroadcast();
		
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.mipmap.empty_bg)
				.showImageForEmptyUri(R.mipmap.empty_bg)
				.showImageOnFail(R.mipmap.empty_bg)
				.cacheInMemory(true)
				.cacheOnDisc(false)
				.bitmapConfig(Bitmap.Config.RGB_565)	 
				.build();
		
		listView = (ListView)findViewById(R.id.download_lv);
		adapter = new DownLoadListAdapter(this, dlList, options);
		adapter.setHandler(handler);
		listView.setAdapter(adapter);
		
		llBack = (LinearLayout)findViewById(R.id.download_ll_back);
		llBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			
				finish();
			}
		});
	}
	
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(myTaskReceiver);
		unregisterReceiver(myPosReceiver);
		super.onDestroy();
	}
	
	
	private void sendGetTaskInfoBroadcast(){
		Intent intent = new Intent(DOWNLOAD_RECEIVE_ACTION_TASK_INFO);
		intent.putExtra(DOWNLOAD_SERVICE_KEY_CONTEXT, DOWNLOAD_SERVICE_VAL_CONTEXT_DLL);
		sendBroadcast(intent);
	}
	
	
	
	public class DownLoadTaskReceiver extends BroadcastReceiver{

		private Bitmap getBitmap(Context context, String fileName){
			if (FileUtil.isBitmapExist(fileName)) {
				Drawable drawable = context.getResources().getDrawable(R.mipmap.empty_bg);
				Bitmap bitmap = FileUtil.loadImageBitmapFromDisk(fileName,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
				return bitmap;
			}else{
				return null;
			}
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			int num = -1;
			NetRectFileItem bar=null;
			dlList.clear();
			num = intent.getIntExtra(DOWNLOAD_RECEIVE_KEY_TASK_COUNT, -1);
			boolean ret = false;
			for(int i=0;i<num;i++){
				bar = (NetRectFileItem) intent.getSerializableExtra("item"+i);
				if (bar!=null) {
					Bitmap bitmap = getBitmap(context,bar.getFileNameToString());
					dlList.add(new NetItemInfo(bar, bitmap));
					ret = true;
				}
			}
			if (num==0) {
				ret = true;
			}
			
			
			if (ret) {
				adapter.refreshAdapter(dlList);
			}
			
			
		}
		
	}
	
	public class DownLoadPosReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			int pos = intent.getIntExtra(DOWNLOAD_RECEIVE_KEY_PROGRESS_POS, 0);
			String fileInfo = intent.getStringExtra(DOWNLOAD_RECEIVE_KEY_FILE_INFO);
			if (!dlList.isEmpty()) {
				dlList.get(0).getItem().getItemInfo().setDownLoadProgress(pos);
				if (fileInfo!=null) {
					dlList.get(0).getItem().getItemInfo().setDownLoadIng(DOWNLOAD_VAL_DOWNLOAD_ING);
					dlList.get(0).getItem().getItemInfo().setDownLoadIngInfoStr(fileInfo);
				}
				adapter.refreshAdapter(dlList);
			}
		
		}
		
	}


}
