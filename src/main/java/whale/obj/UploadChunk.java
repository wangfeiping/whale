package whale.obj;

import lombok.Data;

@Data
public class UploadChunk {

	private String fid;
	private int offset;
	private int size;
}
