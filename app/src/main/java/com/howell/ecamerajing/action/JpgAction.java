package com.howell.ecamerajing.action;

import com.howell.ecamerajing.bean.NetRectFileItem;
import com.howell.ecamerajing.utils.Constable;
import com.howell.ecamerajing.utils.FileUtil;
import com.howell.ecamerajing.utils.JpgUtil;
import com.howell.jni.CamJniUtil;

import android.graphics.Bitmap;
import android.util.Log;

public class JpgAction implements Constable{
	private static JpgAction mInstance = null;
//	private byte [] jpgBuf = new byte[3*1024*1024];
	private byte [] jpgBuf = null;
	private NetRectFileItem curItem = null;//jpg
	private String fileName;
	private int bitmapWidth;
	private int bitmapHeigh;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getBitmapWidth() {
		return bitmapWidth;
	}
	public void setBitmapWidth(int bitmapWidth) {
		this.bitmapWidth = bitmapWidth;
	}
	public int getBitmapHeigh() {
		return bitmapHeigh;
	}
	public void setBitmapHeigh(int bitmapHeigh) {
		this.bitmapHeigh = bitmapHeigh;
	}
	public static JpgAction getInstance(){
		if (mInstance==null) {
			mInstance = new JpgAction();
		}
		return mInstance;
	}
	private JpgAction(){
		CamJniUtil.jpgInit();
		CamJniUtil.jpgSetCallbackObject(this, 0);
		CamJniUtil.jpgSetCallbackFieldName("jpgBuf", 0);
		CamJniUtil.jpgSetCallbackMethodName("saveJpg", 0);
		CamJniUtil.jpgSetCallbackMethodName("creatBuf", 1);
	}

	@Override
	protected void finalize() throws Throwable {
		CamJniUtil.jpgDeinit();
		super.finalize();
	}
	public NetRectFileItem getCurItem() {
		return curItem;
	}
	public void setCurItem(NetRectFileItem curItem) {
		this.curItem = curItem;
		fileName = curItem.getFileNameToString();
		bitmapWidth = curItem.getBitmapWidth();
		bitmapHeigh = curItem.getBitmapHeight();
	}


	public void creatBuf(int len){
		jpgBuf = new byte[len];
//		CamJniUtil.jpgSetCallbackFieldName("jpgBuf", 0);
	}





	
	
	public void saveJpg(int len,int width,int height){
		Log.i("123", "len= "+len+"  width="+width+"  height="+height);
		if (jpgBuf==null) {
			Log.e("123", "jpgBuf==null");
			return;
		}
		//save to file
		//	FileUtil.saveImageBufToDisk(jpgBuf, curItem.getFileNameToString());
//		Bitmap bitmap = yuv2rgb(jpgBuf,width,height);
//		Bitmap bitmap = decodeYUV420SP(jpgBuf,width,height);
//		Bitmap bitmap = yuvToRGB(jpgBuf,width,height);
	
	
		
		
		
		try {
//			Bitmap bitmap = yuv2rgb(jpgBuf,width,height);
//			Bitmap bitmap = decodeYUV420SP(jpgBuf,width,height);
//			Bitmap bitmap = yuvToRGB(jpgBuf,width,height);
//			YuvImage image = new YuvImage(JpgUtil.I420ToNv21(jpgBuf,width,height), ImageFormat.NV21, width, height, null);
			
			
			
//			FileUtil.saveImageBufToDisk(bitmap, curItem.getFileNameToString(), curItem.getBitmapWidth(), curItem.getBitmapHeight());
			
//			FileUtil.saveImageBufToDisk(image, curItem.getFileNameToString(), width, height);
//			FileUtil.saveImageBufToDisk(jpgBuf, curItem.getFileNameToString(),len,curItem.getBitmapWidth(), curItem.getBitmapHeight());
		
//			YV12ToRGB24 bar = new YV12ToRGB24(width, height);
//			byte [] rgb = new byte[width*height*4];
//			boolean ret = false;
//			ret = bar.convert(jpgBuf, rgb);
//			Log.i("123", "bar convert ret="+ret);
//			FileUtil.saveImageBufToDisk(rgb, curItem.getFileNameToString(), len, curItem.getBitmapWidth(), curItem.getBitmapHeight());
			
			
			Bitmap bitmap = JpgUtil.rgb24ToBitmap(jpgBuf, width, height);
			FileUtil.saveImageBufToDisk(bitmap, fileName, bitmapWidth, bitmapHeigh);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	
		
		//save to item
		if (curItem!=null) {
			//		curItem.setBitmap(FileUtil.zoomBitmap(bitmap, curItem.getBitmapWidth(), curItem.getBitmapHeight()));
		}
	}




	@Deprecated
	public boolean getPlayBackJpg(int user_handle,NetRectFileItem  netRectFileItem){
		return CamJniUtil.getPlayBackJPG(user_handle, netRectFileItem);
	}


	public void setNeedFirstFrameJpg(NetRectFileItem netRectFileItem){
		setCurItem(netRectFileItem);
		String path = FileUtil.getBitmapCachePath()+netRectFileItem.getFileNameToString()+".jpg";
		CamJniUtil.jpgSetNeedFirstJpg(path);
	}

	public void setNeedFirstFrameJpg(String fileName,int width,int height){
		this.fileName = fileName;
		this.bitmapWidth = width;
		this.bitmapHeigh = height;
		
		String path = FileUtil.getBitmapCachePath()+fileName+".jpg";		
		CamJniUtil.jpgSetNeedFirstJpg(path);
	}
}
