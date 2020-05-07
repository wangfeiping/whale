package whale;

import lombok.Data;

@Data
public class UploadConfig {

	public static final String API_DEFAULT =
//			"http://dev-apis.qianbao.com/basicservice/v1/intranet/filer";
	        "http://172.17.15.194:9330/filer";
//	        "http://loan.qianbao.com/file/weed.do";
//	        "http://apis.qianbao.com/basicservice/v1/intranet/filer";
	
	public static final String UNI_SOURCE_DEFAULT = "whale-dev/APK";
	
	/**
	 * https://en.wikipedia.org/wiki/Uniform_Resource_Identifier
	 * scheme:[//[user[:password]@]host[:port]][/path][?query][#fragment]
	 */
	public static final String SCHEME_DEFAULT = "http";
	
	public static final String ROOT_FOLDER_DEFAULT = "/public/test/";
	
	public static final int MAX_BYTES_SIZE_DEFAULT = 4 * 1024 * 1024;
	
	public static final int WORKER_REFRESH_MILLIS_DEFAULT = 1000;
	
	public static final int TIMEOUT_MILLIS_DEFAULT = 60000;
	
	public static final boolean LOGGING_DEFAULT = true;
	
	// 上传接口调用是否同步返回结果
	public static final boolean SYNC_DEFAULT = false;
	
	private static UploadConfig config;
	
	private String api = API_DEFAULT;
	private String uniSource = UNI_SOURCE_DEFAULT;
	private String scheme = SCHEME_DEFAULT;
	private String rootFolder = ROOT_FOLDER_DEFAULT;
	private int maxBytesSize = MAX_BYTES_SIZE_DEFAULT;
	private int workerRefreshMillis = WORKER_REFRESH_MILLIS_DEFAULT;
	private int timeoutMillis = TIMEOUT_MILLIS_DEFAULT;
	private boolean sync = SYNC_DEFAULT;
	
	public static UploadConfig defaultConfig(){
		UploadConfig c = new UploadConfig();
		return c;
	}
	
	public static void setUploadConfig(UploadConfig conf){
		config = conf;
	}
	
	public static String Api(){
		return config.api;
	}
	
	public static String UniSource(){
		return config.uniSource;
	}
	
	public static String Scheme(){
		return config.scheme;
	}
	
	public static String RootFolder(){
		return config.rootFolder;
	}
	
	public static int MaxBytesSize(){
		return config.maxBytesSize;
	}
	
	public static int WorkerRefreshMillis(){
		return config.workerRefreshMillis;
	}
	
	public static int TimeoutMillis(){
		return config.timeoutMillis;
	}
	
	public static boolean Sync(){
		return config.sync;
	}
}
