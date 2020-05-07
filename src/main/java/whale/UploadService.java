package whale;

import java.util.Hashtable;

import whale.obj.UploadCache;
import whale.obj.UploadFile;
import whale.obj.UploadInfo;
import whale.obj.UploadResult;
import whale.util.UploadWorker;

public class UploadService {
	
	public static final boolean LOGGING = UploadConfig.LOGGING_DEFAULT;
	
	private UploadLogger log;
	private UploadWorker worker;
	private Hashtable<String, UploadCache> mapper =
			new Hashtable<>();
	
	/**
	 * 
	 * 使用默认上传服务基本配置创建上传接口示例
	 */
	public UploadService(){
		this(UploadConfig.defaultConfig());
	}
	
	/**
	 * 
	 * @param config 上传服务基本配置
	 */
	public UploadService(UploadConfig config){
		this(config, null);
	}
	
	/**
	 * 
	 * @param log 日志接口
	 */
	public UploadService(UploadLogger log){
		this(null, log);
	}
	
	public UploadService(
			UploadConfig conf, UploadLogger logger){
		log = logger;
		
		if(conf==null){
			UploadConfig.setUploadConfig(
					UploadConfig.defaultConfig());
		}else{
			UploadConfig.setUploadConfig(conf);
		}
		if(log==null){
			log = new UploadLogger();
		}
	}
	
	public void setUploadConfig(UploadConfig conf){
		if(conf!=null){
			UploadConfig.setUploadConfig(conf);
		}
	}
	
	public UploadResult upload(UploadInfo info, UploadFile[] files,
			IUploadCallback callback) throws Exception{
		UploadResult ret = new UploadResult(info, files);
		if(LOGGING){
			log.info("new upload job...");
		}
		if(UploadConfig.Sync()){
			UploadWorker w = new UploadWorker(this, log);
			w.doUpload(ret);
			
		}else{
			// 登记上传任务
			UploadCache cache = new UploadCache(ret, callback);
			mapper.put(info.getKey(), cache);
			// 检查服务线程是否启动，没有则启动
			startOrStop(true);
		}
		return ret;
	}
	
	public UploadResult upload(UploadInfo info, UploadFile file,
			IUploadCallback callback) throws Exception{
		return upload(info, new UploadFile[]{file}, callback);
	}
	
	/**
	 * 上传多个文件
	 * @param info
	 * @param files
	 * @return
	 * @throws Exception
	 */
	public UploadResult upload(UploadInfo info, UploadFile[] files)
			throws Exception{
		return upload(info, files, null);
	}
	
	/**
	 * 上传单个文件
	 * @param info
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public UploadResult upload(UploadInfo info, UploadFile file)
			throws Exception{
		return upload(info, file, null);
	}
	
	/**
	 * 查询文件上传记录；
	 * 注意：查询到上传文件对应的记录并不代表上传文件任务执行完成。
	 * @param info
	 * @return
	 */
	public UploadResult checkResult(UploadInfo info){
		return checkResult(info.getKey());
	}
	
	/**
	 * 查询文件上传记录；
	 * 注意：查询到上传文件对应的记录并不代表上传文件任务执行完成。
	 * @param key
	 * @return
	 */
	public UploadResult checkResult(String key){
		// 返回上传任务结果（或过程中处理进度或异常信息）
		UploadCache cache = mapper.get(key);
		if(cache==null){
			return null;
		}
		UploadResult ret = cache.getResult();
		if(cache.getCallback()==null && ret.isFinished()){
			// 已经上传完成并且调用端调用后，不在缓存。
			mapper.remove(key);
		}
		return ret;
	}
	
	synchronized public void startOrStop(boolean isStart){
    	if(isStart && worker==null){
    		worker = new UploadWorker(this, log);
    		worker.start();
    		if(LOGGING){
    			log.info("started upload worker.");
    		}
    	}
    	if(isStart){
    		worker.refresh();
    	}else{
    		worker.isRunning(false);
    		worker = null;
    		if(LOGGING){
    			log.info("stopped upload worker.");
    		}
    	}
    }
	
	public Hashtable<String, UploadCache> getMapper(){
		return mapper;
	}
}
