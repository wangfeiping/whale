package whale.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import whale.UploadConfig;
import whale.UploadLogger;
import whale.UploadService;
import whale.obj.UploadCache;
import whale.obj.UploadFile;
import whale.obj.UploadResult;

public class UploadWorker extends Thread {

	public static final boolean LOGGING = UploadConfig.LOGGING_DEFAULT;
	
	private boolean isRunning = true;
	private long idleTime;
	private UploadService service;
	private UploadLogger log;
	
	public UploadWorker(
			UploadService srv, UploadLogger logger){
		service = srv;
		log = logger;
		refresh();
	}
	
	public boolean isRunning(){
		return isRunning;
	}
	
	public void isRunning(boolean yes){
		isRunning = yes;
	}
	
	public void refresh(){
		idleTime = System.currentTimeMillis();
	}
	
	public void run(){
		if(LOGGING){
			log.info("upload worker is started...");
		}
		while(isRunning){
			if(LOGGING){
				log.debug("upload worker is running...");
			}
			try{
				handle(service.getMapper());
				Thread.sleep(UploadConfig.WorkerRefreshMillis());
				checkRunning();
			}catch(Throwable th){
				if(LOGGING){
					log.fatal(th.getMessage(), th);
				}
			}
		}
		if(LOGGING){
			log.info("upload worker is shutdown.");
		}
	}
	
	private void handle(Hashtable<String, UploadCache> mapper){
		if (mapper.size()<=0){
			if(LOGGING){
				log.debug("mapper size=0");
			}
			return;
		}
		Enumeration<String> keys = mapper.keys();
		while(keys.hasMoreElements()){
			String key = keys.nextElement();
			UploadCache cache = mapper.get(key);
			UploadResult ret = cache.getResult();
			
			if(ret.isPending()){
				// 未处理，修改状态进一步处理。
				ret.setStatus(UploadResult.STATUS_PROCESSING);
			}else{
				// 这里的状态只会有未处理和已完成（成功或失败）
				if(ret.isFinished()
						&& (checkCallback(cache)
								|| cache.isTimeout())){
					// 超时或已触发结束状态回调则删除缓存，
					//否则需要继续缓存以等待可能的查询。
					if(LOGGING){
						log.debug("removing "+key);
					}
					mapper.remove(key);
				}
				// 已处理未超时，跳过继续缓存等待查询。
				continue;
			}
			
			try{
				doUpload(cache, ret);
			}catch(Exception e){
				ret.setStatus(400);
				ret.setMessage("error");
				ret.setDetail(e.getMessage());
				if(LOGGING){
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	public void doUpload(UploadResult ret) throws Exception {
		doUpload(null, ret);
	}
	
	private void doUpload(UploadCache cache, UploadResult ret) throws Exception {
		MultipartLoader loader = new MultipartLoader(
				ret.getInfo(), ret.getResult(), log);
		boolean uploading = true;
		int comprehensiveStatus = UploadResult.STATUS_PENDING;
		try{
			while(uploading){
				Builder builder = new MultipartBody.Builder()
						.setType(MultipartBody.FORM);
				uploading = loader.load(builder);
				String jsonStr = upload(builder.build());
				if(LOGGING){
					log.debug("response json: "+jsonStr);
				}
				UploadResult result =
						loader.responseFilter(jsonStr);
				if(cache!=null){
					cache.refresh();
				}
				if(result!=null){
					comprehensiveStatus = result
							.getStatus(comprehensiveStatus);
					if(!uploading){
						// 如果没有继续上传的需要，则综合状态为最终状态。
						ret.setStatus(comprehensiveStatus);
					}
					if(result.isSuccess()){
						ret.freshResult(result);
					}
					if(ret.isSuccess()){
						ret.setMessage("ok");
						refreshFileUrl(ret);
					}else{
						ret.setMessage("error");
						ret.setDetail(result.getDetail());
						uploading = false;
					}
				}
			}
		}finally {
			loader.close();
		}
	}
	
	private boolean checkCallback(UploadCache cache){
		if(cache.getCallback()!=null){
			UploadResult ret = cache.getResult();
			try{
				if(ret.isSuccess()){
					cache.getCallback().onSuccess(ret);
				}else{
					cache.getCallback().onFailed(ret);
				}
			}catch(Throwable th){
				log.error(th.getMessage(), th);
			}
			return true;
		}
		return false;
	}
	
	private void checkRunning() throws IOException{
		if(System.currentTimeMillis()-idleTime
				> UploadConfig.TimeoutMillis()*2){
			service.startOrStop(false);
		}
	}
	
	private String upload(RequestBody reqBody)
			throws Exception{
//		OkHttpClient client = new OkHttpClient();
		OkHttpClient client = new OkHttpClient.Builder()
		        .connectTimeout(100, TimeUnit.SECONDS)
		        .writeTimeout(100, TimeUnit.SECONDS)
		        .readTimeout(600, TimeUnit.SECONDS)
		        .build();
		Request request = new Request.Builder()
        		.url(UploadConfig.Api())
        		.addHeader("Uni-Source", UploadConfig.UniSource())
                .post(reqBody).build();
        Response response = client.newCall(request).execute();
        String resp = null;
        if(response.body()!=null){
        	resp = new String(response.body().bytes(), "UTF-8");
        }
        if (!response.isSuccessful()) {
            log.error("Upload error, ",
            		new IOException("Unexpected code "+response));
            log.info(resp);
            return null;
        }
//        if(LOGGING){
//        	log.debug("response: "+respCode+"; "+resp);
//        }
        return resp;
	}
	
	private void refreshFileUrl(UploadResult result){
		if(result==null || result.getResult()==null){
			return;
		}
		for(UploadFile file: result.getResult()){
			file.setFileUrl(
					MultipartLoader.createFileKey(
							result.getInfo(), file.getFileName()));
		}
	}
}
