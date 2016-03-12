package cs3.calstatela.edu.SearchEngine.Crawl;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import cs3.calstatela.edu.SearchEngine.Dal.MongoDal;
import cs3.calstatela.edu.SearchEngine.Model.keyValue;


public class Crawler {
	static int depth = 1;
	static MongoDal dal = new MongoDal();
	
	public void setStartUrl(String startUrl){
		DBCollection urlTable =  dal.getTableFromDatabase("searchengine", "urltable");
		DBCursor cursor = dal.findInTableAsPerQuery(urlTable, new keyValue("DNS",startUrl));
		if(! cursor.hasNext()) {
			List<keyValue> keyvalues = new ArrayList<keyValue>();
			keyvalues.add(new keyValue("DNS",startUrl));
			keyvalues.add(new keyValue("visited", "false"));
			keyvalues.add(new keyValue("path", null));
			keyvalues.add(new keyValue("title", getTitleOfUrl(startUrl)));
			keyvalues.add(new keyValue("metadata", null));
			keyvalues.add(new keyValue("outgoinglinks", null));
			keyvalues.add(new keyValue("releventdata", null));
			keyvalues.add(new keyValue("imagedetails", null));
			dal.insertIntoTable(urlTable, keyvalues);
			saveURLAsFile(startUrl);
		}
	}
	
	public void Crawl(int count){
		keyValue query = new keyValue("visited", "false");
		DB db = dal.getDatabase("searchengine");
		DBCollection urlTable = db.getCollection("urltable");
		DBCursor cursor = dal.findInTableAsPerQuery(urlTable, query);
		//unvisited URL for each loop
		while (cursor.hasNext()) {
			
			String url = cursor.next().get("DNS").toString();
			System.out.println(url);
			//setting the visited value as true
			setVisitedTrue(url,urlTable);
			
			//get the possible url form the page
			List<String> allPossibleUrls =  getAllUrlFromRootUrl(url);
			
			updateOutgoingLink(url,urlTable,allPossibleUrls);
			//push to mongoDB 
			pushUrlToMongo(allPossibleUrls,urlTable);
			
		}
		//exit condition
		if(count <= depth){
			Crawl(count +1);
		}	
	}
	
	private void updateOutgoingLink(String url, DBCollection urlTable, List<String> allPossibleUrls) {
		JSONObject object=new JSONObject();		
		object.put("listofoutgoinglinks", allPossibleUrls);
		BasicDBObject queryByUrl = new BasicDBObject();
		queryByUrl.put("DNS", url);
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("outgoinglinks", object.toString());	
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);
		urlTable.update(queryByUrl, updateObj);	
		
	}

	public void updatePathValue(String url, DBCollection urlTable,String path){
		BasicDBObject queryByUrl = new BasicDBObject();
		queryByUrl.put("DNS", url);
		//System.out.println("visited " +url);
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("path", path);
				
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);
		
		urlTable.update(queryByUrl, updateObj);	
	}
	
	public void saveURLAsFile(String url){
		DownloadFile saveFile = new DownloadFile();
		DBCollection urlTable = dal.getTableFromDatabase("searchengine", "urltable");
		DBCursor cursor = dal.findInTableAsPerQuery(urlTable, new keyValue("DNS", url));
		String content = "";
		String path = "";
		if(cursor.hasNext()){
			try {
				content = Jsoup.connect(url).validateTLSCertificates(false).ignoreContentType(true).ignoreHttpErrors(true).timeout(10*100000).get().html();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		path = saveFile.downloadFile(content);
		updatePathValue(url,urlTable,path);
	}
	
	public boolean titleExist(String title,DBCollection table){
		table = dal.getTableFromDatabase("searchengine", "urltable");
		DBCursor cursor = dal.findInTableAsPerQuery(table, new keyValue("title", title));
		while (cursor.hasNext()) {
			return true;
		}
		return false;
	}
	
	public void pushUrlToMongo(List<String> allPossibleUrls,DBCollection urlTable ){
		//controlling width of the tree by 2
		int width_control = 2;
		int count = 0;
		String urlTitle = "";
		for (String eachUrl : allPossibleUrls) {
			if(count <= width_control){
				if(urlExist(eachUrl,urlTable) == false){
					urlTitle =getTitleOfUrl(eachUrl);
					if(!titleExist(urlTitle,urlTable)){
						List<keyValue> keyvalues = new ArrayList<keyValue>();
						keyvalues.add(new keyValue("DNS",eachUrl));
						keyvalues.add(new keyValue("visited", "false"));
						keyvalues.add(new keyValue("path", "C:\\CrawledData"));
						keyvalues.add(new keyValue("title", getTitleOfUrl(eachUrl)));
						keyvalues.add(new keyValue("metadata", null));
						keyvalues.add(new keyValue("outgoinglinks", null));
						keyvalues.add(new keyValue("releventdata", null));
						keyvalues.add(new keyValue("imagedetails", null));
						dal.insertIntoTable(urlTable, keyvalues);
						count = count + 1;
						//download the content of URL
						saveURLAsFile(eachUrl);
					}
				}
			}
			else{
				break;
			}
		}
	}
	public boolean urlExist(String url,DBCollection urlTable) {
		urlTable = dal.getTableFromDatabase("searchengine", "urltable");
		DBCursor cursor =  dal.findInTableAsPerQuery(urlTable, new keyValue("DNS",url));
		if(cursor.hasNext()){
			return true;	
		}
		return false;
	}
	public void setVisitedTrue(String url, DBCollection urlTable){
		BasicDBObject queryByUrl = new BasicDBObject();
		queryByUrl.put("DNS", url);
		//System.out.println("visited " +url);
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("visited", "true");
				
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);
		urlTable.update(queryByUrl, updateObj);
		
	}
	public String getTitleOfUrl(String url){
		Document doc = null;
		try {
			doc = Jsoup.connect(url).ignoreContentType(true).validateTLSCertificates(false).ignoreHttpErrors(true).timeout(10*100000).get();
			String title = doc.title().trim();
			title = title.replaceAll("[^a-zA-Z]+","");
			title = title.replaceAll(" ","");
			return title;
		}catch (UnknownHostException e) {
		    System.err.println("Unknown host");
		    e.printStackTrace(); // I'd rather (re)throw it though.
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	public List<String> getAllUrlFromRootUrl(String url){
		Document doc = null;
		try {
			doc = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).timeout(10*100000).get();
		}
		catch (UnknownHostException e) {
		    System.err.println("Unknown host");
		    e.printStackTrace(); // I'd rather (re)throw it though.
		}
		catch (SocketTimeoutException e) {
		    System.err.println("IP cannot be reached");
		    e.printStackTrace(); // I'd rather (re)throw it though.
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		Elements elements = doc.select("a");
		//String title = doc.title();
		
		//System.out.println(title);
		List<String> filteredElements = new ArrayList<String>();
		LinkedHashSet<String> outGoingLinksSet =  new LinkedHashSet<String>();
		ProperURL obj = new ProperURL();
		for (Element element : elements) {
			String eachOutgoingURL = element.absUrl("href");
			System.out.println(eachOutgoingURL);
			URL aURL;
			try {
				if((eachOutgoingURL != null) && (eachOutgoingURL != "")){
					aURL = new URL(eachOutgoingURL);
					if(("http".equals(aURL.getProtocol())) || ("https".equals(aURL.getProtocol()))){
						outGoingLinksSet.add(obj.removeUrl(element.absUrl("href")));
					}
				}	
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
		}
		for (String eachSetElement : outGoingLinksSet) {
			filteredElements.add(eachSetElement);
		}
		
		
		return filteredElements;
	}
	
	public static void main(String[] args) {
//		dal.dropDatabase("searchengine");
//		if((args[0] != null || args[0] != "")&&(args[1] != null || args[1] != "")){
//			depth = Integer.parseInt(args[0]);
//			Crawler crawl = new Crawler();
//			String startUrl = args[1];
//			crawl.setStartUrl(startUrl);	
//			crawl.Crawl(1);
//			}
//			if(args.length == 3 && args[2].equals("-e")){
//				Extractor extractorObj = new Extractor();
//				extractorObj.fetchUrlDetails();
//			}
		
		
		System.out.println("started crawling");
		dal.dropDatabase("searchengine");
		
			depth = 1;
			Crawler crawl = new Crawler();
			String startUrl = "http://www.calstatela.edu/";
			crawl.setStartUrl(startUrl);	
			crawl.Crawl(1);
			
				Extractor extractorObj = new Extractor();
				extractorObj.fetchUrlDetails();
			
	}
}