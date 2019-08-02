package com.huashe.lockscreen;

import android.content.Context;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.youth.banner.loader.ImageLoader;

/**
 * Description:
 * created by Jamil
 * Time: 2019/5/17
 **/
//自定义的图片加载器
public class MyLoader extends ImageLoader {
    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        if(path instanceof Integer){
            Glide.with(context).load(path).into(imageView);
        }else{
            Glide.with(context).load((String) path).into(imageView);
        }

    }
}