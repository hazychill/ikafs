package net.hazychill.ikafs.webfeed;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.IkafsServletException;
import net.hazychill.ikafs.models.FeedUrl;
import net.hazychill.ikafs.models.FeedUrlMeta;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class InitDownloadFeedHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);

		try {
			List<FeedUrl> urlList = Datastore.query(FeedUrl.class).filter(FeedUrlMeta.get().active.equal(true)).asList();

			if (urlList.size() == 0) {
				logger.info("InitDownloadFeedHandler: no feed to download");
				resp.setStatus(IkafsConstants.STATUS_CODE_OK);
				resp.getWriter().write("OK");
				return;
			}

			for (FeedUrl feedUrl : urlList) {
				Queue queue = QueueFactory.getQueue(IkafsConstants.QUEUE_NAME_DEFAULT);
				String queueUrl = req.getServletPath() + IkafsConstants.PATH_WEBFEED_DOWNLOAD_FEED;
				queue.add(TaskOptions.Builder.withUrl(queueUrl).param(IkafsConstants.QUEUE_PARAM_NAME_URL, feedUrl.getKey().getName()));
			}

			resp.setStatus(IkafsConstants.STATUS_CODE_OK);
			resp.getWriter().write("OK");
		}
		catch (IOException e) {
			e.printStackTrace();
			logger.severe(e.toString());
			throw new IkafsServletException(null, e);
		}
	}
}
