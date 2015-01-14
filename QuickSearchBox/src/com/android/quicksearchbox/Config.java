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

import android.content.Context;
import android.content.res.Resources;
import android.os.Process;
import android.util.Log;

import java.util.HashSet;

/**
 * Provides values for configurable parameters in all of QSB.
 * 提供QSB的基本配置
 * 
 * All the methods in this class return fixed default values. Subclasses may
 * make these values server-side settable.
 * 
 */
public class Config
{
	private static final String	TAG										= "QSB.Config";			//输出日志
	private static final long	DAY_MILLIS								= 86400000L;			//一天的时长，微秒
	private static final int	NUM_SUGGESTIONS_ABOVE_KEYBOARD			= 4;		//搜索区域只有4个，通讯录、音乐、短信、应用程序
	private static final int	NUM_PROMOTED_SOURCES					= 3;					//
	private static final int	MAX_PROMOTED_SUGGESTIONS				= 8;				//
	private static final int	MAX_RESULTS_PER_SOURCE					= 50;			//每个资源最多只能有50个结果
	private static final long	SOURCE_TIMEOUT_MILLIS					= 10000;		//每个资源的延时时间10s
	
	//搜索进程的优先级：9；进程的优先级别为19~(-20)
	private static final int	QUERY_THREAD_PRIORITY					= Process.THREAD_PRIORITY_BACKGROUND
																				+ Process.THREAD_PRIORITY_MORE_FAVORABLE;
	//
	private static final long	MAX_STAT_AGE_MILLIS						= 30 * DAY_MILLIS;
	private static final int	MIN_CLICKS_FOR_SOURCE_RANKING			= 3;
	private static final int	MAX_SHORTCUTS_RETURNED					= MAX_PROMOTED_SUGGESTIONS;
	private static final int	NUM_WEB_CORPUS_THREADS					= 2;
	private static final int	LATENCY_LOG_FREQUENCY					= 1000;		//
	private static final long	TYPING_SUGGESTIONS_UPDATE_DELAY_MILLIS	= 100;
	
	private final Context		mContext;				//
	private HashSet<String>		mDefaultCorpora;	//

	/**
	 * Creates a new config that uses hard-coded default values.
	 */
	public Config(Context context)
	{
		mContext = context;
	}

	protected Context getContext()
	{
		return mContext;
	}

	/**
	 * Releases any resources used by the configuration object.
	 * 
	 * Default implementation does nothing.
	 */
	public void close()
	{
	}

	//加载默认的字符数组集合
	private HashSet<String> loadDefaultCorpora()
	{
		HashSet<String> defaultCorpora = new HashSet<String>();
		try
		{
			// Get the list of default corpora from a resource, which allows vendor overlays.
			//为了把资源R.array.default_corpora和字符串数组corpora关联起来
			//R.array.default_corpora:是一个字符串数组，其具体值可以查看config.xml
			//mContext.getResources()：首先获得这个资源，返回一个Resources;
			//再调用getStringArray，将这个资源与具体的变量关联起来。
			String[] corpora = mContext.getResources().getStringArray(R.array.default_corpora);
			
			//遍历数组corpora,将其值添加到defaultCorpora中
			for (String corpus : corpora)
			{
				defaultCorpora.add(corpus);		//添加元素
			}
			return defaultCorpora;				//返回这个HashSet<String>
		}
		catch (Resources.NotFoundException ex)
		{
			Log.e(TAG, "Could not load default corpora", ex);
			return defaultCorpora;
		}
	}

	/**
	 * Checks if we trust the given source not to be spammy.
	 */
	//检查corpusName是否包含在mDefaultCorpora中
	public synchronized boolean isCorpusEnabledByDefault(String corpusName)
	{
		if (mDefaultCorpora == null)
		{
			mDefaultCorpora = loadDefaultCorpora();		//先加载HashSet<String>
		}
		//判断corpusName是否在mDefaultCorpora中
		return mDefaultCorpora.contains(corpusName);
	}

	/**
	 * The number of promoted sources.
	 */
	public int getNumPromotedSources()
	{
		return NUM_PROMOTED_SOURCES;
	}

	/**
	 * The number of suggestions visible above the onscreen keyboard.
	 */
	public int getNumSuggestionsAboveKeyboard()
	{
		try
		{
			// Get the list of default corpora from a resource, which allows vendor overlays.
			//
			return mContext.getResources().getInteger(R.integer.num_suggestions_above_keyboard);
		}
		catch (Resources.NotFoundException ex)
		{
			Log.e(TAG, "Could not load num_suggestions_above_keyboard", ex);
			return NUM_SUGGESTIONS_ABOVE_KEYBOARD;
		}
	}

	/**
	 * The maximum number of suggestions to promote.
	 */
	public int getMaxPromotedSuggestions()
	{
		return MAX_PROMOTED_SUGGESTIONS;
	}

	/**
	 * The number of results to ask each source for.
	 */
	public int getMaxResultsPerSource()
	{
		return MAX_RESULTS_PER_SOURCE;
	}

	/**
	 * The timeout for querying each source, in milliseconds.
	 */
	public long getSourceTimeoutMillis()
	{
		return SOURCE_TIMEOUT_MILLIS;
	}

	/**
	 * The priority of query threads.
	 * 
	 * @return A thread priority, as defined in {@link Process}.
	 */
	public int getQueryThreadPriority()
	{
		return QUERY_THREAD_PRIORITY;		//进程级别：9
	}

	/**
	 * The maximum age of log data used for shortcuts.
	 */
	public long getMaxStatAgeMillis()
	{
		return MAX_STAT_AGE_MILLIS;
	}

	/**
	 * The minimum number of clicks needed to rank a source.
	 */
	public int getMinClicksForSourceRanking()
	{
		return MIN_CLICKS_FOR_SOURCE_RANKING;
	}

	/**
	 * The maximum number of shortcuts shown.
	 */
	public int getMaxShortcutsReturned()
	{
		return MAX_SHORTCUTS_RETURNED;
	}

	public int getNumWebCorpusThreads()
	{
		return NUM_WEB_CORPUS_THREADS;
	}

	/**
	 * How often query latency should be logged.
	 * 
	 * @return An integer in the range 0-1000. 0 means that no latency events
	 *         should be logged. 1000 means that all latency events should be
	 *         logged.
	 */
	public int getLatencyLogFrequency()
	{
		return LATENCY_LOG_FREQUENCY;
	}

	/**
	 * The delay in milliseconds before suggestions are updated while typing. If
	 * a new character is typed before this timeout expires, the timeout is
	 * reset.
	 */
	public long getTypingUpdateSuggestionsDelayMillis()
	{
		return TYPING_SUGGESTIONS_UPDATE_DELAY_MILLIS;
	}
}
