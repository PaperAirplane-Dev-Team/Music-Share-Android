package com.paperairplane.music.share;

import java.lang.Thread.UncaughtExceptionHandler;


public class CrashHandler implements UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
    	MyLogger.wtf(Consts.DEBUG_TAG, "UNCAUGHT EXCEPTION!", ex);
    }


}
