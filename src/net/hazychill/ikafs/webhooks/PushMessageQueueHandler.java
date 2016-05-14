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
import com.google.gson.Gson;

public class PushMessageQueueHandler implements IkafsRequestHandler {
	
	private static Gson gson = new Gson();

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);
		try {
			String cacheKey = req.getParameter(IkafsConstants.QUEUE_PARAM_NAME_CACHE_KEY);
			String sendGroup = req.getParameter(IkafsConstants.QUEUE_PARAM_NAME_SEND_GROUP);
			MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
			String packJson = (String)ms.get(cacheKey);
			SendRequestPack pack = gson.fromJson(packJson, SendRequestPack.class);
			String destinationTeam = pack.getDestination();
			IncomingWebhookPayload payload = pack.getMessage();
			String jsonPayloadText = gson.toJson(payload);

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
		catch (IOException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.toString());
			throw new IkafsServletException(null, e);
		}
	}

}
