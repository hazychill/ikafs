package net.hazychill.ikafs.webfeed;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.models.FeedChannelRelation;
import net.hazychill.ikafs.models.FeedChannelRelationMeta;
import net.hazychill.ikafs.models.FeedEntry;
import net.hazychill.ikafs.models.FeedEntryMeta;
import net.hazychill.ikafs.webhooks.IkafsServletException;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class RequestPostMessageHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);
		try {
			List<FeedChannelRelation> fcrList = Datastore.query(FeedChannelRelation.class).filter(FeedChannelRelationMeta.get().active.equal(true)).asList();

			if (fcrList.size() == 0) {
				logger.info("RequestPostMessageHandler: no FeedChannelRelation");
				resp.setStatus(200);
				resp.getWriter().write("OK");
				return;
			}

			Map<String, JSONObject> entryJsonMap = new HashMap<String, JSONObject>();

			List<Object> models = new ArrayList<Object>();

			boolean error = false;

			for (FeedChannelRelation fcr : fcrList) {
				try {
					Date lastUpdated = fcr.getUpdated();
					String feedUrl = fcr.getKey().getParent().getName();
					FeedEntryMeta meta = FeedEntryMeta.get();
					List<FeedEntry> entries = Datastore.query(FeedEntry.class).filter(meta.feedUrl.equal(feedUrl), meta.updated.greaterThan(fcr.getUpdated())).asList();

					if (entries.size() == 0) {
						logger.info("RequestPostMessageHandler: no updates for " + fcr.getKey().getName() + ", " + fcr.getKey().getParent().getName());
						fcr.setActive(false);
						Datastore.put(fcr);
						continue;
					}

					String teamChannel = fcr.getKey().getName();
					String[] temp = teamChannel.split("#");
					String team = temp[0];
					String channel = "#" + temp[1];

					MemcacheService ms = MemcacheServiceFactory.getMemcacheService();

					for (FeedEntry entry : entries) {
						if (lastUpdated.compareTo(entry.getUpdated()) < 0) {
							lastUpdated = entry.getUpdated();
						}
						JSONObject entryJson;
						String link = entry.getKey().getName();
						if (entryJsonMap.containsKey(link)) {
							JSONObject original = entryJsonMap.get(link);
							entryJson = new JSONObject(original.toString());
							entryJson.put(IkafsConstants.JSON_KEY_DESCTINATION, team);
							((JSONObject) entryJson.get(IkafsConstants.JSON_KEY_MESSAGE)).put(IkafsConstants.JSON_KEY_CHANNEL, channel);
						}
						else {
							entryJson = createEntryJson(entry, team, channel);
							entryJsonMap.put(link, entryJson);
						}

						String cacheKey = UUID.randomUUID().toString();
						ms.put(cacheKey, entryJson.toString());

						Queue queue = QueueFactory.getQueue(IkafsConstants.QUEUE_NAME_DEFAULT);
						String queueUrl = IkafsConstants.PATH_SERVLET_CONTEXT_WEBHOOK + IkafsConstants.PATH_WEBHOOK_TASK_PUSH;
						queue.add(TaskOptions.Builder.withUrl(queueUrl).method(Method.POST).param(IkafsConstants.QUEUE_PARAM_NAME_CACHE_KEY, cacheKey)
								.param(IkafsConstants.QUEUE_PARAM_NAME_SEND_GROUP, IkafsConstants.SEND_GROUP_FEED_ENTRIES));
					}

					fcr.setUpdated(lastUpdated);
					fcr.setActive(false);
					Datastore.put(fcr);
				}
				catch (Exception e) {
					error = true;
					logger.log(Level.SEVERE, "error", e);
				}
			}

			if (error) {
				resp.setStatus(IkafsConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
			}
			else {
				resp.setStatus(IkafsConstants.STATUS_CODE_OK);
				resp.getWriter().write("OK");
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
	}

	private JSONObject createEntryJson(FeedEntry entry, String team, String channel) throws JSONException {
		JSONObject entryJson = new JSONObject();
		entryJson.put(IkafsConstants.JSON_KEY_DESCTINATION, team);

		JSONObject messageJson = new JSONObject();
		entryJson.put(IkafsConstants.JSON_KEY_MESSAGE, messageJson);
		messageJson.put(IkafsConstants.JSON_KEY_CHANNEL, channel);
		messageJson.put(IkafsConstants.JSON_KEY_USERNAME, entry.getFeedTitle());
		messageJson.put(IkafsConstants.JSON_KEY_ICONEMOJI, IkafsConstants.JSON_VALUE_ICONEMOJI_WEBFEED);

		JSONArray attachments = new JSONArray();
		messageJson.put(IkafsConstants.JSON_KEY_ATTACHMENTS, attachments);

		JSONObject attachmentBody = new JSONObject();
		attachments.put(attachmentBody);
		String entryLink = entry.getKey().getName();
		String fallback = MessageFormat.format(IkafsConstants.MESSAGE_FORMAT_FALLBACK, entryLink, entry.getTitle());
		attachmentBody.put(IkafsConstants.JSON_KEY_FALLBACK, fallback);
		attachmentBody.put(IkafsConstants.JSON_KEY_TITLE, entry.getTitle());
		attachmentBody.put(IkafsConstants.JSON_KEY_TITLELINK, entryLink);
		attachmentBody.put(IkafsConstants.JSON_KEY_TEXT, entry.getSummary());
		String image = entry.getImage();
		if (image != null && image.length() > 0) {
			attachmentBody.put(IkafsConstants.JSON_KEY_IMAGEURL, image);
		}

		return entryJson;
	}

}
