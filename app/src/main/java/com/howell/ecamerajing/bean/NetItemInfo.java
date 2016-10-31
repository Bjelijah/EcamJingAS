package com.howell.ecamerajing.bean;

import android.graphics.Bitmap;

public class NetItemInfo {
	BitmapManager bitmapManager= new BitmapManager();
	NetRectFileItem item=null;
	public NetItemInfo(NetRectFileItem item) {
		this.item = item;
	}
	public NetItemInfo(NetRectFileItem item,Bitmap bitmap) {
		this.bitmapManager.setBitmap(bitmap);
		this.item = item;
	}
	public Bitmap getBitmap() {
		return this.bitmapManager.getBitmap();
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmapManager.setBitmap(bitmap);
	}
	public NetRectFileItem getItem() {
		return item;
	}
	public void setItem(NetRectFileItem item) {
		this.item = item;
	}
	
	public BitmapManager getBitmapManager() {
		return bitmapManager;
	}
	public void setBitmapManager(BitmapManager bitmapManager) {
		this.bitmapManager = bitmapManager;
	}

	public class BitmapManager{
		Bitmap bitmap = null;
		int bitmapWidth = 0;
		int bitmapHeight = 0;
		public Bitmap getBitmap() {
			return bitmap;
		}
		public void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
		}
		public int getBitmapWidth() {
			return bitmapWidth;
		}
		public void setBitmapWidth(int bitmapWidth) {
			this.bitmapWidth = bitmapWidth;
		}
		public int getBitmapHeight() {
			return bitmapHeight;
		}
		public void setBitmapHeight(int bitmapHeight) {
			this.bitmapHeight = bitmapHeight;
		}
		public void setBitmapSize(int w,int h){
			this.bitmapWidth = w;
			this.bitmapHeight = h;
		}
	}
}
