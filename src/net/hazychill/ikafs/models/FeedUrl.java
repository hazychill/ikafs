package net.hazychill.ikafs.models;

import java.util.Date;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;

import com.google.appengine.api.datastore.Key;

@Model
public class FeedUrl {
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

	public void setActive(boolean updated) {
		this.active = updated;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updatedTime) {
		this.updated = updatedTime;
	}

}
