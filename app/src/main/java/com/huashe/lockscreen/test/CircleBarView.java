package com.huashe.lockscreen.test;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import com.huashe.lockscreen.R;
import com.huashe.lockscreen.util.Utils;

/**
 * Description:
 * created by Jamil
 * Time: 2019/7/30
 **/
public class CircleBarView extends View {

    private Paint progressPaint;//绘制圆弧的画笔

    private Paint bgPaint;//绘制背景圆弧的画笔



    private float progressSweepAngle;//进度条圆弧扫过的角度
    private float startAngle;//背景圆弧的起始角度
    private float sweepAngle;//背景圆弧扫过的角度

    private RectF mRectF;//绘制圆弧的矩形区域

    private float barWidth;//圆弧进度条宽度
    private int defaultSize;//自定义View默认的宽高

    private int progressColor;//进度条圆弧颜色
    private int bgColor;//背景圆弧颜色
    private float maxNum;//进度条最大值
    private float progressNum;//可以更新的进度条数值
    CircleBarAnim  anim;
    public CircleBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs){



        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.STROKE);//只描边，不填充
        progressPaint.setColor(Color.GREEN);
        progressPaint.setAntiAlias(true);//设置抗锯齿


        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.STROKE);//只描边，不填充
        bgPaint.setColor(Color.GRAY);
        bgPaint.setAntiAlias(true);//设置抗锯齿




        mRectF = new RectF();
        anim = new CircleBarAnim();

        defaultSize = Utils.dip2px(context,100);
        barWidth = Utils.dip2px(context,10);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleBarView);

        progressColor = typedArray.getColor(R.styleable.CircleBarView_progress_color,Color.GREEN);//默认为绿色
        bgColor = typedArray.getColor(R.styleable.CircleBarView_bg_color,Color.GRAY);//默认为灰色
        startAngle = typedArray.getFloat(R.styleable.CircleBarView_start_angle,0);//默认为0
        sweepAngle = typedArray.getFloat(R.styleable.CircleBarView_sweep_angle,360);//默认为360
        barWidth = typedArray.getDimension(R.styleable.CircleBarView_bar_width,Utils.dip2px(context,10));//默认为10dp
        typedArray.recycle();//typedArray用完之后需要回收，防止内存泄漏


        progressPaint.setColor(progressColor);
        progressPaint.setStrokeWidth(barWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);


        bgPaint.setColor(bgColor);
        bgPaint.setStrokeWidth(barWidth);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);

        progressNum = 0;
        maxNum=100;

    }

    private int measureSize(int defaultSize,int measureSpec) {
        int result = defaultSize;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = measureSize(defaultSize, heightMeasureSpec);
        int width = measureSize(defaultSize, widthMeasureSpec);
        int min = Math.min(width, height);// 获取View最短边的长度
        setMeasuredDimension(min, min);// 强制改View为以最短边为长度的正方形

        if(min >= barWidth*2){//这里简单限制了圆弧的最大宽度
            mRectF.set(barWidth/2,barWidth/2,min-barWidth/2,min-barWidth/2);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawArc(mRectF,startAngle,sweepAngle,false,bgPaint);
        canvas.drawArc(mRectF,startAngle,progressSweepAngle,false, progressPaint);


    }

    public class CircleBarAnim extends Animation {

        public CircleBarAnim(){
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            //sweepAngle = interpolatedTime * 270;
            progressSweepAngle = interpolatedTime * sweepAngle * progressNum / maxNum;//这里计算进度条的比例
            if(textView !=null && onAnimationListener!=null){
                textView.setText(onAnimationListener.howToChangeText(interpolatedTime,progressNum,maxNum));
            }
            postInvalidate();
        }
    }

    //写个方法给外部调用，用来设置动画时间
    public void setProgressNum(float progressNum, int time) {
        anim.setDuration(time);
        this.startAnimation(anim);
        this.progressNum = progressNum;
    }



    TextView textView;
    private OnAnimationListener onAnimationListener;
    /**
     * 设置显示文字的TextView
     * @param textView
     */
    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public interface OnAnimationListener {
        /**
         * 如何处理要显示的文字内容
         * @param interpolatedTime 从0渐变成1,到1时结束动画
         * @param progressNum 进度条数值
         * @param maxNum 进度条最大值
         * @return
         */
        String howToChangeText(float interpolatedTime, float progressNum, float maxNum);
    }


    public void setOnAnimationListener(OnAnimationListener onAnimationListener) {
        this.onAnimationListener = onAnimationListener;
    }
}
