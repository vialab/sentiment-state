package ca.uoit.kddm.client.filter;

import java.util.List;

import nsidc.spheres.LatLonBoundingBox;
import nsidc.spheres.Point;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class LocationFilter implements TweetFilter {
	
	List<LatLonBoundingBox> locations;
	
	public LocationFilter(List<LatLonBoundingBox> locations) {
		this.locations = locations;
	}
	
	@Override
	public boolean filter(String m) {
		DBObject msg = (DBObject)JSON.parse(m);
		if (msg.get("place") != null){
			String place = ((DBObject)msg.get("place")).get("name").toString();
			if (place == "Chicago"){
				System.out.println("Chicago!");
			}
		}
		
		// structure of coordinates field: https://dev.twitter.com/docs/platform-objects/tweets
		DBObject outterCoord = (DBObject)msg.get("coordinates");
		if (outterCoord != null){
			BasicDBList coords = (BasicDBList) outterCoord.get("coordinates");
			
			Double lat = (Double)coords.get(1);
			Double lon = (Double)coords.get(0);
			
			Point p = new Point(lat, lon);
			
			for (LatLonBoundingBox l : locations)
				if (l.contains(p))
					return true;
			
			
		}
		
		return false;
	}

}
