package ca.uoit.kddm.client.callback;

import ca.uoit.kddm.client.filter.TweetFilter;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class PrintCallback extends FilterCallback {

	public PrintCallback(TweetFilter filter) {
		super(filter);
	}
	
	@Override
	protected void handleMessage(String msg) {
		DBObject tweet = (DBObject)JSON.parse(msg);
//		System.out.println(msg);
		System.out.println(String.format("Message: %s", tweet.get("text")));
		DBObject place = (DBObject)tweet.get("place");
		if (place != null){
			System.out.println(String.format("Place: %s", place.get("full_name")));
			System.out.println(String.format("Country: %s", place.get("country")));
		}
		System.out.println(String.format("%,d messages read, %,d messages saved...", read, filtered));
	}

}
