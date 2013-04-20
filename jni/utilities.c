/*#include<stdlib.h>
#include<string.h>*/
#include<jni.h>
/*#include<stdio.h>*/

jstring Java_com_paperairplane_music_share_Main_doNothing(JNIEnv* env,
		jobject thiz/*, jlong _duration*/) {
	/*char* result, min, sec;
	long m = _duration / (60 * 1000);
	gcvt(m, 2, min);
	if (m < 10) {
		char *temp = "";
		strcat(temp, min);
		strcat(result, temp);
	}
	else strcat(result, min);
	strcat(result, ":");
	long s = (_duration % (60 * 1000)) / 1000;
	gcvt(s, 2, sec);
	if (s < 10) {
		char *temp = "";
		strcat(temp, sec);
		strcat(result, temp);
	}
	else strcat(result, sec);*/
	//我TMD是真心无力，NDK的库都不全，gcvt都没有，都没有！
	return (*env)->NewStringUTF(env, "Hi There, I am JNI. 真无聊啊真没用啊");
	//干点儿别的吧……
	//别忘了每次ndk-build
}
