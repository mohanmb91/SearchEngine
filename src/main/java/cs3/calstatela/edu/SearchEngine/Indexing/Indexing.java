package cs3.calstatela.edu.SearchEngine.Indexing;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import cs3.calstatela.edu.SearchEngine.Dal.MongoDal;
import cs3.calstatela.edu.SearchEngine.Model.PositionTFweightageModel;
import cs3.calstatela.edu.SearchEngine.Model.keyValue;

public class Indexing {

	static MongoDal dal = new MongoDal();
	static Indexing objIndexing = new Indexing();
	static DBCollection wordIndexTable =  dal.getTableFromDatabase("searchengine", "wordindextable");

	private void performIndexing() throws MalformedURLException {
		setWordIndexTabel();
		calculateTFIDFWeightage();//weightage
	}
	
	private void calculateTFIDFWeightage() {
		DBCollection urlTable =  dal.getTableFromDatabase("searchengine", "urltable");
		double noOfDocuments = dal.findCount(urlTable);
		DBCursor rows = dal.getAllFromTable(wordIndexTable);
		while(rows.hasNext()) {
			DBObject eachRow = rows.next();
			String word = (String) eachRow.get("word");
			updateWeightage(word,noOfDocuments);
		}	
	}
		
	private void updateWeightage(String word,double noOfDocuments) {
		DBCollection wordIndexTable =  dal.getTableFromDatabase("searchengine", "wordindextable");
		DBCursor cursor =  dal.findInTableAsPerQuery(wordIndexTable, new keyValue("word",word));
		ObjectMapper mapper = new ObjectMapper();
		Map<String,List<PositionTFweightageModel>> frequencyMapFromTable = new HashMap<String, List<PositionTFweightageModel>>(); 
		double weightage = 0.0;
		double tf = 0.0;
		double idf = 0.0;
		double df= 0.0;
		if(cursor.hasNext()){
			DBObject eachRow = cursor.next();
			JSONObject jsonObject = new JSONObject( (String) eachRow.get("frequency")); // HashMap
			Iterator<?> urlKeySet = jsonObject.keys(); // HM
			ArrayList<Integer> positionArray = new ArrayList<Integer>();
			while (urlKeySet.hasNext()) {
				String eachurlKey=  (String) urlKeySet.next();
				//getting json for evey url
				JSONObject innerJsonObject = new JSONObject((jsonObject.get(eachurlKey)).toString()); // HashMap
				List<PositionTFweightageModel> positionTFWeitageDetails= new ArrayList<PositionTFweightageModel>();
				try {
					String title = innerJsonObject.getString("title");
					positionArray = mapper.readValue(((innerJsonObject.get("position")).toString()), new TypeReference<ArrayList<Integer>>(){});
					tf = Double.parseDouble((innerJsonObject.get("tf").toString()));
					df = jsonObject.length();
					idf = Math.log(noOfDocuments/df) / Math.log(10);
					weightage = tf * idf;
					positionTFWeitageDetails.add(new PositionTFweightageModel(weightage,tf,positionArray,title));
					frequencyMapFromTable.put( eachurlKey, positionTFWeitageDetails );
					
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			updateWordIndexTable(wordIndexTable,word,frequencyMapFromTable);
		}
		
	}

	private void setWordIndexTabel() throws MalformedURLException{
		DBCollection urlTable =  dal.getTableFromDatabase("searchengine", "urltable");
		DBCursor rows = dal.getAllFromTable(urlTable);
		while(rows.hasNext()) {
			DBObject eachRow = rows.next();
			String url = (String) eachRow.get("DNS");
			String releventData = (String) eachRow.get("releventdata");
			String titleFromTable = (String) eachRow.get("title");
			String titleItems[] = titleFromTable.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
			System.out.println(titleFromTable);
			String title = " ";
			for (String eachString : titleItems) {
				System.out.println(eachString);
				title = title.concat(eachString).concat(" ");
			}
			title = title.toLowerCase();
			System.out.println(title);
			Map<String, ArrayList<Integer>> wordWithPositionMap = new HashMap<String, ArrayList<Integer>>();
			wordWithPositionMap = getwordWithPositionMap(releventData);
			perpareWordIndexTable(wordWithPositionMap,url,title);
		
		}
	}
	
	private Map<String, ArrayList<Integer>> getwordWithPositionMap(String releventData) {
		Map<String, ArrayList<Integer>> wordsWithCountMap = new HashMap<String, ArrayList<Integer>>(); 

		JSONObject jsonObject = new JSONObject( releventData ); // HashMap
		Iterator<?> wordsKeySet = jsonObject.keys(); // HM

		while (wordsKeySet.hasNext()) {
			String eachWordKey =  (String) wordsKeySet.next();
			String data = (jsonObject.get(eachWordKey)).toString();
			String[] items = data.replaceAll("\\[", "").replaceAll("\\]", "").split(",");

			ArrayList<Integer> eachWordValueList = new ArrayList<Integer>();
			for (int j = 0; j < items.length; j++) {
				eachWordValueList.add(Integer.parseInt(items[j]));

			}
			wordsWithCountMap.put( eachWordKey, eachWordValueList );
		}

		return wordsWithCountMap;
	}

	

	private void perpareWordIndexTable(Map<String, ArrayList<Integer>> wordWithPositionMap, String url,String title) {
		
		String word = null;
		double termFrequency = 0;
		JSONObject jsonObject=null;
		JSONObject jsonObject1 = null;
		
		Set<Entry<String, ArrayList<Integer>>> wordWithPositionSet = wordWithPositionMap.entrySet();
		ArrayList<Integer> positionsList = null;
		//System.out.println("**************8Display Word Counts*******************");
		Map<String,List<PositionTFweightageModel>> frequencyMapFromTable = new HashMap<String, List<PositionTFweightageModel>>(); 
		List<PositionTFweightageModel> positionListobj = new ArrayList<PositionTFweightageModel>();
		for(Entry<String, ArrayList<Integer>> eachWordEntry : wordWithPositionSet){
			
			//get the list of position for each word
			word = eachWordEntry.getKey();
			positionsList = new ArrayList<Integer>();
			positionsList = eachWordEntry.getValue();
			
			if(isWordExist(word))
			{	
				//System.out.println("exit");
				termFrequency = 1+(Math.log(positionsList.size()) / Math.log(10));
				
				frequencyMapFromTable = new HashMap<String, List<PositionTFweightageModel>>();
				frequencyMapFromTable =getDocumentPositionList(word);
				
				positionListobj=new ArrayList<PositionTFweightageModel>();
				positionListobj.add(new PositionTFweightageModel(0,termFrequency,positionsList,title));
				frequencyMapFromTable.put(url, positionListobj);
			
				updateWordIndexTable(wordIndexTable,word,frequencyMapFromTable);
				
			}
			else
			{
				//System.out.println("new");
				termFrequency = 1+(Math.log(positionsList.size()) / Math.log(10));
				
				jsonObject=new JSONObject();
				jsonObject.put("weightage",0);
				jsonObject.put("tf", termFrequency);
				jsonObject.put("position",positionsList);
				jsonObject.put("title",title);
				//System.out.println("frequency"+jsonObject);
				
				jsonObject1 = new JSONObject();
				jsonObject1.put(url, jsonObject);

				insertIntoWordIndexTable(wordIndexTable,word,jsonObject1);
			}
			
			
	    }
		
	}


	private void updateWordIndexTable(DBCollection wordIndexTable, String word,
			Map<String, List<PositionTFweightageModel>> frequencyMapFromTable) {
		
		JSONObject jsonObject = null,jsonObject1 = null;
		Set<Entry<String, List<PositionTFweightageModel>>> frequencySet = frequencyMapFromTable.entrySet();
		List<PositionTFweightageModel> positionListObject = new ArrayList<PositionTFweightageModel>();
		jsonObject1 = new JSONObject();
		for (Entry<String, List<PositionTFweightageModel>> eachEntry : frequencySet) {
			String url = eachEntry.getKey();
			positionListObject = eachEntry.getValue();
			
			jsonObject =new JSONObject();
			for (PositionTFweightageModel eachEntryInList : positionListObject) {
				//System.out.println("**************"+url+" "+eachEntryInList.getPosition()+"  "+eachEntryInList.getTermFreq());	
				jsonObject.put("weightage", eachEntryInList.getWeightage());
				jsonObject.put("tf", eachEntryInList.getTermFreq());
				jsonObject.put("position",eachEntryInList.getPosition());
				jsonObject.put("title",eachEntryInList.getTitle());
				
			}
			//System.out.println("jsonobject"+jsonObject);
		
		jsonObject1.put(url, jsonObject);
		}
		//System.out.println("jsonObject1"+jsonObject1);


		DBObject dbObject = (DBObject) JSON.parse(jsonObject1.toString());
		BasicDBObject queryByUrl = new BasicDBObject();
		queryByUrl.put("word", word);
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.put("frequency", dbObject.toString());	
		BasicDBObject updateObj = new BasicDBObject();
		updateObj.put("$set", newDocument);
		wordIndexTable.update(queryByUrl, updateObj);
		
	}

	private Map<String, List<PositionTFweightageModel>> getDocumentPositionList(String word) {
		DBCollection wordIndexTable =  dal.getTableFromDatabase("searchengine", "wordindextable");
		DBCursor cursor =  dal.findInTableAsPerQuery(wordIndexTable, new keyValue("word",word));
		ObjectMapper mapper = new ObjectMapper();
		Map<String,List<PositionTFweightageModel>> frequencyMapFromTable = new HashMap<String, List<PositionTFweightageModel>>(); 
		
		if(cursor.hasNext()){
			DBObject eachRow = cursor.next();
			JSONObject jsonObject = new JSONObject( (String) eachRow.get("frequency")); // HashMap
			Iterator<?> urlKeySet = jsonObject.keys(); // HM
			ArrayList<Integer> positionArray = new ArrayList<Integer>();
			while (urlKeySet.hasNext()) {
				String eachurlKey=  (String) urlKeySet.next();
				//getting json for evey url
				JSONObject innerJsonObject = new JSONObject((jsonObject.get(eachurlKey)).toString()); // HashMap
				List<PositionTFweightageModel> positionListObj= new ArrayList<PositionTFweightageModel>();
				try {
					positionArray = mapper.readValue(((innerJsonObject.get("position")).toString()), new TypeReference<ArrayList<Integer>>(){});
					positionListObj.add(new PositionTFweightageModel(Double.parseDouble((innerJsonObject.get("weightage").toString())),Double.parseDouble((innerJsonObject.get("tf").toString())),positionArray,innerJsonObject.get("title").toString()));
					frequencyMapFromTable.put( eachurlKey, positionListObj );
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
		return frequencyMapFromTable;
	}


	private void insertIntoWordIndexTable(DBCollection wordIndexTable, String word, JSONObject jsonObject) {
        DBObject dbObject = (DBObject) JSON.parse(jsonObject.toString());
		List<keyValue> keyvalues = new ArrayList<keyValue>();
		keyvalues.add(new keyValue("word",word));
		keyvalues.add(new keyValue("frequency", dbObject.toString()));
		//System.out.println("frequency"+dbObject.toString());
		dal.insertIntoTable(wordIndexTable, keyvalues);
		
	}

	private boolean isWordExist(String word) {
		DBCollection wordIndexTable =  dal.getTableFromDatabase("searchengine", "wordindextable");
		DBCursor cursor =  dal.findInTableAsPerQuery(wordIndexTable, new keyValue("word",word));
		if(cursor.hasNext()){
			return true;	
		}
		return false;
	}


	public static void main(String[] args) throws MalformedURLException {
		dal.dropTable("wordindextable");
		objIndexing.performIndexing();

	}

}
