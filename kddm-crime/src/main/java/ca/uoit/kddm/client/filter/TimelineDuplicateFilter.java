package ca.uoit.kddm.client.filter;

import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONException;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class TimelineDuplicateFilter implements TweetFilter {

	DBCollection collection;
	
	public TimelineDuplicateFilter(String dbName, String collectionName) throws UnknownHostException {
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB(dbName);
		
		collection = db.getCollection(collectionName);
	}
	
	@Override
	public boolean filter(String msg) {
		JSONArray array = null;
		try {
			array = new JSONArray(msg);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		Status status = null;
		try {
			status = DataObjectFactory.createStatus(array.getJSONObject(0).toString());
		} catch (TwitterException | JSONException e) {
			e.printStackTrace();
		}
		
		Long id = status.getUser().getId(); // Author's id
		
		BasicDBObject query = new BasicDBObject("user.id", id);
		
		int count = collection.find(query).count();
		// DEBUG block
		if (count>0){
			System.err.println("[DEBUG] Tweets of author " + id + " already live in DB.");
		}
		
		return count==0;
	}

}
