package net.hazychill.ikafs.webhooks;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.IkafsServletException;
import net.hazychill.ikafs.misc.ConfigManager;
import net.hazychill.ikafs.models.MessageSpec;
import net.hazychill.ikafs.models.SlackTeam;

import org.slim3.datastore.Datastore;
import org.slim3.datastore.EntityNotFoundRuntimeException;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class SendMessageHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);

		String messageKeyStr = req.getParameter(IkafsConstants.QUEUE_PARAM_NAME_MESSAGE_KEY);
		long id = Long.parseLong(messageKeyStr);
		Key messageKey = Datastore.createKey(MessageSpec.class, id);
		MessageSpec spec = Datastore.get(MessageSpec.class, messageKey);

		OutputStream output = null;
		OutputStreamWriter writer = null;
		try {
			Key teamKey = Datastore.createKey(SlackTeam.class, spec.getDestinationTeam());
			SlackTeam team = Datastore.get(SlackTeam.class, teamKey);

			String webhookUrl = team.getWebhookUrl();
			String jsonPayload = spec.getJsonPayload().getValue();

			ConfigManager configManager = new ConfigManager();
			int urlfetchDeadlineSeconds = configManager.getInt(IkafsConstants.CONFIG_KEY_URLFETCH_DEADLINE_SECONDS);

			jsonPayload = truncateUsername(jsonPayload, configManager, logger);

			FetchOptions options = FetchOptions.Builder.withDeadline(urlfetchDeadlineSeconds);
			URL requestUrl = new URL(webhookUrl);
			HTTPRequest request = new HTTPRequest(requestUrl, HTTPMethod.POST, options);
			byte[] payload = jsonPayload.getBytes(IkafsConstants.CHARSET_UTF8);
			request.setHeader(new HTTPHeader(IkafsConstants.HTTP_HEADER_NAME_CONTENT_TYPE, IkafsConstants.MIME_TYPE_JSON));
			request.setPayload(payload);

			URLFetchService urlFetch = URLFetchServiceFactory.getURLFetchService();
			HTTPResponse response = urlFetch.fetch(request);

			int status = response.getResponseCode();
			if (200 <= status && status <= 299) {
				spec.setSendStatus(IkafsConstants.MESSAGE_SPEC_SEND_STATUS_SENT);
				Datastore.put(spec);

				resp.setStatus(IkafsConstants.STATUS_CODE_OK);
				resp.getWriter().write("OK");
			}
			else {
				resp.setStatus(500);
			}
		}
		catch (EntityNotFoundRuntimeException e) {
			logger.log(Level.SEVERE, "Message to send not found: ", +id);
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (output != null) {
				try {
					output.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String truncateUsername(String jsonPayload, ConfigManager configManager, Logger logger) throws IkafsServletException {
		try {
			int maxUsernameBytes = configManager.getInt(IkafsConstants.CONFIG_KEY_MAX_USERNAME_BYTES);
			JSONObject messageJson = new JSONObject(jsonPayload);
			String origUsername = messageJson.getString(IkafsConstants.JSON_KEY_USERNAME);

			if (origUsername == null || origUsername.length() == 0) {
				return jsonPayload;
			}

			String newUsername;
			if (origUsername.length() > maxUsernameBytes) {
				newUsername = origUsername.substring(0, maxUsernameBytes);
			}
			else {
				newUsername = origUsername;
			}

			while (true) {
				byte[] bytes = newUsername.getBytes(IkafsConstants.CHARSET_UTF8);
				if (bytes.length <= maxUsernameBytes) {
					break;
				}
				else {
					if (newUsername.length() > 0) {
						newUsername = newUsername.substring(0, newUsername.length()-1);
					}
					else {
						break;
					}
				}
			}

			if (!origUsername.equals(newUsername)) {
				logger.info("username truncated from \"" + origUsername + "\" to \"" + newUsername + "\"");
				messageJson.remove(IkafsConstants.JSON_KEY_USERNAME);
				messageJson.put(IkafsConstants.JSON_KEY_USERNAME, newUsername);
				return messageJson.toString();
			}
			else {
				return jsonPayload;
			}

		}
		catch (JSONException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
		finally {}
	}

}
