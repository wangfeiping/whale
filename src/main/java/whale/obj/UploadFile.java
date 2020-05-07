package whale.obj;

import lombok.Data;

/**
 * 上传文件本地属性，及上传返回参数
 * @author wangfp
 *
 */
@Data
public class UploadFile {
	
	// 上传文件本地读取路径
	private String readPath;
	// 上传文件数组数据，用于支持已读入缓存的文件数据上传
	private byte[] bytes;
	// 上传成功时返回的文件名称，如果上传前设置上传文件时将强制按照设置字符串命名上传文件
	private String fileName;
	
	private int status;        // 上传百分比，100为成功，其他为还未成功。
	private String fileUrl;    // 上传成功时返回的文件url
	private int size;          // 上传成功时返回的文件大小
	
	public boolean isSuccess(){
		if(status==100){
			return true;
		}
		return false;
	}
	
	final public static String joinPath(String root, String path){
		if(root==null){
			root = "";
		}
		if(path==null){
			path = "";
		}
		if(!root.endsWith("/")){
			root += "/";
		}
		if(path.startsWith("/")){
			path = path.substring(1);
		}
		return root + path;
	}
}
