package software.www.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Calendar;

/**
 * Created by Administrator on 2016/10/11.
 */

public class MyView extends SurfaceView implements SurfaceHolder.Callback,Runnable{

    private static final int DEFAULT_RADIUS = 200;

    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private Thread mThread;
    private boolean flag;

    private Paint mPaint;
    private Paint mPointPaint;

    private int mCanvasWidth,mCanvasHeight;
    private int mRadius = DEFAULT_RADIUS;
    private int mSecondPointerLength;
    private int mMinutePointerLength;
    private int mHourPointerLength;
    private int mHourDegreeLength;
    private int mSecondDegreeLength;

    private int mHour,mMinute,mSecond;

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mHour = Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        mMinute = Calendar.getInstance().get(Calendar.MINUTE);
        mSecond = Calendar.getInstance().get(Calendar.SECOND);

        mHolder = getHolder();
        mHolder.addCallback(this);
        mThread = new Thread(this);

        mPaint = new Paint();
        mPointPaint = new Paint();

        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mPointPaint.setColor(Color.BLACK);
        mPointPaint.setAntiAlias(true);
        mPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPointPaint.setTextSize(22);
        mPointPaint.setTextAlign(Paint.Align.CENTER);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs,0);
    }

    public MyView(Context context) {
        super(context,null);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        flag = true;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        flag = false;
    }

    @Override
    public void run() {
        long start,end;

        while (flag){
            start = System.currentTimeMillis();
            draw();
            logic();
            end = System.currentTimeMillis();

            try {
                if (end-start<1000)
                    Thread.sleep(1000-(end-start));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int desiredWidth,desiredHeight;
        if (widthMode == MeasureSpec.EXACTLY){
            desiredWidth = widthSize;
        }else {
            desiredWidth = mRadius*2 + getPaddingLeft() + getPaddingRight();
            if (widthMode == MeasureSpec.AT_MOST){
                desiredWidth = Math.min(widthSize,desiredWidth);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY){
            desiredHeight = heightSize;
        }else {
            desiredHeight = mRadius*2 + getPaddingTop() + getPaddingBottom();
            if (heightMode == MeasureSpec.AT_MOST){
                desiredHeight = Math.min(heightSize,desiredHeight);
            }
        }

        setMeasuredDimension(mCanvasWidth = desiredWidth + 4,mCanvasHeight = desiredHeight + 4);

        mRadius = (int)(Math.min(desiredWidth-getPaddingLeft()-getPaddingRight(),
                desiredHeight-getPaddingTop()-getPaddingBottom())*1.0f/2);
        calculateLengths();
    }

    private void calculateLengths() {

        mHourDegreeLength = (int)(mRadius * 1.0f/7) ;
        mSecondDegreeLength = (int)(mHourDegreeLength*1.0f)/2;

        mHourPointerLength = (int)(mRadius*1.0/2);
        mMinutePointerLength = (int)(mHourPointerLength*1.25f);
        mSecondPointerLength = (int)(mHourPointerLength*1.5f);
        Log.w("haha",mHourPointerLength+"!!!"+mMinutePointerLength+"!!!"+mSecondPointerLength+"");
    }

    /**
     * 逻辑操作
     */
    private void logic() {
        mSecond++;
        if (mSecond==60){
            mSecond = 0;
            mMinute++;
            if (mMinute==60){
                mMinute = 0;
                mHour++;
                if (mHour==24)
                    mHour = 0;
            }
        }
    }

    private void draw() {

        try {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas!=null){
                mCanvas.translate(mCanvasWidth*1.0f/2+getPaddingLeft()-getPaddingRight(),
                        mCanvasHeight*1.0f/2+getPaddingTop()-getPaddingBottom());

                mPaint.setStrokeWidth(2f);
                mCanvas.drawCircle(0,0,mRadius,mPaint);

                for (int i=0;i<12;i++){
                    mCanvas.drawLine(0,mRadius,0,mRadius - mHourDegreeLength,mPaint);
                    mCanvas.rotate(30);
                }

                mPaint.setStrokeWidth(1.5f);
                for (int i=0;i<60;i++){
                    if (i%5!=0){
                        mCanvas.drawLine(0,mRadius,0,mRadius-mSecondDegreeLength,mPaint);
                    }
                    mCanvas.rotate(6);
                }

                mPointPaint.setColor(Color.BLACK);
                for (int i=0;i<12;i++){
                    String number = 6+i<12 ? String.valueOf(6+i):6+i>12?String.valueOf(i-6):"12";
                    mCanvas.drawText(number,0,mRadius*5.5f/7,mPointPaint);
                    mCanvas.rotate(30);
                }

                mCanvas.drawText(mHour<12 ?"AM":"PM",0,mRadius*1.5f/4,mPointPaint);

                Path path = new Path();
                path.moveTo(0,0);
                int[] hourPointerCoordinates = getPointerCoordinates(mHourPointerLength);
                path.moveTo(hourPointerCoordinates[0],hourPointerCoordinates[1]);
                path.moveTo(hourPointerCoordinates[2],hourPointerCoordinates[3]);
                path.moveTo(hourPointerCoordinates[4],hourPointerCoordinates[5]);
                path.close();
                mCanvas.save();
                mCanvas.rotate(180+mHour%12*30+mMinute*1.0f/60*30);
                mCanvas.drawPath(path,mPointPaint);
                mCanvas.restore();

                path.reset();
                path.moveTo(0,0);
                int[] minutePointerCoordinates = getPointerCoordinates(mMinutePointerLength);
                path.moveTo(minutePointerCoordinates[0],minutePointerCoordinates[1]);
                path.moveTo(minutePointerCoordinates[2],minutePointerCoordinates[3]);
                path.moveTo(minutePointerCoordinates[4],minutePointerCoordinates[5]);
                path.close();
                mCanvas.save();
                mCanvas.rotate(180+mMinute*6);
                mCanvas.drawPath(path,mPointPaint);
                mCanvas.restore();

                mPointPaint.setColor(Color.RED);
                path.reset();
                path.moveTo(0,0);
                int[] secondPointerCoordinates = getPointerCoordinates(mSecondPointerLength);
                path.moveTo(secondPointerCoordinates[0],secondPointerCoordinates[1]);
                path.moveTo(secondPointerCoordinates[2],secondPointerCoordinates[3]);
                path.moveTo(secondPointerCoordinates[4],secondPointerCoordinates[5]);
                path.close();
                mCanvas.save();
                mCanvas.rotate(180+mSecond*6);
                mCanvas.drawPath(path,mPointPaint);
                mCanvas.restore();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (mCanvas==null){
                //提交画布，否则什么都看不见
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    /**
     * 获取指针坐标
     * @param pointerLength
     * @return
     */
    private int[] getPointerCoordinates(int pointerLength) {
        int y = (int)(pointerLength*3.0f/4);
        int x = (int)(y*Math.tan(Math.PI/180*5));
        return new int[]{-x,y,0,pointerLength,x,y};
    }
}
