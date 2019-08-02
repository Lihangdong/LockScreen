package com.huashe.lockscreen.View;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.POWER_SERVICE;

/**
 * Description: 锁屏工具类
 * created by Jamil
 * Time: 2019/7/19
 **/
public class LockUtil {

    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    DevicePolicyManager policyManager;
    ComponentName adminReceiver;

    private static Activity mActivity;

    private static LockUtil mLockUtil;

    public static synchronized LockUtil getInstance(Activity activity) {
        if(mLockUtil==null){
            mLockUtil=new LockUtil();
            mActivity=activity;
        }
        return mLockUtil;
    }


    public  void initLock(){
        if(policyManager==null||!policyManager.isAdminActive(adminReceiver)){//没有激活这行下面的代码
            mPowerManager = (PowerManager) mActivity.getSystemService(POWER_SERVICE);
            policyManager = (DevicePolicyManager) mActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            adminReceiver = new ComponentName(mActivity, ScreenOffAdminReceiver.class);
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,  adminReceiver);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"开启后就可以使用锁屏功能了...");//显示位置见图二
            mActivity.startActivityForResult(intent, 0);
        }

    }


    public  void doLockScreen(){
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (admin) {
            policyManager.lockNow();
             handler.sendEmptyMessageDelayed(1,3000);  //这个是3秒后的自动亮屏
        } else {
            Toast.makeText(mActivity,"没有设备管理权限",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 检测用户是否开启了超级管理员
     */
    public void isOpen() {
        if(policyManager.isAdminActive(adminReceiver)){//判断超级管理员是否激活
            Toast.makeText(mActivity,"设备已被激活",
                    Toast.LENGTH_LONG).show();
        }else{

            Toast.makeText(mActivity,"设备没有被激活",
                    Toast.LENGTH_LONG).show();
        }
    }


    public void turnOnScreen() {
        // turn on screen
        Log.v("ProximityActivity", "ON!");
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag:");
        mWakeLock.acquire();
        mWakeLock.release();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    turnOnScreen();
                    break;
                case 2:

                    break;
            }
        }
    };

    public void destroy(){
        adminReceiver=null;
        mPowerManager=null;
        policyManager=null;
        mLockUtil=null;
        mActivity=null;
        handler.removeCallbacksAndMessages(null);
    }

}
