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
	 * ���������ϵı��ػ����˹��ɶ��ı�ǩ
	 */
	CharSequence getLabel();

	/**
	 * Gets the icon for this corpus. ����������ϵ�ͼ��
	 */
	Drawable getCorpusIcon();

	/**
	 * Gets the icon URI for this corpus. ��ü���ͼ���URI
	 */
	Uri getCorpusIconUri();

	/**
	 * Gets the description to use for this corpus in system search settings.
	 * �����ϵͳ����������ʹ��������ϵ�����
	 */
	CharSequence getSettingsDescription();

	/**
	 * Gets the search hint text for this corpus. ��ü��ϵ�������ʾ�ı�
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
	 * ��������
	 * 
	 * @return
	 */
	boolean voiceSearchEnabled();

	/**
	 * ����һ�����
	 * 
	 * @param query
	 * @param appData
	 * @return
	 */
	Intent createSearchIntent(String query, Bundle appData);

	/**
	 * ������������
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
	 * �Ƿ���������������
	 * 
	 * @return
	 */
	boolean isWebCorpus();

	/**
	 * Gets the sources that this corpus uses.
	 */
	Collection<Source> getSources();
}
