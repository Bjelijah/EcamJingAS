package com.howell.ecamerajing.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.howell.ecamerajing.bean.NetItemInfo;
import com.howell.ecamerajing.bean.NetRectFileItem;
import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamerajing.utils.DownLoadUtil;
import com.howell.ecamerajing.utils.FileUtil;
import com.howell.ecamjing.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * @author 霍之昊 
 *
 * 类说明
 */
public class PlayBackListAdapter extends BaseAdapter implements Constable{
	
	private Context context;
	private ArrayList<NetItemInfo> list;
    private DisplayImageOptions options;
    private FileUtil fileUtil;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private int adapterMode;
    private Handler handler;
    
    public void setHandler(Handler handler){
    	this.handler = handler;
    }
    
    
	public PlayBackListAdapter(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.adapterMode = DOWNLIAD_MODE;
	}
	
	public PlayBackListAdapter(Context context,
			ArrayList<NetItemInfo> list, DisplayImageOptions options) {
		super();
		this.context = context;
		this.list = list;
		this.options = options;
		this.adapterMode = DOWNLIAD_MODE;
	}

	public void refreshList(ArrayList<NetItemInfo> list) {
		Log.i("123", "refreshlist");
		this.list = list;
		notifyDataSetChanged();
	}
	
	public void setMode(int mode){
		adapterMode = mode;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		NetItemInfo itemInfo = list.get(position);
		NetRectFileItem item = itemInfo.getItem();
    	if (convertView == null) {
    		LayoutInflater layoutInflater = LayoutInflater.from(context);
    		convertView = layoutInflater.inflate(R.layout.play_back_list_item, null);
			holder = new ViewHolder();
				
			holder.duration = (TextView)convertView.findViewById(R.id.playback_item_duration);
			holder.startTime = (TextView)convertView.findViewById(R.id.playback_item_start_time);
			holder.bg = (ImageView)convertView.findViewById(R.id.playback_item_image);
			
			holder.downlaodIcon = (ImageView)convertView.findViewById(R.id.playback_item_download);
			
			holder.downloadLayout = (LinearLayout)convertView.findViewById(R.id.playback_ll_download);
		
            convertView.setTag(holder);
    	}else{
         	holder = (ViewHolder)convertView.getTag();
        }
    	holder.downloadLayout.setOnClickListener(new OnHolderClickListener(item, context,position));
    	if(adapterMode == NORMAL_MODE){
    		holder.downlaodIcon.setVisibility(View.GONE);
    	}else if(adapterMode == DOWNLIAD_MODE){
    		holder.downlaodIcon.setVisibility(View.VISIBLE);
    	}
    	
    	if (item.getDownLoadState() == PLAYBACK_VAL_DOWNLOAD_ING) {
    		Log.i("123", "draw pos="+position);
			holder.downlaodIcon.setImageDrawable(context.getResources().getDrawable(R.mipmap.btn_playback_downloading));
		}else if(item.getDownLoadState() == PLAYBACK_VAL_DOWNLOAD_YES){
			holder.downlaodIcon.setImageDrawable(context.getResources().getDrawable(R.mipmap.btn_playback_downloadfinish));
		}else if (item.getDownLoadState()== PLAYBACK_VAL_DOWNLOAD_NO) {
			holder.downlaodIcon.setImageDrawable(context.getResources().getDrawable(R.mipmap.btn_playback_download));
		}
    	
    	
    	holder.duration.setText(DownLoadUtil.getDownLoadTotalDuration(item));
    	
    	holder.startTime.setText(DownLoadUtil.getDownLoadStartTime(item));
    	 
    	//如果文件存在则加载
    	
    	
    	if (itemInfo.getBitmap()==null) {//未加载过
			if (!FileUtil.isBitmapExist(item.getFileNameToString())) {
				Log.i("123", "不存在");
				holder.bg.setImageDrawable(context.getResources().getDrawable(R.mipmap.empty_bg));//FIXME
//				holder.bg.setImageDrawable(context.getResources().getDrawable(R.drawable.img_normal));
    			item.setBitmapExists(false);
    			Drawable drawable = context.getResources().getDrawable(R.mipmap.empty_bg);
//    			Drawable drawable = context.getResources().getDrawable(R.drawable.img_normal);
    			Log.i("123", "width="+drawable.getIntrinsicWidth()+"  height="+drawable.getIntrinsicHeight());
        		item.setBitmapSize(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			}else{
				Log.i("123", "存在");
				//加载
				item.setBitmapExists(true);
				Drawable drawable = context.getResources().getDrawable(R.mipmap.empty_bg);
	    		Bitmap bitmap = FileUtil.loadImageBitmapFromDisk(item.getFileNameToString(),drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
	    		holder.bg.setImageBitmap(bitmap);
	    		itemInfo.setBitmap(bitmap);
			}
		}else{//加载过
			holder.bg.setImageBitmap(itemInfo.getBitmap());
		}
		return convertView;
	}
	
	class ViewHolder{
		public TextView duration,startTime;
		public ImageView bg;
		public ImageView downlaodIcon;
		public LinearLayout downloadLayout;
	}

	

	private class OnHolderClickListener implements OnClickListener{
		NetRectFileItem item;
		Context context;
		int pos;
		public OnHolderClickListener(NetRectFileItem item,Context context,int pos) {
			this.item = item;
			this.context = context;
			this.pos =pos;
		}
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.i("123", "on click pos="+pos);
			
			switch (v.getId()) {
			case R.id.playback_ll_download:
				if (item.getDownLoadState()==PLAYBACK_VAL_DOWNLOAD_NO) {
					item.setDownLoadState(PLAYBACK_VAL_DOWNLOAD_ING);
					Log.i("123", "PLAYBACK_VAL_DOWNLOAD_ING pos= "+ pos);
					notifyDataSetChanged();
					
					
					Message msg = new Message();
					msg.what = PLAYBACK_MSG_ITEM_DOWNLOAD;
					msg.arg1 = pos;
					handler.sendMessage(msg);
					
				}
				
				break;

			default:
				break;
			}
		}
		
	}

}
