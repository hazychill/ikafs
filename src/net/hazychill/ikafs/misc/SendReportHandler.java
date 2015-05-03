package net.hazychill.ikafs.misc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.IkafsServletException;
import net.hazychill.ikafs.models.AppEventReport;
import net.hazychill.ikafs.models.AppEventReportMeta;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

public class SendReportHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);

		String path = req.getPathInfo();
		if (IkafsConstants.PATH_MISC_SEND_REPORT.equals(path)) {
			queue(req, resp, logger);
		}
		else if (IkafsConstants.PATH_MISC_TASK_SEND_REPORT.equals(path)) {
			sendReport(resp, logger);
		}
		else {
			resp.setStatus(IkafsConstants.STATUS_CODE_NOT_FOUND);
		}

	}

	private void sendReport(HttpServletResponse resp, Logger logger) throws IkafsServletException {
		try {
			ConfigManager configManager = new ConfigManager();
			int maxEventsInOneReport = configManager.getInt(IkafsConstants.CONFIG_KEY_MAX_EVENTS_IN_ONE_REPORT);
			AppEventReportMeta meta = AppEventReportMeta.get();
			List<AppEventReport> events = Datastore.query(meta).filter(meta.reported.equal(false)).limit(maxEventsInOneReport).asList();
			if (events.size() == 0) {
				resp.setStatus(IkafsConstants.STATUS_CODE_OK);
				try {
					resp.getWriter().write("OK");
				}
				catch (IOException e) {
					e.printStackTrace();
					throw new IkafsServletException(null, e);
				}
				return;
			}

			String fromName = configManager.get(IkafsConstants.CONFIG_KEY_REPORT_SENDER_NAME);
			String fromAddr = configManager.get(IkafsConstants.CONFIG_KEY_REPORT_SENDER_ADDR);
			String to = fromAddr;
			String[] bccList = getBcc(configManager);
			String subject = MessageFormat.format("App Event Report at {0}", formatDateForReport(new Date()));

			StringBuilder msgBody = new StringBuilder();
			for (AppEventReport event : events) {
				String title = event.getTitle();
				Date created = event.getCreated();
				String text = event.getText().getValue();
				msgBody.append("----------------------------------------").append("\r\n");
				msgBody.append(formatDateForReport(created)).append(" ").append(title).append("\r\n");
				msgBody.append("\r\n");
				msgBody.append(text).append("\r\n");

				event.setReported(true);
			}

			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);

			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(fromAddr, fromName));
			msg.addRecipient(RecipientType.TO, new InternetAddress(to));
			for (String bcc : bccList) {
				msg.addRecipient(RecipientType.BCC, new InternetAddress(bcc));
			}
			msg.setSubject(subject);
			msg.setText(msgBody.toString());
			Transport.send(msg);

			logger.info("report sent: " + subject);

			Datastore.put(events);
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
		catch (MessagingException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
	}

	private String[] getBcc(ConfigManager configManager) {
		String bccListStr = configManager.get(IkafsConstants.CONFIG_KEY_REPORT_RECEIVER_EMAILS);
		String[] bccList = bccListStr.split(",");
		return bccList;
	}

	private String formatDateForReport(Date date) {
		SimpleDateFormat format = new SimpleDateFormat(IkafsConstants.DATE_FORMAT_ISO);
		return format.format(date);
	}

	private void queue(HttpServletRequest req, HttpServletResponse resp, Logger logger) throws IkafsServletException {
		Queue queue = QueueFactory.getQueue(IkafsConstants.QUEUE_NAME_DEFAULT);
		String queueUrl = req.getServletPath() + IkafsConstants.PATH_MISC_TASK_SEND_REPORT;
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

}
