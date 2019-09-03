package com.huashe.lockscreen;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huashe.lockscreen.View.LockUtil;
import com.huashe.lockscreen.network.CONSTANTS;
import com.huashe.lockscreen.network.RetrofitHelper;
import com.huashe.lockscreen.service.WhiteService;
import com.huashe.lockscreen.test.CircleBarView;
import com.huashe.lockscreen.util.AESCipher;
import com.huashe.lockscreen.util.QRCodeUtil;
import com.huashe.lockscreen.util.SPUtils;
import com.huashe.lockscreen.video.MVideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String TAG="eeeMainActivity";
    Button LockScreenBtn,unlockBtn;
    EditText inputPassword_edt;
    LockUtil lockUtil;
    boolean isLock=true; //默认锁住了
    Dialog dialog; //倒计时询问框
    String encryptImei;
    TelephonyManager  phone;
    String IMEI; //平板的imei
    ImageView qrcodeImg;// 请求二维码
    String currentCode="currentCode";// 当前解锁使用的解锁码
    String IsGrant="isgrant";

    //视频使用属性
    List<String> videoList;
    MVideoView mVideoView;
    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;
    int CurrentVideoPosition=0; //初始播放视频序号
    //加载本地视频
    String path=  Environment.getExternalStorageDirectory().getPath()+"/pic";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //锁屏外悬浮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        //彻底隐藏非安全验证的keyguard
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);


        //禁止系统锁屏
        KeyguardManager keyguardManager = (KeyguardManager)this.getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();//关闭系统锁屏


        setContentView(R.layout.activity_main);

        Log.i(TAG,"onCreate执行了一次");

        String isgrant=  SPUtils.get(this,IsGrant,"").toString();
        if(isgrant.equals("yes")){
            Log.i(TAG,"yes and createView");
            creteView();
        }else{
            Log.i(TAG,"permissino");
            initPermission();
        }

    }



    public void creteView(){
        Intent whiteIntent = new Intent(MainActivity.this, WhiteService.class);
        startService(whiteIntent);
        initView();
        makeQrCode();//生成二维码
        LoadViewFiles();


    }



    private  void  initView(){
        //设置监听广播通知
        initFilter();
        registerReceiver(mBatInfoReceiver, filter);

        //实例化控件
        LockScreenBtn= findViewById(R.id.lockScreen_btn);
        unlockBtn= findViewById(R.id.unlock_btn);
        qrcodeImg =findViewById(R.id.qrcode_img);

        inputPassword_edt=findViewById(R.id.inputPassword_edt);

        LockScreenBtn.setOnClickListener(this);
        unlockBtn.setOnClickListener(this);

        //锁屏管理器对象实例
        lockUtil= LockUtil.getInstance(this);
        lockUtil.initLock();

    }


    //广播监听
    IntentFilter filter;
    private void initFilter() {
        if(filter==null){
            filter = new IntentFilter();
            // 屏幕灭屏广播
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            // 屏幕亮屏广播
            filter.addAction(Intent.ACTION_SCREEN_ON);
            // 屏幕解锁广播
            filter.addAction(Intent.ACTION_USER_PRESENT);
            // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
            // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
            // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
            filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            //filter.addAction(Intent.);
        }

    }

    boolean isBlackScreen=false;
    BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            //   Log.i("eeeTestActivity","触发的Intent为："+action);

            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Log.d(TAG, "screen on");
                onStartActivity();
                isBlackScreen=false; //屏幕亮了
            }
            else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Log.d(TAG, "screen off");
                isBlackScreen=true; //屏幕熄灭了
                onStartActivity();

            }
            else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                Log.d(TAG, "screen unlock");
            } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {

                String reason=intent.getStringExtra("reason");

                Log.i(TAG,"reason:"+reason);
                if(!isBlackScreen&&isLock){//
                    onStartActivity();
                    lockUtil.doLockScreen();
                }

            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        lockUtil.isOpen();
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lockScreen_btn:
                lockUtil.doLockScreen();
                break;

            case R.id.unlock_btn:

                checkRequestCode();
                //注释为测试时候使用
//                isLock=false;
//                moveTaskToBack(false);

                break;
            default:
                break;
        }
    }


    public  void onStartActivity(){
        if(isBackground(this)){

            Log.i(TAG,"执行了页面跳转");

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, intent, 0);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if(inputPassword_edt!=null){
            inputPassword_edt.clearFocus();
        }
        Log.i("eee","执行了onPause");
//        if(isLock){
////            onStartActivity();
////            Log.i("eee","执行了onPause中Activity");
//
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mVideoView!=null){
            mVideoView.stopPlayback();
            Log.i("eeeTestActivity","stopPlayback");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("eeeTestActivity","onNewIntent");
        LoadViewFiles();
    }

    @Override
    protected void onDestroy() {
        Log.i("eee","执行了destroy");
        if(mBatInfoReceiver!=null){
            unregisterReceiver(mBatInfoReceiver);
        }
        if(lockUtil!=null){
            lockUtil.destroy();
        }
        if(mVideoView!=null){
            mVideoView.stopPlayback();
        }
        super.onDestroy();
    }

    @Override  //这里对返回键进行屏蔽
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_HOME||keyCode==KeyEvent.KEYCODE_BACK){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Log.i(context.getPackageName(), "后台"
                            + appProcess.processName);
                    return true;
                } else {
                    Log.i(context.getPackageName(), "前台"
                            + appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }



    //<editor-fold desc="#操作询问#">
    //TODO 这里设置掉起锁屏时间。
    TimeCount time = new TimeCount(1*60 * 1000, 1000);

    class TimeCount extends CountDownTimer {

        //构造发方法
        TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            onStartActivity();
            QueryDialog();
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示

        }
    }
    /**询问dialog
     * **/
    public void QueryDialog(){

        View dialogView=getLayoutInflater().inflate(R.layout.dialog_query_layout,null);

        CircleBarView circleBarView=dialogView.findViewById(R.id.progress_view);

        TextView TimeTxt= dialogView.findViewById(R.id.time_txt);
        TextView sureBtn= dialogView.findViewById(R.id.surebtn_txt);


        circleBarView.setTextView(TimeTxt);
        circleBarView.setOnAnimationListener(new CircleBarView.OnAnimationListener() {
            @Override
            public String howToChangeText(float interpolatedTime, float progressNum, float maxNum) {
                //DecimalFormat decimalFormat=new DecimalFormat("0.00");
                //  String s = decimalFormat.format(interpolatedTime * progressNum / maxNum * 100) + "%"; 这个是进度条百分比计数
                int cf= 9-(int) Math.floor(interpolatedTime * 10);
                if(cf==0){
                    dialog.dismiss();

                    if(!isLock){
                        //上锁请求告诉后台上锁了
                        updateUseRecords();
                        Log.i("eee","关闭dialog并上锁");
                    }
                    isLock=true;
                }
                return cf+"";
            }
        });


        dialog=new Dialog(this,R.style.dialogStyle);
        ViewGroup.LayoutParams params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.addContentView(dialogView,params);

        dialog.setCancelable(false);
        dialog.show();
        Log.i("eee","执行了dialog");
        circleBarView.setProgressNum(100,10000);

        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击确认按钮之后 去掉dialog回到桌面并重新计时
                dialog.dismiss();
                if(time!=null){
                    time.cancel();
                    time.start();
                    moveTaskToBack(false);
                }
            }
        });

    }
    //</editor-fold>

    //<editor-fold desc="#视频加载控制#">
    //初始化视频控件
    public void initVideoView(){

        if(mVideoView==null){
            mVideoView= findViewById(R.id.videoView_one);
            mVideoView.setVideoURI(Uri.parse(videoList.get(0)));
            mVideoView.requestFocus();
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mp.setLooping(false);//设置视频重复播放
                }
            });
            mVideoView.start();//播放视频
            mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    turnNextVideo("down");
                }
            });

            mVideoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    //继承了Activity的onTouchEvent方法，直接监听点击事件
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        //当手指按下的时候
                        x1 = event.getX();
                        y1 = event.getY();
                    }
                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        //当手指离开的时候
                        x2 = event.getX();
                        y2 = event.getY();
                        if((x1 - x2 > 50)||(y1 - y2 > 50)) {
                            Log.i("eee","执行了一次向左滑动");
                            turnNextVideo("down");
                        } else if((x2 - x1 > 50)||(y2 - y1 > 50)) {
                            Log.i("eee","执行了一次向右滑动");
                            turnNextVideo("up");
                        }
                    }
                    return true;
                }
            });


//        MediaController medis=new MediaController(this);//显示控制条
//        videoView.setMediaController(medis);
//        medis.setMediaPlayer(videoView);//设置控制的对象
//        medis.show();

        }else{
            mVideoView.start();//播放视频
        }

    }

    //播放上一个或下一个视频
    private  void turnNextVideo(String upOrDown){
        //第一个视频不能继续往上翻视频
        if((CurrentVideoPosition-1<0&&upOrDown.equals("up"))){
            return;
        }

        //最后一个视频往下翻时自动跳到第一个视频
        if((CurrentVideoPosition+1==videoList.size()&&upOrDown.equals("down"))){
            CurrentVideoPosition=0;
            mVideoView.setVideoPath(videoList.get(CurrentVideoPosition));

        }else if(upOrDown.equals("down")){
            mVideoView.setVideoPath(videoList.get(CurrentVideoPosition+1));
            CurrentVideoPosition++;
        }else{
            mVideoView.setVideoPath(videoList.get(CurrentVideoPosition-1));
            CurrentVideoPosition--;
        }
        mVideoView.start();

    }


    //从本地读取视频
    private void LoadViewFiles(){
        if(videoList==null){
            videoList=new ArrayList<>();
        }else{
            videoList.clear();
        }
        File file = new File(path);
        if (file.exists()) {
            File[] listFiles = file.listFiles();

            if(listFiles.length==0){
                Toast.makeText(this, "请添加宣传视频", Toast.LENGTH_SHORT).show();
                return;
            }
            for (File file1 : listFiles) {
                videoList.add(file1.toString());
            }
            initVideoView();

        }else{
            file.mkdir();
            Toast.makeText(this, "请添加宣传视频", Toast.LENGTH_SHORT).show();
        }
    }
    //</editor-fold>


    //<editor-fold desc="#网络请求#">
    /***
     * 请求验证码页面连接
     * */

    public void makeQrCode(){

        phone = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            IMEI= phone.getImei();
        }else{
            IMEI= phone.getDeviceId();
        }
        if(IMEI!=null&&IMEI.length()>0){
            encryptImei= AESCipher.encrypt(IMEI);
            String url= CONSTANTS.CodeRequestUrl +"?deviceId="+encryptImei;

            Bitmap bitmap= QRCodeUtil.createQRCodeBitmap(url,480,480);
            qrcodeImg.setImageBitmap(bitmap);
        }


    }


    /** 核实验证码
     * */
    public void checkRequestCode(){
        final String  inputCode=  inputPassword_edt.getText().toString().trim();
        if(inputCode.length()!=6){
            Toast.makeText(this, "请确认验证码", Toast.LENGTH_SHORT).show();
            return;
        }
        final Gson gson=new Gson();
        RetrofitHelper.getHttpAPITest().checkRequestCode("0",encryptImei,inputCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onNext(Object o) {
                        String jsonString = gson.toJson(o);
                        try {
                            JSONObject jsonObject=new JSONObject(jsonString);
                            String rlt=jsonObject.get("data").toString();
                            if("true".equals(rlt)){
                                isLock=false;
                                moveTaskToBack(false);
                                Log.i(TAG,"解锁成功");
                                inputPassword_edt.setText("");
                                time.start();
                                SPUtils.put(MainActivity.this,currentCode,inputCode);
                            }else{
                                Toast.makeText(MainActivity.this, "验证码有误", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**更新使用记录
     * */
    public void updateUseRecords(){

        final Gson gson=new Gson();
        String CurCode=SPUtils.get(this,currentCode,"").toString();
        RetrofitHelper.getHttpAPITest().updateUseRecords("0",encryptImei,CurCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onNext(Object o) {
                        String jsonString = gson.toJson(o);
                        try {
                            JSONObject jsonObject=new JSONObject(jsonString);
                            String rlt=jsonObject.get("data").toString();
                            if("true".equals(rlt)){
                                Toast.makeText(MainActivity.this, "已上锁", Toast.LENGTH_SHORT).show();
                                Log.i("eee","上锁回传成功");

                            }else{
                                Toast.makeText(MainActivity.this, "上锁回传失败", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }
    //</editor-fold>


    //<editor-fold desc="#权限申请#">
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("eee","权限申请onRequestPermissionsResult");
        if (requestCode == 123) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //判断是否勾选禁止后不再询问
                    boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions[i]);
                    if (showRequestPermission) {
                        Toast.makeText(this, "权限未申请", Toast.LENGTH_SHORT).show();
                    } else {
                        SPUtils.put(this,IsGrant,"yes");
                        creteView();
                    }
                }else{
                    SPUtils.put(this,IsGrant,"yes");
                    creteView();
                }
            }
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.READ_PHONE_STATE
                ,Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<>();

        for (String perm :permissions){
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()){
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }
    //</editor-fold>


}
