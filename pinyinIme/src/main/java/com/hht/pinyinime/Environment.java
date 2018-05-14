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
import android.content.res.Configuration;
import android.view.Display;
import android.view.WindowManager;

/**
 * Global environment configurations for showing soft keyboard and candidate
 * view. All original dimension values are defined in float, and the real size
 * is calculated from the float values of and screen size. In this way, this
 * input method can work even when screen size is changed.
 * 该类保存布局的一些尺寸。比如：屏幕的宽度、屏幕的高度
 * 、按键的高度、候选词区域的高度、按键气泡宽度比按键宽度大的差值、按键气泡高度比按键高度大的差值、正常按键中文本的大小
 * 、功能按键中文本的大小、正常按键气泡中文本的大小、功能按键气泡中文本的大小。
 */
public class Environment {
	/**
	 * The key height for portrait mode. It is relative to the screen height.
	 * 竖屏按键高度，值是相对于屏幕高度。
	 */
	private static final float KEY_HEIGHT_RATIO_PORTRAIT = 0.105f;

	/**
	 * The key height for landscape mode. It is relative to the screen height.
	 * 横屏按键高度，值是相对于屏幕高度。
	 */
	private static final float KEY_HEIGHT_RATIO_LANDSCAPE = 0.147f;

	/**
	 * The height of the candidates area for portrait mode. It is relative to
	 * screen height. 竖屏候选词区域的高度，值是相对于屏幕高度。
	 */
	private static final float CANDIDATES_AREA_HEIGHT_RATIO_PORTRAIT = 0.084f;

	/**
	 * The height of the candidates area for portrait mode. It is relative to
	 * screen height. 横屏候选词区域高度，值是相对于屏幕高度。
	 */
	private static final float CANDIDATES_AREA_HEIGHT_RATIO_LANDSCAPE = 0.125f;

	/**
	 * How much should the balloon width be larger than width of the real key.
	 * It is relative to the smaller one of screen width and height.
	 * 猜测：点击软键盘按钮时弹出来的气泡大于按键的宽度的差值，值是相对于屏幕高度和宽度较小的那一个。
	 */
	private static final float KEY_BALLOON_WIDTH_PLUS_RATIO = 0.08f;

	/**
	 * How much should the balloon height be larger than that of the real key.
	 * It is relative to the smaller one of screen width and height.
	 * 猜测：点击软键盘按钮时弹出来的气泡大于按键的高度的差值，值是相对于屏幕高度和宽度较小的那一个。
	 */
	private static final float KEY_BALLOON_HEIGHT_PLUS_RATIO = 0.07f;

	/**
	 * The text size for normal keys. It is relative to the smaller one of
	 * screen width and height. 正常按键的文本的大小，值是相对于屏幕高度和宽度较小的那一个。
	 */
	private static final float NORMAL_KEY_TEXT_SIZE_RATIO = 0.075f;

	/**
	 * The text size for function keys. It is relative to the smaller one of
	 * screen width and height. 功能按键的文本的大小，值是相对于屏幕高度和宽度较小的那一个。
	 */
	private static final float FUNCTION_KEY_TEXT_SIZE_RATIO = 0.055f;

	/**
	 * The text size balloons of normal keys. It is relative to the smaller one
	 * of screen width and height. 正常按键弹出的气泡的文本的大小，值是相对于屏幕高度和宽度较小的那一个。
	 */
	private static final float NORMAL_BALLOON_TEXT_SIZE_RATIO = 0.14f;

	/**
	 * The text size balloons of function keys. It is relative to the smaller
	 * one of screen width and height. 功能按键弹出的气泡的文本的大小，值是相对于屏幕高度和宽度较小的那一个。
	 */
	private static final float FUNCTION_BALLOON_TEXT_SIZE_RATIO = 0.085f;

	/**
	 * The configurations are managed in a singleton. 该类的实例，该类采用设计模式的单例模式。
	 */
	private static Environment mInstance;

	private int mScreenWidth; // 屏幕的宽度
	private int mScreenHeight; // 屏幕的高度
	private int mKeyHeight; // 按键的高度
	private int mCandidatesAreaHeight; // 候选词区域的高度
	private int mKeyBalloonWidthPlus; // 按键气泡宽度比按键宽度大的差值
	private int mKeyBalloonHeightPlus; // 按键气泡高度比按键高度大的差值
	private int mNormalKeyTextSize; // 正常按键中文本的大小
	private int mFunctionKeyTextSize; // 功能按键中文本的大小
	private int mNormalBalloonTextSize; // 正常按键气泡中文本的大小
	private int mFunctionBalloonTextSize; // 功能按键气泡中文本的大小
	private Configuration mConfig = new Configuration();
	private boolean mDebug = false;

	private Environment() {
	}

	public static Environment getInstance() {
		if (null == mInstance) {
			mInstance = new Environment();
		}
		return mInstance;
	}

	public void onConfigurationChanged(Configuration newConfig, Context context) {
		if (mConfig.orientation != newConfig.orientation) {
			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			Display d = wm.getDefaultDisplay();
			mScreenWidth = d.getWidth();
			mScreenHeight = d.getHeight();

			int scale;
			if (mScreenHeight > mScreenWidth) {
				mKeyHeight = (int) (mScreenHeight * KEY_HEIGHT_RATIO_PORTRAIT);
				mCandidatesAreaHeight = (int) (mScreenHeight * CANDIDATES_AREA_HEIGHT_RATIO_PORTRAIT);
				scale = mScreenWidth;
			} else {
				mKeyHeight = (int) (mScreenHeight * KEY_HEIGHT_RATIO_LANDSCAPE);
				mCandidatesAreaHeight = (int) (mScreenHeight * CANDIDATES_AREA_HEIGHT_RATIO_LANDSCAPE);
				scale = mScreenHeight;
			}
			mNormalKeyTextSize = (int) (scale * NORMAL_KEY_TEXT_SIZE_RATIO);
			mFunctionKeyTextSize = (int) (scale * FUNCTION_KEY_TEXT_SIZE_RATIO);
			mNormalBalloonTextSize = (int) (scale * NORMAL_BALLOON_TEXT_SIZE_RATIO);
			mFunctionBalloonTextSize = (int) (scale * FUNCTION_BALLOON_TEXT_SIZE_RATIO);
			mKeyBalloonWidthPlus = (int) (scale * KEY_BALLOON_WIDTH_PLUS_RATIO);
			mKeyBalloonHeightPlus = (int) (scale * KEY_BALLOON_HEIGHT_PLUS_RATIO);
		}

		mConfig.updateFrom(newConfig);
	}

	public Configuration getConfiguration() {
		return mConfig;
	}

	public int getScreenWidth() {
		return mScreenWidth;
	}

	public int getScreenHeight() {
		return mScreenHeight;
	}

	public int getHeightForCandidates() {
		return mCandidatesAreaHeight;
	}

	public float getKeyXMarginFactor() {
		return 1.0f;
	}

	public float getKeyYMarginFactor() {
		if (Configuration.ORIENTATION_LANDSCAPE == mConfig.orientation) {
			return 0.7f;
		}
		return 1.0f;
	}

	public int getKeyHeight() {
		return mKeyHeight;
	}

	public int getKeyBalloonWidthPlus() {
		return mKeyBalloonWidthPlus;
	}

	public int getKeyBalloonHeightPlus() {
		return mKeyBalloonHeightPlus;
	}

	public int getSkbHeight() {
		if (Configuration.ORIENTATION_PORTRAIT == mConfig.orientation) {
			return mKeyHeight * 4;
		} else if (Configuration.ORIENTATION_LANDSCAPE == mConfig.orientation) {
			return mKeyHeight * 4;
		}
		return 0;
	}

	/**
	 * 获得按键的文本大小
	 * 
	 * @param isFunctionKey
	 *            是否是功能键
	 * @return
	 */
	public int getKeyTextSize(boolean isFunctionKey) {
		if (isFunctionKey) {
			return mFunctionKeyTextSize;
		} else {
			return mNormalKeyTextSize;
		}
	}

	public int getBalloonTextSize(boolean isFunctionKey) {
		if (isFunctionKey) {
			return mFunctionBalloonTextSize;
		} else {
			return mNormalBalloonTextSize;
		}
	}

	public boolean hasHardKeyboard() {
		if (mConfig.keyboard == Configuration.KEYBOARD_NOKEYS
				|| mConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
			return false;
		}
		return true;
	}

	public boolean needDebug() {
		return mDebug;
	}
}
