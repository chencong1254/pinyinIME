package com.hht.pinyinime;



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DragView extends View {
	
	private static final int MOVE_TOLERANCE = 3;

	/**
	 * The x coordination of the last touch event. 最后触摸事件的x坐标。
	 */
	private int mXLast;

	/**
	 * The y coordination of the last touch event. 最后触摸事件的 y坐标。
	 */
	private int mYLast;
	
	private OnEventListener mOnEventListener;
	
	private Paint mPaint;
	private float mScale;
	private int mWidth;
    private int mHeight;
    private PointF mCenter,mAbsoluteCenter;
    private float mRadius;
    private boolean mMove = false;
	
	public static interface OnEventListener{
        public void onTouchMove(float x, float y);
    }
	
	public void setOnEventListener(OnEventListener l){
		mOnEventListener = l;
	}
	
	public OnEventListener getOnEventListener(){
		return mOnEventListener;
	}

	public DragView(Context context) {
		super(context);
		init(context);
	}
	
	public DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
	
	private void init(Context context){
		mScale = context.getResources().getDisplayMetrics().density;
        mHeight = context.getResources().getDisplayMetrics().heightPixels;
        mWidth = context.getResources().getDisplayMetrics().widthPixels;
		mPaint = new Paint();
		mPaint.setColor(Color.BLUE);
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(dip2px(2));
	}
	
	/**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(float dpValue) {
        return (int)(dpValue * mScale + 0.5f);
    }
    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    private int px2dip(float pxValue) {
        return (int)(pxValue / mScale + 0.5f);
    }
    
    private float calculateDistance(PointF p1,PointF p2){
        return (float)Math.sqrt((p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        
        int x = (int) event.getRawX();
		int y = (int) event.getRawY();
		
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				mXLast = x;
				mYLast = y;
				return true;
			}

			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				if ((Math.abs(x - mXLast) > MOVE_TOLERANCE
						|| Math.abs(y - mYLast) > MOVE_TOLERANCE) ) {
					int offX = x-mXLast;
					int offY = y-mYLast;
					mXLast = x;
					mYLast = y;
					if(mOnEventListener != null){
						mOnEventListener.onTouchMove(offX, offY);
					}
				}
				return true;
			}
		
		if(event.getAction() == MotionEvent.ACTION_UP){
			return true;
		}
		
        return true;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawColor(this.getResources().getColor(R.color.lightgrey));
        /*
        if(mCenter == null){
        	mCenter = new PointF(this.getWidth()/4*3, this.getHeight()/2);
    		mRadius = this.getWidth()/4;
        }
        
        canvas.drawCircle(this.getWidth()/4*3, this.getHeight()/2, this.getWidth()/4, mPaint);
        canvas.drawLine(this.getWidth()/4*3 - this.getWidth()/4, this.getHeight()/2,
        		this.getWidth()/4*3 + this.getWidth()/4, this.getHeight()/2, mPaint);
        canvas.drawLine(this.getWidth()/4*3, this.getHeight()/2 - this.getWidth()/4 , 
        		this.getWidth()/4*3, this.getHeight()/2 + this.getWidth()/4, mPaint);*/
    }

}
