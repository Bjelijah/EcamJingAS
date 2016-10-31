package com.howell.ecamerajing.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.howell.ecamerajing.bean.NetWlanApInfo;
import com.howell.ecamerajing.bean.SystimeInfo;
import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamerajing.utils.FileUtil;
import com.howell.ecamjing.R;
import com.howell.jni.CamJniUtil;

import java.io.File;
import java.util.Calendar;

public class SettingActivity extends Activity implements OnClickListener,Constable{

	private static final int MSG_GET_RECORD_STATE 		= 0x01; 	
	private static final int MSG_SET_RECORD_STATE		= 0x02;
	private static final int MSG_GET_WIFI_INFO			= 0x03;
	private static final int MSG_SET_WIFI_INFO			= 0x04;
	private static final int MSG_GET_DISK_CACH			= 0x05;
	private static final int MSG_CACL_DISK_CACH			= 0x06;
	private static final int VAL_RECORD_STATE_STOP		= 0x10;
	private static final int VAL_RECORD_STATE_START		= 0x11;
	
	
	private LinearLayout llWifi,llTime;
	private Switch swState;
	private boolean bGetState = false;
	private NetWlanApInfo wifiInfo = new NetWlanApInfo();
	private View wifiView;
	private EditText etName,etPwd;
	private Button btnTest;
	private TextView tvCach;
	private PopupWindow popupWindow;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			Log.i("123", "msg ="+ msg.what);
			switch (msg.what) {
			case MSG_GET_RECORD_STATE:
				bGetState = true;
				if (msg.arg1==VAL_RECORD_STATE_START) {
					swState.setChecked(true);
				}else if(msg.arg1==VAL_RECORD_STATE_STOP){
					swState.setChecked(false);
				}
				
				break;

			case MSG_SET_RECORD_STATE:
				//FIXME
				
				if (msg.arg1 == 1) {
					Toast.makeText(SettingActivity.this, "录像设置成功", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(SettingActivity.this, "录像设置失败", Toast.LENGTH_SHORT).show();
				}
				
				break;
				
			case MSG_GET_WIFI_INFO:
			{
				if (msg.arg1==1) {
					if (etName!=null && etPwd!=null) {
						Log.i("123", "etName="+wifiInfo.getSsid()+" etPwd"+wifiInfo.getKey());
						etName.setText(wifiInfo.getSsid());
						etPwd.setText(wifiInfo.getKey());
					}
				}else{
					Toast.makeText(SettingActivity.this, "获取wifi信息失败", Toast.LENGTH_SHORT).show();//FIXME
				}
			}
			break;
			case MSG_GET_DISK_CACH:
				
				tvCach.setText(( String.format("%.2f", (float)msg.arg1/1024/1024))+"M");
			
			break;
			case MSG_CACL_DISK_CACH:
				getCach();
				break;
			default:
				break;
			}
			
			
			
		};
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_setting);
		tvCach = (TextView)findViewById(R.id.setting_tv_cach);
		
		llWifi = (LinearLayout)findViewById(R.id.setting_wifi_ll);
		llWifi.setOnClickListener(this);
		llTime = (LinearLayout)findViewById(R.id.setting_time_ll);
		llTime.setOnClickListener(this);	
		swState = (Switch)findViewById(R.id.setting_state_sw);
		swState.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (bGetState) {
					bGetState = false;
					return;
				}
				if (buttonView.getId()==R.id.setting_state_sw) {
					Log.i("123", "check change :"+isChecked);
					setCamRecordState(isChecked);
				}
				
			}
		});
		
		btnTest = (Button)findViewById(R.id.button_test);
		btnTest.setOnClickListener(this);
		getCach();
		getCamRecordState();
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.setting_wifi_ll:
			setWifiInfo();
			break;
		case R.id.setting_time_ll:
//			setCamSysTime();
			showPopupWindow();
			break;
	
		case R.id.button_test:{
			test();
		}
		case R.id.setting_popmenu_ll_time:
			Log.i("123", "setting_popmenu_ll_time");
			setCamSysTime();
			popupWindow.dismiss();
			break;
		case R.id.setting_popmenu_ll_cancel:
			Log.i("123", "setting_popmenu_ll_cancel");
			popupWindow.dismiss();
			break;
		default:
			break;
		}
	}
	
	private void showPopupWindow() {

		View view = (LinearLayout) LayoutInflater.from(SettingActivity.this).inflate(R.layout.popmenu, null);
				/*.inflate(R.layout.popmenu, null);*/

		LinearLayout btSetTime = (LinearLayout) view.findViewById(R.id.setting_popmenu_ll_time);
		LinearLayout btTimeCancel = (LinearLayout) view.findViewById(R.id.setting_popmenu_ll_cancel);
	
		btSetTime.setOnClickListener(this);
		btTimeCancel.setOnClickListener(this);

		if (popupWindow == null) {

			popupWindow = new PopupWindow(SettingActivity.this);

//			popupWindow.setFocusable(true); // 设置PopupWindow可获得焦点
			popupWindow.setTouchable(true); // 设置PopupWindow可触摸
			popupWindow.setOutsideTouchable(true); // 设置非PopupWindow区域可触摸

			popupWindow.setContentView(view);
			
			popupWindow.setWidth(LayoutParams.MATCH_PARENT);
			popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
			
			popupWindow.setAnimationStyle(R.style.popuStyle);	//设置 popupWindow 动画样式
		}

		popupWindow.showAtLocation(llTime, Gravity.BOTTOM, 0, 0);

		popupWindow.update();

	}
	
	
	
	private void setCamSysTime(){
		SystimeInfo systimeInfo = new SystimeInfo();
		Calendar c = Calendar.getInstance();
		systimeInfo.setwYear((short) c.get(Calendar.YEAR));
		systimeInfo.setwMonth((short) (c.get(Calendar.MONTH)+1));
		systimeInfo.setwDay((short) c.get(Calendar.DAY_OF_MONTH));
		systimeInfo.setwDayofWeek((short) c.get(Calendar.DAY_OF_WEEK));
		systimeInfo.setwHour((short) c.get(Calendar.HOUR_OF_DAY));
		systimeInfo.setwMinute((short) c.get(Calendar.MINUTE));
		systimeInfo.setwSecond((short) c.get(Calendar.SECOND));
		systimeInfo.setwMilliseconds((short) c.get(Calendar.MILLISECOND));
		
		Log.i("123", systimeInfo.toString());
		
		
		new AsyncTask<SystimeInfo, Void, Boolean>() {
			@Override
			
			protected Boolean doInBackground(SystimeInfo... params) {
				// TODO Auto-generated method stub
				if(CamJniUtil.cameraLogin(TEST_IP)==-1){
					return false;
				}
				return CamJniUtil.setCamSystemTime(params[0]);
			}
			protected void onPostExecute(Boolean result) {
				if (result) {
					Toast.makeText(SettingActivity.this, "校时成功", Toast.LENGTH_SHORT).show();
					SystimeInfo sInfo = CamJniUtil.getCamSystemTime();
					Log.i("123", "after:"+sInfo.toString());
					
				}else{
					Log.e("123", "校时失败");
					Toast.makeText(SettingActivity.this, "校时失败", Toast.LENGTH_SHORT).show();
				}
				CamJniUtil.cameraLogout();
			};
		}.execute(systimeInfo);
		
	}
	
	private void getCamRecordState(){//
		new AsyncTask<Void, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Void... params) {
				if (CamJniUtil.cameraLogin(TEST_IP)==-1) {
					return false;
				}
				return CamJniUtil.getCamRecordState();//TODO 获取record状态
			};
			
			protected void onPostExecute(Boolean result) {
				Message msg = new Message();
				msg.what = MSG_GET_RECORD_STATE;
				if(!result){//stop state
					Log.i("log123", "stop 模式");
					msg.arg1 = VAL_RECORD_STATE_STOP;
				}else{
					Log.i("log123", "record 模式");
					msg.arg1 = VAL_RECORD_STATE_START;	
				}
				handler.sendMessage(msg);
				CamJniUtil.cameraLogout();
			};
		}.execute();
	} 
	
	private void setCamRecordState(boolean bRecordStart){
		new AsyncTask<Boolean, Void, Integer>(){
			private int ret = 0;
			private int uh;
			@Override
			protected Integer doInBackground(Boolean... params) {
				if ((uh=CamJniUtil.cameraLogin(TEST_IP))==-1) {
					Log.e("123","record login error");
					return 0;
				}else{
					Log.i("123", "record login ok");
				}
				if (params[0]) {
					
					ret = CamJniUtil.startRecord(uh, 0);
				}else{
					ret = CamJniUtil.stopRecord(uh, 0);
				}
		
				return ret;
			}
			
			protected void onPostExecute(Integer result) {
				Message msg = new Message();
				msg.what = MSG_SET_RECORD_STATE;
				msg.arg1 = result;
				Log.i("log123", "msg="+msg.what+" arg1="+msg.arg1);
				handler.sendMessage(msg);
				CamJniUtil.cameraLogout();
			};
		}.execute(bRecordStart);
	}
	
	private void setWifiInfo(){
		View view = LayoutInflater.from(this).inflate(R.layout.dialog_wifi, null);
		etName = (EditText)view.findViewById(R.id.wifi_et_name);
		etPwd = (EditText)view.findViewById(R.id.wifi_et_pwd);
		
		new AlertDialog.Builder(this)
		.setTitle("wifi 设置")
		.setView(view)
		.setPositiveButton("确定", new DialogLister())
		.setNegativeButton("取消", null)
		.show();
		getWifiTask();
	}

	private class DialogLister implements DialogInterface.OnClickListener{
		
		
		public DialogLister() {
			super();
		
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			String [] bar=new String[2];
			bar[0] = etName.getText()+"";
			bar[1] = etPwd.getText()+"";
			Log.i("123", "name="+bar[0]+" pwd="+bar[1]);
			setWifiTask(bar);
			
		}
		
	}
	
	private void setWifiTask(String [] info){
		new AsyncTask<String, Void, Boolean>(){

			@Override
			protected Boolean doInBackground(String... params) {
				// TODO Auto-generated method stub
				if(CamJniUtil.cameraLogin(TEST_IP)==-1){
					Log.e("123", "login error");
					return false;
				}
				
				NetWlanApInfo info = new NetWlanApInfo();
				info.setSsid(params[0]);
				info.setKey(params[1]);
				info.setChannel(0);
				info.setEncrypt(0);
				info.setFlag(3);
				
				return CamJniUtil.setWifiInfo(info);
			}
			
			protected void onPostExecute(Boolean result) {
				if (result) {
					Log.i("123", "set wifi ok");
					Toast.makeText(SettingActivity.this, "wifi设置成功", Toast.LENGTH_SHORT).show();
				}else{
					Log.i("123", "set wifi error");
					Toast.makeText(SettingActivity.this, "wifi设置失败", Toast.LENGTH_SHORT).show();
				}
				CamJniUtil.cameraLogout();
			};
			
		}.execute(info);
	}
	
	
	private void getWifiTask(){
		
		new AsyncTask<NetWlanApInfo, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(NetWlanApInfo... params) {
				// TODO Auto-generated method stub
				if (CamJniUtil.cameraLogin(TEST_IP)==-1) {
					return false;
				}
				
				return CamJniUtil.getWifiInfo(wifiInfo);
			}
			
			protected void onPostExecute(Boolean result) {
				Message msg = new Message();
				msg.what = MSG_GET_WIFI_INFO;
				
				if (result) {
					msg.arg1 = 1;
				}else{
					msg.arg1 = 0;
				}
				handler.sendMessage(msg);
				CamJniUtil.cameraLogout();
			};
			
		}.execute();
	}
	
	private void test(){
		new AsyncTask<Void, Void, Void>(){
			@Override
			protected Void doInBackground(Void... params) {
				FileUtil.removeCache(FileUtil.getBitmapCachePath());
				FileUtil.removeCache(FileUtil.getVideoCachePath());
				return null;
			}
			protected void onPostExecute(Void result) {
				handler.sendEmptyMessage(MSG_CACL_DISK_CACH);
			};
			
		}.execute();
		
	}
	
	private void getCach(){
		new AsyncTask<Void, Void, Integer>(){

			@Override
			protected Integer doInBackground(Void... params) {
				// TODO Auto-generated method stub
				Log.i("123", "do get cach");
				File dir = new File(FileUtil.getVideoCachePath());
				
				
				return FileUtil.CalcCach(dir);
			}
			
			protected void onPostExecute(Integer result) {
				Message msg = new Message();
				msg.what = MSG_GET_DISK_CACH;
				msg.arg1 = result;
				handler.sendMessage(msg);
			};
			
		}.execute();
	}
	
	
}
