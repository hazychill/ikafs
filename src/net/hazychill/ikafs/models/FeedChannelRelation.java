package net.hazychill.ikafs.models;

import java.util.Date;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;

import com.google.appengine.api.datastore.Key;

@Model(schemaVersion = 1)
public class FeedChannelRelation {
	@Attribute(primaryKey = true)
	Key key;
	boolean active;
	Date updated;

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean posted) {
		this.active = posted;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date postedTime) {
		this.updated = postedTime;
	}

}
