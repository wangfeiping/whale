package whale;

import whale.UploadLogger;

public class UploadLoggerTestUtil extends UploadLogger{
	
	public void debug(String msg){
		System.out.println("DEBU: "+msg);
	}
	
	public void info(String msg){
		System.out.println("INFO: "+msg);
	}
	
	public void error(String msg, Throwable th){
		System.out.println("ERRO: "+msg);
		th.printStackTrace();
	}
	
	public void fatal(String msg, Throwable th){
		System.out.println("FATA: "+msg);
		th.printStackTrace();
	}
}
