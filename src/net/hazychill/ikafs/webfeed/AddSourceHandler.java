package net.hazychill.ikafs.webfeed;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.IkafsServletException;
import net.hazychill.ikafs.models.FeedSourceSheet;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Key;

public class AddSourceHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);
		try {
			String method = req.getMethod();
			String fileId = req.getParameter(IkafsConstants.REQUEST_PARAM_NAME_WEBFEED_SOURCE_FILE_ID);
			String comment = req.getParameter(IkafsConstants.REQUEST_PARAM_NAME_WEBFEED_SOURCE_COMMENT);

			boolean updated = false;

			if (IkafsConstants.HTTP_METHOD_POST.equals(method) && fileId != null && fileId.length() > 0 && comment != null && comment.length() > 0) {
				Key key = Datastore.createKey(FeedSourceSheet.class, fileId);
				FeedSourceSheet source = new FeedSourceSheet();
				source.setKey(key);
				source.setComment(comment);
				Datastore.put(source);
				updated = true;
			}

			resp.setStatus(IkafsConstants.STATUS_CODE_OK);
			resp.setContentType(IkafsConstants.HTTP_HEADER_VALUE_CONTENT_TYPE_TEXT_HTML);

			PrintWriter writer = resp.getWriter();
			writer.write("<html><head><title>Add WebFeed Source</title></head><body>");
			if (updated) {
				writer.write("<div>Feed source successfully updated.</div>");
			}
			writer.write("<form method=\"POST\"><table>");
			writer.write("<tr><td>File ID</td><td><input type=\"text\" name=\"" + IkafsConstants.REQUEST_PARAM_NAME_WEBFEED_SOURCE_FILE_ID + "\" /></td></tr>");
			writer.write("<tr><td>Comment</td><td><input type=\"text\" name=\"" + IkafsConstants.REQUEST_PARAM_NAME_WEBFEED_SOURCE_COMMENT + "\" /></td></tr>");
			writer.write("</table>");
			writer.write("<input type=\"submit\" name=\"submit\" />");
			writer.write("<hr />");

			List<FeedSourceSheet> sources = Datastore.query(FeedSourceSheet.class).asList();
			if (sources.size() > 0) {
				writer.write("<table style=\"border-collapse: collapse;\">");
				for (FeedSourceSheet source : sources) {
					writer.write("<tr><td style=\"border: 1px solid #999999;\">");
					writer.write(source.getKey().getName());
					writer.write("</td><td style=\"border: 1px solid #999999;\">");
					writer.write(source.getComment());
					writer.write("</td></tr>");
				}
				writer.write("</table>");
			}
		}
		catch (IOException e) {
			throw new IkafsServletException(null, e);
		}
	}

}
