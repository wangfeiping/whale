package whale.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;

import com.alibaba.fastjson.JSON;

import whale.UploadConfig;
import whale.UploadLogger;
import whale.obj.UploadChunk;
import whale.obj.UploadChunkMeta;
import whale.obj.UploadFile;

public class FileLoader {

	public static final boolean LOGGING = UploadConfig.LOGGING_DEFAULT;
	private UploadLogger log;
	
	private UploadFile file;
	
	private int index; // 当前文件在上传调用UploadFile[] 的索引值
	private FileInputStream reader;
	private FileChannel channel;
	private int allocate;
	private long size;
	private long chunkNum;    // 以缓存计算，需要读取的次数减1
	private int mod;          // 最后需要读取的缓存
	private int chunkOffset; // 读取次数记录
	private MappedByteBuffer mapper;
	private String contentType;
	private boolean hasMore = true;
	private boolean isClosed = false;
	private UploadChunkMeta meta; // 超大文件分割上传时的元数据封装类
	
	public FileLoader(UploadFile file, int index,
			UploadLogger logger)
					throws FileNotFoundException, Exception{
		this.file = file;
		this.index = index;
		log = logger;
		
		if(contentType==null){
			contentType = checkContentType(file);
		}
		if(file.getBytes()!=null){
			size = file.getBytes().length;
		}else{
			File f = new File(file.getReadPath());
			if(file.getFileName()==null){
				file.setFileName(f.getName());
			}
		    reader = new FileInputStream(f);
		    channel = reader.getChannel();
		    size = channel.size();
		    mapper = channel.map(MapMode.READ_ONLY, 0, size);
		}
	    if(isLargeFile()){
	    	allocate = UploadConfig.MaxBytesSize();
	    	chunkNum = size / allocate;
	        mod = (int)(size % allocate);
	        meta = new UploadChunkMeta();
			meta.setName(getFile().getFileName());
			meta.setMime(getContentType());
			meta.createChunks(chunkNum, mod);
			if(LOGGING){
				log.debug("size: "+size+"; mod: "+mod
						+"; chunks: "+meta.getChunks().length);
			}
	    }else{
	    	allocate = (int)size;
	    	chunkNum = 0;
	        mod = 0;
	    }
	}
	
	public UploadChunkMeta getUploadChunkMeta(){
		return meta;
	}

	private String checkContentType(UploadFile file) {
		String contentType = "application/octet-stream";
		String name = file.getFileName();
		if(name!=null){
			name = name.substring(name.lastIndexOf('.')+1);
		}
		if("jpg".equalsIgnoreCase(name)
				|| "jpeg".equalsIgnoreCase(name)){
			contentType = "image/jpeg";
		}else if("png".equalsIgnoreCase(name)){
			contentType = "image/png";
		}else if("xlsx".equalsIgnoreCase(name)){
			contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		}else if("xls".equalsIgnoreCase(name)){
			contentType = "application/vnd.ms-excel";
		}else if("csv".equalsIgnoreCase(name)){
			contentType = "text/csv";
		}else if("jar".equalsIgnoreCase(name)){
			contentType = "application/java-archive";
		}else if("apk".equalsIgnoreCase(name)){
			contentType = "application/vnd.android.package-archive";
		}
		if(LOGGING){
			log.debug(file.getFileName()+" "+contentType);
		}
		return contentType;
	}
	
	public UploadFile getFile(){
		return file;
	}
	
	public int getIndex(){
		return index;
	}
	
	public long getFileSize(){
		return size;
	}
	
	/**
	 * 如果文件大小超过配置大小则认为是"大"文件，需要分割上传。
	 * @return
	 */
	public boolean isLargeFile(){
		return size>UploadConfig.MaxBytesSize();
	}
	
	public void close()
			throws Exception {
		if (channel != null) {
    		channel.close();
    	}
		if (reader != null) {
			reader.close();
		}
		isClosed = true;
	}
	
	public boolean isClosed(){
		return isClosed;
	}
	
	public byte[] load()
			throws UnsupportedEncodingException{
		byte[] tmp = null;
		if(LOGGING){
			log.debug("chunkOffset: "+chunkOffset
					+"; chunkNum: "+chunkNum);
		}
		if(chunkNum<=0){
			if(file.getBytes()!=null){
				return file.getBytes();
			}
    		tmp = new byte[allocate];
    		hasMore = false;
    	}else if (chunkOffset>=chunkNum){
    		if(mod>0){
    			if(file.getBytes()==null){
    				tmp = new byte[mod];
    			}else{
    				// 读取byte数组
    				tmp = Arrays.copyOfRange(file.getBytes(),
    						file.getBytes().length-mod,
    						file.getBytes().length);
    			}
    			createChunk(mod);
    			chunkOffset++;
    			meta.countSize(mod);
    			mod = 0;
    			hasMore = true;
    		}else{
    			// 最后上传大文件分割元数据UploadChunkMeta
    			String metaJson = JSON.toJSONString(meta);
    			if(LOGGING){
    				log.debug("chunks meta: "+metaJson);
    			}
    			tmp = metaJson.getBytes("UTF-8");
    			hasMore = false;
    			return tmp;
    		}
    	}else{
    		if(file.getBytes()==null){
    			tmp = new byte[allocate];
    		}else{
    			// 读取byte数组
    			int from = chunkOffset*allocate;
    			tmp = Arrays.copyOfRange(file.getBytes(),
						from, from+allocate);
    		}
    		createChunk(allocate);
    		
    		chunkOffset++;
    		meta.countSize(allocate);
    		hasMore = true;
    	}
		if(LOGGING){
			log.debug("tmp: "+tmp.length);
		}
		if(file.getBytes()==null){
			// 读取文件
			mapper.get(tmp);
		}
		return tmp;
	}

	private void createChunk(int chunkSize) {
		UploadChunk c = new UploadChunk();
		c.setOffset(chunkOffset*allocate);
		c.setSize(chunkSize<allocate ? chunkSize:allocate);
		meta.getChunks()[chunkOffset] = c;
	}
	
	public boolean hasMore(){
		return hasMore;
	}
	
	public String getContentType(){
		return contentType;
	}
	
	public long getCurrentChunkNum(){
		return chunkOffset;
	}
	
	public void responseFid(String fid){
		UploadChunk[] chunks = meta.getChunks();
		for(int i=0,j=chunks.length; i<j; i++){
			UploadChunk chunk = chunks[i];
			if(chunk!=null && chunk.getFid()==null){
				chunk.setFid(fid);
			}
		}
	}
}
