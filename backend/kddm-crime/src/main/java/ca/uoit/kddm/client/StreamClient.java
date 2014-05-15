package ca.uoit.kddm.client;


import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ca.uoit.kddm.client.callback.TwitterMessageCallback;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.Location;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class StreamClient {
	
	// Long/Lat Chicago's bounding box: (-88.1406,41.4407), (-87.3221,42.4153)
	// from http://boundingbox.klokantech.com/
//	LatLonBoundingBox location = new LatLonBoundingBox(41.4407, 42.4153, -88.1406, -87.3221);	
	
	List<String> keywords;
	List<Location> locations;
	Authentication auth;
	
	TwitterMessageCallback callback;
	
	public StreamClient(TwitterCredentials credentials, TwitterMessageCallback callback) {
		this.callback = callback;
		setCredentials(credentials.consumerKey, credentials.consumerSecret, credentials.token, credentials.tokenSecret);
	}
	
	public void setCredentials(String consumerKey, String consumerSecret, String token, String tokenSecret){
		auth = new OAuth1(consumerKey, consumerSecret, token, tokenSecret);
		//Authentication oauth2 = new OAuth2d11Request(tokenSecret, parsedRequest)
	}
	
	public void run() throws InterruptedException{
		/** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
		BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
		BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(1000);

		/** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
		StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();

//		List<String> terms = Arrays.asList("chicago");
//		List<Location> locations = Arrays.asList(new Location(southwest, northeast))
		if (keywords != null)
			endpoint.trackTerms(keywords);
		if (locations != null)
			endpoint.locations(locations);
		
		
		ClientBuilder builder = new ClientBuilder()
		  .name("Hosebird-Client-01")                              // optional: mainly for the logs
		  .hosts(Constants.STREAM_HOST)
		  .authentication(auth)
		  .endpoint(endpoint)
		  .processor(new StringDelimitedProcessor(msgQueue))
		  .eventMessageQueue(eventQueue);                          // optional: use this if you want to process client events
		
		Client hosebirdClient = builder.build();
		hosebirdClient.connect();
		
		// Do whatever needs to be done with messages
	    while (true) {
	      String msg = msgQueue.take();
	      callback.callback(msg);
	    }
	    
	}
	
	public List<String> getKeywords() { return keywords;}
	public void setKeywords(List<String> keywords) { this.keywords = keywords; }
	public List<Location> getLocations() { return locations; }
	public void setLocations(List<Location> locations) { this.locations = locations;}
	
}
