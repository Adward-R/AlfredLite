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

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity that shows a list of choices.
 */
//How to start this program?
public abstract class ChoiceActivity extends Activity
{
	protected TextView	mTitleView;
	protected ListView	mChoicesView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//getWindow():返回当前窗口；Window.requestFeature():扩展窗口特征
		//Window.FEATURE_NO_TITLE:窗口无title
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choice_activity);		//加载窗口
		mTitleView = (TextView) findViewById(R.id.alertTitle); //
		mChoicesView = (ListView) findViewById(R.id.list);
	}

	public void setHeading(int titleRes)
	{
		mTitleView.setText(titleRes);			//设置title文本
	}

	public void setHeading(CharSequence title)
	{
		mTitleView.setText(title);
	}

	public void setAdapter(ListAdapter adapter)
	{
		mChoicesView.setAdapter(adapter);
	}

	//点击Item时的消息响应函数；
	public void setOnItemClickListener(AdapterView.OnItemClickListener listener)
	{
		//
		mChoicesView.setOnItemClickListener(listener);
		// TODO: for some reason, putting this in the XML layout instead makes
		// the list items unclickable.
		mChoicesView.setFocusable(true);
	}
}
