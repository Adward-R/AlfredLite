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
package com.android.quicksearchbox;

import java.util.Collection;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

/**
 * A corpus is a user-visible set of suggestions. A corpus gets suggestions from
 * one or more sources.
 * 
 * Objects that implement this interface should override
 * {@link Object#equals(Object)} and {@link Object#hashCode()} so that they can
 * be used as keys in hash maps.
 */
public interface Corpus extends SuggestionCursorProvider<CorpusResult>
{
	/**
	 * Gets the localized, human-readable label for this corpus.
	 * 获得这个集合的本地化，人工可读的标签
	 */
	CharSequence getLabel();

	/**
	 * Gets the icon for this corpus. 获得搜索集合的图标
	 */
	Drawable getCorpusIcon();

	/**
	 * Gets the icon URI for this corpus. 获得集合图标的URI
	 */
	Uri getCorpusIconUri();

	/**
	 * Gets the description to use for this corpus in system search settings.
	 * 获得在系统搜索设置中使用这个集合的描述
	 */
	CharSequence getSettingsDescription();

	/**
	 * Gets the search hint text for this corpus. 获得集合的搜索提示文本
	 */
	CharSequence getHint();

	/**
	 * 
	 * @return
	 */
	int getQueryThreshold();

	/**
	 * 
	 * @return
	 */
	boolean queryAfterZeroResults();

	/**
	 * 语音搜索
	 * 
	 * @return
	 */
	boolean voiceSearchEnabled();

	/**
	 * 创建一般查找
	 * 
	 * @param query
	 * @param appData
	 * @return
	 */
	Intent createSearchIntent(String query, Bundle appData);

	/**
	 * 创建语音查找
	 * 
	 * @param appData
	 * @return
	 */
	Intent createVoiceSearchIntent(Bundle appData);

	/**
	 * 
	 * @param query
	 * @return
	 */
	SuggestionData createSearchShortcut(String query);

	/**
	 * 是否是网络搜索集合
	 * 
	 * @return
	 */
	boolean isWebCorpus();

	/**
	 * Gets the sources that this corpus uses.
	 */
	Collection<Source> getSources();
}
