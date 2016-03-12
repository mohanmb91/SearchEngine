package cs3.calstatela.edu.SearchEngine.LinkAnalysis;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.json.JSONObject;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import cs3.calstatela.edu.SearchEngine.Crawl.ProperURL;
import cs3.calstatela.edu.SearchEngine.Dal.MongoDal;
import cs3.calstatela.edu.SearchEngine.JSON.JsonConvertor.JsonConvertor;
import cs3.calstatela.edu.SearchEngine.Model.keyValue;

public class LinkAnalysis {
	
	static MongoDal dal = new MongoDal();
	static LinkAnalysis analysis = new LinkAnalysis();
	static DBCollection linkAnalysisTable =  dal.getTableFromDatabase("searchengine", "linkAnalysis");
	
	
	public void performLinkAnalysis() throws IOException{
		setLinkAnalysisTable();
		updateLinkAnalysisTabel();
		ranking();
	}
	
	private void ranking() {
		setInitialRank();
		iterateRanking();
	}

	private void iterateRanking() {
		DBCollection linkAnalysisTable =  dal.getTableFromDatabase("searchengine", "linkAnalysis");
		DBCursor rows = dal.getAllFromTable(linkAnalysisTable);

		JsonConvertor object = new JsonConvertor();
		JSONObject jsonObject = new JSONObject();
		ObjectMapper mapper = new ObjectMapper();
		
		List<String> incomingLinks = null;
		
		
		while(rows.hasNext()) {
			DBObject eachRow = rows.next();
			String url = (String) eachRow.get("url");
			jsonObject = object.getJSONObject((String) eachRow.get("incominglinks"));
			String data = jsonObject.get("listofincominglinks").toString();
			double currentRank = Double.parseDouble(eachRow.get("rank").toString());
			double pageRank = 0;
			try {
				incomingLinks = mapper.readValue(data, new TypeReference<List<String>>(){});
				
				for (String eachIncomingLinks : incomingLinks) {
					 pageRank += pageRankingForEachIncomingLinks(eachIncomingLinks);
				}

				int count = dal.getAllFromTable(linkAnalysisTable).count();
				double sum1 = ((1 - 0.85)/count);
				double sum2 = 0.85 * pageRank;
				pageRank = sum1 + sum2;
				
				updateRank(url,currentRank,linkAnalysisTable,"rankPrevious");
				updateRank(url,pageRank,linkAnalysisTable,"rank");
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			incomingLinks = null;
			
		}
	}

	private double pageRankingForEachIncomingLinks(String url) {
		keyValue query = new keyValue("url", url);
		DBCursor cursor = dal.findInTableAsPerQuery(linkAnalysisTable, query);
		double pageRankofIncoming = 0;
		while(cursor.hasNext()){
			pageRankofIncoming = 0;
			DBObject eachRow = cursor.next();
			double pageRank = Double.parseDouble(eachRow.get("rankPrevious").toString());
			int noOfOutGoingLinks = Integer.parseInt(eachRow.get("lengthofoutgoinglink").toString());
			pageRankofIncoming = (pageRank / noOfOutGoingLinks);
		}
		return pageRankofIncoming;
	}

	private void setInitialRank() {
		DBCollection linkAnalysisTable =  dal.getTableFromDatabase("searchengine", "linkAnalysis");
		int noOfDocuments = dal.findCount(linkAnalysisTable);
		double numerator = 1;
		double initalRank = numerator / noOfDocuments;
		DBCursor rows = dal.getAllFromTable(linkAnalysisTable);
		while(rows.hasNext()) {
			DBObject eachRow = rows.next();
			String url = (String) eachRow.get("url");
			updateRank(url,initalRank,linkAnalysisTable,"rank");
			updateRank(url,initalRank,linkAnalysisTable,"rankPrevious");
		}
		
	}

	public void updateRank(String url,double rank, DBCollection linkAnalysisTable,String key){
		BasicDBObject queryByUrl = new BasicDBObject();
		queryByUrl.put("url", url);
		
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put(key, rank);
				
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);
		linkAnalysisTable.update(queryByUrl, updateObj);
		
	}

	private void updateLinkAnalysisTabel() {
		DBCollection linkAnalysisTable =  dal.getTableFromDatabase("searchengine", "linkAnalysis");
		DBCursor rows = dal.getAllFromTable(linkAnalysisTable);
		List<String> incomingLinkList = new ArrayList<String>();
		while(rows.hasNext()) {
			DBObject eachRow = rows.next();
			String url = (String) eachRow.get("url");
			incomingLinkList = getIncomingLinks(url);
			updateIncomingLinkList(url,linkAnalysisTable,incomingLinkList);
			updateIncomingLinkListLength(url,linkAnalysisTable,incomingLinkList.size());
		}
		
	}


	private void updateIncomingLinkListLength(String url, DBCollection linkAnalysisTable, int size) {
		BasicDBObject queryByUrl = new BasicDBObject();
		queryByUrl.put("url", url);
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("lengthofincominglink", size);	
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);
		linkAnalysisTable.update(queryByUrl, updateObj);
	}



	private void updateIncomingLinkList(String url, DBCollection linkAnalysisTable, List<String> incomingLinkList) {
		JSONObject object=new JSONObject();		
		object.put("listofincominglinks", incomingLinkList);
		BasicDBObject queryByUrl = new BasicDBObject();
		queryByUrl.put("url", url);
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("incominglinks", object.toString());	
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);
		linkAnalysisTable.update(queryByUrl, updateObj);
		
	}

	private List<String> getIncomingLinks(String parentUrl) {
		DBCollection linkAnalysisTable =  dal.getTableFromDatabase("searchengine", "linkAnalysis");
		DBCursor rows = dal.getAllFromTable(linkAnalysisTable);
		JsonConvertor object = new JsonConvertor();
		JSONObject jsonObject = new JSONObject();
		ObjectMapper mapper = new ObjectMapper();
		
		List<String> inComingLinks = new ArrayList<String>();
		LinkedHashSet<String> inComingLinksSet =  new LinkedHashSet<String>();
		
		while(rows.hasNext()) {
			DBObject eachRow = rows.next();
			String url = (String) eachRow.get("url");
			//to get the outgoing link list
			jsonObject = object.getJSONObject((String) eachRow.get("outgoinglinks"));
			String data = jsonObject.get("listofoutgoinglinks").toString();
			List<String> outGoingLinks = null;
			try {
				outGoingLinks = mapper.readValue(data, new TypeReference<List<String>>(){});
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    
			for (String eachOutGoingLink : outGoingLinks) {
				if(eachOutGoingLink.equalsIgnoreCase(parentUrl)){
					inComingLinksSet.add(url);
				}
				
			}
		}
		for (String eachSetElement : inComingLinksSet) {
			inComingLinks.add(eachSetElement);
		}
		return inComingLinks;
	}


	private void setLinkAnalysisTable(){
		
		DBCollection urlTable =  dal.getTableFromDatabase("searchengine", "urltable");
		DBCursor rows = dal.getAllFromTable(urlTable);
		List<String> outGoingLinks = null;
		int lengthOfOutGoingLinks = 0;
		
		JsonConvertor object = new JsonConvertor();
		JSONObject jsonObject = new JSONObject();
		ObjectMapper mapper = new ObjectMapper();
		
		while(rows.hasNext()) {
			outGoingLinks = new ArrayList<String>();
			DBObject eachRow = rows.next();
			String path = (String) eachRow.get("path");
			String data1 = (String) eachRow.get("outgoinglinks");
			if(data1 != null){
				jsonObject = object.getJSONObject(data1);
				String data2 = jsonObject.get("listofoutgoinglinks").toString();
				try {
					outGoingLinks = mapper.readValue(data2, new TypeReference<List<String>>(){});
					lengthOfOutGoingLinks = outGoingLinks.size();
					
					ProperURL obj = new ProperURL();
					String url = (String) eachRow.get("DNS");
					URL aURL = new URL(url);
					if((path != null) && (("http".equals(aURL.getProtocol())) || ("https".equals(aURL.getProtocol()))))
						{
						
						analysis.insertIntoLinkAnalysis(obj.removeUrl(url), outGoingLinks, lengthOfOutGoingLinks, null, 0, linkAnalysisTable);
					}	
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	private void insertIntoLinkAnalysis(String url,List<String> outGoingLinks,int lengthOfOutgoingLinks,List<String> inComingLinks,int lengthOfIncomingLinks,DBCollection linkAnalysisTable) {
		JsonConvertor object = new JsonConvertor();
		List<keyValue> keyvalues = new ArrayList<keyValue>();
		keyvalues.add(new keyValue("url",url));
		keyvalues.add(new keyValue("outgoinglinks", object.stringifyJsonObject("listofoutgoinglinks", outGoingLinks) ) );
		keyvalues.add(new keyValue("lengthofoutgoinglink", Integer.toString(lengthOfOutgoingLinks) ) );
		keyvalues.add(new keyValue("incominglinks", (inComingLinks == null)? null:object.stringifyJsonObject("listofincominglinks", inComingLinks) ) );
		keyvalues.add(new keyValue("lengthofincominglink", Integer.toString(lengthOfIncomingLinks) ) );
		keyvalues.add(new keyValue("rank", "0"));
		keyvalues.add(new keyValue("rankPrevious", "0"));
		dal.insertIntoTable(linkAnalysisTable, keyvalues);

	
	}

	public static void main(String[] args) throws IOException {
		dal.dropTable("linkAnalysis");
		analysis.performLinkAnalysis();
		}


}
