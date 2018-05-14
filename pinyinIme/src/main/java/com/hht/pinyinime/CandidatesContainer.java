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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

import com.hht.pinyinime.PinyinIME.DecodingInfo;

/**
 * 集装箱中的箭头更新监听器
 * 
 * @ClassName ArrowUpdater
 * @author keanbin
 */
interface ArrowUpdater {
	void updateArrowStatus();
}

/**
 * Container used to host the two candidate views. When user drags on candidate
 * view, animation is used to dismiss the current candidate view and show a new
 * one. These two candidate views and their parent are hosted by this container.
 * <p>
 * Besides the candidate views, there are two arrow views to show the page
 * forward/backward arrows.
 * </p>
 */
/**
 * 候选词集装箱
 * 
 * @ClassName CandidatesContainer
 * @author keanbin
 */
public class CandidatesContainer extends RelativeLayout implements
		OnTouchListener, AnimationListener, ArrowUpdater {
	private static final String TAG = "CandidatesContainer";
	/**
	 * Alpha value to show an enabled arrow. 箭头图片显示时的透明度
	 */
	private static int ARROW_ALPHA_ENABLED = 0xff;

	/**
	 * Alpha value to show an disabled arrow. 箭头图片不显示时的透明度
	 */
	private static int ARROW_ALPHA_DISABLED = 0x40;

	/**
	 * Animation time to show a new candidate view and dismiss the old one.
	 * 显示或者关闭一个候选词View的动画时间
	 */
	private static int ANIMATION_TIME = 200;

	/**
	 * Listener used to notify IME that user clicks a candidate, or navigate
	 * between them. 候选词视图监听器
	 */
	private CandidateViewListener mCvListener;

	/**
	 * The left arrow button used to show previous page. 左边箭头按钮
	 */
	private ImageButton mLeftArrowBtn;

	/**
	 * The right arrow button used to show next page. 右边箭头按钮
	 */
	private ImageButton mRightArrowBtn;
	
	private DragView mDragView1,mDragView2;
	
	private int mXmargin;

	/**
	 * Decoding result to show. 词库解码对象
	 */
	private DecodingInfo mDecInfo;

	/**
	 * The animation view used to show candidates. It contains two views.
	 * Normally, the candidates are shown one of them. When user navigates to
	 * another page, animation effect will be performed.
	 * ViewFlipper页面管理，它包含两个视图，正常只显示其中一个，当切换候选词页的时候，就启动另一个视图装载接着要显示的候选词切入进来。
	 */
	private ViewFlipper mFlipper;

	/**
	 * The x offset of the flipper in this container. ViewFlipper 在集装箱的偏移位置。
	 */
	private int xOffsetForFlipper;

	/**
	 * Animation used by the incoming view when the user navigates to a left
	 * page. 传入页面移动向左边的动画
	 */
	private Animation mInAnimPushLeft;

	/**
	 * Animation used by the incoming view when the user navigates to a right
	 * page. 传入页面移动向右边的动画
	 */
	private Animation mInAnimPushRight;

	/**
	 * Animation used by the incoming view when the user navigates to a page
	 * above. If the page navigation is triggered by DOWN key, this animation is
	 * used. 传入页面移动向上的动画
	 */
	private Animation mInAnimPushUp;

	/**
	 * Animation used by the incoming view when the user navigates to a page
	 * below. If the page navigation is triggered by UP key, this animation is
	 * used. 传入页面移动向下的动画
	 */
	private Animation mInAnimPushDown;

	/**
	 * Animation used by the outgoing view when the user navigates to a left
	 * page. 传出页面移动向左边的动画
	 */
	private Animation mOutAnimPushLeft;

	/**
	 * Animation used by the outgoing view when the user navigates to a right
	 * page.传出页面移动向右边的动画
	 */
	private Animation mOutAnimPushRight;

	/**
	 * Animation used by the outgoing view when the user navigates to a page
	 * above. If the page navigation is triggered by DOWN key, this animation is
	 * used.传出页面移动向上边的动画
	 */
	private Animation mOutAnimPushUp;

	/**
	 * Animation used by the incoming view when the user navigates to a page
	 * below. If the page navigation is triggered by UP key, this animation is
	 * used.传出页面移动向下边的动画
	 */
	private Animation mOutAnimPushDown;

	/**
	 * Animation object which is used for the incoming view currently.
	 * 传入页面当前使用的动画
	 */
	private Animation mInAnimInUse;

	/**
	 * Animation object which is used for the outgoing view currently.
	 * 传出页面当前使用的动画
	 */
	private Animation mOutAnimInUse;

	/**
	 * Current page number in display. 当前显示的页码
	 */
	private int mCurrentPage = -1;
	
	private int mSkbScale = 1;
	private int mSkbWidthScale = 1;
	
	private int mIMEHeight,mIMEWidth;
	private int mWeight;

	public CandidatesContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mIMEWidth = this.getResources().getDisplayMetrics().widthPixels/2;
		mIMEHeight = this.getResources().getDisplayMetrics().heightPixels/3;
		mWeight=9;
	}

	public void initialize(CandidateViewListener cvListener,
			BalloonHint balloonHint, GestureDetector gestureDetector) {
		mCvListener = cvListener;

		mLeftArrowBtn = (ImageButton) findViewById(R.id.arrow_left_btn);
		mRightArrowBtn = (ImageButton) findViewById(R.id.arrow_right_btn);
		mLeftArrowBtn.setOnTouchListener(this);
		mRightArrowBtn.setOnTouchListener(this);
		
		mDragView1 = (DragView) findViewById(R.id.dragview3);
		mDragView2 = (DragView) findViewById(R.id.dragview4);

		mFlipper = (ViewFlipper) findViewById(R.id.candidate_flipper);
		//Sets whether to consider all children, or just those in the VISIBLE or INVISIBLE state, when measuring.
		mFlipper.setMeasureAllChildren(true);

		invalidate();
		requestLayout();

		for (int i = 0; i < mFlipper.getChildCount(); i++) {
			CandidateView cv = (CandidateView) mFlipper.getChildAt(i);
			cv.initialize(this, balloonHint, gestureDetector, mCvListener);
		}
	}
	
	public void setIMEWH(int width, int height){
		mIMEWidth = width;
		mIMEHeight = height;
	}
	public void setWeight(int weight){
		mWeight = weight;
	}

	/**
	 * 显示候选词
	 * 
	 * @param decInfo
	 * @param enableActiveHighlight
	 */
	public void showCandidates(PinyinIME.DecodingInfo decInfo,
			boolean enableActiveHighlight) {
		if (null == decInfo)
			return;
		mDecInfo = decInfo;
		mCurrentPage = 0;

		if (decInfo.isCandidatesListEmpty()) {
			showArrow(mLeftArrowBtn, false);
			showArrow(mRightArrowBtn, false);
		} else {
			showArrow(mLeftArrowBtn, true);
			showArrow(mRightArrowBtn, true);
		}

		for (int i = 0; i < mFlipper.getChildCount(); i++) {
			CandidateView cv = (CandidateView) mFlipper.getChildAt(i);
			cv.setDecodingInfo(mDecInfo);
		}
		stopAnimation();

		CandidateView cv = (CandidateView) mFlipper.getCurrentView();
		cv.showPage(mCurrentPage, 0, enableActiveHighlight);

		updateArrowStatus();
		invalidate();
	}

	/**
	 * 获取当前的页码
	 * 
	 * @return
	 */
	public int getCurrentPage() {
		return mCurrentPage;
	}

	/**
	 * 设置候选词是否高亮
	 * 
	 * @param enableActiveHighlight
	 */
	public void enableActiveHighlight(boolean enableActiveHighlight) {
		CandidateView cv = (CandidateView) mFlipper.getCurrentView();
		cv.enableActiveHighlight(enableActiveHighlight);
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Environment env = Environment.getInstance();
		//int measuredWidth = env.getScreenWidth()/mSkbScale;
		//int measuredHeight = getPaddingTop()/mSkbScale;
		//measuredHeight += env.getHeightForCandidates()/mSkbScale;
		widthMeasureSpec = MeasureSpec.makeMeasureSpec(mIMEWidth,
				MeasureSpec.EXACTLY);
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(mIMEHeight/mWeight,
				MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (null != mLeftArrowBtn) {
			// 设置候选词所在的 ViewFlipper 在集装箱中的偏移位置
			//xOffsetForFlipper = mLeftArrowBtn.getMeasuredWidth();
		}
		
		mXmargin = (mIMEWidth/10*9)/100;

		if(mDragView1 != null){
			ViewGroup.LayoutParams lp = mDragView1.getLayoutParams();
			lp.width = mIMEWidth/20 + mXmargin;
			mDragView1.setLayoutParams(lp);
			mDragView1.setBackgroundColor(this.getResources().getColor(R.color.transparent));
		}
		if(mDragView2 != null){
			ViewGroup.LayoutParams lp = mDragView2.getLayoutParams();
			lp.width = mIMEWidth/20 + mXmargin;
			mDragView2.setLayoutParams(lp);
			mDragView2.setBackgroundColor(this.getResources().getColor(R.color.transparent));
		}
		/*
		if(mLeftArrowBtn != null){
			ViewGroup.LayoutParams lp = mLeftArrowBtn.getLayoutParams();
			lp.width = mIMEWidth/20 + mXmargin;
			mLeftArrowBtn.setLayoutParams(lp);
		}
		if(mRightArrowBtn != null){
			ViewGroup.LayoutParams lp = mRightArrowBtn.getLayoutParams();
			lp.width = mIMEWidth/20 + mXmargin;
			mRightArrowBtn.setLayoutParams(lp);
		}
		*/
		if(mFlipper != null){
			ViewGroup.LayoutParams lp = mFlipper.getLayoutParams();
			lp.width = mIMEWidth - (mIMEWidth/20 + mXmargin)*4 + 30;
			//lp.width = mIMEWidth - (mIMEWidth/40 + mXmargin)*4;
			mFlipper.setLayoutParams(lp);
			//mFlipper.setBackgroundColor(this.getResources().getColor(R.color.turquoise));
		}

	}

	/**
	 * 高亮位置向上一个候选词移动或者移动到上一页的最后一个候选词的位置。
	 * 
	 * @return
	 */
	public boolean activeCurseBackward() {
		if (mFlipper.isFlipping() || null == mDecInfo) {
			return false;
		}

		CandidateView cv = (CandidateView) mFlipper.getCurrentView();

		if (cv.activeCurseBackward()) {
			cv.invalidate();
			return true;
		} else {
			return pageBackward(true, true);
		}
	}

	/**
	 * 高亮位置向下一个候选词移动或者移动到下一页的第一个候选词的位置。
	 * 
	 * @return
	 */
	public boolean activeCurseForward() {
		if (mFlipper.isFlipping() || null == mDecInfo) {
			return false;
		}

		CandidateView cv = (CandidateView) mFlipper.getCurrentView();

		if (cv.activeCursorForward()) {
			cv.invalidate();
			return true;
		} else {
			return pageForward(true, true);
		}
	}

	/**
	 * 到上一页候选词
	 * 
	 * @param animLeftRight
	 *            高亮位置是否到本页最后一个候选词位置
	 * @param enableActiveHighlight
	 * @return
	 */
	public boolean pageBackward(boolean animLeftRight,
			boolean enableActiveHighlight) {
		if (null == mDecInfo)
			return false;

		if (mFlipper.isFlipping() || 0 == mCurrentPage)
			return false;

		int child = mFlipper.getDisplayedChild();
		int childNext = (child + 1) % 2;
		CandidateView cv = (CandidateView) mFlipper.getChildAt(child);
		CandidateView cvNext = (CandidateView) mFlipper.getChildAt(childNext);

		mCurrentPage--;
		int activeCandInPage = cv.getActiveCandiatePosInPage();
		if (animLeftRight)
			activeCandInPage = mDecInfo.mPageStart.elementAt(mCurrentPage + 1)
					- mDecInfo.mPageStart.elementAt(mCurrentPage) - 1;

		cvNext.showPage(mCurrentPage, activeCandInPage, enableActiveHighlight);
		loadAnimation(animLeftRight, false);
		startAnimation();

		updateArrowStatus();
		return true;
	}

	/**
	 * 到下一页候选词
	 * 
	 * @param animLeftRight
	 *            高亮位置是否到本页第一个候选词位置
	 * @param enableActiveHighlight
	 * @return
	 */
	public boolean pageForward(boolean animLeftRight,
			boolean enableActiveHighlight) {
		Log.i("PinyinIME","mDecInfo:"+mDecInfo);
		if (null == mDecInfo)
			return false;

		Log.i("PinyinIME","mCurrentPage:"+mCurrentPage);
		if (mFlipper.isFlipping() || !mDecInfo.preparePage(mCurrentPage + 1)) {
			return false;
		}

		int child = mFlipper.getDisplayedChild();
		int childNext = (child + 1) % 2;
		Log.i("PinyinIME","child:"+child+" childNext:"+childNext);
		CandidateView cv = (CandidateView) mFlipper.getChildAt(child);
		int activeCandInPage = cv.getActiveCandiatePosInPage();
		cv.enableActiveHighlight(enableActiveHighlight);

		CandidateView cvNext = (CandidateView) mFlipper.getChildAt(childNext);
		mCurrentPage++;
		if (animLeftRight)
			activeCandInPage = 0;

		Log.i("PinyinIME","mCurrentPage:"+mCurrentPage+" activeCandInPage:"+activeCandInPage
								+" enableActiveHighlight:"+enableActiveHighlight);
		cvNext.showPage(mCurrentPage, activeCandInPage, enableActiveHighlight);
		loadAnimation(animLeftRight, true);
		startAnimation();

		updateArrowStatus();
		return true;
	}

	/**
	 * 获取活动（高亮）的候选词在所有候选词中的位置
	 * 
	 * @return
	 */
	public int getActiveCandiatePos() {
		if (null == mDecInfo)
			return -1;
		CandidateView cv = (CandidateView) mFlipper.getCurrentView();
		return cv.getActiveCandiatePosGlobal();
	}

	/**
	 * 更新箭头显示
	 */
	public void updateArrowStatus() {
		if (mCurrentPage < 0)
			return;
		boolean forwardEnabled = mDecInfo.pageForwardable(mCurrentPage);
		boolean backwardEnabled = mDecInfo.pageBackwardable(mCurrentPage);

		if (backwardEnabled) {
			enableArrow(mLeftArrowBtn, true);
		} else {
			enableArrow(mLeftArrowBtn, false);
		}
		if (forwardEnabled) {
			enableArrow(mRightArrowBtn, true);
		} else {
			enableArrow(mRightArrowBtn, false);
		}
	}

	/**
	 * 设置箭头图标是否有效，和图标的透明度。
	 * 
	 * @param arrowBtn
	 * @param enabled
	 */
	private void enableArrow(ImageButton arrowBtn, boolean enabled) {
		arrowBtn.setEnabled(enabled);
		if (enabled)
			arrowBtn.setAlpha(ARROW_ALPHA_ENABLED);
		else
			arrowBtn.setAlpha(ARROW_ALPHA_DISABLED);
	}

	/**
	 * 设置箭头图标是否显示
	 * 
	 * @param arrowBtn
	 * @param show
	 */
	private void showArrow(ImageButton arrowBtn, boolean show) {
		if (show)
			arrowBtn.setVisibility(View.VISIBLE);
		else
			arrowBtn.setVisibility(View.INVISIBLE);
	}

	/**
	 * view的触摸事件监听器
	 * 
	 * @param v
	 * @param event
	 */
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (v == mLeftArrowBtn) {
				// 调用候选词视图监听器的向右滑动手势处理函数
				mCvListener.onToRightGesture();
			} else if (v == mRightArrowBtn) {
				// 调用候选词视图监听器的向左滑动手势处理函数
				mCvListener.onToLeftGesture();
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			// 设置候选词视图高亮活动的候选词。
			CandidateView cv = (CandidateView) mFlipper.getCurrentView();
			cv.enableActiveHighlight(true);
		}

		return false;
	}

	// The reason why we handle candiate view's touch events here is because
	// that the view under the focused view may get touch events instead of the
	// focused one.
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO 触摸事件的坐标点是以哪里为原点？
		// 调整触摸事件坐标点的坐标
		//event.offsetLocation(-xOffsetForFlipper, 0);
		// 调用候选词视图触摸事件处理函数
		CandidateView cv = (CandidateView) mFlipper.getCurrentView();
		int x = (int)event.getX();
		//add by chencong 超出提示词部分区域不给触控处理
		Log.i(TAG,"x:"+x);
		int width = mFlipper.getLayoutParams().width;
		int offset = mIMEWidth/20 + mXmargin;
		Log.i(TAG,"width:"+width);
		if(x <= width+offset)
			cv.onTouchEventReal(event,offset);//修正左侧新增的dragview宽度
		return true;
	}

	/**
	 * 创建动画，并设置给ViewFlipper mFlipper。
	 * 
	 * @param animLeftRight
	 * @param forward
	 */
	public void loadAnimation(boolean animLeftRight, boolean forward) {
		if (animLeftRight) {
			if (forward) {
				if (null == mInAnimPushLeft) {
					mInAnimPushLeft = createAnimation(1.0f, 0, 0, 0, 0, 1.0f,
							ANIMATION_TIME);
					mOutAnimPushLeft = createAnimation(0, -1.0f, 0, 0, 1.0f, 0,
							ANIMATION_TIME);
				}
				mInAnimInUse = mInAnimPushLeft;
				mOutAnimInUse = mOutAnimPushLeft;
			} else {
				if (null == mInAnimPushRight) {
					mInAnimPushRight = createAnimation(-1.0f, 0, 0, 0, 0, 1.0f,
							ANIMATION_TIME);
					mOutAnimPushRight = createAnimation(0, 1.0f, 0, 0, 1.0f, 0,
							ANIMATION_TIME);
				}
				mInAnimInUse = mInAnimPushRight;
				mOutAnimInUse = mOutAnimPushRight;
			}
		} else {
			if (forward) {
				if (null == mInAnimPushUp) {
					mInAnimPushUp = createAnimation(0, 0, 1.0f, 0, 0, 1.0f,
							ANIMATION_TIME);
					mOutAnimPushUp = createAnimation(0, 0, 0, -1.0f, 1.0f, 0,
							ANIMATION_TIME);
				}
				mInAnimInUse = mInAnimPushUp;
				mOutAnimInUse = mOutAnimPushUp;
			} else {
				if (null == mInAnimPushDown) {
					mInAnimPushDown = createAnimation(0, 0, -1.0f, 0, 0, 1.0f,
							ANIMATION_TIME);
					mOutAnimPushDown = createAnimation(0, 0, 0, 1.0f, 1.0f, 0,
							ANIMATION_TIME);
				}
				mInAnimInUse = mInAnimPushDown;
				mOutAnimInUse = mOutAnimPushDown;
			}
		}

		// 设置动画监听器，当动画结束的时候，调用onAnimationEnd（）。
		mInAnimInUse.setAnimationListener(this);

		mFlipper.setInAnimation(mInAnimInUse);
		mFlipper.setOutAnimation(mOutAnimInUse);
	}

	/**
	 * 创建移动动画
	 * 
	 * @param xFrom
	 * @param xTo
	 * @param yFrom
	 * @param yTo
	 * @param alphaFrom
	 * @param alphaTo
	 * @param duration
	 * @return
	 */
	private Animation createAnimation(float xFrom, float xTo, float yFrom,
			float yTo, float alphaFrom, float alphaTo, long duration) {
		AnimationSet animSet = new AnimationSet(getContext(), null);
		Animation trans = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
				xFrom, Animation.RELATIVE_TO_SELF, xTo,
				Animation.RELATIVE_TO_SELF, yFrom, Animation.RELATIVE_TO_SELF,
				yTo);
		Animation alpha = new AlphaAnimation(alphaFrom, alphaTo);
		animSet.addAnimation(trans);
		animSet.addAnimation(alpha);
		animSet.setDuration(duration);
		return animSet;
	}

	/**
	 * 开始动画，mFlipper显示下一个。
	 */
	private void startAnimation() {
		mFlipper.showNext();
	}

	/**
	 * 停止动画，mFlipper停止切换Flipping。
	 */
	private void stopAnimation() {
		mFlipper.stopFlipping();
	}

	/**
	 * 动画监听器：动画停止的时候的监听器回调。
	 */
	public void onAnimationEnd(Animation animation) {
		if (!mLeftArrowBtn.isPressed() && !mRightArrowBtn.isPressed()) {
			CandidateView cv = (CandidateView) mFlipper.getCurrentView();
			cv.enableActiveHighlight(true);
		}
	}

	/**
	 * 动画监听器：动画重复的时候的监听器回调。
	 */
	public void onAnimationRepeat(Animation animation) {
	}

	/**
	 * 动画监听器：动画开始的时候的监听器回调。
	 */
	public void onAnimationStart(Animation animation) {
	}
}
