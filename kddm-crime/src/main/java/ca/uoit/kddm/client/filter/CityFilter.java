package ca.uoit.kddm.client.filter;

import java.util.List;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class CityFilter implements TweetFilter {
	
	List<String> cities;
	
	public CityFilter(List<String> cities) {
		this.cities = cities;
	}
	
	@Override
	public boolean filter(String msg) {
		DBObject tweet = (DBObject)JSON.parse(msg);
		DBObject place = (DBObject)tweet.get("place");
		if (place != null){
			String placeName = place.get("name").toString();
			
			if (cities.contains(placeName))
				return true;
		}
		
		return false;
	}

}
