package net.hazychill.ikafs;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.webhooks.IkafsServletException;

public interface IkafsRequestHandler {
	void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException;
}
