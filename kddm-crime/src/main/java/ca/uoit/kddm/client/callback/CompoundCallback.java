package ca.uoit.kddm.client.callback;

import java.util.List;

import scala.actors.threadpool.Arrays;

public class CompoundCallback implements TwitterMessageCallback {

	List<TwitterMessageCallback> callbacks;
	
	public CompoundCallback(List<TwitterMessageCallback> callbacks) {
		this.callbacks = callbacks;
	}
	
	public CompoundCallback(TwitterMessageCallback... callbacks) {
		this(Arrays.asList(callbacks));
	}
	
	@Override
	public void callback(String msg) {
		for (TwitterMessageCallback c : callbacks) {
			c.callback(msg);
		}
	}

}
