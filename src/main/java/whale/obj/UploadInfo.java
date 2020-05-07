package whale.obj;

import java.util.UUID;

import lombok.Data;

/**
 * 上传参数设置
 * @author wangfp
 *
 */
@Data
public class UploadInfo {

	private String key;  // 上传请求唯一标示，用于查询上传请求返回结果
	private String path; // 上传文件网络访问路径
	private String ttl;  // 上传文件有效期参数
	
	public UploadInfo(){
		key = UUID.randomUUID().toString();
	}
}
