package com.dell.fortune.touchripple;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class RippleButton extends android.support.v7.widget.AppCompatButton {
    private TouchRippleDrawable rippleDrawable;

    public RippleButton(Context context) {
        this(context, null);
    }

    public RippleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        rippleDrawable = new TouchRippleDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.temp));
        //设置刷新接口,在View已经实现，
//        rippleDrawable.setCallback(this);

        //如果直接设置setBackgroundDrawable可以去掉on，verifyDrawable，onSizeChange,setCallback方法
        setBackgroundDrawable(rippleDrawable);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        rippleDrawable.draw(canvas);
        super.onDraw(canvas);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        rippleDrawable.onTouch(event);
        return super.onTouchEvent(event);
    }

    //    @Override
//    protected boolean verifyDrawable(@NonNull Drawable who) {
//        //验证
//        return who == rippleDrawable || super.verifyDrawable(who);
//    }

//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//        //设置Drawable绘制和刷新的区域
//        rippleDrawable.setBounds(0, 0, getWidth(), getHeight());
//    }


}
