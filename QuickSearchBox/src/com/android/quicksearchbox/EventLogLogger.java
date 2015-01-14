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
//这个类是被删除的对象

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.EventLog;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Logs events to {@link EventLog}.
 */
public class EventLogLogger implements Logger
{
	private static final char	LIST_SEPARATOR	= '|';
	private final Context		mContext;
	private final Config		mConfig;				//配置信息
	private final String		mPackageName;
	private final int			mVersionCode;
	private final Random		mRandom;

	public EventLogLogger(Context context, Config config)
	{
		mContext = context;
		mConfig = config;
		mPackageName = mContext.getPackageName();
		try
		{
			PackageInfo pkgInfo = mContext.getPackageManager().getPackageInfo(mPackageName, 0);
			mVersionCode = pkgInfo.versionCode;
		}
		catch (PackageManager.NameNotFoundException ex)
		{
			// The current package should always exist, how else could we run code from it?
			throw new RuntimeException(ex);
		}
		mRandom = new Random();
	}

	protected Context getContext()
	{
		return mContext;
	}

	protected int getVersionCode()
	{
		return mVersionCode;
	}

	protected Config getConfig()
	{
		return mConfig;
	}

	/**要被注释掉*/
	public void logStart(int latency, String intentSource, Corpus corpus, List<Corpus> orderedCorpora)
	{
		// TODO: Add more info to startMethod
		String startMethod = intentSource;
		String currentCorpus = getCorpusLogName(corpus);
		String enabledCorpora = getCorpusLogNames(orderedCorpora);
		
		//EventLogTags.writeQsbStart(mPackageName, mVersionCode, startMethod,
		//		latency, currentCorpus, enabledCorpora);
	}

	/**要被注释掉*/
	public void logSuggestionClick(int position,
			SuggestionCursor suggestionCursor, Collection<Corpus> queriedCorpora)
	{
		String suggestions = getSuggestions(suggestionCursor);
		String corpora = getCorpusLogNames(queriedCorpora);
		int numChars = suggestionCursor.getUserQuery().length();
		
		//EventLogTags.writeQsbClick(position, suggestions, corpora, numChars);
	}

	/**要被注释掉*/
	public void logSearch(Corpus corpus, int startMethod, int numChars)
	{
		String corpusName = getCorpusLogName(corpus);
		
		//EventLogTags.writeQsbSearch(corpusName, startMethod, numChars);
	}

	/**要被注释掉*/
	public void logVoiceSearch(Corpus corpus)
	{
		String corpusName = getCorpusLogName(corpus);
		
		//EventLogTags.writeQsbVoiceSearch(corpusName);
	}
	
	/**要被注释掉*/
	public void logExit(SuggestionCursor suggestionCursor, int numChars)
	{
		String suggestions = getSuggestions(suggestionCursor);
		
		//EventLogTags.writeQsbExit(suggestions, numChars);
	}

	/**要被注释掉*/
	public void logLatency(CorpusResult result)
	{
		if (!shouldLogLatency())
			return;
		String corpusName = getCorpusLogName(result.getCorpus());
		int latency = result.getLatency();
		int numChars = result.getUserQuery().length();
		
		//EventLogTags.writeQsbLatency(corpusName, latency, numChars);
	}

	private boolean shouldLogLatency()
	{
		int freq = mConfig.getLatencyLogFrequency();
		return freq > mRandom.nextInt(1000);
	}

	private String getCorpusLogName(Corpus corpus)
	{
		if (corpus == null)
			return null;
		return corpus.getName();
	}

	private String getSuggestions(SuggestionCursor cursor)
	{
		StringBuilder sb = new StringBuilder();
		final int count = cursor == null ? 0 : cursor.getCount();
		for (int i = 0; i < count; i++)
		{
			if (i > 0)
				sb.append(LIST_SEPARATOR);
			cursor.moveTo(i);
			String source = cursor.getSuggestionSource().getName();
			String type = cursor.getSuggestionLogType();
			if (type == null)
				type = "";
			String shortcut = cursor.isSuggestionShortcut() ? "shortcut" : "";
			sb.append(source).append(':').append(type).append(':').append(shortcut);
		}
		return sb.toString();
	}

	
	private String getCorpusLogNames(Collection<Corpus> corpora)
	{
		StringBuilder sb = new StringBuilder();
		for (Corpus corpus : corpora)
		{
			if (sb.length() > 0)
				sb.append(LIST_SEPARATOR);
			sb.append(getCorpusLogName(corpus));
		}
		return sb.toString();
	}
}
