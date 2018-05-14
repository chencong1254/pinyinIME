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

import java.util.Vector;

import android.content.Context;

/**
 * Class used to cache previously loaded soft keyboard layouts.
 */
/**
 * 软键盘内存池，该类采用单例模式，它有两个向量列表：软键盘模版列表、软键盘列表。
 * 
 * @ClassName SkbPool
 * @author keanbin
 */
public class SkbPool {
	private static SkbPool mInstance = null;

	private Vector<SkbTemplate> mSkbTemplates = new Vector<SkbTemplate>();
	private Vector<SoftKeyboard> mSoftKeyboards = new Vector<SoftKeyboard>();

	private SkbPool() {
	}

	public static SkbPool getInstance() {
		if (null == mInstance)
			mInstance = new SkbPool();
		return mInstance;
	}

	public void resetCachedSkb() {
		mSoftKeyboards.clear();
	}

	/**
	 * 获取软件盘模版。逻辑简介：首先先从mSkbTemplates列表中获取，如果没有获取到，
	 * 就调用XmlKeyboardLoader解析资源文件ID为skbTemplateId的软键盘模版xml文件
	 * ，生成一个模版，并加入mSkbTemplates列表中。
	 * 
	 * @param skbTemplateId
	 * @param context
	 * @return
	 */
	public SkbTemplate getSkbTemplate(int skbTemplateId, Context context) {
		for (int i = 0; i < mSkbTemplates.size(); i++) {
			SkbTemplate t = mSkbTemplates.elementAt(i);
			if (t.getSkbTemplateId() == skbTemplateId) {
				return t;
			}
		}

		if (null != context) {
			XmlKeyboardLoader xkbl = new XmlKeyboardLoader(context);
			SkbTemplate t = xkbl.loadSkbTemplate(skbTemplateId);
			if (null != t) {
				mSkbTemplates.add(t);
				return t;
			}
		}
		return null;
	}

	// Try to find the keyboard in the pool with the cache id. If there is no
	// keyboard found, try to load it with the given xml id.
	/**
	 * 获取软件盘。逻辑简介：首先先从mSoftKeyboards列表中获取，如果没有获取到，
	 * 就调用XmlKeyboardLoader解析资源文件ID为skbXmlId的软键盘xml文件
	 * ，生成一个软键盘，并加入mSoftKeyboards列表中。
	 * 
	 * @param skbCacheId
	 * @param skbXmlId
	 * @param skbWidth
	 * @param skbHeight
	 * @param context
	 * @return
	 */
	public SoftKeyboard getSoftKeyboard(int skbCacheId, int skbXmlId,
			int skbWidth, int skbHeight, Context context) {
		for (int i = 0; i < mSoftKeyboards.size(); i++) {
			SoftKeyboard skb = mSoftKeyboards.elementAt(i);
			if (skb.getCacheId() == skbCacheId && skb.getSkbXmlId() == skbXmlId) {
				skb.setSkbCoreSize(skbWidth, skbHeight);
				skb.setNewlyLoadedFlag(false);
				return skb;
			}
		}
		if (null != context) {
			XmlKeyboardLoader xkbl = new XmlKeyboardLoader(context);
			SoftKeyboard skb = xkbl.loadKeyboard(skbXmlId, skbWidth, skbHeight);
			if (skb != null) {
				if (skb.getCacheFlag()) {
					skb.setCacheId(skbCacheId);
					mSoftKeyboards.add(skb);
				}
			}
			return skb;
		}
		return null;
	}
}
