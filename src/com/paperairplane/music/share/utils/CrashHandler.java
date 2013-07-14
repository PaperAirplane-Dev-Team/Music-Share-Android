package com.paperairplane.music.share.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import com.paperairplane.music.share.Consts;


public class CrashHandler implements UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
    	MyLogger.wtf(Consts.DEBUG_TAG, "UNCAUGHT EXCEPTION!", ex);
    	//或许我们应该处理一下
    	//FIXME
    }


}
