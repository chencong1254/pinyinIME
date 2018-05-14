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

import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Class for soft keys which defined in the keyboard xml file. A soft key can be
 * a basic key or a toggling key. 按键
 * 
 * @see com.android.inputmethod.pinyin.SoftKeyToggle
 */
public class SoftKey {
	protected static final int KEYMASK_REPEAT = 0x10000000;
	protected static final int KEYMASK_BALLOON = 0x20000000;

	/**
	 * For a finger touch device, after user presses a key, there will be some
	 * consequent moving events because of the changing in touching pressure. If
	 * the moving distance in x is within this threshold, the moving events will
	 * be ignored. 触摸移动事件有效的x坐标差值，移动的x坐标差值小于有效的x坐标差值，移动事件被抛弃。
	 */
	public static final int MAX_MOVE_TOLERANCE_X = 0;

	/**
	 * For a finger touch device, after user presses a key, there will be some
	 * consequent moving events because of the changing in touching pressure. If
	 * the moving distance in y is within this threshold, the moving events will
	 * be ignored. 触摸移动事件有效的y坐标差值，移动的x坐标差值小于有效的y坐标差值，移动事件被抛弃。
	 */
	public static final int MAX_MOVE_TOLERANCE_Y = 0;

	/**
	 * Used to indicate the type and attributes of this key. the lowest 8 bits
	 * should be reserved for SoftkeyToggle. 按键的属性和类型，最低的8位留给软键盘变换状态。
	 */
	protected int mKeyMask;

	/** key的类型 */
	protected SoftKeyType mKeyType;

	/** key的图标 */
	protected Drawable mKeyIcon;

	/** key的弹出图标 */
	protected Drawable mKeyIconPopup;

	/** key的文本 */
	protected String mKeyLabel;

	/** key的code */
	protected int mKeyCode;

	/**
	 * If this value is not 0, this key can be used to popup a sub soft keyboard
	 * when user presses it for some time.
	 * 软件盘弹出对话框的id。如果这个值不为空，那么当它被长按的时候，弹出一个副软键盘。
	 */
	public int mPopupSkbId;

	/** 键盘宽度的百分比 ，mLeft = (int) (mLeftF * skbWidth); */
	public float mLeftF;
	public float mRightF;
	/** 键盘高度的百分比 */
	public float mTopF;
	public float mBottomF;
	// TODO 以下的 区域坐标是相对于什么的？是全局还是相对于父视图的？
	public int mLeft;
	public int mRight;
	public int mTop;
	public int mBottom;
	public int mPosXOffset;//键盘位置X偏移量 FUCK!!!  public int mPosXOffset = 192 ;
	public int mPosYOffset;//键盘位置Y偏移量
	public float mScaleValue = 1.0f;//缩放比例 FUCK!!!  public float mScaleValue = 0.8f;
	
	public float mTouchCorrect;
	
	public void setTouchCorrect(float f){
		mTouchCorrect = f;
	}

	/**
	 * 设置按键的类型、图标、弹出图标
	 * 
	 * @param keyType
	 * @param keyIcon
	 * @param keyIconPopup
	 */
	public void setKeyType(SoftKeyType keyType, Drawable keyIcon,
			Drawable keyIconPopup) {
		mKeyType = keyType;
		mKeyIcon = keyIcon;
		mKeyIconPopup = keyIconPopup;
	}

	// The caller guarantees that all parameters are in [0, 1]
	public void setKeyDimensions(float left, float top, float right,
			float bottom) {
		mLeftF = left;
		mTopF = top;
		mRightF = right;
		mBottomF = bottom;
	}

	public void setKeyAttribute(int keyCode, String label, boolean repeat,
			boolean balloon) {
		mKeyCode = keyCode;
		mKeyLabel = label;

		if (repeat) {
			mKeyMask |= KEYMASK_REPEAT;
		} else {
			mKeyMask &= (~KEYMASK_REPEAT);
		}

		if (balloon) {
			mKeyMask |= KEYMASK_BALLOON;
		} else {
			mKeyMask &= (~KEYMASK_BALLOON);
		}
	}

	/**
	 * 设置副软键盘弹出框
	 * 
	 * @param popupSkbId
	 */
	public void setPopupSkbId(int popupSkbId) {
		mPopupSkbId = popupSkbId;
	}

	// Call after setKeyDimensions(). The caller guarantees that the
	// keyboard with and height are valid.
	/**
	 * 设置按键的区域
	 * 
	 * @param skbWidth
	 *            键盘的宽度
	 * @param skbHeight
	 *            键盘的高度
	 */
	public void setSkbCoreSize(int skbWidth, int skbHeight) {
		skbWidth = (int) (skbWidth * mScaleValue);
		mLeft = (int) (mLeftF * skbWidth) + mPosXOffset;
		mRight = (int) (mRightF * skbWidth) + mPosXOffset;
		mTop = (int) (mTopF * skbHeight);
		mBottom = (int) (mBottomF * skbHeight);
		Log.d("wsldwo","SoftKey   label "+this.getKeyLabel());
		Log.d("wsldwo","SoftKey setSkbCoreSize:  mLeftF "+mLeftF+" mRightF "+mRightF+" mTopF "+mTopF+" mBottomF "+mBottomF);
		Log.d("wsldwo","SoftKey setSkbCoreSize:  mLeft "+mLeft+" mRight "+mRight+" mTop "+mTop+" mBottom "+mBottom);
		Log.d("wsldwo","SoftKey   width "+this.width()+" height "+height());
	}

	public Drawable getKeyIcon() {
		return mKeyIcon;
	}

	public Drawable getKeyIconPopup() {
		if (null != mKeyIconPopup) {
			return mKeyIconPopup;
		}
		return mKeyIcon;
	}

	/**
	 * 获取按键的key code
	 * 
	 * @return
	 */
	public int getKeyCode() {
		return mKeyCode;
	}

	/**
	 * 获取按键的字符
	 * 
	 * @return
	 */
	public String getKeyLabel() {
		return mKeyLabel;
	}

	/**
	 * 大小写转换
	 * 
	 * @param upperCase
	 */
	public void changeCase(boolean upperCase) {
		if (null != mKeyLabel) {
			if (upperCase)
				mKeyLabel = mKeyLabel.toUpperCase();
			else
				mKeyLabel = mKeyLabel.toLowerCase();
		}
	}

	public Drawable getKeyBg() {
		return mKeyType.mKeyBg;
	}

	public Drawable getKeyHlBg() {
		return mKeyType.mKeyHlBg;
	}

	public int getColor() {
		return mKeyType.mColor;
	}

	public int getColorHl() {
		return mKeyType.mColorHl;
	}

	public int getColorBalloon() {
		return mKeyType.mColorBalloon;
	}

	/**
	 * 是否是系统的keycode
	 * 
	 * @return
	 */
	public boolean isKeyCodeKey() {
		if (mKeyCode > 0)
			return true;
		return false;
	}

	/**
	 * 是否是用户定义的keycode
	 * 
	 * @return
	 */
	public boolean isUserDefKey() {
		if (mKeyCode < 0)
			return true;
		return false;
	}

	/**
	 * 是否是字符按键
	 * 
	 * @return
	 */
	public boolean isUniStrKey() {
		if (null != mKeyLabel && mKeyCode == 0)
			return true;
		return false;
	}

	/**
	 * 是否需要弹出气泡
	 * 
	 * @return
	 */
	public boolean needBalloon() {
		return (mKeyMask & KEYMASK_BALLOON) != 0;
	}

	/**
	 * 是否有重复按下功能，即连续按这个按键是否执行不同的操作。
	 * 
	 * @return
	 */
	public boolean repeatable() {
		return (mKeyMask & KEYMASK_REPEAT) != 0;
	}

	public int getPopupResId() {
		return mPopupSkbId;
	}

	public int width() {
		return mRight - mLeft;
	}

	public int height() {
		return mBottom - mTop;
	}

	/**
	 * 判断坐标是否在该按键的区域内
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean moveWithinKey(int x, int y) {
		if (mLeft - MAX_MOVE_TOLERANCE_X <= x
				&& mTop - MAX_MOVE_TOLERANCE_Y <= y
				&& mRight + MAX_MOVE_TOLERANCE_X > x
				&& mBottom + MAX_MOVE_TOLERANCE_Y > y) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		String str = "\n";
		str += "  keyCode: " + String.valueOf(mKeyCode) + "\n";
		str += "  keyMask: " + String.valueOf(mKeyMask) + "\n";
		str += "  keyLabel: " + (mKeyLabel == null ? "null" : mKeyLabel) + "\n";
		str += "  popupResId: " + String.valueOf(mPopupSkbId) + "\n";
		str += "  Position: " + String.valueOf(mLeftF) + ", "
				+ String.valueOf(mTopF) + ", " + String.valueOf(mRightF) + ", "
				+ String.valueOf(mBottomF) + "\n";
		str += "  PosXY: " + String.valueOf(mLeft) + ", "
				+ String.valueOf(mTop) + ", " + String.valueOf(mRight) + ", "
				+ String.valueOf(mBottom) + "\n";
		return str;
	}
}
