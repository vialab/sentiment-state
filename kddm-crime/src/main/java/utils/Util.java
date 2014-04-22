package utils;

import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Util {
	public static String toString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	public static JSONArray toJSONArray(java.io.InputStream is){
		JSONTokener jt = new JSONTokener(new InputStreamReader(is));
		try {
			return new JSONArray(jt);
		} catch (JSONException e){
			e.printStackTrace();
			System.out.println("A problem occurred while parsing the JSON document");
			return new JSONArray();
		}
	}
	
	public static JSONArray toJSONArray(String s){
		JSONTokener jt = new JSONTokener(s);
		try {
			return new JSONArray(jt);
		} catch (JSONException e){
			//e.printStackTrace();
			System.out.println("JSON document invalid!");
			return null;
		}
	}
	
	public static JSONObject toJSONObject(String s){
		JSONTokener jt = new JSONTokener(s);
		try {
			return new JSONObject(jt);
		} catch (JSONException e){
			e.printStackTrace();
			System.out.println("A problem occurred while parsing the JSON document");
			return new JSONObject();
		}
	}
}
