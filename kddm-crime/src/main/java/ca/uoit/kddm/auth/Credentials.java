package ca.uoit.kddm.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;

import ca.uoit.kddm.client.TwitterCredentials;

public class Credentials {

	public static TwitterCredentials getCredentials(String path, String user) throws FileNotFoundException{
		Scanner credScanner = new Scanner(new File(String.format("%s/%s", path, user)));
		credScanner.useDelimiter("[:\n]");
		HashMap<String, String> credentials = new HashMap<>();
		while (credScanner.hasNext()){
			String key   = credScanner.next().trim();
			String value = credScanner.next().trim();
			credentials.put(key, value);
		}
		credScanner.close();

		credentials.put("username", user);
		return new TwitterCredentials(credentials);
	}
	
	public static TwitterCredentials getCredentials(String user) throws FileNotFoundException{
		Scanner credScanner = new Scanner(new File(user));
		credScanner.useDelimiter("[:\n]");
		HashMap<String, String> credentials = new HashMap<>();
		while (credScanner.hasNext()){
			String key   = credScanner.next().trim();
			String value = credScanner.next().trim();
			credentials.put(key, value);
		}
		credScanner.close();

		credentials.put("username", user);
		return new TwitterCredentials(credentials);
	}

	public static TwitterCredentials getCredentials(InputStream rsc) {
		Scanner credScanner = new Scanner(new InputStreamReader(rsc));
		credScanner.useDelimiter("[:\n]");
		HashMap<String, String> credentials = new HashMap<>();
		while (credScanner.hasNext()){
			String key   = credScanner.next().trim();
			String value = credScanner.next().trim();
			credentials.put(key, value);
		}
		credScanner.close();

		credentials.put("username", rsc.toString());
		return new TwitterCredentials(credentials);
	}
	
	public static TwitterCredentials getCredentials() {
		HashMap<String, String> credentials = new HashMap<String, String>();
		credentials.put("username", System.getenv("SS_USERNAME"));
		credentials.put("consumerKey", System.getenv("SS_CONSUMER_KEY"));
		credentials.put("consumerSecret", System.getenv("SS_CONSUMER_SEC"));
		credentials.put("token", System.getenv("SS_TOKEN"));
		credentials.put("tokenSecret", System.getenv("SS_TOKEN_SEC"));
		
		return new TwitterCredentials(credentials);
	}
}
