package whale;

import whale.obj.UploadResult;

public interface IUploadCallback {

	public void onSuccess(UploadResult result);
	
	public void onFailed(UploadResult result);
}
