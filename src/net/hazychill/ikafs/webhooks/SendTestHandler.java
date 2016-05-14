package net.hazychill.ikafs.webhooks;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.IkafsServletException;
import net.hazychill.ikafs.models.SlackTeam;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.gson.Gson;

public class SendTestHandler implements IkafsRequestHandler {
	
	Gson gson = new Gson();

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);
		try {
			boolean testMessageSent = false;
			String method = req.getMethod();
			String team = req.getParameter(IkafsConstants.REQUEST_PARAM_NAME_TEAM_NAME);
			String jsonPayloadStr = req.getParameter(IkafsConstants.REQUEST_PARAM_NAME_JSON_PAYLOAD);
			if ((IkafsConstants.HTTP_METHOD_POST.equals(method)) && (team != null && team.length() > 0) && (jsonPayloadStr != null && jsonPayloadStr.length() > 0)) {
				SendRequestPack pack = new SendRequestPack();
				pack.setDestination(team);
				
				IncomingWebhookPayload payload = gson.fromJson(jsonPayloadStr, IncomingWebhookPayload.class);
				pack.setMessage(payload);
				
				String cacheKey = UUID.randomUUID().toString();
				String jsonStr = gson.toJson(pack);
				logger.info("test message: " + jsonStr);
				MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
				String packJson = gson.toJson(pack);
				ms.put(cacheKey, packJson);

				Queue queue = QueueFactory.getQueue(IkafsConstants.QUEUE_NAME_DEFAULT);
				String queueUrl = req.getServletPath() + IkafsConstants.PATH_WEBHOOK_TASK_PUSH;
				queue.add(TaskOptions.Builder.withUrl(queueUrl).method(Method.POST).param(IkafsConstants.QUEUE_PARAM_NAME_CACHE_KEY, cacheKey));

				testMessageSent = true;
			}

			List<Key> keyList = Datastore.query(SlackTeam.class).asKeyList();

			PrintWriter writer = resp.getWriter();
			writer.write("<html><head><title>Send Test Message</title></head><body>");
			if (testMessageSent) {
				writer.write("<div>Test message sent.</div>");
			}
			writer.write("<form method=\"POST\"><table>");
			writer.write("<tr><td>Team name</td><td><select name=\"" + IkafsConstants.REQUEST_PARAM_NAME_TEAM_NAME + "\">");
			for (Key key : keyList) {
				String teamName = key.getName();
				writer.write("<option value=\"" + teamName + "\">" + teamName + "</option>");
			}
			writer.write("</select></td></tr>");
			writer.write("<tr><td>Message json</td><td><textarea style=\"width:300px; height:100px;\" name=\"" + IkafsConstants.REQUEST_PARAM_NAME_JSON_PAYLOAD + "\"></textarea></td></tr>");
			writer.write("</table>");
			writer.write("<input type=\"submit\" name=\"submit\" />");
			writer.write("</form>");
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
		finally {
		}
	}

}
