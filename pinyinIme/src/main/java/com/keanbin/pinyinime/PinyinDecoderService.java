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

package com.keanbin.pinyinime;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import com.hht.pinyinime.R;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.IBinder;
import android.util.Log;

/**
 * This class is used to separate the input method kernel in an individual
 * service so that both IME and IME-syncer can use it.
 * 
 * 词库解码JNI函数服务
 * 
 * @ClassName PinyinDecoderService
 * @author keanbin
 */
public class PinyinDecoderService extends Service {
	private static String TAG = "PinyinDecoderService";
	native static boolean nativeImOpenDecoder(byte fn_sys_dict[],
			byte fn_usr_dict[]);

	/**
	 * JNI函数：打开解码器
	 * 
	 * @param fd
	 * @param startOffset
	 * @param length
	 * @param fn_usr_dict
	 * @return
	 */
	native static boolean nativeImOpenDecoderFd(FileDescriptor fd,
			long startOffset, long length, byte fn_usr_dict[]);

	/**
	 * JNI函数：设置最大的长度
	 * 
	 * @param maxSpsLen
	 * @param maxHzsLen
	 */
	native static void nativeImSetMaxLens(int maxSpsLen, int maxHzsLen);

	/**
	 * JNI函数：关闭解码器
	 * 
	 * @return
	 */
	native static boolean nativeImCloseDecoder();

	/**
	 * JNI函数：根据拼音查询候选词
	 * 
	 * @param pyBuf
	 * @param pyLen
	 * @return
	 */
	native static int nativeImSearch(byte pyBuf[], int pyLen);

	/**
	 * JNI函数：删除指定位置的拼音后进行查询
	 * 
	 * @param pos
	 * @param is_pos_in_splid
	 * @param clear_fixed_this_step
	 * @return
	 */
	native static int nativeImDelSearch(int pos, boolean is_pos_in_splid,
			boolean clear_fixed_this_step);

	/**
	 * JNI函数：重置拼音查询，应该是清除之前查询的数据
	 */
	native static void nativeImResetSearch();

	/**
	 * JNI函数：增加字母。
	 * 
	 * @备注 目前没有使用。
	 * @param ch
	 * @return
	 */
	native static int nativeImAddLetter(byte ch);

	/**
	 * JNI函数：获取拼音字符串
	 * 
	 * @param decoded
	 * @return
	 */
	native static String nativeImGetPyStr(boolean decoded);

	/**
	 * JNI函数：获取拼音字符串的长度
	 * 
	 * @param decoded
	 * @return
	 */
	native static int nativeImGetPyStrLen(boolean decoded);

	/**
	 * JNI函数：获取每个拼写的开始位置，猜测：第一个元素是拼写的总数量？
	 * 
	 * @return
	 */
	native static int[] nativeImGetSplStart();

	/**
	 * JNI函数：获取指定位置的候选词
	 * 
	 * @param choiceId
	 * @return
	 */
	native static String nativeImGetChoice(int choiceId);

	/**
	 * JNI函数：获取候选词的数量
	 * 
	 * @param choiceId
	 * @return
	 */
	native static int nativeImChoose(int choiceId);

	/**
	 * JNI函数：取消最后的选择
	 * 
	 * @备注 目前没有使用
	 * @return
	 */
	native static int nativeImCancelLastChoice();

	/**
	 * JNI函数：获取固定字符的长度
	 * 
	 * @return
	 */
	native static int nativeImGetFixedLen();

	/**
	 * JNI函数：取消输入
	 * 
	 * @备注 目前没有使用
	 * @return
	 */
	native static boolean nativeImCancelInput();

	/**
	 * JNI函数：刷新缓存
	 * 
	 * @备注 目前没有使用
	 * @return
	 */
	native static boolean nativeImFlushCache();

	/**
	 * JNI函数：根据字符串 fixedStr 获取预报的候选词
	 * 
	 * @param fixedStr
	 * @return
	 */
	native static int nativeImGetPredictsNum(String fixedStr);

	/**
	 * JNI函数：获取指定位置的预报候选词
	 * 
	 * @param predictNo
	 * @return
	 */
	native static String nativeImGetPredictItem(int predictNo);

	// Sync related
	/**
	 * JNI函数：同步到用户词典，猜测：是不是记住用户的常用词。
	 * 
	 * @备注 目前没有使用
	 * @param user_dict
	 * @param tomerge
	 * @return
	 */
	native static String nativeSyncUserDict(byte[] user_dict, String tomerge);

	/**
	 * JNI函数：开始用户词典同步
	 * 
	 * @备注 目前没有使用
	 * @param user_dict
	 * @return
	 */
	native static boolean nativeSyncBegin(byte[] user_dict);

	/**
	 * JNI函数：同步结束
	 * 
	 * @备注 目前没有使用
	 * @return
	 */
	native static boolean nativeSyncFinish();

	/**
	 * JNI函数：同步获取Lemmas
	 * 
	 * @备注 目前没有使用
	 * @return
	 */
	native static String nativeSyncGetLemmas();

	/**
	 * JNI函数：同步存入Lemmas
	 * 
	 * @备注 目前没有使用
	 * @param tomerge
	 * @return
	 */
	native static int nativeSyncPutLemmas(String tomerge);

	/**
	 * JNI函数：同步获取最后的数量
	 * 
	 * @备注 目前没有使用
	 * @return
	 */
	native static int nativeSyncGetLastCount();

	/**
	 * JNI函数：同步获取总数量
	 * 
	 * @备注 目前没有使用
	 * @return
	 */
	native static int nativeSyncGetTotalCount();

	/**
	 * JNI函数：同步清空最后获取
	 * 
	 * @备注 目前没有使用
	 * @return
	 */
	native static boolean nativeSyncClearLastGot();

	/**
	 * JNI函数：同步获取容量
	 * 
	 * @备注 目前没有使用
	 * @return
	 */
	native static int nativeSyncGetCapacity();

	/**
	 * 最大的文件路径长度
	 */
	private final static int MAX_PATH_FILE_LENGTH = 100;

	/**
	 * 是否完成初始化
	 */
	private static boolean inited = false;

	/**
	 * 用户的词典文件
	 */
	private String mUsr_dict_file;

	// 导入本地函数库
	static {
		try {
			System.loadLibrary("jni_pinyinime");
		} catch (UnsatisfiedLinkError ule) {
			Log.e("PinyinDecoderService",
					"WARNING: Could not load jni_pinyinime natives");
		}
	}

	/**
	 * Get file name of the specified dictionary 获取用户词典的文件名
	 * 
	 * @param usr_dict
	 * @return
	 */
	private boolean getUsrDictFileName(byte usr_dict[]) {
		if (null == usr_dict) {
			return false;
		}

		for (int i = 0; i < mUsr_dict_file.length(); i++)
			usr_dict[i] = (byte) mUsr_dict_file.charAt(i);
		usr_dict[mUsr_dict_file.length()] = 0;

		return true;
	}

	/**
	 * 初始化拼音引擎
	 */
	private void initPinyinEngine() {
		byte usr_dict[];
		usr_dict = new byte[MAX_PATH_FILE_LENGTH];

		// Here is how we open a built-in dictionary for access through
		// a file descriptor...
		// 获取词库 R.raw.dict_pinyin 的文件描述符
		AssetFileDescriptor afd = getResources().openRawResourceFd(
				R.raw.dict_pinyin);
		/*if (Environment.getInstance().needDebug()) {
			Log.i("foo", "Dict: start=" + afd.getStartOffset() + ", length="
					+ afd.getLength() + ", fd=" + afd.getParcelFileDescriptor());
		}*/
		if (getUsrDictFileName(usr_dict)) {
			// JNI函数：打开解码器
			inited = nativeImOpenDecoderFd(afd.getFileDescriptor(),
					afd.getStartOffset(), afd.getLength(), usr_dict);
			Log.i(TAG,"inited:"+inited);
		}
		try {
			afd.close();
		} catch (IOException e) {
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 获取用户词典"usr_dict.dat"的路径。"usr_dict.dat"放在file目录下。
		// 猜测：调用getFileStreamPath("usr_dict.dat")，如果"usr_dict.dat"不存在，会调用openFileOutput（）创建该文件。
		mUsr_dict_file = getFileStreamPath("usr_dict.dat").getPath();
		Log.i(TAG,"mUsr_dict_file:"+mUsr_dict_file);
		// This is a hack to make sure our "files" directory has been
		// created.
		try {
			openFileOutput("dummy", 0).close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		initPinyinEngine();
	}

	@Override
	public void onDestroy() {
		// JNI函数：关闭解码器
		nativeImCloseDecoder();
		inited = false;
		super.onDestroy();
	}

	/**
	 * 给外部调用的接口
	 */
	private final IPinyinDecoderService.Stub mBinder = new IPinyinDecoderService.Stub() {

		/**
		 * 返回12345
		 */
		public int getInt() {
			return 12345;
		}

		/**
		 * 设置最大的长度
		 */
		public void setMaxLens(int maxSpsLen, int maxHzsLen) {
			nativeImSetMaxLens(maxSpsLen, maxHzsLen);
		}

		/**
		 * 根据拼音查询候选词
		 */
		public int imSearch(byte[] pyBuf, int pyLen) {
			return nativeImSearch(pyBuf, pyLen);
		}

		/**
		 * 删除指定位置的拼音后进行查询
		 */
		public int imDelSearch(int pos, boolean is_pos_in_splid,
				boolean clear_fixed_this_step) {
			return nativeImDelSearch(pos, is_pos_in_splid,
					clear_fixed_this_step);
		}

		/**
		 * 重置拼音查询，应该是清除之前查询的数据
		 */
		public void imResetSearch() {
			nativeImResetSearch();
		}

		/**
		 * 增加字母。
		 * 
		 * @备注 目前没有使用。
		 */
		public int imAddLetter(byte ch) {
			return nativeImAddLetter(ch);
		}

		/**
		 * 获取拼音字符串
		 */
		public String imGetPyStr(boolean decoded) {
			return nativeImGetPyStr(decoded);
		}

		/**
		 * 获取拼音字符串的长度
		 */
		public int imGetPyStrLen(boolean decoded) {
			return nativeImGetPyStrLen(decoded);
		}

		/**
		 * 获取每个拼写的开始位置，猜测：第一个元素是拼写的总数量？
		 */
		public int[] imGetSplStart() {
			return nativeImGetSplStart();
		}

		/**
		 * 获取指定位置的候选词
		 */
		public String imGetChoice(int choiceId) {
			return nativeImGetChoice(choiceId);
		}

		/**
		 * 获取多个候选词
		 * 
		 * @备注 目前没有使用。
		 */
		public String imGetChoices(int choicesNum) {
			String retStr = null;
			for (int i = 0; i < choicesNum; i++) {
				if (null == retStr)
					retStr = nativeImGetChoice(i);
				else
					retStr += " " + nativeImGetChoice(i);
			}
			return retStr;
		}

		/**
		 * 获取候选词列表。choicesStart位置的候选词从sentFixedLen开始截取。
		 */
		public List<String> imGetChoiceList(int choicesStart, int choicesNum,
				int sentFixedLen) {
			Log.i(TAG,"choicesStart:"+choicesStart+" choicesNum:"+choicesNum);
			Vector<String> choiceList = new Vector<String>();
			for (int i = choicesStart; i < choicesStart + choicesNum; i++) {
				String retStr = nativeImGetChoice(i);
				if (0 == i)
					retStr = retStr.substring(sentFixedLen);
				choiceList.add(retStr);
				Log.i(TAG,"retStr:"+retStr);
			}
			return choiceList;
		}

		/**
		 * 获取候选词的数量
		 */
		public int imChoose(int choiceId) {
			//Log.i(TAG,"nativeImChoose(choiceId):"+nativeImChoose(choiceId));//不能这样打印，会影响下一次输入结果！！！！！！
			return nativeImChoose(choiceId);
		}

		/**
		 * 取消最后的选择
		 * 
		 * @备注 目前没有使用
		 */
		public int imCancelLastChoice() {
			return nativeImCancelLastChoice();
		}

		/**
		 * 获取固定字符的长度
		 */
		public int imGetFixedLen() {
			return nativeImGetFixedLen();
		}

		/**
		 * 取消输入
		 * 
		 * @备注 目前没有使用
		 */
		public boolean imCancelInput() {
			return nativeImCancelInput();
		}

		/**
		 * 刷新缓存
		 * 
		 * @备注 目前没有使用
		 */
		public void imFlushCache() {
			nativeImFlushCache();
		}

		/**
		 * 根据字符串 fixedStr 获取预报的候选词
		 */
		public int imGetPredictsNum(String fixedStr) {
			return nativeImGetPredictsNum(fixedStr);
		}

		/**
		 * 获取指定位置的预报候选词
		 */
		public String imGetPredictItem(int predictNo) {
			return nativeImGetPredictItem(predictNo);
		}

		/**
		 * 获取候选词列表
		 */
		public List<String> imGetPredictList(int predictsStart, int predictsNum) {
			Vector<String> predictList = new Vector<String>();
			for (int i = predictsStart; i < predictsStart + predictsNum; i++) {
				//Log.i(TAG,"nativeImGetPredictItem("+i+")"+nativeImGetPredictItem(i));不能这样打印，会影响下一次输入结果！！！！！！
				predictList.add(nativeImGetPredictItem(i));
			}
			return predictList;
		}

		/**
		 * 同步到用户词典，猜测：是不是记住用户的常用词。
		 * 
		 * @备注 目前没有使用
		 */
		public String syncUserDict(String tomerge) {
			byte usr_dict[];
			usr_dict = new byte[MAX_PATH_FILE_LENGTH];

			if (getUsrDictFileName(usr_dict)) {
				return nativeSyncUserDict(usr_dict, tomerge);
			}
			return null;
		}

		/**
		 * 开始用户词典同步
		 * 
		 * @备注 目前没有使用
		 */
		public boolean syncBegin() {
			byte usr_dict[];
			usr_dict = new byte[MAX_PATH_FILE_LENGTH];

			if (getUsrDictFileName(usr_dict)) {
				return nativeSyncBegin(usr_dict);
			}
			return false;
		}

		/**
		 * 同步结束
		 * 
		 * @备注 目前没有使用
		 */
		public void syncFinish() {
			nativeSyncFinish();
		}

		/**
		 * 同步存入Lemmas
		 * 
		 * @备注 目前没有使用
		 */
		public int syncPutLemmas(String tomerge) {
			return nativeSyncPutLemmas(tomerge);
		}

		/**
		 * 同步获取Lemmas
		 * 
		 * @备注 目前没有使用
		 */
		public String syncGetLemmas() {
			return nativeSyncGetLemmas();
		}

		/**
		 * 同步获取最后的数量
		 * 
		 * @备注 目前没有使用
		 */
		public int syncGetLastCount() {
			return nativeSyncGetLastCount();
		}

		/**
		 * 同步获取总数量
		 * 
		 * @备注 目前没有使用
		 */
		public int syncGetTotalCount() {
			//Log.i(TAG,"nativeSyncGetTotalCount:"+nativeSyncGetTotalCount());//不能这样打印，会影响下一次输入结果！！！！！！
			return nativeSyncGetTotalCount();
		}

		/**
		 * 同步清空最后获取
		 * 
		 * @备注 目前没有使用
		 */
		public void syncClearLastGot() {
			nativeSyncClearLastGot();
		}

		/**
		 * 同步获取容量
		 * 
		 * @备注 目前没有使用
		 */
		public int imSyncGetCapacity() {
			return nativeSyncGetCapacity();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
}
