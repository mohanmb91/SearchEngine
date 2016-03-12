package cs3.calstatela.edu.SearchEngine.JSON.JsonConvertor;

import java.util.List;

import org.json.JSONObject;

public class JsonConvertor {
	
	public String stringifyJsonObject(String name, List<String> listObject) {
		
	JSONObject object=new JSONObject();
	object.put(name, listObject);
	
	return object.toString();
	}
	public String stringifyJson(String name, String listObject) {
		
		JSONObject object=new JSONObject();
		object.put(name, listObject);
		
		return object.toString();
		}
	
	public String stringifyJsonObjectWithIntList(String name, List<Integer> listObject) {
		
		JSONObject object=new JSONObject();
		object.put(name, listObject);
		
		return object.toString();
		}
	
public String stringifyJsonObjectWithDouble(String name, double listObject) {
		
		JSONObject object=new JSONObject();
		object.put(name, listObject);
		
		return object.toString();
		}
	
	public JSONObject getJSONObject(String jsonInString){
		JSONObject json = new JSONObject(jsonInString);
		return json;
		
	}
	
}

