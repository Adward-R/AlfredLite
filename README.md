AlfredLite 技术说明文档
====

---

###**Project of Intelligent Terminal Course**

**2014-15 Autumn, ZJU**

*——An enhanced version of **toraleap.collimator**, Android search tool*

 - An open source software under Apache License 2.0 as our reference of developing this new search application;
 
 - To checkout original source code please turn to
      

	`https://code.google.com/p/collimator/source/checkout`
     
    
     or run the following command in terminal:
     
     
    `svn checkout http://collimator.googlecode.com/svn/trunk/ collimator-read-only`

---
##注意：

 - 本应用构建于Android 4.4.2版本内核（API Level 19），最低支持的SDK版本为10；
 
 - 将本项目导入为Eclipse、ADT、Intellij IDEA或Android Studio项目时请不要选择将jar包解开为依赖关系或重组为Gradle项目，否则会找不到引用的外部库而引发编译错误；
 
 - 若要查看使用帮助，请在打开应用后按菜单键或底下的状态栏，在弹出的菜单中选择帮助，即可浏览内嵌的帮助文档；
 
 - 本技术文档由Markdown标记语言渲染，建议用现代网页浏览器打开以保证层次结构效果。

---
##I. 项目成员及分工

---

##II. 外部库引用

 - ###org.mozilla.juniversalchardet
 	
 	  - 字符编码解析库universalchardet的Java接口，支持UTF-8、GB18030等几十种编码的识别；
 	  - 具体支持的编码和用法示例见 `https://code.google.com/p/juniversalchardet/`；
 	  
 	  - 源代码见 `http://lxr.mozilla.org/seamonkey/source/extensions/universalchardet/`；
 	  
 	  - 详细的技术文档见 ` http://www.mozilla.org/projects/intl/UniversalCharsetDetection.html`。 
 	     
 ---


##III. 程序架构 （src）

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
		 
		 	  新增的应用搜索功能的工具类，包括：
		 	  1. public List<Map<String,Object>> getUserApps(Context, String[]) : 
		 	  通过上下文调用系统的PackageManager类获得已安装的各类应用程序包的信息PackageInfo，利用PackageInfo.applicationInfo中的标志位判断并筛选出用户安装而非系统原生或预刷的应用；然后若该应用的标签名能够匹配参数中的全部关键词，就将其packageLabel、packageName和packageIcon字段放入返回列表的一个HashMap项中，以备提供给结果呈现列表的Adapter。能够支持关键字的拼音首字母匹配。
		 	  
		 	  2. public void openApp(String) : 
		 	  应用搜索模式下单击结果列表项时被调用，通过获取的包名检索出对应的主Activity名称，进而实现打开相应的应用；若遇到特殊情况打不开应用（偶尔有系统级应用会出现问题）或名字提供错误，则抛出NameNotFoundException。
		 	  
		- ####ContactUtil
		
			  新增的联系人搜索工具类，包括：
			  1. public void reloadContactIndex() : 
			  因为联系人搜索是在二维的内容提供表中进行爬取和判断，复杂度比应用搜索要高，不建立索引的话效率是不可接受的。使用ContentResolver类提供的方法对联系人表的Uri进行解析，在查询得到的表中先获得联系人ID为入口，以之再次查询并以爬取结果行中mimeType符合联系人名字、电话的数据项插入重新初始化的数据表中；同时存入联系人名字经转化后的首字母小写字符串形式。若一个联系人有多个电话项，采取冗余方法存储。该方法在Index类的异步线程中被调用，以免影响I/O响应造成严重卡顿。
			  
			  2. public List<Map<String,Object>> getUserContacts(String[]) : 
			  用指定的关键词参数在内建的联系人信息数据库中依赖索引进行匹配，返回的各字段同样以HashMap列表的形式提供给主活动的Adapter。支持电话号码的匹配、联系人名字的匹配以及联系人名字首字母序列的匹配。
			  
	
	
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
	
	- ###SearchActivity `extends Activity`
	
		  作为AlfredLite的主Activity串接起了主界面上各控件之间的响应关系。
		  定义了若干系统级别的类，包括SharedPreferences，Notification，NotificationManager等；
		  定义了各工具类和容器类的对象，当前状态变量，首选项参数，界面控件（结果呈现列表及其各Adapter等）；
		  重写了主要控件的若干事件响应方法，对话框创建器和作为内部类的消息处理器。
		
---
		
##IV. 关键代码说明

####1. Index类中异步重载索引的线程函数：
	
	public static synchronized void reloadEntriesAsync() {
		status = STATUS_RELOADING;
		new Thread(new Runnable() {
			public void run() {
				interrupt();
				data = null;
				// 读取首选项中关于索引的项目（是否索引系统文件、隐藏文件等）对IndexLoader类进行初始化，并获取全局context用以初始化能够调用联系人索引重建方法的工具类
				IndexLoader loader = new IndexLoader(sPrefs);
				ContactUtil contactUtil = new ContactUtil(GlobalContext.getInstance());
				try {
					// 重建本地文件和联系人的数据库索引，完成后自动对IndexData进行序列化
					IndexData newData = loader.reload();
					contactUtil.reloadContactIndex();
					IndexLoader.serialize(newData);
					data = newData;
					sPrefs.edit().putBoolean("index_is_obsolete", false).commit();
					status = STATUS_READY;
					checkObsolete();
					// 通知消息处理器重建已经完成，接下来的查询从新索引读取
					sendHandlerMessage(MESSAGE_RELOAD_SUCCESS, 0, 0, null);
				} catch (NoSDCardException e) {
					status = STATUS_FAILED;
					sendHandlerMessage(MESSAGE_NOSDCARD, 0, 0, null);
				} catch (SerializingException e) {
					status = STATUS_FAILED;
					sendHandlerMessage(MESSAGE_SERIALIZING_FAILED, 0, 0, null);
				}
			}
		}).start();
	}

####2. IndexLoader类中根据构造函数获取的位置，扫描SD卡重建文件索引的函数：

	public IndexData reload() throws NoSDCardException {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			throw new NoSDCardException();
		}
		// 准备初始栈
		Stack<String> stack = new Stack<String>();
		stack.push(Environment.getExternalStorageDirectory().getPath());
		// 准备缓存列表
		IndexData data = new IndexData();
		ArrayList<String> lName = new ArrayList<String>();
		ArrayList<String> lPath = new ArrayList<String>();
		ArrayList<String> lNameAlpha = new ArrayList<String>();
		ArrayList<String> lPathAlpha = new ArrayList<String>();
		ArrayList<Long> lSize = new ArrayList<Long>();
		ArrayList<Long> lTime = new ArrayList<Long>();
		// 开始文件遍历
		while (!stack.isEmpty()) {
			String parent = stack.pop();
			String parentAlpha = null;
			if (isIndexFirstLetter) {
				parentAlpha = Unicode2Alpha.toAlpha(parent);
			}
			File path = new File(parent);
			File[] files = path.listFiles();
			if (null == files) continue;
			for (File f : files){
				if (f.isDirectory()) { 
					// 判断该路径在目前设置下是否应该被索引
					if (isQualifiedDirectory(f)) 
						stack.push(f.getPath());
					}
					else {
						// 判断该文件在目前设置下是否应该被索引
						if (isQualifiedFile(f)) {
							lName.add(f.getName());
							lPath.add(parent);
							lTime.add(f.lastModified());
							lSize.add(f.length());
							if (isIndexFirstLetter) {
								lNameAlpha.add(Unicode2Alpha.toAlpha(f.getName()));
								lPathAlpha.add(parentAlpha);
							} else {
								lNameAlpha.add("");
								lPathAlpha.add("");
							}
						}	
					}
				}
			}
			// 重设索引相关的信息，用以判断索引是否过时需要重建等
			int length = lName.size();
			data.indexTime = System.currentTimeMillis();
			data.availableSpace = getAvailableSpace();
			data.name = lName.toArray(new String[length]);
			data.path = lPath.toArray(new String[length]);
			data.nameAlpha = lNameAlpha.toArray(new String[length]);
			data.pathAlpha = lPathAlpha.toArray(new String[length]);
			data.size = new long[length];
			for (int i = 0; i < length; i++) 
				data.size[i] = lSize.get(i).longValue();
			data.time = new long[length];
			for (int i = 0; i < length; i++) 
				data.time[i] = lTime.get(i).longValue();
			return data;
		}
		
####3. AppUtil类中获取能够匹配的App信息列表的函数：

	public List<Map<String,Object>> getUserApps(Context context,String[] keys) {
        	List<Map<String,Object>> apps = new ArrayList<Map<String,Object>>();
        	PackageManager pManager = context.getPackageManager();
        	// 获取手机中安装的全部包的信息
        	List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        	for (int i = 0; i < paklist.size(); i++) {
            	PackageInfo pak = paklist.get(i);
            	// 判断是否是预装的系统级包
            	if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {
                	// 是用户安装的应用，应该被检索出
                	String pkgLabel = pManager.getApplicationLabel(pak.applicationInfo).toString();
                	int flag = 0;
                	for (int j=1;j<keys.length;j++){
                    	boolean flag1 = pkgLabel.toLowerCase().contains(keys[j].toLowerCase());
                    	boolean flag2 = Unicode2Alpha.toAlpha(pkgLabel).contains(Unicode2Alpha.toAlpha(keys[j]));
                    	if (!flag1&&!flag2){
                        	flag++;
                        	break;
                    	}
                	}
					// 如果该App的Label可以以小写字母或首字母序列中的任何一种方式匹配全部的关键字
                	if (flag==0) {
                    	Map<String, Object> listItem = new HashMap<String, Object>();
                    	listItem.put("pkgName", pak.packageName);
                    	listItem.put("pkgLabel", pkgLabel);
                    	apps.add(listItem);
                   }
            	}
        	}
        	return apps;
    	}
    	
    
####4. AppUtil类中通过包名寻找默认活动并打开相应应用的函数：

	public void openApp(String packageName) throws PackageManager.NameNotFoundException {
        PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);

		// 通过包名获得Manifest中定义的所有Activity
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(pi.packageName);

        List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(resolveIntent, 0);
		// 用迭代器获得默认的活动名称并构建Intent
        ResolveInfo ri = apps.iterator().next();
        if (ri != null ) {
            String pkgName = ri.activityInfo.packageName;
            String className = ri.activityInfo.name;

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            ComponentName cn = new ComponentName(pkgName, className);

	        intent.setComponent(cn);
    	    context.startActivity(intent);
        }
    }  
    	
####5. ContactUtil类中重建联系人索引的函数：

	public void reloadContactIndex(){
        SQLiteDatabase db = context.openOrCreateDatabase("database.db",0,null);
		// 专用的联系人数据表中存储名字、电话号码和名字的拼音首字母小写序列
        db.execSQL("DROP TABLE IF EXISTS data");
        db.execSQL("CREATE TABLE data ( " +
                "contactName varchar(20)," +
                "contactPhoneNum varchar(20)," +
                "contactAlpha varchar(20)" +
                ")");

        db.execSQL("CREATE INDEX data1_index ON data(contactName,contactPhoneNum,contactAlpha)");

		// 使用ContentResolver提供的方法，通过Uri解析查询得到联系人ID
        Uri uri = Uri.parse("content://com.android.contacts/contacts");
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[] { "_id" }, null, null, null);

        while (cursor.moveToNext()) {
            int contactID = cursor.getInt(0);
            // 以ID为入口再查询得到存储该联系人信息的表格行
            uri = Uri.parse("content://com.android.contacts/contacts/"
                    + contactID + "/data");
            Cursor cursor1 = resolver.query(uri, new String[] { "mimetype",
                    "data1" }, null, null, null);
            ContentValues cValue = new ContentValues();
            // 定义标志位：是否是该联系人的第一个电话号码（是否需要冗余存储）
            boolean isFirstNum = true;
            boolean insertFlag = true; // 定义标志位：是否符合结果需要的条件
            String contactName = "";
            while (cursor1.moveToNext()){
                String data1 = cursor1.getString(cursor1.getColumnIndex("data1"));
                if(data1 == null) {
                    insertFlag = false;
                    break;
                }
                // 在行内找到相应mime类型的数据并插入自建的专用数据库
                String mimeType = cursor1.getString(cursor1.getColumnIndex("mimetype"));
                if(mimeType.equals("vnd.android.cursor.item/name")){
                    contactName = data1;//.toLowerCase();
                    cValue.put("contactName", contactName);
                    cValue.put("contactAlpha", Unicode2Alpha.toAlpha(contactName));
                }
                if(mimeType.equals("vnd.android.cursor.item/phone_v2")) {
                    if(isFirstNum){
                        cValue.put("contactPhoneNum",data1);
                        isFirstNum = false;
                    }
                    else { // 若有两个以上的电话号码，需要冗余存储
                        ContentValues newcValue = new ContentValues();
                        newcValue.put("contactName",contactName);
                        newcValue.put("contactPhoneNum",data1);
                        newcValue.put("contactAlpha", Unicode2Alpha.toAlpha(contactName));
                        db.insert("data",null,newcValue);
                    }
                }
            }
            if (insertFlag) { // 包装类提供的简单的写法，也可以写作SQL语句
                db.insert("data", null, cValue);
            }
        }
        db.close();
    }
    
####6. ContactUtil类中获取匹配给定关键字序列的联系人列表的函数：

	public List<Map<String,Object>> getUserContacts(String[] keys) {
        SQLiteDatabase db = context.openOrCreateDatabase("database.db",0,null);
        List<Map<String,Object>> contacts = new ArrayList<Map<String,Object>>();
        // keys[0]是输入文本中标志着查询关键字（命令）的一项，不属于有效关键字
        String totalKey = "%";
        for(int j = 1;j < keys.length;j++){
            totalKey += keys[j].toLowerCase()+"%";
        }
        // 查询语句使用支持通配符和混杂大小写的like
        Cursor cursor = db.rawQuery("SELECT contactName,contactPhoneNum,contactAlpha FROM data WHERE contactName like "
                +"'"+totalKey+"'"+" or "+ "contactPhoneNum like " +"'"
                +totalKey+"'"+" or "+ "contactAlpha like " +"'"+Unicode2Alpha.toAlpha(totalKey)+"'", null);

        if(cursor.moveToFirst()){
            for(int i = 0; i < cursor.getCount();i++){
                Map<String,Object> contact = new HashMap<String, Object>();
                contact.put("contactName", cursor.getString(cursor.getColumnIndex("contactName")));
                contact.put("contactPhoneNum", cursor.getString(cursor.getColumnIndex("contactPhoneNum")));
                contacts.add(contact);
                cursor.moveToNext();
            }
        }
        db.close();
        return contacts;
    }
    
####7. SearchActivity主活动中调用搜索呈现结果的函数，也是实现命令式搜索（本地搜索与应用和联系人搜索之间即时切换）的接口：

	private void doSearch() {
		if (Index.getStatus() == Index.STATUS_READY || Index.getStatus() == Index.STATUS_OBSOLETE) {
			// 在Index标志自己可用时启动检索，发出中断重建索引线程的请求
			isSearching = true;
			Index.interrupt();
			mSearchResult.clear();

			// 新搜索模式需要从Expression类中拿到搜索关键字的原始文本，重写它的匹配方法
			String key = mExpression.getKey();
			String[] str = key.split(" ");
			输入特殊搜索命令后，需要后跟空格以及可选的若干关键字才会启动非本地文件搜索
			if (str[0].equals(appCmd) && (str.length > 1 || key.equals(appCmd + " "))) {
				mode = 1; //指示搜索模式切换到应用搜索
				isSearching = false; //应用搜索效率较高，不需要异步线程
				if (str.length > 1) {
					apps = appUtil.getUserApps(this, str);
				} else { // 如果空格后未跟任何关键字视为“”，即匹配所有
					String _str[] = {appCmd, ""};
					apps = appUtil.getUserApps(this, _str);
				}
				// 接收结果数据的列表，构建内容适配器
				mAdapter = new SimpleAdapter(this, apps,
						R.layout.listitem_apps,
						new String[]{"pkgIcon", "pkgLabel", "pkgName"},
						new int[]{R.id.thumbnail, R.id.filename, R.id.filepath});
				mListEntries.setAdapter(mAdapter);
				// 为了显示应用的图标，需要视图的动态绑定器
				mAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
					public boolean setViewValue(View view, Object data, String textRepresentation) {
						if (view instanceof ImageView && data instanceof Drawable) {
							ImageView iv = (ImageView) view;
							iv.setImageDrawable((Drawable) data);
							return true;
						} else return false;
					}
				});
				// 底部状态栏显示的文字
				mTextStatus.setText(apps.size() + " applications found.");
			} else if (str[0].equals(contactCmd) && (str.length > 1 || key.equals(contactCmd + " "))) {
				mode = 2; // 指示当前未联系人搜索模式
				isSearching = false; //if the searching period is short enough
				if (str.length > 1) {
					apps = contactUtil.getUserContacts(str);
				} else {
					String _str[] = {contactCmd, ""};
					apps = contactUtil.getUserContacts(_str);
				}
				mAdapter = new SimpleAdapter(this, apps,
						R.layout.listitem_apps,
						new String[]{/*"contactPhoto",*/"contactName", "contactPhoneNum"},
						new int[]{/*R.id.thumbnail,*/R.id.filename, R.id.filepath});
				mListEntries.setAdapter(mAdapter);
				mTextStatus.setText(apps.size() + " contacts found.");
			} else {
				// 否则启动本地文件搜索的异步匹配
				mode = 0;
				mListEntries.setAdapter(mListAdapter);
				mExpression.matchAsync();
			}

		}
	}
	