package com.huashe.lockscreen.test;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


import com.huashe.lockscreen.R;
import com.huashe.lockscreen.service.WhiteService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TestActivity extends AppCompatActivity {

    String TAG="eeeTestActivity";
    Button button1,button2;
    NotificationManager manager;

    private MyReceiver receiver;
    //用来存放每一个recentApplication的信息，我们这里存放应用程序名，应用程序图标和intent。
    private List<HashMap<String,Object>> appInfos = new ArrayList<HashMap<String, Object>>();
    CircleBarView  circleBarView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);



        Log.i("eeeTestActivity","onCreate执行一次");

        button1 =findViewById(R.id.style1);
        button2= findViewById(R.id.style2);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        circleBarView = (CircleBarView)findViewById(R.id.circle_view);
        TextView textView= findViewById(R.id.valueString);





        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent whiteIntent = new Intent(TestActivity.this, WhiteService.class);
                startService(whiteIntent);


            }
        });

  /*        circleBarView.setTextView(textView);
        circleBarView.setOnAnimationListener(new CircleBarView.OnAnimationListener() {
            @Override
            public String howToChangeText(float interpolatedTime, float progressNum, float maxNum) {
                DecimalFormat decimalFormat=new DecimalFormat("0.00");
                //  String s = decimalFormat.format(interpolatedTime * progressNum / maxNum * 100) + "%"; 这个是进度条百分比计数
                int cf= (int) Math.floor(interpolatedTime * 10);
                return 10-cf+"";
            }
        });
        circleBarView.setProgressNum(100,10000);


        //屏幕广播监听
        receiver = new MyReceiver();
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        registerReceiver(receiver, homeFilter);*/


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(receiver!=null){
            unregisterReceiver(receiver);
        }

        Log.i("eeeTestActivity","执行了onDestroy方法");
    }



    private class MyReceiver extends BroadcastReceiver {

        private final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

                if (reason == null)
                    return;

                // Home键
                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                    Log.i("eeeTestActivity","按了Home键");
                    //     int c=  TaskUtils.getRunningAppProcessInfoSize(TestActivity.this);


                }

                // 最近任务列表键
                if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                    Log.i("eeeTestActivity","按了最近任务列表");

                    Log.i("eee","搅屎棍");


                }
            }
        }
    }





    boolean isTaskList2Home=false;
    public  void rebootApp(){

        Log.i("eee","执行了重启rebootApp");
        isTaskList2Home=false;
        Intent intent = getPackageManager()
                .getLaunchIntentForPackage(getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(this, 0, intent, 0);
        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, 1000, restartIntent); // 1秒钟后重启应用

    }






}


