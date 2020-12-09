package whale;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Random;

import whale.obj.UploadFile;
import whale.obj.UploadInfo;
import whale.obj.UploadResult;

public class TestUpload {
	
	private static UploadLoggerTestUtil log =
			new UploadLoggerTestUtil();
	
	private static String path = "qianbaolife/";

	private static String[] files = {
//		"/home/wang/Downloads/images/saturn.jpg",
		"./docs/902633105.jpg",
//		"D:/workspace/seaweedfs/test/t02.jpg",
//		"D:/workspace/seaweedfs/test/t03.xlsx",
//		"D:/workspace/seaweedfs/test/h01.zip"
//		"D:/workspace/seaweedfs/test/h02.zip",
//		"D:/workspace/seaweedfs/test/h03.zip",
//		"D:/workspace/seaweedfs/test/h04.zip",
//		"D:/workspace/seaweedfs/test/h05.zip",
//		"D:/workspace/seaweedfs/test/h06.zip",
//		"D:/workspace/seaweedfs/test/h07.zip",
//		"D:/workspace/seaweedfs/test/h08.zip",
//		"D:/workspace/seaweedfs/test/h09.zip",
//		"D:/workspace/seaweedfs/test/1498556609870395.jpg",
//		"D:/workspace/seaweedfs/test/aliyun_java_sdk_20171127.zip",
//		"D:/workspace/seaweedfs/test/koynare-0.1.1-SNAPSHOT.zip",
//		"D:/workspace/seaweedfs/test/sdk.zip"
	};
	
	public static void main(String[] args){
//		final UploadService srv = new UploadService(log);
		UploadConfig conf = new UploadConfig();
		conf.setUniSource("WhaleTesting");
		conf.setApi("http://apis.qianbao.com/basicservice/v1/intranet/filer");
//		conf.setApi("http://dev-seaweed.qianbao-inc.com/filer");
//		conf.setApi("http://seaweed.qianbao-inc.com/filer");
//		conf.setApi("http://192.168.1.182:9330/filer");
//		conf.setApi("http://192.168.1.182:9430/cdn/apk.qianbaoyidai.com");
//		conf.setApi("http://sit-apis.qianbao.com/basicservice/v1/intranet/cdn/apk.borrowfund.com");
//		conf.setApi("http://test-img7.qianbao.com/filer");
//		conf.setApi("https://apis.qianbao.com/basicservice/v1/filer");
//		conf.setApi("http://sit-apis.qianbao.com/basicservice/v1/intranet/filer");
//		conf.setApi("https://apis.qianbao.com/basicservice/v1/intranet/filer");
		conf.setSync(true);
//		conf.setSync(false);
		conf.setRootFolder("/public/test/");
		conf.setMaxBytesSize(6 * 1024 * 1024);
		final UploadService srv = new UploadService(conf, log);

//		Thread t = new Thread(){
//			public void run(){
//				test(srv);
//			}
//		};
//		t.start();
//		Thread t1 = new Thread(){
//			public void run(){
//				test1(srv);
//			}
//		};
//		t1.start();
		Thread t2 = new Thread(){
			public void run(){
				test2(srv);
			}
		};
		t2.start();
//		Thread t3 = new Thread(){
//			public void run(){
//				test3(srv);
//			}
//		};
//		t3.start();
//		Thread t4 = new Thread(){
//			public void run(){
//				try{
//					test4(srv);
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//			}
//		};
//		t4.start();
	}
	
	public static void test4(UploadService srv) throws Exception{
		log.debug("test4 running......uploading file bytes......");
		
		UploadFile f = new UploadFile();
		UploadInfo info = new UploadInfo();
		info.setPath(path);
		
		String file = "D:/workspace/seaweedfs/test/"
				+ "koynare-0.1.1-SNAPSHOT.zip";
//		String file = "D:/ttt.jpg";
		byte[] bytes = new byte[1000];
        FileInputStream inputStream = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = inputStream.read(bytes);
        while(read>=0){
        	baos.write(bytes, 0, read);
        	read = inputStream.read(bytes);
        }
        inputStream.close();
//        f.setFileName("test.jpg");
        f.setFileName("koynare-0.1.1.zip");
        f.setBytes(baos.toByteArray());
		
		try{
			long start = System.currentTimeMillis();
			UploadResult ret = srv.upload(info, f);
			ret = syncCheck(srv, info, ret);
			log("test4", start, ret);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void test3(UploadService srv) {
		log.debug("test3 running......uploading large file......");
		
		UploadFile f = new UploadFile();
//		f.setReadPath("D:/ttt.jpg");
		f.setReadPath("D:/workspace/seaweedfs/test/"
				+ "whale-0.0.0-SNAPSHOT.jar");
		UploadInfo info = new UploadInfo();
		info.setPath(path);
		
		try{
			long start = System.currentTimeMillis();
			UploadResult ret = srv.upload(info, f);
			ret = syncCheck(srv, info, ret);
			log("test3", start, ret);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static UploadResult syncCheck(
			UploadService srv, UploadInfo info, UploadResult ret) {
		if(UploadConfig.Sync()){
			// 如果配置是同步，则说明返回的结果无需进一步检查，直接返回。
			return ret;
		}
		try {
		    boolean finished = false;
	        while(!finished){
	            ret = srv.checkResult(info);
	            finished = ret.isFinished();
	            
	            Thread.sleep(100);
	        }
		}catch(InterruptedException e) {
            log.error(e.getMessage(), e);
        }
		return ret;
	}
	
	public static void test(UploadService srv) {
		log.debug("test running......");
		
		UploadFile f = new UploadFile();
//		f.setReadPath("D:/echo.png");
//		f.setReadPath("D:/workspace/code/github/"
//				+ "wangfeiping/oven/doc/blockchain/imgs/"
//				+ "mesos-tow-level-scheduling.jpg");
//		f.setReadPath("D:/test.zip");
//		f.setReadPath("D:/workspace/seaweedfs/test/whale-0.0.0-SNAPSHOT.jar");
//		f.setReadPath("D:/workspace/logservice/release/largeTestFile.zip");
		f.setReadPath("D:/backup/x.zip");
		UploadInfo info = new UploadInfo();
//		info.setPath(path);
		info.setPath("x.zip");
		
		try{
			long start = System.currentTimeMillis();
			UploadResult ret = srv.upload(info, f);
			ret = syncCheck(srv, info, ret);
			log("test", start, ret);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void test2(UploadService srv) {
		log.debug("test2 running......");
		
		log.debug("files.length: "+files.length);
		UploadFile[] ups = new UploadFile[files.length];
		for(int i=0,j=files.length; i<j; i++){
			UploadFile f = new UploadFile();
			f.setReadPath(files[i]);
			ups[i] = f;
		}
		
		UploadFile tmp = null;
		for(int i=0,j=files.length; i<j; i++){
			int r = new Random().nextInt(files.length);
			int l = new Random().nextInt(files.length);
			tmp = ups[r];
			ups[r] = ups[l];
			ups[l] = tmp;
		}
		
		log.debug("ups.length: "+ups.length);
		for(int i=0,j=ups.length; i<j; i++){
			log.debug("random: "+ups[i].getFileName()+"; "
					+ups[i].getReadPath());
		}
		
		UploadInfo info = new UploadInfo();
		info.setPath(path);
		
		try{
			long start = System.currentTimeMillis();
			UploadResult ret = srv.upload(info, ups);
//			String fid = ret.getResult()[0].getFid();
//			log.debug("ret fid: "+fid);
			ret = syncCheck(srv, info, ret);
			log("test2", start, ret);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void test1(UploadService srv) {
		log.debug("test1 running......");
		
		UploadFile f = new UploadFile();
		f.setReadPath("D:/workspace/seaweedfs/test/test.xls");
		UploadInfo info = new UploadInfo();
		info.setPath(path);
		
		try{
			srv.upload(info, f);
			System.out.println("uploaded.");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void log(String name, long start, UploadResult ret) {
		log.debug(name+" upload result: cost millis "
				+(System.currentTimeMillis()-start)
				+"; "+ret.getStatus());
		UploadFile[] fs = ret.getResult();
		for(UploadFile f: fs){
			log.debug("uploaded: "+f.getFid()+" "+f.getFileUrl());
		}
	}
}
