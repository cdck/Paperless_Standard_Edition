#ifndef  _JNI_WALLETNET_INIT_INTERFACE_H_
#define _JNI_WALLETNET_INIT_INTERFACE_H_
#include "base.h"
#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif
//register jni functions
int wallet_netinit_register_native_methods(JNIEnv* env);

//init net system
//data --> pbui_MeetCore_InitParam
//Sucess return 0,failed retunr -1
int Init_walletSys(JNIEnv *env, jobject thiz, jbyteArray data);

//benable 0:foregroud 1:backgroud
void jni_enablebackgroud(JNIEnv *env, jobject thiz, jint benable);

//for system function call
//sucess return a bytearray parse with type and method,failed return a null bytearray
jbyteArray jni_call(JNIEnv *env, jobject thiz, jint type, jint method, jbyteArray pdata);

//for init android camara capture
//channelstart is channelindex
int jni_AndroidDevice_initcapture(JNIEnv *env, jobject thiz, jint type, jint channelstart);

//for pass android camara capture data
//channelstart is channelindex
//iskeyframe is video encode key frame flag 0代表普通帧，1代表关键帧
//pts playtimestamp microseconds
int jni_AndroidDevice_call(JNIEnv *env, jobject thiz, jint channelstart, jint iskeyframe, jlong pts, jbyteArray pdata);

jbyteArray jni_AndroidDevice_NV21ToI420(JNIEnv *env, jobject thiz,jbyteArray pdata,jint width, jint height);
jbyteArray jni_AndroidDevice_NV21ToNV12(JNIEnv *env, jobject thiz, jbyteArray pdata, jint width, jint height);
jbyteArray jni_AndroidDevice_YV12ToNV12(JNIEnv *env, jobject thiz, jbyteArray pdata, jint width, jint height);


//回调函数 java callback function c++ return to 
//数据变更通知回调
//int callback_method(int type, int method, byte[] pdata, int datalen);

//流通道操作回调
//channelstart 流通道索引
//oper 操作值 参见libDevice_android.h头文件中ANDROID_OPERFLAG_PIXFORMAT的定义
//int callback(int channelstart, int oper);

//jni_call执行后的错误码回调函数
//type 类型
//method 类型对于的方法
//retcode 错误码，参见错误码的定义
//void error_ret(int type, int method, int retcode);

//将yuv的图像数据返回给安卓层显示
//res 资源id
//int callback_yuvdisplay(int res, int w, int h, byte[] y, byte[] u, byte[] v);

//将未解码的图像帧数据返回给安卓层解码显示
//res 资源id
//codecid 帧的编码id
//w,h宽高
//packet帧数据
//pts该帧数据的显示时间戳 单位：毫秒
//codecdata sps/pps数据
//int callback_videodecode(int iskeyframe, int res, int codecid, int w, int h, byte[] packet, long pts, byte[] codecdata);

//初始化解码显示通知
//res 资源id
//int callback_startdisplay(int res);

//停止解码显示通知
//res 资源id
//int callback_stopdisplay(int res);
#ifdef __cplusplus
}
#endif

#endif
