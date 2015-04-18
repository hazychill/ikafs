package net.hazychill.ikafs.models;

import java.util.Date;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;

import com.google.appengine.api.datastore.Key;

@Model
public class FeedChannelRelation {
	@Attribute(primaryKey = true)
	Key key;
	String teamChannel;
	String url;
	boolean posted;
	Date postedTime;

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public String getTeamChannel() {
		return teamChannel;
	}

	public void setTeamChannel(String teamChannel) {
		this.teamChannel = teamChannel;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isPosted() {
		return posted;
	}

	public void setPosted(boolean posted) {
		this.posted = posted;
	}

	public Date getPostedTime() {
		return postedTime;
	}

	public void setPostedTime(Date postedTime) {
		this.postedTime = postedTime;
	}

}
