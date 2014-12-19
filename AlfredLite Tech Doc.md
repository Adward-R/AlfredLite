AlfredLite 技术说明文档
====

****

#I. 程序架构

##src 
`除非注明，以下斜体字为文件夹，正体表示"*.java"文件所定义的类名`

 -  ###*android.media*
	
	 - ####MediaMetadataRetriever
	 
	 		Android的media包提供的获取媒体文件元数据的工具类，可拓展；主要用来获取本地视频或音频文件的可播放时长与帧宽度数据用以在搜索结果中显示摘要。
	 

 -  ###*com.android.AlfredLite*
	
	- ###*bll*
		
		- ####FileScannerService `extends Service`
		
		      继承了安卓SDK的服务类，是建立本地文件索引的核心实现代码；
		      重载了onCreate()等若干方法；
		      scanAll完整扫描SD卡并重新建立索引，重写继承了FileRecursiveObserver的RealtimeScanner内部类递归检索本地文件夹，并用isQualifiedDirectory()和isQualifiedFile()判断是否应该压到重建栈中。
		      		
		- ####TagGenerator
		
			  model.BaseTag的包装类，处理各个文件并为它们生成对应的类型标签，方便分类索引。
			  
	- ###*cmd*
	
		 - ####AppUtil
		 
		 	  
	
	
	- ###*dal*
		
		- ####`interface` DBColumns `extends BaseColumns`
		
			  定义了一系列的常量字符串，实质上就是文件索引数据库表头的各字段名。
		
		- ####DBHelper `extends SQLiteOpenHelper`
		
			  重载了一系列安卓SDK提供的用于操作SQLite数据库的方法，主要是建表、删表、在某些查询需要的字段上建立索引以及建立视图；
			  获取数据库操作所需的句柄在这个类中被确保为线程安全的操作，因为用户可能在任何时候（异步地）提出查询或对文件的操作，需要避免其与数据库索引更动的冲突。
		
		- ####DBOperation
		
			  调用一系列针对文件查询和操作的数据库语句，包括Tag与File的互查、查看文件是否存在、是否被修改，以及应用本APP中对文件插入、更新乃至删除的操作。
			  
	- ###*data*
		
		- ####Expression
		
			  代表一个检索式，包含检索范围及排序方式等信息。支持序列化到JSON字符串，以及从JSON字符串恢复。
			  调用matchAsync()方法即使用此检索式进行异步匹配。期间产生的任何消息都将发送到 Matcher 类的消息处理器，解析给定的匹配表达式，并试图转换为 Matcher 对象数组；因此要截获消息，先调用 Matcher.init 方法注册消息处理器。如果已有一次匹配过程在进行中，将取消先前的匹配过程，然后启动新的匹配。
			  
		
		- ####Index
		
			  包含文件索引的相关操作，调用（或通知）一部分实现于IndexData和IndexLoader中的方法和数据对象，可以：
			  1. 返回文件索引的当前状态、是否过期的标志、条目总数、建立时间及建立时SD卡的剩余空间等信息；
			  2. 初始化程序与索引的关系，启动异步线程重载文件索引和联系人索引；
			  3. 进行索引对象的序列化和反序列化，完成相应操作后通知消息处理器；
			  4. 获取指定索引条目的各种相关信息，包括缩略图，以便在结果列表显示摘要，或执行（调用系统中合适的程序）定位、打开该文件条目的操作；
			  5. 物理删除指定文件条目（高危，需要在首选项中解锁）。
		
		- ####IndexData
		
			  存储文件索引及相关信息的数据结构，实现文件索引的序列化及反序列化。
		
		- ####IndexLoader
		
			  包含：
			  1. 关于索引的若干首选项参数：是否索引隐藏文件，是否索引系统文件，是否索引以"."开头的文件，是否索引首字母以及是否索引全部已知文件类型；
			  2. 文件索引的重建、序列化及反序列化的方法。
		
		- ####Match
		
			  每个该对象表示一个成功匹配的文件条目，包含：
			  1. 索引号，高亮范围（为了显示结果中的匹配字段）
			  2. 获取指向它的各必要索引信息、路径、文件名的各种表示以及摘要、元数据的方法。
		
		- ####Matcher
		
			  每个 Matcher 对象表示单个匹配判断条件。Matcher类不可被外部实例化，必须使用本类的静态方法进行匹配，其包含的方法大致分类为：
			  1. 依据匹配条件类型对测试索引条目进行匹配；
			  2. 获取匹配结果的匹配器类型、开始结束位置等各项信息；
			  3. 使用给定的表达式在文件索引中进行匹配（MatchAsync()及其调用的方法）；
			  4. 为了维护异步匹配线程的行为而与消息管理器交互的方法；
			  5. 初始化匹配条件与程序的关联（参数为默认首选项对象和主程序的消息处理器）。
		
		- ####Sorter
		
			  包含针对匹配结果集的一系列静态排序方法。
	
	- ###*ext*
	
		- ####GlobalProvider `extends ContentProvider`
		
			  继承了安卓SDK的ContentProvider类，使得在安卓原生的全局搜索应用QuickSearchBox中可以调用到本APP检索到的结果条目。
		
		- ####Playlist
		
			  用给定名称在媒体库中创建一个播放列表，若同名则覆盖之；新建过程中，先调用createPlaylist()方法返回媒体库中新建的列表的Uri，然后调用toMediaId()查询媒体数据库，按顺序把每一个结果项目文件的表示转换成媒体数据库中的ID表示。
	 
	- ###*model*
		
		- ####BaseTag
		
			  其各数据字段指示了这个标签拥有者文件的类型属性。
		
	- ###*ui*
	
		- ####FloatingDialog `extends Dialog`
		
			  点击搜索框左侧和右侧的按钮时出现的浮动面板，重写了安卓SDK提供的各事件响应函数和内容提供类BaseAdapter等。
		
		- ####MatchAdapter `extends BaseAdapter`
		
			  连接搜索服务主活动（SearchActivity）中结果列表 ListView 及其匹配结果集的内容适配器。
		
	- ###*util*
	
		- ####DigestUtil `extends SoftCache`
		
			  包含获取文件摘要的相关静态工具函数。根据完整的路径文件名获取其对应的摘要信息。对于文本文件，返回前120字节的数据；对于图片文件，返回图片尺寸；对于音乐文件，返回艺术家及专辑信息；其他类型返回null。针对每种类型都定义了一个函数调用。
		
		- ####FileInfo
		
			  提供与文件信息相关的静态工具函数,包括：
			  1. 分别获取主文件名部分和拓展名；
			  2. 获取文件的MIME类型值；
			  3. 获取文件长度的智能可读字符串形式；
			  4. 各种表示转换函数；
		
		- ####MimeTypeMap
		
			  包含从MIME类型到拓展名的双向映射表，调用其包含的各函数即可实现双向表示转换。
		
		- ####RecursiveFileObserver `extends FileObserver`
		
			  拓展了SDK的文件查看器类，使得可以递归地监管SD卡中的文件夹；当文件夹内容有更动时，被其他类调用而将读到的更动（文件句柄）压栈，同时随着其在文件系统中的每一步动作输出日志以备调试时查看。
			  
		- ####ShortcutHelper
		
			  包含生成桌面快捷方式搜索图标的相关工具。构造器接受一个Context和一个Intent参数，将ShortcutActivity发出的意向包装成一个快捷方式辅助类，调用install()将快捷方式图标置于桌面。
		
		- ####`abstract` SoftCache <*K*,*V*> 
		
			  代表一个软引用缓存器，派生类应重写 request 方法获取关键字对应的结果，参数K和V分别代表关键字和结果的类型。提供get()、getCache()等线程安全的方法以关键字请求获得对应结果，并维护一个有最大长度的请求队列；同时每次处理完请求都向主活动中注册的消息处理器Handler发出消息，若当前请求处理期间被中断后从队列恢复，则不发送任何消息。
			  DigestUtil和ThumbnailUtil实现了该抽象类的接口。
		
		- ####ThumbnailUtil `extends SoftCache`
		
			  缩略图获取工具类。内部包含缩略图软引用缓存器，当内存不足时能自动释放缓存以保证系统正常运行。内建缩略图尺寸为 96x96。根据完整的路径文件名获取其对应的缩略图。对于图片文件，尝试加载该图片并转换为缩略图；对于音乐文件，尝试加载对应封面图片；其他类型尝试加载资源文件里与扩展名对应的图标作为缩略图。如果未找到对应图标，则返回默认图标。
		
		- ####Unicode2Alpha
			
			  包含进行首字母转换的静态工具函数。此版本支持汉字和日语平假名片假名的转换。
			  toAlpha()方法将源字符串转换成其对应的小写首字母表达形式。数字、特殊符号等非转换范围的字符将不被转换，字符串长度保持不变；
			  toPureAlpha()方法则将源字符串转换成其对应的小写首字母表达形式。数字、特殊符号等非转换范围的字符将被去掉，字符串长度可能缩短。
	
	- ###GlobalContext `extends Application`
	
		  包含getInstance()方法，用以在使用new对象构造新对象等语境中需要上下文环境而无法获得时，返回一个全局Application的instance作为上下文，于是可以借此调用PackageManager等。
	
	- ###PrefsActivity `extends PreferenceActivity`
	
		  首选项界面的活动，加载并修改于默认的preferences.xml中（在三个语言包中分别有一份，根据系统的语言环境是简体中文、繁体中文或是英文自动切换字段的提示信息）。
	
	- ###HelpActivity `extends Activity`
	
		  显示帮助文档的活动，设置了JavaScript可调用的接口，渲染网页AlfredLite/assets/help-*.html来在窗口中显示帮助APP的帮助信息。
	
	- ###ShortcutActivity `extends Activity`
	
		  作为接口，弹出对话框让用户输入选定的快捷搜索名称，调用ShortcutHelper的方法创建之。
	
	- ###SearchActivity
		