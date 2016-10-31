package com.howell.ecamerajing.utils;
/**
 * @author 霍之昊 
 *
 * 类说明
 */
public interface Constable {
	final int DEBUG_TEST   = 0;
	
	final int NORMAL_MODE = 1;
	final int DOWNLIAD_MODE = 2;
	final int FAIL = 3;
	final int SUCCESS = 4;
	final String TEST_IP = "192.168.2.103";
//	final String TEST_IP = "192.168.128.249";
//	final String TEST_IP = "192.168.128.200";
	
	/*play mode*/
	final int PLAY_MODE_REVIEW 				= 0;//预览模式
	final int PLAY_MODE_PALYBACK			= 1;//回放模式
	final int PLAY_MODE_LOCAL_HW			= 2;//本地播放hw流
	final int PLAY_MODE_LOCAL_AVI			= 3;//本地播放avi流
	/*msg*/
	final int PLAY_MSG_LOGIN_ERROR			= 0x00;
	final int PLAY_MSG_PLAY_ERROR			= 0x01;
	final int PLAY_MSG_PLAY_PREPARE 		= 0x02;
	final int PLAY_MSG_PLAY_PROGRESS		= 0x03;
	final int PLAY_MSG_HIDE_CONTROL			= 0x04;
	final int PLAY_MSG_SET_POS				= 0x05;
	final int PLAY_MSG_ASNYC_INFO			= 0x06;
	final int PLAY_MSG_GET_FRAME			= 0x07;
	/*val*/
	final int PLAY_VAL_NEED_ASNYC			= 1;
	final int PLAY_VAL_NONEED_ASNYC			= 0;
	
	
	/*play back list*/
	final int PLAYBACK_MSG_LOGIN_ERROR		= 0x10;
	final int PLAYBACK_MSG_RECORD_ERROR		= 0x11;
	final int PLAYBACK_MSG_GETLIST_ERROR	= 0x12;
	final int PLAYBACK_MSG_LIST_PREPARE		= 0x13;
	final int PLAYBACK_MSG_LIST_REFRESH		= 0x14;
	final int PLAYBACK_MSG_ITEM_DOWNLOAD	= 0x15;
	final int PLAYBACK_MSG_JPG_REFRESH		= 0x16;

	
	
	final int PLAYBACK_VAL_DOWNLOAD_NO		= -1;
	final int PLAYBACK_VAL_DOWNLOAD_ING		= 0;
	final int PLAYBACK_VAL_DOWNLOAD_YES		= 1;
	
	
	
	/*course */
	final int COURSE_MSG_GIF_FINISH			= 0x20;
	final int COURSE_MSG_STEP_FINISH		= 0x21;
	
	
	/*download service*/
	final int DOWNLOAD_SERVICE_MSG_TASK_START   	= 0x30;//开始下载任务
	final int DOWNLOAD_SERVICE_MSG_TASK_END			= 0x31;//结束下载任务	
	final int DOWNLOAD_SERVICE_MSG_GET_POS			= 0x32;//获取下载进度
	final int DOWNLOAD_SERVICE_MSG_SERVICE_END		= 0x33;
	final String DOWNLOAD_SERVICE_KEY_CONTEXT		= "service_context";
	final int DOWNLOAD_SERVICE_VAL_CONTEXT_PBL		= 0;//playbacklist 发出的广播
	final int DOWNLOAD_SERVICE_VAL_CONTEXT_DLL		= 1;//DownloadList 发出的广播
	final int DOWNLOAD_VAL_DOWNLOAD_ING				= 1;
	final int DOWNLOAD_VAL_DOWNLOAD_WAIT			= 0;
	
	
	/*receiver*/
	final String DOWNLOAD_RECEIVE_ACTION_TASK_PBL  = "com.howell.ecamerajing.downloadpbl";
	final String DOWNLOAD_RECEIVE_ACTION_TASK_DLL  = "com.howell.ecamerajing.downloaddll";
	final String DOWNLOAD_RECEIVE_KEY_TASK_COUNT     = "task_count";
	final String DOWNLOAD_RECEIVE_ACTION_TASK_INFO	 = "com.howell.ecamerajing.servicetaskinfo"	;
	final String DOWNLOAD_RECEIVE_ACTION_TASK_CANCEL = "com.howell.ecamerajing.servicetaskcancel";
	final String DOWNLOAD_RECEIVE_ACTION_TASK_CANCEL_ALL = "com.howell.ecamerajing.servicetaskcancelall";	
	final String DOWNLOAD_RECEIVE_KEY_TASK_CANCEL	 = "task_cancel";	
	final String DOWNLOAD_RECEIVE_KEY_PROGRESS_POS	 = "task_progress_pos";
	final String DOWNLOAD_RECEIVE_KEY_FILE_INFO		 = "task_file_info";
	final String DOWNLOAD_RECEICVE_ACTION_TASK_PROGRESS = "com.howell.ecamerajing.downloadprogress";
	
}
