package ca.uoit.kddm.client.filter;

import java.util.List;

import scala.actors.threadpool.Arrays;

public class CompoundFilter implements TweetFilter {
	
	List<TweetFilter> filters;
	
	public CompoundFilter(List<TweetFilter> filters) {
		this.filters = filters;
	}
	
	public CompoundFilter(TweetFilter... filters) {
		this.filters = Arrays.asList(filters);
	}

	@Override
	public boolean filter(String msg) {
		for (TweetFilter f : this.filters)
			if (!f.filter(msg))
				return false;
			
		return true;
	}
	
	
}
