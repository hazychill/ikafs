package net.hazychill.ikafs.webhooks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.models.SlackTeam;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class AddTeamHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet)
			throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);

		InputStream propInput = null;
		try {
			String teamName = req.getParameter("teamName");
			String webhookUrl = req.getParameter("webhookUrl");
			Key key = Datastore.createKey(SlackTeam.class, teamName);
			SlackTeam team = new SlackTeam();
			team.setKey(key);
			team.setWebhookUrl(webhookUrl);
			Datastore.put(team);
			
			resp.setStatus(IkafsConstants.STATUS_CODE_OK);
			resp.getWriter().write("OK");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
		finally {
			if (propInput != null) {
				try {
					propInput.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
