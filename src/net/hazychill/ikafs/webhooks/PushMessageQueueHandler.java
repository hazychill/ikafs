package net.hazychill.ikafs.webhooks;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.IkafsServletException;
import net.hazychill.ikafs.models.MessageSpec;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class PushMessageQueueHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);
		try {
			String cacheKey = req.getParameter(IkafsConstants.QUEUE_PARAM_NAME_CACHE_KEY);
			String sendGroup = req.getParameter(IkafsConstants.QUEUE_PARAM_NAME_SEND_GROUP);
			MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
			String jsonObjStr = (String) ms.get(cacheKey);
			JSONObject jsonObj = new JSONObject(jsonObjStr);
			String destinationTeam = jsonObj.getString(IkafsConstants.JSON_KEY_DESCTINATION);
			JSONObject jsonPayload = jsonObj.getJSONObject(IkafsConstants.JSON_KEY_MESSAGE);
			String jsonPayloadText = jsonPayload.toString();

			boolean doSend = false;
			if (sendGroup == null || sendGroup.length() == 0) {
				sendGroup = UUID.randomUUID().toString();
				doSend = true;
			}

			MessageSpec messageSpec = new MessageSpec();
			messageSpec.setCreated(new Date());
			messageSpec.setDestinationTeam(destinationTeam);
			messageSpec.setJsonPayload(new Text(jsonPayloadText));
			messageSpec.setSendGroup(sendGroup);
			messageSpec.setSendStatus(IkafsConstants.MESSAGE_SPEC_SEND_STATUS_UNSENT);

			Datastore.put(messageSpec);

			if (doSend) {
				Queue queue = QueueFactory.getQueue(IkafsConstants.QUEUE_NAME_DEFAULT);
				String queueUrl = req.getServletPath() + IkafsConstants.PATH_WEBHOOK_TASK_INIT_SEND;
				queue.add(TaskOptions.Builder.withUrl(queueUrl).param(IkafsConstants.QUEUE_PARAM_NAME_SEND_GROUP, sendGroup));
			}

			resp.setStatus(200);
			resp.getWriter().write("OK");
		}
		catch (JSONException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.toString());
		}
		catch (IOException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.toString());
			throw new IkafsServletException(null, e);
		}
	}

}
