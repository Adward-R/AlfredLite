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

import com.android.quicksearchbox.ui.CorpusViewFactory;
import com.android.quicksearchbox.ui.CorpusViewInflater;
import com.android.quicksearchbox.ui.DelayingSuggestionsAdapter;
import com.android.quicksearchbox.ui.EmptySuggestionsFooter;
import com.android.quicksearchbox.ui.SuggestionViewFactory;
import com.android.quicksearchbox.ui.SuggestionViewInflater;
import com.android.quicksearchbox.ui.SuggestionsAdapter;
import com.android.quicksearchbox.ui.SuggestionsFooter;
import com.android.quicksearchbox.util.Factory;
import com.android.quicksearchbox.util.NamedTaskExecutor;
import com.android.quicksearchbox.util.PerNameExecutor;
import com.android.quicksearchbox.util.PriorityThreadFactory;
import com.android.quicksearchbox.util.SingleThreadNamedTaskExecutor;
import com.google.common.util.concurrent.NamingThreadFactory;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

//此类是个很重要的类，
public class QsbApplication extends Application
{
	private Handler					mUiThreadHandler;
	private Config					mConfig;				//配置搜索引擎功能
	private Corpora					mCorpora;				//结果集
	private CorpusRanker			mCorpusRanker;		//排序的结果集
	private ShortcutRepository		mShortcutRepository;	//
	private ShortcutRefresher		mShortcutRefresher;
	private NamedTaskExecutor		mSourceTaskExecutor;		//
	private ThreadFactory			mQueryThreadFactory;			//
	private SuggestionsProvider		mSuggestionsProvider;		//
	private SuggestionViewFactory	mSuggestionViewFactory;	//
	private CorpusViewFactory		mCorpusViewFactory;		//
	private Logger					mLogger;					//写日志，这个变量应该被注释掉

	@Override
	public void onTerminate()
	{
		close();
		super.onTerminate();
	}

	//
	protected void checkThread()
	{
		if (Looper.myLooper() != Looper.getMainLooper())		//获得主进程
		{
			//抛出异常：
			throw new IllegalStateException("Accessed Application object from thread "
							+ Thread.currentThread().getName());
		}
	}

	protected void close()
	{
		checkThread();
		if (mConfig != null)		//
		{
			mConfig.close();
			mConfig = null;
		}
		if (mCorpora != null)		//
		{
			mCorpora.close();
			mCorpora = null;
		}
		if (mShortcutRepository != null)	//
		{
			mShortcutRepository.close();
			mShortcutRepository = null;
		}
		if (mSourceTaskExecutor != null)	//
		{
			mSourceTaskExecutor.close();
			mSourceTaskExecutor = null;
		}
		if (mSuggestionsProvider != null)	//
		{
			mSuggestionsProvider.close();
			mSuggestionsProvider = null;
		}
	}

	//获得主进程的句柄，异步模式
	public synchronized Handler getMainThreadHandler()
	{
		if (mUiThreadHandler == null)
		{
			mUiThreadHandler = new Handler(Looper.getMainLooper());
		}
		return mUiThreadHandler;
	}

	/**
	 * Gets the QSB configuration object. May be called from any thread.
	 * 获得QSB的配置信息
	 */
	public synchronized Config getConfig()
	{
		if (mConfig == null)
		{
			mConfig = createConfig();
		}
		return mConfig;
	}

	/**
	 * 创建一个Config对象
	 * @return
	 */
	protected Config createConfig()
	{
		return new Config(this);		
	}

	/**
	 * Gets the corpora. May only be called from the main thread.
	 * 得到结果集
	 */
	public Corpora getCorpora()
	{
		checkThread();			//
		if (mCorpora == null)
		{
			mCorpora = createCorpora();
		}
		return mCorpora;
	}

	/**
	 * 
	 * @return
	 */
	protected Corpora createCorpora()
	{
		//搞明白传递的这4个参数的意义
		SearchableCorpora corpora = new SearchableCorpora(this, getConfig(),
				createSources(), createCorpusFactory());
		corpora.load();		//
		return corpora;
	}
	/**
	 * 
	 * @return
	 */
	protected Sources createSources()
	{
		return new SearchableSources(this, getMainThreadHandler());
	}

	protected CorpusFactory createCorpusFactory()
	{
		int numWebCorpusThreads = getConfig().getNumWebCorpusThreads();
		return new SearchableCorpusFactory(this, createExecutorFactory(numWebCorpusThreads));
	}

	protected Factory<Executor> createExecutorFactory(final int numThreads)
	{
		final ThreadFactory threadFactory = getQueryThreadFactory();
		return new Factory<Executor>()
		{
			public Executor create()
			{
				return Executors.newFixedThreadPool(numThreads, threadFactory);
			}
		};
	}

	/**
	 * Gets the corpus ranker. May only be called from the main thread.
	 */
	public CorpusRanker getCorpusRanker()
	{
		checkThread();		//
		if (mCorpusRanker == null)		//没有实例化
		{
			mCorpusRanker = createCorpusRanker();
		}
		return mCorpusRanker;
	}

	//创建了一个DefaultCorpusRanker对象
	protected CorpusRanker createCorpusRanker()
	{
		//类DefaultCorpusRanker继承AbstractCorpusRanker；
		//AbstractCorpusRanker实现了CorpusRanker接口；
		return new DefaultCorpusRanker(getCorpora(), getShortcutRepository());
	}

	/**
	 * Gets the shortcut repository. May only be called from the main thread.
	 */
	public ShortcutRepository getShortcutRepository()
	{
		checkThread();
		if (mShortcutRepository == null)
		{
			mShortcutRepository = createShortcutRepository();
		}
		return mShortcutRepository;
	}
	
	//创建ShortcutRepositoryImplLog对象
	protected ShortcutRepository createShortcutRepository()
	{
		//创建日志线程
		ThreadFactory logThreadFactory = new NamingThreadFactory(
				"ShortcutRepositoryWriter #%d", new PriorityThreadFactory(
						Process.THREAD_PRIORITY_BACKGROUND));
		
		//创建了一个新线程
		Executor logExecutor = Executors.newSingleThreadExecutor(logThreadFactory);
		
		return ShortcutRepositoryImplLog.create(this, getConfig(),
				getCorpora(), getShortcutRefresher(), getMainThreadHandler(),
				logExecutor);
	}

	/**
	 * Gets the shortcut refresher. May only be called from the main thread.
	 */
	public ShortcutRefresher getShortcutRefresher()
	{
		checkThread();
		if (mShortcutRefresher == null)
		{
			mShortcutRefresher = createShortcutRefresher();
		}
		return mShortcutRefresher;
	}

	protected ShortcutRefresher createShortcutRefresher()
	{
		// For now, ShortcutRefresher gets its own SourceTaskExecutor
		return new SourceShortcutRefresher(createSourceTaskExecutor());
	}

	/**
	 * Gets the source task executor. May only be called from the main thread.
	 */
	public NamedTaskExecutor getSourceTaskExecutor()
	{
		checkThread();
		if (mSourceTaskExecutor == null)
		{
			mSourceTaskExecutor = createSourceTaskExecutor();
		}
		return mSourceTaskExecutor;
	}

	protected NamedTaskExecutor createSourceTaskExecutor()
	{
		Config config = getConfig();
		ThreadFactory queryThreadFactory = getQueryThreadFactory();
		return new PerNameExecutor(SingleThreadNamedTaskExecutor
				.factory(queryThreadFactory));
	}

	/**
	 * Gets the query thread factory. May only be called from the main thread.
	 */
	protected ThreadFactory getQueryThreadFactory()
	{
		checkThread();
		if (mQueryThreadFactory == null)
		{
			mQueryThreadFactory = createQueryThreadFactory();
		}
		return mQueryThreadFactory;
	}

	protected ThreadFactory createQueryThreadFactory()
	{
		String nameFormat = "QSB #%d";
		int priority = getConfig().getQueryThreadPriority();
		return new NamingThreadFactory(nameFormat, new PriorityThreadFactory(
				priority));
	}

	/**
	 * Gets the suggestion provider. May only be called from the main thread.
	 */
	protected SuggestionsProvider getSuggestionsProvider()
	{
		checkThread();
		if (mSuggestionsProvider == null)
		{
			mSuggestionsProvider = createSuggestionsProvider();
		}
		return mSuggestionsProvider;
	}

	/**
	 * 
	 * @return
	 */
	protected SuggestionsProvider createSuggestionsProvider()
	{
		Promoter promoter = new ShortcutPromoter(new RankAwarePromoter(getConfig(), getCorpora()));
		SuggestionsProvider provider = new SuggestionsProviderImpl(getConfig(),
				getSourceTaskExecutor(), getMainThreadHandler(), promoter,
				getShortcutRepository(), getCorpora(), getLogger());
		return provider;
	}

	/**
	 * Gets the suggestion view factory. May only be called from the main
	 * thread.
	 */
	public SuggestionViewFactory getSuggestionViewFactory()
	{
		checkThread();
		if (mSuggestionViewFactory == null)
		{
			mSuggestionViewFactory = createSuggestionViewFactory();
		}
		return mSuggestionViewFactory;
	}

	protected SuggestionViewFactory createSuggestionViewFactory()
	{
		return new SuggestionViewInflater(this);
	}

	/**
	 * Gets the corpus view factory. May only be called from the main thread.
	 */
	public CorpusViewFactory getCorpusViewFactory()
	{
		checkThread();
		if (mCorpusViewFactory == null)
		{
			mCorpusViewFactory = createCorpusViewFactory();
		}
		return mCorpusViewFactory;
	}

	protected CorpusViewFactory createCorpusViewFactory()
	{
		return new CorpusViewInflater(this);
	}

	/**
	 * Creates a suggestions adapter. May only be called from the main thread.
	 */
	public SuggestionsAdapter createSuggestionsAdapter()
	{
		Config config = getConfig();
		SuggestionViewFactory viewFactory = getSuggestionViewFactory();
		DelayingSuggestionsAdapter adapter = new DelayingSuggestionsAdapter(viewFactory);
		return adapter;
	}

	/**
	 * Creates a footer view to add at the bottom of the search activity.
	 */
	public SuggestionsFooter createSuggestionsFooter()
	{
		return new EmptySuggestionsFooter(this);
	}

	/**
	 * Gets the event logger. May only be called from the main thread.
	 * 要将此方法的调用去掉
	 */	
	public Logger getLogger()
	{
		checkThread();
		if (mLogger == null)
		{
			mLogger = createLogger();	//创建日志事件
		}
		return mLogger;
	}
	
	/**在此处启动写日志文件？要将其去掉*/
	protected Logger createLogger()
	{
		return new EventLogLogger(this, getConfig());
	}
}
