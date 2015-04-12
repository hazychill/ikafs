package net.hazychill.ikafs.webhooks;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class ReceiveMessageRequestHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		try {
			int contentLength = req.getContentLength();

			if (contentLength <= 0) {
				resp.setStatus(IkafsConstants.STATUS_CODE_LENGTH_REQUIRED);
				return;
			}
			
			if (contentLength > IkafsConstants.MAX_JSON_PAYLOAD_BYTES) {
				resp.setStatus(IkafsConstants.STATUS_CODE_REQUEST_ENTITY_TOO_LARGE);
				return;
			}
			
			InputStream input = null;
			Reader reader = null;
			String inputJsonText;
			try {
				byte[] jsonBytes = new byte[contentLength];
				input = req.getInputStream();
				int nextRead;
				int totalRead = 0;
				while (totalRead < contentLength) {
					int remaining = contentLength - totalRead;
					nextRead = (remaining < IkafsConstants.NETWORK_READ_BUFFER_LENGTH) ? (IkafsConstants.NETWORK_READ_BUFFER_LENGTH) : (remaining);
					int read = input.read(jsonBytes, totalRead, nextRead);
					totalRead += read;
				}
				inputJsonText = new String(jsonBytes, IkafsConstants.CHARSET_UTF8);
				JSONObject jsonObj = new JSONObject(inputJsonText);
				
				String cacheKey = UUID.randomUUID().toString();
				MemcacheService ms = MemcacheServiceFactory.getMemcacheService();
				ms.put(cacheKey, jsonObj.toString());
				
				Queue queue = QueueFactory.getQueue(IkafsConstants.QUEUE_NAME_DEFAULT);
				String queueUrl = req.getServletPath() + IkafsConstants.PATH_WEBHOOK_TASK_PUSH;
				queue.add(TaskOptions.Builder.withUrl(queueUrl).method(Method.POST).param(IkafsConstants.QUEUE_PARAM_NAME_CACHE_KEY, cacheKey));
		
				resp.setStatus(200);
				resp.getWriter().write("OK");
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
		catch (JSONException e) {
			e.printStackTrace();
			resp.setStatus(IkafsConstants.STATUS_CODE_BAD_REQUEST);
			try {
				resp.getWriter().write("invalid json");
			} catch (IOException e1) {
				e1.printStackTrace();
				throw new IkafsServletException("error", e1);
			}
			return;
		}
		catch (Exception e) {
			throw new IkafsServletException("error", e);
		}
	}
}
