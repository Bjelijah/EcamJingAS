package com.howell.ecamerajing.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;

import com.howell.ecamerajing.bean.NetRectFileItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * @author 霍之昊 
 *
 * 类说明
 */
public class FileUtil {
	//SD卡路径
	private static String SDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	//ecamera_jing root path:
	public static final String eRoot = SDCardPath+"/eCamera";
	//ecamera_jing download video path:
	public static final String eVideo = eRoot+"/eVideo";
	//ecamera_jing download video review picture path:
	public static final String ePic = eVideo+"/ePic";
	//ecamera_jing download picture path:
	public static final String eCach = eRoot+"/jing_cache";

	//获取SD卡路径
	public static String getSDCardPath() {
		return SDCardPath;
	}
	//获取指定文件夹下的文件路径
	public static ArrayList<String> getFileNameByDir(File dir){
		System.out.println(dir.exists());
		File[] fileArray = dir.listFiles();
		ArrayList<String> mList = new ArrayList<String>();
		if(fileArray != null){
			for (File f : fileArray) {
				System.out.println(f.getPath());
				if(f.isFile() && !mList.contains(f.getPath())){
					mList.add(f.getPath());
				}
			}
		}else{
			System.out.println("fileArray is null");
		}
		return mList;
	}
	//以录像文件的创建时间命名
	public static String createNetRectFileItemFile(NetRectFileItem netRectFileItem){
		return getBitmapCachePath()+netRectFileItem.getFileNameToString();
	}
	//获取e看景缓存文件夹
	public static String getBitmapCachePath(){
		//		return getSDCardPath() + File.separator + "eCamera" + File.separator + "jing_cache" + File.separator;
		return ePic+File.separator;
	}
	//获取录像文件夹
	public static String getVideoCachePath(){
		return eVideo + File.separator;
	}

	public static String createVideoFilePathName(NetRectFileItem netRectFileItem){
		return getVideoCachePath()+netRectFileItem.getFileNameToString()+".hw";
	}

	public static boolean checkVideoFileExists(NetRectFileItem netRectFileItem){
		String filePath = createVideoFilePathName(netRectFileItem);
		//		Log.i("123", "filepath = "+filePath);
		File dir = new File(eVideo);

		File [] files = dir.listFiles();
		for (File file : files) {
			//			Log.i("123", "file="+file.toString()+" filePath="+filePath);
			if (file.isFile() && file.toString().equals(filePath)) {
				//				Log.i("123", "file exists");
				return true;
			}
		}
		//		Log.e("123", "file no exisits");
		return false;
	}


	//创建e看景缓存文件夹
	public static void createEcameraDir(){
		File dir = new File(eRoot);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		dir = new File(eCach);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		dir = new File(eVideo);	
		if(!dir.exists()){
			dir.mkdirs();
		}
		dir = new File(ePic);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	/** 
	 * 计算sdcard上的剩余空间 
	 * @return 
	 */  
	public static int freeSpaceOnSd() {  
		StatFs stat = new StatFs(getSDCardPath());  
		double sdFreeMB = ((double)stat.getAvailableBlocks() * (double) stat.getBlockSize()) / ( 1024 *1024 );  
		return (int) sdFreeMB;  
	} 

	//删除指定文件夹下的文件
	public static void removeCache(String dirPath) {  
		File dir = new File(dirPath);  
		File[] files = dir.listFiles();  
		if (files == null) {  
			return;  
		}  
		Log.i("123", "文件个数："+files.length);  
		for (int i = files.length ; i > 0; i--) {  
			if(files[i-1].isFile()){
				files[i - 1].delete();  
			}
		}  
	}  

	//保存图片到e看景文件夹下
	public static void saveBmpToSd(Bitmap bm, String filename) {  
		if (bm == null) {  
			return;  
		}  
		File file = new File(getBitmapCachePath() + filename);  
		try {  
			file.createNewFile();  
			OutputStream outStream = new FileOutputStream(file);  
			bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);  
			outStream.flush();  
			outStream.close();  
		} catch (FileNotFoundException e) {  
			Log.e("saveBmpToSd","FileNotFoundException");
		} catch (IOException e) {  
			Log.e("saveBmpToSd","IOException");
		}  
		Log.i("saveBmpToSd","create " + filename + " success");
	} 

	//判断在e看景缓存文件夹下是否存在指定文件
	public static boolean isBitmapExist(String fileName){
		if (fileName == null) {
			return false;
		}
		String picPath = FileUtil.getBitmapCachePath()+fileName+".jpg";
		
		File f = new File(picPath);
		return f.exists();
	}

	public static boolean isFileExist(String filePathName){
		if (filePathName==null) {
			return false;
		}
		File f = new File(filePathName);
		return f.exists();
		
	}
	
	public static String getPicPathOfVideo(String videoPath,int flag){
		String picName = videoPath.substring(videoPath.lastIndexOf(File.separator),videoPath.indexOf("."));	
		String picPath = FileUtil.ePic+picName+".jpg";
		if (flag==0) {
			return picName;
		}else if(flag ==1){
			return picPath;
		}
		return picPath;
	}

	public static String getNameOfVideo(String videoPath){
		return videoPath.substring(videoPath.lastIndexOf(File.separator)+1);
	}

	private static String String(String picPath) {
		// TODO Auto-generated method stub
		return null;
	}
	public static Bitmap getFileBitmap(String videoPath,int height,int width){
		return ThumbnailUtils.extractThumbnail(ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MICRO_KIND),
				width, height,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
	}

	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public static Bitmap Bytes2Bimap(byte[] b) {
		if (b.length != 0) {         
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;         
		}
	}	

	public static void saveImageBufToDisk(byte[] img, String fileName,int len,int width,int height) {
		
		
		
		
		
		Bitmap image = BitmapFactory.decodeByteArray(img, 0, img.length);
		saveImageBufToDisk(image,fileName,width,height);
	
	
//		try {
//			File file = new File(FileUtil.getBitmapCachePath() + fileName +".jpg");
//			FileOutputStream fops = new FileOutputStream(file);
//			fops.write(img);
//			fops.flush();
//			fops.close();
//			Log.i("123", "saveImageBufToDisk ok");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}




	public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		
		float scaleWidth = width;
		float scaleHeight = height;
		try {
			 scaleWidth = ((float) width / w);
			 scaleHeight = ((float) height / h);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
//		Log.i("", msg)
		
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}


	public static void saveImageBufToDisk(Bitmap bitmap,String fileName,int width,int height){
		try {
			File file = new File(FileUtil.getBitmapCachePath() + fileName +".jpg");
			FileOutputStream fops = new FileOutputStream(file);
//			zoomBitmap(bitmap,width,height).compress(Bitmap.CompressFormat.JPEG, 100, fops);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fops);
		} catch (FileNotFoundException e) {
		
			e.printStackTrace();
		}
	}

	public static void saveImageBufToDisk(YuvImage image,String fileName,int width,int height){
	
		try {
			File file = new File(FileUtil.getBitmapCachePath() + fileName +".jpg");
			FileOutputStream fops = new FileOutputStream(file);
			Rect rectangle = new Rect(0,0,width,height);			
			image.compressToJpeg(rectangle, 100, fops);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}

	public static Bitmap loadImageBitmapFromDisk(String fileName,int width,int height){
		String pathString = FileUtil.getBitmapCachePath()+fileName+".jpg";
		File file = new File(pathString);
		Bitmap bitmap = null;
		if(file.exists())
		{
			bitmap = BitmapFactory.decodeFile(pathString);
		}
		if (bitmap==null) {
			Log.e("123", "decodefile error");
			return null;
		}
		return zoomBitmap(bitmap, width, height);
	}

	public static RandomAccessFile createVideoFile(String pathFileName) throws FileNotFoundException {
		File file = new File(pathFileName);
		return new RandomAccessFile(file, "rw");
	}
	
	public static void write2VideoFile(RandomAccessFile file,byte [] data) throws IOException{
		
		file.write(data);
	}
	public static void closeVideoFile(RandomAccessFile file) throws IOException{
		file.close();
		
	}
	
	public static boolean delVideoFileCach(String filePathName){
		File file = new File(filePathName);  
		if (file.exists()) {
			return file.delete();
		}
		return false;
	}
	
	public static int CalcCach(File dir){
		if (dir == null) {
			return 0;
		}
		int cachSize = 0;
		if( dir.exists() && dir.isDirectory()){
			File [] files = dir.listFiles();
			for(int i=0;i<files.length;i++){
				if(files[i].isDirectory()){
					cachSize += CalcCach(files[i]);
				}
				cachSize += files[i].length();
				
			}
		}
		return cachSize;
	}
	
}
