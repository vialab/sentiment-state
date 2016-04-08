package ca.uoit.kddm.client;

import java.util.HashMap;

public class TwitterCredentials {

	String consumerKey;
	String consumerSecret;
	String token;
	String tokenSecret;
	String username;
	
	public TwitterCredentials(String username, String consumerKey, String consumerSecret,
			String token, String tokenSecret) {
		super();
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.token = token;
		this.tokenSecret = tokenSecret;
		this.username = username;
	}
	
	public TwitterCredentials(HashMap<String, String> credentials) {
		this(credentials.get("username"), credentials.get("consumerKey"), 
				credentials.get("consumerSecret"), 
				credentials.get("token"), 
				credentials.get("tokenSecret"));
	}
	
}
