package cs3.calstatela.edu.SearchEngine.Crawl;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import cs3.calstatela.edu.SearchEngine.Dal.MongoDal;

public class DataDump {
	
	static MongoDal dal = new MongoDal();
	private void readCollection() {
		DBCollection urlTable =  dal.getTableFromDatabase("searchengine", "wordindextable");
		DBCursor cursor = dal.getAllFromTable(urlTable);	

		String word = null,frequency = null;	
		System.out.println("CRAWLED WEBSITE DETAILS");
		while (cursor.hasNext()) {
			DBObject obj = cursor.next(); 
			word = (String)obj.get("word");
			frequency = (String)obj.get("frequency");
			System.out.println("  ");
			System.out.println("URL            : "+word);
			System.out.println("Frequent Words : "+frequency);
		}
	}

	public static void main(String[] args) {
		DataDump obj = new DataDump();
		obj.readCollection();
	}

}
