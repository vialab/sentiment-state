package ca.uoit.science.vialab.vialab;

import static spark.Spark.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import spark.*;
import ca.uoit.kddm.auth.Credentials;
import ca.uoit.kddm.client.BadCredentialsException;
import ca.uoit.kddm.client.LimitReachedException;
import ca.uoit.kddm.client.TwitterCredentials;
import ca.uoit.kddm.client.TwitterRESTClient;

import StringToken.*;

public class TwitterScore {

	public static void main(String[] args) {
		String portStr = System.getenv("PORT");
		
		if (portStr == null) {
			setPort(Integer.valueOf(portStr));
		}

		get(new Route("/check") {
			@Override
			public Object handle(Request request, Response response) {
				return "<h1>Spark is Active!</h1>";
			}
		});
		
		// allows access to the json file
		before(new Filter("/twitter/:name") {
			@Override
			public void handle(Request request, Response response) {
				response.header("Access-Control-Allow-Origin", "*");
			}
		});

		get(new Route("/twitter/:name") {
			@SuppressWarnings("null")
			@Override
			public Object handle(Request request, Response response) {

				DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
				Date date = new Date();
				TwitterRESTClient client;
				TwitterCredentials cred = null;
				
				cred = Credentials.getCredentials();
				client = new TwitterRESTClient(cred);

				String username = request.params(":name");
				CalculateScore cs = null;

				try {
					cs = new CalculateScore();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				List<Integer> score = null;
				JSONArray tweets = null;
				long start = System.currentTimeMillis(); // starts counting
				try {
					tweets = client.getAllUserTweets(username);
					
				} 
				catch (LimitReachedException | BadCredentialsException e) {
					e.printStackTrace();
				}

				for (int i=0; i<tweets.length(); i++) {
					try{
						JSONObject item = tweets.getJSONObject(i);
						score = cs.getScore(item.getString("text"));

						tweets.getJSONObject(i).put("score_positive", score.get(0));
						tweets.getJSONObject(i).put("score_negative", score.get(1));
						tweets.getJSONObject(i).put("score_anger", score.get(2));
						tweets.getJSONObject(i).put("score_anticipation", score.get(3));
						tweets.getJSONObject(i).put("score_disgust", score.get(4));
						tweets.getJSONObject(i).put("score_fear", score.get(5));
						tweets.getJSONObject(i).put("score_joy", score.get(6));
						tweets.getJSONObject(i).put("score_sadness", score.get(7));
						tweets.getJSONObject(i).put("score_surprise", score.get(8));
						tweets.getJSONObject(i).put("score_trust", score.get(9));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}			
				}

				long end = System.currentTimeMillis(); // ends counting
				long dif = (end - start) / 1000;

				// Log in a more Apache-friendly format for easier parsing in the future
				String out = String.format("%s - %s [%s] \"GET /twitter/%s\" " + 
					"\"TIME=%d&NUM_TWEETS=%d&LEXICON_WORDS=%d\"",
					request.ip(), username, dateFormat.format(date), username, dif,
					tweets.length(), CalculateScore.count);
				System.out.println(out);

				return tweets;
			}
		});       
	}
}
