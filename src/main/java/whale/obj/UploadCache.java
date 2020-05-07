package whale.obj;

import lombok.Data;
import whale.IUploadCallback;
import whale.UploadConfig;

@Data
public class UploadCache {

	/**
	 * 上传返回结果（成功或失败）的时间。
	 * 
	 * 文件上传为独立线程负责，上传文件返回结果会缓存起来，
	 * 此变量主要作为返回结果超时清理等操作的时间标记。
	 */
	private long responseTime;
	
	private UploadResult result;
	private IUploadCallback callback;
	
	public UploadCache(UploadResult result, IUploadCallback callback){
		this.result = result;
		this.callback = callback;
		refresh();
	}
	
	public void refresh() {
		responseTime = System.currentTimeMillis();
	}
	
	/**
	 * 缓存是否超时
	 * @return
	 */
	public boolean isTimeout(){
		return System.currentTimeMillis()-responseTime
				> UploadConfig.TIMEOUT_MILLIS_DEFAULT;
	}
}
