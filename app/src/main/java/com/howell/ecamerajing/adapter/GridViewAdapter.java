package com.howell.ecamerajing.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.howell.ecamerajing.utils.FileUtil;
import com.howell.ecamerajing.utils.PhoneConfigUtil;
import com.howell.ecamjing.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * @author 霍之昊 
 *
 * 类说明:网格状图片列表适配器
 */
public class GridViewAdapter extends BaseAdapter {
	//上下文对象 
    private Context context; 
    private ArrayList<String> paths;
    private FrameLayout.LayoutParams lp;
    private int imageWidth;
    private int imageHeight;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options;
 
    
    public GridViewAdapter(Context context,ArrayList<String> paths ,DisplayImageOptions options) {
		// TODO Auto-generated constructor stub
    	this.context = context; 
        this.paths = paths;
        this.options = options;
        imageWidth = (PhoneConfigUtil.getPhoneWidth(context) - 8)/3;
		imageHeight = imageWidth ;
        lp = new FrameLayout.LayoutParams(imageWidth, imageHeight);
       
	}
    
    public void refreshAdapter(ArrayList<String> paths){
    	this.paths.clear();
    	this.paths = paths;
    	notifyDataSetChanged();
    }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return paths.size();
	}

	@Override
	public Object getItem(int item) {
		// TODO Auto-generated method stub
		return paths.get(item);
	}

	@Override
	public long getItemId(int id) {
		// TODO Auto-generated method stub
		return id;
	}

	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
    	if (convertView == null) { 
    		LayoutInflater layoutInflater = LayoutInflater.from(context);
    		convertView = layoutInflater.inflate(R.layout.grid_view_item, null);
    		holder = new ViewHolder();
			
			holder.bg = (ImageView)convertView.findViewById(R.id.grid_view_image_bg);
			holder.bg.setScaleType(ScaleType.FIT_XY);
			holder.bg.setLayoutParams(lp);
			holder.playIcon = (ImageView)convertView.findViewById(R.id.grid_view_image_play_icon);
            convertView.setTag(holder);
    	}else { 
    		holder = (ViewHolder)convertView.getTag();
        } 
    	//如果文件存在则加载

    	String videoName = FileUtil.getNameOfVideo(paths.get(position)); 
    	if (videoName.contains(".avi")||videoName.contains(".AVI")) {
        	Bitmap bitmap = FileUtil.getFileBitmap(paths.get(position), imageHeight, imageWidth);  	
        	if(bitmap==null){
        		holder.bg.setImageDrawable(context.getResources().getDrawable(R.mipmap.empty_bg));
        	}else{//
        		holder.bg.setImageBitmap(bitmap);
        	}
		}else if(videoName.contains(".hw")||videoName.contains(".HW")){
//			Log.i("123", "aaaaa     "+paths.get(position));
	    	String picName = FileUtil.getPicPathOfVideo(paths.get(position),0);
//	    	Log.i("123", "pic path="+picPath);
	    
	    	if(!FileUtil.isBitmapExist(picName)){//默认图片
	    		holder.bg.setImageDrawable(context.getResources().getDrawable(R.mipmap.empty_bg));
	    	}else{//加载图片
//	    		imageLoader.displayImage("file:/"+picPath, holder.bg, options);    
	    		Drawable drawable = context.getResources().getDrawable(R.mipmap.empty_bg);
	    	
	    		Bitmap bitmap = FileUtil.loadImageBitmapFromDisk(picName,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
	    		holder.bg.setImageBitmap(bitmap);
	    	}
		}
        return convertView; 
	}
	
	class ViewHolder {
        public ImageView bg;
        public ImageView playIcon;
    }

}
