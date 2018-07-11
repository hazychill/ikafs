package net.hazychill.ikafs.models;

import java.util.Date;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;

@Model(schemaVersion = 1)
public class MessageSpec {
	@Attribute(primaryKey = true)
	private Key key;
	private Date created;
	private String sendGroup;
	private int sendStatus;
	@Attribute(unindexed = true)
	private String destinationTeam;
	@Attribute(unindexed = true)
	private Text jsonPayload;

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getSendGroup() {
		return sendGroup;
	}

	public void setSendGroup(String sendGroup) {
		this.sendGroup = sendGroup;
	}

	public int getSendStatus() {
		return sendStatus;
	}

	public void setSendStatus(int sendStatus) {
		this.sendStatus = sendStatus;
	}

	public String getDestinationTeam() {
		return destinationTeam;
	}

	public void setDestinationTeam(String destinationTeam) {
		this.destinationTeam = destinationTeam;
	}

	public Text getJsonPayload() {
		return jsonPayload;
	}

	public void setJsonPayload(Text jsonPayload) {
		this.jsonPayload = jsonPayload;
	}

}
