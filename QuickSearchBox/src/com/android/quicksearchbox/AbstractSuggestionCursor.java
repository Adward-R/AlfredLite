/*
 * Copyright (C) 2010 The Android Open Source Project
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

/**
 * Base class for suggestion cursors.
 */
public abstract class AbstractSuggestionCursor implements SuggestionCursor
{
	private final String	mUserQuery;			//用户查找的关键字？

	public AbstractSuggestionCursor(String userQuery)
	{
		mUserQuery = userQuery;
	}

	public String getUserQuery()
	{
		return mUserQuery;
	}

	/**
	 * 
	 */
	public String getSuggestionDisplayQuery()
	{
		String query = getSuggestionQuery();
		if (query != null)
		{
			return query;
		}
		Source source = getSuggestionSource();		//获得搜索资源
		if (source.shouldRewriteQueryFromData())
		{
			String data = getSuggestionIntentDataString();
			if (data != null)
			{
				return data;
			}
		}
		if (source.shouldRewriteQueryFromText())
		{
			String text1 = getSuggestionText1();
			if (text1 != null)
			{
				return text1;
			}
		}
		return null;
	}
}
