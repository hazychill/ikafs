package net.hazychill.ikafs.webhooks;

public class IncomingWebhookPayload {
	private String channel;
	private String username;
	private String icon_emoji;
	private String text;
	MessageAttachment[] attachments;

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getIcon_emoji() {
		return icon_emoji;
	}

	public void setIcon_emoji(String icon_emoji) {
		this.icon_emoji = icon_emoji;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public MessageAttachment[] getAttachments() {
		return attachments;
	}

	public void setAttachments(MessageAttachment[] attachments) {
		this.attachments = attachments;
	}
}
