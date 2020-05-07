package whale;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestContentType{
    public static void main(String[] args){
//        Path path = Paths.get("D:/ttt.jpg");
//        Path path = Paths.get("D:/workspace/seaweedfs/20170424.xls");
//    	Path path = Paths.get("D:/workspace/seaweedfs/test/test.xlsx");
//    	Path path = Paths.get("ttt123.zip");
    	Path path = Paths.get("D:/workspace/seaweedfs/test/h01.zip");
        String contentType = null;  
        try {  
            contentType = Files.probeContentType(path);
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        System.out.println("File content type is : " + contentType);   
    }
}