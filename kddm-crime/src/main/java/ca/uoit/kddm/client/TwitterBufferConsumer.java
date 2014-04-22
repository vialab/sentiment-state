package ca.uoit.kddm.client;

import java.io.FileNotFoundException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;

import utils.Util;
import ca.uoit.kddm.auth.Credentials;
import ca.uoit.kddm.client.callback.TwitterMessageCallback;


/**
 * This class consumes a queue of user ids, requesting the public tweets
 * of each user and firing a callback at every response from twitter.
 * This class is designed to run in a separate thread.
 * @author rafa
 *
 */
public class TwitterBufferConsumer implements Runnable{

	TwitterRESTClient client;
	Queue<Long> userIds;
	TwitterMessageCallback callback;
	
	public TwitterBufferConsumer(TwitterRESTClient client,
			Queue<Long> userIds, TwitterMessageCallback callback) {
		this.client = client;
		this.userIds = userIds;
		this.callback = callback;
	}
	
	public TwitterBufferConsumer(TwitterCredentials cred,
			Queue<Long> userIds, TwitterMessageCallback callback) {
		this(new TwitterRESTClient(cred), userIds, callback);
	}

	private Long waitID(){
		Long id = null;
		while ((id = userIds.poll()) == null){
			try {
				Thread.sleep(1000); // sleep for 1 second, then try it again
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
		return id;
	}
	
	private void deliverTweets(JSONArray tweets){
		for (int i = 0; i < tweets.length(); i++) {
			try {
				this.callback.callback(tweets.getJSONObject(i).toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Begin consuming the ids queue. Stops only when
	 * the queue is empty. It avoids rate limiting by suspending
	 * the requests until a new rate limiting window begins.
	 */
	@Override
	public void run() {
		Long id = userIds.poll();
		
		while (id != null || (id = waitID()) != null){
			try {
				JSONArray tweets = client.getAllUserTweets(id);
				if (tweets != null)
					this.callback.callback(tweets.toString());
				id = userIds.poll();
			} catch (LimitReachedException e) {
				// limit reached, wait for a certain time, then try the same id in the next iteration
				try {
					System.err.println(client.credentials.username + " suspended for "+ e.getSecondsUntilReset() + "s to avoid rate limit...");
					Thread.sleep((e.getSecondsUntilReset()+2)*1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (BadCredentialsException e) {
				// bad credentials? What? try again!
				e.printStackTrace();
			}
		}
	}
	
	
	
	public static void main(String[] args) throws FileNotFoundException, InterruptedException{
		LinkedBlockingQueue<Long> queue = new LinkedBlockingQueue<>();
		Long[] ids = {336740135L, 99161388L, 341497625L, 381878960L, 23625127L, 61965963L, 1198450562L, 748023582L,56211316L,212087370L,274255683L,104324287L,463291828L,99161388L,274255683L,454851514L,76230555L, 610173041L,702037208L,291532946L};
		//Long[] ids = {};
		for (Long long1 : ids) {
			queue.offer(long1);
		}
		TwitterRESTClient c = new TwitterRESTClient(Credentials.getCredentials("rafadevrafa"));
		
		TwitterBufferConsumer consumer = new TwitterBufferConsumer(c, queue, new TwitterMessageCallback() {
			@Override
			public void callback(String msg) {
				
			}
		});
		
		new Thread(consumer).start();
	
//		Thread.sleep(10000);
//		queue.offer(341497625L);
		
	}
}
