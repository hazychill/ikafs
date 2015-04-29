package net.hazychill.ikafs.misc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;

public class MaintenanceServlet extends HttpServlet {
	private Map<String, IkafsRequestHandler> handlers;

	public MaintenanceServlet() {
		handlers = new HashMap<String, IkafsRequestHandler>();
		handlers.put(IkafsConstants.PATH_MISC_CONFIG, new ConfigHandler());
		handlers.put(IkafsConstants.PATH_MISC_BATCH_DELETE, new BatchDeleteHandler());
		handlers.put(IkafsConstants.PATH_MISC_EXEC_DELETE, new BatchDeleteHandler());
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
