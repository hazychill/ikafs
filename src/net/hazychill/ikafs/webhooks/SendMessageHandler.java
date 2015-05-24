package net.hazychill.ikafs.webhooks;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
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
			int connectionTimeout = configManager.getInt(IkafsConstants.CONFIG_KEY_URLCONNECTION_CONNECTION_TIMEOUT);
			int readTimeout = configManager.getInt(IkafsConstants.CONFIG_KEY_URLCONNECTION_READ_TIMEOUT);

			URL requestUrl = new URL(webhookUrl);
			HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod(IkafsConstants.HTTP_METHOD_POST);
			connection.setRequestProperty(IkafsConstants.HTTP_HEADER_NAME_CONTENT_TYPE, IkafsConstants.MIME_TYPE_JSON);
			connection.setConnectTimeout(connectionTimeout);
			connection.setReadTimeout(readTimeout);
			output = connection.getOutputStream();
			writer = new OutputStreamWriter(output, IkafsConstants.CHARSET_UTF8);
			writer.write(jsonPayload);
			writer.flush();
			output.flush();

			int status = connection.getResponseCode();
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

}
