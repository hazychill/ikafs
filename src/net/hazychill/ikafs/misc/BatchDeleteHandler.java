package net.hazychill.ikafs.misc;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.CommonUtils;
import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.IkafsServletException;
import net.hazychill.ikafs.models.FeedEntryMeta;
import net.hazychill.ikafs.models.MessageSpecMeta;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

public class BatchDeleteHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);

		String path = req.getPathInfo();

		if (IkafsConstants.PATH_MISC_BATCH_DELETE.equals(path)) {
			queue(req, resp, logger);
		}
		else if (IkafsConstants.PATH_MISC_EXEC_DELETE.equals(path)) {
			execDelete(resp, logger);
		}
		else {
			resp.setStatus(IkafsConstants.STATUS_CODE_METHOD_NOT_ALLOWED);
		}
	}

	private void queue(HttpServletRequest req, HttpServletResponse resp, Logger logger) throws IkafsServletException {
		Queue queue = QueueFactory.getQueue(IkafsConstants.QUEUE_NAME_DEFAULT);
		String queueUrl = req.getServletPath() + IkafsConstants.PATH_MISC_EXEC_DELETE;
		queue.add(TaskOptions.Builder.withUrl(queueUrl).method(Method.POST));
		logger.info("exec delete task queued");

		resp.setStatus(200);
		try {
			resp.getWriter().write("OK");
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
	}

	private void execDelete(HttpServletResponse resp, Logger logger) throws IkafsServletException {
		ConfigManager configManager = new ConfigManager();

		int expireDays = configManager.getInt(IkafsConstants.CONFIG_KEY_FEED_ENTRY_EXPIRE_DAYS);
		Date oldestUpdatedDate = CommonUtils.calcExpireDate(expireDays);
		String oldestUpdated = formatDate(oldestUpdatedDate);
		logger.info("FeedEntry older than " + oldestUpdated + " will be deleted");
		FeedEntryMeta feedEntryMeta = FeedEntryMeta.get();
		List<Key> feedEntryKeys = Datastore.query(feedEntryMeta).filter(feedEntryMeta.updated.lessThan(oldestUpdatedDate)).asKeyList();
		Datastore.delete(feedEntryKeys);
		for (Key key : feedEntryKeys) {
			logger.info(" FeedEntry deleted: " + key.getName());
		}

		expireDays = configManager.getInt(IkafsConstants.CONFIG_KEY_MESSAGE_SPEC_EXPIRE_DAYS);
		oldestUpdatedDate = CommonUtils.calcExpireDate(expireDays);
		oldestUpdated = formatDate(oldestUpdatedDate);
		logger.info("MessageSpec finished or older than " + oldestUpdated + " fill be deleted");
		MessageSpecMeta messageSpecMeta = MessageSpecMeta.get();
		List<Key> messageSpecKeys = Datastore.query(messageSpecMeta).filter(messageSpecMeta.sendStatus.equal(IkafsConstants.MESSAGE_SPEC_SEND_STATUS_SENT)).asKeyList();
		Datastore.delete(messageSpecKeys);
		for (Key key : messageSpecKeys) {
			logger.info(" MessageSpec deleted: " + key.getId());
		}
		messageSpecKeys = Datastore.query(messageSpecMeta)
				.filter(messageSpecMeta.sendStatus.equal(IkafsConstants.MESSAGE_SPEC_SEND_STATUS_UNSENT), messageSpecMeta.created.lessThan(oldestUpdatedDate)).asKeyList();
		Datastore.delete(messageSpecKeys);
		for (Key key : messageSpecKeys) {
			logger.info(" MessageSpec deleted: " + key.getId());
		}
		messageSpecKeys = Datastore.query(messageSpecMeta)
				.filter(messageSpecMeta.sendStatus.equal(IkafsConstants.MESSAGE_SPEC_SEND_STATUS_SENDING), messageSpecMeta.created.lessThan(oldestUpdatedDate)).asKeyList();
		Datastore.delete(messageSpecKeys);
		for (Key key : messageSpecKeys) {
			logger.info(" MessageSpec deleted: " + key.getId());
		}
		messageSpecKeys = Datastore.query(messageSpecMeta).filter(messageSpecMeta.sendStatus.equal(IkafsConstants.MESSAGE_SPEC_SEND_STATUS_ERROR), messageSpecMeta.created.lessThan(oldestUpdatedDate))
				.asKeyList();
		Datastore.delete(messageSpecKeys);
		for (Key key : messageSpecKeys) {
			logger.info(" MessageSpec deleted: " + key.getId());
		}

		resp.setStatus(200);
		try {
			resp.getWriter().write("OK");
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
	}

	private String formatDate(Date date) {
		return new SimpleDateFormat(IkafsConstants.DATE_FORMAT_ISO).format(date);
	}
}
