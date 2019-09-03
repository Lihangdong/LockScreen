package com.huashe.lockscreen.video;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by steven on 2017/9/29.
 */

public class MVideoView extends VideoView
{
    public MVideoView(Context context)
    {
        super(context);
    }

    public MVideoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public MVideoView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getDefaultSize(getWidth(), widthMeasureSpec);
        int height = getDefaultSize(getHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
