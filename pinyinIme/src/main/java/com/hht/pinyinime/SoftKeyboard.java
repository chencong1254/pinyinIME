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

import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;

import com.hht.pinyinime.InputModeSwitcher.ToggleStates;

/**
 * Class used to represent a soft keyboard definition, including the height, the
 * background image, the image for high light, the keys, etc.
 * 一个软件盘的定义，包括按键的排列布局，宽度高度。
 */
public class SoftKeyboard {
	private static final String TAG = SoftKeyboard.class.getSimpleName();

	/**
	 * The XML resource id for this soft keyboard. 软键盘的xml文件ID
	 * */
	private int mSkbXmlId;

	/**
	 * Do we need to cache this soft keyboard? 是否缓存这个软件盘？
	 */
	private boolean mCacheFlag;

	/**
	 * After user switches to this soft keyboard, if this flag is true, this
	 * soft keyboard will be kept unless explicit switching operation is
	 * performed, otherwise IME will switch back to the previous keyboard layout
	 * whenever user clicks on any none-function key.
	 **/
	private boolean mStickyFlag;

	/**
	 * The cache id for this soft keyboard. It is used to identify it in the
	 * soft keyboard pool. 缓存ID
	 */
	private int mCacheId;

	/**
	 * Used to indicate whether this soft keyboard is newly loaded from an XML
	 * file or is just gotten from the soft keyboard pool.
	 * 是否是最近从xml文件导入的软键盘，或者刚刚从软键盘池中得到的软键盘？
	 */
	private boolean mNewlyLoadedFlag = true;

	/**
	 * The width of the soft keyboard. 键盘的宽度
	 */
	private int mSkbCoreWidth;

	/**
	 * The height of the soft keyboard. 键盘的高度
	 */
	private int mSkbCoreHeight; 

	/**
	 * The soft keyboard template for this soft keyboard.软键盘的模版
	 */
	private SkbTemplate mSkbTemplate;

	/**
	 * Used to indicate whether this soft keyboard is a QWERTY keyboard.
	 * 是否使用标准的软键盘
	 */
	private boolean mIsQwerty;

	/**
	 * When {@link #mIsQwerty} is true, this member is Used to indicate that the
	 * soft keyboard should be displayed in uppercase. 是否是标准键盘的大写
	 */
	private boolean mIsQwertyUpperCase;

	/**
	 * The id of the rows which are enabled. Rows with id
	 * {@link KeyRow#ALWAYS_SHOW_ROW_ID} are always enabled. 可用行的id
	 */
	private int mEnabledRowId;

	/**
	 * Rows in this soft keyboard. Each row has a id. Only matched rows will be
	 * enabled. 按键排列的行的链表，每个元素都是一行。
	 */
	private List<KeyRow> mKeyRows;

	/**
	 * Background of the soft keyboard. If it is null, the one in the soft
	 * keyboard template will be used. 软键盘的背景，如果为空，就会使用软键盘模版中的背景。
	 **/
	public Drawable mSkbBg;

	/**
	 * Background for key balloon. If it is null, the one in the soft keyboard
	 * template will be used. 气泡的背景，如果为空，就会使用软键盘模版中的气泡背景。
	 **/
	private Drawable mBalloonBg;

	/**
	 * Background for popup mini soft keyboard. If it is null, the one in the
	 * soft keyboard template will be used. 弹出框的背景，如果为空，就会使用软键盘模版中的弹出框背景。
	 **/
	private Drawable mPopupBg;

	/** The left and right margin of a key. 一个按键的左右间隔 */
	private float mKeyXMargin = 0;

	/** The top and bottom margin of a key. 一个按键的上下间隔 */
	private float mKeyYMargin = 0;

	private Rect mTmpRect = new Rect();
	 

	public SoftKeyboard(int skbXmlId, SkbTemplate skbTemplate, int skbWidth,
			int skbHeight) {
		mSkbXmlId = skbXmlId;
		mSkbTemplate = skbTemplate;
		mSkbCoreWidth = skbWidth;
		mSkbCoreHeight = skbHeight;
	}

	public void setFlags(boolean cacheFlag, boolean stickyFlag,
			boolean isQwerty, boolean isQwertyUpperCase) {
		mCacheFlag = cacheFlag;
		mStickyFlag = stickyFlag;
		mIsQwerty = isQwerty;
		mIsQwertyUpperCase = isQwertyUpperCase;
	}

	public boolean getCacheFlag() {
		return mCacheFlag;
	}

	public void setCacheId(int cacheId) {
		mCacheId = cacheId;
	}

	public boolean getStickyFlag() {
		return mStickyFlag;
	}

	public void setSkbBackground(Drawable skbBg) {
		mSkbBg = skbBg;
	}

	public void setPopupBackground(Drawable popupBg) {
		mPopupBg = popupBg;
	}

	public void setKeyBalloonBackground(Drawable balloonBg) {
		mBalloonBg = balloonBg;
	}

	public void setKeyMargins(float xMargin, float yMargin) {
		mKeyXMargin = xMargin;
		mKeyYMargin = yMargin;
	}

	public int getCacheId() {
		return mCacheId;
	}

	/**
	 * 重置软键盘，只做了清除mKeyRows列表的操作。
	 */
	public void reset() {
		if (null != mKeyRows)
			mKeyRows.clear();
	}

	public void setNewlyLoadedFlag(boolean newlyLoadedFlag) {
		mNewlyLoadedFlag = newlyLoadedFlag;
	}

	public boolean getNewlyLoadedFlag() {
		return mNewlyLoadedFlag;
	}

	/**
	 * 开始新的一行
	 * 
	 * @param rowId
	 * @param yStartingPos
	 */
	public void beginNewRow(int rowId, float yStartingPos) {
		if (null == mKeyRows)
			mKeyRows = new ArrayList<KeyRow>();
		KeyRow keyRow = new KeyRow();
		keyRow.mRowId = rowId;
		keyRow.mTopF = yStartingPos;
		keyRow.mBottomF = yStartingPos;
		keyRow.mSoftKeys = new ArrayList<SoftKey>();
		mKeyRows.add(keyRow);
	}

	/**
	 * 添加一个按键，按键是添加在最后一行中。
	 * 
	 * @param softKey
	 * @return
	 */
	public boolean addSoftKey(SoftKey softKey) { 
		if (mKeyRows.size() == 0)
			return false;
		KeyRow keyRow = mKeyRows.get(mKeyRows.size() - 1);
		if (null == keyRow)
			return false;
		List<SoftKey> softKeys = keyRow.mSoftKeys;

		softKey.setSkbCoreSize(mSkbCoreWidth, mSkbCoreHeight);
		softKeys.add(softKey);

		// 根据加入的按键的top和bottom，调整行的top和bottom
		if (softKey.mTopF < keyRow.mTopF) {
			keyRow.mTopF = softKey.mTopF;
		}
		if (softKey.mBottomF > keyRow.mBottomF) {
			keyRow.mBottomF = softKey.mBottomF;
		}
		return true;
	}

	public int getSkbXmlId() {
		return mSkbXmlId;
	}

	// Set the size of the soft keyboard core. In other words, the background's
	// padding are not counted.
	/**
	 * 设置键盘核心的宽度和高度（不包括padding），并根据新的宽度和高度，调整键盘中各行的top和bottom，调整行中的按键的尺寸。
	 * 
	 * @param skbCoreWidth
	 * @param skbCoreHeight
	 */
	public void setSkbCoreSize(int skbCoreWidth, int skbCoreHeight) {
		if (null == mKeyRows
				|| (skbCoreWidth == mSkbCoreWidth && skbCoreHeight == mSkbCoreHeight)) {
			Log.d("wsldwo","setSkbCoreSize: just return! ");
			//return;
		} 
		for (int row = 0; row < mKeyRows.size(); row++) {
			
			KeyRow keyRow = mKeyRows.get(row);
			keyRow.mBottom = (int) (skbCoreHeight * keyRow.mBottomF);
			keyRow.mTop = (int) (skbCoreHeight * keyRow.mTopF);
			
			Log.d("wsldwo","setSkbCoreSize: keyRow "+row);

			List<SoftKey> softKeys = keyRow.mSoftKeys;
			for (int i = 0; i < softKeys.size(); i++) {
				SoftKey softKey = softKeys.get(i);
				Log.d("wsldwo","setSkbCoreSize: softKey "+softKey.getKeyLabel()+" setCoreWidth "+skbCoreWidth+" "+skbCoreHeight);
				softKey.setSkbCoreSize(skbCoreWidth, skbCoreHeight);
			}
		}
		mSkbCoreWidth = skbCoreWidth;
		mSkbCoreHeight = skbCoreHeight;
	}

	public int getSkbCoreWidth() {
		return mSkbCoreWidth;
	}

	public int getSkbCoreHeight() {
		return mSkbCoreHeight;
	}  
	
	public int getSkbTotalWidth() {
		Rect padding = getPadding();
		return mSkbCoreWidth + padding.left + padding.right;
	}

	public int getSkbTotalHeight() {
		Rect padding = getPadding();
		return mSkbCoreHeight + padding.top + padding.bottom;
	}

	// TODO 不明白这个函数的作用
	public int getKeyXMargin() {
		Environment env = Environment.getInstance();
		return (int) (mKeyXMargin * mSkbCoreWidth * env.getKeyXMarginFactor());
	}

	// TODO 不明白这个函数的作用
	public int getKeyYMargin() {
		Environment env = Environment.getInstance();
		return (int) (mKeyYMargin * mSkbCoreHeight * env.getKeyYMarginFactor());
	}

	public Drawable getSkbBackground() {
		if (null != mSkbBg)
			return mSkbBg;
		return mSkbTemplate.getSkbBackground();
	}

	public Drawable getBalloonBackground() {
		if (null != mBalloonBg)
			return mBalloonBg;
		return mSkbTemplate.getBalloonBackground();
	}

	public Drawable getPopupBackground() {
		if (null != mPopupBg)
			return mPopupBg;
		return mSkbTemplate.getPopupBackground();
	}

	public int getRowNum() {
		if (null != mKeyRows) {
			return mKeyRows.size();
		}
		return 0;
	}

	public KeyRow getKeyRowForDisplay(int row) {
		if (null != mKeyRows && mKeyRows.size() > row) {
			KeyRow keyRow = mKeyRows.get(row);
			if (KeyRow.ALWAYS_SHOW_ROW_ID == keyRow.mRowId
					|| keyRow.mRowId == mEnabledRowId) {
				return keyRow;
			}
		}
		return null;
	}

	public SoftKey getKey(int row, int location) {
		if (null != mKeyRows && mKeyRows.size() > row) {
			List<SoftKey> softKeys = mKeyRows.get(row).mSoftKeys;
			if (softKeys.size() > location) {
				return softKeys.get(location);
			}
		}
		return null;
	}

	/**
	 * 根据坐标查找按键，如果坐标在某个按键区域内，就返回这个按键，如果坐标不在所有的按键区域内，返回离它最近的按键。
	 * 
	 * @优化 可以在判断坐标在某个按键区域内的时候，并且加上判断离它最近的按键，这样就只需要一次遍历就行了。
	 * @修改 去掉最近键盘查找
	 * @param x
	 * @param y
	 * @return
	 */
	public SoftKey mapToKey(int x, int y) {
		if (null == mKeyRows) {
			return null;
		}
		// If the position is inside the rectangle of a certain key, return that
		// key.
		int rowNum = mKeyRows.size();
		for (int row = 0; row < rowNum; row++) {
			KeyRow keyRow = mKeyRows.get(row);
			if (KeyRow.ALWAYS_SHOW_ROW_ID != keyRow.mRowId
					&& keyRow.mRowId != mEnabledRowId)
				continue;
			if (keyRow.mTop > y && keyRow.mBottom <= y)
				continue;

			List<SoftKey> softKeys = keyRow.mSoftKeys;
			int keyNum = softKeys.size();
			for (int i = 0; i < keyNum; i++) {
				SoftKey sKey = softKeys.get(i);
				if (sKey.mLeft <= x && sKey.mTop <= y && sKey.mRight > x
						&& sKey.mBottom > y) {
					return sKey;
				}
			}
		}

		// If the position is outside the rectangles of all keys, find the
		// nearest one.
//		SoftKey nearestKey = null;
//		float nearestDis = Float.MAX_VALUE;
//		for (int row = 0; row < rowNum; row++) {
//			KeyRow keyRow = mKeyRows.get(row);
//			if (KeyRow.ALWAYS_SHOW_ROW_ID != keyRow.mRowId
//					&& keyRow.mRowId != mEnabledRowId)
//				continue;
//			if (keyRow.mTop > y && keyRow.mBottom <= y)
//				continue;
//
//			List<SoftKey> softKeys = keyRow.mSoftKeys;
//			int keyNum = softKeys.size();
//			for (int i = 0; i < keyNum; i++) {
//				SoftKey sKey = softKeys.get(i);
//				int disx = (sKey.mLeft + sKey.mRight) / 2 - x; // 按键x坐标的中心点到x之间的距离
//				int disy = (sKey.mTop + sKey.mBottom) / 2 - y; // 按键y坐标的中心点到y之间的距离
//				float dis = disx * disx + disy * disy;
//				if (dis < nearestDis) {
//					nearestDis = dis;
//					nearestKey = sKey;
//				}
//			}
//		}
		return null;
	}

	/**
	 * 改变Qwerty键盘中每个按键的状态
	 * 
	 * @param toggle_state_id
	 *            按键的状态属性值
	 * @param upperCase
	 *            大小写
	 */
	public void switchQwertyMode(int toggle_state_id, boolean upperCase) {
		if (!mIsQwerty)
			return;

		int rowNum = mKeyRows.size();
		for (int row = 0; row < rowNum; row++) {
			KeyRow keyRow = mKeyRows.get(row);
			List<SoftKey> softKeys = keyRow.mSoftKeys;
			int keyNum = softKeys.size();
			for (int i = 0; i < keyNum; i++) {
				SoftKey sKey = softKeys.get(i);
				if (sKey instanceof SoftKeyToggle) {
					((SoftKeyToggle) sKey).enableToggleState(toggle_state_id,
							true);
				}
				if (sKey.mKeyCode >= KeyEvent.KEYCODE_A
						&& sKey.mKeyCode <= KeyEvent.KEYCODE_Z) {
					sKey.changeCase(upperCase);
				}
			}
		}
	}

	/**
	 * 改变键盘中每个按键的状态
	 * 
	 * @param toggleStateId
	 * @param resetIfNotFound
	 */
	public void enableToggleState(int toggleStateId, boolean resetIfNotFound) {
		int rowNum = mKeyRows.size();
		for (int row = 0; row < rowNum; row++) {
			KeyRow keyRow = mKeyRows.get(row);
			List<SoftKey> softKeys = keyRow.mSoftKeys;
			int keyNum = softKeys.size();
			for (int i = 0; i < keyNum; i++) {
				SoftKey sKey = softKeys.get(i);
				if (sKey instanceof SoftKeyToggle) {
					((SoftKeyToggle) sKey).enableToggleState(toggleStateId,
							resetIfNotFound);
				}
			}
		}
	}

	/**
	 * 消除键盘中按键的某一个状态
	 * 
	 * @param toggleStateId
	 * @param resetIfNotFound
	 */
	public void disableToggleState(int toggleStateId, boolean resetIfNotFound) {
		int rowNum = mKeyRows.size();
		for (int row = 0; row < rowNum; row++) {
			KeyRow keyRow = mKeyRows.get(row);
			List<SoftKey> softKeys = keyRow.mSoftKeys;
			int keyNum = softKeys.size();
			for (int i = 0; i < keyNum; i++) {
				SoftKey sKey = softKeys.get(i);
				if (sKey instanceof SoftKeyToggle) {
					((SoftKeyToggle) sKey).disableToggleState(toggleStateId,
							resetIfNotFound);
				}
			}
		}
	}

	/**
	 * 改变键盘的状态，并且根据键盘状态中的mKeyStates[]来设置每个按键。
	 * 
	 * @param toggleStates
	 *            整个键盘的状态
	 */
	public void enableToggleStates(ToggleStates toggleStates) {
		if (null == toggleStates)
			return;

		enableRow(toggleStates.mRowIdToEnable);

		boolean isQwerty = toggleStates.mQwerty;
		boolean isQwertyUpperCase = toggleStates.mQwertyUpperCase;
		boolean needUpdateQwerty = (isQwerty && mIsQwerty && (mIsQwertyUpperCase != isQwertyUpperCase));
		int states[] = toggleStates.mKeyStates;
		int statesNum = toggleStates.mKeyStatesNum;

		int rowNum = mKeyRows.size();
		for (int row = 0; row < rowNum; row++) {
			KeyRow keyRow = mKeyRows.get(row);
			if (KeyRow.ALWAYS_SHOW_ROW_ID != keyRow.mRowId
					&& keyRow.mRowId != mEnabledRowId) {
				continue;
			}
			List<SoftKey> softKeys = keyRow.mSoftKeys;
			int keyNum = softKeys.size();
			for (int keyPos = 0; keyPos < keyNum; keyPos++) {
				SoftKey sKey = softKeys.get(keyPos);
				if (sKey instanceof SoftKeyToggle) {
					for (int statePos = 0; statePos < statesNum; statePos++) {
						((SoftKeyToggle) sKey).enableToggleState(
								states[statePos], statePos == 0);
					}
					if (0 == statesNum) {
						((SoftKeyToggle) sKey).disableAllToggleStates();
					}
				}
				if (needUpdateQwerty) {
					if (sKey.mKeyCode >= KeyEvent.KEYCODE_A
							&& sKey.mKeyCode <= KeyEvent.KEYCODE_Z) {
						sKey.changeCase(isQwertyUpperCase);
					}
				}
			}
		}
		mIsQwertyUpperCase = isQwertyUpperCase;
	}

	private Rect getPadding() {
		mTmpRect.set(0, 0, 0, 0);
		Drawable skbBg = getSkbBackground();
		if (null == skbBg)
			return mTmpRect;
		skbBg.getPadding(mTmpRect);
		return mTmpRect;
	}

	/**
	 * Enable a row with the give toggle Id. Rows with other toggle ids (except
	 * the id {@link KeyRow#ALWAYS_SHOW_ROW_ID}) will be disabled.
	 * 
	 * @param rowId
	 *            The row id to enable.
	 * @return True if the soft keyboard requires redrawing.
	 */
	/**
	 * 先查找与rowId相等的行的id，如果有，就设置mEnabledRowId = rowId。
	 * 
	 * @param rowId
	 * @return
	 */
	private boolean enableRow(int rowId) {
		if (KeyRow.ALWAYS_SHOW_ROW_ID == rowId)
			return false;

		boolean enabled = false;
		int rowNum = mKeyRows.size();
		for (int row = rowNum - 1; row >= 0; row--) {
			if (mKeyRows.get(row).mRowId == rowId) {
				enabled = true;
				break;
			}
		}
		if (enabled) {
			mEnabledRowId = rowId;
		}
		return enabled;
	}

	@Override
	public String toString() {
		String str = "------------------SkbInfo----------------------\n";
		String endStr = "-----------------------------------------------\n";
		str += "Width: " + String.valueOf(mSkbCoreWidth) + "\n";
		str += "Height: " + String.valueOf(mSkbCoreHeight) + "\n";
		str += "KeyRowNum: " + mKeyRows == null ? "0" : String.valueOf(mKeyRows
				.size()) + "\n";
		if (null == mKeyRows)
			return str + endStr;
		int rowNum = mKeyRows.size();
		for (int row = 0; row < rowNum; row++) {
			KeyRow keyRow = mKeyRows.get(row);
			List<SoftKey> softKeys = keyRow.mSoftKeys;
			int keyNum = softKeys.size();
			for (int i = 0; i < softKeys.size(); i++) {
				str += "-key " + String.valueOf(i) + ":"
						+ softKeys.get(i).toString();
			}
		}
		return str + endStr;
	}

	public String toShortString() {
		return super.toString();
	}

	/**
	 * 键盘的行
	 * 
	 * @ClassName KeyRow
	 * @author keanbin
	 */
	class KeyRow {
		static final int ALWAYS_SHOW_ROW_ID = -1;
		static final int DEFAULT_ROW_ID = 0;

		List<SoftKey> mSoftKeys;
		/**
		 * If the row id is {@link #ALWAYS_SHOW_ROW_ID}, this row will always be
		 * enabled.
		 */
		int mRowId;
		float mTopF;
		float mBottomF;
		int mTop;
		int mBottom;
	}
}
