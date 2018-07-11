package net.hazychill.ikafs.webhooks;

public class SendRequestPack {
	private String destination;
	private IncomingWebhookPayload message;

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public IncomingWebhookPayload getMessage() {
		return message;
	}

	public void setMessage(IncomingWebhookPayload message) {
		this.message = message;
	}
}
