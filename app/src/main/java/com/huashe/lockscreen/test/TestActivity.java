package com.huashe.lockscreen.test;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;


import com.huashe.lockscreen.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class TestActivity extends AppCompatActivity {

    String TAG="eeeTestActivity";
    List<String> videoList;
    VideoView mVideoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);


         initView();

    }


    private void initView() {

        LoadViewFiles();


    }
    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;


    int CurrentVideoPosition=0;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("eeeTestActivity","执行了onDestroy方法");
    }

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


    @Override
    protected void onResume() {
        super.onResume();
//        if(mVideoView!=null&&!mVideoView.isPlaying()){
//            mVideoView.start();
//            Log.i("eeeTestActivity","执行了OnResume方法中的start方法");
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//
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


    //加载本地视频
    String path=  Environment.getExternalStorageDirectory().getPath()+"/pic";
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


    //初始化视频控件
    public void initVideoView(){

        if(mVideoView==null){
            mVideoView= findViewById(R.id.videoView_one);
            mVideoView.setVideoURI(Uri.parse(videoList.get(0)));
            mVideoView.requestFocus();
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
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
                            Toast.makeText(TestActivity.this, "向右滑", Toast.LENGTH_SHORT).show();
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




}


