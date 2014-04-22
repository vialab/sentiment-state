package ca.uoit.kddm.mining;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import ca.uoit.kddm.mining.entity.LightweightTweet;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class TweetLoader {

	String dbName;
	String collectionName;
	
	DBCollection collection;
	DBObject query;
	
	public TweetLoader(String dbName, String collectionName, BasicDBObject query) {
		this.dbName = dbName;
		this.collectionName = collectionName;
		this.query = query;
		
		MongoClient client = null;
		try {
			client = new MongoClient();
		} catch (UnknownHostException e) { e.printStackTrace(); }

		this.collection = client.getDB(dbName).getCollection(collectionName);
	}
	
	public List<LightweightTweet> load(){
		return load(this.collection);
	}
	
	private List<LightweightTweet> load(DBCollection collection) {
		List<LightweightTweet> array = new ArrayList<>();
		
		DBCursor cursor = collection.find(query);
		
		while (cursor.hasNext()){
			DBObject o = cursor.next();
			LightweightTweet t = LightweightTweet.createInstance(o.toMap());
			array.add(t);
		}
		
		return array;
	}
	
	/**
	 * Test method
	 */
	public static void main(String[] args) {
		Date from = new GregorianCalendar(2012, 9, 1).getTime();
		Date to   = new GregorianCalendar(2013, 9, 1).getTime();
		
		BasicDBObject query = new BasicDBObject("created_at", new BasicDBObject("$gte", from))
				.append("created_at", new BasicDBObject("$lt", to));
		
		TweetLoader loader = new TweetLoader("tweets", "timeline", query);
		loader.load();
	}
	
}
