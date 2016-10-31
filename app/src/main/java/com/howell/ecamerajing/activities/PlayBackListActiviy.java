package com.howell.ecamerajing.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.howell.ecamerajing.action.JpgAction;
import com.howell.ecamerajing.action.PlayBack;
import com.howell.ecamerajing.action.PlayBackAction;
import com.howell.ecamerajing.adapter.PlayBackListAdapter;
import com.howell.ecamerajing.base.ImageLoaderBaseActivity;
import com.howell.ecamerajing.bean.NetItemInfo;
import com.howell.ecamerajing.bean.NetRectFileItem;
import com.howell.ecamerajing.bean.Pagination;
import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamerajing.utils.DialogUtil;
import com.howell.ecamerajing.utils.FileUtil;
import com.howell.ecamerajing.view.MyListViewWithFoot;
import com.howell.ecamerajing.view.MyListViewWithFoot.OnRefreshListener;
import com.howell.ecamjing.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.ArrayList;

/**
 * @author 霍之昊 
 *
 * 类说明
 */
public class PlayBackListActiviy extends ImageLoaderBaseActivity implements OnRefreshListener,OnTouchListener,OnItemClickListener,Constable, View.OnClickListener{

	private static final int REPLAY_COUNT = 10;	//获取录像的个数REPLAY_COUNT
	private Pagination pagination;
	private PlayBack playbackManager;
	private ImageView mBack;
	private TextView tvDownloadManager , tvDownloadNum;

	private DisplayImageOptions options;
	private FileUtil fileUtil;

	private PlayBackListAdapter adapter;

	private NetRectFileItem[] netRectFileItem = null;//size = 10
	private boolean onStop;

	private Dialog waitDialog;

	private PlayBackAction playBackAction;
	ArrayList<NetItemInfo> itemList = new ArrayList<NetItemInfo>();
	ArrayList<NetItemInfo> downLoadList = new ArrayList<NetItemInfo>();
	private int curPageIndex=0;
	DownLoadTaskReceiver myTaskReceiver;

	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PLAYBACK_MSG_GETLIST_ERROR:
				Toast.makeText(PlayBackListActiviy.this, "网络错误", Toast.LENGTH_SHORT).show();

				break;
			case PLAYBACK_MSG_LIST_PREPARE:
				netRectFileItem = playBackAction.getNetRectFileItem();
				int len = playBackAction.getItemSize();
				
				ArrayList<NetItemInfo> barList = new ArrayList<NetItemInfo>(itemList);
				itemList.clear();
				
				for(int i=0;i<len;i++){
//					Log.i("123", "cur-size="+pagination.getCur_size());
					//					Log.i("123", "[i]="+i+" "+netRectFileItem[i].toString());
					itemList.add(new NetItemInfo( netRectFileItem[i]));
				}
				//				adapter.refreshList((ArrayList<NetRectFileItem>)Arrays.asList(netRectFileItem));

				itemList.addAll(barList);

				if (pagination.page_no==0) {
					waitDialog.dismiss();
				}else{
					handler.sendEmptyMessage(PLAYBACK_MSG_LIST_REFRESH);
				}

				//同步task info
				checkItemListFileExists();
//				checkItemJpg();
				
				refreshTaskInfo();
				//刷新界面
				adapter.refreshList(itemList);
				break;
			case PLAYBACK_MSG_LIST_REFRESH:
				Log.i("123", "PLAYBACK_MSG_LIST_REFRESH");
				listView.onRefreshComplete();

				break;

			case PLAYBACK_MSG_ITEM_DOWNLOAD:
				//有新任务下载
				Log.i("123", "PLAYBACK_MSG_ITEM_DOWNLOAD");
				playBackAction.startDownloadService(itemList.get(msg.arg1).getItem());
				break;

			case PLAYBACK_MSG_JPG_REFRESH:
				//jpg 刷新
				adapter.refreshList(itemList);
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
		setContentView(R.layout.playback_list);

		if(null != savedInstanceState)  
		{  
			pauseOnScroll = savedInstanceState.getBoolean(STATE_PAUSE_ON_SCROLL);
			pauseOnFling = savedInstanceState.getBoolean(STATE_PAUSE_ON_FLING);
		}  


		myTaskReceiver = new DownLoadTaskReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(DOWNLOAD_RECEIVE_ACTION_TASK_PBL);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(myTaskReceiver, filter);


		init();
		//getPlayBackList();
		loginDevice();
	}

	@Override
	protected void onStart() {

		super.onStart();
	}

	@Override
	protected void onStop() {

		super.onStop();
	}

	private void loginDevice(){
		new AlertDialog.Builder(this)   
		.setTitle("摄像机将切换到预览模式")   
		.setMessage("在预览模式下将暂时停止录像，退出时将自动为您启动录像模式")                 
		.setPositiveButton("切换", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub

				dialog.dismiss();
				waitDialog = DialogUtil.postWaitDialog(PlayBackListActiviy.this);
				waitDialog.show();



				playBackAction.getPlayBackList(pagination);


				//				if(playbackManager.login(TEST_IP)){//192.168.2.103
				//					if(playbackManager.stopRecord(0)){
				//						//停止录像成功
				//						//刷新回放列表
				//						getPlayBackList();
				//						return;
				//					}
				//				}
				//登录失败



				//listView.refresh();
				//playbackManager.logout();
				//getPlayBackList();
			}
		})   
		.setNegativeButton("取消", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
				//playbackManager.logout();
				PlayBackListActiviy.this.finish();
				dialog.dismiss();
			}
		})
		.setCancelable(false)
		.show();  
	}

	//初始化
	private void init(){
		mBack = (ImageView)findViewById(R.id.playback_back);
		mBack.setOnTouchListener(this);

		playbackManager = new PlayBack();
		pagination = new Pagination(REPLAY_COUNT,0);

		options = new DisplayImageOptions.Builder()
				.showStubImage(R.mipmap.empty_bg)
				.showImageForEmptyUri(R.mipmap.empty_bg)
				.showImageOnFail(R.mipmap.empty_bg)
				.cacheInMemory(true)
				.cacheOnDisc(false)
				.bitmapConfig(Bitmap.Config.RGB_565)	 
				.build();

		fileUtil = new FileUtil();
		listView = (MyListViewWithFoot)findViewById(R.id.playback_listview);
		adapter = new PlayBackListAdapter(this,new ArrayList<NetItemInfo> (),options);
		adapter.setHandler(handler);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setonRefreshListener(this);

		tvDownloadManager = (TextView)findViewById(R.id.playback_list_download);
		tvDownloadManager.setOnClickListener(this);
		tvDownloadNum = (TextView)findViewById(R.id.playback_list_downloadnum);
		tvDownloadNum.setVisibility(View.GONE);

		onStop = false;

		playBackAction = PlayBackAction.getInstance();
		playBackAction.setContext(this).setHandler(handler);

	}

	//填入截图的本地路径
	/*private ArrayList<String> fillAdapterList(NetRectFileItem[] netRectFileItem){
		ArrayList<String> list = new ArrayList<String>();
		for(int i = 0 ; i < netRectFileItem.length ; i++){
			list.add(fileUtils.createNetRectFileItemFile(netRectFileItem[i]));
		}
		return list;
	}

	//添加新数据到原数组
	private NetRectFileItem[] combineArray(NetRectFileItem[] first,NetRectFileItem[] second){
		NetRectFileItem[] netRectFileItem = new NetRectFileItem[first.length + second.length];
		System.arraycopy(first, 0, netRectFileItem, 0, first.length);  
		System.arraycopy(second, 0, netRectFileItem, first.length, second.length); 
		return netRectFileItem;
	}
	 */

	private void refreshTaskInfo(){
		Intent intent = new Intent(DOWNLOAD_RECEIVE_ACTION_TASK_INFO);
		//intent.putExtra(DOWNLOAD_RECEIVE_KEY_TASK_COUNT, downloadList.size());
		intent.putExtra(DOWNLOAD_SERVICE_KEY_CONTEXT, DOWNLOAD_SERVICE_VAL_CONTEXT_PBL);
		sendBroadcast(intent);
	}

	private void cancelAllDownloadTask(){
		Intent intent = new Intent(DOWNLOAD_RECEIVE_ACTION_TASK_CANCEL_ALL);
		sendBroadcast(intent);
	}

	private boolean registerFileHandle(){
		return playbackManager.initToGetList(pagination);

	}

	private void unregisterFileHandle(){
		playbackManager.deinitToGetList();
	}

	//获取录像列表
	private void getPlayBackList(){
		//清空缓存文件夹
		//fileUtils.removeCache(fileUtils.getBitmapCachePath());
		//获取截图并保存到缓存文件夹并显示图片
		new AsyncTask<Void, Integer, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				//if(playbackManager.login("192.168.2.103")){
				pagination.page_no = 0;
				if(playbackManager.initToGetList(pagination)){
					System.out.println("页码："+pagination.page_no+" 文件数："+pagination.cur_size);
					netRectFileItem = playbackManager.getList(pagination.cur_size);
				}
				//}
				return null;
			}

			@SuppressLint("NewApi")
			protected void onPostExecute(Void result) {
				if(netRectFileItem != null){
					playbackManager.deinitToGetList();

//					adapter.refreshList((ArrayList<NetRectFileItem>)Arrays.asList(netRectFileItem));
					//adapter.refreshAdapter(fillAdapterList(netRectFileItem));
					//获取视频截图并存入sd卡
					/*for(int i = 0 ; i < netRectFileItem.length ; i++){
						//如果页面已经销毁则退出循环
						if(PlayBackListActiviy.this.isDestroyed() || onStop){
							Log.e("", "activity is destoryed");
							return;
						}
						Log.e("java",netRectFileItem[i].toString());
						//如果缓存文件夹不存在该截图则获取截图
						if(!fileUtils.isBitmapExist(fileUtils.createNetRectFileItemFile(netRectFileItem[i]))){
							getPlayBackScreenshot(i);
						}
					}*/
					//getPlayBackScreenshots();
				}else{
					System.out.println("获取失败");
					if(!isDestroyed()){
						new AlertDialog.Builder(PlayBackListActiviy.this)   
						.setMessage("获取列表失败")                 
						.setPositiveButton("确定", new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								finish();
							}
						})   
						.show();
					}
				}
			};

		}.execute();
	}

	/*private void getPlayBackScreenshots(){
		new AsyncTask<Void, Integer, Void>(){

			@Override
			protected Void doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				//获取视频截图并存入sd卡
				for(int i = 0 ; i < netRectFileItem.length ; i++){
					//如果页面已经销毁则退出循环
					if(PlayBackListActiviy.this.isDestroyed() || onStop){
						Log.e("", "activity is destoryed");
						return null;
					}
					Log.e("java","total size:"+netRectFileItem.length+","+netRectFileItem[i].toString());
					//如果缓存文件夹不存在该截图则获取截图
					if(!fileUtils.isBitmapExist(fileUtils.createNetRectFileItemFile(netRectFileItem[i]))){
						getPlayBackScreenshot(i);
					}
				}
				return null;
			}

		}.execute();
	}

	//获取录像文件截图
	private void getPlayBackScreenshot(final int index){
		Log.e("java getPlayBackScreenshot", "index:"+index);
		if(netRectFileItem != null){
			playbackManager.getJpg(netRectFileItem[index]);//获取视频截图并存入sd卡
			handler.sendEmptyMessage(0x01);
		}
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			adapter.notifyDataSetChanged();
		};
	};

	//分页获取更多录像列表
	private void getMorePlayBackList(){
		//页数加1
		pagination.page_no += 1;
		//如果页数大于总页数则返回
		if(pagination.page_count <  pagination.page_no){
			Log.i("getMorePlayBackList", "没有更多页");
			return ;
		}
		//获取截图并保存到缓存文件夹并显示图片
		new AsyncTask<Void, Integer, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				// TODO Auto-generated method stub

				if(playbackManager.initToGetList(pagination)){
					System.out.println("页码："+pagination.page_no+" 文件数："+pagination.cur_size);
					netRectFileItem = playbackManager.getList(pagination.cur_size);
					playbackManager.getJpg(netRectFileItem[0]);//获取视频截图并存入sd卡
				}
				return null;
			}

			protected void onPostExecute(Void result) {
				playbackManager.deinitToGetList();
				adapter.refreshAdapter(fillAdapterList(netRectFileItem));
			};

		}.execute();
	}*/

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		Log.i("123", "onrestart");
		//刷新界面 
		adapter.refreshList(itemList);
		super.onRestart();
		onStop = false;
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(myTaskReceiver);
		//重启 录像 FIXME
		playBackAction.startRecord();//FIXME
		//关闭download service
		cancelAllDownloadTask();
	
		super.onDestroy();
		//		playbackManager.startRecord(0);
		//		playbackManager.logout();
	}

	@Override
	public void onItemClick(AdapterView<?> parent , View view , int position , long id) {
		// TODO Auto-generated method stub
		Log.e("123", "pos = "+position+"  size=  "+netRectFileItem.length);
		
		
//		Log.e("123", "activity onItemClick pos:"+position    +"is exists= "+netRectFileItem[position-1].isBitmapExists());
		Log.e("123", "activity onItemClick pos:"+position    +"is exists= "+itemList.get(position-1).getItem().isBitmapExists());
		
		NetRectFileItem item = itemList.get(position-1).getItem();
		
		if(!item.isBitmapExists()){
			JpgAction.getInstance().setNeedFirstFrameJpg(item);
		}
		onStop = true;
		playbackManager.logout();
		Intent intent = new Intent(this,PlayActivity.class);
		intent.putExtra("netRectFileItem", item);
		intent.putExtra("playMode", PLAY_MODE_PALYBACK);
		startActivity(intent);
	}

//	private boolean bStopDownLoadDialog = false;
	private void showStopDownloadDailog(){
		
		Log.i("123", "showStopDownloadDailog");
		boolean ret = false;
		
		new AlertDialog.Builder(this)   
		.setTitle("摄像机将切换到录像模式")   
		.setMessage("在录像模式下将停止下载，并自动为您启动录像")                 
		.setPositiveButton("切换", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
//				bStopDownLoadDialog = true;
				dialog.dismiss();
				finish();
			}
		})   
		.setNegativeButton("取消", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
				//playbackManager.logout();
//				bStopDownLoadDialog = false;
				dialog.dismiss();
			}
		})
		.setCancelable(false)
		.show();  
			
	
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent arg1) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.playback_back:
			
			Log.i("123", "play back");
			
			showStopDownloadDailog();
						
//			finish();
			break;
		
			
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.i("123", "按下了返回键");
			showStopDownloadDailog();
		}
		return false;
//		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		//		getPlayBackList();
		Log.i("123", "on refreash");


		curPageIndex++;
		pagination.setPage_no(curPageIndex);
		playBackAction.getPlayBackList(pagination);

	}



	@Override
	public void onFirstRefresh() {
		// TODO Auto-generated method stub
		Log.i("123", "on first refresh");
	}

	@Override
	public void onFirstRefreshDown() {
		// TODO Auto-generated method stub
		listView.onRefreshComplete();
		Log.i("123", "on first refresh down");
	}



	@Override
	public void onFootRefresh() {
		// TODO Auto-generated method stub
		Log.i("123", "on foot refresh");
	}


	


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.playback_list_download:
			Log.i("123", "playback_list_download");
			//下载管理界面
			Intent intent = new Intent(this,DownLoadListActivity.class);
			//			intent.putExtra(DOWNLOAD_RECEIVE_KEY_TASK_COUNT, downLoadList.size());
			//			for(int i=0;i<downLoadList.size();i++){
			//				intent.putExtra("item"+i, downLoadList.get(i));
			//			}
			startActivity(intent);


			break;

		default:
			break;
		}


	}


	public Boolean checkItemDownLoadOK(NetRectFileItem item){

		return FileUtil.checkVideoFileExists(item);
	}

	public void checkItemJpg(){
		
		for(int i=0;i<playBackAction.getItemSize();i++){
			if(!FileUtil.isBitmapExist(FileUtil.getBitmapCachePath()+netRectFileItem[i].getFileNo())){//fixme
				playBackAction.getPlayBackJpg(i);
			}	
		}
	}
	
	
	public void checkItemListFileExists(){//判断是否下载过
		new Thread(){
			@Override
			public void run() {
				for(int i=0;i<itemList.size();i++){
					if (checkItemDownLoadOK(itemList.get(i).getItem())) {
						itemList.get(i).getItem().setDownLoadState(PLAYBACK_VAL_DOWNLOAD_YES);
					}else{
						itemList.get(i).getItem().setDownLoadState(PLAYBACK_VAL_DOWNLOAD_NO);
					}
				}
				super.run();
			}
		}.run();
	}
	
	public void checkItemListState(){//判断是否正在下载

		new Thread(){
			@Override
			public void run() {
				for(int i=0;i<itemList.size();i++){
					
					for(int j=0;j<downLoadList.size();j++){
						if (itemList.get(i).getItem().equals(downLoadList.get(j).getItem())) {
							itemList.get(i).getItem().setDownLoadState(PLAYBACK_VAL_DOWNLOAD_ING);
						}
					}	
				}
				adapter.refreshList(itemList);
				super.run();
			}
		}.run();
	}

	public class DownLoadTaskReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int num = intent.getIntExtra(DOWNLOAD_RECEIVE_KEY_TASK_COUNT, 0);
			if (num==0) {
				tvDownloadNum.setText("0");
				tvDownloadNum.setVisibility(View.GONE);
			}else{
				tvDownloadNum.setText(""+num);
				tvDownloadNum.setVisibility(View.VISIBLE);
			}
			NetRectFileItem bar=null;
			downLoadList.clear();
			for(int i=0;i<num;i++){
				bar = (NetRectFileItem) intent.getSerializableExtra("item"+i);
				NetItemInfo itemInfo = new NetItemInfo(bar);
				if (bar!=null) {
					downLoadList.add(itemInfo);
				}else{
					Log.i("123", "bar==null");
				}

			}

			checkItemListFileExists();
			checkItemListState();


			for (NetItemInfo bar2 : downLoadList) {//test FIXME
				Log.i("123", "item="+bar2.getItem().toString());
			}
		}
	}
}
