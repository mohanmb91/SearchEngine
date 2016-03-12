
package cs3.calstatela.edu.SearchEngine.Crawl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import cs3.calstatela.edu.SearchEngine.Dal.MongoDal;
import cs3.calstatela.edu.SearchEngine.Model.ImageModel;
import cs3.calstatela.edu.SearchEngine.Model.keyValue;

public class Extractor {


	static MongoDal dal = new MongoDal();

	//to fetch all the documents in the mongoDB
	public void fetchUrlDetails(){
		DBCollection urlTable =  dal.getTableFromDatabase("searchengine", "urltable");
		DBCursor cursor = dal.getAllFromTable(urlTable);
		while (cursor.hasNext()) {
			//fetching the URL and Path
			DBObject tobj = cursor.next();  
			String url = (String)tobj.get("DNS");
			String path = (String)tobj.get("path");
			String titleFromTable = (String)tobj.get("title");
			String titleItems[] = titleFromTable.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");

			String title = " ";
			for (String eachString : titleItems) {
				//System.out.println(eachString);
				title = title.concat(eachString).concat(" ");
			}
			title = title.toLowerCase();
			System.out.println(title);
			if((path != null) && (url != null)){
				//to extract the text content from the downloaded file
				getMetaData(url,path,title,urlTable);
			}

		}

	}

	//to extract the text content from the downloaded file
	private void getMetaData(String url,String path,String title,DBCollection urlTable){
		
		//detecting the file type
		BodyContentHandler handler = new BodyContentHandler(100000000);
		Metadata metadata = new Metadata();
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(new File(path));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		ParseContext pcontext = new ParseContext();

		//Html parser 
		HtmlParser htmlparser = new HtmlParser();
		try {
			htmlparser.parse(fileInputStream, handler, metadata,pcontext);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (TikaException e) {
			e.printStackTrace();
		}
		
		//creating a list for the set of meta data
		List<keyValue> metadataObj = new ArrayList<keyValue>();
		String[] metadataNames = metadata.names();
		for(String name : metadataNames) {
			metadataObj.add(new keyValue(name,metadata.get(name))); 
		}
		
		String textContent = handler.toString();
		textContent = title.concat(" ").concat(textContent);
		ContentProcessing objContentProcessing = new ContentProcessing();
		Map<String, ArrayList<Integer>> wordWithPositionMap;
		wordWithPositionMap = objContentProcessing.getWordWithPosition(textContent);
		
		//java program to get the image details
		FetchImage imageDetailsObj = new FetchImage();
		List<ImageModel> imageDetails = imageDetailsObj.fetchImageDetails(url);
		
		//function to update the metadata and releventdata field in URLTable
		updateURLtable(url,metadataObj,"metadata",urlTable);
		updateURLtableForReleventData(url,wordWithPositionMap,"releventdata",urlTable);
		updateURLtableforImage(url,imageDetails,"imagedetails",urlTable);
	

	}
	
	private void updateURLtableForReleventData(String url, Map<String, ArrayList<Integer>> wordWithPositionMap,
			String key, DBCollection urlTable) {
		
		JSONObject object=new JSONObject();		
		Set<Entry<String, ArrayList<Integer>>> wordWithPositionSet = wordWithPositionMap.entrySet();
		for (Entry<String, ArrayList<Integer>> entry : wordWithPositionSet) {
			object.put(entry.getKey(),entry.getValue());
		}
		DBObject dbObject = (DBObject) JSON.parse(object.toString());
		
		BasicDBObject queryByUrl = new BasicDBObject();
		queryByUrl.put("DNS", url);
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put(key, dbObject.toString());	
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);
		urlTable.update(queryByUrl, updateObj);	
		
	}

	private void updateURLtableforImage(String url, List<ImageModel> imageDetails, String key, DBCollection urlTable) {
		JSONObject object=new JSONObject();		
		object.put("listimagedetails", imageDetails);
		BasicDBObject queryByUrl = new BasicDBObject();
		queryByUrl.put("DNS", url);
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put(key, object.toString());	
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);
		urlTable.update(queryByUrl, updateObj);	
		
	}

	//Update the URLTable
	public void updateURLtable(String url,List<keyValue> obj,String key,DBCollection urlTable){
		
		JSONObject object=new JSONObject();
		for (keyValue keyValue : obj) {
			object.put(keyValue.key,keyValue.value);
		}
		
        DBObject dbObject = (DBObject) JSON.parse(object.toString());
		BasicDBObject queryByUrl = new BasicDBObject();
		queryByUrl.put("DNS", url);
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put(key, dbObject.toString());	
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);
		urlTable.update(queryByUrl, updateObj);	

	}

}

