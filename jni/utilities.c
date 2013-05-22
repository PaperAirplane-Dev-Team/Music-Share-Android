#include<string.h>
#include<jni.h>
#include<stdio.h>

jstring Java_com_paperairplane_music_share_Main_convertDuration(JNIEnv* env,
		jobject thiz, jlong duration) {
	char result[8] = "";
	int hour = duration / (3600 * 1000);
	int minute = (duration / (60 * 1000)) % 60;
	int second = (duration % (60 * 1000)) / 1000;
	if (hour == 0)
		sprintf(result, "%02d:%02d", minute, second);
	else
		sprintf(result, "%02d:%02d:%02d", hour, minute, second);
	return (*env)->NewStringUTF(env, result);
}
