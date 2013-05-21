#include<string.h>
#include<jni.h>
#include<stdio.h>

jstring Java_com_paperairplane_music_share_Main_convertDuration(JNIEnv* env,
		jobject thiz , jlong _duration) {
	char result[20]="", *min, *sec;
	min=malloc(sizeof(char)*2);
	sec=malloc(sizeof(char)*2);
	int m = _duration / (60 * 1000);
	sprintf(min, "%02d", m);
	strcat(result, min);
	strcat(result, ":");
	int s = (_duration % (60 * 1000)) / 1000;
	sprintf(sec, "%02d", s);
	strcat(result, sec);
	//我真是疯了连sprintf都不知道了
	return (*env)->NewStringUTF(env, result);
}
