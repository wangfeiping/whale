package whale.util;

import java.util.Arrays;

import com.alibaba.fastjson.JSON;

import lombok.Data;
import okhttp3.MediaType;
import okhttp3.MultipartBody.Builder;
import okhttp3.RequestBody;
import whale.UploadConfig;
import whale.UploadLogger;
import whale.obj.ResponseResult;
import whale.obj.UploadChunkMeta;
import whale.obj.UploadFile;
import whale.obj.UploadInfo;
import whale.obj.UploadResult;

@Data
public class MultipartLoader {
	
	public static final boolean LOGGING = UploadConfig.LOGGING_DEFAULT;
	private UploadLogger log;
	
	private UploadInfo info;
	private UploadFile[] files;
	private FileLoader loader;
	// 一次请求上传文件的总数据量
	private int total;
	
	public MultipartLoader(UploadInfo info,
			UploadFile[] files, UploadLogger logger){
		this.info = info;
		this.files = Arrays.copyOf(files, files.length);
		log = logger;
	}
	
	/**
	 * 读取数据并装载到MultipartEntityBuilder 中，以便于准备上传
	 * 
	 * 参考：NIO 读取文件
	 * http://blog.csdn.net/gzu_imis/article/details/21109753
	 * http://blog.jobbole.com/104880/
	 * 
	 * @param builder 封装上传数据
	 * @return boolean 是否还有数据没有读取完
	 */
	public boolean load(Builder builder)
			throws Exception{
		boolean keepLoading = true;
		while(keepLoading){
			if(loader==null || loader.isClosed()){
				loader = createFileLoader();
			}
			if(loader==null){
				break;
			}
	    	if(loader.isLargeFile()){
	    		splittingUpload(builder);
	    		keepLoading = false;
	    	}else{
	    		keepLoading = upload(builder);
	    	}
		}
		// 该属性表示是否还有待上传数据需要后续请求处理(单次请求不能处理，
		// 如：本次上传数据量已达到上限)。
		// 该属性用于返回给函数调用方，用于判断是否继续调用本函数处理上传文件数据。
		total = 0;
		if(loader!=null){
			UploadFile f = loader.getFile();
			return loader.hasMore();
		}else if(getValidFileIndex()>0){
			return true;
		}
        return false;
	}
	
	private void splittingUpload(Builder builder)
					throws Exception {
		if(total>0){
			// 拆分上传都是上传大文件，所以如果total 大于0，说明已有文件准备上传。
			// 因此此次拆分需要跳过。
			return;
		}
		UploadChunkMeta meta = loader.getUploadChunkMeta();
		if(LOGGING){
			log.debug("splitting uploaded: "+meta.getName()+"; "+
					meta.getMime()+"; "+meta.getSize()+
					"/"+loader.getFileSize()+"; "
					+UploadConfig.MaxBytesSize());
		}
		byte[] tmp = loader.load();
		if(loader.hasMore()){
			builder.addFormDataPart(
					"upload_chunk_"+loader.getCurrentChunkNum(),
					createFileKey(info,
							loader.getFile().getFileName()
							+ "-" + loader.getCurrentChunkNum()),
					RequestBody.create(MediaType
							.parse("application/octet-stream"), tmp)
					);
		}else{
			// 上传文件分割元数据
			builder.addFormDataPart(
					"meta",
					info.getPath()+"meta",
					RequestBody.create(MediaType
							.parse("application/json"),
							"{\"action\":\"registerChunkMeta\"}"
							.getBytes("UTF-8")));
			builder.addFormDataPart(
					"upload_chunks_json",
					createFileKey(info,
							loader.getFile().getFileName()),
					RequestBody.create(MediaType
							.parse("application/json"), tmp));
			loader.close();
			loader = null;
		}
	}

	private boolean upload(Builder builder)
			throws Exception {
//    	HttpEntity entity = builder.build();
		long size = loader.getFileSize();
		if(size<0){
			loader.close();
			loader = null;
		}else if(total+size<UploadConfig.MaxBytesSize()){
			byte[] tmp = loader.load();
			builder.addFormDataPart(
					"upload_"+loader.getIndex(),
					createFileKey(info,
							"aa"),
					RequestBody.create(MediaType.parse(
							loader.getContentType()), tmp)
					);
			total += size;
			loader.close();
			loader = null;
		}else{
			return false;
		}
		return true;
	}
	
	public void close()
			throws Exception {
		if(loader!=null){
			loader.close();
			loader = null;
		}
	}
	
	public FileLoader createFileLoader() throws Exception{
		int index = getValidFileIndex();
		if(index<0){
			return null;
		}
		UploadFile file = files[index];
		loader = new FileLoader(file, index, log);
		files[index] = null;
		return loader;
	}

	private int getValidFileIndex() {
		for(int i=0,j=files.length; i<j; i++){
			UploadFile file = files[i];
			if(file!=null){
				return i;
			}
		}
		return -1;
	}
	
	public static String createFileKey(
			UploadInfo info, String filename) {
		String fileKey = info.getPath();
		fileKey = fileKey==null ? "":fileKey;
        if (UploadConfig.RootFolder() != null) {
        	fileKey = UploadFile.joinPath(UploadConfig.RootFolder(), fileKey);
        }
        fileKey = UploadFile.joinPath(fileKey, filename);
		return fileKey;
	}
	
	public UploadResult responseFilter(String jsonStr){
		UploadResult result = null;
		if(loader!=null && loader.isLargeFile()){
			if(loader.hasMore()){
				result = checkResponseResult(jsonStr);
			}else{
				result = checkUploadResult(jsonStr);
			}
		}else{
			result = checkUploadResult(jsonStr);
		}
		return result;
	}
	
	private UploadResult checkUploadResult(String jsonStr){
		UploadResult result = null;
		try{
			result = JSON.parseObject(jsonStr, UploadResult.class);
		}catch(Exception e){
			result = new UploadResult();
			result.setStatus(500);
			result.setMessage("error");
			log.error(e.getMessage(), e);
		}
		return result;
	}
	
	private UploadResult checkResponseResult(String jsonStr){
		UploadResult result = null;
		try{
			ResponseResult resp = JSON.parseObject(
					jsonStr, ResponseResult.class);
			String fid = resp.getResult()[0].getFid();
			log.debug("uploaded fid: "+fid);
			loader.responseFid(fid);
		}catch(Exception e){
			result = new UploadResult();
			result.setStatus(500);
			result.setMessage("error");
			log.error(e.getMessage(), e);
		}
		return result;
	}
}
