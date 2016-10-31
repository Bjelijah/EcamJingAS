package com.howell.ecamerajing.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.howell.ecamerajing.bean.NetItemInfo;
import com.howell.ecamerajing.bean.NetRectFileItem;
import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamerajing.utils.DownLoadUtil;
import com.howell.ecamjing.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class DownLoadListAdapter extends BaseAdapter implements Constable{
	private Handler handler;
	private Context context;
	ArrayList<NetItemInfo> list;
	private DisplayImageOptions options;
	 private ImageLoader imageLoader = ImageLoader.getInstance();
	public DownLoadListAdapter(Context context) {
		this.context = context;
	}
	
	public DownLoadListAdapter(Context context,ArrayList<NetItemInfo> list, DisplayImageOptions options) {
		this.context = context;
		this.list = list;
		this.options = options;
	}
	
	public void refreshAdapter(ArrayList<NetItemInfo> list){
		this.list = list;
		notifyDataSetChanged();
	}
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		NetItemInfo itemInfo = list.get(position);
		NetRectFileItem item = itemInfo.getItem();
		if (convertView==null) {
			LayoutInflater layoutInflater = LayoutInflater.from(context);
			convertView = layoutInflater.inflate(R.layout.download_list_item, null);
			holder = new ViewHolder();
			holder.bg = (ImageView) convertView.findViewById(R.id.download_item_image);
			holder.duration = (TextView)convertView.findViewById(R.id.download_item_duration);
			holder.info = (TextView)convertView.findViewById(R.id.download_tv_info);
			holder.pb = (ProgressBar)convertView.findViewById(R.id.download_pb);
			holder.downloadLayout = (LinearLayout)convertView.findViewById(R.id.download_ll_cancel);
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		holder.downloadLayout.setOnClickListener(new OnHolderClickListener(context,item,position));
		holder.duration.setText(DownLoadUtil.getDownLoadTotalDuration(item));
		holder.info.setText(DownLoadUtil.getDownLoadState(item));
		holder.pb.setProgress(item.getItemInfo().getDownLoadProgress());
		
		if (itemInfo.getBitmap()==null) {
			holder.bg.setImageDrawable(context.getResources().getDrawable(R.mipmap.empty_bg));
		}else{
			holder.bg.setImageBitmap(itemInfo.getBitmap());
		}
		
		
//		if(!FileUtil.isBitmapExist(FileUtil.getBitmapCachePath()+item.getFileNo())){//fixme
//    		holder.bg.setImageDrawable(context.getResources().getDrawable(R.drawable.empty_bg));
//    	}else{
//    		imageLoader.displayImage("file:/"+FileUtil.getBitmapCachePath()+item.getFileNo(), holder.bg, options);
//    	}
		
		return convertView;
	}

	public void sendCancelTaskBroadcast(int pos){
		Intent intent = new Intent(DOWNLOAD_RECEIVE_ACTION_TASK_CANCEL);
		intent.putExtra(DOWNLOAD_RECEIVE_KEY_TASK_CANCEL, pos);
		context.sendBroadcast(intent);
	}
	
	
	class ViewHolder{
		public TextView duration,info;
		public ImageView bg;
		
		public LinearLayout downloadLayout;
		public ProgressBar pb;
	}
	
	class OnHolderClickListener implements OnClickListener{
		NetRectFileItem item;
		Context context;
		int pos;
		public OnHolderClickListener(Context context,NetRectFileItem item,int pos) {
			this.context = context;
			this.item = item;
			this.pos = pos;
		}
		
		
		@Override
		public void onClick(View v) {
			//send cancel download
			sendCancelTaskBroadcast(pos);
			
		}
		
	}
	
}
