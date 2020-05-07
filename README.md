### whale 使用开发简明文档

> \# 版本: 0.0.0-SNAPSHOT

该项目的开发目的是尽可能的减少项目依赖，建立前后端可通用的文件上传服务sdk。  

封装了相对复杂但是必须的上传协议，以实现超大文件和多文件上传，并且最大限度的简化调用端的操作和开发。  

### 上传单个文件

只要调用端运行环境允许，不限制文件大小。  

```
// 实例化上传服务
UploadService srv =
		new UploadService();

// 实例化上传文件封装对象，需要设置本地读取路径
UploadFile f = new UploadFile();
f.setReadPath("/test.jpg");

// 实例化上传参数，如上传路径
UploadInfo info = new UploadInfo();
info.setPath(path);

// 执行上传
UploadResult ret = srv.upload(info, f);

// 查询结果
ret = srv.checkResult(info);

```

UploadInfo 中每次实例化时会生成一个UUID，可用来进行本次请求的查询。

### 输出日志

为了减少依赖，使前后端同时可用，因此没有直接使用log4j等第三方日志工具，而是定义了日志接口。  
实例化UploadService 时作为参数传入即可。  

```
// 日志空实现
package whale;

public class UploadLogger {

	public void trace(String msg){}
	
	public void debug(String msg){}
	
	public void info(String msg){}
	
	public void warn(String msg){}
	
	public void error(String msg, Throwable th){}
	
	public void fatal(String msg, Throwable th){}
}
```

```
// 设置日志输出

UploadServerLogger log = new UploadServerLogger();

UploadService srv =
		new UploadService(log);
```

### 上传多个文件

只要调用端运行环境允许，不限制文件数量和文件大小。  

```
// 实例化上传服务
UploadService srv =
		new UploadService();

// 实例化上传文件封装对象，需要设置本地读取路径
UploadFile f = new UploadFile();
f.setReadPath("/test.jpg");

UploadFile f1 = new UploadFile();
f1.setReadPath("/test1.jpg");

UploadFile f2 = new UploadFile();
f2.setReadPath("/test2.jpg");

// 定义数组
UploadFile[] files = {f, f1, f2};

// 实例化上传参数，如上传路径
UploadInfo info = new UploadInfo();
info.setPath(path);

// 执行多文件上传
UploadResult ret = srv.upload(info, files);

// 查询结果
ret = srv.checkResult(info);

```

### 配置上传服务

实例化UploadConfig 并设置相应属性，然后作为参数传入UploadService 构造函数中即可。如果没有设置，会使用代码默认的配置。

```
public class UploadConfig {
	
	// 上传文件服务远程调用接口；
	private String api;
	
	// 上传文件远程接口要求的数据头信息，以便于区分业务和统计分析；
	private String uniSource;
	
	// 上传文件的文件根路径；
	private String rootFolder;
	
	// 上传文件单个请求允许的数据量大小，单个文件或多个文件超过改大小都会自动拆包上传；
	private int maxBytesSize;
	
	// 上传任务在独立线程中处理，线程每次处理完成后，会按照该配置睡眠一段时间以便于释放上传时抢占的系统资源；
	private int workerRefreshMillis;
	
	// 上传线程在空闲时间超过该配置时会检查和清理缓存的上传任务结果，超过两倍的配置时间会自动结束线程运行。
	private int timeoutMillis;
	
	// 上传接口调用是否同步返回结果，默认为false
	private boolean sync;
......
```

```
// 配置上传文件服务

UploadConfig config = new UploadConfig();
config.setXXX
...


UploadService srv =
		new UploadService(config);
```

### 同步调用上传文件接口

```
    UploadConfig conf = new UploadConfig();
    conf.setSync(true);
    UploadService srv = new UploadService(conf);
    UploadResult ret = srv.upload(......);

```
