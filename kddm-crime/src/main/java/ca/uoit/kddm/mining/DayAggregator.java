package ca.uoit.kddm.mining;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ca.uoit.kddm.mining.entity.LightweightTweet;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;

public class DayAggregator {

	
	public static void main(String[] args) throws IOException {
		String DIR_PATH = "/home/rafa/Data/kddm/chicago_tweets";
		
		Date from = new GregorianCalendar(2012, 9, 1).getTime();
		Date to   = new GregorianCalendar(2013, 9, 1).getTime();
		
		// Query all tweets between Oct,2102 and Oct,2013
		BasicDBObject query = new BasicDBObject("created_at", BasicDBObjectBuilder.start("$gte", from)
				.add("$lt", to).get());//.append("retweeted_status", null);
		
		TweetLoader loader = new TweetLoader("tweets", "timelines", query);
		List<LightweightTweet> tweets = loader.load();

		Map<Date, List<LightweightTweet>> dayDist = new TreeMap<Date, List<LightweightTweet>>(new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				Calendar c1 = new GregorianCalendar();
				Calendar c2 = new GregorianCalendar();
				c1.setTime(o1);
				c2.setTime(o2);
				c1.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH), 0, 0);
				c2.set(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH), 0, 0);
				
				return c1.compareTo(c2);
			}
		});
		
		// Aggregate tweets by date
		for (LightweightTweet t : tweets) {
			Date createdAt = t.getCreated_at();
			
			if (!dayDist.containsKey(createdAt))
				dayDist.put(createdAt, new ArrayList<LightweightTweet>());
			
			dayDist.get(t.getCreated_at()).add(t);
		}
		
		// Create files with tweets for each date
		for (Date d : dayDist.keySet()) {
			SimpleDateFormat f = new SimpleDateFormat("yyyy.MM.dd");
			String filePath = String.format("%s/%s.txt", DIR_PATH, f.format(d));
			File file = new File(filePath);
			FileWriter fw = new FileWriter(file);
			
			StringBuilder builder = new StringBuilder();
			for (LightweightTweet t : dayDist.get(d))
				builder.append(t.getMsg()+'\n');
			
			fw.write(builder.toString());
			
			fw.close();
		}
	}

}
