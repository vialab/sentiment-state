package ca.uoit.kddm.client.filter;

import java.util.List;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class KeywordFilter implements TweetFilter {

	List<String> keywords;
	
	public KeywordFilter(List<String> keywords) {
		this.keywords = keywords;
	}
	
	@Override
	public boolean filter(String msg) {
		DBObject tweet = (DBObject)JSON.parse(msg);
		boolean answer = false;
		String text = (String) tweet.get("text");
		for (String k : keywords) {
			if (text.contains(k)){
				answer = true;
				break;
			}
		}
		
		return answer;
	}

}
