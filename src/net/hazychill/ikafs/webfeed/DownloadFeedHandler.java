package net.hazychill.ikafs.webfeed;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.IkafsServletException;
import net.hazychill.ikafs.models.FeedEntry;
import net.hazychill.ikafs.models.FeedUrl;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Key;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class DownloadFeedHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);

		InputStream input = null;
		Reader reader = null;
		try {
			String urlParam = req.getParameter(IkafsConstants.QUEUE_PARAM_NAME_URL);
			if (urlParam == null || urlParam.length() == 0) {
				logger.severe("DownloadFeedHandler: no query parameter");
				resp.setStatus(500);
				return;
			}

			URL url = new URL(urlParam);
			input = url.openStream();
			reader = new XmlReader(input);
			SyndFeedInput feedInput = new SyndFeedInput();
			SyndFeed feed = feedInput.build(reader);

			String feedTitle = feed.getTitle();

			List<Object> modelList = new ArrayList<Object>();

			for (Object entryObj : feed.getEntries()) {
				SyndEntry entry = (SyndEntry) entryObj;
				FeedEntry entryModel = readEntry(entry, urlParam, feedTitle);
				modelList.add(entryModel);
			}

			FeedUrl feedUrl = new FeedUrl();
			feedUrl.setKey(Datastore.createKey(FeedUrl.class, urlParam));
			feedUrl.setActive(false);
			feedUrl.setUpdated(new Date());
			modelList.add(feedUrl);

			Datastore.put(modelList);
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
		catch (FeedException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (input != null) {
				try {
					input.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private FeedEntry readEntry(SyndEntry entry, String feedUrl, String feedTitle) {
		int maxSummaryLength = 200;

		String link;
		String title;
		Date updated;
		String summary;
		String image;

		link = entry.getLink();

		title = entry.getTitle();

		updated = entry.getUpdatedDate();
		if (updated == null) {
			updated = entry.getPublishedDate();
		}

		SyndContent content = null;
		if (entry.getContents().size() > 0) {
			content = (SyndContent) entry.getContents().get(0);
		}
		else {
			content = entry.getDescription();
		}
		if (content != null) {
			if (content.getType() == null || content.getType().startsWith("text/plain")) {
				summary = content.getValue();
			}
			else {
				summary = content.getValue().replaceAll("(?s)<.+?>", " ").replaceAll(" +", " ");
				summary = StringEscapeUtils.unescapeHtml4(summary);
			}
		}
		else {
			summary = entry.getTitle();
		}
		int summaryLength = (summary.length() < maxSummaryLength) ? (summary.length()) : (maxSummaryLength);
		summary = summary.substring(0, summaryLength);

		if (content != null) {
			String contentValue = content.getValue();
			// <img [^>]*src=["'](.+?)["']
			Pattern pattern = Pattern.compile("<img [^>]*src=[\"\'](.+?)[\"\']");
			Matcher matcher = pattern.matcher(contentValue);
			if (matcher.find()) {
				image = matcher.group(1);
			}
			else {
				image = null;
			}
		}
		else {
			image = null;
		}

		FeedEntry entryModel = new FeedEntry();
		Key key = Datastore.createKey(FeedEntry.class, link);
		entryModel.setKey(key);
		entryModel.setFeedUrl(feedUrl);
		entryModel.setFeedTitle(feedTitle);
		entryModel.setTitle(title);
		entryModel.setUpdated(updated);
		entryModel.setSummary(summary);
		entryModel.setImage(image);

		return entryModel;
	}

}
