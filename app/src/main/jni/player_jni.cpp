#include <jni.h>
#include <pthread.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <time.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>


#include "include/stream_type.h"
#include "include/hwplay/play_def.h"
#include "include/net_sdk.h"
#include "com_howell_jni_CamJniUtil.h"

#define JNI_TEST (0)

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "log123", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "log123", __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "log123", __VA_ARGS__))


#define  HW_MEDIA_TAG 0x48574D49


typedef struct{
	PLAY_HANDLE play_handle;
	LIVE_STREAM_HANDLE live_stream_handle;
	FILE_STREAM_HANDLE file_stream_handle;
	FILE_LIST_HANDLE file_list_handle;
	ALARM_STREAM_HANDLE alarm_stream_handle;
	USER_HANDLE user_handle;
	UDP_FILE_STREAM_HANDLE udp_file_handle;
	UDP_FILE_STREAM_HANDLE udp_download_handle;
}handle_manager_t;



typedef struct{

	int play_mode;//0:review  1:playback  2:local
	int media_head_len;
	size_t stream_len;
	JavaVM * jvm;
	JNIEnv * env;//线程中
	jobject callback_obj;
	jmethodID net_rfid_alarm_method;
	int isExit;
}stream_manager_t;


typedef struct {
	int slot;
	SYSTEMTIME beg;
	SYSTEMTIME end;
	int type;/*0-normal 1-normal file 2-mot file*/
}tRecFile;

// jzh: 与struct tRecFile一样的大小,和原协议兼容
typedef struct {
	uint32_t fileno; // 文件序号/标识符,用于表示唯一的录像资源
	SYSTEMTIME created; // 录像资源创建时间
	uint32_t total_seconds; // 录像资源总时间
	uint32_t total_frames; // 录像资源总帧数
	uint32_t vod_seconds; // 点播偏移秒数
	uint32_t reserved; // 保留
	uint32_t fileno2; // 文件序号/标识符,用于表示唯一的录像资源
} NetRecFileItem;

typedef struct{
	char set_time_method_name[32];
	char request_method_name[32];
}yv12_info_t;

typedef struct{
	char * y;
	char * u;
	char * v;
	unsigned long long time;
	int width;
	int height;
	int enable;

	/* multi thread */
	int method_ready;
	JavaVM * jvm;
	JNIEnv * env;
	jmethodID request_render_method,set_time_method;
	jobject callback_obj;
	pthread_mutex_t lock;
	unsigned long long first_time;
}YV12_display_t;

typedef struct{
	char callback_method_name[32];
}audio_info_t;

typedef struct{
	/* multi thread */
	int method_ready;
	JavaVM * jvm;
	JNIEnv * env;
	jmethodID methodID_audio_write;
	jobject callback_obj;
	jfieldID fieldID_data_length;
	jbyteArray data_array;
	int data_array_len;
	int stop;
}audio_manager_t;

typedef struct{
	char callback_field_name[32];
	char callback_method_name[32];
	char callback_create_buf_method_name[32];
	char first_jpg_path[64];
}jpg_info_t;

typedef struct GetJpg
{
	/* multi thread */
	JavaVM * jvm;
	JNIEnv * env;
	jmethodID mid,creatBuf;
	jobject obj;
	jbyteArray data_array;
	int bNeedFistJpg;
	pthread_mutex_t lock;
}jpg_manager_t;

#if 1
typedef struct
{
	unsigned long long len;/*文件的长度*/
	int reserved[32];/*保留,必须为0*/
}file_stream_head_t;
#endif

typedef struct DownLoad//FIXMEs
{
	int method_ready; //do once
	int head_ready;//do once
	JavaVM * jvm;
	JNIEnv * env;
	JNIEnv * env2;
	jfieldID total_size,cur_size,first_timesteamp;
	jobject obj;
	jfieldID fieldID_data_length;
	jbyteArray data_array;
	jmethodID methodID_download_write;
	char callback_method_name[32];
	int data_array_len;
	int bStop;
	long stream_len;
	long long timestemp;
	bool bfirst_timestamp;
	int frame_len;
	pthread_mutex_t lock;
	jlong download_file_len;
}download_maganer_t;

typedef struct {
	long len;
	long type; //0-bbp frame,1-i frame,2-audio
	unsigned long long time_stamp;
	long tag;
	long sys_time;
	//long reserve[1];
}stream_head_t;

typedef struct{
	int cur_pos;//当前buf 结尾的地方
	long fream_len;//帧结束的地方
	int offset;
	bool bDoOnce;//第一次
}download_timestamp_t;

static handle_manager_t* g_handle_mgr  = NULL;
static stream_manager_t* g_stream_mgr  = NULL;
static yv12_info_t    * g_yuv_info     = NULL;
static YV12_display_t * g_yuv_display  = NULL;
static audio_info_t   * g_audio_info   = NULL;
static audio_manager_t* g_audio_mgr	   = NULL;
static jpg_info_t	  * g_jpg_info	   = NULL;
static jpg_manager_t  * g_jpg_mgr	   = NULL;
static download_maganer_t* g_download_mgr = NULL;
static download_timestamp_t* g_download_stamp = NULL;
//local interface


void audio_play(const char* buf,int len,int au_sample,int au_channel,int au_bits)
{
	if(g_audio_mgr==NULL)return;
	if(g_audio_info == NULL)return;
	if (g_audio_mgr->stop) return;
	if(g_audio_mgr->callback_obj==NULL){
		LOGE("audio callback obj = null");
		return;
	}

	if (g_audio_mgr->jvm->AttachCurrentThread(&g_audio_mgr->env,NULL) != JNI_OK) {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
		return;
	}
	/* get JAVA method first */
	if (!g_audio_mgr->method_ready) {

		LOGE("audio_play11111111   ");

		jclass cls = g_audio_mgr->env->GetObjectClass(g_audio_mgr->callback_obj);
		LOGE("audio_play22222");
		if (cls == NULL) {
			LOGE("FindClass() Error.....");
			goto error;
		}
		//�ٻ�����еķ���

		g_audio_mgr->methodID_audio_write = g_audio_mgr->env->GetMethodID(cls,g_audio_info->callback_method_name,"()V");

		if ( g_audio_mgr->methodID_audio_write == NULL) {
			LOGE("%d  GetMethodID() Error.....   methodname:%s",__LINE__,g_audio_info->callback_method_name);
			goto error;
		}

		g_audio_mgr->method_ready=1;
	}

	/* update length */
	g_audio_mgr->env->SetIntField(g_audio_mgr->callback_obj,g_audio_mgr->fieldID_data_length,len);

	if (len<=g_audio_mgr->data_array_len) {
		g_audio_mgr->env->SetByteArrayRegion(g_audio_mgr->data_array,0,len,(const signed char*)buf);
		g_audio_mgr->env->CallVoidMethod(g_audio_mgr->callback_obj,g_audio_mgr->methodID_audio_write,NULL);
	}

	if (g_audio_mgr->jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}

	return;
	error:
	if (g_audio_mgr->jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}
}

void do_download(const char* buf,int len){
	if(g_download_mgr==NULL){
		LOGE("do_download mgr==null");
		return;
	}
	if(g_download_mgr->bStop){
		LOGE("do_download bstop");
		return;
	}
	if(g_download_mgr->obj==NULL){
		LOGE("obj == null;");
		return;
	}
	//	LOGI("do_download");



	if (g_download_mgr->jvm->AttachCurrentThread(&g_download_mgr->env,NULL) != JNI_OK) {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
		return;
	}

	if(!g_download_mgr->method_ready){


		jclass clz = g_download_mgr->env->GetObjectClass(g_download_mgr->obj);
		if(clz == NULL){
			LOGE("%s clz==null",__func__);
			goto error;
		}else{
			LOGI("get clz ok");
		}

		g_download_mgr->methodID_download_write = g_download_mgr->env->GetMethodID(clz,g_download_mgr->callback_method_name,"()V");
		if(g_download_mgr->methodID_download_write == NULL){
			LOGE("get method id error");
			goto error;
		}
		g_download_mgr->method_ready = 1;
	}

	g_download_mgr->env->SetLongField(g_download_mgr->obj,g_download_mgr->cur_size,g_download_mgr->stream_len);
	g_download_mgr->env->SetIntField(g_download_mgr->obj,g_download_mgr->fieldID_data_length,len);

	pthread_mutex_lock(&g_download_mgr->lock);
	if(g_download_mgr==NULL)  {
		LOGE("do_download mgr 2=null");
		pthread_mutex_unlock(&g_download_mgr->lock);
		goto error;
	}
	if(g_download_mgr->bStop) {
		LOGE("do_download bstop 2");
		pthread_mutex_unlock(&g_download_mgr->lock);
		goto error;
	}
	if(g_download_mgr->obj==NULL) {
		LOGE("obj == null 2");
		pthread_mutex_unlock(&g_download_mgr->lock);
		goto error;
	}

	if(!g_download_mgr->head_ready){
		g_download_mgr->head_ready = 1;

		HW_MEDIAINFO media_head;
		memset(&media_head,0,sizeof(media_head));
		media_head.media_fourcc = HW_MEDIA_TAG;
		media_head.vdec_code = VDEC_H264;
		media_head.adec_code = ADEC_ADPCM_WAV;
		media_head.au_bits = 16;
		media_head.au_sample = 8;
		media_head.au_channel = 1;

		g_download_mgr->env->SetByteArrayRegion(g_download_mgr->data_array,0,sizeof(media_head),(const signed char*)&media_head);
		//		g_download_mgr->env->SetCharArrayRegion(g_download_mgr->data_array,0,sizeof(media_head),(jchar *)&media_head);
		//		LOGI("head set byte array ok");jbtye
		g_download_mgr->env->CallVoidMethod(g_download_mgr->obj,g_download_mgr->methodID_download_write);

	}

	//	stream_head_t stream_head;
	//	memset(&stream_head,0,sizeof(stream_head));
	//	memcpy(&stream_head,buf,sizeof(stream_head));
	//	LOGI("len=%d    len=%ld   type=%ld  timestemp=%lld   tag=%ld   sys_time=%ld",len,stream_head.len,
	//			stream_head.type,stream_head.time_stamp,stream_head.tag,stream_head.sys_time);

	//	g_download_mgr->env->SetLongField(g_download_mgr->obj,g_download_mgr->cur_timesteamp,stream_head.time_stamp);



	if (len<=g_download_mgr->data_array_len) {
		g_download_mgr->env->SetByteArrayRegion(g_download_mgr->data_array,0,len,(const signed char*)buf);
		//		g_download_mgr->env->SetCharArrayRegion(g_download_mgr->data_array,0,len,(jchar *)buf);
		//		LOGI("set byte array ok");
		g_download_mgr->env->CallVoidMethod(g_download_mgr->obj,g_download_mgr->methodID_download_write);

	}
	pthread_mutex_unlock(&g_download_mgr->lock);

	if (g_download_mgr->jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}

	return;
	error:
	if (g_download_mgr->jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}
}


void yv12gl_display(const unsigned char * y, const unsigned char *u,const unsigned char *v, int width, int height, unsigned long long time)
{
	if(g_yuv_display == NULL) return;
	if (!g_yuv_display->enable) return;
	g_yuv_display->time = time/1000;

	if( g_yuv_display->jvm->AttachCurrentThread(&g_yuv_display->env,NULL)!= JNI_OK) {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
		return;
	}
	/* get JAVA method first */
	if (!g_yuv_display->method_ready) {
		jclass cls =  g_yuv_display->env->GetObjectClass(g_yuv_display->callback_obj);
		if (cls == NULL) {
			LOGE("FindClass() Error.....");
			goto error;
		}
		//�ٻ�����еķ���

		g_yuv_display->request_render_method = g_yuv_display->env->GetMethodID(cls,g_yuv_info->request_method_name,"()V");
		g_yuv_display->set_time_method = g_yuv_display->env->GetMethodID(cls,g_yuv_info->set_time_method_name,"(J)V");
		if (g_yuv_display->request_render_method == NULL || g_yuv_display->set_time_method == NULL) {
			LOGE("%d  GetMethodID() Error.....",__LINE__);
			goto error;
		}
		g_yuv_display->method_ready=1;
	}
	g_yuv_display->env->CallVoidMethod(g_yuv_display->callback_obj,g_yuv_display->set_time_method,g_yuv_display->time);


	pthread_mutex_lock(&g_yuv_display->lock);

	if (width!=g_yuv_display->width || height!=g_yuv_display->height) {
		LOGI("g_display->width = %d  width=%d",g_yuv_display->width,width);
		if(g_yuv_display->y!=NULL){
			free(g_yuv_display->y);
			g_yuv_display->y = NULL;
		}
		if(g_yuv_display->u!=NULL){
			free(g_yuv_display->u);
			g_yuv_display->u = NULL;
		}
		if(g_yuv_display->v!=NULL){
			free(g_yuv_display->v);
			g_yuv_display->v = NULL;
		}
		g_yuv_display->y = (char *)realloc(g_yuv_display->y,width*height);
		g_yuv_display->u = (char *)realloc(g_yuv_display->u,width*height/4);
		g_yuv_display->v = (char *)realloc(g_yuv_display->v,width*height/4);
		g_yuv_display->width = width;
		g_yuv_display->height = height;
	}
	memcpy(g_yuv_display->y,y,width*height);
	memcpy(g_yuv_display->u,u,width*height/4);
	memcpy(g_yuv_display->v,v,width*height/4);
	pthread_mutex_unlock(&g_yuv_display->lock);

	g_yuv_display->env->CallVoidMethod(g_yuv_display->callback_obj,g_yuv_display->request_render_method,NULL);

	if (g_yuv_display->jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}
	return;

	error:
	if (g_yuv_display->jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}
	return;
}


bool YV12_to_RGB24(unsigned char* pYV12, unsigned char* pRGB24, int iWidth, int iHeight)
{
	if(!pYV12 || !pRGB24)
		return false;
	const long nYLen = long(iHeight * iWidth);
	const int nHfWidth = (iWidth>>1);
	if(nYLen<1 || nHfWidth<1)
		return false;
	// yv12数据格式，其中Y分量长度为width * height, U和V分量长度都为width * height / 4
	// |WIDTH |
	// y......y--------
	// y......y   HEIGHT
	// y......y
	// y......y--------
	// v..v
	// v..v
	// u..u
	// u..u
	unsigned char* yData = pYV12;
	unsigned char* vData = &yData[nYLen];
	unsigned char* uData = &vData[nYLen>>2];
	if(!uData || !vData)
		return false;
	// Convert YV12 to RGB24
	//
	// formula
	//                                       [1            1                        1             ]
	// [r g b] = [y u-128 v-128] [0            0.34375             0             ]
	//                                       [1.375      0.703125          1.734375]
	// another formula
	//                                       [1                   1                      1            ]
	// [r g b] = [y u-128 v-128] [0                   0.698001         0            ]
	//                                       [1.370705      0.703125         1.732446]
	int rgb[3];
	int i, j, m, n, x, y;
	m = -iWidth;
	n = -nHfWidth;
	for(y=0; y < iHeight; y++)
	{
		m += iWidth;
		if(!(y % 2))
			n += nHfWidth;
		for(x=0; x < iWidth; x++)
		{
			i = m + x;
			j = n + (x>>1);
			rgb[2] = int(yData[i] + 1.370705 * (vData[j] - 128)); // r分量值
			rgb[1] = int(yData[i] - 0.698001 * (uData[j] - 128)  - 0.703125 * (vData[j] - 128)); // g分量值
			rgb[0] = int(yData[i] + 1.732446 * (uData[j] - 128)); // b分量值
			//			j = nYLen - iWidth - m + x;//图像翻转
			j = m+x;
			i = (j<<1) + j;
			for(j=0; j<3; j++)
			{
				if(rgb[j]>=0 && rgb[j]<=255)
					pRGB24[i + j] = rgb[j];
				else
					pRGB24[i + j] = (rgb[j] < 0) ? 0 : 255;
			}
		}
	}
	return true;
}




void save_play_back_jpg(const char* buf,int len,int w,int h){
	if( g_jpg_mgr->jvm->AttachCurrentThread(&g_jpg_mgr->env,NULL)!= JNI_OK) {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
		return;
	}
	int rgb_len = w*h*4;
	char *pRGB24 = (char *)malloc(rgb_len);
	char *pUvBuf = (char *)malloc(len);

	//	int buf_len = 128 * 1024;
	//	char* jpg_buf = (char *)malloc(buf_len);
	//	memset(jpg_buf,0, buf_len);

	jclass cls = g_jpg_mgr->env->GetObjectClass(g_jpg_mgr->obj);
	jfieldID id = 0 ;
	jbyteArray data_array_local_ref = NULL;

	g_jpg_mgr->mid = g_jpg_mgr->env->GetMethodID( cls, g_jpg_info->callback_method_name, "(III)V");
	g_jpg_mgr->creatBuf = g_jpg_mgr->env->GetMethodID(cls,g_jpg_info->callback_create_buf_method_name,"(I)V");
	if (g_jpg_mgr->mid  == NULL) {
		LOGE("%d  GetMethodID() Error.....",__LINE__);
		goto error;
	}


	g_jpg_mgr->env->CallVoidMethod(g_jpg_mgr->obj, g_jpg_mgr->creatBuf,rgb_len);

	id = g_jpg_mgr->env->GetFieldID(cls,g_jpg_info->callback_field_name,"[B");
	data_array_local_ref = (jbyteArray)g_jpg_mgr->env->GetObjectField(g_jpg_mgr->obj,id);
	g_jpg_mgr->data_array = (jbyteArray)g_jpg_mgr->env->NewGlobalRef( data_array_local_ref);


	if(g_jpg_mgr->data_array==NULL){
		LOGE("data_array=null");
		goto error;
	}

	pthread_mutex_lock(&g_jpg_mgr->lock);

	for(int i=0;i<len;i++){
		if(buf[i]==0){
			LOGI("%d  %x",i,buf[i]);
		}

	}

	if(g_jpg_mgr->data_array==NULL){
		LOGE("data_array==null");
		goto error;
	}

	//	yv12_2_nv21(buf,pUvBuf,len,w,h);
	YV12_to_RGB24((unsigned char*)buf,(unsigned char*)pRGB24,w,h);


	g_jpg_mgr->env->SetByteArrayRegion(g_jpg_mgr->data_array,0,rgb_len,(const jbyte*)pRGB24);//FIXME
	//	g_jpg_mgr->env->SetByteArrayRegion(g_jpg_mgr->data_array,0,len,(const jbyte*)buf);//FIXME
	//	g_jpg_mgr->env->SetByteArrayRegion(g_jpg_mgr->data_array,0,len,(const jbyte*)pUvBuf);//FIXME


	g_jpg_mgr->env->CallVoidMethod(g_jpg_mgr->obj, g_jpg_mgr->mid,rgb_len,w,h);//FIXME
	pthread_mutex_unlock(&g_jpg_mgr->lock);

	if (g_jpg_mgr->jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}
	if(pRGB24!=NULL){
		free(pRGB24);
		pRGB24 = NULL;
	}
	if(pUvBuf!=NULL){
		free(pUvBuf);
		pUvBuf = NULL;
	}


	return;

	error:
	if (g_jpg_mgr->jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}
	if(pRGB24!=NULL){
		free(pRGB24);
		pRGB24 = NULL;
	}

	if(pUvBuf!=NULL){
		free(pUvBuf);
		pUvBuf = NULL;
	}
}

void savePlayBackJpg(PLAY_HANDLE ph){//nouse
	//	int buf_len = 128 * 1024;
	//	char* jpg_buf = (char *)malloc(buf_len);
	//	memset(jpg_buf,0, buf_len);
	//	int jpg_len = 0;
	LOGI("first jpg path = %s",g_jpg_info->first_jpg_path);
	int ret = hwplay_save_to_jpg(ph,g_jpg_info->first_jpg_path,100);//ret=0

	LOGI("play back jpg ret=%d",ret);
}







static long long frame_num = 0;
static long first_timestamp = 0;

static void showCurTimeStamp(long cur_time){

	long curSec = cur_time/1000;
	long sec = curSec%60;
	long min = curSec/60;


	LOGI("showCurTimeStamp    cur_time=  %02ld :  %02ld   ",min,sec);

}

static void on_yuv_callback_ex(PLAY_HANDLE handle,
		const unsigned char* y,
		const unsigned char* u,
		const unsigned char* v,
		int y_stride,
		int uv_stride,
		int width,
		int height,
		unsigned long long time,
		long user)
{
	//	if(res->isExit) return;
	//__android_log_print(ANDROID_LOG_INFO, "jni", "on_yuv_callback_ex  time: %llu",time);

	if(first_timestamp == 0 && time!=0){
		first_timestamp = time;
	}
	LOGE("_________");
	showCurTimeStamp( (time - first_timestamp)/1000);


	yv12gl_display(y,u,v,width,height,time);
}

static void on_audio_callback(PLAY_HANDLE handle,
		const char* buf,//数据缓存,如果是视频，则为YV12数据，如果是音频则为pcm数据
		int len,//数据长度,如果为视频则应该等于w * h * 3 / 2
		unsigned long timestamp,//时标,单位为毫秒
		long user){
	//	if(res->isExit) return;
	//__android_log_print(ANDROID_LOG_INFO, "audio", "on_audio_callback timestamp: %lu ",timestamp);

	audio_play(buf,len,0,0,0);

}


static void on_source_callback(PLAY_HANDLE handle, int type, const char* buf, int len, unsigned long timestamp, long sys_tm, int w, int h, int framerate, int au_sample, int au_channel, int au_bits, long user){
	if(type == 0){//音频
		audio_play(buf,len,au_sample,au_channel,au_bits);
	}else if(type == 1){//视频
		frame_num++;
		//		LOGI("framerate=%d    len=%d",framerate,len);
		if(g_jpg_mgr){
			if(g_jpg_mgr->bNeedFistJpg==1){
				LOGE("time=%ld   sys_time=%ld   len = %d   framerate=%d",timestamp,sys_tm,len,framerate);

				g_jpg_mgr->bNeedFistJpg = 0;
				save_play_back_jpg(buf,len,w,h);
			}
		}

		//test//FIXME
		if(first_timestamp == 0 && timestamp!=0){
			first_timestamp = timestamp;
		}
		LOGE("_________");
		showCurTimeStamp( timestamp - first_timestamp);
		unsigned char* y = (unsigned char *)buf;
		unsigned char* u = y+w*h;
		unsigned char* v = u+w*h/4;
		yv12gl_display(y,u,v,w,h,timestamp);
	}
}

static void on_download_callback(PLAY_HANDLE handle, int type, const char* buf, int len, unsigned long timestamp, long sys_tm, int w, int h, int framerate, int au_sample, int au_channel, int au_bits, long user){

	if(g_download_mgr==NULL) return;
	if(g_download_mgr->bStop) return;

	if(type == 1){
		g_download_mgr->timestemp = timestamp;
	}

	//	if(type == 0){//音频
	//		audio_play(buf,len,au_sample,au_channel,au_bits);
	//	}else if(type == 1){//视频
	//		unsigned char* y = (unsigned char *)buf;
	//		unsigned char* u = y+w*h;
	//		unsigned char* v = u+w*h/4;
	//		yv12gl_display(y,u,v,w,h,timestamp);
	//	}

	//	do_download(buf,len);
	//	LOGI("timestemp = %ld",timestamp);
	//	if (g_download_mgr->jvm->AttachCurrentThread(&g_download_mgr->env,NULL) != JNI_OK) {
	//		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
	//		return;
	//	}

	//	g_download_mgr->env->SetLongField(g_download_mgr->obj,g_download_mgr->cur_timesteamp,timestamp);


	//	if (g_download_mgr->jvm->DetachCurrentThread() != JNI_OK) {
	//		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	//	}


}


void on_live_stream_fun(LIVE_STREAM_HANDLE handle,int stream_type,const char* buf,int len,long userdata){
	//__android_log_print(ANDROID_LOG_INFO, "jni", "-------------stream_type %d-len %d",stream_type,len);
	g_stream_mgr->stream_len += len;
	int ret = hwplay_input_data(g_handle_mgr->play_handle, buf ,len);
}

static long long first_time_stamp = 0;


long getCurrentTime()
{
	struct timeval tv;
	gettimeofday(&tv,NULL);
	return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

long first_time=0;
long cur_time = 0;
int time_num = 0;
void on_udp_file_stream_fun(UDP_FILE_STREAM_HANDLE handle,const char* buf,int len,long userdata){
	if(g_stream_mgr->isExit) return;
	g_stream_mgr->stream_len += len;
	//
	//	//Test FIXME
	LOGI("udp file_stream_fun  stream_len=%d  len=%d",g_stream_mgr->stream_len,len);
	stream_head_t stream_head;
	memset(&stream_head,0,sizeof(stream_head));
	memcpy(&stream_head,buf,sizeof(stream_head));
	LOGI("type=%ld len=%ld timestamp=%lld",stream_head.type,stream_head.len,stream_head.time_stamp);
	if(first_time_stamp == 0 && stream_head.time_stamp!=0){
		first_time_stamp = stream_head.time_stamp;

	}


//	showCurTimeStamp((stream_head.time_stamp - first_time_stamp)/1000);
//
//	if(stream_head.type !=2 ){
//		cur_time = getCurrentTime();
//		time_num++;
//		if(first_time==0){
//			first_time=cur_time;
//		}
//		if(time_num==25){
//			LOGE("25frames cost %ld msec ",cur_time - first_time);
//			time_num = 0;
//			first_time = 0;
//		}
//	}
	int ret = hwplay_input_data(g_handle_mgr->play_handle, buf ,len);

	LOGI("input data ret=%d  ",ret);
}

void on_file_stream_fun(FILE_STREAM_HANDLE handle,const char* buf,int len,long userdata){
	if(g_stream_mgr->isExit) return;
	g_stream_mgr->stream_len += len;
	//	LOGI("len = %d" ,g_stream_mgr->stream_len );
	int ret = hwplay_input_data(g_handle_mgr->play_handle, buf ,len);
}

int g_cur_pos = 0;//当前buf 结尾的地方
long g_fream_len = 0;
int g_offset = 0;
bool g_bDoOnce = true;//第一次
static stream_head_t g_stream_head;
int g_bar = 0;
bool g_timestamp_need_copy = false;
int g_get_stream_head_num = 10;
int g_copy_offset = 0;
void get_stream_head_info(const char* buf,int len){
	if(g_download_mgr==NULL) return;
	if(g_download_mgr->bStop) return;
	if(g_download_stamp == NULL){
		LOGE("g_download_stamp == NULL");
		return;
	}


	memset(&g_stream_head,0,sizeof(g_stream_head));
	int offset = 0;
	g_get_stream_head_num = 10;
	do {
		memcpy(&g_stream_head,buf+g_download_stamp->offset,sizeof(g_stream_head));
		g_download_mgr->timestemp = g_stream_head.time_stamp;
		g_download_stamp->fream_len = g_stream_head.len;
		//		LOGI("type=%ld   len=%ld  time=%lld", g_stream_head.type,g_stream_head.len,g_stream_head.time_stamp );

		offset = g_download_stamp->offset;
		if(offset + g_stream_head.len < len && (len-(offset + g_stream_head.len)>= sizeof(g_stream_head))){
			g_download_stamp->offset = offset + g_stream_head.len;
		}else if((len-(offset + g_stream_head.len)) < sizeof(g_stream_head)){

			//
			//			LOGE("stream head out of range  size =  %d    head.len=%ld",len-(offset + g_stream_head.len),g_stream_head.len);

			offset = offset+g_stream_head.len;
			memset(&g_stream_head,0,sizeof(g_stream_head));
			//			LOGE("offset = %d   len-offset=%d",offset,len-offset);
			g_copy_offset = len-offset;
			memcpy(&g_stream_head,buf+offset,len-offset);
			g_bar = sizeof(g_stream_head)-(len-offset);
			//			LOGE("head size=%d   g_bar=%d",sizeof(g_stream_head),g_bar);
			g_timestamp_need_copy = true;
			//			for(int i=0;i<len-offset;i++){
			//				LOGE("%x",*(buf+offset+i));
			//			}


			break;
		}
		g_get_stream_head_num--;
	} while (offset + g_stream_head.len < len && g_get_stream_head_num>0);

	g_download_stamp->cur_pos = 0;
	g_download_stamp->cur_pos += len - g_download_stamp->offset;
}

void calc_timestemp(const char* buf,int len){
	if(g_download_mgr==NULL) return;
	if(g_download_mgr->bStop) return;
	if(g_download_stamp==NULL) return;
	if(g_timestamp_need_copy){
		g_timestamp_need_copy = false;
		//
		LOGE("need copy g_bar=%d   g_copy_offset=%d",g_bar,g_copy_offset);
		//		for(int i=0;i<g_bar;i++){
		//			LOGE("%x",buf[i]);
		//		}
		memcpy(&g_stream_head+g_copy_offset,buf,g_bar);
		LOGI("need copy type=%ld len=%ld time=%lld",g_stream_head.type,g_stream_head.len,g_stream_head.time_stamp);
		g_download_stamp->fream_len = g_stream_head.len;
		g_download_stamp->cur_pos = 0;
		g_download_stamp->cur_pos -= g_bar;
	}
	g_download_stamp->cur_pos += len;//buf 结束位置

	if(g_download_stamp->fream_len == 0){
		get_stream_head_info(buf,len);
		return;
	}

	if(g_download_stamp->cur_pos > g_download_stamp->fream_len){
		if(g_download_stamp->cur_pos -  g_download_stamp->fream_len >= sizeof(g_stream_head)){
			g_download_stamp->offset = len - (g_download_stamp->cur_pos - g_download_stamp->fream_len);
			get_stream_head_info(buf,len);
		}else{
			//			g_timestamp_need_copy = true;
			LOGE("g_timestamp_need_copy = true");
		}
	}
}


void calc_udp_timestamp(const char* buf,int len){//case 会丢帧 只能在 当前buf中找head
	if(g_download_mgr==NULL) return;
	//	if(!g_download_mgr->bCalc_timestamp) return;


	if (g_download_mgr->jvm->AttachCurrentThread(&g_download_mgr->env2,NULL) != JNI_OK) {
		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
		return;
	}
	for(int i=0;i<len-sizeof(g_stream_head);i++){
		memcpy(&g_stream_head,buf+i,sizeof(g_stream_head));
		if(g_stream_head.tag == HW_MEDIA_TAG){
			//			LOGI("type=%ld len=%ld time=%lld",g_stream_head.type,g_stream_head.len,g_stream_head.time_stamp);
			if(g_download_mgr->bfirst_timestamp){
				g_download_mgr->bfirst_timestamp = false;
				g_download_mgr->env2->SetLongField(g_download_mgr->obj,g_download_mgr->first_timesteamp,g_stream_head.time_stamp);
			}


			if(g_download_mgr!=NULL){
				g_download_mgr->timestemp = g_stream_head.time_stamp;
				g_download_mgr->frame_len = g_stream_head.len;
			}
			break;
		}
	}
	if (g_download_mgr->jvm->DetachCurrentThread() != JNI_OK) {
		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	}
	//	if(g_download_mgr!=NULL){
	//		g_download_mgr->bCalc_timestamp = false;
	//	}
}

/**
 *
 */
void calc_first_frame_len(const char* buf,int len){
	if(g_download_mgr==NULL) return;
	if(g_download_stamp==NULL)return;
	if(!g_download_stamp->bDoOnce){
		g_download_stamp->bDoOnce = true;
		memset(&g_stream_head,0,sizeof(g_stream_head));
		memcpy(&g_stream_head,buf,sizeof(g_stream_head));
		LOGI("do once type = %ld len=%ld time=%lld",g_stream_head.type,g_stream_head.len,g_stream_head.time_stamp);
		g_download_mgr->frame_len = g_stream_head.len;

	}

}

void on_udp_download_stream_fun(UDP_FILE_STREAM_HANDLE handle,const char* buf,int len,long userdata){
	if(g_download_mgr==NULL) return;
	if(g_download_mgr->bStop){
		LOGE("download bStop");
		return;
	}
	g_download_mgr->stream_len += len;//获取下载了的文件长度
	//	LOGI("stream len=%ld",g_download_mgr->stream_len);
#if 0
	int ret = hwplay_input_data(g_handle_mgr->play_handle, buf ,len);
#else
	//	int ret = hwplay_input_data(g_handle_mgr->play_handle, buf ,len);
	//	LOGI("len = %d",len);
	//	calc_timestemp(buf,len);
	calc_udp_timestamp(buf,len);
	do_download(buf,len);
#endif
}


PLAY_HANDLE do_play_preview(int slot,int is_sub,int connect_mode){
	RECT area ;
	HW_MEDIAINFO media_head;
	memset(&media_head,0,sizeof(media_head));
	g_handle_mgr->live_stream_handle = hwnet_get_live_stream(g_handle_mgr->user_handle,slot,is_sub,connect_mode,on_live_stream_fun,0);
	hwnet_get_live_stream_head(g_handle_mgr->live_stream_handle,(char*)&media_head,1024,&g_stream_mgr->media_head_len);
	PLAY_HANDLE  ph = hwplay_open_stream((char*)&media_head,sizeof(media_head),1024*1024,0,area);
	hwplay_open_sound(ph);
	hwplay_set_max_framenum_in_buf(ph,5);
	hwplay_register_source_data_callback(ph,on_source_callback,0);
	hwplay_play(ph);
	return ph;
}







PLAY_HANDLE do_play_playback(int slot,int stream,rec_file_t *file_info){

	if(JNI_TEST){
		//		FILE_STREAM_HANDLE hwnet_get_file_stream(USER_HANDLE handle,int slot,SYSTEMTIME beg,SYSTEMTIME end,file_stream_fun* fun,long userdata,file_stream_t* file_info);
		file_stream_t info;
		memset(&info,0,sizeof(info));
		LOGI("get file stream");
		LOGI("year=%d month=%d day=%d hour=%d minute=%d second=%d",file_info->beg.wYear,file_info->beg.wMonth,file_info->beg.wDay,file_info->beg.wHour,file_info->beg.wMinute,file_info->beg.wSecond );
		LOGI("year=%d month=%d day=%d hour=%d minute=%d second=%d",file_info->end.wYear,file_info->end.wMonth,file_info->end.wDay,file_info->end.wHour,file_info->end.wMinute,file_info->end.wSecond );
		LOGI("slot=%d user_handle=%d",slot,g_handle_mgr->user_handle);
		//		g_handle_mgr->udp_file_handle
		//		FILE_STREAM_HANDLE handle = hwnet_get_file_stream_ex2(g_handle_mgr->user_handle,slot,1,file_info->beg,file_info->end,0,0,on_file_stream_fun,0,&info);
		g_handle_mgr->udp_file_handle = hwnet_get_file_stream(g_handle_mgr->user_handle, slot,file_info->beg,file_info->end,on_file_stream_fun,0,&info);
	}else{
		LOGI("udp file stream   user_handler=%d",g_handle_mgr->user_handle);

		/*
		 *@param stream : 0 按帧率发送  1全速发送
		 */
		LOGE("slot = %d stream=%d",slot,stream);//0 1
		g_handle_mgr->udp_file_handle = hwnet_get_udp_file_stream(g_handle_mgr->user_handle,slot,0,file_info,on_udp_file_stream_fun,0);
	}


	//test FIXME
	NetRecFileItem* recFile;
	//	memset(&recFile,0,sizeof(NetRecFileItem));
	recFile = (NetRecFileItem*)file_info;
	LOGI("fileno = %d   fileno2=%d",recFile->fileno,recFile->fileno2);


	if(g_handle_mgr->udp_file_handle < 0)
	{
		LOGE("get udp file stream failed");
		//		LOGE("fileNo=%d  y=%d m=%d d=%d   ")




		//		recFile.fileno = fileNo;
		//			recFile.created.wYear = begYear;
		//			recFile.created.wMonth = begMonth;
		//			recFile.created.wDay = begDay;
		//			recFile.created.wHour = begHour;
		//			recFile.created.wMinute = begMinute;
		//			recFile.created.wSecond = begSecond;
		//			recFile.total_seconds = totalSeconds;
		//			recFile.total_frames = totalFrames;
		//			recFile.vod_seconds = offsetSeconds;
		//			recFile.fileno2 = fileNo;





		return -1;
	}

	RECT area ;
	HW_MEDIAINFO media_head;
	memset(&media_head,0,sizeof(media_head));
	media_head.media_fourcc = HW_MEDIA_TAG;
	media_head.vdec_code = VDEC_H264;
	media_head.adec_code = ADEC_ADPCM_WAV;
	media_head.au_bits = 16;
	media_head.au_sample = 8;
	media_head.au_channel = 1;

	PLAY_HANDLE  ph = hwplay_open_stream((char*)&media_head,sizeof(media_head),1024*1024,1,area);
	if(ph == -1){
		LOGE("ph get error");
		return -1;
	}
	hwplay_open_sound(ph);
	hwplay_set_max_framenum_in_buf(ph,25);
	hwplay_register_source_data_callback(ph,on_source_callback,0);

	//	hwplay_register_yuv_callback_ex(ph,on_yuv_callback_ex,0);
	//	hwplay_register_audio_callback(ph,on_audio_callback,0);


	hwplay_play(ph);
	hwplay_set_speed(ph,1);
	//	if(g_jpg_mgr){
	//		if(g_jpg_mgr->bNeedFistJpg==1){
	//			savePlayBackJpg(ph,file_info);
	//			g_jpg_mgr->bNeedFistJpg=0;
	//		}
	//	}


	return ph;
}

PLAY_HANDLE do_play_local(const char * file_name){
	PLAY_HANDLE  ph = hwplay_open_local_file(file_name);
	hwplay_open_sound(ph);
	//	hwplay_set_max_framenum_in_buf(ph,25);
	hwplay_set_speed(ph,1.0);
	hwplay_register_source_data_callback(ph,on_source_callback,0);
	hwplay_play(ph);

	return ph;
}




//jni interface
JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_cameraInit
(JNIEnv *, jclass){
	if(g_handle_mgr == NULL){
		g_handle_mgr = (handle_manager_t*)malloc(sizeof(handle_manager_t));
		memset(g_handle_mgr,0,sizeof(handle_manager_t));
	}
	if(g_stream_mgr == NULL){
		g_stream_mgr = (stream_manager_t*)malloc(sizeof(stream_manager_t));
		memset(g_stream_mgr,0,sizeof(stream_manager_t));
	}

	hwplay_init(1,0,0);
	hwnet_init(5888);
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_cameraDeinit
(JNIEnv *, jclass){
	if(g_handle_mgr == NULL)return;
	if(g_stream_mgr == NULL)return;

	hwnet_release();
	hwplay_release();
	if(g_stream_mgr !=NULL){
		free(g_stream_mgr);
		g_stream_mgr = NULL;
	}
	if(g_handle_mgr!=NULL){
		free(g_handle_mgr);
		g_handle_mgr = NULL;
	}
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_cameraLogin
(JNIEnv *env, jclass, jstring j_ip){
	if(g_handle_mgr == NULL) return -1;

	const char *ip = env->GetStringUTFChars(j_ip,NULL);
	g_handle_mgr->user_handle = hwnet_login(ip,5198,"admin","12345");
	env->ReleaseStringUTFChars(j_ip,ip);
	return g_handle_mgr->user_handle;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_cameraLogout
(JNIEnv *, jclass){
	if(g_handle_mgr == NULL)return 0;
	int ret =  hwnet_logout(g_handle_mgr->user_handle);
	if(ret == 1){
		g_handle_mgr->user_handle = -1;
	}

	return ret;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_cameraPlay
(JNIEnv *env, jclass, jint is_playback, jint slot, jint is_sub, jint connect_mode, jobject j_obj){
	if(g_handle_mgr == NULL)return 0;
	if(g_stream_mgr == NULL)return 0;
	g_stream_mgr->play_mode = is_playback;
	g_stream_mgr->stream_len = 0;
	if(is_playback==0){//预览
		g_handle_mgr->play_handle = do_play_preview(slot,is_sub,connect_mode);
	}else{//回放
		jclass objectClass = env->GetObjectClass(j_obj);
		jfieldID begYearFieldID = env->GetFieldID(objectClass,"begYear","S");
		jfieldID begMonthFieldID = env->GetFieldID(objectClass,"begMonth","S");
		jfieldID begDayFieldID = env->GetFieldID(objectClass,"begDay","S");
		jfieldID begHourFieldID = env->GetFieldID(objectClass,"begHour","S");
		jfieldID begMinuteFieldID = env->GetFieldID(objectClass,"begMinute","S");
		jfieldID begSecondFieldID = env->GetFieldID(objectClass,"begSecond","S");
		jfieldID fileNoFieldID = env->GetFieldID(objectClass,"fileNo","I");
		jfieldID totalSecondsFieldID = env->GetFieldID(objectClass,"totalSeconds","I");
		jfieldID totalFramesFieldID = env->GetFieldID(objectClass,"totalFrames","I");
		jfieldID offsetSecondsFieldID = env->GetFieldID(objectClass,"offsetSeconds","I");

		jshort begYear = env->GetShortField( j_obj , begYearFieldID);
		jshort begMonth = env->GetShortField( j_obj , begMonthFieldID);
		jshort begDay = env->GetShortField(j_obj , begDayFieldID);
		jshort begHour = env->GetShortField( j_obj , begHourFieldID);
		jshort begMinute = env->GetShortField(j_obj , begMinuteFieldID);
		jshort begSecond = env->GetShortField(j_obj , begSecondFieldID);
		jint fileNo = env->GetShortField(j_obj , fileNoFieldID);
		jint totalSeconds = env->GetShortField(j_obj , totalSecondsFieldID);
		jint totalFrames = env->GetShortField(j_obj , totalFramesFieldID);
		jint offsetSeconds = env->GetShortField(j_obj , offsetSecondsFieldID);

		NetRecFileItem recFile;
		memset(&recFile,0,sizeof(NetRecFileItem));
		recFile.fileno = fileNo;
		recFile.created.wYear = begYear;
		recFile.created.wMonth = begMonth;
		recFile.created.wDay = begDay;
		recFile.created.wHour = begHour;
		recFile.created.wMinute = begMinute;
		recFile.created.wSecond = begSecond;
		recFile.total_seconds = totalSeconds;
		recFile.total_frames = totalFrames;
		recFile.vod_seconds = offsetSeconds;
		recFile.fileno2 = fileNo;
		LOGI("fileno=%d y=%d m=%d d=%d h=%d min=%d sec=%d totsec=%d totfram=%d vod=%d fileno2=%d",
				recFile.fileno,	recFile.created.wYear,recFile.created.wMonth,recFile.created.wDay,
				recFile.created.wHour,recFile.created.wMinute,recFile.created.wSecond,recFile.total_seconds,
				recFile.total_frames,recFile.vod_seconds,recFile.fileno2);
		rec_file_t *file_info = (rec_file_t *)&recFile;

		if(JNI_TEST){
			jfieldID id = env->GetFieldID(objectClass,"endYear","S");
			jshort 	val = env->GetShortField( j_obj , id);
			file_info->end.wYear = val;
			id= env->GetFieldID(objectClass,"endMonth","S");
			val = env->GetShortField( j_obj , id);
			file_info->end.wMonth = val;
			id = env->GetFieldID(objectClass,"endDay","S");
			val = env->GetShortField( j_obj , id);
			file_info->end.wDay = val;
			id = env->GetFieldID(objectClass,"endHour","S");
			val = env->GetShortField( j_obj , id);
			file_info->end.wHour = val;
			id = env->GetFieldID(objectClass,"endMinute","S");
			val = env->GetShortField( j_obj , id);
			file_info->end.wMinute = val;
			id = env->GetFieldID(objectClass,"endSecond","S");
			val = env->GetShortField( j_obj , id);
			file_info->end.wSecond = val;
		}


		g_handle_mgr->play_handle = do_play_playback(slot,is_sub,file_info);
	}
	return g_handle_mgr->play_handle;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_cameraLoaclPlay
(JNIEnv *env, jclass,jstring file_name){
	if(g_handle_mgr==NULL)return 0;
	if(g_stream_mgr==NULL)return 0;
	g_stream_mgr->play_mode = 2;

	const char* _file_name = env->GetStringUTFChars(file_name,NULL);

	g_handle_mgr->play_handle =   do_play_local(_file_name);
	env->ReleaseStringUTFChars(file_name,_file_name);
	return g_handle_mgr->play_handle;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_cameraGetPos
(JNIEnv *, jclass){
	if(g_handle_mgr==NULL)return 0;
	if(g_handle_mgr->play_handle==-1)return 0;
	int pos=0;
	int ret = -1;
	ret = hwplay_get_pos(g_handle_mgr->play_handle,&pos);
	if(ret == 0){
		LOGE("hwplay_get_pos error");
		return -1;
	}
	return pos;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_cameraSetPos
(JNIEnv *, jclass, jint pos){
	if(g_handle_mgr==NULL)return 0;
	if(g_handle_mgr->play_handle==-1)return 0;
	return hwplay_set_pos(g_handle_mgr->play_handle,pos);
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_cameraGetTotalMSec
(JNIEnv *, jclass){
	if(g_handle_mgr==NULL)return 0;
	if(g_handle_mgr->play_handle==-1)return 0;
	int totalMSec = 0;
	int ret = 0;
	ret = hwplay_get_total_msec(g_handle_mgr->play_handle,&totalMSec);
	return totalMSec;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_cameraGetTotalFrame
(JNIEnv *, jclass){
	if(g_handle_mgr==NULL)return 0;
	if(g_handle_mgr->play_handle==-1)return 0;
	int totalFrame=0;
	int ret = 0;
	ret = hwplay_get_total_frame(g_handle_mgr->play_handle,&totalFrame);
	return totalFrame;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_cameraGetPlayedMsc
(JNIEnv *, jclass){
	if(g_handle_mgr==NULL)return 0;
	if(g_handle_mgr->play_handle==-1)return 0;
	int playedMSec=0;
	hwplay_get_played_msec(g_handle_mgr->play_handle,&playedMSec);
	return playedMSec;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_cameraGetCurFrame
(JNIEnv *, jclass){
	if(g_handle_mgr==NULL)return 0;
	if(g_handle_mgr->play_handle==-1)return 0;
	int curFrame=0;
	hwplay_get_current_frame(g_handle_mgr->play_handle,&curFrame);
	return curFrame;

}


JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_cameraSetFrame
(JNIEnv *, jclass, jint frame){
	if(g_handle_mgr==NULL)return 0;
	if(g_handle_mgr->play_handle==-1)return 0;
	return hwplay_set_frame(g_handle_mgr->play_handle,frame);
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_cameraPause
(JNIEnv *, jclass, jint bPause){
	if(g_handle_mgr==NULL)return 0;
	if(g_handle_mgr->play_handle==-1)return 0;
	return hwplay_pause(g_handle_mgr->play_handle,bPause);
}


JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_cameraStop
(JNIEnv *, jclass){
	if(g_handle_mgr == NULL)return 0;
	if(g_stream_mgr == NULL)return 0;
	int ret = 0;
	LOGI("g_stream_mgr play mode=%d",g_stream_mgr->play_mode);
	if(g_stream_mgr->play_mode == 0){
		ret = hwnet_close_live_stream(g_handle_mgr->live_stream_handle);
	}else if(g_stream_mgr->play_mode==1){
		//ret = hwnet_close_file_stream(res-> file_stream_handle);
		LOGI("close udp file stream");
		if(JNI_TEST){
			ret = hwnet_close_file_stream(g_handle_mgr->udp_file_handle);
			LOGI("ret = %d",ret);
		}else{
			ret = hwnet_close_udp_file_stream(g_handle_mgr->udp_file_handle);
		}

	}

	ret &= hwplay_stop(g_handle_mgr->play_handle);
	LOGI("stop   ret2=%d",ret);
	if(ret){

		g_handle_mgr->play_handle = -1;
	}
	return ret;
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_YUVInit
(JNIEnv *env, jclass){
	if(g_yuv_info == NULL){
		g_yuv_info = (yv12_info_t*)malloc(sizeof(yv12_info_t));
		memset(g_yuv_info,0,sizeof(yv12_info_t));
	}
	if(g_yuv_display == NULL){
		g_yuv_display = (YV12_display_t*)malloc(sizeof(YV12_display_t));
		memset(g_yuv_display,0,sizeof(YV12_display_t));
	}
	env->GetJavaVM(&g_yuv_display->jvm);
	//FIXME  now obj=JniYV12Util should be YV12Renderer
	pthread_mutex_init(&g_yuv_display->lock,NULL);
	g_yuv_display->width  = 352;
	g_yuv_display->height = 288;
	g_yuv_display->y = (char*)malloc(g_yuv_display->width*g_yuv_display->height);
	g_yuv_display->u = (char*)malloc(g_yuv_display->width*g_yuv_display->height/4);
	g_yuv_display->v = (char*)malloc(g_yuv_display->width*g_yuv_display->height/4);
	memset(g_yuv_display->y,0,g_yuv_display->width*g_yuv_display->height);//4:2:2 black
	memset(g_yuv_display->u,128,g_yuv_display->width*g_yuv_display->height/4);
	memset(g_yuv_display->v,128,g_yuv_display->width*g_yuv_display->height/4);
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_YUVDeinit
(JNIEnv *env, jclass){
	if(g_yuv_info!=NULL){
		free(g_yuv_info);
		g_yuv_info = NULL;
	}
	if(g_yuv_display!=NULL){
		g_yuv_display->method_ready = 0;
		if(g_yuv_display->y!=NULL){
			free(g_yuv_display->y);
			g_yuv_display->y = NULL;
		}
		if(g_yuv_display->u!=NULL){
			free(g_yuv_display->u);
			g_yuv_display->u = NULL;
		}
		if(g_yuv_display->v!=NULL ){
			free(g_yuv_display->v);
			g_yuv_display->v = NULL;
		}
		pthread_mutex_destroy(&g_yuv_display->lock);
		env->DeleteGlobalRef(g_yuv_display->callback_obj);
		free(g_yuv_display);
		g_yuv_display = NULL;
	}
}




JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_YUVSetCallbackObject
(JNIEnv *env, jclass, jobject callbackObject, jint flag){
	if(g_yuv_info==NULL)return;
	switch (flag) {
	case 0:
		g_yuv_display->callback_obj = env->NewGlobalRef(callbackObject);
		break;
	default:
		break;
	}
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_YUVSetCallbackMethodName
(JNIEnv *env, jclass, jstring method_name, jint flag){
	if(g_yuv_info==NULL)return;
	const char * _method_name= env->GetStringUTFChars(method_name,NULL);
	switch (flag) {
	case 0:
		strcpy(g_yuv_info->set_time_method_name,_method_name);
		break;
	case 1:
		strcpy(g_yuv_info->request_method_name,_method_name);
		break;
	default:
		break;
	}
	env->ReleaseStringUTFChars(method_name,_method_name);
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_YUVLock
(JNIEnv *, jclass){
	if(g_yuv_display == NULL){
		return;
	}
	pthread_mutex_lock(&g_yuv_display->lock);
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_YUVUnlock
(JNIEnv *, jclass){
	if(g_yuv_display == NULL){
		return;
	}
	pthread_mutex_unlock(&g_yuv_display->lock);
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_YUVSetEnable
(JNIEnv *, jclass){
	g_yuv_display->enable = 1;
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_YUVRenderY
(JNIEnv *, jclass){
	if (g_yuv_display->y == NULL) {
		char value[4] = {0,0,0,0};
		glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,2,2,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,value);
	}
	else {
		//LOGI("render y");
		glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,g_yuv_display->width,g_yuv_display->height,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,g_yuv_display->y);
	}
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_YUVRenderU
(JNIEnv *, jclass){
	if (g_yuv_display->u == NULL) {
		char value[] = {128};
		glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,1,1,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,value);
	}
	else {
		glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,g_yuv_display->width/2,g_yuv_display->height/2,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,g_yuv_display->u);
	}
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_YUVRenderV
(JNIEnv *, jclass){
	if (g_yuv_display->v == NULL) {
		char value[] = {128};
		glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,1,1,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,value);
	}
	else {
		glTexImage2D(GL_TEXTURE_2D,0,GL_LUMINANCE,g_yuv_display->width/2,g_yuv_display->height/2,0,GL_LUMINANCE,GL_UNSIGNED_BYTE,g_yuv_display->v);
	}
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_AudioInit
(JNIEnv *env, jclass){
	if(g_audio_mgr == NULL){
		g_audio_mgr = (audio_manager_t *)malloc(sizeof(audio_manager_t));
		memset(g_audio_mgr,0,sizeof(audio_manager_t));
		g_audio_mgr->method_ready 	= 0;
		g_audio_mgr->stop 			= 0;
		env->GetJavaVM(&g_audio_mgr->jvm);
	}
	if(g_audio_info == NULL){
		g_audio_info = (audio_info_t*)malloc(sizeof(audio_info_t));
		memset(g_audio_info,0,sizeof(audio_info_t));
	}
	LOGI("audio init ok");
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_AudioDeinit
(JNIEnv *env, jclass){
	if(g_audio_mgr !=NULL){
		env->DeleteGlobalRef(g_audio_mgr->callback_obj);
		env->DeleteGlobalRef(g_audio_mgr->data_array);
		free(g_audio_mgr);
		g_audio_mgr = NULL;
	}
	if(g_audio_info != NULL){
		free(g_audio_info);
		g_audio_info = NULL;
	}
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_AudioSetCallbackObject
(JNIEnv *env, jclass, jobject callbackObj, jint flag){
	if(g_audio_mgr == NULL){
		LOGI("%d  g_audio_mgr = NULL",__LINE__);
		return;
	}
	switch (flag) {
	case 0:
		g_audio_mgr->callback_obj = env->NewGlobalRef(callbackObj);
		break;
	default:
		break;
	}
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_AudioSetCallbackFieldName
(JNIEnv *env, jclass, jstring field_name, jint flag){
	if(g_audio_mgr == NULL){
		LOGI("%d  g_audio_mgr = NULL",__LINE__);
		return;
	}

	jclass clz = env->GetObjectClass(g_audio_mgr->callback_obj);
	const char * _field_name= env->GetStringUTFChars(field_name,NULL);
	jfieldID id = NULL;
	switch (flag) {
	case 0://mAudioDataLength
	{
		id = env->GetFieldID(clz,_field_name,"I");
		g_audio_mgr->fieldID_data_length = id;
	}
	break;
	case 1://
	{
		id = env->GetFieldID(clz,_field_name,"[B");
		jbyteArray arr = (jbyteArray)env->GetObjectField(g_audio_mgr->callback_obj,id);
		g_audio_mgr->data_array_len = env->GetArrayLength(arr);
		g_audio_mgr->data_array = (jbyteArray)env->NewGlobalRef(arr);
	}
	break;
	default:
		break;
	}
	env->ReleaseStringUTFChars(field_name,_field_name);
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_AudioSetCallbackMethodName
(JNIEnv *env, jclass, jstring method_name, jint flag){
	if(g_audio_info == NULL)return;
	const char * _method_name= env->GetStringUTFChars(method_name,NULL);
	switch (flag) {
	case 0:
	{
		strcpy(g_audio_info->callback_method_name,_method_name);
	}
	break;
	default:
		break;
	}
	env->ReleaseStringUTFChars(method_name,_method_name);
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_AudioStop
(JNIEnv *, jclass){
	if(g_audio_mgr == NULL) return;
	g_audio_mgr->stop = 1;
}


JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_getFileList
(JNIEnv *env, jclass cls, jint user_handle, jint slot, jobject begTime, jobject endTime){
	SYSTEMTIME beg ;
	memset(&beg,0,sizeof(SYSTEMTIME));
	SYSTEMTIME end ;
	memset(&end,0,sizeof(SYSTEMTIME));

	jclass clzBeg = env->GetObjectClass(begTime);
	if(clzBeg==NULL){
		LOGE("clzBeg == NULL");
		return -1;
	}

	jclass clzEnd = env->GetObjectClass(endTime);
	if(endTime == NULL) return -1;

	jfieldID id  = env->GetFieldID(clzBeg,"wYear","S");
	jshort   val = env->GetShortField(begTime,id);
	beg.wYear = val;
	id = env->GetFieldID(clzBeg,"wMonth","S");
	val = env->GetShortField(begTime,id);
	beg.wMonth = val;
	id = env->GetFieldID(clzBeg,"wDayofWeek","S");
	val = env->GetShortField(begTime,id);
	beg.wDayofWeek = val;
	id = env->GetFieldID(clzBeg,"wDay","S");
	val = env->GetShortField(begTime,id);
	beg.wDay = val;
	id = env->GetFieldID(clzBeg,"wHour","S");
	val = env->GetShortField(begTime,id);
	beg.wHour = val;
	id = env->GetFieldID(clzBeg,"wMinute","S");
	val = env->GetShortField(begTime,id);
	beg.wMinute = val;
	id = env->GetFieldID(clzBeg,"wSecond","S");
	val = env->GetShortField(begTime,id);
	beg.wSecond = val;
	id = env->GetFieldID(clzBeg,"wMilliseconds","S");
	val = env->GetShortField(begTime,id);
	beg.wMilliseconds = val;

	id  = env->GetFieldID(clzEnd,"wYear","S");
	val = env->GetShortField(endTime,id);
	end.wYear = val;
	id = env->GetFieldID(clzEnd,"wMonth","S");
	val = env->GetShortField(endTime,id);
	end.wMonth = val;
	id = env->GetFieldID(clzEnd,"wDayofWeek","S");
	val = env->GetShortField(endTime,id);
	end.wDayofWeek = val;
	id = env->GetFieldID(clzEnd,"wDay","S");
	val = env->GetShortField(endTime,id);
	end.wDay = val;
	id = env->GetFieldID(clzEnd,"wHour","S");
	val = env->GetShortField(endTime,id);
	end.wHour = val;
	id = env->GetFieldID(clzEnd,"wMinute","S");
	val = env->GetShortField(endTime,id);
	end.wMinute = val;
	id = env->GetFieldID(clzEnd,"wSecond","S");
	val = env->GetShortField(endTime,id);
	end.wSecond = val;
	id = env->GetFieldID(clzEnd,"wMilliseconds","S");
	val = env->GetShortField(endTime,id);
	end.wMilliseconds = val;

	return  hwnet_get_file_list(g_handle_mgr->user_handle, slot, beg, end,0);
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_getListByPage
(JNIEnv *env, jclass cls, jint user_handle, jint slot, jint stream, jobject replay, jint type, jint order_by_time, jobject page_info){
	SYSTEMTIME beg ;
	memset(&beg,0,sizeof(SYSTEMTIME));
	SYSTEMTIME end ;
	memset(&end,0,sizeof(SYSTEMTIME));

	jclass obj = env->GetObjectClass(replay);
	jfieldID id = env->GetFieldID(obj,"begYear","S");
	jshort val = env->GetShortField(replay,id);
	beg.wYear = val;
	id = env->GetFieldID(obj,"begMonth","S");
	val = env->GetShortField(replay,id);
	beg.wMonth = val;
	id = env->GetFieldID(obj,"begMonth","S");
	val = env->GetShortField(replay,id);
	beg.wMonth = val;
	id = env->GetFieldID(obj,"begDay","S");
	val = env->GetShortField(replay,id);
	beg.wDay = val;
	id = env->GetFieldID(obj,"begHour","S");
	val = env->GetShortField(replay,id);
	beg.wHour = val;
	id = env->GetFieldID(obj,"begMinute","S");
	val = env->GetShortField(replay,id);
	beg.wMinute = val;
	id = env->GetFieldID(obj,"begSecond","S");
	val = env->GetShortField(replay,id);
	beg.wSecond = val;



	id = env->GetFieldID(obj,"endYear","S");
	val = env->GetShortField(replay,id);
	end.wYear = val;
	id = env->GetFieldID(obj,"endMonth","S");
	val = env->GetShortField(replay,id);
	end.wMonth = val;
	id = env->GetFieldID(obj,"endMonth","S");
	val = env->GetShortField(replay,id);
	end.wMonth = val;
	id = env->GetFieldID(obj,"endDay","S");
	val = env->GetShortField(replay,id);
	end.wDay = val;
	id = env->GetFieldID(obj,"endHour","S");
	val = env->GetShortField(replay,id);
	end.wHour = val;
	id = env->GetFieldID(obj,"endMinute","S");
	val = env->GetShortField(replay,id);
	end.wMinute = val;
	id = env->GetFieldID(obj,"endSecond","S");
	val = env->GetShortField(replay,id);
	end.wSecond = val;







	jclass objectClass = env->GetObjectClass(page_info);
	if(objectClass == NULL)
	{
		LOGE("GetObjectClass failed \n");
		return -1;
	}
	jfieldID pageSizeFieldID = env->GetFieldID(objectClass,"page_size","I");
	jfieldID pageNoFieldID = env->GetFieldID(objectClass,"page_no","I");

	jint page_size = env->GetIntField( page_info , pageSizeFieldID);
	jint page_no = env->GetIntField( page_info , pageNoFieldID);
	LOGE("jni:%d   page_size:%d ,page_no:%d",__LINE__,page_size,page_no);
	Pagination page;
	page.page_size = page_size;
	page.page_no = page_no;
	int file_list_handle =  hwnet_get_file_list_by_page(g_handle_mgr->user_handle,slot,stream, beg, end, type, order_by_time,0,&page);
	if(file_list_handle == -1){
		LOGE("hwnet_get_file_list_by_page failed \n");
		return -1;
	}


	//获取类中每一个变量的定义
	jfieldID totalSizeFieldID = env->GetFieldID(objectClass,"total_size","I");
	jfieldID curSizeFieldID = env->GetFieldID(objectClass,"cur_size","I");
	jfieldID pageCountFieldID = env->GetFieldID(objectClass,"page_count","I");

	env->SetIntField(page_info,totalSizeFieldID,page.total_size);
	env->SetIntField( page_info,curSizeFieldID,page.cur_size);
	env->SetIntField( page_info,pageCountFieldID,page.page_count);
	LOGE("page.total_size:%d ,page.cur_size:%d ,page.page_count:%d",page.total_size,page.cur_size,page.page_count);
	return file_list_handle;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_getFileListCount
(JNIEnv *, jclass, jint fh){
	int count = 0;
	int ret =  hwnet_get_file_count(fh,& count);
	if(ret==0){
		return 0;
	}
	return count;
}


JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_closeFileList
(JNIEnv *, jclass, jint file_list_handle){
	return hwnet_close_file_list(file_list_handle);
}

JNIEXPORT jobjectArray JNICALL Java_com_howell_jni_CamJniUtil_getReplayList
(JNIEnv *env, jclass, jint file_list_handle, jint count){
	int i ,type;
	//SYSTEMTIME beg ,end;
	jobjectArray MXArray = NULL;       // jobjectArray 为指针类型
	jclass clsMX = NULL;         // jclass 为指针类型
	jobject obj;
	//知道要返回的class.
	clsMX = env->FindClass("com/howell/ecamerajing/bean/NetRectFileItem");

	//创建一个MXAray的数组对象.
	MXArray = env->NewObjectArray(count, clsMX, NULL);
	//获取类中每一个变量的定义
	jfieldID fileNo = env->GetFieldID(clsMX, "fileNo", "I");

	jfieldID begYear = env->GetFieldID(clsMX, "begYear", "S");
	jfieldID begMonth = env->GetFieldID(clsMX, "begMonth", "S");
	jfieldID begDay = env->GetFieldID(clsMX, "begDay", "S");
	jfieldID begHour = env->GetFieldID(clsMX, "begHour", "S");
	jfieldID begMinute = env->GetFieldID(clsMX, "begMinute", "S");
	jfieldID begSecond = env->GetFieldID(clsMX, "begSecond", "S");
	jfieldID endYear = env->GetFieldID(clsMX,"endYear","S");
	jfieldID endMonth = env->GetFieldID(clsMX, "endMonth", "S");
	jfieldID endDay = env->GetFieldID(clsMX, "endDay", "S");
	jfieldID endHour = env->GetFieldID(clsMX, "endHour", "S");
	jfieldID endMinute = env->GetFieldID(clsMX, "endMinute", "S");
	jfieldID endSecond = env->GetFieldID(clsMX, "endSecond", "S");


	jfieldID totalSeconds = env->GetFieldID(clsMX, "totalSeconds", "I");
	jfieldID totalFrames = env->GetFieldID(clsMX, "totalFrames", "I");
	jfieldID offsetSeconds = env->GetFieldID(clsMX, "offsetSeconds", "I");



	//得到这个类的构造方法id.  //得到类的默认构造方法的id.都这样写.
	jmethodID consID = env->GetMethodID(clsMX, "<init>", "()V");
	//int j = 0;
	for (i = 0 ; i < count; i++)
	{
		tRecFile old ;
		memset(&old,0,sizeof(tRecFile));
		NetRecFileItem recFile ;
		memset(&recFile,0,sizeof(NetRecFileItem));
		int ret = hwnet_get_file_detail(file_list_handle,i,&old.beg,&old.end,&old.type);//1成功 0失败
		memcpy(&recFile,&old,sizeof(recFile));
		LOGE("jni fileno2:%d",recFile.fileno2);
		obj = env->NewObject(clsMX, consID);
		env->SetIntField(obj, fileNo, recFile.fileno2);
		env->SetShortField(obj, begYear, recFile.created.wYear);
		env->SetShortField(obj, begMonth, recFile.created.wMonth);
		env->SetShortField(obj, begDay, recFile.created.wDay);
		env->SetShortField(obj, begHour, recFile.created.wHour);
		env->SetShortField(obj, begMinute, recFile.created.wMinute);
		env->SetShortField(obj, begSecond, recFile.created.wSecond);

		env->SetShortField(obj,endYear,old.end.wYear);
		env->SetShortField(obj,endMonth,old.end.wMonth);
		env->SetShortField(obj,endDay,old.end.wDay);
		env->SetShortField(obj,endHour,old.end.wHour);
		env->SetShortField(obj,endMinute,old.end.wMinute);
		env->SetShortField(obj,endSecond,old.end.wSecond);

		env->SetIntField(obj, totalSeconds, recFile.total_seconds);
		env->SetIntField(obj, totalFrames, recFile.total_frames);
		env->SetIntField(obj, offsetSeconds, recFile.vod_seconds);
		env->SetObjectArrayElement(MXArray, i, obj);
		//j++;
		LOGI("y=%d m=%d d=%d",old.beg.wYear,old.beg.wMonth,old.beg.wDay);
		LOGI("year=%d month=%d day=%d",recFile.created.wYear,recFile.created.wMonth,recFile.created.wDay);
	}
	return MXArray;
}

JNIEXPORT jobject JNICALL Java_com_howell_jni_CamJniUtil_nativeGetNetRfidInfo
(JNIEnv *env , jclass){
	if(g_handle_mgr == NULL)return NULL;
	net_rfid_info_t rfid_info;
	memset(&rfid_info,0,sizeof(rfid_info));
	if(hwnet_ipc_get_rfid_info(g_handle_mgr->user_handle,&rfid_info))
	{
		LOGE("rfid:attenuation=%d\n",rfid_info.attenuation);
		jclass objectClass = env->FindClass( "com/howell/ecamerajing/bean/NetRfidInfo");
		jmethodID mid = env->GetMethodID(objectClass,"<init>","(IIIII)V");
		jobject obj = env->NewObject(objectClass,mid,rfid_info.id,rfid_info.attenuation
				,rfid_info.type,rfid_info.range_beg,rfid_info.range_end);
		return obj;
	}
	else
	{
		LOGE("get rfid info failed\n");
		return NULL;
	}
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_nativeSetNetRfidInfo
(JNIEnv *env, jclass , jobject obj){
	if(g_handle_mgr == NULL) return 0;
	jclass objectClass = env->GetObjectClass(obj);
	if(objectClass == NULL)
	{
		LOGE("GetObjectClass failed \n");
		return 0;
	}
	jfieldID idFieldID = env->GetFieldID(objectClass,"id","I");
	jfieldID attenuationFieldID = env->GetFieldID(objectClass,"attenuation","I");
	jfieldID typeFieldID = env->GetFieldID(objectClass,"type","I");
	jfieldID rangeBegFieldID = env->GetFieldID(objectClass,"rangeBeg","I");
	jfieldID rangeEndFieldID = env->GetFieldID(objectClass,"rangeEnd","I");

	jint id = env->GetIntField( obj , idFieldID);
	jint attenuation = env->GetIntField( obj , attenuationFieldID);
	jint type = env->GetIntField( obj , typeFieldID);
	jint rangeBeg = env->GetIntField( obj , rangeBegFieldID);
	jint rangeEnd = env->GetIntField( obj , rangeEndFieldID);

	net_rfid_info_t rfid_info;
	rfid_info.id = id;
	rfid_info.attenuation = attenuation;
	rfid_info.type = type;
	rfid_info.range_beg = rangeBeg;
	rfid_info.range_end = rangeEnd;

	return hwnet_ipc_set_rfid_info(g_handle_mgr->user_handle,&rfid_info);
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_jpgInit
(JNIEnv *env, jclass){
	if(g_jpg_mgr == NULL){
		g_jpg_mgr = (jpg_manager_t*)malloc(sizeof(jpg_manager_t));
		memset(g_jpg_mgr,0,sizeof(jpg_manager_t));
	}
	if(g_jpg_info == NULL){
		g_jpg_info = (jpg_info_t*)malloc(sizeof(jpg_info_t));
		memset(g_jpg_info,0,sizeof(jpg_info_t));
	}
	pthread_mutex_init(&g_jpg_mgr->lock,NULL);

	env->GetJavaVM(&g_jpg_mgr->jvm);
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_jpgDeinit
(JNIEnv *env, jclass){
	if(g_jpg_info != NULL){
		free(g_jpg_info);
		g_jpg_info = NULL;
	}
	if(g_jpg_mgr != NULL){
		env->DeleteGlobalRef(g_jpg_mgr->obj);
		env->DeleteGlobalRef(g_jpg_mgr->data_array);
		pthread_mutex_destroy(&g_jpg_mgr->lock);
		free(g_jpg_mgr);
		g_jpg_mgr = NULL;
	}
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_jpgSetCallbackObject
(JNIEnv *env, jclass, jobject callbackObj, jint flag){
	if(g_jpg_mgr == NULL) return ;
	switch (flag) {
	case 0:
	{
		g_jpg_mgr->obj = env->NewGlobalRef(callbackObj);
	}
	break;
	default:
		break;
	}
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_jpgSetCallbackFieldName
(JNIEnv *env, jclass, jstring field_name, jint flag){
	if(g_jpg_mgr == NULL)return;
	if(g_jpg_info == NULL)return;
	jclass clz = env->GetObjectClass(g_jpg_mgr->obj);
	const char * _field_name= env->GetStringUTFChars(field_name,NULL);
	jfieldID id = NULL;
	switch (flag) {
	case 0:
	{
		strcpy(g_jpg_info->callback_field_name,_field_name);
		//		id = env->GetFieldID(clz,_field_name,"[B");
		//		jbyteArray data_array_local_ref = (jbyteArray)env->GetObjectField(g_jpg_mgr->obj,id);
		//		g_jpg_mgr->data_array = (jbyteArray)env->NewGlobalRef( data_array_local_ref);
	}
	break;
	default:
		break;
	}

	env->ReleaseStringUTFChars(field_name,_field_name);
}


JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_jpgSetCallbackMethodName
(JNIEnv *env, jclass, jstring method_name, jint flag){
	if(g_jpg_info == NULL)return;
	const char * _method_name= env->GetStringUTFChars(method_name,NULL);
	switch (flag) {
	case 0:
		strcpy(g_jpg_info->callback_method_name,_method_name);
		break;
	case 1:
		strcpy(g_jpg_info->callback_create_buf_method_name,_method_name);
		break;
	default:
		break;
	}
	env->ReleaseStringUTFChars(method_name,_method_name);
}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_CamJniUtil_getPlayBackJPG
(JNIEnv *env, jclass, jint user_handle, jobject netRectFileItem){
	jclass clz= env->GetObjectClass(netRectFileItem);
	jfieldID fileNoID = env->GetFieldID(clz, "fileNo", "I");
	jfieldID begYearID = env->GetFieldID(clz, "begYear", "S");
	jfieldID begMonthID = env->GetFieldID(clz, "begMonth", "S");
	jfieldID begDayID = env->GetFieldID(clz, "begDay", "S");
	jfieldID begHourID = env->GetFieldID(clz, "begHour", "S");
	jfieldID begMinuteID = env->GetFieldID(clz, "begMinute", "S");
	jfieldID begSecondID = env->GetFieldID(clz, "begSecond", "S");

	jint fileNo = env->GetIntField(netRectFileItem, fileNoID);
	jshort begYear = env->GetShortField(netRectFileItem, begYearID);
	jshort begMonth = env->GetShortField(netRectFileItem, begMonthID);
	jshort begDay = env->GetShortField(netRectFileItem, begDayID);
	jshort begHour = env->GetShortField(netRectFileItem, begHourID);
	jshort begMinute = env->GetShortField(netRectFileItem, begMinuteID);
	jshort begSecond = env->GetShortField(netRectFileItem, begSecondID);
	LOGE("fileNo:%d",fileNo);
	LOGE("%d %d %d %d %d %d",begYear,begMonth,begDay,begHour,begMinute,begSecond);

	int buf_len = 128 * 1024;
	char* jpg_buf = (char *)malloc(buf_len);
	memset(jpg_buf,0, buf_len);
	int jpg_len = 0;
	net_capture_jpg_t req;
	memset(&req,0,sizeof(net_capture_jpg_t));
	req.slot = fileNo;
	int ret = hwnet_get_jpg_buf(user_handle,&req,jpg_buf, buf_len,&jpg_len);
	LOGI("ret:%d,jpg_len:%d",ret,jpg_len);
	if(ret == 0){
		LOGE("ret=%d   hwnet_get_jpg_buf error",ret);
		return false;
	}

	jclass cls;
	cls = env->GetObjectClass(g_jpg_mgr->obj);
	if (cls == NULL) {
		LOGE("FindClass() Error.....");
		return false;
		//goto error;
	}
	g_jpg_mgr->mid = env->GetMethodID( cls, g_jpg_info->callback_method_name, "(Ljava/lang/String;I)V");
	if (g_jpg_mgr->mid  == NULL) {
		LOGE("%d  GetMethodID() Error.....",__LINE__);
		return false;
		//goto error;
	}
	env->SetByteArrayRegion(g_jpg_mgr->data_array,0,jpg_len,(const jbyte*)jpg_buf);
	/* notify the JAVA */
	char* createTime = (char *)malloc(15);
	memset(createTime,0, 15);
	sprintf(createTime,"%04d%02d%02d%02d%02d%02d",begYear,begMonth,begDay,begHour,begMinute,begSecond);
	jstring string = env->NewStringUTF(createTime);
	free(createTime);
	env->CallVoidMethod(g_jpg_mgr->obj, g_jpg_mgr->mid, string,jpg_len);
	return true;
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_jpgSetNeedFirstJpg
(JNIEnv *env, jclass, jstring path){
	if(g_jpg_mgr==NULL) return;
	g_jpg_mgr->bNeedFistJpg = 1;
	const char * _path= env->GetStringUTFChars(path,NULL);
	strcpy(g_jpg_info->first_jpg_path,_path);
	env->ReleaseStringUTFChars(path,_path);
}


JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_seekbarChange
(JNIEnv *env, jclass cls, jint slot, jint stream, jint fileNo, jint offsetSeconds){
	if(g_handle_mgr == NULL)return;

	int ret = hwnet_close_udp_file_stream(g_handle_mgr->udp_file_handle);
	LOGI("close udp file stream ret:%d,offset:%d",ret,offsetSeconds);
	hwplay_clear_stream_buf(g_handle_mgr->play_handle);

	NetRecFileItem recFile;
	memset(&recFile,0,sizeof(NetRecFileItem));
	recFile.fileno = fileNo;
	recFile.vod_seconds = offsetSeconds;
	recFile.fileno2 = fileNo;
	rec_file_t *file_info = (rec_file_t *)&recFile;
	g_handle_mgr->udp_file_handle = hwnet_get_udp_file_stream(g_handle_mgr->user_handle,slot,stream,file_info,on_udp_file_stream_fun,0);
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_startRecord
(JNIEnv *, jclass, jint user_handle, jint slot){
	int ret = 0;
	ret = hwnet_start_record(user_handle,slot);
	LOGI("ret=%d  start_record",ret);
	return ret;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_stopRecord
(JNIEnv *, jclass, jint user_handle, jint slot){

	int ret = 0;
	ret = hwnet_stop_record(user_handle,slot);
	LOGI("ret= %d  stop_record",ret);
	return ret;
}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_CamJniUtil_setCamSystemTime
(JNIEnv *env, jclass, jobject obj){
	if(g_handle_mgr==NULL ){
		return false;
	}
	if(g_handle_mgr->user_handle == -1){
		return false;
	}
	SYSTEMTIME systm;
	memset(&systm,0,sizeof(systm));
	jclass clz= env->GetObjectClass(obj);
	jfieldID yearID = env->GetFieldID(clz, "wYear", "S");
	jfieldID monthID = env->GetFieldID(clz, "wMonth", "S");
	jfieldID dayOfWeekID = env->GetFieldID(clz, "wDayofWeek", "S");
	jfieldID dayID = env->GetFieldID(clz, "wDay", "S");
	jfieldID hourID = env->GetFieldID(clz, "wHour", "S");
	jfieldID minuteID = env->GetFieldID(clz, "wMinute", "S");
	jfieldID secondID = env->GetFieldID(clz, "wSecond", "S");
	jfieldID millSecondID = env->GetFieldID(clz, "wMilliseconds", "S");

	jshort year = env->GetShortField(obj, yearID);
	jshort month = env->GetShortField(obj, monthID);
	jshort dayOfWeek = env->GetShortField(obj, dayOfWeekID);
	jshort day = env->GetShortField(obj, dayID);
	jshort hour = env->GetShortField(obj, hourID);
	jshort minute = env->GetShortField(obj, minuteID);
	jshort second = env->GetShortField(obj, secondID);
	jshort millSecond = env->GetShortField(obj, millSecondID);

	systm.wYear = year;
	systm.wMonth = month;
	systm.wDayofWeek = dayOfWeek;
	systm.wDay = day;
	systm.wHour = hour;
	systm.wMinute = minute;
	systm.wSecond = second;
	systm.wMilliseconds = millSecond;


	LOGI("year=%d,month=%d,dayofweek=%d,day=%d,hour=%d,minute=%d,second=%d,millSecond=%d",
			systm.wYear,systm.wMonth,systm.wDayofWeek,systm.wDay,systm.wHour,systm.wMinute,systm.wSecond,
			systm.wMilliseconds);
	return hwnet_set_systime(g_handle_mgr->user_handle,&systm)? true:false;
}

JNIEXPORT jobject JNICALL Java_com_howell_jni_CamJniUtil_getCamSystemTime
(JNIEnv *env, jclass clz){
	if(g_handle_mgr==NULL)return NULL;
	SYSTEMTIME systm;
	memset(&systm,0,sizeof(systm));
	if(hwnet_get_systime(g_handle_mgr->user_handle,&systm)){

		LOGI("getCamSystemTime:  y=%d m=%d d=%d h=%d min=%d  sec=%d ",systm.wYear,systm.wMonth,systm.wDay,
				systm.wHour,systm.wMinute,systm.wSecond);
		jclass objectClass = env->FindClass("com/howell/ecamerajing/bean/SystimeInfo");
		jmethodID mcid = env->GetMethodID(objectClass,"<init>","()V");
		jobject obj = env->NewObject(objectClass,mcid);
		jfieldID id = env->GetFieldID(objectClass,"wYear","S");
		env->SetShortField(obj,id,systm.wYear);
		id = env->GetFieldID(objectClass,"wMonth","S");
		env->SetShortField(obj,id,systm.wMonth);
		id = env->GetFieldID(objectClass,"wDayofWeek","S");
		env->SetShortField(obj,id,systm.wDayofWeek);
		id = env->GetFieldID(objectClass,"wDay","S");
		env->SetShortField(obj,id,systm.wDay);
		id = env->GetFieldID(objectClass,"wHour","S");
		env->SetShortField(obj,id,systm.wHour);
		id = env->GetFieldID(objectClass,"wMinute","S");
		env->SetShortField(obj,id,systm.wMinute);
		id = env->GetFieldID(objectClass,"wSecond","S");
		env->SetShortField(obj,id,systm.wSecond);
		id = env->GetFieldID(objectClass,"wMilliseconds","S");
		env->SetShortField(obj,id,systm.wMilliseconds);
		return obj;
	}else{
		return NULL;
	}
}


JNIEXPORT jboolean JNICALL Java_com_howell_jni_CamJniUtil_getCamRecordState
(JNIEnv *, jclass){
	if(g_handle_mgr == NULL)return false;
	if(g_handle_mgr->user_handle==-1)return false;
	alarm_state_t alarm_state;
	memset(&alarm_state,0,sizeof(alarm_state_t));
	int ret=0;
	ret = hwnet_get_alarm_state(g_handle_mgr->user_handle,&alarm_state);
	LOGI("ret = %d state = %d\n",ret,alarm_state.rec_state[0]);
	if(alarm_state.rec_state[0]==1){
		return true;
	}else{
		return false;
	}
}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_CamJniUtil_setWifiInfo
(JNIEnv *env, jclass, jobject obj){
	if(g_handle_mgr==NULL)return false;
	if(g_handle_mgr->user_handle==-1)return false;

	NetWlanAP info;
	memset(&info,0,sizeof(info));
	jclass clz= env->GetObjectClass(obj);
	jfieldID ssidID = env->GetFieldID(clz,"ssid","Ljava/lang/String;");
	jfieldID keyID = env->GetFieldID(clz,"key","Ljava/lang/String;");
	jfieldID channelID = env->GetFieldID(clz,"channel","I");
	jfieldID encryptID = env->GetFieldID(clz,"encrypt","I");
	jfieldID flagID = env->GetFieldID(clz,"flag","I");

	jstring jssid = (jstring)env->GetObjectField(obj, ssidID);
	const char* cssid = env-> GetStringUTFChars(jssid,NULL);
	jstring jkey = (jstring)env->GetObjectField(obj,keyID);
	const char * ckey = env->GetStringUTFChars(jkey,NULL);

	int channel = env->GetIntField(obj,channelID);
	int encrypt = env->GetIntField(obj,encryptID);
	int flag = env->GetIntField(obj,flagID);

	strcpy(info.ssid,cssid);
	strcpy(info.key,ckey);
	info.channel = channel;
	info.encrypt = encrypt;
	info.flag = flag;

	LOGI("1ssid=%s key=%s channel=%d encrypt=%d flag=%d\n",cssid,ckey,channel,encrypt,flag);
	LOGI("2ssid=%s key=%s channel=%d encrypt=%d flag=%d\n",info.ssid,info.key,info.channel,info.encrypt,info.flag);


	int ret = 0;
	ret = hwnet_set_wifi(g_handle_mgr->user_handle,&info);

	env->ReleaseStringUTFChars(jssid,cssid);
	env->ReleaseStringUTFChars(jkey,ckey);
	return ret?true:false;
}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_CamJniUtil_getWifiInfo
(JNIEnv *env, jclass, jobject obj){
	if(g_handle_mgr==NULL) return false;
	if(g_handle_mgr->user_handle==-1)return false;

	NetWlanAP info;
	memset(&info,0,sizeof(info));
	int ret = 0;
	ret = hwnet_get_wifi(g_handle_mgr->user_handle,&info);
	if(ret == 0){
		return false;
	}

	//test

	if(JNI_TEST){
		strcpy(info.ssid,"asdfsa");
		strcpy(info.key,"a阿斯蒂芬");
		info.channel = 0;
		info.encrypt = 2;
		info.flag =123;
	}
	jclass clz = env->GetObjectClass(obj);
	jfieldID ssidID = env->GetFieldID(clz,"ssid","Ljava/lang/String;");
	jfieldID keyID = env->GetFieldID(clz,"key","Ljava/lang/String;");
	jfieldID channelID = env->GetFieldID(clz,"channel","I");
	jfieldID encryptID = env->GetFieldID(clz,"encrypt","I");
	jfieldID flagID = env->GetFieldID(clz,"flag","I");


	jstring jssid = env->NewStringUTF(info.ssid);
	jstring jkey = env->NewStringUTF(info.key);

	env->SetObjectField(obj,ssidID,jssid);
	env->SetObjectField(obj,keyID,jkey);
	env->SetIntField(obj,channelID,info.channel);
	env->SetIntField(obj,encryptID,info.encrypt);
	env->SetIntField(obj,flagID,info.flag);

	env->DeleteLocalRef(jssid);
	env->DeleteLocalRef(jkey);
	return true;
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_downloadInit
(JNIEnv *env, jclass){
	if(g_download_mgr==NULL){
		g_download_mgr = (download_maganer_t*)malloc(sizeof(download_maganer_t));
		memset(g_download_mgr,0,sizeof(download_maganer_t));
	}
	pthread_mutex_init(&g_download_mgr->lock,NULL);
	g_download_mgr->frame_len = 0;
	g_download_mgr->bStop = 0;

	env->GetJavaVM(&g_download_mgr->jvm);
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_downloadDeinit
(JNIEnv *env, jclass){
	if(NULL!=g_download_mgr){
		//free obj
		if(g_download_mgr->obj !=NULL){
			env->DeleteGlobalRef(g_download_mgr->obj);
			g_download_mgr->obj = NULL;
		}

		if(g_download_mgr->data_array!=NULL){
			env->DeleteGlobalRef(g_download_mgr->data_array);
			g_download_mgr->data_array = NULL;
		}
		pthread_mutex_destroy(&g_download_mgr->lock);

		free(g_download_mgr);
		g_download_mgr = NULL;
	}

}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_downloadSetCallbackObject
(JNIEnv *env, jclass clz, jobject obj, jint flag){
	if(g_download_mgr==NULL)return;
	switch (flag) {
	case 0:
		g_download_mgr->obj = env->NewGlobalRef(obj);
		break;
	default:
		break;
	}
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_downloadSetCallbackFieldName
(JNIEnv *env, jclass cls, jstring field_name, jint flag){
	if(g_download_mgr == NULL) return;
	jclass clz = env->GetObjectClass(g_download_mgr->obj);
	const char * _field_name= env->GetStringUTFChars(field_name,NULL);
	jfieldID id = NULL;
	switch (flag) {
	case 0:
	{
		id = env->GetFieldID(clz,_field_name,"J");
		g_download_mgr->total_size = id;
	}
	break;
	case 1://no use
	{
		id = env->GetFieldID(clz,_field_name,"J");
		g_download_mgr->cur_size = id;
	}
	break;
	case 2:
	{
		id = env->GetFieldID(clz,_field_name,"I");
		g_download_mgr->fieldID_data_length = id;
	}
	break;

	case 3:
	{
		id = env->GetFieldID(clz,_field_name,"[B");
		jbyteArray arr = (jbyteArray)env->GetObjectField(g_download_mgr->obj,id);
		g_download_mgr->data_array_len = env->GetArrayLength(arr);
		g_download_mgr->data_array = (jbyteArray)env->NewGlobalRef(arr);

	}
	break;
	case 4:
	{
		id = env->GetFieldID(clz,_field_name,"J");
		g_download_mgr->first_timesteamp = id;
	}
	break;
	default:
		break;
	}

	env->ReleaseStringUTFChars(field_name,_field_name);
}

JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_downloadSetCallbackMethodName
(JNIEnv *env, jclass cls, jstring method_name, jint flag){
	if(g_download_mgr == NULL)return;
	jclass clz = env->GetObjectClass(g_download_mgr->obj);
	const char * _method_name= env->GetStringUTFChars(method_name,NULL);

	switch(flag){
	case 0:
		strcpy(g_download_mgr->callback_method_name,_method_name);
		break;
	default:
		break;
	}

	env->ReleaseStringUTFChars(method_name,_method_name);
}



long get_download_total_size(){
	//	if (g_download_mgr->jvm->AttachCurrentThread(&g_download_mgr->env,NULL) != JNI_OK) {
	//		LOGE("%s: AttachCurrentThread() failed", __FUNCTION__);
	//		return;
	//	}

	//	if(g_download_mgr==NULL)return -1;

	//
	//	unsigned long long len = 0;
	//
	//	file_stream_head_t file_stream;
	//	memset(&file_stream,0,sizeof(file_stream_head_t));
	//	int ret = 0;
	//	int head_len=0;
	//
	//	ret = hwnet_get_file_stream_head(g_handle_mgr->udp_download_handle ,(char *)&file_stream,sizeof(file_stream_head_t),&head_len);
	//
	//
	//
	//	len = file_stream.len;
	//
	//	LOGE("len = %lld",len);
	//
	//	LOGI("ret=%d  len=%lld    head_len=%d  file_len=%ld ",ret,len,head_len,file_stream.len);

	//	env->SetLongField(obj,g_download_mgr->total_size,len);

	//	LOGE("get downlaod total size ok");

	//	if (g_download_mgr->jvm->DetachCurrentThread() != JNI_OK) {
	//		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	//	}

	//	return len;
	//	error:
	//	if (g_download_mgr->jvm->DetachCurrentThread() != JNI_OK) {
	//		LOGE("%s: DetachCurrentThread() failed", __FUNCTION__);
	//	}

}

JNIEXPORT jboolean JNICALL Java_com_howell_jni_CamJniUtil_downloadStart
(JNIEnv *env, jclass cls ,jobject j_obj){
	if(g_handle_mgr==NULL) {
		LOGE("g_handle_mgr==null  downloadstart error");
		return false;
	}

	if(g_download_mgr==NULL){
		LOGE("g_download_mgr==null downloadstart error");
		return false;
	}
	if(g_download_stamp == NULL){
		g_download_stamp = (download_timestamp_t *)malloc(sizeof(download_timestamp_t));
		memset(g_download_stamp , 0 , sizeof(download_timestamp_t));
		g_download_stamp->bDoOnce = false;
	}


	jclass objectClass = env->GetObjectClass(j_obj);
	jfieldID begYearFieldID = env->GetFieldID(objectClass,"begYear","S");
	jfieldID begMonthFieldID = env->GetFieldID(objectClass,"begMonth","S");
	jfieldID begDayFieldID = env->GetFieldID(objectClass,"begDay","S");
	jfieldID begHourFieldID = env->GetFieldID(objectClass,"begHour","S");
	jfieldID begMinuteFieldID = env->GetFieldID(objectClass,"begMinute","S");
	jfieldID begSecondFieldID = env->GetFieldID(objectClass,"begSecond","S");
	jfieldID fileNoFieldID = env->GetFieldID(objectClass,"fileNo","I");
	jfieldID totalSecondsFieldID = env->GetFieldID(objectClass,"totalSeconds","I");
	jfieldID totalFramesFieldID = env->GetFieldID(objectClass,"totalFrames","I");
	jfieldID offsetSecondsFieldID = env->GetFieldID(objectClass,"offsetSeconds","I");

	jshort begYear = env->GetShortField( j_obj , begYearFieldID);
	jshort begMonth = env->GetShortField( j_obj , begMonthFieldID);
	jshort begDay = env->GetShortField(j_obj , begDayFieldID);
	jshort begHour = env->GetShortField( j_obj , begHourFieldID);
	jshort begMinute = env->GetShortField(j_obj , begMinuteFieldID);
	jshort begSecond = env->GetShortField(j_obj , begSecondFieldID);
	jint fileNo = env->GetIntField(j_obj,fileNoFieldID);
	jint totalSeconds = env->GetIntField(j_obj , totalSecondsFieldID);
	jint totalFrames = env->GetIntField(j_obj , totalFramesFieldID);
	jint offsetSeconds = env->GetIntField(j_obj , offsetSecondsFieldID);

	LOGI("%d %d %d %d %d %d  sec=%d  frame=%d vod=%d no=%d",begYear,begMonth,begDay,begHour,begMinute,begSecond,totalSeconds,totalFrames,offsetSeconds,fileNo);


	NetRecFileItem recFile;
	memset(&recFile,0,sizeof(NetRecFileItem));
	recFile.fileno = fileNo;
	recFile.created.wYear = begYear;
	recFile.created.wMonth = begMonth;
	recFile.created.wDay = begDay;
	recFile.created.wHour = begHour;
	recFile.created.wMinute = begMinute;
	recFile.created.wSecond = begSecond;
	recFile.total_seconds = totalSeconds;
	recFile.total_frames = totalFrames;
	recFile.vod_seconds = offsetSeconds;
	recFile.fileno2 = fileNo;
	rec_file_t *file_info = (rec_file_t *)&recFile;


	//初始化
	g_download_mgr->bStop = 0;
	g_download_mgr->head_ready = 0;

	//	g_handle_mgr->udp_file_handle =     hwnet_get_udp_file_stream(g_handle_mgr->user_handle,slot,stream,file_info,on_udp_file_stream_fun,0);
	g_cur_pos = 0;//当前buf 结尾的地方
	g_fream_len = 0;
	g_offset = 0;
	g_bDoOnce = true;//第一次
	g_download_mgr->timestemp = 0;
	g_download_mgr->frame_len = 0;
	g_download_mgr->bfirst_timestamp = true;

	if(JNI_TEST){
		file_stream_t info;
		memset(&info,0,sizeof(info));

		jfieldID id = env->GetFieldID(objectClass,"endYear","S");
		jshort val  = env->GetShortField( j_obj , id);
		file_info->end.wYear = val;
		id = env->GetFieldID(objectClass,"endMonth","S");
		val = env->GetShortField(j_obj,id);
		file_info->end.wMonth = val;
		id = env->GetFieldID(objectClass,"endDay","S");
		val = env->GetShortField(j_obj,id);
		file_info->end.wDay = val;
		id = env->GetFieldID(objectClass,"endHour","S");
		val = env->GetShortField(j_obj,id);
		file_info->end.wHour = val;
		id = env->GetFieldID(objectClass,"endMinute","S");
		val = env->GetShortField(j_obj,id);
		file_info->end.wMinute = val;
		id = env->GetFieldID(objectClass,"endSecond","S");
		val = env->GetShortField(j_obj,id);
		file_info->end.wSecond = val;
		file_info->slot = 0;
		file_info->type = 0;
		LOGI("beg year:%d  month:%d  day%d  hour:%d  min:%d sec:%d  ",file_info->beg.wYear,
				file_info->beg.wMonth,file_info->beg.wDay,file_info->beg.wHour,file_info->beg.wMinute,file_info->beg.wSecond);



		LOGI("end year:%d  month:%d  day%d  hour:%d  min:%d sec:%d  slot=%d  type=%d",file_info->end.wYear,
				file_info->end.wMonth,file_info->end.wDay,file_info->end.wHour,file_info->end.wMinute,file_info->end.wSecond,
				file_info->slot,file_info->type);

		LOGI("user handle = %d",g_handle_mgr->user_handle);

		g_handle_mgr->udp_download_handle = hwnet_get_file_stream(g_handle_mgr->user_handle, 0,file_info->beg,file_info->end,on_udp_download_stream_fun,0,&info);
		LOGE("info.len=%lld",info.len);
		//		g_handle_mgr->udp_download_handle = hwnet_get_udp_file_stream(g_handle_mgr->user_handle,0,1,file_info,on_udp_download_stream_fun,0);
		g_download_mgr->download_file_len = info.len;
	}else{


		/*
		 *@param stream : 0 按帧率发送  1全速发送
		 */
		g_handle_mgr->udp_download_handle = hwnet_get_udp_file_stream(g_handle_mgr->user_handle,0,1,file_info,on_udp_download_stream_fun,0);
		//		file_stream_t info;
		//		memset(&info,0,sizeof(info));
		//		g_handle_mgr->udp_download_handle = hwnet_get_file_stream(g_handle_mgr->user_handle, 0,file_info->beg,file_info->end,on_udp_download_stream_fun,0,&info);
		//		g_download_mgr->download_file_len = info.len;
	}



	if(g_handle_mgr->udp_download_handle < 0)
	{
		LOGE("get udp download stream failed");
		return false;
	}



#if 0
	RECT area ;
	HW_MEDIAINFO media_head;
	memset(&media_head,0,sizeof(media_head));
	media_head.media_fourcc = HW_MEDIA_TAG;
	media_head.vdec_code = VDEC_H264;
	media_head.adec_code = ADEC_ADPCM_WAV;
	media_head.au_bits = 16;
	media_head.au_sample = 8;
	media_head.au_channel = 1;

	PLAY_HANDLE  ph = hwplay_open_stream((char*)&media_head,sizeof(media_head),1024*1024,1,area);
	if(ph == -1){
		LOGE("ph get error");
		return false;
	}
	hwplay_open_sound(ph);
	//	hwplay_set_max_framenum_in_buf(ph,25);
	hwplay_register_source_data_callback(ph,on_download_callback,0);

	//get total size 回调


	//开始下载
	hwplay_play(ph);

	g_handle_mgr->play_handle = ph;
#endif
	return true;

}


JNIEXPORT void JNICALL Java_com_howell_jni_CamJniUtil_downloadStop
(JNIEnv *, jclass){
	if(g_handle_mgr==NULL)return;

	g_download_mgr->bStop = 1;

	//
	//	int ret =
	//			hwnet_close_file_stream(g_handle_mgr->udp_download_handle handle);

	int ret = 0;
	if(JNI_TEST){
		ret = hwnet_close_file_stream(g_handle_mgr->udp_download_handle);
	}else{
		ret = hwnet_close_udp_file_stream(g_handle_mgr->udp_download_handle);
	}
	g_download_mgr->stream_len = 0;
	g_download_mgr->timestemp = 0;
	LOGI("downloat stop ret=%d",ret);

	if(g_download_stamp!=NULL){
		free(g_download_stamp);
		g_download_stamp = NULL;
	}


}


JNIEXPORT jlong JNICALL Java_com_howell_jni_CamJniUtil_downloadGetPos
(JNIEnv *, jclass){
	if(g_download_mgr == NULL)return -1;
	return g_download_mgr->stream_len;
}

JNIEXPORT jlong JNICALL Java_com_howell_jni_CamJniUtil_downloadGetTotalLen
(JNIEnv *evn, jclass ){
	if(g_download_mgr==NULL)return -1;
	//	return get_download_total_size();
	return g_download_mgr->download_file_len;
}

JNIEXPORT jlong JNICALL Java_com_howell_jni_CamJniUtil_downloadGetCutTimeStamp
(JNIEnv *, jclass){
	if(g_download_mgr==NULL)return -1;
	//	g_download_mgr->bCalc_timestamp = true;
	LOGI("get download cut time stamp = %lld",g_download_mgr->timestemp);
	return g_download_mgr->timestemp;
}

JNIEXPORT jint JNICALL Java_com_howell_jni_CamJniUtil_downloadGetFrameLen
(JNIEnv *, jclass){
	if(g_download_mgr==NULL)return -1;
	return g_download_mgr->frame_len;
}





