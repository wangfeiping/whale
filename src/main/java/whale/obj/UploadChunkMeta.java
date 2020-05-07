package whale.obj;

import lombok.Data;

@Data
public class UploadChunkMeta {

	private String name;
	private String mime;
	private long size;
	private UploadChunk[] chunks;
	
	public void createChunks(long chunkNum, int mod) {
		if(mod>0){
			chunkNum++;
		}
		chunks = new UploadChunk[(int)chunkNum];
	}
	
	public void countSize(int more){
		size += more;
	}
}
