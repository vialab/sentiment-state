package ca.uoit.kddm.mining;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.mongodb.BasicDBObject;

public class CrimeMiner {

	public CrimeMiner() {
	}
	
	public static void main(String[] args) {
		Date from = new GregorianCalendar(2012, 9, 1).getTime();
		Date to   = new GregorianCalendar(2013, 9, 1).getTime();
		
		BasicDBObject query = new BasicDBObject("created_at", new BasicDBObject("$gte", from))
				.append("created_at", new BasicDBObject("$lt", to));
		
		TweetLoader loader = new TweetLoader("tweets", "timeline", query);
		loader.load();
		
		
	}
}
