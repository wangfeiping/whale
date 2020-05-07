package whale;

import java.io.File;
import java.nio.file.Path;

public class TestFile {

	public static void main(String[] args){
		String logFile = "/apps/logs/.log";
		test(logFile);
		logFile = "/apps/logs/app/app.log";
		test(logFile);
		logFile = "/apps/logs/.app/.app.log";
		test(logFile);
	}

	private static void test(String logFile){
		try{
			File f = new File(logFile);
			System.out.println("canonical="+f.getCanonicalPath());
			System.out.println("name="+f.getName());
			System.out.println("parent="+f.getParent());
			Path p = f.toPath();
			System.out.println("path name count="+p.getNameCount());
			System.out.println("path name 0="+p.getName(0));
			System.out.println("path name -1="
					+p.getName(p.getNameCount()-1));
			System.out.println("path name -2="
					+p.getName(p.getNameCount()-2));
			
			long millis = System.currentTimeMillis();
			System.out.println("millis="+millis);
			System.out.println("      ="+(1527160274l*1000));
			System.out.println("      ="+(millis-1527160274l*1000));
			System.out.println("      ="+7*24*3600000l);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
