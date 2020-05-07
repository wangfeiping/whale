package whale;

import whale.obj.UploadFile;
import whale.obj.UploadInfo;
import whale.obj.UploadResult;

public class TestUploadSample {
	
	private static UploadLoggerTestUtil log =
			new UploadLoggerTestUtil();
	
	private static String path = "apk/202005071549/";
	private static String file = "C:/devtools/upload/apk/app-assistant-release.apk";
	
	public static void main(String[] args){
//		final UploadService srv = new UploadService(log);
		UploadConfig conf = new UploadConfig();
		conf.setSync(true);
		conf.setMaxBytesSize(12 * 1024 * 1024);
		final UploadService srv = new UploadService(conf, log);

		Thread t = new Thread(){
			public void run(){
				test(srv);
			}
		};
		t.start();
	}
	
	public static void test(UploadService srv) {
		log.debug("test running......");
		
		UploadFile f = new UploadFile();
		f.setReadPath(file);
		UploadInfo info = new UploadInfo();
		info.setPath(path);
		
		try{
			long start = System.currentTimeMillis();
			UploadResult ret = srv.upload(info, f);
			log(start, ret);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void log(long start, UploadResult ret) {
		log.debug("test upload result: cost millis "
				+(System.currentTimeMillis()-start)
				+"; "+ret.getStatus());
		UploadFile[] fs = ret.getResult();
		for(UploadFile f: fs){
			log.debug("uploaded "+f.getFileUrl());
		}
	}
}
