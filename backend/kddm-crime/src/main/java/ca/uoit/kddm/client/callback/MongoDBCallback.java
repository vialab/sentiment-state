package ca.uoit.kddm.client.callback;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import utils.Util;
import ca.uoit.kddm.client.filter.TweetFilter;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class MongoDBCallback extends FilterCallback {

	DBCollection collection;
	SimpleDateFormat TWITTER_DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
	
	public MongoDBCallback(TweetFilter filter, String collectionName) throws UnknownHostException {
		super(filter);
		
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB("tweets");
		
		collection = db.getCollection(collectionName);
	}
	
	public DBCollection getCollection() {
		return collection;
	}
	
	@Override
	public void handleMessage(String msg) {
		  if (msg.charAt(0)=='['){
			  JSONArray array = Util.toJSONArray(msg);
			  for (int i = 0; i < array.length(); i++) {
				  try {
					collection.save(parseJSONTweet(array.getString(i)));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			  }
		  } else{
	    	  DBObject object = parseJSONTweet(msg);
	    	  collection.save(object);
	    	  System.out.println(object.get("id") + ": " + object.get("text"));
	    	  System.out.println(String.format("%,d messages read, %,d messages saved...", read, filtered));
	    	  System.out.println();
		  }
	}
	
	private DBObject parseJSONTweet(String msg){
		DBObject o = (DBObject)JSON.parse(msg);
		String created_at = (String)o.get("created_at");
		try {
			// Changes the type of the date field to Date instead of String
			// this will allow us to query the records by Date using the Mongo operators
			Date creation_date = TWITTER_DATE_FORMAT.parse(created_at);
			o.put("created_at", creation_date);
			return o;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
