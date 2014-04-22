package ca.uoit.kddm.app;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;
import ca.uoit.kddm.auth.Credentials;
import ca.uoit.kddm.client.StreamClient;
import ca.uoit.kddm.client.TwitterBufferConsumer;
import ca.uoit.kddm.client.TwitterCredentials;
import ca.uoit.kddm.client.callback.CompoundCallback;
import ca.uoit.kddm.client.callback.MongoDBCallback;
import ca.uoit.kddm.client.callback.TwitterMessageCallback;
import ca.uoit.kddm.client.filter.CityFilter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.twitter.hbc.core.endpoint.Location;
import com.twitter.hbc.core.endpoint.Location.Coordinate;

public class CityHose {
	
	public static void main(String[] args) throws InterruptedException, UnknownHostException, FileNotFoundException{
		
		TwitterCredentials credentials = Credentials.getCredentials("brittany");
		
		CityFilter filter = new CityFilter(Arrays.asList(new String[]{"Chicago"}));
		
		// 1. MongoDBCallback saves to DB the tweets from Chicago
		// 2. UserTrackerCallback checks the origin of the author of every Tweet.
		// In case the origin is Chicago, saves to DB all tweets from the author. 
		CompoundCallback callback = new CompoundCallback(new MongoDBCallback(filter, "chicago_stream"),
			new UserTrackerCallback());
		
		StreamClient client = new StreamClient(credentials, callback);
		
		// bounding box from http://boundingbox.klokantech.com/
		Location chicago = new Location(new Coordinate(-87.940101,41.643919), new Coordinate(-87.523984,42.023022));
		List<Location> locations = Arrays.asList(chicago);
		client.setLocations(locations);
		
		client.run();
	}
	
}

class UserTrackerCallback implements TwitterMessageCallback{

	Queue<Long> userIDs;
	DBCollection collection;
	
	public UserTrackerCallback() throws FileNotFoundException, UnknownHostException {
		userIDs = new LinkedBlockingQueue<Long>(1000);
		
//		MongoDBCallback mongodb = new MongoDBCallback(new TimelineDuplicateFilter("tweets", "timelines"), "timelines");
		MongoDBCallback mongodb = new MongoDBCallback(null, "timelines");
		// we also need the collection to test if a user is already in the DB
		collection = mongodb.getCollection();
		
		TwitterBufferConsumer c1 = new TwitterBufferConsumer(Credentials.getCredentials("rafadevrafa"), userIDs, mongodb);
		TwitterBufferConsumer c2 = new TwitterBufferConsumer(Credentials.getCredentials("brittany"), userIDs, mongodb);
		TwitterBufferConsumer c3 = new TwitterBufferConsumer(Credentials.getCredentials("rafaveguim"), userIDs, mongodb);
		
		new Thread(c1).start();
		new Thread(c2).start();
		new Thread(c3).start();
	}
	
	private boolean isInDB(long userID){
		BasicDBObject query = new BasicDBObject("user.id", userID);
		
		// DEBUG block
		int count = collection.find(query).count();
		if (count>0){
			System.err.println("[DEBUG] Tweets of author " + userID + " already live in DB.");
		}
		
		return count==0;
	}
	
	@Override
	public void callback(String msg) {
		Status s = null;
		try {
			s = DataObjectFactory.createStatus(msg);
		} catch (TwitterException e) { e.printStackTrace(); }
		
		// if the author is from Chicago and his timeline is not yet in the DB, puts in the queue
		if (s.getUser().getLocation().toLowerCase().contains("chicago")
				&& isInDB(s.getUser().getId()))
			userIDs.offer(s.getUser().getId());
	}
}

