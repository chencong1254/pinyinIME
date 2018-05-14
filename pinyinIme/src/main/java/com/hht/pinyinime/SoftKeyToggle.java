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

/**
 * Class for soft keys which defined in the keyboard xml file. A soft key can be
 * a basic key or a toggling key.
 * 
 * @see com.android.inputmethod.pinyin.SoftKey
 */
/**
 * 可以变换状态的按键
 * 
 * @ClassName SoftKeyToggle
 * @author keanbin
 */
public class SoftKeyToggle extends SoftKey {
	/**
	 * The current state number is stored in the lowest 8 bits of mKeyMask, this
	 * mask is used to get the state number. If the current state is 0, the
	 * normal state is enabled; if the current state is more than 0, a toggle
	 * state in the toggle state chain will be enabled.
	 */
	private static final int KEYMASK_TOGGLE_STATE = 0x000000ff;

	private ToggleState mToggleState;

	public int getToggleStateId() {
		return (mKeyMask & KEYMASK_TOGGLE_STATE);
	}

	// The state id should be valid, and less than 255.
	// If resetIfNotFound is true and there is no such toggle state with the
	// given id, the key state will be reset.
	// If the key state is newly changed (enabled to the given state, or
	// reseted) and needs re-draw, return true.
	/**
	 * 查找该按键是否有stateId的状态，如果有就切换到这个状态。
	 * 
	 * @param stateId
	 * @param resetIfNotFound
	 *            如果没有找到与stateId的状态，是否重置mKeyMask的KEYMASK_TOGGLE_STATE位为0。
	 * @return
	 */
	public boolean enableToggleState(int stateId, boolean resetIfNotFound) {
		int oldStateId = (mKeyMask & KEYMASK_TOGGLE_STATE);
		if (oldStateId == stateId)
			return false;

		mKeyMask &= (~KEYMASK_TOGGLE_STATE);
		if (stateId > 0) {
			/*
			 * 先设置判断的stateId为当前mKeyMask，然后在getToggleState（）中根据mKeyMask进行查找，如果找到了，
			 * 就返回true，如果没有找到
			 * ，就重置mKeyMask中的KEYMASK_TOGGLE_STATE位为0，再判断resetIfNotFound是否为true，
			 * 如果不是就还原以前的oldStateId。
			 */
			mKeyMask |= (KEYMASK_TOGGLE_STATE & stateId);
			if (getToggleState() == null) {
				mKeyMask &= (~KEYMASK_TOGGLE_STATE);
				if (!resetIfNotFound && oldStateId > 0) {
					mKeyMask |= (KEYMASK_TOGGLE_STATE & oldStateId);
				}
				return resetIfNotFound;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	// The state id should be valid, and less than 255.
	// If resetIfNotFound is true and there is no such toggle state with the
	// given id, the key state will be reset.
	// If the key state is newly changed and needs re-draw, return true.
	/**
	 * 判断当前的状态是否是stateId，如果是，就重置mKeyMask的KEYMASK_TOGGLE_STATE位为0。
	 * 
	 * @param stateId
	 * @param resetIfNotFound
	 *            如果没有找到与stateId的状态，是否重置mKeyMask的KEYMASK_TOGGLE_STATE位为0。
	 * @return
	 */
	public boolean disableToggleState(int stateId, boolean resetIfNotFound) {
		int oldStateId = (mKeyMask & KEYMASK_TOGGLE_STATE);
		if (oldStateId == stateId) {
			mKeyMask &= (~KEYMASK_TOGGLE_STATE);
			return stateId != 0;
		}

		if (resetIfNotFound) {
			mKeyMask &= (~KEYMASK_TOGGLE_STATE);
			return oldStateId != 0;
		}
		return false;
	}

	// Clear any toggle state. If the key needs re-draw, return true.
	/**
	 * 重置mKeyMask的KEYMASK_TOGGLE_STATE位为0。
	 * 
	 * @return
	 */
	public boolean disableAllToggleStates() {
		int oldStateId = (mKeyMask & KEYMASK_TOGGLE_STATE);
		mKeyMask &= (~KEYMASK_TOGGLE_STATE);
		return oldStateId != 0;
	}

	@Override
	public Drawable getKeyIcon() {
		ToggleState state = getToggleState();
		if (null != state)
			return state.mKeyIcon;
		return super.getKeyIcon();
	}

	@Override
	public Drawable getKeyIconPopup() {
		ToggleState state = getToggleState();
		if (null != state) {
			if (null != state.mKeyIconPopup) {
				return state.mKeyIconPopup;
			} else {
				return state.mKeyIcon;
			}
		}
		return super.getKeyIconPopup();
	}

	@Override
	public int getKeyCode() {
		ToggleState state = getToggleState();
		if (null != state)
			return state.mKeyCode;
		return mKeyCode;
	}

	@Override
	public String getKeyLabel() {
		ToggleState state = getToggleState();
		if (null != state)
			return state.mKeyLabel;
		return mKeyLabel;
	}

	@Override
	public Drawable getKeyBg() {
		ToggleState state = getToggleState();
		if (null != state && null != state.mKeyType) {
			return state.mKeyType.mKeyBg;
		}
		return mKeyType.mKeyBg;
	}

	@Override
	public Drawable getKeyHlBg() {
		ToggleState state = getToggleState();
		if (null != state && null != state.mKeyType) {
			return state.mKeyType.mKeyHlBg;
		}
		return mKeyType.mKeyHlBg;
	}

	@Override
	public int getColor() {
		ToggleState state = getToggleState();
		if (null != state && null != state.mKeyType) {
			return state.mKeyType.mColor;
		}
		return mKeyType.mColor;
	}

	@Override
	public int getColorHl() {
		ToggleState state = getToggleState();
		if (null != state && null != state.mKeyType) {
			return state.mKeyType.mColorHl;
		}
		return mKeyType.mColorHl;
	}

	@Override
	public int getColorBalloon() {
		ToggleState state = getToggleState();
		if (null != state && null != state.mKeyType) {
			return state.mKeyType.mColorBalloon;
		}
		return mKeyType.mColorBalloon;
	}

	@Override
	public boolean isKeyCodeKey() {
		ToggleState state = getToggleState();
		if (null != state) {
			if (state.mKeyCode > 0)
				return true;
			return false;
		}
		return super.isKeyCodeKey();
	}

	@Override
	public boolean isUserDefKey() {
		ToggleState state = getToggleState();
		if (null != state) {
			if (state.mKeyCode < 0)
				return true;
			return false;
		}
		return super.isUserDefKey();
	}

	@Override
	public boolean isUniStrKey() {
		ToggleState state = getToggleState();
		if (null != state) {
			if (null != state.mKeyLabel && state.mKeyCode == 0) {
				return true;
			}
			return false;
		}
		return super.isUniStrKey();
	}

	@Override
	public boolean needBalloon() {
		ToggleState state = getToggleState();
		if (null != state) {
			return (state.mIdAndFlags & KEYMASK_BALLOON) != 0;
		}
		return super.needBalloon();
	}

	@Override
	public boolean repeatable() {
		ToggleState state = getToggleState();
		if (null != state) {
			return (state.mIdAndFlags & KEYMASK_REPEAT) != 0;
		}
		return super.repeatable();
	}

	@Override
	public void changeCase(boolean lowerCase) {
		ToggleState state = getToggleState();
		if (null != state && null != state.mKeyLabel) {
			if (lowerCase)
				state.mKeyLabel = state.mKeyLabel.toLowerCase();
			else
				state.mKeyLabel = state.mKeyLabel.toUpperCase();
		}
	}

	public ToggleState createToggleState() {
		return new ToggleState();
	}

	public boolean setToggleStates(ToggleState rootState) {
		if (null == rootState)
			return false;
		mToggleState = rootState;
		return true;
	}

	/**
	 * 判断当前的ToggleState的mIdAndFlags &
	 * KEYMASK_TOGGLE_STATE是否与stateId时相等的，如果不是就移动到下一个ToggleState再找
	 * ，直到与stateId时相等或者没有下一个ToggleState为止。
	 * 
	 * @return
	 */
	private ToggleState getToggleState() {
		int stateId = (mKeyMask & KEYMASK_TOGGLE_STATE);
		if (0 == stateId)
			return null;

		ToggleState state = mToggleState;
		while ((null != state)
				&& (state.mIdAndFlags & KEYMASK_TOGGLE_STATE) != stateId) {
			state = state.mNextState;
		}
		return state;
	}

	/**
	 * 按键变化的状态，每一个按键的变化状态都是一个单向列表，mNextState指向一下个变化的状态。
	 * 
	 * @ClassName ToggleState
	 * @author keanbin
	 */
	public class ToggleState {
		// The id should be bigger than 0;
		private int mIdAndFlags;
		public SoftKeyType mKeyType;
		public int mKeyCode;
		public Drawable mKeyIcon;
		public Drawable mKeyIconPopup;
		public String mKeyLabel;
		public ToggleState mNextState;

		public void setStateId(int stateId) {
			mIdAndFlags |= (stateId & KEYMASK_TOGGLE_STATE);
		}

		public void setStateFlags(boolean repeat, boolean balloon) {
			if (repeat) {
				mIdAndFlags |= KEYMASK_REPEAT;
			} else {
				mIdAndFlags &= (~KEYMASK_REPEAT);
			}

			if (balloon) {
				mIdAndFlags |= KEYMASK_BALLOON;
			} else {
				mIdAndFlags &= (~KEYMASK_BALLOON);
			}
		}
	}
}
