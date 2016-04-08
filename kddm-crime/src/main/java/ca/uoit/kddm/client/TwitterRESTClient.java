package ca.uoit.kddm.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.signature.AuthorizationHeaderSigningStrategy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.RateLimitStatus;
import twitter4j.TwitterException;
//import twitter4j.internal.http.HttpResponse;
import twitter4j.json.DataObjectFactory;
import utils.Util;
import ca.uoit.kddm.auth.Credentials;


public class TwitterRESTClient {

	TwitterCredentials credentials;
	
//	int user_timeline_requests = 0;
	
	// rate limiting for each resource, like /statuses/user_timeline
	Map<String, Integer> remainingRequests;
	Map<String, Integer> resetTime; // time when the current count will be reset
	
	public TwitterRESTClient(TwitterCredentials credentials) {
		this.credentials = credentials;
		remainingRequests = new HashMap<>();
		resetTime = new HashMap<>();
		
		Map<String, RateLimitStatus> limits = null;
		try {
			limits = rate_limit("statuses");
		} catch (LimitReachedException e1) {
			e1.printStackTrace();
		} catch (BadCredentialsException e1) {
			e1.printStackTrace();
		}
		
		for (Map.Entry<String, RateLimitStatus>  e : limits.entrySet()){
			remainingRequests.put(e.getKey(), e.getValue().getRemaining());
			resetTime.put(e.getKey(), e.getValue().getResetTimeInSeconds());
		}
	}
	
	public JSONArray getUserTweets(String username, int count, String max_id) throws LimitReachedException, BadCredentialsException{
		JSONArray tweets = null;
		String resource = "/statuses/user_timeline";
		try {
			countRequest(resource);
			URI uri = null;
			URIBuilder builder = new URIBuilder("https://api.twitter.com/1.1/statuses/user_timeline.json")
				.addParameter("screen_name", username)
				.addParameter("count", Integer.toString(count));
			
			if (max_id != null)
				builder.addParameter("max_id", max_id);
			
			uri = builder.build();
			String body = GET(uri);
			if (body != null)
				tweets = Util.toJSONArray(body);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (LimitReachedException e){
			updateRateLimitStatus(resource);
			e.setResetTime(resetTime.get(resource));
			throw e;
		}
		
		return tweets;
	}
	
	
	public JSONArray getUserTweets(Long userID, int count, String max_id) throws LimitReachedException, BadCredentialsException{
		JSONArray tweets = null;
		String resource = "/statuses/user_timeline";
		try {
			countRequest(resource);
			URI uri = null;
			URIBuilder builder = new URIBuilder("https://api.twitter.com/1.1/statuses/user_timeline.json")
				.addParameter("user_id", Long.toString(userID))
				.addParameter("count", Integer.toString(count));
			
			if (max_id != null)
				builder.addParameter("max_id", max_id);
			
			uri = builder.build();
			String body = GET(uri);
			if (body != null)
				tweets = Util.toJSONArray(body);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (LimitReachedException e){
			updateRateLimitStatus(resource);
			e.setResetTime(resetTime.get(resource));
			throw e;
		}
		
		return tweets;
	}
	
	public JSONArray getAllUserTweets(String username) throws LimitReachedException, BadCredentialsException{
		System.out.println("Requesting tweets from username " + username + "...\n");
		
		JSONArray all = new JSONArray(); 
		
		Long max_id = null;
		JSONArray page; 
		try {
			do {
				page = getUserTweets(username, 200, max_id != null ? Long.toString(max_id) : null);
				for (int i = 0; i < page.length(); i++)
						all.put(page.getJSONObject(i));
				
				if (page.length()>0){
					JSONObject last = page.getJSONObject(page.length()-1);
					max_id = last.getLong("id") - 1;
				} else break;
				
			} while (true);
		
		} catch (JSONException e) {	e.printStackTrace(); }
		
		System.out.println(all.length() + " tweets from username " + username + " received.\n");
		
		return all;
	}
			
	public JSONArray getAllUserTweets(Long userID) throws LimitReachedException, BadCredentialsException{
		System.out.println("Requesting tweets from userID " + userID + "...\n");
		
		JSONArray all = new JSONArray(); 
		
		Long max_id = null;
		JSONArray page; 
		try {
			do {
				page = getUserTweets(userID, 200, max_id != null ? Long.toString(max_id) : null);
				for (int i = 0; i < page.length(); i++)
						all.put(page.getJSONObject(i));
				
				if (page.length()>0){
					JSONObject last = page.getJSONObject(page.length()-1);
					max_id = last.getLong("id") - 1;
				} else break;
				
			} while (true);
		
		} catch (JSONException e) {	e.printStackTrace(); }
		
		System.out.println(all.length() + " tweets from userID " + userID + " received.\n");
		
		return all;
	}
	
	private void updateRateLimitStatus(String resource){
		RateLimitStatus s;
		try {
			s = rate_limit("statuses").get(resource);
		} catch (Exception e){
			return;
		}
		remainingRequests.put(resource, s.getRemaining());
		resetTime.put(resource, s.getResetTimeInSeconds());
	}
	
	public void countRequest(String resource) throws LimitReachedException{
		long currentT = System.currentTimeMillis()/1000;
		int resetT = resetTime.get(resource);
		
		if (currentT >= resetT)
			updateRateLimitStatus(resource);
		
		
		int r = remainingRequests.get(resource);
		if (r > 0)
			remainingRequests.put(resource, --r);
		else {
			LimitReachedException e = new LimitReachedException(resetTime.get(resource));
//			DateFormat df = new SimpleDateFormat("K:mm a,z");
//			GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
//			gc.setTimeInMillis(resetT*1000L);
//			System.out.println(df.format(gc.getTime()));
			throw e;
		}
	}
	
	
	public void sign(HttpRequest url){
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(credentials.consumerKey, credentials.consumerSecret);
        consumer.setTokenWithSecret(credentials.token, credentials.tokenSecret);
		try {
			consumer.setSigningStrategy(new AuthorizationHeaderSigningStrategy());
			
			consumer.sign(url);
		} catch (OAuthMessageSignerException | OAuthExpectationFailedException
				| OAuthCommunicationException e) {
			e.printStackTrace();
			System.out.println("Sh*t! A problem occurred while signing the URL...");
		}
	}
	
	public Map<String, RateLimitStatus> rate_limit (String resources) throws LimitReachedException, BadCredentialsException{
//		System.out.println("Rate Limit was called");
		URI uri = null;
		try {
			uri = new URIBuilder("https://api.twitter.com/1.1/application/rate_limit_status.json")
				.addParameter("resources", resources)
			    .build();
		} catch (URISyntaxException e) { e.printStackTrace(); }

		Map<String, RateLimitStatus> statuses = null;
		try {
			statuses = DataObjectFactory.createRateLimitStatus(GET(uri));
		} catch (TwitterException e) { e.printStackTrace();	}
		
		return statuses;
	}
	
	private String GET(URI uri) throws LimitReachedException, BadCredentialsException{
		HttpGet request = new HttpGet(uri);
		sign(request);
		
		HttpClient c = HttpClients.createDefault();
		String body = null;
		
		try {
			org.apache.http.HttpResponse response = c.execute(request);
			HttpEntity entity = response.getEntity();
			
			body = Util.toString(entity.getContent());
		
			if (response.getStatusLine().getStatusCode() == 88){
				System.err.println(String.format("Status 88 (rate limited) raised for %s", credentials.username));
				throw new LimitReachedException();
			} else if (response.getStatusLine().getStatusCode() != 200){
				System.err.println(String.format("Problem requesting resource with %s's credentials: %s", credentials.username, body));
				throw new BadCredentialsException();
			}

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return body;
		
	}
	

	
	public static void main(String[] args) throws Exception{
		TwitterRESTClient c = new TwitterRESTClient(Credentials.getCredentials("rafaveguim"));
		System.out.println(c.getAllUserTweets("rafaveguim").get(1));
;//		while (true){
//			JSONArray tweets = c.getAllUserTweets("16319797");
//		}
		
	}
	
}
