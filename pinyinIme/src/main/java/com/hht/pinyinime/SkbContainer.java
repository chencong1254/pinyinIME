/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hht.pinyinime;

import java.io.IOException;

import com.hht.pinyinime.DragView.OnEventListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

/**
 * The top container to host soft keyboard view(s). 软键盘View的集装箱，主持一个软件盘View。
 */
public class SkbContainer extends RelativeLayout implements OnTouchListener {
	/**
	 * For finger touch, user tends to press the bottom part of the target key,
	 * or he/she even presses the area out of it, so it is necessary to make a
	 * simple bias correction. If the input method runs on emulator, no bias
	 * correction will be used. 对于手指触摸点的Y坐标进行的偏好 。
	 */
	private static final int Y_BIAS_CORRECTION = -10;

	/**
	 * Used to skip these move events whose position is too close to the
	 * previous touch events. 触摸移动的x坐标和y坐标的差值如果在MOVE_TOLERANCE之内，触摸的移动事件就被抛弃。
	 */
	private static final int MOVE_TOLERANCE = 5;

	/**
	 * If this member is true, PopupWindow is used to show on-key highlight
	 * effect. 弹出框是否用于重点显示效果。
	 */
	private static boolean POPUPWINDOW_FOR_PRESSED_UI = false;

	/**
	 * The current soft keyboard layout. 当前的软键盘布局文件资源ID。
	 * 
	 * @see com.android.inputmethod.pinyin.InputModeSwitcher for detailed layout
	 *      definitions.
	 * 
	 * 
	 */
	private int mSkbLayout = 0;
	
	private int mSkbScale = 1;
	private int mSkbWidthScale = 1;
	
	private int mIMEHeight,mIMEWidth;
	private int mWeight;
	
	private DragView mDragView,mDragView2;

	/**
	 * The input method service. 输入法服务
	 */
	private InputMethodService mService;

	/**
	 * Input mode switcher used to switch between different modes like Chinese,
	 * English, etc. 输入法变换器
	 */
	private InputModeSwitcher mInputModeSwitcher;

	/**
	 * The gesture detector. 手势识别
	 */
	private GestureDetector mGestureDetector;

	private Environment mEnvironment;

	/**
	 * view切换管理
	 */
	private ViewFlipper mSkbFlipper;

	/**
	 * The popup balloon hint for key press/release. 气泡
	 */
	private BalloonHint mBalloonPopup;

	/**
	 * The on-key balloon hint for key press/release. 气泡
	 */
	private BalloonHint mBalloonOnKey = null;

	/** The major sub soft keyboard. 主要视图：软键盘视图。 */
	private SoftKeyboardView mMajorView;

	/**
	 * The last parameter when function {@link #toggleCandidateMode(boolean)}
	 * was called. 最后的候选词显示。
	 */
	private boolean mLastCandidatesShowing;

	/**
	 * Used to indicate whether a popup soft keyboard is shown. 一个弹出副的软件盘是否在显示 ？
	 */
	private boolean mPopupSkbShow = false;

	/**
	 * Used to indicate whether a popup soft keyboard is just shown, and waits
	 * for the touch event to release. After the release, the popup window can
	 * response to touch events.
	 * 是否一个副软键盘弹出框正在显示，并且等待触摸事件释放，触摸事件释放之后，副软键盘可以响应触摸事件？
	 **/
	private boolean mPopupSkbNoResponse = false;

	/** Popup sub keyboard. 副软键盘弹出框 */
	private PopupWindow mPopupSkb;

	/** The view of the popup sub soft keyboard. 副软键盘弹出框中的软键盘视图 */
	private SoftKeyboardView mPopupSkbView;

	private int mPopupX;

	private int mPopupY;

	/**
	 * When user presses a key, a timer is started, when it times out, it is
	 * necessary to detect whether user still holds the key.
	 * 当用户按下一个按键，一个定时器启动，定时器时间到的时候，需要检查用户是否还按住这个键。
	 */
	private volatile boolean mWaitForTouchUp = false;

	/**
	 * When user drags on the soft keyboard and the distance is enough, this
	 * drag will be recognized as a gesture and a gesture-based action will be
	 * taken, in this situation, ignore the consequent events.
	 * 当用户在键盘上拖拽足够的距离后，是否忽略随之而生的事件？
	 */
	private volatile boolean mDiscardEvent = false;

	/**
	 * For finger touch, user tends to press the bottom part of the target key,
	 * or he/she even presses the area out of it, so it is necessary to make a
	 * simple bias correction in Y. 对用户点击的触摸点进行Y坐标的纠正。
	 */
	private int mYBiasCorrection = 0;

	/**
	 * The x coordination of the last touch event. 最后触摸事件的x坐标。
	 */
	private int mXLast;

	/**
	 * The y coordination of the last touch event. 最后触摸事件的 y坐标。
	 */
	private int mYLast;

	/**
	 * The soft keyboard view. 软键盘视图
	 */
	private SoftKeyboardView mSkv;

	private CandidatesContainer mCandidatesContainer;

	/**
	 * The position of the soft keyboard view in the container. 软键盘视图的集装箱的位置
	 */
	private int mSkvPosInContainer[] = new int[2];

	/**
	 * The key pressed by user.用户按下的按键
	 */
	private SoftKey mSoftKeyDown = null;
	private SoftKey mSaveSoftKeyDown;

	private  boolean isLanguageEnglish = true;

	/**
	 * Used to timeout a press if user holds the key for a long time. 长按定时器
	 */
	private LongPressTimer mLongPressTimer;
	Context mContext;
	/**
	 * For temporary use. 临时使用
	 */
	private int mXyPosTmp[] = new int[2];
	
	private OnEventListener mOnEventListener;
	
	/*public static interface OnEventListener{
        public void onTouchMove(float x, float y);
    }*/
	
	public void setOnEventListener(OnEventListener l){
		mOnEventListener = l;
		if(mDragView!=null)
		mDragView.setOnEventListener(l);
	}
	
	public void setIMEWH(int width, int height){
		mIMEWidth = width;
		mIMEHeight = height;
	}
	
	public void setWeight(int weight){
		mWeight = weight;
	}
	

	public SkbContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i("carlIME","SkbContainer...");
		mContext = context;
		mEnvironment = Environment.getInstance();

		mLongPressTimer = new LongPressTimer(this);

		// If it runs on an emulator, no bias correction
		// if ("1".equals(SystemProperties.get("ro.kernel.qemu"))) {
		// mYBiasCorrection = 0;
		// } else {
		mYBiasCorrection = Y_BIAS_CORRECTION;
		// }

		// 创建弹出气泡
		mBalloonPopup = new BalloonHint(context, this, MeasureSpec.AT_MOST);
		if (POPUPWINDOW_FOR_PRESSED_UI) {
			mBalloonOnKey = new BalloonHint(context, this, MeasureSpec.AT_MOST);
		}

		// 常见弹出软键盘
		mPopupSkb = new PopupWindow(mContext);
		mPopupSkb.setBackgroundDrawable(null);
		mPopupSkb.setClippingEnabled(false);
		
		mIMEWidth = this.getResources().getDisplayMetrics().widthPixels/2;
		mIMEHeight = this.getResources().getDisplayMetrics().heightPixels/3;
		mWeight=9;

		mCandidatesContainer = (CandidatesContainer) ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.candidates_container, null);
	}

	public void setService(InputMethodService service) {
		mService = service;
	}

	public void setInputModeSwitcher(InputModeSwitcher inputModeSwitcher) {
		mInputModeSwitcher = inputModeSwitcher;
	}

	public void setGestureDetector(GestureDetector gestureDetector) {
		mGestureDetector = gestureDetector;
	}

	// TODO 这个函数用来做什么？
	public boolean isCurrentSkbSticky() {
		if (null == mMajorView)
			return true;
		SoftKeyboard skb = mMajorView.getSoftKeyboard();
		if (null != skb) {
			return skb.getStickyFlag();
		}
		return true;
	}

	/**
	 * 切换候选词模式。逻辑简介：先从mInputModeSwitcher输入法模式交换器中获得中文候选词模式状态，然后判断是否是要切入候选词模式，
	 * 如果是键盘就变为中文候选词模式状态
	 * ，如果不是，键盘就消除中文候选词模式状态，变为mInputModeSwitcher中的mToggleStates设置键盘的状态。
	 * 
	 * @param candidatesShowing
	 */
	public void toggleCandidateMode(boolean candidatesShowing) {
		Log.i("carlIME","toggleCandidateMode... candidatesShowing:"+candidatesShowing);
		if (null == mMajorView || !mInputModeSwitcher.isChineseText()
				|| mLastCandidatesShowing == candidatesShowing)
			return;
		mLastCandidatesShowing = candidatesShowing;

		SoftKeyboard skb = mMajorView.getSoftKeyboard();
		if (null == skb)
			return;

		int state = mInputModeSwitcher.getTooggleStateForCnCand();
		if (!candidatesShowing) {
			skb.disableToggleState(state, false);
			skb.enableToggleStates(mInputModeSwitcher.getToggleStates());
		} else {
			skb.enableToggleState(state, false);
		}

		mMajorView.invalidate();
	}

	/**
	 * 更新输入法模式。逻辑简介：先获取软键盘xml布局文件，然后更新软键盘布局，设置软键盘状态。
	 */
	public void updateInputMode() {
		Log.i("carlIME","updateInputMode... isLanguageEnglish:"+isLanguageEnglish+" mSaveSoftKeyDown:"+mSaveSoftKeyDown);
		//add by chencong
		if(isLanguageEnglish){
			if(mSaveSoftKeyDown != null){
				mSaveSoftKeyDown.setKeyType(new SoftKeyType(6, getResources().getDrawable(R.drawable.switch_eng),
								getResources().getDrawable(R.drawable.switch_eng)), getResources().getDrawable(R.drawable.switch_eng),
						getResources().getDrawable(R.drawable.switch_eng));
			}
		}else{
			if(mSaveSoftKeyDown != null){
				mSaveSoftKeyDown.setKeyType(new SoftKeyType(6, getResources().getDrawable(R.drawable.switch_ch),
								getResources().getDrawable(R.drawable.switch_ch)), getResources().getDrawable(R.drawable.switch_ch),
						getResources().getDrawable(R.drawable.switch_ch));
			}
		}

		int skbLayout = mInputModeSwitcher.getSkbLayout();
		if (mSkbLayout != skbLayout) {
			mSkbLayout = skbLayout;
			updateSkbLayout();
		}

		mLastCandidatesShowing = false;

		if (null == mMajorView)
			return;

		SoftKeyboard skb = mMajorView.getSoftKeyboard();
		if (null == skb)
			return;
		skb.enableToggleStates(mInputModeSwitcher.getToggleStates());
		invalidate();
		return;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Environment env = Environment.getInstance();
		//int measuredWidth = env.getScreenWidth()/mSkbWidthScale;
		//int measuredHeight = getPaddingTop()/mSkbScale;
		//measuredHeight += env.getSkbHeight()/mSkbScale;
		widthMeasureSpec = MeasureSpec.makeMeasureSpec(mIMEWidth,MeasureSpec.EXACTLY);
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(mIMEHeight/mWeight*(mWeight-1),MeasureSpec.EXACTLY);
		setBackgroundColor(this.getResources().getColor(R.color.auqamarin));
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	/**
	 * 更新软键盘布局
	 */
	private void updateSkbLayout() {
		int screenWidth = mIMEWidth;
		int skbHeight = mIMEHeight/mWeight*(mWeight-1);
		
		Log.d("wsldwo","updateSkbLayout:"+screenWidth+" "+skbHeight);
		
		if (null == mSkbFlipper) {
			mSkbFlipper = (ViewFlipper) findViewById(R.id.alpha_floatable);
			//mSkbFlipper.setBackgroundColor(Color.GRAY);
			ViewGroup.LayoutParams lp = mSkbFlipper.getLayoutParams();
			//lp.width = screenWidth/6*5;
			lp.height = skbHeight;
			mSkbFlipper.setLayoutParams(lp);
			mSkbFlipper.setBackgroundColor(this.getResources().getColor(R.color.turquoise));
			//mSkbFlipper.
		}
		if (null == mDragView){
			mDragView = (DragView) findViewById(R.id.dragview1);
			//mDragView.setBackgroundColor(Color.DKGRAY);
			
			ViewGroup.LayoutParams lp = mDragView.getLayoutParams();
			lp.width = screenWidth/20;
			lp.height = skbHeight;
			mDragView.setLayoutParams(lp);
			//mDragView.setBackgroundColor(this.getResources().getColor(R.color.auqamarin));
			
			if(mDragView.getOnEventListener()==null){
				mDragView.setOnEventListener(mOnEventListener);
			}
			
		}
		if( null == mDragView2){
			mDragView = (DragView) findViewById(R.id.dragview2);
			//mDragView.setBackgroundColor(Color.DKGRAY);
			
			ViewGroup.LayoutParams lp = mDragView.getLayoutParams();
			lp.width = screenWidth/20;
			lp.height = skbHeight;
			mDragView.setLayoutParams(lp);
			//mDragView.setBackgroundColor(this.getResources().getColor(R.color.auqamarin));
			
			if(mDragView.getOnEventListener()==null){
				mDragView.setOnEventListener(mOnEventListener);
			}
		}
		
		int newWidth = screenWidth/10*9;
		
		mMajorView = (SoftKeyboardView) mSkbFlipper.getChildAt(0);

		SoftKeyboard majorSkb = null;
		SkbPool skbPool = SkbPool.getInstance();

		switch (mSkbLayout) {
		case R.xml.skb_qwerty:
			majorSkb = skbPool.getSoftKeyboard(R.xml.skb_qwerty,
					R.xml.skb_qwerty, newWidth, skbHeight, mContext);
			break;

		case R.xml.skb_sym1:
			majorSkb = skbPool.getSoftKeyboard(R.xml.skb_sym1, R.xml.skb_sym1,
					newWidth, skbHeight, mContext);
			break;

		case R.xml.skb_sym2:
			majorSkb = skbPool.getSoftKeyboard(R.xml.skb_sym2, R.xml.skb_sym2,
					newWidth, skbHeight, mContext);
			break;

		case R.xml.skb_smiley:
			majorSkb = skbPool.getSoftKeyboard(R.xml.skb_smiley,
					R.xml.skb_smiley, newWidth, skbHeight, mContext);
			break;

		case R.xml.skb_phone:
			majorSkb = skbPool.getSoftKeyboard(R.xml.skb_phone,
					R.xml.skb_phone, newWidth, skbHeight, mContext);
			break;
		default:
		}

		if (null == majorSkb || !mMajorView.setSoftKeyboard(majorSkb)) {
			return;
		}
		
		mMajorView.setBalloonHint(mBalloonOnKey, mBalloonPopup, false);
		//mMajorView.setBackgroundColor(Color.RED);
		//mMajorView.setOffX(-screenWidth/6);
		//mMajorView.setOffsetToSkbContainer(new int[]{-200,0});
		mMajorView.invalidate();
		
	}

	/**
	 * 响应按键事件。调用输入法服务的响应按键事件方法，把按键事件流到输入法服务里面去处理。
	 * 
	 * @param sKey
	 */
	private void responseKeyEvent(SoftKey sKey) {
		if (null == sKey)
			return;
		((PinyinIME) mService).responseSoftKeyEvent(sKey);
		return;
	}

	/**
	 * 返回软键盘视图。逻辑简介：先判断副软键盘弹出框是否在显示，是的话，就判断坐标点是否在副软键盘的区域内，如果是，就返回副软键盘弹出框，
	 * 否则返回null。如果副软键盘弹出框没有显示，就直接返回主软键盘视图mMajorView。
	 * 
	 * @param x
	 * @param y
	 * @param positionInParent
	 * @return
	 */
	private SoftKeyboardView inKeyboardView(int x, int y,
			int positionInParent[]) {
		if (mPopupSkbShow) {
			if (mPopupX <= x && mPopupX + mPopupSkb.getWidth() > x
					&& mPopupY <= y && mPopupY + mPopupSkb.getHeight() > y) {
				positionInParent[0] = mPopupX;
				positionInParent[1] = mPopupY;
				mPopupSkbView.setOffsetToSkbContainer(positionInParent);
				return mPopupSkbView;
			}
			return null;
		}

		return mMajorView;
	}

	/**
	 * 弹出副软键盘弹出框。副软键盘弹出框的软键盘xml资源ID是存放在按下的按键mSoftKeyDown的属性mPopupSkbId中的。
	 * 弹出副软键盘弹出框后，主软键盘视图mMajorView会被隐藏。
	 */
	@SuppressLint("WrongCall") private void popupSymbols() {
		int popupResId = mSoftKeyDown.getPopupResId();
		if (popupResId > 0) {
			int skbContainerWidth = getWidth();
			int skbContainerHeight = getHeight();
			// The paddings of the background are not included.
			int miniSkbWidth = (int) (skbContainerWidth * 0.8);
			int miniSkbHeight = (int) (skbContainerHeight * 0.23);

			SkbPool skbPool = SkbPool.getInstance();
			SoftKeyboard skb = skbPool.getSoftKeyboard(popupResId, popupResId,
					miniSkbWidth, miniSkbHeight, mContext);
			if (null == skb)
				return;

			mPopupX = (skbContainerWidth - skb.getSkbTotalWidth()) / 2;
			mPopupY = (skbContainerHeight - skb.getSkbTotalHeight()) / 2;

			if (null == mPopupSkbView) {
				mPopupSkbView = new SoftKeyboardView(mContext, null);
				mPopupSkbView.onMeasure(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);
			}
			mPopupSkbView.setOnTouchListener(this);
			mPopupSkbView.setSoftKeyboard(skb);
			mPopupSkbView.setBalloonHint(mBalloonOnKey, mBalloonPopup, true);

			mPopupSkb.setContentView(mPopupSkbView);
			mPopupSkb.setWidth(skb.getSkbCoreWidth()
					+ mPopupSkbView.getPaddingLeft()
					+ mPopupSkbView.getPaddingRight());
			mPopupSkb.setHeight(skb.getSkbCoreHeight()
					+ mPopupSkbView.getPaddingTop()
					+ mPopupSkbView.getPaddingBottom());

			getLocationInWindow(mXyPosTmp);
			mPopupSkb.showAtLocation(this, Gravity.NO_GRAVITY, mPopupX, mPopupY
					+ mXyPosTmp[1]);
			mPopupSkbShow = true;
			mPopupSkbNoResponse = true;
			// Invalidate itself to dim the current soft keyboards.
			dimSoftKeyboard(true);
			resetKeyPress(0);
		}
	}

	/**
	 * 隐藏主软键盘视图
	 * 
	 * @param dimSkb
	 */
	private void dimSoftKeyboard(boolean dimSkb) {
		mMajorView.dimSoftKeyboard(dimSkb);
	}

	/**
	 * 隐藏副软键盘弹出框，显示主软键盘视图。
	 */
	private void dismissPopupSkb() {
		mPopupSkb.dismiss();
		mPopupSkbShow = false;
		dimSoftKeyboard(false);
		resetKeyPress(0);
	}

	/**
	 * 重置按下按键
	 * 
	 * @param delay
	 */
	private void resetKeyPress(long delay) {
		mLongPressTimer.removeTimer();

		if (null != mSkv) {
			mSkv.resetKeyPress(delay);
		}
	}

	/**
	 * 副软键盘弹出框显示的时候，如果realAction为true，那么就调用dismissPopupSkb（）隐藏副软键盘弹出框，显示主软键盘视图。
	 * 
	 * @param realAction
	 * @return
	 */
	public boolean handleBack(boolean realAction) {
		if (mPopupSkbShow) {
			if (!realAction)
				return true;

			dismissPopupSkb();
			mDiscardEvent = true;
			return true;
		}
		return false;
	}

	/**
	 * 隐藏副软键盘弹出框
	 */
	public void dismissPopups() {
		Log.i("carlIME","dismissPopups...");
		handleBack(true);
		resetKeyPress(0);
	}

	/**
	 * 隐藏软件盘
	 */
	private void hideSoftKeyboard() {
		 Context context = getContext();
		 if (context instanceof InputMethodService) {
			InputMethodService inputMethodService = (InputMethodService) context;
			inputMethodService.hideWindow();

			 isLanguageEnglish = true;
		}
	}

	public void setInputLanguage(boolean flag){
		isLanguageEnglish = flag;
	}
	public boolean getInputLanguage(){ return  isLanguageEnglish;}

	/**
	 * 修改按键icon
	 * @param event
	 * @return
	 */
	private void switchLanguageKeyIcon(){
		isLanguageEnglish = !isLanguageEnglish;
		/*
		if(isLanguageEnglish) {
			mSoftKeyDown.setKeyType(new SoftKeyType(6, getResources().getDrawable(R.drawable.switch_eng),
							getResources().getDrawable(R.drawable.switch_eng)), getResources().getDrawable(R.drawable.switch_eng),
					getResources().getDrawable(R.drawable.switch_eng));
		}else{
			mSoftKeyDown.setKeyType(new SoftKeyType(6, getResources().getDrawable(R.drawable.switch_ch),
							getResources().getDrawable(R.drawable.switch_ch)), getResources().getDrawable(R.drawable.switch_ch),
					getResources().getDrawable(R.drawable.switch_ch));
		}
		*/

		mSaveSoftKeyDown = mSoftKeyDown;
	}

	/**
	 * 切换字符按键
	 * @param event
	 * @return
	 */
	private void switchLanguageToChar(){
		Log.i("carlIME","mSaveSoftKeyDown:"+mSaveSoftKeyDown+" isLanguageEnglish:"+isLanguageEnglish);
		if(mSaveSoftKeyDown != null) {
			if (isLanguageEnglish) {
				mSaveSoftKeyDown.setKeyType(new SoftKeyType(6, getResources().getDrawable(R.drawable.switch_eng),
								getResources().getDrawable(R.drawable.switch_eng)), getResources().getDrawable(R.drawable.switch_eng),
						getResources().getDrawable(R.drawable.switch_eng));
			} else {
				mSaveSoftKeyDown.setKeyType(new SoftKeyType(6, getResources().getDrawable(R.drawable.switch_ch),
								getResources().getDrawable(R.drawable.switch_ch)), getResources().getDrawable(R.drawable.switch_ch),
						getResources().getDrawable(R.drawable.switch_ch));
			}
		}
		invalidate();
	}

	private void switchInputShowOrHide()
	{
		if(isLanguageEnglish){
			mCandidatesContainer.setVisibility(View.INVISIBLE);
		}else{
			mCandidatesContainer.setVisibility(View.VISIBLE);
		}
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);

		if (mSkbFlipper.isFlipping()) {
			resetKeyPress(0);
			return true;
		}

		int x = (int) event.getX() - mIMEWidth/20;//修正左方新增的dragview宽度，否则点击会有误差
		int y = (int) event.getY();
		// Bias correction
		y = y + mYBiasCorrection;

		if (!mPopupSkbShow) {
			// mGestureDetector的监听器在输入法服务PinyinIME中。
			if (mGestureDetector.onTouchEvent(event)) {
				resetKeyPress(0);
				mDiscardEvent = true;
				return true;
			}
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.i("carlIME","ACTION_DOWN");
			resetKeyPress(0);

			mWaitForTouchUp = true;
			mDiscardEvent = false;

			mSkv = null;
			mSoftKeyDown = null;
			mSkv = inKeyboardView(x, y, mSkvPosInContainer);
			if (null != mSkv) {
				mSoftKeyDown = mSkv.onKeyPress(x - mSkvPosInContainer[0], y
						- mSkvPosInContainer[1], mLongPressTimer, false);
			}
			if (mSoftKeyDown != null && mSoftKeyDown.mKeyCode == 68) {//点击退出键 隐藏软键盘
				hideSoftKeyboard();
			}
			if(mSoftKeyDown != null && mSoftKeyDown.mKeyCode == -2){//点击中英文切换按键
				Log.i("carlIME","press switch eng-ch");
				//switchInputShowOrHide();
				switchLanguageKeyIcon();
			}
			if(mSoftKeyDown != null && mSoftKeyDown.mKeyCode == -3){//点击切换字符按键
				Log.i("carlIME","press switch char");
				//switchLanguageToChar();
			}

			/*
			Log.i("carlIME","111111mPopupSkbShow:"+mPopupSkbShow+" mPopupSkbNoResponse:"+mPopupSkbNoResponse);
			if (!mPopupSkbShow || !mPopupSkbNoResponse) {
				responseKeyEvent(mSoftKeyDown);
			}
			*/
			break;

		case MotionEvent.ACTION_MOVE:
			if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
				break;
			}
			if (mDiscardEvent) {
				resetKeyPress(0);
				break;
			}

			if (mPopupSkbShow && mPopupSkbNoResponse) {
				break;
			}

			SoftKeyboardView skv = inKeyboardView(x, y, mSkvPosInContainer);
			if (null != skv) {
				if (skv != mSkv) {
					mSkv = skv;
					mSoftKeyDown = mSkv.onKeyPress(x - mSkvPosInContainer[0], y
							- mSkvPosInContainer[1], mLongPressTimer, true);
				} else if (null != skv) {
					if (null != mSkv) {
						mSoftKeyDown = mSkv.onKeyMove(
								x - mSkvPosInContainer[0], y
										- mSkvPosInContainer[1]);
						if (null == mSoftKeyDown) {
							mDiscardEvent = true;
						}
					}
				}
			}
			break;

		case MotionEvent.ACTION_UP:
			Log.i("carlIME","ACTION_UP");
			if (mDiscardEvent) {
				resetKeyPress(0);
				break;
			}

			mWaitForTouchUp = false;

			// The view which got the {@link MotionEvent#ACTION_DOWN} event is
			// always used to handle this event.
			if (null != mSkv) {
				mSkv.onKeyRelease(x - mSkvPosInContainer[0], y
						- mSkvPosInContainer[1]);
			}
			//delete by chencong 2018.5.4

			Log.i("carlIME","mPopupSkbShow:"+mPopupSkbShow+" mPopupSkbNoResponse:"+mPopupSkbNoResponse);
			if (!mPopupSkbShow || !mPopupSkbNoResponse) {
				responseKeyEvent(mSoftKeyDown);
			}


			if (mSkv == mPopupSkbView && !mPopupSkbNoResponse) {
				dismissPopupSkb();
			}
			mPopupSkbNoResponse = false;
			break;

		case MotionEvent.ACTION_CANCEL:
			break;
		}

		if (null == mSkv) {
			return false;
		}

		return true;
	}

	// Function for interface OnTouchListener, it is used to handle touch events
	// which will be delivered to the popup soft keyboard view.
	public boolean onTouch(View v, MotionEvent event) {
		// Translate the event to fit to the container.
		MotionEvent newEv = MotionEvent.obtain(event.getDownTime(),
				event.getEventTime(), event.getAction(),
				event.getX() + mPopupX, event.getY() + mPopupY,
				event.getPressure(), event.getSize(), event.getMetaState(),
				event.getXPrecision(), event.getYPrecision(),
				event.getDeviceId(), event.getEdgeFlags());
		boolean ret = onTouchEvent(newEv);
		return ret;
	}

	/**
	 * 长按定时器
	 * 
	 * @ClassName LongPressTimer
	 * @author keanbin
	 */
	class LongPressTimer extends Handler implements Runnable {
		/**
		 * When user presses a key for a long time, the timeout interval to
		 * generate first {@link #LONG_PRESS_KEYNUM1} key events. 长按时间一
		 */
		public static final int LONG_PRESS_TIMEOUT1 = 500;

		/**
		 * When user presses a key for a long time, after the first
		 * {@link #LONG_PRESS_KEYNUM1} key events, this timeout interval will be
		 * used. 长按时间二
		 */
		private static final int LONG_PRESS_TIMEOUT2 = 100;

		/**
		 * When user presses a key for a long time, after the first
		 * {@link #LONG_PRESS_KEYNUM2} key events, this timeout interval will be
		 * used. 长按时间三
		 */
		private static final int LONG_PRESS_TIMEOUT3 = 100;

		/**
		 * When user presses a key for a long time, after the first
		 * {@link #LONG_PRESS_KEYNUM1} key events, timeout interval
		 * {@link #LONG_PRESS_TIMEOUT2} will be used instead.
		 * 
		 */
		public static final int LONG_PRESS_KEYNUM1 = 1;

		/**
		 * When user presses a key for a long time, after the first
		 * {@link #LONG_PRESS_KEYNUM2} key events, timeout interval
		 * {@link #LONG_PRESS_TIMEOUT3} will be used instead.
		 */
		public static final int LONG_PRESS_KEYNUM2 = 3;

		SkbContainer mSkbContainer;

		private int mResponseTimes = 0;

		public LongPressTimer(SkbContainer skbContainer) {
			mSkbContainer = skbContainer;
		}

		public void startTimer() {
			postAtTime(this, SystemClock.uptimeMillis() + LONG_PRESS_TIMEOUT1);
			mResponseTimes = 0;
		}

		public boolean removeTimer() {
			removeCallbacks(this);
			return true;
		}

		public void run() {
			if (mWaitForTouchUp) {
				mResponseTimes++;
				if (mSoftKeyDown.repeatable()) {
					if (mSoftKeyDown.isUserDefKey()) {
						// 用户定义的按键
						if (1 == mResponseTimes) {
							if (mInputModeSwitcher
									.tryHandleLongPressSwitch(mSoftKeyDown.mKeyCode)) {
								mDiscardEvent = true;
								resetKeyPress(0);
							}
						}
					} else {
						// 系统定义的按键，长按相当于执行重复按键功能，mResponseTimes是按的次数
						responseKeyEvent(mSoftKeyDown);
						long timeout;
						if (mResponseTimes < LONG_PRESS_KEYNUM1) {
							timeout = LONG_PRESS_TIMEOUT1;
						} else if (mResponseTimes < LONG_PRESS_KEYNUM2) {
							timeout = LONG_PRESS_TIMEOUT2;
						} else {
							timeout = LONG_PRESS_TIMEOUT3;
						}
						postAtTime(this, SystemClock.uptimeMillis() + timeout);
					}
				} else {
					if (1 == mResponseTimes) {
						popupSymbols();
					}
				}
			}
		}
	}
}
