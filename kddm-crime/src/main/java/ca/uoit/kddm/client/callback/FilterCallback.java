package ca.uoit.kddm.client.callback;

import ca.uoit.kddm.client.filter.TweetFilter;

public abstract class FilterCallback implements TwitterMessageCallback {
	
	TweetFilter filter;
	int read = 0;
	int filtered = 0;
	
	public FilterCallback(TweetFilter filter) {
		this.filter = filter;
	}
	
	@Override
	public void callback(String msg) {
		read++;
		if (filter == null || filter.filter(msg)){
			filtered++;
			handleMessage(msg);
		}
	}
	
	protected abstract void handleMessage(String msg);

}
