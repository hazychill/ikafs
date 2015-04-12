package net.hazychill.ikafs.webhooks;

import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.models.MessageSpec;
import net.hazychill.ikafs.models.MessageSpecMeta;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class InitiateSendHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) {
		String sendGroup = req.getParameter(IkafsConstants.QUEUE_PARAM_NAME_SEND_GROUP);
		MessageSpecMeta meta = MessageSpecMeta.get();
		List<MessageSpec> list = Datastore.query(meta)
				.filter(meta.sendGroup.equal(sendGroup), meta.sendStatus.equal(IkafsConstants.MESSAGE_SPEC_SEND_STATUS_UNSENT))
				.asList();

		for (MessageSpec spec : list) {
			spec.setSendStatus(IkafsConstants.MESSAGE_SPEC_SEND_STATUS_SENDING);
		}
		List<Key> keys = Datastore.put(list);

		int i = 0;
		for (MessageSpec spec : list) {
			Queue queue = QueueFactory.getQueue(IkafsConstants.QUEUE_NAME_DEFAULT);
			String queueUrl = req.getServletPath() + IkafsConstants.PATH_WEBHOOK_TASK_SEND;
			Key key = keys.get(i);
			queue.add(TaskOptions.Builder.withUrl(queueUrl).param(IkafsConstants.QUEUE_PARAM_NAME_MESSAGE_KEY, Long.toString(key.getId())));
			i++;
		}
	}

}
