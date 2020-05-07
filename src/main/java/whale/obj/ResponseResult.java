package whale.obj;

import lombok.Data;

@Data
public class ResponseResult {

	private int status;
	private String message;
	private String detail;
	private ResultFile[] result;
}
