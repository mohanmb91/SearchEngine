package cs3.calstatela.edu.SearchEngine.Crawl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;


public class DownloadFile {
	
	public String downloadFile(String htmlContent){
		 UUID uuid = UUID.randomUUID();
		 String fileName = uuid + ".txt";
		 String dirName = "C:/SearchEngine/CrawledData";
		 
		 File dir = new File(dirName);
		 if(!dir.exists()){
			 dir.mkdir();
		 }
		 
		 File fos = new File(dir,fileName);
		 String absolutePath = fos.getAbsolutePath();
		 BufferedWriter output = null; 
		 try {
			output = new BufferedWriter(new FileWriter(fos));
			output.write(htmlContent);
			output.close();
		}
		 catch(FileNotFoundException e){
			 
		 }
		 catch (IOException e) {
			e.printStackTrace();
		}
		 //System.out.println("Finsihed");
		return absolutePath;
		
	} 

}
 

