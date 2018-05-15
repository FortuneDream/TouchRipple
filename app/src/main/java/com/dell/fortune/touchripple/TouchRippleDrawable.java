package com.dell.fortune.touchripple;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

//退出动画有两种
// 1.点击后快速释放：进入动画完成后再执行退出动画
// 2.点击后长时间按住：进入动画完成后，当用户抬起手指时才执行退出动画
//透明度的问题目前还没有解决
public class TouchRippleDrawable extends Drawable {
    private int mAlpha = 100;
    private int mRippleColor = 0;
    private Paint mPaint;
    //圆心坐标以及半径
    private float mRipplePointX = 0;
    private float mRipplePointY = 0;
    private float mRippleRadius = 200;
    private Bitmap mBitmap;
    //按下时的点击点
    private float mDonePointX, mDonePointY;
    //控件的中心区域点
    private float mCenterPointX, mCenterPointY;
    //开始和结束的半径
    private float mStartRadius, mEndRadius;
    private int mBackgroundColor;
    private int mPaintAlpha = 255;


    private float mEnterProgress = 0;
    //每次递增的进度值
    private float mEnterIncrement = 16f / 360;
    private Interpolator mEnterInterpolator = new DecelerateInterpolator(2);//先快后慢差值器；2表示变化速率
    private Runnable mEnterRunnable = new Runnable() {
        @Override
        public void run() {
            mEnterProgress = mEnterProgress + mEnterIncrement;//百分比
            if (mEnterProgress > 1) {
                onEnterProgress(1);
                onEnterDone();
                return;//终点
            }
            float realProgress = mEnterInterpolator.getInterpolation(mEnterProgress);//传入百分比,返回真实进度值
            onEnterProgress(realProgress);

            scheduleSelf(this, SystemClock.uptimeMillis() + 16);//延迟16ms，刷新频率为60FPS
        }
    };

    //退出动画进度值
    private float mExitProgress = 0;
    //每次递增的进度值
    private float mExitIncrement = 16f / 280;
    private Interpolator mExitInterpolator = new AccelerateInterpolator(2);//先慢后快差值器；2表示变化速率
    private Runnable mExitRunnable = new Runnable() {
        @Override
        public void run() {
            mExitProgress += mExitIncrement;//百分比
            if (mExitProgress > 1) {
                onExitProgress(1);//如果没有设置为1的话，最后的progress没有到1
                onExitDone();
                return;//终点
            }
            float realProgress = mExitInterpolator.getInterpolation(mExitProgress);//传入百分比,返回真实进度值
            onExitProgress(realProgress);
            scheduleSelf(this, SystemClock.uptimeMillis() + 16);//延迟16ms，刷新频率为60FPS
        }
    };


    //用户的手指是否抬起
    private boolean mTouchRelease;
    //进入动画完成
    private boolean mEnterDone;

    /**
     * 退出刷新方法
     *
     * @param progress 进度值0-1
     */
    //背景减淡
    private void onExitProgress(float progress) {
        int alpha = (int) getProgressValue(255, 0, progress);
        mBackgroundColor = changeColorAlpha(0x30000000, alpha);
        mPaintAlpha = (int) getProgressValue(255, 0, progress);
        Log.e("TAG", "onExitProgress" + String.valueOf(progress));
        invalidateSelf();
    }


    /**
     * 进入动画刷新方法
     *
     * @param progress 进度值 0-1
     */
    private void onEnterProgress(float progress) {
        mRippleRadius = 360 * progress;
        mRippleRadius = getProgressValue(mStartRadius, mEndRadius, progress);
        mRipplePointX = getProgressValue(mDonePointX, mCenterPointX, progress);
        mRipplePointY = getProgressValue(mDonePointY, mCenterPointY, progress);
        int alph = (int) getProgressValue(0, 255, progress);
        mBackgroundColor = changeColorAlpha(0x30000000, alph);//背景逐渐加深,透明度极限30
        invalidateSelf();
    }


    private float getProgressValue(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    public TouchRippleDrawable(Bitmap bitmap) {
        this.mBitmap = bitmap;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        setRippleColor(Color.RED);
        //第一个是保留的颜色（红色），第二个表示填充的颜色，
//        setColorFilter(new LightingColorFilter(0xFFFF0000,0x00330000));
    }

    public void setRippleColor(int color) {
        this.mRippleColor = color;
        onColorOrAlphaChange();
    }

    //把具体操作转移到Drawable中，动态更新Drawable
    public void onTouch(MotionEvent event) {
        //判断点击操作类型
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                onTouchUp(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_CANCEL:
                onTouchCancel(event.getX(), event.getY());
                break;
        }
    }

    //当进入动画完成时启动退出动画
    private void onTouchCancel(float x, float y) {
        mTouchRelease = true;//抬起
        //启动退出动画
        if (mEnterDone) {
            startExitRunnable();
        }
    }

    private void onTouchUp(float x, float y) {
//        unscheduleSelf(mEnterRunnable);//抬起时去掉。但如果没有在onTouchEvent用返回true或者Button.setOnclickListener的话，并不能触发Action__Up
        mTouchRelease = true;//抬起
        //当进入动画完成时启动退出动画
        if (mEnterDone) {
            startExitRunnable();
        }
    }

    private void onTouchMove(float x, float y) {
    }


    private void onTouchDown(float x, float y) {
        mDonePointX = x;
        mDonePointY = y;
        invalidateSelf();//需要设置Callback（View已经实现了），重写以及verifyDrawable,onSizeChanged
        mTouchRelease = false;//按下
        startEnterRunnable();
    }

    /**
     * 启动进入的方法
     */
    private void startEnterRunnable() {
        mPaintAlpha = 255;
        mEnterProgress = 0;
        mEnterDone = false;
        unscheduleSelf(mEnterRunnable);//先取消一次，否则每次点击会，加载越来越快（因为每次点击都会开一个线程）
        scheduleSelf(mEnterRunnable, SystemClock.uptimeMillis());//事件队列，使用Drawable所依附的View的已经实现Callback接口，在Callback中使用handler机制发送任务
    }

    /**
     * 启动退出动画
     */
    private void startExitRunnable() {
        mExitProgress = 0;
        unscheduleSelf(mEnterRunnable);//取消进入动画
        unscheduleSelf(mExitRunnable);//先取消一次，否则每次点击会，加载越来越快（因为每次点击都会开一个线程）
        scheduleSelf(mExitRunnable, SystemClock.uptimeMillis());//事件队列，使用Drawable所依附的View的已经实现Callback接口，在Callback中使用handler机制发送任务
    }

    /**
     * 进入动画完成
     */
    private void onEnterDone() {
        Log.e("TAG", "onEnterDone");
        mEnterDone = true;
        //用户手放开时，启动退出动画
        if (mTouchRelease) {
            startExitRunnable();
        }
    }

    /**
     * 退出动画完成
     */
    private void onExitDone() {
        Log.e("TAG", "onExitDone");
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mCenterPointX = bounds.centerX();//得到中心点位置
        mCenterPointY = bounds.centerY();

        //最大半径,有两个方向，横向和纵向
        float maxRadius = Math.max(mCenterPointX, mCenterPointY);
        mStartRadius = maxRadius * 0f;
        mEndRadius = maxRadius * 0.8f;
    }

    //super.draw(canvas)；里面有绘制文字的操作
    //
    @Override
    public void draw(@NonNull Canvas canvas) {
        //绘制背景区域
        canvas.drawColor(mBackgroundColor);
        mPaint.setAlpha(mPaintAlpha);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        canvas.drawCircle(mRipplePointX, mRipplePointY, mRippleRadius, mPaint);

    }

    //更改颜色透明度方法
    private int changeColorAlpha(int color, int alpha) {
        int a = (color >> 24) & 0xff;
        a = (int) (a * (alpha / 255f));
        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = (color) & 0xff;
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void setAlpha(int alpha) {
        this.mAlpha = alpha;
        onColorOrAlphaChange();
    }

    public int getAlpha() {
        return mAlpha;
    }

    //目标Alpha=原Alpha/255 * 修改Alpha(固定公式)
    //透明度的改变导致颜色的改变，最终的mPaint.Color!=mRippleColor
    private void onColorOrAlphaChange() {
        mPaint.setColor(mRippleColor);
        if (mAlpha != 255) {//不是全透明
            int pAlpha = mPaint.getAlpha();
            int realAlpha = (int) (pAlpha * (mAlpha / 255f));
            mPaint.setAlpha(realAlpha);
        }
        invalidateSelf();
    }


    //颜色滤镜,一般针对图片，颜色替换
    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        int alpha = mPaint.getAlpha();
        if (alpha == 255) {//不透明
            return PixelFormat.OPAQUE;
        } else if (alpha == 0) {//全透明
            return PixelFormat.TRANSPARENT;
        } else {//半透明
            return PixelFormat.TRANSLUCENT;
        }

    }
}
