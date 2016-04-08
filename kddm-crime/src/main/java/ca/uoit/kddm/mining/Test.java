package ca.uoit.kddm.mining;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bson.types.ObjectId;

import ca.uoit.kddm.mining.entity.LightweightTweet;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class Test {

	public static void main(String[] args) throws ParseException {
//		SimpleDateFormat f = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
//		f.setLenient(true);
//		Date k = f.parse("Wed Dec 16 15:02:41 +0000 2009");
//		
////		String d = "Mon Dec 12 08:55:57 +0000 2011";
//		System.out.println(f.format(k));
//		//System.out.println(c.compare(from, to));
//		//System.out.println(f.format(from));
		
		MongoClient client = null;
		try {
			client = new MongoClient();
		} catch (Exception e) { e.printStackTrace(); }

		DBCollection collection = client.getDB("tweets").getCollection("timelines");
		BasicDBObject query = new BasicDBObject("_id", new ObjectId("528d62f6593c04b9638114f5"));
		DBCursor cursor = collection.find(query);
		while (cursor.hasNext()){
			DBObject o = cursor.next();
			LightweightTweet t = LightweightTweet.createInstance(o.toMap());
			//SimpleDateFormat f = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
			SimpleDateFormat f = new SimpleDateFormat("yyyy.MM.dd");
			f.setLenient(true);
			System.out.println(f.format(t.getCreated_at()));
		}
	}
	

}
