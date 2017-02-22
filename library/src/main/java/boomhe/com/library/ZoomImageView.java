package boomhe.com.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

public class ZoomImageView extends ImageView implements ScaleGestureDetector.OnScaleGestureListener,View.OnTouchListener,ViewTreeObserver.OnGlobalLayoutListener{

    public static final String Tag = ZoomImageView.class.getSimpleName();

    public boolean once = true;

    float initScale = 1.0f;
    public static final float SCALE_MAX = 4.0f;
    public static final float SCALE_MID = 2.0f;

    private final float[] matrixValues = new float[9];


    /**
     * 缩放的手势检测
     */
    private ScaleGestureDetector mScaleGestureDetector;
    private Matrix mScaleMatrix = new Matrix();
    /**
     *用于双击检测
     */
    private GestureDetector mGestureDetector;
    private boolean isAutoScale;

    private float mLastX;
    private float mLastY;

    private int lastPointerCount;

    private boolean isCheckTopAndBottom = true;
    private boolean isCheckLeftAndRight = true;

    public ZoomImageView(Context context) {
        this(context,null,0);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setScaleType(ScaleType.MATRIX);

        /**
         * 双击事件
         */
        mGestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {

                if (isAutoScale == true){
                    return true;
                }
                float x = e.getX();
                float y = e.getY();

                if (getScale() < SCALE_MID){
                    ZoomImageView.this.postDelayed(new AutoScaleRunnable(SCALE_MID,x,y),16);
                    isAutoScale = true;
                }else if (getScale() >= SCALE_MID && getScale() < SCALE_MAX){
                    ZoomImageView.this.postDelayed(new AutoScaleRunnable(SCALE_MAX,x,y),16);
                    isAutoScale = true;
                }else {
                    ZoomImageView.this.postDelayed(new AutoScaleRunnable(initScale,x,y),16);
                    isAutoScale = true;
                }
                return true;
            }
        });
        mScaleGestureDetector = new ScaleGestureDetector(context,this);
        this.setOnTouchListener(this);
    }

    /**
     * 自动缩放
     */
    private class AutoScaleRunnable implements Runnable{

        static final float BIGGER = 1.07f;
        static final float SMALLER = 0.93f;
        private float mTargetScale;
        private float tmpScale;
        /**
         * 缩放中心
         */
        private float x;
        private float y;

        /**
         *
         */
        public AutoScaleRunnable(float targetScale,float x,float y){
            this.mTargetScale = targetScale;
            this.x = x;
            this.y = y;
            if (getScale() < mTargetScale){
                tmpScale = BIGGER;
            }else {
                tmpScale = SMALLER;
            }
        }
        @Override
        public void run() {

            mScaleMatrix.postScale(tmpScale,tmpScale,x,y);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);

            final float currentScale = getScale();
            Log.e(Tag,"CurrentScale:"+ getScale());
            /**
             * ???
             */
            if (((tmpScale > 1f) && (currentScale < mTargetScale))
                    || ((tmpScale < 1f) && (mTargetScale < currentScale))){
                ZoomImageView.this.postDelayed(this,16);
            }else {
                final float deltaScale = mTargetScale / currentScale;
                mScaleMatrix.postScale(deltaScale,deltaScale,x,y);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                isAutoScale = false;
            }
        }
    }

    private void checkBorderAndCenterWhenScale(){
        RectF rectF = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        /**
         *如果宽或高大于屏幕，则控制范围
         */
        if (rectF.width() >= width){
            if (rectF.left > 0){
                deltaX = -rectF.left;
            }
            if (rectF.right < width){
                deltaX = width -rectF.right;
            }
        }
        if (rectF.height() >= height){
            if (rectF.top > 0){
                deltaY = -rectF.top;
            }
            if (rectF.bottom < height){
                deltaY = height - rectF.bottom;
            }
        }
        /**
         *如果宽或高小于屏幕，则让其居中；
         */
        if (rectF.width() < width){
            deltaX = width * 0.5f - rectF.right + 0.5f * rectF.width();

        }
        if (rectF.height() < height){
            deltaY = height * 0.5f - rectF.bottom + 0.5f * rectF.height();
        }
        Log.e(Tag,"deltaX = "+ deltaX+ "delaY = " + deltaY);
        mScaleMatrix.postTranslate(deltaX,deltaY);
    }

    /**
     *根据当前图片的Matrix获得图片的范围
     */
    private RectF getMatrixRectF(){
        Matrix matrix = mScaleMatrix;
        RectF rect = new RectF();
        Drawable drawable = getDrawable();
        if (drawable != null){
            rect.set(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }

    /**
     * 获得当前的缩放比例
     */
    public final float getScale(){
        mScaleMatrix.getValues(matrixValues);//复制矩阵的值到数组中
        return matrixValues[Matrix.MSCALE_X];
    }

    /**
     * OnScaleGestureListnener 方法
     * @param scaleGestureDetector
     */
    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

        float scale = getScale();
        float scaleFactor = scaleGestureDetector.getScaleFactor();

        if (getDrawable() == null){
            return true;
        }

        if ((scale < SCALE_MAX && scaleFactor >1.0f) || (scale > initScale && scaleFactor < 1.0f)){
            /**
             *
             */
            if (scaleFactor * scale < initScale){
                scaleFactor = initScale / scale;
            }
            if (scaleFactor * scale > SCALE_MAX){
                scaleFactor = SCALE_MAX / scale;
            }
            mScaleMatrix.postScale(scaleFactor,scaleFactor,
                    scaleGestureDetector.getFocusX(),scaleGestureDetector.getFocusY());
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);
        }
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

    }

    /**
     * OnTouchListener 方法
     * @param view
     * @param motionEvent
     */

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mGestureDetector.onTouchEvent(motionEvent)){
            return true;
        }
        mScaleGestureDetector.onTouchEvent(motionEvent);
        float x = 0,y =0;
        final int pointerCount = motionEvent.getPointerCount();
        for (int i = 0; i < pointerCount; i++){
            x += motionEvent.getX(i);
            y += motionEvent.getY(i);
        }
        x = x/ pointerCount;
        y = y/ pointerCount;

        /**
         *
         */
        if (pointerCount != lastPointerCount){
            mLastX = x;
            mLastY = y;
        }
        lastPointerCount = pointerCount;
        RectF rectF = getMatrixRectF();
        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                if ( rectF.width() > getWidth() || rectF.height() > getHeight()){
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (rectF.width() > getWidth() || rectF.height() > getHeight()){
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                float dx = x - mLastX;
                float dy = y - mLastY;

                if (getDrawable() != null){
                    isCheckLeftAndRight = isCheckTopAndBottom = true;

                    if (rectF.width() < getWidth()){
                        dx = 0;
                        isCheckLeftAndRight = false;
                    }

                    if (rectF.height() < getHeight()){
                        dy = 0;
                        isCheckTopAndBottom = false;
                    }
                    mScaleMatrix.postTranslate(dx,dy);
                    checkMatrixBounds();
                    setImageMatrix(mScaleMatrix);
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                lastPointerCount = 0;
                break;
        }
        return true;

    }



    /**
     * OnGlobalLayoutListener 方法
     */
    @Override
    public void onGlobalLayout() {
        if (once){
            Drawable drawable = getDrawable();
            if (drawable == null){
                return;
            }
            /**
             * 控件的宽高
             */
            int width = getWidth();
            int height = getHeight();
            /**
             * 图片的宽高
             */
            int dw = drawable.getIntrinsicWidth();
            int dh = drawable.getIntrinsicHeight();

            float scale = 1.0f;
            if (dw > width && dh < height){
                scale = width * 1.0f / dw;
            }
            if (dw < width && dh > height){
                scale = height * 1.0f / dh;
            }

            if ((dw > width && dh > height)||(dw < width && dh < height)){
                scale = Math.min(width * 1.0f/ dw, height * 1.0f / dh);
            }


            initScale = scale;
            mScaleMatrix.postTranslate((width - dw)/2 ,(height - dh)/2);
            mScaleMatrix.postScale(scale,scale,getWidth()/2,getHeight()/2);
            setImageMatrix(mScaleMatrix);
            once = false;
        }
    }

    /**
     *
     */
    private void checkMatrixBounds(){
        RectF rect = getMatrixRectF();

        float deltaX = 0, deltaY = 0;
        final float viewWidth = getWidth();
        final float viewHeight = getHeight();

        if (rect.top > 0 && isCheckTopAndBottom){
            deltaY = -rect.top;
        }
        if (rect.bottom < viewHeight && isCheckTopAndBottom){
            deltaY = viewHeight - rect.bottom;
        }

        if (rect.left > 0 && isCheckLeftAndRight){
            deltaX = -rect.left;
        }
        if (rect.right < viewWidth && isCheckLeftAndRight){
            deltaX = viewWidth - rect.right;
        }
        mScaleMatrix.postTranslate(deltaX,deltaY);
    }


    /**
     * 注册以及移除OnGlobalLayoutListener
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }
}