package ca.uoit.kddm.mining.entity;

import java.util.Date;
import java.util.Map;

public class LightweightTweet {
	
	Long id;
	String msg;
	Date created_at;
	
	public static LightweightTweet createInstance(Map tweet){
		LightweightTweet t = new LightweightTweet();
		
		t.setCreated_at((Date)tweet.get("created_at"));
		t.setId(((Number)tweet.get("id")).longValue());
		t.setMsg((String)tweet.get("text"));
		
		return t;
	}

	public Date getCreated_at() { return created_at;}
	public Long getId() { return id;}
	public String getMsg() { return msg; }
	
	public void setCreated_at(Date created_at) { this.created_at = created_at;}
	public void setId(Long id) { this.id = id; }
	public void setMsg(String msg) { this.msg = msg;}
	
	public LightweightTweet() {
		
	}

	@Override
	public String toString() {
		return "LightweightTweet [id=" + id + ", msg=" + msg + ", created_at="
				+ created_at + "]";
	}
	
	

}
