package net.hazychill.ikafs.webfeed;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;

public class WebFeedServiceServlet extends HttpServlet {
	private Map<String, IkafsRequestHandler> handlers;

	public WebFeedServiceServlet() {
		handlers = new HashMap<String, IkafsRequestHandler>();
		handlers.put(IkafsConstants.PATH_WEBFEED_FIND_SOURCES, new FindSourcesHandler());
		handlers.put(IkafsConstants.PATH_WEBFEED_INIT_DOWNLOAD_FEED, new InitDownloadFeedHandler());
		handlers.put(IkafsConstants.PATH_WEBFEED_DOWNLOAD_FEED, new DownloadFeedHandler());
		handlers.put(IkafsConstants.PATH_WEBFEED_REQUEST_POST_MESSAGE, new RequestPostMessageHandler());
		handlers.put(IkafsConstants.PATH_WEBFEED_ADD_SOURCE, new AddSourceHandler());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getPathInfo();

		if (handlers.containsKey(path)) {
			IkafsRequestHandler handler = handlers.get(path);
			handler.handle(req, resp, this);
		}
		else {
			resp.setStatus(IkafsConstants.STATUS_CODE_METHOD_NOT_ALLOWED);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getPathInfo();

		if (handlers.containsKey(path)) {
			IkafsRequestHandler handler = handlers.get(path);
			handler.handle(req, resp, this);
		}
		else {
			resp.setStatus(IkafsConstants.STATUS_CODE_METHOD_NOT_ALLOWED);
		}
	}
}
