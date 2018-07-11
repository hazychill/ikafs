package net.hazychill.ikafs.webhooks;

import com.google.gson.annotations.SerializedName;

public class MessageAttachmentField {
	private String title;
	private String value;
	@SerializedName("short")
	private Boolean shortField;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getShort() {
		return shortField;
	}

	public void setShort(Boolean shortField) {
		this.shortField = shortField;
	}
}
