package whale.obj;

import lombok.Data;

@Data
public class UploadResult {

	public static int STATUS_PENDING = 100;
	public static int STATUS_PROCESSING = 102;
	
	private String message = "pending";
	private String detail;
	
	/**
	 * 借用http 状态码
	 * 100 Continue   - pending
	 * 102 Processing - processing
	 */
	private int status = STATUS_PENDING;
	private UploadFile[] result;
	
	private UploadInfo info;
	
	public UploadResult(){}
	
	public UploadResult(UploadInfo info, UploadFile[] files){
		this.info = info;
		result = files;
	}
	
	/**
	 * 由于一个调用可能拆分为多次请求，因此需要先后两个状态进行综合判断。
	 * 如果有一个状态是失败，则综合状态为失败；
	 * 否则以当前状态（status）为准；
	 * 即：如果参数状态（pStatus）为失败则综合状态为失败，否则以当前（status）状态为准。
	 * @param pStatus 参数状态
	 * @return
	 */
	public int getStatus(int pStatus){
		if(pStatus>=300){
			return pStatus;
		}
		return status;
	}
	
	public boolean isSuccess(){
		if(status>=200 && status<300){
			return true;
		}
		return false;
	}
	
	public boolean isFinished(){
		if(status>=200){
			return true;
		}
		return false;
	}
	
	/**
	 * 处理中
	 */
	public boolean isProcessing(){
		if(status>100 && status<200){
			return true;
		}
		return false;
	}
	
	/**
	 * 待处理状态，刚刚初始化，还未处理。
	 * @return
	 */
	public boolean isPending(){
		return STATUS_PENDING==status;
	}
	
	/**
	 * 更新对应文件的上传结果
	 * @param result
	 */
	public void freshResult(UploadResult otherResult){
		UploadFile[] files = otherResult.getResult();
		if(files==null){
			return;
		}
		for(int i=0,j=files.length; i<j; i++){
			UploadFile f = files[i];
			freshUploadFile(f);
		}
	}
	
	private void freshUploadFile(UploadFile file){
		for(int i=0,j=result.length; i<j; i++){
			UploadFile local = result[i];
			if(local.getFileName().equals(file.getFileName())){
				local.setStatus(file.getStatus());
				return;
			}
		}
	}
}
