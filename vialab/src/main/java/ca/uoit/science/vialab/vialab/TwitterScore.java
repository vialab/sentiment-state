package ca.uoit.science.vialab.vialab;

import static spark.Spark.*;
import org.slf4j.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

import spark.*;
import ca.uoit.kddm.auth.Credentials;
import ca.uoit.kddm.client.BadCredentialsException;
import ca.uoit.kddm.client.LimitReachedException;
import ca.uoit.kddm.client.TwitterCredentials;
import ca.uoit.kddm.client.TwitterRESTClient;

import StringToken.*;

public class TwitterScore {

	public static void main(String[] args) throws FileNotFoundException{
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

				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");
				Date date = new Date();
				TwitterRESTClient client;
				TwitterCredentials cred = null;
				File file = new File("log.txt");
				InputStream rsc = this.getClass().getResourceAsStream("vialab");
				
				cred = Credentials.getCredentials(rsc);
				client = new TwitterRESTClient(cred);

				String username = request.params(":name");
				CalculateScore cs = null;

				try {
					cs = new CalculateScore();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				List<Integer> score;
				JSONArray tweets = null;
				long start = System.currentTimeMillis(); // starts counting
				try {
					tweets = client.getAllUserTweets(username);
					
				} 
				catch (LimitReachedException | BadCredentialsException e) {
					e.printStackTrace();
				}

				System.out.println("Beginning to calculate tweets...");
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
				long dif = (end - start)/1000;
				System.out.println("Elapsed Time: "+ dif+"s");
				System.out.println(CalculateScore.count + " words compared to the lexicon");
				System.out.println("Done...Sending to client...");
				System.out.println();

				try{
				if (!file.exists())
					file.createNewFile();
							
				PrintWriter out = new PrintWriter(new FileWriter(file, true));
				out.println(dateFormat.format(date));
				out.println("User: "+username);
				out.println("Requested Client IP: "+ request.ip());
				out.println("Duration: "+dif+"s");
				out.println("Number of Tweets: "+tweets.length());
				out.println("Words compared to lexicon: "+ CalculateScore.count);
				out.println("---------------------------------------------------");
				out.close();
				}				
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
				return tweets;
			}
		});       
	}
}
