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

import android.content.res.Resources;
import android.view.inputmethod.EditorInfo;

import com.hht.pinyinime.SoftKeyboard.KeyRow;

/**
 * 
 * 输入法模式转换器。设置输入法的软键盘。
 * 
 * @author keanbin
 * 
 */
public class InputModeSwitcher {
	/**
	 * User defined key code, used by soft keyboard.
	 * 用户定义的key的code，用于软键盘。shift键的code。
	 */
	private static final int USERDEF_KEYCODE_SHIFT_1 = -1;

	/**
	 * User defined key code, used by soft keyboard. 语言键的code
	 */
	private static final int USERDEF_KEYCODE_LANG_2 = -2;

	/**
	 * User defined key code, used by soft keyboard. “？123”键的code，作用是与数字键盘的切换。
	 */
	private static final int USERDEF_KEYCODE_SYM_3 = -3;

	/**
	 * User defined key code, used by soft keyboard.
	 */
	public static final int USERDEF_KEYCODE_PHONE_SYM_4 = -4;

	/**
	 * User defined key code, used by soft keyboard.
	 */
	private static final int USERDEF_KEYCODE_MORE_SYM_5 = -5;

	/**
	 * User defined key code, used by soft keyboard. 微笑按键的code，比如中文时，语言键的上面那个按键。
	 */
	private static final int USERDEF_KEYCODE_SMILEY_6 = -6;

	/**
	 * Bits used to indicate soft keyboard layout. If none bit is set, the
	 * current input mode does not require a soft keyboard.
	 * 第8位指明软键盘的布局。如果最8位为0，那么就表明当前输入法模式不需要软键盘。
	 **/
	private static final int MASK_SKB_LAYOUT = 0xf0000000;

	/**
	 * A kind of soft keyboard layout. An input mode should be anded with
	 * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout. 指明标准的传统键盘
	 */
	private static final int MASK_SKB_LAYOUT_QWERTY = 0x10000000;

	/**
	 * A kind of soft keyboard layout. An input mode should be anded with
	 * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout. 指明符号软键盘一
	 */
	private static final int MASK_SKB_LAYOUT_SYMBOL1 = 0x20000000;

	/**
	 * A kind of soft keyboard layout. An input mode should be anded with
	 * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout. 指明符号软键盘二
	 */
	private static final int MASK_SKB_LAYOUT_SYMBOL2 = 0x30000000;

	/**
	 * A kind of soft keyboard layout. An input mode should be anded with
	 * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout. 指明微笑软键盘
	 */
	private static final int MASK_SKB_LAYOUT_SMILEY = 0x40000000;

	/**
	 * A kind of soft keyboard layout. An input mode should be anded with
	 * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout. 指明电话软键盘
	 */
	private static final int MASK_SKB_LAYOUT_PHONE = 0x50000000;

	/**
	 * Used to indicate which language the current input mode is in. If the
	 * current input mode works with a none-QWERTY soft keyboard, these bits are
	 * also used to get language information. For example, a Chinese symbol soft
	 * keyboard and an English one are different in an icon which is used to
	 * tell user the language information. BTW, the smiley soft keyboard mode
	 * should be set with {@link #MASK_LANGUAGE_CN} because it can only be
	 * launched from Chinese QWERTY soft keyboard, and it has Chinese icon on
	 * soft keyboard. 第7位指明语言。
	 */
	private static final int MASK_LANGUAGE = 0x0f000000;

	/**
	 * Used to indicate the current language. An input mode should be anded with
	 * {@link #MASK_LANGUAGE} to get this information. 指明中文语言。
	 */
	private static final int MASK_LANGUAGE_CN = 0x01000000;

	/**
	 * Used to indicate the current language. An input mode should be anded with
	 * {@link #MASK_LANGUAGE} to get this information. 指明英文语言。
	 */
	private static final int MASK_LANGUAGE_EN = 0x02000000;

	/**
	 * Used to indicate which case the current input mode is in. For example,
	 * English QWERTY has lowercase and uppercase. For the Chinese QWERTY, these
	 * bits are ignored. For phone keyboard layout, these bits can be
	 * {@link #MASK_CASE_UPPER} to request symbol page for phone soft keyboard.
	 * 第6位指明软键盘当前的状态，比如高（大写），低（小写）。
	 */
	private static final int MASK_CASE = 0x00f00000;

	/**
	 * Used to indicate the current case information. An input mode should be
	 * anded with {@link #MASK_CASE} to get this information. 指明软键盘状态为低（小写）。
	 */
	private static final int MASK_CASE_LOWER = 0x00100000;

	/**
	 * Used to indicate the current case information. An input mode should be
	 * anded with {@link #MASK_CASE} to get this information. 指明软键盘状态为高（大写）。
	 */
	private static final int MASK_CASE_UPPER = 0x00200000;

	/**
	 * Mode for inputing Chinese with soft keyboard. 中文标准软键盘模式
	 */
	public static final int MODE_SKB_CHINESE = (MASK_SKB_LAYOUT_QWERTY | MASK_LANGUAGE_CN);

	/**
	 * Mode for inputing basic symbols for Chinese mode with soft keyboard.
	 * 中文符号软键盘一模式
	 */
	public static final int MODE_SKB_SYMBOL1_CN = (MASK_SKB_LAYOUT_SYMBOL1 | MASK_LANGUAGE_CN);

	/**
	 * Mode for inputing more symbols for Chinese mode with soft keyboard.
	 * 中文符号软键盘二模式
	 */
	public static final int MODE_SKB_SYMBOL2_CN = (MASK_SKB_LAYOUT_SYMBOL2 | MASK_LANGUAGE_CN);

	/**
	 * Mode for inputing English lower characters with soft keyboard. 英文小写软键盘模式
	 */
	public static final int MODE_SKB_ENGLISH_LOWER = (MASK_SKB_LAYOUT_QWERTY
			| MASK_LANGUAGE_EN | MASK_CASE_LOWER);

	/**
	 * Mode for inputing English upper characters with soft keyboard. 英文大写软键盘模式
	 */
	public static final int MODE_SKB_ENGLISH_UPPER = (MASK_SKB_LAYOUT_QWERTY
			| MASK_LANGUAGE_EN | MASK_CASE_UPPER);

	/**
	 * Mode for inputing basic symbols for English mode with soft keyboard.
	 * 英文符号软键盘一模式
	 */
	public static final int MODE_SKB_SYMBOL1_EN = (MASK_SKB_LAYOUT_SYMBOL1 | MASK_LANGUAGE_EN);

	/**
	 * Mode for inputing more symbols for English mode with soft keyboard.
	 * 英文符号软键盘二模式
	 */
	public static final int MODE_SKB_SYMBOL2_EN = (MASK_SKB_LAYOUT_SYMBOL2 | MASK_LANGUAGE_EN);

	/**
	 * Mode for inputing smileys with soft keyboard. 中文笑脸软键盘模式
	 */
	public static final int MODE_SKB_SMILEY = (MASK_SKB_LAYOUT_SMILEY | MASK_LANGUAGE_CN);

	/**
	 * Mode for inputing phone numbers. 电话号码软键盘模式
	 */
	public static final int MODE_SKB_PHONE_NUM = (MASK_SKB_LAYOUT_PHONE);

	/**
	 * Mode for inputing phone numbers. 电话号码大写软键盘模式
	 */
	public static final int MODE_SKB_PHONE_SYM = (MASK_SKB_LAYOUT_PHONE | MASK_CASE_UPPER);

	/**
	 * Mode for inputing Chinese with a hardware keyboard. 中文硬键盘模式。（即不需要软键盘）
	 */
	public static final int MODE_HKB_CHINESE = (MASK_LANGUAGE_CN);

	/**
	 * Mode for inputing English with a hardware keyboard 英文硬键盘模式。（即不需要软键盘）
	 */
	public static final int MODE_HKB_ENGLISH = (MASK_LANGUAGE_EN);

	/**
	 * Unset mode. 未设置输入法模式。
	 */
	public static final int MODE_UNSET = 0;

	/**
	 * Maximum toggle states for a soft keyboard.
	 * 一个软键盘的切换状态的最大数量。这里的切换状态是指要显示的按键的状态
	 * ，所以这些状态不可能有两个同时出现在一个按键当中。如果有两个同时出现在一个按键当中，那就 不知道要显示哪一个了。
	 */
	public static final int MAX_TOGGLE_STATES = 4;

	/**
	 * The input mode for the current edit box. 当前输入法的模式
	 */
	private int mInputMode = MODE_UNSET;

	/**
	 * Used to remember previous input mode. When user enters an edit field, the
	 * previous input mode will be tried. If the previous mode can not be used
	 * for the current situation (For example, previous mode is a soft keyboard
	 * mode to input symbols, and we have a hardware keyboard for the current
	 * situation), {@link #mRecentLauageInputMode} will be tried. 前一个输入法的模式
	 **/
	private int mPreviousInputMode = MODE_SKB_CHINESE;

	/**
	 * Used to remember recent mode to input language. 最近的语言输入法模式
	 */
	private int mRecentLauageInputMode = MODE_SKB_CHINESE;

	/**
	 * Editor information of the current edit box. 当前编辑框的 EditorInfo 。
	 */
	private EditorInfo mEditorInfo;

	/**
	 * Used to indicate required toggling operations.
	 * 控制当前输入法模式软键盘布局要显示的按键切换状态和要显示的行ID。比如当前软键盘布局中
	 * ，有一个按键有默认状态、和两个切换状态，ToggleStates中的mKeyStates[]保存的就是当前要显示的切换状态
	 * ，如果按键的两个切换状态都没有在mKeyStates[]中
	 * ，那么按键就显示默认状态，如果两个切换状态中有一个在mKeyStates[]中，就显示那个切换状态
	 * 。注意：绝对不可能有一个按键的两个或两个以上的切换状态同时在mKeyStates
	 * []中。ToggleStates不仅控制按键的显示状态，还可以直接控制一行按键的显示
	 * 。mRowIdToEnable保存的就是可显示的行的ID（每行的ID不是唯一的 ，一个ID同时赋值给多行）。只有ID为
	 * mRowIdToEnable 和 ALWAYS_SHOW_ROW_ID 的行才会被显示出来。
	 */
	private ToggleStates mToggleStates = new ToggleStates();

	/**
	 * The current field is a short message field? 当前的字段是否是一个短语字段？ 当
	 * editorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION ==
	 * EditorInfo.TYPE_MASK_VARIATION 时，被设置为true，否则为false。
	 */
	private boolean mShortMessageField;

	/**
	 * Is return key in normal state? 是否是正常状态下的 Enter 键？
	 */
	private boolean mEnterKeyNormal = true;

	/**
	 * Current icon. 0 for none icon. 当前输入法的图标。显示在信号栏的图标。
	 */
	int mInputIcon = R.drawable.ime_pinyin;

	/**
	 * IME service. 输入法服务
	 */
	private PinyinIME mImeService;

	/**
	 * Key toggling state for Chinese mode. 软键盘中的按键的切换状态的ID：中文状态。
	 */
	private int mToggleStateCn;

	/**
	 * Key toggling state for Chinese mode with candidates.
	 * 软键盘中的按键的切换状态的ID：中文候选词状态。
	 */
	private int mToggleStateCnCand;

	/**
	 * Key toggling state for English lowwercase mode. 软键盘中的按键的切换状态的ID：英文小写状态。
	 */
	private int mToggleStateEnLower;

	/**
	 * Key toggling state for English upppercase mode. 软键盘中的按键的切换状态的ID：英文大写状态。
	 */
	private int mToggleStateEnUpper;

	/**
	 * Key toggling state for English symbol mode for the first page.
	 * 软键盘中的按键的切换状态的ID：英文符号一状态。
	 */
	private int mToggleStateEnSym1;

	/**
	 * Key toggling state for English symbol mode for the second page.
	 * 软键盘中的按键的切换状态的ID：英文符号二状态。
	 */
	private int mToggleStateEnSym2;

	/**
	 * Key toggling state for smiley mode. 软键盘中的按键的切换状态的ID：笑脸状态。
	 */
	private int mToggleStateSmiley;

	/**
	 * Key toggling state for phone symbol mode. 软键盘中的按键的切换状态的ID：电话号码状态。
	 */
	private int mToggleStatePhoneSym;

	/**
	 * Key toggling state for GO action of ENTER key.
	 * 软键盘中的按键的切换状态的ID：Enter键作为go操作状态。
	 */
	private int mToggleStateGo;

	/**
	 * Key toggling state for SEARCH action of ENTER key.
	 * 软键盘中的按键的切换状态的ID：Enter键作为搜索操作状态。
	 */
	private int mToggleStateSearch;

	/**
	 * Key toggling state for SEND action of ENTER key.
	 * 软键盘中的按键的切换状态的ID：Enter键作为发送操作状态。
	 */
	private int mToggleStateSend;

	/**
	 * Key toggling state for NEXT action of ENTER key.
	 * 软键盘中的按键的切换状态的ID：Enter键作为下一步操作状态。
	 */
	private int mToggleStateNext;

	/**
	 * Key toggling state for SEND action of ENTER key.
	 * 软键盘中的按键的切换状态的ID：Enter键作为完成操作状态。
	 */
	private int mToggleStateDone;

	/**
	 * QWERTY row toggling state for Chinese input. 软键盘中的行的ID：中文状态。
	 */
	private int mToggleRowCn;

	/**
	 * QWERTY row toggling state for English input. 软键盘中的行的ID：英文状态。
	 */
	private int mToggleRowEn;

	/**
	 * QWERTY row toggling state for URI input. 软键盘中的行的ID：Uri输入状态。
	 */
	private int mToggleRowUri;

	/**
	 * QWERTY row toggling state for email address input. 软键盘中的行的ID：邮件地址输入状态。
	 */
	private int mToggleRowEmailAddress;

	/**
	 * 控制当前输入法模式软键盘布局要显示的按键切换状态和要显示的行ID的管理类。比如当前软键盘布局中
	 * ，有一个按键有默认状态、和两个切换状态，ToggleStates中的mKeyStates[]保存的就是当前要显示的切换状态
	 * ，如果按键的两个切换状态都没有在mKeyStates[]中
	 * ，那么按键就显示默认状态，如果两个切换状态中有一个在mKeyStates[]中，就显示那个切换状态
	 * 。注意：绝对不可能有一个按键的两个或两个以上的切换状态同时在mKeyStates
	 * []中。ToggleStates不仅控制按键的显示状态，还可以直接控制一行按键的显示
	 * 。mRowIdToEnable保存的就是可显示的行的ID（每行的ID不是唯一的 ，一个ID同时赋值给多行）。只有ID为
	 * mRowIdToEnable 和 ALWAYS_SHOW_ROW_ID 的行才会被显示出来。
	 * 
	 * @ClassName ToggleStates
	 * @author keanbin
	 */
	class ToggleStates {
		/**
		 * If it is true, this soft keyboard is a QWERTY one. 是否是标准键盘
		 */
		boolean mQwerty;

		/**
		 * If {@link #mQwerty} is true, this variable is used to decide the
		 * letter case of the QWERTY keyboard. 是否是标准键盘大写模式
		 */
		boolean mQwertyUpperCase;

		/**
		 * The id of enabled row in the soft keyboard. Refer to
		 * {@link com.android.inputmethod.pinyin.SoftKeyboard.KeyRow} for
		 * details. 软键盘中要显示的行的ID。只有ID为 mRowIdToEnable 和 ALWAYS_SHOW_ROW_ID
		 * 的行才会被显示出来。
		 */
		public int mRowIdToEnable;

		/**
		 * Used to store all other toggle states for the current input mode.
		 * 这里保持此刻要显示的按键的状态，所以这些状态不可能有两个同时出现在一个按键当中。如果有两个同时出现在一个按键当中，那就
		 * 不知道要显示哪一个了。
		 */
		public int mKeyStates[] = new int[MAX_TOGGLE_STATES];

		/**
		 * Number of states to toggle. 按键切换状态的数量。mKeyStates[]的有用的长度。
		 */
		public int mKeyStatesNum;
	}

	public InputModeSwitcher(PinyinIME imeService) {
		mImeService = imeService;
		Resources r = mImeService.getResources();

		// 初始化按键各种切换状态的ID 和 行的ID
		mToggleStateCn = Integer.parseInt(r.getString(R.string.toggle_cn));
		mToggleStateCnCand = Integer.parseInt(r
				.getString(R.string.toggle_cn_cand));
		mToggleStateEnLower = Integer.parseInt(r
				.getString(R.string.toggle_en_lower));
		mToggleStateEnUpper = Integer.parseInt(r
				.getString(R.string.toggle_en_upper));
		mToggleStateEnSym1 = Integer.parseInt(r
				.getString(R.string.toggle_en_sym1));
		mToggleStateEnSym2 = Integer.parseInt(r
				.getString(R.string.toggle_en_sym2));
		mToggleStateSmiley = Integer.parseInt(r
				.getString(R.string.toggle_smiley));
		mToggleStatePhoneSym = Integer.parseInt(r
				.getString(R.string.toggle_phone_sym));

		mToggleStateGo = Integer
				.parseInt(r.getString(R.string.toggle_enter_go));
		mToggleStateSearch = Integer.parseInt(r
				.getString(R.string.toggle_enter_search));
		mToggleStateSend = Integer.parseInt(r
				.getString(R.string.toggle_enter_send));
		mToggleStateNext = Integer.parseInt(r
				.getString(R.string.toggle_enter_next));
		mToggleStateDone = Integer.parseInt(r
				.getString(R.string.toggle_enter_done));

		mToggleRowCn = Integer.parseInt(r.getString(R.string.toggle_row_cn));
		mToggleRowEn = Integer.parseInt(r.getString(R.string.toggle_row_en));
		mToggleRowUri = Integer.parseInt(r.getString(R.string.toggle_row_uri));
		mToggleRowEmailAddress = Integer.parseInt(r
				.getString(R.string.toggle_row_emailaddress));
	}

	/**
	 * 获取当前的输入法模式
	 * 
	 * @有效值 
	 *      MODE_UNSET（未设置输入法模式）、MODE_SKB_CHINESE（中文标准软键盘模式）、MODE_SKB_SYMBOL1_CN
	 *      （中文符号软键盘一模式）、MODE_SKB_SYMBOL2_CN（中文符号软键盘二模式）、
	 *      MODE_SKB_ENGLISH_LOWER
	 *      （英文小写软键盘模式）、MODE_SKB_ENGLISH_UPPER（英文大写软键盘模式
	 *      ）、MODE_SKB_SYMBOL1_EN（英文符号软键盘一模式
	 *      ）、MODE_SKB_SYMBOL2_EN（英文符号软键盘二模式）、MODE_SKB_SMILEY
	 *      （中文笑脸软键盘模式）、MODE_SKB_PHONE_NUM
	 *      （电话号码软键盘模式）、MODE_SKB_PHONE_SYM（电话号码大写软键盘模式
	 *      ）、MODE_HKB_CHINESE（中文硬键盘模式）、MODE_HKB_ENGLISH（英文硬键盘模式）。
	 * @return
	 */
	public int getInputMode() {
		return mInputMode;
	}

	/**
	 * 获取控制当前输入法模式软键盘布局的按键切换状态和可显示行ID的对象。
	 * 
	 * @return
	 */
	public ToggleStates getToggleStates() {
		return mToggleStates;
	}

	/**
	 * 更加软键盘 LAYOUT 获取软键盘布局文件资源ID
	 * 
	 * @return
	 */
	public int getSkbLayout() {
		int layout = (mInputMode & MASK_SKB_LAYOUT);

		switch (layout) {
		case MASK_SKB_LAYOUT_QWERTY:
			return R.xml.skb_qwerty;
		case MASK_SKB_LAYOUT_SYMBOL1:
			return R.xml.skb_sym1;
		case MASK_SKB_LAYOUT_SYMBOL2:
			return R.xml.skb_sym2;
		case MASK_SKB_LAYOUT_SMILEY:
			return R.xml.skb_smiley;
		case MASK_SKB_LAYOUT_PHONE:
			return R.xml.skb_phone;
		}
		return 0;
	}

	/**
	 * 切换硬键盘的语言模式，返回切换后的语言模式的图标。
	 * 先设置新的输入法语言是中文模式，再判断当前的输入法语言模式是否是中文模式，是的话，就改成英文模式。
	 * 
	 * @return the icon to update
	 */
	public int switchLanguageWithHkb() {
		int newInputMode = MODE_HKB_CHINESE;
		mInputIcon = R.drawable.ime_pinyin;

		if (MODE_HKB_CHINESE == mInputMode) {
			newInputMode = MODE_HKB_ENGLISH;
			mInputIcon = R.drawable.ime_en;
		}

		saveInputMode(newInputMode);
		return mInputIcon;
	}

	/**
	 * 通过我们定义的软键盘的按键，切换输入法模式。
	 * 
	 * @param userKey
	 * @return the icon to update.
	 */
	public int switchModeForUserKey(int userKey) {
		int newInputMode = MODE_UNSET;

		if (USERDEF_KEYCODE_LANG_2 == userKey) {
			// 语言键：显示中文或者英文、中符、英符的键
			if (MODE_SKB_CHINESE == mInputMode) {
				newInputMode = MODE_SKB_ENGLISH_LOWER;
			} else if (MODE_SKB_ENGLISH_LOWER == mInputMode
					|| MODE_SKB_ENGLISH_UPPER == mInputMode) {
				newInputMode = MODE_SKB_CHINESE;
			} else if (MODE_SKB_SYMBOL1_CN == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL1_EN;
			} else if (MODE_SKB_SYMBOL1_EN == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL1_CN;
			} else if (MODE_SKB_SYMBOL2_CN == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL2_EN;
			} else if (MODE_SKB_SYMBOL2_EN == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL2_CN;
			} else if (MODE_SKB_SMILEY == mInputMode) {
				newInputMode = MODE_SKB_CHINESE;
			}
		} else if (USERDEF_KEYCODE_SYM_3 == userKey) {
			// 系统键：显示“?123”的键
			if (MODE_SKB_CHINESE == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL1_CN;
			} else if (MODE_SKB_ENGLISH_UPPER == mInputMode
					|| MODE_SKB_ENGLISH_LOWER == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL1_EN;
			} else if (MODE_SKB_SYMBOL1_EN == mInputMode
					|| MODE_SKB_SYMBOL2_EN == mInputMode) {
				newInputMode = MODE_SKB_ENGLISH_LOWER;
			} else if (MODE_SKB_SYMBOL1_CN == mInputMode
					|| MODE_SKB_SYMBOL2_CN == mInputMode) {
				newInputMode = MODE_SKB_CHINESE;
			} else if (MODE_SKB_SMILEY == mInputMode) {
				newInputMode = MODE_SKB_SYMBOL1_CN;
			}
		} else if (USERDEF_KEYCODE_SHIFT_1 == userKey) {
			// shift键：显示“，” 或者 大小写图标的按键。
			if (MODE_SKB_ENGLISH_LOWER == mInputMode) {
				newInputMode = MODE_SKB_ENGLISH_UPPER;
			} else if (MODE_SKB_ENGLISH_UPPER == mInputMode) {
				newInputMode = MODE_SKB_ENGLISH_LOWER;
			}
		} else if (USERDEF_KEYCODE_MORE_SYM_5 == userKey) {
			// 更多系统键，显示“ALT”的按键
			int sym = (MASK_SKB_LAYOUT & mInputMode);
			if (MASK_SKB_LAYOUT_SYMBOL1 == sym) {
				sym = MASK_SKB_LAYOUT_SYMBOL2;
			} else {
				sym = MASK_SKB_LAYOUT_SYMBOL1;
			}
			newInputMode = ((mInputMode & (~MASK_SKB_LAYOUT)) | sym);
		} else if (USERDEF_KEYCODE_SMILEY_6 == userKey) {
			// 笑脸键：显示机器人笑脸图标的按键
			if (MODE_SKB_CHINESE == mInputMode) {
				newInputMode = MODE_SKB_SMILEY;
			} else {
				newInputMode = MODE_SKB_CHINESE;
			}
		} else if (USERDEF_KEYCODE_PHONE_SYM_4 == userKey) {
			// 电话键：显示“*#{”或者“123”的按键
			if (MODE_SKB_PHONE_NUM == mInputMode) {
				newInputMode = MODE_SKB_PHONE_SYM;
			} else {
				newInputMode = MODE_SKB_PHONE_NUM;
			}
		}

		if (newInputMode == mInputMode || MODE_UNSET == newInputMode) {
			return mInputIcon;
		}

		// 保存新的输入法模式
		saveInputMode(newInputMode);
		// 准备切换输入法状态
		prepareToggleStates(true);
		return mInputIcon;
	}

	/**
	 * 根据编辑框的 EditorInfo 信息获取硬键盘的输入法模式。
	 * 
	 * @param editorInfo
	 * @return the icon to update.
	 */
	public int requestInputWithHkb(EditorInfo editorInfo) {
		mShortMessageField = false;
		boolean english = false;
		int newInputMode = MODE_HKB_CHINESE;

		switch (editorInfo.inputType & EditorInfo.TYPE_MASK_CLASS) {
		case EditorInfo.TYPE_CLASS_NUMBER:
		case EditorInfo.TYPE_CLASS_PHONE:
		case EditorInfo.TYPE_CLASS_DATETIME:
			english = true;
			break;
		case EditorInfo.TYPE_CLASS_TEXT:
			int v = editorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION;
			if (v == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
					|| v == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
					|| v == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
					|| v == EditorInfo.TYPE_TEXT_VARIATION_URI) {
				english = true;
			} else if (v == EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE) {
				mShortMessageField = true;
			}
			break;
		default:
		}

		if (english) {
			// If the application request English mode, we switch to it.
			newInputMode = MODE_HKB_ENGLISH;
		} else {
			// If the application do not request English mode, we will
			// try to keep the previous mode to input language text.
			// Because there is not soft keyboard, we need discard all
			// soft keyboard related information from the previous language
			// mode.
			// 如果最近一次的输入法模式是中文，那么就改为中文。
			if ((mRecentLauageInputMode & MASK_LANGUAGE) == MASK_LANGUAGE_CN) {
				newInputMode = MODE_HKB_CHINESE;
			} else {
				newInputMode = MODE_HKB_ENGLISH;
			}
		}
		mEditorInfo = editorInfo;
		saveInputMode(newInputMode);
		prepareToggleStates(false);
		return mInputIcon;
	}

	/**
	 * 根据编辑框的 EditorInfo 信息获取软键盘的输入法模式。
	 * 
	 * @param editorInfo
	 * @return the icon to update.
	 */
	public int requestInputWithSkb(EditorInfo editorInfo) {
		mShortMessageField = false;

		int newInputMode = MODE_SKB_CHINESE;

		switch (editorInfo.inputType & EditorInfo.TYPE_MASK_CLASS) {
		case EditorInfo.TYPE_CLASS_NUMBER:
		case EditorInfo.TYPE_CLASS_DATETIME:
			newInputMode = MODE_SKB_SYMBOL1_EN;
			break;
		case EditorInfo.TYPE_CLASS_PHONE:
			newInputMode = MODE_SKB_PHONE_NUM;
			break;
		case EditorInfo.TYPE_CLASS_TEXT:
			int v = editorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION;
			if (v == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
					|| v == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
					|| v == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
					|| v == EditorInfo.TYPE_TEXT_VARIATION_URI) {
				// If the application request English mode, we switch to it.
				newInputMode = MODE_SKB_ENGLISH_LOWER;
			} else {
				if (v == EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE) {
					mShortMessageField = true;
				}
				// If the application do not request English mode, we will
				// try to keep the previous mode.
				int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
				newInputMode = mInputMode;
				if (0 == skbLayout) {
					if ((mInputMode & MASK_LANGUAGE) == MASK_LANGUAGE_CN) {
						newInputMode = MODE_SKB_CHINESE;
					} else {
						newInputMode = MODE_SKB_ENGLISH_LOWER;
					}
				}
			}
			break;
		default:
			// Try to keep the previous mode.
			int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
			newInputMode = mInputMode;
			if (0 == skbLayout) {
				if ((mInputMode & MASK_LANGUAGE) == MASK_LANGUAGE_CN) {
					newInputMode = MODE_SKB_CHINESE;
				} else {
					newInputMode = MODE_SKB_ENGLISH_LOWER;
				}
			}
			break;
		}

		mEditorInfo = editorInfo;
		saveInputMode(newInputMode);
		prepareToggleStates(true);
		return mInputIcon;
	}

	/**
	 * 请求返还上一次输入法模式
	 * 
	 * @return
	 */
	public int requestBackToPreviousSkb() {
		int layout = (mInputMode & MASK_SKB_LAYOUT);
		int lastLayout = (mPreviousInputMode & MASK_SKB_LAYOUT);
		if (0 != layout && 0 != lastLayout) {
			// TODO 进行以下代码后 mInputMode 和 mPreviousInputMode 就一样了，这样好吗？
			mInputMode = mPreviousInputMode;
			saveInputMode(mInputMode);
			prepareToggleStates(true);
			return mInputIcon;
		}
		return 0;
	}

	/**
	 * 获得中文候选词模式状态
	 * 
	 * @return
	 */
	public int getTooggleStateForCnCand() {
		return mToggleStateCnCand;
	}

	/**
	 * 是否是硬键盘输入法模式
	 * 
	 * @return
	 */
	public boolean isEnglishWithHkb() {
		return MODE_HKB_ENGLISH == mInputMode;
	}

	/**
	 * 是否是软件盘英语模式
	 * 
	 * @return
	 */
	public boolean isEnglishWithSkb() {
		return MODE_SKB_ENGLISH_LOWER == mInputMode
				|| MODE_SKB_ENGLISH_UPPER == mInputMode;
	}

	/**
	 * 是否是软键盘高（大写）模式
	 * 
	 * @return
	 */
	public boolean isEnglishUpperCaseWithSkb() {
		return MODE_SKB_ENGLISH_UPPER == mInputMode;
	}

	/**
	 * 是否是中文语言（传统标准软键盘或者硬键盘）。
	 * 
	 * @return
	 */
	public boolean isChineseText() {
		int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
		if (MASK_SKB_LAYOUT_QWERTY == skbLayout || 0 == skbLayout) {
			int language = (mInputMode & MASK_LANGUAGE);
			if (MASK_LANGUAGE_CN == language)
				return true;
		}
		return false;
	}

	/**
	 * 是否是硬键盘的中文语言
	 * 
	 * @return
	 */
	public boolean isChineseTextWithHkb() {
		int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
		if (0 == skbLayout) {
			int language = (mInputMode & MASK_LANGUAGE);
			if (MASK_LANGUAGE_CN == language)
				return true;
		}
		return false;
	}

	/**
	 * 是否是软键盘的中文语言
	 * 
	 * @return
	 */
	public boolean isChineseTextWithSkb() {
		int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
		if (MASK_SKB_LAYOUT_QWERTY == skbLayout) {
			int language = (mInputMode & MASK_LANGUAGE);
			if (MASK_LANGUAGE_CN == language)
				return true;
		}
		return false;
	}

	/**
	 * 是否是软键盘的符号
	 * 
	 * @return
	 */
	public boolean isSymbolWithSkb() {
		int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
		if (MASK_SKB_LAYOUT_SYMBOL1 == skbLayout
				|| MASK_SKB_LAYOUT_SYMBOL2 == skbLayout) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是正常Enter键状态
	 * 
	 * @return
	 */
	public boolean isEnterNoramlState() {
		return mEnterKeyNormal;
	}

	/**
	 * @param 长按处理
	 * @return
	 */
	public boolean tryHandleLongPressSwitch(int keyCode) {
		if (USERDEF_KEYCODE_LANG_2 == keyCode
				|| USERDEF_KEYCODE_PHONE_SYM_4 == keyCode) {
			mImeService.showOptionsMenu();
			return true;
		}
		return false;
	}

	/**
	 * 保存新的输入法模式
	 * 
	 * @param newInputMode
	 */
	private void saveInputMode(int newInputMode) {
		// 保存当前输入法模式
		mPreviousInputMode = mInputMode;
		// 设置新的输入法模式为当前的输入法模式
		mInputMode = newInputMode;

		// 输入法模式的布局属性（第8位）
		int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
		if (MASK_SKB_LAYOUT_QWERTY == skbLayout || 0 == skbLayout) {
			// 保存最近的语言输入法模式
			mRecentLauageInputMode = mInputMode;
		}

		// 设置输入法图标
		mInputIcon = R.drawable.ime_pinyin;
		if (isEnglishWithHkb()) {
			mInputIcon = R.drawable.ime_en;
		} else if (isChineseTextWithHkb()) {
			mInputIcon = R.drawable.ime_pinyin;
		}

		// 如果有硬键盘，就设置输入法模式为未设置状态。
		if (!Environment.getInstance().hasHardKeyboard()) {
			mInputIcon = 0;
		}
	}

	/**
	 * 准备设置控制显示的按键切换状态和可显示行ID的对象的数据，封装mToggleStates的数据。
	 * 
	 * @param needSkb
	 */
	private void prepareToggleStates(boolean needSkb) {
		mEnterKeyNormal = true;
		if (!needSkb)
			return;

		mToggleStates.mQwerty = false;
		mToggleStates.mKeyStatesNum = 0;

		int states[] = mToggleStates.mKeyStates;
		int statesNum = 0;
		// Toggle state for language.
		int language = (mInputMode & MASK_LANGUAGE);
		int layout = (mInputMode & MASK_SKB_LAYOUT);
		int charcase = (mInputMode & MASK_CASE);
		int variation = mEditorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION;

		// 更加输入法模式添加要显示的按键的切换状态
		if (MASK_SKB_LAYOUT_PHONE != layout) {
			if (MASK_LANGUAGE_CN == language) {
				// Chinese and Chinese symbol are always the default states,
				// do not add a toggling operation.
				if (MASK_SKB_LAYOUT_QWERTY == layout) {
					mToggleStates.mQwerty = true;
					mToggleStates.mQwertyUpperCase = true;
					if (mShortMessageField) {
						states[statesNum] = mToggleStateSmiley;
						statesNum++;
					}
				}
			} else if (MASK_LANGUAGE_EN == language) {
				if (MASK_SKB_LAYOUT_QWERTY == layout) {
					mToggleStates.mQwerty = true;
					mToggleStates.mQwertyUpperCase = false;
					states[statesNum] = mToggleStateEnLower;
					if (MASK_CASE_UPPER == charcase) {
						mToggleStates.mQwertyUpperCase = true;
						states[statesNum] = mToggleStateEnUpper;
					}
					statesNum++;
				} else if (MASK_SKB_LAYOUT_SYMBOL1 == layout) {
					states[statesNum] = mToggleStateEnSym1;
					statesNum++;
				} else if (MASK_SKB_LAYOUT_SYMBOL2 == layout) {
					states[statesNum] = mToggleStateEnSym2;
					statesNum++;
				}
			}

			// Toggle rows for QWERTY.
			mToggleStates.mRowIdToEnable = KeyRow.DEFAULT_ROW_ID;
			if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
				mToggleStates.mRowIdToEnable = mToggleRowEmailAddress;
			} else if (variation == EditorInfo.TYPE_TEXT_VARIATION_URI) {
				mToggleStates.mRowIdToEnable = mToggleRowUri;
			} else if (MASK_LANGUAGE_CN == language) {
				mToggleStates.mRowIdToEnable = mToggleRowCn;
			} else if (MASK_LANGUAGE_EN == language) {
				mToggleStates.mRowIdToEnable = mToggleRowEn;
			}
		} else {
			if (MASK_CASE_UPPER == charcase) {
				states[statesNum] = mToggleStatePhoneSym;
				statesNum++;
			}
		}

		// Toggle state for enter key.
		// 根据EditorInfo.imeOptions添加 要显示的按键的切换状态 ，以下只添加 Enter 键的切换状态。
		int action = mEditorInfo.imeOptions
				& (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION);

		if (action == EditorInfo.IME_ACTION_GO) {
			states[statesNum] = mToggleStateGo;
			statesNum++;
			mEnterKeyNormal = false;
		} else if (action == EditorInfo.IME_ACTION_SEARCH) {
			states[statesNum] = mToggleStateSearch;
			statesNum++;
			mEnterKeyNormal = false;
		} else if (action == EditorInfo.IME_ACTION_SEND) {
			states[statesNum] = mToggleStateSend;
			statesNum++;
			mEnterKeyNormal = false;
		} else if (action == EditorInfo.IME_ACTION_NEXT) {
			int f = mEditorInfo.inputType & EditorInfo.TYPE_MASK_FLAGS;
			if (f != EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) {
				states[statesNum] = mToggleStateNext;
				statesNum++;
				mEnterKeyNormal = false;
			}
		} else if (action == EditorInfo.IME_ACTION_DONE) {
			states[statesNum] = mToggleStateDone;
			statesNum++;
			mEnterKeyNormal = false;
		}
		mToggleStates.mKeyStatesNum = statesNum;
	}
}
