package cs3.calstatela.edu.SearchEngine.Dal;

import java.net.UnknownHostException;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

import cs3.calstatela.edu.SearchEngine.Model.keyValue;


public class MongoDal {
	public static MongoClient mongo = null;
	public MongoDal(){		
		try {
			mongo = new MongoClient("localhost", 27017);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public DB getDatabase(String databaseName){
		DB db = mongo.getDB(databaseName);
		return db;
	}
	
	public void dropTable(String tableName){
		DB db = mongo.getDB("searchengine");

		db.getCollection(tableName).drop();
		
	}
	public DBCollection getTableFromDatabase(String databaseName, String tableName){
		DB db = mongo.getDB(databaseName);

		DBCollection table = db.getCollection(tableName);
		
		return table;
	}
	
	public void insertIntoTable(DBCollection tableCollection , List<keyValue> keyvalues){
		BasicDBObject document = new BasicDBObject();
		
		for(keyValue eachKeyValue : keyvalues){
			document.put(eachKeyValue.key,eachKeyValue.value);
		}
		
		tableCollection.insert(document);
	}
	
	public DBCursor findInTableAsPerQuery(DBCollection table,keyValue query){
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put(query.key, query.value);
		DBCursor cursor = table.find(searchQuery);
		return cursor;
	}
	
	public int findCount(DBCollection table){
		DBCursor cursor = table.find();
		return cursor.count();
	}
	public DBCursor getAllFromTable(DBCollection table){
		DBCursor cursor = table.find();
		return cursor;
	}
	
	public void dropDatabase(String databaseName){
		
		mongo.dropDatabase(databaseName);
	}
	
}
