package ca.uoit.kddm.client.filter;

import com.mongodb.DBObject;

public interface TweetFilter {
	
	public boolean filter(String msg);
	
}
