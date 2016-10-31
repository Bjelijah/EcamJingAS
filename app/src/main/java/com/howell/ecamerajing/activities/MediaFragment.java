package com.howell.ecamerajing.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.howell.ecamerajing.action.JpgAction;
import com.howell.ecamerajing.adapter.GridViewAdapter;
import com.howell.ecamerajing.base.ImageLoaderBaseFragment;
import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamerajing.utils.FileUtil;
import com.howell.ecamjing.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.io.File;
import java.util.ArrayList;

/**
 * @author 霍之昊 
 *
 * 类说明：我的界面
 */

public class MediaFragment extends ImageLoaderBaseFragment implements OnItemClickListener,Constable{
	
	private DisplayImageOptions options;
	private ArrayList<String> paths;
	private GridViewAdapter adatper;
	private JpgAction jpgAction;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i("log123 ", savedInstanceState+"");
		if(null != savedInstanceState)  
        {  
			pauseOnScroll = savedInstanceState.getBoolean(STATE_PAUSE_ON_SCROLL);
			pauseOnFling = savedInstanceState.getBoolean(STATE_PAUSE_ON_FLING);
        }  
		
		options = new DisplayImageOptions.Builder()
		.showStubImage(R.mipmap.empty_bg)
		.showImageForEmptyUri(R.mipmap.empty_bg)
		.showImageOnFail(R.mipmap.empty_bg)
		.cacheInMemory(true)
		.cacheOnDisc(false)
		.bitmapConfig(Bitmap.Config.RGB_565)	 
		.build();
		
	}
	
	
	@Override
	public void onResume() {
		paths = FileUtil.getFileNameByDir(new File(FileUtil.eVideo));
		adatper.refreshAdapter(paths);
		super.onResume();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.meida_fragment, null);
		listView = (GridView)view.findViewById(R.id.gridView);
		paths = FileUtil.getFileNameByDir(new File(FileUtil.eVideo));
		adatper = new GridViewAdapter(this.getActivity(),paths,options); 
		listView.setAdapter(adatper);
		listView.setOnItemClickListener(this);
		return view;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent , View view , int position , long id) {
		// TODO Auto-generated method stub
		String videoName = FileUtil.getNameOfVideo(paths.get(position)); 
		Log.i("123", "videoName="+videoName);
		
		String string = videoName.substring(0,videoName.indexOf("."));
		Log.i("123", " "+string);
		if(!FileUtil.isBitmapExist(videoName.substring(videoName.indexOf(".")))){
			jpgAction = JpgAction.getInstance();
			Drawable drawable = getResources().getDrawable(R.mipmap.empty_bg);
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			jpgAction.setNeedFirstFrameJpg(string, width, height);
		}
		
		if (videoName.contains(".hw")||videoName.contains(".HW")) {
			Log.i("123", "hw file");
			Intent intent = new Intent(getActivity(),PlayActivity.class);
			intent.putExtra("playMode", PLAY_MODE_LOCAL_HW);
			intent.putExtra("localFile", paths.get(position));
			startActivity(intent);
			
		}else if (videoName.contains(".avi")||videoName.contains(".AVI")) {
			Log.i("123", "avi file");
			
			Intent intent = new Intent(getActivity(),PlayActivity.class);
			intent.putExtra("playMode", PLAY_MODE_LOCAL_AVI);
			intent.putExtra("localFile", paths.get(position));
			startActivity(intent);
			
		}
		

	}
}
