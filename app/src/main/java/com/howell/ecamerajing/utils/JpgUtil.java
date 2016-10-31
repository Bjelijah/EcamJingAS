package com.howell.ecamerajing.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class JpgUtil {
	static public Bitmap decodeYUV420SP( byte[] yuv420sp, int width,int height) {
		final int frameSize = width * height;
		int[] rgba = new int[frameSize];
		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				// rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) &
				// 0xff00) | ((b >> 10) & 0xff);
				// rgba, divide 2^10 ( >> 10)
				rgba[yp] = ((r << 14) & 0xff000000) | ((g << 6) & 0xff0000)
						| ((b >> 2) | 0xff00);
			}
		}
		Bitmap bmp = Bitmap.createBitmap(width, height,
				Config.ARGB_8888);
		bmp.setPixels(rgba, 0, width, 0, 0, width, height);
		return bmp;

	}



	public static Bitmap yuv2rgb(byte [] yuv,int width,int height){
		Log.i("123", "yuv2rgb   width="+width+"height="+height);
		int frameSize = width * height;
		//		Log.i("123", "framesize="+frameSize*4);
		int[] rgba = new int[frameSize];
		for (int i = 0; i < height; i++){
			for (int j = 0; j < width; j++) {
				//nv21
				int y = (0xff & ((int) yuv[i * width + j]));
				int u = (0xff & ((int) yuv [frameSize + (i >> 1) * width + (j & ~1) + 0]));
				int v = (0xff & ((int) yuv [frameSize + (i >> 1) * width + (j & ~1) + 1]));	
				y = y < 16 ? 16 : y;
				int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
				int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
				int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
				r = r < 0 ? 0 : (r > 255 ? 255 : r);
				g = g < 0 ? 0 : (g > 255 ? 255 : g);
				b = b < 0 ? 0 : (b > 255 ? 255 : b);
				rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
			}
		}    

		Bitmap bmp = Bitmap.createBitmap(width, height,	Config.ARGB_8888);
		bmp.setPixels(rgba, 0, width, 0, 0, width, height);
		return bmp;
	}

	public static Bitmap yuvToRGB(byte [] data, int imageWidth,int imageHeight){
		Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Config.ARGB_8888);
		int numPixels = imageWidth*imageHeight;

		// the buffer we fill up which we then fill the bitmap with
		IntBuffer intBuffer = IntBuffer.allocate(imageWidth*imageHeight);
		// If you're reusing a buffer, next line imperative to refill from the start,
		// if not good practice
		intBuffer.position(0);

		// Set the alpha for the image: 0 is transparent, 255 fully opaque
		final byte alpha = (byte) 255;

		// Get each pixel, one at a time
		for (int y = 0; y < imageHeight; y++) {
			for (int x = 0; x < imageWidth; x++) {
				// Get the Y value, stored in the first block of data
				// The logical "AND 0xff" is needed to deal with the signed issue
				int Y = data[y*imageWidth + x] & 0xff;

				// Get U and V values, stored after Y values, one per 2x2 block
				// of pixels, interleaved. Prepare them as floats with correct range
				// ready for calculation later.
				int xby2 = x/2;
				int yby2 = y/2;

				// make this V for NV12/420SP
				float U = (float)(data[numPixels + 2*xby2 + yby2*imageWidth] & 0xff) - 128.0f;

				// make this U for NV12/420SP
				float V = (float)(data[numPixels + 2*xby2 + 1 + yby2*imageWidth] & 0xff) - 128.0f;

				// Do the YUV -> RGB conversion
				float Yf = 1.164f*((float)Y) - 16.0f;
				int R = (int)(Yf + 1.596f*V);
				int G = (int)(Yf - 0.813f*V - 0.391f*U);
				int B = (int)(Yf            + 2.018f*U);

				// Clip rgb values to 0-255
				R = R < 0 ? 0 : R > 255 ? 255 : R;
				G = G < 0 ? 0 : G > 255 ? 255 : G;
				B = B < 0 ? 0 : B > 255 ? 255 : B;

				// Put that pixel in the buffer
				intBuffer.put(alpha*16777216 + R*65536 + G*256 + B);
			}
		}

		// Get buffer ready to be read
		intBuffer.flip();

		// Push the pixel information from the buffer onto the bitmap.
		bitmap.copyPixelsFromBuffer(intBuffer);
		return bitmap;
	}


	public static byte[] nv21ToI420(byte[] data, int width, int height) {  
		byte[] ret = new byte[4*width*height];  
		int total = width * height;  

		ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);  
		ByteBuffer bufferU = ByteBuffer.wrap(ret, total, total / 4);  
		ByteBuffer bufferV = ByteBuffer.wrap(ret, total + total / 4, total / 4);  

		bufferY.put(data, 0, total);  
		for (int i=total; i<data.length; i+=2) {  
			bufferV.put(data[i]);  
			bufferU.put(data[i+1]);  
		}  

		return ret;  
	}  

	@Deprecated //error
	/**
	 * error
	 * @param data
	 * @param width
	 * @param height
	 * @return
	 */
	public static byte [] I420ToNv21(byte [] data,int width,int height){//error
		byte [] ret = new byte[4*width*height];
		int total = width* height;

		ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);  
		bufferY.put(data,0,total);
		for(int i=total;i<total+ total/4;i++){
			int j = i;
			ret[j] = data[i];	
			j+=2;
		}

		for(int i=total+total/4;i<data.length;i++){
			int j=total;
			ret[j+1] = data[i];
			j+=2;
		}


		return ret;
	}

	public static Bitmap rgb24ToBitmap(byte [] rgb,int w,int h){
		MyBitmap myBitmap = new MyBitmap();
		return myBitmap.createMyBitmap(rgb, w, h);
	}







	private static class MyBitmap{

		public Bitmap createMyBitmap(byte[] data, int width, int height){ 
			int []colors = convertByteToColor(data);
			if (colors == null){
				return null;
			}

			Bitmap bmp = Bitmap.createBitmap(colors, 0, width, width, height, 
					Config.ARGB_8888);
			return bmp;
		}


		// 将一个byte数转成int
		// 实现这个函数的目的是为了将byte数当成无符号的变量去转化成int
		public  int convertByteToInt(byte data){

			int heightBit = (int) ((data>>4) & 0x0F);
			int lowBit = (int) (0x0F & data);
			return heightBit * 16 + lowBit;
		}


		// 将纯RGB数据数组转化成int像素数组
		public int[] convertByteToColor(byte[] data){
			int size = data.length;
			if (size == 0){
				return null;
			}

			int arg = 0;
			if (size % 3 != 0){
				arg = 1;
			}

			// 一般情况下data数组的长度应该是3的倍数，这里做个兼容，多余的RGB数据用黑色0XFF000000填充  
			int []color = new int[size / 3 + arg];
			int red, green, blue;

			if (arg == 0){
				for(int i = 0; i < color.length; ++i){
					red = convertByteToInt(data[i * 3]);
					green = convertByteToInt(data[i * 3 + 1]);
					blue = convertByteToInt(data[i * 3 + 2]); 

					// 获取RGB分量值通过按位或生成int的像素值      
					color[i] = (red << 16) | (green << 8) | blue | 0xFF000000; 
				}
			}else{
				for(int i = 0; i < color.length - 1; ++i){
					red = convertByteToInt(data[i * 3]);
					green = convertByteToInt(data[i * 3 + 1]);
					blue = convertByteToInt(data[i * 3 + 2]); 
					color[i] = (red << 16) | (green << 8) | blue | 0xFF000000; 
				}
				color[color.length - 1] = 0xFF000000;
			}
			return color;
		}
	}





}
