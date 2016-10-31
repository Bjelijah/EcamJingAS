package com.howell.ecamerajing.action;

import com.howell.ecamerajing.bean.NetRectFileItem;
import com.howell.ecamerajing.bean.Pagination;
import com.howell.ecamerajing.bean.Record;
import com.howell.jni.CamJniUtil;

/**
 * @author 霍之昊 
 *
 * 类说明：录像、录像列表API封装类
 */
public class PlayBack {
	private int fileHandle;	//获取录像列表的句柄
	private int userHandle;	//登录摄像机的句柄
//	private ECamAPJNI jni ;
	
	public PlayBack() {
		// TODO Auto-generated constructor stub
//		jni = new ECamAPJNI();
		CamJniUtil.cameraInit();
	}
	
	//登录到摄像机
	public boolean login(String ip){
		
		userHandle = CamJniUtil.cameraLogin(ip);
		if(userHandle == -1){
			return false;
		}else{
			return true;
		}
	}
	//sd卡开始录像
	public boolean startRecord(int slot){
		return CamJniUtil.startRecord(userHandle, slot) == 1 ? true : false;
	}
	//sd卡停止录像
	public boolean stopRecord(int slot){
		return CamJniUtil.stopRecord(userHandle, slot) == 1 ? true : false;
	}
	
	//获取录像列表句柄
	public boolean initToGetList(Pagination pagination){
		fileHandle = CamJniUtil.getListByPage(userHandle, 0, 1, new Record(), 0, 1, pagination);
		if(fileHandle == -1){
			return false;
		}else{
			return true;
		}
	}
	//获取录像列表
	public NetRectFileItem[] getList(int replayCount){
		return CamJniUtil.getReplayList(fileHandle,replayCount);
	}
	//释放fileHandle句柄
	public void deinitToGetList(){
		CamJniUtil.closeFileList(fileHandle);
	}
	//释放userHandle句柄
	public void logout(){
//		camjni.cameraLogout(userHandle);
		CamJniUtil.cameraLogout();
	}
	//获取录像截图
	public void getJpg(NetRectFileItem netRectFileItem){
//		jni.initGetJpg();
//		jni.getPlayBackJPG(userHandle, netRectFileItem);
//		jni.nativeGetJpgDeinit();
		
		CamJniUtil.jpgInit();
		CamJniUtil.getPlayBackJPG(userHandle, netRectFileItem);
		CamJniUtil.jpgDeinit();
	}
}
