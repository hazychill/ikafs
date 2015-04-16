package net.hazychill.ikafs.webhooks;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.models.SlackTeam;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Key;

public class AddTeamHandler implements IkafsRequestHandler {
	
	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet)
			throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);

		InputStream propInput = null;
		try {
			String method = req.getMethod();
			String teamName = req.getParameter("teamName");
			String webhookUrl = req.getParameter("webhookUrl");
			
			boolean added = false;
			
			if (IkafsConstants.HTTP_METHOD_POST.equals(method) &&
					teamName != null && teamName.length() > 0 &&
					webhookUrl != null && webhookUrl.length() > 0) {
				Key key = Datastore.createKey(SlackTeam.class, teamName);
				SlackTeam team = new SlackTeam();
				team.setKey(key);
				team.setWebhookUrl(webhookUrl);
				Datastore.put(team);
				added = true;
			}
			
			resp.setStatus(IkafsConstants.STATUS_CODE_OK);
			resp.setContentType(IkafsConstants.HTTP_HEADER_VALUE_CONTENT_TYPE_TEXT_HTML);

			PrintWriter writer = resp.getWriter();
			writer.write("<html><head><title>Add Team</title></head><body>");
			if (added) {
				writer.write("<div>Team successfully added.</div>");
			}
			writer.write("<form method=\"POST\">");
			writer.write("<table><tr><td>Team name</td><td><input type=\"text\" name=\"" + IkafsConstants.REQUEST_PARAM_NAME_TEAM_NAME + "\" /></td></tr><tr><td>Webhook url</td><td><input type=\"text\" name=\"" + IkafsConstants.REQUEST_PARAM_NAME_WEBHOOK_URL + "\" /></td></tr></table>");
			writer.write("<input type=\"submit\" name=\"submit\" />");
			writer.write("<hr />");
			List<SlackTeam> teams = Datastore.query(SlackTeam.class).asList();
			if (teams.size() > 0) {
				writer.write("<table style=\"border-collapse: collapse;\">");
				for (SlackTeam team : teams) {
					writer.write("<tr><td style=\"border: 1px solid #999999;\">");
					writer.write(team.getKey().getName());
					writer.write("</td><td style=\"border: 1px solid #999999;\">");
					writer.write(team.getWebhookUrl());
					writer.write("</td></tr>");
				}
				writer.write("</table>");
			}
			writer.write("</form></body></html>");
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
