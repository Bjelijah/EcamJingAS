package com.howell.jni;

import com.howell.ecamerajing.bean.NetRectFileItem;
import com.howell.ecamerajing.bean.NetRfidInfo;
import com.howell.ecamerajing.bean.NetWlanApInfo;
import com.howell.ecamerajing.bean.Pagination;
import com.howell.ecamerajing.bean.Record;
import com.howell.ecamerajing.bean.SystimeInfo;

@SuppressWarnings("JniMissingFunction")
public class CamJniUtil {
	static{
		System.loadLibrary("hwplay");
		System.loadLibrary("player_jni");
	}
	public static native void cameraInit();
	public static native void cameraDeinit();	
	public static native int cameraLogin(String ip);	//登录
	public static native int cameraLogout();//登出
	public static native int cameraPlay(int isPlayBack,int slot,int is_sub,int connect_mode, NetRectFileItem netRectFileItem);
	public static native int cameraLoaclPlay(String fileName);
	public static native int cameraGetPos();
	public static native int cameraSetPos(int pos);
	public static native int cameraGetTotalMSec();
	public static native int cameraGetTotalFrame();
	public static native int cameraGetPlayedMsc();
	public static native int cameraGetCurFrame();
	public static native int cameraSetFrame(int frame);
	public static native int cameraPause(int bPause);
	
	public static native int cameraStop();
    //用于显示YUV数据
    public static native void YUVInit();			//初始化
    public static native void YUVDeinit();			//释放内存
    public static native void YUVSetCallbackObject(Object callbackObject,int flag);
    public static native void YUVSetCallbackMethodName(String methodStr,int flag);
    public static native void YUVLock();
    public static native void YUVUnlock();
    public static native void YUVSetEnable();//开始显示YUV数据
    public static native void YUVRenderY();			//渲染Y数据
    public static native void YUVRenderU();			//渲染U数据
    public static native void YUVRenderV();			//渲染V数据
	
    //用于播放音频
    public static native void AudioInit();		//初始化
    public static native void AudioDeinit();		//释放内存
	public static native void AudioSetCallbackObject(Object callbackObj,int flag);//flag 0
	public static native void AudioSetCallbackFieldName(String fieldStr,int flag);//flag  0 mAudioDataLength,1 mAudioData
	public static native void AudioSetCallbackMethodName(String methodStr,int flag);
    public static native void AudioStop();		//停止

	public static native int getFileList(int user_handle,int slot,SystimeInfo beg,SystimeInfo end);
	public static native int getFileListCount(int file_list_handle);
    public static native int getListByPage(int user_handle,int slot,int stream,Record replay,int type,int order_by_time,Pagination page_info);
	public static native int closeFileList(int file_list_handle);//释放内存与getListByPage成对出现
	public static native NetRectFileItem[] getReplayList(int fileHandle,int count);

	//获取录像列表 老协议 just test FIXME
	
	
	
	public static native NetRfidInfo nativeGetNetRfidInfo();			//获取NetRfidInfo
	public static native int nativeSetNetRfidInfo(NetRfidInfo info);	
	
	
	public static native void jpgInit();		//初始化获取图片接口
	public static native void jpgDeinit();	//释放内存
	public static native void jpgSetCallbackObject(Object callbackObj,int flag);
	public static native void jpgSetCallbackFieldName(String fieldStr,int flag);//"mJpgData"
	public static native void jpgSetCallbackMethodName(String methodStr,int flag);//"saveJpg2Sdcard"	
	@Deprecated
	public static native boolean getPlayBackJPG(int user_handle,NetRectFileItem netRectFileItem);//获取图片
	public static native void jpgSetNeedFirstJpg(String jpgPath);
	
	public static native void seekbarChange(int slot,int stream,int fileNo,int offsetSeconds);
	
	public static native int startRecord(int userhandle,int slot);
	public static native int stopRecord(int userhandle,int slot);

	/**
	 * 功能
	 */
	public static native boolean setCamSystemTime(SystimeInfo systime);
	public static native SystimeInfo getCamSystemTime();
	public static native boolean getCamRecordState();
	
	public static native boolean setWifiInfo(NetWlanApInfo info);
	public static native boolean getWifiInfo(NetWlanApInfo info);
	
	/**
	 * 下载
	 */
	public static native void downloadInit();
	public static native void downloadDeinit();
	public static native void downloadSetCallbackObject(Object obj,int flag);
	public static native void downloadSetCallbackFieldName(String fieldStr,int flag);//0 totalsize  1 cursize
	public static native void downloadSetCallbackMethodName(String methodStr,int flag);
	public static native boolean downloadStart(Object obj);
	public static native void downloadStop();
	
	public static native long downloadGetPos();
	public static native long downloadGetTotalLen();
	public static native long downloadGetCutTimeStamp();
	public static native int downloadGetFrameLen();//第一帧的长度
	
	
}
