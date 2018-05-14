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
import java.util.Vector;

import com.hht.pinyinime.DragView.OnEventListener;
import com.keanbin.pinyinime.IPinyinDecoderService;
import com.keanbin.pinyinime.PinyinDecoderService;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 * Main class of the Pinyin input method. 输入法服务
 */
public class PinyinIME extends InputMethodService {
	/**
	 * TAG for debug.
	 */
	static final String TAG = "PinyinIME";
	static PinyinIME pinyinIME;
	/**
	 * If is is true, IME will simulate key events for delete key, and send the
	 * events back to the application.
	 */
	private static final boolean SIMULATE_KEY_DELETE = true;

	/**
	 * Necessary environment configurations like screen size for this IME.
	 * 该对象保存了布局的一些尺寸，它的类是单例模式。
	 */
	private Environment mEnvironment;

	/**
	 * Used to switch input mode. 输入法状态变换器
	 */
	private InputModeSwitcher mInputModeSwitcher;

	/**
	 * Soft keyboard container view to host real soft keyboard view. 软键盘集装箱
	 */
	private SkbContainer mSkbContainer;

	/**
	 * The floating container which contains the composing view. If necessary,
	 * some other view like candiates container can also be put here. 浮动视图集装箱
	 */
	private LinearLayout mFloatingContainer;

	/**
	 * View to show the composing string. 组成字符串的View，用于显示输入的拼音。
	 */
	private ComposingView mComposingView;

	/**
	 * Window to show the composing string. 用于输入拼音字符串的窗口。
	 */
	private PopupWindow mFloatingWindow;

	/**
	 * Used to show the floating window. 显示输入的拼音字符串PopupWindow 定时器
	 */
	private PopupTimer mFloatingWindowTimer = new PopupTimer();

	/**
	 * View to show candidates list. 候选词视图集装箱
	 */
	private CandidatesContainer mCandidatesContainer;

	/**
	 * Balloon used when user presses a candidate. 候选词气泡
	 */
	private BalloonHint mCandidatesBalloon;

	/**
	 * Used to notify the input method when the user touch a candidate.
	 * 当用户选择了候选词或者在候选词视图滑动了手势时的通知输入法。 实现了候选词视图的监听器CandidateViewListener。
	 */
	private ChoiceNotifier mChoiceNotifier;

	/**
	 * Used to notify gestures from soft keyboard. 软键盘的手势监听器
	 */
	private OnGestureListener mGestureListenerSkb;

	/**
	 * Used to notify gestures from candidates view. 候选词的手势监听器
	 */
	private OnGestureListener mGestureListenerCandidates;

	/**
	 * The on-screen movement gesture detector for soft keyboard. 软键盘的手势检测器
	 */
	private GestureDetector mGestureDetectorSkb;

	/**
	 * The on-screen movement gesture detector for candidates view. 候选词的手势检测器
	 */
	private GestureDetector mGestureDetectorCandidates;

	/**
	 * Option dialog to choose settings and other IMEs. 功能对话框
	 */
	private AlertDialog mOptionsDialog;

	/**
	 * Connection used to bind the decoding service. 链接
	 * 词库解码远程服务PinyinDecoderService 的监听器
	 */
	private PinyinDecoderServiceConnection mPinyinDecoderServiceConnection;

	/**
	 * The current IME status. 当前的输入法状态
	 * 
	 * @see com.android.inputmethod.pinyin.PinyinIME.ImeState
	 */
	private ImeState mImeState = ImeState.STATE_IDLE;

	/**
	 * The decoding information, include spelling(Pinyin) string, decoding
	 * result, etc. 词库解码操作对象
	 */
	private DecodingInfo mDecInfo = new DecodingInfo();

	/**
	 * For English input. 英文输入法按键处理器
	 * 
	 */
	private EnglishInputProcessor mImEn;
	
	private Dialog mDialog;
	private int mXLast,mYLast;
	private WindowManager mWindowManager;// 窗口管理者
	private View mRootView;
	
	private int mIMEHeight,mIMEWidth;
	private float mScale;

	// receive ringer mode changes
	/**
	 * 声音模式改变时的广播接收器
	 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SoundManager.getInstance(context).updateRingerMode();
		}
	};

	@Override
	public void onCreate() {
		mEnvironment = Environment.getInstance();
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onCreate.");
		}
		super.onCreate();
		pinyinIME = this;

		// 绑定词库解码远程服务PinyinDecoderService
		startPinyinDecoderService();

		mImEn = new EnglishInputProcessor();
		Settings.getInstance(PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext()));

		mInputModeSwitcher = new InputModeSwitcher(this);
		mChoiceNotifier = new ChoiceNotifier(this);
		mGestureListenerSkb = new OnGestureListener(false);
		mGestureListenerCandidates = new OnGestureListener(true);
		mGestureDetectorSkb = new GestureDetector(this, mGestureListenerSkb);
		mGestureDetectorCandidates = new GestureDetector(this,
				mGestureListenerCandidates);

		mEnvironment.onConfigurationChanged(getResources().getConfiguration(),
				this);
		
		
		mDialog = this.getWindow();//InputMethodService返回的一个输入法窗口对象，也是Dialog
		mRootView = mDialog.getWindow().getDecorView();
		if(mRootView != null){
			//mRootView.setBackgroundColor(Color.YELLOW);
		}
		mWindowManager = (WindowManager) getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);
		
		mScale = getResources().getDisplayMetrics().density;
	}

	@Override
	public void onDestroy() {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onDestroy.");
		}

		// 解绑定词库解码远程服务PinyinDecoderService
		unbindService(mPinyinDecoderServiceConnection);

		// 释放设置类的引用
		Settings.releaseInstance();

		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Environment env = Environment.getInstance();
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onConfigurationChanged");
			Log.d(TAG, "--last config: " + env.getConfiguration().toString());
			Log.d(TAG, "---new config: " + newConfig.toString());
		}
		// We need to change the local environment first so that UI components
		// can get the environment instance to handle size issues. When
		// super.onConfigurationChanged() is called, onCreateCandidatesView()
		// and onCreateInputView() will be executed if necessary.
		env.onConfigurationChanged(newConfig, this);

		// Clear related UI of the previous configuration.
		if (null != mSkbContainer) {
			mSkbContainer.dismissPopups();
		}
		if (null != mCandidatesBalloon) {
			mCandidatesBalloon.dismiss();
		}
		super.onConfigurationChanged(newConfig);

		// 重置到空闲状态
		resetToIdleState(false);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (processKey(event, 0 != event.getRepeatCount()))
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (processKey(event, true))
			return true;
		return super.onKeyUp(keyCode, event);
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

	/**
	 * 给EditText发送文本，在广播接收器MyReceiver的onReceive（）中调用。
	 * 
	 * @param text
	 */
	public void SetText(CharSequence text) {
		InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return;
		ic.beginBatchEdit();

		ic.commitText(text, 0);
		ic.endBatchEdit();

	}

	/**
	 * 按键处理函数
	 * 
	 * @param event
	 * @param realAction
	 * @return
	 */
	private boolean processKey(KeyEvent event, boolean realAction) {
		if (ImeState.STATE_BYPASS == mImeState)
			return false;

		int keyCode = event.getKeyCode();
		// SHIFT-SPACE is used to switch between Chinese and English
		// when HKB is on.
		// SHIFT + SPACE 按键组合处理
		if (KeyEvent.KEYCODE_SPACE == keyCode && event.isShiftPressed()) {
			if (!realAction)
				return true;

			updateIcon(mInputModeSwitcher.switchLanguageWithHkb());
			resetToIdleState(false);

			// 清除alt shift sym 键按住的状态
			int allMetaState = KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON
					| KeyEvent.META_ALT_RIGHT_ON | KeyEvent.META_SHIFT_ON
					| KeyEvent.META_SHIFT_LEFT_ON
					| KeyEvent.META_SHIFT_RIGHT_ON | KeyEvent.META_SYM_ON;
			getCurrentInputConnection().clearMetaKeyStates(allMetaState);
			return true;
		}

		// If HKB is on to input English, by-pass the key event so that
		// default key listener will handle it.
		// 如果是硬键盘英文输入状态，就忽略掉该按键，让默认的按键监听器去处理它。
		if (mInputModeSwitcher.isEnglishWithHkb()) {
			return false;
		}

		// 功能键处理
		if (processFunctionKeys(keyCode, realAction)) {
			return true;
		}

		int keyChar = 0;
		if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
			keyChar = keyCode - KeyEvent.KEYCODE_A + 'a';
		} else if (keyCode >= KeyEvent.KEYCODE_0
				&& keyCode <= KeyEvent.KEYCODE_9) {
			keyChar = keyCode - KeyEvent.KEYCODE_0 + '0';
		} else if (keyCode == KeyEvent.KEYCODE_COMMA) {
			keyChar = ',';
		} else if (keyCode == KeyEvent.KEYCODE_PERIOD) {
			keyChar = '.';
		} else if (keyCode == KeyEvent.KEYCODE_SPACE) {
			keyChar = ' ';
		} else if (keyCode == KeyEvent.KEYCODE_APOSTROPHE) {
			keyChar = '\'';
		}

		if (mInputModeSwitcher.isEnglishWithSkb()) {// 英语软键盘处理
			return mImEn.processKey(getCurrentInputConnection(), event,
					mInputModeSwitcher.isEnglishUpperCaseWithSkb(), realAction);
		} else if (mInputModeSwitcher.isChineseText()) {// 中文输入法模式
			if (mImeState == ImeState.STATE_IDLE
					|| mImeState == ImeState.STATE_APP_COMPLETION) {
				mImeState = ImeState.STATE_IDLE;
				return processStateIdle(keyChar, keyCode, event, realAction);
			} else if (mImeState == ImeState.STATE_INPUT) {
				return processStateInput(keyChar, keyCode, event, realAction);
			} else if (mImeState == ImeState.STATE_PREDICT) {
				return processStatePredict(keyChar, keyCode, event, realAction);
			} else if (mImeState == ImeState.STATE_COMPOSING) {
				return processStateEditComposing(keyChar, keyCode, event,
						realAction);
			}
		} else {// 符号处理
			if (0 != keyChar && realAction) {
				// 发送文本给EditText
				commitResultText(String.valueOf((char) keyChar));
			}
		}
		
		return false;
	}

	// keyCode can be from both hard key or soft key.
	/**
	 * 功能键处理函数
	 * 
	 * @param keyCodeaddView
	 * @param realAction
	 * @return
	 */
	private boolean processFunctionKeys(int keyCode, boolean realAction) {
		// Back key is used to dismiss all popup UI in a soft keyboard.
		// 后退键的处理。副软键盘弹出框显示的时候，如果realAction为true，那么就调用dismissPopupSkb（）隐藏副软键盘弹出框，显示主软键盘视图。
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isInputViewShown()) {
				if (mSkbContainer.handleBack(realAction))
					return true;
			}
		}

		// Chinese related input is handle separately.
		// 中文相关输入是单独处理的，不在这边处理。
		if (mInputModeSwitcher.isChineseText()) {
			return false;
		}

		if (null != mCandidatesContainer && mCandidatesContainer.isShown()
				&& !mDecInfo.isCandidatesListEmpty()) {// 候选词视图显示的时候
			if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
				if (!realAction)
					return true;

				// 选择当前高亮的候选词
				chooseCandidate(-1);
				return true;
			}

			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				if (!realAction)
					return true;

				// 高亮位置向上一个候选词移动或者移动到上一页的最后一个候选词的位置。
				mCandidatesContainer.activeCurseBackward();
				return true;
			}

			if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				if (!realAction)
					return true;

				// 高亮位置向下一个候选词移动或者移动到下一页的第一个候选词的位置。
				mCandidatesContainer.activeCurseForward();
				return true;
			}

			if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				if (!realAction)
					return true;

				// 到上一页候选词
				mCandidatesContainer.pageBackward(false, true);
				return true;
			}

			if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				if (!realAction)
					return true;

				// 到下一页候选词
				mCandidatesContainer.pageForward(false, true);
				return true;
			}

			// 在预报状态下的删除键处理
			if (keyCode == KeyEvent.KEYCODE_DEL
					&& ImeState.STATE_PREDICT == mImeState) {
				if (!realAction)
					return true;
				resetToIdleState(false);
				return true;
			}
		} else {// 没有候选词显示的时候

			if (keyCode == KeyEvent.KEYCODE_DEL) {
				if (!realAction)
					return true;
				if (SIMULATE_KEY_DELETE) {
					// 给EditText发送一个删除按键的按下和弹起事件。
					simulateKeyEventDownUp(keyCode);
				} else {
					// 发送删除一个字符的操作给EditText
					getCurrentInputConnection().deleteSurroundingText(1, 0);
				}
				return true;
			}
			if (keyCode == KeyEvent.KEYCODE_ENTER) {
				if (!realAction)
					return true;

				// 发送Enter键给EditText
				sendKeyChar('\n');
				return true;
			}
			if (keyCode == KeyEvent.KEYCODE_SPACE) {
				if (!realAction)
					return true;

				// 发送' '字符给EditText
				sendKeyChar(' ');
				return true;
			}
		}

		return false;
	}

	/**
	 * 当 mImeState == ImeState.STATE_IDLE 或者 mImeState ==
	 * ImeState.STATE_APP_COMPLETION 时的按键处理函数
	 * 
	 * @param keyChar
	 * @param keyCode
	 * @param event
	 * @param realAction
	 * @return
	 */
	private boolean processStateIdle(int keyChar, int keyCode, KeyEvent event,
			boolean realAction) {
		// In this status, when user presses keys in [a..z], the status will
		// change to input state.
		if (keyChar >= 'a' && keyChar <= 'z' && !event.isAltPressed()) {
			if (!realAction)
				return true;
			mDecInfo.addSplChar((char) keyChar, true);

			// 对输入的拼音进行查询
			chooseAndUpdate(-1);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DEL) {
			if (!realAction)
				return true;
			if (SIMULATE_KEY_DELETE) {
				// 模拟删除键发送给 EditText
				simulateKeyEventDownUp(keyCode);
			} else {
				// 发送删除一个字符的操作给 EditText
				getCurrentInputConnection().deleteSurroundingText(1, 0);
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			if (!realAction)
				return true;

			// 发送 ENTER 键给 EditText
			sendKeyChar('\n');
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_ALT_LEFT
				|| keyCode == KeyEvent.KEYCODE_ALT_RIGHT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_LEFT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
			return true;
		} else if (event.isAltPressed()) {
			// 获取中文全角字符
			char fullwidth_char = KeyMapDream.getChineseLabel(keyCode);
			if (0 != fullwidth_char) {
				if (realAction) {
					String result = String.valueOf(fullwidth_char);
					commitResultText(result);
				}
				return true;
			} else {
				if (keyCode >= KeyEvent.KEYCODE_A
						&& keyCode <= KeyEvent.KEYCODE_Z) {
					return true;
				}
			}
		} else if (keyChar != 0 && keyChar != '\t') {
			if (realAction) {
				if (keyChar == ',' || keyChar == '.') {
					// 发送 '\uff0c' 或者 '\u3002' 给EditText
					inputCommaPeriod("", keyChar, false, ImeState.STATE_IDLE);
				} else {
					if (0 != keyChar) {
						String result = String.valueOf((char) keyChar);
						commitResultText(result);
					}
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 当 mImeState == ImeState.STATE_INPUT 时的按键处理函数
	 * 
	 * @param keyChar
	 * @param keyCode
	 * @param event
	 * @param realAction
	 * @return
	 */
	private boolean processStateInput(int keyChar, int keyCode, KeyEvent event,
			boolean realAction) {
		// If ALT key is pressed, input alternative key. But if the
		// alternative key is quote key, it will be used for input a splitter
		// in Pinyin string.
		// 如果 ALT 被按住
		if (event.isAltPressed()) {
			if ('\'' != event.getUnicodeChar(event.getMetaState())) {
				if (realAction) {
					// 获取中文全角字符
					char fullwidth_char = KeyMapDream.getChineseLabel(keyCode);
					if (0 != fullwidth_char) {
						// 发送高亮的候选词 + 中文全角字符 给 EditView
						commitResultText(mDecInfo
								.getCurrentFullSent(mCandidatesContainer
										.getActiveCandiatePos())
								+ String.valueOf(fullwidth_char));
						resetToIdleState(false);
					}
				}
				return true;
			} else {
				keyChar = '\'';
			}
		}

		if (keyChar >= 'a' && keyChar <= 'z' || keyChar == '\''
				&& !mDecInfo.charBeforeCursorIsSeparator()
				|| keyCode == KeyEvent.KEYCODE_DEL) {
			if (!realAction)
				return true;

			// 添加输入的拼音，然后进行词库查询，或者删除输入的拼音指定的字符或字符串，然后进行词库查询。
			return processSurfaceChange(keyChar, keyCode);
		} else if (keyChar == ',' || keyChar == '.') {
			if (!realAction)
				return true;

			// 发送 '\uff0c' 或者 '\u3002' 给EditText
			inputCommaPeriod(mDecInfo.getCurrentFullSent(mCandidatesContainer
					.getActiveCandiatePos()), keyChar, true,
					ImeState.STATE_IDLE);
			return true;

		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP
				|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN
				|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (!realAction)
				return true;

			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				// 高亮位置向上一个候选词移动或者移动到上一页的最后一个候选词的位置。
				mCandidatesContainer.activeCurseBackward();
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				// 高亮位置向下一个候选词移动或者移动到下一页的第一个候选词的位置。
				mCandidatesContainer.activeCurseForward();
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				// If it has been the first page, a up key will shift
				// the state to edit composing string.
				// 到上一页候选词
				if (!mCandidatesContainer.pageBackward(false, true)) {
					mCandidatesContainer.enableActiveHighlight(false);
					changeToStateComposing(true);
					updateComposingText(true);
				}
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				// 到下一页候选词
				mCandidatesContainer.pageForward(false, true);
			}
			return true;
		} else if (keyCode >= KeyEvent.KEYCODE_1
				&& keyCode <= KeyEvent.KEYCODE_9) {
			if (!realAction)
				return true;

			int activePos = keyCode - KeyEvent.KEYCODE_1;
			int currentPage = mCandidatesContainer.getCurrentPage();
			if (activePos < mDecInfo.getCurrentPageSize(currentPage)) {
				activePos = activePos
						+ mDecInfo.getCurrentPageStart(currentPage);
				if (activePos >= 0) {
					// 选择候选词，并根据条件是否进行下一步的预报。
					chooseAndUpdate(activePos);
				}
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			if (!realAction)
				return true;
			if (mInputModeSwitcher.isEnterNoramlState()) {
				// 把输入的拼音字符串发送给EditText
				commitResultText(mDecInfo.getOrigianlSplStr().toString());
				resetToIdleState(false);
			} else {
				// 把高亮的候选词发送给EditText
				commitResultText(mDecInfo
						.getCurrentFullSent(mCandidatesContainer
								.getActiveCandiatePos()));
				// 把ENTER发送给EditText
				sendKeyChar('\n');
				resetToIdleState(false);
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
				|| keyCode == KeyEvent.KEYCODE_SPACE) {
			if (!realAction)
				return true;
			// 选择高亮的候选词
			chooseCandidate(-1);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!realAction)
				return true;
			resetToIdleState(false);
			// 关闭输入法
			requestHideSelf(0);
			return true;
		}
		return false;
	}

	/**
	 * 当 mImeState == ImeState.STATE_PREDICT 时的按键处理函数
	 * 
	 * @param keyChar
	 * @param keyCode
	 * @param event
	 * @param realAction
	 * @return
	 */
	private boolean processStatePredict(int keyChar, int keyCode,
			KeyEvent event, boolean realAction) {
		if (!realAction)
			return true;

		// If ALT key is pressed, input alternative key.
		// 按住Alt键
		if (event.isAltPressed()) {
			// 获取中文全角字符
			char fullwidth_char = KeyMapDream.getChineseLabel(keyCode);
			if (0 != fullwidth_char) {
				// 发送高亮的候选词 + 中文全角字符 给 EditView
				commitResultText(mDecInfo.getCandidate(mCandidatesContainer
						.getActiveCandiatePos())
						+ String.valueOf(fullwidth_char));
				resetToIdleState(false);
			}
			return true;
		}

		// In this status, when user presses keys in [a..z], the status will
		// change to input state.
		if (keyChar >= 'a' && keyChar <= 'z') {
			changeToStateInput(true);
			// 加一个字符进输入的拼音字符串中
			mDecInfo.addSplChar((char) keyChar, true);
			// 对输入的拼音进行查询。
			chooseAndUpdate(-1);
		} else if (keyChar == ',' || keyChar == '.') {
			// 发送 '\uff0c' 或者 '\u3002' 给EditText
			inputCommaPeriod("", keyChar, true, ImeState.STATE_IDLE);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP
				|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN
				|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				// 高亮位置向上一个候选词移动或者移动到上一页的最后一个候选词的位置。
				mCandidatesContainer.activeCurseBackward();
			}
			if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				// 高亮位置向下一个候选词移动或者移动到下一页的第一个候选词的位置。
				mCandidatesContainer.activeCurseForward();
			}
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
				// 到上一页候选词
				mCandidatesContainer.pageBackward(false, true);
			}
			if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				// 到下一页候选词
				mCandidatesContainer.pageForward(false, true);
			}
		} else if (keyCode == KeyEvent.KEYCODE_DEL) {
			resetToIdleState(false);
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			resetToIdleState(false);
			// 关闭输入法
			requestHideSelf(0);
		} else if (keyCode >= KeyEvent.KEYCODE_1
				&& keyCode <= KeyEvent.KEYCODE_9) {
			int activePos = keyCode - KeyEvent.KEYCODE_1;
			int currentPage = mCandidatesContainer.getCurrentPage();
			if (activePos < mDecInfo.getCurrentPageSize(currentPage)) {
				activePos = activePos
						+ mDecInfo.getCurrentPageStart(currentPage);
				if (activePos >= 0) {
					// 选择候选词
					chooseAndUpdate(activePos);
				}
			}
		} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			// 发生ENTER键给EditText
			sendKeyChar('\n');
			resetToIdleState(false);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
				|| keyCode == KeyEvent.KEYCODE_SPACE) {
			// 选择候选词
			chooseCandidate(-1);
		}

		return true;
	}

	/**
	 * 当 mImeState == ImeState.STATE_COMPOSING 时的按键处理函数
	 * 
	 * @param keyChar
	 * @param keyCode
	 * @param event
	 * @param realAction
	 * @return
	 */
	private boolean processStateEditComposing(int keyChar, int keyCode,
			KeyEvent event, boolean realAction) {
		if (!realAction)
			return true;

		// 获取输入的音字符串的状态
		ComposingView.ComposingStatus cmpsvStatus = mComposingView
				.getComposingStatus();

		// If ALT key is pressed, input alternative key. But if the
		// alternative key is quote key, it will be used for input a splitter
		// in Pinyin string.
		// 按住 ALT 键
		if (event.isAltPressed()) {
			if ('\'' != event.getUnicodeChar(event.getMetaState())) {
				// 获取中文全角字符
				char fullwidth_char = KeyMapDream.getChineseLabel(keyCode);
				if (0 != fullwidth_char) {
					String retStr;
					if (ComposingView.ComposingStatus.SHOW_STRING_LOWERCASE == cmpsvStatus) {
						// 获取原始的输入拼音的字符
						retStr = mDecInfo.getOrigianlSplStr().toString();
					} else {
						// 获取组合的输入拼音的字符（有可能存在选中的候选词）
						retStr = mDecInfo.getComposingStr();
					}
					// 发送文本给EditText
					commitResultText(retStr + String.valueOf(fullwidth_char));
					resetToIdleState(false);
				}
				return true;
			} else {
				keyChar = '\'';
			}
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			if (!mDecInfo.selectionFinished()) {
				changeToStateInput(true);
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			// 移动候选词的光标
			mComposingView.moveCursor(keyCode);
		} else if ((keyCode == KeyEvent.KEYCODE_ENTER && mInputModeSwitcher
				.isEnterNoramlState())
				|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER
				|| keyCode == KeyEvent.KEYCODE_SPACE) {
			if (ComposingView.ComposingStatus.SHOW_STRING_LOWERCASE == cmpsvStatus) {
				// 获取原始的输入拼音的字符
				String str = mDecInfo.getOrigianlSplStr().toString();
				if (!tryInputRawUnicode(str)) {
					// 发送文本给EditText
					commitResultText(str);
				}
			} else if (ComposingView.ComposingStatus.EDIT_PINYIN == cmpsvStatus) {
				// 获取组合的输入拼音的字符（有可能存在选中的候选词）
				String str = mDecInfo.getComposingStr();
				// 对开头或者结尾为"unicode"的字符串进行转换
				if (!tryInputRawUnicode(str)) {
					// 发送文本给EditText
					commitResultText(str);
				}
			} else {
				// 发生 组合的输入拼音的字符（有可能存在选中的候选词） 给 EditText
				commitResultText(mDecInfo.getComposingStr());
			}
			resetToIdleState(false);
		} else if (keyCode == KeyEvent.KEYCODE_ENTER
				&& !mInputModeSwitcher.isEnterNoramlState()) {
			String retStr;
			if (!mDecInfo.isCandidatesListEmpty()) {
				// 获取当前高亮的候选词
				retStr = mDecInfo.getCurrentFullSent(mCandidatesContainer
						.getActiveCandiatePos());
			} else {
				// 获取组合的输入拼音的字符（有可能存在选中的候选词）
				retStr = mDecInfo.getComposingStr();
			}
			// 发送文本给EditText
			commitResultText(retStr);
			// 发生ENTER键给EditText
			sendKeyChar('\n');
			resetToIdleState(false);
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			resetToIdleState(false);
			// 关闭输入法
			requestHideSelf(0);
			return true;
		} else {
			// 添加输入的拼音，然后进行词库查询，或者删除输入的拼音指定的字符或字符串，然后进行词库查询。
			return processSurfaceChange(keyChar, keyCode);
		}
		return true;
	}

	/**
	 * 对开头或者结尾为"unicode"的字符串进行转换
	 * 
	 * @param str
	 * @return
	 */
	private boolean tryInputRawUnicode(String str) {
		if (str.length() > 7) {
			if (str.substring(0, 7).compareTo("unicode") == 0) {// str是"unicode"开头
				try {
					// 截取"unicode"后面的字符串
					String digitStr = str.substring(7);
					int startPos = 0;
					int radix = 10;
					if (digitStr.length() > 2 && digitStr.charAt(0) == '0'
							&& digitStr.charAt(1) == 'x') {
						startPos = 2;
						radix = 16;
					}
					digitStr = digitStr.substring(startPos);
					// 取digitStr对应的整数
					int unicode = Integer.parseInt(digitStr, radix);
					if (unicode > 0) {
						char low = (char) (unicode & 0x0000ffff);
						char high = (char) ((unicode & 0xffff0000) >> 16);
						commitResultText(String.valueOf(low));
						if (0 != high) {
							commitResultText(String.valueOf(high));
						}
					}
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			} else if (str.substring(str.length() - 7, str.length()).compareTo(
					"unicode") == 0) {// str是"unicode"结尾
				String resultStr = "";
				for (int pos = 0; pos < str.length() - 7; pos++) {
					if (pos > 0) {
						resultStr += " ";
					}

					resultStr += "0x" + Integer.toHexString(str.charAt(pos));
				}
				commitResultText(String.valueOf(resultStr));
				return true;
			}
		}
		return false;
	}

	/**
	 * 添加输入的拼音，然后进行词库查询，或者删除输入的拼音指定的字符或字符串，然后进行词库查询。
	 * 
	 * @param keyChar
	 * @param keyCode
	 * @return
	 */
	private boolean processSurfaceChange(int keyChar, int keyCode) {
		if (mDecInfo.isSplStrFull() && KeyEvent.KEYCODE_DEL != keyCode) {
			return true;
		}

		if ((keyChar >= 'a' && keyChar <= 'z')
				|| (keyChar == '\'' && !mDecInfo.charBeforeCursorIsSeparator())
				|| (((keyChar >= '0' && keyChar <= '9') || keyChar == ' ') && ImeState.STATE_COMPOSING == mImeState)) {
			mDecInfo.addSplChar((char) keyChar, false);
			chooseAndUpdate(-1);
		} else if (keyCode == KeyEvent.KEYCODE_DEL) {
			mDecInfo.prepareDeleteBeforeCursor();
			chooseAndUpdate(-1);
		}
		return true;
	}

	/**
	 * 设置输入法状态为 mImeState = ImeState.STATE_COMPOSING;
	 * 
	 * @param updateUi
	 *            是否更新UI
	 */
	private void changeToStateComposing(boolean updateUi) {
		mImeState = ImeState.STATE_COMPOSING;
		if (!updateUi)
			return;

		if (null != mSkbContainer && mSkbContainer.isShown()) {
			mSkbContainer.toggleCandidateMode(true);
		}
	}

	/**
	 * 设置输入法状态为 mImeState = ImeState.STATE_INPUT;
	 * 
	 * @param updateUi
	 *            是否更新UI
	 */
	private void changeToStateInput(boolean updateUi) {
		mImeState = ImeState.STATE_INPUT;
		if (!updateUi)
			return;

		if (null != mSkbContainer && mSkbContainer.isShown()) {
			mSkbContainer.toggleCandidateMode(true);
		}
		showCandidateWindow(true);
	}

	/**
	 * 模拟按下一个按键
	 * 
	 * @param keyCode
	 */
	private void simulateKeyEventDownUp(int keyCode) {
		InputConnection ic = getCurrentInputConnection();
		if (null == ic)
			return;

		ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
		ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
	}

	/**
	 * 发送字符串给编辑框
	 * 
	 * @param resultText
	 */
	private void commitResultText(String resultText) {
		Log.i(TAG,"commitResultText...");
		InputConnection ic = getCurrentInputConnection();
		if (null != ic)
			ic.commitText(resultText, 1);
		if (null != mComposingView) {
			mComposingView.setVisibility(View.INVISIBLE);
			mFloatingContainer.setVisibility(View.INVISIBLE);
			mComposingView.invalidate();
		}
	}

	/**
	 * 设置是否显示输入拼音的view
	 * 
	 * @param visible
	 */
	private void updateComposingText(boolean visible) {
		if (!visible) {
			mComposingView.setVisibility(View.INVISIBLE);
			mFloatingContainer.setVisibility(View.INVISIBLE);
		} else {
			mComposingView.setDecodingInfo(mDecInfo, mImeState);
			mComposingView.setVisibility(View.VISIBLE);
			mFloatingContainer.setVisibility(View.VISIBLE);
		}
		mComposingView.invalidate();
	}

	/**
	 * 发送 '\uff0c' 或者 '\u3002' 给EditText
	 * 
	 * @param preEdit
	 * @param keyChar
	 * @param dismissCandWindow
	 *            是否重置候选词窗口
	 * @param nextState
	 *            mImeState的下一个状态
	 */
	private void inputCommaPeriod(String preEdit, int keyChar,
			boolean dismissCandWindow, ImeState nextState) {
		if (keyChar == ',')
			preEdit += '\uff0c';
		else if (keyChar == '.')
			preEdit += '\u3002';
		else
			return;
		commitResultText(preEdit);
		if (dismissCandWindow)
			resetCandidateWindow();
		mImeState = nextState;
	}

	/**
	 * 重置到空闲状态
	 * 
	 * @param resetInlineText
	 */
	private void resetToIdleState(boolean resetInlineText) {
		if (ImeState.STATE_IDLE == mImeState)
			return;

		mImeState = ImeState.STATE_IDLE;
		mDecInfo.reset();

		// 重置显示输入拼音字符串的 View
		if (null != mComposingView)
			mComposingView.reset();
		if (resetInlineText)
			commitResultText("");

		resetCandidateWindow();
	}

	/**
	 * 选择候选词，并根据条件是否进行下一步的预报。
	 * 
	 * @param candId
	 *            如果candId小于0 ，就对输入的拼音进行查询。
	 */
	private void chooseAndUpdate(int candId) {
		Log.i(TAG,"chooseAndUpdate... mImeState:"+mImeState);
		Log.i(TAG,"chooseAndUpdate... isChineseText:"+mInputModeSwitcher.isChineseText());
		// 不是中文输入法状态
		if (!mInputModeSwitcher.isChineseText()) {
			String choice = mDecInfo.getCandidate(candId);
			if (null != choice) {
				commitResultText(choice);
			}
			resetToIdleState(false);
			return;
		}

		if (ImeState.STATE_PREDICT != mImeState) {
			// Get result candidate list, if choice_id < 0, do a new decoding.
			// If choice_id >=0, select the candidate, and get the new candidate
			// list.
			mDecInfo.chooseDecodingCandidate(candId);
		} else {
			// Choose a prediction item.
			mDecInfo.choosePredictChoice(candId);
		}

		Log.i(TAG,"chooseAndUpdate... length:"+mDecInfo.getComposingStr().length()+" mDecInfo.getComposingStr:"+mDecInfo.getComposingStr());
		if (mDecInfo.getComposingStr().length() > 0) {
			String resultStr;
			// 获取选择了的候选词
			resultStr = mDecInfo.getComposingStrActivePart();
			Log.i(TAG,"chooseAndUpdate... resultStr:"+resultStr);
			Log.i(TAG,"chooseAndUpdate... candId:"+candId);
			Log.i(TAG,"chooseAndUpdate... mDecInfo.canDoPrediction():"+mDecInfo.canDoPrediction());
			// choiceId >= 0 means user finishes a choice selection.
			if (candId >= 0 && mDecInfo.canDoPrediction()) {
				// 发生选择了的候选词给EditText
				commitResultText(resultStr);
				// 设置输入法状态为预报
				mImeState = ImeState.STATE_PREDICT;
				// TODO 这一步是做什么？
				if (null != mSkbContainer && mSkbContainer.isShown()) {
					mSkbContainer.toggleCandidateMode(false);
				}

				// Try to get the prediction list.
				// 获取预报的候选词列表
				if (Settings.getPrediction()) {
					InputConnection ic = getCurrentInputConnection();
					if (null != ic) {
						CharSequence cs = ic.getTextBeforeCursor(3, 0);
						if (null != cs) {
							mDecInfo.preparePredicts(cs);
						}
					}
				} else {
					mDecInfo.resetCandidates();
				}

				if (mDecInfo.mCandidatesList.size() > 0) {
					showCandidateWindow(false);
				} else {
					resetToIdleState(false);
				}
			} else {
				if (ImeState.STATE_IDLE == mImeState) {
					if (mDecInfo.getSplStrDecodedLen() == 0) {
						changeToStateComposing(true);
					} else {
						changeToStateInput(true);
					}
				} else {
					if (mDecInfo.selectionFinished()) {
						changeToStateComposing(true);
					}
				}
				showCandidateWindow(true);
			}
		} else {
			resetToIdleState(false);
		}
	}

	// If activeCandNo is less than 0, get the current active candidate number
	// from candidate view, otherwise use activeCandNo.
	/**
	 * 选择候选词
	 * 
	 * @param activeCandNo
	 *            如果小于0，就选择当前高亮的候选词。
	 */
	private void chooseCandidate(int activeCandNo) {
		if (activeCandNo < 0) {
			activeCandNo = mCandidatesContainer.getActiveCandiatePos();
		}
		if (activeCandNo >= 0) {
			chooseAndUpdate(activeCandNo);
		}
	}

	/**
	 * 绑定词库解码远程服务PinyinDecoderService
	 * 
	 * @return
	 */
	private boolean startPinyinDecoderService() {
		if (null == mDecInfo.mIPinyinDecoderService) {
			Intent serviceIntent = new Intent();
			serviceIntent.setClass(this, PinyinDecoderService.class);

			if (null == mPinyinDecoderServiceConnection) {
				mPinyinDecoderServiceConnection = new PinyinDecoderServiceConnection();
			}

			// Bind service
			if (bindService(serviceIntent, mPinyinDecoderServiceConnection,
					Context.BIND_AUTO_CREATE)) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	

	/**
	 * 响应软键盘按键的处理函数。在软键盘集装箱SkbContainer中responseKeyEvent（）的调用。
	 * 软键盘集装箱SkbContainer的responseKeyEvent（）在自身类中调用。
	 * 
	 * @param sKey
	 */
	public void responseSoftKeyEvent(SoftKey sKey) {
		Log.i("carlIME","responseSoftKeyEvent...");
		if (null == sKey)
			return;

		InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return;

		int keyCode = sKey.getKeyCode();
		// Process some general keys, including KEYCODE_DEL, KEYCODE_SPACE,
		// KEYCODE_ENTER and KEYCODE_DPAD_CENTER.
		if (sKey.isKeyCodeKey()) {// 是系统的keycode
			// 功能键处理函数
			if (processFunctionKeys(keyCode, true))
				return;
		}

		if (sKey.isUserDefKey()) {// 是用户定义的keycode
			// 通过我们定义的软键盘的按键，切换输入法模式。
			updateIcon(mInputModeSwitcher.switchModeForUserKey(keyCode));
			resetToIdleState(false);
			Log.i("carlIME","mSkbContainer.updateInputMode...");
			mSkbContainer.updateInputMode();
			//add by chencong
			if(mSkbContainer.getInputLanguage()){
				dismissCandidateWindow();
			}
		} else {
			if (sKey.isKeyCodeKey()) {// 是系统的keycode
				KeyEvent eDown = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
						keyCode, 0, 0, 0, 0, KeyEvent.FLAG_SOFT_KEYBOARD);
				KeyEvent eUp = new KeyEvent(0, 0, KeyEvent.ACTION_UP, keyCode,
						0, 0, 0, 0, KeyEvent.FLAG_SOFT_KEYBOARD);

				onKeyDown(keyCode, eDown);
				onKeyUp(keyCode, eUp);
			} else if (sKey.isUniStrKey()) {// 是字符按键
				boolean kUsed = false;
				// 获取按键的字符
				String keyLabel = sKey.getKeyLabel();
				if (mInputModeSwitcher.isChineseTextWithSkb()
						&& (ImeState.STATE_INPUT == mImeState || ImeState.STATE_COMPOSING == mImeState)) {
					if (mDecInfo.length() > 0 && keyLabel.length() == 1
							&& keyLabel.charAt(0) == '\'') {
						// 加入拼音分隔符，然后进行词库查询
						processSurfaceChange('\'', 0);
						kUsed = true;
					}
				}
				if (!kUsed) {
					if (ImeState.STATE_INPUT == mImeState) {
						// 发送高亮候选词给EditText
						commitResultText(mDecInfo
								.getCurrentFullSent(mCandidatesContainer
										.getActiveCandiatePos()));
					} else if (ImeState.STATE_COMPOSING == mImeState) {
						// 发送 拼音字符串（有可能存在选中的候选词） 给EditText
						commitResultText(mDecInfo.getComposingStr());
					}

					// 发送 按键的字符 给EditText
					commitResultText(keyLabel);
					resetToIdleState(false);
				}
			}

			// If the current soft keyboard is not sticky, IME needs to go
			// back to the previous soft keyboard automatically.
			// 如果当前的软键盘不是粘性的，那么输入法需要返回上一个输入法模式。
			if (!mSkbContainer.isCurrentSkbSticky()) {
				updateIcon(mInputModeSwitcher.requestBackToPreviousSkb());
				resetToIdleState(false);
				Log.i("carlIME","isCurrentSkbSticky is false mSkbContainer.updateInputMode...");
				mSkbContainer.updateInputMode();
			}
		}
	}

	/**
	 * 显示候选词视图
	 * 
	 * @param showComposingView
	 *            是否显示输入的拼音View
	 */
	private void showCandidateWindow(boolean showComposingView) {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "Candidates window is shown. Parent = "
					+ mCandidatesContainer);
		}

		setCandidatesViewShown(true);

		if (null != mSkbContainer)
			mSkbContainer.requestLayout();

		if (null == mCandidatesContainer) {
			resetToIdleState(false);
			return;
		}

		updateComposingText(showComposingView);
		mCandidatesContainer.showCandidates(mDecInfo,
				ImeState.STATE_COMPOSING != mImeState);
		mFloatingWindowTimer.postShowFloatingWindow();
	}

	/**
	 * 关闭候选词窗口，并且关闭用于输入拼音字符串的窗口
	 */
	private void dismissCandidateWindow() {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "Candidates window is to be dismissed");
		}
		if (null == mCandidatesContainer)
			return;
		try {
			mFloatingWindowTimer.cancelShowing();
			mFloatingWindow.dismiss();
		} catch (Exception e) {
			Log.e(TAG, "Fail to show the PopupWindow.");
		}
		setCandidatesViewShown(false);

		if (null != mSkbContainer && mSkbContainer.isShown()) {
			mSkbContainer.toggleCandidateMode(false);
		}
	}

	/**
	 * 重置候选词区域
	 */
	private void resetCandidateWindow() {
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "Candidates window is to be reset");
		}
		if (null == mCandidatesContainer)
			return;
		try {
			mFloatingWindowTimer.cancelShowing();
			mFloatingWindow.dismiss();
		} catch (Exception e) {
			Log.e(TAG, "Fail to show the PopupWindow.");
		}

		if (null != mSkbContainer && mSkbContainer.isShown()) {
			mSkbContainer.toggleCandidateMode(false);
		}

		mDecInfo.resetCandidates();

		if (null != mCandidatesContainer && mCandidatesContainer.isShown()) {
			showCandidateWindow(false);
		}
	}

	/**
	 * 更新输入法服务的图标
	 * 
	 * @param iconId
	 */
	private void updateIcon(int iconId) {
		if (iconId > 0) {
			showStatusIcon(iconId);
		} else {
			hideStatusIcon();
		}
	}
	
	@Override
	public View onCreateCandidatesView() {
		Log.i("carlIME","onCreateCandidatesView...");
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onCreateCandidatesView.");
		}

		LayoutInflater inflater = getLayoutInflater();

		// 设置显示输入拼音字符串View的集装箱
		// Inflate the floating container view
		mFloatingContainer = (LinearLayout) inflater.inflate(
				R.layout.floating_container, null);
		mIMEWidth = this.getResources().getDisplayMetrics().widthPixels/2;
		Log.i("carlIME","mIMEWidth:"+mIMEWidth);
		mFloatingContainer.setPadding(mIMEWidth/20+(mIMEWidth/10*9)/100, 0, mIMEWidth/20+(mIMEWidth/10*9)/100, 0);//修正拼音气泡位置
		Log.i("carlIME","height:"+mFloatingContainer.getHeight());

		// The first child is the composing view.
		mComposingView = (ComposingView) mFloatingContainer.getChildAt(0);

		// 设置候选词集装箱
		mCandidatesContainer = (CandidatesContainer) inflater.inflate(
				R.layout.candidates_container, null);
		// Create balloon hint for candidates view. 创建候选词气泡
		mCandidatesBalloon = new BalloonHint(this, mCandidatesContainer,
				MeasureSpec.UNSPECIFIED);
		mCandidatesBalloon.setBalloonBackground(getResources().getDrawable(
				R.drawable.candidate_balloon_bg));
		mCandidatesContainer.initialize(mChoiceNotifier, mCandidatesBalloon,
				mGestureDetectorCandidates);

		//mCandidatesContainer.addView(mFloatingContainer);

		// The floating window
		if (null != mFloatingWindow && mFloatingWindow.isShowing()) {
			mFloatingWindowTimer.cancelShowing();
			mFloatingWindow.dismiss();
		}
		mFloatingWindow = new PopupWindow(this);
		mFloatingWindow.setClippingEnabled(false);
		mFloatingWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		mFloatingWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
		//mFloatingWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		//mFloatingWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		mFloatingWindow.setContentView(mFloatingContainer);//add by chencong

		//setCandidatesViewShown(true);
		
		return mCandidatesContainer;
	}
	

	@Override
	public View onCreateInputView() {
		
		Log.d("carlIME","onCreateInputView");
		
		
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onCreateInputView.");
		}
		LayoutInflater inflater = getLayoutInflater();
		mSkbContainer = (SkbContainer) inflater.inflate(R.layout.skb_container,
				null);
		mSkbContainer.setService(this);
		mSkbContainer.setInputModeSwitcher(mInputModeSwitcher);
		mSkbContainer.setGestureDetector(mGestureDetectorSkb);
		mSkbContainer.setOnEventListener(new OnEventListener(){

			@Override
			public void onTouchMove(float x, float y) {
				Log.d("wsldwo","mSkbContainer onTouchMove");
				// TODO Auto-generated method stub
				if(mRootView != null){
					Log.d("wsldwo","mRootView != null");
					//-481 481
					WindowManager.LayoutParams params = (android.view.WindowManager.LayoutParams) mRootView.getLayoutParams();

					params.x += x;
					params.y += y;

					if(params.x <-481) {
						params.x = -481;
					}

					if(params.x > 481) {
						params.x = 481;
					}

					Log.d("wsldwo","x:"+x+" y:"+y);
					Log.d("wsldwo","params.x:"+params.x+" params.y:"+params.y);
					mWindowManager.updateViewLayout(mRootView,params);

				}
			}});
		//mSkbContainer.setGravity(Gravity.LEFT|Gravity.TOP);
		//mSkbContainer.setPadding(0, 0, 0, 0);
		//mSkbContainer.setBackgroundColor(Color.BLUE);
		
		/*ViewGroup.LayoutParams lp2 = mSkbContainer.getLayoutParams();
		lp2.width = this.getResources().getDisplayMetrics().widthPixels/2;
		lp2.height = (this.getResources().getDisplayMetrics().heightPixels/2)/4*3;
		mSkbContainer.setLayoutParams(lp2);*/
		
		return mSkbContainer;
	}

	/**
	 * Input输入框出现时执行
	 * @param onStartInputView->onWindowShown
	 */
	@Override
	public void onWindowShown() {
		Log.d("carlIME","PinyinIME onWindowShown");
        // Intentionally empty
		if(mDialog != null){
			Log.d("carlIME","dialog != null");
			WindowManager.LayoutParams params = new WindowManager.LayoutParams();
			//params.x = 0;
			//params.y = 0;
			params.width = this.getResources().getDisplayMetrics().widthPixels/2; //设定好输入窗体的宽度，是通过屏蔽宽度的1/2
			params.height = this.getResources().getDisplayMetrics().heightPixels/3;
			params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL;
			params.y = dip2px(130);
			params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
			params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			params.format = PixelFormat.RGBA_8888;
			//mDialog.getWindow().setBackgroundDrawableResource(R.color.lightgrey);
			mDialog.getWindow().setAttributes(params);
			
			mIMEWidth = params.width;
			mIMEHeight = params.height;
		}
		
		/*if(mSkbContainer != null){
			mSkbContainer.setIMEWH(mIMEWidth, mIMEHeight);
			mSkbContainer.setWeight(9);
		}
		if(mCandidatesContainer != null){
			Log.d("wsldwo","onWindowShown candidates gone");
			mCandidatesContainer.setVisibility(View.GONE);
		}
		if(mCandidatesContainer != null){
			//mCandidatesContainer.setIMEWH(mIMEWidth, mIMEHeight);
			//mCandidatesContainer.setWeight(9);
			//mCandidatesContainer.setBackgroundColor(Color.WHITE);
			//mCandidatesContainer.setVisibility(View.VISIBLE);
		}
		*/
		
		
    }

    /**
     * 获取编辑框数据
     */
	@Override
	public void onStartInput(EditorInfo editorInfo, boolean restarting) {
		Log.i("carlIME","onStartInput...");
		if (mEnvironment.needDebug()) {
			Log.d(TAG,
					"onStartInput " + " ccontentType: "
							+ String.valueOf(editorInfo.inputType)
							+ " Restarting:" + String.valueOf(restarting));
		}
		updateIcon(mInputModeSwitcher.requestInputWithHkb(editorInfo));
		resetToIdleState(false);
	}

	/**
	 * Input输入框出现时执行
	 * @param onStartInputView
	 */
	@Override
	public void onStartInputView(EditorInfo editorInfo, boolean restarting) {
		Log.i("carlIME","onStartInputView...");
		if (mEnvironment.needDebug()) {
			Log.d(TAG,
					"onStartInputView " + " contentType: "
							+ String.valueOf(editorInfo.inputType)
							+ " Restarting:" + String.valueOf(restarting));
		}
		updateIcon(mInputModeSwitcher.requestInputWithSkb(editorInfo));
		resetToIdleState(false);
		mSkbContainer.updateInputMode();
		setCandidatesViewShown(false);
	}

	/**
	 * Input输入框退出消失时执行
	 * @param finishingInput
	 */
	@Override
	public void onFinishInputView(boolean finishingInput) {
		Log.i("carlIME","onFinishInputView...");
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onFinishInputView.");
		}
		if(mSkbContainer != null){
			Log.d("carlIME","mSkbContainer != null");
			mSkbContainer.setInputLanguage(true);
		}
		resetToIdleState(false);
		super.onFinishInputView(finishingInput);
	}

	@Override
	public void onFinishInput() {
		Log.i("carlIME","onFinishInput...");
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onFinishInput.");
		}
		resetToIdleState(false);
		super.onFinishInput();
	}

	@Override
	public void onFinishCandidatesView(boolean finishingInput) {
		Log.i("carlIME","onFinishCandidatesView...");
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "onFinishCandidateView.");
		}
		resetToIdleState(false);
		super.onFinishCandidatesView(finishingInput);
	}

	@Override
	public void onDisplayCompletions(CompletionInfo[] completions) {
		// TODO 该函数什么情况下被调用？
		if (!isFullscreenMode())
			return;
		if (null == completions || completions.length <= 0)
			return;
		if (null == mSkbContainer || !mSkbContainer.isShown())
			return;

		if (!mInputModeSwitcher.isChineseText()
				|| ImeState.STATE_IDLE == mImeState
				|| ImeState.STATE_PREDICT == mImeState) {
			mImeState = ImeState.STATE_APP_COMPLETION;
			// 准备从app获取候选词
			mDecInfo.prepareAppCompletions(completions);
			showCandidateWindow(false);
		}
	}

	/**
	 * 选择候选词后的处理函数。在ChoiceNotifier中实现CandidateViewListener监听器的onClickChoice（）中调用
	 * 。
	 * 
	 * @param activeCandNo
	 */
	private void onChoiceTouched(int activeCandNo) {
		Log.i(TAG,"onChoiceTouched... activeCandNo:"+activeCandNo);
		Log.i(TAG,"onChoiceTouched... mImeState:"+mImeState);
		if (mImeState == ImeState.STATE_COMPOSING) {
			changeToStateInput(true);
		} else if (mImeState == ImeState.STATE_INPUT
				|| mImeState == ImeState.STATE_PREDICT) {
			// 选择候选词
			chooseCandidate(activeCandNo);
		} else if (mImeState == ImeState.STATE_APP_COMPLETION) {
			if (null != mDecInfo.mAppCompletions && activeCandNo >= 0
					&& activeCandNo < mDecInfo.mAppCompletions.length) {
				CompletionInfo ci = mDecInfo.mAppCompletions[activeCandNo];
				if (null != ci) {
					InputConnection ic = getCurrentInputConnection();
					// 发送从APP中获取的候选词给EditText
					ic.commitCompletion(ci);
				}
			}
			resetToIdleState(false);
		}
	}

	@Override
	public void requestHideSelf(int flags) {
		Log.i("carlIME","requestHideSelf...");
		if (mEnvironment.needDebug()) {
			Log.d(TAG, "DimissSoftInput.");
		}
		dismissCandidateWindow();
		if (null != mSkbContainer && mSkbContainer.isShown()) {
			mSkbContainer.dismissPopups();
		}
		super.requestHideSelf(flags);
	}
	
	@Override
	public boolean onEvaluateFullscreenMode(){
		return false;
	}

	/**
	 * 选项菜单对话框
	 */
	public void showOptionsMenu() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setIcon(R.drawable.app_icon);
		builder.setNegativeButton(android.R.string.cancel, null);
		CharSequence itemSettings = getString(R.string.ime_settings_activity_name);
		CharSequence itemInputMethod = "";// =
											// getString(com.android.internal.R.string.inputMethod);
		builder.setItems(new CharSequence[] { itemSettings, itemInputMethod },
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface di, int position) {
						di.dismiss();
						switch (position) {
						case 0:
							launchSettings();

							break;
						case 1:
							// InputMethodManager.getInstance(PinyinIME.this)
							// .showInputMethodPicker();
							break;
						}
					}
				});
		builder.setTitle(getString(R.string.ime_name));
		mOptionsDialog = builder.create();
		Window window = mOptionsDialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.token = mSkbContainer.getWindowToken();
		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
		window.setAttributes(lp);
		window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		mOptionsDialog.show();
	}

	/**
	 * 启动系统的设置页面
	 */
	private void launchSettings() {
		Intent intent = new Intent();
		intent.setClass(PinyinIME.this, SettingsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	/**
	 * 显示输入的拼音字符串PopupWindow 定时器
	 * 
	 * @ClassName PopupTimer
	 * @author keanbin
	 */
	private class PopupTimer extends Handler implements Runnable {
		private int mParentLocation[] = new int[2];

		void postShowFloatingWindow() {
			mFloatingContainer.measure(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			mFloatingWindow.setWidth(mFloatingContainer.getMeasuredWidth());
			mFloatingWindow.setHeight(mFloatingContainer.getMeasuredHeight());
			post(this);
		}

		void cancelShowing() {
			if (mFloatingWindow.isShowing()) {
				mFloatingWindow.dismiss();
			}
			removeCallbacks(this);
		}

		public void run() {
			// 获取候选集装箱的位置
			mCandidatesContainer.getLocationInWindow(mParentLocation);

			if (!mFloatingWindow.isShowing()) {
				// 显示候选词PopupWindow
				mFloatingWindow.showAtLocation(mCandidatesContainer,
						Gravity.LEFT | Gravity.TOP, mParentLocation[0],
						mParentLocation[1] - mFloatingWindow.getHeight());
			} else {
				// 更新候选词PopupWindow
				mFloatingWindow
						.update(mParentLocation[0], mParentLocation[1]
								- mFloatingWindow.getHeight(),
								mFloatingWindow.getWidth(),
								mFloatingWindow.getHeight());
			}
			Log.i("carlIME","mFloatingWindow.getWidth():"+mFloatingWindow.getWidth());
			Log.i("carlIME","mFloatingWindow.getHeight():"+mFloatingWindow.getHeight());
		}
	}

	/**
	 * Used to notify IME that the user selects a candidate or performs an
	 * gesture. 当用户选择了候选词或者在候选词视图滑动了手势时的通知输入法。实现了候选词视图的监听器CandidateViewListener，
	 * 有选择候选词的处理函数、手势向右滑动的处理函数、手势向左滑动的处理函数 、手势向上滑动的处理函数、手势向下滑动的处理函数。
	 */
	public class ChoiceNotifier extends Handler implements
			CandidateViewListener {
		PinyinIME mIme;

		ChoiceNotifier(PinyinIME ime) {
			mIme = ime;
		}

		public void onClickChoice(int choiceId) {
			if (choiceId >= 0) {
				mIme.onChoiceTouched(choiceId);
			}
		}

		public void onToLeftGesture() {
			if (ImeState.STATE_COMPOSING == mImeState) {
				changeToStateInput(true);
			}
			mCandidatesContainer.pageForward(true, false);
		}

		public void onToRightGesture() {
			if (ImeState.STATE_COMPOSING == mImeState) {
				changeToStateInput(true);
			}
			mCandidatesContainer.pageBackward(true, false);
		}

		public void onToTopGesture() {
		}

		public void onToBottomGesture() {
		}
	}

	/**
	 * 手势监听器
	 * 
	 * @ClassName OnGestureListener
	 * @author keanbin
	 */
	public class OnGestureListener extends
			GestureDetector.SimpleOnGestureListener {
		/**
		 * When user presses and drags, the minimum x-distance to make a
		 * response to the drag event. 当用户拖拽的时候，x轴上最小的差值才可以产生拖拽事件。
		 */
		private static final int MIN_X_FOR_DRAG = 60;

		/**
		 * When user presses and drags, the minimum y-distance to make a
		 * response to the drag event.当用户拖拽的时候，y轴上最小的差值才可以产生拖拽事件。
		 */
		private static final int MIN_Y_FOR_DRAG = 40;

		/**
		 * Velocity threshold for a screen-move gesture. If the minimum
		 * x-velocity is less than it, no
		 * gesture.x轴上的手势的最小速率阀值，小于这个阀值，就不是手势。只要在滑动的期间
		 * ，有任意一段的速率小于这个值，就判断这次的滑动不是手势mNotGesture = true，就算接下去滑动的速率变高也是没用。
		 */
		static private final float VELOCITY_THRESHOLD_X1 = 0.3f;

		/**
		 * Velocity threshold for a screen-move gesture. If the maximum
		 * x-velocity is less than it, no
		 * gesture.x轴上的手势的最大速率阀值，大于这个阀值，就一定是手势，mGestureRecognized = true。
		 */
		static private final float VELOCITY_THRESHOLD_X2 = 0.7f;

		/**
		 * Velocity threshold for a screen-move gesture. If the minimum
		 * y-velocity is less than it, no
		 * gesture.y轴上的手势的最小速率阀值，小于这个阀值，就不是手势。只要在滑动的期间
		 * ，有任意一段的速率小于这个值，就判断这次的滑动不是手势mNotGesture =
		 * true，就算接下去滑动的速率变高也是没用，mGestureRecognized = true。
		 */
		static private final float VELOCITY_THRESHOLD_Y1 = 0.2f;

		/**
		 * Velocity threshold for a screen-move gesture. If the maximum
		 * y-velocity is less than it, no gesture.y轴上的手势的最大速率阀值，大于这个阀值，就一定是手势。
		 */
		static private final float VELOCITY_THRESHOLD_Y2 = 0.45f;

		/** If it false, we will not response detected gestures. 是否响应检测到的手势 */
		private boolean mReponseGestures;

		/** The minimum X velocity observed in the gesture. 能检测到的x最小速率的手势 */
		private float mMinVelocityX = Float.MAX_VALUE;

		/** The minimum Y velocity observed in the gesture. 能检测到y最小速率的手势 */
		private float mMinVelocityY = Float.MAX_VALUE;

		/**
		 * The first down time for the series of touch events for an
		 * action.第一次触摸事件的时间
		 */
		private long mTimeDown;

		/** The last time when onScroll() is called.最后一次 onScroll（）被调用的时间 */
		private long mTimeLastOnScroll;

		/**
		 * This flag used to indicate that this gesture is not a gesture.
		 * 是否不是一个手势？
		 */
		private boolean mNotGesture;

		/**
		 * This flag used to indicate that this gesture has been recognized.
		 * 是否是一个公认的手势？
		 */
		private boolean mGestureRecognized;

		public OnGestureListener(boolean reponseGestures) {
			mReponseGestures = reponseGestures;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			mMinVelocityX = Integer.MAX_VALUE;
			mMinVelocityY = Integer.MAX_VALUE;
			mTimeDown = e.getEventTime();
			mTimeLastOnScroll = mTimeDown;
			mNotGesture = false;
			mGestureRecognized = false;
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (mNotGesture)
				return false;
			if (mGestureRecognized)
				return true;

			if (Math.abs(e1.getX() - e2.getX()) < MIN_X_FOR_DRAG
					&& Math.abs(e1.getY() - e2.getY()) < MIN_Y_FOR_DRAG)
				return false;

			long timeNow = e2.getEventTime();
			long spanTotal = timeNow - mTimeDown;
			long spanThis = timeNow - mTimeLastOnScroll;
			if (0 == spanTotal)
				spanTotal = 1;
			if (0 == spanThis)
				spanThis = 1;

			// 计算总速率
			float vXTotal = (e2.getX() - e1.getX()) / spanTotal;
			float vYTotal = (e2.getY() - e1.getY()) / spanTotal;

			// The distances are from the current point to the previous one.
			// 计算这次 onScroll 的速率
			float vXThis = -distanceX / spanThis;
			float vYThis = -distanceY / spanThis;

			float kX = vXTotal * vXThis;
			float kY = vYTotal * vYThis;
			float k1 = kX + kY;
			float k2 = Math.abs(kX) + Math.abs(kY);

			// TODO 这个是什么计算公式？
			if (k1 / k2 < 0.8) {
				mNotGesture = true;
				return false;
			}
			float absVXTotal = Math.abs(vXTotal);
			float absVYTotal = Math.abs(vYTotal);
			if (absVXTotal < mMinVelocityX) {
				mMinVelocityX = absVXTotal;
			}
			if (absVYTotal < mMinVelocityY) {
				mMinVelocityY = absVYTotal;
			}

			// 如果最小的速率比规定的小，那么就不是手势。
			if (mMinVelocityX < VELOCITY_THRESHOLD_X1
					&& mMinVelocityY < VELOCITY_THRESHOLD_Y1) {
				mNotGesture = true;
				return false;
			}

			// 判断是什么手势？并调用手势处理函数。
			if (vXTotal > VELOCITY_THRESHOLD_X2
					&& absVYTotal < VELOCITY_THRESHOLD_Y2) {
				if (mReponseGestures)
					onDirectionGesture(Gravity.RIGHT);
				mGestureRecognized = true;
			} else if (vXTotal < -VELOCITY_THRESHOLD_X2
					&& absVYTotal < VELOCITY_THRESHOLD_Y2) {
				if (mReponseGestures)
					onDirectionGesture(Gravity.LEFT);
				mGestureRecognized = true;
			} else if (vYTotal > VELOCITY_THRESHOLD_Y2
					&& absVXTotal < VELOCITY_THRESHOLD_X2) {
				if (mReponseGestures)
					onDirectionGesture(Gravity.BOTTOM);
				mGestureRecognized = true;
			} else if (vYTotal < -VELOCITY_THRESHOLD_Y2
					&& absVXTotal < VELOCITY_THRESHOLD_X2) {
				if (mReponseGestures)
					onDirectionGesture(Gravity.TOP);
				mGestureRecognized = true;
			}

			mTimeLastOnScroll = timeNow;
			return mGestureRecognized;
		}

		@Override
		public boolean onFling(MotionEvent me1, MotionEvent me2,
				float velocityX, float velocityY) {
			return mGestureRecognized;
		}

		/**
		 * 手势的处理函数
		 * 
		 * @param gravity
		 *            手势的类别
		 */
		public void onDirectionGesture(int gravity) {
			if (Gravity.NO_GRAVITY == gravity) {
				return;
			}

			if (Gravity.LEFT == gravity || Gravity.RIGHT == gravity) {
				if (mCandidatesContainer.isShown()) {
					if (Gravity.LEFT == gravity) {
						mCandidatesContainer.pageForward(true, true);
					} else {
						mCandidatesContainer.pageBackward(true, true);
					}
					return;
				}
			}
		}
	}

	/**
	 * Connection used for binding to the Pinyin decoding service.
	 * 词库解码远程服务PinyinDecoderService 的监听器
	 */
	public class PinyinDecoderServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDecInfo.mIPinyinDecoderService = IPinyinDecoderService.Stub
					.asInterface(service);
		}

		public void onServiceDisconnected(ComponentName name) {
		}
	}
	

	/**
	 * 输入法状态
	 * STATE_IDLE 空闲
	 * STATE_INPUT 输入
	 * STATE_COMPOSING 组合、构成
	 * STATE_PREDICT 预备
	 */
	public enum ImeState {
		STATE_BYPASS, STATE_IDLE, STATE_INPUT, STATE_COMPOSING, STATE_PREDICT, STATE_APP_COMPLETION
	}

	/**
	 * 词库解码操作对象
	 * 
	 * @ClassName DecodingInfo
	 * @author keanbin
	 */
	public class DecodingInfo {
		/**
		 * Maximum length of the Pinyin string
		 * 最大的字符串的长度，其实只有27，因为最后一位为0，是mPyBuf[]的长度
		 */
		private static final int PY_STRING_MAX = 28;

		/**
		 * Maximum number of candidates to display in one page. 一页显示候选词的最大个数
		 */
		private static final int MAX_PAGE_SIZE_DISPLAY = 16;

		/**
		 * Spelling (Pinyin) string. 拼音字符串
		 */
		private StringBuffer mSurface;

		/**
		 * Byte buffer used as the Pinyin string parameter for native function
		 * call. 字符缓冲区作为拼音字符串参数给本地函数调用，它的长度为PY_STRING_MAX，最后一位为0
		 */
		private byte mPyBuf[];

		/**
		 * The length of surface string successfully decoded by engine.
		 * 成功解码的字符串长度
		 */
		private int mSurfaceDecodedLen;

		/**
		 * Composing string. 拼音字符串
		 */
		private String mComposingStr;

		/**
		 * Length of the active composing string. 活动的拼音字符串长度
		 */
		private int mActiveCmpsLen;

		/**
		 * Composing string for display, it is copied from mComposingStr, and
		 * add spaces between spellings.
		 * 显示的拼音字符串，是从mComposingStr复制过来的，并且在拼写之间加上了空格。
		 **/
		private String mComposingStrDisplay;

		/**
		 * Length of the active composing string for display. 显示的拼音字符串的长度
		 */
		private int mActiveCmpsDisplayLen;

		/**
		 * The first full sentence choice. 第一个完整句子，第一个候选词。
		 */
		private String mFullSent;

		/**
		 * Number of characters which have been fixed. 固定的字符的数量
		 */
		private int mFixedLen;

		/**
		 * If this flag is true, selection is finished. 是否选择完成了？
		 */
		private boolean mFinishSelection;

		/**
		 * The starting position for each spelling. The first one is the number
		 * of the real starting position elements. 每个拼写的开始位置，猜测：第一个元素是拼写的总数量？
		 */
		private int mSplStart[];

		/**
		 * Editing cursor in mSurface. 光标的位置
		 */
		private int mCursorPos;

		/**
		 * Remote Pinyin-to-Hanzi decoding engine service. 解码引擎远程服务
		 */
		private IPinyinDecoderService mIPinyinDecoderService;

		/**
		 * The complication information suggested by application. 应用的并发建议信息
		 */
		private CompletionInfo[] mAppCompletions;

		/**
		 * The total number of choices for display. The list may only contains
		 * the first part. If user tries to navigate to next page which is not
		 * in the result list, we need to get these items. 显示的可选择的总数
		 **/
		public int mTotalChoicesNum;

		/**
		 * Candidate list. The first one is the full-sentence candidate. 候选词列表
		 */
		public List<String> mCandidatesList = new Vector<String>();

		/**
		 * Element i stores the starting position of page i. 页的开始位置
		 */
		public Vector<Integer> mPageStart = new Vector<Integer>();

		/**
		 * Element i stores the number of characters to page i. 每一页的数量
		 */
		public Vector<Integer> mCnToPage = new Vector<Integer>();

		/**
		 * The position to delete in Pinyin string. If it is less than 0, IME
		 * will do an incremental search, otherwise IME will do a deletion
		 * operation. if {@link #mIsPosInSpl} is true, IME will delete the whole
		 * string for mPosDelSpl-th spelling, otherwise it will only delete
		 * mPosDelSpl-th character in the Pinyin string. 在拼音字符串中的删除位置
		 */
		public int mPosDelSpl = -1;

		/**
		 * If {@link #mPosDelSpl} is big than or equal to 0, this member is used
		 * to indicate that whether the postion is counted in spelling id or
		 * character. 如果 mPosDelSpl 大于等于 0，那么这个参数就用于表明是否是 拼写的id 或者 字符。
		 */
		public boolean mIsPosInSpl;

		public DecodingInfo() {
			mSurface = new StringBuffer();
			mSurfaceDecodedLen = 0;
		}

		/**
		 * 重置
		 */
		public void reset() {

			mSurface.delete(0, mSurface.length());
			mSurfaceDecodedLen = 0;
			mCursorPos = 0;
			mFullSent = "";
			mFixedLen = 0;
			mFinishSelection = false;
			mComposingStr = "";
			mComposingStrDisplay = "";
			mActiveCmpsLen = 0;
			mActiveCmpsDisplayLen = 0;
			Log.i(TAG,"reset mComposingStr:"+mComposingStr);
			resetCandidates();
		}

		/**
		 * 候选词列表是否为空
		 * 
		 * @return
		 */
		public boolean isCandidatesListEmpty() {
			return mCandidatesList.size() == 0;
		}

		/**
		 * 拼写的字符串是否已满
		 * 
		 * @return
		 */
		public boolean isSplStrFull() {
			if (mSurface.length() >= PY_STRING_MAX - 1)
				return true;
			return false;
		}

		/**
		 * 增加拼写字符
		 * 
		 * @param ch
		 * @param reset
		 *            拼写字符是否重置
		 */
		public void addSplChar(char ch, boolean reset) {
			if (reset) {
				mSurface.delete(0, mSurface.length());
				mSurfaceDecodedLen = 0;
				mCursorPos = 0;
				try {
					mIPinyinDecoderService.imResetSearch();
				} catch (RemoteException e) {
				}
			}
			mSurface.insert(mCursorPos, ch);
			mCursorPos++;
		}

		// Prepare to delete before cursor. We may delete a spelling char if
		// the cursor is in the range of unfixed part, delete a whole spelling
		// if the cursor in inside the range of the fixed part.
		// This function only marks the position used to delete.
		/**
		 * 删除前的准备。该函数只是标记要删除的位置。
		 */
		public void prepareDeleteBeforeCursor() {
			if (mCursorPos > 0) {
				int pos;

				for (pos = 0; pos < mFixedLen; pos++) {
					if (mSplStart[pos + 2] >= mCursorPos
							&& mSplStart[pos + 1] < mCursorPos) {
						// 删除一个拼写字符串
						mPosDelSpl = pos;
						mCursorPos = mSplStart[pos + 1];
						mIsPosInSpl = true;
						break;
					}
				}

				if (mPosDelSpl < 0) {
					// 删除一个字符
					mPosDelSpl = mCursorPos - 1;
					mCursorPos--;
					mIsPosInSpl = false;
				}
			}
		}

		/**
		 * 获取拼音字符串长度
		 * 
		 * @return
		 */
		public int length() {
			return mSurface.length();
		}

		/**
		 * 获得拼音字符串中指定位置的字符
		 * 
		 * @param index
		 * @return
		 */
		public char charAt(int index) {
			return mSurface.charAt(index);
		}

		/**
		 * 获得拼音字符串
		 * 
		 * @return
		 */
		public StringBuffer getOrigianlSplStr() {
			return mSurface;
		}

		/**
		 * 获得成功解码的字符串长度
		 * 
		 * @return
		 */
		public int getSplStrDecodedLen() {
			return mSurfaceDecodedLen;
		}

		/**
		 * 获得每个拼写字符串的开始位置
		 * 
		 * @return
		 */
		public int[] getSplStart() {
			return mSplStart;
		}

		/**
		 * 获取拼音字符串，有可能存在选中的候选词
		 * 
		 * @return
		 */
		public String getComposingStr() {
			return mComposingStr;
		}

		/**
		 * 获取活动的拼音字符串，就是选择了的候选词。
		 * 
		 * @return
		 */
		public String getComposingStrActivePart() {
			assert (mActiveCmpsLen <= mComposingStr.length());
			return mComposingStr.substring(0, mActiveCmpsLen);
		}

		/**
		 * 获得活动的拼音字符串长度
		 * 
		 * @return
		 */
		public int getActiveCmpsLen() {
			return mActiveCmpsLen;
		}

		/**
		 * 获取显示的拼音字符串
		 * 
		 * @return
		 */
		public String getComposingStrForDisplay() {
			return mComposingStrDisplay;
		}

		/**
		 * 显示的拼音字符串的长度
		 * 
		 * @return
		 */
		public int getActiveCmpsDisplayLen() {
			return mActiveCmpsDisplayLen;
		}

		/**
		 * 第一个完整句子
		 * 
		 * @return
		 */
		public String getFullSent() {
			return mFullSent;
		}

		/**
		 * 获取当前完整句子
		 * 
		 * @param activeCandPos
		 * @return
		 */
		public String getCurrentFullSent(int activeCandPos) {
			try {
				String retStr = mFullSent.substring(0, mFixedLen);
				retStr += mCandidatesList.get(activeCandPos);
				return retStr;
			} catch (Exception e) {
				return "";
			}
		}

		/**
		 * 重置候选词列表
		 */
		public void resetCandidates() {
			mCandidatesList.clear();
			mTotalChoicesNum = 0;

			mPageStart.clear();
			mPageStart.add(0);
			mCnToPage.clear();
			mCnToPage.add(0);
		}

		/**
		 * 候选词来自app，判断输入法状态 mImeState == ImeState.STATE_APP_COMPLETION。
		 * 
		 * @return
		 */
		public boolean candidatesFromApp() {
			return ImeState.STATE_APP_COMPLETION == mImeState;
		}

		/**
		 * 判断 mComposingStr.length() == mFixedLen ？
		 * 
		 * @return
		 */
		public boolean canDoPrediction() {
			Log.i(TAG,"chooseAndUpdate... mComposingStr.length():"+mComposingStr.length()+" mFixedLen:"+mFixedLen);
			return mComposingStr.length() == mFixedLen;
		}

		/**
		 * 选择是否完成
		 * 
		 * @return
		 */
		public boolean selectionFinished() {
			return mFinishSelection;
		}

		// After the user chooses a candidate, input method will do a
		// re-decoding and give the new candidate list.
		// If candidate id is less than 0, means user is inputting Pinyin,
		// not selecting any choice.
		/**
		 * 如果candId〉0，就选择一个候选词，并且重新获取一个候选词列表，选择的候选词存放在mComposingStr中，通过mDecInfo.
		 * getComposingStrActivePart()取出来。如果candId小于0 ，就对输入的拼音进行查询。
		 * 
		 * @param candId
		 */
		private void chooseDecodingCandidate(int candId) {
			if (mImeState != ImeState.STATE_PREDICT) {
				resetCandidates();
				int totalChoicesNum = 0;
				try {
					if (candId < 0) {
						if (length() == 0) {
							totalChoicesNum = 0;
						} else {
							if (mPyBuf == null)
								mPyBuf = new byte[PY_STRING_MAX];
							for (int i = 0; i < length(); i++)
								mPyBuf[i] = (byte) charAt(i); //获得拼音字符串中指定位置的字符
							mPyBuf[length()] = 0;

							if (mPosDelSpl < 0) {
								totalChoicesNum = mIPinyinDecoderService
										.imSearch(mPyBuf, length()); //根据拼音查询候选词
							} else {
								boolean clear_fixed_this_step = true;
								if (ImeState.STATE_COMPOSING == mImeState) {
									clear_fixed_this_step = false;
								}
								totalChoicesNum = mIPinyinDecoderService
										.imDelSearch(mPosDelSpl, mIsPosInSpl,
												clear_fixed_this_step); //删除指定位置的拼音后进行查询
								mPosDelSpl = -1;
							}
						}
					} else {
						totalChoicesNum = mIPinyinDecoderService
								.imChoose(candId); //获取候选词的数量
						Log.i(TAG,"totalChoicesNum:"+totalChoicesNum);
					}
				} catch (RemoteException e) {
				}
				updateDecInfoForSearch(totalChoicesNum);
			}
		}

		/**
		 * 更新查询词库后的信息
		 * 
		 * @param totalChoicesNum
		 */
		private void updateDecInfoForSearch(int totalChoicesNum) {
			mTotalChoicesNum = totalChoicesNum;
			if (mTotalChoicesNum < 0) {
				mTotalChoicesNum = 0;
				return;
			}

			try {
				String pyStr;

				mSplStart = mIPinyinDecoderService.imGetSplStart();//获取每个拼写的开始位置
				pyStr = mIPinyinDecoderService.imGetPyStr(false); //获取拼音字符串
				mSurfaceDecodedLen = mIPinyinDecoderService.imGetPyStrLen(true);//获取拼音字符串的长度
				assert (mSurfaceDecodedLen <= pyStr.length());

				mFullSent = mIPinyinDecoderService.imGetChoice(0);//获取指定位置的候选词
				mFixedLen = mIPinyinDecoderService.imGetFixedLen();//获取固定字符的长度

				// Update the surface string to the one kept by engine.
				mSurface.replace(0, mSurface.length(), pyStr);

				if (mCursorPos > mSurface.length())
					mCursorPos = mSurface.length();
				Log.i(TAG,"updateDecInfoForSearch totalChoicesNum:"+totalChoicesNum);
				Log.i(TAG,"updateDecInfoForSearch mFullSent:"+mFullSent);
				Log.i(TAG,"updateDecInfoForSearch mFixedLen:"+mFixedLen);
				Log.i(TAG,"updateDecInfoForSearch mSurface:"+mSurface);
				Log.i(TAG,"updateDecInfoForSearch mSplStart.length:"+mSplStart.length);
				Log.i(TAG,"updateDecInfoForSearch mSplStart[mFixedLen + 1]:"+mSplStart[mFixedLen + 1]);
				Log.i(TAG,"updateDecInfoForSearch mSurface:"+mSurface.substring(mSplStart[mFixedLen + 1]));
				mComposingStr = mFullSent.substring(0, mFixedLen)
						+ mSurface.substring(mSplStart[mFixedLen+1]);
				Log.i(TAG,"updateDecInfoForSearch mComposingStr:"+mComposingStr);

				mActiveCmpsLen = mComposingStr.length();
				if (mSurfaceDecodedLen > 0) {
					mActiveCmpsLen = mActiveCmpsLen
							- (mSurface.length() - mSurfaceDecodedLen);
				}

				// Prepare the display string.
				if (0 == mSurfaceDecodedLen) {
					mComposingStrDisplay = mComposingStr;
					mActiveCmpsDisplayLen = mComposingStr.length();
				} else {
					mComposingStrDisplay = mFullSent.substring(0, mFixedLen);
					for (int pos = mFixedLen + 1; pos < mSplStart.length - 1; pos++) {
						mComposingStrDisplay += mSurface.substring(
								mSplStart[pos], mSplStart[pos + 1]);
						if (mSplStart[pos + 1] < mSurfaceDecodedLen) {
							mComposingStrDisplay += " ";
						}
					}
					mActiveCmpsDisplayLen = mComposingStrDisplay.length();
					if (mSurfaceDecodedLen < mSurface.length()) {
						mComposingStrDisplay += mSurface
								.substring(mSurfaceDecodedLen);
					}
				}

				if (mSplStart.length == mFixedLen + 2) {
					mFinishSelection = true;
				} else {
					mFinishSelection = false;
				}
			} catch (RemoteException e) {
				Log.w(TAG, "PinyinDecoderService died", e);
			} catch (Exception e) {
				mTotalChoicesNum = 0;
				mComposingStr = "";
			}
			/*catch (Exception e) {
				//mTotalChoicesNum = 0;
				//mComposingStr = "";
			}*/
			Log.i(TAG,"updateDecInfoForSearch2 mComposingStr:"+mComposingStr);
			// Prepare page 0.
			if (!mFinishSelection) {
				preparePage(0);
			}
		}

		/**
		 * 选择预报候选词
		 * 
		 * @param choiceId
		 */
		private void choosePredictChoice(int choiceId) {
			Log.i(TAG,"choiceId:"+choiceId+" mTotalChoicesNum:"+mTotalChoicesNum);
			if (ImeState.STATE_PREDICT != mImeState || choiceId < 0
					|| choiceId >= mTotalChoicesNum) {
				return;
			}

			String tmp = mCandidatesList.get(choiceId);

			resetCandidates();

			mCandidatesList.add(tmp);
			mTotalChoicesNum = 1;

			mSurface.replace(0, mSurface.length(), "");
			mCursorPos = 0;
			mFullSent = tmp;
			mFixedLen = tmp.length();
			mComposingStr = mFullSent;
			Log.i(TAG,"choosePredictChoice11111111 mComposingStr:"+mComposingStr);
			mActiveCmpsLen = mFixedLen;

			mFinishSelection = true;
		}

		/**
		 * 获得指定的候选词
		 * 
		 * @param candId
		 * @return
		 */
		public String getCandidate(int candId) {
			// Only loaded items can be gotten, so we use mCandidatesList.size()
			// instead mTotalChoiceNum.
			if (candId < 0 || candId > mCandidatesList.size()) {
				return null;
			}
			return mCandidatesList.get(candId);
		}

		/**
		 * 从缓存中获取一页的候选词，然后放进mCandidatesList中。三种不同的获取方式：1、mIPinyinDecoderService.
		 * imGetChoiceList
		 * （）；2、mIPinyinDecoderService.imGetPredictList；3、从mAppCompletions[]取。
		 */
		private void getCandiagtesForCache() {
			int fetchStart = mCandidatesList.size();
			int fetchSize = mTotalChoicesNum - fetchStart;
			if (fetchSize > MAX_PAGE_SIZE_DISPLAY) {
				fetchSize = MAX_PAGE_SIZE_DISPLAY;
			}
			Log.i(TAG,"getCandiagtesForCache mImeState:"+mImeState);
			Log.i(TAG,"getCandiagtesForCache fetchStart:"+fetchStart);
			Log.i(TAG,"getCandiagtesForCache fetchSize:"+fetchSize);
			Log.i(TAG,"getCandiagtesForCache mFixedLen:"+mFixedLen);
			try {
				List<String> newList = null;
				if (ImeState.STATE_INPUT == mImeState
						|| ImeState.STATE_IDLE == mImeState
						|| ImeState.STATE_COMPOSING == mImeState) {
					newList = mIPinyinDecoderService.imGetChoiceList(
							fetchStart, fetchSize, mFixedLen);
				} else if (ImeState.STATE_PREDICT == mImeState) {
					newList = mIPinyinDecoderService.imGetPredictList(
							fetchStart, fetchSize);
				} else if (ImeState.STATE_APP_COMPLETION == mImeState) {
					newList = new ArrayList<String>();
					if (null != mAppCompletions) {
						for (int pos = fetchStart; pos < fetchSize; pos++) {
							CompletionInfo ci = mAppCompletions[pos];
							if (null != ci) {
								CharSequence s = ci.getText();
								if (null != s)
									newList.add(s.toString());
							}
						}
					}
				}
				mCandidatesList.addAll(newList);
			} catch (RemoteException e) {
				Log.w(TAG, "PinyinDecoderService died", e);
			}
		}

		/**
		 * 判断指定页是否准备好了？
		 * 
		 * @param pageNo
		 * @return
		 */
		public boolean pageReady(int pageNo) {
			// If the page number is less than 0, return false
			if (pageNo < 0)
				return false;

			// Page pageNo's ending information is not ready.
			if (mPageStart.size() <= pageNo + 1) {
				return false;
			}

			return true;
		}

		/**
		 * 准备指定页，从缓存中取出指定页的候选词。
		 * 
		 * @param pageNo
		 * @return
		 */
		public boolean preparePage(int pageNo) {
			// If the page number is less than 0, return false
			if (pageNo < 0)
				return false;

			// Make sure the starting information for page pageNo is ready.
			if (mPageStart.size() <= pageNo) {
				return false;
			}

			// Page pageNo's ending information is also ready.
			if (mPageStart.size() > pageNo + 1) {
				return true;
			}

			// If cached items is enough for page pageNo.
			if (mCandidatesList.size() - mPageStart.elementAt(pageNo) >= MAX_PAGE_SIZE_DISPLAY) {
				return true;
			}

			// Try to get more items from engine
			getCandiagtesForCache();

			// Try to find if there are available new items to display.
			// If no new item, return false;
			if (mPageStart.elementAt(pageNo) >= mCandidatesList.size()) {
				return false;
			}

			// If there are new items, return true;
			return true;
		}

		/**
		 * 准备预报候选词
		 * 
		 * @param history
		 */
		public void preparePredicts(CharSequence history) {
			if (null == history)
				return;

			resetCandidates();

			if (Settings.getPrediction()) {
				String preEdit = history.toString();
				int predictNum = 0;
				if (null != preEdit) {
					try {
						mTotalChoicesNum = mIPinyinDecoderService
								.imGetPredictsNum(preEdit);
					} catch (RemoteException e) {
						return;
					}
				}
			}

			preparePage(0);
			mFinishSelection = false;
		}

		/**
		 * 准备从app获取候选词
		 * 
		 * @param completions
		 */
		private void prepareAppCompletions(CompletionInfo completions[]) {
			resetCandidates();
			mAppCompletions = completions;
			mTotalChoicesNum = completions.length;
			preparePage(0);
			mFinishSelection = false;
			return;
		}

		/**
		 * 获取当前页的长度
		 * 
		 * @param currentPage
		 * @return
		 */
		public int getCurrentPageSize(int currentPage) {
			if (mPageStart.size() <= currentPage + 1)
				return 0;
			return mPageStart.elementAt(currentPage + 1)
					- mPageStart.elementAt(currentPage);
		}

		/**
		 * 获取当前页的开始位置
		 * 
		 * @param currentPage
		 * @return
		 */
		public int getCurrentPageStart(int currentPage) {
			if (mPageStart.size() < currentPage + 1)
				return mTotalChoicesNum;
			return mPageStart.elementAt(currentPage);
		}

		/**
		 * 是否还有下一页？
		 * 
		 * @param currentPage
		 * @return
		 */
		public boolean pageForwardable(int currentPage) {
			if (mPageStart.size() <= currentPage + 1)
				return false;
			if (mPageStart.elementAt(currentPage + 1) >= mTotalChoicesNum) {
				return false;
			}
			return true;
		}

		/**
		 * 是否有上一页
		 * 
		 * @param currentPage
		 * @return
		 */
		public boolean pageBackwardable(int currentPage) {
			if (currentPage > 0)
				return true;
			return false;
		}

		/**
		 * 光标前面的字符是否是分隔符“'”
		 * 
		 * @return
		 */
		public boolean charBeforeCursorIsSeparator() {
			int len = mSurface.length();
			if (mCursorPos > len)
				return false;
			if (mCursorPos > 0 && mSurface.charAt(mCursorPos - 1) == '\'') {
				return true;
			}
			return false;
		}

		/**
		 * 获取光标位置
		 * 
		 * @return
		 */
		public int getCursorPos() {
			return mCursorPos;
		}

		/**
		 * 获取光标在拼音字符串中的位置
		 * 
		 * @return
		 */
		public int getCursorPosInCmps() {
			int cursorPos = mCursorPos;
			int fixedLen = 0;

			for (int hzPos = 0; hzPos < mFixedLen; hzPos++) {
				if (mCursorPos >= mSplStart[hzPos + 2]) {
					cursorPos -= mSplStart[hzPos + 2] - mSplStart[hzPos + 1];
					cursorPos += 1;
				}
			}
			return cursorPos;
		}

		/**
		 * 获取光标在显示的拼音字符串中的位置
		 * 
		 * @return
		 */
		public int getCursorPosInCmpsDisplay() {
			int cursorPos = getCursorPosInCmps();
			// +2 is because: one for mSplStart[0], which is used for other
			// purpose(The length of the segmentation string), and another
			// for the first spelling which does not need a space before it.
			for (int pos = mFixedLen + 2; pos < mSplStart.length - 1; pos++) {
				if (mCursorPos <= mSplStart[pos]) {
					break;
				} else {
					cursorPos++;
				}
			}
			return cursorPos;
		}

		/**
		 * 移动光标到末尾
		 * 
		 * @param left
		 */
		public void moveCursorToEdge(boolean left) {
			if (left)
				mCursorPos = 0;
			else
				mCursorPos = mSurface.length();
		}

		// Move cursor. If offset is 0, this function can be used to adjust
		// the cursor into the bounds of the string.
		/**
		 * 移动光标
		 * 
		 * @param offset
		 */
		public void moveCursor(int offset) {
			if (offset > 1 || offset < -1)
				return;

			if (offset != 0) {
				int hzPos = 0;
				for (hzPos = 0; hzPos <= mFixedLen; hzPos++) {
					if (mCursorPos == mSplStart[hzPos + 1]) {
						if (offset < 0) {
							if (hzPos > 0) {
								offset = mSplStart[hzPos]
										- mSplStart[hzPos + 1];
							}
						} else {
							if (hzPos < mFixedLen) {
								offset = mSplStart[hzPos + 2]
										- mSplStart[hzPos + 1];
							}
						}
						break;
					}
				}
			}
			mCursorPos += offset;
			if (mCursorPos < 0) {
				mCursorPos = 0;
			} else if (mCursorPos > mSurface.length()) {
				mCursorPos = mSurface.length();
			}
		}

		/**
		 * 获取拼写字符串的数量
		 * 
		 * @return
		 */
		public int getSplNum() {
			return mSplStart[0];
		}

		/**
		 * 获取固定的字符的数量
		 * 
		 * @return
		 */
		public int getFixedLen() {
			return mFixedLen;
		}
	}
}
