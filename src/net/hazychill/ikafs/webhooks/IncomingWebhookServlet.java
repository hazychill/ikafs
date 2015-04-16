package net.hazychill.ikafs.webhooks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;

public class IncomingWebhookServlet extends HttpServlet {
	private Map<String, IkafsRequestHandler> handlers;
	
	public IncomingWebhookServlet() {
		handlers = new HashMap<String, IkafsRequestHandler>();
		handlers.put(IkafsConstants.PATH_WEBHOOK_REQUEST, new ReceiveMessageRequestHandler());
		handlers.put(IkafsConstants.PATH_WEBHOOK_TASK_PUSH, new PushMessageQueueHandler());
		handlers.put(IkafsConstants.PATH_WEBHOOK_TASK_INIT_SEND, new InitiateSendHandler());
		handlers.put(IkafsConstants.PATH_WEBHOOK_TASK_SEND, new SendMessageHandler());
		handlers.put(IkafsConstants.PATH_WEBHOOK_ADD_TEAM, new AddTeamHandler());
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String path = req.getPathInfo();
		if (IkafsConstants.PATH_WEBHOOK_ADD_TEAM.equals(path)) {
			if (handlers.containsKey(path)) {
				IkafsRequestHandler handler = handlers.get(path);
				handler.handle(req, resp, this);
			}
			else {
				resp.setStatus(IkafsConstants.STATUS_CODE_METHOD_NOT_ALLOWED);
			}
		}
		else {
			resp.setStatus(IkafsConstants.STATUS_CODE_METHOD_NOT_ALLOWED);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
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
