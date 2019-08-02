package com.huashe.lockscreen;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class BaseApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        //权限申请


        Log.i("eee","执行了BaseApplication");
        mContext=this;
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }
    /**
     * 获取全局上下文*/
    public static synchronized Context getInstance() {
        return mContext;
    }


    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            //  restartApp(); //发生崩溃异常时,重启应用
            Log.e("未捕获的程序异常","执行了uncaughtException:"+e);

//            Intent intent =new Intent("", TestActivity.class);
//            startActivity(intent);

            Intent intent = getPackageManager()
                    .getLaunchIntentForPackage(getPackageName());
            PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
            AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, 0, restartIntent); // 1秒钟后重启应用
            System.exit(0);

        }
    };



}
