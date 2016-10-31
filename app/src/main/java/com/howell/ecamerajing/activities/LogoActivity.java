package com.howell.ecamerajing.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.howell.ecamerajing.utils.FileUtil;
import com.howell.ecamjing.R;
import com.howell.jni.CamJniUtil;

/**
 * @author 霍之昊 
 *
 * 类说明：广告图片页面
 */
public class LogoActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo);
		appInit();
		new AsyncTask<Void, Integer, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return null;
			}
			
			protected void onPostExecute(Void result) {
				Intent intent = new Intent(LogoActivity.this,HomeFragmentActivity.class);
				startActivity(intent);
			};
		}.execute();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		finish();
	}
	
	private void appInit(){
		FileUtil.createEcameraDir();//创建文件夹
		CamJniUtil.cameraInit();
	

	}
}
